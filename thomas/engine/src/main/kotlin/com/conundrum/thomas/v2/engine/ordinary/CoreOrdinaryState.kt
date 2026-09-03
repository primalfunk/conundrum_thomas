package com.conundrum.thomas.v2.engine.ordinary

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.StructuredPolicyState
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.ProblemInfluence
import com.conundrum.thomas.v2.engine.verticalslice.ResponsePreference
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import com.conundrum.thomas.v2.ontology.EpistemicResolution
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision

private val coreStateIdFormat = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")

enum class OrdinaryRoute {
    CLARIFY_PREFERENCE,
    LISTEN_SUPPORT,
    UNDERSTAND_CLARIFY,
    PRACTICAL_PROBLEM_SOLVING,
    CONSOLIDATE_CLOSE,
}

enum class RequestedOrdinarySupport {
    LISTEN,
    UNDERSTAND,
    PRACTICAL_HELP,
    DECISION_SUPPORT,
}

enum class OrdinaryEngagement {
    ENGAGED,
    DOES_NOT_WANT_TOPIC,
    PAUSE_REQUESTED,
    CLOSE_REQUESTED,
}

enum class ExpressionProgress {
    NO_CONTENT_YET,
    NEW_CONTENT_AVAILABLE,
    REFLECTION_RECEIVED,
    USER_WANTS_TO_CONTINUE,
    EXPRESSION_COMPLETE,
    SUMMARY_DELIVERED,
}

enum class CorrectionStatus { NONE, UNHANDLED, ACKNOWLEDGED }

enum class PlanReviewStatus { NOT_REVIEWED, REVIEWED }

enum class CoreInformationRequirement {
    SUPPORT_PREFERENCE,
    USER_EXPRESSION,
    RESPONSE_TO_REFLECTION,
    MORE_USER_EXPRESSION,
    CLOSURE_OR_NEW_DIRECTION,
    PRESENT_CONCERN,
    IMPORTANT_MISSING_INFORMATION,
    SHARED_UNDERSTANDING_CONFIRMATION,
    CORRECTED_MEANING,
    INFLUENCEABLE_PART,
    READINESS_FOR_OPTIONS,
    USER_GENERATED_OPTIONS,
    USER_SELECTED_OPTION,
    BOUNDED_ACTION_PLAN,
    REPORTED_PLAN_OUTCOME,
    OUTCOME_MEANING_OR_OBSTACLE,
    NONE,
}

data class CoreActionExecution(
    val actionId: PolicyActionId,
    val conversationRevision: Long,
    val route: OrdinaryRoute,
) {
    init { require(conversationRevision > 0) }
}

data class ExplicitRepeatAuthorization(
    val actionId: PolicyActionId,
    val conversationRevision: Long,
    val reason: String,
    val evidenceReference: String,
    val maximumAdditionalExecutions: Int = 1,
) {
    init {
        require(conversationRevision > 0)
        require(reason.isNotBlank())
        require(evidenceReference.isNotBlank())
        require(maximumAdditionalExecutions == 1) { "CT-V2-05 permits at most one explicitly justified repeat." }
    }
}

/**
 * Immutable, structured qualification input. It accepts no raw user prose for route selection and
 * exposes no durable-profile mutation operation.
 */
