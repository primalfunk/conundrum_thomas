package com.conundrum.thomas.v2.qualification

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.*
import org.junit.Test

class LanguageEvidenceBoundaryQualificationTest {
    private val root = Path.of(System.getProperty("thomas.repositoryRoot"))
    private fun text(relative: String) = Files.readString(root.resolve(relative))

    @Test fun languageModuleIsPureAndSeparatedFromTherapy() {
        val settings = text("settings.gradle.kts")
        val build = text("thomas/language-evidence/build.gradle.kts").lowercase()
        assertTrue(settings.contains(":thomas:language-evidence"))
        assertTrue(build.contains(":thomas:longitudinal-admission"))
        listOf("android", "compose", "room", "jni", "llama", "speech", "renderer", ":thomas:engine", ":thomas:safety")
            .forEach { assertFalse("forbidden dependency $it", build.contains(it)) }
        assertFalse(text("thomas/engine/build.gradle.kts").contains(":thomas:language-evidence"))
    }

    @Test fun onlyAuthorizedSyntheticCaptureAndQualificationModulesConsumeLanguageEvidence() {
        val consumers = root.toFile().walkCanonicalTopDown().filter { file ->
            file.isFile && file.name == "build.gradle.kts" && file.readText().contains(":thomas:language-evidence")
        }.map { it.relativeTo(root.toFile()).invariantSeparatorsPath }.sorted().toList()
        assertEquals(
            listOf(
                "qualification/build.gradle.kts",
                "thomas/biographer/build.gradle.kts",
                "thomas/journal/build.gradle.kts",
                "thomas/runtime/build.gradle.kts",
                "thomas/therapy-longitudinal/build.gradle.kts",
            ),
            consumers,
        )
        val storeConsumers = root.toFile().walkCanonicalTopDown().filter { file ->
            file.isFile && file.name == "build.gradle.kts" && file.readText().contains(":thomas:longitudinal-store")
        }.map { it.relativeTo(root.toFile()).invariantSeparatorsPath }.sorted().toList()
        assertEquals(listOf("qualification/build.gradle.kts"), storeConsumers)
    }

    @Test fun rawLanguageHasNoTherapeuticPolicyRoute() {
        val policyText = listOf("thomas/engine/src", "thomas/safety/src")
            .map { root.resolve(it).toFile() }.filter { it.exists() }
            .flatMap { it.walkTopDown().filter { file -> file.isFile && file.extension == "kt" }.toList() }
            .joinToString("\n") { it.readText() }
        listOf("languageevidence", "CommittedSourceText", "LanguagePerceptionResult")
            .forEach { assertFalse(policyText.contains(it)) }
        val runtime = root.resolve("thomas/runtime/src/main").toFile().walkTopDown()
            .filter { it.isFile && it.extension == "kt" }.joinToString("\n") { it.readText() }
        assertTrue(runtime.contains("GovernedLanguageEvidencePipeline"))
        assertFalse(runtime.contains("CommittedSourceText"))
        assertFalse(runtime.contains("LanguagePerceptionResult"))
    }

    @Test fun noModelPromptNetworkOrSqlInLanguageAuthority() {
        val implementation = listOf(root.resolve("thomas/language-evidence/src/main"),
            root.resolve("qualification/src/main/kotlin/com/conundrum/thomas/v2/qualification/languageevidence"))
            .flatMap { it.toFile().walkTopDown().filter { file -> file.isFile }.toList() }
            .joinToString("\n") { it.readText() }.lowercase()
        listOf("openai", "gguf", "llama", "retrofit", "httpclient", "embedding", "system prompt",
            "java.sql", "drivermanager", "jdbc:", "projection_state", "dao")
            .forEach { assertFalse("forbidden implementation token $it", implementation.contains(it)) }
    }

    @Test fun languageCompositionHasExactlyTwoGovernedSubmissionSites() {
        val composition = text("thomas/language-evidence/src/main/kotlin/com/conundrum/thomas/v2/languageevidence/GovernedLanguageEvidencePipeline.kt")
        assertEquals(2, "admission.submit".toRegex(RegexOption.LITERAL).findAll(composition).count())
        assertFalse(composition.contains("AcceptedAdmissionReceipt"))
    }

    @Test fun canonicalGitMetadataAndIdeMappingAreHealthy() {
        val top = git("rev-parse", "--show-toplevel").replace('/', '\\')
        assertEquals(root.toAbsolutePath().normalize().toString(), Path.of(top).toAbsolutePath().normalize().toString())
        assertEquals(".git", git("rev-parse", "--git-dir"))
        assertEquals("true", git("rev-parse", "--is-inside-work-tree"))
        assertTrue(Files.isDirectory(root.resolve(".git")))
        val stale = root.toFile().listFiles().orEmpty().filter { file -> file.isDirectory &&
            (file.name.startsWith(".git-ct-v2-") || file.name in setOf(".git-work", ".git-temp", "git-metadata-backup")) }
        assertTrue("Stale Git metadata: ${stale.map { it.name }}", stale.isEmpty())
        val vcs = root.resolve(".idea/vcs.xml")
        if (Files.exists(vcs)) {
            val mapping = Files.readString(vcs)
            assertTrue(mapping.contains("PROJECT_DIR") && mapping.contains("vcs=") && mapping.contains("Git"))
        }
    }

    @Test fun forwardPlanAndV1DenialRemainSealed() {
        val plan = root.resolve("docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md")
        assertEquals(25031, Files.size(plan))
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(plan)).joinToString("") { "%02x".format(it) }
        assertEquals("bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886", hash)
        assertFalse(Files.exists(root.resolve("fdp.txt")))
        val register = text("migration/v1-component-register.json")
        assertEquals(24, register.lines().count { it.contains("approvalState") && it.contains("DENIED") })
        assertTrue(register.contains("v1CodeMigrationAuthorized") && register.contains("false"))
    }

    private fun git(vararg arguments: String): String {
        val process = ProcessBuilder(listOf("git", "-C", root.toString()) + arguments).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        assertEquals("git command failed: $output", 0, process.waitFor())
        return output
    }
}
