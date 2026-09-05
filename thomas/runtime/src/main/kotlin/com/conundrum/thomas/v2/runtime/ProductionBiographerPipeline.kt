package com.conundrum.thomas.v2.runtime

import com.conundrum.thomas.v2.biographer.BiographerAdmissionOutcome
import com.conundrum.thomas.v2.biographer.BiographerAdmissionPort
import com.conundrum.thomas.v2.biographer.BiographerAnswerCaptureEngine
import com.conundrum.thomas.v2.biographer.BiographerAnswerCommand
import com.conundrum.thomas.v2.biographer.BiographerCaptureResult
import com.conundrum.thomas.v2.biographer.BiographerLanguageDisposition
import com.conundrum.thomas.v2.biographer.BiographerLanguageOutcome
import com.conundrum.thomas.v2.biographer.BiographerLanguageProcessor
import com.conundrum.thomas.v2.biographer.BiographerPrivacy
import com.conundrum.thomas.v2.biographer.BiographerQuestionDecision
import com.conundrum.thomas.v2.biographer.BiographerSourceAdmissionRequest
import com.conundrum.thomas.v2.biographer.CoverageEvidence
import com.conundrum.thomas.v2.biographer.CoverageRequest
import com.conundrum.thomas.v2.biographer.DeterministicBiographerCoverageEngine
import com.conundrum.thomas.v2.biographer.BiographerInvestigationHistory
import com.conundrum.thomas.v2.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.languageevidence.LanguagePipelineDisposition
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionContext
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDraft
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStore
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Production Biographer composition. Target selection remains in CT-V2-10; this adapter only
 * supplies governed structural evidence and admits committed user answers.
 */
class ProductionBiographerPipeline(
    private val store: ProtectedPersonalDataStore,
) {
    private val language = GovernedLanguageEvidencePipeline(
        store.admission,
        store.reader,
        StoreDataClassification.PROTECTED_PERSONAL_DATA,
    )
    private val coverage = DeterministicBiographerCoverageEngine()
    private val capture = BiographerAnswerCaptureEngine(
        ProductionBiographerAdmissionPort(store),
        BiographerLanguageProcessor { revision, _ ->
            val result = language.process(revision, PerceptionContext())
            val disposition = when (result.disposition) {
                LanguagePipelineDisposition.ADMITTED -> BiographerLanguageDisposition.ADMITTED
                LanguagePipelineDisposition.IDEMPOTENT_REPLAY -> BiographerLanguageDisposition.IDEMPOTENT_REPLAY
                LanguagePipelineDisposition.SOURCE_ONLY -> BiographerLanguageDisposition.SOURCE_ONLY
                LanguagePipelineDisposition.REJECTED_PROPOSAL,
                LanguagePipelineDisposition.ADMISSION_REJECTED,
                -> BiographerLanguageDisposition.REJECTED
            }
            val accepted = result.evidenceAdmission?.disposition in setOf(
                AdmissionDisposition.ACCEPTED,
                AdmissionDisposition.IDEMPOTENT_REPLAY,
            )
            val ids = if (accepted) {
                (result.perception.proposals.map { it.assertion.id.value } +
                    listOfNotNull(result.perception.correctionCandidate?.correctingAssertion?.id?.value))
                    .distinct().sorted()
            } else {
                emptyList()
            }
            BiographerLanguageOutcome(
                disposition,
                result.perception,
                ids,
                result.perception.unresolved.size + result.validation.issues.size,
                result.state.storeRevision,
                result.state.canonicalDigest,
            )
        },
    )

    fun decide(
        request: CoverageRequest,
        history: BiographerInvestigationHistory = BiographerInvestigationHistory(),
    ): BiographerQuestionDecision = coverage.decide(
        CoverageEvidence(store.reader.currentStoreRevision()),
        history,
        request,
    )

    fun captureAnswer(command: BiographerAnswerCommand): BiographerCaptureResult = capture.capture(command)
    fun formedState() = language.formState()
}

private class ProductionBiographerAdmissionPort(
    private val store: ProtectedPersonalDataStore,
) : BiographerAdmissionPort {
    override fun admitSource(request: BiographerSourceAdmissionRequest): BiographerAdmissionOutcome {
        val operation = LongitudinalWriteOperation.AdmitSource(
            SourceDraft(
                request.stableSourceId,
                request.sourceRevisionId,
                request.acquisitionMode,
                SourceAuthorRole.USER,
                InteractionId.parse(request.stableSourceId.value),
                OriginalSourceContent.Inline(request.exactCommittedText),
                EventTime.Unknown("Biographer report time is separate from described historical event time"),
                request.reportTime,
                when (request.privacy) {
                    BiographerPrivacy.ELIGIBLE -> SourcePrivacy.ELIGIBLE
                    BiographerPrivacy.PRIVATE -> SourcePrivacy.PRIVATE
                },
                mapOf(
                    "biographer.answer-origin" to request.origin.name,
                    "biographer.contract-version" to com.conundrum.thomas.v2.biographer.CT_V2_10_CAPTURE_VERSION,
                    "biographer.target-id" to (request.targetId?.value ?: "open-story"),
                ),
            ),
        )
        val key = "biographer.answer.${request.idempotencyKey.value}"
        val origin = if (request.acquisitionMode == AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE) {
            AdmissionOrigin.BIOGRAPHER_OPEN_NARRATIVE
        } else {
            AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE
        }
        val result = store.admission.submit(
            LongitudinalAdmissionRequest(
                AdmissionRequestId.parse("biographer.answer.${sha256(key).take(20)}"),
                IdempotencyKey.parse(key),
                request.expectedStoreRevision,
                AdmissionActor.USER,
                origin,
                AdmissionPolicyVersion.CT_V2_07_V1,
                StoreDataClassification.PROTECTED_PERSONAL_DATA,
                operation,
            ),
        )
        return BiographerAdmissionOutcome(
            result.disposition,
            request.stableSourceId,
            if (result.receipt == null) null else request.sourceRevisionId,
            result.receipt?.resultingStoreRevision,
            result.reasonCodes,
            result.receipt?.payloadFingerprint,
        )
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
