package com.conundrum.thomas.v2.safety

import com.conundrum.thomas.v2.domain.mode.ThomasMode

private val safetyStateIdFormat = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")

@JvmInline
value class SafetyEvidenceRevision private constructor(val value: Long) {
    companion object {
        fun of(value: Long): SafetyEvidenceRevision {
            require(value > 0) { "Safety evidence revision must be positive." }
            return SafetyEvidenceRevision(value)
        }
    }
}

enum class SafetyEvidenceResolution {
    ESTABLISHED,
    NOT_ASKED,
    UNKNOWN,
    USER_DECLINED,
    TENTATIVE,
    CONTRADICTORY,
}

enum class SafetyEvidenceOrigin {
    DIRECT_USER_REPORT,
    VERIFIED_GOVERNED_RESPONSE,
    HISTORICAL_USER_REPORT,
    DERIVED_STRUCTURED_FACT,
    UPSTREAM_SCOPE_AUTHORITY,
}

/**
 * Immutable structured evidence. Absence is a value that must be established; it is never
 * synthesized from UNKNOWN, NOT_ASKED, USER_DECLINED, TENTATIVE, or CONTRADICTORY.
 */
data class SafetyEvidence<T>(
    val value: T?,
    val resolution: SafetyEvidenceResolution,
    val origin: SafetyEvidenceOrigin?,
    val evidenceReferences: Set<String>,
) {
    fun validationErrors(fieldName: String): List<String> = buildList {
        when (resolution) {
            SafetyEvidenceResolution.ESTABLISHED,
            SafetyEvidenceResolution.TENTATIVE -> {
                if (value == null) add("$fieldName: $resolution evidence requires a value")
                if (origin == null) add("$fieldName: $resolution evidence requires an origin")
                if (evidenceReferences.isEmpty()) add("$fieldName: $resolution evidence requires a reference")
            }
            SafetyEvidenceResolution.USER_DECLINED -> {
                if (value != null) add("$fieldName: USER_DECLINED cannot carry a value")
                if (origin !in setOf(SafetyEvidenceOrigin.DIRECT_USER_REPORT, SafetyEvidenceOrigin.VERIFIED_GOVERNED_RESPONSE)) {
                    add("$fieldName: USER_DECLINED requires a user-response origin")
                }
                if (evidenceReferences.isEmpty()) add("$fieldName: USER_DECLINED requires a reference")
            }
            SafetyEvidenceResolution.CONTRADICTORY -> {
                if (value != null) add("$fieldName: CONTRADICTORY cannot collapse to one value")
                if (evidenceReferences.size < 2) add("$fieldName: CONTRADICTORY requires at least two references")
            }
            SafetyEvidenceResolution.NOT_ASKED,
            SafetyEvidenceResolution.UNKNOWN -> {
                if (value != null || origin != null || evidenceReferences.isNotEmpty()) {
                    add("$fieldName: $resolution must not carry value or evidence")
                }
            }
        }
        if (evidenceReferences.any(String::isBlank)) add("$fieldName: evidence references cannot be blank")
    }

    fun isEstablishedAs(expected: T): Boolean =
        resolution == SafetyEvidenceResolution.ESTABLISHED && value == expected

    companion object {
        fun <T> established(value: T, origin: SafetyEvidenceOrigin, reference: String) =
            SafetyEvidence(value, SafetyEvidenceResolution.ESTABLISHED, origin, setOf(reference))

        fun <T> tentative(value: T, origin: SafetyEvidenceOrigin, reference: String) =
            SafetyEvidence(value, SafetyEvidenceResolution.TENTATIVE, origin, setOf(reference))

        fun <T> notAsked() = SafetyEvidence<T>(null, SafetyEvidenceResolution.NOT_ASKED, null, emptySet())

        fun <T> unknown() = SafetyEvidence<T>(null, SafetyEvidenceResolution.UNKNOWN, null, emptySet())

        fun <T> declined(reference: String) = SafetyEvidence<T>(
            null,
            SafetyEvidenceResolution.USER_DECLINED,
            SafetyEvidenceOrigin.DIRECT_USER_REPORT,
            setOf(reference),
        )

        fun <T> contradictory(vararg references: String) = SafetyEvidence<T>(
            null,
            SafetyEvidenceResolution.CONTRADICTORY,
            SafetyEvidenceOrigin.DERIVED_STRUCTURED_FACT,
            references.toSet(),
        )
    }
}

