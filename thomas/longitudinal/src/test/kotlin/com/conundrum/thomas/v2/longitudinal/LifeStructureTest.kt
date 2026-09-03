package com.conundrum.thomas.v2.longitudinal

import java.time.Instant
import java.time.Year
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeStructureTest {
    @Test fun `core life structures remain evidence linked and cross references validate`() {
        val sourceId = SourceRecordId.parse("source-life-structure")
        val assertionId = AssertionId.parse("assertion-life-structure")
        val source = SourceRecord(
            sourceId, PersonalEvidenceProvenance(AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE),
            ReportTime(Instant.parse("2030-01-01T10:00:00Z")), RecordTime(Instant.parse("2030-01-01T10:00:01Z")),
            OriginalSourceContent.Inline("Synthetic account containing several life structures."),
        )
        val assertion = EvidenceAssertion(
            assertionId, sourceId, AssertionSubject.User,
            AssertionPredicate(PersonalConceptId.parse("life.structure"), PredicateSemantics.OTHER),
            AssertionValue.Text("synthetic structures"), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
        )
        fun id(value: String) = LifeEntityId.parse(value)
        val user = Person(id("person-synthetic-user"), "synthetic user", setOf(assertionId))
        val other = Person(id("person-synthetic-other"), "synthetic other", setOf(assertionId))
        val place = Place(id("place-synthetic-city"), "synthetic city", setOf(assertionId))
        val event = LifeEvent(id("event-synthetic-change"), "synthetic change", setOf(assertionId), setOf(user.id, other.id), setOf(place.id))
        val period = LifePeriod(id("period-synthetic-college"), "synthetic college period", setOf(assertionId), EventTime.ApproximateYear(Year.of(2012)))
        val relationship = Relationship(id("relationship-synthetic-friends"), "synthetic friendship", setOf(assertionId), setOf(user.id, other.id))
        val role = Role(id("role-synthetic-student"), "synthetic student role", setOf(assertionId), user.id, period.id)
        val decision = Decision(id("decision-synthetic-move"), "synthetic move decision", setOf(assertionId), setOf(user.id))
        val behavior = Behavior(id("behavior-synthetic-call"), "synthetic call", setOf(assertionId), setOf(user.id))
        val coping = CopingResponse(id("coping-synthetic-walk"), "synthetic walk", setOf(assertionId), user.id, event.id)
        val outcome = Outcome(id("outcome-synthetic-result"), "synthetic result", setOf(assertionId), decision.id)
        val snapshot = LongitudinalEvidenceSnapshot(
            sources = listOf(source), assertions = listOf(assertion),
            entities = listOf(user, other, place, event, period, relationship, role, decision, behavior, coping, outcome),
        ).requireValid()
        assertEquals(11, snapshot.entities.size)
        assertEquals(listOf(source), snapshot.sourcesFor(event.id))
    }

    @Test fun `unresolved identity is valid without merge evidence`() {
        val left = LifeEntityId.parse("person-reference-one")
        val right = LifeEntityId.parse("person-reference-two")
        val link = EntityIdentityLink(
            IdentityLinkId.parse("identity-reference-unresolved"), left, right,
            EntityIdentityStatus.UNRESOLVED, rationale = "Insufficient identity evidence.",
        )
        assertTrue(link.supportingAssertionIds.isEmpty())
        assertEquals(left to right, link.canonicalPair())
    }
}
