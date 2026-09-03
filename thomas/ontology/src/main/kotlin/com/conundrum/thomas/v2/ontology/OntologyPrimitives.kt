package com.conundrum.thomas.v2.ontology

private val conceptIdFormat = Regex("^[a-z][a-z0-9-]*(?:\\.[a-z][a-z0-9-]*)+$")

@JvmInline
value class OntologyConceptId private constructor(val value: String) : Comparable<OntologyConceptId> {
    override fun compareTo(other: OntologyConceptId): Int = value.compareTo(other.value)

    companion object {
        fun parse(value: String): OntologyConceptId {
            require(conceptIdFormat.matches(value)) {
                "Ontology concept IDs must be lowercase, namespaced, and hyphenated."
            }
            return OntologyConceptId(value)
        }
    }
}

data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
) {
    init {
        require(major >= 0 && minor >= 0 && patch >= 0) { "Semantic-version components cannot be negative." }
    }

    override fun toString(): String = "$major.$minor.$patch"
}

data class OntologyRelease(
    val releaseId: String,
    val version: SemanticVersion,
    val phase: String,
) {
    init {
        require(releaseId.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")))
        require(phase == "CT-V2-02") { "This release is scoped to CT-V2-02." }
    }
}

enum class ConceptFamily {
    OBSERVED_USER_STATE,
    THERAPEUTIC_GOAL,
    DIALOGUE_ACT,
    INTERVENTION_FAMILY,
    CONSTRAINT,
    SAFETY_CONTEXT,
    PROCEDURAL_VOCABULARY,
}

enum class OntologyDomain {
    PSYCHOLOGICAL_SUPPORT,
    SAFETY_BOUNDARY,
    MODE_AUTHORITY,
    PROCEDURAL_GRAMMAR,
    ENGINEERING_GOVERNANCE,
}

enum class DefinitionStatus {
    DEFINED,
    CANDIDATE,
    UNOPENED,
}

enum class RuntimeAuthorizationStatus {
    NOT_AUTHORIZED,
    AUTHORIZED_FOR_RUNTIME_USE,
}

enum class SourceSupportStatus {
    ARCHITECTURAL_DEFINITION,
    SOURCE_LINKED_CANDIDATE,
    SOURCE_REVIEW_NEEDED,
}

sealed interface ConceptLookup {
    data class Known(val concept: OntologyConcept) : ConceptLookup
    data class Unrecognized(val preservedId: OntologyConceptId) : ConceptLookup
}
