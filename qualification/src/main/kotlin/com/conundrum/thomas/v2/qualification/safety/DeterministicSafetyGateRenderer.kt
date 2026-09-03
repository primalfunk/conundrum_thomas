package com.conundrum.thomas.v2.qualification.safety

import com.conundrum.thomas.v2.domain.rendering.ConversationalRenderer
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.domain.rendering.RenderedDraft

data class SafetyQualificationRenderResult(
    val selectedActionId: String,
    val preservedDialogueActId: String,
    val draft: RenderedDraft,
)

/** Qualification-only language realization; it cannot select or change a gate disposition. */
class DeterministicSafetyGateRenderer : ConversationalRenderer {
    override suspend fun render(request: RenderRequest): RenderedDraft = renderNow(request).draft

    fun renderNow(request: RenderRequest): SafetyQualificationRenderResult {
        val command = request.command
        require(command.selectedPolicyActionId == "clarify-required-safety-fact")
        require(!command.ordinaryTherapeuticContentPermitted)
        val requirement = request.authorizedSupportingText.single {
            it.reference == "required-safety-information"
        }.text
        val text = when (requirement) {
            "CURRENT_EMERGENCY_STATUS" -> "Is an emergency happening right now that means ordinary problem-solving must stop?"
            "ACUTE_MEDICAL_EMERGENCY_STATUS" -> "Is there an acute medical emergency happening right now?"
            "SELF_HARM_RELEVANCE" -> "Does the present situation include a self-harm concern that needs a different support path?"
            "HARM_TO_OTHERS_RELEVANCE" -> "Does the present situation include concern about harm to another person?"
            "SPECIALIZED_SCOPE_STATUS" -> "Does this situation require specialized support outside ordinary problem-solving?"
            "POPULATION_APPLICABILITY" -> "Is this an adult situation within the currently supported qualification scope?"
            "PRESENTING_SCOPE" -> "Is the presenting concern a bounded ordinary personal problem?"
            else -> error("No deterministic qualification wording exists for $requirement")
        }
        return SafetyQualificationRenderResult(
            selectedActionId = command.selectedPolicyActionId,
            preservedDialogueActId = command.selectedDialogueActId,
            draft = RenderedDraft(text),
        )
    }
}
