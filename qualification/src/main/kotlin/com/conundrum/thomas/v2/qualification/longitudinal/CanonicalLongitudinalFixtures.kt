package com.conundrum.thomas.v2.qualification.longitudinal

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.CoverageTopic
import com.conundrum.thomas.v2.longitudinal.CoverageTopicId
import com.conundrum.thomas.v2.longitudinal.DependencyRole
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.LifeEvent
import com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PersonalEvidenceProvenance
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.SupersessionKind
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.ThomasHypothesis
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import java.time.Instant
import java.time.LocalDate
import java.time.Year

/** Synthetic evidence only. No fixture describes a real person. */
object CanonicalLongitudinalFixtures {
    private val journalTime = Instant.parse("2026-09-03T17:00:00Z")
    private val eventDate = LocalDate.parse("2026-09-03")
    private val eventId = entityId("event-fictional-meeting")

    fun contemporaneousJournalEvent(): LongitudinalEvidenceSnapshot {
        val source = source("source-journal-meeting", AcquisitionMode.JOURNAL, journalTime, "A fictional meeting ended early today.")
        val assertion = assertion(
            "assertion-journal-meeting",
            source.id,
            "event.occurred",
            PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT,
            AssertionValue.EntityReference(eventId),
            EventTime.CalendarDate(eventDate),
        )
        return LongitudinalEvidenceSnapshot(
            sources = listOf(source),
            assertions = listOf(assertion),
            entities = listOf(LifeEvent(eventId, "fictional meeting", setOf(assertion.id))),
        ).requireValid()
    }

    fun retrospectiveBiographerAccount(): LongitudinalEvidenceSnapshot {
        val journal = contemporaneousJournalEvent()
        val report = Instant.parse("2031-02-10T18:30:00Z")
        val source = source("source-biographer-meeting", AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, report, "Years ago, a fictional meeting ended early.")
        val assertion = assertion(
            "assertion-biographer-meeting",
            source.id,
            "event.recollected",
            PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT,
            AssertionValue.EntityReference(eventId),
            EventTime.ApproximateDate(eventDate, com.conundrum.thomas.v2.longitudinal.ApproximationPrecision.DAYS),
            uncertainty = AssertionUncertainty.APPROXIMATE,
        )
        val originalEvent = journal.entities.single() as LifeEvent
        return journal.copy(
            sources = journal.sources + source,
            assertions = journal.assertions + assertion,
            entities = listOf(originalEvent.copy(supportingAssertionIds = originalEvent.supportingAssertionIds + assertion.id)),
        ).requireValid()
    }

    fun contradictoryAccounts(): LongitudinalEvidenceSnapshot {
        val base = retrospectiveBiographerAccount()
        val journalAssertion = base.assertions.first()
        val biographerAssertion = base.assertions.last()
        val journalDetail = journalAssertion.copy(
            id = assertionId("assertion-journal-location"),
            predicate = predicate("event.location-detail", PredicateSemantics.LOCATION),
            value = AssertionValue.Text("fictional office"),
        )
        val retrospectiveDetail = biographerAssertion.copy(
            id = assertionId("assertion-biographer-location"),
            predicate = predicate("event.location-detail", PredicateSemantics.LOCATION),
            value = AssertionValue.Text("fictional cafe"),
        )
        val event = base.entities.single() as LifeEvent
        return base.copy(
            assertions = base.assertions + journalDetail + retrospectiveDetail,
            entities = listOf(event.copy(supportingAssertionIds = event.supportingAssertionIds + setOf(journalDetail.id, retrospectiveDetail.id))),
            contradictions = listOf(
                ContradictionRelation(
                    relationId("relation-location-contradiction"),
                    journalDetail.id,
                    retrospectiveDetail.id,
                    rationale = "The two preserved accounts name different locations.",
                ),
            ),
        ).requireValid()
    }

