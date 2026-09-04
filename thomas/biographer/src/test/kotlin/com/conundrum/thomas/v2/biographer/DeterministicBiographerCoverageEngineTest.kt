package com.conundrum.thomas.v2.biographer

import com.conundrum.thomas.v2.languageevidence.perception.LanguagePerceptionResult
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionDisposition
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.Year

class DeterministicBiographerCoverageEngineTest {
    private val engine = DeterministicBiographerCoverageEngine()

    @Test
    fun openStoryHasNoHiddenTargetAndOneQuestionMaximum() {
        val decision = engine.decide(CoverageEvidence(0), request = CoverageRequest(BiographerPosture.OPEN_STORY))
        assertEquals(BiographerQuestionDisposition.AUTHORIZED, decision.disposition)
        assertNull(decision.plan?.targetId)
        assertEquals(InvestigationTargetKind.OPEN_STORY, decision.plan?.targetKind)
        assertEquals(1, decision.plan?.maximumQuestionCount)
    }

    @Test
    fun structuralPriorityAndStableTieBreakAreDeterministic() {
        val lower = candidate("gap.z", InvestigationTargetKind.TEMPORAL_GAP)
        val higher = candidate("identity.z", InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED)
        val tie = candidate("identity.a", InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED)
        val evidence = CoverageEvidence(4, candidates = listOf(lower, higher, tie))
        val first = engine.decide(evidence, request = targeted())
        val second = engine.decide(evidence, request = targeted())
        assertEquals(InvestigationTargetId.parse("identity.a"), first.coverageMap.selectedTarget?.id)
        assertEquals(first.coverageMap.canonicalDigest, second.coverageMap.canonicalDigest)
    }

    @Test
    fun userNamedTargetOutranksEngineGeneratedGap() {
        val evidence = CoverageEvidence(2, candidates = listOf(candidate("gap.one", InvestigationTargetKind.TEMPORAL_GAP)))
        val request = CoverageRequest(
            BiographerPosture.TARGETED_COVERAGE,
            UserNamedCoverageTarget(InvestigationTargetId.parse("user.high-school"), "high-school-history"),
        )
        val decision = engine.decide(evidence, request = request)
        assertEquals(InvestigationTargetKind.USER_NAMED_TOPIC, decision.plan?.targetKind)
    }

    @Test
    fun unchangedOfferedTargetCannotLoop() {
        val evidence = CoverageEvidence(8, candidates = listOf(candidate("gap.one", InvestigationTargetKind.TEMPORAL_GAP)))
        val first = engine.decide(evidence, request = targeted())
        val second = engine.decide(evidence, first.resultingHistory, targeted())
        assertEquals(BiographerQuestionDisposition.NO_TARGET, second.disposition)
        assertEquals(TargetEligibility.RECENTLY_ASKED, second.coverageMap.targets.single().eligibility)
    }

    @Test
    fun materiallyChangedTargetCanBeRevisited() {
        val before = CoverageEvidence(8, candidates = listOf(candidate("gap.one", token = "basis-v1")))
        val first = engine.decide(before, request = targeted())
        val after = CoverageEvidence(9, candidates = listOf(candidate("gap.one", token = "basis-v2")))
        val second = engine.decide(after, first.resultingHistory, targeted())
        assertEquals(BiographerQuestionDisposition.AUTHORIZED, second.disposition)
    }

    @Test
    fun deferDeclinePrivateAndCoveredAreExcluded() {
        val targets = listOf(
            candidate("gap.defer"),
            candidate("gap.decline", status = CoverageStatus.DECLINED),
            candidate("gap.private", status = CoverageStatus.PRIVATE),
            candidate("gap.covered", status = CoverageStatus.COVERED_ENOUGH_FOR_CURRENT_PURPOSE),
        )
        val first = engine.decide(CoverageEvidence(1, candidates = targets), request = targeted())
        val selected = requireNotNull(first.coverageMap.selectedTarget)
        val history = first.resultingHistory.recordOutcome(
            selected, 1, InvestigationAnswerDisposition.DEFERRED,
        )
        val next = engine.decide(CoverageEvidence(1, candidates = targets), history, targeted())
        assertEquals(BiographerQuestionDisposition.NO_TARGET, next.disposition)
        assertTrue(next.coverageMap.deferredTargetIds.contains(selected.id))
        assertEquals(1, next.coverageMap.declinedTargetIds.size)
        assertEquals(1, next.coverageMap.privateTargetIds.size)
    }

