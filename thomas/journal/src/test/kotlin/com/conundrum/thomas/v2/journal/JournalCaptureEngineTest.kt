package com.conundrum.thomas.v2.journal

import com.conundrum.thomas.v2.languageevidence.perception.EvidenceProposal
import com.conundrum.thomas.v2.languageevidence.perception.LanguagePerceptionResult
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionDisposition
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class JournalCaptureEngineTest {
    @Test fun defaultNoResponseBypassesPlanner() {
        var plannerCalls = 0
        val engine = engine(planner = JournalResponseIntentPlanner { _, _ ->
            plannerCalls += 1
            error("must not run")
        })
        val result = engine.commit(command())
        assertEquals(JournalCaptureDisposition.CAPTURED, result.disposition)
        assertEquals(JournalResponseIntentDisposition.NONE_SELECTED, result.receipt!!.responseIntentDisposition)
        assertNull(result.responsePlan)
        assertEquals(0, plannerCalls)
    }

    @Test fun emptyEntryIsRejectedBeforeAdmission() {
        var admissions = 0
        val engine = engine(admission = FakeAdmission { admissions += 1; accepted(it) })
        assertEquals(JournalCaptureDisposition.REJECTED_EMPTY_ENTRY, engine.commit(command(text = " ")).disposition)
        assertEquals(0, admissions)
    }

    @Test fun unauthorizedCommandIsRejected() {
        val result = engine().commit(command(authority = JournalQualificationAuthority.NOT_AUTHORIZED))
        assertEquals(JournalCaptureDisposition.REJECTED_AUTHORITY, result.disposition)
        assertFalse(result.sourceCaptured)
    }

    @Test fun privateCaptureSkipsLanguageAndResponse() {
        var languageCalls = 0
        val engine = engine(language = JournalLanguageProcessor { languageCalls += 1; processing() })
        val result = engine.commit(command(privacy = JournalPrivacy.PRIVATE, preference = JournalResponsePreference.REFLECT))
        assertEquals(JournalLanguageProcessingDisposition.SKIPPED_PRIVATE, result.receipt!!.languageProcessingDisposition)
        assertEquals(JournalResponseIntentDisposition.PRIVATE_ENTRY_SUPPRESSED, result.receipt!!.responseIntentDisposition)
        assertNull(result.responsePlan)
        assertEquals(0, languageCalls)
    }

    @Test fun languageFailurePreservesSourceReceipt() {
        val result = engine(language = JournalLanguageProcessor { error("synthetic") }).commit(command())
        assertEquals(JournalCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE, result.disposition)
        assertTrue(result.sourceCaptured)
        assertEquals(JournalLanguageProcessingDisposition.FAILED_AFTER_SOURCE_CAPTURE, result.receipt!!.languageProcessingDisposition)
    }

    @Test fun planningFailurePreservesCapture() {
        val result = engine(planner = JournalResponseIntentPlanner { _, _ -> error("synthetic") })
            .commit(command(preference = JournalResponsePreference.REFLECT))
        assertEquals(JournalCaptureDisposition.RESPONSE_PLANNING_FAILED_AFTER_CAPTURE, result.disposition)
        assertTrue(result.sourceCaptured)
        assertNull(result.responsePlan)
    }

    @Test fun reflectPlanIsBoundedAndGrounded() {
        val result = engine().commit(command(preference = JournalResponsePreference.REFLECT))
        val plan = requireNotNull(result.responsePlan)
        assertEquals(JournalResponseSemanticAct.BRIEF_REFLECTION, plan.semanticAct)
        assertEquals(0, plan.maximumQuestionCount)
        assertEquals(SourceRecordId.parse("journal.test.rev-1"), plan.grounding.sourceRevisionId)
        assertTrue(JournalProhibitedResponseAct.THERAPY_TECHNIQUE in plan.prohibitedActs)
    }

    @Test fun questionPlanAllowsExactlyOneQuestion() {
        val plan = requireNotNull(engine().commit(command(preference = JournalResponsePreference.ASK_ONE_QUESTION)).responsePlan)
        assertEquals(JournalResponseSemanticAct.ONE_GROUNDED_QUESTION, plan.semanticAct)
        assertEquals(1, plan.maximumQuestionCount)
        assertTrue(JournalProhibitedResponseAct.BIOGRAPHER_GAP_PURSUIT in plan.prohibitedActs)
    }

    @Test fun unsupportedEntryDoesNotManufactureQuestion() {
        val unsupported = LanguagePerceptionResult("journal.test.rev-1", "0".repeat(64),
            disposition = PerceptionDisposition.NO_EVIDENCE_PROPOSAL)
        val result = engine(language = JournalLanguageProcessor { processing(unsupported) })
            .commit(command(preference = JournalResponsePreference.ASK_ONE_QUESTION))
        assertNull(result.responsePlan)
        assertEquals(JournalResponseIntentDisposition.NO_SAFE_GROUNDED_RESPONSE, result.receipt!!.responseIntentDisposition)
    }

    @Test fun noPlanMakesNoRendererCall() {
        val result = engine().commit(command())
        var calls = 0
        val execution = JournalResponseExecutor().execute(result) { calls += 1; "unexpected" }
        assertEquals(JournalResponseExecutionDisposition.NOT_REQUESTED, execution.disposition)
        assertEquals(0, calls)
    }

    @Test fun rendererFailureCannotRevokeCapture() {
        val result = engine().commit(command(preference = JournalResponsePreference.REFLECT))
        val execution = JournalResponseExecutor().execute(result) { error("synthetic") }
        assertEquals(JournalResponseExecutionDisposition.RENDERING_FAILED_AFTER_CAPTURE, execution.disposition)
        assertTrue(result.sourceCaptured)
    }

    @Test fun draftTypeIsNotAcceptedByCommitApi() {
        val parameter = JournalCaptureEngine::class.java.methods.single { it.name == "commit" }.parameterTypes.single()
        assertEquals(JournalCommitCommand::class.java, parameter)
        assertNotEquals(JournalDraft::class.java, parameter)
    }

    private fun engine(
        admission: JournalAdmissionPort = FakeAdmission(::accepted),
        language: JournalLanguageProcessor = JournalLanguageProcessor { processing() },
        planner: JournalResponseIntentPlanner = DeterministicJournalResponseIntentPlanner(),
    ) = JournalCaptureEngine(admission, language, planner)

    private fun command(
        text: String = "I was angry today.",
        preference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
        privacy: JournalPrivacy = JournalPrivacy.ELIGIBLE,
        authority: JournalQualificationAuthority = JournalQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
    ) = JournalCommitCommand(
        JournalEntryId.parse("test"),
        JournalIdempotencyKey.parse("test-key"),
        0,
        text,
        JournalCaptureOrigin.TYPED,
        preference,
        privacy,
        ReportTime(Instant.parse("2039-01-01T00:00:00Z")),
        authority,
    )

    private fun accepted(request: JournalSourceAdmissionRequest) = JournalAdmissionOutcome(
        AdmissionDisposition.ACCEPTED,
        request.stableSourceId,
        request.revisionId,
        request.captureOrigin,
        request.privacy,
        1,
        listOf(request.revisionId.value),
        emptyList(),
        "1".repeat(64),
    )

    private fun processing(perception: LanguagePerceptionResult = perception()) = JournalLanguageProcessingOutcome(
        JournalLanguageProcessingDisposition.ADMITTED,
        perception,
        perception.proposals.map { it.assertion.id.value },
        0,
        2,
        "2".repeat(64),
    )

    private fun perception(): LanguagePerceptionResult {
        val source = SourceRecordId.parse("journal.test.rev-1")
        val assertion = EvidenceAssertion(
            AssertionId.parse("journal.test.rev-1.emotion"),
            source,
            AssertionSubject.User,
            AssertionPredicate(PersonalConceptId.parse("self.reported-emotion"), PredicateSemantics.USER_INTERNAL_EXPERIENCE),
            AssertionValue.Text("angry"),
            UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
            EventTime.Unknown(),
            EvidenceEpistemicClass.EXPLICIT_SELF_REPORT,
        )
        return LanguagePerceptionResult(source.value, "0".repeat(64), disposition = PerceptionDisposition.UNDERSTOOD,
            proposals = listOf(EvidenceProposal(assertion)))
    }
}

private class FakeAdmission(
    private val source: (JournalSourceAdmissionRequest) -> JournalAdmissionOutcome,
) : JournalAdmissionPort {
    override fun admitSource(request: JournalSourceAdmissionRequest) = source(request)
    override fun appendSourceRevision(request: JournalSourceRevisionRequest): JournalAdmissionOutcome = error("not used")
    override fun changePrivacy(request: JournalPrivacyAdmissionRequest): JournalAdmissionOutcome = error("not used")
}
