package com.conundrum.thomas.v2.therapylongitudinal

import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.retrieval.RetrievalItemKind
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalReason

/**
 * Read-only memory-use policy. It receives an already selected CT-V2-05 action and cannot replace
 * it. Packet order is CT-V2-11 rank order; this gate only narrows renderer visibility.
 */
class DeterministicTherapyMemoryUseGate {
    fun decide(request: TherapyMemoryGateRequest): TherapyMemoryUseDecision = when (request.memoryIntent) {
        TherapyMemoryIntent.ORDINARY -> ordinary(request)
        TherapyMemoryIntent.EXPLICIT_RECALL -> explicitRecall(request)
        TherapyMemoryIntent.EXPLAIN_THOMAS_VIEW -> explanation(request)
    }

    private fun ordinary(request: TherapyMemoryGateRequest): TherapyMemoryUseDecision {
        if (!ordinaryActionAllowsMemory(request)) {
            return decision(
                TherapyMemoryUseDisposition.SUPPRESSED_BY_POLICY,
                emptyList(),
                request.sessionState,
                "SELECTED_THERAPY_ACT_DOES_NOT_AUTHORIZE_MEMORY_SUPPORT",
            )
        }
        val packet = request.packet
        if (packet.longitudinal.items.isEmpty()) {
            return decision(
                TherapyMemoryUseDisposition.NO_RELEVANT_MEMORY,
                emptyList(),
                request.sessionState,
                "NO_ELIGIBLE_RELEVANT_HISTORICAL_OBJECT",
            )
        }
        val candidate = packet.longitudinal.items.firstOrNull { item ->
            item.currentAuthority &&
                item.kind != RetrievalItemKind.HYPOTHESIS &&
                item.kind != RetrievalItemKind.CONTRADICTION &&
                item.lifecycle in setOf(RetrievalLifecycleStatus.ACTIVE) &&
                !item.unresolvedIdentity &&
                !hasKnownContradiction(item.stableId, packet) &&
                ordinaryRelation(item.retrievedBecause, item.kind) != null &&
                item.stableId !in request.directContinuationObjectIds &&
                notSuppressedBySession(item.stableId, request)
        }
        if (candidate == null) {
            return decision(
                TherapyMemoryUseDisposition.CONTEXT_AVAILABLE_NOT_SURFACED,
                emptyList(),
                request.sessionState,
                "AVAILABLE_CONTEXT_DID_NOT_PASS_ORDINARY_SURFACING_GATE",
            )
        }
        val relation = requireNotNull(ordinaryRelation(candidate.retrievedBecause, candidate.kind))
        val reference = reference(
            request,
            candidate.stableId,
            relation,
            if (relation == TherapyMemoryRelation.EXPLICIT_USER_REFERENCE) {
                TherapyMemorySemanticAct.DIRECT_RECALL
            } else {
                TherapyMemorySemanticAct.TENTATIVE_CONNECTION
            },
            relation == TherapyMemoryRelation.EXPLICIT_USER_REFERENCE,
        )
        val history = SurfacedMemoryHistoryEntry(
            stableObjectId = reference.stableObjectId,
            surfacedOnTurnId = request.turnId,
            relation = reference.relation,
            reasonCode = "ORDINARY_DIRECT_STRUCTURAL_CONTINUITY",
            evidenceMeaningToken = request.packet.metadata.packetDigest,
            explicitlyReinvoked = false,
        )
        return decision(
            TherapyMemoryUseDisposition.SURFACE_ONE_MEMORY,
            listOf(reference),
            request.sessionState.copy(surfaced = request.sessionState.surfaced + history),
            "ONE_DIRECT_STRUCTURAL_MEMORY_AUTHORIZED",
        )
    }

    private fun explicitRecall(request: TherapyMemoryGateRequest): TherapyMemoryUseDecision {
        val selected = request.packet.longitudinal.items
            .filter { it.kind != RetrievalItemKind.HYPOTHESIS }
            .take(4)
            .map { item ->
                reference(
                    request,
                    item.stableId,
                    TherapyMemoryRelation.EXPLICIT_USER_REFERENCE,
                    TherapyMemorySemanticAct.USER_REQUESTED_COMPARISON,
                    true,
                )
            }
        if (selected.isEmpty()) {
            return decision(
                TherapyMemoryUseDisposition.NO_RELEVANT_MEMORY,
                emptyList(),
                request.sessionState,
                "EXPLICIT_RECALL_FOUND_NO_ELIGIBLE_MEMORY",
            )
        }
        val additions = selected.map { memory ->
            SurfacedMemoryHistoryEntry(
                memory.stableObjectId,
                request.turnId,
                memory.relation,
                "EXPLICIT_USER_RECALL",
                request.packet.metadata.packetDigest,
                explicitlyReinvoked = memory.stableObjectId in request.explicitReinvocationObjectIds ||
                    request.sessionState.surfaced.any { it.stableObjectId == memory.stableObjectId },
            )
        }
        return decision(
            TherapyMemoryUseDisposition.EXPLICIT_RECALL_CONTEXT,
            selected,
            request.sessionState.copy(surfaced = request.sessionState.surfaced + additions),
            "EXPLICIT_USER_RECALL_AUTHORIZED_BOUNDED_CONTEXT",
        )
    }