enum class SafetyPresence { PRESENT, ABSENT }

enum class ExplicitEmergencyCircumstance {
    NONE_ESTABLISHED,
    SELF_HARM_EMERGENCY_EXPLICITLY_ESTABLISHED,
    OTHER_EMERGENCY_EXPLICITLY_ESTABLISHED,
}

enum class SpecializedScopeCondition {
    NONE_IDENTIFIED,
    PSYCHOSIS,
    MANIA,
    SEVERE_INTOXICATION,
    ABUSE_REQUIRING_SPECIALIZED_RESPONSE,
    TRAUMA_TREATMENT_OUTSIDE_SUPPORTED_PROCEDURE,
    COMPLEX_SUBSTANCE_USE_TREATMENT,
    OTHER_SPECIALIZED_CONDITION,
}

enum class PopulationApplicability {
    SUPPORTED_ADULT_QUALIFICATION_CONTEXT,
    UNSUPPORTED_AGE_OR_POPULATION,
    UNSUPPORTED_SETTING,
}

enum class PresentingScope {
    BOUNDED_ORDINARY_PERSONAL_PROBLEM,
    OUT_OF_SCOPE,
    SPECIALIZED_POLICY_REQUIRED,
}

enum class SafetyField {
    CURRENT_EMERGENCY,
    ACUTE_MEDICAL_EMERGENCY,
    SELF_HARM_RELEVANCE,
    HARM_TO_OTHERS_RELEVANCE,
    SPECIALIZED_SCOPE_CONDITION,
    POPULATION_APPLICABILITY,
    PRESENTING_SCOPE,
}

/**
 * Controls what the caller does with genuinely absent safety evidence. This is not a safety
 * value: UNKNOWN remains UNKNOWN in either mode. Qualification callers can require an explicit
 * clarification; a production conversational caller can continue ordinary therapy until current
 * content supplies an authorized safety boundary or a clarification is legitimately pending.
 */
enum class SafetyUnknownEvidencePolicy {
    REQUIRE_CLARIFICATION,
    ALLOW_ORDINARY_WITHOUT_REASSURANCE,
}

data class SafetyScopeInput(
    val stateId: String,
    val evidenceRevision: SafetyEvidenceRevision,
    val mode: ThomasMode,
    val currentEmergency: SafetyEvidence<ExplicitEmergencyCircumstance>,
    val acuteMedicalEmergency: SafetyEvidence<SafetyPresence>,
    val selfHarmRelevance: SafetyEvidence<SafetyPresence>,
    val harmToOthersRelevance: SafetyEvidence<SafetyPresence>,
    val specializedScopeCondition: SafetyEvidence<SpecializedScopeCondition>,
    val populationApplicability: SafetyEvidence<PopulationApplicability>,
    val presentingScope: SafetyEvidence<PresentingScope>,
    val unknownEvidencePolicy: SafetyUnknownEvidencePolicy = SafetyUnknownEvidencePolicy.REQUIRE_CLARIFICATION,
) : SafetyReviewInput {
    fun validationErrors(): List<String> = buildList {
        if (!safetyStateIdFormat.matches(stateId)) add("stateId must be a lowercase hyphenated identifier")
        SafetyField.entries.forEach { field -> addAll(evidence(field).validationErrors(field.name)) }
    }

    fun evidence(field: SafetyField): SafetyEvidence<*> = when (field) {
        SafetyField.CURRENT_EMERGENCY -> currentEmergency
        SafetyField.ACUTE_MEDICAL_EMERGENCY -> acuteMedicalEmergency
        SafetyField.SELF_HARM_RELEVANCE -> selfHarmRelevance
        SafetyField.HARM_TO_OTHERS_RELEVANCE -> harmToOthersRelevance
        SafetyField.SPECIALIZED_SCOPE_CONDITION -> specializedScopeCondition
        SafetyField.POPULATION_APPLICABILITY -> populationApplicability
        SafetyField.PRESENTING_SCOPE -> presentingScope
    }
}
