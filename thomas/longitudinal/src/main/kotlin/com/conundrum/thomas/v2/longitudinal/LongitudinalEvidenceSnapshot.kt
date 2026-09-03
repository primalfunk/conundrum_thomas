package com.conundrum.thomas.v2.longitudinal

data class LongitudinalValidationIssue(val code: String, val detail: String)

/**
 * Immutable evidence-oriented aggregate. It validates references and history; it is not a store,
 * writer, mutable psychological profile, retrieval view, or admission authority.
 */
data class LongitudinalEvidenceSnapshot(
    val sources: List<SourceRecord> = emptyList(),
    val assertions: List<EvidenceAssertion> = emptyList(),
    val entities: List<LifeEntity> = emptyList(),
    val contradictions: List<ContradictionRelation> = emptyList(),
    val corrections: List<CorrectionRelation> = emptyList(),
    val supersessions: List<SupersessionRelation> = emptyList(),
    val hypotheses: List<ThomasHypothesis> = emptyList(),
    val hypothesisDependencies: List<HypothesisDependency> = emptyList(),
    val identityLinks: List<EntityIdentityLink> = emptyList(),
    val coverageTopics: List<CoverageTopic> = emptyList(),
) {
    fun validationIssues(): List<LongitudinalValidationIssue> {
        val issues = mutableListOf<LongitudinalValidationIssue>()
        fun issue(code: String, detail: String) { issues += LongitudinalValidationIssue(code, detail) }

        duplicateValues(sources.map { it.id }).forEach { issue("DUPLICATE_SOURCE_ID", it.value) }
        duplicateValues(assertions.map { it.id }).forEach { issue("DUPLICATE_ASSERTION_ID", it.value) }
        duplicateValues(entities.map { it.id }).forEach { issue("DUPLICATE_ENTITY_ID", it.value) }
        duplicateValues(hypotheses.map { it.id }).forEach { issue("DUPLICATE_HYPOTHESIS_ID", it.value) }
        duplicateValues(identityLinks.map { it.id }).forEach { issue("DUPLICATE_IDENTITY_LINK_ID", it.value) }
        duplicateValues(coverageTopics.map { it.id }).forEach { issue("DUPLICATE_COVERAGE_ID", it.value) }
        duplicateValues(allEvidenceRelationIds()).forEach { issue("DUPLICATE_RELATION_ID", it.value) }

        val sourceById = sources.associateBy { it.id }
        val assertionById = assertions.associateBy { it.id }
        val entityById = entities.associateBy { it.id }
        val hypothesisById = hypotheses.associateBy { it.id }

        sources.forEach { source ->
            source.provenance.previousRevisionId?.let { previous ->
                val prior = sourceById[previous]
                if (prior == null) issue("MISSING_SOURCE_REVISION", "${source.id.value} -> ${previous.value}")
                else if (source.provenance.sourceRevision <= prior.provenance.sourceRevision) {
                    issue("SOURCE_REVISION_ORDER_INVALID", "${source.id.value} -> ${previous.value}")
                }
            }
        }
        val sourceRevisionEdges = sources.mapNotNull { source -> source.provenance.previousRevisionId?.let { it to source.id } }
        if (hasDirectedCycle(sourceRevisionEdges)) issue("SOURCE_REVISION_CYCLE", "Source revisions must be acyclic")

        assertions.forEach { assertion ->
            if (assertion.sourceRecordId !in sourceById) issue("ASSERTION_SOURCE_MISSING", assertion.id.value)
            referencedEntityIds(assertion.subject, assertion.value, assertion.eventTime).forEach { entityId ->
                if (entityId !in entityById) issue("ASSERTION_ENTITY_MISSING", "${assertion.id.value} -> ${entityId.value}")
            }
        }

        entities.forEach { entity ->
            entity.supportingAssertionIds.forEach { assertionId ->
                if (assertionId !in assertionById) issue("ENTITY_ASSERTION_MISSING", "${entity.id.value} -> ${assertionId.value}")
            }
            referencedEntityIds(entity).forEach { referenced ->
                if (referenced !in entityById) issue("ENTITY_REFERENCE_MISSING", "${entity.id.value} -> ${referenced.value}")
            }
        }

        hypotheses.forEach { hypothesis ->
            referencedEntityIds(hypothesis.subject, hypothesis.proposedValue, null).forEach { entityId ->
                if (entityId !in entityById) issue("HYPOTHESIS_ENTITY_MISSING", "${hypothesis.id.value} -> ${entityId.value}")
            }
            if (hypothesisDependencies.none { it.dependentHypothesisId == hypothesis.id }) {
                issue("HYPOTHESIS_WITHOUT_DEPENDENCY", hypothesis.id.value)
            }
        }

        contradictions.forEach { relation ->
            if (relation.leftAssertionId !in assertionById) issue("CONTRADICTION_ASSERTION_MISSING", relation.leftAssertionId.value)
            if (relation.rightAssertionId !in assertionById) issue("CONTRADICTION_ASSERTION_MISSING", relation.rightAssertionId.value)
        }

        corrections.forEach { relation ->
            val correcting = assertionById[relation.correctingAssertionId]
            if (correcting == null) issue("CORRECTION_ASSERTION_MISSING", relation.correctingAssertionId.value)
            if (relation.correctedAssertionId !in assertionById) issue("CORRECTED_ASSERTION_MISSING", relation.correctedAssertionId.value)
            val correctionSource = correcting?.let { sourceById[it.sourceRecordId] }
            if (correctionSource != null && correctionSource.provenance.acquisitionMode != AcquisitionMode.USER_CORRECTION) {
                issue("CORRECTION_SOURCE_MODE_INVALID", relation.correctingAssertionId.value)
            }
            if (relation.effect == CorrectionEffect.CORRECTS_AND_SUPERSEDES && supersessions.none {
                    it.successor == ClaimReference.Assertion(relation.correctingAssertionId) &&
                        it.predecessor == ClaimReference.Assertion(relation.correctedAssertionId) &&
                        it.kind == SupersessionKind.CORRECTS
                }
            ) issue("CORRECTION_SUPERSESSION_MISSING", relation.id.value)
        }

        supersessions.forEach { relation ->
            if (!claimExists(relation.successor, assertionById, hypothesisById)) issue("SUPERSESSION_SUCCESSOR_MISSING", relation.id.value)
            if (!claimExists(relation.predecessor, assertionById, hypothesisById)) issue("SUPERSESSION_PREDECESSOR_MISSING", relation.id.value)
        }
        if (hasClaimCycle(supersessions.map { it.predecessor to it.successor })) issue("SUPERSESSION_CYCLE", "Supersession must be acyclic")

        hypothesisDependencies.forEach { dependency ->
            if (dependency.dependentHypothesisId !in hypothesisById) issue("DEPENDENT_HYPOTHESIS_MISSING", dependency.id.value)
            if (!claimExists(dependency.prerequisite, assertionById, hypothesisById)) issue("DEPENDENCY_PREREQUISITE_MISSING", dependency.id.value)
        }
        val hypothesisEdges = hypothesisDependencies.mapNotNull { dependency ->
            (dependency.prerequisite as? ClaimReference.Hypothesis)?.let { it.hypothesisId to dependency.dependentHypothesisId }
        }
        if (hasDirectedCycle(hypothesisEdges)) issue("HYPOTHESIS_DEPENDENCY_CYCLE", "Hypothesis dependencies must be acyclic")

        identityLinks.forEach { link ->
            if (link.leftEntityId !in entityById) issue("IDENTITY_ENTITY_MISSING", link.leftEntityId.value)
            if (link.rightEntityId !in entityById) issue("IDENTITY_ENTITY_MISSING", link.rightEntityId.value)
            link.supportingAssertionIds.forEach { assertionId ->
                if (assertionId !in assertionById) issue("IDENTITY_ASSERTION_MISSING", "${link.id.value} -> ${assertionId.value}")
            }
        }
        validateIdentityConsistency(identityLinks, entityById.keys, ::issue)

        coverageTopics.flatMap { topic -> topic.sourceRecordIds.map { topic to it } }.forEach { (topic, sourceId) ->
            if (sourceId !in sourceById) issue("COVERAGE_SOURCE_MISSING", "${topic.id.value} -> ${sourceId.value}")
        }

        return issues.distinct().sortedWith(compareBy<LongitudinalValidationIssue> { it.code }.thenBy { it.detail })
    }

    fun requireValid(): LongitudinalEvidenceSnapshot {
        val issues = validationIssues()
        require(issues.isEmpty()) { issues.joinToString("; ") { "${it.code}:${it.detail}" } }
        return this
    }

    fun assertionsFrom(sourceRecordId: SourceRecordId): List<EvidenceAssertion> =
        assertions.filter { it.sourceRecordId == sourceRecordId }.sortedBy { it.id }

    fun sourcesFor(entityId: LifeEntityId): List<SourceRecord> {
        val sourceIds = entities.firstOrNull { it.id == entityId }?.supportingAssertionIds.orEmpty()
            .mapNotNull { assertionId -> assertions.firstOrNull { it.id == assertionId }?.sourceRecordId }
            .toSet()
        return sources.filter { it.id in sourceIds }.sortedBy { it.id }
    }

    fun temporalCoordinates(assertionId: AssertionId): TemporalCoordinates {
        val assertion = requireNotNull(assertions.firstOrNull { it.id == assertionId }) { "Unknown assertion ${assertionId.value}" }
        val source = requireNotNull(sources.firstOrNull { it.id == assertion.sourceRecordId }) { "Unknown source ${assertion.sourceRecordId.value}" }
        return TemporalCoordinates(assertion.eventTime, source.reportTime, source.recordTime)
    }

    private fun allEvidenceRelationIds(): List<EvidenceRelationId> =
        contradictions.map { it.id } + corrections.map { it.id } + supersessions.map { it.id } + hypothesisDependencies.map { it.id }
}

