package com.conundrum.thomas.v2.platform.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import java.util.Locale

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

    @Test fun `accepted product profiles resolve to their frozen offline Google voices`() {
        val catalog = listOf(
            candidate("en-us-x-iob-local"),
            candidate("en-us-x-iog-local"),
            candidate("en-us-x-iol-local"),
        )

        assertEquals("en-us-x-iob-local", selectThomasSystemVoice(ThomasVoiceProfile.MALE, catalog).candidate?.name)
        assertEquals("en-us-x-iog-local", selectThomasSystemVoice(ThomasVoiceProfile.FEMALE, catalog).candidate?.name)
        assertEquals("en-us-x-iol-local", selectThomasSystemVoice(ThomasVoiceProfile.NEUTRAL, catalog).candidate?.name)
        assertEquals(ThomasVoiceSelectionKind.PREFERRED, selectThomasSystemVoice(ThomasVoiceProfile.MALE, catalog).kind)
    }

    @Test fun `preferred mapping requires installed offline US English and uses its qualified fallback`() {
        val catalog = listOf(
            candidate("en-us-x-iob-local", network = true),
            candidate("en-us-x-iom-local"),
            candidate("en-us-x-iog-local", installed = false),
            candidate("en-us-x-sfg-local"),
            candidate("en-us-x-iol-local", locale = Locale.UK),
            candidate("en-us-x-tpc-local"),
        )

        ThomasVoiceProfile.entries.forEach { profile ->
            val selection = selectThomasSystemVoice(profile, catalog)
            assertEquals(ThomasVoiceSelectionKind.QUALIFIED_FALLBACK, selection.kind)
            assertEquals(ThomasSystemVoiceMappings.qualifiedFallbackFor(profile), selection.candidate?.name)
        }
    }

    @Test fun `safe offline English fallback remains functional and cloud only catalog is unavailable`() {
        val safe = candidate("en-gb-x-local", locale = Locale.UK)
        assertEquals(
            ThomasVoiceSelectionKind.SAFE_ENGLISH_FALLBACK,
            selectThomasSystemVoice(ThomasVoiceProfile.MALE, listOf(safe)).kind,
        )
        val unavailable = selectThomasSystemVoice(
            ThomasVoiceProfile.FEMALE,
            listOf(candidate("en-us-x-cloud", network = true)),
        )
        assertEquals(ThomasVoiceSelectionKind.UNAVAILABLE, unavailable.kind)
        assertNull(unavailable.candidate)
    }

    private fun candidate(
        name: String,
        locale: Locale = Locale.US,
        network: Boolean = false,
        installed: Boolean = true,
    ) = ThomasSystemVoiceCandidate(name, locale, network, installed)
}
