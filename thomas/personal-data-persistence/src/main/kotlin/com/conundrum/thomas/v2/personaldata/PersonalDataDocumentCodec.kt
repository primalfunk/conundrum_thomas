package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOperationType
import com.conundrum.thomas.v2.longitudinal.admission.CanonicalLongitudinalEncoding
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionPolicy
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAggregateState
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.time.Instant

internal data class DurableReceipt(
    val requestId: String,
    val idempotencyKey: String,
    val policyVersion: String,
    val operationType: AdmissionOperationType,
    val priorRevision: Long,
    val resultingRevision: Long,
    val affectedStableIds: List<String>,
    val eventId: String,
    val recordTime: Instant,
    val decisionTrace: List<String>,
    val payloadFingerprint: String,
) : Serializable

internal data class DurableLedgerEvent(
    val eventId: String,
    val revision: Long,
    val recordTime: Instant,
    val request: LongitudinalAdmissionRequest,
    val payloadFingerprint: String,
    val affectedStableIds: List<String>,
    val decisionTrace: List<String>,
    val previousEventDigest: String,
    val eventDigest: String,
) : Serializable

internal data class RedactedLedgerHeader(
    val eventId: String,
    val revision: Long,
    val recordTime: Instant,
    val requestId: String,
    val idempotencyKey: String,
    val operationType: AdmissionOperationType,
    val policyVersion: String,
    val payloadFingerprint: String,
    val affectedStableIds: List<String>,
    val erasureReason: String,
) : Serializable

internal data class RejectedAdmissionRecord(
    val sequence: Long,
    val requestId: String,
    val idempotencyKey: String,
    val operationType: AdmissionOperationType,
    val disposition: AdmissionDisposition,
    val policyVersion: String,
    val reasonCodes: List<String>,
    val payloadFingerprint: String,
    val recordTime: Instant,
) : Serializable

internal data class PersonalDataDocumentV1(
    val checkpoint: LongitudinalAggregateState,
    val ledger: List<DurableLedgerEvent>,
    val receipts: Map<String, DurableReceipt>,
    val projection: LongitudinalAggregateState,
    val projectionDigest: String,
) : Serializable

internal data class PersonalDataDocumentV2(
    val policyVersion: String,
    val checkpoint: LongitudinalAggregateState,
    val archivedLedger: List<RedactedLedgerHeader>,
    val activeLedger: List<DurableLedgerEvent>,
    val receipts: Map<String, DurableReceipt>,
    val rejectedAdmissions: List<RejectedAdmissionRecord>,
    val projection: LongitudinalAggregateState,
    val projectionDigest: String,
) : Serializable

internal data class VerifiedDocument(
    val document: PersonalDataDocumentV2,
    val projectionRebuilt: Boolean,
    val migrated: Boolean,
)

internal object PersonalDataDocumentCodec {
    private val magic = "CTV2DOC".toByteArray(StandardCharsets.US_ASCII)
    private val policy = LongitudinalAdmissionPolicy(StoreDataClassification.PROTECTED_PERSONAL_DATA)

    fun empty(): PersonalDataDocumentV2 {
        val state = LongitudinalAggregateState()
        return PersonalDataDocumentV2(
            CT_V2_14_PERSISTENCE_POLICY_VERSION,
            state,
            emptyList(),
            emptyList(),
            emptyMap(),
            emptyList(),
            state,
            CanonicalLongitudinalEncoding.stateDigest(state),
        )
    }

    fun encode(document: PersonalDataDocumentV2): ByteArray = encodeVersioned(CT_V2_14_STORE_SCHEMA_VERSION, document)

    internal fun encodeLegacyV1(document: PersonalDataDocumentV1): ByteArray = encodeVersioned(1, document)

