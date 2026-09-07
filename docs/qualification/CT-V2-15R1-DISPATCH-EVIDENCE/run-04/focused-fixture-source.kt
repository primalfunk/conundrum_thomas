package com.conundrum.thomas.v2

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Repeats actual touch submissions; no procedural state injection or forced Thomas response. */
class CTV215R1DispatchInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var model: ThomasViewModel
    private fun users() = model.state.value.transcript.count { it.role == TranscriptRole.USER }

    @Test fun twentyConsecutiveTouchSubmissionsCrossPresentationAdmissionExactlyOnce() {
        assertEquals(CTV215R1ProductionDeviceInstrumentedTest.FIXTURE,
            InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        compose.activityRule.scenario.onActivity { activity ->
            model = ViewModelProvider(activity)[ThomasViewModel::class.java]
            fun watch(view: View) {
                if (view.javaClass.simpleName == "AndroidComposeView") {
                    view.setOnTouchListener { _, event ->
                        if (event.actionMasked in listOf(0, 1, 3)) SubmissionDispatchProbe.record("COMPOSE_VIEW_TOUCH",
                            "action=" + event.actionMasked + " x=" + event.x + " y=" + event.y)
                        false
                    }
                }
                if (view is ViewGroup) for (i in 0 until view.childCount) watch(view.getChildAt(i))
            }
            watch(activity.window.decorView)
        }
        compose.onNodeWithTag("mode-therapy").performClick()
        val inputs = listOf(
            CTV215R1ProductionDeviceInstrumentedTest.DECLARATIONS + "\nMy specific concern is: the synthetic UI appointment",
            "My specific concern is: THE synthetic UI appointment!",
            "My specific concern is: the synthetic UI appointment.",
            "Another detail is: the organizer moved the date",
            "Please pause",
            "I am ready to resume",
            "I don't want to discuss this",
            "I want to continue",
            "I don’t want to discuss this",
            "I am ready to resume",
            "That's all for now",
            "Thank you",
            "Stop",
            "My specific concern is: a different synthetic scheduling question",
            "Another detail is: the invitation contains an earlier date and I need to understand which date was intended before describing the rest of this synthetic scheduling concern.",
            "Yes, that's right",
            "Please pause",
            "I am ready to resume",
            "I do not want to discuss this",
            "I want to continue",
        )
        inputs.forEachIndexed { ordinal, text ->
            val before = users()
            compose.onNodeWithTag("turn-draft").performTextInput(text)
            println("R1 DISPATCH_TOUCH ordinal=" + (ordinal + 1) + " input=" + text)
            compose.onNodeWithTag("commit-turn").performClick()
            // Synchronous admission must be visible when the actual click callback returns.
            compose.runOnIdle {
                assertTrue("SILENT_SUBMISSION_LOSS ordinal=" + (ordinal + 1) +
                    " processing=" + model.state.value.processing + " draftLength=" + model.state.value.draft.length,
                    model.state.value.processing || users() == before + 1)
            }
            compose.waitUntil(60_000) { !model.state.value.processing && model.state.value.draft.isEmpty() }
            assertEquals("Exactly one user turn per submit", before + 1, users())
            val turn = model.state.value.transcript.last { it.role == TranscriptRole.USER }
            assertTrue("Submitted source must be committed", turn.committed)
            assertEquals(text, turn.text)
            compose.onNodeWithTag("commit-turn").assertExists()
            println("R1 DISPATCH_ACCEPTED ordinal=" + (ordinal + 1) + " identity=" + turn.id +
                " committed=true draftEmpty=" + model.state.value.draft.isEmpty())
        }
        assertEquals(20, users())
        println("R1 TWENTY_TOUCH_SUBMISSIONS_EXACTLY_ONCE=true")
    }
}
