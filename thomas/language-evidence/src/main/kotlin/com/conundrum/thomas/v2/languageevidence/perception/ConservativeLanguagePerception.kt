package com.conundrum.thomas.v2.languageevidence.perception

import com.conundrum.thomas.v2.longitudinal.ApproximationPrecision
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPolarity
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.CalendarBoundary
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionAdjudication
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.LifeEvent
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.Place
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.RelativeTemporalRelation
import com.conundrum.thomas.v2.longitudinal.SourceNormalization
import com.conundrum.thomas.v2.longitudinal.SourceSpanGrounding
import com.conundrum.thomas.v2.longitudinal.SupersessionKind
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.Year

/**
 * A deliberately bounded grammar. It recognizes only audited forms and succeeds conservatively;
 * it is not a general English parser and does not infer therapeutic meaning.
 */
class ConservativeLanguagePerception : LanguagePerceptionEngine {
    override fun perceive(source: CommittedSourceText, context: PerceptionContext): LanguagePerceptionResult {
        val text = source.exactText
        if (text.isBlank() || text.length > MAX_SOURCE_LENGTH || '\u0000' in text) {
            return result(source, PerceptionDisposition.INVALID_INPUT, unresolved = unresolved("INVALID_SOURCE_TEXT"))
        }
        val trimmed = text.trim()
        CORRECTION_YEAR.matchEntire(trimmed)?.let { match ->
            return correction(source, match.groupValues[1], context.correctionTarget)
        }
        DATE_MOVE.matchEntire(trimmed)?.let { match ->
            return movementAt(source, match.groupValues[2], EventTime.CalendarDate(LocalDate.parse(match.groupValues[1])))
        }
        INSTANT_EVENT.matchEntire(trimmed)?.let { match ->
            return one(source, assertion(source, "arrived-instant", AssertionSubject.User, "event.arrived",
                PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.BooleanValue(true),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EVENT_REFERENCE, EventTime.ExactInstant(Instant.parse(match.groupValues[1]))))
        }
        YEAR_RANGE_RESIDENCE.matchEntire(trimmed)?.let { match ->
            return residenceRange(source, match.groupValues[3], match.groupValues[1], match.groupValues[2])
        }
        ONGOING_RESIDENCE.matchEntire(trimmed)?.let { match ->
            return ongoingResidence(source, match.groupValues[1], match.groupValues[2])
        }
        WORK_INTERRUPTION.matchEntire(trimmed)?.let {
            val today = EventTime.RelativePeriod("today", RelativeTemporalRelation.DURING)
            val experience = assertion(source, "work-exhaustion", AssertionSubject.User, "self.reported-work-experience",
                PredicateSemantics.USER_INTERNAL_EXPERIENCE, AssertionValue.Text("exhausting"),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, today)
            val interruptions = assertion(source, "work-interruptions", AssertionSubject.User, "event.reported-work-interruptions",
                PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.Text("kept getting interrupted"),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EVENT_REFERENCE, today)
            return result(source, PerceptionDisposition.UNDERSTOOD,
                proposals = listOf(EvidenceProposal(experience), EvidenceProposal(interruptions)))
        }
        THREE_LUNCH_REPORTS.matchEntire(trimmed)?.let {
            val reports = listOf("today", "again", "yesterday").mapIndexed { index, scope ->
                EvidenceProposal(assertion(source, "reported-lunch-${index + 1}", AssertionSubject.User,
                    "reported.behavior.skipped-lunch", PredicateSemantics.ACTION_OR_BEHAVIOR,
                    AssertionValue.Text("skipped lunch"), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
                    AssertionUncertainty.STATED_WITHOUT_QUALIFICATION, EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION,
                    EventTime.RelativePeriod(scope, RelativeTemporalRelation.DURING)))
            }
            return result(source, PerceptionDisposition.UNDERSTOOD, proposals = reports)
        }
        REMEMBERED_MOVE.matchEntire(trimmed)?.let { match ->
            val time = EventTime.ApproximateYear(Year.of(match.groupValues[1].toInt()))
            return one(source, assertion(source, "remembered-move", AssertionSubject.User, "event.moved-unspecified-location",
                PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.BooleanValue(true),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.APPROXIMATE,
                EvidenceEpistemicClass.EVENT_REFERENCE, time))
        }
        MAYBE_BEFORE_COLLEGE.matchEntire(trimmed)?.let {
            val time = EventTime.RelativePeriod("before college", RelativeTemporalRelation.BEFORE)
            return partial(source, assertion(source, "uncertain-before-college", AssertionSubject.User,
                "temporal.user-suspected-before-college", PredicateSemantics.TEMPORAL,
                AssertionValue.TimeReference(time), UserEvidenceKind.USER_INTERPRETATION,
                AssertionUncertainty.STATED_AS_UNCERTAIN, EvidenceEpistemicClass.USER_INTERPRETATION, time),
                UnresolvedPerception(UnresolvedPerceptionKind.AMBIGUOUS_REFERENT, "RELATIVE_EVENT_REFERENT_UNRESOLVED"))
        }
        BOUGHT_GROCERIES.matchEntire(trimmed)?.let {
            val time = EventTime.RelativePeriod("after work", RelativeTemporalRelation.AFTER)
            return one(source, assertion(source, "bought-groceries", AssertionSubject.User, "event.bought-groceries",
                PredicateSemantics.ACTION_OR_BEHAVIOR, AssertionValue.BooleanValue(true),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EVENT_REFERENCE, time))
        }
        if (trimmed.count { it == '?' } > 1 || YEAR.findAll(trimmed).count() > 1) {
            return ambiguous(source, "MULTIPLE_PROPOSITIONS_OR_DATES")
        }
        if (DOUBLE_NEGATION.containsMatchIn(trimmed)) return ambiguous(source, "DOUBLE_NEGATION")
        if (AMBIGUOUS_HUMOR.containsMatchIn(trimmed)) return ambiguous(source, "AMBIGUOUS_HUMOR", NonassertiveKind.AMBIGUOUS_HUMOR)
        if (trimmed.endsWith('?')) return nonassertive(source, NonassertiveKind.QUESTION, "INTERROGATIVE_NOT_ASSERTED")
        if (COUNTERFACTUAL.containsMatchIn(trimmed)) return nonassertive(source, NonassertiveKind.COUNTERFACTUAL, "COUNTERFACTUAL_NOT_EVENT")
        if (HYPOTHETICAL.containsMatchIn(trimmed)) return nonassertive(source, NonassertiveKind.HYPOTHETICAL, "HYPOTHETICAL_NOT_ASSERTED")
        if (trimmed.startsWith('"') && trimmed.endsWith('"')) return nonassertive(source, NonassertiveKind.QUOTATION, "UNATTRIBUTED_QUOTATION")
        if (AMBIGUOUS_PRONOUN.containsMatchIn(trimmed)) return ambiguous(source, "AMBIGUOUS_PRONOUN")

        MOVE.matchEntire(trimmed)?.let { match ->
            return movement(source, match.groupValues[2], match.groupValues[3], match.groupValues[1].isNotBlank())
        }
        LIVED.matchEntire(trimmed)?.let { match ->
            return residence(source, match.groupValues[1], match.groupValues[2])
        }
        APPROX_JOB.matchEntire(trimmed)?.let { match ->
            val year = EventTime.ApproximateYear(Year.of(match.groupValues[1].toInt()))
            return one(source, assertion(source, "job-change", AssertionSubject.User, "event.changed-job",
                PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.BooleanValue(true),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.APPROXIMATE,
                EvidenceEpistemicClass.EVENT_REFERENCE, year))
        }
        MAYBE_YEAR.matchEntire(trimmed)?.let { match ->
            val time = EventTime.ApproximateYear(Year.of(match.groupValues[1].toInt()))
            return partial(source, assertion(source, "uncertain-year", AssertionSubject.User, "temporal.candidate-year",
                PredicateSemantics.TEMPORAL, AssertionValue.TimeReference(time), UserEvidenceKind.USER_INTERPRETATION,
                AssertionUncertainty.STATED_AS_UNCERTAIN, EvidenceEpistemicClass.USER_INTERPRETATION, time),
                UnresolvedPerception(UnresolvedPerceptionKind.AMBIGUOUS_REFERENT, "UNRESOLVED_TEMPORAL_REFERENT"))
        }
        UNKNOWN_TIME.matchEntire(trimmed)?.let {
            val unknown = EventTime.Unknown("User explicitly reported not remembering event time")
            return partial(source, assertion(source, "unknown-time", AssertionSubject.User, "temporal.unknown",
                PredicateSemantics.TEMPORAL, AssertionValue.TimeReference(unknown), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
                AssertionUncertainty.STATED_WITHOUT_QUALIFICATION, EvidenceEpistemicClass.EVENT_REFERENCE, unknown),
                UnresolvedPerception(UnresolvedPerceptionKind.UNKNOWN_EVENT_TIME, "EVENT_TIME_EXPLICITLY_UNKNOWN"))
        }
        RELATIVE_JOB.matchEntire(trimmed)?.let {
            val time = EventTime.RelativePeriod("a few years later", RelativeTemporalRelation.AFTER)
            return partial(source, assertion(source, "relative-job", AssertionSubject.User, "event.changed-job",
                PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.BooleanValue(true),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EVENT_REFERENCE, time),
                UnresolvedPerception(UnresolvedPerceptionKind.UNKNOWN_EVENT_TIME, "RELATIVE_ANCHOR_UNRESOLVED"))
        }
        DURATION_EMOTION.matchEntire(trimmed)?.let { match ->
            val time = EventTime.RelativePeriod("for a while", RelativeTemporalRelation.DURATION)
            return one(source, assertion(source, "duration-emotion", AssertionSubject.User, "self.reported-emotion",
                PredicateSemantics.USER_INTERNAL_EXPERIENCE, AssertionValue.Text(match.groupValues[1].lowercase()),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.APPROXIMATE,
                EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, time))
        }
        SEASONAL_JOB.matchEntire(trimmed)?.let {
            val time = EventTime.RelativePeriod("sometime that winter", RelativeTemporalRelation.AROUND)
            return one(source, assertion(source, "seasonal-job", AssertionSubject.User, "event.changed-job",
                PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.BooleanValue(true),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.APPROXIMATE,
                EvidenceEpistemicClass.EVENT_REFERENCE, time))
        }
        SELF_EMOTION.matchEntire(trimmed)?.let { match ->
            val time = EventTime.RelativePeriod(match.groupValues[2].lowercase(), RelativeTemporalRelation.DURING)
            return one(source, assertion(source, "self-emotion", AssertionSubject.User, "self.reported-emotion",
                PredicateSemantics.USER_INTERNAL_EXPERIENCE, AssertionValue.Text(match.groupValues[1].lowercase()),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, time))
        }
        SELF_FEELING.matchEntire(trimmed)?.let { match ->
            val time = EventTime.RelativePeriod(match.groupValues[2].lowercase(), RelativeTemporalRelation.DURING)
            return one(source, assertion(source, "self-feeling", AssertionSubject.User, "self.reported-emotion",
                PredicateSemantics.USER_INTERNAL_EXPERIENCE, AssertionValue.Text(match.groupValues[1].lowercase()),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, time))
        }
        THINK_PERSON_EMOTION.matchEntire(trimmed)?.let { match ->
            val person = personEntity(source, match.groupValues[1], "interpreted-person")
            val claim = assertion(source, "interpreted-person", AssertionSubject.Entity(person.id), "user.interprets-other-emotion",
                PredicateSemantics.THIRD_PARTY_INTERNAL_STATE, AssertionValue.Text(match.groupValues[2].lowercase()),
                UserEvidenceKind.USER_INTERPRETATION, AssertionUncertainty.STATED_AS_UNCERTAIN,
                EvidenceEpistemicClass.USER_INTERPRETATION)
            return one(source, claim, listOf(person))
        }
        REPORTED_EMOTION.matchEntire(trimmed)?.let { match ->
            val person = personEntity(source, match.groupValues[1], "third-party-report")
            val claim = assertion(source, "third-party-report", AssertionSubject.Entity(person.id), "third-party.reported-statement",
                PredicateSemantics.OTHER, AssertionValue.Text(match.groupValues[2].lowercase()),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.THIRD_PARTY_REPORT)
            return one(source, claim, listOf(person))
        }
        MOVING_MISTAKE.matchEntire(trimmed)?.let {
            return one(source, assertion(source, "moving-meaning", AssertionSubject.User, "user.interprets-move",
                PredicateSemantics.EVALUATION_OR_MEANING, AssertionValue.Text("mistake"),
                UserEvidenceKind.USER_INTERPRETATION, AssertionUncertainty.STATED_AS_UNCERTAIN,
                EvidenceEpistemicClass.USER_INTERPRETATION))
        }
        PERSON_HATES_ME.matchEntire(trimmed)?.let { match ->
            val person = personEntity(source, match.groupValues[1], "hates-interpretation")
            val claim = assertion(source, "hates-interpretation", AssertionSubject.Entity(person.id), "user.interprets-other-attitude",
                PredicateSemantics.THIRD_PARTY_INTERNAL_STATE, AssertionValue.Text("hates user"),
                UserEvidenceKind.USER_INTERPRETATION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.USER_INTERPRETATION)
            return one(source, claim, listOf(person))
        }
        NOBODY_LIKES.matchEntire(trimmed)?.let {
            return one(source, assertion(source, "self-belief-liked", AssertionSubject.User, "self.belief.social-acceptance",
                PredicateSemantics.EVALUATION_OR_MEANING, AssertionValue.Text("nobody likes me"),
                UserEvidenceKind.USER_INTERPRETATION, AssertionUncertainty.STATED_AS_UNCERTAIN,
                EvidenceEpistemicClass.SELF_BELIEF))
        }
        ALWAYS_FAIL.matchEntire(trimmed)?.let {
            return one(source, assertion(source, "self-belief-relationships", AssertionSubject.User, "self.belief.relationship-failure",
                PredicateSemantics.EVALUATION_OR_MEANING, AssertionValue.Text("always fail at relationships"),
                UserEvidenceKind.USER_INTERPRETATION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.SELF_BELIEF))
        }
        FAIL_EVERYTHING.matchEntire(trimmed)?.let {
            return one(source, assertion(source, "self-belief-everything", AssertionSubject.User, "self.belief-global-failure",
                PredicateSemantics.EVALUATION_OR_MEANING, AssertionValue.Text("fail at everything"),
                UserEvidenceKind.USER_INTERPRETATION, AssertionUncertainty.STATED_AS_UNCERTAIN,
                EvidenceEpistemicClass.SELF_BELIEF))
        }
        JOB_TRAJECTORY.matchEntire(trimmed)?.let { match ->
            val phase = match.groupValues[1].lowercase().replace(" ", "-")
            val time = EventTime.RelativePeriod(phase, RelativeTemporalRelation.DURING)
            return one(source, assertion(source, "job-sentiment", AssertionSubject.User, "self.reported-job-sentiment",
                PredicateSemantics.USER_INTERNAL_EXPERIENCE, AssertionValue.Text(match.groupValues[2].lowercase()),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, time))
        }
        REPORTED_BEHAVIOR.matchEntire(trimmed)?.let { match ->
            val time = EventTime.RelativePeriod(match.groupValues[2].lowercase(), RelativeTemporalRelation.DURING)
            return one(source, assertion(source, "reported-behavior", AssertionSubject.User, "reported.behavior.${stableSlug(match.groupValues[1])}",
                PredicateSemantics.ACTION_OR_BEHAVIOR, AssertionValue.Text(match.groupValues[1].lowercase()),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
                EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION, time))
        }
        MODAL_SPECULATION.matchEntire(trimmed)?.let {
            return nonassertive(source, NonassertiveKind.HYPOTHETICAL, "MODAL_SPECULATION")
        }
        return result(source, PerceptionDisposition.NO_EVIDENCE_PROPOSAL,
            unresolved = unresolved("BOUNDED_GRAMMAR_UNSUPPORTED", UnresolvedPerceptionKind.UNSUPPORTED_LANGUAGE))
    }

