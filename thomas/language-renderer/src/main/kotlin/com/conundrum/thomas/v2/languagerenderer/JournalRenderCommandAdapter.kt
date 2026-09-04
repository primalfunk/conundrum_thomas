package com.conundrum.thomas.v2.languagerenderer

import com.conundrum.thomas.v2.journal.JournalResponsePlan
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.journal.JournalResponseSemanticAct
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass

object JournalRenderCommandAdapter {
    fun adapt(
        commandId: RenderCommandId,
        turnIndex: Int,
        preference: JournalResponsePreference,
        plan: JournalResponsePlan? = null,
        grounding: RenderableGrounding? = null,
    ): GovernedRenderCommand {
        if (preference == JournalResponsePreference.NO_RESPONSE) {
            require(plan == null && grounding == null)
            return silenceCommand(commandId, turnIndex, GovernedRenderMode.JOURNAL)
        }
        requireNotNull(plan)
        requireNotNull(grounding)
        require(plan.posture == preference)
        val epistemic = epistemicRendering(grounding.epistemicClass)
        val statement = journalStatement(grounding)
        val act: GovernedSemanticAct
        val posture: GovernedResponsePosture
        val budget: RenderBudget
        val forms: List<String>
        when (plan.semanticAct) {
            JournalResponseSemanticAct.NONE -> error("NONE is not renderable")
            JournalResponseSemanticAct.BRIEF_REFLECTION -> {
                act = GovernedSemanticAct.BRIEF_REFLECTION
                posture = GovernedResponsePosture.JOURNAL_REFLECT
                budget = RenderBudget.BRIEF_REFLECTION
                forms = listOf(
                    statement,
                    "What you wrote conveys ${grounding.surfaceMeaning}.",
                    "In this entry, you described ${grounding.surfaceMeaning}.",
                )
            }
            JournalResponseSemanticAct.ONE_GROUNDED_QUESTION -> {
                act = GovernedSemanticAct.CLARIFYING_QUESTION
                posture = GovernedResponsePosture.JOURNAL_ASK_ONE_QUESTION
                budget = RenderBudget.ONE_QUESTION
                forms = listOf(
                    "You described ${grounding.surfaceMeaning}. What part stands out most to you?",
                    "What would you like to explore about ${grounding.surfaceMeaning}?",
                )
            }
        }
        val unit = renderUnit(
            grounding,
            if (act == GovernedSemanticAct.BRIEF_REFLECTION)
                SemanticUnitKind.AUTHORIZED_REFLECTION_TARGET else SemanticUnitKind.AUTHORIZED_QUESTION_TARGET,
            if (act == GovernedSemanticAct.BRIEF_REFLECTION)
                SemanticUnitUse.REFLECTION else SemanticUnitUse.QUESTION_GROUNDING,
        )
        return GovernedRenderCommand(
            commandId, turnIndex, GovernedRenderMode.JOURNAL, act,
            responsePosture = posture, semanticUnits = listOf(unit),
            epistemicConstraints = listOf(RenderEpistemicConstraint(
                unit.id,
                if (grounding.uncertainty == AssertionUncertainty.STATED_WITHOUT_QUALIFICATION)
                    epistemic.markers else uncertaintyMarkers(grounding.uncertainty),
                grounding.uncertainty != AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
            )),
            temporalConstraints = temporalConstraints(unit), budget = budget,
            style = RenderStyleContract(questionStyle = if (budget.maximumQuestions == 0)
                RenderQuestionStyle.NONE else RenderQuestionStyle.OPTIONAL),
            directivenessLimit = RenderDirectness.GENTLE, advicePermitted = false,
            memoryReferencePermission = MemoryReferencePermission.NONE,
            allowedEntityNames = grounding.allowedEntityNames,
            allowedTemporalLiterals = grounding.allowedTemporalLiterals + temporalLiterals(grounding.temporalScope),
            prohibitedLiteralPhrases = setOf(
                "you have difficulty enforcing boundaries", "persistent negative core belief",
            ),
            authorizedReferenceRealizations = forms, deterministicFallbackText = forms.first(),
            fallbackAuthority = RenderFallbackAuthority.DETERMINISTIC_FALLBACK,
        )
    }

    private fun journalStatement(grounding: RenderableGrounding): String = when (grounding.epistemicClass) {
        EvidenceEpistemicClass.SELF_BELIEF -> "You described believing ${grounding.surfaceMeaning}."
        EvidenceEpistemicClass.USER_INTERPRETATION -> "You described your sense that ${grounding.surfaceMeaning}."
        EvidenceEpistemicClass.THIRD_PARTY_REPORT -> "You reported that ${grounding.surfaceMeaning}."
        else -> "You described ${grounding.surfaceMeaning}."
    }
}
