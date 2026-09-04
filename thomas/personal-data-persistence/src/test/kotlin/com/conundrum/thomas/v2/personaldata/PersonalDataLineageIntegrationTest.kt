package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.SupersessionKind
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.assertionRef
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalDataLineageIntegrationTest {
    @Test fun `correction lineage and current authority survive restart`() = PersonalDataHarness().use { harness ->
        val originalSource = harness.source("correct-original", "Synthetic original statement")
        val original = harness.assertion("correct-original", originalSource.revisionId)
        harness.admit(originalSource)
        harness.admit(original)
        val correctionSource = harness.source(
            "correct-new",
            "Synthetic explicit correction",
            AcquisitionMode.USER_CORRECTION,
        )
        val correcting = EvidenceAssertion(
            AssertionId.parse("personal-assertion-correct-new"),
            correctionSource.revisionId,
            AssertionSubject.User,
            original.predicate,
            AssertionValue.Text("Synthetic corrected value"),
            UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
        )
        val correction = CorrectionRelation(
            EvidenceRelationId.parse("personal-correction-relation"),
            correcting.id,
            original.id,
            CorrectionEffect.CORRECTS_AND_SUPERSEDES,
            "Synthetic explicit correction",
        )
        val supersession = SupersessionRelation(
            EvidenceRelationId.parse("personal-correction-supersession"),
            ClaimReference.Assertion(correcting.id),
            ClaimReference.Assertion(original.id),
            SupersessionKind.CORRECTS,
            "Synthetic correction lineage",
        )
        val operation = LongitudinalWriteOperation.RecordUserCorrection(
            correctionSource,
            correcting,
            correction,
            supersession,
        )
        assertEquals(
            AdmissionDisposition.ACCEPTED,
            harness.store.admission.submit(harness.request(operation, AdmissionActor.USER, AdmissionOrigin.USER_CORRECTION)).disposition,
        )
        harness.reopen()
        assertEquals(1, harness.store.reader.corrections().size)
        assertEquals(1, harness.store.reader.supersessions().size)
        assertEquals(LongitudinalLifecycleStatus.SUPERSEDED, harness.store.reader.lifecycle(assertionRef(original.id))!!.status)
        assertFalse(harness.store.reader.isEligible(assertionRef(original.id)))
        assertTrue(harness.store.reader.isEligible(assertionRef(correcting.id)))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val directory = Files.createTempDirectory("ct-v2-14-correction-backup-")
            try {
                val restored = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                    backup,
                    key,
                    NioAtomicProtectedArtifactStorage(directory.resolve("correction.ctpd")),
                    MemoryKeyProvider(),
                    IncrementingClock(),
                ).getOrThrow()
                assertEquals(1, restored.store.reader.corrections().size)
                assertEquals(1, restored.store.reader.supersessions().size)
                assertEquals(LongitudinalLifecycleStatus.SUPERSEDED, restored.store.reader.lifecycle(assertionRef(original.id))!!.status)
                restored.store.close()
            } finally {
                directory.toFile().walkBottomUp().forEach { it.delete() }
            }
        }
    }

    @Test fun `privacy restoration remains review required after restart`() = PersonalDataHarness().use { harness ->
        val source = harness.source("privacy-review")
        val assertion = harness.assertion("privacy-review", source.revisionId)
        harness.admit(source)
        harness.admit(assertion)
        listOf(SourcePrivacy.PRIVATE, SourcePrivacy.ELIGIBLE).forEach { privacy ->
            assertEquals(
                AdmissionDisposition.ACCEPTED,
                harness.store.admission.submit(harness.request(
                    LongitudinalWriteOperation.ChangePrivacy(source.stableSourceId, privacy),
                    AdmissionActor.USER,
                    AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
                )).disposition,
            )
        }
        harness.reopen()
        assertEquals(LongitudinalLifecycleStatus.REVIEW_REQUIRED, harness.store.reader.lifecycle(assertionRef(assertion.id))!!.status)
        assertFalse(harness.store.reader.isEligible(assertionRef(assertion.id)))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val directory = Files.createTempDirectory("ct-v2-14-privacy-backup-")
            try {
                val restored = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                    backup,
                    key,
                    NioAtomicProtectedArtifactStorage(directory.resolve("privacy.ctpd")),
                    MemoryKeyProvider(),
                    IncrementingClock(),
                ).getOrThrow()
                assertEquals(LongitudinalLifecycleStatus.REVIEW_REQUIRED, restored.store.reader.lifecycle(assertionRef(assertion.id))!!.status)
                assertFalse(restored.store.reader.isEligible(assertionRef(assertion.id)))
                restored.store.close()
            } finally {
                directory.toFile().walkBottomUp().forEach { it.delete() }
            }
        }
    }

    @Test fun `cross mode source provenance survives backup reset and restore`() = PersonalDataHarness().use { harness ->
        val modes = listOf(
            AcquisitionMode.JOURNAL,
            AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE,
            AcquisitionMode.THERAPIST_CONVERSATION,
        )
        val sources = modes.mapIndexed { index, mode -> harness.source("mode-${index + 1}", mode = mode) }
        sources.forEach(harness::admit)
        val digest = harness.store.reader.canonicalLogicalStateDigest()
        RecoveryKey.generate().use { key ->
            val backup = ProtectedBackupArtifact.fromUserCustody(harness.store.createProtectedBackup(key).protectedBytes())
            val reset = harness.store.reset()
            assertTrue(reset.storeArtifactDeleted && reset.keyMaterialDestroyed)
            val restored = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                backup,
                key,
                harness.storage,
                harness.keyProvider,
                harness.clock,
            ).getOrThrow()
            harness.store = restored.store
            assertEquals(digest, restored.logicalStateDigest)
            assertEquals(modes, sources.map { harness.store.reader.source(it.stableSourceId)!!.provenance.acquisitionMode })
        }
    }

    @Test fun `interrupted restore leaves empty target and is safely retryable`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("restore-interrupt"))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val directory = Files.createTempDirectory("ct-v2-14-interrupted-restore-")
            val storage = NioAtomicProtectedArtifactStorage(directory.resolve("target.ctpd"))
            try {
                val first = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                    backup,
                    key,
                    storage,
                    MemoryKeyProvider(),
                    IncrementingClock(),
                    faultInjector = PersistenceFaultInjector { point ->
                        if (point == PersistenceFaultPoint.BEFORE_RESTORE_COMMIT) error("synthetic interruption")
                    },
                )
                assertTrue(first.isFailure)
                assertFalse(storage.exists())
                val retried = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                    backup,
                    key,
                    storage,
                    MemoryKeyProvider(),
                    IncrementingClock(),
                ).getOrThrow()
                assertEquals(1, retried.restoredRevision)
                retried.store.close()
            } finally {
                directory.toFile().walkBottomUp().forEach { it.delete() }
            }
        }
    }

    @Test fun `empty protected corpus can be backed up and restored`() = PersonalDataHarness().use { harness ->
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val directory = Files.createTempDirectory("ct-v2-14-empty-restore-")
            try {
                val restored = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                    backup,
                    key,
                    NioAtomicProtectedArtifactStorage(directory.resolve("empty.ctpd")),
                    MemoryKeyProvider(),
                    IncrementingClock(),
                ).getOrThrow()
                assertEquals(0, restored.restoredRevision)
                assertTrue(restored.store.reader.snapshot().sources.isEmpty())
                restored.store.close()
            } finally {
                directory.toFile().walkBottomUp().forEach { it.delete() }
            }
        }
    }
}
