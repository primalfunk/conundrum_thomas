package com.conundrum.thomas.v2.languageevidence.stateformation

import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.CalendarBoundary
import com.conundrum.thomas.v2.longitudinal.ContradictionAdjudication
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Identifies only same-concept, same-temporal-scope, mutually different structured values. */
class StructuralContradictionDetector {
    fun identify(evidence: LongitudinalStateEvidence): List<ContradictionRelation> {
        val eligible = evidence.snapshot.assertions
            .filter { it.id in evidence.eligibleAssertionIds && it.sourceRecordId in evidence.eligibleSourceRevisionIds }
        val existingPairs = evidence.snapshot.contradictions.map { canonicalPair(it.leftAssertionId.value, it.rightAssertionId.value) }.toSet()
        return eligible.groupBy { it.predicate.conceptId }
            .values.flatMap { comparable ->
                comparable.sortedBy { it.id }.flatMapIndexed { index, left ->
                    comparable.sortedBy { it.id }.drop(index + 1).mapNotNull { right ->
                        if (!sameTemporalScope(left.eventTime, right.eventTime) || sameValue(left, right)) return@mapNotNull null
                        val pair = canonicalPair(left.id.value, right.id.value)
                        if (pair in existingPairs) return@mapNotNull null
                        val digest = sha256("${pair.first}|${pair.second}|${left.predicate.conceptId.value}").take(20)
                        ContradictionRelation(
                            EvidenceRelationId.parse("contradiction.$digest"),
                            if (left.id.value == pair.first) left.id else right.id,
                            if (left.id.value == pair.first) right.id else left.id,
                            ContradictionAdjudication.UNRESOLVED,
                            "Comparable structured claims have different values in the same temporal scope; neither is selected",
                        )
                    }
                }
            }.sortedBy { it.id }
    }

    private fun sameValue(left: EvidenceAssertion, right: EvidenceAssertion): Boolean =
        left.value.canonical() == right.value.canonical() && left.polarity == right.polarity

    private fun sameTemporalScope(left: EventTime, right: EventTime): Boolean = when {
        left is EventTime.Range && right is EventTime.Range ->
            left.start.earliest == right.start.earliest && left.end.latest == right.end.latest
        left is EventTime.CalendarDate && right is EventTime.CalendarDate -> left.value == right.value
        left is EventTime.ExactInstant && right is EventTime.ExactInstant -> left.value == right.value
        left is EventTime.ApproximateYear && right is EventTime.ApproximateYear -> left.year == right.year
        left is EventTime.RelativePeriod && right is EventTime.RelativePeriod ->
            left.relation == right.relation && left.description.equals(right.description, ignoreCase = true)
        else -> false
    }

    private fun AssertionValue.canonical(): String = when (this) {
        is AssertionValue.Text -> "text:${value.lowercase()}"
        is AssertionValue.EntityReference -> "entity:${entityId.value}"
        is AssertionValue.EntityReferences -> "entities:${entityIds.sorted().joinToString { it.value }}"
        is AssertionValue.TimeReference -> "time:$value"
        is AssertionValue.BooleanValue -> "boolean:$value"
        is AssertionValue.IntegerValue -> "integer:$value"
        is AssertionValue.ConceptValue -> "concept:${conceptId.value}"
    }

    private fun canonicalPair(left: String, right: String) = if (left < right) left to right else right to left
    private fun sha256(value: String) = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