    fun explicitCorrection(): LongitudinalEvidenceSnapshot {
        val firstTime = Instant.parse("2028-01-01T12:00:00Z")
        val correctionTime = Instant.parse("2028-01-03T12:00:00Z")
        val event = entityId("event-fictional-move")
        val originalSource = source("source-biographer-move", AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE, firstTime, "The fictional move was in 2011.")
        val correctionSource = source("source-correction-move", AcquisitionMode.USER_CORRECTION, correctionTime, "I checked; the fictional move was in 2012.")
        val original = assertion("assertion-move-2011", originalSource.id, "event.time", PredicateSemantics.TEMPORAL,
            AssertionValue.TimeReference(EventTime.ApproximateYear(Year.of(2011))), EventTime.ApproximateYear(Year.of(2011)))
        val correction = assertion("assertion-move-2012", correctionSource.id, "event.time", PredicateSemantics.TEMPORAL,
            AssertionValue.TimeReference(EventTime.ApproximateYear(Year.of(2012))), EventTime.ApproximateYear(Year.of(2012)))
        return LongitudinalEvidenceSnapshot(
            sources = listOf(originalSource, correctionSource),
            assertions = listOf(original, correction),
            entities = listOf(LifeEvent(event, "fictional move", setOf(original.id, correction.id))),
            corrections = listOf(CorrectionRelation(relationId("relation-move-correction"), correction.id, original.id,
                CorrectionEffect.CORRECTS_AND_SUPERSEDES, "The user explicitly corrected the year after checking.")),
            supersessions = listOf(SupersessionRelation(relationId("relation-move-supersession"), ClaimReference.Assertion(correction.id),
                ClaimReference.Assertion(original.id), SupersessionKind.CORRECTS, "The correction supersedes the earlier date without deleting it.")),
        ).requireValid()
    }

    fun userInterpretation(): LongitudinalEvidenceSnapshot {
        val source = source("source-biographer-father-job", AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE,
            Instant.parse("2029-03-04T10:00:00Z"), "I think my fictional father hated that job.")
        val person = entityId("person-fictional-father")
        val assertion = EvidenceAssertion(
            assertionId("assertion-father-job-interpretation"), source.id, AssertionSubject.Entity(person),
            predicate("person.internal-attitude", PredicateSemantics.THIRD_PARTY_INTERNAL_STATE),
            AssertionValue.Text("hated the fictional job"), UserEvidenceKind.USER_INTERPRETATION,
            AssertionUncertainty.STATED_AS_UNCERTAIN,
        )
        return LongitudinalEvidenceSnapshot(
            sources = listOf(source), assertions = listOf(assertion),
            entities = listOf(Person(person, "fictional father", setOf(assertion.id))),
        ).requireValid()
    }

    fun thomasHypothesis(): LongitudinalEvidenceSnapshot {
        val source = source("source-journal-two-events", AcquisitionMode.JOURNAL, Instant.parse("2030-04-01T09:00:00Z"),
            "Two fictional schedule changes were difficult this month.")
        val a = assertion("assertion-change-one", source.id, "event.change", PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT,
            AssertionValue.Text("first fictional schedule change"), EventTime.CalendarDate(LocalDate.parse("2030-03-05")))
        val b = assertion("assertion-change-two", source.id, "event.change", PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT,
            AssertionValue.Text("second fictional schedule change"), EventTime.CalendarDate(LocalDate.parse("2030-03-29")))
        val hypothesis = ThomasHypothesis(
            hypothesisId("hypothesis-change-recurrence"), AssertionSubject.User,
            predicate("pattern.possible-recurrence", PredicateSemantics.EVALUATION_OR_MEANING),
            AssertionValue.Text("schedule changes may be a recurring concern"), HypothesisStatus.TENTATIVE,
            RecordTime(Instant.parse("2030-04-01T09:00:03Z")), "Two separate assertions may indicate recurrence; this is not a user assertion.",
        )
        return LongitudinalEvidenceSnapshot(
            sources = listOf(source), assertions = listOf(a, b), hypotheses = listOf(hypothesis),
            hypothesisDependencies = listOf(
                HypothesisDependency(relationId("dependency-recurrence-one"), hypothesis.id, ClaimReference.Assertion(a.id), DependencyRole.SUPPORTS, "First asserted event."),
                HypothesisDependency(relationId("dependency-recurrence-two"), hypothesis.id, ClaimReference.Assertion(b.id), DependencyRole.SUPPORTS, "Second asserted event."),
            ),
        ).requireValid()
    }