    private fun movement(source: CommittedSourceText, city: String, yearText: String, negated: Boolean): LanguagePerceptionResult {
        val time = calendarYear(yearText)
        val claim = assertion(source, "move", AssertionSubject.User, "event.moved-location",
            PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.Text(city),
            UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
            EvidenceEpistemicClass.EVENT_REFERENCE, time,
            if (negated) AssertionPolarity.NEGATIVE else AssertionPolarity.AFFIRMATIVE)
        return one(source, claim, moveEntities(source, claim.id, city))
    }

    private fun movementAt(source: CommittedSourceText, city: String, time: EventTime): LanguagePerceptionResult {
        val claim = assertion(source, "move", AssertionSubject.User, "event.moved-location",
            PredicateSemantics.OBSERVABLE_OR_REPORTED_EVENT, AssertionValue.Text(city),
            UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
            EvidenceEpistemicClass.EVENT_REFERENCE, time)
        return one(source, claim, moveEntities(source, claim.id, city))
    }

    private fun residence(source: CommittedSourceText, city: String, yearText: String): LanguagePerceptionResult {
        val claim = assertion(source, "residence", AssertionSubject.User, "residence.location",
            PredicateSemantics.LOCATION, AssertionValue.Text(city), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION, EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION,
            calendarYear(yearText))
        return one(source, claim, listOf(Place(entityId(source, "residence-place"), city, setOf(claim.id))))
    }

