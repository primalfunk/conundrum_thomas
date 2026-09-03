package com.conundrum.thomas.v2.tools.provenance

import com.conundrum.thomas.v2.safety.SafetyRuleKind
import com.conundrum.thomas.v2.safety.SafetyRuleProvenance
import com.conundrum.thomas.v2.safety.SafetyScopeRuleCatalog
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SafetyRuleSourceBindingTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))
    private lateinit var database: File

    @Before
    fun buildGovernedCorpus() {
        database = temporaryFolder.newFile("safety-rule-source-bindings.sqlite")
        assertTrue(database.delete())
        buildDatabase(
            migrations = File(repositoryRoot, "provenance/migrations"),
            seeds = File(repositoryRoot, "provenance/seeds"),
            output = database,
        )
    }

    @Test
    fun `every clinical safety rule resolves exact document version section artifact rights reviews and conflict`() = connected { connection ->
        val clinicalRules = SafetyScopeRuleCatalog.rules.filter { it.kind != SafetyRuleKind.ENGINEERING_AUTHORITY_GUARD }
        assertEquals(1, clinicalRules.size)
        clinicalRules.forEach { rule ->
            rule.provenance.filterIsInstance<SafetyRuleProvenance.ClinicalSource>().forEach { provenance ->
                val source = provenance.source
                connection.prepareStatement(
                    "SELECT ss.location_value, rr.commercial_use_status FROM source_section ss " +
                        "JOIN source_version sv ON sv.version_id=ss.version_id " +
                        "JOIN rights_record rr ON rr.version_id=sv.version_id " +
                        "WHERE sv.document_id=? AND sv.version_id=? AND ss.section_id=? AND ss.locator_id=?",
                ).use { statement ->
                    statement.setString(1, source.documentId.value)
                    statement.setString(2, source.versionId.value)
                    statement.setString(3, source.sectionId.value)
                    statement.setString(4, requireNotNull(source.locatorId).value)
                    statement.executeQuery().use { result ->
                        assertTrue("Missing governed safety section for ${rule.id.value}", result.next())
                        assertEquals("1.6.5-1.6.6", result.getString("location_value"))
                        assertEquals("LEGAL_REVIEW_REQUIRED", result.getString("commercial_use_status"))
                        assertTrue(!result.next())
                    }
                }
                connection.prepareStatement(
                    "SELECT artifact_sha256 FROM source_locator WHERE version_id=? AND locator_id=?",
                ).use { statement ->
                    statement.setString(1, source.versionId.value)
                    statement.setString(2, requireNotNull(provenance.immutableArtifactLocatorId).value)
                    statement.executeQuery().use { result ->
                        assertTrue(result.next())
                        assertEquals("869813027e9ff30351a26ae9ceda614ec3cf3f0dc553384be1fb8087405d35c2", result.getString(1))
                    }
                }
                connection.prepareStatement(
                    "SELECT review_type FROM review_requirement WHERE version_id=? AND status='PENDING' ORDER BY review_type",
                ).use { statement ->
                    statement.setString(1, source.versionId.value)
                    statement.executeQuery().use { result ->
                        val types = buildList { while (result.next()) add(result.getString(1)) }
                        assertEquals(listOf("CLINICAL", "LEGAL", "RIGHTS", "SOFTWARE_AUTONOMY"), types)
                    }
                }
                connection.prepareStatement(
                    "SELECT status FROM source_conflict WHERE conflict_id=? AND left_version_id='nimh-asq-tool-2025' AND right_version_id=?",
                ).use { statement ->
                    statement.setString(1, provenance.conflictIds.single().value)
                    statement.setString(2, source.versionId.value)
                    statement.executeQuery().use { result ->
                        assertTrue(result.next())
                        assertEquals("NOT_A_CONFLICT_DIFFERENT_SCOPE", result.getString(1))
                    }
                }
            }
        }
    }

    @Test
    fun `provenance corpus grants no safety runtime or rule authority`() = connected { connection ->
        val names = connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name IN " +
                    "('safety_rule','risk_score','screening_instrument_runtime','runtime_authority') ORDER BY name",
            ).use { result -> buildList { while (result.next()) add(result.getString(1)) } }
        }
        assertEquals(emptyList<String>(), names)
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
}
