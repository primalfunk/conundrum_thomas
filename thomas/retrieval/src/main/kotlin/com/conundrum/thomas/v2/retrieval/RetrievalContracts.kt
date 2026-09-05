package com.conundrum.thomas.v2.retrieval

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.SourceSpanGrounding
import com.conundrum.thomas.v2.longitudinal.ThomasHypothesis

const val CT_V2_11_RETRIEVAL_POLICY_VERSION = "ct-v2-11.retrieval.v1"
const val CT_V2_11_LEXICAL_POLICY_VERSION = "ct-v2-11.lexical.v1"
const val CT_V2_11_BUDGET_VERSION = "ct-v2-11.context-budget.v1"

private val retrievalIdPattern = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")

@JvmInline
value class RetrievalRequestId private constructor(val value: String) {
    companion object {
        fun parse(value: String): RetrievalRequestId {
            require(retrievalIdPattern.matches(value))
            return RetrievalRequestId(value)
        }
    }
}

enum class RetrievalIntent {
    ORDINARY_MODE_CONTEXT,
    EXPLICIT_LOOK_BACK,
    BIOGRAPHER_TARGET_CONTEXT,
    EXPLAIN_DERIVED_OBJECT,
    EXPLICIT_SOURCE_RECALL,
}

enum class RetrievalMode { JOURNAL, BIOGRAPHER, THERAPY }
enum class RetrievalAuthority { SYNTHETIC_QUALIFICATION_ONLY, ANDROID_PRODUCTION, NOT_AUTHORIZED }

data class ContextBudget(
    val version: String = CT_V2_11_BUDGET_VERSION,
    val maximumTotalTextCharacters: Int = 4_096,
    val maximumLongitudinalObjects: Int = 8,
    val maximumSourceExcerpts: Int = 4,
    val maximumExcerptCharacters: Int = 320,
    val maximumDependencyDepth: Int = 2,
    val maximumImmediateItems: Int = 6,
    val maximumRuntimeItems: Int = 4,
) {
    init {
        require(version == CT_V2_11_BUDGET_VERSION)
        require(maximumTotalTextCharacters in 256..32_768)
        require(maximumLongitudinalObjects in 1..32)
        require(maximumSourceExcerpts in 0..16)
        require(maximumExcerptCharacters in 32..2_048)
        require(maximumDependencyDepth in 0..4)
        require(maximumImmediateItems in 0..16)
        require(maximumRuntimeItems in 0..16)
    }
}

data class RetrievalAnchors(
    val sourceIdentityIds: Set<SourceIdentityId> = emptySet(),
    val sourceRevisionIds: Set<SourceRecordId> = emptySet(),
    val assertionIds: Set<AssertionId> = emptySet(),
    val hypothesisIds: Set<HypothesisId> = emptySet(),
    val entityIds: Set<LifeEntityId> = emptySet(),
    val eventIds: Set<LifeEntityId> = emptySet(),
    val relationshipIds: Set<LifeEntityId> = emptySet(),
    val periodIds: Set<LifeEntityId> = emptySet(),
    val predicateIds: Set<PersonalConceptId> = emptySet(),
    val openQuestionIds: Set<String> = emptySet(),
    val lexicalTerms: Set<String> = emptySet(),
    val temporalBounds: List<EventTime> = emptyList(),
) {
    init {
        require(openQuestionIds.none(String::isBlank))
        require(lexicalTerms.none(String::isBlank))
    }

    fun isEmpty(): Boolean = sourceIdentityIds.isEmpty() && sourceRevisionIds.isEmpty() &&
        assertionIds.isEmpty() && hypothesisIds.isEmpty() && entityIds.isEmpty() && eventIds.isEmpty() &&
        relationshipIds.isEmpty() && periodIds.isEmpty() && predicateIds.isEmpty() &&
        openQuestionIds.isEmpty() && lexicalTerms.isEmpty() && temporalBounds.isEmpty()
}

