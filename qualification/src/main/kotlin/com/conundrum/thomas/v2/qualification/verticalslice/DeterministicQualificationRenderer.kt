package com.conundrum.thomas.v2.qualification.verticalslice

import com.conundrum.thomas.v2.domain.rendering.ConversationalRenderer
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.domain.rendering.RenderedDraft

data class QualificationRenderResult(
    val selectedPolicyActionId: String,
    val preservedDialogueActId: String,
    val draft: RenderedDraft,
)

/**
 * A deliberately plain, non-LLM test renderer. It switches only on the already-selected action ID.
 * The class lives in the qualification module and is not reachable from the application runtime.
 */
class DeterministicQualificationRenderer : ConversationalRenderer {
    override suspend fun render(request: RenderRequest): RenderedDraft = renderNow(request).draft

    fun renderNow(request: RenderRequest): QualificationRenderResult {
        val command = request.command
        val text = if (command.outputDisposition == RenderOutputDisposition.NO_RESPONSE) {
            ""
        } else {
            when (command.selectedPolicyActionId) {
                "ask-support-preference" ->
                    "Would you like me to listen, help you understand it, or work through a practical next step?"
                "ask-problem-description" ->
                    "What is the one present problem you would like us to focus on?"
                "verify-problem-understanding" ->
                    "I understand the problem as: ${requiredText(request, "problem-statement")}. Is that right?"
                "reflect-for-listening" ->
                    "I’m hearing that ${requiredText(request, "problem-statement")}"
                "summarize-for-understanding" ->
                    "What we have established is: ${requiredText(request, "problem-statement")}"
                "ask-influenceable-part" ->
                    "Which part of this situation do you think you can influence or change?"
                "ask-readiness-for-options" ->
                    "Would you like to consider some possible ways you could influence that part?"
                "invite-user-options" ->
                    "What possible ways of influencing that part come to mind for you?"
                "ask-user-to-choose-option" ->
                    "Which of your options seems most helpful and feasible to try?"
                "develop-bounded-plan" ->
                    "What small first step would you take, and when would you take it?"
                "review-reported-outcome" ->
                    "What happened when you tried the plan?"
                else -> error("The deterministic qualification renderer has no realization for ${command.selectedPolicyActionId}")
            }
        }
        return QualificationRenderResult(
            selectedPolicyActionId = command.selectedPolicyActionId,
            preservedDialogueActId = command.selectedDialogueActId,
            draft = RenderedDraft(text),
        )
    }

    private fun requiredText(request: RenderRequest, reference: String): String =
        requireNotNull(request.authorizedSupportingText.singleOrNull { it.reference == reference }) {
            "Required authorized supporting text is missing: $reference"
        }.text
}
