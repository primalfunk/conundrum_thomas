package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.ApproximationPrecision
import com.conundrum.thomas.v2.longitudinal.CalendarBoundary
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.RelativeTemporalRelation
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.admitSource
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertAccepted
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.path
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.sourceDraft
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalTemporalPersistenceTest {
    @Test fun `all required temporal shapes round trip without precision change`() {
        val values = listOf<EventTime>(
            EventTime.ExactInstant(Instant.parse("2012-01-02T03:04:05Z")),
            EventTime.CalendarDate(LocalDate.parse("2012-01-02")),
            EventTime.ApproximateDate(LocalDate.parse("2012-01-02"), ApproximationPrecision.MONTHS),
            EventTime.ApproximateYear(Year.of(2012)),
            EventTime.Range(CalendarBoundary.Month(YearMonth.parse("2012-01")), CalendarBoundary.CalendarYear(Year.of(2013)), true, true),
            EventTime.RelativePeriod("Synthetic college period", RelativeTemporalRelation.DURING),
            EventTime.OngoingInterval(CalendarBoundary.CalendarYear(Year.of(2012)), "Synthetic ongoing interval"),
            EventTime.UncertainChronology("Synthetic uncertain chronology", listOf("before move", "after course")),
            EventTime.Unknown("Synthetic time not remembered"),
        )
        SyntheticLongitudinalStoreHarness(path("temporal-round-trip")).use { harness ->
            values.forEachIndexed { index, time -> assertAccepted(admitSource(harness, sourceDraft("temporal-$index", eventTime = time))) }
            harness.reopen()
            val actual = values.indices.map { index -> harness.store.reader.source(SourceIdentityId.parse("source-temporal-$index"))!!.eventTime }
            assertEquals(values, actual)
        }
    }

    @Test fun `source revision API cannot accept replacement event time`() {
        val fields = LongitudinalWriteOperation.AppendSourceRevision::class.java.declaredFields.map { it.name }
        assertFalse(fields.any { it.equals("eventTime", ignoreCase = true) })
        assertFalse(fields.any { it.equals("recordTime", ignoreCase = true) })
    }

    @Test fun `event report and record times remain independent after persistence`() {
        val event = EventTime.CalendarDate(LocalDate.parse("2012-01-02"))
        val draft = sourceDraft("separate-times", eventTime = event)
        SyntheticLongitudinalStoreHarness(path("separate-times")).use { harness ->
            val result = admitSource(harness, draft)
            assertAccepted(result)
            val stored = harness.store.reader.source(draft.stableSourceId)!!
            assertEquals(event, stored.eventTime)
            assertEquals(draft.reportTime, stored.reportTime)
            assertEquals(result.receipt!!.recordTime, stored.recordTime.value)
            assertTrue(stored.recordTime.value.isAfter(stored.reportTime.value))
        }
    }

    @Test fun `invalid range remains impossible before admission`() {
        assertThrows(IllegalArgumentException::class.java) {
            EventTime.Range(CalendarBoundary.Date(LocalDate.parse("2013-01-01")), CalendarBoundary.Date(LocalDate.parse("2012-01-01")))
        }
    }
}
