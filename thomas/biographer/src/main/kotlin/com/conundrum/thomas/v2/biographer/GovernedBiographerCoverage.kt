package com.conundrum.thomas.v2.biographer

import com.conundrum.thomas.v2.languageevidence.stateformation.FormedLongitudinalState
import com.conundrum.thomas.v2.longitudinal.*
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalReader
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.ZoneOffset

/** CT-V2-10 structural derivation shared by protected production and synthetic qualification.
 * Eligible formed state supplies facts; the existing coverage engine alone selects targets.
 */
object GovernedBiographerCoverage {
    fun derive(state: FormedLongitudinalState, reader: LongitudinalReader): CoverageEvidence {

        val snapshot = reader.snapshot(state.storeRevision)
        val activeEntities = state.activeEntitiesAndEvents
        val periods = activeEntities.filterIsInstance<LifePeriod>().map {
            RepresentedPeriod(it.id.value, it.temporalDescription, it.supportingAssertionIds.map { id -> id.value }.sorted(), CoverageStatus.SPARSE)
        }
        val candidates = mutableListOf<CoverageCandidate>()
        activeEntities.filterIsInstance<LifePeriod>().forEach { period ->
            candidates += candidate(
                "period." + period.id.value,
                InvestigationTargetKind.PERIOD_DETAIL,
                period.supportingAssertionIds.map { it.value },
                listOf(period.id),
                listOf(period.temporalDescription),
                period.temporalDescription.isUncertain(),
                "REPRESENTED_PERIOD_HAS_PARTIAL_DETAIL",
                CoverageStatus.SPARSE,
            )
        }
        activeEntities.filterIsInstance<LifeEvent>().forEach { event ->
            val assertions = event.supportingAssertionIds.mapNotNull { id -> snapshot.assertions.find { it.id == id } }
            if (assertions.isEmpty() || assertions.all { it.eventTime is EventTime.Unknown }) {
                candidates += candidate(
                    "event-time." + event.id.value,
                    InvestigationTargetKind.EVENT_TIME_UNRESOLVED,
                    event.supportingAssertionIds.map { it.value },
                    listOf(event.id),
                    assertions.map { it.eventTime },
                    true,
                    "KNOWN_EVENT_TIME_UNRESOLVED",
                    CoverageStatus.UNRESOLVED,
                )
            }
        }
        activeEntities.filterIsInstance<Role>().filter { it.contextId == null }.forEach { role ->
            candidates += candidate(
                "role." + role.id.value,
                InvestigationTargetKind.ROLE_GAP,
                role.supportingAssertionIds.map { it.value },
                listOf(role.id),
                emptyList(),
                false,
                "KNOWN_ROLE_CONTEXT_INCOMPLETE",
                CoverageStatus.SPARSE,
            )
        }
        state.unresolvedIdentities.forEach { identity ->
            candidates += candidate(
                "identity." + shortHash(identity.entityIds.joinToString("|") { it.value }),
                InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED,
                identity.entityIds.map { it.value },
                identity.entityIds,
                emptyList(),
                true,
                identity.reasonCode,
                CoverageStatus.UNRESOLVED,
            )
        }
        state.contradictions.forEach { contradiction ->
            candidates += candidate(
                "contradiction." + contradiction.id.value,
                InvestigationTargetKind.CONTRADICTION_CLARIFICATION,
                listOf(contradiction.leftAssertionId.value, contradiction.rightAssertionId.value),
                emptyList(),
                emptyList(),
                true,
                "COMPARABLE_ADMISSIBLE_CLAIMS_CONFLICT",
                CoverageStatus.UNRESOLVED,
            )
        }
        state.unresolvedCorrectionSourceRevisionIds.forEach { sourceId ->
            candidates += candidate(
                "correction." + sourceId.value,
                InvestigationTargetKind.CORRECTION_TARGET_UNRESOLVED,
                listOf(sourceId.value),
                emptyList(),
                emptyList(),
                true,
                "CORRECTION_TARGET_NOT_DETERMINISTIC",
                CoverageStatus.UNRESOLVED,
            )
        }
        state.openEvidenceQuestions.forEach { question ->
            val kind = when (question.kind.name) {
                "UNRESOLVED_IDENTITY", "UNRESOLVED_REFERENCE" -> InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED
                "AMBIGUOUS_CORRECTION_TARGET" -> InvestigationTargetKind.CORRECTION_TARGET_UNRESOLVED
                "UNRESOLVED_CONTRADICTION" -> InvestigationTargetKind.CONTRADICTION_CLARIFICATION
                "UNKNOWN_EVENT_TIME" -> InvestigationTargetKind.EVENT_TIME_UNRESOLVED
                else -> InvestigationTargetKind.EXISTING_OPEN_EVIDENTIARY_QUESTION
            }
            candidates += candidate(
                "open." + safeId(question.id),
                kind,
                question.basisIds.ifEmpty { listOf(question.id) },
                emptyList(),
                emptyList(),
                true,
                question.reasonCode,
                CoverageStatus.UNRESOLVED,
            )
        }
        candidates += temporalGapCandidates(state.activeExplicitClaims + state.activeSelfReports + state.activeUserInterpretations, snapshot.sources.associate { it.id to it.recordTime.value })
        snapshot.coverageTopics.filterNot { it.label.startsWith("biographer-target:") }.filter { topic -> topic.sourceRecordIds.all { reader.isEligible(com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef(com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType.SOURCE_REVISION, it.value), state.storeRevision) } }.forEach { topic ->
            val status = topic.status.toCoverageStatus()
            val kind = when {
                topic.label.startsWith("role:", true) -> InvestigationTargetKind.ROLE_GAP
                topic.label.startsWith("place:", true) -> InvestigationTargetKind.PLACE_GAP
                topic.label.startsWith("relationship:", true) -> InvestigationTargetKind.RELATIONSHIP_CONTEXT
                topic.label.startsWith("event:", true) -> InvestigationTargetKind.EVENT_DETAIL
                else -> InvestigationTargetKind.EXISTING_OPEN_EVIDENTIARY_QUESTION
            }
            candidates += candidate(
                "coverage." + topic.id.value,
                kind,
                topic.sourceRecordIds.map { it.value }.ifEmpty { listOf(topic.id.value) },
                emptyList(),
                emptyList(),
                status == CoverageStatus.UNKNOWN || status == CoverageStatus.UNRESOLVED,
                "GOVERNED_COVERAGE_TOPIC_" + topic.status.name,
                status,
            )
        }
        return CoverageEvidence(
            state.storeRevision,
            periods,
            activeEntities.filterIsInstance<Role>().map { it.id },
            activeEntities.filterIsInstance<Place>().map { it.id },
            activeEntities.filterIsInstance<Relationship>().map { it.id },
            candidates.distinctBy { it.id }.map { candidate ->
                val control = snapshot.coverageTopics.firstOrNull { it.label == "biographer-target:" + candidate.id.value }
                if (control == null) candidate else candidate.copy(status = control.status.toCoverageStatus())
            },
        )
    }

