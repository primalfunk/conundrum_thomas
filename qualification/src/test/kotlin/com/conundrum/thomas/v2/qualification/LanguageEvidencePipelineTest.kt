package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.languageevidence.perception.PerceptionContext
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.qualification.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.qualification.languageevidence.LanguagePipelineDisposition
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import org.junit.Assert.*
import org.junit.Test

class LanguageEvidencePipelineTest {
    @Test fun sourceMustExistBeforePerception() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("source-first")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            assertThrows(IllegalArgumentException::class.java) {
                pipeline.process(com.conundrum.thomas.v2.longitudinal.SourceRecordId.parse("missing-source"))
            }
            assertEquals(0, harness.store.reader.currentStoreRevision())
        }
    }

    @Test fun admittedSourceBecomesGroundedEvidenceOnlyThroughController() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("admit-move")).use { harness ->
            val source = CTV208TestSupport.admitText(harness, "move", "I moved to Denver in 2018.")
            val result = GovernedLanguageEvidencePipeline(harness.store).process(source)
            assertEquals(LanguagePipelineDisposition.ADMITTED, result.disposition)
            assertEquals(AdmissionDisposition.ACCEPTED, result.evidenceAdmission?.disposition)
            val claim = harness.store.reader.snapshot().assertions.single()
            assertEquals(source, claim.sourceGrounding?.sourceRevisionId)
        }
    }

    @Test fun unsupportedLanguageLeavesCommittedSourceUntouched() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("unsupported")).use { harness ->
            val source = CTV208TestSupport.admitText(harness, "unsupported", "The blue idea slept quickly.")
            val result = GovernedLanguageEvidencePipeline(harness.store).process(source)
            assertEquals(LanguagePipelineDisposition.SOURCE_ONLY, result.disposition)
            assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
            assertEquals("The blue idea slept quickly.",
                (harness.store.reader.snapshot().sources.single().originalContent as com.conundrum.thomas.v2.longitudinal.OriginalSourceContent.Inline).exactContent)
        }
    }

    @Test fun identicalReprocessingIsIdempotent() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("idempotent")).use { harness ->
            val source = CTV208TestSupport.admitText(harness, "idempotent", "I moved to Denver in 2018.")
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            val first = pipeline.process(source)
            val revision = harness.store.reader.currentStoreRevision()
            val second = pipeline.process(source)
            assertEquals(LanguagePipelineDisposition.ADMITTED, first.disposition)
            assertEquals(LanguagePipelineDisposition.IDEMPOTENT_REPLAY, second.disposition)
            assertEquals(revision, harness.store.reader.currentStoreRevision())
            assertEquals(1, harness.store.reader.snapshot().assertions.size)
        }
    }

    @Test fun resolvableCorrectionPreservesOriginalAndSupersedesIt() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("correction")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            val original = CTV208TestSupport.admitText(harness, "move-2012", "I moved to Denver in 2012.")
            val first = pipeline.process(original)
            val target = first.state.activeExplicitClaims.single().id
            val correction = CTV208TestSupport.admitText(harness, "move-correction",
                "Actually, it was 2013, not 2012.", AcquisitionMode.USER_CORRECTION)
            val corrected = pipeline.process(correction, PerceptionContext(ClaimReference.Assertion(target)))
            assertEquals(LanguagePipelineDisposition.ADMITTED, corrected.disposition)
            assertEquals(2, harness.store.reader.snapshot().assertions.size)
            assertEquals(1, harness.store.reader.corrections().size)
            assertFalse(harness.store.reader.isEligible(com.conundrum.thomas.v2.longitudinal.admission.assertionRef(target)))
            assertTrue(corrected.state.excludedEvidence.any { it.objectId == target.value && it.lifecycleState.startsWith("SUPERSEDED") })
        }
    }

    @Test fun ambiguousCorrectionChangesNoEarlierClaim() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("ambiguous-correction")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            val original = CTV208TestSupport.admitText(harness, "original-date", "I moved to Denver in 2012.")
            pipeline.process(original)
            val correction = CTV208TestSupport.admitText(harness, "orphan-correction",
                "Actually, it was 2013, not 2012.", AcquisitionMode.USER_CORRECTION)
            val before = harness.store.reader.snapshot().assertions.size
            val result = pipeline.process(correction)
            assertEquals(LanguagePipelineDisposition.SOURCE_ONLY, result.disposition)
            assertEquals(before, harness.store.reader.snapshot().assertions.size)
            assertTrue(result.state.unresolvedCorrectionSourceRevisionIds.contains(correction))
        }
    }

    @Test fun comparableSameYearClaimsFormUnresolvedContradiction() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("contradiction")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            pipeline.process(CTV208TestSupport.admitText(harness, "seattle-2010", "I lived in Seattle in 2010."))
            val result = pipeline.process(CTV208TestSupport.admitText(harness, "portland-2010", "I lived in Portland in 2010."))
            assertEquals(1, result.state.contradictions.size)
            assertEquals(2, result.state.activeExplicitClaims.size)
            assertTrue(result.state.openEvidenceQuestions.any { it.reasonCode == "COMPARABLE_CLAIMS_CONFLICT" })
        }
    }

    @Test fun differentYearsDoNotFormFalseContradiction() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("not-contradiction")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            pipeline.process(CTV208TestSupport.admitText(harness, "seattle-2010-ok", "I lived in Seattle in 2010."))
            val result = pipeline.process(CTV208TestSupport.admitText(harness, "portland-2018-ok", "I lived in Portland in 2018."))
            assertTrue(result.state.contradictions.isEmpty())
        }
    }

    @Test fun closeReopenReconstructsIdenticalStateDigest() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("reopen-language")).use { harness ->
            val source = CTV208TestSupport.admitText(harness, "reopen-move", "I moved to Denver in 2018.")
            val before = GovernedLanguageEvidencePipeline(harness.store).process(source).state.canonicalDigest
            harness.reopen()
            val after = GovernedLanguageEvidencePipeline(harness.store).formState().canonicalDigest
            assertEquals(before, after)
        }
    }

    @Test fun emptyStoreProducesValidDeterministicState() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("empty-state")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            assertEquals(pipeline.formState(), pipeline.formState())
            assertTrue(pipeline.formState().activeExplicitClaims.isEmpty())
        }
    }

    @Test fun admissionControllerRejectsTamperedSourceSpanWithoutMutation() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("tampered-span")).use { harness ->
            val sourceId = CTV208TestSupport.admitText(harness, "tampered-span", "I moved to Denver in 2018.")
            val source = harness.store.reader.snapshot().sources.single()
            val parsed = com.conundrum.thomas.v2.languageevidence.perception.ConservativeLanguagePerception()
                .perceive(com.conundrum.thomas.v2.languageevidence.perception.CommittedSourceText.from(source))
            val claim = parsed.proposals.single().assertion
            val tampered = claim.copy(sourceGrounding = claim.sourceGrounding!!.copy(sourceRevisionSha256 = "0".repeat(64)))
            val before = harness.store.reader.currentStoreRevision()
            val result = harness.submitBundle(com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle(assertions = listOf(tampered)))
            assertEquals(AdmissionDisposition.REJECTED_VALIDATION, result.disposition)
            assertEquals(before, harness.store.reader.currentStoreRevision())
            assertNull(harness.store.reader.assertion(tampered.id))
            assertNotNull(harness.store.reader.snapshot().sources.firstOrNull { it.id == sourceId })
        }
    }
}
