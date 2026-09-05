package com.conundrum.thomas.v2.runtime

import com.conundrum.thomas.v2.contextpacket.DeterministicContextPacketBuilder
import com.conundrum.thomas.v2.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.languageevidence.LanguagePipelineDisposition
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionContext
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.RecordTime
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
import com.conundrum.thomas.v2.retrieval.DeterministicLongitudinalRetriever
import com.conundrum.thomas.v2.retrieval.GovernedLongitudinalReadPort
import com.conundrum.thomas.v2.retrieval.RetrievalOpenQuestion
import com.conundrum.thomas.v2.retrieval.RetrievalRecurrenceCandidate
import com.conundrum.thomas.v2.therapylongitudinal.CT_V2_12_CAPTURE_CONTRACT_VERSION
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyIntegrationEngine
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnCommand
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnResult
import com.conundrum.thomas.v2.therapylongitudinal.TherapyContextPacketPort
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessingDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessingOutcome
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessor
import com.conundrum.thomas.v2.therapylongitudinal.TherapySourceAdmissionOutcome
import com.conundrum.thomas.v2.therapylongitudinal.TherapySourceAdmissionPort
import com.conundrum.thomas.v2.therapylongitudinal.TherapySourceAdmissionRequest
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnPrivacy
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Production route-first Therapy composition. The core policy receives no store or retrieval port;
 * this adapter joins already-governed boundaries and writes user turns only through CT-V2-07.
 */
class ProductionTherapyPipeline(
    private val store: ProtectedPersonalDataStore,
    openQuestions: (Long) -> List<RetrievalOpenQuestion> = { emptyList() },
    recurrences: (Long) -> List<RetrievalRecurrenceCandidate> = { emptyList() },
) {
    private val language = GovernedLanguageEvidencePipeline(
        store.admission,
        store.reader,
        StoreDataClassification.PROTECTED_PERSONAL_DATA,
    )
    private val packets = DeterministicContextPacketBuilder(
        DeterministicLongitudinalRetriever(
            GovernedLongitudinalReadPort(
                store.reader,
                StoreDataClassification.PROTECTED_PERSONAL_DATA,
                openQuestions,
                recurrences,
            ),
        ),
    )
    private val engine = LongitudinalTherapyIntegrationEngine(
        ProductionTherapySourceAdmissionPort(store),
        governedLanguageProcessor(),
        TherapyContextPacketPort(packets::build),
    )

    fun integrate(command: LongitudinalTherapyTurnCommand): LongitudinalTherapyTurnResult =
        engine.integrate(command)

    fun formedState() = language.formState()

    private fun governedLanguageProcessor() = TherapyLanguageProcessor { request ->
        val result = language.process(request.sourceRevisionId, PerceptionContext(request.correctionTarget))
        val disposition = when (result.disposition) {
            LanguagePipelineDisposition.ADMITTED -> TherapyLanguageProcessingDisposition.ADMITTED
            LanguagePipelineDisposition.IDEMPOTENT_REPLAY -> TherapyLanguageProcessingDisposition.IDEMPOTENT_REPLAY
            LanguagePipelineDisposition.SOURCE_ONLY -> TherapyLanguageProcessingDisposition.SOURCE_ONLY
            LanguagePipelineDisposition.REJECTED_PROPOSAL -> TherapyLanguageProcessingDisposition.REJECTED_PROPOSAL
            LanguagePipelineDisposition.ADMISSION_REJECTED -> TherapyLanguageProcessingDisposition.ADMISSION_REJECTED
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
        TherapyLanguageProcessingOutcome(
            disposition,
            result.perception.disposition,
            ids,
            result.perception.unresolved.size + result.validation.issues.size,
            result.state.storeRevision,
            result.state.canonicalDigest,
        )
    }
}

private class ProductionTherapySourceAdmissionPort(
    private val store: ProtectedPersonalDataStore,
) : TherapySourceAdmissionPort {
    override fun admitSource(request: TherapySourceAdmissionRequest): TherapySourceAdmissionOutcome {
        val operation = LongitudinalWriteOperation.AdmitSource(
            SourceDraft(
                request.stableSourceId,
                request.sourceRevisionId,
                AcquisitionMode.THERAPIST_CONVERSATION,
                SourceAuthorRole.USER,
                InteractionId.parse(request.stableSourceId.value),
                OriginalSourceContent.Inline(request.exactUserText),
                EventTime.Unknown("Therapy report time is separate from described event time"),
                request.reportTime,
                when (request.privacy) {
                    TherapyTurnPrivacy.ELIGIBLE -> SourcePrivacy.ELIGIBLE
                    TherapyTurnPrivacy.PRIVATE -> SourcePrivacy.PRIVATE
                },
                mapOf(
                    "therapy.capture-origin" to request.captureOrigin.name,
                    "therapy.session-id" to request.sessionId.value,
                    "therapy.turn-id" to request.turnId.value,
                    "therapy.capture-contract-version" to CT_V2_12_CAPTURE_CONTRACT_VERSION,
                ),
            ),
        )
        val key = "therapy.capture.${request.idempotencyKey.value}"
        val result = store.admission.submit(
            LongitudinalAdmissionRequest(
                AdmissionRequestId.parse("therapy.capture.${sha256(key).take(20)}"),
                IdempotencyKey.parse(key),
                request.expectedStoreRevision,
                AdmissionActor.USER,
                AdmissionOrigin.THERAPIST_CONVERSATION,
                AdmissionPolicyVersion.CT_V2_07_V1,
                StoreDataClassification.PROTECTED_PERSONAL_DATA,
                operation,
            ),
        )
        return TherapySourceAdmissionOutcome(
            result.disposition,
            request.stableSourceId,
            if (result.receipt != null) request.sourceRevisionId else null,
            result.receipt?.priorStoreRevision,
            result.receipt?.resultingStoreRevision,
            result.receipt?.recordTime?.let(::RecordTime),
            result.receipt?.affectedStableIds.orEmpty(),
            result.reasonCodes,
            result.receipt?.payloadFingerprint,
        )
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
