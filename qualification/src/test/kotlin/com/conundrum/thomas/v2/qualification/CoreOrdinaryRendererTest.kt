package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryActions
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.CoreRenderRequestFactory
import com.conundrum.thomas.v2.engine.ordinary.ExpressionProgress
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import com.conundrum.thomas.v2.qualification.ordinary.CanonicalCoreConversations
import com.conundrum.thomas.v2.qualification.ordinary.DeterministicCoreOrdinaryRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOrdinaryRendererTest {
    private val renderer = DeterministicCoreOrdinaryRenderer()

    @Test fun `renderer preserves every selected act and goal across canonical conversations`() {
        val turns = listOf(
            CanonicalCoreConversations.listening(),
            CanonicalCoreConversations.understanding(),
            CanonicalCoreConversations.practicalProblemSolving(),
            CanonicalCoreConversations.preferenceChange(),
            CanonicalCoreConversations.correction(),
            CanonicalCoreConversations.stagnation(),
        ).flatten().filter { it.decision.disposition == CorePolicyDisposition.ACTION_SELECTED }
        turns.forEach { turn ->
            assertEquals(turn.decision.selectedAction!!.definition.id.value, turn.rendered!!.selectedActionId)
            assertEquals(turn.decision.selectedAction!!.definition.dialogueActId.value, turn.rendered!!.preservedDialogueActId)
            assertEquals(turn.decision.activeGoalId!!.value, turn.rendered!!.preservedGoalId)
        }
    }

    @Test fun `tentative renderer contract cannot receive established text as its tentative support`() {
        val state = CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.UNDERSTAND).copy(
            concernStatement = PolicyEvidence.reported("a fictional concern", "concern"),
            problemClarity = PolicyEvidence.reported(ProblemClarity.BOUNDED, "clarity"),
            thomasUnderstanding = PolicyEvidence.reported("an established statement", "established"),
            sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "tentative-status"),
        )
        val evaluator = CoreOrdinaryTherapyEvaluator()
        val decision = evaluator.evaluate(state, CoreOrdinaryTestFixtures.permit(state))
        assertEquals(CorePolicyDisposition.INVALID_INPUT, decision.disposition)
    }

    @Test fun `tentative output uses explicit uncertainty and one question`() {
        val turn = CanonicalCoreConversations.understanding()[1]
        val command = turn.renderRequest!!.command
        val text = turn.rendered!!.draft.text
        assertTrue(command.interpretationMustRemainTentative)
        assertTrue(text.contains("might"))
        assertEquals(1, text.count { it == '?' })
    }

    @Test fun `option elicitation forbids advice and renderer adds no option`() {
        val turn = CanonicalCoreConversations.practicalProblemSolving()[4]
        assertEquals("core-invite-user-options", turn.rendered!!.selectedActionId)
        assertFalse(turn.renderRequest!!.command.advicePermitted)
        assertTrue(turn.renderRequest!!.command.userAgencyMustBeExplicitlyPreserved)
        assertTrue(turn.renderRequest!!.authorizedSupportingText.isEmpty())
        assertFalse(turn.rendered!!.draft.text.contains("you should", ignoreCase = true))
    }

    @Test fun `no response is an exact empty output and is not response-required`() {
        val turn = CanonicalCoreConversations.practicalProblemSolving()[7]
        assertEquals(RenderOutputDisposition.NO_RESPONSE, turn.renderRequest!!.command.outputDisposition)
        assertFalse(turn.renderRequest!!.command.responseRequired)
        assertEquals("", turn.rendered!!.draft.text)
    }

    @Test fun `terminal decision cannot be rendered`() {
        val state = CoreOrdinaryTestFixtures.state(support = RequestedOrdinarySupport.DECISION_SUPPORT)
        val evaluator = CoreOrdinaryTherapyEvaluator()
        val decision = evaluator.evaluate(state, CoreOrdinaryTestFixtures.permit(state))
        assertThrows(IllegalArgumentException::class.java) { CoreRenderRequestFactory.create(decision, state) }
    }

    @Test fun `every core renderer contract forbids unauthorized advice and new interpretation`() {
        CoreOrdinaryActions.all.forEach { action ->
            assertFalse(action.renderSpecification.advicePermitted)
            val prohibited = action.renderSpecification.prohibitedSemanticContent.joinToString(" ")
            assertTrue(prohibited.contains("Advice"))
            assertTrue(prohibited.contains("interpretation"))
        }
    }
}
