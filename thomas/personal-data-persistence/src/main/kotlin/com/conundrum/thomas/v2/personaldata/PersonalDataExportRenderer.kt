package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.admission.CanonicalLongitudinalEncoding
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAggregateState
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import java.time.Instant

internal object PersonalDataExportRenderer {
    private val quote = 34.toChar()

    fun render(state: LongitudinalAggregateState, createdAt: Instant): PersonalDataExport {
        val digest = CanonicalLongitudinalEncoding.stateDigest(state)
        return PersonalDataExport(
            CT_V2_14_EXPORT_FORMAT_VERSION,
            createdAt,
            state.storeRevision,
            digest,
            machineReadable(state, digest, createdAt),
            humanReadable(state, digest, createdAt),
        )
    }

    private fun machineReadable(state: LongitudinalAggregateState, digest: String, createdAt: Instant): String = buildString {
        append('{')
        name("formatVersion"); append(CT_V2_14_EXPORT_FORMAT_VERSION); append(',')
        name("createdAt"); string(createdAt.toString()); append(',')
        name("storeRevision"); append(state.storeRevision); append(',')
        name("logicalStateDigest"); string(digest); append(',')
        name("sourceEvidence"); append('[')
        state.snapshot.sources.sortedBy { it.id.value }.forEachIndexed { index, source ->
            if (index > 0) append(',')
            append(sourceJson(source, state.sourcePrivacy[source.stableSourceId] ?: SourcePrivacy.ELIGIBLE))
        }
        append(']').append(',')
        name("derivedState"); append('{')
        name("assertions"); array(state.snapshot.assertions.sortedBy { it.id.value }) { assertion ->
            objectValue(
                "id" to assertion.id.value,
                "sourceRevisionId" to assertion.sourceRecordId.value,
                "subject" to subject(assertion.subject),
                "predicate" to assertion.predicate.conceptId.value,
                "predicateSemantics" to assertion.predicate.semantics.name,
                "value" to assertionValue(assertion.value),
                "kind" to assertion.kind.name,
                "epistemicClass" to assertion.epistemicClass.name,
                "uncertainty" to assertion.uncertainty.name,
                "eventTime" to assertion.eventTime.toString(),
            )
        }
        append(','); name("entities"); array(state.snapshot.entities.sortedBy { it.id.value }) { entity ->
            objectValue(
                "id" to entity.id.value,
                "type" to entity::class.simpleName.orEmpty(),
                "label" to entity.label,
                "supportingAssertionIds" to entity.supportingAssertionIds.map { it.value }.sorted().joinToString("|"),
            )
        }
        append(','); name("hypotheses"); array(state.snapshot.hypotheses.sortedBy { it.id.value }) { hypothesis ->
            objectValue(
                "id" to hypothesis.id.value,
                "status" to hypothesis.status.name,
                "subject" to subject(hypothesis.subject),
                "predicate" to hypothesis.predicate.conceptId.value,
                "proposedValue" to assertionValue(hypothesis.proposedValue),
                "createdAt" to hypothesis.createdAt.value.toString(),
                "rationale" to hypothesis.rationale,
            )
        }
        append(','); name("hypothesisDependencies"); array(state.snapshot.hypothesisDependencies.sortedBy { it.id.value }) { relation ->
            objectValue(
                "id" to relation.id.value,
                "dependentHypothesisId" to relation.dependentHypothesisId.value,
                "prerequisite" to claim(relation.prerequisite),
                "role" to relation.role.name,
                "rationale" to relation.rationale,
            )
        }
        append(','); name("corrections"); array(state.snapshot.corrections.sortedBy { it.id.value }) { relation ->
            objectValue(
                "id" to relation.id.value,
                "correctingAssertionId" to relation.correctingAssertionId.value,
                "correctedAssertionId" to relation.correctedAssertionId.value,
                "effect" to relation.effect.name,
                "rationale" to relation.rationale,
            )
        }
        append(','); name("contradictions"); array(state.snapshot.contradictions.sortedBy { it.id.value }) { relation ->
            objectValue(
                "id" to relation.id.value,
                "leftAssertionId" to relation.leftAssertionId.value,
                "rightAssertionId" to relation.rightAssertionId.value,
                "adjudication" to relation.adjudication.name,
                "rationale" to relation.rationale,
            )
        }
        append(','); name("supersessions"); array(state.snapshot.supersessions.sortedBy { it.id.value }) { relation ->
            objectValue(
                "id" to relation.id.value,
                "successor" to claim(relation.successor),
                "predecessor" to claim(relation.predecessor),
                "kind" to relation.kind.name,
                "rationale" to relation.rationale,
            )
        }
        append(','); name("identityLinks"); array(state.snapshot.identityLinks.sortedBy { it.id.value }) { link ->
            objectValue(
                "id" to link.id.value,
                "leftEntityId" to link.leftEntityId.value,
                "rightEntityId" to link.rightEntityId.value,
                "status" to link.status.name,
                "supportingAssertionIds" to link.supportingAssertionIds.map { it.value }.sorted().joinToString("|"),
                "rationale" to link.rationale,
            )
        }
        append(','); name("coverageTopics"); array(state.snapshot.coverageTopics.sortedBy { it.id.value }) { topic ->
            objectValue(
                "id" to topic.id.value,
                "label" to topic.label,
                "status" to topic.status.name,
                "sourceRevisionIds" to topic.sourceRecordIds.map { it.value }.sorted().joinToString("|"),
            )
        }
        append('}').append(',')
        name("lifecycle"); array(state.lifecycle.entries.sortedBy { "${it.key.type.name}:${it.key.stableId}" }) { entry ->
            objectValue(
                "objectType" to entry.key.type.name,
                "objectId" to entry.key.stableId,
                "status" to entry.value.status.name,
                "eligibleForOrdinaryUse" to entry.value.eligibleForOrdinaryUse.toString(),
                "causeCode" to entry.value.causeCode,
                "changedAtRevision" to entry.value.changedAtRevision.toString(),
            )
        }
        append('}')
    }

