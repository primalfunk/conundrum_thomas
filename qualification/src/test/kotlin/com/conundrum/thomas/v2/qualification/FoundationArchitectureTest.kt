package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoundationArchitectureTest {
    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))

    private fun file(path: String) = File(repositoryRoot, path)
    private fun text(path: String) = file(path).readText()

    private fun projectDependencies(path: String): Set<String> =
        Regex("project\\(\"([^\"]+)\"\\)")
            .findAll(text(path))
            .map { it.groupValues[1] }
            .toSet()

    @Test
    fun `all foundation modules are included`() {
        val settings = text("settings.gradle.kts")
        val modules = setOf(
            ":app",
            ":thomas:domain",
            ":thomas:provenance",
            ":thomas:engine",
            ":thomas:safety",
            ":thomas:runtime",
            ":platform:persistence-android",
            ":platform:renderer-llama-android",
            ":platform:speech-android",
            ":qualification",
            ":tools:provenance",
        )

        modules.forEach { module ->
            assertTrue("Missing $module", settings.contains("include(\"$module\")"))
        }
    }

    @Test
    fun `production dependency graph preserves authority boundaries`() {
        assertEquals(emptySet<String>(), projectDependencies("thomas/domain/build.gradle.kts"))
        assertEquals(setOf(":thomas:domain"), projectDependencies("thomas/provenance/build.gradle.kts"))
        assertEquals(
            setOf(":thomas:domain", ":thomas:provenance"),
            projectDependencies("thomas/engine/build.gradle.kts"),
        )
        assertEquals(
            setOf(":thomas:domain", ":thomas:provenance"),
            projectDependencies("thomas/safety/build.gradle.kts"),
        )
        assertEquals(
            setOf(":thomas:domain", ":thomas:provenance", ":thomas:engine", ":thomas:safety"),
            projectDependencies("thomas/runtime/build.gradle.kts"),
        )
        assertEquals(setOf(":thomas:runtime"), projectDependencies("app/build.gradle.kts"))
        assertEquals(setOf(":thomas:domain"), projectDependencies("platform/persistence-android/build.gradle.kts"))
        assertEquals(setOf(":thomas:domain"), projectDependencies("platform/renderer-llama-android/build.gradle.kts"))
        assertEquals(setOf(":thomas:domain"), projectDependencies("platform/speech-android/build.gradle.kts"))
    }

    @Test
    fun `app and renderer cannot reach inference authority or persistence`() {
        val app = text("app/build.gradle.kts")
        val renderer = text("platform/renderer-llama-android/build.gradle.kts")

        listOf(":thomas:engine", ":thomas:safety", ":platform:renderer-llama-android", ":platform:persistence-android")
            .forEach { forbidden -> assertFalse("app must not depend on $forbidden", app.contains(forbidden)) }

        listOf(":thomas:engine", ":thomas:safety", ":thomas:provenance", ":thomas:runtime", ":platform:persistence-android")
            .forEach { forbidden -> assertFalse("renderer must not depend on $forbidden", renderer.contains(forbidden)) }
    }

    @Test
    fun `renderer request exposes only command and authorized supporting text`() {
        val request = RenderRequest(
            command = RenderCommand("Render the already selected act."),
            authorizedSupportingText = listOf(AuthorizedSupportingText("evidence-1", "Authorized text")),
        )

        assertEquals("Render the already selected act.", request.command.value)
        assertEquals(listOf("evidence-1"), request.authorizedSupportingText.map { it.reference })
        val materialFields = RenderRequest::class.java.declaredFields
            .filterNot { it.isSynthetic }
            .map { it.name }
            .toSet()
        assertEquals(setOf("command", "authorizedSupportingText"), materialFields)
    }

    @Test
    fun `application identity platform floor ABI and backup policy are fixed`() {
        val appBuild = text("app/build.gradle.kts")
        val rendererBuild = text("platform/renderer-llama-android/build.gradle.kts")
        val manifest = text("app/src/main/AndroidManifest.xml")
        val extractionRules = text("app/src/main/res/xml/data_extraction_rules.xml")

        assertTrue(appBuild.contains("namespace = \"com.conundrum.thomas.v2\""))
        assertTrue(appBuild.contains("applicationId = \"com.conundrum.thomas.v2\""))
        assertTrue(appBuild.contains("minSdk = 31"))
        assertTrue(rendererBuild.contains("abiFilters += \"arm64-v8a\""))
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertTrue(extractionRules.contains("<cloud-backup"))
        assertTrue(extractionRules.contains("<device-transfer>"))
        assertFalse(extractionRules.contains("<include"))
    }

    @Test
    fun `migration register is deny by default`() {
        val register = text("migration/v1-component-register.json")
        val componentCount = Regex("\"componentId\"\\s*:").findAll(register).count()
        val deniedCount = Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count()

        assertTrue("Expected a seeded component inventory", componentCount >= 20)
        assertEquals(componentCount, deniedCount)
        assertFalse(register.contains("\"approvalState\": \"APPROVED_FOR_MIGRATION\""))
        assertFalse(register.contains("\"eventualMigrationCommit\": \""))
    }

    @Test
    fun `no restricted source or model artifact is present`() {
        val forbiddenExtensions = setOf("gguf", "safetensors", "onnx", "tflite", "pte")
        val files = repositoryRoot.walkTopDown()
            .onEnter { directory ->
                directory.name !in setOf(".git", ".git-ct-v2-setup", ".gradle", ".idea", "build")
            }
            .filter { it.isFile }
            .toList()

        assertTrue(files.none { it.extension.lowercase() in forbiddenExtensions })
        assertTrue(text(".gitignore").contains("/provenance/raw/"))
        assertTrue(text("provenance/.gitignore").contains("/raw/"))
        assertTrue(
            repositoryRoot.walkTopDown()
                .onEnter { it.name !in setOf(".git", ".git-ct-v2-01-work", ".gradle", ".idea", "build", "raw") }
                .filter { it.isFile }
                .none { it.extension.lowercase() == "pdf" },
        )
        assertFalse(file("provenance/sources").exists())
        assertFalse(file("clinical-sources").exists())
        assertFalse(file("restricted-sources").exists())
    }

    @Test
    fun `raw source cache is absent from the Git index`() {
        val gitDirectory = listOf(file(".git"), file(".git-ct-v2-01-work"))
            .firstOrNull { it.exists() }
        assertNotNull("Repository Git metadata is required for the tracked-raw-source audit", gitDirectory)

        val process = ProcessBuilder(
            "git",
            "--git-dir=${gitDirectory!!.absolutePath}",
            "--work-tree=${repositoryRoot.absolutePath}",
            "ls-files",
            "--",
            "provenance/raw",
            "provenance/sources",
            "clinical-sources",
            "restricted-sources",
        )
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }

        assertEquals("git ls-files failed: $output", 0, process.waitFor())
        assertTrue("Raw or restricted source material is tracked: $output", output.isBlank())
    }

    @Test
    fun `CT-V2-01 records stay build time and CT-V2-02 remains unopened`() {
        val seedDirectory = file("provenance/seeds")
        val persistenceSource = file("platform/persistence-android/src")

        assertNotNull(seedDirectory.listFiles())
        assertTrue(seedDirectory.listFiles()!!.any { it.extension == "sql" })
        assertFalse(persistenceSource.exists())
        assertFalse(text("thomas/runtime/build.gradle.kts").contains(":tools:provenance"))
        assertFalse(text("app/build.gradle.kts").contains(":tools:provenance"))
    }
}
