package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.BiographerAnswerOrigin
import com.conundrum.thomas.v2.biographer.BiographerAnswerId
import com.conundrum.thomas.v2.biographer.BiographerInvestigationHistory
import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerPrivacy
import com.conundrum.thomas.v2.biographer.BiographerQuestionDisposition
import com.conundrum.thomas.v2.biographer.CoverageCandidate
import com.conundrum.thomas.v2.biographer.CoverageEvidence
import com.conundrum.thomas.v2.biographer.CoverageRequest
import com.conundrum.thomas.v2.biographer.CoverageStatus
import com.conundrum.thomas.v2.biographer.DeterministicBiographerCoverageEngine
import com.conundrum.thomas.v2.biographer.InvestigationAnswerDisposition
import com.conundrum.thomas.v2.biographer.InvestigationTargetId
import com.conundrum.thomas.v2.biographer.InvestigationTargetKind
import com.conundrum.thomas.v2.biographer.TargetEligibility
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.qualification.biographer.GovernedBiographerPipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.time.Year
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiographerCoverageQualificationTest {
    @Test fun acceptance01OpenStoryNeedsNoPreselectedTarget() = withStore("a01") { _, pipeline ->
        val decision = pipeline.decide(CoverageRequest(BiographerPosture.OPEN_STORY))
        assertEquals(BiographerQuestionDisposition.AUTHORIZED, decision.disposition)
        assertNull(decision.plan!!.targetId)
        assertEquals(InvestigationTargetKind.OPEN_STORY, decision.plan!!.targetKind)
    }

    @Test fun acceptance02OpenStoryHistoricalNarrativeHasBiographerProvenance() = withStore("a02") { harness, pipeline ->
        val result = CTV210TestSupport.capture(harness, pipeline, "a02", "I moved to Denver in 2018.").capture
        assertEquals(AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, result.receipt!!.acquisitionMode)
        assertEquals(AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, harness.store.reader.snapshot().sources.single().provenance.acquisitionMode)
    }

    @Test fun acceptance03OpenNarrativeMaySpanMultipleEventsWithoutGuessing() = withStore("a03") { harness, pipeline ->
        val result = CTV210TestSupport.capture(harness, pipeline, "a03", "I moved and changed jobs.").capture
        assertTrue(result.sourceCaptured)
        assertTrue(result.receipt!!.unresolvedOrUnsupportedCount > 0)
    }

    @Test fun acceptance04UnsupportedOpenStoryLanguageKeepsSource() = withStore("a04") { harness, pipeline ->
        val result = CTV210TestSupport.capture(harness, pipeline, "a04", "What a strange chapter.").capture
        assertTrue(result.sourceCaptured)
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
    }

    @Test fun acceptance05OpenStoryCreatesNoMandatoryFollowup() = withStore("a05") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a05", "What a strange chapter.")
        val next = pipeline.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE))
        assertEquals(BiographerQuestionDisposition.NO_TARGET, next.disposition)
    }

    @Test fun acceptance06Represented2010And2015ProduceSparseTemporalGap() = withStore("a06") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a06-one", "I moved to Denver in 2010.")
        CTV210TestSupport.capture(harness, pipeline, "a06-two", "I moved to Portland in 2015.")
        val decision = pipeline.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE))
        assertTrue(decision.coverageMap.unresolvedTemporalTargetIds.isNotEmpty())
        assertEquals(InvestigationTargetKind.TEMPORAL_GAP, decision.coverageMap.selectedTarget!!.kind)
    }

    @Test fun acceptance07ApproximateTemporalBoundsNeverBecomeExact() {
        val candidate = CoverageCandidate(
            InvestigationTargetId.parse("gap.approximate"),
            InvestigationTargetKind.TEMPORAL_GAP,
            listOf("left", "right"),
            temporalBounds = listOf(EventTime.ApproximateYear(Year.of(2010)), EventTime.ApproximateYear(Year.of(2015))),
            uncertainty = true,
            reasonCode = "SPARSE_APPROXIMATE_INTERVAL",
            status = CoverageStatus.SPARSE,
            materialChangeToken = "approximate",
        )
        val plan = DeterministicBiographerCoverageEngine().decide(
            CoverageEvidence(1, candidates = listOf(candidate)),
            request = CoverageRequest(BiographerPosture.TARGETED_COVERAGE),
        ).plan!!
        assertTrue(plan.safeFacts.isEmpty())
        assertTrue(plan.uncertaintyConstraints.contains("DO_NOT_INCREASE_TEMPORAL_PRECISION"))
        assertTrue(candidate.temporalBounds.all { it is EventTime.ApproximateYear })
    }

    @Test fun acceptance08HistoricalAnswerImprovesRepresentedEvidence() = withStore("a08") { harness, pipeline ->
        val plan = CTV210TestSupport.targetedPlan("gap.2010-2015", InvestigationTargetKind.TEMPORAL_GAP)
        val outcome = CTV210TestSupport.capture(harness, pipeline, "a08", "Around 2012 I changed jobs.", plan)
        assertTrue(outcome.capture.receipt!!.admittedEvidenceIds.isNotEmpty())
        assertTrue(pipeline.formedState().activeExplicitClaims.isNotEmpty())
    }

    @Test fun acceptance09DontRememberInventsNoEvent() = withStore("a09") { harness, pipeline ->
        val outcome = CTV210TestSupport.capture(
            harness, pipeline, "a09", "I don't remember.",
            CTV210TestSupport.targetedPlan("gap.unknown", InvestigationTargetKind.TEMPORAL_GAP),
        )
        assertTrue(outcome.capture.sourceCaptured)
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
        assertEquals(InvestigationAnswerDisposition.NO_EXTRACTABLE_EVIDENCE, outcome.history.entries.values.single().answerDisposition)
    }

    @Test fun acceptance10DeferredTargetIsNotImmediatelyReselected() {
        val engine = DeterministicBiographerCoverageEngine()
        val evidence = CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("target.defer")))
        val first = engine.decide(evidence, request = CoverageRequest(BiographerPosture.TARGETED_COVERAGE))
        val history = first.resultingHistory.recordOutcome(first.coverageMap.selectedTarget!!, 1, InvestigationAnswerDisposition.DEFERRED)
        val next = engine.decide(evidence, history, CoverageRequest(BiographerPosture.TARGETED_COVERAGE))
        assertEquals(BiographerQuestionDisposition.NO_TARGET, next.disposition)
        assertEquals(TargetEligibility.DEFERRED, next.coverageMap.targets.single().eligibility)
    }

    @Test fun acceptance11DeclinedTargetIsExcludedAutomatically() {
        val decision = decisionFor(CTV210TestSupport.candidate("target.declined", status = CoverageStatus.DECLINED))
        assertEquals(BiographerQuestionDisposition.NO_TARGET, decision.disposition)
        assertEquals(TargetEligibility.DECLINED, decision.coverageMap.targets.single().eligibility)
    }

    @Test fun acceptance12PrivateTargetIsExcludedAutomatically() {
        val decision = decisionFor(CTV210TestSupport.candidate("target.private", status = CoverageStatus.PRIVATE))
        assertEquals(BiographerQuestionDisposition.NO_TARGET, decision.disposition)
        assertEquals(TargetEligibility.PRIVATE, decision.coverageMap.targets.single().eligibility)
    }

    @Test fun acceptance13KnownRoleWithMissingContextProducesRoleTarget() {
        val decision = decisionFor(CTV210TestSupport.candidate("role.unknown-end", InvestigationTargetKind.ROLE_GAP))
        assertEquals(InvestigationTargetKind.ROLE_GAP, decision.coverageMap.selectedTarget!!.kind)
    }

    @Test fun acceptance14ResidenceSuccessorGapProducesPlaceTarget() {
        val decision = decisionFor(CTV210TestSupport.candidate("place.next", InvestigationTargetKind.PLACE_GAP))
        assertEquals(InvestigationTargetKind.PLACE_GAP, decision.coverageMap.selectedTarget!!.kind)
    }

    @Test fun acceptance15KnownEventUnknownDateProducesTimeTarget() {
        val decision = decisionFor(CTV210TestSupport.candidate("event.time", InvestigationTargetKind.EVENT_TIME_UNRESOLVED))
        assertEquals(InvestigationTargetKind.EVENT_TIME_UNRESOLVED, decision.coverageMap.selectedTarget!!.kind)
    }

    @Test fun acceptance16PartialPeriodCanBeTargetedWithoutClaimingAbsence() {
        val candidate = CTV210TestSupport.candidate("period.partial", InvestigationTargetKind.PERIOD_DETAIL)
        assertEquals(CoverageStatus.SPARSE, candidate.status)
        assertEquals(InvestigationTargetKind.PERIOD_DETAIL, decisionFor(candidate).plan!!.targetKind)
    }

    @Test fun acceptance17TwoSamReferencesCreateIdentityTarget() = withStore("a17") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a17-one", "I think Sam was angry.")
        CTV210TestSupport.capture(harness, pipeline, "a17-two", "Sam hates me.")
        val decision = pipeline.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE))
        assertTrue(pipeline.formedState().unresolvedIdentities.isNotEmpty())
        assertEquals(InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED, decision.plan!!.targetKind)
    }

    @Test fun acceptance18UserCanGovernedlyConfirmSamePerson() = withStore("a18") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a18-one", "I think Sam was angry.")
        CTV210TestSupport.capture(harness, pipeline, "a18-two", "Sam hates me.")
        val people = pipeline.formedState().activeEntitiesAndEvents.filterIsInstance<Person>()
        val assertions = harness.store.reader.snapshot().assertions.map { it.id }.toSet()
        val result = pipeline.reviseIdentity(
            InvestigationTargetId.parse("identity.sam"),
            people[0].id, people[1].id, EntityIdentityStatus.ESTABLISHED_SAME_ENTITY,
            assertions, "same-a18",
        )
        assertEquals(AdmissionDisposition.ACCEPTED, result.disposition)
        assertEquals(EntityIdentityStatus.ESTABLISHED_SAME_ENTITY, harness.store.reader.identityLinks().last().status)
    }

    @Test fun acceptance19UserCanGovernedlyConfirmDifferentPeople() = withStore("a19") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a19-one", "I think Sam was angry.")
        CTV210TestSupport.capture(harness, pipeline, "a19-two", "Sam hates me.")
        val people = pipeline.formedState().activeEntitiesAndEvents.filterIsInstance<Person>()
        val result = pipeline.reviseIdentity(
            InvestigationTargetId.parse("identity.sam"),
            people[0].id, people[1].id, EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY,
            harness.store.reader.snapshot().assertions.map { it.id }.toSet(), "different-a19",
        )
        assertEquals(AdmissionDisposition.ACCEPTED, result.disposition)
        assertEquals(EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY, harness.store.reader.identityLinks().last().status)
    }

    @Test fun acceptance20UnsureIdentityLeavesReferencesUnresolved() = withStore("a20") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a20-one", "I think Sam was angry.")
        CTV210TestSupport.capture(harness, pipeline, "a20-two", "Sam hates me.")
        assertTrue(pipeline.formedState().unresolvedIdentities.isNotEmpty())
        assertTrue(harness.store.reader.identityLinks().isEmpty())
    }

    @Test fun acceptance21PreviouslyDeclinedIdentityIsNotRetargeted() {
        val candidate = CTV210TestSupport.candidate(
            "identity.declined",
            InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED,
            CoverageStatus.DECLINED,
        )
        assertEquals(BiographerQuestionDisposition.NO_TARGET, decisionFor(candidate).disposition)
    }

    @Test fun acceptance22ConflictingYearsProduceClarificationTarget() = withStore("a22") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a22-one", "I moved to Denver in 2012.")
        CTV210TestSupport.capture(harness, pipeline, "a22-two", "I moved to Denver in 2013.")
        val assertions = harness.store.reader.snapshot().assertions
        val relation = ContradictionRelation(
            EvidenceRelationId.parse("a22.same-event-year-conflict"),
            assertions[0].id,
            assertions[1].id,
            rationale = "Synthetic fixture establishes that the two different years concern the same event.",
        )
        val admitted = harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.RecordContradiction(relation),
            AdmissionActor.THOMAS,
            AdmissionOrigin.THOMAS_DERIVATION,
        ))
        assertEquals(AdmissionDisposition.ACCEPTED, admitted.disposition)
        assertTrue(pipeline.formedState().contradictions.isNotEmpty())
        val decision = pipeline.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE))
        assertEquals(InvestigationTargetKind.CONTRADICTION_CLARIFICATION, decision.plan!!.targetKind)
    }

    @Test fun acceptance23ConfirmedYearUsesExistingCorrectionAuthority() = withStore("a23") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a23-old", "I moved to Denver in 2012.")
        val old = harness.store.reader.snapshot().assertions.single().id.value
        val plan = CTV210TestSupport.targetedPlan(
            "contradiction.year",
            InvestigationTargetKind.CONTRADICTION_CLARIFICATION,
            listOf(old),
        )
        val answer = CTV210TestSupport.capture(harness, pipeline, "a23-correction", "Actually, it was 2013, not 2012.", plan)
        assertTrue(answer.capture.sourceCaptured)
        val correction = pipeline.recordExplicitUserCorrection(
            BiographerAnswerId.parse("a23-correction").sourceIdentity(),
            com.conundrum.thomas.v2.longitudinal.AssertionId.parse(old),
            2013,
            "a23",
        )
        assertEquals(AdmissionDisposition.ACCEPTED, correction.disposition)
        assertTrue(harness.store.reader.corrections().isNotEmpty())
        assertEquals(2, harness.store.reader.snapshot().assertions.size)
    }

    @Test fun acceptance24BothReportsMayBeWrongPreservesUncertainty() = withStore("a24") { harness, pipeline ->
        val result = CTV210TestSupport.capture(
            harness, pipeline, "a24", "Maybe both reports were wrong.",
            CTV210TestSupport.targetedPlan("contradiction.uncertain", InvestigationTargetKind.CONTRADICTION_CLARIFICATION),
        )
        assertTrue(result.capture.sourceCaptured)
        assertTrue(result.capture.receipt!!.unresolvedOrUnsupportedCount > 0)
    }

    @Test fun acceptance25TimeSeparatedClaimsAreNotFalseContradictions() = withStore("a25") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a25-one", "I moved to Denver in 2010.")
        CTV210TestSupport.capture(harness, pipeline, "a25-two", "I moved to Portland in 2018.")
        assertTrue(pipeline.formedState().contradictions.isEmpty())
    }

    private fun decisionFor(candidate: CoverageCandidate) = DeterministicBiographerCoverageEngine().decide(
        CoverageEvidence(1, candidates = listOf(candidate)),
        request = CoverageRequest(BiographerPosture.TARGETED_COVERAGE),
    )

    private fun withStore(
        name: String,
        block: (SyntheticLongitudinalStoreHarness, GovernedBiographerPipeline) -> Unit,
    ) {
        SyntheticLongitudinalStoreHarness(CTV210TestSupport.path(name)).use { harness ->
            block(harness, GovernedBiographerPipeline(harness.store))
        }
    }
}