    private fun sourceJson(source: SourceRecord, privacy: SourcePrivacy): String = buildString {
        append('{')
        name("stableSourceId"); string(source.stableSourceId.value); append(',')
        name("revisionId"); string(source.id.value); append(',')
        name("sourceRevision"); append(source.provenance.sourceRevision); append(',')
        name("previousRevisionId")
        val previous = source.provenance.previousRevisionId?.value
        if (previous == null) append("null") else string(previous)
        append(',')
        name("acquisitionMode"); string(source.provenance.acquisitionMode.name); append(',')
        name("authorRole"); string(source.authorRole.name); append(',')
        name("privacy"); string(privacy.name); append(',')
        name("eventTime"); string(source.eventTime.toString()); append(',')
        name("reportTime"); string(source.reportTime.value.toString()); append(',')
        name("recordTime"); string(source.recordTime.value.toString()); append(',')
        name("content")
        when (val content = source.originalContent) {
            is OriginalSourceContent.Inline -> string(content.exactContent)
            is OriginalSourceContent.FutureSafeReference -> {
                append('{'); name("reference"); string(content.contentReference)
                content.contentSha256?.let { append(','); name("sha256"); string(it) }
                append('}')
            }
        }
        append('}')
    }

    private fun humanReadable(state: LongitudinalAggregateState, digest: String, createdAt: Instant): String = buildString {
        appendLine("# Conundrum Thomas personal-data export")
        appendLine()
        appendLine("Created: $createdAt")
        appendLine("Store revision: ${state.storeRevision}")
        appendLine("Logical digest: `$digest`")
        appendLine()
        appendLine("## Source evidence")
        state.snapshot.sources.sortedBy { it.id.value }.forEach { source ->
            appendLine()
            appendLine("### ${source.stableSourceId.value} / revision ${source.provenance.sourceRevision}")
            appendLine("- Revision ID: ${source.id.value}")
            appendLine("- Previous revision: ${source.provenance.previousRevisionId?.value ?: "none"}")
            appendLine("- Provenance: ${source.provenance.acquisitionMode}")
            appendLine("- Privacy: ${state.sourcePrivacy[source.stableSourceId] ?: SourcePrivacy.ELIGIBLE}")
            appendLine("- Event time: ${source.eventTime}")
            appendLine("- Report time: ${source.reportTime.value}")
            appendLine("- Record time: ${source.recordTime.value}")
            appendLine()
            appendLine((source.originalContent as? OriginalSourceContent.Inline)?.exactContent ?: "[externally referenced content]")
        }
        appendLine()
        appendLine("## Derived state (subordinate and rebuildable)")
        state.snapshot.assertions.sortedBy { it.id.value }.forEach { assertion ->
            appendLine("- Assertion `${assertion.id.value}` from `${assertion.sourceRecordId.value}`: ${assertion.epistemicClass}; ${assertion.uncertainty}")
        }
        state.snapshot.hypotheses.sortedBy { it.id.value }.forEach { hypothesis ->
            appendLine("- Hypothesis `${hypothesis.id.value}`: ${hypothesis.status}")
        }
        state.snapshot.hypothesisDependencies.sortedBy { it.id.value }.forEach { dependency ->
            appendLine("- Dependency `${dependency.id.value}`: `${dependency.dependentHypothesisId.value}` ${dependency.role} ${claim(dependency.prerequisite)}")
        }
        state.snapshot.corrections.sortedBy { it.id.value }.forEach { correction ->
            appendLine("- Correction `${correction.id.value}`: `${correction.correctingAssertionId.value}` corrects `${correction.correctedAssertionId.value}` (${correction.effect})")
        }
        state.snapshot.contradictions.sortedBy { it.id.value }.forEach { contradiction ->
            appendLine("- Contradiction `${contradiction.id.value}`: `${contradiction.leftAssertionId.value}` / `${contradiction.rightAssertionId.value}` (${contradiction.adjudication})")
        }
        state.snapshot.identityLinks.sortedBy { it.id.value }.forEach { identity ->
            appendLine("- Identity `${identity.id.value}`: `${identity.leftEntityId.value}` / `${identity.rightEntityId.value}` (${identity.status})")
        }
        appendLine()
        appendLine("## Lifecycle state")
        state.lifecycle.entries.sortedBy { "${it.key.type.name}:${it.key.stableId}" }.forEach { (reference, lifecycle) ->
            appendLine("- ${reference.type} `${reference.stableId}`: ${lifecycle.status}; ordinary use=${lifecycle.eligibleForOrdinaryUse}; revision=${lifecycle.changedAtRevision}")
        }
    }

