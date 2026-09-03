package com.conundrum.thomas.v2.engine.verticalslice

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.StructuredPolicyState
import com.conundrum.thomas.v2.ontology.EpistemicResolution

enum class UpstreamSafetyDisposition {
    ORDINARY_SLICE_ALLOWED,
    UNKNOWN,
    SPECIALIZED_POLICY_REQUIRED,
}

enum class BoundedProblemScope {
    BOUNDED_NON_EMERGENCY_PERSONAL_PROBLEM,
    UNKNOWN,
    OUT_OF_SCOPE,
    SPECIALIZED_POLICY_REQUIRED,
}

enum class SupportIntent {
    LISTENING,
    UNDERSTANDING,
    PRACTICAL_HELP,
}

enum class ParticipationWillingness {
    WILLING_TO_EXPLORE,
    WILLING_TO_ACT,
    UNWILLING_TO_CONTINUE,
}

enum class ProblemClarity {
    VAGUE,
    BOUNDED,
}

enum class SharedUnderstanding {
    TENTATIVE,
    CONFIRMED,
}

enum class ProblemInfluence {
    AT_LEAST_PARTLY_INFLUENCEABLE,
    NOT_INFLUENCEABLE,
}

enum class PlanOutcome {
    NOT_ATTEMPTED,
    PARTLY_ATTEMPTED,
    ATTEMPTED,
}

enum class ResponsePreference {
    NO_RESPONSE,
    MINIMAL,
    CONVERSATIONAL,
    ACTIVE,
}

enum class InformationRequirement {
    SUPPORT_INTENT,
    BOUNDED_PROBLEM_DESCRIPTION,
    SHARED_UNDERSTANDING_CONFIRMATION,
    INFLUENCEABLE_PART,
    READINESS_FOR_OPTIONS,
    USER_GENERATED_OPTIONS,
    USER_SELECTED_OPTION,
    BOUNDED_ACTION_PLAN,
    REPORTED_PLAN_OUTCOME,
    OUTCOME_MEANING_OR_OBSTACLE,
}

/**
 * A typed input value whose epistemic status remains visible to policy.
 * This is immutable evidence and has no durable-profile admission operation.
 */
data class PolicyEvidence<T>(
    val value: T?,
    val resolution: EpistemicResolution,
    val evidenceReferences: Set<String>,
) {
    fun validationErrors(fieldName: String): List<String> = buildList {
        if (resolution == EpistemicResolution.UNKNOWN && value != null) {
            add("$fieldName: UNKNOWN evidence cannot carry a value")
        }
        if (resolution in valueRequiredResolutions && value == null) {
            add("$fieldName: $resolution evidence requires a value")
        }
        if (resolution == EpistemicResolution.CONFLICTING && evidenceReferences.size < 2) {
            add("$fieldName: CONFLICTING evidence requires at least two references")
        }
        if (resolution !in setOf(EpistemicResolution.UNKNOWN, EpistemicResolution.INSUFFICIENT_EVIDENCE) &&
            evidenceReferences.isEmpty()
        ) {
            add("$fieldName: resolved, tentative, unresolved, or conflicting evidence requires a reference")
        }
        if (evidenceReferences.any(String::isBlank)) add("$fieldName: evidence references cannot be blank")
    }

    fun isKnown(): Boolean = value != null && resolution in valueRequiredResolutions

    fun isEstablished(): Boolean = value != null && resolution == EpistemicResolution.RESOLVED_AS_REPORTED

    fun isMissing(): Boolean = value == null && resolution in setOf(
        EpistemicResolution.UNKNOWN,
        EpistemicResolution.INSUFFICIENT_EVIDENCE,
        EpistemicResolution.UNRESOLVED,
    )

    fun isConflicting(): Boolean = resolution == EpistemicResolution.CONFLICTING

    companion object {
        private val valueRequiredResolutions = setOf(
            EpistemicResolution.RESOLVED_AS_REPORTED,
            EpistemicResolution.TENTATIVE,
        )

        fun <T> unknown(): PolicyEvidence<T> = PolicyEvidence(null, EpistemicResolution.UNKNOWN, emptySet())

        fun <T> insufficient(vararg evidenceReferences: String): PolicyEvidence<T> =
            PolicyEvidence(null, EpistemicResolution.INSUFFICIENT_EVIDENCE, evidenceReferences.toSet())

        fun <T> reported(value: T, evidenceReference: String): PolicyEvidence<T> =
            PolicyEvidence(value, EpistemicResolution.RESOLVED_AS_REPORTED, setOf(evidenceReference))

        fun <T> tentative(value: T, evidenceReference: String): PolicyEvidence<T> =
            PolicyEvidence(value, EpistemicResolution.TENTATIVE, setOf(evidenceReference))

        fun <T> conflicting(vararg evidenceReferences: String): PolicyEvidence<T> =
            PolicyEvidence(null, EpistemicResolution.CONFLICTING, evidenceReferences.toSet())
    }
}