    fun decodeAndVerify(bytes: ByteArray): VerifiedDocument {
        val (schema, payload) = readVersioned(bytes)
        val migrated = when (schema) {
            CT_V2_14_STORE_SCHEMA_VERSION -> decodeObject<PersonalDataDocumentV2>(payload)
            1 -> migrate(decodeObject<PersonalDataDocumentV1>(payload))
            else -> throw PersonalDataPersistenceException(
                PersonalDataFailureDisposition.UNSUPPORTED_SCHEMA,
                "UNSUPPORTED_PERSONAL_DATA_SCHEMA_$schema",
            )
        }
        if (migrated.policyVersion != CT_V2_14_PERSISTENCE_POLICY_VERSION) {
            throw PersonalDataPersistenceException(PersonalDataFailureDisposition.UNSUPPORTED_SCHEMA, "PERSISTENCE_POLICY_VERSION_UNSUPPORTED")
        }
        val archivedRevisions = migrated.archivedLedger.map { it.revision }
        if (archivedRevisions != archivedRevisions.sorted() || archivedRevisions.distinct().size != archivedRevisions.size ||
            (archivedRevisions.isNotEmpty() && archivedRevisions.last() != migrated.checkpoint.storeRevision)
        ) failLedger("ARCHIVED_LEDGER_CHECKPOINT_MISMATCH")
        if (migrated.activeLedger.any { it.revision <= migrated.checkpoint.storeRevision }) {
            failLedger("ACTIVE_LEDGER_PRECEDES_CHECKPOINT")
        }
        val activeRevisions = migrated.activeLedger.map { it.revision }
        val expectedActiveRevisions = if (activeRevisions.isEmpty()) emptyList() else
            ((migrated.checkpoint.storeRevision + 1)..(migrated.checkpoint.storeRevision + activeRevisions.size)).toList()
        if (activeRevisions != expectedActiveRevisions) failLedger("ACTIVE_LEDGER_REVISION_SEQUENCE_INVALID")
        if (migrated.receipts.size != migrated.archivedLedger.size + migrated.activeLedger.size) {
            failLedger("RECEIPT_COUNT_MISMATCH")
        }
        migrated.receipts.forEach { (key, receipt) ->
            if (key != receipt.idempotencyKey || !receipt.payloadFingerprint.matches(Regex("^[0-9a-f]{64}$"))) {
                failLedger("IDEMPOTENCY_RECEIPT_INTEGRITY_FAILURE")
            }
        }
        val reconstructed = reconstructCurrent(migrated)
        val expected = CanonicalLongitudinalEncoding.stateDigest(reconstructed)
        val projectionValid = runCatching {
            migrated.projection.snapshot.requireValid()
            migrated.projection.storeRevision == reconstructed.storeRevision &&
                migrated.projectionDigest == CanonicalLongitudinalEncoding.stateDigest(migrated.projection) &&
                migrated.projectionDigest == expected
        }.getOrDefault(false)
        val normalized = if (projectionValid) migrated else migrated.copy(projection = reconstructed, projectionDigest = expected)
        return VerifiedDocument(normalized, !projectionValid, schema == 1)
    }

    fun reconstructCurrent(document: PersonalDataDocumentV2): LongitudinalAggregateState {
        document.checkpoint.snapshot.requireValid()
        var state = document.checkpoint
        var previousDigest = document.archivedLedger.lastOrNull()?.let(::archivedDigestAnchor) ?: ZERO_DIGEST
        document.activeLedger.sortedBy { it.revision }.forEach { event ->
            if (event.revision != state.storeRevision + 1) failLedger("LEDGER_REVISION_GAP")
            if (event.request.expectedStoreRevision != state.storeRevision) failLedger("LEDGER_REQUEST_REVISION_MISMATCH")
            if (event.request.classification != StoreDataClassification.PROTECTED_PERSONAL_DATA) failLedger("LEDGER_CLASSIFICATION_INVALID")
            if (CanonicalLongitudinalEncoding.requestFingerprint(event.request) != event.payloadFingerprint) failLedger("LEDGER_PAYLOAD_FINGERPRINT_MISMATCH")
            if (event.previousEventDigest != previousDigest) failLedger("LEDGER_CHAIN_PREDECESSOR_MISMATCH")
            if (event.eventDigest != eventDigest(event, previousDigest)) failLedger("LEDGER_EVENT_DIGEST_MISMATCH")
            val plan = policy.plan(event.request, state, RecordTime(event.recordTime))
            val accepted = plan as? com.conundrum.thomas.v2.longitudinal.admission.AdmissionPlanResult.Accepted
                ?: failLedger("LEDGER_EVENT_REJECTED_ON_REPLAY")
            state = accepted.mutation.state
            previousDigest = event.eventDigest
        }
        state.snapshot.requireValid()
        return state
    }

    fun stateAsOf(document: PersonalDataDocumentV2, revision: Long): LongitudinalAggregateState {
        require(revision in 0..document.projection.storeRevision) { "Requested revision is outside the store" }
        if (revision < document.checkpoint.storeRevision) {
            throw com.conundrum.thomas.v2.longitudinal.store.HistoricalStateErasedException(
                "Historical state before a governed deletion checkpoint is unavailable",
            )
        }
        if (revision == document.checkpoint.storeRevision) return document.checkpoint
        val partial = document.copy(activeLedger = document.activeLedger.filter { it.revision <= revision })
        return reconstructCurrent(partial)
    }

