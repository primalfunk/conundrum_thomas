package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus
import com.conundrum.thomas.v2.longitudinal.LifeEvent
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.qualification.longitudinal.CanonicalLongitudinalFixtures
import java.time.LocalDate
import java.time.Year
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalFixtureTest {
    @Test fun `fixture A contemporaneous Journal event keeps three time coordinates`() {
        val snapshot = CanonicalLongitudinalFixtures.contemporaneousJournalEvent()
        val source = snapshot.sources.single()
        val assertion = snapshot.assertions.single()
        val coordinates = snapshot.temporalCoordinates(assertion.id)
        assertEquals(AcquisitionMode.JOURNAL, source.provenance.acquisitionMode)
        assertEquals(EventTime.CalendarDate(LocalDate.parse("2026-09-03")), coordinates.eventTime)
        assertEquals(source.reportTime, coordinates.reportTime)
        assertEquals(source.recordTime, coordinates.recordTime)
        assertNotEquals(coordinates.reportTime.value, coordinates.recordTime.value)
    }

    @Test fun `fixture B later Biographer account coexists and links to one event`() {
        val snapshot = CanonicalLongitudinalFixtures.retrospectiveBiographerAccount()
        val original = CanonicalLongitudinalFixtures.contemporaneousJournalEvent()
        assertEquals(2, snapshot.sources.size)
        assertEquals(setOf(AcquisitionMode.JOURNAL, AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE),
            snapshot.sources.map { it.provenance.acquisitionMode }.toSet())
        assertEquals(2, snapshot.assertions.size)
        assertEquals(2, (snapshot.entities.single() as LifeEvent).supportingAssertionIds.size)
        assertEquals(original.sources.single(), snapshot.sources.first())
        assertEquals(original.assertions.single(), snapshot.assertions.first())
        assertTrue(snapshot.sources.first().reportTime.value < snapshot.sources.last().reportTime.value)
    }

    @Test fun `fixture C contradictory accounts remain present`() {
        val snapshot = CanonicalLongitudinalFixtures.contradictoryAccounts()
        val relation = snapshot.contradictions.single()
        assertTrue(snapshot.assertions.any { it.id == relation.leftAssertionId })
        assertTrue(snapshot.assertions.any { it.id == relation.rightAssertionId })
        assertNotEquals(
            snapshot.assertions.single { it.id == relation.leftAssertionId }.value,
            snapshot.assertions.single { it.id == relation.rightAssertionId }.value,
        )
    }

    @Test fun `fixture D correction preserves original and records supersession`() {
        val snapshot = CanonicalLongitudinalFixtures.explicitCorrection()
        val correction = snapshot.corrections.single()
        assertEquals(2, snapshot.assertions.size)
        assertTrue(snapshot.assertions.any { it.id == correction.correctedAssertionId })
        assertTrue(snapshot.assertions.any { it.id == correction.correctingAssertionId })
        assertEquals(AcquisitionMode.USER_CORRECTION,
            snapshot.sources.single { it.id == snapshot.assertions.single { a -> a.id == correction.correctingAssertionId }.sourceRecordId }.provenance.acquisitionMode)
        assertEquals(1, snapshot.supersessions.size)
    }

    @Test fun `fixture E other-person motivation remains user interpretation`() {
        val snapshot = CanonicalLongitudinalFixtures.userInterpretation()
        val assertion = snapshot.assertions.single()
        assertEquals(UserEvidenceKind.USER_INTERPRETATION, assertion.kind)
        assertTrue(assertion.value is AssertionValue.Text)
        assertTrue(snapshot.sources.single().originalContent.toString().contains("think"))
    }

    @Test fun `fixture F Thomas hypothesis is separate and dependencies are inspectable`() {
        val snapshot = CanonicalLongitudinalFixtures.thomasHypothesis()
        val hypothesis = snapshot.hypotheses.single()
        assertEquals(HypothesisStatus.TENTATIVE, hypothesis.status)
        assertEquals(2, snapshot.hypothesisDependencies.count { it.dependentHypothesisId == hypothesis.id })
        assertFalse(snapshot.assertions.any { it.value == hypothesis.proposedValue })
    }

    @Test fun `fixture G around 2012 remains approximate year`() {
        val time = CanonicalLongitudinalFixtures.uncertainTime().assertions.single().eventTime
        assertTrue(time is EventTime.ApproximateYear)
        assertEquals(Year.of(2012), (time as EventTime.ApproximateYear).year)
    }

    @Test fun `fixture H person identity stays unresolved without merge`() {
        val snapshot = CanonicalLongitudinalFixtures.unresolvedPersonIdentity()
        assertEquals(2, snapshot.entities.filterIsInstance<Person>().size)
        assertEquals(com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus.UNRESOLVED, snapshot.identityLinks.single().status)
        assertNotEquals(snapshot.identityLinks.single().leftEntityId, snapshot.identityLinks.single().rightEntityId)
    }

    @Test fun `fixture I private and declined coverage are valid final states`() {
        val snapshot = CanonicalLongitudinalFixtures.privateAndDeclinedCoverage()
        assertEquals(setOf(InformationCoverageStatus.PRIVATE, InformationCoverageStatus.DECLINED),
            snapshot.coverageTopics.map { it.status }.toSet())
        assertTrue(snapshot.validationIssues().isEmpty())
        assertTrue(snapshot.sources.isEmpty())
    }
}
