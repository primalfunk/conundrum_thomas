package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.journal.JournalCaptureDisposition
import com.conundrum.thomas.v2.journal.JournalCaptureOrigin
import com.conundrum.thomas.v2.journal.JournalLanguageProcessingDisposition
import com.conundrum.thomas.v2.journal.JournalPrivacy
import com.conundrum.thomas.v2.journal.JournalPrivacyChangeCommand
import com.conundrum.thomas.v2.journal.JournalPrivacyChangeDisposition
import com.conundrum.thomas.v2.journal.JournalResponseExecutionDisposition
import com.conundrum.thomas.v2.journal.JournalResponseExecutor
import com.conundrum.thomas.v2.journal.JournalResponseIntentDisposition
import com.conundrum.thomas.v2.journal.JournalResponseIntentPlanner
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.journal.JournalResponseSemanticAct
import com.conundrum.thomas.v2.journal.JournalIdempotencyKey
import com.conundrum.thomas.v2.languageevidence.stateformation.OpenEvidenceQuestionKind
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.qualification.journal.DeterministicJournalRenderer
import com.conundrum.thomas.v2.qualification.journal.GovernedJournalCapturePipeline
import com.conundrum.thomas.v2.qualification.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.time.Year
import org.junit.Assert.*
import org.junit.Test

class JournalCaptureQualificationTest {
    @Test fun acceptance01TypedNoResponseCapturesJournalSource() = withStore("a01") { harness, pipeline ->
        val result = CTV209TestSupport.requireCaptured(CTV209TestSupport.capture(harness, pipeline, "a01", "I was angry today."))
        assertEquals(AcquisitionMode.JOURNAL, result.receipt!!.acquisitionMode)
        assertEquals(JournalCaptureOrigin.TYPED, result.receipt!!.captureOrigin)
        assertEquals(JournalResponseIntentDisposition.NONE_SELECTED, result.receipt!!.responseIntentDisposition)
        assertNull(result.responsePlan)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
    }

