package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPlanResult
import com.conundrum.thomas.v2.longitudinal.admission.CanonicalLongitudinalEncoding
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionPolicy
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.store.AcceptedAdmissionReceipt
import com.conundrum.thomas.v2.longitudinal.store.AdmissionResult
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalAdmissionController
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalReader
import com.conundrum.thomas.v2.longitudinal.store.RedactedAdmissionHistoryEntry
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import java.security.SecureRandom
import java.time.Instant

object ProtectedPersonalDataStoreFactory {
    fun open(
        storage: ProtectedArtifactStorage,
        keyProvider: PersonalDataKeyProvider,
        clock: StoreClock,
        random: SecureRandom = SecureRandom(),
        faultInjector: PersistenceFaultInjector = PersistenceFaultInjector { },
    ): PersonalDataOpenResult = try {
        val protectedBytes = storage.read()
        if (protectedBytes == null) {
            val document = PersonalDataDocumentCodec.empty()
            val store = ProtectedPersonalDataStoreImpl(storage, keyProvider, clock, random, faultInjector, document)
            store.persistInitial()
            PersonalDataOpenResult.Opened(store, PersonalDataOpenDisposition.OPENED_EMPTY)
        } else {
            val key = try { keyProvider.existing() } catch (failure: Exception) {
                throw PersonalDataPersistenceException(PersonalDataFailureDisposition.KEY_UNAVAILABLE, "PRIMARY_KEY_UNAVAILABLE", failure)
            }
            val plaintext = AuthenticatedProtection.unprotect(
                protectedBytes,
                key,
                ProtectedArtifactPurpose.PRIMARY_STORE,
                keyProvider.descriptor.alias,
            )
            val verified = PersonalDataDocumentCodec.decodeAndVerify(plaintext)
            val store = ProtectedPersonalDataStoreImpl(storage, keyProvider, clock, random, faultInjector, verified.document)
            if (verified.migrated) {
                faultInjector.check(PersistenceFaultPoint.BEFORE_MIGRATION_COMMIT)
                store.persistCurrentDocument()
            } else if (verified.projectionRebuilt) {
                faultInjector.check(PersistenceFaultPoint.BEFORE_PROJECTION_REBUILD_COMMIT)
                store.persistCurrentDocument()
            }
            val disposition = when {
                verified.migrated -> PersonalDataOpenDisposition.OPENED_AFTER_SCHEMA_MIGRATION
                verified.projectionRebuilt -> PersonalDataOpenDisposition.OPENED_AFTER_PROJECTION_REBUILD
                else -> PersonalDataOpenDisposition.OPENED_CURRENT
            }
            PersonalDataOpenResult.Opened(store, disposition)
        }
    } catch (failure: PersonalDataPersistenceException) {
        PersonalDataOpenResult.Unavailable(failure.disposition, failure.reasonCode)
    } catch (_: Exception) {
        PersonalDataOpenResult.Unavailable(PersonalDataFailureDisposition.STORAGE_UNAVAILABLE, "PRIMARY_STORAGE_UNAVAILABLE")
    }

