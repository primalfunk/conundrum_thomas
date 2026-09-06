package com.conundrum.thomas.v2

import android.os.Process
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.conundrum.thomas.v2.engine.ordinary.*
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.languagerenderer.*
import com.conundrum.thomas.v2.runtime.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Read-only inspection of actual delivered action/history; no state injection or policy selection. */
@RunWith(AndroidJUnit4::class)
class CTV215R1ProductionUiInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var model: ThomasViewModel
    private lateinit var runtime: ThomasProductionRuntime
    @Suppress("UNCHECKED_CAST")
    private fun <T> read(owner: Any, name: String): T =
        owner.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(owner) as T
    private fun session(): CoreOrdinaryTherapyState = read(read<Any>(runtime, "therapyInput"), "session")
    private fun assistantCount() = model.state.value.transcript.count { it.role == TranscriptRole.THOMAS }
    private fun send(text: String, expectedAction: String?, silent: Boolean = false): CoreOrdinaryTherapyState {
        val before = assistantCount()
        compose.onNodeWithTag("turn-draft").performTextInput(text)
        compose.onNodeWithTag("commit-turn").performClick()
        compose.waitUntil(20_000) { !model.state.value.processing && model.state.value.draft.isEmpty() }
        val state = session()
        if (expectedAction != null) assertEquals("core-" + expectedAction, state.actionHistory.last().actionId.value)
        assertEquals(ProductionThomasMode.THERAPY, model.state.value.mode)
        if (silent) assertEquals(before, assistantCount()) else {
            assertEquals(before + 1, assistantCount())
            assertEquals("Completed", model.state.value.status)
            val actual = model.state.value.transcript.last()
            assertEquals(TranscriptRole.THOMAS, actual.role)
            val history = read<RenderHistoryState>(runtime, "renderHistory").entries.last()
            val renderText = Class.forName("com.conundrum.thomas.v2.languagerenderer.RenderText")
            val fingerprint = renderText.getDeclaredMethod("responseFingerprint", String::class.java).invoke(
                renderText.getDeclaredField("INSTANCE").get(null), actual.text)
            assertEquals(history.normalizedResponseFingerprint, fingerprint)
            assertTrue(history.semanticAct in setOf(GovernedSemanticAct.BRIEF_REFLECTION,
                GovernedSemanticAct.CLARIFYING_QUESTION, GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION))
        }
        println("R1 UI input=" + text + " expectedAction=" + expectedAction + " actual=" + state.actionHistory.lastOrNull() +
            " revision=" + state.conversationRevision + " pending=" + state.pendingInformation +
            " status=" + model.state.value.status + " displayed=" + model.state.value.transcript.lastOrNull()?.text)
        return state
    }

    @Test fun cActualInputControlsAndSemanticProgression() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(CTV215R1ProductionDeviceInstrumentedTest.FIXTURE, context.packageName)
        assertNotEquals(requireNotNull(InstrumentationRegistry.getArguments().getString("canonicalUid")).toInt(), Process.myUid())
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        runtime = read<ThomasAndroidCompositionRoot>(model, "root").runtime!!
        assertTrue("Run complete device scenarios first", model.state.value.sourceSummaries.size > 20)
        compose.onNodeWithTag("mode-therapy").performClick()
        send(CTV215R1ProductionDeviceInstrumentedTest.DECLARATIONS + "\nMy specific concern is: the synthetic UI appointment", "reflect-established-content")
        val initial = session().conversationRevision
        send("My specific concern is: THE synthetic UI appointment!", "offer-direction-choice")
        assertEquals(initial, session().conversationRevision)
        send("My specific concern is: the synthetic UI appointment.", null, silent = true)
        assertEquals(initial, session().conversationRevision)
        send("Another detail is: the organizer moved the date", "reflect-established-content")
        assertTrue(session().conversationRevision > initial)
        assertEquals(OrdinaryEngagement.PAUSE_REQUESTED, send("Please pause", "pause-without-response", true).engagement.value)
        send("I am ready to resume", "invite-further-expression")
        assertEquals(OrdinaryEngagement.DOES_NOT_WANT_TOPIC, send("I don't want to discuss this", "pause-without-response", true).engagement.value)
        send("I want to continue", "invite-further-expression")
        send("That's all for now", "summarize-listening")
        send("Thank you", "check-further-or-close")
        compose.onNodeWithText("Understand").performClick()
        send("My specific concern is: a cancelled UI meeting\nWhat I haven't explained is: who changed it", "ask-important-missing-piece")
        send("The missing detail is: the organizer changed the time", "verify-tentative-understanding")
        assertFalse(session().thomasUnderstanding.isEstablished())
        send("No, that's not what I mean", "acknowledge-correction")
        assertNull(session().thomasUnderstanding.value)
        send("What I mean is: the UI meeting was delayed", "verify-tentative-understanding")
        send("Yes, that's right", "summarize-shared-understanding")
        assertTrue(session().sharedUnderstanding.isEstablished())
        compose.onNodeWithText("Practical").performClick()
        send("My specific concern is: arranging a new UI meeting", "verify-problem-understanding")
        send("Yes, that's right", "ask-influenceable-part")
        send("I can influence: contacting the organizer", "ask-readiness-for-options")
        send("I am willing to act", "invite-user-options")
        send("My options are: email; call", "ask-user-to-choose-option")
        assertNull(session().selectedOption.value)
        send("I choose: email", "develop-bounded-plan")
        send("My first step is: email the organizer; when: tomorrow morning", "wait-for-outcome", true)
        assertNull(session().planOutcome.value)
        send("I have not attempted the plan", "review-reported-outcome")
        assertEquals(PlanOutcome.NOT_ATTEMPTED, session().planOutcome.value)
        send("What happened was: I did not have time", "consolidate-plan-learning")
        assertEquals(PlanReviewStatus.REVIEWED, session().planReviewStatus.value)
        println("R1 UI_CONTROLS_COMPLETE=true")
    }
}
