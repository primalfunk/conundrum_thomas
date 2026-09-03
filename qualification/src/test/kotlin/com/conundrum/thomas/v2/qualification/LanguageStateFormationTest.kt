package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.languageevidence.perception.CommittedSourceText
import com.conundrum.thomas.v2.languageevidence.perception.ConservativeLanguagePerception
import com.conundrum.thomas.v2.languageevidence.stateformation.DeterministicStateFormation
import com.conundrum.thomas.v2.languageevidence.stateformation.LongitudinalStateEvidence
import com.conundrum.thomas.v2.longitudinal.*
import com.conundrum.thomas.v2.longitudinal.admission.*
import com.conundrum.thomas.v2.qualification.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class LanguageStateFormationTest {
    @Test fun threeIndependentSourcesMayFormNeutralRecurrenceCandidate() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("recurrence")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            listOf("recurrence-a", "recurrence-b", "recurrence-c").forEach { suffix ->
                pipeline.process(CTV208TestSupport.admitText(harness, suffix, "I skipped lunch today."))
            }
            val candidate = pipeline.formState().recurrenceCandidates.single()
            assertEquals("REPEATED_REPORTED_OCCURRENCE", candidate.label)
            assertEquals(3, candidate.independentSourceIds.size)
        }
    }

    @Test fun threeDerivedClaimsFromOneSourceDoNotCountAsIndependent() {
        val source = source("I skipped lunch today.", "one-source")
        val base = ConservativeLanguagePerception().perceive(CommittedSourceText.from(source)).proposals.single().assertion
        val assertions = (1..3).map { base.copy(id = AssertionId.parse("one-source-assertion-$it")) }
        val snapshot = LongitudinalEvidenceSnapshot(sources = listOf(source), assertions = assertions).requireValid()
        val state = DeterministicStateFormation().form(LongitudinalStateEvidence(1, snapshot, setOf(source.id), assertions.map { it.id }.toSet()))
        assertTrue(state.recurrenceCandidates.isEmpty())
    }

    @Test fun twoSamMentionsRemainUnresolvedRatherThanMerged() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("two-sams")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            pipeline.process(CTV208TestSupport.admitText(harness, "sam-one", "I think Sam was angry."))
            val state = pipeline.process(CTV208TestSupport.admitText(harness, "sam-two", "Sam hates me.")).state
            assertEquals(2, state.activeEntitiesAndEvents.filterIsInstance<Person>().size)
            assertEquals(1, state.unresolvedIdentities.size)
        }
    }

    @Test fun selfBeliefAndThirdPartyReportStaySeparate() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("epistemic-state")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            pipeline.process(CTV208TestSupport.admitText(harness, "belief", "I feel like nobody likes me."))
            val state = pipeline.process(CTV208TestSupport.admitText(harness, "report", "Sam told me he was angry.")).state
            assertEquals(1, state.activeSelfBeliefs.size)
            assertEquals(1, state.activeThirdPartyReports.size)
            assertTrue(state.activeExplicitClaims.none { it.predicate.semantics == PredicateSemantics.THIRD_PARTY_INTERNAL_STATE })
        }
    }

    @Test fun privateEvidenceIsExcludedAndDoesNotSilentlyReactivate() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("private-state")).use { harness ->
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            val source = CTV208TestSupport.admitText(harness, "private-move", "I moved to Denver in 2018.")
            pipeline.process(source)
            val stableId = SourceIdentityId.parse("language-private-move")
            val privateResult = harness.store.admission.submit(harness.request(
                LongitudinalWriteOperation.ChangePrivacy(stableId, SourcePrivacy.PRIVATE),
                AdmissionActor.USER, AdmissionOrigin.QUALIFICATION_HARNESS))
            assertEquals(AdmissionDisposition.ACCEPTED, privateResult.disposition)
            assertTrue(pipeline.formState().activeExplicitClaims.isEmpty())
            val restore = harness.store.admission.submit(harness.request(
                LongitudinalWriteOperation.ChangePrivacy(stableId, SourcePrivacy.ELIGIBLE),
                AdmissionActor.USER, AdmissionOrigin.QUALIFICATION_HARNESS))
            assertEquals(AdmissionDisposition.ACCEPTED, restore.disposition)
            assertTrue(pipeline.formState().activeExplicitClaims.isEmpty())
        }
    }

    @Test fun declinedCoverageDoesNotCreateEvidenceOfAbsence() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("declined-state")).use { harness ->
            val topic = CoverageTopic(CoverageTopicId.parse("coverage.declined"), "Synthetic private topic", InformationCoverageStatus.DECLINED)
            val result = harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ChangeCoverage(topic),
                AdmissionActor.USER, AdmissionOrigin.QUALIFICATION_HARNESS))
            assertEquals(AdmissionDisposition.ACCEPTED, result.disposition)
            val state = GovernedLanguageEvidencePipeline(harness.store).formState()
            assertTrue(state.activeExplicitClaims.isEmpty())
            assertTrue(state.activeUserInterpretations.isEmpty())
        }
    }

    @Test fun sourceRevisionKeepsOldExtractionHistoricalAndUsesCurrentRevision() {
        SyntheticLongitudinalStoreHarness(CTV208TestSupport.path("source-revision")).use { harness ->
            val oldId = CTV208TestSupport.admitText(harness, "revisioned", "I moved to Denver in 2018.")
            val pipeline = GovernedLanguageEvidencePipeline(harness.store)
            pipeline.process(oldId)
            val newId = SourceRecordId.parse("language-revisioned-rev-2")
            val append = harness.store.admission.submit(harness.request(
                LongitudinalWriteOperation.AppendSourceRevision(SourceIdentityId.parse("language-revisioned"), oldId, newId,
                    OriginalSourceContent.Inline("I moved to Portland in 2018."), ReportTime(Instant.parse("2039-01-02T00:00:00Z"))),
                AdmissionActor.USER, AdmissionOrigin.JOURNAL))
            assertEquals(AdmissionDisposition.ACCEPTED, append.disposition)
            val state = pipeline.process(newId).state
            assertEquals(2, harness.store.reader.snapshot().assertions.size)
            assertEquals(listOf(newId), state.contributingSourceRevisionIds)
            assertEquals("Portland", (state.activeExplicitClaims.single().value as AssertionValue.Text).value)
        }
    }

    private fun source(text: String, suffix: String) = SourceRecord(
        SourceRecordId.parse("source.$suffix.rev-1"), PersonalEvidenceProvenance(AcquisitionMode.JOURNAL),
        ReportTime(Instant.parse("2050-01-01T00:00:00Z")), RecordTime(Instant.parse("2050-01-01T00:00:01Z")),
        OriginalSourceContent.Inline(text), SourceIdentityId.parse("source.$suffix"),
    )
}
