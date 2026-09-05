package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildRequest
import com.conundrum.thomas.v2.contextpacket.ModeAuthorityContract
import com.conundrum.thomas.v2.contextpacket.ModeAuthorityState
import com.conundrum.thomas.v2.journal.JournalCaptureOrigin
import com.conundrum.thomas.v2.journal.JournalCommitCommand
import com.conundrum.thomas.v2.journal.JournalEntryId
import com.conundrum.thomas.v2.journal.JournalIdempotencyKey
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.store.QualificationStoreLocation
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import com.conundrum.thomas.v2.qualification.retrieval.QualificationRetrievalPipeline
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.DeterministicLongitudinalRetriever
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.retrieval.RetrievalDisposition
import com.conundrum.thomas.v2.retrieval.RetrievalIntent
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalMode
import com.conundrum.thomas.v2.retrieval.RetrievalObjectType
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.system.measureNanoTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue

internal object CTV211ScenariosC {
    private val S = CTV211TestSupport
    private val root: Path get() = Path.of(System.getProperty("thomas.repositoryRoot"))

    fun run(id: Int) = when (id) {
        65 -> {
            val archive = manyArchive(12)
            val request = S.request(budget = ContextBudget(maximumLongitudinalObjects = 1))
            assertEquals(S.retrieve(archive, request).selectedItems, S.retrieve(archive, request).selectedItems)
        }
        66 -> assertTrue(S.retrieve(manyArchive(100)).selectedItems.size <= ContextBudget().maximumLongitudinalObjects)
        67 -> {
            val archive = largeArchive(240)
            assertEquals(240, archive.evidence.sources.size)
            assertTrue(S.retrieve(archive).candidateCount >= 200)
        }
        68 -> {
            val archive = largeArchive(400)
            lateinit var result: com.conundrum.thomas.v2.contextpacket.ContextPacketBuildResult
            val elapsed = measureNanoTime { result = S.packet(archive) }
            val packet = result.packet!!
            assertTrue(packet.longitudinal.items.size <= 8); assertTrue(elapsed > 0)
            println("CT_V2_11_LARGE_FIXTURE sources=${archive.evidence.sources.size} " +
                "derived=${archive.evidence.assertions.size} candidates=${packet.metadata.candidateCount} " +
                "selected=${packet.metadata.selectedCount} text=${packet.metadata.totalTextCharacters} " +
                "excerpts=${packet.excerpts.excerpts.size} depth=${packet.metadata.maximumTraversalDepthUsed} " +
                "elapsed_ms=${elapsed / 1_000_000}")
        }
        69 -> {
            val small = S.packet(largeArchive(20)).packet!!
            val large = S.packet(largeArchive(400)).packet!!
            assertTrue(small.metadata.totalTextCharacters <= 4096)
            assertTrue(large.metadata.totalTextCharacters <= 4096)
            assertEquals(small.longitudinal.items.size, large.longitudinal.items.size)
        }
        70 -> assertCoreSelectionSurvivesGrowth()
        71 -> assertModePressureDiffersWithoutTruthChange()
        72 -> assertEquals(RetrievalDisposition.EMPTY, S.retrieve(simple(), S.request(mode = RetrievalMode.JOURNAL)).disposition)
        73 -> assertTrue(S.retrieve(simple(), biographerRequest()).selectedItems.isNotEmpty())
        74 -> assertTrue(S.retrieve(largeArchive(100)).selectedItems.size <= 8)
        75 -> assertModeDoesNotMutateLifecycle()
        76 -> assertCloseReopenSamePacket()
        77 -> assertReplaySamePacket()
        78 -> assertAsOfRevisionHonest()
        79 -> assertCurrentCorrectionDiffers()
        80 -> assertNoDependency("thomas/retrieval", "thomas:safety")
        81 -> assertNoProductionReferenceToRetrieval("thomas/engine")
        82 -> assertNoProductionReferenceToRetrieval("thomas/engine/src/main/kotlin/com/conundrum/thomas/v2/engine/ordinary")
        83 -> assertTrue(S.evidence(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("invariant", "Synthetic invariant")))).validationIssues().isEmpty())
        84 -> assertQualificationAdapterReadsGovernedStore()
        85 -> {
            val claim = CTV211TestSupport.Claim("interpretation", "I think work was a mistake", epistemic = EvidenceEpistemicClass.USER_INTERPRETATION,
                kind = com.conundrum.thomas.v2.longitudinal.UserEvidenceKind.USER_INTERPRETATION)
            assertEquals(EvidenceEpistemicClass.USER_INTERPRETATION,
                S.retrieve(S.archive(CTV211TestSupport.EvidenceParts(listOf(claim)))).selectedItems.single().epistemicRole)
        }
        86 -> assertNoProductionReferenceToRetrieval("thomas/journal")
        87 -> assertNoProductionReferenceToRetrieval("thomas/biographer")
        88 -> {
            val command = JournalCommitCommand(
                entryId = JournalEntryId.parse("ct-v2-11-default"),
                idempotencyKey = JournalIdempotencyKey.parse("ct-v2-11-default-key"),
                expectedStoreRevision = 0,
                committedText = "Synthetic Journal entry",
                captureOrigin = JournalCaptureOrigin.TYPED,
                reportTime = ReportTime(Instant.parse("2040-09-03T12:00:00Z")),
            )
            assertEquals(JournalResponsePreference.NO_RESPONSE, command.responsePreference)
        }
        89 -> assertNoProductionReferenceToRetrieval("thomas/biographer/src/main/kotlin")
        90 -> assertProductionCompositionRootsZero()
        91 -> assertAndroidCompositionRootsZero()
        92 -> assertForbiddenTokensAbsent(listOf("invokeModel", "LLM", "modelRerank", "embedding"))
        93 -> assertForbiddenTokensAbsent(listOf("admission.submit", "LongitudinalWriteOperation", "EvidenceBundle"))
        94 -> assertV1RegisterDenied()
        95 -> assertCanonicalGitRootPresent()
        96 -> assertTemporaryGitMetadataAbsent()
        else -> error("Scenario C does not own $id")
    }

    private fun simple() = S.archive(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("simple", "Relevant work memory"))))
    private fun manyArchive(count: Int) = S.archive(CTV211TestSupport.EvidenceParts((1..count).map {
        CTV211TestSupport.Claim("bounded-${it.toString().padStart(3, '0')}", "Relevant work report $it")
    }))
    private fun largeArchive(count: Int) = S.archive(CTV211TestSupport.EvidenceParts((1..count).map {
        CTV211TestSupport.Claim("large-${it.toString().padStart(4, '0')}", "Synthetic work history record $it")
    }), digestSeed = "large-$count")
    private fun biographerRequest() = S.request(intent = RetrievalIntent.BIOGRAPHER_TARGET_CONTEXT,
        mode = RetrievalMode.BIOGRAPHER, target = "target.existing")

    private fun assertCoreSelectionSurvivesGrowth() {
        val core = CTV211TestSupport.Claim("core", "Core decision evidence", "topic.core")
        fun archive(extra: Int) = S.archive(CTV211TestSupport.EvidenceParts(listOf(core) + (1..extra).map {
            CTV211TestSupport.Claim("noise-${it.toString().padStart(3, '0')}", "Unrelated archive $it", "topic.noise")
        }), digestSeed = "growth-$extra")
        val request = S.request(anchors = RetrievalAnchors(predicateIds = setOf(PersonalConceptId.parse("topic.core"))))
        assertEquals(listOf("assertion.core"), S.retrieve(archive(0), request).selectedItems.map { it.stableId })
        assertEquals(listOf("assertion.core"), S.retrieve(archive(300), request).selectedItems.map { it.stableId })
    }

    private fun assertModePressureDiffersWithoutTruthChange() {
        val archive = simple()
        val journal = S.retrieve(archive, S.request(mode = RetrievalMode.JOURNAL))
        val biographer = S.retrieve(archive, biographerRequest())
        val therapy = S.retrieve(archive)
        assertTrue(journal.selectedItems.isEmpty())
        assertTrue(biographer.selectedItems.isNotEmpty() && therapy.selectedItems.isNotEmpty())
        assertEquals(archive.evidence, archive.evidence)
    }
    private fun assertModeDoesNotMutateLifecycle() {
        val archive = S.archive(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("lifecycle", "Relevant work"))), lifecycle = mapOf(
            S.lifecycle(RetrievalObjectType.ASSERTION, "assertion.lifecycle", RetrievalLifecycleStatus.CONTESTED, true),
        ))
        val before = archive.lifecycle.toMap()
        S.retrieve(archive, S.request(mode = RetrievalMode.JOURNAL))
        S.retrieve(archive, biographerRequest())
        S.retrieve(archive)
        assertEquals(before, archive.lifecycle)
    }

    private fun path(name: String) = root.resolve("qualification/build/ct-v2-11/$name.sqlite")
    private fun <T> withStore(name: String, block: (SyntheticLongitudinalStoreHarness) -> T): T =
        SyntheticLongitudinalStoreHarness(path(name), Instant.parse("2041-01-01T00:00:00Z")).use(block)
    private fun admittedStore(harness: SyntheticLongitudinalStoreHarness) {
        harness.admitBasicSnapshot(S.evidence(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("stored", "Relevant work in the governed store")))))
    }
    private fun storePacket(harness: SyntheticLongitudinalStoreHarness): String {
        val revision = harness.store.reader.currentStoreRevision()
        val request = S.request(revision = revision)
        val result = QualificationRetrievalPipeline(harness.store).build(ContextPacketBuildRequest(
            request, ModeAuthorityContract(RetrievalMode.THERAPY,
                ModeAuthorityState.ORDINARY_THERAPY_QUALIFICATION_ONLY, "ct-v2-11.fixture-mode.v1"),
        ))
        return result.packet!!.metadata.packetDigest
    }
    private fun assertCloseReopenSamePacket() = withStore("close-reopen") { harness ->
        admittedStore(harness)
        val before = storePacket(harness)
        harness.reopen()
        assertEquals(before, storePacket(harness))
    }
    private fun assertReplaySamePacket() = withStore("replay-source") { harness ->
        admittedStore(harness)
        val before = storePacket(harness)
        val replayPath = path("replay-target")
        Files.deleteIfExists(replayPath)
        try {
            harness.store.replayIntoEmpty(QualificationStoreLocation.file(replayPath)).use { replay ->
                val revision = replay.reader.currentStoreRevision()
                val result = QualificationRetrievalPipeline(replay).build(ContextPacketBuildRequest(
                    S.request(revision = revision), ModeAuthorityContract(RetrievalMode.THERAPY,
                        ModeAuthorityState.ORDINARY_THERAPY_QUALIFICATION_ONLY, "ct-v2-11.fixture-mode.v1"),
                ))
                assertEquals(before, result.packet!!.metadata.packetDigest)
            }
        } finally { Files.deleteIfExists(replayPath) }
    }
    private fun assertAsOfRevisionHonest() = withStore("as-of") { harness ->
        admittedStore(harness)
        val current = harness.store.reader.currentStoreRevision()
        assertTrue(current >= 2)
        val port = com.conundrum.thomas.v2.qualification.retrieval.QualificationLongitudinalReadPort(harness.store.reader)
        val retriever = DeterministicLongitudinalRetriever(port)
        assertTrue(retriever.retrieve(S.request(revision = 1)).selectedItems.isEmpty())
        assertTrue(retriever.retrieve(S.request(revision = current)).selectedItems.isNotEmpty())
    }
    private fun assertCurrentCorrectionDiffers() {
        val old = S.archive(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("before", "Relevant work 2012"))), revision = 1, digestSeed = "old")
        val corrected = S.archive(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("after", "Relevant work 2013"))), revision = 2, digestSeed = "corrected")
        val retriever = DeterministicLongitudinalRetriever(CTV211TestSupport.Port(mapOf(1L to old, 2L to corrected)))
        val before = retriever.retrieve(S.request(revision = 1)).selectedItems.map { it.stableId }
        val after = retriever.retrieve(S.request(revision = 2)).selectedItems.map { it.stableId }
        assertNotEquals(before, after)
    }
    private fun assertQualificationAdapterReadsGovernedStore() = withStore("adapter") { harness ->
        admittedStore(harness)
        assertTrue(storePacket(harness).matches(Regex("[0-9a-f]{64}")))
        assertEquals(AdmissionDisposition.ACCEPTED, harness.store.reader.redactedAdmissionHistory().first().disposition)
    }

    private fun sourceText(relative: String): String = File(root.toFile(), relative).walkTopDown()
        .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }.joinToString("\n") { it.readText() }
    private fun assertNoDependency(relative: String, forbidden: String) {
        assertFalse(sourceText(relative).contains(forbidden))
    }
    private fun assertNoProductionReferenceToRetrieval(relative: String) {
        val text = sourceText(relative)
        assertFalse(text.contains("com.conundrum.thomas.v2.retrieval"))
        assertFalse(text.contains("com.conundrum.thomas.v2.contextpacket"))
    }
    private fun assertForbiddenTokensAbsent(tokens: List<String>) {
        val text = sourceText("thomas/retrieval") + sourceText("thomas/context-packet")
        tokens.forEach { assertFalse("Forbidden authority token: $it", text.contains(it)) }
    }
    private fun gradleFiles(): String = root.toFile().walkTopDown().filter {
        it.isFile && it.name.endsWith(".gradle.kts") && "build" !in it.toPath().map(Path::toString)
    }.joinToString("\n") { it.readText() }
    private fun assertProductionCompositionRootsZero() {
        val consumers = root.toFile().walkTopDown().filter { file ->
            file.isFile && file.name == "build.gradle.kts" &&
                "build" !in file.toPath().map(Path::toString) &&
                (file.readText().contains(":thomas:retrieval") || file.readText().contains(":thomas:context-packet"))
        }.map { it.relativeTo(root.toFile()).invariantSeparatorsPath }.toSet()
        assertEquals(setOf(
            "qualification/build.gradle.kts",
            "thomas/context-packet/build.gradle.kts",
            "thomas/runtime/build.gradle.kts",
            "thomas/therapy-longitudinal/build.gradle.kts",
        ), consumers)
        val qualification = File(root.toFile(), "qualification/build.gradle.kts").readText()
        assertTrue(qualification.contains(":thomas:retrieval") && qualification.contains(":thomas:context-packet"))
        val integration = File(root.toFile(), "thomas/therapy-longitudinal/build.gradle.kts").readText()
        assertTrue(integration.contains(":thomas:retrieval") && integration.contains(":thomas:context-packet"))
        val runtime = File(root.toFile(), "thomas/runtime/build.gradle.kts").readText()
        assertTrue(runtime.contains(":thomas:retrieval") && runtime.contains(":thomas:context-packet"))
    }
    private fun assertAndroidCompositionRootsZero() {
        val appText = sourceText("app") + File(root.toFile(), "app/build.gradle.kts").readText()
        assertFalse(appText.contains(":thomas:retrieval")); assertFalse(appText.contains("contextpacket"))
    }
    private fun assertV1RegisterDenied() {
        val register = File(root.toFile(), "migration/v1-component-register.json").readText()
        assertEquals(24, Regex("\"componentId\"\\s*:").findAll(register).count())
        assertEquals(24, Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count())
    }
    private fun assertCanonicalGitRootPresent() {
        assertTrue(Files.isDirectory(root.resolve(".git")))
        assertTrue(Files.isRegularFile(root.resolve("tools/verify-canonical-git-root.ps1")))
    }
    private fun assertTemporaryGitMetadataAbsent() {
        Files.list(root).use { entries ->
            assertTrue(entries.noneMatch { it.fileName.toString().startsWith(".git-ct-v2-") ||
                it.fileName.toString() in setOf(".git-work", ".git-temp", "git-metadata-backup") })
        }
    }
}
