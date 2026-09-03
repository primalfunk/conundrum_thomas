package com.conundrum.thomas.v2.languageevidence

import com.conundrum.thomas.v2.languageevidence.perception.CommittedSourceText
import com.conundrum.thomas.v2.languageevidence.perception.ConservativeLanguagePerception
import com.conundrum.thomas.v2.longitudinal.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class TemporalLanguagePerceptionTest {
    private val parser = ConservativeLanguagePerception()

    @Test fun calendarDateRemainsDateOnly() {
        assertTrue(parse("On 2018-05-03 I moved to Denver.", "date").eventTime is EventTime.CalendarDate)
    }

    @Test fun exactInstantRemainsExactInstant() {
        assertTrue(parse("At 2018-05-03T10:15:30Z I arrived.", "instant").eventTime is EventTime.ExactInstant)
    }

    @Test fun yearRangeRemainsRange() {
        val time = parse("Between 2012 and 2014 I lived in Denver.", "range").eventTime as EventTime.Range
        assertFalse(time.startApproximate)
        assertEquals("2012-01-01", time.start.earliest.toString())
        assertEquals("2014-12-31", time.end.latest.toString())
    }

    @Test fun ongoingIntervalDoesNotAcquireEnd() {
        val time = parse("I have lived in Denver since 2018.", "ongoing").eventTime as EventTime.OngoingInterval
        assertNotNull(time.knownStart)
    }

    @Test fun unresolvedRelativeTimeDoesNotAcquireYear() {
        val time = parse("A few years later I changed jobs.", "relative").eventTime
        assertTrue(time is EventTime.RelativePeriod)
    }

    @Test fun vagueDurationRemainsRelativeDuration() {
        val time = parse("For a while I felt worried.", "duration").eventTime as EventTime.RelativePeriod
        assertEquals(RelativeTemporalRelation.DURATION, time.relation)
    }

    @Test fun seasonalReferenceRemainsRelative() {
        val time = parse("Sometime that winter I changed jobs.", "season").eventTime as EventTime.RelativePeriod
        assertEquals(RelativeTemporalRelation.AROUND, time.relation)
    }

    private fun parse(text: String, suffix: String): EvidenceAssertion = parser.perceive(CommittedSourceText.from(
        SourceRecord(SourceRecordId.parse("temporal.$suffix.rev-1"), PersonalEvidenceProvenance(AcquisitionMode.JOURNAL),
            ReportTime(Instant.parse("2050-01-01T00:00:00Z")), RecordTime(Instant.parse("2050-01-01T00:00:01Z")),
            OriginalSourceContent.Inline(text), SourceIdentityId.parse("temporal.$suffix")),
    )).proposals.single().assertion
}
