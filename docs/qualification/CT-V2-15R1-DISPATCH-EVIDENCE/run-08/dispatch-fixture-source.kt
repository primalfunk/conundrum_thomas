package com.conundrum.thomas.v2

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

    @Test fun keyboardMovementCannotStrandAnIntendedSubmission() {
        assertEquals(CTV215R1ProductionDeviceInstrumentedTest.FIXTURE,
            InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        compose.onNodeWithTag("mode-therapy").performClick()
        val driver = UiSubmissionDriver(compose)
        driver.prepareForInput()
        driver.awaitKeyboard(false)
        val staleCenter = compose.onNodeWithTag("commit-turn").fetchSemanticsNode().boundsInRoot.center
        val specimen = "I don’t want to discuss this"
        compose.onNodeWithTag("turn-draft").performTextInput(specimen)
        driver.awaitKeyboard(true)
        val currentBounds = compose.onNodeWithTag("commit-turn").fetchSemanticsNode().boundsInRoot
        assertFalse("The controlled IME transition must invalidate the earlier tap point", currentBounds.contains(staleCenter))
        println("R1 CONTROLLED_STALE_POINT point=$staleCenter currentBounds=$currentBounds")
        // The driver resolves the target after the OS animation, never reuses the obsolete point.
        driver.clickCommit()
        compose.runOnIdle {
            assertTrue("STALE_COORDINATE_MUST_NOT_STRAND_SUBMISSION", model.state.value.processing || users() == 1)
        }
        compose.waitUntil(60_000) { !model.state.value.processing && model.state.value.draft.isEmpty() }
        assertEquals(1, users())
        assertTrue(model.state.value.transcript.single { it.role == TranscriptRole.USER }.committed)
        assertEquals(specimen, model.state.value.transcript.single { it.role == TranscriptRole.USER }.text)
    }

    @Test fun twentyConsecutiveTouchSubmissionsCrossPresentationAdmissionExactlyOnce() {
        assertEquals(CTV215R1ProductionDeviceInstrumentedTest.FIXTURE,
            InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        val driver = UiSubmissionDriver(compose)
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
            val sourceCount = model.state.value.sourceSummaries.size
            driver.prepareForInput()
            compose.onNodeWithTag("turn-draft").performTextInput(text)
            println("R1 DISPATCH_TOUCH ordinal=" + (ordinal + 1) + " input=" + text)
            driver.clickCommit()
            // Synchronous admission must be visible when the actual click callback returns.
            compose.runOnIdle {
                assertTrue("SILENT_SUBMISSION_LOSS ordinal=" + (ordinal + 1) +
                    " processing=" + model.state.value.processing + " draftLength=" + model.state.value.draft.length,
                    model.state.value.processing || users() == before + 1)
            }
            compose.waitUntil(60_000) { !model.state.value.processing && model.state.value.draft.isEmpty() }
            assertEquals("Exactly one user turn per submit", before + 1, users())
            assertEquals("Exactly one committed source per submit", sourceCount + 1, model.state.value.sourceSummaries.size)
            val turn = model.state.value.transcript.last { it.role == TranscriptRole.USER }
            assertTrue("Submitted source must be committed", turn.committed)
            assertEquals(text, turn.text)
            compose.onNodeWithTag("commit-turn").assertExists()
            println("R1 DISPATCH_ACCEPTED ordinal=" + (ordinal + 1) + " identity=" + turn.id +
                " committed=true draftEmpty=" + model.state.value.draft.isEmpty())
        }
        assertEquals(20, users())
        val prefs = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("dispatch-stress", 0)
        val turns = model.state.value.transcript.filter { it.role == TranscriptRole.USER }
        val edit = prefs.edit().putStringSet("identities", turns.map { it.id }.toSet())
        turns.forEach { edit.putString(it.id, it.text) }
        assertTrue(edit.commit())
        println("R1 TWENTY_TOUCH_SUBMISSIONS_EXACTLY_ONCE=true")
    }

    @Test fun twentySubmittedTurnsSurviveProcessReopen() {
        assertEquals(CTV215R1ProductionDeviceInstrumentedTest.FIXTURE,
            InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        val prefs = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("dispatch-stress", 0)
        val identities = requireNotNull(prefs.getStringSet("identities", null))
        assertEquals(20, identities.size)
        val root = model.javaClass.getDeclaredField("root").apply { isAccessible = true }.get(model) as ThomasAndroidCompositionRoot
        val runtime = requireNotNull(root.runtime)
        val store = runtime.javaClass.getDeclaredField("store").apply { isAccessible = true }.get(runtime) as
            com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStore
        val sources = store.reader.snapshot().sources
        identities.forEach { identity ->
            val source = sources.single { it.provenance.metadata["therapy.turn-id"] == identity }
            assertEquals(prefs.getString(identity, null),
                (source.originalContent as com.conundrum.thomas.v2.longitudinal.OriginalSourceContent.Inline).exactContent)
        }
        println("R1 TWENTY_SUBMISSIONS_DURABLE_AFTER_PROCESS_REOPEN=true identities=" + identities.sorted())
    }
}
