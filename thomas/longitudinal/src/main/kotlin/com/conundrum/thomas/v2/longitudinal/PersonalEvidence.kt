package com.conundrum.thomas.v2.longitudinal

enum class AcquisitionMode {
    JOURNAL,
    BIOGRAPHER_OPEN_NARRATIVE,
    BIOGRAPHER_GUIDED_TIMELINE,
    THERAPIST_CONVERSATION,
    USER_CORRECTION,
}

sealed interface OriginalSourceContent {
    data class Inline(val exactContent: String) : OriginalSourceContent {
        init { require(exactContent.isNotBlank()) }
    }

    data class FutureSafeReference(
        val contentReference: String,
        val contentSha256: String? = null,
    ) : OriginalSourceContent {
        init {
            require(contentReference.isNotBlank())
            require(contentSha256 == null || contentSha256.matches(Regex("^[0-9a-f]{64}$")))
        }
    }
}

data class PersonalEvidenceProvenance(
    val acquisitionMode: AcquisitionMode,
    val interactionId: InteractionId? = null,
    val sourceRevision: Int = 1,
    val previousRevisionId: SourceRecordId? = null,
    val metadata: Map<String, String> = emptyMap(),
) {
    init {
        require(sourceRevision > 0)
        require(sourceRevision > 1 || previousRevisionId == null)
        require(metadata.keys.none(String::isBlank))
        require(metadata.values.none(String::isBlank))
    }
}

/** Evidence of what was communicated. Derived records always retain this identity. */
data class SourceRecord(
    val id: SourceRecordId,
    val provenance: PersonalEvidenceProvenance,
    val reportTime: ReportTime,
    val recordTime: RecordTime,
    val originalContent: OriginalSourceContent,
) {
    init {
        require(!recordTime.value.isBefore(reportTime.value))
        require(provenance.previousRevisionId != id)
    }
}

enum class UserEvidenceKind {
    EXPLICIT_USER_ASSERTION,
    USER_INTERPRETATION,
}

enum class AssertionUncertainty {
    STATED_WITHOUT_QUALIFICATION,
    STATED_AS_UNCERTAIN,
    APPROXIMATE,
    CONTESTED,
}

enum class PredicateSemantics {
    OBSERVABLE_OR_REPORTED_EVENT,
    USER_INTERNAL_EXPERIENCE,
    THIRD_PARTY_INTERNAL_STATE,
    EVALUATION_OR_MEANING,
    RELATIONSHIP_OR_ROLE,
    LOCATION,
    TEMPORAL,
    ACTION_OR_BEHAVIOR,
    OUTCOME,
    OTHER,
}

data class AssertionPredicate(
    val conceptId: PersonalConceptId,
    val semantics: PredicateSemantics,
)

sealed interface AssertionSubject {
    data object User : AssertionSubject
    data class Entity(val entityId: LifeEntityId) : AssertionSubject
}

sealed interface AssertionValue {
    data class Text(val value: String) : AssertionValue { init { require(value.isNotBlank()) } }
    data class EntityReference(val entityId: LifeEntityId) : AssertionValue
    data class EntityReferences(val entityIds: Set<LifeEntityId>) : AssertionValue { init { require(entityIds.isNotEmpty()) } }
    data class TimeReference(val value: EventTime) : AssertionValue
    data class BooleanValue(val value: Boolean) : AssertionValue
    data class IntegerValue(val value: Long) : AssertionValue
    data class ConceptValue(val conceptId: PersonalConceptId) : AssertionValue
}

/** User-derived claim. Thomas hypotheses use a separate type and cannot be constructed here. */
data class EvidenceAssertion(
    val id: AssertionId,
    val sourceRecordId: SourceRecordId,
    val subject: AssertionSubject,
    val predicate: AssertionPredicate,
    val value: AssertionValue,
    val kind: UserEvidenceKind,
    val uncertainty: AssertionUncertainty,
    val eventTime: EventTime = EventTime.Unknown("No event time supplied"),
) {
    init {
        require(
            kind != UserEvidenceKind.EXPLICIT_USER_ASSERTION ||
                predicate.semantics != PredicateSemantics.THIRD_PARTY_INTERNAL_STATE,
        ) { "A third party's internal state cannot be admitted as a direct user-established fact" }
    }
}

enum class HypothesisStatus {
    TENTATIVE,
    PARTIALLY_SUPPORTED,
    CONTESTED,
    REQUIRES_REVIEW,
    SUPERSEDED,
}

/** Thomas-owned interpretation layer. It is structurally separate from user assertions. */
data class ThomasHypothesis(
    val id: HypothesisId,
    val subject: AssertionSubject,
    val predicate: AssertionPredicate,
    val proposedValue: AssertionValue,
    val status: HypothesisStatus,
    val createdAt: RecordTime,
    val rationale: String,
) {
    init {
        require(rationale.isNotBlank())
        require(status != HypothesisStatus.PARTIALLY_SUPPORTED || rationale.length >= 10)
    }
}

sealed interface ClaimReference {
    data class Assertion(val assertionId: AssertionId) : ClaimReference
    data class Hypothesis(val hypothesisId: HypothesisId) : ClaimReference
}
