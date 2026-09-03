package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryRoute
import com.conundrum.thomas.v2.engine.ordinary.ProgressionDisposition
import com.conundrum.thomas.v2.qualification.ordinary.CanonicalCoreConversations
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOrdinaryConversationTest {
    @Test
    fun `conversation A listens without premature problem solving`() {
        val turns = CanonicalCoreConversations.listening()
        assertEquals(
            listOf(
                "core-invite-expression",
                "core-reflect-established-content",
                "core-invite-further-expression",
                "core-summarize-listening",
                "core-check-further-or-close",
                "core-acknowledge-close",
            ),
            turns.map { it.decision.selectedAction?.definition?.id?.value },
        )
        assertTrue(turns.take(5).all { it.decision.routeSelection.selectedRoute == OrdinaryRoute.LISTEN_SUPPORT })
        assertEquals(OrdinaryRoute.CONSOLIDATE_CLOSE, turns.last().decision.routeSelection.selectedRoute)
        assertTrue(turns.take(5).none { it.renderRequest!!.command.selectedPolicyActionId.contains("option") })
        assertTrue(turns.take(5).all { !it.renderRequest!!.command.advicePermitted })
    }

    @Test
    fun `conversation B clarifies verifies tentative meaning and summarizes only after confirmation`() {
        val turns = CanonicalCoreConversations.understanding()
        assertEquals(
            listOf(
                "core-ask-important-missing-piece",
                "core-verify-tentative-understanding",
                "core-summarize-shared-understanding",
                "core-check-understanding-next-direction",
            ),
            turns.map { it.decision.selectedAction?.definition?.id?.value },
        )
        assertTrue(turns[1].renderRequest!!.command.interpretationMustRemainTentative)
        assertTrue(turns[1].rendered!!.draft.text.contains("might"))
        assertFalse(turns[2].renderRequest!!.command.interpretationMustRemainTentative)
        assertTrue(turns.all { it.decision.routeSelection.selectedRoute == OrdinaryRoute.UNDERSTAND_CLARIFY })
    }

    @Test
    fun `conversation C progresses through bounded PM plus problem solving`() {
        val turns = CanonicalCoreConversations.practicalProblemSolving()
        assertEquals(
            listOf(
                "core-ask-problem-description",
                "core-verify-problem-understanding",
                "core-ask-influenceable-part",
                "core-ask-readiness-for-options",
                "core-invite-user-options",
                "core-ask-user-to-choose-option",
                "core-develop-bounded-plan",
                "core-wait-for-outcome",
                "core-review-reported-outcome",
                "core-consolidate-plan-learning",
            ),
            turns.map { it.decision.selectedAction?.definition?.id?.value },
        )
        assertEquals("", turns[7].rendered!!.draft.text)
        assertFalse(turns[4].renderRequest!!.command.advicePermitted)
        assertTrue(turns[4].renderRequest!!.command.userAgencyMustBeExplicitlyPreserved)
    }

    @Test
    fun `conversation D changes from listening to understanding on explicit preference evidence`() {
        val turns = CanonicalCoreConversations.preferenceChange()
        assertEquals(OrdinaryRoute.LISTEN_SUPPORT, turns[0].decision.routeSelection.selectedRoute)
        assertEquals(OrdinaryRoute.UNDERSTAND_CLARIFY, turns[1].decision.routeSelection.selectedRoute)
        assertEquals(OrdinaryRoute.LISTEN_SUPPORT, turns[1].decision.routeSelection.transition!!.from)
        assertEquals(OrdinaryRoute.UNDERSTAND_CLARIFY, turns[1].decision.routeSelection.transition!!.to)
        assertEquals("core-ask-important-missing-piece", turns[1].decision.selectedAction!!.definition.id.value)
    }

    @Test
    fun `conversation E withdraws the corrected interpretation and never argues`() {
        val turns = CanonicalCoreConversations.correction()
        assertEquals("core-verify-tentative-understanding", turns[0].decision.selectedAction!!.definition.id.value)
        assertEquals("core-acknowledge-correction", turns[1].decision.selectedAction!!.definition.id.value)
        assertNull(turns[1].state.thomasUnderstanding.value)
        assertTrue("correction-thomas-1" in turns[1].state.withdrawnInterpretationReferences)
        assertTrue(turns[1].rendered!!.draft.text.startsWith("I had that wrong"))
        assertFalse(turns[1].rendered!!.draft.text.contains("but", ignoreCase = true))
        assertEquals("core-verify-tentative-understanding", turns[2].decision.selectedAction!!.definition.id.value)
    }

    @Test
    fun `conversation F substitutes once then stops on unchanged evidence`() {
        val turns = CanonicalCoreConversations.stagnation()
        assertEquals("core-reflect-established-content", turns[0].decision.selectedAction!!.definition.id.value)
        assertEquals("core-offer-direction-choice", turns[1].decision.selectedAction!!.definition.id.value)
        assertEquals(ProgressionDisposition.SUBSTITUTE_DIRECTION_CHOICE, turns[1].decision.progressionTrace!!.disposition)
        assertEquals(CorePolicyDisposition.NO_AUTHORIZED_ACTION, turns[2].decision.disposition)
        assertNull(turns[2].rendered)
        assertEquals(ProgressionDisposition.STOP_NO_PROGRESS, turns[2].decision.progressionTrace!!.disposition)
    }

    @Test
    fun `conversation G revokes ordinary authority when safety evidence revision changes`() {
        val fixture = CanonicalCoreConversations.safetyRevocation()
        assertEquals(CorePolicyDisposition.ACTION_SELECTED, fixture.initialDecision.disposition)
        assertEquals(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, fixture.revisedGateAuthority)
        assertEquals(CorePolicyDisposition.INVALID_INPUT, fixture.stalePermitAttempt.disposition)
        assertEquals("FRESH_SAFETY_SCOPE_GATE_DECISION_REQUIRED", fixture.stalePermitAttempt.handoffRequirement)
    }
}
