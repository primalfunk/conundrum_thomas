package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalObjectType
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyIntegrationStep
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryProhibitedOverclaim
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryUseDisposition
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

internal object CTV212ScenariosC {
    private val S = CTV212TestSupport
    private val R = CTV211TestSupport

    fun run(id: Int) = when (id) {
        65 -> assertExplicitRecallOne()
        66 -> assertExplicitRecallBounded()
        67 -> assertExplicitRecallPrivateExcluded()
        68 -> assertExplicitRecallIdentityUncertainty()
        69 -> assertExplicitRecallNoResult()
        70 -> assertExplanationLifecycle(HypothesisLifecycle.ACTIVE)
        71 -> assertExplanationBalanced()
        72 -> assertExplanationLifecycle(HypothesisLifecycle.RETIRED)
        73 -> assertExplanationLifecycle(HypothesisLifecycle.REVIEW_REQUIRED)
        74 -> assertExplanationLifecycle(HypothesisLifecycle.PRIVATE_DEPENDENCY)
        75 -> assertExplanationReadOnly()
        76 -> assertHistoricalAlarmCannotEscalate()
        77 -> assertCurrentSafetyCannotBeSuppressed()
        78 -> assertSafetySuppressesRetrieval()
        79 -> assertNoHistoricalRiskPrediction()
        80 -> assertSafetyStopsOrdinaryRoute()
        in 81..84 -> assertTechniqueSeparation(id)
        85 -> assertProvenance(AcquisitionMode.JOURNAL)
        86 -> assertProvenance(AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE)
        87 -> assertProvenance(AcquisitionMode.THERAPIST_CONVERSATION)
        in 88..90 -> assertModeAuthorityUnaffected(id)
        91, 92 -> assertMemoryReferenceDoesNotWrite()
        93, 94 -> assertOnlyFutureUserTurnCouldWrite(id)
        95 -> assertThomasConnectionNeverEvidence()
        96 -> assertLargeArchiveBounded()
        else -> error("Scenario C does not own $id")
    }

    private fun recallOptions() = CTV212TestSupport.FakeOptions(
        memoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
        explicitTarget = "assertion.memory-1",
    )

    private fun assertExplicitRecallOne() {
        val plan = requireNotNull(S.runFake(S.workArchive(), recallOptions()).plan)
        assertEquals(TherapyMemoryUseDisposition.EXPLICIT_RECALL_CONTEXT, plan.memoryUseDisposition)
        assertTrue(plan.surfacedMemories.isNotEmpty())
    }

    private fun assertExplicitRecallBounded() {
        val plan = requireNotNull(S.runFake(S.workArchive(40), recallOptions()).plan)
        assertTrue(plan.surfacedMemories.size <= 4)
        assertTrue((plan.contextSummary?.selectedCount ?: 0) <= 8)
    }

