package com.conundrum.thomas.v2.engine.verticalslice

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.ontology.GovernanceReviewStatus
import com.conundrum.thomas.v2.ontology.OntologyConceptId
import com.conundrum.thomas.v2.provenance.GovernedSourceReference

private val policyIdFormat = Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")

@JvmInline
value class PolicyRuleId private constructor(val value: String) : Comparable<PolicyRuleId> {
    override fun compareTo(other: PolicyRuleId): Int = value.compareTo(other.value)

    companion object {
        fun parse(value: String): PolicyRuleId {
            require(policyIdFormat.matches(value)) { "Policy rule IDs must be lowercase and hyphenated." }
            return PolicyRuleId(value)
        }
    }
}

@JvmInline
value class PolicyActionId private constructor(val value: String) : Comparable<PolicyActionId> {
    override fun compareTo(other: PolicyActionId): Int = value.compareTo(other.value)

    companion object {
        fun parse(value: String): PolicyActionId {
            require(policyIdFormat.matches(value)) { "Policy action IDs must be lowercase and hyphenated." }
            return PolicyActionId(value)
        }
    }
}

enum class RuleKind {
    ARCHITECTURAL_SCOPE_GUARD,
    SOURCE_DERIVED_ACTION,
    SOURCE_DERIVED_SCOPE_BOUNDARY,
}

enum class RuleExecutionAuthority {
    CANDIDATE_RULE,
    EXECUTABLE_FOR_QUALIFICATION,
}

enum class ProductionTherapeuticAuthority {
    NOT_GRANTED,
}

enum class ReviewRestriction {
    CLINICAL_REVIEW_PENDING,
    RIGHTS_REVIEW_PENDING,
    IMPLEMENTATION_SCOPE_REVIEW_PENDING,
    SOFTWARE_AUTONOMY_REVIEW_PENDING,
    HUMAN_TRAINING_AND_SUPERVISION_ASSUMPTIONS_UNRESOLVED,
    PRODUCTION_AUTHORITY_NOT_GRANTED,
}

enum class SourceRuleRelationship {
    SUPPORTS_PROCEDURAL_STEP,
    BOUNDS_PROCEDURAL_SCOPE,
    IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR,
}

enum class RuleSourceConflictState {
    NONE_RECORDED,
    DIFFERENT_SCOPE_RECORDED,
    UNRESOLVED,
}

sealed interface RuleProvenance {
    val preciseLocator: String

    data class ClinicalSource(
        val source: GovernedSourceReference,
        val relationship: SourceRuleRelationship,
        override val preciseLocator: String,
        val population: String,
        val setting: String,
        val intendedDeliverer: String,
        val limitations: List<String>,
        val clinicalReviewStatus: GovernanceReviewStatus = GovernanceReviewStatus.PENDING,
        val rightsReviewStatus: GovernanceReviewStatus = GovernanceReviewStatus.PENDING,
        val conflictState: RuleSourceConflictState = RuleSourceConflictState.NONE_RECORDED,
    ) : RuleProvenance {
        init {
            require(preciseLocator.isNotBlank())
            require(population.isNotBlank() && setting.isNotBlank() && intendedDeliverer.isNotBlank())
            require(limitations.isNotEmpty() && limitations.none(String::isBlank))
            require(clinicalReviewStatus != GovernanceReviewStatus.COMPLETE)
            require(rightsReviewStatus != GovernanceReviewStatus.COMPLETE)
        }
    }

    data class GoverningArchitecture(
        val artifactId: String,
        override val preciseLocator: String,
    ) : RuleProvenance {
        init {
            require(artifactId.isNotBlank())
            require(preciseLocator.isNotBlank())
        }
    }
}

data class ConditionEvaluation(
    val conditionId: String,
    val matched: Boolean,
    val actual: String,
    val expected: String,
)

sealed interface PolicyCondition {
    val stableId: String
    fun evaluate(state: BoundedProblemPolicyState): ConditionEvaluation
}

private fun evaluation(id: String, actual: Any?, expected: Any?, matched: Boolean) = ConditionEvaluation(
    conditionId = id,
    matched = matched,
    actual = actual?.toString() ?: "UNKNOWN",
    expected = expected.toString(),
)

data class ModeIs(val expected: ThomasMode) : PolicyCondition {
    override val stableId = "condition-mode-is-${expected.name.lowercase()}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.mode, expected, state.mode == expected)
}

data class ModeIsNot(val prohibited: ThomasMode) : PolicyCondition {
    override val stableId = "condition-mode-is-not-${prohibited.name.lowercase()}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.mode, "not $prohibited", state.mode != prohibited)
}

