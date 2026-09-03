package com.conundrum.thomas.v2.engine.ordinary

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.engine.verticalslice.PolicyRuleId
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.ProblemInfluence
import com.conundrum.thomas.v2.engine.verticalslice.ProductionTherapeuticAuthority
import com.conundrum.thomas.v2.engine.verticalslice.ReviewRestriction
import com.conundrum.thomas.v2.engine.verticalslice.RuleExecutionAuthority
import com.conundrum.thomas.v2.engine.verticalslice.RuleKind
import com.conundrum.thomas.v2.engine.verticalslice.RuleProvenance
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import com.conundrum.thomas.v2.ontology.OntologyConceptId
import com.conundrum.thomas.v2.safety.OrdinaryTherapyPermit

sealed interface CoreActionRuleResult {
    data class SelectAction(val actionId: PolicyActionId) : CoreActionRuleResult
    data class Terminate(val disposition: CorePolicyDisposition, val handoffRequirement: String) : CoreActionRuleResult
}

class CoreActionCondition(
    val stableId: String,
    private val expected: String,
    private val actual: (CoreOrdinaryTherapyState, OrdinaryRoute) -> String,
    private val predicate: (CoreOrdinaryTherapyState, OrdinaryRoute) -> Boolean,
) {
    init { require(stableId.isNotBlank() && expected.isNotBlank()) }

    fun evaluate(state: CoreOrdinaryTherapyState, route: OrdinaryRoute) = CoreConditionEvaluation(
        stableId,
        predicate(state, route),
        actual(state, route),
        expected,
    )
}

