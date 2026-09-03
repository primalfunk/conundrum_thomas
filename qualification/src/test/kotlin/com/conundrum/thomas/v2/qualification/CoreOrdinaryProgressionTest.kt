package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.engine.ordinary.CoreActionExecution
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.ExplicitRepeatAuthorization
import com.conundrum.thomas.v2.engine.ordinary.ExpressionProgress
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryEngagement
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryRoute
import com.conundrum.thomas.v2.engine.ordinary.ProgressionDisposition
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.ProblemInfluence
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOrdinaryProgressionTest {
    private val evaluator = CoreOrdinaryTherapyEvaluator()

    @Test fun `same action and unchanged evidence substitutes once then stops`() {
        val action = PolicyActionId.parse("core-reflect-established-content")
        val base = listeningNewContent()
        val first = evaluate(base)
        val repeated = base.copy(activeRoute = OrdinaryRoute.LISTEN_SUPPORT, actionHistory = listOf(CoreActionExecution(action, 1, OrdinaryRoute.LISTEN_SUPPORT)))
        val second = evaluate(repeated)
        val third = evaluate(repeated.copy(actionHistory = repeated.actionHistory + CoreActionExecution(
            PolicyActionId.parse("core-offer-direction-choice"), 1, OrdinaryRoute.LISTEN_SUPPORT,
        )))
        assertEquals(action, first.selectedAction!!.definition.id)
        assertEquals("core-offer-direction-choice", second.selectedAction!!.definition.id.value)
        assertEquals(ProgressionDisposition.SUBSTITUTE_DIRECTION_CHOICE, second.progressionTrace!!.disposition)
        assertEquals(CorePolicyDisposition.NO_AUTHORIZED_ACTION, third.disposition)
        assertNull(third.selectedAction)
    }

    @Test fun `answered support question is not immediately asked again`() {
        val unknown = CoreOrdinaryTestFixtures.state(support = null)
        assertEquals("core-ask-support-preference", evaluate(unknown).selectedAction!!.definition.id.value)
        val answered = unknown.copy(
            stateId = "answered-route",
            conversationRevision = 2,
            activeRoute = OrdinaryRoute.CLARIFY_PREFERENCE,
            routePreference = PolicyEvidence.reported(RequestedOrdinarySupport.LISTEN, "answered-preference"),
            actionHistory = listOf(CoreActionExecution(PolicyActionId.parse("core-ask-support-preference"), 1, OrdinaryRoute.CLARIFY_PREFERENCE)),
        )
        assertEquals("core-invite-expression", evaluate(answered).selectedAction!!.definition.id.value)
    }

    @Test fun `resolved information requirement disappears and goal advances`() {
        val state = bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND)).copy(
            importantMissingInformation = PolicyEvidence.reported("which fictional event came first", "gap"),
        )
        assertEquals("core-ask-important-missing-piece", evaluate(state).selectedAction!!.definition.id.value)
        val answered = state.copy(
            stateId = "resolved-gap",
            conversationRevision = 2,
            activeRoute = OrdinaryRoute.UNDERSTAND_CLARIFY,
            importantMissingInformation = PolicyEvidence.unknown(),
            thomasUnderstanding = PolicyEvidence.tentative("the order may have changed the meaning", "new-understanding"),
            sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "new-understanding"),
            actionHistory = listOf(CoreActionExecution(PolicyActionId.parse("core-ask-important-missing-piece"), 1, OrdinaryRoute.UNDERSTAND_CLARIFY)),
        )
        assertEquals("core-verify-tentative-understanding", evaluate(answered).selectedAction!!.definition.id.value)
    }

    @Test fun `completed listening goal advances toward closure`() {
        val complete = bounded(CoreOrdinaryTestFixtures.state()).copy(
            expressionProgress = PolicyEvidence.reported(ExpressionProgress.EXPRESSION_COMPLETE, "complete"),
        )
        assertEquals("core-summarize-listening", evaluate(complete).selectedAction!!.definition.id.value)
        val summarized = complete.copy(expressionProgress = PolicyEvidence.reported(ExpressionProgress.SUMMARY_DELIVERED, "summarized"))
        assertEquals("core-check-further-or-close", evaluate(summarized).selectedAction!!.definition.id.value)
    }

    @Test fun `explicit user route change overrides prior route`() {
        val state = bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND, revision = 2)).copy(
            activeRoute = OrdinaryRoute.LISTEN_SUPPORT,
            importantMissingInformation = PolicyEvidence.reported("the one missing fictional detail", "gap"),
        )
        val decision = evaluate(state)
        assertEquals(OrdinaryRoute.UNDERSTAND_CLARIFY, decision.routeSelection.selectedRoute)
        assertEquals(OrdinaryRoute.LISTEN_SUPPORT, decision.routeSelection.transition!!.from)
    }

    @Test fun `deterministic progression needs no renderer wording variation`() {
        val firstState = CoreOrdinaryTestFixtures.state()
        val secondState = bounded(firstState.copy(stateId = "progressed-content", conversationRevision = 2)).copy(
            expressionProgress = PolicyEvidence.reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, "content"),
        )
        val first = evaluate(firstState)
        val second = evaluate(secondState)
        assertNotEquals(first.selectedAction!!.definition.id, second.selectedAction!!.definition.id)
        assertEquals("core-invite-expression", first.selectedAction!!.definition.id.value)
        assertEquals("core-reflect-established-content", second.selectedAction!!.definition.id.value)
    }

    @Test fun `reentry after close requires explicit preference and changed evidence`() {
        val closeAction = CoreActionExecution(PolicyActionId.parse("core-acknowledge-close"), 1, OrdinaryRoute.CONSOLIDATE_CLOSE)
        val unchanged = CoreOrdinaryTestFixtures.state().copy(
            activeRoute = OrdinaryRoute.CONSOLIDATE_CLOSE,
            actionHistory = listOf(closeAction),
        )
        assertEquals(CorePolicyDisposition.NO_AUTHORIZED_ACTION, evaluate(unchanged).disposition)
        val changed = unchanged.copy(stateId = "reentry-changed", conversationRevision = 2,
            routePreference = PolicyEvidence.reported(RequestedOrdinarySupport.LISTEN, "explicit-reentry"))
        assertEquals("core-invite-expression", evaluate(changed).selectedAction!!.definition.id.value)
    }

    @Test fun `no response cannot cause an execution loop`() {
        val waitId = PolicyActionId.parse("core-wait-for-outcome")
        val state = planned().copy(
            actionHistory = listOf(CoreActionExecution(waitId, 1, OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING)),
        )
        val decision = evaluate(state)
        assertEquals(CorePolicyDisposition.NO_AUTHORIZED_ACTION, decision.disposition)
        assertEquals(ProgressionDisposition.STOP_NO_PROGRESS, decision.progressionTrace!!.disposition)
        assertEquals("AWAIT_CHANGED_EVIDENCE", decision.handoffRequirement)
    }

    @Test fun `explicit repeat reason permits one additional execution only`() {
        val action = PolicyActionId.parse("core-reflect-established-content")
        val state = listeningNewContent().copy(
            actionHistory = listOf(CoreActionExecution(action, 1, OrdinaryRoute.LISTEN_SUPPORT)),
            explicitRepeatAuthorization = ExplicitRepeatAuthorization(action, 1, "The synthetic user explicitly asked to hear the reflection once more.", "repeat-request"),
        )
        val allowed = evaluate(state)
        assertEquals(ProgressionDisposition.EXPLICIT_REPEAT_ALLOWED, allowed.progressionTrace!!.disposition)
        val consumed = state.copy(actionHistory = state.actionHistory + CoreActionExecution(action, 1, OrdinaryRoute.LISTEN_SUPPORT))
        val stopped = evaluate(consumed)
        assertEquals("core-offer-direction-choice", stopped.selectedAction!!.definition.id.value)
    }

    private fun evaluate(state: CoreOrdinaryTherapyState) = evaluator.evaluate(state, CoreOrdinaryTestFixtures.permit(state))

    private fun bounded(state: CoreOrdinaryTherapyState) = state.copy(
        concernStatement = PolicyEvidence.reported("a bounded fictional concern", "concern"),
        problemClarity = PolicyEvidence.reported(ProblemClarity.BOUNDED, "clarity"),
    )

    private fun listeningNewContent() = bounded(CoreOrdinaryTestFixtures.state()).copy(
        expressionProgress = PolicyEvidence.reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, "new-content"),
    )

    private fun planned() = bounded(CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.PRACTICAL_HELP)).copy(
        thomasUnderstanding = PolicyEvidence.reported("confirmed fictional problem", "confirmed"),
        sharedUnderstanding = PolicyEvidence.reported(SharedUnderstanding.CONFIRMED, "confirmed"),
        problemInfluence = PolicyEvidence.reported(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE, "influence"),
        willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_ACT, "willing"),
        generatedOptions = PolicyEvidence.reported(listOf("fictional option"), "options"),
        selectedOption = PolicyEvidence.reported("fictional option", "selection"),
        actionPlan = PolicyEvidence.reported("try the fictional option", "plan"),
    )
}