data class SafetyDispositionIs(val expected: Set<UpstreamSafetyDisposition>) : PolicyCondition {
    init { require(expected.isNotEmpty()) }
    override val stableId = "condition-safety-is-${expected.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.upstreamSafetyDisposition, expected.sortedBy { it.name }, state.upstreamSafetyDisposition in expected)
}

data class ScopeIs(val expected: Set<BoundedProblemScope>) : PolicyCondition {
    init { require(expected.isNotEmpty()) }
    override val stableId = "condition-scope-is-${expected.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.scope, expected.sortedBy { it.name }, state.scope in expected)
}

data class SupportIntentIs(val expected: Set<SupportIntent>) : PolicyCondition {
    init { require(expected.isNotEmpty()) }
    override val stableId = "condition-intent-is-${expected.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.supportIntent.value, expected.sortedBy { it.name }, state.supportIntent.isEstablished() && state.supportIntent.value in expected)
}

data object SupportIntentMissing : PolicyCondition {
    override val stableId = "condition-support-intent-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.supportIntent.resolution, "not established", !state.supportIntent.isEstablished())
}

data class WillingnessIs(val expected: Set<ParticipationWillingness>) : PolicyCondition {
    init { require(expected.isNotEmpty()) }
    override val stableId = "condition-willingness-is-${expected.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.willingness.value, expected.sortedBy { it.name }, state.willingness.isEstablished() && state.willingness.value in expected)
}

data object WillingnessMissing : PolicyCondition {
    override val stableId = "condition-willingness-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.willingness.resolution, "not established", !state.willingness.isEstablished())
}

data object ProblemDescriptionMissing : PolicyCondition {
    override val stableId = "condition-problem-description-missing"
    override fun evaluate(state: BoundedProblemPolicyState): ConditionEvaluation {
        val missing = !state.problemStatement.isEstablished() || state.problemStatement.value.isNullOrBlank() ||
            !state.problemClarity.isEstablished() || state.problemClarity.value == ProblemClarity.VAGUE
        return evaluation(stableId, state.problemClarity.value, "known bounded problem", missing)
    }
}

data object BoundedProblemAvailable : PolicyCondition {
    override val stableId = "condition-bounded-problem-available"
    override fun evaluate(state: BoundedProblemPolicyState): ConditionEvaluation {
        val available = state.problemClarity.value == ProblemClarity.BOUNDED &&
            state.problemClarity.isEstablished() && state.problemStatement.isEstablished() && !state.problemStatement.value.isNullOrBlank()
        return evaluation(stableId, state.problemClarity.value, ProblemClarity.BOUNDED, available)
    }
}

data class SharedUnderstandingIs(val expected: Set<SharedUnderstanding>) : PolicyCondition {
    init { require(expected.isNotEmpty()) }
    override val stableId = "condition-understanding-is-${expected.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.sharedUnderstanding.value, expected.sortedBy { it.name }, state.sharedUnderstanding.isEstablished() && state.sharedUnderstanding.value in expected)
}

data object SharedUnderstandingMissing : PolicyCondition {
    override val stableId = "condition-shared-understanding-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.sharedUnderstanding.resolution, "not confirmed as reported", !state.sharedUnderstanding.isEstablished() || state.sharedUnderstanding.value == SharedUnderstanding.TENTATIVE)
}

data class ProblemInfluenceIs(val expected: Set<ProblemInfluence>) : PolicyCondition {
    init { require(expected.isNotEmpty()) }
    override val stableId = "condition-influence-is-${expected.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.problemInfluence.value, expected.sortedBy { it.name }, state.problemInfluence.isEstablished() && state.problemInfluence.value in expected)
}

data object ProblemInfluenceMissing : PolicyCondition {
    override val stableId = "condition-problem-influence-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.problemInfluence.resolution, "not established", !state.problemInfluence.isEstablished())
}

data object UserOptionsMissing : PolicyCondition {
    override val stableId = "condition-user-options-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.generatedOptions.value?.size, "no established options", !state.generatedOptions.isEstablished())
}

data object UserOptionsAvailable : PolicyCondition {
    override val stableId = "condition-user-options-available"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.generatedOptions.value?.size, "one or more established options", state.generatedOptions.isEstablished() && state.generatedOptions.value.orEmpty().isNotEmpty())
}

data object SelectedOptionMissing : PolicyCondition {
    override val stableId = "condition-selected-option-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.selectedOption.resolution, "not established", !state.selectedOption.isEstablished())
}

