package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.admission.assertionRef
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStoreFactory
import com.conundrum.thomas.v2.longitudinal.store.QualificationStoreLocation
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalStoreSchema
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.admitAssertion
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.admitSource
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertAccepted
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertion
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.path
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.sourceDraft
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.nio.file.Files
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalStoreInvariantTest {
    @Test fun `schema has a fixed deterministic fingerprint`() {
        assertEquals("cec67480788273b329cf0c6b1e916a1511dd90fc8886c434e2ad2fccc8f15db0", LongitudinalStoreSchema.fingerprint)
    }

    @Test fun `store clock before report time is rejected as temporal dishonesty`() {
        val database = path("backdated-store-clock")
        Files.createDirectories(database.parent); Files.deleteIfExists(database)
        QualificationLongitudinalStoreFactory.open(QualificationStoreLocation.file(database), StoreClock { Instant.parse("2030-01-01T00:00:00Z") }).use { store ->
            SyntheticLongitudinalStoreHarness(path("backdated-request")).use { helper ->
                val request = helper.request(LongitudinalWriteOperation.AdmitSource(sourceDraft("backdated")), AdmissionActor.USER, AdmissionOrigin.JOURNAL)
                assertEquals(AdmissionDisposition.REJECTED_TEMPORAL_DISHONESTY, store.admission.submit(request).disposition)
            }
            assertEquals(0, store.reader.currentStoreRevision())
        }
        Files.deleteIfExists(database)
    }

    @Test fun `in memory qualification store uses the same governed controller`() {
        QualificationLongitudinalStoreFactory.open(
            QualificationStoreLocation.inMemory("ct-v2-07-memory"),
            StoreClock { Instant.parse("2040-01-01T00:00:00Z") },
        ).use { store ->
            SyntheticLongitudinalStoreHarness(path("memory-request")).use { helper ->
                val request = helper.request(LongitudinalWriteOperation.AdmitSource(sourceDraft("memory")), AdmissionActor.USER, AdmissionOrigin.JOURNAL)
                assertEquals(AdmissionDisposition.ACCEPTED, store.admission.submit(request).disposition)
            }
            assertEquals(1, store.reader.currentStoreRevision())
        }
    }

    @Test fun `qualification and protected production classifications remain explicit`() {
        assertEquals(
            listOf(StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY, StoreDataClassification.PROTECTED_PERSONAL_DATA),
            StoreDataClassification.entries,
        )
    }

    @Test fun `lifecycle history records active then private without deleting assertion`() {
        SyntheticLongitudinalStoreHarness(path("lifecycle-history")).use { harness ->
            val source = sourceDraft("lifecycle-history")
            assertAccepted(admitSource(harness, source))
            val evidence = assertion("lifecycle-history", source.revisionId)
            assertAccepted(admitAssertion(harness, evidence))
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ChangePrivacy(source.stableSourceId, SourcePrivacy.PRIVATE), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            val history = harness.store.reader.lifecycleHistory(assertionRef(evidence.id))
            assertEquals(listOf(LongitudinalLifecycleStatus.ACTIVE, LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE), history.map { it.status })
            assertFalse(history.last().eligibleForOrdinaryUse)
            assertEquals(evidence, harness.store.reader.assertion(evidence.id))
        }
    }

    @Test fun `inconsistent simultaneous identity resolutions reject complete bundle`() {
        SyntheticLongitudinalStoreHarness(path("identity-inconsistent")).use { harness ->
            val source = sourceDraft("identity-inconsistent")
            assertAccepted(admitSource(harness, source))
            val evidence = listOf("a", "b", "c").map { assertion("identity-inconsistent-$it", source.revisionId) }
            val people = evidence.mapIndexed { index, assertion -> Person(LifeEntityId.parse("person-identity-${index + 1}"), "Synthetic person ${index + 1}", setOf(assertion.id)) }
            val links = listOf(
                EntityIdentityLink(IdentityLinkId.parse("identity-link-ab"), people[0].id, people[1].id, EntityIdentityStatus.ESTABLISHED_SAME_ENTITY, setOf(evidence[0].id), "Synthetic established link"),
                EntityIdentityLink(IdentityLinkId.parse("identity-link-bc"), people[1].id, people[2].id, EntityIdentityStatus.ESTABLISHED_SAME_ENTITY, setOf(evidence[1].id), "Synthetic established link"),
                EntityIdentityLink(IdentityLinkId.parse("identity-link-ac"), people[0].id, people[2].id, EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY, setOf(evidence[2].id), "Synthetic conflicting link"),
            )
            val result = harness.submitBundle(EvidenceBundle(assertions = evidence, entities = people, identityLinks = links))
            assertEquals(AdmissionDisposition.REJECTED_VALIDATION, result.disposition)
            assertTrue(harness.store.reader.snapshot().entities.isEmpty())
        }
    }

    @Test fun `qualification cleanup removes its file backed database`() {
        val database = path("cleanup-proof")
        SyntheticLongitudinalStoreHarness(database).use { harness -> assertAccepted(admitSource(harness, sourceDraft("cleanup-proof"))) }
        assertFalse(Files.exists(database))
        assertFalse(Files.exists(database.resolveSibling(database.fileName.toString() + "-journal")))
    }
}
