package com.conundrum.thomas.v2.domain.rendering

/**
 * A bounded language-realization instruction selected upstream by Thomas.
 *
 * This value conveys no authority to select, alter, or add therapeutic behavior.
 */
enum class RenderOutputDisposition {
    GENERATE_TEXT,
    NO_RESPONSE,
}

enum class RenderForm {
    INTERROGATIVE,
    REFLECTIVE,
    SUMMARY,
    PLANNING,
    REVIEW,
    SILENCE,
}

data class RenderCommand(
    val policyDecisionReference: String,
    val selectedPolicyActionId: String,
    val selectedDialogueActId: String,
    val therapeuticGoalId: String,
    val instruction: String,
    val requiredSemanticContent: List<String>,
    val allowedSemanticContent: List<String>,
    val prohibitedSemanticContent: List<String>,
    val toneConstraints: List<String>,
    val maximumWords: Int,
    val maximumQuestions: Int,
    val advicePermitted: Boolean,
    val form: RenderForm,
    val outputDisposition: RenderOutputDisposition = RenderOutputDisposition.GENERATE_TEXT,
    val directWordingRequired: Boolean = false,
    val ordinaryTherapeuticContentPermitted: Boolean = true,
    val externalHelpInformationRequired: Boolean = false,
    val conversationContinuationPermitted: Boolean = true,
    val interpretationMustRemainTentative: Boolean = false,
    val userAgencyMustBeExplicitlyPreserved: Boolean = false,
    val responseRequired: Boolean = outputDisposition == RenderOutputDisposition.GENERATE_TEXT,
) {
    init {
        require(policyDecisionReference.isNotBlank())
        require(selectedPolicyActionId.matches(Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")))
        require(selectedDialogueActId.matches(Regex("^[a-z][a-z0-9-]*(?:\\.[a-z][a-z0-9-]*)+$")))
        require(therapeuticGoalId.matches(Regex("^[a-z][a-z0-9-]*(?:\\.[a-z][a-z0-9-]*)+$")))
        require(instruction.isNotBlank()) { "A render instruction must not be blank." }
        require(requiredSemanticContent.none(String::isBlank))
        require(allowedSemanticContent.none(String::isBlank))
        require(prohibitedSemanticContent.none(String::isBlank))
        require(toneConstraints.none(String::isBlank))
        require(maximumWords >= 0 && maximumQuestions >= 0)
        if (outputDisposition == RenderOutputDisposition.NO_RESPONSE) {
            require(maximumWords == 0 && maximumQuestions == 0 && form == RenderForm.SILENCE)
            require(!responseRequired)
        } else {
            require(maximumWords > 0 && form != RenderForm.SILENCE)
            require(responseRequired)
        }
    }
}

/** Text or evidence deliberately disclosed to a renderer for one render request. */
data class AuthorizedSupportingText(
    val reference: String,
    val text: String,
) {
    init {
        require(reference.isNotBlank())
        require(text.isNotBlank())
    }
}

/** The complete and exclusive input boundary of a conversational renderer. */
data class RenderRequest(
    val command: RenderCommand,
    val authorizedSupportingText: List<AuthorizedSupportingText> = emptyList(),
)

/** A renderer candidate. It remains untrusted until downstream validation succeeds. */
@JvmInline
value class RenderedDraft(val text: String)

/**
 * Language realization only. Implementations receive neither policy authority nor
 * unrestricted transcript, profile, provenance, or persistence access.
 */
fun interface ConversationalRenderer {
    suspend fun render(request: RenderRequest): RenderedDraft
}
