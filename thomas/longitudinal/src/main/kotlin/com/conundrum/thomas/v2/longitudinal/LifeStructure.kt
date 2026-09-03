package com.conundrum.thomas.v2.longitudinal

sealed interface LifeEntity {
    val id: LifeEntityId
    val label: String
    val supportingAssertionIds: Set<AssertionId>
}

private fun validateEntity(label: String, supportingAssertionIds: Set<AssertionId>) {
    require(label.isNotBlank())
    require(supportingAssertionIds.isNotEmpty()) { "A life entity requires supporting assertions" }
}

data class Person(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
) : LifeEntity { init { validateEntity(label, supportingAssertionIds) } }

data class Place(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
) : LifeEntity { init { validateEntity(label, supportingAssertionIds) } }

data class LifeEvent(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val participantIds: Set<LifeEntityId> = emptySet(),
    val placeIds: Set<LifeEntityId> = emptySet(),
) : LifeEntity { init { validateEntity(label, supportingAssertionIds) } }

data class LifePeriod(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val temporalDescription: EventTime,
) : LifeEntity { init { validateEntity(label, supportingAssertionIds) } }

data class Relationship(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val participantIds: Set<LifeEntityId>,
) : LifeEntity {
    init {
        validateEntity(label, supportingAssertionIds)
        require(participantIds.size >= 2)
    }
}

data class Role(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val holderId: LifeEntityId,
    val contextId: LifeEntityId? = null,
) : LifeEntity { init { validateEntity(label, supportingAssertionIds) } }

data class Decision(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val decisionMakerIds: Set<LifeEntityId>,
) : LifeEntity {
    init {
        validateEntity(label, supportingAssertionIds)
        require(decisionMakerIds.isNotEmpty())
    }
}

data class Behavior(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val actorIds: Set<LifeEntityId>,
) : LifeEntity {
    init {
        validateEntity(label, supportingAssertionIds)
        require(actorIds.isNotEmpty())
    }
}

data class CopingResponse(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val actorId: LifeEntityId,
    val relatedEventId: LifeEntityId? = null,
) : LifeEntity { init { validateEntity(label, supportingAssertionIds) } }

data class Outcome(
    override val id: LifeEntityId,
    override val label: String,
    override val supportingAssertionIds: Set<AssertionId>,
    val outcomeOfId: LifeEntityId,
) : LifeEntity { init { validateEntity(label, supportingAssertionIds) } }

enum class EntityIdentityStatus {
    UNRESOLVED,
    CANDIDATE_SAME_ENTITY,
    ESTABLISHED_SAME_ENTITY,
    ESTABLISHED_DIFFERENT_ENTITY,
}

data class EntityIdentityLink(
    val id: IdentityLinkId,
    val leftEntityId: LifeEntityId,
    val rightEntityId: LifeEntityId,
    val status: EntityIdentityStatus,
    val supportingAssertionIds: Set<AssertionId> = emptySet(),
    val rationale: String,
) {
    init {
        require(leftEntityId != rightEntityId) { "An identity link cannot compare an entity with itself" }
        require(rationale.isNotBlank())
        if (status == EntityIdentityStatus.ESTABLISHED_SAME_ENTITY || status == EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY) {
            require(supportingAssertionIds.isNotEmpty()) { "Established identity requires evidence" }
        }
    }

    fun canonicalPair(): Pair<LifeEntityId, LifeEntityId> =
        if (leftEntityId < rightEntityId) leftEntityId to rightEntityId else rightEntityId to leftEntityId
}

enum class InformationCoverageStatus {
    UNKNOWN,
    NOT_EXPLORED,
    PARTIAL,
    PRIVATE,
    DECLINED,
    IRRELEVANT,
    UNRESOLVED,
    SUFFICIENTLY_UNDERSTOOD,
}

data class CoverageTopic(
    val id: CoverageTopicId,
    val label: String,
    val status: InformationCoverageStatus,
    val sourceRecordIds: Set<SourceRecordId> = emptySet(),
) {
    init { require(label.isNotBlank()) }
}
