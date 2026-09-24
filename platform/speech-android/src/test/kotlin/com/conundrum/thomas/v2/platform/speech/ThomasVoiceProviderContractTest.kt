package com.conundrum.thomas.v2.platform.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ThomasVoiceProviderContractTest {
    @Test fun `only nonblank presentation text forms a validated response`() {
        assertEquals("Visible final reply", ValidatedThomasResponse("Visible final reply").text)
        try {
            ValidatedThomasResponse("   ")
            fail("Blank content must never reach a voice provider")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test fun `accessible rates are bounded and conversational`() {
        assertEquals(0.84f, ThomasSpeechRate.SLOW.multiplier)
        assertEquals(1.0f, ThomasSpeechRate.NORMAL.multiplier)
        assertEquals(1.16f, ThomasSpeechRate.FAST.multiplier)
    }
}
