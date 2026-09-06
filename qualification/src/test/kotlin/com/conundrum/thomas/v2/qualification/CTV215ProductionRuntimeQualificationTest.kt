package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.runtime.ProductionThomasMode
import com.conundrum.thomas.v2.runtime.ProductionTurnDisposition
import com.conundrum.thomas.v2.runtime.ProductionTurnPrivacy
import com.conundrum.thomas.v2.runtime.TherapySafetyDeclaration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CTV215ProductionRuntimeQualificationTest {
    @Test
    fun `journal true silence commits exactly once and never calls renderer`() = CTV215Harness().use { harness ->
        val result = harness.runtime.submit(
            harness.turn(1, ProductionThomasMode.JOURNAL, "I was sad today.") {
                copy(journalResponsePreference = JournalResponsePreference.NO_RESPONSE)
            },
        )

        assertEquals(ProductionTurnDisposition.NO_RESPONSE, result.disposition)
        assertNotNull(result.committedSourceId)
        assertNull(result.assistantArtifact)
        assertNull(result.renderResult)
        assertTrue(harness.runtime.snapshot().storeRevision >= 1)
        assertEquals(0, harness.runtime.snapshot().rendererCallCount)
        assertEquals(0, harness.runtime.snapshot().acceptedAssistantArtifactCount)
        assertEquals(AcquisitionMode.JOURNAL, harness.runtime.sourceSummaries().single().acquisitionMode)
    }

    @Test
    fun `journal reflection and one-question postures remain renderer bounded`() = CTV215Harness().use { harness ->
        val reflection = harness.runtime.submit(
            harness.turn(1, ProductionThomasMode.JOURNAL, "I was sad today.") {
                copy(journalResponsePreference = JournalResponsePreference.REFLECT)
            },
        )
        val question = harness.runtime.submit(
            harness.turn(2, ProductionThomasMode.JOURNAL, "I think moving there was a mistake.") {
                copy(journalResponsePreference = JournalResponsePreference.ASK_ONE_QUESTION)
            },
        )

        assertEquals(
            "reasons=${reflection.reasonCodes}; render=${reflection.renderResult}",
            ProductionTurnDisposition.COMPLETED,
            reflection.disposition,
        )
        assertNotNull(reflection.assistantArtifact)
        assertEquals(0, reflection.renderResult?.questionCount)
        assertEquals(
            "reasons=${question.reasonCodes}; render=${question.renderResult}",
            ProductionTurnDisposition.COMPLETED,
            question.disposition,
        )
        assertTrue((question.renderResult?.questionCount ?: 0) <= 1)
    }

    @Test
    fun `biographer target is selected upstream and answer persists with provenance`() = CTV215Harness().use { harness ->
        harness.runtime.submit(harness.turn(10, ProductionThomasMode.JOURNAL, "I moved to Denver in 2010."))
        harness.runtime.submit(harness.turn(11, ProductionThomasMode.JOURNAL, "I moved to Portland in 2018."))
        val prompt = harness.runtime.nextBiographerPrompt(12)
        assertNotNull(prompt?.targetId)
        assertEquals(com.conundrum.thomas.v2.biographer.InvestigationTargetKind.TEMPORAL_GAP, prompt?.decision?.coverageMap?.selectedTarget?.kind)
        assertNotNull(prompt)
        assertNotNull(prompt?.renderResult)

        val answer = harness.runtime.submit(
            harness.turn(13, ProductionThomasMode.BIOGRAPHER, "Synthetic historical answer with uncertain timing."),
        )

        assertNotNull(answer.committedSourceId)
        assertTrue(
            harness.runtime.sourceSummaries().any {

                it.acquisitionMode == AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE
            },
        )
    }

    @Test
    fun `therapy route and renderer operate through explicit current-turn safety authority`() = CTV215Harness().use { harness ->
        val result = harness.runtime.submit(
            harness.turn(1, ProductionThomasMode.THERAPY, "${CTV215R1ProductionConversationTest.DECLARATIONS}\nMy specific concern is: I was furious yesterday.") {
                copy(
                    requestedTherapySupport = RequestedOrdinarySupport.LISTEN,
                    therapySafetyDeclaration = TherapySafetyDeclaration.ORDINARY_NON_EMERGENCY_ADULT_CONTEXT,
                )
            },
        )

        assertEquals(
            "reasons=${result.reasonCodes}; route=${result.therapyPlan?.routeDecision}; support=${result.therapyPlan?.renderSupport}",
            ProductionTurnDisposition.COMPLETED,
            result.disposition,
        )
        assertNotNull(result.committedSourceId)
        assertNotNull(result.therapyPlan?.routeDecision)
        assertNotNull(result.assistantArtifact)
        assertEquals(AcquisitionMode.THERAPIST_CONVERSATION, harness.runtime.sourceSummaries().single().acquisitionMode)
    }

    @Test
    fun `unspecified current safety scope preempts ordinary therapy without historical inference`() = CTV215Harness().use { harness ->
        harness.runtime.submit(
            harness.turn(1, ProductionThomasMode.JOURNAL, "Synthetic archived crisis vocabulary unrelated to now."),
        )
        val result = harness.runtime.submit(
            harness.turn(2, ProductionThomasMode.THERAPY, "A benign current project update.") {
                copy(therapySafetyDeclaration = TherapySafetyDeclaration.UNSPECIFIED)
            },
        )

        assertNull(result.therapyPlan?.routeDecision)
        assertTrue(result.therapyPlan?.contextSummary == null)
        assertNotNull(result.assistantArtifact)
    }

    @Test
    fun `current private input is committed for immediate use but excluded from future reuse`() = CTV215Harness().use { harness ->
        val result = harness.runtime.submit(
            harness.turn(1, ProductionThomasMode.JOURNAL) {
                copy(privacy = ProductionTurnPrivacy.PRIVATE)
            },
        )
        val summary = harness.runtime.sourceSummaries().single()

        assertNotNull(result.committedSourceId)
        assertFalse(summary.eligibleForOrdinaryUse)
    }

    @Test
    fun `governed source privacy and deletion propagate without a UI database path`() = CTV215Harness().use { harness ->
        val admitted = harness.runtime.submit(harness.turn(1, ProductionThomasMode.JOURNAL))
        val id = requireNotNull(admitted.committedSourceId)

        val privacy = harness.runtime.changeSourcePrivacy(id, makePrivate = true, commandIndex = 2)
        assertTrue(privacy.accepted)
        assertFalse(harness.runtime.sourceSummaries().single().eligibleForOrdinaryUse)

        val deletion = harness.runtime.deleteSource(id, commandIndex = 3)
        assertTrue(deletion.accepted)
        assertTrue(harness.runtime.sourceSummaries().isEmpty())
    }

    @Test
    fun `protected restart preserves logical state and does not duplicate source evidence`() = CTV215Harness().use { harness ->
        harness.runtime.submit(harness.turn(1, ProductionThomasMode.JOURNAL))
        val before = harness.runtime.snapshot()

        val after = harness.reopen().snapshot()

        assertEquals(before.storeRevision, after.storeRevision)
        assertEquals(before.logicalStateDigest, after.logicalStateDigest)
        assertEquals(1, harness.runtime.sourceSummaries().size)
    }
}
