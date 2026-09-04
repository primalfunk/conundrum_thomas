package com.conundrum.thomas.v2.journal

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Journal's only commit authority. Capture always precedes language processing and response
 * planning. The ports have no production implementation in CT-V2-09.
 */
class JournalCaptureEngine(
    private val admission: JournalAdmissionPort,
    private val language: JournalLanguageProcessor,
    private val responsePlanner: JournalResponseIntentPlanner = DeterministicJournalResponseIntentPlanner(),
) {
    fun commit(command: JournalCommitCommand): JournalCaptureResult {
        validate(command.committedText, command.expectedStoreRevision, command.authority)?.let { return it }
        val sourceOutcome = try {
            admission.admitSource(
                JournalSourceAdmissionRequest(
                    command.entryId,
                    command.entryId.sourceIdentity(),
                    command.entryId.sourceRevision(1),
                    command.idempotencyKey,
                    command.expectedStoreRevision,
                    command.committedText,
                    command.captureOrigin,
                    command.privacy,
                    command.reportTime,
                ),
            )
        } catch (_: RuntimeException) {
            return failure(JournalCaptureDisposition.STORE_FAILURE_WITHOUT_COMMIT, "JOURNAL_SOURCE_PORT_FAILURE")
        }
        return afterSource(command.entryId, command.responsePreference, sourceOutcome)
    }

    fun revise(command: JournalRevisionCommand): JournalCaptureResult {
        validate(command.committedText, command.expectedStoreRevision, command.authority)?.let { return it }
        if (command.newRevisionNumber <= 1 || command.priorRevisionId == command.entryId.sourceRevision(command.newRevisionNumber)) {
            return failure(JournalCaptureDisposition.REJECTED_INVALID_COMMAND, "INVALID_SOURCE_REVISION")
        }
        val sourceOutcome = try {
            admission.appendSourceRevision(
                JournalSourceRevisionRequest(
                    command.entryId,
                    command.entryId.sourceIdentity(),
                    command.priorRevisionId,
                    command.entryId.sourceRevision(command.newRevisionNumber),
                    command.idempotencyKey,
                    command.expectedStoreRevision,
                    command.committedText,
                    command.reportTime,
                ),
            )
        } catch (_: RuntimeException) {
            return failure(JournalCaptureDisposition.STORE_FAILURE_WITHOUT_COMMIT, "JOURNAL_REVISION_PORT_FAILURE")
        }
        return afterSource(command.entryId, command.responsePreference, sourceOutcome)
    }

    fun changePrivacy(command: JournalPrivacyChangeCommand): JournalPrivacyChangeResult {
        if (command.authority != JournalQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY || command.expectedStoreRevision < 0) {
            return JournalPrivacyChangeResult(
                JournalPrivacyChangeDisposition.REJECTED,
                AdmissionDisposition.REJECTED_AUTHORITY,
                null,
                listOf("INVALID_OR_UNAUTHORIZED_PRIVACY_COMMAND"),
            )
        }
        val outcome = try {
            admission.changePrivacy(
                JournalPrivacyAdmissionRequest(
                    command.entryId.sourceIdentity(),
                    command.idempotencyKey,
                    command.expectedStoreRevision,
                    command.privacy,
                ),
            )
        } catch (_: RuntimeException) {
            return JournalPrivacyChangeResult(
                JournalPrivacyChangeDisposition.STORE_FAILURE,
                AdmissionDisposition.FAILED_WITHOUT_COMMIT,
                null,
                listOf("JOURNAL_PRIVACY_PORT_FAILURE"),
            )
        }
        val disposition = when (outcome.disposition) {
            AdmissionDisposition.ACCEPTED -> JournalPrivacyChangeDisposition.CHANGED
            AdmissionDisposition.IDEMPOTENT_REPLAY -> JournalPrivacyChangeDisposition.IDEMPOTENT_REPLAY
            AdmissionDisposition.FAILED_WITHOUT_COMMIT -> JournalPrivacyChangeDisposition.STORE_FAILURE
            else -> JournalPrivacyChangeDisposition.REJECTED
        }
        return JournalPrivacyChangeResult(
            disposition,
            outcome.disposition,
            outcome.resultingStoreRevision,
            outcome.reasonCodes,
        )
    }

    private fun afterSource(
        entryId: JournalEntryId,
        responsePreference: JournalResponsePreference,
        source: JournalAdmissionOutcome,
    ): JournalCaptureResult {
        if (source.disposition !in setOf(AdmissionDisposition.ACCEPTED, AdmissionDisposition.IDEMPOTENT_REPLAY)) {
            return failure(mapAdmissionFailure(source.disposition), *source.reasonCodes.toTypedArray())
        }
        requireNotNull(source.sourceRevisionId)
        requireNotNull(source.captureOrigin)
        val privacy = requireNotNull(source.privacy)
        if (privacy == JournalPrivacy.PRIVATE) {
            val receipt = receipt(
                entryId,
                source,
                JournalLanguageProcessingDisposition.SKIPPED_PRIVATE,
                emptyList(),
                0,
                requireNotNull(source.resultingStoreRevision),
                responsePreference,
                JournalResponseIntentDisposition.PRIVATE_ENTRY_SUPPRESSED,
            )
            return JournalCaptureResult(captureDisposition(source), receipt, null, emptyList())
        }

        val processed = try {
            language.process(requireNotNull(source.sourceRevisionId))
        } catch (_: RuntimeException) {
            val receipt = receipt(
                entryId,
                source,
                JournalLanguageProcessingDisposition.FAILED_AFTER_SOURCE_CAPTURE,
                emptyList(),
                1,
                requireNotNull(source.resultingStoreRevision),
                responsePreference,
                JournalResponseIntentDisposition.NOT_REACHED,
            )
            return JournalCaptureResult(
                JournalCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
                receipt,
                null,
                listOf("LANGUAGE_PROCESSING_FAILURE_AFTER_SOURCE_CAPTURE"),
            )
        }

        if (processed.disposition == JournalLanguageProcessingDisposition.ADMISSION_REJECTED) {
            val receipt = receipt(
                entryId,
                source,
                processed.disposition,
                processed.admittedEvidenceIds,
                processed.unresolvedOrUnsupportedCount,
                processed.resultingStoreRevision,
                responsePreference,
                JournalResponseIntentDisposition.NOT_REACHED,
            )
            return JournalCaptureResult(
                JournalCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
                receipt,
                null,
                listOf("LANGUAGE_EVIDENCE_ADMISSION_REJECTED_AFTER_SOURCE_CAPTURE"),
            )
        }

        if (responsePreference == JournalResponsePreference.NO_RESPONSE) {
            return successfulResult(
                entryId,
                source,
                processed,
                responsePreference,
                JournalResponsePlanningOutcome(JournalResponseIntentDisposition.NONE_SELECTED),
            )
        }
        val planning = try {
            responsePlanner.plan(responsePreference, processed.perception)
        } catch (_: RuntimeException) {
            val receipt = receipt(
                entryId,
                source,
                processed.disposition,
                processed.admittedEvidenceIds,
                processed.unresolvedOrUnsupportedCount,
                processed.resultingStoreRevision,
                responsePreference,
                JournalResponseIntentDisposition.PLANNING_FAILED,
            )
            return JournalCaptureResult(
                JournalCaptureDisposition.RESPONSE_PLANNING_FAILED_AFTER_CAPTURE,
                receipt,
                null,
                listOf("RESPONSE_PLANNING_FAILURE_AFTER_CAPTURE"),
            )
        }
        return successfulResult(entryId, source, processed, responsePreference, planning)
    }

    private fun successfulResult(
        entryId: JournalEntryId,
        source: JournalAdmissionOutcome,
        processed: JournalLanguageProcessingOutcome,
        preference: JournalResponsePreference,
        planning: JournalResponsePlanningOutcome,
    ): JournalCaptureResult {
        val receipt = receipt(
            entryId,
            source,
            processed.disposition,
            processed.admittedEvidenceIds,
            processed.unresolvedOrUnsupportedCount,
            processed.resultingStoreRevision,
            preference,
            planning.disposition,
        )
        return JournalCaptureResult(captureDisposition(source), receipt, planning.plan, emptyList())
    }

    private fun receipt(
        entryId: JournalEntryId,
        source: JournalAdmissionOutcome,
        processing: JournalLanguageProcessingDisposition,
        evidenceIds: List<String>,
        unresolvedCount: Int,
        storeRevision: Long,
        preference: JournalResponsePreference,
        responseDisposition: JournalResponseIntentDisposition,
    ): JournalCaptureReceipt {
        val sourceRevisionId = requireNotNull(source.sourceRevisionId)
        val captureOrigin = requireNotNull(source.captureOrigin)
        val privacy = requireNotNull(source.privacy)
        val canonical = listOf(
            entryId.value,
            source.stableSourceId.value,
            sourceRevisionId.value,
            captureOrigin.name,
            privacy.name,
            source.payloadFingerprint.orEmpty(),
            processing.name,
            evidenceIds.sorted().joinToString(","),
            unresolvedCount.toString(),
            storeRevision.toString(),
        ).joinToString("|")
        return JournalCaptureReceipt(
            entryId,
            source.stableSourceId,
            sourceRevisionId,
            AcquisitionMode.JOURNAL,
            captureOrigin,
            privacy,
            source.disposition,
            processing,
            evidenceIds.distinct().sorted(),
            unresolvedCount,
            storeRevision,
            preference,
            responseDisposition,
            sha256(canonical),
        )
    }

    private fun captureDisposition(source: JournalAdmissionOutcome) =
        if (source.disposition == AdmissionDisposition.IDEMPOTENT_REPLAY) {
            JournalCaptureDisposition.IDEMPOTENT_REPLAY
        } else {
            JournalCaptureDisposition.CAPTURED
        }

    private fun validate(
        text: String,
        expectedStoreRevision: Long,
        authority: JournalQualificationAuthority,
    ): JournalCaptureResult? = when {
        text.isBlank() -> failure(JournalCaptureDisposition.REJECTED_EMPTY_ENTRY, "EMPTY_JOURNAL_ENTRY")
        authority != JournalQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY ->
            failure(JournalCaptureDisposition.REJECTED_AUTHORITY, "NON_SYNTHETIC_JOURNAL_AUTHORITY")
        expectedStoreRevision < 0 || text.length > MAX_ENTRY_LENGTH || '\u0000' in text ->
            failure(JournalCaptureDisposition.REJECTED_INVALID_COMMAND, "INVALID_JOURNAL_COMMIT_COMMAND")
        else -> null
    }

    private fun mapAdmissionFailure(disposition: AdmissionDisposition) = when (disposition) {
        AdmissionDisposition.REJECTED_AUTHORITY -> JournalCaptureDisposition.REJECTED_AUTHORITY
        AdmissionDisposition.REJECTED_IDEMPOTENCY_CONFLICT -> JournalCaptureDisposition.REJECTED_IDEMPOTENCY_CONFLICT
        AdmissionDisposition.REJECTED_PRIVACY -> JournalCaptureDisposition.REJECTED_PRIVACY_STATE
        AdmissionDisposition.FAILED_WITHOUT_COMMIT -> JournalCaptureDisposition.STORE_FAILURE_WITHOUT_COMMIT
        else -> JournalCaptureDisposition.SOURCE_ADMISSION_FAILED
    }

    private fun failure(disposition: JournalCaptureDisposition, vararg codes: String) =
        JournalCaptureResult(disposition, null, null, codes.toList().sorted())

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }

    companion object { private const val MAX_ENTRY_LENGTH = 4_096 }
}
