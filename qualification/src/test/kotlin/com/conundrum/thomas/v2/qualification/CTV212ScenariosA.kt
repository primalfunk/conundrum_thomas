package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.qualification.therapy.GovernedLongitudinalTherapyPipeline
import com.conundrum.thomas.v2.retrieval.RetrievalItemKind
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalReason
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyDegradationDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessor
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryRelation
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryUseDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnCaptureDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnCaptureOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

internal object CTV212ScenariosA {
    private val S = CTV212TestSupport

    fun run(id: Int) = when (id) {
        1 -> assertEmptyHistory()
        2 -> assertRetrievalFailureDegrades()
        3 -> assertUnrelatedArchiveDoesNotSurface()
        4 -> assertDeterministicPlan()
        5 -> assertRealCapture(TherapyTurnCaptureOrigin.TYPED)
        6 -> assertRealCapture(TherapyTurnCaptureOrigin.SPEECH_TRANSCRIPT)
        7, 8 -> assertAssistantArtifactsAreNotEvidence()
        9 -> assertCaptureFailureDegrades()
        10 -> assertDerivationFailurePreservesSource()
        in 11..14 -> assertSameTurnBoundary(id)
        15 -> assertNextTurnCanUsePrior()
        in 16..20 -> assertRouteIndependence(id)
        21 -> assertStrongRelation(RetrievalReason.SAME_RESOLVED_ENTITY, TherapyMemoryRelation.DIRECT_ENTITY_CONTINUITY)
        22 -> assertStrongRelation(RetrievalReason.SAME_EVENT, TherapyMemoryRelation.DIRECT_EVENT_CONTINUITY)
        23 -> assertStrongRelation(RetrievalReason.SAME_RELATIONSHIP, TherapyMemoryRelation.DIRECT_RELATIONSHIP_CONTINUITY)
        24 -> assertRecurrenceIsNeutral()
        in 25..28 -> assertWeakRelationNotSurfaced(id)
        29 -> assertDeterministicBestMemory()
        30 -> assertOrdinaryMaximumOne()
        31 -> assertFirstSurfaceRecorded()
        32 -> assertOrdinaryRepeatSuppressed()
        else -> error("Scenario A does not own $id")
    }

    private fun assertEmptyHistory() {
        val result = S.runFake(S.emptyArchive())
        val plan = requireNotNull(result.plan)
        assertEquals(LongitudinalTherapyTurnDisposition.COMPLETE_LONGITUDINAL_THERAPY_SUCCESS, result.disposition)
        assertNotNull(plan.routeDecision?.route)
        assertEquals(TherapyMemoryUseDisposition.NO_RELEVANT_MEMORY, plan.memoryUseDisposition)
        assertTrue(plan.surfacedMemories.isEmpty())
        assertNotNull(plan.renderSupport)
    }

    private fun assertRetrievalFailureDegrades() {
        val result = S.runFake(S.emptyArchive(), CTV212TestSupport.FakeOptions(throwRetrieval = true))
        val plan = requireNotNull(result.plan)
        assertEquals(LongitudinalTherapyTurnDisposition.MEMORYLESS_THERAPY, result.disposition)
        assertNotNull(plan.routeDecision?.route)
        assertTrue(TherapyDegradationDisposition.LONGITUDINAL_RETRIEVAL_UNAVAILABLE in plan.degradation)
        assertNotNull(plan.renderSupport)
    }

    private fun assertUnrelatedArchiveDoesNotSurface() {
        val result = S.runFake(S.workArchive(400))
        val plan = requireNotNull(result.plan)
        assertTrue(plan.surfacedMemories.isEmpty())
        assertTrue((plan.contextSummary?.selectedCount ?: 0) <= plan.contextSummary?.candidateCount ?: 0)
    }

    private fun assertDeterministicPlan() {
        val one = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        val two = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        assertEquals(one.canonicalPlanDigest, two.canonicalPlanDigest)
        assertEquals(one.routeDecision, two.routeDecision)
    }

