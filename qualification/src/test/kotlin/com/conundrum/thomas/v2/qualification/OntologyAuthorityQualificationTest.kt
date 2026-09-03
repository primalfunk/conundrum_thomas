package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.DirectProfileMutationAuthority
import com.conundrum.thomas.v2.domain.mode.LanguageModelDecisionAuthority
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.ontology.DefinitionStatus
import com.conundrum.thomas.v2.ontology.RuntimeAuthorizationStatus
import com.conundrum.thomas.v2.ontology.SourceBindingAuthority
import com.conundrum.thomas.v2.ontology.TherapeuticOntologyCatalog
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OntologyAuthorityQualificationTest {
    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))
    private val catalog = TherapeuticOntologyCatalog.snapshot

    private fun text(path: String) = File(repositoryRoot, path).readText()

    @Test
    fun `ontology has no production path to policy safety rendering model or persistence`() {
        val ontologyBuild = text("thomas/ontology/build.gradle.kts")
        val allowedDependencies = Regex("project\\(\"([^\"]+)\"\\)")
            .findAll(ontologyBuild)
            .map { it.groupValues[1] }
            .toSet()
        assertEquals(setOf(":thomas:domain", ":thomas:provenance"), allowedDependencies)

        val forbidden = listOf(
            ":thomas:engine",
            ":thomas:safety",
            ":thomas:runtime",
            ":platform:renderer-llama-android",
            ":platform:persistence-android",
            ":platform:speech-android",
            "com.android",
            "sqlite",
            "Room",
        )
        forbidden.forEach { assertFalse("Ontology reached forbidden dependency $it", ontologyBuild.contains(it)) }

        listOf(
            "thomas/engine/build.gradle.kts",
            "thomas/safety/build.gradle.kts",
            "thomas/runtime/build.gradle.kts",
            "app/build.gradle.kts",
        ).forEach { path ->
            assertFalse("CT-V2-02 ontology must not yet be wired into production: $path", text(path).contains(":thomas:ontology"))
        }
    }

    @Test
    fun `ontology exposes no therapeutic execution classifiers scores or keyword routers`() {
        val source = File(repositoryRoot, "thomas/ontology/src/main").walkTopDown()
            .filter { it.isFile }
            .joinToString("\n") { it.readText() }

        listOf(
            "RiskScore",
            "PredictiveRisk",
            "DiagnosticClassifier",
            "CrisisKeywordRouter",
            "selectIntervention",
            "selectDialogueAct",
            "invokeModel",
            "renderResponse",
            "respondToUser",
        ).forEach { forbidden -> assertFalse("Forbidden authority surface: $forbidden", source.contains(forbidden)) }
    }

    @Test
    fun `no concept intervention safety context or source binding has runtime authority`() {
        assertTrue(catalog.concepts.all { it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED })
        assertTrue(TherapeuticOntologyCatalog.interventionFamilies.all {
            it.definitionStatus == DefinitionStatus.CANDIDATE &&
                it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED
        })
        assertTrue(TherapeuticOntologyCatalog.safetyContexts.all {
            it.definitionStatus == DefinitionStatus.CANDIDATE &&
                it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED
        })
        assertTrue(catalog.sourceBindings.all {
            it.bindingAuthority == SourceBindingAuthority.PROVENANCE_ONLY &&
                it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED
        })
    }

    @Test
    fun `mode contracts grant neither model decisions nor direct profile mutation`() {
        assertTrue(ThomasModeAuthorityContracts.all.all {
            it.languageModelDecisionAuthority == LanguageModelDecisionAuthority.NONE
        })
        assertTrue(ThomasModeAuthorityContracts.all.all {
            it.directProfileMutationAuthority == DirectProfileMutationAuthority.NONE
        })
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, ThomasModeAuthorityContracts.journal.defaultResponse)
    }

    @Test
    fun `V1 migration authority remains zero in ontology phase`() {
        val register = text("migration/v1-component-register.json")
        val componentCount = Regex("\"componentId\"\\s*:").findAll(register).count()
        val deniedCount = Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count()
        assertTrue(componentCount > 0)
        assertEquals(componentCount, deniedCount)
        assertFalse(register.contains("\"eventualMigrationCommit\": \""))
        assertFalse(register.contains("\"v1CodeMigrationAuthorized\": true"))
        assertFalse(register.contains("\"therapeuticImplementationAuthorized\": true"))
    }
}
