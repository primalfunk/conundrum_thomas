package com.conundrum.thomas.v2.languageevidence

import com.conundrum.thomas.v2.languageevidence.perception.*
import com.conundrum.thomas.v2.longitudinal.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class ConservativeLanguagePerceptionTest {
    private val parser = ConservativeLanguagePerception()

    @Test fun moveIsExplicitEventWithCalendarYear() {
        val claim = parse("I moved to Denver in 2018.", "move").proposals.single().assertion
        assertEquals(EvidenceEpistemicClass.EVENT_REFERENCE, claim.epistemicClass)
        assertTrue(claim.eventTime is EventTime.Range)
        assertEquals("Denver", (claim.value as AssertionValue.Text).value)
    }

    @Test fun movingMistakeIsUserInterpretation() {
        assertEquals(EvidenceEpistemicClass.USER_INTERPRETATION,
            parse("I think moving there was a mistake.", "mistake").proposals.single().assertion.epistemicClass)
    }

    @Test fun selfEmotionIsSelfReportNotTrait() {
        val claim = parse("I was furious yesterday.", "self-emotion").proposals.single().assertion
        assertEquals(EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, claim.epistemicClass)
        assertFalse(claim.predicate.conceptId.value.contains("trait"))
    }

    @Test fun inferredOtherEmotionIsInterpretation() {
        assertEquals(EvidenceEpistemicClass.USER_INTERPRETATION,
            parse("I think Sam was furious.", "sam-think").proposals.single().assertion.epistemicClass)
    }

    @Test fun otherPersonsStatementIsThirdPartyReport() {
        assertEquals(EvidenceEpistemicClass.THIRD_PARTY_REPORT,
            parse("Sam told me he was furious.", "sam-said").proposals.single().assertion.epistemicClass)
    }

    @Test fun approximateYearDoesNotBecomeExactDate() {
        assertTrue(parse("Around 2012 I changed jobs.", "approx-year").proposals.single().assertion.eventTime is EventTime.ApproximateYear)
    }

    @Test fun maybePreservesUncertainty() {
        val claim = parse("Maybe it was 2012.", "maybe-year").proposals.single().assertion
        assertEquals(EvidenceEpistemicClass.USER_INTERPRETATION, claim.epistemicClass)
        assertTrue(claim.eventTime is EventTime.ApproximateYear)
        assertEquals(AssertionUncertainty.STATED_AS_UNCERTAIN, claim.uncertainty)
    }

    @Test fun forgottenTimeRemainsUnknown() {
        val result = parse("I don't remember when it happened.", "unknown-time")
        assertTrue(result.proposals.single().assertion.eventTime is EventTime.Unknown)
        assertTrue(result.unresolved.any { it.kind == UnresolvedPerceptionKind.UNKNOWN_EVENT_TIME })
    }

    @Test fun counterfactualCreatesNoEvent() {
        val result = parse("If I'd stayed, I'd probably have hated it.", "counterfactual")
        assertEquals(PerceptionDisposition.NO_EVIDENCE_PROPOSAL, result.disposition)
        assertTrue(result.proposals.isEmpty())
    }

    @Test fun interrogativeCreatesNoFact() {
        val result = parse("Did I move there in 2012?", "question")
        assertTrue(result.proposals.isEmpty())
        assertEquals(NonassertiveKind.QUESTION, result.unresolved.single().nonassertiveKind)
    }

    @Test fun correctionWithTargetIsGroundedCandidate() {
        val target = ClaimReference.Assertion(AssertionId.parse("assertion.prior"))
        val result = parse("Actually, it was 2013, not 2012.", "correction", PerceptionContext(target))
        assertNotNull(result.correctionCandidate?.correction)
        assertEquals(target, result.correctionCandidate?.target)
    }

    @Test fun correctionWithoutTargetRemainsUnresolved() {
        val result = parse("Actually, it was 2013, not 2012.", "correction-ambiguous")
        assertEquals(PerceptionDisposition.PARTIALLY_UNDERSTOOD, result.disposition)
        assertNull(result.correctionCandidate?.target)
        assertTrue(result.unresolved.any { it.kind == UnresolvedPerceptionKind.AMBIGUOUS_CORRECTION_TARGET })
    }

    @Test fun hatesMeIsInterpretationAboutOtherMind() {
        assertEquals(EvidenceEpistemicClass.USER_INTERPRETATION,
            parse("Sam hates me.", "hates").proposals.single().assertion.epistemicClass)
    }

    @Test fun nobodyLikesMeIsSelfBeliefNotCensusFact() {
        assertEquals(EvidenceEpistemicClass.SELF_BELIEF,
            parse("I feel like nobody likes me.", "nobody").proposals.single().assertion.epistemicClass)
    }

    @Test fun alwaysFailIsSelfBeliefNotRecurrence() {
        val claim = parse("I always fail at relationships.", "always").proposals.single().assertion
        assertEquals(EvidenceEpistemicClass.SELF_BELIEF, claim.epistemicClass)
        assertFalse(claim.predicate.conceptId.value.startsWith("reported.behavior."))
    }

    @Test fun explicitNegationRemainsNegative() {
        assertEquals(AssertionPolarity.NEGATIVE,
            parse("I did not move to Denver in 2018.", "negation").proposals.single().assertion.polarity)
    }

    @Test fun doubleNegationIsAmbiguous() {
        assertEquals(PerceptionDisposition.AMBIGUOUS, parse("I wasn't not unhappy.", "double-negation").disposition)
    }

    @Test fun unattributedQuotationIsNonassertive() {
        val quoted = 34.toChar() + "I moved to Denver in 2018." + 34.toChar()
        assertTrue(parse(quoted, "quotation").proposals.isEmpty())
    }

    @Test fun hypotheticalIsNonassertive() {
        assertTrue(parse("Imagine I moved to Denver.", "hypothetical").proposals.isEmpty())
    }

    @Test fun ambiguousHumorIsNotPromoted() {
        assertEquals(PerceptionDisposition.AMBIGUOUS,
            parse("Yeah right, everyone loves me; just kidding.", "humor").disposition)
    }

    @Test fun modalSpeculationIsNotPromoted() {
        assertTrue(parse("I might move to Denver.", "modal").proposals.isEmpty())
    }

    @Test fun ambiguousPronounIsNotResolvedByGuessing() {
        assertEquals(PerceptionDisposition.AMBIGUOUS, parse("He moved to Denver.", "pronoun").disposition)
    }

    @Test fun multipleDatesAreRejectedAsAmbiguous() {
        assertEquals(PerceptionDisposition.AMBIGUOUS,
            parse("I moved in 2012 and left in 2018.", "dates").disposition)
    }

    @Test fun unsupportedLanguageSafelyCreatesNoProposal() {
        val result = parse("The blue idea slept quickly.", "unsupported")
        assertEquals(PerceptionDisposition.NO_EVIDENCE_PROPOSAL, result.disposition)
        assertTrue(result.proposals.isEmpty())
    }

    @Test fun malformedInputFailsWithoutProposal() {
        val result = parse("bad\u0000input", "malformed")
        assertEquals(PerceptionDisposition.INVALID_INPUT, result.disposition)
        assertTrue(result.proposals.isEmpty())
    }

    @Test fun exactSpanCoversOriginalUnnormalizedText() {
        val source = source("I moved to Denver in 2018.", "span")
        val grounding = parser.perceive(CommittedSourceText.from(source)).proposals.single().assertion.sourceGrounding!!
        assertEquals((source.originalContent as OriginalSourceContent.Inline).exactContent, grounding.exactFragment)
        assertEquals(0, grounding.startOffsetInclusive)
        assertEquals(grounding.exactFragment.length, grounding.endOffsetExclusive)
    }

    private fun parse(text: String, suffix: String, context: PerceptionContext = PerceptionContext()) =
        parser.perceive(CommittedSourceText.from(source(text, suffix)), context)

    private fun source(text: String, suffix: String) = SourceRecord(
        SourceRecordId.parse("source.$suffix.rev-1"),
        PersonalEvidenceProvenance(AcquisitionMode.JOURNAL),
        ReportTime(Instant.parse("2050-01-01T00:00:00Z")),
        RecordTime(Instant.parse("2050-01-01T00:00:01Z")),
        OriginalSourceContent.Inline(text),
        SourceIdentityId.parse("source.$suffix"),
    )
}
