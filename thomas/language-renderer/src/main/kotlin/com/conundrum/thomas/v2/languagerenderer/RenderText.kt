package com.conundrum.thomas.v2.languagerenderer

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

internal object RenderText {
    fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    fun normalize(value: String): String = value.lowercase(Locale.ROOT)
        .map { if (it.isLetterOrDigit()) it else ' ' }
        .joinToString("")
        .trim()
        .replace(Regex("\\s+"), " ")

    fun responseFingerprint(value: String): String = sha256(normalize(value))

    fun openingFingerprint(value: String): String = sha256(
        normalize(value).split(' ').filter(String::isNotBlank).take(3).joinToString(" "),
    )

    fun containsMarker(text: String, markers: Set<String>): Boolean {
        val normalized = text.lowercase(Locale.ROOT)
        return markers.any { normalized.contains(it.lowercase(Locale.ROOT)) }
    }

    fun unquotedText(text: String): String {
        val result = StringBuilder(text.length)
        var insideStraight = false
        var insideCurly = false
        text.forEach { character ->
            when {
                character.code == 34 -> insideStraight = !insideStraight
                character == '“' -> insideCurly = true
                character == '”' -> insideCurly = false
                !insideStraight && !insideCurly -> result.append(character)
                else -> result.append(' ')
            }
        }
        return result.toString()
    }

    /** Question marks inside straight or curly quoted source data are ignored. */
    fun questionCount(text: String): Int {
        var insideStraight = false
        var insideCurly = false
        var count = 0
        text.forEach { character ->
            when (character) {
                '"' -> insideStraight = !insideStraight
                '“' -> insideCurly = true
                '”' -> insideCurly = false
                '?' -> if (!insideStraight && !insideCurly) count += 1
            }
        }
        return count
    }

    /** Conservative punctuation detector; rhetorical marked questions count as questions. */
    fun sentenceCount(text: String): Int {
        if (text.isBlank()) return 0
        var insideStraight = false
        var insideCurly = false
        var count = 0
        var previousWasTerminal = false
        text.forEach { character ->
            when (character) {
                '"' -> insideStraight = !insideStraight
                '“' -> insideCurly = true
                '”' -> insideCurly = false
            }
            val terminal = !insideStraight && !insideCurly && character in setOf('.', '!', '?')
            if (terminal && !previousWasTerminal) count += 1
            previousWasTerminal = terminal
        }
        return maxOf(1, count)
    }
}
