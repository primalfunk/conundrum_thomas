package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.HypothesisDraft
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.assertionRef
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertAccepted
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.path
import com.conundrum.thomas.v2.qualification.longitudinal.CanonicalLongitudinalFixtures
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import com.conundrum.thomas.v2.qualification.longitudinalstore.toDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalDurableFixtureTest {
    @Test fun `fixture A contemporaneous Journal event survives reopen`() = withStore("fixture-a") { harness ->
        val fixture = CanonicalLongitudinalFixtures.contemporaneousJournalEvent()
        harness.admitBasicSnapshot(fixture); harness.reopen()
        val actual = harness.store.reader.snapshot()
        assertEquals(fixture.sources.single().provenance.acquisitionMode, actual.sources.single().provenance.acquisitionMode)
        assertEquals(fixture.assertions.single().eventTime, actual.assertions.single().eventTime)
    }

    @Test fun `fixture B retrospective Biographer account remains separate from Journal`() = withStore("fixture-b") { harness ->
        val fixture = CanonicalLongitudinalFixtures.retrospectiveBiographerAccount()
        harness.admitBasicSnapshot(fixture); harness.reopen()
        val actual = harness.store.reader.snapshot()
        assertEquals(2, actual.sources.size)
        assertEquals(2, actual.sources.map { it.provenance.acquisitionMode }.toSet().size)
        assertEquals(2, actual.assertions.size)
        assertEquals(1, actual.entities.size)
    }

    @Test fun `fixture C contradiction persists with both endpoints`() = withStore("fixture-c") { harness ->
        val fixture = CanonicalLongitudinalFixtures.contradictoryAccounts()
        harness.admitBasicSnapshot(fixture); harness.reopen()
        val actual = harness.store.reader.snapshot()
        assertEquals(4, actual.assertions.size)
        assertEquals(1, actual.contradictions.size)
        assertNotNull(actual.assertions.firstOrNull { it.id == actual.contradictions.single().leftAssertionId })
        assertNotNull(actual.assertions.firstOrNull { it.id == actual.contradictions.single().rightAssertionId })
    }

    @Test fun `fixture D explicit correction preserves original and supersession`() = withStore("fixture-d") { harness ->
        val fixture = CanonicalLongitudinalFixtures.explicitCorrection()
        val originalSource = fixture.sources.first { it.provenance.acquisitionMode != com.conundrum.thomas.v2.longitudinal.AcquisitionMode.USER_CORRECTION }
        val correctionSource = fixture.sources.first { it.provenance.acquisitionMode == com.conundrum.thomas.v2.longitudinal.AcquisitionMode.USER_CORRECTION }
        val original = fixture.assertions.first { it.sourceRecordId == originalSource.id }
        val correcting = fixture.assertions.first { it.sourceRecordId == correctionSource.id }
        assertAccepted(harness.submitSource(originalSource))
        assertAccepted(harness.submitBundle(EvidenceBundle(assertions = listOf(original))))
        val operation = LongitudinalWriteOperation.RecordUserCorrection(correctionSource.toDraft(), correcting, fixture.corrections.single(), fixture.supersessions.single())
        assertAccepted(harness.store.admission.submit(harness.request(operation, AdmissionActor.USER, AdmissionOrigin.USER_CORRECTION)))
        assertAccepted(harness.submitBundle(EvidenceBundle(entities = fixture.entities)))
        harness.reopen()
        assertEquals(2, harness.store.reader.snapshot().sources.size)
        assertEquals(2, harness.store.reader.snapshot().assertions.size)
        assertEquals(CorrectionEffect.CORRECTS_AND_SUPERSEDES, harness.store.reader.snapshot().corrections.single().effect)
        assertEquals(LongitudinalLifecycleStatus.SUPERSEDED, harness.store.reader.lifecycle(assertionRef(original.id))!!.status)
    }

    @Test fun `fixture E third party motivation remains user interpretation`() = withStore("fixture-e") { harness ->
        val fixture = CanonicalLongitudinalFixtures.userInterpretation()
        harness.admitBasicSnapshot(fixture); harness.reopen()
        assertEquals(UserEvidenceKind.USER_INTERPRETATION, harness.store.reader.snapshot().assertions.single().kind)
    }

    @Test fun `fixture F Thomas hypothesis remains separate with visible dependencies`() = withStore("fixture-f") { harness ->
        val fixture = CanonicalLongitudinalFixtures.thomasHypothesis()
        fixture.sources.forEach { assertAccepted(harness.submitSource(it)) }
        assertAccepted(harness.submitBundle(EvidenceBundle(assertions = fixture.assertions)))
        val hypothesis = fixture.hypotheses.single()
        val draft = HypothesisDraft(hypothesis.id, hypothesis.subject, hypothesis.predicate, hypothesis.proposedValue, HypothesisStatus.TENTATIVE, hypothesis.rationale)
        assertAccepted(harness.submitBundle(EvidenceBundle(hypothesisDrafts = listOf(draft), hypothesisDependencies = fixture.hypothesisDependencies), AdmissionActor.THOMAS))
        harness.reopen()
        assertEquals(1, harness.store.reader.snapshot().hypotheses.size)
        assertEquals(2, harness.store.reader.directDependencies(ClaimReference.Hypothesis(hypothesis.id)).size)
        assertFalse(harness.store.reader.snapshot().assertions.any { it.id.value == hypothesis.id.value })
    }

    @Test fun `fixture G approximate year survives without exact date`() = withStore("fixture-g") { harness ->
        val fixture = CanonicalLongitudinalFixtures.uncertainTime()
        harness.admitBasicSnapshot(fixture); harness.reopen()
        assertTrue(harness.store.reader.snapshot().assertions.single().eventTime is EventTime.ApproximateYear)
    }

    @Test fun `fixture H unresolved Sam identity survives without merge`() = withStore("fixture-h") { harness ->
        val fixture = CanonicalLongitudinalFixtures.unresolvedPersonIdentity()
        harness.admitBasicSnapshot(fixture); harness.reopen()
        assertEquals(2, harness.store.reader.snapshot().entities.size)
        assertEquals(EntityIdentityStatus.UNRESOLVED, harness.store.reader.identityLinks().single().status)
    }

    @Test fun `fixture I private and declined remain valid coverage states`() = withStore("fixture-i") { harness ->
        val fixture = CanonicalLongitudinalFixtures.privateAndDeclinedCoverage()
        harness.admitBasicSnapshot(fixture); harness.reopen()
        assertEquals(setOf(InformationCoverageStatus.PRIVATE, InformationCoverageStatus.DECLINED), harness.store.reader.coverage().map { it.status }.toSet())
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
    }

    private fun withStore(name: String, block: (SyntheticLongitudinalStoreHarness) -> Unit) {
        SyntheticLongitudinalStoreHarness(path(name)).use(block)
    }
}
