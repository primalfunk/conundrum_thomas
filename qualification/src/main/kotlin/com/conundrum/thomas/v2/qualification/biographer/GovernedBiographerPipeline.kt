package com.conundrum.thomas.v2.qualification.biographer

import com.conundrum.thomas.v2.biographer.BiographerAdmissionOutcome
import com.conundrum.thomas.v2.biographer.BiographerAdmissionPort
import com.conundrum.thomas.v2.biographer.BiographerAnswerCommand
import com.conundrum.thomas.v2.biographer.BiographerAnswerCaptureEngine
import com.conundrum.thomas.v2.biographer.BiographerCaptureResult
import com.conundrum.thomas.v2.biographer.BiographerCoverageMap
import com.conundrum.thomas.v2.biographer.BiographerInvestigationHistory
import com.conundrum.thomas.v2.biographer.BiographerLanguageDisposition
import com.conundrum.thomas.v2.biographer.BiographerLanguageOutcome
import com.conundrum.thomas.v2.biographer.BiographerLanguageProcessor
import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerPrivacy
import com.conundrum.thomas.v2.biographer.BiographerQuestionDecision
import com.conundrum.thomas.v2.biographer.BiographerQuestionPlan
import com.conundrum.thomas.v2.biographer.CoverageCandidate
import com.conundrum.thomas.v2.biographer.CoverageEvidence
import com.conundrum.thomas.v2.biographer.CoverageRequest
import com.conundrum.thomas.v2.biographer.CoverageSafeFact
import com.conundrum.thomas.v2.biographer.CoverageStatus
import com.conundrum.thomas.v2.biographer.DeterministicBiographerCoverageEngine
import com.conundrum.thomas.v2.biographer.InvestigationAnswerDisposition
import com.conundrum.thomas.v2.biographer.InvestigationTarget
import com.conundrum.thomas.v2.biographer.InvestigationTargetId
import com.conundrum.thomas.v2.biographer.InvestigationTargetKind
import com.conundrum.thomas.v2.biographer.RepresentedPeriod
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionContext
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.ApproximationPrecision
import com.conundrum.thomas.v2.longitudinal.AssertionPolarity
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.CalendarBoundary
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus
import com.conundrum.thomas.v2.longitudinal.CoverageTopic
import com.conundrum.thomas.v2.longitudinal.CoverageTopicId
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.LifeEvent
import com.conundrum.thomas.v2.longitudinal.LifePeriod
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.Place
import com.conundrum.thomas.v2.longitudinal.Relationship
import com.conundrum.thomas.v2.longitudinal.Role
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceNormalization
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.SourceSpanGrounding
import com.conundrum.thomas.v2.longitudinal.SupersessionKind
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.sourceTextSha256
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDraft
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.longitudinal.store.AdmissionResult
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import com.conundrum.thomas.v2.qualification.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.qualification.languageevidence.LanguagePipelineDisposition
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.ZoneOffset
import java.time.Year

data class GovernedBiographerCaptureOutcome(
    val capture: BiographerCaptureResult,
    val history: BiographerInvestigationHistory,
    val coverageMap: BiographerCoverageMap,
)

/**
 * CT-V2-10's sole complete composition root. It lives only in qualification and delegates every
 * durable write to the CT-V2-07 admission controller.
 */
