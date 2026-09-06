package com.conundrum.thomas.v2

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.conundrum.thomas.v2.runtime.ProductionThomasMode
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Refuses to submit ANY data to the canonical package. Run a then b with a fixture-only force-stop
 * between runner invocations for process-restart evidence. No reset/restore or real-corpus mutation.
 */
@RunWith(AndroidJUnit4::class)
class CTV215R1ProductionUiInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var model: ThomasViewModel

    private fun fixture() {
        assertEquals("Run only the separately packaged disposable fixture",
            "com.conundrum.thomas.v2.ctv215r1fixture",
            InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
    }

    private fun send(text: String): String? {
        compose.onNodeWithTag("turn-draft").performTextInput(text)
        compose.onNodeWithTag("commit-turn").performClick()
        compose.waitUntil(20_000) { !model.state.value.processing && model.state.value.draft.isEmpty() }
        val value = model.state.value
        println("FIXTURE TURN $text | ${value.status}")
        return value.transcript.lastOrNull { it.mode == value.mode && it.role == TranscriptRole.THOMAS }?.text
    }
    private fun contains(response: String?, expected: String) {
        assertTrue("Expected '$expected' in '$response'", response?.contains(expected, true) == true)
    }
    private fun mode(mode: String) { compose.onNodeWithTag("mode-$mode").performClick() }

    @Test fun aTypedProductionConversationsAndCrossModeRecall() {
        fixture()
        assertTrue("Fixture must start empty; never reset an unknown corpus", model.state.value.sourceSummaries.isEmpty())
        send("I moved to Denver in 2010.")
        send("I moved to Portland in 2018.")
        mode("biographer")
        contains(model.state.value.transcript.last().text, "2010")
        contains(model.state.value.transcript.last().text, "2018")
        contains(send("I moved to Seattle in 2014."), "2014")
        mode("therapy")
        contains(send(DECLARATIONS + "\nMy specific concern is: the delayed meeting"), "hear")
        contains(send("Yes, that's right"), "else")
        contains(send("That's all for now"), "heard")
        contains(send("Thank you"), "stop here")

        compose.onNodeWithText("Understand").performClick()
        contains(send("My specific concern is: a changed meeting\nWhat I haven't explained is: who changed the time"), "clarify")
        contains(send("The missing detail is: my manager changed the time"), "Is that right")
        contains(send("No, that's not what I mean"), "had that wrong")
        contains(send("What I mean is: the manager changed the date"), "Is that right")
        contains(send("Yes, that's right"), "established")

        compose.onNodeWithText("Practical").performClick()
        contains(send("My specific concern is: arranging tomorrow's meeting"), "Is that right")
        contains(send("Yes, that's right"), "influence")
        contains(send("I can influence: contacting the manager"), "options")
        contains(send("I am willing to act"), "ways forward")
        contains(send("My options are: email; call"), "helpful")
        contains(send("I choose: email"), "first step")
        val before = model.state.value.transcript.count { it.role == TranscriptRole.THOMAS }
        send("My first step is: email the manager; when: tomorrow morning")
        assertEquals(before, model.state.value.transcript.count { it.role == TranscriptRole.THOMAS })
        contains(send("I have not attempted the plan"), "plan")
        val reviewed = send("What happened was: I did not have time")
        contains(reviewed, "NOT_ATTEMPTED")
        assertFalse(reviewed.orEmpty().contains("you tried", true))

        compose.onNodeWithText("Listen").performClick()
        compose.onNodeWithTag("therapy-explicit-recall").performClick()
        val journal = send("My specific concern is: remembering Denver\nPlease recall my earlier words: I moved to Denver in 2010.")
        contains(journal, "Journal"); contains(journal, "Denver")
        val biography = send("My specific concern is: remembering Seattle\nPlease recall my earlier words: I moved to Seattle in 2014.")
        contains(biography, "history"); contains(biography, "Seattle")
        contains(send("I withdraw: Self-harm is relevant now."), "self-harm")
        send("Correction: Self-harm is not relevant now.")
        contains(send("Self-harm is relevant now."), "self-harm")
        assertTrue(model.state.value.sourceSummaries.size > 20)
    }

    @Test fun bColdReopenPreservesCorpusButRequiresCurrentSafety() {
        fixture()
        assertTrue("Run a first, then force-stop ONLY the fixture package", model.state.value.sourceSummaries.size > 20)
        mode("therapy")
        contains(send("My specific concern is: remembering Seattle"), "emergency")
        val response = send(DECLARATIONS + "\nMy specific concern is: remembering Seattle\nPlease recall my earlier words: I moved to Seattle in 2014.")
        // Explicit recall remains a user control, not an implicit consequence of the quoted text.
        assertNotNull(response)
        compose.onNodeWithTag("therapy-explicit-recall").performClick()
        val recalled = send("My specific concern is: remembering the Seattle move\nPlease recall my earlier words: I moved to Seattle in 2014.")
        contains(recalled, "history"); contains(recalled, "Seattle")
    }

    companion object {
        private const val DECLARATIONS = "There is no current emergency.\nThere is no acute medical emergency.\nSelf-harm is not relevant now.\nHarm to others is not relevant now.\nI report no specialized condition for this conversation.\nI am an adult in the supported setting.\nMy present concern is one bounded ordinary personal problem."
    }
}
