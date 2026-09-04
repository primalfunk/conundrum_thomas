package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.journal.JournalCaptureDisposition
import com.conundrum.thomas.v2.journal.JournalEntryId
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.languageevidence.perception.NonassertiveKind
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionDisposition
import com.conundrum.thomas.v2.languageevidence.stateformation.OpenEvidenceQuestionKind
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.ContradictionAdjudication
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.longitudinal.store.QualificationStoreLocation
import com.conundrum.thomas.v2.qualification.journal.GovernedJournalCapturePipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.nio.file.Files
import java.time.Year
import org.junit.Assert.*
import org.junit.Test

class JournalModeAndLanguageQualificationTest {
    @Test fun acceptance30EmotionallyIntenseEntryDoesNotInvokeTherapy() = withStore("a30") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a30", "I hate myself today.")
        assertTrue(result.sourceCaptured)
        assertNull(result.responsePlan)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
    }

    @Test fun acceptance31ChildhoodGapDoesNotInvokeBiographerInvestigation() = withStore("a31") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a31", "My childhood feels blank.",
            JournalResponsePreference.ASK_ONE_QUESTION)
        assertNull(result.responsePlan)
        assertTrue(pipeline.formedState().openEvidenceQuestions.none { it.reasonCode.contains("COVERAGE") })
    }

    @Test fun acceptance32QuestionGroundingIsCurrentEntryLocal() = withStore("a32") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a32-old", "I was sad yesterday.")
        val current = CTV209TestSupport.capture(harness, pipeline, "a32-current", "I was calm today.",
            JournalResponsePreference.ASK_ONE_QUESTION)
        assertEquals(CTV209TestSupport.sourceRevision("a32-current"), current.responsePlan!!.grounding.sourceRevisionId)
        assertNotEquals(CTV209TestSupport.sourceRevision("a32-old"), current.responsePlan!!.grounding.sourceRevisionId)
    }

    @Test fun acceptance33JournalMayCapturePastHistoryWithoutChangingMode() = withStore("a33") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a33", "I moved to Denver in 2018.")
        val source = harness.store.reader.snapshot().sources.single()
        assertEquals(AcquisitionMode.JOURNAL, source.provenance.acquisitionMode)
        assertEquals(EventTime.Unknown("Journal report time is separate from described event time"), source.eventTime)
        assertTrue(harness.store.reader.snapshot().assertions.single().eventTime is EventTime.Range)
    }

    @Test fun acceptance34SelfReportRemainsSelfReport() = withStore("a34") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a34", "I feel exhausted today.")
        assertEquals(EvidenceEpistemicClass.EXPLICIT_SELF_REPORT,
            harness.store.reader.snapshot().assertions.single().epistemicClass)
    }

    @Test fun acceptance35UserInterpretationRemainsInterpretation() = withStore("a35") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a35", "I think moving there was a mistake.")
        assertEquals(EvidenceEpistemicClass.USER_INTERPRETATION,
            harness.store.reader.snapshot().assertions.single().epistemicClass)
    }

    @Test fun acceptance36ThirdPartyReportDoesNotBecomeDirectOtherMindFact() = withStore("a36") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a36", "Sam told me he was furious.")
        assertEquals(EvidenceEpistemicClass.THIRD_PARTY_REPORT,
            harness.store.reader.snapshot().assertions.single().epistemicClass)
    }

    @Test fun acceptance37CounterfactualFormsNoEventEvidence() = withStore("a37") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a37", "If I'd stayed, I'd probably have hated it.")
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
        assertEquals(NonassertiveKind.COUNTERFACTUAL, result.responsePlan?.let { null }
            ?: com.conundrum.thomas.v2.languageevidence.perception.ConservativeLanguagePerception()
                .perceive(com.conundrum.thomas.v2.languageevidence.perception.CommittedSourceText.from(
                    harness.store.reader.snapshot().sources.single())).unresolved.single().nonassertiveKind)
    }

    @Test fun acceptance38InterrogativeDoesNotBecomeAssertion() = withStore("a38") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a38", "Did I move there in 2012?")
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
    }

    @Test fun acceptance39AmbiguousPronounRemainsUnresolved() = withStore("a39") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a39", "He was angry.")
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
        assertTrue(pipeline.formedState().openEvidenceQuestions.any { it.kind == OpenEvidenceQuestionKind.UNSUPPORTED_OR_AMBIGUOUS_SOURCE })
    }

    @Test fun acceptance40ApproximateDateRemainsApproximate() = withStore("a40") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a40", "Around 2012 I changed jobs.")
        assertEquals(EventTime.ApproximateYear(Year.of(2012)), harness.store.reader.snapshot().assertions.single().eventTime)
        assertEquals(AssertionUncertainty.APPROXIMATE, harness.store.reader.snapshot().assertions.single().uncertainty)
    }

    @Test fun acceptance41ExplicitUserEditIsGovernedRevisionNotOverwrite() = withStore("a41") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a41", "I moved to Denver in 2012.")
        val result = pipeline.revise(CTV209TestSupport.revision(harness, "a41", 1, 2, "I moved to Denver in 2013."))
        assertEquals(JournalCaptureDisposition.CAPTURED, result.disposition)
        val history = harness.store.reader.sourceRevisionHistory(JournalEntryId.parse("a41").sourceIdentity())
        assertEquals(CTV209TestSupport.sourceRevision("a41"), history.last().provenance.previousRevisionId)
        assertEquals(2, history.size)
    }

    @Test fun acceptance42ContradictoryLaterEntryIsPreserved() = withStore("a42") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a42-seattle", "I lived in Seattle in 2010.")
        CTV209TestSupport.capture(harness, pipeline, "a42-portland", "I lived in Portland in 2010.")
        assertEquals(2, harness.store.reader.snapshot().assertions.size)
        assertEquals(1, harness.store.reader.contradictions().size)
        assertEquals(ContradictionAdjudication.UNRESOLVED, harness.store.reader.contradictions().single().adjudication)
    }

    @Test fun acceptance43IndependentEntriesCreateIndependentSources() = withStore("a43") { harness, pipeline ->
        listOf("today", "yesterday", "again").forEachIndexed { index, scope ->
            CTV209TestSupport.capture(harness, pipeline, "a43-${index + 1}", "I skipped lunch $scope.")
        }
        assertEquals(3, harness.store.reader.snapshot().sources.map { it.stableSourceId }.distinct().size)
    }

    @Test fun acceptance44ThreeIndependentReportsMayFormNeutralRecurrenceCandidate() = withStore("a44") { harness, pipeline ->
        listOf("today", "yesterday", "again").forEachIndexed { index, scope ->
            CTV209TestSupport.capture(harness, pipeline, "a44-${index + 1}", "I skipped lunch $scope.")
        }
        val recurrence = pipeline.formedState().recurrenceCandidates.single()
        assertEquals("REPEATED_REPORTED_OCCURRENCE", recurrence.label)
        assertEquals(3, recurrence.independentSourceIds.size)
    }

    @Test fun acceptance45ThreeSentencesInOneEntryDoNotCreateIndependentRecurrence() = withStore("a45") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a45",
            "I skipped lunch today. I skipped lunch again. I skipped lunch yesterday.")
        assertEquals(3, harness.store.reader.snapshot().assertions.size)
        assertTrue(pipeline.formedState().recurrenceCandidates.isEmpty())
    }

    @Test fun acceptance46ContradictoryEntriesCoexistWithoutWinner() = withStore("a46") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a46-one", "I lived in Seattle in 2010.")
        CTV209TestSupport.capture(harness, pipeline, "a46-two", "I lived in Portland in 2010.")
        val relation = harness.store.reader.contradictions().single()
        assertNotNull(harness.store.reader.assertion(relation.leftAssertionId))
        assertNotNull(harness.store.reader.assertion(relation.rightAssertionId))
    }

    @Test fun acceptance47ChangeOverTimeIsNotFalseContradiction() = withStore("a47") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a47-one", "I lived in Seattle in 2010.")
        CTV209TestSupport.capture(harness, pipeline, "a47-two", "I lived in Portland in 2018.")
        assertTrue(harness.store.reader.contradictions().isEmpty())
    }

    @Test fun acceptance48CloseReopenAndReplayPreserveCanonicalDigest() {
        val path = CTV209TestSupport.path("a48")
        val replayPath = CTV209TestSupport.path("a48-replay")
        Files.deleteIfExists(replayPath)
        SyntheticLongitudinalStoreHarness(path).use { harness ->
            var pipeline = GovernedJournalCapturePipeline(harness.store)
            CTV209TestSupport.capture(harness, pipeline, "a48", "I moved to Denver in 2018.")
            val digest = harness.store.reader.canonicalLogicalStateDigest()
            val formedDigest = pipeline.formedState().canonicalDigest
            harness.reopen()
            pipeline = GovernedJournalCapturePipeline(harness.store)
            assertEquals(digest, harness.store.reader.canonicalLogicalStateDigest())
            assertEquals(formedDigest, pipeline.formedState().canonicalDigest)
            harness.store.replayIntoEmpty(QualificationStoreLocation.file(replayPath)).use { replay ->
                assertEquals(digest, replay.reader.canonicalLogicalStateDigest())
                assertEquals(formedDigest, GovernedJournalCapturePipeline(replay).formedState().canonicalDigest)
            }
        }
        Files.deleteIfExists(replayPath)
    }

    private fun withStore(
        name: String,
        block: (SyntheticLongitudinalStoreHarness, GovernedJournalCapturePipeline) -> Unit,
    ) {
        SyntheticLongitudinalStoreHarness(CTV209TestSupport.path(name)).use { harness ->
            block(harness, GovernedJournalCapturePipeline(harness.store))
        }
    }
}