class GovernedBiographerPipeline(
    val store: QualificationLongitudinalStore,
) {
    private val languagePipeline = GovernedLanguageEvidencePipeline(store)
    private val coverageEngine = DeterministicBiographerCoverageEngine()
    private val captureEngine = BiographerAnswerCaptureEngine(
        QualificationBiographerAdmissionPort(store),
        BiographerLanguageProcessor { sourceRevisionId, plan ->
            // The answer remains BIOGRAPHER provenance. A correction is separately promoted through
            // RecordUserCorrection so it cannot masquerade as USER_CORRECTION at source ingestion.
            val result = languagePipeline.process(sourceRevisionId, PerceptionContext())
            val disposition = when (result.disposition) {
                LanguagePipelineDisposition.ADMITTED -> BiographerLanguageDisposition.ADMITTED
                LanguagePipelineDisposition.IDEMPOTENT_REPLAY -> BiographerLanguageDisposition.IDEMPOTENT_REPLAY
                LanguagePipelineDisposition.SOURCE_ONLY -> BiographerLanguageDisposition.SOURCE_ONLY
                LanguagePipelineDisposition.REJECTED_PROPOSAL,
                LanguagePipelineDisposition.ADMISSION_REJECTED,
                -> BiographerLanguageDisposition.REJECTED
            }
            val evidenceAccepted = result.evidenceAdmission?.disposition in setOf(
                AdmissionDisposition.ACCEPTED,
                AdmissionDisposition.IDEMPOTENT_REPLAY,
            )
            val evidenceIds = if (evidenceAccepted) {
                (result.perception.proposals.map { it.assertion.id.value } +
                    listOfNotNull(result.perception.correctionCandidate?.correctingAssertion?.id?.value))
                    .distinct().sorted()
            } else {
                emptyList()
            }
            BiographerLanguageOutcome(
                disposition,
                result.perception,
                evidenceIds,
                result.perception.unresolved.size + result.validation.issues.size,
                result.state.storeRevision,
                result.state.canonicalDigest,
            )
        },
    )

    fun decide(
        request: CoverageRequest,
        history: BiographerInvestigationHistory = BiographerInvestigationHistory(),
    ): BiographerQuestionDecision = coverageEngine.decide(coverageEvidence(), history, request)

    fun captureAnswer(
        command: BiographerAnswerCommand,
        history: BiographerInvestigationHistory,
    ): GovernedBiographerCaptureOutcome {
        val result = captureEngine.capture(command)
        val receipt = result.receipt
        val target = targetFor(command.questionPlan, history)
        val disposition = when {
            command.privacy == BiographerPrivacy.PRIVATE -> InvestigationAnswerDisposition.MARKED_PRIVATE
            receipt == null -> InvestigationAnswerDisposition.ANSWERED_AMBIGUOUS
            receipt.admittedEvidenceIds.isEmpty() -> InvestigationAnswerDisposition.NO_EXTRACTABLE_EVIDENCE
            else -> InvestigationAnswerDisposition.ANSWERED_RELEVANT
        }
        val nextHistory = if (target == null || receipt == null) {
            history
        } else {
            history.recordOutcome(
                target,
                receipt?.resultingStoreRevision ?: store.reader.currentStoreRevision(),
                disposition,
                receipt?.stableSourceId,
            )
        }
        val map = coverageEngine.decide(
            coverageEvidence(),
            nextHistory,
            CoverageRequest(BiographerPosture.TARGETED_COVERAGE),
        ).coverageMap
        return GovernedBiographerCaptureOutcome(result, nextHistory, map)
    }

    fun recordOperationalOutcome(
        decision: BiographerQuestionDecision,
        disposition: InvestigationAnswerDisposition,
    ): BiographerInvestigationHistory {
        val target = decision.coverageMap.selectedTarget ?: return decision.resultingHistory
        return decision.resultingHistory.recordOutcome(
            target,
            store.reader.currentStoreRevision(),
            disposition,
        )
    }

    fun formedState() = languagePipeline.formState()

    /** Durable coverage dispositions still pass through CT-V2-07; DEFER remains operational only. */
    fun changeCoverage(
        targetId: InvestigationTargetId,
        status: InformationCoverageStatus,
        sourceRevisionIds: Set<com.conundrum.thomas.v2.longitudinal.SourceRecordId> = emptySet(),
        idempotencySuffix: String,
    ): AdmissionResult = store.admission.submit(
        governedRequest(
            "coverage." + idempotencySuffix,
            AdmissionActor.USER,
            AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE,
            LongitudinalWriteOperation.ChangeCoverage(
                CoverageTopic(
                    CoverageTopicId.parse("biographer." + targetId.value),
                    "biographer-target:" + targetId.value,
                    status,
                    sourceRevisionIds,
                ),
            ),
        ),
    )

    /** A structured user answer may resolve identity; the question engine itself never merges. */
    fun reviseIdentity(
        targetId: InvestigationTargetId,
        left: LifeEntityId,
        right: LifeEntityId,
        status: EntityIdentityStatus,
        supportingAssertionIds: Set<AssertionId>,
        idempotencySuffix: String,
    ): AdmissionResult {
        val prior = store.reader.identityDecisionHistory(left, right).lastOrNull()
        val decision = EntityIdentityLink(
            IdentityLinkId.parse("biographer." + targetId.value + "." + shortHash(idempotencySuffix)),
            left,
            right,
            status,
            supportingAssertionIds,
            "Explicit synthetic user answer to governed identity target.",
        )
        return store.admission.submit(
            governedRequest(
                "identity." + idempotencySuffix,
                AdmissionActor.USER,
                AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE,
                LongitudinalWriteOperation.ReviseIdentityLink(decision, prior?.id),
            ),
        )
    }

    /**
     * Promotes an explicit, target-resolved Biographer answer through CT-V2-07's dedicated user
     * correction operation. The original Biographer answer remains separate and inspectable.
     */
    fun recordExplicitUserCorrection(
        biographerAnswerSourceId: SourceIdentityId,
        correctedAssertionId: AssertionId,
        correctedYear: Int,
        idempotencySuffix: String,
    ): AdmissionResult {
        val answer = requireNotNull(store.reader.source(biographerAnswerSourceId))
        require(answer.provenance.acquisitionMode in setOf(
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE,
            AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE,
        ))
        val exact = (answer.originalContent as OriginalSourceContent.Inline).exactContent
        val stableId = SourceIdentityId.parse("correction." + biographerAnswerSourceId.value)
        val revisionId = SourceRecordId.parse(stableId.value + ".rev-1")
        val assertionId = AssertionId.parse(stableId.value + ".assertion")
        val correctionSource = SourceDraft(
            stableId,
            revisionId,
            AcquisitionMode.USER_CORRECTION,
            SourceAuthorRole.USER,
            InteractionId.parse(stableId.value),
            OriginalSourceContent.Inline(exact),
            EventTime.ApproximateYear(Year.of(correctedYear)),
            answer.reportTime,
            SourcePrivacy.ELIGIBLE,
            mapOf("biographer-answer-source" to biographerAnswerSourceId.value),
        )
        val assertion = EvidenceAssertion(
            assertionId,
            revisionId,
            AssertionSubject.User,
            AssertionPredicate(PersonalConceptId.parse("temporal.corrected-year"), PredicateSemantics.TEMPORAL),
            AssertionValue.TimeReference(EventTime.ApproximateYear(Year.of(correctedYear))),
            UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
            EventTime.ApproximateYear(Year.of(correctedYear)),
            EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION,
            AssertionPolarity.AFFIRMATIVE,
            SourceSpanGrounding(
                revisionId,
                0,
                exact.length,
                exact,
                sourceTextSha256(exact),
                "ct-v2-10.explicit-correction.v1",
                SourceNormalization.NONE,
            ),
        )
        val relation = CorrectionRelation(
            EvidenceRelationId.parse(stableId.value + ".relation"),
            assertion.id,
            correctedAssertionId,
            CorrectionEffect.CORRECTS_AND_SUPERSEDES,
            "Explicit user correction supplied through a governed Biographer target.",
        )
        val supersession = SupersessionRelation(
            EvidenceRelationId.parse(stableId.value + ".supersession"),
            ClaimReference.Assertion(assertion.id),
            ClaimReference.Assertion(correctedAssertionId),
            SupersessionKind.CORRECTS,
            "Explicit user correction supersedes current eligibility while preserving history.",
        )
        return store.admission.submit(
            governedRequest(
                "correction." + idempotencySuffix,
                AdmissionActor.USER,
                AdmissionOrigin.USER_CORRECTION,
                LongitudinalWriteOperation.RecordUserCorrection(
                    correctionSource,
                    assertion,
                    relation,
                    supersession,
                ),
            ),
        )
    }

    fun coverageEvidence(): CoverageEvidence =
        com.conundrum.thomas.v2.biographer.GovernedBiographerCoverage.derive(languagePipeline.formState(), store.reader)

    private fun targetFor(plan: BiographerQuestionPlan, history: BiographerInvestigationHistory): InvestigationTarget? {
        val id = plan.targetId ?: return null
        val candidate = coverageEvidence().candidates.firstOrNull { it.id == id }
        return if (candidate != null) {
            val decision = coverageEngine.decide(
                coverageEvidence(),
                history,
                CoverageRequest(BiographerPosture.TARGETED_COVERAGE, explicitlyReopenedTargetId = id),
            )
            decision.coverageMap.targets.firstOrNull { it.id == id }
        } else {
            InvestigationTarget(
                id,
                plan.targetKind,
                plan.groundingIds,
                emptyList(),
                plan.safeFacts.mapNotNull { it.temporalExpression },
                plan.uncertaintyConstraints.isNotEmpty(),
                plan.reasonCode,
                com.conundrum.thomas.v2.biographer.TargetEligibility.ELIGIBLE,
                history.entries[id],
                plan.safeFacts,
                plan.groundingIds.sorted().joinToString("|"),
            )
        }
    }

    private fun governedRequest(
        suffix: String,
        actor: AdmissionActor,
        origin: AdmissionOrigin,
        operation: LongitudinalWriteOperation,
    ): LongitudinalAdmissionRequest {
        val key = "biographer." + suffix
        return LongitudinalAdmissionRequest(
            AdmissionRequestId.parse("biographer." + sha256(key).take(24)),
            IdempotencyKey.parse(key),
            store.reader.currentStoreRevision(),
            actor,
            origin,
            AdmissionPolicyVersion.CT_V2_07_V1,
            StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
            operation,
        )
    }

    private fun ClaimReference.stableId() = when (this) {
        is ClaimReference.Assertion -> assertionId.value
        is ClaimReference.Hypothesis -> hypothesisId.value
    }

    private fun safeId(value: String): String {
        val normalized = value.lowercase().replace(Regex("[^a-z0-9.-]+"), "-").trim('-', '.')
        return normalized.takeIf { it.isNotBlank() } ?: "target." + shortHash(value)
    }

    private fun shortHash(value: String) = sha256(value).take(16)
    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

private class QualificationBiographerAdmissionPort(
    private val store: QualificationLongitudinalStore,
) : BiographerAdmissionPort {
    override fun admitSource(request: com.conundrum.thomas.v2.biographer.BiographerSourceAdmissionRequest): BiographerAdmissionOutcome {
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
                request.privacy.toSourcePrivacy(),
                mapOf(
                    "biographer.answer-origin" to request.origin.name,
                    "biographer.contract-version" to com.conundrum.thomas.v2.biographer.CT_V2_10_CAPTURE_VERSION,
                    "biographer.target-id" to (request.targetId?.value ?: "open-story"),
                ),
            ),
        )
        val key = "biographer.answer." + request.idempotencyKey.value
        val origin = if (request.acquisitionMode == AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE) {
            AdmissionOrigin.BIOGRAPHER_OPEN_NARRATIVE
        } else {
            AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE
        }
        val result = store.admission.submit(
            LongitudinalAdmissionRequest(
                AdmissionRequestId.parse("biographer.answer." + sha256(key).take(20)),
                IdempotencyKey.parse(key),
                request.expectedStoreRevision,
                AdmissionActor.USER,
                origin,
                AdmissionPolicyVersion.CT_V2_07_V1,
                StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
                operation,
            ),
        )
        return result.toBiographerOutcome(request.stableSourceId, request.sourceRevisionId)
    }

    private fun AdmissionResult.toBiographerOutcome(
        stableSourceId: com.conundrum.thomas.v2.longitudinal.SourceIdentityId,
        revisionId: com.conundrum.thomas.v2.longitudinal.SourceRecordId,
    ) = BiographerAdmissionOutcome(
        disposition,
        stableSourceId,
        revisionId,
        receipt?.resultingStoreRevision,
        reasonCodes,
        receipt?.payloadFingerprint,
    )

    private fun BiographerPrivacy.toSourcePrivacy() = when (this) {
        BiographerPrivacy.ELIGIBLE -> SourcePrivacy.ELIGIBLE
        BiographerPrivacy.PRIVATE -> SourcePrivacy.PRIVATE
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