    @Test
    fun explicitUserReopenPermitsPreviouslyDeclinedTarget() {
        val target = candidate("gap.declined", status = CoverageStatus.DECLINED)
        val request = CoverageRequest(
            BiographerPosture.TARGETED_COVERAGE,
            explicitlyReopenedTargetId = target.id,
        )
        assertEquals(BiographerQuestionDisposition.AUTHORIZED, engine.decide(CoverageEvidence(1, candidates = listOf(target)), request = request).disposition)
    }

    @Test
    fun userNamedTargetCannotOverridePrivateCoverage() {
        val target = candidate("gap.private", status = CoverageStatus.PRIVATE)
        val request = CoverageRequest(
            BiographerPosture.TARGETED_COVERAGE,
            UserNamedCoverageTarget(target.id, "private-period"),
        )
        val decision = engine.decide(CoverageEvidence(1, candidates = listOf(target)), request = request)
        assertEquals(BiographerQuestionDisposition.NO_TARGET, decision.disposition)
        assertEquals(TargetEligibility.PRIVATE, decision.coverageMap.targets.single().eligibility)
    }

    @Test
    fun noEligibleTargetIsSuccessfulNoTarget() {
        val decision = engine.decide(CoverageEvidence(0), request = targeted())
        assertEquals(BiographerQuestionDisposition.NO_TARGET, decision.disposition)
        assertNull(decision.plan)
    }

    @Test
    fun higherSafetyScopeAuthorityCanBlockInvestigation() {
        val decision = engine.decide(
            CoverageEvidence(1, candidates = listOf(candidate("gap.one"))),
            request = CoverageRequest(
                BiographerPosture.TARGETED_COVERAGE,
                investigationAuthority = BiographerInvestigationAuthority.BLOCKED_BY_SAFETY_SCOPE,
            ),
        )
        assertEquals(BiographerQuestionDisposition.NO_TARGET, decision.disposition)
    }

    @Test
    fun temporalUncertaintyAppearsAsQuestionConstraint() {
        val candidate = CoverageCandidate(
            InvestigationTargetId.parse("gap.approximate"),
            InvestigationTargetKind.TEMPORAL_GAP,
            listOf("assertion-a", "assertion-b"),
            temporalBounds = listOf(EventTime.ApproximateYear(Year.of(2010)), EventTime.ApproximateYear(Year.of(2015))),
            uncertainty = true,
            reasonCode = "SPARSE_INTERVAL",
            status = CoverageStatus.SPARSE,
            materialChangeToken = "approximate-bounds",
        )
        val plan = engine.decide(CoverageEvidence(1, candidates = listOf(candidate)), request = targeted()).plan
        assertTrue(requireNotNull(plan).uncertaintyConstraints.contains("DO_NOT_INCREASE_TEMPORAL_PRECISION"))
        assertTrue(plan.prohibitedActs.contains(BiographerProhibitedQuestionAct.UNSUPPORTED_DATE))
    }

    @Test
    fun rendererCannotExceedOneQuestion() {
        val decision = engine.decide(CoverageEvidence(1, candidates = listOf(candidate("gap.one"))), request = targeted())
        val result = BiographerQuestionExecutor().execute(decision) { "One? Two?" }
        assertEquals(BiographerQuestionRenderDisposition.FAILED_WITHOUT_SOURCE_MUTATION, result.disposition)
    }

    @Test
    fun coverageDigestChangesWithOperationalHistoryButNotRandomness() {
        val evidence = CoverageEvidence(1, candidates = listOf(candidate("gap.one")))
        val first = engine.decide(evidence, request = targeted())
        val repeated = engine.decide(evidence, request = targeted())
        val afterOffer = engine.decide(evidence, first.resultingHistory, targeted())
        assertEquals(first.coverageMap.canonicalDigest, repeated.coverageMap.canonicalDigest)
        assertNotEquals(first.coverageMap.canonicalDigest, afterOffer.coverageMap.canonicalDigest)
    }

