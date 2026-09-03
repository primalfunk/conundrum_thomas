package com.conundrum.thomas.v2.provenance

enum class AuthorityDomain {
    CLINICAL_PUBLIC_HEALTH,
    CLINICAL_GOVERNMENT,
    CLINICAL_RESOURCE_PROVIDER,
    ENGINEERING_GOVERNANCE,
}

enum class SourceClass {
    FOUNDATIONAL_HELPING,
    STRUCTURED_INTERVENTION,
    CLINICAL_GUIDELINE,
    SAFETY_GUIDANCE,
    THERAPEUTIC_METHOD,
    IMPLEMENTATION_GUIDANCE,
    COMPETENCY_ASSESSMENT,
    ENGINEERING_GOVERNANCE,
}

enum class CommercialUseStatus {
    PUBLIC_DOMAIN,
    OPEN_COMMERCIAL_USE,
    OPEN_NONCOMMERCIAL_ONLY,
    COMMERCIAL_PERMISSION_REQUIRED,
    COPYRIGHTED_REFERENCE_ONLY,
    RIGHTS_UNCLEAR,
    LEGAL_REVIEW_REQUIRED,
}

enum class ReviewState {
    DISCOVERED,
    VERIFIED,
    RIGHTS_REVIEW_REQUIRED,
    CLINICAL_REVIEW_REQUIRED,
    APPROVED_AS_SOURCE,
    REJECTED,
    SUPERSEDED,
}

enum class VersionRelationshipType {
    SUPERSEDED_BY,
    UPDATED_BY,
    TRAINING_MANUAL_FOR,
    WEB_ANNEX_TO,
    PROCEDURAL_REFERENCE_INFORMED_BY_NEWER_GUIDELINE,
    COMPLEMENTS,
}

@JvmInline
value class Sha256 private constructor(val value: String) {
    companion object {
        private val format = Regex("^[0-9a-f]{64}$")

        fun parse(value: String): Sha256 {
            require(format.matches(value)) { "SHA-256 must be 64 lowercase hexadecimal characters" }
            return Sha256(value)
        }
    }
}

data class ArtifactIdentity(
    val sha256: Sha256,
    val byteSize: Long,
) {
    init {
        require(byteSize > 0) { "Artifact byte size must be positive" }
    }
}
