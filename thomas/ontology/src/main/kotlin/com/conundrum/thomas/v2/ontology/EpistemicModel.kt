package com.conundrum.thomas.v2.ontology

import com.conundrum.thomas.v2.provenance.GovernedSourceReference

enum class EvidenceKind {
    DIRECTLY_STATED_BY_USER,
    OBSERVED_IN_CURRENT_INTERACTION,
    HISTORICAL_USER_REPORT,
    EXTERNALLY_SOURCED_FACT,
    DERIVED_STRUCTURED_FACT,
    THOMAS_HYPOTHESIS,
    CONTRADICTION,
    UNRESOLVED,
    UNKNOWN,
}

enum class EpistemicResolution {
    RESOLVED_AS_REPORTED,
    TENTATIVE,
    CONFLICTING,
    INSUFFICIENT_EVIDENCE,
    UNRESOLVED,
    UNKNOWN,
}

enum class EpistemicStrength {
    UNSPECIFIED,
    TENTATIVE,
    SUPPORTED,
}

enum class DurableProfileAdmission {
    GOVERNED_ADMISSION_REQUIRED,
}

@JvmInline
value class EvidenceRecordId private constructor(val value: String) {
    companion object {
        fun parse(value: String): EvidenceRecordId {
            require(value.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")))
            return EvidenceRecordId(value)
        }
    }
}

sealed interface EvidencePointer {
    data class InteractionEvidence(val reference: String) : EvidencePointer {
        init { require(reference.isNotBlank()) }
    }

    data class HistoricalEvidence(val reference: String) : EvidencePointer {
        init { require(reference.isNotBlank()) }
    }

    data class ExternalSourceEvidence(val source: GovernedSourceReference) : EvidencePointer

    data class DerivedEvidence(val evidenceIds: Set<EvidenceRecordId>) : EvidencePointer {
        init { require(evidenceIds.isNotEmpty()) }
    }
}

/**
 * An immutable epistemic record. It is evidence only and has no conversion to a durable fact.
 */
data class EpistemicRecord<T>(
    val id: EvidenceRecordId,
    val conceptId: OntologyConceptId,
    val value: T?,
    val evidenceKind: EvidenceKind,
    val resolution: EpistemicResolution,
    val strength: EpistemicStrength,
    val evidence: List<EvidencePointer> = emptyList(),
    val conflictsWith: Set<EvidenceRecordId> = emptySet(),
    val supersedesEvidence: Set<EvidenceRecordId> = emptySet(),
    val durableProfileAdmission: DurableProfileAdmission = DurableProfileAdmission.GOVERNED_ADMISSION_REQUIRED,
) {
    init {
        if (evidenceKind == EvidenceKind.UNKNOWN) {
            require(value == null && resolution == EpistemicResolution.UNKNOWN) {
                "Unknown evidence must remain explicitly unknown."
            }
        }
        if (resolution == EpistemicResolution.CONFLICTING || evidenceKind == EvidenceKind.CONTRADICTION) {
            require(conflictsWith.isNotEmpty()) { "Conflicting evidence must identify what it conflicts with." }
        }
        if (evidenceKind == EvidenceKind.THOMAS_HYPOTHESIS) {
            require(resolution != EpistemicResolution.RESOLVED_AS_REPORTED) {
                "A Thomas hypothesis cannot masquerade as user-established fact."
            }
        }
    }
}
