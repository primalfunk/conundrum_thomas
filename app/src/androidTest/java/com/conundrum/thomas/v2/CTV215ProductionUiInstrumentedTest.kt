package com.conundrum.thomas.v2

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI remains a command/projection surface; true Journal silence is observable end to end. */
@RunWith(AndroidJUnit4::class)
class CTV215ProductionUiInstrumentedTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun modesAreExplicitAndJournalNoResponseCreatesNoThomasArtifact() {
        compose.onNodeWithTag("mode-journal").assertExists()
        compose.onNodeWithTag("mode-biographer").assertExists().performClick()
        compose.onNodeWithTag("mode-therapy").assertExists().performClick()
        compose.onNodeWithTag("mode-journal").performClick()

        compose.onNodeWithTag("settings").assertExists().performClick()
        compose.onNodeWithTag("journal-no_response").assertExists().performClick()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithTag("turn-draft").performTextInput("Synthetic UI qualification entry.")
        compose.onNodeWithTag("commit-turn").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(hasTestTag("transcript-user")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("transcript-user").assertExists()
        compose.onNodeWithTag("transcript-thomas").assertDoesNotExist()
        compose.onNodeWithTag("speech-unavailable").assertExists()
        compose.onNodeWithTag("settings").assertExists().performClick()
        compose.onNodeWithTag("data-custody").assertExists()
    }
}
