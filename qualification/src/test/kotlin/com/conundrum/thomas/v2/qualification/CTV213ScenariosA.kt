package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.BiographerQuestionSemanticAct
import com.conundrum.thomas.v2.biographer.InvestigationTargetKind
import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.languagerenderer.BiographerRenderCommandAdapter
import com.conundrum.thomas.v2.languagerenderer.CandidateRealizationOutcome
import com.conundrum.thomas.v2.languagerenderer.GovernedSemanticAct
import com.conundrum.thomas.v2.languagerenderer.JournalRenderCommandAdapter
import com.conundrum.thomas.v2.languagerenderer.RenderDisposition
import com.conundrum.thomas.v2.languagerenderer.RenderValidationReason
import com.conundrum.thomas.v2.languagerenderer.RenderableGrounding
import com.conundrum.thomas.v2.languagerenderer.TherapyRenderCommandAdapter
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemorySemanticAct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

internal object CTV213ScenariosA {
    private val S = CTV213TestSupport

    fun run(id: Int) = when (id) {
        1, 6 -> journalSilence()
        2 -> journalReflect()
        3 -> journalInvalidQuestion()
        4 -> journalQuestion()
        5 -> journalManyQuestions()
        7 -> journalTechniqueRejected()
        8 -> journalHistoryRejected()
        9 -> biographerOpenStory()
        10 -> biographerTemporalGap()
        11 -> biographerIdentity()
        12 -> biographerContradiction()
        13 -> biographerTraumaRejected()
        14 -> biographerManyQuestions()
        15 -> biographerTargetChange()
        16 -> biographerFailureFallback()
        17 -> therapyReflection()
        18 -> therapyQuestion()
        19 -> therapyAction()
        20 -> therapyAdviceRejected()
        21 -> therapyRoutePreserved()
        22 -> therapyDiagnosisRejected()
        23 -> rendererWithoutMemory()
        in 24..26 -> memoryAttribution(id)
        27 -> tentativeConnection()
        28 -> certaintyRejected()
        29 -> unselectedMemoryRejected()
        30 -> privateMemoryRejected()
        31 -> oneMemoryVisibility()
        else -> error("Scenario A does not own $id")
    }

    private fun journalGrounding(epistemic: EvidenceEpistemicClass = EvidenceEpistemicClass.EXPLICIT_SELF_REPORT) =
        RenderableGrounding("journal.grounding", "the interruptions left you exhausted",
            listOf(setOf("interruptions")), epistemic)

    private fun journalCommand(preference: JournalResponsePreference) = JournalRenderCommandAdapter.adapt(
        S.id("journal.${preference.name.lowercase().replace('_', '-')}"), 1, preference,
        if (preference == JournalResponsePreference.NO_RESPONSE) null else S.journalPlan(preference),
        if (preference == JournalResponsePreference.NO_RESPONSE) null else journalGrounding(),
    )

    private fun journalSilence() {
        val command = journalCommand(JournalResponsePreference.NO_RESPONSE)
        val realizer = S.scripted(CandidateRealizationOutcome.Unavailable("must-not-run"))
        val result = S.renderer.render(command, externalRealizer = realizer)
        assertEquals(RenderDisposition.NO_RESPONSE, result.disposition)
        assertEquals(0, realizer.calls)
        assertEquals(null, result.finalText)
    }

    private fun journalReflect() {
        val result = S.renderer.render(journalCommand(JournalResponsePreference.REFLECT))
        assertEquals(RenderDisposition.ACCEPTED_REFERENCE_REALIZATION, result.disposition)
        assertEquals(0, result.questionCount)
        assertTrue(result.finalText!!.contains("interruptions"))
    }

    private fun journalInvalidQuestion() {
        val command = journalCommand(JournalResponsePreference.REFLECT)
        val result = S.renderExternal(command, "The interruptions were exhausting. What happened next?")
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(result.rejectedCandidateReasons.flatten().contains(RenderValidationReason.QUESTION_LIMIT))
    }

    private fun journalQuestion() {
        val result = S.renderer.render(journalCommand(JournalResponsePreference.ASK_ONE_QUESTION))
        assertTrue(result.questionCount <= 1)
        assertEquals(GovernedSemanticAct.CLARIFYING_QUESTION, result.semanticAct)
    }

    private fun journalManyQuestions() {
        val command = journalCommand(JournalResponsePreference.ASK_ONE_QUESTION)
        val result = S.renderExternal(command, "What happened? How did it feel? What now?")
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(result.rejectedCandidateReasons.flatten().contains(RenderValidationReason.QUESTION_LIMIT))
    }