    fun uncertainTime(): LongitudinalEvidenceSnapshot {
        val source = source("source-biographer-around-2012", AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE,
            Instant.parse("2032-06-01T12:00:00Z"), "A fictional course began around 2012; I do not remember exactly.")
        val assertion = assertion("assertion-course-around-2012", source.id, "event.time", PredicateSemantics.TEMPORAL,
            AssertionValue.TimeReference(EventTime.ApproximateYear(Year.of(2012))), EventTime.ApproximateYear(Year.of(2012)),
            AssertionUncertainty.APPROXIMATE)
        return LongitudinalEvidenceSnapshot(sources = listOf(source), assertions = listOf(assertion)).requireValid()
    }

    fun unresolvedPersonIdentity(): LongitudinalEvidenceSnapshot {
        val time = Instant.parse("2033-01-01T10:00:00Z")
        val journal = source("source-journal-sam", AcquisitionMode.JOURNAL, time, "Fictional Sam called today.")
        val biography = source("source-biographer-college-sam", AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE,
            time.plusSeconds(60), "My fictional friend Sam from college used to call often.")
        val a = assertion("assertion-journal-sam", journal.id, "person.reference", PredicateSemantics.RELATIONSHIP_OR_ROLE, AssertionValue.Text("Sam"))
        val b = assertion("assertion-college-sam", biography.id, "person.reference", PredicateSemantics.RELATIONSHIP_OR_ROLE, AssertionValue.Text("friend Sam from college"))
        val first = Person(entityId("person-sam-journal"), "Sam", setOf(a.id))
        val second = Person(entityId("person-sam-college"), "friend Sam from college", setOf(b.id))
        return LongitudinalEvidenceSnapshot(
            sources = listOf(journal, biography), assertions = listOf(a, b), entities = listOf(first, second),
            identityLinks = listOf(EntityIdentityLink(identityId("identity-sam-unresolved"), first.id, second.id,
                EntityIdentityStatus.UNRESOLVED, rationale = "Names overlap, but identity is not established.")),
        ).requireValid()
    }

    fun privateAndDeclinedCoverage(): LongitudinalEvidenceSnapshot = LongitudinalEvidenceSnapshot(
        coverageTopics = listOf(
            CoverageTopic(coverageId("coverage-private-topic"), "synthetic private topic", InformationCoverageStatus.PRIVATE),
            CoverageTopic(coverageId("coverage-declined-topic"), "synthetic declined topic", InformationCoverageStatus.DECLINED),
        ),
    ).requireValid()

    private fun source(id: String, mode: AcquisitionMode, report: Instant, content: String) = SourceRecord(
        SourceRecordId.parse(id),
        PersonalEvidenceProvenance(mode, InteractionId.parse("interaction-$id")),
        ReportTime(report), RecordTime(report.plusSeconds(1)), OriginalSourceContent.Inline(content),
    )

    private fun assertion(
        id: String,
        sourceId: SourceRecordId,
        concept: String,
        semantics: PredicateSemantics,
        value: AssertionValue,
        eventTime: EventTime = EventTime.Unknown("No event time stated"),
        uncertainty: AssertionUncertainty = AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
    ) = EvidenceAssertion(assertionId(id), sourceId, AssertionSubject.User, predicate(concept, semantics), value,
        UserEvidenceKind.EXPLICIT_USER_ASSERTION, uncertainty, eventTime)

    private fun assertionId(value: String) = AssertionId.parse(value)
    private fun entityId(value: String) = LifeEntityId.parse(value)
    private fun relationId(value: String) = EvidenceRelationId.parse(value)
    private fun identityId(value: String) = IdentityLinkId.parse(value)
    private fun coverageId(value: String) = CoverageTopicId.parse(value)
    private fun hypothesisId(value: String) = HypothesisId.parse(value)
    private fun predicate(value: String, semantics: PredicateSemantics) = AssertionPredicate(PersonalConceptId.parse(value), semantics)
}