    private fun residenceRange(source: CommittedSourceText, city: String, start: String, end: String): LanguagePerceptionResult {
        val time = EventTime.Range(CalendarBoundary.CalendarYear(Year.of(start.toInt())),
            CalendarBoundary.CalendarYear(Year.of(end.toInt())))
        val claim = assertion(source, "residence", AssertionSubject.User, "residence.location",
            PredicateSemantics.LOCATION, AssertionValue.Text(city), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION, EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION, time)
        return one(source, claim, listOf(Place(entityId(source, "residence-place"), city, setOf(claim.id))))
    }

    private fun ongoingResidence(source: CommittedSourceText, city: String, start: String): LanguagePerceptionResult {
        val time = EventTime.OngoingInterval(CalendarBoundary.CalendarYear(Year.of(start.toInt())), "ongoing since stated year")
        val claim = assertion(source, "ongoing-residence", AssertionSubject.User, "residence.location",
            PredicateSemantics.LOCATION, AssertionValue.Text(city), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION, EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION, time)
        return one(source, claim, listOf(Place(entityId(source, "residence-place"), city, setOf(claim.id))))
    }

    private fun correction(source: CommittedSourceText, yearText: String, target: ClaimReference?): LanguagePerceptionResult {
        val time = calendarYear(yearText)
        val correcting = assertion(source, "correction", AssertionSubject.User, "temporal.corrected-year",
            PredicateSemantics.TEMPORAL, AssertionValue.TimeReference(time), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION, EvidenceEpistemicClass.EVENT_REFERENCE, time)
        if (target == null) {
            return result(source, PerceptionDisposition.PARTIALLY_UNDERSTOOD,
                correction = CorrectionCandidate(correcting, null),
                unresolved = listOf(UnresolvedPerception(UnresolvedPerceptionKind.AMBIGUOUS_CORRECTION_TARGET, "CORRECTION_TARGET_REQUIRED")))
        }
        val supersession = SupersessionRelation(
            EvidenceRelationId.parse("${source.source.id.value}.correction-supersession"),
            ClaimReference.Assertion(correcting.id), target,
            if (target is ClaimReference.Hypothesis) SupersessionKind.INVALIDATES_DERIVED_INTERPRETATION else SupersessionKind.CORRECTS,
            "Explicit user correction with a caller-established unambiguous target",
        )
        val relation = (target as? ClaimReference.Assertion)?.let {
            CorrectionRelation(EvidenceRelationId.parse("${source.source.id.value}.correction"), correcting.id, it.assertionId,
                CorrectionEffect.CORRECTS_AND_SUPERSEDES, "Explicit user correction")
        }
        return result(source, PerceptionDisposition.UNDERSTOOD,
            correction = CorrectionCandidate(correcting, target, relation, supersession))
    }

