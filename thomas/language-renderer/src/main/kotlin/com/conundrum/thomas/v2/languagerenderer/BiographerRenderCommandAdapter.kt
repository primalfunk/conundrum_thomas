package com.conundrum.thomas.v2.languagerenderer

import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerQuestionPlan
import com.conundrum.thomas.v2.biographer.BiographerQuestionSemanticAct

object BiographerRenderCommandAdapter {
    fun adapt(
        commandId: RenderCommandId,
        turnIndex: Int,
        plan: BiographerQuestionPlan,
        grounding: RenderableGrounding,
    ): GovernedRenderCommand {
        val posture = if (plan.posture == BiographerPosture.OPEN_STORY)
            GovernedResponsePosture.BIOGRAPHER_OPEN_STORY else GovernedResponsePosture.BIOGRAPHER_TARGETED_COVERAGE
        val act = if (plan.semanticAct == BiographerQuestionSemanticAct.OPEN_HISTORICAL_INVITATION)
            GovernedSemanticAct.OPEN_QUESTION else GovernedSemanticAct.CLARIFYING_QUESTION
        val forms = forms(plan.semanticAct, grounding.surfaceMeaning)
        val unit = AuthorizedSemanticUnit(
            grounding.id, SemanticUnitKind.AUTHORIZED_QUESTION_TARGET, grounding.surfaceMeaning,
            RenderEpistemicStatus.STRUCTURAL, RenderAttribution.GOVERNED_POLICY,
            SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING, grounding.temporalScope,
            grounding.uncertainty.name, setOf(SemanticUnitUse.QUESTION_GROUNDING),
            requiredMarkerGroups = grounding.requiredMarkerGroups,
        )
        return GovernedRenderCommand(
            commandId, turnIndex, GovernedRenderMode.BIOGRAPHER, act,
            responsePosture = posture, semanticUnits = listOf(unit),
            temporalConstraints = temporalConstraints(unit), budget = RenderBudget.ONE_QUESTION,
            style = RenderStyleContract(questionStyle = RenderQuestionStyle.NEUTRAL),
            directivenessLimit = RenderDirectness.GENTLE, advicePermitted = false,
            memoryReferencePermission = MemoryReferencePermission.NONE,
            allowedEntityNames = grounding.allowedEntityNames,
            allowedTemporalLiterals = grounding.allowedTemporalLiterals + temporalLiterals(grounding.temporalScope),
            prohibitedLiteralPhrases = setOf("traumatized", "made you insecure", "depression began"),
            authorizedReferenceRealizations = forms, deterministicFallbackText = forms.first(),
            fallbackAuthority = RenderFallbackAuthority.DETERMINISTIC_FALLBACK,
        )
    }

    private fun forms(act: BiographerQuestionSemanticAct, meaning: String): List<String> = when (act) {
        BiographerQuestionSemanticAct.OPEN_HISTORICAL_INVITATION -> listOf(
            "Where would you like to begin with your history?",
            "What part of your history would you like to start with?",
        )
        BiographerQuestionSemanticAct.CLARIFY_IDENTITY -> listOf(
            "Are $meaning the same person, different people, or are you not sure?",
        )
        BiographerQuestionSemanticAct.CLARIFY_CONTRADICTION -> listOf(
            "You have differing accounts of $meaning. Which is closer, or is it still uncertain?",
        )
        BiographerQuestionSemanticAct.CLARIFY_CORRECTION_TARGET -> listOf(
            "Which earlier account does $meaning correct, or is that still uncertain?",
        )
        BiographerQuestionSemanticAct.EXPLORE_USER_NAMED_TOPIC -> listOf(
            "What would you like to share about $meaning?",
        )
        BiographerQuestionSemanticAct.EXPLORE_STRUCTURAL_GAP -> listOf(
            "What do you remember about $meaning?",
            "What was happening during $meaning?",
        )
    }
}