data class BoundedProblemPolicyState(
    val stateId: String,
    val mode: ThomasMode,
    val upstreamSafetyDisposition: UpstreamSafetyDisposition,
    val scope: BoundedProblemScope,
    val supportIntent: PolicyEvidence<SupportIntent> = PolicyEvidence.unknown(),
    val problemStatement: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val problemClarity: PolicyEvidence<ProblemClarity> = PolicyEvidence.unknown(),
    val sharedUnderstanding: PolicyEvidence<SharedUnderstanding> = PolicyEvidence.unknown(),
    val willingness: PolicyEvidence<ParticipationWillingness> = PolicyEvidence.unknown(),
    val problemInfluence: PolicyEvidence<ProblemInfluence> = PolicyEvidence.unknown(),
    val generatedOptions: PolicyEvidence<List<String>> = PolicyEvidence.unknown(),
    val selectedOption: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val actionPlan: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val planOutcome: PolicyEvidence<PlanOutcome> = PolicyEvidence.unknown(),
    val awaitingInformation: InformationRequirement? = null,
    val priorActionIds: List<PolicyActionId> = emptyList(),
    val responsePreference: ResponsePreference = ResponsePreference.CONVERSATIONAL,
) : StructuredPolicyState {
    fun validationErrors(): List<String> = buildList {
        if (!stateId.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$"))) {
            add("stateId must be a lowercase hyphenated identifier")
        }
        listOf(
            "supportIntent" to supportIntent,
            "problemStatement" to problemStatement,
            "problemClarity" to problemClarity,
            "sharedUnderstanding" to sharedUnderstanding,
            "willingness" to willingness,
            "problemInfluence" to problemInfluence,
            "generatedOptions" to generatedOptions,
            "selectedOption" to selectedOption,
            "actionPlan" to actionPlan,
            "planOutcome" to planOutcome,
        ).forEach { (name, evidence) -> addAll(evidence.validationErrors(name)) }

        if (problemClarity.value == ProblemClarity.BOUNDED && problemClarity.isEstablished() &&
            (problemStatement.value.isNullOrBlank() || !problemStatement.isEstablished())
        ) {
            add("a bounded problem requires a known non-blank problem statement")
        }
        if (generatedOptions.isKnown() && generatedOptions.value.orEmpty().any(String::isBlank)) {
            add("generated options cannot contain blank values")
        }
        if (generatedOptions.isKnown() && generatedOptions.value.orEmpty().isEmpty()) {
            add("known generated options cannot be empty")
        }
        if (selectedOption.isKnown() && generatedOptions.isKnown() &&
            selectedOption.value !in generatedOptions.value.orEmpty()
        ) {
            add("the selected option must be one of the user-generated options")
        }
        if (actionPlan.isKnown() && !selectedOption.isKnown()) {
            add("an action plan requires a selected option")
        }
        if (planOutcome.value in setOf(PlanOutcome.PARTLY_ATTEMPTED, PlanOutcome.ATTEMPTED) && !actionPlan.isKnown()) {
            add("an attempted outcome requires a known action plan")
        }
    }

    fun hasConflictingEvidence(): Boolean = listOf(
        supportIntent,
        problemStatement,
        problemClarity,
        sharedUnderstanding,
        willingness,
        problemInfluence,
        generatedOptions,
        selectedOption,
        actionPlan,
        planOutcome,
    ).any { it.isConflicting() }
}
