package com.conundrum.thomas.v2.tools.provenance

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProvenanceDatabaseTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))
    private lateinit var database: File

    @Before
    fun buildFromZero() {
        database = temporaryFolder.newFile("qualified-provenance.sqlite")
        assertTrue(database.delete())
        buildDatabase(
            migrations = File(repositoryRoot, "provenance/migrations"),
            seeds = File(repositoryRoot, "provenance/seeds"),
            output = database,
        )
    }

    @Test
    fun `schema migrates from zero and records input checksums`() = connected { connection ->
        assertEquals(1L, count(connection, "schema_migration"))
        assertEquals(10L, count(connection, "seed_application"))
        assertTrue(queryStrings(connection, "SELECT sha256 FROM schema_migration").all { it.matches(Regex("[0-9a-f]{64}")) })
    }

    @Test
    fun `document versions classifications rights and authorities are complete`() = connected { connection ->
        assertEquals(19L, count(connection, "source_document"))
        assertEquals(21L, count(connection, "source_version"))
        assertEquals(0L, count(connection, "source_version_without_class"))
        assertEquals(0L, count(connection, "source_version_without_freshness"))
        assertEquals(21L, count(connection, "rights_record"))
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM source_document WHERE authority_id IS NULL"))
        assertEquals(8L, scalar(connection, "SELECT COUNT(*) FROM source_class"))
    }

    @Test
    fun `document and version identities are unique`() = connected { connection ->
        assertThrows(SQLException::class.java) {
            connection.createStatement().execute(
                "INSERT INTO source_document SELECT * FROM source_document WHERE document_id='who-live-life'",
            )
        }
        assertThrows(SQLException::class.java) {
            connection.createStatement().execute(
                "INSERT INTO source_version SELECT * FROM source_version WHERE version_id='who-live-life-2021'",
            )
        }
    }

    @Test
    fun `acquired artifact hashes are exact and immutable`() = connected { connection ->
        assertEquals(18L, scalar(connection, "SELECT COUNT(*) FROM source_locator WHERE locator_kind='OFFICIAL_ARTIFACT'"))
        assertEquals(
            0L,
            scalar(
                connection,
                "SELECT COUNT(*) FROM source_locator WHERE locator_kind='OFFICIAL_ARTIFACT' " +
                    "AND (length(artifact_sha256)<>64 OR artifact_byte_size<=0 OR cache_file_name IS NULL)",
            ),
        )
        assertThrows(SQLException::class.java) {
            connection.createStatement().executeUpdate(
                "UPDATE source_locator SET artifact_sha256='" + "0".repeat(64) + "' WHERE locator_id='artifact-who-fhs'",
            )
        }
    }

    @Test
    fun `multiple versions and supersession are traversable`() = connected { connection ->
        assertEquals(2L, scalar(connection, "SELECT COUNT(*) FROM source_version WHERE document_id='who-mhgap-guideline'"))
        assertEquals(2L, scalar(connection, "SELECT COUNT(*) FROM source_version WHERE document_id='who-mhgap-intervention-guide'"))
        val chain = queryStrings(
            connection,
            "WITH RECURSIVE successors(version_id) AS (" +
                "SELECT 'who-mhgap-guideline-2015' UNION ALL " +
                "SELECT r.to_version_id FROM source_version_relationship r JOIN successors s ON r.from_version_id=s.version_id " +
                "WHERE r.relationship_type='SUPERSEDED_BY') SELECT version_id FROM successors ORDER BY version_id",
        )
        assertEquals(listOf("who-mhgap-guideline-2015", "who-mhgap-guideline-2023"), chain)
    }

    @Test
    fun `conflicts and exact source locations are represented`() = connected { connection ->
        assertTrue(scalar(connection, "SELECT COUNT(*) FROM source_conflict WHERE status='OPEN'") >= 1)
        assertEquals(
            1L,
            scalar(
                connection,
                "SELECT COUNT(*) FROM source_section WHERE version_id='nice-ng225-2025' " +
                    "AND location_type='RECOMMENDATION' AND location_value='1.6.1-1.6.4'",
            ),
        )
        assertTrue(scalar(connection, "SELECT COUNT(*) FROM candidate_subject WHERE no_rule_authority=1") >= 10)
    }

    @Test
    fun `review transitions are constrained and clinical queue is unclaimed`() = connected { connection ->
        assertEquals(0L, count(connection, "clinical_version_without_pending_review"))
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM review_requirement WHERE status='COMPLETE'"))
        assertEquals(0L, count(connection, "review_event"))
        assertThrows(SQLException::class.java) {
            connection.createStatement().execute(
                "INSERT INTO review_event(event_id,version_id,sequence_number,from_state,to_state,reviewer_role,reviewer_identity,reviewed_at,disposition) " +
                    "VALUES('synthetic-invalid','who-live-life-2021',1,'DISCOVERED','APPROVED_AS_SOURCE','synthetic-test','synthetic-test','2026-09-03','synthetic-test')",
            )
        }
    }

    @Test
    fun `missing rights and freshness are rejected by corpus validation`() = connected { connection ->
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM rights_record WHERE license_identifier IS NULL OR commercial_use_status IS NULL"))
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM source_freshness WHERE next_review_due IS NULL OR official_source_url IS NULL"))
        assertTrue(scalar(connection, "SELECT COUNT(*) FROM rights_record WHERE rights_review_required=1") >= 1)
    }

    @Test
    fun `raw cache is excluded and production cannot depend on it`() {
        val rootIgnore = File(repositoryRoot, ".gitignore").readText()
        val provenanceIgnore = File(repositoryRoot, "provenance/.gitignore").readText()
        assertTrue(rootIgnore.contains("/provenance/raw/"))
        assertTrue(provenanceIgnore.contains("/raw/"))

        val productionBuilds = listOf(
            "app/build.gradle.kts",
            "thomas/domain/build.gradle.kts",
            "thomas/provenance/build.gradle.kts",
            "thomas/engine/build.gradle.kts",
            "thomas/safety/build.gradle.kts",
            "thomas/runtime/build.gradle.kts",
        )
        productionBuilds.forEach { path ->
            val text = File(repositoryRoot, path).readText()
            assertFalse("$path cannot depend on provenance tooling", text.contains(":tools:provenance"))
            assertFalse("$path cannot consume raw sources", text.contains("provenance/raw"))
        }
    }

    @Test
    fun `when local acquisition cache exists it matches every recorded artifact`() = connected { connection ->
        val raw = File(repositoryRoot, "provenance/raw")
        if (!raw.isDirectory) return@connected

        connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT cache_file_name, artifact_byte_size, artifact_sha256 FROM source_locator " +
                    "WHERE locator_kind='OFFICIAL_ARTIFACT' ORDER BY cache_file_name",
            ).use { result ->
                var checked = 0
                while (result.next()) {
                    val artifact = File(raw, result.getString(1))
                    assertTrue("Missing acquired artifact ${artifact.name}", artifact.isFile)
                    assertEquals(result.getLong(2), artifact.length())
                    assertEquals(result.getString(3), sha256(artifact.readBytes()))
                    assertEquals("%PDF-", artifact.inputStream().use { String(it.readNBytes(5), Charsets.US_ASCII) })
                    checked++
                }
                assertEquals(18, checked)
            }
        }
    }

    @Test
    fun `provenance core stays platform independent and behavior free`() {
        val build = File(repositoryRoot, "thomas/provenance/build.gradle.kts").readText()
        assertFalse(build.contains("com.android"))
        assertFalse(build.contains("sqlite"))
        assertFalse(build.contains("Room"))

        val productionSources = File(repositoryRoot, "thomas/provenance/src/main").walkTopDown()
            .filter { it.isFile }
            .joinToString("\n") { it.readText() }
        listOf("ifUserSays", "TherapeuticRule", "SafetyAlgorithm", "InterventionSelector")
            .forEach { forbidden -> assertFalse(productionSources.contains(forbidden)) }
    }

    private fun connected(block: (Connection) -> Unit) {
        DriverManager.getConnection("jdbc:sqlite:${database.absolutePath}").use { connection ->
            connection.createStatement().execute("PRAGMA foreign_keys = ON")
            block(connection)
        }
    }

    private fun count(connection: Connection, table: String): Long = scalar(connection, "SELECT COUNT(*) FROM $table")

    private fun scalar(connection: Connection, sql: String): Long =
        connection.createStatement().use { statement ->
            statement.executeQuery(sql).use { result ->
                result.next()
                result.getLong(1)
            }
        }

    private fun queryStrings(connection: Connection, sql: String): List<String> =
        connection.createStatement().use { statement ->
            statement.executeQuery(sql).use { result ->
                buildList {
                    while (result.next()) add(result.getString(1))
                }
            }
        }
}