    @Test fun acceptance02SpeechTranscriptUsesSameJournalPathWithoutAudio() = withStore("a02") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a02", "I was angry today.",
            origin = JournalCaptureOrigin.SPEECH_TRANSCRIPT)
        val source = harness.store.reader.source(result.receipt!!.stableSourceId)!!
        assertEquals("SPEECH_TRANSCRIPT", source.provenance.metadata["journal.capture-origin"])
        assertFalse(source.provenance.metadata.keys.any { "audio" in it.lowercase() })
    }

    @Test fun acceptance03TypedAndSpeechEvidenceAreSemanticallyEquivalentExceptProvenance() = withStore("a03") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a03-typed", "I was angry today.")
        CTV209TestSupport.capture(harness, pipeline, "a03-speech", "I was angry today.",
            origin = JournalCaptureOrigin.SPEECH_TRANSCRIPT)
        val snapshot = harness.store.reader.snapshot()
        val typed = snapshot.assertions.single { it.sourceRecordId == CTV209TestSupport.sourceRevision("a03-typed") }
        val speech = snapshot.assertions.single { it.sourceRecordId == CTV209TestSupport.sourceRevision("a03-speech") }
        assertEquals(CTV209TestSupport.semantic(typed), CTV209TestSupport.semantic(speech))
        assertNotEquals(snapshot.sources[0].provenance.metadata, snapshot.sources[1].provenance.metadata)
    }

    @Test fun acceptance04MundaneEntryIsValidWithoutPsychologicalSignificance() = withStore("a04") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a04", "I bought groceries after work.")
        assertTrue(result.sourceCaptured)
        assertEquals("event.bought-groceries", harness.store.reader.snapshot().assertions.single().predicate.conceptId.value)
    }

    @Test fun acceptance05UnsupportedProseRemainsAValidSource() = withStore("a05") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a05", "What a weird day.")
        assertTrue(result.sourceCaptured)
        assertEquals(JournalLanguageProcessingDisposition.SOURCE_ONLY, result.receipt!!.languageProcessingDisposition)
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
    }

    @Test fun acceptance06NoResponsePostureIsSilent() = withStore("a06") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a06", "I was sad today.", JournalResponsePreference.NO_RESPONSE)
        assertNull(result.responsePlan)
        assertEquals(JournalResponseIntentDisposition.NONE_SELECTED, result.receipt!!.responseIntentDisposition)
    }

    @Test fun acceptance07ReflectPostureCreatesOnlyBriefPlan() = withStore("a07") { harness, pipeline ->
        val plan = CTV209TestSupport.capture(harness, pipeline, "a07", "I was sad today.", JournalResponsePreference.REFLECT).responsePlan!!
        assertEquals(JournalResponseSemanticAct.BRIEF_REFLECTION, plan.semanticAct)
        assertEquals(0, plan.maximumQuestionCount)
    }

    @Test fun acceptance08AllPosturesLeaveEvidenceAndStateEquivalent() = withStore("a08") { harness, pipeline ->
        val initial = CTV209TestSupport.command(harness, "a08", "I was worried today.")
        val silent = pipeline.commit(initial)
        val digest = pipeline.formedState().canonicalDigest
        val sources = harness.store.reader.snapshot().sources
        val assertions = harness.store.reader.snapshot().assertions
        val reflected = pipeline.commit(initial.copy(responsePreference = JournalResponsePreference.REFLECT))
        val asked = pipeline.commit(initial.copy(responsePreference = JournalResponsePreference.ASK_ONE_QUESTION))
        assertEquals(digest, pipeline.formedState().canonicalDigest)
        assertEquals(sources, harness.store.reader.snapshot().sources)
        assertEquals(assertions, harness.store.reader.snapshot().assertions)
        assertEquals(silent.receipt!!.admittedEvidenceIds, reflected.receipt!!.admittedEvidenceIds)
        assertEquals(silent.receipt!!.admittedEvidenceIds, asked.receipt!!.admittedEvidenceIds)
    }

    @Test fun acceptance09NoResponseInvokesNoRenderer() = withStore("a09") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a09", "I was calm today.")
        val renderer = DeterministicJournalRenderer()
        assertEquals(JournalResponseExecutionDisposition.NOT_REQUESTED,
            JournalResponseExecutor().execute(result, renderer).disposition)
        assertEquals(0, renderer.invocationCount)
    }

    @Test fun acceptance10ReflectIsGroundedOnlyInCurrentEntry() = withStore("a10") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a10",
            "Work was exhausting today. I kept getting interrupted.", JournalResponsePreference.REFLECT)
        val plan = result.responsePlan!!
        assertEquals(result.receipt!!.sourceRevisionId, plan.grounding.sourceRevisionId)
        assertEquals(EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, plan.grounding.epistemicClass)
        assertEquals(0, plan.maximumQuestionCount)
    }

    @Test fun acceptance11AskOneQuestionAllowsAtMostOneGroundedQuestion() = withStore("a11") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a11",
            "Work was exhausting today. I kept getting interrupted.", JournalResponsePreference.ASK_ONE_QUESTION)
        val renderer = DeterministicJournalRenderer()
        val rendered = JournalResponseExecutor().execute(result, renderer)
        assertEquals(1, rendered.renderedText!!.count { it == '?' })
        assertEquals(result.receipt!!.sourceRevisionId, result.responsePlan!!.grounding.sourceRevisionId)
    }

    @Test fun acceptance12AskMayDegradeToSilenceWithoutSafeGrounding() = withStore("a12") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a12", "What a weird day.",
            JournalResponsePreference.ASK_ONE_QUESTION)
        assertNull(result.responsePlan)
        assertEquals(JournalResponseIntentDisposition.NO_SAFE_GROUNDED_RESPONSE, result.receipt!!.responseIntentDisposition)
    }

    @Test fun acceptance13PlannerFailureOccursAfterDurableCapture() {
        SyntheticLongitudinalStoreHarness(CTV209TestSupport.path("a13")).use { harness ->
            val pipeline = GovernedJournalCapturePipeline(harness.store, JournalResponseIntentPlanner { _, _ -> error("synthetic") })
            val result = CTV209TestSupport.capture(harness, pipeline, "a13", "I was angry today.", JournalResponsePreference.REFLECT)
            assertEquals(JournalCaptureDisposition.RESPONSE_PLANNING_FAILED_AFTER_CAPTURE, result.disposition)
            assertTrue(result.sourceCaptured)
            assertEquals(1, harness.store.reader.snapshot().sources.size)
        }
    }

    @Test fun acceptance14RendererFailureOccursAfterDurableCapture() = withStore("a14") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a14", "I was angry today.", JournalResponsePreference.REFLECT)
        val execution = JournalResponseExecutor().execute(result) { error("synthetic") }
        assertEquals(JournalResponseExecutionDisposition.RENDERING_FAILED_AFTER_CAPTURE, execution.disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
    }

    @Test fun acceptance15ThomasResponseArtifactNeverBecomesJournalEvidence() = withStore("a15") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a15", "I was angry today.", JournalResponsePreference.REFLECT)
        val before = harness.store.reader.snapshot()
        JournalResponseExecutor().execute(result, DeterministicJournalRenderer())
        assertEquals(before, harness.store.reader.snapshot())
    }

    @Test fun acceptance16CurrentFeelingFormsExplicitSelfReport() = withStore("a16") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a16", "I feel exhausted today.")
        val assertion = harness.store.reader.snapshot().assertions.single()
        assertEquals(EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, assertion.epistemicClass)
        assertEquals(EventTime.RelativePeriod("today", com.conundrum.thomas.v2.longitudinal.RelativeTemporalRelation.DURING), assertion.eventTime)
    }

    @Test fun acceptance17RememberedMoveSeparatesCurrentReportFromApproximateEventTime() = withStore("a17") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a17", "I remembered today that we moved around 2012.")
        val snapshot = harness.store.reader.snapshot()
        assertEquals(EventTime.ApproximateYear(Year.of(2012)), snapshot.assertions.single().eventTime)
        assertEquals("2039-09-03T12:00:00Z", snapshot.sources.single().reportTime.value.toString())
    }

    @Test fun acceptance18UncertainRelativeTimingRemainsUnresolved() = withStore("a18") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a18", "Maybe that happened before college.")
        val assertion = harness.store.reader.snapshot().assertions.single()
        assertEquals(EvidenceEpistemicClass.USER_INTERPRETATION, assertion.epistemicClass)
        assertTrue(assertion.eventTime is EventTime.RelativePeriod)
        assertTrue(pipeline.formedState().openEvidenceQuestions.any { it.kind == OpenEvidenceQuestionKind.UNRESOLVED_REFERENCE })
    }

    @Test fun acceptance19BiographerAccountCoexistsWithoutChangingJournalProvenance() = withStore("a19") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a19", "I remembered today that we moved around 2012.")
        val biographyRevision = CTV208TestSupport.admitText(harness, "a19-biography", "Around 2012 I changed jobs.",
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE)
        GovernedLanguageEvidencePipeline(harness.store).process(biographyRevision)
        val modes = harness.store.reader.snapshot().sources.map { it.provenance.acquisitionMode }.toSet()
        assertEquals(setOf(AcquisitionMode.JOURNAL, AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE), modes)
    }

    @Test fun acceptance20PrivateEntryIsRetainedButSkippedForDerivation() = withStore("a20") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a20", "What a weird day.", privacy = JournalPrivacy.PRIVATE)
        assertEquals(JournalLanguageProcessingDisposition.SKIPPED_PRIVATE, result.receipt!!.languageProcessingDisposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
    }

    @Test fun acceptance21ExtractablePrivateSelfReportNeverBecomesActiveState() = withStore("a21") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a21", "I was sad today.", privacy = JournalPrivacy.PRIVATE)
        assertTrue(pipeline.formedState().activeSelfReports.isEmpty())
        assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
    }

    @Test fun acceptance22PrivacyRestorationRequiresReviewWithoutReactivation() = withStore("a22") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a22", "I was sad today.", privacy = JournalPrivacy.PRIVATE)
        val change = pipeline.changePrivacy(JournalPrivacyChangeCommand(
            com.conundrum.thomas.v2.journal.JournalEntryId.parse("a22"),
            JournalIdempotencyKey.parse("restore-a22"),
            harness.store.reader.currentStoreRevision(),
            JournalPrivacy.ELIGIBLE,
        ))
        assertEquals(JournalPrivacyChangeDisposition.CHANGED, change.disposition)
        val lifecycle = harness.store.reader.lifecycle(LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION,
            CTV209TestSupport.sourceRevision("a22").value))
        assertEquals(LongitudinalLifecycleStatus.REVIEW_REQUIRED, lifecycle!!.status)
        assertTrue(pipeline.formedState().activeSelfReports.isEmpty())
    }

    @Test fun acceptance23CommittedEntryHasOneStableSourceIdentity() = withStore("a23") { harness, pipeline ->
        val result = CTV209TestSupport.capture(harness, pipeline, "a23", "I moved to Denver in 2018.")
        assertEquals("journal.a23", result.receipt!!.stableSourceId.value)
        assertEquals(1, harness.store.reader.sourceRevisionHistory(result.receipt!!.stableSourceId).size)
    }

    @Test fun acceptance24PostCommitEditAppendsRevisionAndPreservesOriginal() = withStore("a24") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a24", "I moved to Denver in 2012.")
        pipeline.revise(CTV209TestSupport.revision(harness, "a24", 1, 2, "I moved to Denver in 2013."))
        val history = harness.store.reader.sourceRevisionHistory(com.conundrum.thomas.v2.journal.JournalEntryId.parse("a24").sourceIdentity())
        assertEquals(2, history.size)
        assertEquals("I moved to Denver in 2012.", (history.first().originalContent as OriginalSourceContent.Inline).exactContent)
        assertEquals("I moved to Denver in 2013.", (history.last().originalContent as OriginalSourceContent.Inline).exactContent)
    }

    @Test fun acceptance25DateRevisionMarksPriorDerivationReviewRequired() = withStore("a25") { harness, pipeline ->
        CTV209TestSupport.capture(harness, pipeline, "a25", "I moved to Denver in 2012.")
        val oldAssertion = harness.store.reader.snapshot().assertions.single()
        pipeline.revise(CTV209TestSupport.revision(harness, "a25", 1, 2, "I moved to Denver in 2013."))
        val oldLifecycle = harness.store.reader.lifecycle(LongitudinalObjectRef(StoredObjectType.ASSERTION, oldAssertion.id.value))
        assertEquals(LongitudinalLifecycleStatus.REVIEW_REQUIRED, oldLifecycle!!.status)
        assertEquals(2, harness.store.reader.snapshot().sources.size)
        assertEquals(2, harness.store.reader.snapshot().assertions.size)
    }

    @Test fun acceptance26PreferenceChangeAloneCreatesNoRevision() = withStore("a26") { harness, pipeline ->
        val command = CTV209TestSupport.command(harness, "a26", "I was calm today.")
        pipeline.commit(command)
        pipeline.commit(command.copy(responsePreference = JournalResponsePreference.REFLECT))
        assertEquals(1, harness.store.reader.sourceRevisionHistory(command.entryId.sourceIdentity()).size)
    }

    @Test fun acceptance27IdenticalCommitKeyReplaysWithoutDuplicate() = withStore("a27") { harness, pipeline ->
        val command = CTV209TestSupport.command(harness, "a27", "I was calm today.")
        pipeline.commit(command)
        val replay = pipeline.commit(command)
        assertEquals(JournalCaptureDisposition.IDEMPOTENT_REPLAY, replay.disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
        assertEquals(1, harness.store.reader.snapshot().assertions.size)
    }

    @Test fun acceptance28SameKeyDifferentTextFailsClosed() = withStore("a28") { harness, pipeline ->
        val command = CTV209TestSupport.command(harness, "a28", "I was calm today.")
        pipeline.commit(command)
        val conflict = pipeline.commit(command.copy(committedText = "I was sad today."))
        assertEquals(JournalCaptureDisposition.REJECTED_IDEMPOTENCY_CONFLICT, conflict.disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
    }

    @Test fun idempotencyKeyCannotChangePrivacy() = withStore("a28-privacy") { harness, pipeline ->
        val command = CTV209TestSupport.command(harness, "a28-privacy", "I was calm today.")
        pipeline.commit(command)
        val conflict = pipeline.commit(command.copy(privacy = JournalPrivacy.PRIVATE))
        assertEquals(JournalCaptureDisposition.REJECTED_IDEMPOTENCY_CONFLICT, conflict.disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
        assertEquals(1, harness.store.reader.snapshot().assertions.size)
    }

    @Test fun acceptance29RetryAfterResponseFailureCreatesNoDuplicateSource() {
        SyntheticLongitudinalStoreHarness(CTV209TestSupport.path("a29")).use { harness ->
            val command = CTV209TestSupport.command(harness, "a29", "I was sad today.", JournalResponsePreference.REFLECT)
            val failing = GovernedJournalCapturePipeline(harness.store, JournalResponseIntentPlanner { _, _ -> error("synthetic") })
            assertEquals(JournalCaptureDisposition.RESPONSE_PLANNING_FAILED_AFTER_CAPTURE, failing.commit(command).disposition)
            val retry = GovernedJournalCapturePipeline(harness.store).commit(command.copy(responsePreference = JournalResponsePreference.NO_RESPONSE))
            assertEquals(JournalCaptureDisposition.IDEMPOTENT_REPLAY, retry.disposition)
            assertEquals(1, harness.store.reader.snapshot().sources.size)
        }
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
