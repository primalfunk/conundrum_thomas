package com.conundrum.thomas.v2.runtime

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.ordinary.*
import com.conundrum.thomas.v2.engine.verticalslice.*
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision
import java.util.Locale

/** Ephemeral observation/session bridge; no policy selection or longitudinal writes.
 * Explicit bounded declarations and replies to delivered CT-V2-05 questions only.
 * Unsupported prose establishes neither novelty nor semantic equivalence.
 */
internal class ProductionTherapyInputBoundary {
    private var session = CoreOrdinaryTherapyState("session", SafetyEvidenceRevision.of(1), 1, ThomasMode.THERAPIST)
    private var lastMeaning: List<Any?>? = null
    private var revision = 0L

    fun observe(request: ProductionTurnRequest): CoreOrdinaryTherapyState {
        val ref = "current-turn-${request.clientTurnIndex}"
        var next = session.copy(routePreference = PolicyEvidence.reported(request.requestedTherapySupport, "$ref-support-declaration"))
        val text = request.committedText.trim()
        val normalized = normalize(text)
        fun report(value: String) = PolicyEvidence.reported(value, ref)
        fun payload(prefix: String): String? = text.takeIf { it.startsWith(prefix, true) }
            ?.substring(prefix.length)?.trim()?.takeIf { it.isNotBlank() }
        fun tentative(value: String) {
            next = next.copy(thomasUnderstanding = PolicyEvidence.tentative(value, "$ref-literal-restatement"),
                sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "$ref-literal-restatement"),
                understandingSummaryDelivered = PolicyEvidence.unknown())
        }
        when {
            normalized == "i want to begin" -> next = next.copy(engagement = PolicyEvidence.reported(OrdinaryEngagement.ENGAGED, ref))
            normalized in setOf("pause", "please pause", "i want to pause") ->
                next = next.copy(engagement = PolicyEvidence.reported(OrdinaryEngagement.PAUSE_REQUESTED, ref))
            normalized in setOf("stop", "please stop", "i want to stop") ->
                next = next.copy(engagement = PolicyEvidence.reported(OrdinaryEngagement.CLOSE_REQUESTED, ref))
            normalized in setOf("i don't want to discuss this", "i do not want to discuss this", "i don't want to continue") ->
                next = next.copy(engagement = PolicyEvidence.reported(OrdinaryEngagement.DOES_NOT_WANT_TOPIC, ref))
            normalized in setOf("i want to continue", "let's continue", "i am ready to resume") -> {
                next = next.copy(engagement = PolicyEvidence.reported(OrdinaryEngagement.ENGAGED, ref), willingness = PolicyEvidence.unknown())
                if (next.concernStatement.isEstablished()) next = next.copy(expressionProgress = PolicyEvidence.reported(ExpressionProgress.USER_WANTS_TO_CONTINUE, ref))
            }
            normalized in setOf("no that's not what i mean", "no that is not what i mean") && next.thomasUnderstanding.isKnown() -> {
                next = next.copy(correctionStatus = PolicyEvidence.reported(CorrectionStatus.UNHANDLED, ref), userCorrection = report(text),
                    withdrawnInterpretationReferences = next.withdrawnInterpretationReferences + next.thomasUnderstanding.evidenceReferences,
                    thomasUnderstanding = PolicyEvidence.unknown(), sharedUnderstanding = PolicyEvidence.unknown(),
                    understandingSummaryDelivered = PolicyEvidence.unknown(), importantMissingInformation = PolicyEvidence.unknown(),
                    problemInfluence = PolicyEvidence.unknown(), willingness = PolicyEvidence.unknown(),
                    generatedOptions = PolicyEvidence.unknown(), selectedOption = PolicyEvidence.unknown(),
                    actionPlan = PolicyEvidence.unknown(), planOutcome = PolicyEvidence.unknown(), planReviewStatus = PolicyEvidence.unknown())
            }
            payload("My specific concern is: ") != null -> {
                val value = requireNotNull(payload("My specific concern is: "))
                if (normalize(value) != next.concernStatement.value?.let(::normalize)) {
                    // User-declared single specific concern, not inferred boundedness from a support button.
                    next = CoreOrdinaryTherapyState(next.stateId, next.safetyEvidenceRevision, next.conversationRevision,
                        next.mode, next.activeRoute, routePreference = next.routePreference, actionHistory = next.actionHistory,
                        concernStatement = report(value), problemClarity = PolicyEvidence.reported(ProblemClarity.BOUNDED, ref),
                        expressionProgress = PolicyEvidence.reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, ref),
                        engagement = PolicyEvidence.reported(OrdinaryEngagement.ENGAGED, ref))
                    if (request.requestedTherapySupport != RequestedOrdinarySupport.LISTEN) tentative(value)
                }
            }
            payload("What I haven't explained is: ") != null && next.concernStatement.isEstablished() ->
                next = next.copy(importantMissingInformation = report(requireNotNull(payload("What I haven't explained is: "))))
            next.pendingInformation == CoreInformationRequirement.IMPORTANT_MISSING_INFORMATION && payload("The missing detail is: ") != null -> {
                next = next.copy(importantMissingInformation = PolicyEvidence.unknown())
                tentative(requireNotNull(next.concernStatement.value) + "; " + requireNotNull(payload("The missing detail is: ")))
            }
            next.pendingInformation == CoreInformationRequirement.CORRECTED_MEANING && payload("What I mean is: ") != null -> {
                val value = requireNotNull(payload("What I mean is: "))
                next = next.copy(concernStatement = report(value), userCorrection = report(value))
                tentative(value)
            }
            next.pendingInformation == CoreInformationRequirement.SHARED_UNDERSTANDING_CONFIRMATION && next.actionHistory.lastOrNull()?.actionId != CoreOrdinaryActions.offerDirectionChoice.id &&
                normalized in setOf("yes", "yes that's right", "yes that is right", "that's right") && next.thomasUnderstanding.isKnown() ->
                next = next.copy(thomasUnderstanding = report(requireNotNull(next.thomasUnderstanding.value)),
                    sharedUnderstanding = PolicyEvidence.reported(SharedUnderstanding.CONFIRMED, ref))
            next.pendingInformation == CoreInformationRequirement.RESPONSE_TO_REFLECTION && next.actionHistory.lastOrNull()?.actionId != CoreOrdinaryActions.offerDirectionChoice.id &&
                normalized in setOf("yes", "yes that's right", "yes that is right") ->
                next = next.copy(expressionProgress = PolicyEvidence.reported(ExpressionProgress.REFLECTION_RECEIVED, ref))
            normalized in setOf("that's all for now", "that is all for now", "i have finished explaining") && next.concernStatement.isEstablished() ->
                next = next.copy(expressionProgress = PolicyEvidence.reported(ExpressionProgress.EXPRESSION_COMPLETE, ref))
            payload("Another detail is: ") != null && next.concernStatement.isEstablished() -> {
                val detail = requireNotNull(payload("Another detail is: "))
                val parts = requireNotNull(next.concernStatement.value).split("; ")
                if (parts.none { normalize(it) == normalize(detail) }) {
                    next = next.copy(concernStatement = report(parts.plus(detail).joinToString("; ")),
                        expressionProgress = PolicyEvidence.reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, ref))
                    if (request.requestedTherapySupport != RequestedOrdinarySupport.LISTEN) tentative(requireNotNull(next.concernStatement.value))
                }
            }
            next.pendingInformation == CoreInformationRequirement.INFLUENCEABLE_PART && payload("I can influence: ") != null ->
                next = next.copy(problemInfluence = PolicyEvidence.reported(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE, ref))
            next.pendingInformation == CoreInformationRequirement.INFLUENCEABLE_PART && normalized == "i cannot influence this problem" ->
                next = next.copy(problemInfluence = PolicyEvidence.reported(ProblemInfluence.NOT_INFLUENCEABLE, ref))
            next.pendingInformation == CoreInformationRequirement.READINESS_FOR_OPTIONS && normalized == "i am willing to act" ->
                next = next.copy(willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_ACT, ref))
            next.pendingInformation == CoreInformationRequirement.READINESS_FOR_OPTIONS && normalized in setOf("no", "not now") ->
                next = next.copy(willingness = PolicyEvidence.reported(ParticipationWillingness.UNWILLING_TO_CONTINUE, ref))
            next.pendingInformation == CoreInformationRequirement.USER_GENERATED_OPTIONS && payload("My options are: ") != null -> {
                val options = requireNotNull(payload("My options are: ")).split("; ").map(String::trim).filter(String::isNotBlank).distinct()
                if (options.isNotEmpty()) next = next.copy(generatedOptions = PolicyEvidence.reported(options, ref))
            }
            next.pendingInformation == CoreInformationRequirement.USER_SELECTED_OPTION && payload("I choose: ") != null -> {
                val choice = requireNotNull(payload("I choose: "))
                val option = next.generatedOptions.value.orEmpty().singleOrNull { normalize(it) == normalize(choice) }
                if (option != null) next = next.copy(selectedOption = report(option))
            }
            next.pendingInformation == CoreInformationRequirement.BOUNDED_ACTION_PLAN && payload("My first step is: ") != null -> {
                val plan = requireNotNull(payload("My first step is: "))
                val parts = plan.split("; when: ", limit = 2)
                if (parts.size == 2 && parts.all(String::isNotBlank)) next = next.copy(actionPlan = report(plan))
            }
            next.pendingInformation == CoreInformationRequirement.REPORTED_PLAN_OUTCOME -> {
                val outcome = when (normalized) {
                    "i attempted the plan" -> PlanOutcome.ATTEMPTED
                    "i partly attempted the plan" -> PlanOutcome.PARTLY_ATTEMPTED
                    "i have not attempted the plan" -> PlanOutcome.NOT_ATTEMPTED
                    else -> null
                }
                if (outcome != null) next = next.copy(planOutcome = PolicyEvidence.reported(outcome, ref))
            }
            next.pendingInformation == CoreInformationRequirement.OUTCOME_MEANING_OR_OBSTACLE && payload("What happened was: ") != null ->
                next = next.copy(planReviewStatus = PolicyEvidence.reported(PlanReviewStatus.REVIEWED, ref))
        }
        val meaning = meaning(next)
        if (meaning != lastMeaning) revision += 1
        lastMeaning = meaning
        session = next.copy(stateId = "android-therapy-state-${request.clientTurnIndex}",
            safetyEvidenceRevision = SafetyEvidenceRevision.of(request.clientTurnIndex), conversationRevision = revision.coerceAtLeast(1))
        return session
    }

    fun delivered(actionId: String, route: OrdinaryRoute) {
        val action = CoreOrdinaryActions.byId[PolicyActionId.parse(actionId)] ?: return
        val ref = "delivered-$actionId-${session.stateId}"
        session = session.copy(activeRoute = route, pendingInformation = if (action == CoreOrdinaryActions.offerDirectionChoice) session.pendingInformation else action.outcomeContract.expectedInformation,
            actionHistory = session.actionHistory + CoreActionExecution(action.id, session.conversationRevision, route))
        when (action) {
            CoreOrdinaryActions.summarizeListening -> session = session.copy(expressionProgress = PolicyEvidence.reported(ExpressionProgress.SUMMARY_DELIVERED, ref))
            CoreOrdinaryActions.summarizeSharedUnderstanding -> session = session.copy(understandingSummaryDelivered = PolicyEvidence.reported(true, ref))
            CoreOrdinaryActions.acknowledgeCorrection -> session = session.copy(correctionStatus = PolicyEvidence.reported(CorrectionStatus.ACKNOWLEDGED, ref))
            else -> Unit
        }
    }

    private fun meaning(s: CoreOrdinaryTherapyState): List<Any?> = listOf(
        s.routePreference, s.engagement, s.concernStatement, s.problemClarity, s.expressionProgress, s.thomasUnderstanding,
        s.sharedUnderstanding, s.importantMissingInformation, s.understandingSummaryDelivered, s.correctionStatus,
        s.userCorrection, s.willingness, s.problemInfluence, s.generatedOptions, s.selectedOption, s.actionPlan,
        s.planOutcome, s.planReviewStatus,
    ).map { listOf(it.resolution, when (val v = it.value) { is String -> normalize(v); is List<*> -> v.map { normalize(it.toString()) }; else -> v }) }

    companion object {
        fun startsObservationBlock(text: String): Boolean = listOf(
            "My specific concern is: ", "What I haven't explained is: ", "The missing detail is: ",
            "What I mean is: ", "Another detail is: ", "I can influence: ", "My options are: ",
            "I choose: ", "My first step is: ", "What happened was: ", "Please recall my earlier words: ",
        ).any { text.startsWith(it, true) }

        fun normalize(value: String): String = value.lowercase(Locale.ROOT).replace('’', '\'')
            .replace(Regex("\\s+"), " ").trim().trimEnd('.', ',', '!', '?')
            .replace(Regex("^(yes|no),\\s+"), "$1 ")
    }
}