data class CoreActionRule(
    override val id: PolicyRuleId,
    override val description: String,
    override val kind: RuleKind,
    override val priority: Int,
    val route: OrdinaryRoute,
    val preconditions: List<CoreActionCondition>,
    val exclusions: List<CoreActionCondition>,
    val result: CoreActionRuleResult,
    override val provenance: List<RuleProvenance>,
    override val executionAuthority: RuleExecutionAuthority = RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION,
    override val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
    override val unresolvedReviewRestrictions: Set<ReviewRestriction>,
) : GovernedCoreRule {
    init {
        require(description.isNotBlank())
        require(priority >= 0)
        require(preconditions.isNotEmpty())
        require(provenance.isNotEmpty())
        require(productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
        require(ReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED in unresolvedReviewRestrictions)
        if (kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD) require(provenance.any { it is RuleProvenance.ClinicalSource })
    }

    fun evaluate(state: CoreOrdinaryTherapyState, selectedRoute: OrdinaryRoute): CoreActionRuleTrace {
        val prerequisiteResults = preconditions.map { it.evaluate(state, selectedRoute) }
        val exclusionResults = exclusions.map { it.evaluate(state, selectedRoute) }
        val matched = selectedRoute == route && prerequisiteResults.all { it.matched } && exclusionResults.none { it.matched }
        val reasons = buildList {
            if (selectedRoute != route) add("route: expected $route; actual $selectedRoute")
            prerequisiteResults.filterNot { it.matched }.forEach { add("${it.conditionId}: expected ${it.expected}; actual ${it.actual}") }
            exclusionResults.filter { it.matched }.forEach { add("excluded by ${it.conditionId}: ${it.actual}") }
        }
        return CoreActionRuleTrace(id, matched, prerequisiteResults, exclusionResults, reasons)
    }
}

data class CoreActionRuleTrace(
    val ruleId: PolicyRuleId,
    val matched: Boolean,
    val prerequisites: List<CoreConditionEvaluation>,
    val exclusions: List<CoreConditionEvaluation>,
    val rejectionReasons: List<String>,
)

enum class ProgressionDisposition { NEW_ACTION, EXPLICIT_REPEAT_ALLOWED, SUBSTITUTE_DIRECTION_CHOICE, STOP_NO_PROGRESS }

data class ProgressionTrace(
    val disposition: ProgressionDisposition,
    val candidateActionId: PolicyActionId?,
    val occurrencesAtCurrentRevision: Int,
    val consideredGuardIds: List<PolicyRuleId>,
    val selectedGuardId: PolicyRuleId?,
    val explanation: String,
)

data class CoreProgressionGuard(
    override val id: PolicyRuleId,
    override val description: String,
    override val kind: RuleKind,
    override val priority: Int,
    override val provenance: List<RuleProvenance>,
    override val executionAuthority: RuleExecutionAuthority = RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION,
    override val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
    override val unresolvedReviewRestrictions: Set<ReviewRestriction>,
) : GovernedCoreRule

object CoreProgressionGuardCatalog {
    val explicitRepeat = CoreProgressionGuard(
        PolicyRuleId.parse("ctv205-g001-explicit-repeat-once"),
        "Allow one repeated act only when a current structured authorization identifies that act and reason.",
        RuleKind.ARCHITECTURAL_SCOPE_GUARD, 4000,
        listOf(CoreOrdinaryProvenance.architecture),
        unresolvedReviewRestrictions = CoreOrdinaryProvenance.architectureRestrictions,
    )
    val noResponseLoopStop = CoreProgressionGuard(
        PolicyRuleId.parse("ctv205-g002-no-response-loop-stop"),
        "A no-response action already executed against this evidence cannot execute again.",
        RuleKind.ARCHITECTURAL_SCOPE_GUARD, 3900,
        listOf(CoreOrdinaryProvenance.architecture),
        unresolvedReviewRestrictions = CoreOrdinaryProvenance.architectureRestrictions,
    )
    val offerDirection = CoreProgressionGuard(
        PolicyRuleId.parse("ctv205-g003-stagnation-offer-direction"),
        "Replace one repeated substantive act with a bounded user-controlled direction choice.",
        RuleKind.SOURCE_DERIVED_ACTION, 3800,
        listOf(CoreOrdinaryProvenance.fhsGoals()),
        unresolvedReviewRestrictions = CoreOrdinaryProvenance.fhsRestrictions,
    )
    val stopAfterDirection = CoreProgressionGuard(
        PolicyRuleId.parse("ctv205-g004-stagnation-stop"),
        "If the direction choice also produced no new evidence, stop rather than oscillating or repeating.",
        RuleKind.ARCHITECTURAL_SCOPE_GUARD, 3700,
        listOf(CoreOrdinaryProvenance.architecture),
        unresolvedReviewRestrictions = CoreOrdinaryProvenance.architectureRestrictions,
    )

    val rules = listOf(explicitRepeat, noResponseLoopStop, offerDirection, stopAfterDirection)
}

private fun actionCondition(
    id: String,
    expected: String,
    actual: (CoreOrdinaryTherapyState, OrdinaryRoute) -> String,
    predicate: (CoreOrdinaryTherapyState, OrdinaryRoute) -> Boolean,
) = CoreActionCondition(id, expected, actual, predicate)

private fun routeCondition(route: OrdinaryRoute) = actionCondition(
    "route-${route.name.lowercase()}", route.name, { _, actual -> actual.name }, { _, actual -> actual == route },
)

private fun sourceActionRule(
    id: String,
    description: String,
    priority: Int,
    route: OrdinaryRoute,
    conditions: List<CoreActionCondition>,
    result: CoreActionRuleResult,
    provenance: List<RuleProvenance.ClinicalSource>,
    restrictions: Set<ReviewRestriction>,
    kind: RuleKind = RuleKind.SOURCE_DERIVED_ACTION,
) = CoreActionRule(
    PolicyRuleId.parse(id), description, kind, priority, route,
    listOf(routeCondition(route)) + conditions, emptyList(), result, provenance,
    unresolvedReviewRestrictions = restrictions,
)

private fun architectureActionRule(
    id: String,
    description: String,
    priority: Int,
    route: OrdinaryRoute,
    conditions: List<CoreActionCondition>,
    result: CoreActionRuleResult,
) = CoreActionRule(
    PolicyRuleId.parse(id), description, RuleKind.ARCHITECTURAL_SCOPE_GUARD, priority, route,
    listOf(routeCondition(route)) + conditions, emptyList(), result,
    listOf(CoreOrdinaryProvenance.architecture),
    unresolvedReviewRestrictions = CoreOrdinaryProvenance.architectureRestrictions,
)

object CoreActionRuleCatalog {
    private val concernMissing = actionCondition("concern-missing", "no established bounded concern", { s, _ -> "${s.concernStatement.resolution}:${s.problemClarity.value}" }) { s, _ ->
        !s.concernStatement.isEstablished() || s.concernStatement.value.isNullOrBlank() ||
            !s.problemClarity.isEstablished() || s.problemClarity.value == ProblemClarity.VAGUE
    }
    private val concernAvailable = actionCondition("concern-available", "established bounded concern", { s, _ -> "${s.concernStatement.resolution}:${s.problemClarity.value}" }) { s, _ ->
        s.concernStatement.isEstablished() && !s.concernStatement.value.isNullOrBlank() &&
            s.problemClarity.isEstablished() && s.problemClarity.value == ProblemClarity.BOUNDED
    }
    private fun expression(vararg expected: ExpressionProgress) = actionCondition(
        "expression-${expected.joinToString("-") { it.name.lowercase() }}", expected.joinToString(),
        { s, _ -> s.expressionProgress.value?.name ?: s.expressionProgress.resolution.name },
        { s, _ -> s.expressionProgress.isEstablished() && s.expressionProgress.value in expected.toSet() },
    )
    private fun correction(expected: CorrectionStatus) = actionCondition(
        "correction-${expected.name.lowercase()}", expected.name,
        { s, _ -> s.correctionStatus.value?.name ?: s.correctionStatus.resolution.name },
        { s, _ -> s.correctionStatus.isEstablished() && s.correctionStatus.value == expected },
    )
    private val missingPieceAvailable = actionCondition(
        "important-missing-piece-available", "one established missing-information subject",
        { s, _ -> s.importantMissingInformation.value ?: s.importantMissingInformation.resolution.name },
        { s, _ -> s.importantMissingInformation.isEstablished() && !s.importantMissingInformation.value.isNullOrBlank() },
    )
    private val tentativeUnderstanding = actionCondition(
        "understanding-tentative", "tentative Thomas understanding", { s, _ -> "${s.sharedUnderstanding.value}:${s.thomasUnderstanding.resolution}" },
        { s, _ -> s.sharedUnderstanding.value == SharedUnderstanding.TENTATIVE && s.thomasUnderstanding.resolution == com.conundrum.thomas.v2.ontology.EpistemicResolution.TENTATIVE },
    )
    private val confirmedUnderstanding = actionCondition(
        "understanding-confirmed", "user-confirmed understanding", { s, _ -> "${s.sharedUnderstanding.value}:${s.thomasUnderstanding.resolution}" },
        { s, _ -> s.sharedUnderstanding.isEstablished() && s.sharedUnderstanding.value == SharedUnderstanding.CONFIRMED && s.thomasUnderstanding.isEstablished() },
    )
    private val understandingSummaryNotDelivered = actionCondition(
        "understanding-summary-not-delivered", "summary not delivered", { s, _ -> s.understandingSummaryDelivered.value?.toString() ?: s.understandingSummaryDelivered.resolution.name },
        { s, _ -> !s.understandingSummaryDelivered.isEstablished() || s.understandingSummaryDelivered.value != true },
    )
    private val understandingSummaryDelivered = actionCondition(
        "understanding-summary-delivered", "summary delivered", { s, _ -> s.understandingSummaryDelivered.value?.toString() ?: s.understandingSummaryDelivered.resolution.name },
        { s, _ -> s.understandingSummaryDelivered.isEstablished() && s.understandingSummaryDelivered.value == true },
    )
    private val influenceMissing = actionCondition("influence-missing", "influence not established", { s, _ -> s.problemInfluence.resolution.name }) { s, _ -> !s.problemInfluence.isEstablished() }
    private fun influence(expected: ProblemInfluence) = actionCondition("influence-${expected.name.lowercase()}", expected.name, { s, _ -> s.problemInfluence.value?.name ?: s.problemInfluence.resolution.name }) { s, _ -> s.problemInfluence.isEstablished() && s.problemInfluence.value == expected }
    private val willingnessMissing = actionCondition("willingness-missing", "willingness not established", { s, _ -> s.willingness.resolution.name }) { s, _ -> !s.willingness.isEstablished() }
    private val willingToAct = actionCondition("willing-to-act", "WILLING_TO_ACT", { s, _ -> s.willingness.value?.name ?: s.willingness.resolution.name }) { s, _ -> s.willingness.isEstablished() && s.willingness.value == ParticipationWillingness.WILLING_TO_ACT }
    private val optionsMissing = actionCondition("options-missing", "no established options", { s, _ -> s.generatedOptions.value?.size?.toString() ?: s.generatedOptions.resolution.name }) { s, _ -> !s.generatedOptions.isEstablished() }
    private val optionsAvailable = actionCondition("options-available", "one or more established options", { s, _ -> s.generatedOptions.value?.size?.toString() ?: s.generatedOptions.resolution.name }) { s, _ -> s.generatedOptions.isEstablished() && s.generatedOptions.value.orEmpty().isNotEmpty() }
    private val selectionMissing = actionCondition("selection-missing", "no established selection", { s, _ -> s.selectedOption.resolution.name }) { s, _ -> !s.selectedOption.isEstablished() }
    private val selectionAvailable = actionCondition("selection-available", "established selection", { s, _ -> s.selectedOption.value ?: s.selectedOption.resolution.name }) { s, _ -> s.selectedOption.isEstablished() }
    private val planMissing = actionCondition("plan-missing", "no established plan", { s, _ -> s.actionPlan.resolution.name }) { s, _ -> !s.actionPlan.isEstablished() }
    private val planAvailable = actionCondition("plan-available", "established plan", { s, _ -> s.actionPlan.value ?: s.actionPlan.resolution.name }) { s, _ -> s.actionPlan.isEstablished() }
    private val outcomeMissing = actionCondition("outcome-missing", "no established outcome", { s, _ -> s.planOutcome.resolution.name }) { s, _ -> !s.planOutcome.isEstablished() }
    private val outcomeAvailable = actionCondition("outcome-available", "established outcome", { s, _ -> s.planOutcome.value?.name ?: s.planOutcome.resolution.name }) { s, _ -> s.planOutcome.isEstablished() }
    private val reviewNotComplete = actionCondition("review-not-complete", "outcome not reviewed", { s, _ -> s.planReviewStatus.value?.name ?: s.planReviewStatus.resolution.name }) { s, _ -> !s.planReviewStatus.isEstablished() || s.planReviewStatus.value == PlanReviewStatus.NOT_REVIEWED }
    private val reviewComplete = actionCondition("review-complete", "outcome reviewed", { s, _ -> s.planReviewStatus.value?.name ?: s.planReviewStatus.resolution.name }) { s, _ -> s.planReviewStatus.isEstablished() && s.planReviewStatus.value == PlanReviewStatus.REVIEWED }

    val rules: List<CoreActionRule> = listOf(
        sourceActionRule("ctv205-a001-ask-support-preference", "Clarify one supported route.", 1000, OrdinaryRoute.CLARIFY_PREFERENCE, emptyList(), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.askSupportPreference.id), listOf(CoreOrdinaryProvenance.fhsGoals()), CoreOrdinaryProvenance.fhsRestrictions),
        architectureActionRule("ctv205-a002-acknowledge-close", "Honor an explicit close request without reopening.", 1000, OrdinaryRoute.CONSOLIDATE_CLOSE,
            listOf(actionCondition("close-requested", "CLOSE_REQUESTED", { s, _ -> s.engagement.value?.name ?: s.engagement.resolution.name }) { s, _ -> s.engagement.isEstablished() && s.engagement.value == OrdinaryEngagement.CLOSE_REQUESTED }),
            CoreActionRuleResult.SelectAction(CoreOrdinaryActions.acknowledgeClose.id)),
        sourceActionRule("ctv205-a003-pause-without-response", "Respect a pause, refusal, or unwillingness without pressure.", 990, OrdinaryRoute.CONSOLIDATE_CLOSE,
            listOf(actionCondition("pause-refusal-unwilling", "pause, refusal, or unwillingness", { s, _ -> "${s.engagement.value}:${s.willingness.value}" }) { s, _ ->
                (s.engagement.isEstablished() && s.engagement.value in setOf(OrdinaryEngagement.PAUSE_REQUESTED, OrdinaryEngagement.DOES_NOT_WANT_TOPIC)) ||
                    (s.willingness.isEstablished() && s.willingness.value == ParticipationWillingness.UNWILLING_TO_CONTINUE)
            }), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.pauseWithoutResponse.id), listOf(CoreOrdinaryProvenance.pmReluctance()), CoreOrdinaryProvenance.pmRestrictions),

        sourceActionRule("ctv205-a004-listen-invite-expression", "Invite expression when no content has been established.", 1000, OrdinaryRoute.LISTEN_SUPPORT, listOf(concernMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.inviteExpression.id), listOf(CoreOrdinaryProvenance.fhsVerbal()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a005-listen-reflect-new-content", "Reflect newly available established content without solving.", 900, OrdinaryRoute.LISTEN_SUPPORT, listOf(concernAvailable, expression(ExpressionProgress.NEW_CONTENT_AVAILABLE)), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.reflectEstablishedContent.id), listOf(CoreOrdinaryProvenance.fhsVerbal()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a006-listen-invite-more", "Invite more expression after reflection when the user wants to continue.", 800, OrdinaryRoute.LISTEN_SUPPORT, listOf(concernAvailable, expression(ExpressionProgress.REFLECTION_RECEIVED, ExpressionProgress.USER_WANTS_TO_CONTINUE)), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.inviteFurtherExpression.id), listOf(CoreOrdinaryProvenance.fhsVerbal()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a007-listen-summarize", "Summarize established content when expression is complete.", 700, OrdinaryRoute.LISTEN_SUPPORT, listOf(concernAvailable, expression(ExpressionProgress.EXPRESSION_COMPLETE)), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.summarizeListening.id), listOf(CoreOrdinaryProvenance.fhsVerbal()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a008-listen-check-close", "After a summary, let the user continue or close.", 600, OrdinaryRoute.LISTEN_SUPPORT, listOf(concernAvailable, expression(ExpressionProgress.SUMMARY_DELIVERED)), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.checkFurtherOrClose.id), listOf(CoreOrdinaryProvenance.fhsGoals()), CoreOrdinaryProvenance.fhsRestrictions),

        sourceActionRule("ctv205-a009-understand-correction", "Acknowledge and withdraw a corrected interpretation before continuing.", 1100, OrdinaryRoute.UNDERSTAND_CLARIFY, listOf(correction(CorrectionStatus.UNHANDLED)), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.acknowledgeCorrection.id), listOf(CoreOrdinaryProvenance.fhsFeedback()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a010-understand-ask-concern", "Ask for the concern the user wants understood.", 1000, OrdinaryRoute.UNDERSTAND_CLARIFY, listOf(concernMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.askPresentConcern.id), listOf(CoreOrdinaryProvenance.fhsVerbal()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a011-understand-missing-piece", "Ask for one explicitly identified important missing piece.", 900, OrdinaryRoute.UNDERSTAND_CLARIFY, listOf(concernAvailable, missingPieceAvailable), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.askImportantMissingPiece.id), listOf(CoreOrdinaryProvenance.fhsVerbal()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a012-understand-verify-tentative", "Verify rather than assert a tentative understanding.", 800, OrdinaryRoute.UNDERSTAND_CLARIFY, listOf(concernAvailable, tentativeUnderstanding), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.verifyTentativeUnderstanding.id), listOf(CoreOrdinaryProvenance.fhsFeedback()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a013-understand-summarize-confirmed", "Summarize only confirmed understanding once.", 700, OrdinaryRoute.UNDERSTAND_CLARIFY, listOf(confirmedUnderstanding, understandingSummaryNotDelivered), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.summarizeSharedUnderstanding.id), listOf(CoreOrdinaryProvenance.fhsVerbal()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a014-understand-next-direction", "After confirmed understanding is summarized, ask for the next supported direction.", 600, OrdinaryRoute.UNDERSTAND_CLARIFY, listOf(confirmedUnderstanding, understandingSummaryDelivered), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.checkUnderstandingNextDirection.id), listOf(CoreOrdinaryProvenance.fhsGoals()), CoreOrdinaryProvenance.fhsRestrictions),

        sourceActionRule("ctv205-a015-problem-correction", "Withdraw a corrected problem interpretation before procedural work.", 1200, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(correction(CorrectionStatus.UNHANDLED)), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.acknowledgeCorrection.id), listOf(CoreOrdinaryProvenance.fhsFeedback()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a016-problem-define", "Establish one bounded practical problem.", 1100, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(concernMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.askProblemDescription.id), listOf(CoreOrdinaryProvenance.pmProblemDefinition()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a017-problem-verify", "Verify the tentative problem understanding before options.", 1000, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(concernAvailable, tentativeUnderstanding), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.verifyProblemUnderstanding.id), listOf(CoreOrdinaryProvenance.fhsFeedback(), CoreOrdinaryProvenance.pmProblemDefinition()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a018-problem-influence", "Identify which part of a confirmed practical problem is influenceable.", 900, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(confirmedUnderstanding, influenceMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.askInfluenceablePart.id), listOf(CoreOrdinaryProvenance.pmProblemDefinition()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a019-problem-non-influenceable", "Do not force the PM+ manageable-problem route onto a non-influenceable problem.", 890, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(confirmedUnderstanding, influence(ProblemInfluence.NOT_INFLUENCEABLE)), CoreActionRuleResult.Terminate(CorePolicyDisposition.OUT_OF_SCOPE, "NON_INFLUENCEABLE_PROBLEM_POLICY_REQUIRED"), listOf(CoreOrdinaryProvenance.pmProblemDefinition()), CoreOrdinaryProvenance.pmRestrictions, RuleKind.SOURCE_DERIVED_SCOPE_BOUNDARY),
        sourceActionRule("ctv205-a020-problem-readiness", "Seek permission before generating options.", 800, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(confirmedUnderstanding, influence(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE), willingnessMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.askReadinessForOptions.id), listOf(CoreOrdinaryProvenance.fhsGoals()), CoreOrdinaryProvenance.fhsRestrictions),
        sourceActionRule("ctv205-a021-problem-options", "Invite user-generated options without advice.", 700, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(confirmedUnderstanding, influence(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE), willingToAct, optionsMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.inviteUserOptions.id), listOf(CoreOrdinaryProvenance.pmOptions(), CoreOrdinaryProvenance.pmAdvice()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a022-problem-select", "Support the user's choice among their established options.", 600, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(optionsAvailable, selectionMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.askUserToChooseOption.id), listOf(CoreOrdinaryProvenance.pmOptions()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a023-problem-plan", "Develop one bounded step from the user's selected option.", 500, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(optionsAvailable, selectionAvailable, planMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.developBoundedPlan.id), listOf(CoreOrdinaryProvenance.pmPlan()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a024-problem-wait", "After plan formation, wait for outcome evidence rather than add advice.", 400, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(planAvailable, outcomeMissing), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.waitForOutcome.id), listOf(CoreOrdinaryProvenance.pmOutcome()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a025-problem-review", "Review an established plan outcome without presuming success or failure.", 300, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(planAvailable, outcomeAvailable, reviewNotComplete), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.reviewReportedOutcome.id), listOf(CoreOrdinaryProvenance.pmOutcome()), CoreOrdinaryProvenance.pmRestrictions),
        sourceActionRule("ctv205-a026-problem-consolidate", "After outcome review, consolidate established learning and offer closure or redirection.", 200, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, listOf(planAvailable, outcomeAvailable, reviewComplete), CoreActionRuleResult.SelectAction(CoreOrdinaryActions.consolidatePlanLearning.id), listOf(CoreOrdinaryProvenance.pmOutcome(), CoreOrdinaryProvenance.fhsGoals()), CoreOrdinaryProvenance.pmRestrictions),
    ).sortedBy { it.id }

    init { require(rules.map { it.id }.distinct().size == rules.size) }
}

data class CoreRouteSelection(
    val selectedRoute: OrdinaryRoute?,
    val selectedRuleId: PolicyRuleId?,
    val selectedProvenance: List<RuleProvenance>,
    val eligibleRuleIds: List<PolicyRuleId>,
    val rejectedRules: List<RejectedCoreRule>,
    val trace: List<CoreRuleEvaluationTrace>,
    val transition: RouteTransition?,
)

data class CorePolicyDecision(
    val policyVersion: String,
    val decisionReference: String,
    val stateId: String,
    val safetyEvidenceRevision: Long,
    val conversationRevision: Long,
    val disposition: CorePolicyDisposition,
    val routeSelection: CoreRouteSelection,
    val activeGoalId: OntologyConceptId?,
    val eligibleCandidateActions: List<PolicyActionId>,
    val rejectedActionRules: List<RejectedCoreRule>,
    val selectedAction: SelectedCoreQualificationAction?,
    val selectedRuleProvenance: List<RuleProvenance>,
    val unresolvedReviewRestrictions: List<ReviewRestriction>,
    val expectedOutcome: CoreOutcomeContract?,
    val handoffRequirement: String?,
    val actionRuleTrace: List<CoreActionRuleTrace>,
    val progressionTrace: ProgressionTrace?,
) {
    init {
        if (disposition == CorePolicyDisposition.ACTION_SELECTED) {
            require(selectedAction != null && activeGoalId == selectedAction.definition.goalId && expectedOutcome != null)
        } else require(selectedAction == null)
    }
}

object CoreOrdinaryRuleCatalog {
    val allRules: List<GovernedCoreRule> =
        CoreRouteRuleCatalog.rules + CoreActionRuleCatalog.rules + CoreProgressionGuardCatalog.rules
}

class CoreOrdinaryTherapyEvaluator(
    routeRules: List<CoreRouteRule> = CoreRouteRuleCatalog.rules,
    actionRules: List<CoreActionRule> = CoreActionRuleCatalog.rules,
    actions: List<CoreActionDefinition> = CoreOrdinaryActions.all,
) {
    private val orderedRouteRules = routeRules.sortedWith(compareByDescending<CoreRouteRule> { it.priority }.thenBy { it.id })
    private val orderedActionRules = actionRules.sortedWith(compareByDescending<CoreActionRule> { it.priority }.thenBy { it.id })
    private val actionsById = actions.associateBy { it.id }

    init {
        require(orderedRouteRules.map { it.id }.distinct().size == orderedRouteRules.size)
        require(orderedActionRules.map { it.id }.distinct().size == orderedActionRules.size)
        require(actionsById.size == actions.size)
    }

    fun evaluate(state: CoreOrdinaryTherapyState, permit: OrdinaryTherapyPermit): CorePolicyDecision {
        val validation = state.validationErrors()
        if (validation.isNotEmpty()) return terminal(state, CorePolicyDisposition.INVALID_INPUT, validation.joinToString("; "))
        if (!permit.authorizes(state.stateId, state.safetyEvidenceRevision)) {
            return terminal(state, CorePolicyDisposition.INVALID_INPUT, "FRESH_SAFETY_SCOPE_GATE_DECISION_REQUIRED")
        }
        if (state.mode != ThomasMode.THERAPIST) return terminal(state, CorePolicyDisposition.OUT_OF_SCOPE, "THERAPIST_MODE_REQUIRED")
        if (state.hasMaterialConflict()) return terminal(state, CorePolicyDisposition.POLICY_CONFLICT, "STRUCTURED_EVIDENCE_ADJUDICATION_REQUIRED")

        val routeEvaluation = selectRoute(state)
        if (routeEvaluation.terminalDisposition != null) {
            return terminal(state, routeEvaluation.terminalDisposition, requireNotNull(routeEvaluation.handoff), routeEvaluation.selection)
        }
        val route = requireNotNull(routeEvaluation.selection.selectedRoute)
        if (state.activeRoute != null && !CoreRouteTransitionCatalog.supports(state.activeRoute, route)) {
            return terminal(state, CorePolicyDisposition.INVALID_INPUT, "UNDEFINED_ROUTE_TRANSITION", routeEvaluation.selection)
        }
        if (state.activeRoute == OrdinaryRoute.CONSOLIDATE_CLOSE && route != OrdinaryRoute.CONSOLIDATE_CLOSE) {
            val lastCloseRevision = state.actionHistory.filter { it.route == OrdinaryRoute.CONSOLIDATE_CLOSE }
                .maxOfOrNull { it.conversationRevision } ?: 0
            if (!state.routePreference.isEstablished() || state.conversationRevision <= lastCloseRevision) {
                return terminal(state, CorePolicyDisposition.NO_AUTHORIZED_ACTION, "REENTRY_REQUIRES_CHANGED_EVIDENCE_AND_EXPLICIT_PREFERENCE", routeEvaluation.selection)
            }
        }

        val traces = orderedActionRules.map { it.evaluate(state, route) }
        val traceById = traces.associateBy { it.ruleId }
        val matched = orderedActionRules.filter { requireNotNull(traceById[it.id]).matched }
        val rejected = orderedActionRules.filterNot { it in matched }.map { RejectedCoreRule(it.id, requireNotNull(traceById[it.id]).rejectionReasons) }
        val eligibleActions = matched.mapNotNull { (it.result as? CoreActionRuleResult.SelectAction)?.actionId }.distinct().sorted()
        if (matched.isEmpty()) return decision(state, CorePolicyDisposition.NO_AUTHORIZED_ACTION, routeEvaluation.selection, traces, eligibleActions, rejected, handoff = "NO_USEFUL_ACTION_FOR_ESTABLISHED_STATE")
        val highest = matched.maxOf { it.priority }
        val contenders = matched.filter { it.priority == highest }.sortedBy { it.id }
        if (contenders.size != 1) return decision(state, CorePolicyDisposition.POLICY_CONFLICT, routeEvaluation.selection, traces, eligibleActions, rejected, handoff = "CORE_POLICY_ADJUDICATION_REQUIRED")
        val winner = contenders.single()
        if (winner.executionAuthority != RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION) {
            return decision(state, CorePolicyDisposition.REVIEW_BLOCKED, routeEvaluation.selection, traces, eligibleActions, rejected,
                provenance = winner.provenance, restrictions = winner.unresolvedReviewRestrictions, handoff = "CORE_RULE_REVIEW_REQUIRED")
        }
        return when (val result = winner.result) {
            is CoreActionRuleResult.Terminate -> decision(state, result.disposition, routeEvaluation.selection, traces, eligibleActions, rejected,
                provenance = winner.provenance, restrictions = winner.unresolvedReviewRestrictions, handoff = result.handoffRequirement)
            is CoreActionRuleResult.SelectAction -> selectWithProgression(state, routeEvaluation.selection, traces, eligibleActions, rejected, winner, result.actionId)
        }
    }

    private data class RouteEvaluation(
        val selection: CoreRouteSelection,
        val terminalDisposition: CorePolicyDisposition? = null,
        val handoff: String? = null,
    )

    private fun selectRoute(state: CoreOrdinaryTherapyState): RouteEvaluation {
        val traces = orderedRouteRules.map { it.evaluate(state) }
        val byId = traces.associateBy { it.ruleId }
        val matched = orderedRouteRules.filter { requireNotNull(byId[it.id]).matched }
        val rejected = orderedRouteRules.filterNot { it in matched }.map { RejectedCoreRule(it.id, requireNotNull(byId[it.id]).rejectionReasons) }
        fun selection(rule: CoreRouteRule? = null, route: OrdinaryRoute? = null) = CoreRouteSelection(
            route, rule?.id, rule?.provenance.orEmpty(), matched.map { it.id }.sorted(), rejected, traces,
            if (state.activeRoute != null && route != null && state.activeRoute != route) RouteTransition(state.activeRoute, route) else null,
        )
        if (matched.isEmpty()) return RouteEvaluation(selection(), CorePolicyDisposition.NO_AUTHORIZED_ACTION, "NO_ROUTE_RULE_MATCHED")
        val highest = matched.maxOf { it.priority }
        val contenders = matched.filter { it.priority == highest }.sortedBy { it.id }
        if (contenders.size != 1) return RouteEvaluation(selection(), CorePolicyDisposition.POLICY_CONFLICT, "ROUTE_POLICY_ADJUDICATION_REQUIRED")
        val winner = contenders.single()
        if (winner.executionAuthority != RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION) {
            return RouteEvaluation(selection(winner), CorePolicyDisposition.REVIEW_BLOCKED, "ROUTE_RULE_REVIEW_REQUIRED")
        }
        return when (val result = winner.result) {
            is CoreRouteRuleResult.Terminate -> RouteEvaluation(selection(winner), result.disposition, result.handoffRequirement)
            is CoreRouteRuleResult.SelectRoute -> RouteEvaluation(selection(winner, result.route))
        }
    }

    private fun selectWithProgression(
        state: CoreOrdinaryTherapyState,
        routeSelection: CoreRouteSelection,
        traces: List<CoreActionRuleTrace>,
        eligibleActions: List<PolicyActionId>,
        rejected: List<RejectedCoreRule>,
        winner: CoreActionRule,
        candidateId: PolicyActionId,
    ): CorePolicyDecision {
        val candidate = actionsById[candidateId] ?: return decision(state, CorePolicyDisposition.NO_AUTHORIZED_ACTION, routeSelection, traces, eligibleActions, rejected, handoff = "ACTION_DEFINITION_REQUIRED")
        val occurrences = state.actionHistory.count { it.actionId == candidateId && it.conversationRevision == state.conversationRevision }
        if (occurrences == 0) return selectedDecision(state, routeSelection, traces, eligibleActions, rejected, winner.id, winner.provenance, winner.unresolvedReviewRestrictions, candidate,
            ProgressionTrace(ProgressionDisposition.NEW_ACTION, candidateId, 0, emptyList(), null, "Candidate has not executed against this evidence revision."))

        val considered = mutableListOf(CoreProgressionGuardCatalog.explicitRepeat.id)
        val authorization = state.explicitRepeatAuthorization
        if (authorization != null && authorization.actionId == candidateId && authorization.conversationRevision == state.conversationRevision &&
            occurrences - 1 < authorization.maximumAdditionalExecutions
        ) return selectedDecision(state, routeSelection, traces, eligibleActions, rejected, CoreProgressionGuardCatalog.explicitRepeat.id,
            CoreProgressionGuardCatalog.explicitRepeat.provenance, CoreProgressionGuardCatalog.explicitRepeat.unresolvedReviewRestrictions, candidate,
            ProgressionTrace(ProgressionDisposition.EXPLICIT_REPEAT_ALLOWED, candidateId, occurrences, considered, CoreProgressionGuardCatalog.explicitRepeat.id, authorization.reason))

        considered += CoreProgressionGuardCatalog.noResponseLoopStop.id
        if (candidate.renderSpecification.outputDisposition == RenderOutputDisposition.NO_RESPONSE) {
            return decision(state, CorePolicyDisposition.NO_AUTHORIZED_ACTION, routeSelection, traces, eligibleActions, rejected,
                provenance = CoreProgressionGuardCatalog.noResponseLoopStop.provenance,
                restrictions = CoreProgressionGuardCatalog.noResponseLoopStop.unresolvedReviewRestrictions,
                handoff = "AWAIT_CHANGED_EVIDENCE",
                progression = ProgressionTrace(ProgressionDisposition.STOP_NO_PROGRESS, candidateId, occurrences, considered,
                    CoreProgressionGuardCatalog.noResponseLoopStop.id, "NO_RESPONSE already executed; repeating silence would create an execution loop."))
        }

        considered += CoreProgressionGuardCatalog.stopAfterDirection.id
        val directionId = CoreOrdinaryActions.offerDirectionChoice.id
        val directionAlreadyUsed = state.actionHistory.any { it.actionId == directionId && it.conversationRevision == state.conversationRevision }
        if (candidateId == directionId || directionAlreadyUsed) {
            return decision(state, CorePolicyDisposition.NO_AUTHORIZED_ACTION, routeSelection, traces, eligibleActions, rejected,
                provenance = CoreProgressionGuardCatalog.stopAfterDirection.provenance,
                restrictions = CoreProgressionGuardCatalog.stopAfterDirection.unresolvedReviewRestrictions,
                handoff = "AWAIT_CHANGED_EVIDENCE_OR_CLOSE",
                progression = ProgressionTrace(ProgressionDisposition.STOP_NO_PROGRESS, candidateId, occurrences, considered,
                    CoreProgressionGuardCatalog.stopAfterDirection.id, "The bounded direction choice also has no new response; no arbitrary repetition is authorized."))
        }

        considered += CoreProgressionGuardCatalog.offerDirection.id
        return selectedDecision(state, routeSelection, traces, eligibleActions, rejected, CoreProgressionGuardCatalog.offerDirection.id,
            CoreProgressionGuardCatalog.offerDirection.provenance, CoreProgressionGuardCatalog.offerDirection.unresolvedReviewRestrictions,
            CoreOrdinaryActions.offerDirectionChoice,
            ProgressionTrace(ProgressionDisposition.SUBSTITUTE_DIRECTION_CHOICE, candidateId, occurrences, considered,
                CoreProgressionGuardCatalog.offerDirection.id, "The substantive candidate already executed against unchanged evidence; ask the user to choose direction instead."))
    }

    private fun selectedDecision(
        state: CoreOrdinaryTherapyState,
        routeSelection: CoreRouteSelection,
        traces: List<CoreActionRuleTrace>,
        eligibleActions: List<PolicyActionId>,
        rejected: List<RejectedCoreRule>,
        selectedBy: PolicyRuleId,
        provenance: List<RuleProvenance>,
        restrictions: Set<ReviewRestriction>,
        action: CoreActionDefinition,
        progression: ProgressionTrace,
    ): CorePolicyDecision {
        val selected = SelectedCoreQualificationAction(action, selectedBy, RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION, restrictions)
        return decision(state, CorePolicyDisposition.ACTION_SELECTED, routeSelection, traces, eligibleActions, rejected,
            selected = selected, activeGoal = action.goalId, provenance = provenance, restrictions = restrictions,
            expected = action.outcomeContract, progression = progression)
    }

    private fun terminal(
        state: CoreOrdinaryTherapyState,
        disposition: CorePolicyDisposition,
        handoff: String,
        routeSelection: CoreRouteSelection = CoreRouteSelection(null, null, emptyList(), emptyList(), emptyList(), emptyList(), null),
    ) = decision(state, disposition, routeSelection, emptyList(), emptyList(), emptyList(), handoff = handoff)

    private fun decision(
        state: CoreOrdinaryTherapyState,
        disposition: CorePolicyDisposition,
        routeSelection: CoreRouteSelection,
        traces: List<CoreActionRuleTrace>,
        eligibleActions: List<PolicyActionId>,
        rejected: List<RejectedCoreRule>,
        selected: SelectedCoreQualificationAction? = null,
        activeGoal: OntologyConceptId? = null,
        provenance: List<RuleProvenance> = emptyList(),
        restrictions: Set<ReviewRestriction> = emptySet(),
        expected: CoreOutcomeContract? = null,
        handoff: String? = null,
        progression: ProgressionTrace? = null,
    ) = CorePolicyDecision(
        CT_V2_05_CORE_POLICY_VERSION,
        "$CT_V2_05_CORE_POLICY_VERSION:${state.stateId}:${state.conversationRevision}",
        state.stateId,
        state.safetyEvidenceRevision.value,
        state.conversationRevision,
        disposition,
        routeSelection,
        activeGoal,
        eligibleActions,
        rejected,
        selected,
        provenance,
        restrictions.sortedBy { it.name },
        expected,
        handoff,
        traces,
        progression,
    )
}

object CorePolicyDecisionExplainer {
    fun compact(decision: CorePolicyDecision): String = listOf(
        decision.routeSelection.selectedRoute?.name ?: "NO_ROUTE",
        decision.activeGoalId?.value ?: "NO_GOAL",
        decision.selectedAction?.selectedByRuleId?.value ?: decision.disposition.name,
        decision.selectedAction?.definition?.id?.value ?: "NO_ACTION",
        decision.expectedOutcome?.expectedInformation?.name ?: decision.handoffRequirement ?: "NONE",
    ).joinToString(" -> ")
}
