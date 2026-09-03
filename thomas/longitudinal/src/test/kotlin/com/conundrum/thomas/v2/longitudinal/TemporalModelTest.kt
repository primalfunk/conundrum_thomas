package com.conundrum.thomas.v2.longitudinal

import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TemporalModelTest {
    @Test fun `event report and record time are independent coordinates`() {
        val event = EventTime.CalendarDate(LocalDate.parse("2012-06-03"))
        val report = ReportTime(Instant.parse("2026-09-03T10:00:00Z"))
        val record = RecordTime(Instant.parse("2026-09-03T10:00:01Z"))
        val coordinates = TemporalCoordinates(event, report, record)
        assertEquals(event, coordinates.eventTime)
        assertNotEquals(report.value, record.value)
        assertNotEquals((event as EventTime.CalendarDate).value.toString(), report.value.toString())
    }

    @Test fun `record cannot precede report`() {
        assertThrows(IllegalArgumentException::class.java) {
            TemporalCoordinates(
                EventTime.Unknown(),
                ReportTime(Instant.parse("2026-09-03T10:00:01Z")),
                RecordTime(Instant.parse("2026-09-03T10:00:00Z")),
            )
        }
    }

    @Test fun `approximate year preserves no invented day or month`() {
        val time = EventTime.ApproximateYear(Year.of(2012))
        assertEquals(Year.of(2012), time.year)
        assertEquals(EventTime.ApproximateYear::class.java, time.javaClass)
    }

    @Test fun `approximate date preserves declared precision`() {
        val time = EventTime.ApproximateDate(LocalDate.parse("2012-12-15"), ApproximationPrecision.SEASON)
        assertEquals(ApproximationPrecision.SEASON, time.precision)
    }

    @Test fun `range accepts mixed calendar boundary precision`() {
        val time = EventTime.Range(CalendarBoundary.Month(YearMonth.of(2012, 1)), CalendarBoundary.CalendarYear(Year.of(2013)))
        assertEquals(LocalDate.parse("2012-01-01"), time.start.earliest)
        assertEquals(LocalDate.parse("2013-12-31"), time.end.latest)
    }

    @Test fun `invalid temporal range is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            EventTime.Range(CalendarBoundary.Date(LocalDate.parse("2014-01-01")), CalendarBoundary.Date(LocalDate.parse("2013-01-01")))
        }
    }

    @Test fun `relative ongoing and unknown time remain explicit`() {
        val anchor = LifeEntityId.parse("event-fictional-move")
        val relative = EventTime.BeforeOrAfter(anchor, RelativeTemporalRelation.BEFORE, "a couple of years before the move")
        val ongoing = EventTime.OngoingInterval(null, "for several months and still ongoing")
        val unknown = EventTime.Unknown("the synthetic speaker does not remember")
        assertEquals(RelativeTemporalRelation.BEFORE, relative.relation)
        assertEquals(null, ongoing.knownStart)
        assertTrue(unknown.statedReason!!.contains("remember"))
    }

    @Test fun `uncertain chronology requires alternatives`() {
        assertThrows(IllegalArgumentException::class.java) { EventTime.UncertainChronology("unclear order", listOf("only one")) }
        val uncertain = EventTime.UncertainChronology("unclear order", listOf("before college", "during college"))
        assertEquals(2, uncertain.candidateLabels.size)
    }
}
