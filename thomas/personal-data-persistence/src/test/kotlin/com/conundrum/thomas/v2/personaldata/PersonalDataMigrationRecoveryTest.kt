package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.CanonicalLongitudinalEncoding
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAggregateState
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalDataMigrationRecoveryTest {
    @Test fun `supported prior schema migrates without changing logical identity`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("migrate"))
        val before = harness.store.reader.canonicalLogicalStateDigest()
        val current = harness.readDocument()
        val legacy = PersonalDataDocumentV1(
            current.checkpoint,
            current.activeLedger,
            current.receipts,
            current.projection,
            current.projectionDigest,
        )
        harness.store.close()
        harness.writePlaintext(PersonalDataDocumentCodec.encodeLegacyV1(legacy))
        val opened = ProtectedPersonalDataStoreFactory.open(harness.storage, harness.keyProvider, harness.clock)
            as PersonalDataOpenResult.Opened
        assertEquals(PersonalDataOpenDisposition.OPENED_AFTER_SCHEMA_MIGRATION, opened.disposition)
        assertEquals(before, opened.store.reader.canonicalLogicalStateDigest())
        opened.store.close()
    }

    @Test fun `interrupted migration leaves prior schema recoverable on retry`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("migration-interrupt"))
        val current = harness.readDocument()
        val legacy = PersonalDataDocumentV1(
            current.checkpoint,
            current.activeLedger,
            current.receipts,
            current.projection,
            current.projectionDigest,
        )
        harness.store.close()
        harness.writePlaintext(PersonalDataDocumentCodec.encodeLegacyV1(legacy))
        val interrupted = ProtectedPersonalDataStoreFactory.open(
            harness.storage,
            harness.keyProvider,
            harness.clock,
            faultInjector = PersistenceFaultInjector { point ->
                if (point == PersistenceFaultPoint.BEFORE_MIGRATION_COMMIT) error("synthetic interruption")
            },
        )
        assertTrue(interrupted is PersonalDataOpenResult.Unavailable)
        val retried = ProtectedPersonalDataStoreFactory.open(harness.storage, harness.keyProvider, harness.clock)
        assertTrue(retried is PersonalDataOpenResult.Opened)
        assertEquals(
            PersonalDataOpenDisposition.OPENED_AFTER_SCHEMA_MIGRATION,
            (retried as PersonalDataOpenResult.Opened).disposition,
        )
        retried.store.close()
    }

    @Test fun `damaged projection is rebuilt from trusted ledger`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("projection-rebuild"))
        val before = harness.store.reader.canonicalLogicalStateDigest()
        val current = harness.readDocument()
        harness.store.close()
        harness.writePlaintext(PersonalDataDocumentCodec.encode(current.copy(
            projection = LongitudinalAggregateState(),
            projectionDigest = CanonicalLongitudinalEncoding.stateDigest(LongitudinalAggregateState()),
        )))
        val opened = ProtectedPersonalDataStoreFactory.open(harness.storage, harness.keyProvider, harness.clock)
            as PersonalDataOpenResult.Opened
        assertEquals(PersonalDataOpenDisposition.OPENED_AFTER_PROJECTION_REBUILD, opened.disposition)
        assertEquals(before, opened.store.reader.canonicalLogicalStateDigest())
        opened.store.close()
    }

    @Test fun `damaged evidence ledger fails closed instead of speculative repair`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("ledger-corrupt"))
        val current = harness.readDocument()
        val damaged = current.copy(activeLedger = current.activeLedger.mapIndexed { index, event ->
            if (index == 0) event.copy(eventDigest = PersonalDataDocumentCodec.ZERO_DIGEST) else event
        })
        harness.store.close()
        harness.writePlaintext(PersonalDataDocumentCodec.encode(damaged))
        val result = ProtectedPersonalDataStoreFactory.open(harness.storage, harness.keyProvider, harness.clock)
        assertTrue(result is PersonalDataOpenResult.Unavailable)
        assertEquals(
            PersonalDataFailureDisposition.LEDGER_INTEGRITY_FAILURE,
            (result as PersonalDataOpenResult.Unavailable).disposition,
        )
    }

    @Test fun `future schema and malformed document fail closed`() = PersonalDataHarness().use { harness ->
        harness.store.close()
        val future = ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use {
                it.write("CTV2DOC".toByteArray(StandardCharsets.US_ASCII))
                it.writeInt(99)
                it.writeInt(1)
                it.writeByte(0)
            }
            bytes.toByteArray()
        }
        harness.writePlaintext(future)
        val futureResult = ProtectedPersonalDataStoreFactory.open(harness.storage, harness.keyProvider, harness.clock)
            as PersonalDataOpenResult.Unavailable
        assertEquals(PersonalDataFailureDisposition.UNSUPPORTED_SCHEMA, futureResult.disposition)
        harness.writePlaintext(byteArrayOf(1, 2, 3))
        val malformed = ProtectedPersonalDataStoreFactory.open(harness.storage, harness.keyProvider, harness.clock)
            as PersonalDataOpenResult.Unavailable
        assertEquals(PersonalDataFailureDisposition.MALFORMED_STORE, malformed.disposition)
    }

    @Test fun `write interruption before commit leaves prior state authoritative`() = PersonalDataHarness().use { harness ->
        val before = harness.store.reader.canonicalLogicalStateDigest()
        harness.store.close()
        val opened = ProtectedPersonalDataStoreFactory.open(
            harness.storage,
            harness.keyProvider,
            harness.clock,
            faultInjector = PersistenceFaultInjector { point ->
                if (point == PersistenceFaultPoint.AFTER_PROTECTION_BEFORE_ATOMIC_WRITE) error("synthetic interruption")
            },
        ) as PersonalDataOpenResult.Opened
        harness.store = opened.store
        val result = harness.admit(harness.source("before-commit"))
        assertEquals(AdmissionDisposition.FAILED_WITHOUT_COMMIT, result.disposition)
        assertEquals(before, harness.store.reader.canonicalLogicalStateDigest())
        assertFalse(harness.store.reader.snapshot().sources.any { it.stableSourceId.value.endsWith("before-commit") })
    }

    @Test fun `failure after atomic write recovers committed receipt idempotently`() = PersonalDataHarness().use { harness ->
        harness.store.close()
        val opened = ProtectedPersonalDataStoreFactory.open(
            harness.storage,
            harness.keyProvider,
            harness.clock,
            faultInjector = PersistenceFaultInjector { point ->
                if (point == PersistenceFaultPoint.AFTER_ATOMIC_WRITE) error("synthetic process death")
            },
        ) as PersonalDataOpenResult.Opened
        harness.store = opened.store
        val result = harness.admit(harness.source("after-commit"), "after-commit-key")
        assertEquals(AdmissionDisposition.ACCEPTED, result.disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
    }
}

private fun PersonalDataHarness.readDocument(): PersonalDataDocumentV2 {
    val plaintext = AuthenticatedProtection.unprotect(
        requireNotNull(storage.read()),
        keyProvider.existing(),
        ProtectedArtifactPurpose.PRIMARY_STORE,
        keyProvider.descriptor.alias,
    )
    return PersonalDataDocumentCodec.decodeAndVerify(plaintext).document
}

private fun PersonalDataHarness.writePlaintext(plaintext: ByteArray) {
    val protected = AuthenticatedProtection.protect(
        plaintext,
        keyProvider.existing(),
        ProtectedArtifactPurpose.PRIMARY_STORE,
        keyProvider.descriptor.alias,
        SecureRandom(),
    )
    storage.writeAtomically(protected)
}
