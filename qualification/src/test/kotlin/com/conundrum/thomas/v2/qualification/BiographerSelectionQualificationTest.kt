package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.BiographerAnswerId
import com.conundrum.thomas.v2.biographer.BiographerInvestigationHistory
import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerPrivacy
import com.conundrum.thomas.v2.biographer.BiographerProhibitedQuestionAct
import com.conundrum.thomas.v2.biographer.BiographerQuestionDisposition
import com.conundrum.thomas.v2.biographer.BiographerQuestionExecutor
import com.conundrum.thomas.v2.biographer.BiographerQuestionRenderDisposition
import com.conundrum.thomas.v2.biographer.BiographerQuestionSemanticAct
import com.conundrum.thomas.v2.biographer.CoverageEvidence
import com.conundrum.thomas.v2.biographer.CoverageRequest
import com.conundrum.thomas.v2.biographer.CoverageStatus
import com.conundrum.thomas.v2.biographer.DeterministicBiographerCoverageEngine
import com.conundrum.thomas.v2.biographer.InvestigationAnswerDisposition
import com.conundrum.thomas.v2.biographer.InvestigationTargetId
import com.conundrum.thomas.v2.biographer.InvestigationTargetKind
import com.conundrum.thomas.v2.biographer.TargetEligibility
import com.conundrum.thomas.v2.biographer.UserNamedCoverageTarget
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.qualification.biographer.GovernedBiographerPipeline
import com.conundrum.thomas.v2.qualification.journal.GovernedJournalCapturePipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiographerSelectionQualificationTest {
    private val engine = DeterministicBiographerCoverageEngine()

    @Test fun acceptance26ExplicitHighSchoolTargetOutranksGap() {
        val request = CoverageRequest(
            BiographerPosture.TARGETED_COVERAGE,
            UserNamedCoverageTarget(InvestigationTargetId.parse("user.high-school"), "high-school"),
        )
        val decision = engine.decide(
            CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("gap.other"))),
            request = request,
        )
        assertEquals(InvestigationTargetKind.USER_NAMED_TOPIC, decision.plan!!.targetKind)
    }

    @Test fun acceptance27BroadUserTopicCreatesNonPresuppositionalQuestion() {
        val decision = engine.decide(
            CoverageEvidence(1),
            request = CoverageRequest(
                BiographerPosture.TARGETED_COVERAGE,
                UserNamedCoverageTarget(InvestigationTargetId.parse("user.work-history"), "work-history"),
            ),
        )
        val plan = decision.plan!!
        assertEquals(BiographerQuestionSemanticAct.EXPLORE_USER_NAMED_TOPIC, plan.semanticAct)
        assertTrue(plan.safeFacts.isEmpty())
        assertTrue(plan.prohibitedActs.contains(BiographerProhibitedQuestionAct.PSYCHOLOGICAL_CAUSAL_CLAIM))
    }

    @Test fun acceptance28UserChangeTopicDoesNotForcePreviousTarget() {
        val oldEvidence = CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("gap.old")))
        val old = engine.decide(oldEvidence, request = targeted())
        val changed = engine.decide(
            oldEvidence,
            old.resultingHistory,
            CoverageRequest(
                BiographerPosture.TARGETED_COVERAGE,
                UserNamedCoverageTarget(InvestigationTargetId.parse("user.chicago"), "chicago-history"),
            ),
        )
        assertEquals(InvestigationTargetId.parse("user.chicago"), changed.coverageMap.selectedTarget!!.id)
    }

    @Test fun acceptance29ExplicitReturnMakesDeferredTopicEligible() {
        val candidate = CTV210TestSupport.candidate("gap.deferred")
        val first = engine.decide(CoverageEvidence(1, candidates = listOf(candidate)), request = targeted())
        val history = first.resultingHistory.recordOutcome(
            first.coverageMap.selectedTarget!!, 1, InvestigationAnswerDisposition.DEFERRED,
        )
        val reopened = engine.decide(
            CoverageEvidence(1, candidates = listOf(candidate)),
            history,
            CoverageRequest(BiographerPosture.TARGETED_COVERAGE, explicitlyReopenedTargetId = candidate.id),
        )
        assertEquals(BiographerQuestionDisposition.AUTHORIZED, reopened.disposition)
    }

    @Test fun acceptance30MultipleGapsHaveOneDeterministicWinner() {
        val evidence = CoverageEvidence(1, candidates = listOf(
            CTV210TestSupport.candidate("gap.role", InvestigationTargetKind.ROLE_GAP),
            CTV210TestSupport.candidate("gap.identity", InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED),
            CTV210TestSupport.candidate("gap.period", InvestigationTargetKind.PERIOD_DETAIL),
        ))
        assertEquals(InvestigationTargetId.parse("gap.identity"), engine.decide(evidence, request = targeted()).coverageMap.selectedTarget!!.id)
    }

    @Test fun acceptance31SameStateProducesSameSelectedTarget() {
        val evidence = CoverageEvidence(1, candidates = listOf(
            CTV210TestSupport.candidate("gap.b"), CTV210TestSupport.candidate("gap.a"),
        ))
        val first = engine.decide(evidence, request = targeted())
        val second = engine.decide(evidence, request = targeted())
        assertEquals(first.coverageMap.selectedTarget, second.coverageMap.selectedTarget)
        assertEquals(first.coverageMap.canonicalDigest, second.coverageMap.canonicalDigest)
    }

    @Test fun acceptance32TiesBreakByStableTargetId() {
        val evidence = CoverageEvidence(1, candidates = listOf(
            CTV210TestSupport.candidate("tie.z"), CTV210TestSupport.candidate("tie.a"),
        ))
        assertEquals(InvestigationTargetId.parse("tie.a"), engine.decide(evidence, request = targeted()).coverageMap.selectedTarget!!.id)
    }

    @Test fun acceptance33UntouchedTargetWinsOverRecentlyAskedTarget() {
        val evidence = CoverageEvidence(1, candidates = listOf(
            CTV210TestSupport.candidate("target.a"), CTV210TestSupport.candidate("target.b"),
        ))
        val first = engine.decide(evidence, request = targeted())
        val next = engine.decide(evidence, first.resultingHistory, targeted())
        assertEquals(InvestigationTargetId.parse("target.b"), next.coverageMap.selectedTarget!!.id)
    }

    @Test fun acceptance34NoEligibleTargetReturnsNoTarget() {
        assertEquals(BiographerQuestionDisposition.NO_TARGET, engine.decide(CoverageEvidence(0), request = targeted()).disposition)
    }

    @Test fun acceptance35AllPrivateOrDeclinedReturnsNoTarget() {
        val evidence = CoverageEvidence(1, candidates = listOf(
            CTV210TestSupport.candidate("private.one", status = CoverageStatus.PRIVATE),
            CTV210TestSupport.candidate("declined.one", status = CoverageStatus.DECLINED),
        ))
        assertEquals(BiographerQuestionDisposition.NO_TARGET, engine.decide(evidence, request = targeted()).disposition)
    }

    @Test fun acceptance36EligibleExplicitUserTargetWinsEngineTarget() = acceptance26ExplicitHighSchoolTargetOutranksGap()

    @Test fun acceptance37AnsweredRelevantTargetIsNotImmediatelyAskedAgain() {
        val evidence = CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("target.answered")))
        val first = engine.decide(evidence, request = targeted())
        val history = first.resultingHistory.recordOutcome(
            first.coverageMap.selectedTarget!!, 1, InvestigationAnswerDisposition.ANSWERED_RELEVANT,
        )
        val next = engine.decide(evidence, history, targeted())
        assertEquals(TargetEligibility.COVERED, next.coverageMap.targets.single().eligibility)
        assertEquals(BiographerQuestionDisposition.NO_TARGET, next.disposition)
    }

    @Test fun acceptance38DeferredTargetIsNotImmediatelyAskedAgain() {
        val evidence = CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("target.defer")))
        val first = engine.decide(evidence, request = targeted())
        val history = first.resultingHistory.recordOutcome(
            first.coverageMap.selectedTarget!!, 1, InvestigationAnswerDisposition.DEFERRED,
        )
        assertEquals(BiographerQuestionDisposition.NO_TARGET, engine.decide(evidence, history, targeted()).disposition)
    }

    @Test fun acceptance39DeclinedTargetIsNeverAutomaticallyAskedAgain() {
        val evidence = CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("target.decline")))
        val first = engine.decide(evidence, request = targeted())
        val history = first.resultingHistory.recordOutcome(
            first.coverageMap.selectedTarget!!, 1, InvestigationAnswerDisposition.DECLINED,
        )
        val next = engine.decide(evidence, history, targeted())
        assertEquals(TargetEligibility.DECLINED, next.coverageMap.targets.single().eligibility)
    }

    @Test fun acceptance40MaterialNewEvidenceCanReopenOldTarget() {
        val before = CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("target.changed", token = "v1")))
        val first = engine.decide(before, request = targeted())
        val history = first.resultingHistory.recordOutcome(
            first.coverageMap.selectedTarget!!, 1, InvestigationAnswerDisposition.ANSWERED_AMBIGUOUS,
        )
        val after = CoverageEvidence(2, candidates = listOf(CTV210TestSupport.candidate("target.changed", token = "v2")))
        assertEquals(BiographerQuestionDisposition.AUTHORIZED, engine.decide(after, history, targeted()).disposition)
    }

    @Test fun acceptance41RepeatedExecutionCannotCycleOneQuestion() {
        val evidence = CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("target.once")))
        val first = engine.decide(evidence, request = targeted())
        val second = engine.decide(evidence, first.resultingHistory, targeted())
        val third = engine.decide(evidence, second.resultingHistory, targeted())
        assertEquals(BiographerQuestionDisposition.NO_TARGET, second.disposition)
        assertEquals(BiographerQuestionDisposition.NO_TARGET, third.disposition)
    }

    @Test fun acceptance42JournalAndBiographerReportsKeepDistinctProvenance() = withStore("a42") { harness, bio ->
        val journal = GovernedJournalCapturePipeline(harness.store)
        CTV209TestSupport.capture(harness, journal, "a42-journal", "I moved to Denver in 2018.")
        CTV210TestSupport.capture(harness, bio, "a42-biographer", "I moved to Denver in 2018.")
        val modes = harness.store.reader.snapshot().sources.map { it.provenance.acquisitionMode }.toSet()
        assertEquals(setOf(AcquisitionMode.JOURNAL, AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE), modes)
    }

    @Test fun acceptance43SameEventAccountsDoNotCollapseSources() = withStore("a43") { harness, bio ->
        val journal = GovernedJournalCapturePipeline(harness.store)
        CTV209TestSupport.capture(harness, journal, "a43-journal", "I moved to Denver in 2018.")
        CTV210TestSupport.capture(harness, bio, "a43-biographer", "I moved to Denver in 2018.")
        assertEquals(2, harness.store.reader.snapshot().sources.size)
        assertEquals(2, harness.store.reader.snapshot().sources.map { it.stableSourceId }.distinct().size)
    }

    @Test fun acceptance44EventReportAndRecordTimeStayIndependent() = withStore("a44") { harness, bio ->
        CTV210TestSupport.capture(harness, bio, "a44", "Around 2012 I changed jobs.")
        val source = harness.store.reader.snapshot().sources.single()
        val assertion = harness.store.reader.snapshot().assertions.single()
        assertTrue(assertion.eventTime is EventTime.ApproximateYear)
        assertEquals("2039-09-03T12:00:00Z", source.reportTime.value.toString())
        assertEquals("2040-01-01T00:00:00Z", source.recordTime.value.toString())
    }

    @Test fun acceptance45QuestionTextIsNeverAdmittedAsUserEvidence() = withStore("a45") { harness, bio ->
        val decision = bio.decide(CoverageRequest(BiographerPosture.OPEN_STORY))
        val before = harness.store.reader.snapshot()
        val rendered = BiographerQuestionExecutor().execute(decision) { "What do you remember?" }
        assertEquals(BiographerQuestionRenderDisposition.RENDERED, rendered.disposition)
        assertEquals(before, harness.store.reader.snapshot())
    }

    @Test fun acceptance46PrivateEvidenceCannotLeakIntoNearbyGapQuestion() = withStore("a46") { harness, bio ->
        CTV210TestSupport.capture(harness, bio, "a46-left", "I moved to Denver in 2010.")
        CTV210TestSupport.capture(harness, bio, "a46-private", "I moved to Seattle in 2012.", privacy = BiographerPrivacy.PRIVATE)
        CTV210TestSupport.capture(harness, bio, "a46-right", "I moved to Portland in 2015.")
        val decision = bio.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE))
        val privateRevision = BiographerAnswerId.parse("a46-private").sourceRevision().value
        assertFalse(decision.plan!!.groundingIds.contains(privateRevision))
        assertFalse(decision.plan!!.safeFacts.any { privateRevision in it.groundingIds })
    }

    @Test fun acceptance47PrivateTargetItselfIsExcluded() {
        assertEquals(BiographerQuestionDisposition.NO_TARGET,
            engine.decide(CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("private", status = CoverageStatus.PRIVATE))),
                request = targeted()).disposition)
    }

    @Test fun acceptance48DeclinedTargetItselfIsExcluded() {
        assertEquals(BiographerQuestionDisposition.NO_TARGET,
            engine.decide(CoverageEvidence(1, candidates = listOf(CTV210TestSupport.candidate("declined", status = CoverageStatus.DECLINED))),
                request = targeted()).disposition)
    }

    @Test fun acceptance49PrivacyRestorationPreservesReviewRequiredSemantics() = withStore("a49") { harness, bio ->
        CTV210TestSupport.capture(harness, bio, "a49", "I moved to Denver in 2018.", privacy = BiographerPrivacy.PRIVATE)
        val sourceId = BiographerAnswerId.parse("a49").sourceIdentity()
        val restored = harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.ChangePrivacy(sourceId, SourcePrivacy.ELIGIBLE),
            AdmissionActor.USER,
            AdmissionOrigin.QUALIFICATION_HARNESS,
        ))
        assertEquals(AdmissionDisposition.ACCEPTED, restored.disposition)
        val lifecycle = harness.store.reader.lifecycle(
            LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION, BiographerAnswerId.parse("a49").sourceRevision().value),
        )
        assertEquals(LongitudinalLifecycleStatus.REVIEW_REQUIRED, lifecycle!!.status)
        assertTrue(bio.formedState().activeExplicitClaims.isEmpty())
    }

    @Test fun acceptance50ExplicitReopenCanRepresentPriorDeclineWithoutErasingIt() {
        val candidate = CTV210TestSupport.candidate("declined.reopen", status = CoverageStatus.DECLINED)
        val blocked = engine.decide(CoverageEvidence(1, candidates = listOf(candidate)), request = targeted())
        val reopened = engine.decide(
            CoverageEvidence(1, candidates = listOf(candidate)),
            request = CoverageRequest(BiographerPosture.TARGETED_COVERAGE, explicitlyReopenedTargetId = candidate.id),
        )
        assertEquals(BiographerQuestionDisposition.NO_TARGET, blocked.disposition)
        assertEquals(BiographerQuestionDisposition.AUTHORIZED, reopened.disposition)
        assertEquals(CoverageStatus.DECLINED, candidate.status)
    }

    private fun targeted() = CoverageRequest(BiographerPosture.TARGETED_COVERAGE)

    private fun withStore(
        name: String,
        block: (SyntheticLongitudinalStoreHarness, GovernedBiographerPipeline) -> Unit,
    ) {
        SyntheticLongitudinalStoreHarness(CTV210TestSupport.path(name)).use { harness ->
            block(harness, GovernedBiographerPipeline(harness.store))
        }
    }
}
