package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.DirectProfileMutationAuthority
import com.conundrum.thomas.v2.domain.mode.LanguageModelDecisionAuthority
import com.conundrum.thomas.v2.domain.mode.ModePrimaryFunction
import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.store.AcceptedAdmissionReceipt
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalAdmissionController
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalReader
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import com.conundrum.thomas.v2.longitudinal.store.QualificationStoreLocation
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalStoreBoundaryQualificationTest {
    private val root = Path.of(System.getProperty("thomas.repositoryRoot"))

    @Test fun `module graph enforces store to admission to domain direction`() {
        val settings = root.resolve("settings.gradle.kts").toFile().readText()
        assertTrue(settings.contains("include(\":thomas:longitudinal-admission\")"))
        assertTrue(settings.contains("include(\":thomas:longitudinal-store\")"))
        val admission = root.resolve("thomas/longitudinal-admission/build.gradle.kts").toFile().readText()
        val store = root.resolve("thomas/longitudinal-store/build.gradle.kts").toFile().readText()
        assertTrue(admission.contains("api(project(\":thomas:longitudinal\"))"))
        assertFalse(admission.contains("sqlite"))
        assertTrue(store.contains("api(project(\":thomas:personal-data-persistence\"))"))
        assertTrue(store.contains("implementation(libs.sqlite.jdbc)"))
        assertFalse(root.resolve("thomas/longitudinal/build.gradle.kts").toFile().readText().contains("longitudinal-admission"))
    }

    @Test fun `core policy and presentation modules remain unwired from admission and concrete store`() {
        val files = listOf(
            "thomas/engine/build.gradle.kts", "thomas/safety/build.gradle.kts",
            "platform/renderer-llama-android/build.gradle.kts", "platform/speech-android/build.gradle.kts",
            "thomas/provenance/build.gradle.kts",
        )
        files.forEach { path ->
            val text = root.resolve(path).toFile().readText()
            assertFalse("$path must not consume admission", text.contains(":thomas:longitudinal-admission"))
            assertFalse("$path must not consume store", text.contains(":thomas:longitudinal-store"))
        }
        val productionSources = listOf("thomas/engine", "thomas/safety", "platform/renderer-llama-android", "platform/speech-android")
            .flatMap { root.resolve(it).toFile().walkTopDown().filter { file -> file.isFile && file.extension in setOf("kt", "java") }.toList() }
        assertFalse(productionSources.any { it.readText().contains("longitudinal.store") || it.readText().contains("longitudinal.admission") })
        val integration = listOf("app/src/main", "thomas/runtime/src/main", "platform/persistence-android/src/main")
            .flatMap { root.resolve(it).toFile().walkTopDown().filter { file -> file.isFile && file.extension in setOf("kt", "java") }.toList() }
            .joinToString("\n") { it.readText() }
        listOf("QualificationLongitudinalStore", "java.sql", "jdbc:", "SQLiteDatabase", "RoomDatabase")
            .forEach { assertFalse("production integration exposes concrete store token $it", integration.contains(it)) }
    }

    @Test fun `only qualification module consumes complete store`() {
        val matches = root.toFile().walkTopDown().filter { file ->
            file.isFile && file.name == "build.gradle.kts" && file.readText().contains(":thomas:longitudinal-store")
        }.map { it.relativeTo(root.toFile()).invariantSeparatorsPath }.toList()
        assertEquals(listOf("qualification/build.gradle.kts"), matches)
    }

    @Test fun `public boundary exposes no SQL connection DAO or transaction`() {
        val boundaryTypes = listOf(
            QualificationLongitudinalStore::class.java,
            LongitudinalAdmissionController::class.java,
            LongitudinalReader::class.java,
            AcceptedAdmissionReceipt::class.java,
        )
        boundaryTypes.flatMap { it.methods.toList() }.forEach { method ->
            val typeNames = listOf(method.returnType) + method.parameterTypes
            assertFalse("${method.name} exposes SQL", typeNames.any { it.name.startsWith("java.sql.") || it.simpleName.contains("Dao", true) || it.simpleName.contains("Transaction", true) })
        }
    }

    @Test fun `accepted receipt implementation is private and cannot be named by caller API`() {
        val contracts = root.resolve("thomas/longitudinal-admission/src/main/kotlin/com/conundrum/thomas/v2/longitudinal/store/GovernedPersistencePorts.kt").toFile().readText()
        assertTrue(contracts.contains("interface AcceptedAdmissionReceipt"))
        val implementation = Class.forName("com.conundrum.thomas.v2.longitudinal.store.StoreAcceptedReceipt")
        assertFalse(Modifier.isPublic(implementation.modifiers))
        assertFalse(AcceptedAdmissionReceipt::class.java.declaredConstructors.any { Modifier.isPublic(it.modifiers) })
    }

    @Test fun `only governed submit accepts external structured write request`() {
        val mutationMethods = LongitudinalAdmissionController::class.java.methods.filter { it.declaringClass == LongitudinalAdmissionController::class.java }
        assertEquals(1, mutationMethods.size)
        assertEquals("submit", mutationMethods.single().name)
        assertEquals(LongitudinalAdmissionRequest::class.java, mutationMethods.single().parameterTypes.single())
    }

    @Test fun `ledger rows are immutable under database enforcement`() {
        val database = CTV207TestSupport.path("immutable-ledger")
        val harness = com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness(database)
        CTV207TestSupport.assertAccepted(CTV207TestSupport.admitSource(harness, CTV207TestSupport.sourceDraft("immutable-ledger")))
        harness.closePreservingFile()
        DriverManager.getConnection("jdbc:sqlite:$database").use { connection ->
            assertThrows(java.sql.SQLException::class.java) { connection.createStatement().executeUpdate("UPDATE ledger_event SET request_id='changed' WHERE store_revision=1") }
            assertThrows(java.sql.SQLException::class.java) { connection.createStatement().executeUpdate("DELETE FROM ledger_event WHERE store_revision=1") }
        }
        Files.deleteIfExists(database)
    }

    @Test fun `store source contains no model prompt log or network machinery`() {
        val roots = listOf(root.resolve("thomas/longitudinal-admission/src/main"), root.resolve("thomas/longitudinal-store/src/main"))
        val text = roots.flatMap { it.toFile().walkTopDown().filter { file -> file.isFile }.toList() }.joinToString("\n") { it.readText() }.lowercase()
        listOf("android.", "room", "httpclient", "retrofit", "embedding", "semantic search", "system prompt", "llm", "logger.", "println(").forEach {
            assertFalse("forbidden implementation token $it", text.contains(it))
        }
    }

    @Test fun `safety state has no persisted representation`() {
        val schema = root.resolve("thomas/longitudinal-store/src/main/kotlin/com/conundrum/thomas/v2/longitudinal/store/LongitudinalStoreSchema.kt").toFile().readText().lowercase()
        assertFalse(schema.contains("safety_state"))
        assertFalse(schema.contains("ordinarytherapypermit".lowercase()))
        assertFalse(schema.contains("risk_score"))
    }

    @Test fun `qualification database location rejects ordinary project paths`() {
        assertThrows(IllegalArgumentException::class.java) {
            QualificationStoreLocation.file(root.resolve("not-a-build-directory/store.sqlite"))
        }
        assertNotNull(QualificationStoreLocation.file(root.resolve("qualification/build/allowed.sqlite")))
    }

    @Test fun `Journal Biographer and model authority contracts remain unchanged`() {
        val journal = ThomasModeAuthorityContracts.journal
        val biographer = ThomasModeAuthorityContracts.biographer
        ThomasModeAuthorityContracts.all.forEach {
            assertEquals(LanguageModelDecisionAuthority.NONE, it.languageModelDecisionAuthority)
            assertEquals(DirectProfileMutationAuthority.NONE, it.directProfileMutationAuthority)
        }
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, journal.defaultResponse)
        assertEquals(ModePrimaryFunction.INVESTIGATION, biographer.primaryFunction)
    }

    @Test fun `canonical forward plan and V1 deny register remain sealed`() {
        val plan = root.resolve("docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md")
        assertEquals(25031, Files.size(plan))
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(plan)).joinToString("") { "%02x".format(it) }
        assertEquals("bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886", hash)
        assertFalse(Files.exists(root.resolve("fdp.txt")))
        val register = root.resolve("migration/v1-component-register.json").toFile().readText()
        assertEquals(24, Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count())
        assertTrue(register.contains("\"v1CodeMigrationAuthorized\": false"))
    }

    @Test fun `application backup remains disabled`() {
        val manifest = root.resolve("app/src/main/AndroidManifest.xml").toFile().readText()
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
    }
}
