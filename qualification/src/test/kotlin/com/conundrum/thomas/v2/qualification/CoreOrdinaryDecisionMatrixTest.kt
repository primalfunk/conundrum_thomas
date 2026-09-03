package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.CorrectionStatus
import com.conundrum.thomas.v2.engine.ordinary.ExpressionProgress
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryEngagement
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryRoute
import com.conundrum.thomas.v2.engine.ordinary.PlanReviewStatus
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.ProblemInfluence
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOrdinaryDecisionMatrixTest {
    private val evaluator = CoreOrdinaryTherapyEvaluator()

    @Test fun `unknown preference selects one route clarification`() {
        val state = CoreOrdinaryTestFixtures.state(support = null)
        assertAction(state, OrdinaryRoute.CLARIFY_PREFERENCE, "core-ask-support-preference")
    }

    @Test fun `standalone decision support remains unopened`() {
        val state = CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.DECISION_SUPPORT)
        val decision = evaluate(state)
        assertEquals(CorePolicyDisposition.UNSUPPORTED_PATHWAY, decision.disposition)
        assertEquals("DECISION_SUPPORT_SOURCE_REQUIRED", decision.handoffRequirement)
        assertNull(decision.selectedAction)
    }

    @Test fun `voluntary engagement must be established`() {
        val state = CoreOrdinaryTestFixtures.state().copy(engagement = PolicyEvidence.unknown())
        val decision = evaluate(state)
        assertEquals(CorePolicyDisposition.INSUFFICIENT_INFORMATION, decision.disposition)
        assertEquals("VOLUNTARY_ENGAGEMENT_EVIDENCE_REQUIRED", decision.handoffRequirement)
    }

    @Test fun `topic refusal selects no response`() {
        val state = CoreOrdinaryTestFixtures.state().copy(
            engagement = PolicyEvidence.reported(OrdinaryEngagement.DOES_NOT_WANT_TOPIC, "declined-topic"),
        )
        assertAction(state, OrdinaryRoute.CONSOLIDATE_CLOSE, "core-pause-without-response")
    }

    @Test fun `explicit close is acknowledged without a question`() {
        val state = CoreOrdinaryTestFixtures.state().copy(
            engagement = PolicyEvidence.reported(OrdinaryEngagement.CLOSE_REQUESTED, "done-now"),
        )
        val decision = assertAction(state, OrdinaryRoute.CONSOLIDATE_CLOSE, "core-acknowledge-close")
        assertEquals(0, decision.selectedAction!!.definition.renderSpecification.maximumQuestions)
    }

    @Test fun `listening with no content invites expression`() {
        assertAction(CoreOrdinaryTestFixtures.state(), OrdinaryRoute.LISTEN_SUPPORT, "core-invite-expression")
    }

    @Test fun `new listening content is reflected rather than solved`() {
        val state = bounded(CoreOrdinaryTestFixtures.state()).copy(
            expressionProgress = PolicyEvidence.reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, "new-content"),
        )
        val decision = assertAction(state, OrdinaryRoute.LISTEN_SUPPORT, "core-reflect-established-content")
        assertTrue(decision.selectedAction!!.definition.candidateInterventionFamilyId!!.value.endsWith("supportive-listening"))
    }

    @Test fun `understanding with no concern asks for the concern`() {
        val state = CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND)
        assertAction(state, OrdinaryRoute.UNDERSTAND_CLARIFY, "core-ask-present-concern")
    }

    @Test fun `identified missing information causes exactly that clarification act`() {
        val state = bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND)).copy(
            importantMissingInformation = PolicyEvidence.reported("what changed between the two fictional events", "gap"),
        )
        assertAction(state, OrdinaryRoute.UNDERSTAND_CLARIFY, "core-ask-important-missing-piece")
    }

    @Test fun `tentative understanding is verified`() {
        val state = tentative(bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND)))
        val decision = assertAction(state, OrdinaryRoute.UNDERSTAND_CLARIFY, "core-verify-tentative-understanding")
        assertTrue(decision.selectedAction!!.definition.renderSpecification.interpretationMustRemainTentative)
    }

    @Test fun `confirmed understanding is summarized before asking direction`() {
        val state = confirmed(bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND)))
        assertAction(state, OrdinaryRoute.UNDERSTAND_CLARIFY, "core-summarize-shared-understanding")
        assertAction(
            state.copy(understandingSummaryDelivered = PolicyEvidence.reported(true, "summary-delivered")),
            OrdinaryRoute.UNDERSTAND_CLARIFY,
            "core-check-understanding-next-direction",
        )
    }

    @Test fun `unhandled correction outranks further interpretation`() {
        val state = bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND)).copy(
            correctionStatus = PolicyEvidence.reported(CorrectionStatus.UNHANDLED, "correction"),
            userCorrection = PolicyEvidence.reported("the fictional issue was timing, not rejection", "correction"),
            withdrawnInterpretationReferences = setOf("tentative-old"),
        )
        assertAction(state, OrdinaryRoute.UNDERSTAND_CLARIFY, "core-acknowledge-correction")
    }

    @Test fun `non influenceable practical problem leaves the admitted PM plus path`() {
        val state = confirmed(bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.PRACTICAL_HELP))).copy(
            problemInfluence = PolicyEvidence.reported(ProblemInfluence.NOT_INFLUENCEABLE, "influence"),
        )
        val decision = evaluate(state)
        assertEquals(CorePolicyDisposition.OUT_OF_SCOPE, decision.disposition)
        assertEquals("NON_INFLUENCEABLE_PROBLEM_POLICY_REQUIRED", decision.handoffRequirement)
    }

    @Test fun `problem route asks readiness before options`() {
        val state = confirmed(bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.PRACTICAL_HELP))).copy(
            problemInfluence = PolicyEvidence.reported(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE, "influence"),
        )
        assertAction(state, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-ask-readiness-for-options")
    }

    @Test fun `problem route elicits options and never supplies them`() {
        val state = problemReady()
        val decision = assertAction(state, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-invite-user-options")
        assertEquals(false, decision.selectedAction!!.definition.renderSpecification.advicePermitted)
        assertTrue(decision.selectedAction!!.definition.renderSpecification.userAgencyMustBeExplicitlyPreserved)
    }

    @Test fun `established options advance to user choice`() {
        val state = problemReady().copy(generatedOptions = PolicyEvidence.reported(listOf("fictional A", "fictional B"), "options"))
        assertAction(state, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-ask-user-to-choose-option")
    }

    @Test fun `user choice advances to bounded planning`() {
        val state = problemReady().copy(
            generatedOptions = PolicyEvidence.reported(listOf("fictional A", "fictional B"), "options"),
            selectedOption = PolicyEvidence.reported("fictional A", "selection"),
        )
        assertAction(state, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-develop-bounded-plan")
    }

    @Test fun `established plan waits without generating language`() {
        val state = planned()
        assertAction(state, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-wait-for-outcome")
    }

    @Test fun `reported outcome advances through review then consolidation`() {
        val state = planned().copy(
            planOutcome = PolicyEvidence.reported(PlanOutcome.ATTEMPTED, "outcome"),
            planReviewStatus = PolicyEvidence.reported(PlanReviewStatus.NOT_REVIEWED, "review-state"),
        )
        assertAction(state, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-review-reported-outcome")
        assertAction(
            state.copy(planReviewStatus = PolicyEvidence.reported(PlanReviewStatus.REVIEWED, "reviewed")),
            OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING,
            "core-consolidate-plan-learning",
        )
    }

    @Test fun `conflicting structured evidence stops route selection`() {
        val state = CoreOrdinaryTestFixtures.state().copy(
            routePreference = PolicyEvidence.conflicting("preference-a", "preference-b"),
        )
        assertEquals(CorePolicyDisposition.POLICY_CONFLICT, evaluate(state).disposition)
    }

    @Test fun `malformed structured state is rejected before policy`() {
        val state = problemReady().copy(actionPlan = PolicyEvidence.reported("orphan plan", "invalid"))
        val decision = evaluate(state)
        assertEquals(CorePolicyDisposition.INVALID_INPUT, decision.disposition)
    }

    @Test fun `same state and permit produce equal decisions`() {
        val state = bounded(CoreOrdinaryTestFixtures.state())
        val permit = CoreOrdinaryTestFixtures.permit(state)
        assertEquals(evaluator.evaluate(state, permit), evaluator.evaluate(state, permit))
    }

    private fun assertAction(state: com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState, route: OrdinaryRoute, action: String): com.conundrum.thomas.v2.engine.ordinary.CorePolicyDecision {
        val decision = evaluate(state)
        assertEquals(CorePolicyDisposition.ACTION_SELECTED, decision.disposition)
        assertEquals(route, decision.routeSelection.selectedRoute)
        assertEquals(action, decision.selectedAction!!.definition.id.value)
        assertNotNull(decision.activeGoalId)
        return decision
    }

    private fun evaluate(state: com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState) =
        evaluator.evaluate(state, CoreOrdinaryTestFixtures.permit(state))

    private fun bounded(state: com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState) = state.copy(
        concernStatement = PolicyEvidence.reported("a bounded fictional concern", "concern"),
        problemClarity = PolicyEvidence.reported(ProblemClarity.BOUNDED, "clarity"),
    )

    private fun tentative(state: com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState) = state.copy(
        thomasUnderstanding = PolicyEvidence.tentative("the concern may have felt excluding", "tentative"),
        sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "tentative"),
    )

    private fun confirmed(state: com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState) = state.copy(
        thomasUnderstanding = PolicyEvidence.reported("the established fictional understanding", "confirmed"),
        sharedUnderstanding = PolicyEvidence.reported(SharedUnderstanding.CONFIRMED, "confirmed"),
    )

    private fun problemReady() = confirmed(bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.PRACTICAL_HELP))).copy(
        problemInfluence = PolicyEvidence.reported(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE, "influence"),
        willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_ACT, "willing"),
    )

    private fun planned() = problemReady().copy(
        generatedOptions = PolicyEvidence.reported(listOf("fictional A", "fictional B"), "options"),
        selectedOption = PolicyEvidence.reported("fictional A", "selection"),
        actionPlan = PolicyEvidence.reported("try fictional A on Thursday", "plan"),
    )
}
