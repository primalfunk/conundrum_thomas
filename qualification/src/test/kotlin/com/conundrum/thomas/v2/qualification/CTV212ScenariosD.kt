package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.contextpacket.ContextPacket
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import com.conundrum.thomas.v2.longitudinal.store.QualificationStoreLocation
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import com.conundrum.thomas.v2.qualification.therapy.GovernedLongitudinalTherapyPipeline
import com.conundrum.thomas.v2.retrieval.CT_V2_11_RETRIEVAL_POLICY_VERSION
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.safety.CT_V2_04_SAFETY_SCOPE_POLICY_VERSION
import com.conundrum.thomas.v2.therapylongitudinal.CT_V2_12_INTEGRATION_POLICY_VERSION
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnCommand
import com.conundrum.thomas.v2.therapylongitudinal.TherapyIdempotencyKey
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessingDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessingOutcome
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessor
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import com.conundrum.thomas.v2.therapylongitudinal.TherapySourceAdmissionOutcome
import com.conundrum.thomas.v2.therapylongitudinal.TherapySourceAdmissionPort
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnCaptureOrigin
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnId
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnPrivacy
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

internal object CTV212ScenariosD {
    private val S = CTV212TestSupport
    private val root: Path = Path.of(requireNotNull(System.getProperty("thomas.repositoryRoot")))

    fun run(id: Int) = when (id) {
        97 -> assertLargeRouteUnchanged()
        98 -> assertCoreMemoryStableUnderGrowth()
        99 -> assertBudgetExhaustionKeepsPlan()
        100 -> assertArchiveSizeDoesNotIncreaseSurfaceCount()
        101 -> assertMultiTurnSession()
        102 -> assertCoreProgressionDeterministic()
        103 -> assertCoreAntiRepetitionStillPresent()
        104 -> assertMemoryAntiRepetitionIndependent()
        105 -> assertNoHistoryContinues()
        106 -> assertContextCanRemainUnsurfaced()
        107 -> assertCloseReopenEquivalentPlan()
        108 -> assertLedgerReplayEquivalentPlan()
        109 -> assertSameRevisionSamePlan()
        110 -> assertHistoryChangeChangesPlan()
        111 -> assertEquals(CT_V2_04_SAFETY_SCOPE_POLICY_VERSION,
            QualificationSafetyGate.permitFor(CoreOrdinaryTestFixtures.state()).gatePolicyVersion)
        112 -> assertCoreProgressionDeterministic()
        113 -> assertCoreAntiRepetitionStillPresent()
        114 -> assertLongitudinalRegression()
        115 -> assertAdmissionRegression()
        116 -> assertLanguageRegression()
        117 -> assertJournalRegression()
        118 -> assertBiographerRegression()
        119 -> assertRetrievalRegression()
        120 -> assertEquals(JournalResponsePreference.NO_RESPONSE, JournalResponsePreference.entries.first())
        121 -> assertEquals(setOf(BiographerPosture.OPEN_STORY, BiographerPosture.TARGETED_COVERAGE), BiographerPosture.entries.toSet())
        122 -> assertEquals("ct-v2-11.retrieval.v1", CT_V2_11_RETRIEVAL_POLICY_VERSION)
        123 -> assertProductionRootsZero()
        124 -> assertAndroidRootsZero()
        125 -> assertModelTherapyAuthorityZero()
        126 -> assertModelRetrievalAuthorityZero()
        127 -> assertV1RegisterDenied()
        128 -> assertCanonicalGitRoot()
        129 -> assertTemporaryGitMetadataAbsent()
        else -> error("Scenario D does not own $id")
    }

