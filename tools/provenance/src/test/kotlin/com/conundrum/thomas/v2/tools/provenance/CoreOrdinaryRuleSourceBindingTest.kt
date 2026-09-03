package com.conundrum.thomas.v2.tools.provenance

import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryRuleCatalog
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

class CoreOrdinaryRuleSourceBindingTest {
    @get:Rule val temporaryFolder = TemporaryFolder()
    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))
    private lateinit var database: File

    @Before fun buildGovernedCorpus() {
        database = temporaryFolder.newFile("core-ordinary-source-bindings.sqlite")
        assertTrue(database.delete())
        buildDatabase(File(repositoryRoot, "provenance/migrations"), File(repositoryRoot, "provenance/seeds"), database)
    }

    @Test fun `every clinical core rule resolves exact governed section artifact rights and pending reviews`() = connected { connection ->
        val rules = CoreOrdinaryRuleCatalog.allRules.filter { it.kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD }
        assertEquals(32, rules.size)
        val expectedHashes = mapOf(
            "who-fhs-2025" to "b3dde55d3e1a601699a41b4aaaef4d020cc9416fbc6773848396db81df52cbe9",
            "who-pm-plus-v1-1-2018" to "aaa6ce06dacc1058ba8b695b7d8146d33abfdfcf8a13ea7d888f71a01a833cbe",
        )
        rules.forEach { rule ->
            rule.provenance.filterIsInstance<RuleProvenance.ClinicalSource>().forEach { provenance ->
                val source = provenance.source
                connection.prepareStatement(
                    "SELECT ss.location_value, sl.artifact_sha256, rr.commercial_use_status FROM source_section ss " +
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
                        assertEquals(expectedHashes[source.versionId.value], result.getString("artifact_sha256"))
                        assertEquals("COMMERCIAL_PERMISSION_REQUIRED", result.getString("commercial_use_status"))
                        assertTrue(!result.next())
                    }
                }
                connection.prepareStatement(
                    "SELECT COUNT(*) FROM review_requirement WHERE version_id=? AND status='PENDING' AND review_type IN ('CLINICAL','RIGHTS','SOFTWARE_AUTONOMY')",
                ).use { statement ->
                    statement.setString(1, source.versionId.value)
                    statement.executeQuery().use { result ->
                        assertTrue(result.next())
                        assertEquals(3L, result.getLong(1))
                    }
                }
            }
        }
    }

    @Test fun `standalone decision support remains an unopened discovery need with no authority`() = connected { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT status, no_source_authority, no_rule_authority FROM source_discovery_need WHERE need_id='need-ordinary-standalone-decision-support'").use { result ->
                assertTrue(result.next())
                assertEquals("UNOPENED", result.getString("status"))
                assertEquals(1L, result.getLong("no_source_authority"))
                assertEquals(1L, result.getLong("no_rule_authority"))
                assertTrue(!result.next())
            }
        }
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM review_event"))
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM review_requirement WHERE status='COMPLETE'"))
    }

    private fun connected(block: (Connection) -> Unit) {
        DriverManager.getConnection("jdbc:sqlite:${database.absolutePath}").use { connection ->
            connection.createStatement().execute("PRAGMA foreign_keys = ON")
            block(connection)
        }
    }

    private fun scalar(connection: Connection, sql: String): Long = connection.createStatement().use { statement ->
        statement.executeQuery(sql).use { result -> result.next(); result.getLong(1) }
    }
}