    private fun targeted() = CoverageRequest(BiographerPosture.TARGETED_COVERAGE)

    private fun candidate(
        id: String,
        kind: InvestigationTargetKind = InvestigationTargetKind.TEMPORAL_GAP,
        status: CoverageStatus = CoverageStatus.SPARSE,
        token: String = "unchanged",
    ) = CoverageCandidate(
        InvestigationTargetId.parse(id),
        kind,
        listOf("synthetic-basis"),
        reasonCode = "STRUCTURAL_TEST_TARGET",
        status = status,
        materialChangeToken = token,
    )
}

class BiographerAnswerCaptureEngineTest {
    @Test
    fun sourceCapturePrecedesLanguageAndSurvivesProcessingFailure() {
        var admitted = false
        val engine = BiographerAnswerCaptureEngine(
            BiographerAdmissionPort {
                admitted = true
                BiographerAdmissionOutcome(
                    AdmissionDisposition.ACCEPTED,
                    it.stableSourceId,
                    it.sourceRevisionId,
                    1,
                    emptyList(),
                    "a".repeat(64),
                )
            },
            BiographerLanguageProcessor { _, _ -> error("synthetic downstream failure") },
        )
        val result = engine.capture(command())
        assertTrue(admitted)
        assertTrue(result.sourceCaptured)
        assertEquals(BiographerCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE, result.disposition)
    }

    @Test
    fun privateAnswerSkipsLanguage() {
        var languageCalls = 0
        val engine = BiographerAnswerCaptureEngine(
            acceptedPort(),
            BiographerLanguageProcessor { _, _ ->
                languageCalls++
                languageOutcome()
            },
        )
        val result = engine.capture(command(privacy = BiographerPrivacy.PRIVATE))
        assertEquals(0, languageCalls)
        assertEquals(BiographerLanguageDisposition.SKIPPED_PRIVATE, result.receipt?.languageDisposition)
    }

    @Test
    fun nonSyntheticAuthorityFailsBeforeAdmission() {
        var admissionCalls = 0
        val engine = BiographerAnswerCaptureEngine(
            BiographerAdmissionPort {
                admissionCalls++
                error("must not be called")
            },
            BiographerLanguageProcessor { _, _ -> languageOutcome() },
        )
        val result = engine.capture(command(authority = BiographerQualificationAuthority.NOT_AUTHORIZED))
        assertEquals(0, admissionCalls)
        assertEquals(BiographerCaptureDisposition.REJECTED_AUTHORITY, result.disposition)
    }

    private fun acceptedPort() = BiographerAdmissionPort {
        BiographerAdmissionOutcome(
            AdmissionDisposition.ACCEPTED,
            it.stableSourceId,
            it.sourceRevisionId,
            1,
            emptyList(),
            "b".repeat(64),
        )
    }

    private fun languageOutcome() = BiographerLanguageOutcome(
        BiographerLanguageDisposition.SOURCE_ONLY,
        LanguagePerceptionResult(
            "source-revision",
            "c".repeat(64),
            disposition = PerceptionDisposition.NO_EVIDENCE_PROPOSAL,
        ),
        emptyList(),
        1,
        1,
        "d".repeat(64),
    )

    private fun command(
        privacy: BiographerPrivacy = BiographerPrivacy.ELIGIBLE,
        authority: BiographerQualificationAuthority = BiographerQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
    ) = BiographerAnswerCommand(
        BiographerAnswerId.parse("answer-one"),
        BiographerIdempotencyKey.parse("answer-one"),
        0,
        BiographerQuestionPlan(
            BiographerPosture.OPEN_STORY,
            null,
            InvestigationTargetKind.OPEN_STORY,
            emptyList(),
            emptyList(),
            emptyList(),
            BiographerQuestionSemanticAct.OPEN_HISTORICAL_INVITATION,
            reasonCode = "OPEN_STORY",
        ),
        "Synthetic historical account.",
        BiographerAnswerOrigin.TYPED,
        privacy,
        ReportTime(Instant.parse("2040-01-01T00:00:00Z")),
        authority,
    )
}
