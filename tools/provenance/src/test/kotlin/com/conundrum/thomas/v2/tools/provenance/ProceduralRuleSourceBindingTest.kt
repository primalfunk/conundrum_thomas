package com.conundrum.thomas.v2.tools.provenance

import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemRuleCatalog
import com.conundrum.thomas.v2.engine.verticalslice.RuleKind
import com.conundrum.thomas.v2.engine.verticalslice.RuleProvenance
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProceduralRuleSourceBindingTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))
    private lateinit var database: File

    @Before
    fun buildGovernedCorpus() {
        database = temporaryFolder.newFile("procedural-rule-source-bindings.sqlite")
        assertTrue(database.delete())
        buildDatabase(
            migrations = File(repositoryRoot, "provenance/migrations"),
            seeds = File(repositoryRoot, "provenance/seeds"),
            output = database,
        )
    }

    @Test
    fun `every source-derived policy provenance resolves to exact version section artifact and pending reviews`() = connected { connection ->
        val sourceRules = BoundedProblemRuleCatalog.rules.filter { it.kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD }
        assertTrue(sourceRules.isNotEmpty())
        sourceRules.forEach { rule ->
            rule.provenance.filterIsInstance<RuleProvenance.ClinicalSource>().forEach { provenance ->
                val source = provenance.source
                connection.prepareStatement(
                    "SELECT ss.location_value, sl.artifact_sha256, rr.commercial_use_status " +
                        "FROM source_section ss " +
                        "JOIN source_locator sl ON sl.locator_id=ss.locator_id AND sl.version_id=ss.version_id " +
                        "JOIN rights_record rr ON rr.version_id=ss.version_id " +
                        "JOIN source_version sv ON sv.version_id=ss.version_id " +
                        "WHERE sv.document_id=? AND sv.version_id=? AND ss.section_id=? AND sl.locator_id=?",
                ).use { statement ->
                    statement.setString(1, source.documentId.value)
                    statement.setString(2, source.versionId.value)
                    statement.setString(3, source.sectionId.value)
                    statement.setString(4, requireNotNull(source.locatorId).value)
                    statement.executeQuery().use { result ->
                        assertTrue("Missing exact governed source for ${rule.id.value}", result.next())
                        assertEquals(provenance.preciseLocator, result.getString("location_value"))
                        assertTrue(result.getString("artifact_sha256").matches(Regex("[0-9a-f]{64}")))
                        assertEquals("COMMERCIAL_PERMISSION_REQUIRED", result.getString("commercial_use_status"))
                        assertTrue("Ambiguous exact source for ${rule.id.value}", !result.next())
                    }
                }
                connection.prepareStatement(
                    "SELECT COUNT(*) FROM review_requirement WHERE version_id=? AND status='PENDING' " +
                        "AND review_type IN ('CLINICAL','RIGHTS')",
                ).use { statement ->
                    statement.setString(1, source.versionId.value)
                    statement.executeQuery().use { result ->
                        assertTrue(result.next())
                        assertEquals(2L, result.getLong(1))
                    }
                }
            }
        }
    }

    @Test
    fun `exact CT-V2-03 sections remain source metadata and grant no database rule authority`() = connected { connection ->
        assertEquals(11L, scalar(connection, "SELECT COUNT(*) FROM source_section WHERE location_notes LIKE 'Exact location inspected%'") )
        assertEquals(
            emptyList<String>(),
            queryStrings(
                connection,
                "SELECT name FROM sqlite_master WHERE type='table' AND name IN " +
                    "('therapeutic_rule','policy_rule','runtime_authority','safety_algorithm') ORDER BY name",
            ),
        )
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM review_event"))
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM review_requirement WHERE status='COMPLETE'"))
    }

    private fun connected(block: (Connection) -> Unit) {
        DriverManager.getConnection("jdbc:sqlite:${database.absolutePath}").use { connection ->
            connection.createStatement().execute("PRAGMA foreign_keys = ON")
            block(connection)
        }
    }

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
