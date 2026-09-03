package com.conundrum.thomas.v2.longitudinal

import java.io.Serializable

private val longitudinalIdPattern = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")

private fun validatedId(label: String, value: String): String {
    require(longitudinalIdPattern.matches(value)) { "$label must be a stable lowercase identifier" }
    return value
}

@JvmInline value class SourceRecordId private constructor(val value: String) : Comparable<SourceRecordId>, Serializable {
    override fun compareTo(other: SourceRecordId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = SourceRecordId(validatedId("Source record ID", value)) }
}

@JvmInline value class SourceIdentityId private constructor(val value: String) : Comparable<SourceIdentityId>, Serializable {
    override fun compareTo(other: SourceIdentityId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = SourceIdentityId(validatedId("Source identity ID", value)) }
}

@JvmInline value class InteractionId private constructor(val value: String) : Comparable<InteractionId>, Serializable {
    override fun compareTo(other: InteractionId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = InteractionId(validatedId("Interaction ID", value)) }
}

@JvmInline value class AssertionId private constructor(val value: String) : Comparable<AssertionId>, Serializable {
    override fun compareTo(other: AssertionId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = AssertionId(validatedId("Assertion ID", value)) }
}

@JvmInline value class LifeEntityId private constructor(val value: String) : Comparable<LifeEntityId>, Serializable {
    override fun compareTo(other: LifeEntityId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = LifeEntityId(validatedId("Life entity ID", value)) }
}

@JvmInline value class PersonalConceptId private constructor(val value: String) : Comparable<PersonalConceptId>, Serializable {
    override fun compareTo(other: PersonalConceptId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = PersonalConceptId(validatedId("Personal concept ID", value)) }
}

@JvmInline value class HypothesisId private constructor(val value: String) : Comparable<HypothesisId>, Serializable {
    override fun compareTo(other: HypothesisId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = HypothesisId(validatedId("Hypothesis ID", value)) }
}

@JvmInline value class EvidenceRelationId private constructor(val value: String) : Comparable<EvidenceRelationId>, Serializable {
    override fun compareTo(other: EvidenceRelationId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = EvidenceRelationId(validatedId("Evidence relation ID", value)) }
}

@JvmInline value class IdentityLinkId private constructor(val value: String) : Comparable<IdentityLinkId>, Serializable {
    override fun compareTo(other: IdentityLinkId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = IdentityLinkId(validatedId("Identity link ID", value)) }
}

@JvmInline value class CoverageTopicId private constructor(val value: String) : Comparable<CoverageTopicId>, Serializable {
    override fun compareTo(other: CoverageTopicId) = value.compareTo(other.value)
    companion object { fun parse(value: String) = CoverageTopicId(validatedId("Coverage topic ID", value)) }
}