    private fun temporalGapCandidates(assertions: List<com.conundrum.thomas.v2.longitudinal.EvidenceAssertion>, recordedAt: Map<SourceRecordId, java.time.Instant>): List<CoverageCandidate> {
        val dated = assertions.distinctBy { it.id }.mapNotNull { assertion ->
            assertion.eventTime.yearOrNull()?.let { Triple(it, assertion, assertion.eventTime) }
        }.groupBy { it.first }.values.map { representedYear ->
            // One oldest eligible boundary witness per represented year. Repeating a historical
            // statement cannot replace the witness merely because it received another source ID.
            representedYear.minWith(compareBy({ recordedAt[it.second.sourceRecordId] }, { it.second.id.value }))
        }.sortedBy { it.first }
        return dated.zipWithNext().mapNotNull { (left, right) ->
            if (right.first - left.first < 2) return@mapNotNull null
            candidate(
                "temporal-gap." + left.first + "." + right.first + "." + shortHash(left.second.id.value + right.second.id.value),
                InvestigationTargetKind.TEMPORAL_GAP,
                listOf(left.second.id.value, right.second.id.value),
                emptyList(),
                listOf(left.third, right.third),
                left.third.isUncertain() || right.third.isUncertain(),
                "SPARSE_INTERVAL_BETWEEN_REPRESENTED_HISTORY",
                CoverageStatus.SPARSE,
                listOf(
                    CoverageSafeFact("represented-history-boundary", listOf(left.second.id.value), left.third),
                    CoverageSafeFact("represented-history-boundary", listOf(right.second.id.value), right.third),
                ),
            )
        }
    }

    private fun candidate(
        id: String,
        kind: InvestigationTargetKind,
        grounding: List<String>,
        entities: List<com.conundrum.thomas.v2.longitudinal.LifeEntityId>,
        times: List<EventTime>,
        uncertainty: Boolean,
        reason: String,
        status: CoverageStatus,
        safeFacts: List<CoverageSafeFact> = emptyList(),
    ): CoverageCandidate {
        val normalizedGrounding = grounding.distinct().sorted().ifEmpty { listOf("structural-gap") }
        return CoverageCandidate(
            InvestigationTargetId.parse(safeId(id)),
            kind,
            normalizedGrounding,
            entities,
            times,
            uncertainty,
            reason,
            status,
            safeFacts,
            shortHash(listOf(kind.name, normalizedGrounding.joinToString(","), times.joinToString(","), status.name).joinToString("|")),
        )
    }

    private fun InformationCoverageStatus.toCoverageStatus() = when (this) {
        InformationCoverageStatus.UNKNOWN, InformationCoverageStatus.NOT_EXPLORED -> CoverageStatus.UNKNOWN
        InformationCoverageStatus.PARTIAL -> CoverageStatus.SPARSE
        InformationCoverageStatus.PRIVATE -> CoverageStatus.PRIVATE
        InformationCoverageStatus.DECLINED -> CoverageStatus.DECLINED
        InformationCoverageStatus.UNRESOLVED -> CoverageStatus.UNRESOLVED
        InformationCoverageStatus.IRRELEVANT, InformationCoverageStatus.SUFFICIENTLY_UNDERSTOOD ->
            CoverageStatus.COVERED_ENOUGH_FOR_CURRENT_PURPOSE
    }

    private fun EventTime.yearOrNull(): Int? = when (this) {
        is EventTime.ExactInstant -> value.atZone(ZoneOffset.UTC).year
        is EventTime.CalendarDate -> value.year
        is EventTime.ApproximateDate -> center.year
        is EventTime.ApproximateYear -> year.value
        is EventTime.Range -> start.earliest.year
        else -> null
    }

    private fun EventTime.isUncertain() = when (this) {
        is EventTime.ExactInstant, is EventTime.CalendarDate -> false
        else -> true
    }

    private fun safeId(value: String): String {
        val normalized = value.lowercase().replace(Regex("[^a-z0-9.-]+"), "-").trim('-', '.')
        return normalized.takeIf { it.isNotBlank() } ?: "target." + shortHash(value)
    }

    private fun shortHash(value: String) = sha256(value).take(16)
    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
