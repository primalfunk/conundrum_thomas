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

/** Canonical-package device regression for conditional Therapy safety gating. */
@RunWith(AndroidJUnit4::class)
class TherapyOrdinaryInteractionInstrumentedTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun ordinaryTherapyTurnRendersWithoutCurrentEmergencyInterrogation() {
        compose.onNodeWithTag("mode-therapy").performClick()
        compose.onNodeWithTag("turn-draft").performTextInput("Work has been frustrating lately.")
        compose.onNodeWithTag("commit-turn").performClick()
        compose.onNodeWithText("Work has been frustrating lately.", useUnmergedTree = true).assertExists()
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodes(hasTestTag("transcript-thomas")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("transcript-thomas").assertExists()
        compose.onNodeWithText(
            "Is an emergency happening right now that means ordinary problem-solving must stop?",
            useUnmergedTree = true,
        ).assertDoesNotExist()
    }
}
