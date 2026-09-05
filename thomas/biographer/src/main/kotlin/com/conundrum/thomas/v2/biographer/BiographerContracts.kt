package com.conundrum.thomas.v2.biographer

import com.conundrum.thomas.v2.languageevidence.perception.LanguagePerceptionResult
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition

const val CT_V2_10_COVERAGE_RULE_VERSION = "ct-v2-10.coverage.v1"
const val CT_V2_10_QUESTION_RULE_VERSION = "ct-v2-10.question.v1"
const val CT_V2_10_CAPTURE_VERSION = "ct-v2-10.biographer-capture.v1"
private val biographerIdPattern = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")

@JvmInline
value class InvestigationTargetId private constructor(val value: String) : Comparable<InvestigationTargetId> {
    override fun compareTo(other: InvestigationTargetId) = value.compareTo(other.value)
    companion object {
        fun parse(value: String): InvestigationTargetId {
            require(biographerIdPattern.matches(value))
            return InvestigationTargetId(value)
        }
    }
}

@JvmInline
value class BiographerAnswerId private constructor(val value: String) {
    companion object {
        fun parse(value: String): BiographerAnswerId {
            require(biographerIdPattern.matches(value))
            return BiographerAnswerId(value)
        }
    }

    fun sourceIdentity(): SourceIdentityId = SourceIdentityId.parse("biographer.$value")
    fun sourceRevision(): SourceRecordId = SourceRecordId.parse("biographer.$value.rev-1")
}

@JvmInline
value class BiographerIdempotencyKey private constructor(val value: String) {
    companion object {
        fun parse(value: String): BiographerIdempotencyKey {
            require(biographerIdPattern.matches(value))
            return BiographerIdempotencyKey(value)
        }
    }
}

enum class BiographerPosture { OPEN_STORY, TARGETED_COVERAGE }

enum class InvestigationTargetKind {
    OPEN_STORY,
    TEMPORAL_GAP,
    PERIOD_DETAIL,
    EVENT_DETAIL,
    EVENT_TIME_UNRESOLVED,
    ROLE_GAP,
    PLACE_GAP,
    RELATIONSHIP_CONTEXT,
    ENTITY_IDENTITY_UNRESOLVED,
    CONTRADICTION_CLARIFICATION,
    CORRECTION_TARGET_UNRESOLVED,
    USER_NAMED_TOPIC,
    EXISTING_OPEN_EVIDENTIARY_QUESTION,
}

enum class CoverageStatus {
    UNKNOWN,
    SPARSE,
    COVERED_ENOUGH_FOR_CURRENT_PURPOSE,
    UNRESOLVED,
    PRIVATE,
    DECLINED,
}

enum class TargetEligibility {
    ELIGIBLE,
    COVERED,
    RECENTLY_ASKED,
    DEFERRED,
    DECLINED,
    PRIVATE,
    UNANSWERABLE,
}

/** Safe facts are identifiers and structural semantics, never invented narrative prose. */
data class CoverageSafeFact(
    val concept: String,
    val groundingIds: List<String>,
    val temporalExpression: EventTime? = null,
) {
    init {
        require(concept.isNotBlank())
        require(groundingIds.isNotEmpty())
    }
}

data class CoverageCandidate(
    val id: InvestigationTargetId,
    val kind: InvestigationTargetKind,
    val groundingIds: List<String>,
    val relevantEntityIds: List<LifeEntityId> = emptyList(),
    val temporalBounds: List<EventTime> = emptyList(),
    val uncertainty: Boolean = false,
    val reasonCode: String,
    val status: CoverageStatus,
    val safeFacts: List<CoverageSafeFact> = emptyList(),
    val materialChangeToken: String,
    val answerable: Boolean = true,
) {
    init {
        require(kind != InvestigationTargetKind.OPEN_STORY)
        require(groundingIds.isNotEmpty())
        require(reasonCode.isNotBlank())
        require(materialChangeToken.isNotBlank())
    }
}

data class RepresentedPeriod(
    val id: String,
    val time: EventTime,
    val groundingIds: List<String>,
    val status: CoverageStatus,
) {
    init { require(id.isNotBlank() && groundingIds.isNotEmpty()) }
}