private fun <T> duplicateValues(values: List<T>): List<T> =
    values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.toList()

private fun referencedEntityIds(subject: AssertionSubject, value: AssertionValue, time: EventTime?): Set<LifeEntityId> = buildSet {
    if (subject is AssertionSubject.Entity) add(subject.entityId)
    when (value) {
        is AssertionValue.EntityReference -> add(value.entityId)
        is AssertionValue.EntityReferences -> addAll(value.entityIds)
        else -> Unit
    }
    when (time) {
        is EventTime.RelativePeriod -> time.anchorEntityId?.let(::add)
        is EventTime.BeforeOrAfter -> add(time.referenceEntityId)
        else -> Unit
    }
}

private fun referencedEntityIds(entity: LifeEntity): Set<LifeEntityId> = when (entity) {
    is Person, is Place, is LifePeriod -> emptySet()
    is LifeEvent -> entity.participantIds + entity.placeIds
    is Relationship -> entity.participantIds
    is Role -> setOfNotNull(entity.holderId, entity.contextId)
    is Decision -> entity.decisionMakerIds
    is Behavior -> entity.actorIds
    is CopingResponse -> setOfNotNull(entity.actorId, entity.relatedEventId)
    is Outcome -> setOf(entity.outcomeOfId)
}

private fun claimExists(
    reference: ClaimReference,
    assertions: Map<AssertionId, EvidenceAssertion>,
    hypotheses: Map<HypothesisId, ThomasHypothesis>,
): Boolean = when (reference) {
    is ClaimReference.Assertion -> reference.assertionId in assertions
    is ClaimReference.Hypothesis -> reference.hypothesisId in hypotheses
}

