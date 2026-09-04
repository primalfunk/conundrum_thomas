package com.conundrum.thomas.v2.personaldata

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
            machineReadable(state, digest),
            humanReadable(state, digest),
        )
    }

    private fun machineReadable(state: LongitudinalAggregateState, digest: String): String = buildString {
        append('{')
        name("formatVersion"); append(CT_V2_14_EXPORT_FORMAT_VERSION); append(',')
        name("storeRevision"); append(state.storeRevision); append(',')
        name("logicalStateDigest"); string(digest); append(',')
        name("sourceEvidence"); append('[')
        state.snapshot.sources.sortedBy { it.id.value }.forEachIndexed { index, source ->
            if (index > 0) append(',')
            append(sourceJson(source, state.sourcePrivacy[source.stableSourceId] ?: SourcePrivacy.ELIGIBLE))
        }
        append(']').append(',')
        name("derivedState"); append('{')
        name("assertionCount"); append(state.snapshot.assertions.size); append(',')
        name("entityCount"); append(state.snapshot.entities.size); append(',')
        name("hypothesisCount"); append(state.snapshot.hypotheses.size); append(',')
        name("correctionCount"); append(state.snapshot.corrections.size); append(',')
        name("contradictionCount"); append(state.snapshot.contradictions.size); append(',')
        name("dependencyCount"); append(state.snapshot.hypothesisDependencies.size)
        append('}').append('}')
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
        name("privacy"); string(privacy.name); append(',')
        name("eventTime"); string(source.eventTime.toString()); append(',')
        name("reportTime"); string(source.reportTime.value.toString()); append(',')
        name("recordTime"); string(source.recordTime.value.toString()); append(',')
        name("content")
        when (val content = source.originalContent) {
            is OriginalSourceContent.Inline -> string(content.exactContent)
            is OriginalSourceContent.FutureSafeReference -> {
                append('{'); name("reference"); string(content.contentReference); append('}')
            }
        }
        append('}')
    }

    private fun humanReadable(state: LongitudinalAggregateState, digest: String): String = buildString {
        appendLine("# Conundrum Thomas personal-data export")
        appendLine()
        appendLine("Store revision: ${state.storeRevision}")
        appendLine("Logical digest: `$digest`")
        appendLine()
        appendLine("## Source evidence")
        state.snapshot.sources.sortedBy { it.id.value }.forEach { source ->
            appendLine()
            appendLine("### ${source.stableSourceId.value} / revision ${source.provenance.sourceRevision}")
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
        appendLine("- Assertions: ${state.snapshot.assertions.size}")
        appendLine("- Entities: ${state.snapshot.entities.size}")
        appendLine("- Hypotheses: ${state.snapshot.hypotheses.size}")
        appendLine("- Corrections: ${state.snapshot.corrections.size}")
        appendLine("- Contradictions: ${state.snapshot.contradictions.size}")
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
                else -> append(character)
            }
        }
    }
}