    private fun assertRealCapture(origin: TherapyTurnCaptureOrigin) = S.withRealPipeline { harness, pipeline ->
        val result = pipeline.integrate(S.realCommand(harness.store, origin = origin))
        val receipt = requireNotNull(result.plan?.captureReceipt)
        val source = requireNotNull(harness.store.reader.source(receipt.stableSourceId))
        assertEquals(AcquisitionMode.THERAPIST_CONVERSATION, source.provenance.acquisitionMode)
        assertEquals(SourceAuthorRole.USER, source.authorRole)
        assertEquals(origin.name, source.provenance.metadata["therapy.capture-origin"])
        assertEquals(origin, receipt.captureOrigin)
    }

    private fun assertAssistantArtifactsAreNotEvidence() = S.withRealPipeline { harness, pipeline ->
        val result = pipeline.integrate(S.realCommand(harness.store))
        assertNotNull(result.plan?.renderSupport)
        val snapshot = harness.store.reader.snapshot()
        assertTrue(snapshot.sources.all { it.authorRole == SourceAuthorRole.USER })
        assertEquals(1, snapshot.sources.size)
        assertFalse(snapshot.sources.any { source -> source.originalContent.toString().contains("RenderCommand") })
    }

    private fun assertCaptureFailureDegrades() {
        val result = S.runFake(S.emptyArchive(), CTV212TestSupport.FakeOptions(failCapture = true))
        val plan = requireNotNull(result.plan)
        assertEquals(LongitudinalTherapyTurnDisposition.MEMORYLESS_THERAPY, result.disposition)
        assertEquals(TherapyTurnCaptureDisposition.SOURCE_ADMISSION_FAILED, plan.currentTurnCaptureDisposition)
        assertNotNull(plan.routeDecision?.route)
    }

    private fun assertDerivationFailurePreservesSource() = S.withRealPipeline { harness, _ ->
        val pipeline = GovernedLongitudinalTherapyPipeline(
            harness.store,
            languageProcessorOverride = TherapyLanguageProcessor { error("synthetic processing failure") },
        )
        val result = pipeline.integrate(S.realCommand(harness.store))
        assertEquals(LongitudinalTherapyTurnDisposition.MEMORYLESS_THERAPY, result.disposition)
        assertEquals(TherapyTurnCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
            result.plan?.currentTurnCaptureDisposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
        assertNotNull(result.plan?.renderSupport)
    }

    private fun assertSameTurnBoundary(case: Int) = S.withRealPipeline { harness, pipeline ->
        val before = harness.store.reader.currentStoreRevision()
        val result = pipeline.integrate(S.realCommand(harness.store, text = "I am frustrated at work."))
        val plan = requireNotNull(result.plan)
        when (case) {
            11 -> assertEquals(0, before)
            12 -> assertNotNull(plan.captureReceipt)
            13 -> assertEquals(before, plan.contextSummary?.snapshotRevision)
            14 -> {
                assertTrue(plan.surfacedMemories.isEmpty())
                assertFalse(plan.captureReceipt?.sourceRevisionId in plan.surfacedMemories.flatMap { it.sourceRevisionIds })
            }
        }
    }

    private fun assertNextTurnCanUsePrior() {
        val first = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        assertEquals(TherapyMemoryUseDisposition.SURFACE_ONE_MEMORY, first.memoryUseDisposition)
        assertEquals("assertion.memory-1", first.surfacedMemories.single().stableObjectId)
    }

    private fun assertRouteIndependence(case: Int) {
        val baseline = requireNotNull(S.runFake(S.emptyArchive()).plan)
        val options = when (case) {
            16 -> CTV212TestSupport.FakeOptions()
            17 -> S.strongMemoryOptions()
            18 -> CTV212TestSupport.FakeOptions(packetTransform = S.withReason(RetrievalReason.EXPLICIT_LOOK_BACK_LEXICAL_MATCH))
            19 -> CTV212TestSupport.FakeOptions(packetTransform = { packet -> packet.copy(longitudinal = packet.longitudinal.copy(
                items = packet.longitudinal.items.map { it.copy(lifecycle = RetrievalLifecycleStatus.CONTESTED) })) })
            else -> CTV212TestSupport.FakeOptions()
        }
        val archive = if (case == 20) S.privateWorkArchive() else S.workArchive()
        val variant = requireNotNull(S.runFake(archive, options).plan)
        assertEquals(baseline.safetyDecision.authorityState, variant.safetyDecision.authorityState)
        assertEquals(baseline.routeDecision?.route, variant.routeDecision?.route)
        assertEquals(baseline.routeDecision?.selectedActionId, variant.routeDecision?.selectedActionId)
        assertEquals(baseline.routeDecision?.progression, variant.routeDecision?.progression)
    }

    private fun assertStrongRelation(reason: RetrievalReason, relation: TherapyMemoryRelation) {
        val options = CTV212TestSupport.FakeOptions(packetTransform = S.withReason(reason))
        val plan = requireNotNull(S.runFake(S.workArchive(), options).plan)
        assertEquals(TherapyMemoryUseDisposition.SURFACE_ONE_MEMORY, plan.memoryUseDisposition)
        assertEquals(relation, plan.surfacedMemories.single().relation)
    }

    private fun assertRecurrenceIsNeutral() {
        val transform: (com.conundrum.thomas.v2.contextpacket.ContextPacket) -> com.conundrum.thomas.v2.contextpacket.ContextPacket = { packet ->
            packet.copy(longitudinal = packet.longitudinal.copy(items = packet.longitudinal.items.mapIndexed { index, item ->
                if (index == 0) item.copy(kind = RetrievalItemKind.RECURRENCE,
                    retrievedBecause = listOf(RetrievalReason.SAME_PREDICATE)) else item
            }))
        }
        val plan = requireNotNull(S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(packetTransform = transform)).plan)
        val memory = plan.surfacedMemories.single()
        assertEquals(TherapyMemoryRelation.QUALIFIED_REPORTED_RECURRENCE, memory.relation)
        assertTrue(com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryProhibitedOverclaim.STABLE_TRAIT in memory.prohibitedOverclaims)
    }