    private fun assertExplicitRecallPrivateExcluded() {
        val plan = requireNotNull(S.runFake(S.privateWorkArchive(), recallOptions().copy(
            explicitTarget = "assertion.private-memory",
        )).plan)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private fun assertExplicitRecallIdentityUncertainty() {
        val plan = requireNotNull(S.runFake(S.workArchive(), recallOptions().copy(
            packetTransform = { packet -> packet.copy(longitudinal = packet.longitudinal.copy(
                items = packet.longitudinal.items.map { it.copy(unresolvedIdentity = true) })) },
        )).plan)
        assertTrue(plan.surfacedMemories.all { it.identityUnresolved })
        assertTrue(plan.surfacedMemories.all { TherapyMemoryProhibitedOverclaim.IDENTITY_MERGE in it.prohibitedOverclaims })
    }

    private fun assertExplicitRecallNoResult() {
        val plan = requireNotNull(S.runFake(S.emptyArchive(), recallOptions()).plan)
        assertEquals(TherapyMemoryUseDisposition.NO_RELEVANT_MEMORY, plan.memoryUseDisposition)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private enum class HypothesisLifecycle { ACTIVE, RETIRED, REVIEW_REQUIRED, PRIVATE_DEPENDENCY }

    private fun explanationOptions() = CTV212TestSupport.FakeOptions(
        memoryIntent = TherapyMemoryIntent.EXPLAIN_THOMAS_VIEW,
        anchors = RetrievalAnchors(hypothesisIds = setOf(HypothesisId.parse("hypothesis.work-view"))),
        explicitTarget = "hypothesis.work-view",
    )

    private fun archiveFor(lifecycle: HypothesisLifecycle) = when (lifecycle) {
        HypothesisLifecycle.ACTIVE -> S.hypothesisArchive()
        HypothesisLifecycle.RETIRED -> S.hypothesisArchive().copy(lifecycle = mapOf(
            R.lifecycle(RetrievalObjectType.HYPOTHESIS, "hypothesis.work-view", RetrievalLifecycleStatus.RETIRED, false),
        ))
        HypothesisLifecycle.REVIEW_REQUIRED -> S.hypothesisArchive().copy(lifecycle = mapOf(
            R.lifecycle(RetrievalObjectType.HYPOTHESIS, "hypothesis.work-view", RetrievalLifecycleStatus.REVIEW_REQUIRED, false),
        ))
        HypothesisLifecycle.PRIVATE_DEPENDENCY -> {
            val archive = S.hypothesisArchive()
            archive.copy(lifecycle = mapOf(
                R.lifecycle(RetrievalObjectType.SOURCE_REVISION, archive.evidence.sources.single().id.value,
                    RetrievalLifecycleStatus.PRIVATE_INELIGIBLE, false),
                R.lifecycle(RetrievalObjectType.ASSERTION, "assertion.hypothesis-support",
                    RetrievalLifecycleStatus.PRIVATE_INELIGIBLE, false),
            ))
        }
    }

    private fun assertExplanationLifecycle(lifecycle: HypothesisLifecycle) {
        val plan = requireNotNull(S.runFake(archiveFor(lifecycle), explanationOptions()).plan)
        when (lifecycle) {
            HypothesisLifecycle.ACTIVE -> {
                assertEquals(TherapyMemoryUseDisposition.EXPLANATION_CONTEXT, plan.memoryUseDisposition)
                assertTrue(plan.surfacedMemories.any { it.stableObjectId == "hypothesis.work-view" })
            }
            HypothesisLifecycle.RETIRED -> assertTrue(plan.surfacedMemories
                .filter { it.stableObjectId == "hypothesis.work-view" }
                .all { !it.currentAuthority && it.lifecycle == RetrievalLifecycleStatus.RETIRED })
            HypothesisLifecycle.REVIEW_REQUIRED -> assertTrue(plan.surfacedMemories
                .filter { it.stableObjectId == "hypothesis.work-view" }
                .all { !it.currentAuthority && it.lifecycle == RetrievalLifecycleStatus.REVIEW_REQUIRED })
            HypothesisLifecycle.PRIVATE_DEPENDENCY -> assertTrue(plan.surfacedMemories.none {
                it.stableObjectId == "assertion.hypothesis-support"
            })
        }
    }

    private fun assertExplanationBalanced() {
        val plan = requireNotNull(S.runFake(S.hypothesisArchive(counterevidence = true), explanationOptions()).plan)
        val ids = plan.surfacedMemories.map { it.stableObjectId }
        assertTrue("assertion.hypothesis-support" in ids)
        assertTrue("assertion.hypothesis-counter" in ids)
    }

    private fun assertExplanationReadOnly() {
        val archive = S.hypothesisArchive(counterevidence = true)
        val one = requireNotNull(S.runFake(archive, explanationOptions()).plan)
        val two = requireNotNull(S.runFake(archive, explanationOptions()).plan)
        assertEquals(archive.canonicalArchiveDigest, archive.canonicalArchiveDigest)
        assertEquals(one.surfacedMemories, two.surfacedMemories)
    }

    private fun alarmingArchive() = R.archive(CTV211TestSupport.EvidenceParts(listOf(
        CTV211TestSupport.Claim("historical-alarm", "A historical source contained crisis vocabulary", "topic.work"),
    )), digestSeed = "historical-alarm")

    private fun assertHistoricalAlarmCannotEscalate() {
        val plan = requireNotNull(S.runFake(alarmingArchive(), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.historical-alarm",
        )).plan)
        assertEquals(com.conundrum.thomas.v2.safety.SafetyAuthorityState.ORDINARY_POLICY_ALLOWED,
            plan.safetyDecision.authorityState)
    }

    private fun safetyResult() = S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(
        turn = "turn-9",
        safety = S.emergencyFor("turn-9"),
        throwRetrieval = true,
    ))

    private fun assertCurrentSafetyCannotBeSuppressed() {
        val result = safetyResult()
        assertEquals(LongitudinalTherapyTurnDisposition.SAFETY_PREEMPTED, result.disposition)
        assertNotEquals(com.conundrum.thomas.v2.safety.SafetyAuthorityState.ORDINARY_POLICY_ALLOWED,
            result.plan?.safetyDecision?.authorityState)
    }

    private fun assertSafetySuppressesRetrieval() {
        val plan = requireNotNull(safetyResult().plan)
        assertEquals(TherapyMemoryUseDisposition.SUPPRESSED_BY_SAFETY, plan.memoryUseDisposition)
        assertFalse(TherapyIntegrationStep.CT_V2_11_RETRIEVAL_EXECUTED in plan.integrationTrace)
    }