    private fun assertion(
        source: CommittedSourceText,
        suffix: String,
        subject: AssertionSubject,
        concept: String,
        semantics: PredicateSemantics,
        value: AssertionValue,
        kind: UserEvidenceKind,
        uncertainty: AssertionUncertainty,
        epistemicClass: EvidenceEpistemicClass,
        eventTime: EventTime = EventTime.Unknown("No event time expressed"),
        polarity: AssertionPolarity = AssertionPolarity.AFFIRMATIVE,
    ) = EvidenceAssertion(
        id = AssertionId.parse("${source.source.id.value}.${stableSlug(suffix)}"),
        sourceRecordId = source.source.id,
        subject = subject,
        predicate = AssertionPredicate(PersonalConceptId.parse(concept), semantics),
        value = value,
        kind = kind,
        uncertainty = uncertainty,
        eventTime = eventTime,
        epistemicClass = epistemicClass,
        polarity = polarity,
        sourceGrounding = SourceSpanGrounding(source.source.id, 0, source.exactText.length, source.exactText,
            source.revisionSha256, CT_V2_08_PERCEPTION_VERSION, SourceNormalization.NONE),
    )

    private fun personEntity(source: CommittedSourceText, label: String, suffix: String): Person {
        val assertionId = AssertionId.parse("${source.source.id.value}.${stableSlug(suffix)}")
        return Person(entityId(source, "$suffix-person"), label, setOf(assertionId))
    }

