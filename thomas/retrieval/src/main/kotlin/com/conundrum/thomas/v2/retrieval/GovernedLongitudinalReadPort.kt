package com.conundrum.thomas.v2.retrieval

import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalReader

/** Narrow governed reader adapter; it receives no admission authority or storage implementation. */
class GovernedLongitudinalReadPort(
    private val reader: LongitudinalReader,
    private val requiredClassification: StoreDataClassification,
    private val openQuestions: (Long) -> List<RetrievalOpenQuestion> = { emptyList() },
    private val recurrences: (Long) -> List<RetrievalRecurrenceCandidate> = { emptyList() },
) : LongitudinalRetrievalReadPort {
    init {
        require(reader.classification == requiredClassification)
    }

    override fun read(asOfRevision: Long): RetrievalArchiveSnapshot {
        val snapshot = reader.snapshot(asOfRevision)
        val currentRevisions = snapshot.sources.groupBy { it.stableSourceId }.values.mapNotNull { history ->
            history.maxByOrNull { it.provenance.sourceRevision }?.id
        }.toSet()
        val lifecycle = buildMap {
            snapshot.sources.forEach { source -> putMapped(StoredObjectType.SOURCE_REVISION, source.id.value, asOfRevision) }
            snapshot.assertions.forEach { assertion -> putMapped(StoredObjectType.ASSERTION, assertion.id.value, asOfRevision) }
            snapshot.entities.forEach { entity -> putMapped(StoredObjectType.ENTITY, entity.id.value, asOfRevision) }
            snapshot.hypotheses.forEach { hypothesis -> putMapped(StoredObjectType.HYPOTHESIS, hypothesis.id.value, asOfRevision) }
            snapshot.contradictions.forEach { relation -> putMapped(StoredObjectType.CONTRADICTION, relation.id.value, asOfRevision) }
            snapshot.corrections.forEach { relation -> putMapped(StoredObjectType.CORRECTION, relation.id.value, asOfRevision) }
        }
        return RetrievalArchiveSnapshot(
            storeRevision = asOfRevision,
            evidence = snapshot,
            lifecycle = lifecycle,
            currentSourceRevisionIds = currentRevisions,
            openQuestions = openQuestions(asOfRevision).sortedBy { it.id },
            recurrenceCandidates = recurrences(asOfRevision).sortedBy { it.id },
            canonicalArchiveDigest = reader.canonicalLogicalStateDigest(asOfRevision),
        )
    }

    private fun MutableMap<RetrievalObjectKey, RetrievalLifecycle>.putMapped(
        type: StoredObjectType,
        stableId: String,
        revision: Long,
    ) {
        val state = reader.lifecycle(LongitudinalObjectRef(type, stableId), revision) ?: return
        val retrievalType = when (type) {
            StoredObjectType.SOURCE_REVISION -> RetrievalObjectType.SOURCE_REVISION
            StoredObjectType.ASSERTION -> RetrievalObjectType.ASSERTION
            StoredObjectType.ENTITY -> RetrievalObjectType.ENTITY
            StoredObjectType.HYPOTHESIS -> RetrievalObjectType.HYPOTHESIS
            StoredObjectType.CONTRADICTION -> RetrievalObjectType.CONTRADICTION
            StoredObjectType.CORRECTION -> RetrievalObjectType.CORRECTION
            else -> return
        }
        put(
            RetrievalObjectKey(retrievalType, stableId),
            RetrievalLifecycle(
                status = when (state.status) {
                    LongitudinalLifecycleStatus.ACTIVE -> RetrievalLifecycleStatus.ACTIVE
                    LongitudinalLifecycleStatus.CONTESTED -> RetrievalLifecycleStatus.CONTESTED
                    LongitudinalLifecycleStatus.SUPERSEDED -> RetrievalLifecycleStatus.SUPERSEDED
                    LongitudinalLifecycleStatus.RETIRED -> RetrievalLifecycleStatus.RETIRED
                    LongitudinalLifecycleStatus.REVIEW_REQUIRED -> RetrievalLifecycleStatus.REVIEW_REQUIRED
                    LongitudinalLifecycleStatus.DEPENDENCY_BLOCKED -> RetrievalLifecycleStatus.DEPENDENCY_BLOCKED
                    LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE -> RetrievalLifecycleStatus.PRIVATE_INELIGIBLE
                    LongitudinalLifecycleStatus.DELETED -> RetrievalLifecycleStatus.AUDIT_ONLY
                },
                eligibleForOrdinaryUse = state.eligibleForOrdinaryUse,
                causeCode = state.causeCode,
                changedAtRevision = state.changedAtRevision,
            ),
        )
    }
}
