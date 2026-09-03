package com.conundrum.thomas.v2.languageevidence

import com.conundrum.thomas.v2.languageevidence.perception.CommittedSourceText
import com.conundrum.thomas.v2.languageevidence.perception.ConservativeLanguagePerception
import com.conundrum.thomas.v2.longitudinal.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class AdversarialLanguageCorpusTest {
    private val parser = ConservativeLanguagePerception()

    @Test fun unsupportedAdversarialFormsNeverProduceEvidence() {
        val cases = listOf(
            "Why would I ever move there?",
            "Sam said, " + 34.toChar() + "I am furious." + 34.toChar(),
            "I guess maybe Sam probably left.",
            "Everyone never listens to me.",
            "I was sad and Sam was angry.",
            "I moved and changed jobs.",
            "Alex called Jordan because he was worried.",
            "No, actually that one happened earlier.",
            "Sure, that went brilliantly; sarcasm.",
        )
        cases.forEachIndexed { index, text ->
            val result = parser.perceive(CommittedSourceText.from(source(text, "adversarial-$index")))
            assertTrue("Unexpected proposal for: $text", result.proposals.isEmpty())
            assertNull(result.correctionCandidate)
        }
    }

    @Test fun changedJobSentimentAcrossPhasesIsTrajectoryNotContradictionInput() {
        val first = parser.perceive(CommittedSourceText.from(source("At first I loved that job.", "job-first"))).proposals.single().assertion
        val last = parser.perceive(CommittedSourceText.from(source("By the end I hated that job.", "job-end"))).proposals.single().assertion
        assertTrue(first.eventTime is EventTime.RelativePeriod)
        assertTrue(last.eventTime is EventTime.RelativePeriod)
        assertNotEquals((first.eventTime as EventTime.RelativePeriod).description, (last.eventTime as EventTime.RelativePeriod).description)
    }

    private fun source(text: String, suffix: String) = SourceRecord(
        SourceRecordId.parse("source.$suffix.rev-1"), PersonalEvidenceProvenance(AcquisitionMode.JOURNAL),
        ReportTime(Instant.parse("2050-01-01T00:00:00Z")), RecordTime(Instant.parse("2050-01-01T00:00:01Z")),
        OriginalSourceContent.Inline(text), SourceIdentityId.parse("source.$suffix"),
    )
}
