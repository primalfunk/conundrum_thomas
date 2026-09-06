package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.engine.ordinary.*
import com.conundrum.thomas.v2.languagerenderer.*
import com.conundrum.thomas.v2.runtime.*
import org.junit.Assert.*
import org.junit.Test

class CTV215R1RecurrenceRuntimeTest {
    @Test fun physicalPauseResumeTraceRequiresDistinctAuthorizedResponse(): Unit = CTV215Harness().use { h ->
        var index = 23L
        var history = RenderHistoryState()
        var firstInvitation: String? = null
        fun send(text: String, expected: String, scope: Boolean = false): ProductionTurnResult {
            val before = history
            val turn = index.also { index += 2 }
            val result = h.runtime.submit(h.turn(turn, ProductionThomasMode.THERAPY,
                (if (scope) CTV215R1ProductionConversationTest.DECLARATIONS + "\n" else "") + text) {
                copy(requestedTherapySupport = RequestedOrdinarySupport.LISTEN)
            })
            assertEquals("core-$expected", result.therapyPlan!!.routeDecision!!.selectedActionId)
            val command = TherapyRenderCommandAdapter.adapt(RenderCommandId.parse("android.therapy.$turn"),
                turn.toInt(), result.therapyPlan!!.renderSupport!!)
            println("RECURRENCE turn=$turn input=$text state=${result.therapyObservation} command=$command history=$before result=${result.renderResult}")
            if (expected == "invite-further-expression" && turn == 37L) {
                val oldForm = requireNotNull(firstInvitation)
                val collision = DeterministicRenderValidator().validate(command,
                    CandidateRealization(oldForm, "qualification-reference", "v1", CTV213TestSupport.manifest(command)), before)
                println("OLD_FORM_COLLISION=$collision")
                assertTrue(RenderValidationReason.EXACT_RECENT_DUPLICATE in collision.reasonCodes)
                assertEquals(ProgressionDisposition.NEW_ACTION, result.therapyPlan!!.routeDecision!!.progression)
                assertEquals(OrdinaryRoute.LISTEN_SUPPORT, result.therapyPlan!!.routeDecision!!.route)
                assertEquals(GovernedSemanticAct.CLARIFYING_QUESTION, result.renderResult!!.semanticAct)
                assertEquals(GovernedRenderMode.THERAPY, result.renderResult!!.mode)
                assertTrue(command.authorizedReferenceRealizations.contains(result.assistantArtifact?.text))
                assertNotEquals(oldForm, result.assistantArtifact?.text)
            }
            if (expected == "pause-without-response") {
                assertEquals(ProductionTurnDisposition.NO_RESPONSE, result.disposition)
                assertEquals(GovernedSemanticAct.NO_RESPONSE, result.renderResult!!.semanticAct)
                assertEquals(before, result.renderResult!!.nextHistory)
                assertNull(result.assistantArtifact)
            } else {
                assertEquals(result.renderResult.toString(), ProductionTurnDisposition.COMPLETED, result.disposition)
                assertTrue(result.renderResult!!.validation.accepted)
                assertEquals(listOf(RenderValidationReason.VALID), result.renderResult!!.validation.reasonCodes)
            }
            if (turn == 25L) firstInvitation = result.assistantArtifact!!.text
            history = result.renderResult!!.nextHistory
            return result
        }
        send("My specific concern is: the delayed meeting", "reflect-established-content", true)
        send("Yes, that's right", "invite-further-expression")
        send("That's all for now", "summarize-listening")
        send("Thank you", "check-further-or-close")
        send("Stop", "acknowledge-close")
        send("My specific concern is: a separate project meeting", "reflect-established-content")
        val pause = send("Please pause", "pause-without-response")
        val resume = send("I am ready to resume", "invite-further-expression")
        assertTrue(resume.therapyObservation!!.conversationRevision > pause.therapyObservation!!.conversationRevision)
        send("I don't want to discuss this", "pause-without-response")
        send("I want to continue", "invite-further-expression")
    }

    @Test fun unchangedStateStagnatesUpstreamAndNewEvidenceAuthorizesDistinctReflection(): Unit = CTV215Harness().use { h ->
        var turn = 100L
        fun send(text: String): ProductionTurnResult = h.runtime.submit(h.turn(turn++, ProductionThomasMode.THERAPY,
            (if (turn == 101L) CTV215R1ProductionConversationTest.DECLARATIONS + "\n" else "") + text) {
            copy(requestedTherapySupport = RequestedOrdinarySupport.LISTEN)
        })
        val first = send("My specific concern is: the project meeting")
        val repeated = send("My specific concern is: THE project meeting!")
        assertEquals(first.therapyObservation!!.conversationRevision, repeated.therapyObservation!!.conversationRevision)
        assertEquals("core-offer-direction-choice", repeated.therapyPlan!!.routeDecision!!.selectedActionId)
        val stopped = send("My specific concern is: the project meeting.")
        assertEquals(ProgressionDisposition.STOP_NO_PROGRESS, stopped.therapyPlan!!.routeDecision!!.progression)
        assertNull(stopped.assistantArtifact)
        val changed = send("Another detail is: the manager moved the date")
        assertTrue(changed.therapyObservation!!.conversationRevision > stopped.therapyObservation!!.conversationRevision)
        assertEquals("core-reflect-established-content", changed.therapyPlan!!.routeDecision!!.selectedActionId)
        assertEquals(first.therapyPlan!!.routeDecision!!.route, changed.therapyPlan!!.routeDecision!!.route)
        assertEquals(ProductionTurnDisposition.COMPLETED, changed.disposition)
        assertTrue(changed.renderResult!!.validation.accepted)
        assertEquals(first.renderResult!!.semanticAct, changed.renderResult!!.semanticAct)
        assertNotEquals(first.assistantArtifact!!.text, changed.assistantArtifact!!.text)
    }
}
