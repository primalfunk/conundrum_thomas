package com.conundrum.thomas.v2.domain.rendering

/**
 * A bounded language-realization instruction selected upstream by Thomas.
 *
 * This value conveys no authority to select, alter, or add therapeutic behavior.
 */
@JvmInline
value class RenderCommand(val value: String) {
    init {
        require(value.isNotBlank()) { "A render command must not be blank." }
    }
}

/** Text or evidence deliberately disclosed to a renderer for one render request. */
data class AuthorizedSupportingText(
    val reference: String,
    val text: String,
)

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