data object SelectedOptionAvailable : PolicyCondition {
    override val stableId = "condition-selected-option-available"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.selectedOption.value, "established selected option", state.selectedOption.isEstablished())
}

data object ActionPlanMissing : PolicyCondition {
    override val stableId = "condition-action-plan-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.actionPlan.resolution, "not established", !state.actionPlan.isEstablished())
}

data object ActionPlanAvailable : PolicyCondition {
    override val stableId = "condition-action-plan-available"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.actionPlan.value, "established action plan", state.actionPlan.isEstablished())
}

data object PlanOutcomeMissing : PolicyCondition {
    override val stableId = "condition-plan-outcome-missing"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.planOutcome.resolution, "not established", !state.planOutcome.isEstablished())
}

data object PlanOutcomeAvailable : PolicyCondition {
    override val stableId = "condition-plan-outcome-available"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.planOutcome.value, "established outcome", state.planOutcome.isEstablished())
}

data object ConflictingEvidencePresent : PolicyCondition {
    override val stableId = "condition-conflicting-evidence-present"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.hasConflictingEvidence(), true, state.hasConflictingEvidence())
}

data object AwaitingInformationPresent : PolicyCondition {
    override val stableId = "condition-awaiting-information-present"
    override fun evaluate(state: BoundedProblemPolicyState) =
        evaluation(stableId, state.awaitingInformation, "an outstanding expected response", state.awaitingInformation != null)
}

enum class PolicyDecisionDisposition {
    ACTION_SELECTED,
    INSUFFICIENT_INFORMATION,
    POLICY_CONFLICT,
    REVIEW_BLOCKED,
    NO_AUTHORIZED_ACTION,
    OUT_OF_SCOPE,
    SPECIALIZED_POLICY_REQUIRED,
    INVALID_INPUT,
}

sealed interface RuleResult {
    data class SelectAction(val actionId: PolicyActionId) : RuleResult
    data class Terminate(
        val disposition: PolicyDecisionDisposition,
        val handoffRequirement: String? = null,
        val unresolvedRequirement: InformationRequirement? = null,
    ) : RuleResult {
        init { require(disposition != PolicyDecisionDisposition.ACTION_SELECTED) }
    }
}

data class ProceduralRule(
    val id: PolicyRuleId,
    val description: String,
    val kind: RuleKind,
    val priority: Int,
    val preconditions: List<PolicyCondition>,
    val exclusions: List<PolicyCondition>,
    val result: RuleResult,
    val provenance: List<RuleProvenance>,
    val executionAuthority: RuleExecutionAuthority,
    val unresolvedReviewRestrictions: Set<ReviewRestriction>,
    val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
) {
    init {
        require(description.isNotBlank())
        require(priority >= 0)
        require(preconditions.isNotEmpty())
        require(provenance.isNotEmpty()) { "Rule ${id.value} requires exact provenance." }
        if (kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD) {
            require(provenance.any { it is RuleProvenance.ClinicalSource }) {
                "Source-derived rule ${id.value} requires governed clinical provenance."
            }
        }
        require(ReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED in unresolvedReviewRestrictions)
        require(productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
    }
}

data class RuleEvaluationTrace(
    val ruleId: PolicyRuleId,
    val priority: Int,
    val preconditions: List<ConditionEvaluation>,
    val exclusions: List<ConditionEvaluation>,
    val matched: Boolean,
    val rejectionReasons: List<String>,
    val executionAuthority: RuleExecutionAuthority,
)

internal fun ProceduralRule.evaluate(state: BoundedProblemPolicyState): RuleEvaluationTrace {
    val prerequisiteResults = preconditions.map { it.evaluate(state) }
    val exclusionResults = exclusions.map { it.evaluate(state) }
    val failedPrerequisites = prerequisiteResults.filterNot { it.matched }
    val triggeredExclusions = exclusionResults.filter { it.matched }
    return RuleEvaluationTrace(
        ruleId = id,
        priority = priority,
        preconditions = prerequisiteResults,
        exclusions = exclusionResults,
        matched = failedPrerequisites.isEmpty() && triggeredExclusions.isEmpty(),
        rejectionReasons = failedPrerequisites.map { "PREREQUISITE_FAILED:${it.conditionId}" } +
            triggeredExclusions.map { "EXCLUSION_MATCHED:${it.conditionId}" },
        executionAuthority = executionAuthority,
    )
}

internal fun conceptId(value: String): OntologyConceptId = OntologyConceptId.parse(value)