data class CoreOrdinaryTherapyState(
    val stateId: String,
    val safetyEvidenceRevision: SafetyEvidenceRevision,
    val conversationRevision: Long,
    val mode: ThomasMode,
    val activeRoute: OrdinaryRoute? = null,
    val routePreference: PolicyEvidence<RequestedOrdinarySupport> = PolicyEvidence.unknown(),
    val engagement: PolicyEvidence<OrdinaryEngagement> = PolicyEvidence.unknown(),
    val concernStatement: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val problemClarity: PolicyEvidence<ProblemClarity> = PolicyEvidence.unknown(),
    val expressionProgress: PolicyEvidence<ExpressionProgress> = PolicyEvidence.unknown(),
    val thomasUnderstanding: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val sharedUnderstanding: PolicyEvidence<SharedUnderstanding> = PolicyEvidence.unknown(),
    val importantMissingInformation: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val understandingSummaryDelivered: PolicyEvidence<Boolean> = PolicyEvidence.unknown(),
    val correctionStatus: PolicyEvidence<CorrectionStatus> = PolicyEvidence.unknown(),
    val userCorrection: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val withdrawnInterpretationReferences: Set<String> = emptySet(),
    val willingness: PolicyEvidence<ParticipationWillingness> = PolicyEvidence.unknown(),
    val problemInfluence: PolicyEvidence<ProblemInfluence> = PolicyEvidence.unknown(),
    val generatedOptions: PolicyEvidence<List<String>> = PolicyEvidence.unknown(),
    val selectedOption: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val actionPlan: PolicyEvidence<String> = PolicyEvidence.unknown(),
    val planOutcome: PolicyEvidence<PlanOutcome> = PolicyEvidence.unknown(),
    val planReviewStatus: PolicyEvidence<PlanReviewStatus> = PolicyEvidence.unknown(),
    val pendingInformation: CoreInformationRequirement? = null,
    val actionHistory: List<CoreActionExecution> = emptyList(),
    val explicitRepeatAuthorization: ExplicitRepeatAuthorization? = null,
    val responsePreference: ResponsePreference = ResponsePreference.CONVERSATIONAL,
) : StructuredPolicyState {
    fun validationErrors(): List<String> = buildList {
        if (!coreStateIdFormat.matches(stateId)) add("stateId must be a lowercase hyphenated identifier")
        if (conversationRevision <= 0) add("conversationRevision must be positive")

        listOf(
            "routePreference" to routePreference,
            "engagement" to engagement,
            "concernStatement" to concernStatement,
            "problemClarity" to problemClarity,
            "expressionProgress" to expressionProgress,
            "thomasUnderstanding" to thomasUnderstanding,
            "sharedUnderstanding" to sharedUnderstanding,
            "importantMissingInformation" to importantMissingInformation,
            "understandingSummaryDelivered" to understandingSummaryDelivered,
            "correctionStatus" to correctionStatus,
            "userCorrection" to userCorrection,
            "willingness" to willingness,
            "problemInfluence" to problemInfluence,
            "generatedOptions" to generatedOptions,
            "selectedOption" to selectedOption,
            "actionPlan" to actionPlan,
            "planOutcome" to planOutcome,
            "planReviewStatus" to planReviewStatus,
        ).forEach { (name, evidence) -> addAll(evidence.validationErrors(name)) }

        if (listOf(concernStatement.value, thomasUnderstanding.value, importantMissingInformation.value, userCorrection.value,
                selectedOption.value, actionPlan.value).any { it != null && it.isBlank() }
        ) add("known text evidence cannot be blank")

        if (problemClarity.isEstablished() && problemClarity.value == ProblemClarity.BOUNDED &&
            (!concernStatement.isEstablished() || concernStatement.value.isNullOrBlank())
        ) add("a bounded problem requires an established non-blank concern statement")

        if (expressionProgress.isEstablished() && expressionProgress.value != ExpressionProgress.NO_CONTENT_YET &&
            (!concernStatement.isEstablished() || concernStatement.value.isNullOrBlank())
        ) add("expression progress beyond NO_CONTENT_YET requires established content")

        if (sharedUnderstanding.value == SharedUnderstanding.TENTATIVE &&
            thomasUnderstanding.resolution != EpistemicResolution.TENTATIVE
        ) add("tentative shared understanding requires a visible tentative Thomas understanding")

        if (sharedUnderstanding.isEstablished() && sharedUnderstanding.value == SharedUnderstanding.CONFIRMED &&
            (!thomasUnderstanding.isEstablished() || thomasUnderstanding.value.isNullOrBlank())
        ) add("confirmed shared understanding requires user-established understanding text")

        if (correctionStatus.isEstablished() && correctionStatus.value == CorrectionStatus.UNHANDLED) {
            if (!userCorrection.isEstablished() || userCorrection.value.isNullOrBlank()) {
                add("an unhandled correction requires established user correction evidence")
            }
            if (withdrawnInterpretationReferences.isEmpty()) {
                add("an unhandled correction must identify the withdrawn tentative interpretation")
            }
            if (thomasUnderstanding.isKnown()) {
                add("a corrected Thomas interpretation must be withdrawn rather than retained as current understanding")
            }
        }
        if (withdrawnInterpretationReferences.any(String::isBlank)) {
            add("withdrawn interpretation references cannot be blank")
        }

        if (generatedOptions.isKnown() && generatedOptions.value.orEmpty().isEmpty()) {
            add("known generated options cannot be empty")
        }
        if (generatedOptions.isKnown() && generatedOptions.value.orEmpty().any(String::isBlank)) {
            add("generated options cannot contain blank values")
        }
        if (selectedOption.isKnown() && generatedOptions.isKnown() &&
            selectedOption.value !in generatedOptions.value.orEmpty()
        ) add("the selected option must be one of the user-generated options")
        if (actionPlan.isKnown() && !selectedOption.isKnown()) add("an action plan requires a selected option")
        if (planOutcome.value in setOf(PlanOutcome.PARTLY_ATTEMPTED, PlanOutcome.ATTEMPTED) && !actionPlan.isKnown()) {
            add("an attempted outcome requires a known action plan")
        }

        if (actionHistory.any { it.conversationRevision > conversationRevision }) {
            add("action history cannot refer to a future conversation revision")
        }
        explicitRepeatAuthorization?.let {
            if (it.conversationRevision != conversationRevision) {
                add("repeat authorization must apply to the current conversation revision")
            }
        }
    }

    fun hasMaterialConflict(): Boolean = listOf(
        routePreference,
        engagement,
        concernStatement,
        problemClarity,
        expressionProgress,
        thomasUnderstanding,
        sharedUnderstanding,
        importantMissingInformation,
        understandingSummaryDelivered,
        correctionStatus,
        userCorrection,
        willingness,
        problemInfluence,
        generatedOptions,
        selectedOption,
        actionPlan,
        planOutcome,
        planReviewStatus,
    ).any { it.isConflicting() }
}
