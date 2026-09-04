package com.conundrum.thomas.v2.languageevidence.stateformation

import com.conundrum.thomas.v2.languageevidence.perception.PerceptionDisposition
import com.conundrum.thomas.v2.languageevidence.perception.UnresolvedPerceptionKind
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.Person
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class DeterministicStateFormation : LongitudinalStateFormer {
    override fun form(evidence: LongitudinalStateEvidence): FormedLongitudinalState {
        require(evidence.storeRevision >= 0)
        evidence.snapshot.requireValid()
        val activeAssertions = evidence.snapshot.assertions
            .filter { it.id in evidence.eligibleAssertionIds && it.sourceRecordId in evidence.eligibleSourceRevisionIds }
            .sortedBy { it.id }
        val activeAssertionIds = activeAssertions.map { it.id }.toSet()
        val activeEntities = evidence.snapshot.entities
            .filter { entity -> entity.supportingAssertionIds.any { it in activeAssertionIds } }
            .sortedBy { it.id }
        val unresolvedIdentities = unresolvedIdentities(evidence, activeEntities.filterIsInstance<Person>())
        val activeContradictions = evidence.snapshot.contradictions
            .filter { it.leftAssertionId in activeAssertionIds && it.rightAssertionId in activeAssertionIds }
            .sortedBy { it.id }
        val unresolvedCorrections = evidence.perceptionResults
            .filter { result -> result.unresolved.any { it.kind == UnresolvedPerceptionKind.AMBIGUOUS_CORRECTION_TARGET } }
            .mapNotNull { result -> evidence.snapshot.sources.firstOrNull { it.id.value == result.sourceRevisionId }?.id }
            .distinct().sorted()
        val recurrences = recurrenceCandidates(evidence, activeAssertions)
        val questions = buildQuestions(evidence, unresolvedIdentities, activeContradictions, unresolvedCorrections)
        val sources = activeAssertions.map { it.sourceRecordId }.distinct().sorted()
        val canonical = buildString {
            append("revision=").append(evidence.storeRevision).append('\n')
            append("version=").append(CT_V2_08_STATE_FORMATION_VERSION).append('\n')
            append("sources=").append(sources.joinToString(",") { it.value }).append('\n')
            activeAssertions.forEach { assertion ->
                append("assertion=").append(assertion.id.value).append('|')
                    .append(assertion.epistemicClass.name).append('|')
                    .append(assertion.predicate.conceptId.value).append('|')
                    .append(assertion.polarity.name).append('|')
                    .append(assertion.uncertainty.name).append('|')
                    .append(assertion.eventTime.canonical()).append('\n')
            }
            activeEntities.forEach { append("entity=").append(it.id.value).append('|').append(it.label).append('\n') }
            activeContradictions.forEach { append("contradiction=").append(it.id.value).append('|').append(it.adjudication.name).append('\n') }
            unresolvedIdentities.forEach { append("identity=").append(it.label).append('|').append(it.entityIds.joinToString(",") { id -> id.value }).append('\n') }
            unresolvedCorrections.forEach { append("correction=").append(it.value).append('\n') }
            recurrences.forEach { append("recurrence=").append(it.conceptId.value).append('|').append(it.independentSourceIds.joinToString(",")).append('\n') }
            questions.forEach { append("question=").append(it.id).append('|').append(it.reasonCode).append('\n') }
            evidence.excludedObjectIds.sorted().forEach { append("excluded=").append(it).append('\n') }
            evidence.excludedLifecycleStates.toSortedMap().forEach { (id, state) ->
                append("excluded-state=").append(id).append('|').append(state).append('\n')
            }
        }
        return FormedLongitudinalState(
            storeRevision = evidence.storeRevision,
            stateFormationVersion = CT_V2_08_STATE_FORMATION_VERSION,
            contributingSourceRevisionIds = sources,
            activeExplicitClaims = activeAssertions.filter { it.epistemicClass in setOf(
                EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION,
                EvidenceEpistemicClass.ENTITY_REFERENCE,
                EvidenceEpistemicClass.EVENT_REFERENCE,
            ) },
            activeSelfReports = activeAssertions.filter { it.epistemicClass == EvidenceEpistemicClass.EXPLICIT_SELF_REPORT },
            activeSelfBeliefs = activeAssertions.filter { it.epistemicClass == EvidenceEpistemicClass.SELF_BELIEF },
            activeUserInterpretations = activeAssertions.filter { it.epistemicClass == EvidenceEpistemicClass.USER_INTERPRETATION },
            activeThirdPartyReports = activeAssertions.filter { it.epistemicClass == EvidenceEpistemicClass.THIRD_PARTY_REPORT },
            activeEntitiesAndEvents = activeEntities,
            contradictions = activeContradictions,
            unresolvedIdentities = unresolvedIdentities,
            unresolvedCorrectionSourceRevisionIds = unresolvedCorrections,
            recurrenceCandidates = recurrences,
            openEvidenceQuestions = questions,
            excludedObjectIds = evidence.excludedObjectIds.sorted(),
            excludedEvidence = evidence.excludedLifecycleStates.toSortedMap().map { ExcludedEvidence(it.key, it.value) },
            canonicalDigest = sha256(canonical),
        )
    }

    private fun unresolvedIdentities(evidence: LongitudinalStateEvidence, people: List<Person>): List<UnresolvedIdentityReference> {
        val establishedPairs = evidence.snapshot.identityLinks
            .filter { it.status == EntityIdentityStatus.ESTABLISHED_SAME_ENTITY || it.status == EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY }
            .map { canonicalPair(it.leftEntityId, it.rightEntityId) }
            .toSet()
        return people.groupBy { it.label.lowercase() }.values
            .filter { it.size > 1 }
            .mapNotNull { group ->
                val ids = group.map { it.id }.sorted()
                val allPairs = ids.flatMapIndexed { index, left -> ids.drop(index + 1).map { right -> canonicalPair(left, right) } }
                if (allPairs.all { it in establishedPairs }) null
                else UnresolvedIdentityReference(group.first().label, ids, "SAME_LABEL_DOES_NOT_ESTABLISH_IDENTITY")
            }
            .sortedBy { it.entityIds.joinToString { id -> id.value } }
    }

    private fun recurrenceCandidates(
        evidence: LongitudinalStateEvidence,
        assertions: List<com.conundrum.thomas.v2.longitudinal.EvidenceAssertion>,
    ): List<StructuralRecurrenceCandidate> {
        val stableSourceByRevision = evidence.snapshot.sources.associate { it.id to it.stableSourceId.value }
        return assertions.filter { it.predicate.conceptId.value.startsWith("reported.behavior.") }
            .groupBy { it.predicate.conceptId }
            .mapNotNull { (concept, comparable) ->
                val sourceIds = comparable.mapNotNull { stableSourceByRevision[it.sourceRecordId] }.distinct().sorted()
                if (sourceIds.size < 3) null else StructuralRecurrenceCandidate(concept, comparable.map { it.id }.sorted(), sourceIds)
            }
            .sortedBy { it.conceptId }
    }

    private fun buildQuestions(
        evidence: LongitudinalStateEvidence,
        identities: List<UnresolvedIdentityReference>,
        contradictions: List<com.conundrum.thomas.v2.longitudinal.ContradictionRelation>,
        corrections: List<com.conundrum.thomas.v2.longitudinal.SourceRecordId>,
    ): List<OpenEvidenceQuestion> = buildList {
        identities.forEach { add(OpenEvidenceQuestion("identity.${sha256(it.entityIds.joinToString { id -> id.value }).take(16)}",
            OpenEvidenceQuestionKind.UNRESOLVED_IDENTITY, it.entityIds.map { id -> id.value }, it.reasonCode)) }
        contradictions.forEach { add(OpenEvidenceQuestion("contradiction.${it.id.value}", OpenEvidenceQuestionKind.UNRESOLVED_CONTRADICTION,
            listOf(it.leftAssertionId.value, it.rightAssertionId.value), "COMPARABLE_CLAIMS_CONFLICT")) }
        corrections.forEach { add(OpenEvidenceQuestion("correction.${it.value}", OpenEvidenceQuestionKind.AMBIGUOUS_CORRECTION_TARGET,
            listOf(it.value), "CORRECTION_TARGET_REQUIRED")) }
        evidence.perceptionResults.sortedBy { it.sourceRevisionId }.forEach { result ->
            result.unresolved.filter { it.kind == UnresolvedPerceptionKind.AMBIGUOUS_REFERENT }.forEach {
                add(OpenEvidenceQuestion("reference.${result.sourceRevisionId}", OpenEvidenceQuestionKind.UNRESOLVED_REFERENCE,
                    listOf(result.sourceRevisionId), it.reasonCode))
            }
            result.unresolved.filter { it.kind == UnresolvedPerceptionKind.UNKNOWN_EVENT_TIME }.forEach {
                add(OpenEvidenceQuestion("time.${result.sourceRevisionId}", OpenEvidenceQuestionKind.UNKNOWN_EVENT_TIME,
                    listOf(result.sourceRevisionId), it.reasonCode))
            }
            if (result.disposition in setOf(PerceptionDisposition.AMBIGUOUS, PerceptionDisposition.UNSUPPORTED)) {
                add(OpenEvidenceQuestion("unsupported.${result.sourceRevisionId}", OpenEvidenceQuestionKind.UNSUPPORTED_OR_AMBIGUOUS_SOURCE,
                    listOf(result.sourceRevisionId), "SOURCE_NOT_SAFELY_INTERPRETED"))
            }
        }
    }.distinctBy { it.id }.sortedBy { it.id }

    private fun canonicalPair(left: LifeEntityId, right: LifeEntityId) =
        if (left < right) left to right else right to left

    private fun EventTime.canonical(): String = when (this) {
        is EventTime.ExactInstant -> "instant:$value"
        is EventTime.CalendarDate -> "date:$value"
        is EventTime.ApproximateDate -> "approx-date:$center:${precision.name}"
        is EventTime.ApproximateYear -> "approx-year:$year"
        is EventTime.Range -> "range:${start.earliest}:${end.latest}:$startApproximate:$endApproximate"
        is EventTime.RelativePeriod -> "relative:${relation.name}:$description:${anchorEntityId?.value.orEmpty()}"
        is EventTime.OngoingInterval -> "ongoing:${knownStart?.earliest}:$description"
        is EventTime.BeforeOrAfter -> "relative-entity:${relation.name}:${referenceEntityId.value}:$description"
        is EventTime.UncertainChronology -> "uncertain:$description:${candidateLabels.joinToString(",")}"
        is EventTime.Unknown -> "unknown:${statedReason.orEmpty()}"
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
