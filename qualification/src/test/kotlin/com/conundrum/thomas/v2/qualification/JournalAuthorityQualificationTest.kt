package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.ModePrimaryFunction
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.*
import org.junit.Test

class JournalAuthorityQualificationTest {
    private val root = Path.of(System.getProperty("thomas.repositoryRoot"))
    private fun text(relative: String) = Files.readString(root.resolve(relative))

    @Test fun acceptance49SafetyPermitBoundaryRemainsUnwiredFromJournal() {
        val journalBuild = text("thomas/journal/build.gradle.kts").lowercase()
        val safetyBuild = text("thomas/safety/build.gradle.kts")
        assertFalse(journalBuild.contains(":thomas:safety"))
        assertFalse(safetyBuild.contains(":thomas:journal"))
    }

    @Test fun acceptance50TherapeuticProgressionRemainsUnwiredFromJournal() {
        assertFalse(text("thomas/journal/build.gradle.kts").contains(":thomas:engine"))
        assertFalse(text("thomas/engine/build.gradle.kts").contains(":thomas:journal"))
    }

    @Test fun acceptance51AntiRepetitionPolicyHasNoJournalDependency() {
        val ordinary = root.resolve("thomas/engine/src/main").toFile().walkTopDown()
            .filter { it.isFile }.joinToString("\n") { it.readText() }
        assertFalse(ordinary.contains("JournalCapture"))
        assertFalse(ordinary.contains("JournalResponsePreference"))
    }

    @Test fun acceptance52LongitudinalDomainRemainsIndependentOfJournal() {
        assertFalse(text("thomas/longitudinal/build.gradle.kts").contains(":thomas:journal"))
        val domain = root.resolve("thomas/longitudinal/src/main").toFile().walkTopDown()
            .filter { it.isFile }.joinToString("\n") { it.readText() }
        assertFalse(domain.contains("JournalCommitCommand"))
    }

    @Test fun acceptance53AdmissionAndStoreRemainIndependentOfJournal() {
        listOf("thomas/longitudinal-admission/build.gradle.kts", "thomas/longitudinal-store/build.gradle.kts")
            .forEach { assertFalse(text(it).contains(":thomas:journal")) }
    }

    @Test fun acceptance54LanguageStateAuthorityDoesNotDependOnJournal() {
        assertFalse(text("thomas/language-evidence/build.gradle.kts").contains(":thomas:journal"))
        assertTrue(text("thomas/journal/build.gradle.kts").contains(":thomas:language-evidence"))
    }