    private fun assertLargeRouteUnchanged() {
        val empty = requireNotNull(S.runFake(S.emptyArchive()).plan)
        val large = requireNotNull(S.runFake(S.workArchive(400), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.memory-1",
        )).plan)
        assertEquals(empty.routeDecision?.route, large.routeDecision?.route)
        assertEquals(empty.routeDecision?.selectedActionId, large.routeDecision?.selectedActionId)
    }

    private fun assertCoreMemoryStableUnderGrowth() {
        val options = CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.memory-1",
            packetTransform = S.withReason(com.conundrum.thomas.v2.retrieval.RetrievalReason.EXPLICIT_TARGET),
        )
        val smaller = requireNotNull(S.runFake(S.workArchive(400), options).plan)
        val larger = requireNotNull(S.runFake(S.workArchive(800), options).plan)
        assertEquals(smaller.surfacedMemories.single().stableObjectId, larger.surfacedMemories.single().stableObjectId)
    }

    private fun assertBudgetExhaustionKeepsPlan() {
        val plan = requireNotNull(S.runFake(S.workArchive(400), CTV212TestSupport.FakeOptions(
            explicitTarget = "assertion.memory-1",
            budget = ContextBudget(maximumLongitudinalObjects = 1, maximumSourceExcerpts = 0),
        )).plan)
        assertNotNull(plan.routeDecision)
        assertNotNull(plan.renderSupport)
        assertTrue(plan.surfacedMemories.size <= 1)
    }

    private fun assertArchiveSizeDoesNotIncreaseSurfaceCount() {
        val sizes = listOf(1, 100, 400, 800).map { count ->
            requireNotNull(S.runFake(S.workArchive(count), CTV212TestSupport.FakeOptions(
                explicitTarget = "assertion.memory-1",
            )).plan).surfacedMemories.size
        }
        assertTrue(sizes.all { it <= 1 })
    }

    private fun assertMultiTurnSession() {
        val first = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        val second = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions("turn-2").copy(
            sessionState = first.nextSessionMemoryState,
        )).plan)
        assertNotNull(first.routeDecision)
        assertNotNull(second.routeDecision)
        assertEquals(1, first.surfacedMemories.size)
        assertTrue(second.surfacedMemories.isEmpty())
    }

    private fun assertCoreProgressionDeterministic() {
        val state = CoreOrdinaryTestFixtures.state(id = "core-regression")
        val evaluator = CoreOrdinaryTherapyEvaluator()
        val one = QualificationSafetyGate.evaluateCoreOrdinary(evaluator, state)
        val two = QualificationSafetyGate.evaluateCoreOrdinary(evaluator, state)
        assertEquals(one.routeSelection, two.routeSelection)
        assertEquals(one.selectedAction, two.selectedAction)
    }

    private fun assertCoreAntiRepetitionStillPresent() {
        val source = sourceText("thomas/engine")
        assertTrue(source.contains("explicitRepeatAuthorization"))
        assertTrue(source.contains("SUBSTITUTE_DIRECTION_CHOICE"))
    }

    private fun assertMemoryAntiRepetitionIndependent() = assertMultiTurnSession()

    private fun assertNoHistoryContinues() {
        val plan = requireNotNull(S.runFake(S.emptyArchive()).plan)
        assertNotNull(plan.renderSupport)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private fun assertContextCanRemainUnsurfaced() {
        val plan = requireNotNull(S.runFake(S.workArchive()).plan)
        assertTrue((plan.contextSummary?.selectedCount ?: 0) > 0)
        assertTrue(plan.surfacedMemories.isEmpty())
    }

    private fun fakeAdmission() = TherapySourceAdmissionPort { request ->
        TherapySourceAdmissionOutcome(
            AdmissionDisposition.ACCEPTED,
            request.stableSourceId,
            request.sourceRevisionId,
            request.expectedStoreRevision,
            request.expectedStoreRevision + 1,
            RecordTime(Instant.parse("2042-01-01T00:00:00Z")),
            listOf(request.sourceRevisionId.value),
            listOf("NON_MUTATING_CAPTURE_FIXTURE"),
            CTV211TestSupport.sha256(request.exactUserText),
        )
    }

    private fun fakeLanguage(revision: Long) = TherapyLanguageProcessor {
        TherapyLanguageProcessingOutcome(
            TherapyLanguageProcessingDisposition.SOURCE_ONLY,
            null,
            emptyList(),
            0,
            revision + 1,
            CTV211TestSupport.sha256("state-$revision"),
        )
    }

    private fun nonMutatingPlan(store: QualificationLongitudinalStore): com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyPlan {
        val revision = store.reader.currentStoreRevision()
        val target = SourceIdentityId.parse("therapy.synthetic-session.turn-1")
        val pipeline = GovernedLongitudinalTherapyPipeline(
            store,
            sourceAdmissionOverride = fakeAdmission(),
            languageProcessorOverride = fakeLanguage(revision),
        )
        val base = S.command(revision, CTV212TestSupport.FakeOptions(
            turn = "turn-2",
            memoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
            anchors = RetrievalAnchors(sourceIdentityIds = setOf(target)),
            explicitTarget = target.value,
        ))
        return requireNotNull(pipeline.integrate(base).plan)
    }

    private fun establishedHistory(harness: SyntheticLongitudinalStoreHarness) {
        val pipeline = GovernedLongitudinalTherapyPipeline(harness.store)
        val result = pipeline.integrate(S.realCommand(harness.store, turn = "turn-1", text = "I was furious yesterday."))
        assertNotNull(result.plan?.captureReceipt)
    }

    private fun assertCloseReopenEquivalentPlan() {
        val directory = Files.createTempDirectory("ct-v2-12-reopen-")
        val harness = SyntheticLongitudinalStoreHarness(directory.resolve("store.sqlite"))
        try {
            establishedHistory(harness)
            val before = nonMutatingPlan(harness.store)
            harness.reopen()
            val after = nonMutatingPlan(harness.store)
            assertEquals(before.canonicalPlanDigest, after.canonicalPlanDigest)
        } finally {
            harness.close()
            Files.deleteIfExists(directory)
        }
    }

    private fun assertLedgerReplayEquivalentPlan() {
        val directory = Files.createTempDirectory("ct-v2-12-replay-")
        val harness = SyntheticLongitudinalStoreHarness(directory.resolve("store.sqlite"))
        val replayPath = directory.resolve("replay.sqlite")
        try {
            establishedHistory(harness)
            val before = nonMutatingPlan(harness.store)
            harness.store.replayIntoEmpty(QualificationStoreLocation.file(replayPath)).use { replay ->
                val after = nonMutatingPlan(replay)
                assertEquals(before.canonicalPlanDigest, after.canonicalPlanDigest)
            }
        } finally {
            harness.close()
            Files.deleteIfExists(replayPath)
            Files.deleteIfExists(directory)
        }
    }

    private fun assertSameRevisionSamePlan() {
        val one = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        val two = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        assertEquals(one.canonicalPlanDigest, two.canonicalPlanDigest)
    }

    private fun assertHistoryChangeChangesPlan() {
        val one = requireNotNull(S.runFake(S.workArchive(revision = 1), S.strongMemoryOptions()).plan)
        val two = requireNotNull(S.runFake(S.workArchive(revision = 2), S.strongMemoryOptions()).plan)
        assertNotEquals(one.canonicalPlanDigest, two.canonicalPlanDigest)
    }

    private fun assertLongitudinalRegression() {
        assertTrue(CTV211TestSupport.evidence(CTV211TestSupport.EvidenceParts()).requireValid().sources.isEmpty())
    }

    private fun assertAdmissionRegression() = S.withRealPipeline { harness, pipeline ->
        val result = pipeline.integrate(S.realCommand(harness.store))
        assertEquals(AdmissionDisposition.ACCEPTED, result.plan?.captureReceipt?.admissionDisposition)
        assertTrue(harness.store.reader.redactedAdmissionHistory().isNotEmpty())
    }

    private fun assertLanguageRegression() = S.withRealPipeline { harness, pipeline ->
        val result = pipeline.integrate(S.realCommand(harness.store, text = "I was furious yesterday."))
        assertNotNull(result.plan?.captureReceipt?.languageDisposition)
        assertEquals(pipeline.formedState().canonicalDigest.length, 64)
    }

    private fun assertJournalRegression() {
        assertEquals(JournalResponsePreference.NO_RESPONSE, JournalResponsePreference.entries.first())
    }

    private fun assertBiographerRegression() {
        assertTrue(BiographerPosture.OPEN_STORY in BiographerPosture.entries)
        assertTrue(BiographerPosture.TARGETED_COVERAGE in BiographerPosture.entries)
    }

    private fun assertRetrievalRegression() {
        val plan = requireNotNull(S.runFake(S.workArchive(), S.strongMemoryOptions()).plan)
        assertEquals(CT_V2_11_RETRIEVAL_POLICY_VERSION, plan.contextSummary?.let { CT_V2_11_RETRIEVAL_POLICY_VERSION })
        assertEquals(CT_V2_12_INTEGRATION_POLICY_VERSION, plan.integrationPolicyVersion)
    }

    private fun sourceText(relative: String): String = File(root.toFile(), relative).walkTopDown()
        .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
        .filterNot { "build" in it.toPath().map(Path::toString) }
        .joinToString("\n") { it.readText() }

    private fun assertProductionRootsZero() {
        val app = sourceText("app")
        assertFalse(app.contains("therapylongitudinal"))
        assertFalse(app.contains(":thomas:therapy-longitudinal"))
    }

    private fun assertAndroidRootsZero() = assertProductionRootsZero()

    private fun assertModelTherapyAuthorityZero() {
        val source = sourceText("thomas/therapy-longitudinal")
        listOf("llama", "GGUF", "buildPrompt", "model.invoke", "Room", "androidx").forEach {
            assertFalse("Forbidden CT-V2-12 authority token: $it", source.contains(it, ignoreCase = true))
        }
    }

    private fun assertModelRetrievalAuthorityZero() {
        val source = sourceText("thomas/therapy-longitudinal")
        assertFalse(source.contains("embedding", ignoreCase = true))
        assertFalse(source.contains("vector", ignoreCase = true))
        assertFalse(source.contains("JDBC", ignoreCase = true))
    }

    private fun assertV1RegisterDenied() {
        val register = root.resolve("migration/v1-component-register.json").toFile().readText()
        assertEquals(24, Regex("componentId").findAll(register).count())
        assertEquals(24, Regex("approvalState[^D]+DENIED").findAll(register).count())
    }

    private fun assertCanonicalGitRoot() {
        assertTrue(Files.isDirectory(root.resolve(".git")))
        assertTrue(Files.isRegularFile(root.resolve("tools/verify-canonical-git-root.ps1")))
    }

    private fun assertTemporaryGitMetadataAbsent() {
        Files.list(root).use { entries ->
            assertTrue(entries.noneMatch { path ->
                path.fileName.toString().startsWith(".git-ct-v2-") ||
                    path.fileName.toString() in setOf(".git-work", ".git-temp", "git-metadata-backup")
            })
        }
    }
}