    private fun journalTechniqueRejected() {
        val command = journalCommand(JournalResponsePreference.REFLECT)
        val manifest = S.manifest(command).copy(routeOrTechniqueChange = true, addedAdvice = true)
        val result = S.renderExternal(command, "The interruptions mattered, so try this exercise.", manifest)
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(result.rejectedCandidateReasons.flatten().contains(RenderValidationReason.ROUTE_OR_TECHNIQUE_CHANGE))
    }

    private fun journalHistoryRejected() {
        val command = journalCommand(JournalResponsePreference.REFLECT)
        val manifest = S.manifest(command).copy(referencedMemoryIds = setOf("private.memory"))
        val result = S.renderExternal(command, "The interruptions match your childhood history.", manifest)
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(result.rejectedCandidateReasons.flatten().contains(RenderValidationReason.UNAUTHORIZED_MEMORY))
    }

    private fun biographerCommand(
        act: BiographerQuestionSemanticAct,
        kind: InvestigationTargetKind,
        meaning: String,
        marker: String,
    ) = BiographerRenderCommandAdapter.adapt(
        S.id("biographer.${act.name.lowercase().replace('_', '-')}"), 1, S.biographerPlan(act, kind),
        RenderableGrounding("biographer.target", meaning, listOf(setOf(marker))),
    )

    private fun biographerOpenStory() {
        val command = biographerCommand(BiographerQuestionSemanticAct.OPEN_HISTORICAL_INVITATION,
            InvestigationTargetKind.OPEN_STORY, "your history", "history")
        val result = S.renderer.render(command)
        assertEquals(1, result.questionCount)
        assertTrue(result.finalText!!.contains("history"))
        assertFalse(result.finalText!!.contains("trauma"))
    }

    private fun biographerTemporalGap() {
        val command = biographerCommand(BiographerQuestionSemanticAct.EXPLORE_STRUCTURAL_GAP,
            InvestigationTargetKind.TEMPORAL_GAP, "the period between Seattle and Portland", "period")
        val result = S.renderer.render(command)
        assertEquals(1, result.questionCount)
        assertTrue(result.finalText!!.contains("period"))
    }

    private fun biographerIdentity() {
        val command = biographerCommand(BiographerQuestionSemanticAct.CLARIFY_IDENTITY,
            InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED, "the two Sam references", "Sam")
        assertTrue(S.renderer.render(command).finalText!!.contains("not sure"))
    }

    private fun biographerContradiction() {
        val command = biographerCommand(BiographerQuestionSemanticAct.CLARIFY_CONTRADICTION,
            InvestigationTargetKind.CONTRADICTION_CLARIFICATION, "the reported years", "differing")
        val text = S.renderer.render(command).finalText!!
        assertTrue(text.contains("differing") && text.contains("uncertain"))
    }

    private fun biographerTraumaRejected() = adversarialBiographer(
        "Why did that period traumatize you?", RenderValidationReason.PROHIBITED_LITERAL,
    )

    private fun biographerManyQuestions() = adversarialBiographer(
        "What happened? How did you feel?", RenderValidationReason.QUESTION_LIMIT,
    )

    private fun biographerTargetChange() {
        val command = biographerCommand(BiographerQuestionSemanticAct.EXPLORE_STRUCTURAL_GAP,
            InvestigationTargetKind.TEMPORAL_GAP, "the period between Seattle and Portland", "period")
        val result = S.renderExternal(command, "What happened during childhood?")
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(result.rejectedCandidateReasons.flatten().contains(RenderValidationReason.MISSING_SEMANTIC_UNIT))
    }

    private fun adversarialBiographer(text: String, reason: RenderValidationReason) {
        val command = biographerCommand(BiographerQuestionSemanticAct.EXPLORE_STRUCTURAL_GAP,
            InvestigationTargetKind.TEMPORAL_GAP, "that period", "period")
        val result = S.renderExternal(command, text)
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(reason in result.rejectedCandidateReasons.flatten())
    }

    private fun biographerFailureFallback() {
        val command = biographerCommand(BiographerQuestionSemanticAct.EXPLORE_STRUCTURAL_GAP,
            InvestigationTargetKind.TEMPORAL_GAP, "that period", "period")
        val result = S.renderer.render(command, externalRealizer = S.scripted(
            CandidateRealizationOutcome.Unavailable("synthetic")))
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertEquals(1, result.questionCount)
    }

    private fun therapyReflection() {
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.reflection"), 1, S.therapyEnvelope())
        val result = S.renderer.render(command)
        assertEquals(GovernedSemanticAct.BRIEF_REFLECTION, result.semanticAct)
        assertEquals(0, result.questionCount)
    }