    @Test fun acceptance55JournalDefaultRemainsNoResponse() {
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, ThomasModeAuthorityContracts.journal.defaultResponse)
        assertEquals(JournalResponsePreference.NO_RESPONSE, JournalResponsePreference.entries.first())
    }

    @Test fun acceptance56BiographerRemainsInvestigationOnly() {
        assertEquals(ModePrimaryFunction.INVESTIGATION, ThomasModeAuthorityContracts.biographer.primaryFunction)
        assertEquals("What is worth learning next?", ThomasModeAuthorityContracts.biographer.governingQuestion)
    }

    @Test fun acceptance57ProductionLongitudinalAndJournalWritersRemainZero() {
        val consumers = root.toFile().walkCanonicalTopDown().filter { file ->
            file.isFile && file.name == "build.gradle.kts" && file.readText().contains(":thomas:journal")
        }.map { it.relativeTo(root.toFile()).invariantSeparatorsPath }.sorted().toList()
        assertEquals(
            listOf(
                "qualification/build.gradle.kts",
                "thomas/language-renderer/build.gradle.kts",
                "thomas/runtime/build.gradle.kts",
            ),
            consumers,
        )
        val rendererSource = root.resolve("thomas/language-renderer/src/main").toFile().walkTopDown()
            .filter { it.isFile }.joinToString("\n") { it.readText() }
        listOf("JournalCaptureEngine", "LongitudinalAdmissionController", "GovernedJournalCapturePipeline")
            .forEach { assertFalse(rendererSource.contains(it)) }
        assertFalse(text("app/build.gradle.kts").contains(":thomas:journal"))
        assertTrue(text("thomas/runtime/build.gradle.kts").contains(":thomas:journal"))
        assertFalse(text("thomas/runtime/build.gradle.kts").contains(":thomas:longitudinal-store"))
    }

    @Test fun acceptance58AppRuntimeWriterCountRemainsZero() {
        val app = root.resolve("app/src/main").toFile().walkTopDown()
            .filter { it.isFile }.joinToString("\n") { it.readText() }
        listOf("JournalCaptureEngine", "LongitudinalAdmissionController", "GovernedJournalCapturePipeline")
            .forEach { assertFalse(app.contains(it)) }
        val runtime = root.resolve("thomas/runtime/src/main").toFile().walkTopDown()
            .filter { it.isFile }.joinToString("\n") { it.readText() }
        assertEquals(1, Regex("JournalCaptureEngine[(]").findAll(runtime).count())
        assertFalse(runtime.contains("GovernedJournalCapturePipeline"))
    }

    @Test fun acceptance59ModelAuthorizedEvidenceWriterCountRemainsZero() {
        val journal = listOf(
            root.resolve("thomas/journal/src/main"),
            root.resolve("qualification/src/main/kotlin/com/conundrum/thomas/v2/qualification/journal"),
        ).flatMap { it.toFile().walkTopDown().filter { file -> file.isFile }.toList() }
            .joinToString("\n") { it.readText() }.lowercase()
        listOf("openai", "gguf", "llama", "system prompt", "embedding", "modeladapter", "invokemodel")
            .forEach { assertFalse("forbidden model authority token $it", journal.contains(it)) }
    }

    @Test fun acceptance60AllV1ComponentsRemainDenied() {
        val register = text("migration/v1-component-register.json")
        assertEquals(24, register.lineSequence().count { it.contains("componentId") })
        assertEquals(24, register.lineSequence().count { it.contains("approvalState") && it.contains("DENIED") })
        assertFalse(register.lineSequence().any {
            it.contains("v1CodeMigrationAuthorized") && it.contains("true")
        })
    }

    @Test fun acceptance61CanonicalGitDirectoryRemainsPresent() {
        assertTrue(Files.isDirectory(root.resolve(".git")))
        assertEquals(".git", git("rev-parse", "--git-dir"))
        assertEquals(
            root.toAbsolutePath().normalize(),
            Path.of(git("rev-parse", "--show-toplevel").replace('/', '\\')).toAbsolutePath().normalize(),
        )
    }

    @Test fun acceptance62TemporaryGitMetadataCountRemainsZero() {
        val stale = root.toFile().listFiles().orEmpty().filter { file ->
            file.isDirectory && (
                file.name.startsWith(".git-ct-v2-") ||
                    file.name in setOf(".git-work", ".git-temp", "git-metadata-backup")
                )
        }
        assertTrue(stale.map { it.name }.toString(), stale.isEmpty())
    }

    @Test fun journalBypassAndLoggingAuditIsClosed() {
        val core = root.resolve("thomas/journal/src/main").toFile().walkTopDown()
            .filter { it.isFile }.joinToString("\n") { it.readText() }
        val composition = text(
            "qualification/src/main/kotlin/com/conundrum/thomas/v2/qualification/journal/" +
                "GovernedJournalCapturePipeline.kt",
        )
        listOf("java.sql", "DriverManager", "jdbc:", "android.util.Log", "println(", "Log.d(")
            .forEach { assertFalse(core.contains(it)) }
        assertEquals(3, Regex("store\\.admission\\.submit").findAll(composition).count())
        assertEquals(1, Regex("LongitudinalWriteOperation\\.AdmitSource").findAll(composition).count())
        assertFalse(core.contains("TherapeuticPolicy"))
        assertFalse(core.contains("Biographer"))
    }

    private fun git(vararg arguments: String): String {
        val process = ProcessBuilder(listOf("git", "-C", root.toString()) + arguments)
            .redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        assertEquals("git command failed: $output", 0, process.waitFor())
        return output
    }
}