data class RetrievalRequest(
    val requestId: RetrievalRequestId,
    val intent: RetrievalIntent,
    val activeMode: RetrievalMode,
    val snapshotRevision: Long,
    val policyVersion: String = CT_V2_11_RETRIEVAL_POLICY_VERSION,
    val anchors: RetrievalAnchors = RetrievalAnchors(),
    val budget: ContextBudget = ContextBudget(),
    val explicitTargetId: String? = null,
    val explicitlyUserDirected: Boolean = false,
    val authority: RetrievalAuthority = RetrievalAuthority.SYNTHETIC_QUALIFICATION_ONLY,
) {
    init {
        require(snapshotRevision >= 0)
        require(explicitTargetId == null || explicitTargetId.isNotBlank())
    }
}

enum class RetrievalObjectType {
    SOURCE_REVISION,
    ASSERTION,
    ENTITY,
    HYPOTHESIS,
    CONTRADICTION,
    CORRECTION,
    OPEN_QUESTION,
    RECURRENCE,
}

data class RetrievalObjectKey(val type: RetrievalObjectType, val stableId: String) {
    init { require(stableId.isNotBlank()) }
}

enum class RetrievalLifecycleStatus {
    ACTIVE,
    CONTESTED,
    SUPERSEDED,
    RETIRED,
    REVIEW_REQUIRED,
    DEPENDENCY_BLOCKED,
    PRIVATE_INELIGIBLE,
    AUDIT_ONLY,
}

data class RetrievalLifecycle(
    val status: RetrievalLifecycleStatus,
    val eligibleForOrdinaryUse: Boolean,
    val causeCode: String,
    val changedAtRevision: Long,
) {
    init { require(causeCode.isNotBlank() && changedAtRevision >= 0) }
}

data class RetrievalOpenQuestion(
    val id: String,
    val basisIds: List<String>,
    val reasonCode: String,
) {
    init { require(id.isNotBlank() && basisIds.isNotEmpty() && reasonCode.isNotBlank()) }
}

data class RetrievalRecurrenceCandidate(
    val id: String,
    val conceptId: PersonalConceptId,
    val assertionIds: List<AssertionId>,
    val independentSourceIds: List<SourceIdentityId>,
) {
    init {
        require(id.isNotBlank())
        require(assertionIds.isNotEmpty())
        require(independentSourceIds.size >= 3)
    }
}

/** Persistence-neutral, immutable read projection. */
data class RetrievalArchiveSnapshot(
    val storeRevision: Long,
    val evidence: LongitudinalEvidenceSnapshot,
    val lifecycle: Map<RetrievalObjectKey, RetrievalLifecycle>,
    val currentSourceRevisionIds: Set<SourceRecordId>,
    val openQuestions: List<RetrievalOpenQuestion> = emptyList(),
    val recurrenceCandidates: List<RetrievalRecurrenceCandidate> = emptyList(),
    val canonicalArchiveDigest: String,
) {
    init {
        require(storeRevision >= 0)
        require(canonicalArchiveDigest.matches(Regex("^[0-9a-f]{64}$")))
        evidence.requireValid()
    }
}

fun interface LongitudinalRetrievalReadPort {
    fun read(asOfRevision: Long): RetrievalArchiveSnapshot
}

enum class RetrievalItemKind {
    SOURCE,
    ASSERTION,
    ENTITY,
    HYPOTHESIS,
    CONTRADICTION,
    CORRECTION,
    OPEN_QUESTION,
    RECURRENCE,
}

sealed interface RetrievedPayload {
    data class Source(val value: SourceRecord) : RetrievedPayload
    data class Assertion(val value: EvidenceAssertion) : RetrievedPayload
    data class Entity(val value: LifeEntity) : RetrievedPayload
    data class Hypothesis(val value: ThomasHypothesis) : RetrievedPayload
    data class Contradiction(val value: ContradictionRelation) : RetrievedPayload
    data class Correction(val value: CorrectionRelation) : RetrievedPayload
    data class OpenQuestion(val value: RetrievalOpenQuestion) : RetrievedPayload
    data class Recurrence(val value: RetrievalRecurrenceCandidate) : RetrievedPayload
}

