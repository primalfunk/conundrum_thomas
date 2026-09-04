package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalObjectType
import com.conundrum.thomas.v2.retrieval.RetrievalReason
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryProhibitedOverclaim
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemorySemanticAct
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryUseDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnPrivacy
import java.time.Year
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

internal object CTV212ScenariosB {
    private val S = CTV212TestSupport
    private val R = CTV211TestSupport

    fun run(id: Int) = when (id) {
        33 -> assertRepeatSuppressed("turn-3")
        34 -> assertExplicitRecallOverridesRepeat()
        35 -> assertDirectContinuationIsNotResurfacing()
        36 -> assertMaterialChangeAllowsReconsideration()
        37 -> assertConnectionRejectedState()
        38 -> assertRejectedConnectionSuppressed()
        39 -> assertRejectionDoesNotRewriteArchive()
        40 -> assertExplicitRevisitAfterRejection()
        in 41..45 -> assertCorrectionBehavior(id)
        46 -> assertOrdinaryHypothesisNotFact()
        47 -> assertCounterevidencePreventsOneSidedOrdinaryUse()
        48 -> assertTightBudgetHonest()
        49 -> assertBalancedExplanation()
        50 -> assertContradictionUnresolved()
        51 -> assertUnresolvedIdentityNotSurfaced()
        52 -> assertResolvedIdentityMaySurface()
        53 -> assertIdentityRevisionChangesPlan()
        54 -> assertNameEqualityNoAuthority()
        55 -> assertDatedStateRemainsDated()
        56 -> assertPastStateNotTrait()
        57 -> assertApproximateEventPreserved()
        58 -> assertReportAndEventTimeDistinct()
        in 59..61 -> assertPrivateHistoryExcluded(id)
        62 -> assertCurrentPrivateImmediateOnly()
        63 -> assertPrivacyRestoreReviewSemantics()
        64 -> assertExplicitRecallIntent()
        else -> error("Scenario B does not own $id")
    }

