package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.languagerenderer.CandidateRealization
import com.conundrum.thomas.v2.languagerenderer.CandidateRealizationOutcome
import com.conundrum.thomas.v2.languagerenderer.GovernedLanguageRenderer
import com.conundrum.thomas.v2.languagerenderer.LanguageRealizer
import com.conundrum.thomas.v2.languagerenderer.RenderDisposition
import com.conundrum.thomas.v2.languagerenderer.RenderValidationReason
import com.conundrum.thomas.v2.languagerenderer.TherapyRenderCommandAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalModelEchoGuardTest {
    @Test fun `external shallow current-text echo is rejected and deterministic fallback remains available`() {
        val command = TherapyRenderCommandAdapter.adapt(
            CTV213TestSupport.id("local-model-shallow-echo"), 1, CTV213TestSupport.therapyEnvelope(),
        )
        val candidate = CandidateRealization(
            "The work felt exhausting today.", "ct-v2-thomas-llama", "device-fixture",
            CTV213TestSupport.manifest(command),
        )
        val result = GovernedLanguageRenderer().render(command, externalRealizer = LanguageRealizer { _, _ ->
            CandidateRealizationOutcome.Candidate(candidate)
        })

        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(RenderValidationReason.SHALLOW_CURRENT_TEXT_ECHO in result.rejectedCandidateReasons.flatten())
        assertTrue(result.finalText!!.startsWith("I hear that"))
    }

    @Test fun `control language and prefixed current-text echo are rejected`() {
        val command = TherapyRenderCommandAdapter.adapt(
            CTV213TestSupport.id("local-model-control-leak"), 1, CTV213TestSupport.therapyEnvelope(),
        )
        val candidate = CandidateRealization(
            "The authorized act is: I hear that The work felt exhausting today.",
            "ct-v2-thomas-llama", "device-fixture", CTV213TestSupport.manifest(command),
        )
        val result = GovernedLanguageRenderer().render(command, externalRealizer = LanguageRealizer { _, _ ->
            CandidateRealizationOutcome.Candidate(candidate)
        })

        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(RenderValidationReason.CONTROL_LANGUAGE_LEAKAGE in result.rejectedCandidateReasons.flatten())
        assertTrue(RenderValidationReason.SHALLOW_CURRENT_TEXT_ECHO in result.rejectedCandidateReasons.flatten())
    }
}
