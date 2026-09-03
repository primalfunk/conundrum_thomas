package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.engine.verticalslice.PolicyRenderRequestFactory
import com.conundrum.thomas.v2.qualification.verticalslice.CanonicalBoundedProblemScenario
import com.conundrum.thomas.v2.qualification.verticalslice.DeterministicQualificationRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RendererBoundaryQualificationTest {
    @Test
    fun `canonical scenario completes without a language model`() {
        val turns = CanonicalBoundedProblemScenario.run()
        assertEquals(9, turns.size)
        assertTrue(turns.all { it.rendered.selectedPolicyActionId == it.renderRequest.command.selectedPolicyActionId })
        assertTrue(turns.all { it.rendered.preservedDialogueActId == it.decision.selectedAction?.definition?.dialogueActId?.value })
    }

    @Test
    fun `renderer receives only explicitly authorized evidence`() {
        val turns = CanonicalBoundedProblemScenario.run()
        assertTrue(turns[0].renderRequest.authorizedSupportingText.isEmpty())
        assertEquals(setOf("problem-statement"), turns[2].renderRequest.authorizedSupportingText.map { it.reference }.toSet())
        assertEquals(setOf("user-option-1", "user-option-2"), turns[5].renderRequest.authorizedSupportingText.map { it.reference }.toSet())
        assertEquals(setOf("user-selected-option"), turns[6].renderRequest.authorizedSupportingText.map { it.reference }.toSet())
        assertEquals(setOf("action-plan", "plan-outcome"), turns[8].renderRequest.authorizedSupportingText.map { it.reference }.toSet())
    }

    @Test
    fun `renderer contract fixes selected act and denies advice`() {
        CanonicalBoundedProblemScenario.run().forEach { turn ->
            val command = turn.renderRequest.command
            assertEquals(turn.decision.selectedAction?.definition?.id?.value, command.selectedPolicyActionId)
            assertEquals(turn.decision.selectedAction?.definition?.dialogueActId?.value, command.selectedDialogueActId)
            assertEquals(turn.decision.activeGoalId?.value, command.therapeuticGoalId)
            assertFalse(command.advicePermitted)
            assertTrue(command.prohibitedSemanticContent.any { it.contains("different dialogue act", ignoreCase = true) })
        }
    }

    @Test
    fun `question count is bounded to one principal question`() {
        CanonicalBoundedProblemScenario.run().forEach { turn ->
            assertTrue(turn.renderRequest.command.maximumQuestions <= 1)
            val actualQuestions = turn.rendered.draft.text.count { it == '?' }
            assertTrue(actualQuestions <= turn.renderRequest.command.maximumQuestions)
        }
    }

    @Test
    fun `no-response action renders exact silence`() {
        val state = CanonicalBoundedProblemScenario.states()[7]
        val decision = com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyEvaluator().evaluate(state)
        val request = PolicyRenderRequestFactory.create(decision, state)
        val result = DeterministicQualificationRenderer().renderNow(request)
        assertEquals(RenderOutputDisposition.NO_RESPONSE, request.command.outputDisposition)
        assertEquals("dialogue.no-response", request.command.selectedDialogueActId)
        assertEquals("", result.draft.text)
    }
}