    fun restoreIntoEmpty(
        artifact: ProtectedBackupArtifact,
        recoveryKey: RecoveryKey,
        storage: ProtectedArtifactStorage,
        keyProvider: PersonalDataKeyProvider,
        clock: StoreClock,
        random: SecureRandom = SecureRandom(),
        faultInjector: PersistenceFaultInjector = PersistenceFaultInjector { },
    ): Result<RestoreResult> = runCatching {
        if (storage.exists()) throw PersonalDataPersistenceException(
            PersonalDataFailureDisposition.NON_EMPTY_RESTORE_TARGET,
            "RESTORE_TARGET_MUST_BE_EMPTY",
        )
        val plaintext = try {
            AuthenticatedProtection.unprotect(
                artifact.encryptedPayload(),
                recoveryKey.secretKey(),
                ProtectedArtifactPurpose.LOCAL_BACKUP,
                BACKUP_KEY_ALIAS,
            )
        } catch (failure: PersonalDataPersistenceException) {
            throw PersonalDataPersistenceException(
                if (failure.disposition == PersonalDataFailureDisposition.AUTHENTICATION_OR_INTEGRITY_FAILURE)
                    PersonalDataFailureDisposition.BACKUP_INTEGRITY_FAILURE else failure.disposition,
                "BACKUP_RESTORE_${failure.reasonCode}",
                failure,
            )
        }
        val verified = PersonalDataDocumentCodec.decodeAndVerify(plaintext)
        if (verified.document.projection.storeRevision != artifact.sourceStoreRevision) {
            throw PersonalDataPersistenceException(
                PersonalDataFailureDisposition.BACKUP_INTEGRITY_FAILURE,
                "BACKUP_SOURCE_REVISION_MISMATCH",
            )
        }
        faultInjector.check(PersistenceFaultPoint.BEFORE_RESTORE_COMMIT)
        val store = ProtectedPersonalDataStoreImpl(storage, keyProvider, clock, random, faultInjector, verified.document)
        store.persistInitial()
        RestoreResult(store, verified.document.projection.storeRevision, verified.document.projectionDigest, verified.projectionRebuilt)
    }

    internal const val BACKUP_KEY_ALIAS = "ct-v2-14.user-recovery-key"
}