private fun hasClaimCycle(edges: List<Pair<ClaimReference, ClaimReference>>): Boolean = hasDirectedCycle(edges)

private fun <T> hasDirectedCycle(edges: List<Pair<T, T>>): Boolean {
    val adjacency = edges.groupBy({ it.first }, { it.second })
    val visiting = mutableSetOf<T>()
    val visited = mutableSetOf<T>()
    fun visit(node: T): Boolean {
        if (node in visiting) return true
        if (!visited.add(node)) return false
        visiting += node
        val cycle = adjacency[node].orEmpty().any(::visit)
        visiting -= node
        return cycle
    }
    return edges.flatMap { listOf(it.first, it.second) }.any(::visit)
}

private fun validateIdentityConsistency(
    links: List<EntityIdentityLink>,
    entityIds: Set<LifeEntityId>,
    issue: (String, String) -> Unit,
) {
    val establishedSame = links.filter { it.status == EntityIdentityStatus.ESTABLISHED_SAME_ENTITY }
    val establishedDifferent = links.filter { it.status == EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY }
    val parent = entityIds.associateWith { it }.toMutableMap()
    fun root(id: LifeEntityId): LifeEntityId {
        val p = parent[id] ?: return id
        if (p == id) return id
        val resolved = root(p)
        parent[id] = resolved
        return resolved
    }
    establishedSame.forEach { link ->
        val left = root(link.leftEntityId)
        val right = root(link.rightEntityId)
        if (left != right) parent[right] = left
    }
    establishedDifferent.forEach { link ->
        if (root(link.leftEntityId) == root(link.rightEntityId)) {
            issue("SELF_CONTRADICTORY_IDENTITY_LINKAGE", link.id.value)
        }
    }
    links.groupBy { it.canonicalPair() }.forEach { (pair, samePair) ->
        val statuses = samePair.map { it.status }.toSet()
        if (EntityIdentityStatus.ESTABLISHED_SAME_ENTITY in statuses && EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY in statuses) {
            issue("CONFLICTING_IDENTITY_LINKS", "${pair.first.value}:${pair.second.value}")
        }
    }
}
