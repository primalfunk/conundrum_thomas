package com.conundrum.thomas.v2

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpeechInputUiInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun keyboardComposerRemainsAvailableWithSpeechAsPrimaryAffordance() {
        compose.onNodeWithTag("speech-start").assertExists().assertIsEnabled()
        compose.onNodeWithTag("turn-draft").assertExists().assertIsEnabled()
        compose.onNodeWithTag("commit-turn").assertExists().assertIsNotEnabled()

        compose.onNodeWithTag("turn-draft").performTextInput("Typed fallback turn")

        compose.onNodeWithTag("turn-draft").assertTextContains("Typed fallback turn")
        compose.onNodeWithTag("speech-start").assertExists().assertIsEnabled()
    }
}