internal class ProtectedPersonalDataStoreImpl(
    private val storage: ProtectedArtifactStorage,
    private val keyProvider: PersonalDataKeyProvider,
    private val clock: StoreClock,
    private val random: SecureRandom,
    private val faultInjector: PersistenceFaultInjector,
    initialDocument: PersonalDataDocumentV2,
) : ProtectedPersonalDataStore, LongitudinalAdmissionController, LongitudinalReader {
    private val policy = LongitudinalAdmissionPolicy(StoreDataClassification.PROTECTED_PERSONAL_DATA)
    private var document: PersonalDataDocumentV2? = initialDocument
    private var closed = false

    override val admission: LongitudinalAdmissionController get() = this
    override val reader: LongitudinalReader get() = this
    override val keyDescriptor: PersonalDataKeyDescriptor get() = keyProvider.descriptor
    override val schemaVersion: Int get() = CT_V2_14_STORE_SCHEMA_VERSION
    override val classification = StoreDataClassification.PROTECTED_PERSONAL_DATA

    internal fun persistInitial() {
        val key = try { keyProvider.getOrCreate() } catch (failure: Exception) {
            throw PersonalDataPersistenceException(PersonalDataFailureDisposition.KEY_UNAVAILABLE, "PRIMARY_KEY_CREATION_FAILED", failure)
        }
        persist(requireDocument(), key)
    }

    internal fun persistCurrentDocument() {
        val key = try { keyProvider.existing() } catch (failure: Exception) {
            throw PersonalDataPersistenceException(PersonalDataFailureDisposition.KEY_UNAVAILABLE, "PRIMARY_KEY_UNAVAILABLE", failure)
        }
        persist(requireDocument(), key)
    }

    @Synchronized
    override fun submit(request: LongitudinalAdmissionRequest): AdmissionResult {
        checkOpen()
        val currentDocument = requireDocument()
        val fingerprint = CanonicalLongitudinalEncoding.requestFingerprint(request)
        currentDocument.receipts[request.idempotencyKey.value]?.let { receipt ->
            return if (receipt.payloadFingerprint == fingerprint) result(AdmissionDisposition.IDEMPOTENT_REPLAY, receipt)
            else rejection(AdmissionDisposition.REJECTED_IDEMPOTENCY_CONFLICT, "IDEMPOTENCY_KEY_PAYLOAD_CONFLICT")
        }
        val current = currentDocument.projection
        val recordTime = clock.instant()
        val planned = policy.plan(request, current, RecordTime(recordTime))
        val accepted = planned as? AdmissionPlanResult.Accepted
            ?: return (planned as AdmissionPlanResult.Rejected).let { rejection(it.disposition, *it.reasonCodes.toTypedArray()) }
        val revision = accepted.mutation.state.storeRevision
        val eventId = "personal-ledger-${revision.toString().padStart(20, '0')}"
        val previousDigest = PersonalDataDocumentCodec.chainAnchor(currentDocument)
        val unsigned = DurableLedgerEvent(
            eventId, revision, recordTime, request, fingerprint, accepted.mutation.affectedStableIds,
            accepted.mutation.decisionTrace, previousDigest, "",
        )
        val event = unsigned.copy(eventDigest = PersonalDataDocumentCodec.eventDigest(unsigned, previousDigest))
        val receipt = DurableReceipt(
            request.requestId.value, request.idempotencyKey.value, request.policyVersion.value,
            request.operation.type, current.storeRevision, revision, accepted.mutation.affectedStableIds,
            eventId, recordTime, accepted.mutation.decisionTrace, fingerprint,
        )
        val candidate = if (request.operation is LongitudinalWriteOperation.DeleteSource) {
            currentDocument.copy(
                checkpoint = accepted.mutation.state,
                archivedLedger = currentDocument.archivedLedger +
                    currentDocument.activeLedger.map { PersonalDataDocumentCodec.archive(it, "SUPERSEDED_BY_DELETION_CHECKPOINT") } +
                    PersonalDataDocumentCodec.archive(event, "USER_SOURCE_DELETION_CHECKPOINT"),
                activeLedger = emptyList(),
                receipts = currentDocument.receipts + (request.idempotencyKey.value to receipt),
                projection = accepted.mutation.state,
                projectionDigest = CanonicalLongitudinalEncoding.stateDigest(accepted.mutation.state),
            )
        } else {
            currentDocument.copy(
                activeLedger = currentDocument.activeLedger + event,
                receipts = currentDocument.receipts + (request.idempotencyKey.value to receipt),
                projection = accepted.mutation.state,
                projectionDigest = CanonicalLongitudinalEncoding.stateDigest(accepted.mutation.state),
            )
        }
        return try {
            faultInjector.check(PersistenceFaultPoint.BEFORE_PROTECTION)
            val plaintext = PersonalDataDocumentCodec.encode(candidate)
            val protected = AuthenticatedProtection.protect(
                plaintext, keyProvider.existing(), ProtectedArtifactPurpose.PRIMARY_STORE,
                keyProvider.descriptor.alias, random,
            )
            faultInjector.check(PersistenceFaultPoint.AFTER_PROTECTION_BEFORE_ATOMIC_WRITE)
            storage.writeAtomically(protected)
            document = candidate
            faultInjector.check(PersistenceFaultPoint.AFTER_ATOMIC_WRITE)
            result(AdmissionDisposition.ACCEPTED, receipt)
        } catch (_: Exception) {
            recoverAfterWriteFailure(request.idempotencyKey.value, fingerprint, receipt)
        }
    }

    private fun recoverAfterWriteFailure(key: String, fingerprint: String, receipt: DurableReceipt): AdmissionResult = try {
        val protected = storage.read()
            ?: return rejection(AdmissionDisposition.FAILED_WITHOUT_COMMIT, "ATOMIC_WRITE_NOT_COMMITTED")
        val plaintext = AuthenticatedProtection.unprotect(
            protected, keyProvider.existing(), ProtectedArtifactPurpose.PRIMARY_STORE, keyProvider.descriptor.alias,
        )
        val recovered = PersonalDataDocumentCodec.decodeAndVerify(plaintext).document
        document = recovered
        if (recovered.receipts[key]?.payloadFingerprint == fingerprint) result(AdmissionDisposition.ACCEPTED, receipt)
        else rejection(AdmissionDisposition.FAILED_WITHOUT_COMMIT, "ATOMIC_WRITE_NOT_COMMITTED")
    } catch (_: Exception) {
        rejection(AdmissionDisposition.FAILED_WITHOUT_COMMIT, "PERSISTENCE_FAILURE_WITHOUT_TRUSTED_COMMIT")
    }

    override fun currentStoreRevision(): Long = requireDocument().projection.storeRevision
    override fun snapshot(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot
    override fun source(stableId: SourceIdentityId, asOfRevision: Long) = stateAsOf(asOfRevision).currentSource(stableId)
    override fun sourceRevisionHistory(stableId: SourceIdentityId, asOfRevision: Long) = stateAsOf(asOfRevision).sourceHistory(stableId)
    override fun assertion(id: AssertionId, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.assertions.firstOrNull { it.id == id }
    override fun entity(id: LifeEntityId, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.entities.firstOrNull { it.id == id }
    override fun lifecycle(reference: LongitudinalObjectRef, asOfRevision: Long) = stateAsOf(asOfRevision).lifecycle[reference]

    override fun lifecycleHistory(reference: LongitudinalObjectRef): List<com.conundrum.thomas.v2.longitudinal.admission.LifecycleState> {
        val doc = requireDocument()
        val values = mutableListOf<com.conundrum.thomas.v2.longitudinal.admission.LifecycleState>()
        doc.checkpoint.lifecycle[reference]?.let(values::add)
        doc.activeLedger.indices.forEach { index ->
            val partial = doc.copy(activeLedger = doc.activeLedger.take(index + 1))
            PersonalDataDocumentCodec.reconstructCurrent(partial).lifecycle[reference]?.let(values::add)
        }
        return values.distinctBy { listOf(it.changedAtRevision, it.status, it.causeCode) }.sortedBy { it.changedAtRevision }
    }

    override fun directDependencies(claim: ClaimReference, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.hypothesisDependencies
        .filter { claim is ClaimReference.Hypothesis && it.dependentHypothesisId == claim.hypothesisId }.sortedBy { it.id }
    override fun directDependents(claim: ClaimReference, asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.hypothesisDependencies
        .filter { it.prerequisite == claim }.sortedBy { it.id }
    override fun corrections(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.corrections.sortedBy { it.id }
    override fun contradictions(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.contradictions.sortedBy { it.id }
    override fun supersessions(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.supersessions.sortedBy { it.id }
    override fun identityLinks(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.identityLinks.sortedBy { it.id }

    override fun identityDecisionHistory(left: LifeEntityId, right: LifeEntityId): List<com.conundrum.thomas.v2.longitudinal.EntityIdentityLink> {
        val pair = if (left < right) left to right else right to left
        val doc = requireDocument()
        val states = listOf(doc.checkpoint) + doc.activeLedger.indices.map { index ->
            PersonalDataDocumentCodec.reconstructCurrent(doc.copy(activeLedger = doc.activeLedger.take(index + 1)))
        }
        return states.flatMap { it.snapshot.identityLinks }
            .filter { it.canonicalPair() == pair }.distinctBy { it.id }
    }

    override fun coverage(asOfRevision: Long) = stateAsOf(asOfRevision).snapshot.coverageTopics.sortedBy { it.id }
    override fun isEligible(reference: LongitudinalObjectRef, asOfRevision: Long) =
        stateAsOf(asOfRevision).lifecycle[reference]?.eligibleForOrdinaryUse == true

    override fun redactedAdmissionHistory(): List<RedactedAdmissionHistoryEntry> {
        val doc = requireDocument()
        val archived = doc.archivedLedger.mapIndexed { index, event ->
            RedactedAdmissionHistoryEntry(
                (index + 1).toLong(), event.revision, event.requestId, event.idempotencyKey, event.operationType,
                AdmissionDisposition.ACCEPTED, event.policyVersion, listOf(event.erasureReason), event.payloadFingerprint, event.recordTime,
            )
        }
        val active = doc.activeLedger.mapIndexed { index, event ->
            RedactedAdmissionHistoryEntry(
                (archived.size + index + 1).toLong(), event.revision, event.request.requestId.value,
                event.request.idempotencyKey.value, event.request.operation.type, AdmissionDisposition.ACCEPTED,
                event.request.policyVersion.value, emptyList(), event.payloadFingerprint, event.recordTime,
            )
        }
        return archived + active
    }

    override fun canonicalLogicalStateDigest(asOfRevision: Long): String =
        CanonicalLongitudinalEncoding.stateDigest(stateAsOf(asOfRevision))

    override fun export(): PersonalDataExport {
        checkOpen()
        return PersonalDataExportRenderer.render(requireDocument().projection, clock.instant())
    }

    override fun createProtectedBackup(key: RecoveryKey): ProtectedBackupArtifact {
        checkOpen()
        faultInjector.check(PersistenceFaultPoint.BEFORE_BACKUP_PROTECTION)
        val plaintext = PersonalDataDocumentCodec.encode(requireDocument())
        val protected = AuthenticatedProtection.protect(
            plaintext, key.secretKey(), ProtectedArtifactPurpose.LOCAL_BACKUP,
            ProtectedPersonalDataStoreFactory.BACKUP_KEY_ALIAS, random,
        )
        faultInjector.check(PersistenceFaultPoint.AFTER_BACKUP_PROTECTION)
        return ProtectedBackupArtifact.create(
            protected, currentStoreRevision(), AuthenticatedProtection.sha256(protected),
        )
    }

    override fun reset(): CompleteResetResult {
        checkOpen()
        val artifactDeleted = !storage.exists() || storage.delete()
        var keyDestroyed = false
        if (artifactDeleted) {
            keyProvider.destroy()
            keyDestroyed = true
        }
        document = null
        closed = true
        return CompleteResetResult(artifactDeleted, keyDestroyed, true)
    }

    override fun close() {
        document = null
        closed = true
    }

    private fun stateAsOf(revision: Long) = PersonalDataDocumentCodec.stateAsOf(requireDocument(), revision)
    private fun checkOpen() = check(!closed) { "Protected personal-data store is closed" }
    private fun requireDocument(): PersonalDataDocumentV2 {
        checkOpen()
        return requireNotNull(document) { "Protected personal-data state is unavailable" }
    }

    private fun persist(document: PersonalDataDocumentV2, key: javax.crypto.SecretKey) {
        val plaintext = PersonalDataDocumentCodec.encode(document)
        val protected = AuthenticatedProtection.protect(
            plaintext, key, ProtectedArtifactPurpose.PRIMARY_STORE, keyProvider.descriptor.alias, random,
        )
        storage.writeAtomically(protected)
    }
}

private data class PersonalStoreReceipt(
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

private data class PersonalStoreResult(
    override val disposition: AdmissionDisposition,
    override val receipt: AcceptedAdmissionReceipt?,
    override val reasonCodes: List<String>,
) : AdmissionResult

private fun result(disposition: AdmissionDisposition, durable: DurableReceipt): AdmissionResult = PersonalStoreResult(
    disposition,
    PersonalStoreReceipt(
        com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId.parse(durable.requestId),
        com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey.parse(durable.idempotencyKey),
        com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion.CT_V2_07_V1,
        durable.operationType, durable.priorRevision, durable.resultingRevision, durable.affectedStableIds,
        listOf(durable.eventId), durable.recordTime, durable.decisionTrace, durable.payloadFingerprint,
    ),
    emptyList(),
)

private fun rejection(disposition: AdmissionDisposition, vararg reasons: String): AdmissionResult =
    PersonalStoreResult(disposition, null, reasons.toList().sorted())
