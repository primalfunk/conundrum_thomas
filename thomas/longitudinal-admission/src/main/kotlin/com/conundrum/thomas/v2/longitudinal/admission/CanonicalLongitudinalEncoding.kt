package com.conundrum.thomas.v2.longitudinal.admission

import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.util.Base64

/** Canonical logical encoding. It is used only as SHA-256 input and is never diagnostic text. */
object CanonicalLongitudinalEncoding {
    fun sha256(value: Any?): String = MessageDigest.getInstance("SHA-256")
        .digest(encode(value).toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    fun stateDigest(state: LongitudinalAggregateState): String = sha256(state)

    fun requestFingerprint(request: LongitudinalAdmissionRequest): String = sha256(
        listOf(
            request.expectedStoreRevision,
            request.actor,
            request.origin,
            request.policyVersion.value,
            request.classification,
            request.operation,
        ),
    )

    private fun encode(value: Any?): String = when (value) {
        null -> "null"
        is String -> "s:${Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))}"
        is Boolean -> "b:$value"
        is Byte, is Short, is Int, is Long, is Float, is Double -> "n:$value"
        is Enum<*> -> "e:${value.javaClass.name}:${value.name}"
        is Instant, is LocalDate, is Year, is YearMonth -> "t:${value.javaClass.name}:$value"
        is Map<*, *> -> value.entries.map { encode(it.key) to encode(it.value) }
            .sortedBy { it.first }.joinToString(prefix = "map[", postfix = "]") { "${it.first}=${it.second}" }
        is Set<*> -> value.map(::encode).sorted().joinToString(prefix = "set[", postfix = "]")
        is Iterable<*> -> value.map(::encode).joinToString(prefix = "list[", postfix = "]")
        is Array<*> -> value.map(::encode).joinToString(prefix = "array[", postfix = "]")
        else -> encodeObject(value)
    }

    private fun encodeObject(value: Any): String {
        val fields = value.javaClass.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) || it.isSynthetic }
            .sortedBy { it.name }
        val body = fields.joinToString(",") { field ->
            field.isAccessible = true
            "${field.name}=${encode(field.get(value))}"
        }
        return "o:${value.javaClass.name}{$body}"
    }
}
