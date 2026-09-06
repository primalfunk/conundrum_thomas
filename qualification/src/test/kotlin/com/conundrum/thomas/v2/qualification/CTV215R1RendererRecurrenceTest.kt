package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.languagerenderer.*
import org.junit.Assert.*
import org.junit.Test

class CTV215R1RendererRecurrenceTest {
    private val s = CTV213TestSupport
    private fun history(texts: List<String>) = RenderHistoryState(texts.mapIndexed { index, text ->
        s.renderer.render(s.basicCommand("history.$index", turn = index + 1,
            forms = listOf(text))).nextHistory.entries.single()
    })
    private fun faithful(command: GovernedRenderCommand, result: GovernedRenderResult) {
        assertTrue(result.toString(), result.validation.accepted)
        assertEquals(listOf(RenderValidationReason.VALID), result.validation.reasonCodes)
        assertEquals(command.semanticAct, result.semanticAct)
        assertEquals(command.mode, result.mode)
        assertTrue(result.finalText in command.authorizedReferenceRealizations || result.finalText == command.deterministicFallbackText)
        assertTrue(result.questionCount <= command.budget.maximumQuestions)
        assertTrue(result.sentenceCount <= command.budget.maximumSentences)
        assertTrue(result.characterCount <= command.budget.maximumCharacters)
        assertEquals(command.historicalSupport.map { it.memoryObjectId }.sorted(), result.surfacedMemoryIds)
    }

    @Test fun exactDuplicateSearchesBeyondSelectedReferenceAndBaseFallback() {
        val command = s.basicCommand("recurrence.exhaustive", turn = 10)
        val preferred = s.renderer.render(command).finalText!!
        val alternate = command.authorizedReferenceRealizations.first { it != preferred && it != command.deterministicFallbackText }
        // Every opening is recent, but one complete authorized realization remains unused.
        val recent = history(command.authorizedReferenceRealizations.map { if (it == alternate) it + " Earlier." else it })
        val result = s.renderer.render(command, recent)
        faithful(command, result)
        assertEquals(alternate, result.finalText)
        assertTrue(RenderValidationReason.EXACT_RECENT_DUPLICATE in result.rejectedCandidateReasons.flatten())
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
    }

    @Test fun repeatedOpeningRejectsCandidateButAcceptsAuthorizedDifferentOpening() {
        val command = s.basicCommand("recurrence.opening", turn = 10)
        val recent = history(listOf("You described repeated interruptions yesterday.", "You described repeated interruptions today."))
        val result = s.renderExternal(command, command.authorizedReferenceRealizations.first(), history = recent)
        faithful(command, result)
        assertTrue(RenderValidationReason.REPEATED_OPENING in result.rejectedCandidateReasons.flatten())
        assertFalse(result.finalText!!.startsWith("You described repeated"))
    }

    @Test fun allAuthorizedFormsAndFallbackExhaustedIsExplicitFailureNotSilence() {
        val command = s.basicCommand("recurrence.exhausted", turn = 10)
        val recent = history(command.authorizedReferenceRealizations)
        val result = s.renderer.render(command, recent)
        assertEquals(RenderDisposition.RENDERING_UNAVAILABLE, result.disposition)
        assertEquals(command.semanticAct, result.semanticAct)
        assertFalse(result.validation.accepted)
        assertNull(result.finalText)
        assertEquals(recent, result.nextHistory)
        assertTrue(RenderValidationReason.EXACT_RECENT_DUPLICATE in result.rejectedCandidateReasons.flatten())
    }

    @Test fun oneUnchangedSurfaceAndDuplicateFallbackRemainProhibited() {
        val command = s.basicCommand("recurrence.accidental", turn = 10,
            forms = listOf("You described repeated interruptions at work."))
        val result = s.renderer.render(command, history(command.authorizedReferenceRealizations))
        assertEquals(RenderDisposition.RENDERING_UNAVAILABLE, result.disposition)
        assertFalse(result.validation.accepted)
        assertNull(result.finalText)
    }

    @Test fun distinctFallbackIsIndependentlyValidatedAfterReferenceExhaustion() {
        val command = s.basicCommand("recurrence.distinct-fallback", turn = 10,
            forms = listOf("You described repeated interruptions at work."),
            fallback = "Repeated interruptions at work were central in what you described.")
        val result = s.renderer.render(command, history(command.authorizedReferenceRealizations))
        faithful(command, result)
        assertEquals(command.deterministicFallbackText, result.finalText)
        assertTrue(result.fallbackUsed)
        assertEquals(AcceptedRealizationSource.DETERMINISTIC_FALLBACK, result.realizationSource)
    }

    @Test fun distinctButUnauthorizedMeaningCannotRescueExhaustedCommand() {
        val command = s.basicCommand("recurrence.invalid-fallback", turn = 10,
            forms = listOf("You described repeated interruptions at work."),
            fallback = "You should quit your job because of the interruptions.")
        val result = s.renderer.render(command, history(command.authorizedReferenceRealizations))
        assertEquals(RenderDisposition.RENDERING_UNAVAILABLE, result.disposition)
        assertTrue(RenderValidationReason.UNAUTHORIZED_ADVICE in result.rejectedCandidateReasons.flatten())
        assertFalse(result.validation.accepted)
    }

    @Test fun invalidReferenceAlternativesStillPassThroughFullValidator() {
        val command = s.basicCommand("recurrence.invalid-alternative", turn = 10,
            forms = listOf("You described repeated interruptions at work.", "You have depression because of the interruptions."))
        val result = s.renderer.render(command, history(listOf(command.authorizedReferenceRealizations.first())))
        assertEquals(RenderDisposition.RENDERING_UNAVAILABLE, result.disposition)
        assertTrue(RenderValidationReason.DIAGNOSIS in result.rejectedCandidateReasons.flatten())
    }

    @Test fun legitimateFixedSafetyPhraseStillRepeatsExactly() {
        val command = SafetyRenderCommandAdapter.adapt(s.id("recurrence.safety"), 1,
            s.domainCommand("core-reflect-established-content", RenderForm.INTERROGATIVE, 1, ordinary = false),
            listOf(AuthorizedSupportingText("required-safety-information", "CURRENT_EMERGENCY_STATUS")))
        val first = s.renderer.render(command)
        val second = s.renderer.render(command.copy(turnIndex = 2), first.nextHistory)
        faithful(command, second)
        assertEquals(command.fixedSafetyText, second.finalText)
        assertEquals(first.finalText, second.finalText)
    }

    @Test fun journalNoResponseDoesNotInvokeRealizerOrAlterHistory() {
        val recent = history(s.basicCommand().authorizedReferenceRealizations)
        val command = JournalRenderCommandAdapter.adapt(s.id("recurrence.journal-silent"), 10,
            JournalResponsePreference.NO_RESPONSE, null, null)
        val result = s.renderer.render(command, recent, LanguageRealizer { _, _ -> error("Silence must not realize") })
        assertEquals(RenderDisposition.NO_RESPONSE, result.disposition)
        assertTrue(result.validation.accepted)
        assertNull(result.finalText)
        assertEquals(recent, result.nextHistory)
        assertEquals(0, result.candidateAttemptCount)
    }
}