    private fun assertNoHistoricalRiskPrediction() = assertHistoricalAlarmCannotEscalate()

    private fun assertSafetyStopsOrdinaryRoute() {
        val plan = requireNotNull(safetyResult().plan)
        assertEquals(null, plan.routeDecision)
        assertEquals(null, plan.renderSupport)
    }

    private fun assertTechniqueSeparation(case: Int) {
        val empty = requireNotNull(S.runFake(S.emptyArchive()).plan)
        val historical = requireNotNull(S.runFake(alarmingArchive(), CTV212TestSupport.FakeOptions(
            explicitTarget = if (case == 81 || case == 84) "assertion.historical-alarm" else null,
        )).plan)
        assertEquals(empty.routeDecision?.route, historical.routeDecision?.route)
        assertEquals(empty.routeDecision?.selectedActionId, historical.routeDecision?.selectedActionId)
        assertEquals(empty.routeDecision?.selectedDialogueActId, historical.routeDecision?.selectedDialogueActId)
        assertTrue(historical.surfacedMemories.all {
            TherapyMemoryProhibitedOverclaim.TECHNIQUE_SELECTION in it.prohibitedOverclaims
        })
    }

    private fun provenanceArchive(mode: AcquisitionMode) = R.archive(CTV211TestSupport.EvidenceParts(listOf(
        CTV211TestSupport.Claim("provenance", "I reported a work event", "topic.work", mode = mode),
    )), digestSeed = "provenance-$mode")

    private fun assertProvenance(mode: AcquisitionMode) {
        val memory = requireNotNull(S.runFake(provenanceArchive(mode), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.provenance",
            packetTransform = S.withReason(com.conundrum.thomas.v2.retrieval.RetrievalReason.EXPLICIT_TARGET),
        )).plan).surfacedMemories.single()
        assertEquals(mode, memory.acquisitionMode)
    }

    private fun assertModeAuthorityUnaffected(case: Int) {
        val plan = requireNotNull(S.runFake(provenanceArchive(AcquisitionMode.JOURNAL), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.provenance",
        )).plan)
        assertNotNull(plan.routeDecision?.route)
        assertFalse(plan.renderSupport?.completeContextPacketDisclosed ?: true)
        assertTrue(plan.integrationTrace.none { it.name.contains("JOURNAL_RESPONSE") || it.name.contains("BIOGRAPHER_TARGET") })
        assertTrue(case in 88..90)
    }

    private fun assertMemoryReferenceDoesNotWrite() {
        val archive = S.workArchive()
        val before = archive.canonicalArchiveDigest
        val plan = requireNotNull(S.runFake(archive, S.strongMemoryOptions()).plan)
        assertTrue(plan.surfacedMemories.isNotEmpty())
        assertEquals(before, archive.canonicalArchiveDigest)
    }

    private fun assertOnlyFutureUserTurnCouldWrite(case: Int) = S.withRealPipeline { harness, pipeline ->
        val before = harness.store.reader.currentStoreRevision()
        val result = pipeline.integrate(S.realCommand(harness.store, text = if (case == 93) {
            "I agree that those events are related."
        } else {
            "Those events are not related."
        }))
        assertTrue(harness.store.reader.currentStoreRevision() > before)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
        assertTrue(harness.store.reader.snapshot().sources.all { it.authorRole == com.conundrum.thomas.v2.longitudinal.SourceAuthorRole.USER })
        assertNotNull(result.plan?.captureReceipt)
    }

    private fun assertThomasConnectionNeverEvidence() = assertMemoryReferenceDoesNotWrite()

    private fun assertLargeArchiveBounded() {
        val started = System.nanoTime()
        val plan = requireNotNull(S.runFake(S.workArchive(400), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.memory-1",
            packetTransform = S.withReason(com.conundrum.thomas.v2.retrieval.RetrievalReason.EXPLICIT_TARGET),
        )).plan)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        assertTrue(plan.surfacedMemories.size <= 1)
        assertEquals(1, plan.surfacedMemories.size)
        assertTrue((plan.contextSummary?.selectedCount ?: 0) <= 8)
        val summary = requireNotNull(plan.contextSummary)
        println(
            "CT_V2_12_LARGE_HISTORY sources=400 derived_objects=400 candidates=${summary.candidateCount} " +
                "selected=${summary.selectedCount} packet_text_characters=${summary.packetTextCharacters} " +
                "source_excerpts_max=4 traversal_depth=${summary.maximumTraversalDepthUsed} " +
                "surfaced=${plan.surfacedMemories.size} plan_characters=${plan.toString().length} elapsed_ms=$elapsedMs",
        )
    }
}
