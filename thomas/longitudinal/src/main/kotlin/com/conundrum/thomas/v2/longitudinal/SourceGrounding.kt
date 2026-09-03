package com.conundrum.thomas.v2.longitudinal

import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

enum class SourceNormalization : Serializable {
    NONE,
    WHITESPACE_TRIMMED_FOR_COMPARISON_ONLY,
    CASE_FOLDED_FOR_COMPARISON_ONLY,
}

/** Exact extractive grounding. The source revision, not normalized text, remains authoritative. */
data class SourceSpanGrounding(
    val sourceRevisionId: SourceRecordId,
    val startOffsetInclusive: Int,
    val endOffsetExclusive: Int,
    val exactFragment: String,
    val sourceRevisionSha256: String,
    val extractionRuleVersion: String,
    val normalization: SourceNormalization = SourceNormalization.NONE,
) : Serializable {
    init {
        require(startOffsetInclusive >= 0)
        require(endOffsetExclusive > startOffsetInclusive)
        require(exactFragment.isNotEmpty())
        require(sourceRevisionSha256.matches(Regex("^[0-9a-f]{64}$")))
        require(extractionRuleVersion.matches(Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")))
    }
}

enum class EvidenceEpistemicClass : Serializable {
    EXPLICIT_USER_ASSERTION,
    EXPLICIT_SELF_REPORT,
    SELF_BELIEF,
    USER_INTERPRETATION,
    THIRD_PARTY_REPORT,
    ENTITY_REFERENCE,
    EVENT_REFERENCE,
}

enum class AssertionPolarity : Serializable {
    AFFIRMATIVE,
    NEGATIVE,
}

fun sourceTextSha256(content: String): String = MessageDigest.getInstance("SHA-256")
    .digest(content.toByteArray(StandardCharsets.UTF_8))
    .joinToString("") { "%02x".format(it) }