data class CoverageEvidence(
    val storeRevision: Long,
    val representedPeriods: List<RepresentedPeriod> = emptyList(),
    val representedRoleIds: List<LifeEntityId> = emptyList(),
    val representedPlaceIds: List<LifeEntityId> = emptyList(),
    val representedRelationshipIds: List<LifeEntityId> = emptyList(),
    val candidates: List<CoverageCandidate> = emptyList(),
    val derivationVersion: String = CT_V2_10_COVERAGE_RULE_VERSION,
) {
    init {
        require(storeRevision >= 0)
        require(candidates.map { it.id }.distinct().size == candidates.size)
    }
}

enum class InvestigationAnswerDisposition {
    OFFERED,
    ANSWERED_RELEVANT,
    ANSWERED_OTHER_EVIDENCE,
    ANSWERED_AMBIGUOUS,
    NO_EXTRACTABLE_EVIDENCE,
    SKIPPED,
    DEFERRED,
    DECLINED,
    MARKED_PRIVATE,
    CHANGED_TOPIC,
    STOPPED,
}

data class InvestigationHistoryEntry(
    val targetId: InvestigationTargetId,
    val firstOfferedRevision: Long,
    val lastOfferedRevision: Long,
    val offerCount: Int,
    val answerDisposition: InvestigationAnswerDisposition,
    val lastSubstantiveAnswerSourceId: SourceIdentityId? = null,
    val materialChangeToken: String,
) {
    init {
        require(firstOfferedRevision in 0..lastOfferedRevision)
        require(offerCount > 0)
        require(materialChangeToken.isNotBlank())
    }
}

data class BiographerInvestigationHistory(
    val entries: Map<InvestigationTargetId, InvestigationHistoryEntry> = emptyMap(),
) {
    fun recordOffer(target: InvestigationTarget, storeRevision: Long): BiographerInvestigationHistory {
        val prior = entries[target.id]
        val next = if (prior == null) {
            InvestigationHistoryEntry(
                target.id,
                storeRevision,
                storeRevision,
                1,
                InvestigationAnswerDisposition.OFFERED,
                materialChangeToken = target.materialChangeToken,
            )
        } else {
            prior.copy(
                lastOfferedRevision = storeRevision,
                offerCount = prior.offerCount + 1,
                answerDisposition = InvestigationAnswerDisposition.OFFERED,
                materialChangeToken = target.materialChangeToken,
            )
        }
        return copy(entries = entries + (target.id to next))
    }

    fun recordOutcome(
        target: InvestigationTarget,
        storeRevision: Long,
        disposition: InvestigationAnswerDisposition,
        answerSourceId: SourceIdentityId? = null,
    ): BiographerInvestigationHistory {
        val prior = entries[target.id] ?: InvestigationHistoryEntry(
            target.id,
            storeRevision,
            storeRevision,
            1,
            InvestigationAnswerDisposition.OFFERED,
            materialChangeToken = target.materialChangeToken,
        )
        return copy(entries = entries + (target.id to prior.copy(
            lastOfferedRevision = maxOf(storeRevision, prior.lastOfferedRevision),
            answerDisposition = disposition,
            lastSubstantiveAnswerSourceId = answerSourceId ?: prior.lastSubstantiveAnswerSourceId,
            materialChangeToken = target.materialChangeToken,
        )))
    }
}

data class UserNamedCoverageTarget(
    val id: InvestigationTargetId,
    val topicConcept: String,
    val groundingIds: List<String> = listOf("user-current-direction"),
    val explicitlyReopens: InvestigationTargetId? = null,
) {
    init { require(topicConcept.isNotBlank() && groundingIds.isNotEmpty()) }
}

enum class BiographerInvestigationAuthority { ALLOWED, BLOCKED_BY_SAFETY_SCOPE }

data class CoverageRequest(
    val posture: BiographerPosture,
    val userNamedTarget: UserNamedCoverageTarget? = null,
    val explicitlyReopenedTargetId: InvestigationTargetId? = null,
    val investigationAuthority: BiographerInvestigationAuthority = BiographerInvestigationAuthority.ALLOWED,
)