    private fun therapyQuestion() {
        val domain = S.domainCommand("core-ask-present-concern", RenderForm.INTERROGATIVE, 1)
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.question"), 1,
            S.therapyEnvelope(command = domain, support = emptyList()))
        assertEquals(1, S.renderer.render(command).questionCount)
    }

    private fun therapyAction() {
        val domain = S.domainCommand("core-develop-bounded-plan", RenderForm.PLANNING, 1, advice = true)
        val envelope = S.therapyEnvelope(command = domain,
            support = listOf(AuthorizedSupportingText("user-selected-option", "send a short email")))
        val result = S.renderer.render(TherapyRenderCommandAdapter.adapt(S.id("therapy.action"), 1, envelope))
        assertEquals(GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION, result.semanticAct)
        assertTrue(result.finalText!!.contains("send a short email"))
    }

    private fun therapyAdviceRejected() = therapyExternalViolation(
        "You should quit your job because work felt exhausting.",
        S.manifest(therapyReflectionCommand()).copy(addedAdvice = true), RenderValidationReason.UNAUTHORIZED_ADVICE,
    )

    private fun therapyRoutePreserved() {
        val command = therapyReflectionCommand()
        val bad = S.manifest(command).copy(declaredSemanticAct = GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION)
        val result = S.renderExternal(command, "You should make a plan about work felt exhausting.", bad)
        assertEquals(GovernedSemanticAct.BRIEF_REFLECTION, result.semanticAct)
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
    }

    private fun therapyDiagnosisRejected() = therapyExternalViolation(
        "I diagnose you with a disorder because work felt exhausting.",
        S.manifest(therapyReflectionCommand()).copy(diagnosisClaim = true), RenderValidationReason.DIAGNOSIS,
    )

    private fun therapyReflectionCommand() =
        TherapyRenderCommandAdapter.adapt(S.id("therapy.external"), 1, S.therapyEnvelope())

    private fun therapyExternalViolation(text: String, manifest: com.conundrum.thomas.v2.languagerenderer.CandidateManifest,
                                         reason: RenderValidationReason) {
        val result = S.renderExternal(therapyReflectionCommand(), text, manifest)
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(reason in result.rejectedCandidateReasons.flatten())
    }

    private fun rendererWithoutMemory() {
        val command = therapyReflectionCommand()
        assertTrue(command.historicalSupport.isEmpty())
        assertTrue(S.renderer.render(command).surfacedMemoryIds.isEmpty())
    }

    private fun memoryAttribution(case: Int) {
        val mode = when (case) {
            24 -> AcquisitionMode.JOURNAL
            25 -> AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE
            else -> AcquisitionMode.THERAPIST_CONVERSATION
        }
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.memory-$case"), 1,
            S.therapyEnvelope(listOf(S.memory(mode))))
        val text = S.renderer.render(command).finalText!!
        when (case) {
            24 -> assertTrue(text.contains("Journal"))
            25 -> assertTrue(text.contains("history"))
            26 -> assertTrue(text.contains("mentioned before"))
        }
    }

    private fun tentativeConnection() {
        val memory = S.memory(semanticAct = TherapyMemorySemanticAct.TENTATIVE_CONNECTION)
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.tentative"), 1, S.therapyEnvelope(listOf(memory)))
        val text = S.renderer.render(command).finalText!!
        assertTrue(text.contains("not sure"))
    }

    private fun certaintyRejected() {
        val memory = S.memory(semanticAct = TherapyMemorySemanticAct.TENTATIVE_CONNECTION)
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.certainty"), 1, S.therapyEnvelope(listOf(memory)))
        val result = S.renderExternal(command, "You wrote in your Journal that this is definitely the same pattern.",
            S.manifest(command).copy(certaintyInflated = true))
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(RenderValidationReason.CERTAINTY_INFLATION in result.rejectedCandidateReasons.flatten())
    }

    private fun unselectedMemoryRejected() {
        val command = therapyReflectionCommand()
        val result = S.renderExternal(command, "An unselected memory explains work felt exhausting.",
            S.manifest(command).copy(referencedMemoryIds = setOf("memory.unselected")))
        assertTrue(RenderValidationReason.UNAUTHORIZED_MEMORY in result.rejectedCandidateReasons.flatten())
    }

    private fun privateMemoryRejected() {
        val command = therapyReflectionCommand()
        val result = S.renderExternal(command, "A private memory explains work felt exhausting.",
            S.manifest(command).copy(referencedMemoryIds = setOf("memory.private")))
        assertTrue(RenderValidationReason.UNAUTHORIZED_MEMORY in result.rejectedCandidateReasons.flatten())
    }

    private fun oneMemoryVisibility() {
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.one-memory"), 1,
            S.therapyEnvelope(listOf(S.memory())))
        val result = S.renderer.render(command)
        assertEquals(1, command.historicalSupport.size)
        assertEquals(1, result.surfacedMemoryIds.size)
    }
}
