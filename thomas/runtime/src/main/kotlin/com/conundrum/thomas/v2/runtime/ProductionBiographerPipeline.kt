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
import com.conundrum.thomas.v2.biographer.InvestigationAnswerDisposition
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
        coverageEvidence(),
        history,
        request,
    )

    fun captureAnswer(command: BiographerAnswerCommand): BiographerCaptureResult = capture.capture(command)

    fun coverageEvidence() = com.conundrum.thomas.v2.biographer.GovernedBiographerCoverage.derive(language.formState(), store.reader)

    fun answer(
        command: BiographerAnswerCommand,
        target: com.conundrum.thomas.v2.biographer.InvestigationTarget?,
        history: BiographerInvestigationHistory,
    ): ProductionBiographerAnswer {
        val explicit = when (ProductionTherapyInputBoundary.normalize(command.committedText)) {
            "skip", "skip this question" -> InvestigationAnswerDisposition.SKIPPED
            "later", "defer this question" -> InvestigationAnswerDisposition.DEFERRED
            "i decline", "i don't want to answer this" -> InvestigationAnswerDisposition.DECLINED
            "keep this private", "this topic is private" -> InvestigationAnswerDisposition.MARKED_PRIVATE
            "stop", "please stop" -> InvestigationAnswerDisposition.STOPPED
            "change topic" -> InvestigationAnswerDisposition.CHANGED_TOPIC
            else -> null
        }
        val before = language.formState()
        val repeatedSourceText = store.reader.snapshot().sources.any {
            (it.originalContent as? OriginalSourceContent.Inline)?.exactContent == command.committedText &&
                store.reader.isEligible(com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef(
                    com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType.SOURCE_REVISION, it.id.value))
        }
        val captured = if (explicit == null) capture.capture(command) else null
        val receipt = captured?.receipt
        val after = language.formState()
        val structuralChange = before.activeExplicitClaims.map { meaning(it) }.toSet() != after.activeExplicitClaims.map { meaning(it) }.toSet() ||
            before.activeSelfReports.map { meaning(it) }.toSet() != after.activeSelfReports.map { meaning(it) }.toSet()
        val admitted = receipt?.admittedEvidenceIds.orEmpty()
        val admittedClaims = (after.activeExplicitClaims + after.activeSelfReports).filter { it.id.value in admitted }
        val repeatedDatedReport = repeatedSourceText && admittedClaims.isNotEmpty() && admittedClaims.all {
            it.eventTime is EventTime.Range || it.eventTime is EventTime.CalendarDate ||
                it.eventTime is EventTime.ExactInstant || it.eventTime is EventTime.ApproximateYear || it.eventTime is EventTime.ApproximateDate
        }
        val changed = structuralChange && !repeatedDatedReport
        val targetChanged = target != null && coverageEvidence().candidates.none { it.id == target.id && it.materialChangeToken == target.materialChangeToken }
        val outcome = explicit ?: when {
            receipt?.privacy == BiographerPrivacy.PRIVATE -> InvestigationAnswerDisposition.MARKED_PRIVATE
            receipt == null -> InvestigationAnswerDisposition.ANSWERED_AMBIGUOUS
            admitted.isEmpty() || !changed -> InvestigationAnswerDisposition.NO_EXTRACTABLE_EVIDENCE
            targetChanged -> InvestigationAnswerDisposition.ANSWERED_RELEVANT
            else -> InvestigationAnswerDisposition.ANSWERED_OTHER_EVIDENCE
        }
        // Only explicit user privacy/refusal becomes durable coverage control. No prompt history or
        // inferred "covered" fact is persisted; answered coverage is reconstructed from source evidence.
        if (target != null && outcome in setOf(InvestigationAnswerDisposition.DECLINED, InvestigationAnswerDisposition.MARKED_PRIVATE)) {
            val status = if (outcome == InvestigationAnswerDisposition.DECLINED)
                com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus.DECLINED
            else com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus.PRIVATE
            val id = "biographer.coverage." + command.answerId.value
            val admittedControl = store.admission.submit(LongitudinalAdmissionRequest(
                AdmissionRequestId.parse(id), IdempotencyKey.parse(id), store.reader.currentStoreRevision(),
                AdmissionActor.USER, AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE, AdmissionPolicyVersion.CT_V2_07_V1,
                StoreDataClassification.PROTECTED_PERSONAL_DATA,
                LongitudinalWriteOperation.ChangeCoverage(com.conundrum.thomas.v2.longitudinal.CoverageTopic(
                    com.conundrum.thomas.v2.longitudinal.CoverageTopicId.parse("biographer." + target.id.value),
                    "biographer-target:" + target.id.value, status))))
            check(admittedControl.disposition in setOf(AdmissionDisposition.ACCEPTED, AdmissionDisposition.IDEMPOTENT_REPLAY))
        }
        val nextHistory = if (target == null) history else history.recordOutcome(target,
            store.reader.currentStoreRevision(), outcome, receipt?.stableSourceId)
        return ProductionBiographerAnswer(captured, outcome, nextHistory, changed)
    }

    private fun meaning(assertion: com.conundrum.thomas.v2.longitudinal.EvidenceAssertion): String =
        listOf(assertion.subject, assertion.predicate, assertion.value, assertion.eventTime).joinToString("|")

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

 data class ProductionBiographerAnswer(
    val capture: BiographerCaptureResult?,
    val disposition: InvestigationAnswerDisposition,
    val resultingHistory: BiographerInvestigationHistory,
    val materialEvidenceChanged: Boolean,
)
