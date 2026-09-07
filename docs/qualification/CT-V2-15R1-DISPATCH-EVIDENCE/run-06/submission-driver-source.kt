package com.conundrum.thomas.v2

import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.rules.ActivityScenarioRule
import java.util.Collections
import java.util.IdentityHashMap

/** Coordinates real UI taps with the OS IME animation; never calls the ViewModel directly. */
internal class UiSubmissionDriver(
    private val compose: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
) {
    private var activity: MainActivity? = null
    private val animations = Collections.synchronizedSet(
        Collections.newSetFromMap(IdentityHashMap<WindowInsetsAnimation, Boolean>()),
    )

    fun prepareForInput() {
        compose.activityRule.scenario.onActivity { current ->
            if (activity !== current) {
                activity = current
                animations.clear()
                current.window.decorView.setWindowInsetsAnimationCallback(
                    object : WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                        override fun onPrepare(animation: WindowInsetsAnimation) {
                            if (animation.typeMask and WindowInsets.Type.ime() != 0) animations.add(animation)
                        }
                        override fun onEnd(animation: WindowInsetsAnimation) { animations.remove(animation) }
                        override fun onProgress(insets: WindowInsets, running: MutableList<WindowInsetsAnimation>): WindowInsets = insets
                    },
                )
            }
        }
    }

    fun awaitKeyboard(visible: Boolean) {
        compose.waitUntil(5_000) {
            var ready = false
            compose.runOnUiThread {
                ready = activity?.window?.decorView?.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == visible &&
                    animations.isEmpty()
            }
            ready
        }
        compose.waitForIdle()
    }

    fun clickCommit() {
        prepareForInput()
        Espresso.closeSoftKeyboard()
        awaitKeyboard(false)
        // Resolve geometry only after the OS transition and its Compose layout have completed.
        compose.onNodeWithTag("commit-turn").assertIsEnabled().performClick()
    }
}
