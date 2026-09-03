package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.store.InconsistentLongitudinalStoreException
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalStoreSchema
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStoreFactory
import com.conundrum.thomas.v2.longitudinal.store.QualificationStoreLocation
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import com.conundrum.thomas.v2.longitudinal.store.StoreFaultInjector
import com.conundrum.thomas.v2.longitudinal.store.StoreFaultPoint
import com.conundrum.thomas.v2.longitudinal.store.UnsupportedStoreSchemaException
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.admitAssertion
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.admitSource
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertAccepted
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.path
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.sourceDraft
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertion
import com.conundrum.thomas.v2.qualification.longitudinalstore.IncrementingFixtureClock
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.sql.DriverManager
import java.time.Instant
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GovernedLongitudinalStoreCoreTest {
    @Test fun `empty store is schema version one and synthetic only`() {
        SyntheticLongitudinalStoreHarness(path("empty-store")).use { harness ->
            assertEquals(1, LongitudinalStoreSchema.VERSION)
            assertEquals(64, LongitudinalStoreSchema.fingerprint.length)
            assertEquals(0, harness.store.reader.currentStoreRevision())
            assertEquals("SYNTHETIC_QUALIFICATION_ONLY", harness.store.reader.classification.name)
        }
    }

    @Test fun `file backed store closes and reopens without loss`() {
        SyntheticLongitudinalStoreHarness(path("close-reopen")).use { harness ->
            assertAccepted(admitSource(harness, sourceDraft("close-reopen")))
            val digest = harness.store.reader.canonicalLogicalStateDigest()
            harness.reopen()
            assertEquals(1, harness.store.reader.currentStoreRevision())
            assertEquals(digest, harness.store.reader.canonicalLogicalStateDigest())
            assertNotNull(harness.store.reader.source(SourceIdentityId.parse("source-close-reopen")))
        }
    }

    @Test fun `ledger replay into empty store reproduces canonical digest`() {
        SyntheticLongitudinalStoreHarness(path("replay-source")).use { harness ->
            val draft = sourceDraft("replay")
            assertAccepted(admitSource(harness, draft))
            assertAccepted(admitAssertion(harness, assertion("replay", draft.revisionId)))
            val replayPath = path("replay-target")
            Files.deleteIfExists(replayPath)
            harness.store.replayIntoEmpty(QualificationStoreLocation.file(replayPath)).use { target ->
                assertEquals(harness.store.reader.canonicalLogicalStateDigest(), target.reader.canonicalLogicalStateDigest())
                assertEquals(harness.store.reader.currentStoreRevision(), target.reader.currentStoreRevision())
            }
            Files.deleteIfExists(replayPath)
        }
    }

    @Test fun `identical idempotent retry returns original receipt without duplicate`() {
        SyntheticLongitudinalStoreHarness(path("idempotent-replay")).use { harness ->
            val request = harness.request(
                LongitudinalWriteOperation.AdmitSource(sourceDraft("idempotent")), AdmissionActor.USER, AdmissionOrigin.JOURNAL,
                idempotency = "idempotency-fixed",
            )
            val first = harness.store.admission.submit(request)
            val second = harness.store.admission.submit(request.copy(requestId = com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId.parse("request-retry")))
            assertEquals(AdmissionDisposition.ACCEPTED, first.disposition)
            assertEquals(AdmissionDisposition.IDEMPOTENT_REPLAY, second.disposition)
            assertEquals(first.receipt, second.receipt)
            assertEquals(1, harness.store.reader.currentStoreRevision())
        }
    }

    @Test fun `changed request under same idempotency key fails closed`() {
        SyntheticLongitudinalStoreHarness(path("idempotency-conflict")).use { harness ->
            val first = harness.request(LongitudinalWriteOperation.AdmitSource(sourceDraft("idem-a")), AdmissionActor.USER, AdmissionOrigin.JOURNAL, idempotency = "same-key")
            assertAccepted(harness.store.admission.submit(first))
            val changed = harness.request(LongitudinalWriteOperation.AdmitSource(sourceDraft("idem-b")), AdmissionActor.USER, AdmissionOrigin.JOURNAL, idempotency = "same-key")
            val result = harness.store.admission.submit(changed)
            assertEquals(AdmissionDisposition.REJECTED_IDEMPOTENCY_CONFLICT, result.disposition)
            assertEquals(1, harness.store.reader.currentStoreRevision())
        }
    }

    @Test fun `stale expected revision rejects without domain mutation`() {
        SyntheticLongitudinalStoreHarness(path("stale-revision")).use { harness ->
            assertAccepted(admitSource(harness, sourceDraft("stale-one")))
            val request = harness.request(LongitudinalWriteOperation.AdmitSource(sourceDraft("stale-two")), AdmissionActor.USER, AdmissionOrigin.JOURNAL, expectedRevision = 0)
            val before = harness.store.reader.canonicalLogicalStateDigest()
            val result = harness.store.admission.submit(request)
            assertEquals(AdmissionDisposition.REJECTED_STALE_REVISION, result.disposition)
            assertEquals(before, harness.store.reader.canonicalLogicalStateDigest())
        }
    }

    @Test fun `missing source rejects entire structured bundle`() {
        SyntheticLongitudinalStoreHarness(path("missing-source")).use { harness ->
            val missing = assertion("missing-source", com.conundrum.thomas.v2.longitudinal.SourceRecordId.parse("source-does-not-exist"))
            val result = admitAssertion(harness, missing)
            assertEquals(AdmissionDisposition.REJECTED_MISSING_REFERENCE, result.disposition)
            assertEquals(0, harness.store.reader.currentStoreRevision())
            assertNull(harness.store.reader.assertion(missing.id))
        }
    }

    @Test fun `duplicate immutable assertion ID rejects bundle atomically`() {
        SyntheticLongitudinalStoreHarness(path("duplicate-assertion")).use { harness ->
            val draft = sourceDraft("duplicate")
            assertAccepted(admitSource(harness, draft))
            val evidence = assertion("duplicate", draft.revisionId)
            assertAccepted(admitAssertion(harness, evidence))
            val duplicate = admitAssertion(harness, evidence)
            assertEquals(AdmissionDisposition.REJECTED_VALIDATION, duplicate.disposition)
            assertEquals(2, harness.store.reader.currentStoreRevision())
            assertEquals(1, harness.store.reader.snapshot().assertions.size)
        }
    }

    @Test fun `unsupported admission policy version fails before a write`() {
        SyntheticLongitudinalStoreHarness(path("unsupported-policy")).use { harness ->
            val request = harness.request(
                LongitudinalWriteOperation.AdmitSource(sourceDraft("unsupported-policy")), AdmissionActor.USER, AdmissionOrigin.JOURNAL,
                policyVersion = AdmissionPolicyVersion.unsupportedForTest("ct-v2-99.invalid"),
            )
            assertEquals(AdmissionDisposition.REJECTED_AUTHORITY, harness.store.admission.submit(request).disposition)
            assertEquals(0, harness.store.reader.currentStoreRevision())
        }
    }

    @Test fun `store assigns deterministic record time from injected clock`() {
        val fixed = Instant.parse("2042-03-04T05:06:07Z")
        val database = path("fixed-clock")
        Files.createDirectories(database.parent)
        Files.deleteIfExists(database)
        QualificationLongitudinalStoreFactory.open(QualificationStoreLocation.file(database), StoreClock { fixed }).use { store ->
            val harness = SyntheticLongitudinalStoreHarness(path("fixed-clock-request"))
            val request = harness.request(LongitudinalWriteOperation.AdmitSource(sourceDraft("fixed-clock")), AdmissionActor.USER, AdmissionOrigin.JOURNAL)
            harness.close()
            val result = store.admission.submit(request)
            assertEquals(fixed, result.receipt!!.recordTime)
            assertEquals(fixed, store.reader.source(SourceIdentityId.parse("source-fixed-clock"))!!.recordTime.value)
        }
        Files.deleteIfExists(database)
    }

    @Test fun `caller payload has no record time field`() {
        val fieldNames = com.conundrum.thomas.v2.longitudinal.admission.SourceDraft::class.java.declaredFields.map { it.name }
        assertFalse(fieldNames.any { it.equals("recordTime", true) })
        assertFalse(com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest::class.java.declaredFields.any { it.name.equals("recordTime", true) })
    }

    @Test fun `fault after ledger insert rolls back ledger and projection`() {
        val database = path("fault-rollback")
        Files.createDirectories(database.parent); Files.deleteIfExists(database)
        var fired = false
        val fault = StoreFaultInjector { point -> if (!fired && point == StoreFaultPoint.AFTER_LEDGER_INSERT) { fired = true; error("synthetic injected failure") } }
        QualificationLongitudinalStoreFactory.open(QualificationStoreLocation.file(database), IncrementingFixtureClock(Instant.parse("2040-01-01T00:00:00Z")), fault).use { store ->
            SyntheticLongitudinalStoreHarness(path("fault-request")).use { requestHarness ->
                val request = requestHarness.request(LongitudinalWriteOperation.AdmitSource(sourceDraft("fault")), AdmissionActor.USER, AdmissionOrigin.JOURNAL)
                assertEquals(AdmissionDisposition.FAILED_WITHOUT_COMMIT, store.admission.submit(request).disposition)
            }
            assertEquals(0, store.reader.currentStoreRevision())
            assertTrue(store.reader.snapshot().sources.isEmpty())
        }
        Files.deleteIfExists(database)
    }

    @Test fun `unsupported persistent schema version fails closed`() {
        val database = path("unsupported-schema")
        Files.createDirectories(database.parent); Files.deleteIfExists(database)
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:$database").use { it.createStatement().execute("PRAGMA user_version=99") }
        assertThrows(UnsupportedStoreSchemaException::class.java) {
            QualificationLongitudinalStoreFactory.open(QualificationStoreLocation.file(database), StoreClock { Instant.EPOCH })
        }
        Files.deleteIfExists(database)
    }

    @Test fun `corrupt projection digest is not silently repaired`() {
        val database = path("corrupt-projection")
        val harness = SyntheticLongitudinalStoreHarness(database)
        assertAccepted(admitSource(harness, sourceDraft("corrupt")))
        harness.closePreservingFile()
        DriverManager.getConnection("jdbc:sqlite:$database").use { connection ->
            connection.createStatement().executeUpdate("UPDATE current_state_projection SET canonical_digest='${"0".repeat(64)}'")
        }
        assertThrows(InconsistentLongitudinalStoreException::class.java) {
            QualificationLongitudinalStoreFactory.open(QualificationStoreLocation.file(database), StoreClock { Instant.EPOCH })
        }
        Files.deleteIfExists(database)
    }

    @Test fun `rejected content is absent from persisted audit text`() {
        val database = path("redacted-rejection")
        val marker = "SYNTHETIC-REJECTED-CONTENT-MUST-NOT-PERSIST"
        val harness = SyntheticLongitudinalStoreHarness(database)
        try {
            val evidence = assertion("redacted", com.conundrum.thomas.v2.longitudinal.SourceRecordId.parse("source-missing")).copy(
                value = com.conundrum.thomas.v2.longitudinal.AssertionValue.Text(marker),
            )
            assertEquals(AdmissionDisposition.REJECTED_MISSING_REFERENCE, admitAssertion(harness, evidence).disposition)
            assertFalse(harness.store.reader.redactedAdmissionHistory().joinToString().contains(marker))
            harness.closePreservingFile()
        } catch (failure: Throwable) {
            harness.close()
            throw failure
        }
        val bytes = Files.readAllBytes(database)
        assertFalse(String(bytes, StandardCharsets.ISO_8859_1).contains(marker))
        Files.deleteIfExists(database)
    }

    @Test fun `as of revision reads reconstruct prior state exactly`() {
        SyntheticLongitudinalStoreHarness(path("as-of")).use { harness ->
            val draft = sourceDraft("as-of", eventTime = EventTime.Unknown("Synthetic unknown"))
            assertAccepted(admitSource(harness, draft))
            assertAccepted(admitAssertion(harness, assertion("as-of", draft.revisionId)))
            assertEquals(1, harness.store.reader.snapshot(1).sources.size)
            assertTrue(harness.store.reader.snapshot(1).assertions.isEmpty())
            assertNotNull(harness.store.reader.assertion(AssertionId.parse("assertion-as-of"), 2))
            assertNotEquals(harness.store.reader.canonicalLogicalStateDigest(1), harness.store.reader.canonicalLogicalStateDigest(2))
        }
    }
}