    private fun moveEntities(source: CommittedSourceText, assertionId: AssertionId, city: String): List<LifeEntity> {
        val place = Place(entityId(source, "move-place"), city, setOf(assertionId))
        val event = LifeEvent(entityId(source, "move-event"), "reported move", setOf(assertionId), placeIds = setOf(place.id))
        return listOf(place, event)
    }

    private fun entityId(source: CommittedSourceText, suffix: String) =
        LifeEntityId.parse("${source.source.id.value}.${stableSlug(suffix)}")

    private fun calendarYear(value: String): EventTime {
        val boundary = CalendarBoundary.CalendarYear(Year.of(value.toInt()))
        return EventTime.Range(boundary, boundary)
    }

    private fun one(source: CommittedSourceText, assertion: EvidenceAssertion, entities: List<LifeEntity> = emptyList()) =
        result(source, PerceptionDisposition.UNDERSTOOD, proposals = listOf(EvidenceProposal(assertion, entities)))

    private fun partial(source: CommittedSourceText, assertion: EvidenceAssertion, issue: UnresolvedPerception) =
        result(source, PerceptionDisposition.PARTIALLY_UNDERSTOOD, proposals = listOf(EvidenceProposal(assertion)), unresolved = listOf(issue))

    private fun ambiguous(source: CommittedSourceText, code: String, kind: NonassertiveKind? = null) =
        result(source, PerceptionDisposition.AMBIGUOUS,
            unresolved = listOf(UnresolvedPerception(UnresolvedPerceptionKind.AMBIGUOUS_REFERENT, code, nonassertiveKind = kind)))