enum class RetrievalReason {
    EXPLICIT_TARGET,
    DIRECT_EVIDENCE,
    SAME_RESOLVED_ENTITY,
    SAME_EVENT,
    SAME_RELATIONSHIP,
    SAME_PERIOD,
    SAME_PREDICATE,
    BIOGRAPHER_TARGET_DEPENDENCY,
    ACTIVE_CONTRADICTION,
    CURRENT_CORRECTION,
    EXPLICIT_LOOK_BACK_LEXICAL_MATCH,
    EXPLICIT_SOURCE_RECALL,
    CURRENT_STATE_MATCH,
    TEMPORAL_MATCH,
    REPRESENTATIVE_COUNTEREVIDENCE,
    EXPLANATION_AUDIT_TARGET,
}

data class RelevanceRank(
    val explicitTargetPenalty: Int,
    val directEvidencePenalty: Int,
    val entityPenalty: Int,
    val eventPenalty: Int,
    val relationshipPenalty: Int,
    val periodPenalty: Int,
    val predicatePenalty: Int,
    val lexicalPenalty: Int,
    val temporalPenalty: Int,
    val modePenalty: Int,
    val stableId: String,
) : Comparable<RelevanceRank> {
    override fun compareTo(other: RelevanceRank): Int {
        val left = listOf(explicitTargetPenalty, directEvidencePenalty, entityPenalty, eventPenalty,
            relationshipPenalty, periodPenalty, predicatePenalty, lexicalPenalty, temporalPenalty, modePenalty)
        val right = listOf(other.explicitTargetPenalty, other.directEvidencePenalty, other.entityPenalty,
            other.eventPenalty, other.relationshipPenalty, other.periodPenalty, other.predicatePenalty,
            other.lexicalPenalty, other.temporalPenalty, other.modePenalty)
        left.indices.forEach { index ->
            val compared = left[index].compareTo(right[index])
            if (compared != 0) return compared
        }
        return stableId.compareTo(other.stableId)
    }
}

data class RetrievedLongitudinalItem(
    val kind: RetrievalItemKind,
    val stableId: String,
    val payload: RetrievedPayload,
    val lifecycle: RetrievalLifecycle,
    val currentAuthority: Boolean,
    val epistemicRole: EvidenceEpistemicClass?,
    val uncertainty: String?,
    val eventTime: EventTime?,
    val reportTime: ReportTime?,
    val acquisitionMode: AcquisitionMode?,
    val sourceRevisionIds: List<SourceRecordId>,
    val entityIds: List<LifeEntityId>,
    val unresolvedIdentity: Boolean,
    val reasons: List<RetrievalReason>,
    val rank: RelevanceRank,
) {
    init { require(stableId.isNotBlank() && reasons.isNotEmpty()) }
}

data class SourceExcerptProposal(
    val itemStableId: String,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId,
    val acquisitionMode: AcquisitionMode,
    val reportTime: ReportTime,
    val eventTime: EventTime,
    val grounding: SourceSpanGrounding,
    val epistemicRole: EvidenceEpistemicClass,
    val lifecycle: RetrievalLifecycle,
    val reason: RetrievalReason,
)

data class RetrievalExclusions(
    val privateCount: Int = 0,
    val lifecycleCount: Int = 0,
    val supersededRevisionCount: Int = 0,
    val assistantAuthoredCount: Int = 0,
    val unresolvedIdentityAssumptionCount: Int = 0,
    val invalidGroundingCount: Int = 0,
    val budgetCount: Int = 0,
)

enum class RetrievalDisposition { RETRIEVED, EMPTY, REJECTED_AUTHORITY, REJECTED_INVALID_REQUEST, REVISION_UNAVAILABLE }

data class RetrievalResult(
    val disposition: RetrievalDisposition,
    val request: RetrievalRequest,
    val archiveDigest: String,
    val candidateCount: Int,
    val selectedItems: List<RetrievedLongitudinalItem>,
    val excerptProposals: List<SourceExcerptProposal>,
    val exclusions: RetrievalExclusions,
    val reasonCodes: List<String>,
) {
    init {
        require(selectedItems.size <= request.budget.maximumLongitudinalObjects)
        require(selectedItems.map { it.stableId }.distinct().size == selectedItems.size)
    }
}
