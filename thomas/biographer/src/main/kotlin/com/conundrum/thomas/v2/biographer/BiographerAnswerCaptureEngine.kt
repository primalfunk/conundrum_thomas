package com.conundrum.thomas.v2.biographer

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Source-first Biographer answer authority. CT-V2-10 supplies no production implementation of its
 * ports. A source receipt remains successful if later perception fails.
 */
class BiographerAnswerCaptureEngine(
    private val admission: BiographerAdmissionPort,
    private val language: BiographerLanguageProcessor,
) {
    fun capture(command: BiographerAnswerCommand): BiographerCaptureResult {
        validate(command)?.let { return it }
        val acquisition = if (command.questionPlan.posture == BiographerPosture.OPEN_STORY) {
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE
        } else {
            AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE
        }
        val source = try {
            admission.admitSource(
                BiographerSourceAdmissionRequest(
                    command.answerId,
                    command.answerId.sourceIdentity(),
                    command.answerId.sourceRevision(),
                    command.idempotencyKey,
                    command.expectedStoreRevision,
                    command.committedText,
                    command.origin,
                    acquisition,
                    command.questionPlan.targetId,
                    command.privacy,
                    command.reportTime,
                ),
            )
        } catch (_: RuntimeException) {
            return failure(BiographerCaptureDisposition.STORE_FAILURE_WITHOUT_COMMIT, "BIOGRAPHER_SOURCE_PORT_FAILURE")
        }
        if (source.disposition !in setOf(AdmissionDisposition.ACCEPTED, AdmissionDisposition.IDEMPOTENT_REPLAY)) {
            return failure(mapAdmissionFailure(source.disposition), *source.reasonCodes.toTypedArray())
        }
        val revisionId = requireNotNull(source.sourceRevisionId)
        val storeRevision = requireNotNull(source.resultingStoreRevision)
        if (command.privacy == BiographerPrivacy.PRIVATE) {
            return success(
                command, source, acquisition, BiographerLanguageDisposition.SKIPPED_PRIVATE,
                emptyList(), 0, storeRevision,
            )
        }
        val processed = try {
            language.process(revisionId, command.questionPlan)
        } catch (_: RuntimeException) {
            val receipt = receipt(
                command, source, acquisition, BiographerLanguageDisposition.FAILED_AFTER_SOURCE_CAPTURE,
                emptyList(), 1, storeRevision,
            )
            return BiographerCaptureResult(
                BiographerCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
                receipt,
                listOf("LANGUAGE_PROCESSING_FAILURE_AFTER_SOURCE_CAPTURE"),
            )
        }
        if (processed.disposition == BiographerLanguageDisposition.REJECTED) {
            val receipt = receipt(
                command, source, acquisition, processed.disposition, processed.admittedEvidenceIds,
                processed.unresolvedOrUnsupportedCount, processed.resultingStoreRevision,
            )
            return BiographerCaptureResult(
                BiographerCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
                receipt,
                listOf("LANGUAGE_EVIDENCE_REJECTED_AFTER_SOURCE_CAPTURE"),
            )
        }
        return success(
            command, source, acquisition, processed.disposition, processed.admittedEvidenceIds,
            processed.unresolvedOrUnsupportedCount, processed.resultingStoreRevision,
        )
    }

    private fun success(
        command: BiographerAnswerCommand,
        source: BiographerAdmissionOutcome,
        acquisition: AcquisitionMode,
        languageDisposition: BiographerLanguageDisposition,
        evidenceIds: List<String>,
        unresolvedCount: Int,
        storeRevision: Long,
    ) = BiographerCaptureResult(
        if (source.disposition == AdmissionDisposition.IDEMPOTENT_REPLAY) {
            BiographerCaptureDisposition.IDEMPOTENT_REPLAY
        } else {
            BiographerCaptureDisposition.CAPTURED
        },
        receipt(command, source, acquisition, languageDisposition, evidenceIds, unresolvedCount, storeRevision),
        emptyList(),
    )

    private fun receipt(
        command: BiographerAnswerCommand,
        source: BiographerAdmissionOutcome,
        acquisition: AcquisitionMode,
        languageDisposition: BiographerLanguageDisposition,
        evidenceIds: List<String>,
        unresolvedCount: Int,
        storeRevision: Long,
    ): BiographerCaptureReceipt {
        val revisionId = requireNotNull(source.sourceRevisionId)
        val canonical = listOf(
            command.answerId.value,
            source.stableSourceId.value,
            revisionId.value,
            acquisition.name,
            command.origin.name,
            command.privacy.name,
            command.questionPlan.targetId?.value.orEmpty(),
            source.payloadFingerprint.orEmpty(),
            languageDisposition.name,
            evidenceIds.distinct().sorted().joinToString(","),
            unresolvedCount.toString(),
            storeRevision.toString(),
        ).joinToString("|")
        return BiographerCaptureReceipt(
            command.answerId,
            source.stableSourceId,
            revisionId,
            acquisition,
            command.origin,
            command.privacy,
            command.questionPlan.targetId,
            source.disposition,
            languageDisposition,
            evidenceIds.distinct().sorted(),
            unresolvedCount,
            storeRevision,
            sha256(canonical),
        )
    }

    private fun validate(command: BiographerAnswerCommand): BiographerCaptureResult? = when {
        command.committedText.isBlank() ->
            failure(BiographerCaptureDisposition.REJECTED_EMPTY_ANSWER, "EMPTY_BIOGRAPHER_ANSWER")
        command.authority == BiographerQualificationAuthority.NOT_AUTHORIZED ->
            failure(BiographerCaptureDisposition.REJECTED_AUTHORITY, "NON_SYNTHETIC_BIOGRAPHER_AUTHORITY")
        command.expectedStoreRevision < 0 || command.committedText.length > MAX_ANSWER_LENGTH ||
            '\u0000' in command.committedText ->
            failure(BiographerCaptureDisposition.REJECTED_INVALID_COMMAND, "INVALID_BIOGRAPHER_ANSWER_COMMAND")
        else -> null
    }

    private fun mapAdmissionFailure(disposition: AdmissionDisposition) = when (disposition) {
        AdmissionDisposition.REJECTED_AUTHORITY -> BiographerCaptureDisposition.REJECTED_AUTHORITY
        AdmissionDisposition.REJECTED_IDEMPOTENCY_CONFLICT ->
            BiographerCaptureDisposition.REJECTED_IDEMPOTENCY_CONFLICT
        AdmissionDisposition.FAILED_WITHOUT_COMMIT -> BiographerCaptureDisposition.STORE_FAILURE_WITHOUT_COMMIT
        else -> BiographerCaptureDisposition.SOURCE_ADMISSION_FAILED
    }

    private fun failure(disposition: BiographerCaptureDisposition, vararg reasonCodes: String) =
        BiographerCaptureResult(disposition, null, reasonCodes.toList().sorted())

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }

    companion object { private const val MAX_ANSWER_LENGTH = 4_096 }
}