    fun eventDigest(event: DurableLedgerEvent, previousDigest: String): String = CanonicalLongitudinalEncoding.sha256(
        listOf(
            event.eventId,
            event.revision,
            event.recordTime,
            event.request.requestId.value,
            event.request.idempotencyKey.value,
            event.payloadFingerprint,
            event.affectedStableIds.sorted(),
            event.decisionTrace,
            previousDigest,
        ),
    )

    fun chainAnchor(document: PersonalDataDocumentV2): String =
        document.activeLedger.lastOrNull()?.eventDigest
            ?: document.archivedLedger.lastOrNull()?.let(::archivedDigestAnchor)
            ?: ZERO_DIGEST

    fun archive(event: DurableLedgerEvent, reason: String) = RedactedLedgerHeader(
        event.eventId,
        event.revision,
        event.recordTime,
        event.request.requestId.value,
        event.request.idempotencyKey.value,
        event.request.operation.type,
        event.request.policyVersion.value,
        event.payloadFingerprint,
        event.affectedStableIds,
        reason,
    )

    private fun migrate(old: PersonalDataDocumentV1): PersonalDataDocumentV2 {
        val base = PersonalDataDocumentV2(
            CT_V2_14_PERSISTENCE_POLICY_VERSION,
            old.checkpoint,
            emptyList(),
            old.ledger,
            old.receipts,
            emptyList(),
            old.projection,
            old.projectionDigest,
        )
        reconstructCurrent(base)
        return base
    }

    private fun encodeVersioned(schema: Int, value: Serializable): ByteArray {
        val payload = ByteArrayOutputStream().use { bytes ->
            ObjectOutputStream(bytes).use { it.writeObject(value) }
            bytes.toByteArray()
        }
        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use {
                it.write(magic)
                it.writeInt(schema)
                it.writeInt(payload.size)
                it.write(payload)
            }
            bytes.toByteArray()
        }
    }

    private fun readVersioned(bytes: ByteArray): Pair<Int, ByteArray> = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val foundMagic = ByteArray(magic.size).also(input::readFully)
            if (!foundMagic.contentEquals(magic)) malformed("DOCUMENT_MAGIC_MISMATCH")
            val schema = input.readInt()
            val length = input.readInt()
            if (length !in 1..64_000_000 || length != input.available()) malformed("DOCUMENT_LENGTH_INVALID")
            schema to ByteArray(length).also(input::readFully)
        }
    } catch (failure: PersonalDataPersistenceException) {
        throw failure
    } catch (failure: Exception) {
        throw PersonalDataPersistenceException(PersonalDataFailureDisposition.MALFORMED_STORE, "DOCUMENT_MALFORMED", failure)
    }

    private inline fun <reified T> decodeObject(bytes: ByteArray): T =
        WhitelistedPersonalDataInputStream(ByteArrayInputStream(bytes)).use { stream ->
            val value = stream.readObject()
            value as? T ?: malformed("DOCUMENT_TYPE_INVALID")
        }

    private fun archivedDigestAnchor(header: RedactedLedgerHeader): String = CanonicalLongitudinalEncoding.sha256(
        listOf(header.eventId, header.revision, header.payloadFingerprint, header.erasureReason),
    )

    private fun failLedger(code: String): Nothing = throw PersonalDataPersistenceException(
        PersonalDataFailureDisposition.LEDGER_INTEGRITY_FAILURE,
        code,
    )

    private fun malformed(code: String): Nothing = throw PersonalDataPersistenceException(
        PersonalDataFailureDisposition.MALFORMED_STORE,
        code,
    )

    const val ZERO_DIGEST = "0000000000000000000000000000000000000000000000000000000000000000"
}

private class WhitelistedPersonalDataInputStream(input: ByteArrayInputStream) : ObjectInputStream(input) {
    override fun resolveClass(desc: ObjectStreamClass): Class<*> {
        val name = desc.name
        val allowed = name.startsWith("com.conundrum.thomas.v2.longitudinal.") ||
            name.startsWith("com.conundrum.thomas.v2.personaldata.") || name.startsWith("java.lang.") ||
            name.startsWith("java.util.") || name.startsWith("java.time.") || name.startsWith("kotlin.") || name.startsWith("[")
        if (!allowed) throw PersonalDataPersistenceException(
            PersonalDataFailureDisposition.MALFORMED_STORE,
            "DOCUMENT_CLASS_NOT_PERMITTED",
        )
        return super.resolveClass(desc)
    }
}
