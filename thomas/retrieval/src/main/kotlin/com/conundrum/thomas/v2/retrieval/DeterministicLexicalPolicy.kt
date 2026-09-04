package com.conundrum.thomas.v2.retrieval

import java.util.Locale

/** Fixed local lexical policy. Lexical-only candidacy is limited to explicit Journal look-back. */
object DeterministicLexicalPolicy {
    private val stopWords = setOf(
        "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "from", "i", "in", "is",
        "it", "me", "my", "of", "on", "or", "that", "the", "this", "to", "was", "were", "with",
    )

    fun normalizeTerms(terms: Set<String>): Set<String> = terms.flatMap(::tokens).toSortedSet()

    fun matchCount(text: String, normalizedTerms: Set<String>): Int {
        if (normalizedTerms.isEmpty()) return 0
        val textTokens = tokens(text).toSet()
        return normalizedTerms.count { it in textTokens }
    }

    fun normalizedPhrase(text: String): String = tokens(text).joinToString(" ")

    private fun tokens(text: String): List<String> = text.lowercase(Locale.ROOT)
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() && it !in stopWords }
}
