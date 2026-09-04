package com.conundrum.thomas.v2.languagerenderer

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition

object SafetyRenderCommandAdapter {
    fun adapt(
        commandId: RenderCommandId,
        turnIndex: Int,
        command: RenderCommand,
        supportingText: List<AuthorizedSupportingText>,
    ): GovernedRenderCommand {
        require(command.outputDisposition == RenderOutputDisposition.GENERATE_TEXT)
        require(!command.ordinaryTherapeuticContentPermitted)
        val requirement = supportingText.single { it.reference == "required-safety-information" }.text
        val fixed = fixedSafetyText(requirement)
        val unit = AuthorizedSemanticUnit(
            "safety-requirement", SemanticUnitKind.SAFETY_REQUIREMENT, requirement,
            RenderEpistemicStatus.STRUCTURAL, RenderAttribution.GOVERNED_POLICY,
            SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING,
            allowedUses = setOf(SemanticUnitUse.SAFETY_REALIZATION),
        )
        return GovernedRenderCommand(
            commandId, turnIndex, GovernedRenderMode.SAFETY, GovernedSemanticAct.SAFETY_MESSAGE,
            responsePosture = GovernedResponsePosture.SAFETY, semanticUnits = listOf(unit),
            budget = RenderBudget.SAFETY,
            style = RenderStyleContract(
                conversationality = RenderConversationality.DIRECT,
                directness = RenderDirectness.DIRECT,
                questionStyle = RenderQuestionStyle.DIRECT_REQUIRED,
                acknowledgementStrength = RenderAcknowledgementStrength.NONE,
            ),
            directivenessLimit = RenderDirectness.DIRECT, advicePermitted = command.advicePermitted,
            memoryReferencePermission = MemoryReferencePermission.NONE,
            authorizedReferenceRealizations = listOf(fixed), deterministicFallbackText = fixed,
            fixedSafetyText = fixed, maximumExternalAttempts = 2,
            fallbackAuthority = RenderFallbackAuthority.DETERMINISTIC_FALLBACK,
        )
    }

    private fun fixedSafetyText(requirement: String): String = when (requirement) {
        "CURRENT_EMERGENCY_STATUS" ->
            "Is an emergency happening right now that means ordinary problem-solving must stop?"
        "ACUTE_MEDICAL_EMERGENCY_STATUS" ->
            "Is there an acute medical emergency happening right now?"
        "SELF_HARM_RELEVANCE" ->
            "Does the present situation include a self-harm concern that needs a different support path?"
        "HARM_TO_OTHERS_RELEVANCE" ->
            "Does the present situation include concern about harm to another person?"
        "SPECIALIZED_SCOPE_STATUS" ->
            "Does this situation require specialized support outside ordinary problem-solving?"
        "POPULATION_APPLICABILITY" ->
            "Is this an adult situation within the currently supported qualification scope?"
        "PRESENTING_SCOPE" ->
            "Is the presenting concern a bounded ordinary personal problem?"
        else -> error("Unsupported governed safety requirement")
    }
}
