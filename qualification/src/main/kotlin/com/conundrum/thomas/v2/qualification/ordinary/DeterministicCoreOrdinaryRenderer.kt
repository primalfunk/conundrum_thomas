package com.conundrum.thomas.v2.qualification.ordinary

import com.conundrum.thomas.v2.domain.rendering.ConversationalRenderer
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.domain.rendering.RenderedDraft

data class CoreQualificationRenderResult(
    val selectedActionId: String,
    val preservedDialogueActId: String,
    val preservedGoalId: String,
    val draft: RenderedDraft,
)

/** Qualification-only realization. It cannot choose a route, goal, or therapeutic act. */
class DeterministicCoreOrdinaryRenderer : ConversationalRenderer {
    override suspend fun render(request: RenderRequest): RenderedDraft = renderNow(request).draft

    fun renderNow(request: RenderRequest): CoreQualificationRenderResult {
        val command = request.command
        val text = if (command.outputDisposition == RenderOutputDisposition.NO_RESPONSE) {
            ""
        } else when (command.selectedPolicyActionId) {
            "core-ask-support-preference" ->
                "Would you like me to listen, help you understand this, or help structure a practical next step?"
            "core-offer-direction-choice" ->
                "Repeating that would add nothing new. Would you rather continue, change direction, pause, or stop?"
            "core-acknowledge-close" -> "Okay. We can stop here."
            "core-invite-expression" -> "What would you like me to hear?"
            "core-reflect-established-content" ->
                "I hear that ${text(request, "established-concern")}"
            "core-invite-further-expression" -> "What else would you like to say about it?"
            "core-summarize-listening" ->
                "What I have heard is: ${text(request, "established-concern")}"
            "core-check-further-or-close" -> "Would you like to add anything, or stop here?"
            "core-ask-present-concern" -> "What part of this would you most like to understand?"
            "core-ask-important-missing-piece" ->
                "To understand this, could you clarify ${text(request, "important-missing-information")}?"
            "core-verify-tentative-understanding", "core-verify-problem-understanding" -> {
                require(command.interpretationMustRemainTentative)
                "I might be understanding this as ${text(request, "tentative-thomas-understanding")}. Is that right?"
            }
            "core-acknowledge-correction" ->
                "I had that wrong. What would be a better way to understand it?"
            "core-summarize-shared-understanding" ->
                "What we have established is: ${text(request, "confirmed-understanding")}"
            "core-check-understanding-next-direction" ->
                "Is that understanding enough for now, or would you like a different direction?"
            "core-ask-problem-description" ->
                "What is the one practical problem you would like to work on?"
            "core-ask-influenceable-part" ->
                "Which part of ${text(request, "established-concern")} can you influence?"
            "core-ask-readiness-for-options" ->
                "Would you like to consider possible options, or leave it here?"
            "core-invite-user-options" ->
                "What possible ways forward come to mind for you?"
            "core-ask-user-to-choose-option" ->
                "Which of your options seems most helpful and feasible: ${texts(request, "user-option-")}?"
            "core-develop-bounded-plan" ->
                "For ${text(request, "user-selected-option")}, what small first step would you take, and when?"
            "core-review-reported-outcome" ->
                "What happened when you tried ${text(request, "action-plan")}?"
            "core-consolidate-plan-learning" ->
                "You tried ${text(request, "action-plan")} and reported ${text(request, "plan-outcome")}. Would you like to stop or choose another direction?"
            else -> error("No deterministic core realization exists for ${command.selectedPolicyActionId}")
        }
        return CoreQualificationRenderResult(
            selectedActionId = command.selectedPolicyActionId,
            preservedDialogueActId = command.selectedDialogueActId,
            preservedGoalId = command.therapeuticGoalId,
            draft = RenderedDraft(text),
        )
    }

    private fun text(request: RenderRequest, reference: String): String =
        requireNotNull(request.authorizedSupportingText.singleOrNull { it.reference == reference }) {
            "Required authorized supporting text is missing: $reference"
        }.text

    private fun texts(request: RenderRequest, prefix: String): String = request.authorizedSupportingText
        .filter { it.reference.startsWith(prefix) }
        .sortedBy { it.reference }
        .joinToString("; ") { it.text }
        .also { require(it.isNotBlank()) { "Required authorized supporting text is missing: $prefix" } }
}