    private fun assertWeakRelationNotSurfaced(case: Int) {
        val reason = when (case) {
            25, 26 -> RetrievalReason.SAME_PREDICATE
            27 -> RetrievalReason.EXPLICIT_LOOK_BACK_LEXICAL_MATCH
            else -> RetrievalReason.TEMPORAL_MATCH
        }
        val plan = requireNotNull(S.runFake(S.workArchive(), CTV212TestSupport.FakeOptions(packetTransform = S.withReason(reason))).plan)
        assertTrue(plan.surfacedMemories.isEmpty())
        assertEquals(TherapyMemoryUseDisposition.CONTEXT_AVAILABLE_NOT_SURFACED, plan.memoryUseDisposition)
    }

    private fun assertDeterministicBestMemory() {
        val options = CTV212TestSupport.FakeOptions(explicitTarget = "assertion.memory-1")
        val first = requireNotNull(S.runFake(S.workArchive(20), options).plan)
        val second = requireNotNull(S.runFake(S.workArchive(20), options).plan)
        assertEquals(first.surfacedMemories, second.surfacedMemories)
    }

    private fun assertOrdinaryMaximumOne() {
        val plan = requireNotNull(S.runFake(S.workArchive(20), CTV212TestSupport.FakeOptions(explicitTarget = "assertion.memory-1")).plan)
        assertTrue(plan.surfacedMemories.size <= 1)
        assertTrue(plan.renderSupport?.surfacedMemorySupport?.size ?: 0 <= 1)
    }

    private fun assertFirstSurfaceRecorded() {
        val plan = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        assertEquals(1, plan.nextSessionMemoryState.surfaced.size)
        assertEquals(plan.surfacedMemories.single().stableObjectId,
            plan.nextSessionMemoryState.surfaced.single().stableObjectId)
    }

    private fun assertOrdinaryRepeatSuppressed() {
        val first = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        val secondOptions = S.strongMemoryOptions("turn-2").copy(sessionState = first.nextSessionMemoryState)
        val second = requireNotNull(S.runFake(S.workArchive(), secondOptions).plan)
        assertTrue(second.surfacedMemories.isEmpty())
    }
}
