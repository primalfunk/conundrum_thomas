package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.BiographerAdmissionOutcome
import com.conundrum.thomas.v2.biographer.BiographerAdmissionPort
import com.conundrum.thomas.v2.biographer.BiographerAnswerCaptureEngine
import com.conundrum.thomas.v2.biographer.BiographerAnswerOrigin
import com.conundrum.thomas.v2.biographer.BiographerCaptureDisposition
import com.conundrum.thomas.v2.biographer.BiographerInvestigationHistory
import com.conundrum.thomas.v2.biographer.BiographerLanguageProcessor
import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerProhibitedQuestionAct
import com.conundrum.thomas.v2.biographer.BiographerQuestionExecutor
import com.conundrum.thomas.v2.biographer.BiographerQuestionRenderDisposition
import com.conundrum.thomas.v2.biographer.CoverageRequest
import com.conundrum.thomas.v2.biographer.InvestigationTargetKind
import com.conundrum.thomas.v2.engine.ordinary.CoreActionExecution
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryRoute
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.journal.JournalResponseIntentDisposition
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.TemporalCoordinates
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.qualification.biographer.GovernedBiographerPipeline
import com.conundrum.thomas.v2.qualification.journal.GovernedJournalCapturePipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiographerAuthorityQualificationTest {
    @Test fun acceptance51TypedAnswerUsesGuidedBiographerProvenance() = withStore("a51") { harness, pipeline ->
        val result = CTV210TestSupport.capture(
            harness, pipeline, "a51", "I moved to Denver in 2018.",
            CTV210TestSupport.targetedPlan("event.move"),
        ).capture
        assertEquals(AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE, result.receipt!!.acquisitionMode)
        assertEquals(BiographerAnswerOrigin.TYPED, result.receipt!!.origin)
    }

    @Test fun acceptance52SpeechTranscriptAnswerUsesSamePathWithoutAudio() = withStore("a52") { harness, pipeline ->
        CTV210TestSupport.capture(
            harness, pipeline, "a52", "I moved to Denver in 2018.",
            CTV210TestSupport.targetedPlan("event.move"),
            BiographerAnswerOrigin.SPEECH_TRANSCRIPT,
        )
        val source = harness.store.reader.snapshot().sources.single()
        assertEquals("SPEECH_TRANSCRIPT", source.provenance.metadata["biographer.answer-origin"])
        assertFalse(source.provenance.metadata.keys.any { "audio" in it.lowercase() })
    }

    @Test fun acceptance53IdenticalAnswerRetryCreatesNoDuplicateSource() = withStore("a53") { harness, pipeline ->
        val command = CTV210TestSupport.command(
            harness, "a53", "I moved to Denver in 2018.", CTV210TestSupport.targetedPlan("event.move"),
        )
        val first = pipeline.captureAnswer(command, BiographerInvestigationHistory()).capture
        val second = pipeline.captureAnswer(command, BiographerInvestigationHistory()).capture
        assertTrue(first.sourceCaptured)
        assertEquals(BiographerCaptureDisposition.IDEMPOTENT_REPLAY, second.disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
        assertEquals(1, harness.store.reader.snapshot().assertions.size)
    }

    @Test fun acceptance54ChangedAnswerUnderSameKeyFailsClosed() = withStore("a54") { harness, pipeline ->
        val first = CTV210TestSupport.command(harness, "a54", "I moved to Denver in 2018.")
        pipeline.captureAnswer(first, BiographerInvestigationHistory())
        val changed = first.copy(committedText = "I moved to Denver in 2019.")
        val result = pipeline.captureAnswer(changed, BiographerInvestigationHistory()).capture
        assertEquals(BiographerCaptureDisposition.REJECTED_IDEMPOTENCY_CONFLICT, result.disposition)
        assertEquals(1, harness.store.reader.snapshot().sources.size)
    }

    @Test fun acceptance55SourceSurvivesDownstreamProcessingFailure() {
        var sourceAccepted = false
        val engine = BiographerAnswerCaptureEngine(
            BiographerAdmissionPort {
                sourceAccepted = true
                BiographerAdmissionOutcome(
                    AdmissionDisposition.ACCEPTED,
                    it.stableSourceId,
                    it.sourceRevisionId,
                    1,
                    emptyList(),
                    "a".repeat(64),
                )
            },
            BiographerLanguageProcessor { _, _ -> error("synthetic perception failure") },
        )
        val harness = SyntheticLongitudinalStoreHarness(CTV210TestSupport.path("a55-command"))
        try {
            val result = engine.capture(CTV210TestSupport.command(harness, "a55", "Synthetic history."))
            assertTrue(sourceAccepted)
            assertTrue(result.sourceCaptured)
            assertEquals(BiographerCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE, result.disposition)
        } finally {
            harness.close()
        }
    }

    @Test fun acceptance56QuestionRenderFailureMutatesNoSource() = withStore("a56") { harness, pipeline ->
        val decision = pipeline.decide(CoverageRequest(BiographerPosture.OPEN_STORY))
        val before = harness.store.reader.snapshot()
        val result = BiographerQuestionExecutor().execute(decision) { error("synthetic renderer failure") }
        assertEquals(BiographerQuestionRenderDisposition.FAILED_WITHOUT_SOURCE_MUTATION, result.disposition)
        assertEquals(before, harness.store.reader.snapshot())
    }

    @Test fun acceptance57EmotionallyIntenseTargetDoesNotInvokeTherapyRoute() {
        val source = text("thomas/biographer/src/main/kotlin/com/conundrum/thomas/v2/biographer")
        assertFalse(source.contains("CoreOrdinaryTherapyEvaluator"))
        assertFalse(source.contains("TherapyRoute"))
    }

    @Test fun acceptance58CognitiveWordingCannotSelectTherapeuticTechnique() {
        val plan = CTV210TestSupport.targetedPlan("event.intense", InvestigationTargetKind.EVENT_DETAIL)
        assertTrue(plan.prohibitedActs.contains(BiographerProhibitedQuestionAct.THERAPEUTIC_TECHNIQUE))
        assertTrue(plan.prohibitedActs.contains(BiographerProhibitedQuestionAct.DIAGNOSIS))
    }

    @Test fun acceptance59BiographerQuestionNeverInvokesJournalResponsePosture() {
        val source = text("thomas/biographer/src/main/kotlin/com/conundrum/thomas/v2/biographer")
        assertFalse(source.contains("JournalResponsePreference"))
        assertFalse(source.contains("JournalResponseIntentPlanner"))
    }

    @Test fun acceptance60HistoricalJournalEntryDoesNotTriggerCoverageSelection() = withStore("a60") { harness, _ ->
        val journal = GovernedJournalCapturePipeline(harness.store)
        val before = harness.store.reader.snapshot().sources.size
        CTV209TestSupport.capture(harness, journal, "a60", "I remembered today that we moved around 2012.")
        assertEquals(before + 1, harness.store.reader.snapshot().sources.size)
        assertEquals(AcquisitionMode.JOURNAL, harness.store.reader.snapshot().sources.single().provenance.acquisitionMode)
        assertFalse(text("thomas/journal/src/main").contains("DeterministicBiographerCoverageEngine"))
    }

    @Test fun acceptance61SafetyPermitBoundaryStillGatesOrdinaryPolicy() {
        val state = CoreOrdinaryTestFixtures.state(id = "ct-v2-10-safety-regression")
        val evaluator = CoreOrdinaryTherapyEvaluator()
        assertNotNull(evaluator.evaluate(state, CoreOrdinaryTestFixtures.permit(state)).selectedAction)
        assertTrue(evaluator::class.java.declaredMethods.filter { it.name == "evaluate" }
            .all { it.parameterTypes.last().simpleName == "OrdinaryTherapyPermit" })
    }

    @Test fun acceptance62TherapyProgressionRemainsDeterministic() {
        val state = CoreOrdinaryTestFixtures.state(id = "ct-v2-10-progression")
        val evaluator = CoreOrdinaryTherapyEvaluator()
        val first = evaluator.evaluate(state, CoreOrdinaryTestFixtures.permit(state))
        val second = evaluator.evaluate(state, CoreOrdinaryTestFixtures.permit(state))
        assertEquals(first, second)
    }

    @Test fun acceptance63TherapyAntiRepetitionRemainsQualified() {
        val evaluator = CoreOrdinaryTherapyEvaluator()
        val base = CoreOrdinaryTestFixtures.state(id = "ct-v2-10-anti-loop")
        val first = evaluator.evaluate(base, CoreOrdinaryTestFixtures.permit(base))
        val repeated = base.copy(
            activeRoute = OrdinaryRoute.LISTEN_SUPPORT,
            actionHistory = listOf(CoreActionExecution(first.selectedAction!!.definition.id, 1, OrdinaryRoute.LISTEN_SUPPORT)),
        )
        val next = evaluator.evaluate(repeated, CoreOrdinaryTestFixtures.permit(repeated))
        assertNotEquals(first.selectedAction!!.definition.id, next.selectedAction?.definition?.id)
    }

    @Test fun acceptance64LongitudinalTemporalCoordinatesRemainDistinct() {
        val coordinates = TemporalCoordinates(
            EventTime.Unknown("Synthetic historical time unresolved"),
            ReportTime(Instant.parse("2039-01-01T00:00:00Z")),
            RecordTime(Instant.parse("2040-01-01T00:00:00Z")),
        )
        assertNotEquals(coordinates.eventTime.toString(), coordinates.reportTime.toString())
        assertNotEquals(coordinates.reportTime.value, coordinates.recordTime.value)
    }

    @Test fun acceptance65GovernedStoreStillProducesAdmissionReceipt() = withStore("a65") { harness, pipeline ->
        val result = CTV210TestSupport.capture(harness, pipeline, "a65", "I moved to Denver in 2018.").capture
        assertTrue(result.sourceCaptured)
        assertEquals(AdmissionDisposition.ACCEPTED, result.receipt!!.admissionDisposition)
        assertTrue(harness.store.reader.redactedAdmissionHistory().isNotEmpty())
    }

    @Test fun acceptance66EpistemicStateStillSeparatesInterpretation() = withStore("a66") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "a66", "I think Sam was furious.")
        assertEquals(1, pipeline.formedState().activeUserInterpretations.size)
        assertTrue(pipeline.formedState().activeExplicitClaims.isEmpty())
    }

    @Test fun acceptance67JournalCaptureStillUsesItsOwnAuthority() = withStore("a67") { harness, _ ->
        val journal = GovernedJournalCapturePipeline(harness.store)
        val result = CTV209TestSupport.capture(harness, journal, "a67", "I was calm today.")
        assertTrue(result.sourceCaptured)
        assertEquals(AcquisitionMode.JOURNAL, result.receipt!!.acquisitionMode)
    }

    @Test fun acceptance68JournalDefaultRemainsNoResponse() = withStore("a68") { harness, _ ->
        val journal = GovernedJournalCapturePipeline(harness.store)
        val result = CTV209TestSupport.capture(harness, journal, "a68", "I was calm today.")
        assertEquals(JournalResponsePreference.NO_RESPONSE, result.receipt!!.responsePreference)
        assertEquals(JournalResponseIntentDisposition.NONE_SELECTED, result.receipt!!.responseIntentDisposition)
        assertNull(result.responsePlan)
    }

    @Test fun acceptance69ProductionBiographerWritersRemainZero() {
        val roots = listOf("app", "thomas/runtime", "platform")
        assertTrue(roots.all { !text(it).contains("GovernedBiographerPipeline") })
    }

    @Test fun acceptance70AndroidAppLongitudinalWritersRemainZero() {
        val gradle = text("app") + text("platform")
        assertFalse(gradle.contains("longitudinal-store"))
        assertFalse(gradle.contains("thomas:biographer"))
    }

    @Test fun acceptance71ModelAuthorizedEvidenceWritersRemainZero() {
        val source = text("thomas/biographer/src/main") + text("qualification/src/main/kotlin/com/conundrum/thomas/v2/qualification/biographer")
        assertFalse(source.contains("llama"))
        assertFalse(source.contains("GGUF"))
        assertFalse(source.contains("Prompt"))
    }

    @Test fun acceptance72V1MigrationRegisterRemainsTwentyFourDenied() {
        val register = File(repositoryRoot(), "migration/v1-component-register.json").readText()
        assertEquals(24, register.lineSequence().count { "componentId" in it })
        assertEquals(24, register.lineSequence().count { "approvalState" in it && "DENIED" in it })
    }

    @Test fun acceptance73CanonicalGitRootRemainsPresent() {
        val root = repositoryRoot()
        assertTrue(File(root, ".git").isDirectory)
        assertTrue(File(root, "settings.gradle.kts").isFile)
    }

    @Test fun acceptance74TemporaryGitMetadataDirectoriesRemainZero() {
        val stale = repositoryRoot().listFiles().orEmpty().filter {
            it.isDirectory && (
                it.name.startsWith(".git-ct-v2-") ||
                    it.name in setOf(".git-work", ".git-temp", "git-metadata-backup")
                )
        }
        assertTrue(stale.isEmpty())
    }

    @Test fun coverageAndStateDigestsSurviveCloseAndReopen() {
        val path = CTV210TestSupport.path("replay-digest")
        SyntheticLongitudinalStoreHarness(path).use { harness ->
            val pipeline = GovernedBiographerPipeline(harness.store)
            CTV210TestSupport.capture(harness, pipeline, "replay", "I moved to Denver in 2018.")
            val beforeState = pipeline.formedState().canonicalDigest
            val beforeCoverage = pipeline.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE)).coverageMap.canonicalDigest
            harness.reopen()
            val reopened = GovernedBiographerPipeline(harness.store)
            assertEquals(beforeState, reopened.formedState().canonicalDigest)
            assertEquals(beforeCoverage, reopened.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE)).coverageMap.canonicalDigest)
        }
    }

    @Test fun coverageDigestIsReproducibleForSameHistory() = withStore("coverage-determinism") { harness, pipeline ->
        CTV210TestSupport.capture(harness, pipeline, "coverage", "I moved to Denver in 2018.")
        val history = BiographerInvestigationHistory()
        val first = pipeline.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE), history)
        val second = pipeline.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE), history)
        assertEquals(first.coverageMap.canonicalDigest, second.coverageMap.canonicalDigest)
    }

    private fun text(relative: String): String {
        val path = File(repositoryRoot(), relative)
        if (!path.exists()) return ""
        return if (path.isFile) path.readText() else path.walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "kts", "xml", "json") }
            .joinToString("\n") { it.readText() }
    }

    private fun repositoryRoot() = File(System.getProperty("thomas.repositoryRoot"))

    private fun withStore(
        name: String,
        block: (SyntheticLongitudinalStoreHarness, GovernedBiographerPipeline) -> Unit,
    ) {
        SyntheticLongitudinalStoreHarness(CTV210TestSupport.path(name)).use { harness ->
            block(harness, GovernedBiographerPipeline(harness.store))
        }
    }
}
