package com.conundrum.thomas.v2.contextpacket

import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.retrieval.DeterministicLongitudinalRetriever
import com.conundrum.thomas.v2.retrieval.RetrievalDisposition
import com.conundrum.thomas.v2.retrieval.RetrievalItemKind
import com.conundrum.thomas.v2.retrieval.RetrievalReason
import com.conundrum.thomas.v2.retrieval.RetrievedLongitudinalItem
import com.conundrum.thomas.v2.retrieval.RetrievedPayload
import com.conundrum.thomas.v2.retrieval.SourceExcerptProposal
import java.security.MessageDigest

/**
 * Builds an immutable packet from a read-only retrieval result. Historical text remains
 * explicitly typed data and can never replace authority or safety layers.
 */
class DeterministicContextPacketBuilder(
    private val retriever: DeterministicLongitudinalRetriever,
) {
    fun build(request: ContextPacketBuildRequest): ContextPacketBuildResult {
        if (request.modeAuthority.activeMode != request.retrieval.activeMode) {
            return rejected(ContextPacketDisposition.REJECTED_AUTHORITY, "MODE_AUTHORITY_MISMATCH")
        }
        val retrieval = retriever.retrieve(request.retrieval)
        when (retrieval.disposition) {
            RetrievalDisposition.REJECTED_AUTHORITY ->
                return rejected(ContextPacketDisposition.REJECTED_AUTHORITY, retrieval.reasonCodes.first())
            RetrievalDisposition.REJECTED_INVALID_REQUEST ->
                return rejected(ContextPacketDisposition.REJECTED_INVALID_REQUEST, retrieval.reasonCodes.first())
            RetrievalDisposition.REVISION_UNAVAILABLE ->
                return rejected(ContextPacketDisposition.REVISION_UNAVAILABLE, retrieval.reasonCodes.first())
            RetrievalDisposition.RETRIEVED, RetrievalDisposition.EMPTY -> Unit
        }

        val budget = request.retrieval.budget
        var usedText = 0
        var omittedLongitudinalText = 0

        fun takeText(value: String): Boolean =
            if (usedText + value.length <= budget.maximumTotalTextCharacters) {
                usedText += value.length
                true
            } else false

        // Recent turns win, but retained turns remain in conversational order.
        val immediate = request.immediateConversation.takeLast(budget.maximumImmediateItems)
            .filter { takeText(it.content) }
        val runtime = request.runtimeState.takeLast(budget.maximumRuntimeItems)
            .filter { takeText(it.value) }

        val packetItems = retrieval.selectedItems.map { selected ->
            val rawValue = dataValue(selected)
            val value = if (rawValue is PacketDataValue.TextData && !takeText(rawValue.value)) {
                omittedLongitudinalText++
                PacketDataValue.OmittedText("TEXT_BUDGET_EXHAUSTED")
            } else rawValue
            PacketLongitudinalItem(
                kind = selected.kind,
                stableId = selected.stableId,
                lifecycle = selected.lifecycle.status,
                currentAuthority = selected.currentAuthority,
                epistemicRole = selected.epistemicRole,
                uncertainty = selected.uncertainty,
                eventTime = selected.eventTime,
                reportTime = selected.reportTime,
                acquisitionMode = selected.acquisitionMode,
                sourceRevisionIds = selected.sourceRevisionIds,
                entityIds = selected.entityIds,
                relatedStableIds = relatedIds(selected),
                unresolvedIdentity = selected.unresolvedIdentity,
                dataValue = value,
                retrievedBecause = selected.reasons,
            )
        }

        var omittedExcerpts = 0
        var truncatedExcerpts = 0
        val packetExcerpts = buildList {
            retrieval.excerptProposals.forEach { proposal ->
                if (size >= budget.maximumSourceExcerpts) {
                    omittedExcerpts++
                    return@forEach
                }
                val remaining = budget.maximumTotalTextCharacters - usedText
                if (remaining <= 0) {
                    omittedExcerpts++
                    return@forEach
                }
                val excerptLimit = minOf(budget.maximumExcerptCharacters, remaining)
                val exact = proposal.grounding.exactFragment
                val selectedText = if (exact.length <= excerptLimit) exact else safePrefix(exact, excerptLimit)
                if (selectedText == null || selectedText.isBlank()) {
                    omittedExcerpts++
                    return@forEach
                }
                val truncated = selectedText.length < exact.length
                usedText += selectedText.length
                if (truncated) truncatedExcerpts++
                add(proposal.toPacketExcerpt(selectedText, truncated))
            }
        }

        val authority = PacketAuthorityLayer(request.modeAuthority)
        val safety = PacketSafetyLayer(request.safetyConstraints.sortedBy { it.constraintId })
        val immediateLayer = PacketImmediateLayer(immediate)
        val runtimeLayer = PacketRuntimeLayer(runtime)
        val longitudinalLayer = PacketLongitudinalLayer(packetItems)
        val excerptLayer = PacketExcerptLayer(packetExcerpts)
        val depthUsed = if (packetItems.any { item ->
                item.kind == RetrievalItemKind.HYPOTHESIS ||
                    item.retrievedBecause.any { it in setOf(RetrievalReason.DIRECT_EVIDENCE, RetrievalReason.REPRESENTATIVE_COUNTEREVIDENCE) }
            }) minOf(1, budget.maximumDependencyDepth) else 0
        val digest = digest(
            authority, safety, immediateLayer, runtimeLayer, longitudinalLayer, excerptLayer,
            request, retrieval.archiveDigest, retrieval.candidateCount, retrieval.exclusions,
            usedText, omittedLongitudinalText, omittedExcerpts, truncatedExcerpts, depthUsed,
        )
        val metadata = ContextPacketMetadata(
            packetVersion = CT_V2_11_CONTEXT_PACKET_VERSION,
            retrievalPolicyVersion = request.retrieval.policyVersion,
            storeRevision = request.retrieval.snapshotRevision,
            intent = request.retrieval.intent,
            activeMode = request.retrieval.activeMode,
            candidateCount = retrieval.candidateCount,
            selectedCount = packetItems.size,
            exclusions = retrieval.exclusions,
            budget = budget,
            totalTextCharacters = usedText,
            omittedLongitudinalTextCount = omittedLongitudinalText,
            omittedExcerptCount = omittedExcerpts,
            truncatedExcerptCount = truncatedExcerpts,
            maximumTraversalDepthUsed = depthUsed,
            packetDigest = digest,
        )
        val packet = ContextPacket(authority, safety, immediateLayer, runtimeLayer, longitudinalLayer, excerptLayer, metadata)
        val isEmpty = immediate.isEmpty() && runtime.isEmpty() && packetItems.isEmpty() && packetExcerpts.isEmpty()
        return ContextPacketBuildResult(
            if (isEmpty) ContextPacketDisposition.EMPTY else ContextPacketDisposition.BUILT,
            packet,
            retrieval.reasonCodes,
        )
    }

    private fun dataValue(item: RetrievedLongitudinalItem): PacketDataValue? = when (val payload = item.payload) {
        is RetrievedPayload.Source -> PacketDataValue.ConceptData("source-record")
        is RetrievedPayload.Assertion -> payload.value.value.toPacketValue()
        is RetrievedPayload.Entity -> PacketDataValue.ConceptData(payload.value::class.simpleName ?: "life-entity")
        is RetrievedPayload.Hypothesis -> payload.value.proposedValue.toPacketValue()
        is RetrievedPayload.Contradiction -> PacketDataValue.ConceptData("contradiction.${payload.value.adjudication.name.lowercase()}")
        is RetrievedPayload.Correction -> PacketDataValue.ConceptData("correction.${payload.value.effect.name.lowercase()}")
        is RetrievedPayload.OpenQuestion -> PacketDataValue.ConceptData(payload.value.reasonCode)
        is RetrievedPayload.Recurrence -> PacketDataValue.ConceptData(payload.value.conceptId.value)
    }

    private fun AssertionValue.toPacketValue(): PacketDataValue = when (this) {
        is AssertionValue.Text -> PacketDataValue.TextData(value)
        is AssertionValue.EntityReference -> PacketDataValue.EntityData(listOf(entityId))
        is AssertionValue.EntityReferences -> PacketDataValue.EntityData(entityIds.sorted())
        is AssertionValue.TimeReference -> PacketDataValue.TimeData(value)
        is AssertionValue.BooleanValue -> PacketDataValue.BooleanData(value)
        is AssertionValue.IntegerValue -> PacketDataValue.IntegerData(value)
        is AssertionValue.ConceptValue -> PacketDataValue.ConceptData(conceptId.value)
    }

    private fun relatedIds(item: RetrievedLongitudinalItem): List<String> = when (val payload = item.payload) {
        is RetrievedPayload.Source -> emptyList()
        is RetrievedPayload.Assertion -> emptyList()
        is RetrievedPayload.Entity -> payload.value.supportingAssertionIds.map { it.value }.sorted()
        is RetrievedPayload.Hypothesis -> emptyList()
        is RetrievedPayload.Contradiction -> listOf(payload.value.leftAssertionId.value, payload.value.rightAssertionId.value).sorted()
        is RetrievedPayload.Correction -> listOf(payload.value.correctingAssertionId.value, payload.value.correctedAssertionId.value)
        is RetrievedPayload.OpenQuestion -> payload.value.basisIds.sorted()
        is RetrievedPayload.Recurrence -> payload.value.assertionIds.map { it.value }.sorted()
    }

    private fun safePrefix(exact: String, limit: Int): String? {
        if (limit < 1) return null
        var end = minOf(limit, exact.length)
        if (end < exact.length) {
            val boundary = exact.lastIndexOfAny(charArrayOf(' ', '\n', '\t'), end - 1)
            if (boundary >= limit / 2) end = boundary
        }
        val prefix = exact.substring(0, end)
        val omitted = exact.substring(end)
        val markers = Regex("(?i)\\b(no|not|never|maybe|probably|uncertain|unsure|unknown|don't|didn't|isn't|wasn't)\\b")
        val omittedMarkers = markers.findAll(omitted).map { it.value.lowercase() }.toSet()
        val keptMarkers = markers.findAll(prefix).map { it.value.lowercase() }.toSet()
        return if ((omittedMarkers - keptMarkers).isNotEmpty()) null else prefix
    }

    private fun SourceExcerptProposal.toPacketExcerpt(text: String, truncated: Boolean) = ContextSourceExcerpt(
        itemStableId = itemStableId,
        stableSourceId = stableSourceId,
        sourceRevisionId = sourceRevisionId,
        acquisitionMode = acquisitionMode,
        reportTime = reportTime,
        eventTime = eventTime,
        startOffsetInclusive = grounding.startOffsetInclusive,
        endOffsetExclusive = grounding.startOffsetInclusive + text.length,
        sourceRevisionFingerprint = grounding.sourceRevisionSha256,
        epistemicRole = epistemicRole,
        lifecycle = lifecycle.status,
        exactText = text,
        truncatedAtEnd = truncated,
    )

    private fun digest(
        authority: PacketAuthorityLayer,
        safety: PacketSafetyLayer,
        immediate: PacketImmediateLayer,
        runtime: PacketRuntimeLayer,
        longitudinal: PacketLongitudinalLayer,
        excerpts: PacketExcerptLayer,
        request: ContextPacketBuildRequest,
        archiveDigest: String,
        candidateCount: Int,
        exclusions: Any,
        usedText: Int,
        omittedText: Int,
        omittedExcerpts: Int,
        truncatedExcerpts: Int,
        depth: Int,
    ): String {
        val canonical = buildString {
            listOf(
                CT_V2_11_CONTEXT_PACKET_VERSION, request.retrieval.policyVersion, archiveDigest,
                request.retrieval.snapshotRevision, request.retrieval.intent, request.retrieval.activeMode,
                canonicalAnchors(request), request.retrieval.budget, authority, safety, immediate, runtime,
                longitudinal, excerpts, candidateCount, exclusions, usedText, omittedText,
                omittedExcerpts, truncatedExcerpts, depth,
            ).forEach { value ->
                val encoded = value.toString()
                append(encoded.length).append(':').append(encoded).append('|')
            }
        }
        return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun canonicalAnchors(request: ContextPacketBuildRequest): String = request.retrieval.anchors.run {
        listOf(
            sourceIdentityIds.map { it.value }.sorted(), sourceRevisionIds.map { it.value }.sorted(),
            assertionIds.map { it.value }.sorted(), hypothesisIds.map { it.value }.sorted(),
            entityIds.map { it.value }.sorted(), eventIds.map { it.value }.sorted(),
            relationshipIds.map { it.value }.sorted(), periodIds.map { it.value }.sorted(),
            predicateIds.map { it.value }.sorted(), openQuestionIds.sorted(), lexicalTerms.sorted(),
            temporalBounds.map { it.toString() }.sorted(),
        ).joinToString("|")
    }

    private fun rejected(disposition: ContextPacketDisposition, reason: String) =
        ContextPacketBuildResult(disposition, null, listOf(reason))
}
