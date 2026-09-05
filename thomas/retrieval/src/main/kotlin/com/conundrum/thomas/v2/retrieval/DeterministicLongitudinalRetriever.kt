package com.conundrum.thomas.v2.retrieval

import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.DependencyRole
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.SourceSpanGrounding
import com.conundrum.thomas.v2.longitudinal.sourceTextSha256
import java.time.ZoneOffset

/** Read-only, deterministic retrieval. No cache and no write-capable collaborator exist. */
class DeterministicLongitudinalRetriever(
    private val readPort: LongitudinalRetrievalReadPort,
) {
    fun retrieve(request: RetrievalRequest): RetrievalResult {
        validate(request)?.let { return rejected(request, it.first, it.second) }
        val archive = try {
            readPort.read(request.snapshotRevision)
        } catch (_: RuntimeException) {
            return rejected(request, RetrievalDisposition.REVISION_UNAVAILABLE, "REQUESTED_REVISION_UNAVAILABLE")
        }
        if (archive.storeRevision != request.snapshotRevision) {
            return rejected(request, RetrievalDisposition.REVISION_UNAVAILABLE, "READ_PORT_REVISION_MISMATCH")
        }
        if (request.intent == RetrievalIntent.ORDINARY_MODE_CONTEXT && request.activeMode != RetrievalMode.THERAPY) {
            return empty(request, archive, "MODE_POLICY_DEFAULTS_TO_NO_HISTORICAL_RETRIEVAL")
        }

        val evidence = archive.evidence
        val sourceByRevision = evidence.sources.associateBy { it.id }
        val lifecycleExclusions = mutableSetOf<String>()
        val privateExclusions = mutableSetOf<String>()
        val supersededExclusions = mutableSetOf<String>()
        val assistantExclusions = mutableSetOf<String>()

        fun lifecycle(key: RetrievalObjectKey): RetrievalLifecycle = archive.lifecycle[key]
            ?: RetrievalLifecycle(RetrievalLifecycleStatus.ACTIVE, true, "DEFAULT_ACTIVE_FIXTURE_STATE", archive.storeRevision)

        fun sourceNormallyEligible(source: SourceRecord): Boolean {
            val state = lifecycle(key(RetrievalObjectType.SOURCE_REVISION, source.id.value))
            if (source.authorRole != SourceAuthorRole.USER) {
                assistantExclusions += source.id.value
                return false
            }
            if (state.status == RetrievalLifecycleStatus.PRIVATE_INELIGIBLE) {
                privateExclusions += source.id.value
                return false
            }
            if (source.id !in archive.currentSourceRevisionIds) {
                supersededExclusions += source.id.value
                return false
            }
            if (!state.eligibleForOrdinaryUse) {
                lifecycleExclusions += source.id.value
                return false
            }
            return true
        }

        val eligibleSources = evidence.sources.filter(::sourceNormallyEligible).associateBy { it.id }
        val ordinaryAssertions = evidence.assertions.filter { assertion ->
            val state = lifecycle(key(RetrievalObjectType.ASSERTION, assertion.id.value))
            val sourceEligible = assertion.sourceRecordId in eligibleSources
            if (!state.eligibleForOrdinaryUse) {
                if (state.status == RetrievalLifecycleStatus.PRIVATE_INELIGIBLE) privateExclusions += assertion.id.value
                else lifecycleExclusions += assertion.id.value
            }
            state.eligibleForOrdinaryUse && sourceEligible
        }
        val ordinaryAssertionIds = ordinaryAssertions.map { it.id }.toSet()
        val ordinaryEntities = evidence.entities.filter { entity ->
            val state = lifecycle(key(RetrievalObjectType.ENTITY, entity.id.value))
            val supported = entity.supportingAssertionIds.any { it in ordinaryAssertionIds }
            if (!state.eligibleForOrdinaryUse) lifecycleExclusions += entity.id.value
            state.eligibleForOrdinaryUse && supported
        }
        fun dependenciesEligible(hypothesisId: com.conundrum.thomas.v2.longitudinal.HypothesisId, visiting: Set<String> = emptySet()): Boolean {
            if (hypothesisId.value in visiting) return false
            val dependencies = evidence.hypothesisDependencies.filter { it.dependentHypothesisId == hypothesisId }
            if (dependencies.isEmpty()) return false
            return dependencies.all { dependency ->
                when (val prerequisite = dependency.prerequisite) {
                    is ClaimReference.Assertion -> prerequisite.assertionId in ordinaryAssertionIds
                    is ClaimReference.Hypothesis -> {
                        val prerequisiteHypothesis = evidence.hypotheses.firstOrNull { it.id == prerequisite.hypothesisId }
                            ?: return@all false
                        val prerequisiteState = lifecycle(key(RetrievalObjectType.HYPOTHESIS, prerequisite.hypothesisId.value))
                        prerequisiteState.eligibleForOrdinaryUse &&
                            prerequisiteHypothesis.status !in setOf(HypothesisStatus.REQUIRES_REVIEW, HypothesisStatus.SUPERSEDED) &&
                            dependenciesEligible(prerequisite.hypothesisId, visiting + hypothesisId.value)
                    }
                }
            }
        }
        val ordinaryHypotheses = evidence.hypotheses.filter { hypothesis ->
            val state = lifecycle(key(RetrievalObjectType.HYPOTHESIS, hypothesis.id.value))
            val statusEligible = hypothesis.status !in setOf(HypothesisStatus.REQUIRES_REVIEW, HypothesisStatus.SUPERSEDED)
            val dependenciesEligible = dependenciesEligible(hypothesis.id)
            if (!state.eligibleForOrdinaryUse || !statusEligible || !dependenciesEligible) lifecycleExclusions += hypothesis.id.value
            state.eligibleForOrdinaryUse && statusEligible && dependenciesEligible
        }

        val resolvedAnchorEntities = resolvedEntityClosure(
            request.anchors.entityIds + request.anchors.eventIds + request.anchors.relationshipIds + request.anchors.periodIds,
            evidence.identityLinks,
        )
        val unresolvedEntities = evidence.identityLinks
            .filter { it.status in setOf(EntityIdentityStatus.UNRESOLVED, EntityIdentityStatus.CANDIDATE_SAME_ENTITY) }
            .flatMap { listOf(it.leftEntityId, it.rightEntityId) }.toSet()
        val lexical = DeterministicLexicalPolicy.normalizeTerms(request.anchors.lexicalTerms)
        val directClaims = directClaimsForRequest(request, evidence)
        val candidates = mutableListOf<RetrievedLongitudinalItem>()

        if (request.intent in setOf(RetrievalIntent.EXPLICIT_LOOK_BACK, RetrievalIntent.EXPLICIT_SOURCE_RECALL)) {
            evidence.sources.sortedBy { it.id }.forEach { source ->
                val explicitlyNamed = source.stableSourceId in request.anchors.sourceIdentityIds ||
                    source.id in request.anchors.sourceRevisionIds
                val auditAllowed = request.intent == RetrievalIntent.EXPLICIT_SOURCE_RECALL && explicitlyNamed &&
                    lifecycle(key(RetrievalObjectType.SOURCE_REVISION, source.id.value)).status != RetrievalLifecycleStatus.PRIVATE_INELIGIBLE &&
                    source.authorRole == SourceAuthorRole.USER
                if (!sourceNormallyEligible(source) && !auditAllowed) return@forEach
                if (request.intent == RetrievalIntent.EXPLICIT_LOOK_BACK &&
                    source.provenance.acquisitionMode != com.conundrum.thomas.v2.longitudinal.AcquisitionMode.JOURNAL
                ) return@forEach
                val body = (source.originalContent as? OriginalSourceContent.Inline)?.exactContent.orEmpty()
                val lexicalMatches = DeterministicLexicalPolicy.matchCount(body, lexical)
                if (!explicitlyNamed && (request.intent != RetrievalIntent.EXPLICIT_LOOK_BACK || lexicalMatches == 0)) return@forEach
                val reasons = buildList {
                    if (explicitlyNamed) add(RetrievalReason.EXPLICIT_SOURCE_RECALL)
                    if (lexicalMatches > 0) add(RetrievalReason.EXPLICIT_LOOK_BACK_LEXICAL_MATCH)
                }
                candidates += item(
                    RetrievalItemKind.SOURCE, source.id.value, RetrievedPayload.Source(source),
                    lifecycle(key(RetrievalObjectType.SOURCE_REVISION, source.id.value)),
                    source.id in archive.currentSourceRevisionIds, null, source.eventTime, source.reportTime,
                    source.provenance.acquisitionMode, listOf(source.id), emptyList(), false, reasons,
                    rank(request, source.id.value, reasons, lexicalMatches, source.eventTime, source.provenance.acquisitionMode),
                )
            }
        }

        ordinaryAssertions.sortedBy { it.id }.forEach { assertion ->
            val source = requireNotNull(eligibleSources[assertion.sourceRecordId])
            val entityIds = assertion.entityReferences()
            val reasons = reasonsFor(
                request, assertion.id.value, assertion.sourceRecordId, assertion.predicate.conceptId,
                entityIds, assertion.eventTime, assertion.searchableText(), directClaims, resolvedAnchorEntities,
            )
            if (!isCandidate(request, reasons)) return@forEach
            val lexicalMatches = DeterministicLexicalPolicy.matchCount(assertion.searchableText(), lexical)
            candidates += item(
                RetrievalItemKind.ASSERTION, assertion.id.value, RetrievedPayload.Assertion(assertion),
                lifecycle(key(RetrievalObjectType.ASSERTION, assertion.id.value)), true, assertion.epistemicClass,
                assertion.eventTime, source.reportTime, source.provenance.acquisitionMode, listOf(assertion.sourceRecordId),
                entityIds.sorted(), entityIds.any { it in unresolvedEntities }, reasons,
                rank(request, assertion.id.value, reasons, lexicalMatches, assertion.eventTime, source.provenance.acquisitionMode),
            )
        }

        ordinaryEntities.sortedBy { it.id }.forEach { entity ->
            val reasons = mutableListOf<RetrievalReason>()
            if (entity.id in request.anchors.entityIds) reasons += RetrievalReason.EXPLICIT_TARGET
            if (entity.id in request.anchors.eventIds) reasons += RetrievalReason.SAME_EVENT
            if (entity.id in request.anchors.relationshipIds) reasons += RetrievalReason.SAME_RELATIONSHIP
            if (entity.id in request.anchors.periodIds) reasons += RetrievalReason.SAME_PERIOD
            if (entity.id in resolvedAnchorEntities && reasons.isEmpty()) reasons += RetrievalReason.SAME_RESOLVED_ENTITY
            if (reasons.isEmpty()) return@forEach
            val entitySourceIds = entity.supportingAssertionIds.mapNotNull { id ->
                evidence.assertions.firstOrNull { it.id == id }?.sourceRecordId
            }.distinct().sorted()
            // A single-source entity can carry that source's provenance without inventing a
            // cross-source attribution. Multi-source entities remain explicitly unattributed.
            val singleEntitySource = entitySourceIds.singleOrNull()?.let(eligibleSources::get)
            candidates += item(
                RetrievalItemKind.ENTITY, entity.id.value, RetrievedPayload.Entity(entity),
                lifecycle(key(RetrievalObjectType.ENTITY, entity.id.value)), true, null, null,
                singleEntitySource?.reportTime, singleEntitySource?.provenance?.acquisitionMode,
                entitySourceIds,
                listOf(entity.id), entity.id in unresolvedEntities, reasons.distinct(),
                rank(request, entity.id.value, reasons, 0, null, null),
            )
        }

        ordinaryHypotheses.sortedBy { it.id }.forEach { hypothesis ->
            val explicitlyNamed = hypothesis.id in request.anchors.hypothesisIds
            val hypothesisEntities = hypothesis.entityReferences()
            val reasons = mutableListOf<RetrievalReason>()
            if (explicitlyNamed) reasons += RetrievalReason.EXPLICIT_TARGET
            if (hypothesis.predicate.conceptId in request.anchors.predicateIds) reasons += RetrievalReason.SAME_PREDICATE
            if (hypothesisEntities.any { it in resolvedAnchorEntities }) reasons += RetrievalReason.SAME_RESOLVED_ENTITY
            if (request.intent == RetrievalIntent.EXPLAIN_DERIVED_OBJECT && explicitlyNamed) {
                reasons += RetrievalReason.EXPLANATION_AUDIT_TARGET
            }
            if (reasons.isEmpty()) return@forEach
            candidates += item(
                RetrievalItemKind.HYPOTHESIS, hypothesis.id.value, RetrievedPayload.Hypothesis(hypothesis),
                lifecycle(key(RetrievalObjectType.HYPOTHESIS, hypothesis.id.value)), true, null, null, null, null,
                emptyList(), hypothesisEntities.sorted(), hypothesisEntities.any { it in unresolvedEntities }, reasons.distinct(),
                rank(request, hypothesis.id.value, reasons, 0, null, null),
            )
        }

        if (request.intent == RetrievalIntent.EXPLAIN_DERIVED_OBJECT) {
            request.anchors.hypothesisIds.sorted().forEach { id ->
                if (candidates.any { it.kind == RetrievalItemKind.HYPOTHESIS && it.stableId == id.value }) return@forEach
                val hypothesis = evidence.hypotheses.firstOrNull { it.id == id } ?: return@forEach
                val state = lifecycle(key(RetrievalObjectType.HYPOTHESIS, id.value))
                if (state.status == RetrievalLifecycleStatus.PRIVATE_INELIGIBLE) {
                    privateExclusions += id.value
                    return@forEach
                }
                if (state.status !in setOf(
                        RetrievalLifecycleStatus.RETIRED,
                        RetrievalLifecycleStatus.SUPERSEDED,
                        RetrievalLifecycleStatus.REVIEW_REQUIRED,
                        RetrievalLifecycleStatus.DEPENDENCY_BLOCKED,
                        RetrievalLifecycleStatus.AUDIT_ONLY,
                    ) && !dependenciesEligible(id)
                ) {
                    lifecycleExclusions += id.value
                    return@forEach
                }
                val reasons = listOf(RetrievalReason.EXPLICIT_TARGET, RetrievalReason.EXPLANATION_AUDIT_TARGET)
                candidates += item(
                    RetrievalItemKind.HYPOTHESIS, id.value, RetrievedPayload.Hypothesis(hypothesis), state, false,
                    null, null, null, null, emptyList(), hypothesis.entityReferences().sorted(), false, reasons,
                    rank(request, id.value, reasons, 0, null, null),
                )
            }
        }

        val candidateIds = candidates.map { it.stableId }.toMutableSet()
        fun addAssertionAsNeighborhood(assertion: EvidenceAssertion, reason: RetrievalReason) {
            if (assertion.id !in ordinaryAssertionIds) return
            val existingIndex = candidates.indexOfFirst {
                it.kind == RetrievalItemKind.ASSERTION && it.stableId == assertion.id.value
            }
            if (existingIndex >= 0) {
                val existing = candidates[existingIndex]
                candidates[existingIndex] = existing.copy(reasons = (existing.reasons + reason).distinct())
                return
            }
            if (!candidateIds.add(assertion.id.value)) return
            val source = requireNotNull(eligibleSources[assertion.sourceRecordId])
            val reasons = listOf(reason)
            candidates += item(
                RetrievalItemKind.ASSERTION, assertion.id.value, RetrievedPayload.Assertion(assertion),
                lifecycle(key(RetrievalObjectType.ASSERTION, assertion.id.value)), true, assertion.epistemicClass,
                assertion.eventTime, source.reportTime, source.provenance.acquisitionMode,
                listOf(assertion.sourceRecordId), assertion.entityReferences().sorted(),
                assertion.entityReferences().any { it in unresolvedEntities }, reasons,
                rank(request, assertion.id.value, reasons, 0, assertion.eventTime, source.provenance.acquisitionMode),
            )
        }

        // Every candidate hypothesis carries its direct evidentiary neighborhood. This is
        // not limited to the explanation intent: an ordinary packet may not present a
        // derived object without the support and known counterevidence needed to read it
        // honestly. The final selector keeps the group together or omits the hypothesis.
        candidates.mapNotNull { candidate ->
            (candidate.payload as? RetrievedPayload.Hypothesis)?.value?.id
        }.distinct().forEach { hypothesisId ->
                evidence.hypothesisDependencies.filter { it.dependentHypothesisId == hypothesisId }
                    .sortedWith(compareBy({ it.role != DependencyRole.WEAKENS }, { it.id }))
                    .forEach { dependency ->
                        val assertionId = (dependency.prerequisite as? ClaimReference.Assertion)?.assertionId ?: return@forEach
                        val assertion = evidence.assertions.firstOrNull { it.id == assertionId } ?: return@forEach
                        addAssertionAsNeighborhood(
                            assertion,
                            if (dependency.role == DependencyRole.WEAKENS) RetrievalReason.REPRESENTATIVE_COUNTEREVIDENCE
                            else RetrievalReason.DIRECT_EVIDENCE,
                        )
                    }
        }

        evidence.contradictions.sortedBy { it.id }.forEach { relation ->
            val endpoints = setOf(relation.leftAssertionId, relation.rightAssertionId)
            val relevant = relation.id.value in request.anchors.openQuestionIds ||
                endpoints.any { it in request.anchors.assertionIds || it.value in directClaims } ||
                candidates.any { it.kind == RetrievalItemKind.ASSERTION && it.stableId in endpoints.map { id -> id.value } }
            if (!relevant || !endpoints.all { it in ordinaryAssertionIds }) return@forEach
            endpoints.mapNotNull { id -> evidence.assertions.firstOrNull { it.id == id } }
                .forEach { addAssertionAsNeighborhood(it, RetrievalReason.ACTIVE_CONTRADICTION) }
            val reasons = listOf(RetrievalReason.ACTIVE_CONTRADICTION)
            if (candidateIds.add(relation.id.value)) candidates += item(
                RetrievalItemKind.CONTRADICTION, relation.id.value, RetrievedPayload.Contradiction(relation),
                lifecycle(key(RetrievalObjectType.CONTRADICTION, relation.id.value)), true, null, null, null, null,
                endpoints.mapNotNull { id -> evidence.assertions.firstOrNull { it.id == id }?.sourceRecordId }.sorted(),
                emptyList(), false, reasons, rank(request, relation.id.value, reasons, 0, null, null),
            )
        }

        evidence.corrections.sortedBy { it.id }.forEach { relation ->
            val relevant = relation.correctingAssertionId in request.anchors.assertionIds ||
                relation.correctedAssertionId in request.anchors.assertionIds ||
                candidates.any { it.stableId == relation.correctingAssertionId.value }
            if (!relevant || relation.correctingAssertionId !in ordinaryAssertionIds) return@forEach
            val correcting = evidence.assertions.first { it.id == relation.correctingAssertionId }
            addAssertionAsNeighborhood(correcting, RetrievalReason.CURRENT_CORRECTION)
            val reasons = listOf(RetrievalReason.CURRENT_CORRECTION)
            if (candidateIds.add(relation.id.value)) candidates += item(
                RetrievalItemKind.CORRECTION, relation.id.value, RetrievedPayload.Correction(relation),
                lifecycle(key(RetrievalObjectType.CORRECTION, relation.id.value)), true, null, null, null, null,
                listOf(correcting.sourceRecordId), emptyList(), false, reasons,
                rank(request, relation.id.value, reasons, 0, correcting.eventTime, null),
            )
        }

        archive.openQuestions.sortedBy { it.id }.forEach { question ->
            val explicit = question.id in request.anchors.openQuestionIds ||
                question.basisIds.any(request.anchors.openQuestionIds::contains)
            if (!explicit) return@forEach
            val reasons = listOf(RetrievalReason.EXPLICIT_TARGET)
            candidates += item(
                RetrievalItemKind.OPEN_QUESTION, question.id, RetrievedPayload.OpenQuestion(question), active(archive),
                true, null, null, null, null, emptyList(), emptyList(), false, reasons,
                rank(request, question.id, reasons, 0, null, null),
            )
        }

        archive.recurrenceCandidates.sortedBy { it.id }.forEach { recurrence ->
            if (recurrence.conceptId !in request.anchors.predicateIds &&
                recurrence.assertionIds.none { it in request.anchors.assertionIds }
            ) return@forEach
            val reasons = listOf(RetrievalReason.SAME_PREDICATE)
            candidates += item(
                RetrievalItemKind.RECURRENCE, recurrence.id, RetrievedPayload.Recurrence(recurrence), active(archive),
                true, null, null, null, null, emptyList(), emptyList(), false, reasons,
                rank(request, recurrence.id, reasons, 0, null, null),
            )
        }

        val distinctCandidates = candidates.distinctBy { it.kind to it.stableId }.sortedBy { it.rank }
        val selected = selectWithNeighborhoodBalance(distinctCandidates, evidence, request.budget)
        val invalidGrounding = mutableSetOf<String>()
        val excerpts = selected.mapNotNull { selectedItem ->
            excerptProposal(selectedItem, sourceByRevision, ::lifecycle, invalidGrounding)
        }.distinctBy {
            listOf(it.sourceRevisionId.value, it.grounding.startOffsetInclusive, it.grounding.endOffsetExclusive)
        }
        val exclusions = RetrievalExclusions(
            privateCount = privateExclusions.size,
            lifecycleCount = lifecycleExclusions.size,
            supersededRevisionCount = supersededExclusions.size,
            assistantAuthoredCount = assistantExclusions.size,
            invalidGroundingCount = invalidGrounding.size,
            budgetCount = (distinctCandidates.size - selected.size).coerceAtLeast(0),
        )
        return RetrievalResult(
            if (selected.isEmpty()) RetrievalDisposition.EMPTY else RetrievalDisposition.RETRIEVED,
            request, archive.canonicalArchiveDigest, distinctCandidates.size, selected, excerpts, exclusions,
            if (selected.isEmpty()) listOf("NO_ELIGIBLE_RELEVANT_ITEM") else listOf("PURPOSE_BOUND_SELECTION_COMPLETE"),
        )
    }

    private fun validate(request: RetrievalRequest): Pair<RetrievalDisposition, String>? = when {
        request.authority == RetrievalAuthority.NOT_AUTHORIZED ->
            RetrievalDisposition.REJECTED_AUTHORITY to "PRODUCTION_RETRIEVAL_AUTHORITY_NOT_GRANTED"
        request.policyVersion != CT_V2_11_RETRIEVAL_POLICY_VERSION ->
            RetrievalDisposition.REJECTED_INVALID_REQUEST to "UNSUPPORTED_RETRIEVAL_POLICY_VERSION"
        request.intent == RetrievalIntent.EXPLICIT_LOOK_BACK &&
            (request.activeMode != RetrievalMode.JOURNAL || !request.explicitlyUserDirected) ->
            RetrievalDisposition.REJECTED_INVALID_REQUEST to "LOOK_BACK_REQUIRES_EXPLICIT_JOURNAL_DIRECTION"
        request.intent == RetrievalIntent.BIOGRAPHER_TARGET_CONTEXT &&
            (request.activeMode != RetrievalMode.BIOGRAPHER || request.explicitTargetId == null) ->
            RetrievalDisposition.REJECTED_INVALID_REQUEST to "BIOGRAPHER_CONTEXT_REQUIRES_EXISTING_TARGET"
        request.intent == RetrievalIntent.EXPLAIN_DERIVED_OBJECT && request.anchors.hypothesisIds.isEmpty() ->
            RetrievalDisposition.REJECTED_INVALID_REQUEST to "EXPLANATION_REQUIRES_EXPLICIT_DERIVED_OBJECT"
        request.intent == RetrievalIntent.EXPLICIT_SOURCE_RECALL &&
            (!request.explicitlyUserDirected ||
                request.anchors.sourceIdentityIds.isEmpty() && request.anchors.sourceRevisionIds.isEmpty()) ->
            RetrievalDisposition.REJECTED_INVALID_REQUEST to "SOURCE_RECALL_REQUIRES_EXPLICIT_SOURCE"
        else -> null
    }

    private fun reasonsFor(
        request: RetrievalRequest,
        stableId: String,
        sourceRevisionId: com.conundrum.thomas.v2.longitudinal.SourceRecordId,
        predicateId: com.conundrum.thomas.v2.longitudinal.PersonalConceptId,
        entityIds: Set<LifeEntityId>,
        eventTime: EventTime,
        text: String,
        directClaims: Set<String>,
        resolvedAnchorEntities: Set<LifeEntityId>,
    ): List<RetrievalReason> = buildList {
        if (stableId in request.anchors.assertionIds.map { it.value } ||
            sourceRevisionId in request.anchors.sourceRevisionIds
        ) add(RetrievalReason.EXPLICIT_TARGET)
        if (stableId in directClaims) add(
            if (request.intent == RetrievalIntent.BIOGRAPHER_TARGET_CONTEXT) RetrievalReason.BIOGRAPHER_TARGET_DEPENDENCY
            else RetrievalReason.DIRECT_EVIDENCE,
        )
        if (entityIds.any { it in resolvedAnchorEntities }) add(RetrievalReason.SAME_RESOLVED_ENTITY)
        if (entityIds.any { it in request.anchors.eventIds }) add(RetrievalReason.SAME_EVENT)
        if (entityIds.any { it in request.anchors.relationshipIds }) add(RetrievalReason.SAME_RELATIONSHIP)
        if (entityIds.any { it in request.anchors.periodIds }) add(RetrievalReason.SAME_PERIOD)
        if (predicateId in request.anchors.predicateIds) add(RetrievalReason.SAME_PREDICATE)
        val lexicalMatches = DeterministicLexicalPolicy.matchCount(
            text,
            DeterministicLexicalPolicy.normalizeTerms(request.anchors.lexicalTerms),
        )
        if (request.intent == RetrievalIntent.EXPLICIT_LOOK_BACK && lexicalMatches > 0) {
            add(RetrievalReason.EXPLICIT_LOOK_BACK_LEXICAL_MATCH)
        }
        if (temporalMatch(eventTime, request.anchors.temporalBounds)) add(RetrievalReason.TEMPORAL_MATCH)
    }.distinct()

    private fun isCandidate(request: RetrievalRequest, reasons: List<RetrievalReason>): Boolean {
        if (reasons.isEmpty()) return false
        if (request.intent == RetrievalIntent.EXPLICIT_LOOK_BACK) return true
        return reasons.any { it != RetrievalReason.EXPLICIT_LOOK_BACK_LEXICAL_MATCH }
    }

    private fun directClaimsForRequest(
        request: RetrievalRequest,
        evidence: com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot,
    ): Set<String> {
        val result = request.anchors.assertionIds.map { it.value }.toMutableSet()
        request.anchors.hypothesisIds.forEach { hypothesisId ->
            evidence.hypothesisDependencies.filter { it.dependentHypothesisId == hypothesisId }.forEach {
                result += when (val prerequisite = it.prerequisite) {
                    is ClaimReference.Assertion -> prerequisite.assertionId.value
                    is ClaimReference.Hypothesis -> prerequisite.hypothesisId.value
                }
            }
        }
        if (request.intent == RetrievalIntent.BIOGRAPHER_TARGET_CONTEXT) result += request.anchors.openQuestionIds
        return result
    }

    private fun selectWithNeighborhoodBalance(
        candidates: List<RetrievedLongitudinalItem>,
        evidence: com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot,
        budget: ContextBudget,
    ): List<RetrievedLongitudinalItem> {
        val byId = candidates.associateBy { it.stableId }
        val selected = linkedMapOf<String, RetrievedLongitudinalItem>()
        candidates.forEach { candidate ->
            if (candidate.stableId in selected) return@forEach
            if (budget.maximumDependencyDepth == 0 &&
                candidate.kind in setOf(RetrievalItemKind.HYPOTHESIS, RetrievalItemKind.CONTRADICTION)
            ) return@forEach
            val required = when (val payload = candidate.payload) {
                is RetrievedPayload.Hypothesis -> {
                    val dependencies = evidence.hypothesisDependencies.filter { it.dependentHypothesisId == payload.value.id }
                    val support = dependencies.filter { it.role != DependencyRole.WEAKENS }
                        .mapNotNull { it.prerequisite.stableIdOrNull()?.let(byId::get) }.take(1)
                    val counter = dependencies.filter { it.role == DependencyRole.WEAKENS }
                        .mapNotNull { it.prerequisite.stableIdOrNull()?.let(byId::get) }.take(1)
                    listOf(candidate) + support + counter
                }
                is RetrievedPayload.Contradiction -> listOf(candidate) + listOf(
                    byId[payload.value.leftAssertionId.value],
                    byId[payload.value.rightAssertionId.value],
                ).filterNotNull()
                else -> listOf(candidate)
            }.distinctBy { it.stableId }
            if (selected.size + required.count { it.stableId !in selected } > budget.maximumLongitudinalObjects) {
                return@forEach
            }
            required.forEach { selected.putIfAbsent(it.stableId, it) }
        }
        return selected.values.toList().take(budget.maximumLongitudinalObjects)
    }

    private fun excerptProposal(
        item: RetrievedLongitudinalItem,
        sourceByRevision: Map<com.conundrum.thomas.v2.longitudinal.SourceRecordId, SourceRecord>,
        lifecycle: (RetrievalObjectKey) -> RetrievalLifecycle,
        invalid: MutableSet<String>,
    ): SourceExcerptProposal? {
        val assertion = (item.payload as? RetrievedPayload.Assertion)?.value
        if (assertion != null) {
            val grounding = assertion.sourceGrounding ?: return null
            val source = sourceByRevision[grounding.sourceRevisionId]
            if (source == null || !grounding.isExactFor(source)) {
                invalid += item.stableId
                return null
            }
            return SourceExcerptProposal(
                item.stableId, source.stableSourceId, source.id, source.provenance.acquisitionMode,
                source.reportTime, assertion.eventTime, grounding, assertion.epistemicClass,
                lifecycle(key(RetrievalObjectType.SOURCE_REVISION, source.id.value)), item.reasons.first(),
            )
        }
        val source = (item.payload as? RetrievedPayload.Source)?.value ?: return null
        val body = (source.originalContent as? OriginalSourceContent.Inline)?.exactContent ?: return null
        val grounding = SourceSpanGrounding(
            source.id, 0, body.length, body, sourceTextSha256(body), "ct-v2-11.source-recall.v1",
        )
        return SourceExcerptProposal(
            item.stableId, source.stableSourceId, source.id, source.provenance.acquisitionMode,
            source.reportTime, source.eventTime, grounding, EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION,
            lifecycle(key(RetrievalObjectType.SOURCE_REVISION, source.id.value)), item.reasons.first(),
        )
    }

    private fun item(
        kind: RetrievalItemKind,
        id: String,
        payload: RetrievedPayload,
        lifecycle: RetrievalLifecycle,
        current: Boolean,
        epistemic: EvidenceEpistemicClass?,
        time: EventTime?,
        reportTime: com.conundrum.thomas.v2.longitudinal.ReportTime?,
        acquisition: com.conundrum.thomas.v2.longitudinal.AcquisitionMode?,
        sources: List<com.conundrum.thomas.v2.longitudinal.SourceRecordId>,
        entities: List<LifeEntityId>,
        unresolved: Boolean,
        reasons: List<RetrievalReason>,
        rank: RelevanceRank,
    ) = RetrievedLongitudinalItem(
        kind, id, payload, lifecycle, current && lifecycle.eligibleForOrdinaryUse, epistemic,
        when (payload) {
            is RetrievedPayload.Assertion -> payload.value.uncertainty.name
            is RetrievedPayload.Hypothesis -> payload.value.status.name
            else -> null
        },
        time, reportTime, acquisition, sources.distinct().sorted(), entities.distinct().sorted(),
        unresolved, reasons.distinct(), rank,
    )

    private fun rank(
        request: RetrievalRequest,
        id: String,
        reasons: List<RetrievalReason>,
        lexicalMatches: Int,
        eventTime: EventTime?,
        acquisitionMode: com.conundrum.thomas.v2.longitudinal.AcquisitionMode?,
    ) = RelevanceRank(
        penalty(RetrievalReason.EXPLICIT_TARGET in reasons || RetrievalReason.EXPLICIT_SOURCE_RECALL in reasons),
        penalty(RetrievalReason.DIRECT_EVIDENCE in reasons || RetrievalReason.BIOGRAPHER_TARGET_DEPENDENCY in reasons),
        penalty(RetrievalReason.SAME_RESOLVED_ENTITY in reasons),
        penalty(RetrievalReason.SAME_EVENT in reasons),
        penalty(RetrievalReason.SAME_RELATIONSHIP in reasons),
        penalty(RetrievalReason.SAME_PERIOD in reasons),
        penalty(RetrievalReason.SAME_PREDICATE in reasons),
        -lexicalMatches,
        temporalDistance(eventTime, request.anchors.temporalBounds),
        modePenalty(request.activeMode, acquisitionMode),
        id,
    )

    private fun modePenalty(
        mode: RetrievalMode,
        acquisition: com.conundrum.thomas.v2.longitudinal.AcquisitionMode?,
    ): Int = when (mode) {
        RetrievalMode.JOURNAL -> if (acquisition == com.conundrum.thomas.v2.longitudinal.AcquisitionMode.JOURNAL) 0 else 1
        RetrievalMode.BIOGRAPHER -> if (acquisition in setOf(
                com.conundrum.thomas.v2.longitudinal.AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE,
                com.conundrum.thomas.v2.longitudinal.AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE,
            )
        ) 0 else 1
        RetrievalMode.THERAPY -> 0
    }

    private fun resolvedEntityClosure(
        anchors: Set<LifeEntityId>,
        links: List<com.conundrum.thomas.v2.longitudinal.EntityIdentityLink>,
    ): Set<LifeEntityId> {
        val result = anchors.toMutableSet()
        var changed = true
        val establishedSame = links.filter { it.status == EntityIdentityStatus.ESTABLISHED_SAME_ENTITY }
        while (changed) {
            changed = false
            establishedSame.forEach { link ->
                if (link.leftEntityId in result && result.add(link.rightEntityId)) changed = true
                if (link.rightEntityId in result && result.add(link.leftEntityId)) changed = true
            }
        }
        return result
    }

    private fun temporalMatch(time: EventTime, anchors: List<EventTime>): Boolean =
        anchors.any { anchor -> temporalDistance(time, listOf(anchor)) == 0 }

    private fun temporalDistance(time: EventTime?, anchors: List<EventTime>): Int {
        if (time == null || anchors.isEmpty()) return 10_000
        val window = time.yearWindow() ?: return 9_000
        return anchors.mapNotNull { it.yearWindow() }.minOfOrNull { other ->
            when {
                window.second < other.first -> other.first - window.second
                other.second < window.first -> window.first - other.second
                else -> 0
            }
        } ?: 9_000
    }

    private fun EventTime.yearWindow(): Pair<Int, Int>? = when (this) {
        is EventTime.ExactInstant -> value.atZone(ZoneOffset.UTC).year.let { it to it }
        is EventTime.CalendarDate -> value.year.let { it to it }
        is EventTime.ApproximateDate -> center.year.let { it to it }
        is EventTime.ApproximateYear -> year.value.let { it to it }
        is EventTime.Range -> start.earliest.year to end.latest.year
        is EventTime.OngoingInterval -> knownStart?.earliest?.year?.let { it to Int.MAX_VALUE }
        else -> null
    }

    private fun EvidenceAssertion.entityReferences(): Set<LifeEntityId> = buildSet {
        (subject as? AssertionSubject.Entity)?.let { add(it.entityId) }
        when (val objectValue = value) {
            is AssertionValue.EntityReference -> add(objectValue.entityId)
            is AssertionValue.EntityReferences -> addAll(objectValue.entityIds)
            else -> Unit
        }
    }

    private fun com.conundrum.thomas.v2.longitudinal.ThomasHypothesis.entityReferences(): Set<LifeEntityId> = buildSet {
        (subject as? AssertionSubject.Entity)?.let { add(it.entityId) }
        when (val objectValue = proposedValue) {
            is AssertionValue.EntityReference -> add(objectValue.entityId)
            is AssertionValue.EntityReferences -> addAll(objectValue.entityIds)
            else -> Unit
        }
    }

    private fun EvidenceAssertion.searchableText(): String = buildString {
        append(predicate.conceptId.value).append(' ')
        when (val objectValue = value) {
            is AssertionValue.Text -> append(objectValue.value)
            is AssertionValue.ConceptValue -> append(objectValue.conceptId.value)
            else -> Unit
        }
    }

    private fun SourceSpanGrounding.isExactFor(source: SourceRecord): Boolean {
        if (source.id != sourceRevisionId) return false
        val body = (source.originalContent as? OriginalSourceContent.Inline)?.exactContent ?: return false
        if (sourceTextSha256(body) != sourceRevisionSha256 || endOffsetExclusive > body.length) return false
        return body.substring(startOffsetInclusive, endOffsetExclusive) == exactFragment
    }

    private fun ClaimReference.stableIdOrNull(): String? = when (this) {
        is ClaimReference.Assertion -> assertionId.value
        is ClaimReference.Hypothesis -> hypothesisId.value
    }

    private fun key(type: RetrievalObjectType, id: String) = RetrievalObjectKey(type, id)
    private fun penalty(match: Boolean) = if (match) 0 else 1
    private fun active(archive: RetrievalArchiveSnapshot) = RetrievalLifecycle(
        RetrievalLifecycleStatus.ACTIVE, true, "STRUCTURAL_STATE_ACTIVE", archive.storeRevision,
    )

    private fun empty(request: RetrievalRequest, archive: RetrievalArchiveSnapshot, reason: String) = RetrievalResult(
        RetrievalDisposition.EMPTY, request, archive.canonicalArchiveDigest, 0, emptyList(), emptyList(),
        RetrievalExclusions(), listOf(reason),
    )

    private fun rejected(request: RetrievalRequest, disposition: RetrievalDisposition, reason: String) = RetrievalResult(
        disposition, request, "0".repeat(64), 0, emptyList(), emptyList(), RetrievalExclusions(), listOf(reason),
    )
}