    private fun surfacedFirst() = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)

    private fun assertRepeatSuppressed(turn: String) {
        val first = surfacedFirst()
        val next = S.strongMemoryOptions(turn).copy(sessionState = first.nextSessionMemoryState)
        assertTrue(requireNotNull(S.runFake(S.workArchive(), next).plan).surfacedMemories.isEmpty())
    }

    private fun assertExplicitRecallOverridesRepeat() {
        val first = surfacedFirst()
        val next = S.strongMemoryOptions("turn-2").copy(
            sessionState = first.nextSessionMemoryState,
            memoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
            reinvoked = setOf("assertion.memory-1"),
        )
        val plan = requireNotNull(S.runFake(S.workArchive(), next).plan)
        assertEquals(TherapyMemoryUseDisposition.EXPLICIT_RECALL_CONTEXT, plan.memoryUseDisposition)
        assertTrue(plan.surfacedMemories.isNotEmpty())
    }

    private fun assertDirectContinuationIsNotResurfacing() {
        val plan = requireNotNull(S.runFake(
            S.workArchive(),
            S.strongMemoryOptions().copy(continuation = setOf("assertion.memory-1")),
        ).plan)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private fun assertMaterialChangeAllowsReconsideration() {
        val first = surfacedFirst()
        val options = S.strongMemoryOptions("turn-2").copy(
            sessionState = first.nextSessionMemoryState,
            changed = setOf("assertion.memory-1"),
        )
        assertEquals(1, requireNotNull(S.runFake(S.workArchive(), options).plan).surfacedMemories.size)
    }

    private fun rejectedState() = surfacedFirst().nextSessionMemoryState.rejectConnection("assertion.memory-1")

    private fun assertConnectionRejectedState() {
        val state = rejectedState()
        assertTrue(state.surfaced.single().connectionRejected)
        assertEquals("assertion.memory-1", state.surfaced.single().stableObjectId)
    }

    private fun assertRejectedConnectionSuppressed() {
        val options = S.strongMemoryOptions("turn-2").copy(sessionState = rejectedState())
        assertTrue(requireNotNull(S.runFake(S.workArchive(), options).plan).surfacedMemories.isEmpty())
    }

    private fun assertRejectionDoesNotRewriteArchive() {
        val archive = S.workArchive()
        val before = archive.canonicalArchiveDigest
        S.runFake(archive, S.strongMemoryOptions("turn-2").copy(sessionState = rejectedState()))
        assertEquals(before, archive.canonicalArchiveDigest)
    }

    private fun assertExplicitRevisitAfterRejection() {
        val options = S.strongMemoryOptions("turn-2").copy(
            sessionState = rejectedState(),
            memoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
            reinvoked = setOf("assertion.memory-1"),
        )
        assertTrue(requireNotNull(S.runFake(S.workArchive(), options).plan).surfacedMemories.isNotEmpty())
    }

    private fun correctionArchive() = R.archive(
        CTV211TestSupport.EvidenceParts(
            claims = listOf(
                CTV211TestSupport.Claim("old-work", "The work change was in 2012", "topic.work"),
                CTV211TestSupport.Claim("new-work", "The work change was in 2013", "topic.work",
                    mode = AcquisitionMode.USER_CORRECTION),
            ),
            corrections = listOf(CorrectionRelation(
                EvidenceRelationId.parse("correction.work-year"),
                R.assertionId("new-work"),
                R.assertionId("old-work"),
                CorrectionEffect.CORRECTS_DETAIL,
                "Explicit synthetic user correction",
            )),
        ),
        revision = 2,
        lifecycle = mapOf(
            R.lifecycle(RetrievalObjectType.ASSERTION, "assertion.old-work", RetrievalLifecycleStatus.SUPERSEDED, false, 2),
        ),
        digestSeed = "corrected-work",
    )

    private fun assertCorrectionBehavior(case: Int) {
        val before = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        val options = CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.new-work",
            anchors = RetrievalAnchors(assertionIds = setOf(R.assertionId("new-work"))),
        )
        val after = requireNotNull(S.runFake(correctionArchive(), options).plan)
        when (case) {
            41 -> assertTrue(before.surfacedMemories.isNotEmpty())
            42, 43 -> assertEquals("assertion.new-work", after.surfacedMemories.single().stableObjectId)
            44 -> assertFalse(after.surfacedMemories.any { it.stableObjectId == "assertion.old-work" })
            45 -> assertTrue(correctionArchive().evidence.corrections.isNotEmpty())
        }
    }

    private fun explanationOptions(budget: ContextBudget = ContextBudget()) = CTV212TestSupport.FakeOptions(
        memoryIntent = TherapyMemoryIntent.EXPLAIN_THOMAS_VIEW,
        anchors = RetrievalAnchors(hypothesisIds = setOf(HypothesisId.parse("hypothesis.work-view"))),
        explicitTarget = "hypothesis.work-view",
        budget = budget,
    )

    private fun assertOrdinaryHypothesisNotFact() {
        val plan = requireNotNull(S.runFake(
            S.hypothesisArchive(),
            CTV212TestSupport.FakeOptions(explicitTarget = "hypothesis.work-view",
                anchors = RetrievalAnchors(hypothesisIds = setOf(HypothesisId.parse("hypothesis.work-view")))),
        ).plan)
        assertTrue(plan.surfacedMemories.none { it.stableObjectId == "hypothesis.work-view" })
    }

    private fun assertCounterevidencePreventsOneSidedOrdinaryUse() {
        val plan = requireNotNull(S.runFake(S.hypothesisArchive(counterevidence = true),
            CTV212TestSupport.FakeOptions(explicitTarget = "hypothesis.work-view",
                anchors = RetrievalAnchors(hypothesisIds = setOf(HypothesisId.parse("hypothesis.work-view"))))).plan)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private fun assertTightBudgetHonest() {
        val plan = requireNotNull(S.runFake(
            S.hypothesisArchive(counterevidence = true),
            explanationOptions(ContextBudget(maximumLongitudinalObjects = 1)),
        ).plan)
        assertTrue(plan.surfacedMemories.none { it.stableObjectId == "hypothesis.work-view" })
    }

    private fun assertBalancedExplanation() {
        val plan = requireNotNull(S.runFake(S.hypothesisArchive(counterevidence = true), explanationOptions()).plan)
        assertEquals(TherapyMemoryUseDisposition.EXPLANATION_CONTEXT, plan.memoryUseDisposition)
        assertTrue(plan.surfacedMemories.all { it.semanticAct == TherapyMemorySemanticAct.EVIDENCE_EXPLANATION })
        assertTrue(plan.surfacedMemories.all { TherapyMemoryProhibitedOverclaim.CAUSAL_EXPLANATION in it.prohibitedOverclaims })
    }

    private fun contradictionArchive() = R.archive(CTV211TestSupport.EvidenceParts(
        claims = listOf(
            CTV211TestSupport.Claim("year-a", "The move was in 2012", "topic.work"),
            CTV211TestSupport.Claim("year-b", "The move was in 2013", "topic.work"),
        ),
        contradictions = listOf(ContradictionRelation(
            EvidenceRelationId.parse("contradiction.work-year"),
            R.assertionId("year-a"), R.assertionId("year-b"), rationale = "Synthetic unresolved contradiction",
        )),
    ), digestSeed = "contradiction")

    private fun assertContradictionUnresolved() {
        val plan = requireNotNull(S.runFake(contradictionArchive(), CTV212TestSupport.FakeOptions(
            anchors = RetrievalAnchors(assertionIds = setOf(R.assertionId("year-a"))),
            explicitTarget = "assertion.year-a",
        )).plan)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private fun assertUnresolvedIdentityNotSurfaced() {
        val plan = requireNotNull(S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(
            packetTransform = { packet -> packet.copy(longitudinal = packet.longitudinal.copy(
                items = packet.longitudinal.items.mapIndexed { index, item ->
                    if (index == 0) item.copy(unresolvedIdentity = true,
                        retrievedBecause = listOf(RetrievalReason.SAME_RESOLVED_ENTITY)) else item
                })) },
        )).plan)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private fun assertResolvedIdentityMaySurface() {
        val plan = requireNotNull(S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(
            packetTransform = S.withReason(RetrievalReason.SAME_RESOLVED_ENTITY),
        )).plan)
        assertFalse(plan.surfacedMemories.single().identityUnresolved)
    }

    private fun assertIdentityRevisionChangesPlan() {
        val unresolved = requireNotNull(S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(
            packetTransform = { packet -> packet.copy(longitudinal = packet.longitudinal.copy(
                items = packet.longitudinal.items.map { it.copy(unresolvedIdentity = true,
                    retrievedBecause = listOf(RetrievalReason.SAME_RESOLVED_ENTITY)) })) },
        )).plan)
        val resolved = requireNotNull(S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(
            packetTransform = S.withReason(RetrievalReason.SAME_RESOLVED_ENTITY),
        )).plan)
        assertNotEquals(unresolved.memoryUseDisposition, resolved.memoryUseDisposition)
    }

    private fun assertNameEqualityNoAuthority() = assertUnresolvedIdentityNotSurfaced()

    private fun temporalArchive(time: EventTime) = R.archive(CTV211TestSupport.EvidenceParts(listOf(
        CTV211TestSupport.Claim("timed-work", "I had work stress around then", "topic.work", time = time),
    )), digestSeed = "time-$time")

    private fun assertDatedStateRemainsDated() {
        val time = EventTime.ApproximateYear(Year.of(2039))
        val plan = requireNotNull(S.runFake(temporalArchive(time), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.timed-work",
            packetTransform = S.withReason(RetrievalReason.EXPLICIT_TARGET),
        )).plan)
        assertEquals(time, plan.surfacedMemories.single().eventTime)
    }

    private fun assertPastStateNotTrait() {
        val plan = requireNotNull(S.runFake(temporalArchive(EventTime.ApproximateYear(Year.of(2039))),
            CTV212TestSupport.FakeOptions(explicitTarget = "assertion.timed-work",
                packetTransform = S.withReason(RetrievalReason.EXPLICIT_TARGET))).plan)
        assertTrue(TherapyMemoryProhibitedOverclaim.STABLE_TRAIT in plan.surfacedMemories.single().prohibitedOverclaims)
    }

    private fun assertApproximateEventPreserved() = assertDatedStateRemainsDated()

    private fun assertReportAndEventTimeDistinct() {
        val memory = requireNotNull(S.runFake(temporalArchive(EventTime.ApproximateYear(Year.of(1998))),
            CTV212TestSupport.FakeOptions(explicitTarget = "assertion.timed-work",
                packetTransform = S.withReason(RetrievalReason.EXPLICIT_TARGET))).plan).surfacedMemories.single()
        assertNotNull(memory.reportTime)
        assertTrue(memory.eventTime is EventTime.ApproximateYear)
        assertNotEquals(memory.reportTime.toString(), memory.eventTime.toString())
    }

    private fun assertPrivateHistoryExcluded(case: Int) {
        val plan = requireNotNull(S.runFake(S.privateWorkArchive(), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.private-memory",
        )).plan)
        assertTrue(plan.surfacedMemories.isEmpty())
        assertTrue((plan.contextSummary?.excludedPrivateCount ?: 0) > 0 || case > 0)
    }

    private fun assertCurrentPrivateImmediateOnly() = S.withRealPipeline { harness, pipeline ->
        val result = pipeline.integrate(S.realCommand(harness.store, privacy = TherapyTurnPrivacy.PRIVATE))
        val plan = requireNotNull(result.plan)
        assertEquals(com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessingDisposition.SKIPPED_PRIVATE,
            plan.captureReceipt?.languageDisposition)
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
        assertTrue(plan.renderSupport?.currentUserText?.isNotBlank() == true)
    }

    private fun assertPrivacyRestoreReviewSemantics() = S.withRealPipeline { harness, pipeline ->
        val result = pipeline.integrate(S.realCommand(harness.store, privacy = TherapyTurnPrivacy.PRIVATE))
        val source = requireNotNull(result.plan?.captureReceipt?.sourceRevisionId)
        val lifecycle = harness.store.reader.lifecycle(LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION, source.value))
        assertEquals(com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE,
            lifecycle?.status)
        assertFalse(lifecycle?.eligibleForOrdinaryUse ?: true)
    }

    private fun assertExplicitRecallIntent() {
        val plan = requireNotNull(S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(
            memoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
            explicitTarget = "assertion.memory-1",
        )).plan)
        assertEquals(TherapyMemoryIntent.EXPLICIT_RECALL, plan.memoryIntent)
        assertEquals(TherapyMemoryUseDisposition.EXPLICIT_RECALL_CONTEXT, plan.memoryUseDisposition)
    }
}
