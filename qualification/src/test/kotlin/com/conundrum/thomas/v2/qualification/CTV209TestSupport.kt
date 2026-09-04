package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.journal.JournalCaptureOrigin
import com.conundrum.thomas.v2.journal.JournalCaptureResult
import com.conundrum.thomas.v2.journal.JournalCommitCommand
import com.conundrum.thomas.v2.journal.JournalEntryId
import com.conundrum.thomas.v2.journal.JournalIdempotencyKey
import com.conundrum.thomas.v2.journal.JournalPrivacy
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.journal.JournalRevisionCommand
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.qualification.journal.GovernedJournalCapturePipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.nio.file.Path
import java.time.Instant
import org.junit.Assert.assertTrue

internal object CTV209TestSupport {
    fun path(name: String): Path = Path.of(
        System.getProperty("thomas.repositoryRoot"),
        "qualification",
        "build",
        "ct-v2-09",
        "$name.sqlite",
    )

    fun command(
        harness: SyntheticLongitudinalStoreHarness,
        id: String,
        text: String,
        preference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
        origin: JournalCaptureOrigin = JournalCaptureOrigin.TYPED,
        privacy: JournalPrivacy = JournalPrivacy.ELIGIBLE,
        key: String = "key-$id",
        expectedRevision: Long = harness.store.reader.currentStoreRevision(),
    ) = JournalCommitCommand(
        JournalEntryId.parse(id),
        JournalIdempotencyKey.parse(key),
        expectedRevision,
        text,
        origin,
        preference,
        privacy,
        ReportTime(Instant.parse("2039-09-03T12:00:00Z")),
    )

    fun revision(
        harness: SyntheticLongitudinalStoreHarness,
        id: String,
        priorRevision: Int,
        newRevision: Int,
        text: String,
        preference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
        key: String = "revision-$id-$newRevision",
    ) = JournalRevisionCommand(
        JournalEntryId.parse(id),
        JournalIdempotencyKey.parse(key),
        harness.store.reader.currentStoreRevision(),
        JournalEntryId.parse(id).sourceRevision(priorRevision),
        newRevision,
        text,
        preference,
        ReportTime(Instant.parse("2039-09-04T12:00:00Z")),
    )

    fun capture(
        harness: SyntheticLongitudinalStoreHarness,
        pipeline: GovernedJournalCapturePipeline,
        id: String,
        text: String,
        preference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
        origin: JournalCaptureOrigin = JournalCaptureOrigin.TYPED,
        privacy: JournalPrivacy = JournalPrivacy.ELIGIBLE,
        key: String = "key-$id",
    ): JournalCaptureResult = pipeline.commit(command(harness, id, text, preference, origin, privacy, key))

    fun requireCaptured(result: JournalCaptureResult): JournalCaptureResult {
        assertTrue("Expected captured source but got ${result.disposition}:${result.reasonCodes}", result.sourceCaptured)
        return result
    }

    fun semantic(assertion: EvidenceAssertion) = listOf(
        assertion.subject.toString(),
        assertion.predicate.conceptId.value,
        assertion.predicate.semantics.name,
        assertion.value.canonicalValue(),
        assertion.kind.name,
        assertion.uncertainty.name,
        assertion.epistemicClass.name,
        assertion.polarity.name,
        assertion.eventTime.toString(),
    )

    private fun AssertionValue.canonicalValue() = when (this) {
        is AssertionValue.Text -> value
        is AssertionValue.EntityReference -> entityId.value
        is AssertionValue.EntityReferences -> entityIds.map { it.value }.sorted().joinToString(",")
        is AssertionValue.TimeReference -> value.toString()
        is AssertionValue.BooleanValue -> value.toString()
        is AssertionValue.IntegerValue -> value.toString()
        is AssertionValue.ConceptValue -> conceptId.value
    }

    fun sourceRevision(id: String, revision: Int = 1): SourceRecordId =
        JournalEntryId.parse(id).sourceRevision(revision)
}
