package com.conundrum.thomas.v2.tools.provenance

import com.conundrum.thomas.v2.ontology.BindingConflictState
import com.conundrum.thomas.v2.ontology.GovernanceReviewStatus
import com.conundrum.thomas.v2.ontology.RuntimeAuthorizationStatus
import com.conundrum.thomas.v2.ontology.SourceBindingAuthority
import com.conundrum.thomas.v2.ontology.TherapeuticOntologyCatalog
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OntologySourceBindingTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))
    private lateinit var database: File

    @Before
    fun buildGovernedCorpus() {
        database = temporaryFolder.newFile("ontology-source-bindings.sqlite")
        assertTrue(database.delete())
        buildDatabase(
            migrations = File(repositoryRoot, "provenance/migrations"),
            seeds = File(repositoryRoot, "provenance/seeds"),
            output = database,
        )
    }

    @Test
    fun `every ontology binding resolves through exact governed source identity`() = connected { connection ->
        TherapeuticOntologyCatalog.sourceBindings.forEach { binding ->
            val source = binding.source
            connection.prepareStatement(
                "SELECT sa.authority_domain, sv.review_state, rr.commercial_use_status " +
                    "FROM source_document sd " +
                    "JOIN source_authority sa ON sa.authority_id=sd.authority_id " +
                    "JOIN source_version sv ON sv.document_id=sd.document_id " +
                    "JOIN rights_record rr ON rr.version_id=sv.version_id " +
                    "WHERE sd.document_id=? AND sv.version_id=?",
            ).use { statement ->
                statement.setString(1, source.documentId.value)
                statement.setString(2, source.versionId.value)
                statement.executeQuery().use { result ->
                    assertTrue("Missing source for ${binding.bindingId}", result.next())
                    assertEquals(binding.sourceAuthorityDomain.name, result.getString("authority_domain"))
                    assertEquals(binding.sourceIdentityReviewState.name, result.getString("review_state"))
                    assertEquals(binding.commercialUseStatus.name, result.getString("commercial_use_status"))
                    assertFalse("Ambiguous source identity for ${binding.bindingId}", result.next())
                }
            }

            connection.prepareStatement(
                "SELECT COUNT(*) FROM source_section ss " +
                    "JOIN source_locator sl ON sl.locator_id=ss.locator_id " +
                    "WHERE ss.section_id=? AND ss.version_id=? AND sl.locator_id=? AND sl.version_id=?",
            ).use { statement ->
                statement.setString(1, source.sectionId.value)
                statement.setString(2, source.versionId.value)
                statement.setString(3, requireNotNull(source.locatorId).value)
                statement.setString(4, source.versionId.value)
                statement.executeQuery().use { result ->
                    assertTrue(result.next())
                    assertEquals("Invalid section/locator chain for ${binding.bindingId}", 1L, result.getLong(1))
                }
            }
        }
    }

    @Test
    fun `binding review gates match pending corpus requirements`() = connected { connection ->
        TherapeuticOntologyCatalog.sourceBindings.forEach { binding ->
            assertEquals(GovernanceReviewStatus.PENDING, binding.clinicalReviewStatus)
            assertEquals(GovernanceReviewStatus.PENDING, binding.rightsReviewStatus)

            listOf(
                requireNotNull(binding.clinicalReviewRequirementId).value to "CLINICAL",
                requireNotNull(binding.rightsReviewRequirementId).value to "RIGHTS",
            ).forEach { (requirementId, reviewType) ->
                connection.prepareStatement(
                    "SELECT COUNT(*) FROM review_requirement " +
                        "WHERE requirement_id=? AND version_id=? AND review_type=? AND status='PENDING'",
                ).use { statement ->
                    statement.setString(1, requirementId)
                    statement.setString(2, binding.source.versionId.value)
                    statement.setString(3, reviewType)
                    statement.executeQuery().use { result ->
                        assertTrue(result.next())
                        assertEquals("Review mismatch for ${binding.bindingId}", 1L, result.getLong(1))
                    }
                }
            }
        }
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM review_requirement WHERE status='COMPLETE'"))
        assertEquals(0L, scalar(connection, "SELECT COUNT(*) FROM review_event"))
    }

    @Test
    fun `recorded conflict references remain unresolved or explicitly different scope`() = connected { connection ->
        TherapeuticOntologyCatalog.sourceBindings.forEach { binding ->
            binding.conflictIds.forEach { conflictId ->
                connection.prepareStatement("SELECT status FROM source_conflict WHERE conflict_id=?").use { statement ->
                    statement.setString(1, conflictId.value)
                    statement.executeQuery().use { result ->
                        assertTrue("Missing conflict ${conflictId.value}", result.next())
                        val expected = when (binding.conflictState) {
                            BindingConflictState.OPEN, BindingConflictState.UNRESOLVED -> "OPEN"
                            BindingConflictState.DIFFERENT_SCOPE_RECORDED -> "NOT_A_CONFLICT_DIFFERENT_SCOPE"
                            BindingConflictState.NONE_RECORDED -> error("A no-conflict binding cannot carry conflict IDs")
                        }
                        assertEquals(expected, result.getString("status"))
                    }
                }
            }
        }
    }

    @Test
    fun `source linkage grants neither rule nor runtime authority`() = connected { connection ->
        assertTrue(TherapeuticOntologyCatalog.sourceBindings.isNotEmpty())
        assertTrue(TherapeuticOntologyCatalog.sourceBindings.all {
            it.bindingAuthority == SourceBindingAuthority.PROVENANCE_ONLY &&
                it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED
        })
        assertEquals(
            emptyList<String>(),
            queryStrings(
                connection,
                "SELECT name FROM sqlite_master WHERE type='table' AND name IN " +
                    "('therapeutic_rule','policy_rule','safety_algorithm','diagnostic_classifier') ORDER BY name",
            ),
        )
        assertTrue(scalar(connection, "SELECT COUNT(*) FROM candidate_subject WHERE no_rule_authority=1") > 0)
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