data class InvestigationTarget(
    val id: InvestigationTargetId,
    val kind: InvestigationTargetKind,
    val groundingIds: List<String>,
    val relevantEntityIds: List<LifeEntityId>,
    val temporalBounds: List<EventTime>,
    val uncertainty: Boolean,
    val reasonCode: String,
    val eligibility: TargetEligibility,
    val priorInvestigation: InvestigationHistoryEntry?,
    val safeFacts: List<CoverageSafeFact>,
    val materialChangeToken: String,
)

data class BiographerCoverageMap(
    val storeRevision: Long,
    val representedPeriods: List<RepresentedPeriod>,
    val sparsePeriodIds: List<String>,
    val unresolvedTemporalTargetIds: List<InvestigationTargetId>,
    val representedRoleIds: List<LifeEntityId>,
    val representedPlaceIds: List<LifeEntityId>,
    val representedRelationshipIds: List<LifeEntityId>,
    val unresolvedIdentityTargetIds: List<InvestigationTargetId>,
    val unresolvedContradictionTargetIds: List<InvestigationTargetId>,
    val unresolvedCorrectionTargetIds: List<InvestigationTargetId>,
    val openEvidenceTargetIds: List<InvestigationTargetId>,
    val deferredTargetIds: List<InvestigationTargetId>,
    val declinedTargetIds: List<InvestigationTargetId>,
    val privateTargetIds: List<InvestigationTargetId>,
    val previouslyInvestigatedTargetIds: List<InvestigationTargetId>,
    val targets: List<InvestigationTarget>,
    val eligibleTargets: List<InvestigationTarget>,
    val selectedTarget: InvestigationTarget?,
    val derivationRuleVersion: String,
    val rankingRuleVersion: String,
    val canonicalDigest: String,
)

enum class BiographerQuestionSemanticAct {
    OPEN_HISTORICAL_INVITATION,
    EXPLORE_STRUCTURAL_GAP,
    CLARIFY_IDENTITY,
    CLARIFY_CONTRADICTION,
    CLARIFY_CORRECTION_TARGET,
    EXPLORE_USER_NAMED_TOPIC,
}

enum class BiographerProhibitedQuestionAct {
    DIAGNOSIS,
    THERAPEUTIC_TECHNIQUE,
    PSYCHOLOGICAL_CAUSAL_CLAIM,
    TRAUMA_PRESUPPOSITION,
    UNSUPPORTED_EMOTION,
    UNSUPPORTED_DATE,
    IDENTITY_MERGE,
    MULTIPLE_QUESTIONS,
    COVERAGE_PRESSURE,
}

data class BiographerQuestionPlan(
    val posture: BiographerPosture,
    val targetId: InvestigationTargetId?,
    val targetKind: InvestigationTargetKind,
    val groundingIds: List<String>,
    val safeFacts: List<CoverageSafeFact>,
    val uncertaintyConstraints: List<String>,
    val semanticAct: BiographerQuestionSemanticAct,
    val maximumQuestionCount: Int = 1,
    val prohibitedActs: Set<BiographerProhibitedQuestionAct> = BiographerProhibitedQuestionAct.entries.toSet(),
    val reasonCode: String,
    val questionRuleVersion: String = CT_V2_10_QUESTION_RULE_VERSION,
) {
    init {
        require(maximumQuestionCount == 1)
        require(targetKind == InvestigationTargetKind.OPEN_STORY || targetId != null)
        require(reasonCode.isNotBlank())
    }
}

enum class BiographerQuestionDisposition { AUTHORIZED, NO_TARGET }

data class BiographerQuestionDecision(
    val disposition: BiographerQuestionDisposition,
    val plan: BiographerQuestionPlan?,
    val coverageMap: BiographerCoverageMap,
    val resultingHistory: BiographerInvestigationHistory,
) {
    init { require((disposition == BiographerQuestionDisposition.AUTHORIZED) == (plan != null)) }
}

