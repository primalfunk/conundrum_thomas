package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import java.nio.charset.StandardCharsets
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectedPersonalDataStoreCoreTest {
    @Test fun `empty protected store is initialized as authenticated ciphertext`() = PersonalDataHarness().use { harness ->
        assertEquals(0, harness.store.reader.currentStoreRevision())
        val bytes = requireNotNull(harness.storage.read())
        assertTrue(bytes.size > 32)
        assertTrue(String(bytes, StandardCharsets.ISO_8859_1).startsWith("CTV2PD14"))
    }

    @Test fun `committed source plaintext never appears in primary artifact`() = PersonalDataHarness().use { harness ->
        val secret = "Synthetic private phrase quartz-cobalt-417"
        assertEquals(AdmissionDisposition.ACCEPTED, harness.admit(harness.source("ciphertext", secret)).disposition)
        val artifact = String(requireNotNull(harness.storage.read()), StandardCharsets.ISO_8859_1)
        assertFalse(artifact.contains(secret))
        assertEquals(secret, (harness.store.reader.source(harness.source("ciphertext").stableSourceId)!!.originalContent as OriginalSourceContent.Inline).exactContent)
    }

    @Test fun `restart preserves logical state and provenance`() = PersonalDataHarness().use { harness ->
        val source = harness.source("restart", mode = AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE,
            eventTime = EventTime.ApproximateYear(java.time.Year.of(1998)))
        assertEquals(AdmissionDisposition.ACCEPTED, harness.admit(source).disposition)
        val before = harness.store.reader.canonicalLogicalStateDigest()
        val result = harness.reopen()
        assertTrue(result is PersonalDataOpenResult.Opened)
        assertEquals(before, harness.store.reader.canonicalLogicalStateDigest())
        val restored = harness.store.reader.source(source.stableSourceId)!!
        assertEquals(AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, restored.provenance.acquisitionMode)
        assertEquals(source.eventTime, restored.eventTime)
    }

    @Test fun `idempotent retry never duplicates a source`() = PersonalDataHarness().use { harness ->
        val source = harness.source("retry")
        val request = harness.request(LongitudinalWriteOperation.AdmitSource(source), AdmissionActor.USER, AdmissionOrigin.JOURNAL, "retry-key")
        assertEquals(AdmissionDisposition.ACCEPTED, harness.store.admission.submit(request).disposition)
        assertEquals(AdmissionDisposition.IDEMPOTENT_REPLAY, harness.store.admission.submit(request).disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
    }

    @Test fun `changed payload under one idempotency key fails closed`() = PersonalDataHarness().use { harness ->
        val first = harness.request(LongitudinalWriteOperation.AdmitSource(harness.source("conflict-a")), AdmissionActor.USER, AdmissionOrigin.JOURNAL, "conflict-key")
        assertEquals(AdmissionDisposition.ACCEPTED, harness.store.admission.submit(first).disposition)
        val second = first.copy(operation = LongitudinalWriteOperation.AdmitSource(harness.source("conflict-b")))
        assertEquals(AdmissionDisposition.REJECTED_IDEMPOTENCY_CONFLICT, harness.store.admission.submit(second).disposition)
    }

    @Test fun `wrong or lost key makes data unavailable without reset`() = PersonalDataHarness().use { harness ->
        val source = harness.source("wrong-key")
        harness.admit(source)
        harness.store.close()
        val wrong = ProtectedPersonalDataStoreFactory.open(
            harness.storage,
            MemoryKeyProvider(ByteArray(32) { (it + 55).toByte() }),
            harness.clock,
        )
        assertTrue(wrong is PersonalDataOpenResult.Unavailable)
        assertEquals(
            PersonalDataFailureDisposition.AUTHENTICATION_OR_INTEGRITY_FAILURE,
            (wrong as PersonalDataOpenResult.Unavailable).disposition,
        )
        assertTrue(harness.storage.exists())
    }

    @Test fun `modified ciphertext is rejected before state authority`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("tamper"))
        harness.store.close()
        val bytes = requireNotNull(harness.storage.read())
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        harness.storage.writeAtomically(bytes)
        val result = ProtectedPersonalDataStoreFactory.open(harness.storage, harness.keyProvider, harness.clock)
        assertTrue(result is PersonalDataOpenResult.Unavailable)
        assertEquals(PersonalDataFailureDisposition.AUTHENTICATION_OR_INTEGRITY_FAILURE,
            (result as PersonalDataOpenResult.Unavailable).disposition)
    }

    @Test fun `source revision survives restart with original lineage`() = PersonalDataHarness().use { harness ->
        val source = harness.source("revision", "Original synthetic wording")
        harness.admit(source)
        val append = LongitudinalWriteOperation.AppendSourceRevision(
            source.stableSourceId,
            source.revisionId,
            SourceRecordId.parse("personal-source-revision-rev-2"),
            OriginalSourceContent.Inline("Corrected synthetic wording"),
            ReportTime(Instant.parse("2039-02-01T00:00:00Z")),
        )
        val result = harness.store.admission.submit(harness.request(append, AdmissionActor.USER, AdmissionOrigin.JOURNAL))
        assertEquals(AdmissionDisposition.ACCEPTED, result.disposition)
        harness.reopen()
        val history = harness.store.reader.sourceRevisionHistory(source.stableSourceId)
        assertEquals(2, history.size)
        assertEquals(source.revisionId, history.last().provenance.previousRevisionId)
        assertEquals("Original synthetic wording", (history.first().originalContent as OriginalSourceContent.Inline).exactContent)
    }

    @Test fun `private source remains present but ineligible after restart`() = PersonalDataHarness().use { harness ->
        val source = harness.source("private", privacy = SourcePrivacy.PRIVATE)
        harness.admit(source)
        harness.reopen()
        assertTrue(harness.store.reader.source(source.stableSourceId) != null)
        val lifecycle = harness.store.reader.lifecycle(
            com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef(
                com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType.SOURCE_REVISION,
                source.revisionId.value,
            ),
        )
        assertFalse(lifecycle!!.eligibleForOrdinaryUse)
    }

    @Test fun `protected backups use fresh nonces while logical state remains stable`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("logical"))
        val digest = harness.store.reader.canonicalLogicalStateDigest()
        val recoveryKey = RecoveryKey.fromUserCustody(ByteArray(32) { (it + 91).toByte() })
        val first = harness.store.createProtectedBackup(recoveryKey)
        val second = harness.store.createProtectedBackup(recoveryKey)
        assertFalse(first.protectedBytes().contentEquals(second.protectedBytes()))
        assertEquals(first.sourceStoreRevision, second.sourceStoreRevision)
        assertEquals(digest, harness.store.reader.canonicalLogicalStateDigest())
        recoveryKey.close()
    }

    @Test fun `protected classification does not accept a synthetic-classified request`() = PersonalDataHarness().use { harness ->
        val request = harness.request(LongitudinalWriteOperation.AdmitSource(harness.source("classification")), AdmissionActor.USER, AdmissionOrigin.JOURNAL)
            .copy(classification = com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY)
        assertEquals(AdmissionDisposition.REJECTED_AUTHORITY, harness.store.admission.submit(request).disposition)
    }
}