    private fun explanation(request: TherapyMemoryGateRequest): TherapyMemoryUseDecision {
        val selected = request.packet.longitudinal.items.take(4).map { item ->
            reference(
                request,
                item.stableId,
                TherapyMemoryRelation.EXPLANATION_EVIDENCE,
                TherapyMemorySemanticAct.EVIDENCE_EXPLANATION,
                true,
            )
        }
        if (selected.isEmpty()) {
            return decision(
                TherapyMemoryUseDisposition.NO_RELEVANT_MEMORY,
                emptyList(),
                request.sessionState,
                "EXPLANATION_TARGET_HAS_NO_RETRIEVABLE_EVIDENCE",
            )
        }
        return decision(
            TherapyMemoryUseDisposition.EXPLANATION_CONTEXT,
            selected,
            request.sessionState,
            "BALANCED_EXPLANATION_CONTEXT_AUTHORIZED",
        )
    }

    private fun ordinaryActionAllowsMemory(request: TherapyMemoryGateRequest): Boolean {
        val action = request.selectedAction.definition
        if (action.renderSpecification.outputDisposition != RenderOutputDisposition.GENERATE_TEXT) return false
        return action.id.value !in setOf(
            "core-ask-support-preference",
            "core-offer-direction-choice",
            "core-acknowledge-close",
            "core-pause-without-response",
            "core-wait-for-outcome",
        )
    }

    private fun ordinaryRelation(
        reasons: List<RetrievalReason>,
        kind: RetrievalItemKind,
    ): TherapyMemoryRelation? = when {
        RetrievalReason.SAME_EVENT in reasons -> TherapyMemoryRelation.DIRECT_EVENT_CONTINUITY
        RetrievalReason.SAME_RELATIONSHIP in reasons -> TherapyMemoryRelation.DIRECT_RELATIONSHIP_CONTINUITY
        RetrievalReason.SAME_RESOLVED_ENTITY in reasons -> TherapyMemoryRelation.DIRECT_ENTITY_CONTINUITY
        kind == RetrievalItemKind.RECURRENCE && RetrievalReason.SAME_PREDICATE in reasons ->
            TherapyMemoryRelation.QUALIFIED_REPORTED_RECURRENCE
        RetrievalReason.EXPLICIT_TARGET in reasons || RetrievalReason.EXPLICIT_SOURCE_RECALL in reasons ->
            TherapyMemoryRelation.EXPLICIT_USER_REFERENCE
        else -> null
    }

    private fun notSuppressedBySession(stableId: String, request: TherapyMemoryGateRequest): Boolean {
        val prior = request.sessionState.surfaced.filter { it.stableObjectId == stableId }
        if (prior.isEmpty()) return true
        if (stableId in request.explicitReinvocationObjectIds) return true
        if (stableId in request.materiallyChangedObjectIds) return true
        return false
    }

    private fun hasKnownContradiction(stableId: String, packet: com.conundrum.thomas.v2.contextpacket.ContextPacket): Boolean =
        packet.longitudinal.items.any { item ->
            item.kind == RetrievalItemKind.CONTRADICTION && stableId in item.relatedStableIds
        }

    private fun reference(
        request: TherapyMemoryGateRequest,
        stableId: String,
        relation: TherapyMemoryRelation,
        semanticAct: TherapyMemorySemanticAct,
        explicit: Boolean,
    ): TherapyMemoryReference {
        val item = requireNotNull(request.packet.longitudinal.items.firstOrNull { it.stableId == stableId })
        val excerpt = request.packet.excerpts.excerpts.firstOrNull { it.itemStableId == stableId }
        return TherapyMemoryReference(
            stableObjectId = item.stableId,
            itemKind = item.kind,
            sourceRevisionIds = item.sourceRevisionIds,
            relation = relation,
            semanticAct = semanticAct,
            retrievalReasons = item.retrievedBecause,
            acquisitionMode = item.acquisitionMode ?: excerpt?.acquisitionMode,
            reportTime = item.reportTime ?: excerpt?.reportTime,
            eventTime = item.eventTime ?: excerpt?.eventTime,
            epistemicRole = item.epistemicRole ?: excerpt?.epistemicRole,
            uncertainty = item.uncertainty,
            lifecycle = item.lifecycle,
            currentAuthority = item.currentAuthority,
            relationIsExplicit = explicit,
            contradictionPresent = hasKnownContradiction(stableId, request.packet) ||
                item.lifecycle == RetrievalLifecycleStatus.CONTESTED,
            identityUnresolved = item.unresolvedIdentity,
            exactSourceExcerpt = excerpt?.exactText,
        )
    }

    private fun decision(
        disposition: TherapyMemoryUseDisposition,
        memories: List<TherapyMemoryReference>,
        next: TherapySessionMemoryState,
        reason: String,
    ) = TherapyMemoryUseDecision(disposition, memories, next, listOf(reason))
}