    private fun nonassertive(source: CommittedSourceText, kind: NonassertiveKind, code: String) =
        result(source, PerceptionDisposition.NO_EVIDENCE_PROPOSAL,
            unresolved = listOf(UnresolvedPerception(UnresolvedPerceptionKind.NONASSERTIVE_LANGUAGE, code, nonassertiveKind = kind)))

    private fun unresolved(code: String, kind: UnresolvedPerceptionKind = UnresolvedPerceptionKind.UNSUPPORTED_LANGUAGE) =
        listOf(UnresolvedPerception(kind, code))

    private fun result(
        source: CommittedSourceText,
        disposition: PerceptionDisposition,
        proposals: List<EvidenceProposal> = emptyList(),
        correction: CorrectionCandidate? = null,
        unresolved: List<UnresolvedPerception> = emptyList(),
    ) = LanguagePerceptionResult(source.source.id.value, source.revisionSha256, disposition = disposition,
        proposals = proposals, correctionCandidate = correction, unresolved = unresolved)

    private fun stableSlug(value: String): String = value.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "value" }

    companion object {
        private const val MAX_SOURCE_LENGTH = 4_096
        private val YEAR = Regex("\\b(?:19|20)\\d{2}\\b")
        private val DATE_MOVE = Regex("(?i)^On (\\d{4}-\\d{2}-\\d{2}) I moved to ([A-Za-z][A-Za-z ]*)\\.$")
        private val INSTANT_EVENT = Regex("(?i)^At (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z) I arrived\\.$")
        private val YEAR_RANGE_RESIDENCE = Regex("(?i)^Between ((?:19|20)\\d{2}) and ((?:19|20)\\d{2}) I lived in ([A-Za-z][A-Za-z ]*)\\.$")
        private val ONGOING_RESIDENCE = Regex("(?i)^I have lived in ([A-Za-z][A-Za-z ]*) since ((?:19|20)\\d{2})\\.$")
        private val WORK_INTERRUPTION = Regex("(?i)^Work was exhausting today\\. I kept getting interrupted\\.$")
        private val THREE_LUNCH_REPORTS = Regex("(?i)^I skipped lunch today\\. I skipped lunch again\\. I skipped lunch yesterday\\.$")
        private val REMEMBERED_MOVE = Regex("(?i)^I remembered today that we moved around ((?:19|20)\\d{2})\\.$")
        private val MAYBE_BEFORE_COLLEGE = Regex("(?i)^Maybe that happened before college\\.$")
        private val BOUGHT_GROCERIES = Regex("(?i)^I bought groceries after work\\.$")
        private val MOVE = Regex("(?i)^I (did not )?move(?:d)? to ([A-Za-z][A-Za-z ]*) in ((?:19|20)\\d{2})\\.$")
        private val LIVED = Regex("(?i)^I lived in ([A-Za-z][A-Za-z ]*) in ((?:19|20)\\d{2})\\.$")
        private val APPROX_JOB = Regex("(?i)^Around ((?:19|20)\\d{2}) I changed jobs\\.$")
        private val MAYBE_YEAR = Regex("(?i)^Maybe it was ((?:19|20)\\d{2})\\.$")
        private val UNKNOWN_TIME = Regex("(?i)^I (?:do not|don't) remember when it happened\\.$")
        private val RELATIVE_JOB = Regex("(?i)^A few years later I changed jobs\\.$")
        private val DURATION_EMOTION = Regex("(?i)^For a while I felt (worried|sad|angry|calm)\\.$")
        private val SEASONAL_JOB = Regex("(?i)^Sometime that winter I changed jobs\\.$")
        private val SELF_EMOTION = Regex("(?i)^I was (furious|angry|sad|worried|calm) (yesterday|today)\\.$")
        private val SELF_FEELING = Regex("(?i)^I feel (exhausted|anxious|sad|angry|worried|calm) (today|yesterday)\\.$")
        private val THINK_PERSON_EMOTION = Regex("(?i)^I think ([A-Z][a-z]+) was (furious|angry|sad|worried)\\.$")
        private val REPORTED_EMOTION = Regex("(?i)^([A-Z][a-z]+) told me (?:he|she|they) was (furious|angry|sad|worried)\\.$")
        private val MOVING_MISTAKE = Regex("(?i)^I think moving there was a mistake\\.$")
        private val PERSON_HATES_ME = Regex("^([A-Z][a-z]+) hates me\\.$")
        private val NOBODY_LIKES = Regex("(?i)^I feel like nobody likes me\\.$")
        private val ALWAYS_FAIL = Regex("(?i)^I always fail at relationships\\.$")
        private val FAIL_EVERYTHING = Regex("(?i)^I feel like I fail at everything\\.$")
        private val JOB_TRAJECTORY = Regex("(?i)^(At first|By the end) I (loved|hated) that job\\.$")
        private val REPORTED_BEHAVIOR = Regex("(?i)^I (skipped lunch|called my friend|went for a walk) (today|yesterday|again)\\.$")
        private val CORRECTION_YEAR = Regex("(?i)^Actually, it was ((?:19|20)\\d{2}), not (?:19|20)\\d{2}\\.$")
        private val COUNTERFACTUAL = Regex("(?i)^If I'd .+")
        private val HYPOTHETICAL = Regex("(?i)^(?:Suppose|Imagine|What if) .+")
        private val MODAL_SPECULATION = Regex("(?i)^I (?:might|could|would) .+")
        private val DOUBLE_NEGATION = Regex("(?i)\\b(?:didn't not|not unhappy|never not)\\b")
        private val AMBIGUOUS_HUMOR = Regex("(?i)\\b(?:just kidding|sarcasm|yeah right)\\b")
        private val AMBIGUOUS_PRONOUN = Regex("(?i)^(?:He|She|They) .+")
    }
}
