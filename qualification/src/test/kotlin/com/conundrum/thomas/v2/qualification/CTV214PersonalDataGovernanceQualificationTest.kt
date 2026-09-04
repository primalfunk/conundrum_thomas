package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.personaldata.CT_V2_14_BACKUP_FORMAT_VERSION
import com.conundrum.thomas.v2.personaldata.CT_V2_14_MIN_SUPPORTED_SCHEMA_VERSION
import com.conundrum.thomas.v2.personaldata.CT_V2_14_STORE_SCHEMA_VERSION
import com.conundrum.thomas.v2.personaldata.PersonalDataRetentionPolicy
import com.conundrum.thomas.v2.personaldata.RetentionCategory
import java.io.File
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CTV214PersonalDataGovernanceQualificationTest {
    private val root = Path.of(requireNotNull(System.getProperty("thomas.repositoryRoot")))
    private fun text(path: String) = root.resolve(path).toFile().readText()

    @Test fun `schema and backup formats are explicit and versioned`() {
        assertEquals(2, CT_V2_14_STORE_SCHEMA_VERSION)
        assertEquals(1, CT_V2_14_MIN_SUPPORTED_SCHEMA_VERSION)
        assertEquals(1, CT_V2_14_BACKUP_FORMAT_VERSION)
    }

    @Test fun `single production capable root remains outside app composition`() {
        val platform = text("platform/persistence-android/src/main/kotlin/com/conundrum/thomas/v2/platform/persistence/AndroidPersonalDataPersistenceFactory.kt")
        assertEquals(1, Regex("ProtectedPersonalDataStoreFactory[.]open[(]").findAll(platform).count())
        assertFalse(text("app/build.gradle.kts").contains(":platform:persistence-android"))
        assertFalse(text("app/build.gradle.kts").contains(":thomas:personal-data-persistence"))
        assertFalse(javaSources("app").contains("personaldata"))
    }

    @Test fun `Android artifact uses no backup storage and atomic replacement`() {
        val storage = text("platform/persistence-android/src/main/kotlin/com/conundrum/thomas/v2/platform/persistence/AndroidAtomicProtectedArtifactStorage.kt")
        assertTrue(storage.contains("noBackupFilesDir"))
        assertTrue(storage.contains("AtomicFile"))
        assertTrue(storage.contains("finishWrite"))
        assertTrue(storage.contains("failWrite"))
        assertFalse(storage.contains("filesDir"))
    }

    @Test fun `Android key boundary uses platform AES GCM and nonexportable alias`() {
        val key = text("platform/persistence-android/src/main/kotlin/com/conundrum/thomas/v2/platform/persistence/AndroidKeystorePersonalDataKeyProvider.kt")
        assertTrue(key.contains("AndroidKeyStore"))
        assertTrue(key.contains("KEY_ALGORITHM_AES"))
        assertTrue(key.contains("BLOCK_MODE_GCM"))
        assertTrue(key.contains("setKeySize(256)"))
        assertTrue(key.contains("setRandomizedEncryptionRequired(true)"))
        assertTrue(key.contains("setUnlockedDeviceRequired(true)"))
        assertFalse(key.contains("encoded"))
    }

    @Test fun `generic Android backup and transfer remain denied`() {
        val manifest = text("app/src/main/AndroidManifest.xml")
        val legacy = text("app/src/main/res/xml/backup_rules.xml")
        val current = text("app/src/main/res/xml/data_extraction_rules.xml")
        assertTrue(manifest.contains("android:allowBackup=" + 34.toChar() + "false" + 34.toChar()))
        assertFalse(legacy.contains("<include"))
        assertFalse(current.contains("<include"))
        assertTrue(Regex("<exclude ").findAll(legacy).count() >= 8)
        assertTrue(Regex("<exclude ").findAll(current).count() >= 16)
    }

    @Test fun `persistent store has no SQL Room model network or logging authority`() {
        val code = javaSources("thomas/personal-data-persistence").lowercase()
        listOf("java.sql", "jdbc:", "androidx.room", "retrofit", "okhttp", "llama", "println(", "logger.").forEach {
            assertFalse("forbidden persistence token $it", code.contains(it))
        }
        val platform = javaSources("platform/persistence-android").lowercase()
        listOf("java.sql", "jdbc:", "androidx.room", "retrofit", "okhttp", "llama", "println(").forEach {
            assertFalse("forbidden platform token $it", platform.contains(it))
        }
    }

    @Test fun `renderer and realizer have zero persistence access`() {
        val renderer = javaSources("thomas/language-renderer")
        val adapter = javaSources("platform/renderer-llama-android")
        listOf(renderer, adapter).forEach {
            assertFalse(it.contains("com.conundrum.thomas.v2.personaldata"))
            assertFalse(it.contains("LongitudinalAdmissionController"))
            assertFalse(it.contains("ProtectedPersonalDataStore"))
            assertFalse(it.contains("java.sql"))
        }
    }

    @Test fun `retention categories make transient and prohibited artifacts explicit`() {
        val rules = PersonalDataRetentionPolicy.rules
        assertEquals(RetentionCategory.NEVER_PERSISTED, rules.getValue("raw-speech-audio").category)
        assertEquals(RetentionCategory.NEVER_PERSISTED, rules.getValue("renderer-candidate").category)
        assertEquals(RetentionCategory.NEVER_PERSISTED, rules.getValue("model-output").category)
        assertEquals(RetentionCategory.EPHEMERAL, rules.getValue("context-packet").category)
        assertEquals(RetentionCategory.RECOMPUTABLE, rules.getValue("derived-state").category)
        assertEquals(RetentionCategory.REVISION_RETAINED, rules.getValue("source-revision-history").category)
    }

    @Test fun `sensitive local artifact extensions are denied by Git`() {
        val ignored = text(".gitignore")
        listOf("*.db", "*.sqlite", "*.ctpd", "*.ctbackup", "*.ctexport", "*.key", "*.keystore").forEach {
            assertTrue("missing ignore rule $it", ignored.contains(it))
        }
        val tracked = git("ls-files")
        val forbidden = setOf("db", "sqlite", "sqlite3", "ctpd", "ctbackup", "ctexport", "key", "keystore", "jks")
        assertTrue(tracked.lineSequence().filter { it.isNotBlank() }.none { File(it).extension.lowercase() in forbidden })
    }

    @Test fun `V1 deny register and forward plan remain sealed`() {
        val register = text("migration/v1-component-register.json")
        assertEquals(24, register.lineSequence().count { it.contains("approvalState") && it.contains("DENIED") })
        assertFalse(register.contains("APPROVED_FOR_MIGRATION"))
        assertEquals(25031L, root.resolve("docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md").toFile().length())
        assertFalse(root.resolve("fdp.txt").toFile().exists())
    }

    @Test fun `required personal data governance documents are present`() {
        listOf(
            "docs/work-orders/CT-V2-14-PERSONAL-DATA-GOVERNANCE-AND-PERSISTENCE-QUALIFICATION.md",
            "docs/CT-V2-14-PERSONAL-DATA-GOVERNANCE-AND-PERSISTENCE-QUALIFICATION.md",
            "docs/qualification/CT-V2-14-QUALIFICATION.md",
            "docs/persistence/CT-V2-14-PERSISTENT-STORE-ARCHITECTURE.md",
            "docs/persistence/CT-V2-14-ENCRYPTION-AND-KEY-BOUNDARY.md",
            "docs/persistence/CT-V2-14-DATA-LIFECYCLE-AND-RETENTION.md",
            "docs/persistence/CT-V2-14-BACKUP-EXPORT-AND-RESTORE.md",
            "docs/persistence/CT-V2-14-MIGRATION-CORRUPTION-AND-RECOVERY.md",
            "docs/security/CT-V2-14-PERSONAL-DATA-THREAT-MODEL.md",
            "docs/adr/0020-protected-personal-data-persistence.md",
        ).forEach { assertTrue("missing required document $it", root.resolve(it).toFile().isFile) }
    }

    private fun javaSources(path: String): String = root.resolve(path).toFile().walkTopDown()
        .filter { it.isFile && it.extension in setOf("kt", "java") && "build" !in it.toPath().map(Path::toString) }
        .joinToString("\n") { it.readText() }

    private fun git(vararg arguments: String): String {
        val process = ProcessBuilder(listOf("git", "-C", root.toString()) + arguments).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        assertEquals(output, 0, process.waitFor())
        return output
    }
}
