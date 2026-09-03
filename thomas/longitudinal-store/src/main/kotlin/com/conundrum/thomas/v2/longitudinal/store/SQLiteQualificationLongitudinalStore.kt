package com.conundrum.thomas.v2.longitudinal.store

import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPlanResult
import com.conundrum.thomas.v2.longitudinal.admission.CanonicalLongitudinalEncoding
import com.conundrum.thomas.v2.longitudinal.admission.LifecycleState
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionPolicy
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAggregateState
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamClass
import java.io.ObjectOutputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant

internal class SQLiteQualificationLongitudinalStore private constructor(
    private val connection: Connection,
    private val location: QualificationStoreLocation,
    private val clock: StoreClock,
    private val faultInjector: StoreFaultInjector,
) : QualificationLongitudinalStore, LongitudinalReader, LongitudinalAdmissionController {
    private val policy = LongitudinalAdmissionPolicy()
    private var closed = false

    override val admission: LongitudinalAdmissionController get() = this
    override val reader: LongitudinalReader get() = this
    override val classification = StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY

    override fun submit(request: LongitudinalAdmissionRequest): AdmissionResult {
        checkOpen()
        val fingerprint = CanonicalLongitudinalEncoding.requestFingerprint(request)
        existingIdempotency(request.idempotencyKey.value)?.let { existing ->
            return if (existing.fingerprint == fingerprint) replayReceipt(existing.eventId)
            else rejectAndAudit(request, AdmissionDisposition.REJECTED_IDEMPOTENCY_CONFLICT, listOf("IDEMPOTENCY_KEY_PAYLOAD_CONFLICT"), fingerprint)
        }

        val recordTime = clock.instant()
        val current = try {
            loadAndVerifyCurrentState()
        } catch (_: RuntimeException) {
            return rejection(AdmissionDisposition.REJECTED_SCHEMA_OR_STORE_STATE, listOf("STORE_STATE_INCONSISTENT"))
        }
        val plan = policy.plan(request, current, RecordTime(recordTime))
        if (plan is AdmissionPlanResult.Rejected) {
            return rejectAndAudit(request, plan.disposition, plan.reasonCodes, fingerprint, recordTime)
        }
        plan as AdmissionPlanResult.Accepted
        val duplicate = immutableObjectKeys(request.operation).firstOrNull(::immutableObjectExists)
        if (duplicate != null) {
            return rejectAndAudit(request, AdmissionDisposition.REJECTED_VALIDATION, listOf("DUPLICATE_STABLE_ID:${duplicate.first}:${duplicate.second}"), fingerprint, recordTime)
        }
        return commitAccepted(request, plan, fingerprint, recordTime)
    }

    private fun commitAccepted(
        request: LongitudinalAdmissionRequest,
        plan: AdmissionPlanResult.Accepted,
        fingerprint: String,
        recordTime: Instant,
    ): AdmissionResult {
        val priorRevision = plan.mutation.state.storeRevision - 1
        val revision = plan.mutation.state.storeRevision
        val eventId = eventId(revision)
        return try {
            connection.autoCommit = false
            connection.prepareStatement(
                """INSERT INTO ledger_event(
                    event_id, store_revision, request_id, idempotency_key, policy_version, operation_type,
                    actor, origin, record_time, payload_fingerprint, operation_payload, affected_stable_ids, decision_trace
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)""".trimIndent(),
            ).use { statement ->
                statement.setString(1, eventId)
                statement.setLong(2, revision)
                statement.setString(3, request.requestId.value)
                statement.setString(4, request.idempotencyKey.value)
                statement.setString(5, request.policyVersion.value)
                statement.setString(6, request.operation.type.name)
                statement.setString(7, request.actor.name)
                statement.setString(8, request.origin.name)
                statement.setString(9, recordTime.toString())
                statement.setString(10, fingerprint)
                statement.setBytes(11, BinaryLongitudinalCodec.encode(request))
                statement.setString(12, encodeList(plan.mutation.affectedStableIds))
                statement.setString(13, encodeList(plan.mutation.decisionTrace))
                statement.executeUpdate()
            }
            faultInjector.check(StoreFaultPoint.AFTER_LEDGER_INSERT)

            writeProjection(plan.mutation.state, eventId)
            writeImmutableObjectIndex(request.operation, revision, eventId)
            writeLifecycleChanges(plan.mutation.state, revision, eventId)
            connection.prepareStatement("INSERT INTO idempotency_record(idempotency_key, logical_fingerprint, ledger_event_id, store_revision) VALUES(?,?,?,?)").use {
                it.setString(1, request.idempotencyKey.value)
                it.setString(2, fingerprint)
                it.setString(3, eventId)
                it.setLong(4, revision)
                it.executeUpdate()
            }
            faultInjector.check(StoreFaultPoint.AFTER_PROJECTION_WRITE)
            faultInjector.check(StoreFaultPoint.BEFORE_COMMIT)
            connection.commit()
            connection.autoCommit = true
            acceptedResult(
                AdmissionDisposition.ACCEPTED,
                request,
                priorRevision,
                revision,
                plan.mutation.affectedStableIds,
                eventId,
                recordTime,
                plan.mutation.decisionTrace,
                fingerprint,
            )
        } catch (failure: Exception) {
            runCatching { connection.rollback() }
            connection.autoCommit = true
            rejection(
                AdmissionDisposition.FAILED_WITHOUT_COMMIT,
                listOf("TRANSACTION_ROLLED_BACK:${failure.javaClass.simpleName}"),
            )
        }
    }

    private fun rejectAndAudit(
        request: LongitudinalAdmissionRequest,
        disposition: AdmissionDisposition,
        reasons: List<String>,
        fingerprint: String,
        recordTime: Instant = clock.instant(),
    ): AdmissionResult = try {
        connection.prepareStatement(
            """INSERT INTO rejection_audit(
                request_id, idempotency_key, policy_version, operation_type, disposition,
                reason_codes, payload_fingerprint, record_time
            ) VALUES(?,?,?,?,?,?,?,?)""".trimIndent(),
        ).use {
            it.setString(1, request.requestId.value)
            it.setString(2, request.idempotencyKey.value)
            it.setString(3, request.policyVersion.value)
            it.setString(4, request.operation.type.name)
            it.setString(5, disposition.name)
            it.setString(6, encodeList(reasons.sorted()))
            it.setString(7, fingerprint)
            it.setString(8, recordTime.toString())
            it.executeUpdate()
        }
        rejection(disposition, reasons.sorted())
    } catch (_: SQLException) {
        rejection(AdmissionDisposition.FAILED_WITHOUT_COMMIT, listOf("REJECTION_AUDIT_FAILED"))
    }

    override fun currentStoreRevision(): Long = loadAndVerifyCurrentState().storeRevision

    override fun snapshot(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot

    override fun source(stableId: SourceIdentityId, asOfRevision: Long) = stateAsOf(asOfRevision).currentSource(stableId)

    override fun sourceRevisionHistory(stableId: SourceIdentityId, asOfRevision: Long) = stateAsOf(asOfRevision).sourceHistory(stableId)

    override fun assertion(id: AssertionId, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.assertions.firstOrNull { it.id == id }

    override fun entity(id: LifeEntityId, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.entities.firstOrNull { it.id == id }

    override fun lifecycle(reference: LongitudinalObjectRef, asOfRevision: Long) = stateAsOf(asOfRevision).lifecycleOf(reference)

    override fun lifecycleHistory(reference: LongitudinalObjectRef): List<LifecycleState> {
        checkOpen()
        return connection.prepareStatement(
            "SELECT lifecycle_status, eligible, cause_code, store_revision FROM lifecycle_history WHERE object_type=? AND stable_id=? ORDER BY store_revision",
        ).use { statement ->
            statement.setString(1, reference.type.name)
            statement.setString(2, reference.stableId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(
                        LifecycleState(
                            com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus.valueOf(rows.getString(1)),
                            rows.getInt(2) == 1,
                            rows.getString(3),
                            rows.getLong(4),
                        ),
                    )
                }
            }
        }
    }

    override fun directDependencies(claim: ClaimReference, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.hypothesisDependencies
        .filter { claim is ClaimReference.Hypothesis && it.dependentHypothesisId == claim.hypothesisId }
        .sortedBy { it.id }

    override fun directDependents(claim: ClaimReference, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.hypothesisDependencies
        .filter { it.prerequisite == claim }.sortedBy { it.id }

    override fun corrections(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.corrections.sortedBy { it.id }

    override fun contradictions(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.contradictions.sortedBy { it.id }

    override fun supersessions(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.supersessions.sortedBy { it.id }

    override fun identityLinks(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.identityLinks.sortedBy { it.id }

    override fun identityDecisionHistory(left: LifeEntityId, right: LifeEntityId): List<EntityIdentityLink> =
        ledgerRows().flatMap { row ->
            when (val operation = row.request.operation) {
                is LongitudinalWriteOperation.ReviseIdentityLink -> listOf(operation.decision)
                is LongitudinalWriteOperation.AdmitEvidenceBundle -> operation.bundle.identityLinks
                else -> emptyList()
            }
        }.filter { link -> setOf(link.leftEntityId, link.rightEntityId) == setOf(left, right) }

    override fun coverage(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.coverageTopics.sortedBy { it.id }

    override fun isEligible(reference: LongitudinalObjectRef, asOfRevision: Long): Boolean =
        stateAsOf(asOfRevision).lifecycleOf(reference)?.eligibleForOrdinaryUse == true

    override fun redactedAdmissionHistory(): List<RedactedAdmissionHistoryEntry> {
        checkOpen()
        val accepted = connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT store_revision, request_id, idempotency_key, operation_type, policy_version, decision_trace, payload_fingerprint, record_time FROM ledger_event ORDER BY store_revision",
            ).use { rows ->
                buildList {
                    while (rows.next()) add(
                        RedactedAdmissionHistoryEntry(
                            rows.getLong(1), rows.getLong(1), rows.getString(2), rows.getString(3),
                            com.conundrum.thomas.v2.longitudinal.admission.AdmissionOperationType.valueOf(rows.getString(4)),
                            AdmissionDisposition.ACCEPTED, rows.getString(5), decodeList(rows.getString(6)), rows.getString(7), Instant.parse(rows.getString(8)),
                        ),
                    )
                }
            }
        }
        val offset = accepted.size.toLong()
        val rejected = connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT rejection_sequence, request_id, idempotency_key, operation_type, disposition, policy_version, reason_codes, payload_fingerprint, record_time FROM rejection_audit ORDER BY rejection_sequence",
            ).use { rows ->
                buildList {
                    while (rows.next()) add(
                        RedactedAdmissionHistoryEntry(
                            offset + rows.getLong(1), null, rows.getString(2), rows.getString(3),
                            com.conundrum.thomas.v2.longitudinal.admission.AdmissionOperationType.valueOf(rows.getString(4)),
                            AdmissionDisposition.valueOf(rows.getString(5)), rows.getString(6), decodeList(rows.getString(7)), rows.getString(8), Instant.parse(rows.getString(9)),
                        ),
                    )
                }
            }
        }
        return accepted + rejected
    }

    override fun canonicalLogicalStateDigest(asOfRevision: Long): String = CanonicalLongitudinalEncoding.stateDigest(stateAsOf(asOfRevision))

    override fun replayIntoEmpty(location: QualificationStoreLocation): QualificationLongitudinalStore {
        checkOpen()
        val target = open(location, StoreClock { throw IllegalStateException("Replay never consults the clock") }, StoreFaultInjector { })
        check(target.currentStoreRevision() == 0L) { "Replay target must be empty" }
        ledgerRows().forEach { target.commitReplay(it) }
        if (target.canonicalLogicalStateDigest() != canonicalLogicalStateDigest()) {
            target.close()
            throw InconsistentLongitudinalStoreException("Replay digest mismatch")
        }
        return target
    }

    private fun commitReplay(row: LedgerRow) {
        val current = loadAndVerifyCurrentState()
        val plan = policy.plan(row.request, current, RecordTime(row.recordTime)) as? AdmissionPlanResult.Accepted
            ?: throw InconsistentLongitudinalStoreException("Ledger event cannot be replayed under its policy")
        if (CanonicalLongitudinalEncoding.requestFingerprint(row.request) != row.fingerprint) {
            throw InconsistentLongitudinalStoreException("Ledger payload fingerprint mismatch")
        }
        connection.autoCommit = false
        try {
            connection.prepareStatement(
                """INSERT INTO ledger_event(event_id, store_revision, request_id, idempotency_key, policy_version,
                    operation_type, actor, origin, record_time, payload_fingerprint, operation_payload, affected_stable_ids, decision_trace)
                    VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)""".trimIndent(),
            ).use {
                it.setString(1, row.eventId); it.setLong(2, row.revision); it.setString(3, row.request.requestId.value)
                it.setString(4, row.request.idempotencyKey.value); it.setString(5, row.request.policyVersion.value)
                it.setString(6, row.request.operation.type.name); it.setString(7, row.request.actor.name); it.setString(8, row.request.origin.name)
                it.setString(9, row.recordTime.toString()); it.setString(10, row.fingerprint); it.setBytes(11, BinaryLongitudinalCodec.encode(row.request))
                it.setString(12, encodeList(plan.mutation.affectedStableIds)); it.setString(13, encodeList(plan.mutation.decisionTrace)); it.executeUpdate()
            }
            writeProjection(plan.mutation.state, row.eventId)
            writeImmutableObjectIndex(row.request.operation, row.revision, row.eventId)
            writeLifecycleChanges(plan.mutation.state, row.revision, row.eventId)
            connection.prepareStatement("INSERT INTO idempotency_record VALUES(?,?,?,?)").use {
                it.setString(1, row.request.idempotencyKey.value); it.setString(2, row.fingerprint); it.setString(3, row.eventId); it.setLong(4, row.revision); it.executeUpdate()
            }
            connection.commit()
        } catch (failure: Exception) {
            connection.rollback()
            throw InconsistentLongitudinalStoreException("Replay transaction failed: ${failure.javaClass.simpleName}")
        } finally {
            connection.autoCommit = true
        }
    }

    private fun stateAsOf(revision: Long): LongitudinalAggregateState {
        checkOpen()
        val currentRevision = rawCurrentRevision()
        require(revision in 0..currentRevision) { "Requested revision is outside durable history" }
        if (revision == currentRevision) return loadAndVerifyCurrentState()
        var state = LongitudinalAggregateState()
        ledgerRows(revision).forEach { row ->
            if (CanonicalLongitudinalEncoding.requestFingerprint(row.request) != row.fingerprint) {
                throw InconsistentLongitudinalStoreException("Ledger payload fingerprint mismatch")
            }
            val result = policy.plan(row.request, state, RecordTime(row.recordTime)) as? AdmissionPlanResult.Accepted
                ?: throw InconsistentLongitudinalStoreException("Ledger event cannot reconstruct state")
            state = result.mutation.state
        }
        return state
    }

    private fun loadAndVerifyCurrentState(): LongitudinalAggregateState {
        checkOpen()
        val state = connection.createStatement().use { statement ->
            statement.executeQuery("SELECT store_revision, state_payload, canonical_digest, ledger_event_id FROM current_state_projection WHERE singleton_id=1").use { rows ->
                if (!rows.next()) throw InconsistentLongitudinalStoreException("Current projection is missing")
                val revision = rows.getLong(1)
                val decoded = BinaryLongitudinalCodec.decode<LongitudinalAggregateState>(rows.getBytes(2))
                val digest = rows.getString(3)
                val eventId = rows.getString(4)
                if (decoded.storeRevision != revision || CanonicalLongitudinalEncoding.stateDigest(decoded) != digest) {
                    throw InconsistentLongitudinalStoreException("Current projection digest mismatch")
                }
                if ((revision == 0L && eventId != null) || (revision > 0 && eventId != eventId(revision))) {
                    throw InconsistentLongitudinalStoreException("Projection ledger anchor mismatch")
                }
                decoded
            }
        }
        val maxLedger = connection.createStatement().use { it.executeQuery("SELECT COALESCE(MAX(store_revision),0) FROM ledger_event").use { rows -> rows.next(); rows.getLong(1) } }
        if (maxLedger != state.storeRevision) throw InconsistentLongitudinalStoreException("Ledger and projection revisions differ")
        return state
    }

    private fun writeProjection(state: LongitudinalAggregateState, eventId: String) {
        val payload = BinaryLongitudinalCodec.encode(state)
        val digest = CanonicalLongitudinalEncoding.stateDigest(state)
        connection.prepareStatement("UPDATE current_state_projection SET store_revision=?, state_payload=?, canonical_digest=?, ledger_event_id=? WHERE singleton_id=1").use {
            it.setLong(1, state.storeRevision); it.setBytes(2, payload); it.setString(3, digest); it.setString(4, eventId)
            check(it.executeUpdate() == 1) { "Current projection row is missing" }
        }
    }

    private fun writeLifecycleChanges(state: LongitudinalAggregateState, revision: Long, eventId: String) {
        state.lifecycle.filterValues { it.changedAtRevision == revision }.forEach { (reference, lifecycle) ->
            connection.prepareStatement("INSERT INTO lifecycle_history(object_type, stable_id, store_revision, lifecycle_status, eligible, cause_code, ledger_event_id) VALUES(?,?,?,?,?,?,?)").use {
                it.setString(1, reference.type.name); it.setString(2, reference.stableId); it.setLong(3, revision)
                it.setString(4, lifecycle.status.name); it.setInt(5, if (lifecycle.eligibleForOrdinaryUse) 1 else 0)
                it.setString(6, lifecycle.causeCode); it.setString(7, eventId); it.executeUpdate()
            }
        }
    }

    private fun writeImmutableObjectIndex(operation: LongitudinalWriteOperation, revision: Long, eventId: String) {
        immutableObjectKeys(operation).forEach { (type, id) ->
            connection.prepareStatement("INSERT INTO immutable_object_index(object_type, stable_id, first_store_revision, ledger_event_id) VALUES(?,?,?,?)").use {
                it.setString(1, type); it.setString(2, id); it.setLong(3, revision); it.setString(4, eventId); it.executeUpdate()
            }
        }
    }

    private fun immutableObjectKeys(operation: LongitudinalWriteOperation): List<Pair<String, String>> = when (operation) {
        is LongitudinalWriteOperation.AdmitSource -> listOf("SOURCE_IDENTITY" to operation.source.stableSourceId.value, "SOURCE_REVISION" to operation.source.revisionId.value)
        is LongitudinalWriteOperation.AppendSourceRevision -> listOf("SOURCE_REVISION" to operation.newRevisionId.value)
        is LongitudinalWriteOperation.AdmitEvidenceBundle -> with(operation.bundle) {
            assertions.map { "ASSERTION" to it.id.value } + entities.map { "ENTITY" to it.id.value } +
                contradictions.map { "CONTRADICTION" to it.id.value } + supersessions.map { "SUPERSESSION" to it.id.value } +
                hypothesisDrafts.map { "HYPOTHESIS" to it.id.value } + hypothesisDependencies.map { "DEPENDENCY" to it.id.value } +
                identityLinks.map { "IDENTITY_DECISION" to it.id.value } + coverageTopics.map { "COVERAGE" to it.id.value }
        }
        is LongitudinalWriteOperation.RecordUserCorrection -> listOf(
            "SOURCE_IDENTITY" to operation.correctionSource.stableSourceId.value,
            "SOURCE_REVISION" to operation.correctionSource.revisionId.value,
            "ASSERTION" to operation.correctingAssertion.id.value,
            "CORRECTION" to operation.correction.id.value,
        ) + listOfNotNull(operation.supersession?.let { "SUPERSESSION" to it.id.value })
        is LongitudinalWriteOperation.RecordSupersession -> listOf("SUPERSESSION" to operation.relation.id.value)
        is LongitudinalWriteOperation.RecordContradiction -> listOf("CONTRADICTION" to operation.relation.id.value)
        is LongitudinalWriteOperation.ReviseIdentityLink -> listOf("IDENTITY_DECISION" to operation.decision.id.value)
        is LongitudinalWriteOperation.ChangeCoverage,
        is LongitudinalWriteOperation.ChangePrivacy,
        is LongitudinalWriteOperation.RetireClaim -> emptyList()
    }

    private fun immutableObjectExists(key: Pair<String, String>): Boolean = connection.prepareStatement(
        "SELECT 1 FROM immutable_object_index WHERE object_type=? AND stable_id=?",
    ).use {
        it.setString(1, key.first); it.setString(2, key.second); it.executeQuery().use(ResultSet::next)
    }

    private fun existingIdempotency(key: String): IdempotencyRow? = connection.prepareStatement(
        "SELECT logical_fingerprint, ledger_event_id FROM idempotency_record WHERE idempotency_key=?",
    ).use {
        it.setString(1, key); it.executeQuery().use { rows -> if (rows.next()) IdempotencyRow(rows.getString(1), rows.getString(2)) else null }
    }

    private fun replayReceipt(existingEventId: String): AdmissionResult = connection.prepareStatement(
        """SELECT request_id, idempotency_key, policy_version, operation_type, store_revision,
            affected_stable_ids, record_time, decision_trace, payload_fingerprint
            FROM ledger_event WHERE event_id=?""".trimIndent(),
    ).use { statement ->
        statement.setString(1, existingEventId)
        statement.executeQuery().use { row ->
            check(row.next())
            val request = BinaryLongitudinalCodec.decode<LongitudinalAdmissionRequest>(
                connection.prepareStatement("SELECT operation_payload FROM ledger_event WHERE event_id=?").use {
                    it.setString(1, existingEventId); it.executeQuery().use { payload -> payload.next(); payload.getBytes(1) }
                },
            )
            acceptedResult(
                AdmissionDisposition.IDEMPOTENT_REPLAY, request, row.getLong(5) - 1, row.getLong(5), decodeList(row.getString(6)),
                existingEventId, Instant.parse(row.getString(7)), decodeList(row.getString(8)), row.getString(9),
            )
        }
    }

    private fun ledgerRows(upToRevision: Long = Long.MAX_VALUE): List<LedgerRow> = connection.prepareStatement(
        "SELECT event_id, store_revision, record_time, payload_fingerprint, operation_payload FROM ledger_event WHERE store_revision<=? ORDER BY store_revision",
    ).use {
        it.setLong(1, upToRevision)
        it.executeQuery().use { rows ->
            buildList {
                while (rows.next()) add(
                    LedgerRow(rows.getString(1), rows.getLong(2), Instant.parse(rows.getString(3)), rows.getString(4), BinaryLongitudinalCodec.decode(rows.getBytes(5))),
                )
            }
        }
    }

    private fun rawCurrentRevision(): Long = connection.createStatement().use {
        it.executeQuery("SELECT store_revision FROM current_state_projection WHERE singleton_id=1").use { row ->
            if (!row.next()) throw InconsistentLongitudinalStoreException("Current projection is missing")
            row.getLong(1)
        }
    }

    override fun close() {
        if (!closed) {
            closed = true
            connection.close()
        }
    }

    private fun checkOpen() = check(!closed) { "Qualification store is closed" }

    companion object {
        fun open(location: QualificationStoreLocation, clock: StoreClock, faultInjector: StoreFaultInjector): SQLiteQualificationLongitudinalStore {
            Class.forName("org.sqlite.JDBC")
            val connection = DriverManager.getConnection(location.jdbcUrl)
            try {
                connection.createStatement().use { it.execute("PRAGMA foreign_keys=ON") }
                val foreignKeys = connection.createStatement().use { it.executeQuery("PRAGMA foreign_keys").use { row -> row.next(); row.getInt(1) } }
                if (foreignKeys != 1) throw UnsupportedStoreSchemaException("Foreign keys could not be enabled")
                initializeOrVerifySchema(connection)
                return SQLiteQualificationLongitudinalStore(connection, location, clock, faultInjector).also { it.loadAndVerifyCurrentState() }
            } catch (failure: Exception) {
                connection.close()
                throw failure
            }
        }

        private fun initializeOrVerifySchema(connection: Connection) {
            val version = connection.createStatement().use { it.executeQuery("PRAGMA user_version").use { row -> row.next(); row.getInt(1) } }
            if (version == 0) {
                val existingTables = connection.createStatement().use {
                    it.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'").use { row -> row.next(); row.getInt(1) }
                }
                if (existingTables != 0) throw UnsupportedStoreSchemaException("Unversioned non-empty database is not supported")
                connection.autoCommit = false
                try {
                    LongitudinalStoreSchema.statements.forEach { sql -> connection.createStatement().use { it.execute(sql) } }
                    connection.prepareStatement("INSERT INTO store_metadata(key,value) VALUES(?,?),(?,?),(?,?)").use {
                        it.setString(1, "schema_version"); it.setString(2, LongitudinalStoreSchema.VERSION.toString())
                        it.setString(3, "schema_fingerprint"); it.setString(4, LongitudinalStoreSchema.fingerprint)
                        it.setString(5, "data_classification"); it.setString(6, LongitudinalStoreSchema.CLASSIFICATION)
                        it.executeUpdate()
                    }
                    val initial = LongitudinalAggregateState()
                    connection.prepareStatement("INSERT INTO current_state_projection(singleton_id,store_revision,state_payload,canonical_digest,ledger_event_id) VALUES(1,0,?,?,NULL)").use {
                        it.setBytes(1, BinaryLongitudinalCodec.encode(initial)); it.setString(2, CanonicalLongitudinalEncoding.stateDigest(initial)); it.executeUpdate()
                    }
                    connection.createStatement().use { it.execute("PRAGMA user_version=${LongitudinalStoreSchema.VERSION}") }
                    connection.commit()
                } catch (failure: Exception) {
                    connection.rollback()
                    throw failure
                } finally {
                    connection.autoCommit = true
                }
            } else if (version != LongitudinalStoreSchema.VERSION) {
                throw UnsupportedStoreSchemaException("Unsupported longitudinal schema version")
            }
            val metadata = connection.createStatement().use {
                it.executeQuery("SELECT key,value FROM store_metadata").use { rows -> buildMap { while (rows.next()) put(rows.getString(1), rows.getString(2)) } }
            }
            if (metadata["schema_version"] != LongitudinalStoreSchema.VERSION.toString() ||
                metadata["schema_fingerprint"] != LongitudinalStoreSchema.fingerprint ||
                metadata["data_classification"] != LongitudinalStoreSchema.CLASSIFICATION) {
                throw UnsupportedStoreSchemaException("Longitudinal schema metadata mismatch")
            }
        }
    }
}

private data class IdempotencyRow(val fingerprint: String, val eventId: String)
private data class LedgerRow(
    val eventId: String,
    val revision: Long,
    val recordTime: Instant,
    val fingerprint: String,
    val request: LongitudinalAdmissionRequest,
)

private data class StoreAcceptedReceipt(
    override val requestId: com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId,
    override val idempotencyKey: com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey,
    override val admissionPolicyVersion: com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion,
    override val operationType: com.conundrum.thomas.v2.longitudinal.admission.AdmissionOperationType,
    override val priorStoreRevision: Long,
    override val resultingStoreRevision: Long,
    override val affectedStableIds: List<String>,
    override val ledgerEventIds: List<String>,
    override val recordTime: Instant,
    override val decisionTrace: List<String>,
    override val payloadFingerprint: String,
) : AcceptedAdmissionReceipt

private data class StoreAdmissionResult(
    override val disposition: AdmissionDisposition,
    override val receipt: AcceptedAdmissionReceipt?,
    override val reasonCodes: List<String>,
) : AdmissionResult

private fun acceptedResult(
    disposition: AdmissionDisposition,
    request: LongitudinalAdmissionRequest,
    priorRevision: Long,
    revision: Long,
    affected: List<String>,
    eventId: String,
    recordTime: Instant,
    trace: List<String>,
    fingerprint: String,
) = StoreAdmissionResult(
    disposition,
    StoreAcceptedReceipt(
        request.requestId, request.idempotencyKey, request.policyVersion, request.operation.type,
        priorRevision, revision, affected, listOf(eventId), recordTime, trace, fingerprint,
    ),
    emptyList(),
)

private fun rejection(disposition: AdmissionDisposition, reasons: List<String>) = StoreAdmissionResult(disposition, null, reasons)
private fun eventId(revision: Long) = "ledger-${revision.toString().padStart(20, '0')}"
private fun encodeList(values: List<String>) = values.joinToString("|")
private fun decodeList(value: String) = if (value.isBlank()) emptyList() else value.split('|')

private object BinaryLongitudinalCodec {
    fun encode(value: Any): ByteArray = ByteArrayOutputStream().use { bytes ->
        ObjectOutputStream(bytes).use { it.writeObject(value) }
        bytes.toByteArray()
    }

    inline fun <reified T> decode(bytes: ByteArray): T = decodeValue(bytes) as T

    fun decodeValue(bytes: ByteArray): Any = WhitelistedObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }
}

private class WhitelistedObjectInputStream(input: ByteArrayInputStream) : ObjectInputStream(input) {
    override fun resolveClass(desc: ObjectStreamClass): Class<*> {
        val name = desc.name
        val allowed = name.startsWith("com.conundrum.thomas.v2.longitudinal.") ||
            name.startsWith("java.lang.") || name.startsWith("java.util.") || name.startsWith("java.time.") ||
            name.startsWith("kotlin.") || name.startsWith("[")
        if (!allowed) throw InconsistentLongitudinalStoreException("Persisted payload class is not permitted")
        return super.resolveClass(desc)
    }
}
