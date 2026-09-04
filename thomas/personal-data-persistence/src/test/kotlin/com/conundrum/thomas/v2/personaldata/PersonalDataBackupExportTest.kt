package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.DependencyRole
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.HypothesisDraft
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalDataBackupExportTest {
    @Test fun `failed or interrupted backup never mutates the primary corpus`() {
        listOf(
            PersistenceFaultPoint.BEFORE_BACKUP_PROTECTION,
            PersistenceFaultPoint.AFTER_BACKUP_PROTECTION,
        ).forEach { faultPoint ->
            PersonalDataHarness().use { harness ->
                harness.admit(harness.source("backup-fault-${faultPoint.name.lowercase().replace('_', '-')}"))
                val before = harness.store.reader.canonicalLogicalStateDigest()
                harness.store.close()
                val opened = ProtectedPersonalDataStoreFactory.open(
                    harness.storage,
                    harness.keyProvider,
                    harness.clock,
                    faultInjector = PersistenceFaultInjector { point ->
                        if (point == faultPoint) error("synthetic backup interruption")
                    },
                ) as PersonalDataOpenResult.Opened
                harness.store = opened.store
                RecoveryKey.generate().use { key ->
                    assertTrue(runCatching { harness.store.createProtectedBackup(key) }.isFailure)
                }
                assertEquals(before, harness.store.reader.canonicalLogicalStateDigest())
                assertEquals(1, harness.store.reader.snapshot().sources.size)
            }
        }
    }

    @Test fun `machine and human exports distinguish source from derived state`() = PersonalDataHarness().use { harness ->
        val source = harness.source("export", "Synthetic export statement", AcquisitionMode.THERAPIST_CONVERSATION)
        harness.admit(source)
        harness.admit(harness.assertion("export", source.revisionId))
        val export = harness.store.export()
        assertTrue(export.machineReadableJson.contains("sourceEvidence"))
        assertTrue(export.machineReadableJson.contains("derivedState"))
        assertTrue(export.machineReadableJson.contains("THERAPIST_CONVERSATION"))
        assertTrue(export.humanReadableMarkdown.contains("## Source evidence"))
        assertTrue(export.humanReadableMarkdown.contains("## Derived state"))
        assertFalse(export.machineReadableJson.contains("qualification.primary"))
    }

    @Test fun `exports expose governed derived dependency lineage rather than counts only`() = PersonalDataHarness().use { harness ->
        val source = harness.source("export-lineage")
        val assertion = harness.assertion("export-lineage", source.revisionId)
        harness.admit(source)
        harness.admit(assertion)
        val hypothesisId = HypothesisId.parse("personal-hypothesis-export-lineage")
        val dependency = HypothesisDependency(
            EvidenceRelationId.parse("personal-dependency-export-lineage"),
            hypothesisId,
            ClaimReference.Assertion(assertion.id),
            DependencyRole.SUPPORTS,
            "Synthetic export dependency lineage",
        )
        val hypothesis = HypothesisDraft(
            hypothesisId,
            AssertionSubject.User,
            AssertionPredicate(PersonalConceptId.parse("personal.export-lineage"), PredicateSemantics.OTHER),
            AssertionValue.Text("Tentative synthetic exported interpretation"),
            HypothesisStatus.TENTATIVE,
            "Synthetic export hypothesis with explicit evidence dependency",
        )
        assertEquals(AdmissionDisposition.ACCEPTED, harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.AdmitEvidenceBundle(EvidenceBundle(
                hypothesisDrafts = listOf(hypothesis),
                hypothesisDependencies = listOf(dependency),
            )),
            AdmissionActor.THOMAS,
            AdmissionOrigin.THOMAS_DERIVATION,
        )).disposition)
        val export = harness.store.export()
        listOf(
            assertion.id.value,
            source.revisionId.value,
            hypothesisId.value,
            dependency.id.value,
            "ASSERTION:${assertion.id.value}",
        ).forEach { expected -> assertTrue(export.machineReadableJson.contains(expected)) }
        assertTrue(export.humanReadableMarkdown.contains(dependency.id.value))
        assertFalse(export.machineReadableJson.contains("\"assertionCount\""))
    }

    @Test fun `machine export escapes every JSON control character`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("export-control", "Synthetic " + 1.toChar() + " control"))
        val machine = harness.store.export().machineReadableJson
        assertTrue(machine.contains("\\u0001"))
        assertFalse(machine.contains(1.toChar()))
    }

    @Test fun `protected backup contains no source plaintext`() = PersonalDataHarness().use { harness ->
        val phrase = "Synthetic backup marker silver-lantern-831"
        harness.admit(harness.source("backup-protected", phrase))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            assertFalse(String(backup.protectedBytes(), StandardCharsets.ISO_8859_1).contains(phrase))
            assertEquals(64, backup.protectedSha256.length)
        }
    }

    @Test fun `protected backup custody bytes round trip through strict parser`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("backup-custody"))
        RecoveryKey.generate().use { key ->
            val created = harness.store.createProtectedBackup(key)
            val imported = ProtectedBackupArtifact.fromUserCustody(created.protectedBytes())
            assertEquals(created.sourceStoreRevision, imported.sourceStoreRevision)
            assertEquals(created.protectedSha256, imported.protectedSha256)
            assertTrue(imported.protectedBytes().contentEquals(created.protectedBytes()))
        }
    }

    @Test fun `backup restore preserves logical state and provenance`() = PersonalDataHarness().use { sourceHarness ->
        val source = sourceHarness.source("restore", mode = AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE)
        sourceHarness.admit(source)
        sourceHarness.admit(sourceHarness.assertion("restore", source.revisionId))
        val before = sourceHarness.store.reader.canonicalLogicalStateDigest()
        RecoveryKey.generate().use { key ->
            val backup = sourceHarness.store.createProtectedBackup(key)
            val directory = Files.createTempDirectory("ct-v2-14-restore-")
            try {
                val result = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                    backup, key, NioAtomicProtectedArtifactStorage(directory.resolve("restored.ctpd")),
                    MemoryKeyProvider(ByteArray(32) { (it + 77).toByte() }), IncrementingClock(),
                ).getOrThrow()
                assertEquals(before, result.logicalStateDigest)
                assertEquals(before, result.store.reader.canonicalLogicalStateDigest())
                assertEquals(AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE,
                    result.store.reader.source(source.stableSourceId)!!.provenance.acquisitionMode)
                result.store.close()
            } finally {
                directory.toFile().walkBottomUp().forEach { it.delete() }
            }
        }
    }

    @Test fun `wrong recovery key rejects backup without creating target`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("backup-wrong-key"))
        RecoveryKey.generate().use { right ->
            val backup = harness.store.createProtectedBackup(right)
            RecoveryKey.generate().use { wrong ->
                val directory = Files.createTempDirectory("ct-v2-14-wrong-key-")
                val storage = NioAtomicProtectedArtifactStorage(directory.resolve("restore.ctpd"))
                try {
                    val failure = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                        backup, wrong, storage, MemoryKeyProvider(), IncrementingClock(),
                    ).exceptionOrNull() as PersonalDataPersistenceException
                    assertEquals(PersonalDataFailureDisposition.BACKUP_INTEGRITY_FAILURE, failure.disposition)
                    assertFalse(storage.exists())
                } finally { directory.toFile().walkBottomUp().forEach { it.delete() } }
            }
        }
    }

    @Test fun `truncated and modified backups fail closed`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("backup-corrupt"))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val variants = listOf(
                backup.protectedBytes().copyOf(12),
                backup.protectedBytes().also { it[it.lastIndex] = (it.last().toInt() xor 4).toByte() },
            )
            variants.forEachIndexed { index, bytes ->
                val directory = Files.createTempDirectory("ct-v2-14-corrupt-$index-")
                val storage = NioAtomicProtectedArtifactStorage(directory.resolve("restore.ctpd"))
                try {
                    assertTrue(runCatching { ProtectedBackupArtifact.fromUserCustody(bytes) }.isFailure)
                    assertFalse(storage.exists())
                } finally { directory.toFile().walkBottomUp().forEach { it.delete() } }
            }
        }
    }

    @Test fun `restore into non-empty target is prohibited`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("backup-nonempty"))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val failure = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
                backup, key, harness.storage, harness.keyProvider, harness.clock,
            ).exceptionOrNull() as PersonalDataPersistenceException
            assertEquals(PersonalDataFailureDisposition.NON_EMPTY_RESTORE_TARGET, failure.disposition)
        }
    }

    @Test fun `backup after deletion cannot restore deleted source`() = PersonalDataHarness().use { harness ->
        val phrase = "Synthetic erased backup marker copper-rain-606"
        val source = harness.source("backup-delete", phrase)
        harness.admit(source)
        harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.DeleteSource(source.stableSourceId), AdmissionActor.USER,
            AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
        ))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val plaintext = AuthenticatedProtection.unprotect(
                backup.encryptedPayload(), key.secretKey(), ProtectedArtifactPurpose.LOCAL_BACKUP,
                ProtectedPersonalDataStoreFactory.BACKUP_KEY_ALIAS,
            )
            assertFalse(String(plaintext, StandardCharsets.ISO_8859_1).contains(phrase))
        }
    }

    @Test fun `independently held backup is not falsely erased by reset`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("external-backup"))
        RecoveryKey.generate().use { key ->
            val backup = harness.store.createProtectedBackup(key)
            val bytes = backup.protectedBytes()
            val reset = harness.store.reset()
            assertTrue(reset.externallyHeldBackupsUnaffected)
            assertTrue(bytes.isNotEmpty())
        }
    }
}