enum class BiographerAnswerOrigin { TYPED, SPEECH_TRANSCRIPT }
enum class BiographerPrivacy { ELIGIBLE, PRIVATE }
enum class BiographerQualificationAuthority { SYNTHETIC_QUALIFICATION_ONLY, ANDROID_PRODUCTION, NOT_AUTHORIZED }

data class BiographerAnswerCommand(
    val answerId: BiographerAnswerId,
    val idempotencyKey: BiographerIdempotencyKey,
    val expectedStoreRevision: Long,
    val questionPlan: BiographerQuestionPlan,
    val committedText: String,
    val origin: BiographerAnswerOrigin,
    val privacy: BiographerPrivacy,
    val reportTime: ReportTime,
    val authority: BiographerQualificationAuthority = BiographerQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
)

enum class BiographerCaptureDisposition {
    CAPTURED,
    IDEMPOTENT_REPLAY,
    REJECTED_EMPTY_ANSWER,
    REJECTED_INVALID_COMMAND,
    REJECTED_AUTHORITY,
    REJECTED_IDEMPOTENCY_CONFLICT,
    SOURCE_ADMISSION_FAILED,
    EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
    STORE_FAILURE_WITHOUT_COMMIT,
}

enum class BiographerLanguageDisposition {
    ADMITTED,
    IDEMPOTENT_REPLAY,
    SOURCE_ONLY,
    SKIPPED_PRIVATE,
    REJECTED,
    FAILED_AFTER_SOURCE_CAPTURE,
}

class BiographerSourceAdmissionRequest internal constructor(
    val answerId: BiographerAnswerId,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId,
    val idempotencyKey: BiographerIdempotencyKey,
    val expectedStoreRevision: Long,
    val exactCommittedText: String,
    val origin: BiographerAnswerOrigin,
    val acquisitionMode: AcquisitionMode,
    val targetId: InvestigationTargetId?,
    val privacy: BiographerPrivacy,
    val reportTime: ReportTime,
)

data class BiographerAdmissionOutcome(
    val disposition: AdmissionDisposition,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId?,
    val resultingStoreRevision: Long?,
    val reasonCodes: List<String>,
    val payloadFingerprint: String?,
)

fun interface BiographerAdmissionPort {
    fun admitSource(request: BiographerSourceAdmissionRequest): BiographerAdmissionOutcome
}

data class BiographerLanguageOutcome(
    val disposition: BiographerLanguageDisposition,
    val perception: LanguagePerceptionResult,
    val admittedEvidenceIds: List<String>,
    val unresolvedOrUnsupportedCount: Int,
    val resultingStoreRevision: Long,
    val stateDigest: String,
)

fun interface BiographerLanguageProcessor {
    fun process(sourceRevisionId: SourceRecordId, questionPlan: BiographerQuestionPlan): BiographerLanguageOutcome
}

class BiographerCaptureReceipt internal constructor(
    val answerId: BiographerAnswerId,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId,
    val acquisitionMode: AcquisitionMode,
    val origin: BiographerAnswerOrigin,
    val privacy: BiographerPrivacy,
    val targetId: InvestigationTargetId?,
    val admissionDisposition: AdmissionDisposition,
    val languageDisposition: BiographerLanguageDisposition,
    val admittedEvidenceIds: List<String>,
    val unresolvedOrUnsupportedCount: Int,
    val resultingStoreRevision: Long,
    val canonicalFingerprint: String,
)

class BiographerCaptureResult internal constructor(
    val disposition: BiographerCaptureDisposition,
    val receipt: BiographerCaptureReceipt?,
    val reasonCodes: List<String>,
) {
    val sourceCaptured: Boolean get() = receipt != null
}

fun interface BiographerQuestionRenderer {
    fun render(plan: BiographerQuestionPlan): String
}

enum class BiographerQuestionRenderDisposition { RENDERED, NO_PLAN, FAILED_WITHOUT_SOURCE_MUTATION }

data class BiographerQuestionRenderResult(
    val disposition: BiographerQuestionRenderDisposition,
    val renderedText: String? = null,
    val reasonCode: String,
)