    private fun subject(subject: AssertionSubject): String = when (subject) {
        AssertionSubject.User -> "USER"
        is AssertionSubject.Entity -> "ENTITY:${subject.entityId.value}"
    }

    private fun assertionValue(value: AssertionValue): String = when (value) {
        is AssertionValue.Text -> "TEXT:${value.value}"
        is AssertionValue.EntityReference -> "ENTITY:${value.entityId.value}"
        is AssertionValue.EntityReferences -> "ENTITIES:${value.entityIds.map { it.value }.sorted().joinToString("|")}"
        is AssertionValue.TimeReference -> "TIME:${value.value}"
        is AssertionValue.BooleanValue -> "BOOLEAN:${value.value}"
        is AssertionValue.IntegerValue -> "INTEGER:${value.value}"
        is AssertionValue.ConceptValue -> "CONCEPT:${value.conceptId.value}"
    }

    private fun claim(reference: ClaimReference): String = when (reference) {
        is ClaimReference.Assertion -> "ASSERTION:${reference.assertionId.value}"
        is ClaimReference.Hypothesis -> "HYPOTHESIS:${reference.hypothesisId.value}"
    }

    private inline fun <T> StringBuilder.array(values: List<T>, render: StringBuilder.(T) -> Unit) {
        append('[')
        values.forEachIndexed { index, value ->
            if (index > 0) append(',')
            render(value)
        }
        append(']')
    }

    private fun StringBuilder.objectValue(vararg fields: Pair<String, String>) {
        append('{')
        fields.forEachIndexed { index, (field, value) ->
            if (index > 0) append(',')
            name(field); string(value)
        }
        append('}')
    }

    private fun StringBuilder.name(value: String) {
        append(quote).append(escape(value)).append(quote).append(':')
    }

    private fun StringBuilder.string(value: String) {
        append(quote).append(escape(value)).append(quote)
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { character ->
            when (character.code) {
                92 -> append(92.toChar()).append(92.toChar())
                34 -> append(92.toChar()).append(34.toChar())
                10 -> append(92.toChar()).append('n')
                13 -> append(92.toChar()).append('r')
                9 -> append(92.toChar()).append('t')
                in 0..31 -> append("\\u").append(character.code.toString(16).padStart(4, '0'))
                else -> append(character)
            }
        }
    }
}
