package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.HypothesisDraft
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDeletionScope
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import java.nio.charset.StandardCharsets
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalDataLifecycleTest {
    @Test fun `Journal Biographer and Therapy sources share governed deletion semantics`() {
        listOf(
            AcquisitionMode.JOURNAL,
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE,
            AcquisitionMode.THERAPIST_CONVERSATION,
        ).forEachIndexed { index, mode ->
            PersonalDataHarness().use { harness ->
                val source = harness.source("delete-mode-${index + 1}", mode = mode)
                harness.admit(source)
                val result = harness.store.admission.submit(harness.request(
                    LongitudinalWriteOperation.DeleteSource(source.stableSourceId),
                    AdmissionActor.USER,
                    AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
                ))
                assertEquals(AdmissionDisposition.ACCEPTED, result.disposition)
                harness.reopen()
                assertNull(harness.store.reader.source(source.stableSourceId))
            }
        }
    }

    @Test fun `deleting revised source purges the entire wording history`() = PersonalDataHarness().use { harness ->
        val firstPhrase = "Synthetic first revision indigo-415"
        val secondPhrase = "Synthetic second revision amber-926"
        val source = harness.source("delete-revisions", firstPhrase)
        harness.admit(source)
        val append = LongitudinalWriteOperation.AppendSourceRevision(
            source.stableSourceId,
            source.revisionId,
            SourceRecordId.parse("personal-source-delete-revisions-rev-2"),
            OriginalSourceContent.Inline(secondPhrase),
            ReportTime(Instant.parse("2039-02-01T00:00:00Z")),
        )
        assertEquals(AdmissionDisposition.ACCEPTED, harness.store.admission.submit(
            harness.request(append, AdmissionActor.USER, AdmissionOrigin.JOURNAL),
        ).disposition)
        assertEquals(2, harness.store.reader.sourceRevisionHistory(source.stableSourceId).size)
        assertEquals(AdmissionDisposition.ACCEPTED, harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.DeleteSource(source.stableSourceId),
            AdmissionActor.USER,
            AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
        )).disposition)
        assertTrue(harness.store.reader.sourceRevisionHistory(source.stableSourceId).isEmpty())
        val plaintext = AuthenticatedProtection.unprotect(
            requireNotNull(harness.storage.read()),
            harness.keyProvider.existing(),
            ProtectedArtifactPurpose.PRIMARY_STORE,
            harness.keyProvider.descriptor.alias,
        )
        val serialized = String(plaintext, StandardCharsets.ISO_8859_1)
        assertFalse(serialized.contains(firstPhrase))
        assertFalse(serialized.contains(secondPhrase))
    }

    @Test fun `selective source deletion purges source and direct evidence`() = PersonalDataHarness().use { harness ->
        val phrase = "Synthetic deletion marker violet-ember-902"
        val source = harness.source("delete", phrase)
        val assertion = harness.assertion("delete", source.revisionId)
        harness.admit(source)
        harness.admit(assertion)
        val result = harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.DeleteSource(source.stableSourceId),
            AdmissionActor.USER,
            AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
        ))
        assertEquals(AdmissionDisposition.ACCEPTED, result.disposition)
        assertNull(harness.store.reader.source(source.stableSourceId))
        assertNull(harness.store.reader.assertion(assertion.id))
        assertFalse(String(requireNotNull(harness.storage.read()), StandardCharsets.ISO_8859_1).contains(phrase))
        harness.reopen()
        assertNull(harness.store.reader.source(source.stableSourceId))
    }

    @Test fun `deletion invalidates dependent Thomas hypothesis`() = PersonalDataHarness().use { harness ->
        val source = harness.source("dependency")
        val assertion = harness.assertion("dependency", source.revisionId)
        harness.admit(source)
        harness.admit(assertion)
        val hypothesisId = HypothesisId.parse("personal-hypothesis-dependency")
        val dependency = HypothesisDependency(
            EvidenceRelationId.parse("personal-relation-dependency"),
            hypothesisId,
            ClaimReference.Assertion(assertion.id),
            com.conundrum.thomas.v2.longitudinal.DependencyRole.SUPPORTS,
            "Synthetic dependency for deletion qualification",
        )
        val hypothesis = HypothesisDraft(
            hypothesisId,
            AssertionSubject.User,
            AssertionPredicate(PersonalConceptId.parse("personal.hypothesis"), PredicateSemantics.OTHER),
            AssertionValue.Text("Tentative synthetic interpretation"),
            HypothesisStatus.TENTATIVE,
            "Synthetic tentative interpretation with governed support",
        )
        val admitted = harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.AdmitEvidenceBundle(EvidenceBundle(
                hypothesisDrafts = listOf(hypothesis), hypothesisDependencies = listOf(dependency),
            )),
            AdmissionActor.THOMAS,
            AdmissionOrigin.THOMAS_DERIVATION,
        ))
        assertEquals(AdmissionDisposition.ACCEPTED, admitted.disposition)
        assertEquals(1, harness.store.reader.snapshot().hypotheses.size)
        val deleted = harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.DeleteSource(source.stableSourceId),
            AdmissionActor.USER,
            AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
        ))
        assertEquals(AdmissionDisposition.ACCEPTED, deleted.disposition)
        assertTrue(harness.store.reader.snapshot().hypotheses.isEmpty())
        assertTrue(harness.store.reader.snapshot().hypothesisDependencies.isEmpty())
    }

    @Test fun `deletion tombstone prevents silent source identity reuse`() = PersonalDataHarness().use { harness ->
        val source = harness.source("tombstone")
        harness.admit(source)
        harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.DeleteSource(source.stableSourceId), AdmissionActor.USER,
            AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
        ))
        val replay = harness.admit(source.copy(revisionId = com.conundrum.thomas.v2.longitudinal.SourceRecordId.parse("personal-source-tombstone-rev-2")))
        assertEquals(AdmissionDisposition.REJECTED_VALIDATION, replay.disposition)
        val lifecycle = harness.store.reader.lifecycle(LongitudinalObjectRef(StoredObjectType.SOURCE_ID, source.stableSourceId.value))
        assertEquals(com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus.DELETED, lifecycle!!.status)
    }

    @Test fun `revision-only deletion fails closed to protect lineage`() = PersonalDataHarness().use { harness ->
        val source = harness.source("revision-only")
        harness.admit(source)
        val result = harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.DeleteSource(source.stableSourceId, SourceDeletionScope.SOURCE_REVISION_ONLY),
            AdmissionActor.USER,
            AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
        ))
        assertEquals(AdmissionDisposition.REJECTED_VALIDATION, result.disposition)
        assertTrue(harness.store.reader.source(source.stableSourceId) != null)
    }

    @Test fun `deletion requires explicit user lifecycle authority`() = PersonalDataHarness().use { harness ->
        val source = harness.source("delete-authority")
        harness.admit(source)
        val result = harness.store.admission.submit(harness.request(
            LongitudinalWriteOperation.DeleteSource(source.stableSourceId), AdmissionActor.THOMAS,
            AdmissionOrigin.THOMAS_DERIVATION,
        ))
        assertEquals(AdmissionDisposition.REJECTED_AUTHORITY, result.disposition)
    }

    @Test fun `complete reset removes artifact destroys key and clears memory`() = PersonalDataHarness().use { harness ->
        harness.admit(harness.source("reset"))
        val result = harness.store.reset()
        assertTrue(result.storeArtifactDeleted)
        assertTrue(result.keyMaterialDestroyed)
        assertTrue(result.inMemoryStateCleared)
        assertTrue(result.externallyHeldBackupsUnaffected)
        assertFalse(harness.storage.exists())
        assertTrue(runCatching { harness.keyProvider.existing() }.isFailure)
    }
}
