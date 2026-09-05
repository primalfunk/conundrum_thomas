package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.LanguageModelDecisionAuthority
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.ontology.GovernanceReviewStatus
import com.conundrum.thomas.v2.ontology.RuntimeAuthorizationStatus
import com.conundrum.thomas.v2.ontology.TherapeuticOntologyCatalog
import com.conundrum.thomas.v2.safety.ProductionSafetyAuthority
import com.conundrum.thomas.v2.safety.SafetyDecisionExplainer
import com.conundrum.thomas.v2.safety.SafetyRuleExecutionAuthority
import com.conundrum.thomas.v2.safety.SafetyRuleKind
import com.conundrum.thomas.v2.safety.SafetyRuleProvenance
import com.conundrum.thomas.v2.safety.SafetyRuleReviewReport
import com.conundrum.thomas.v2.safety.SafetyScopeGate
import com.conundrum.thomas.v2.safety.SafetyScopeRuleCatalog
import com.conundrum.thomas.v2.safety.ScreeningInstrumentImplementationStatus
import com.conundrum.thomas.v2.safety.ScreeningInstrumentRegistry
import com.conundrum.thomas.v2.safety.ScreeningInstrumentRuntimeAuthority
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyScopeInvariantTest {
    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))

    @Test
    fun `all safety rules have provenance and qualification-only authority`() {
        val rules = SafetyScopeRuleCatalog.rules
        assertEquals(14, rules.size)
        assertEquals(1, rules.count { it.kind == SafetyRuleKind.SOURCE_DERIVED_SCOPE_BOUNDARY })
        assertEquals(13, rules.count { it.kind == SafetyRuleKind.ENGINEERING_AUTHORITY_GUARD })
        assertTrue(rules.all { it.provenance.isNotEmpty() })
        assertTrue(rules.all { it.executionAuthority == SafetyRuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION })
        assertTrue(rules.all { it.productionAuthority == ProductionSafetyAuthority.NOT_GRANTED })
    }

    @Test
    fun `clinical source boundary retains every pending review and recorded scope conflict`() {
        val provenance = SafetyScopeRuleCatalog.rules
            .single { it.kind == SafetyRuleKind.SOURCE_DERIVED_SCOPE_BOUNDARY }
            .provenance.single() as SafetyRuleProvenance.ClinicalSource
        assertEquals("nice-ng225-2025", provenance.source.versionId.value)
        assertEquals("section-ng225-assessment-focus", provenance.source.sectionId.value)
        assertEquals("artifact-nice-ng225", provenance.immutableArtifactLocatorId?.value)
        assertEquals(GovernanceReviewStatus.PENDING, provenance.clinicalReviewStatus)
        assertEquals(GovernanceReviewStatus.PENDING, provenance.rightsReviewStatus)
        assertEquals(GovernanceReviewStatus.PENDING, provenance.legalReviewStatus)
        assertEquals(GovernanceReviewStatus.PENDING, provenance.softwareAutonomyReviewStatus)
        assertEquals(setOf("scope-asq-vs-ng225"), provenance.conflictIds.map { it.value }.toSet())
    }

    @Test
    fun `missing provenance prevents construction of a source-derived safety rule`() {
        val rule = SafetyScopeRuleCatalog.rules.single { it.kind == SafetyRuleKind.SOURCE_DERIVED_SCOPE_BOUNDARY }
        assertThrows(IllegalArgumentException::class.java) { rule.copy(provenance = emptyList()) }
    }

    @Test
    fun `screening instrument remains bibliographic reference only`() {
        assertEquals(1, ScreeningInstrumentRegistry.references.size)
        assertEquals(0, ScreeningInstrumentRegistry.implementedCount)
        assertEquals(0, ScreeningInstrumentRegistry.runtimeAuthorizedCount)
        assertTrue(ScreeningInstrumentRegistry.references.all {
            it.implementationStatus == ScreeningInstrumentImplementationStatus.NOT_IMPLEMENTED &&
                it.runtimeAuthority == ScreeningInstrumentRuntimeAuthority.NOT_GRANTED
        })
        val fields = ScreeningInstrumentRegistry.references.single()::class.java.declaredFields.map { it.name }.toSet()
        assertFalse("items" in fields)
        assertFalse("questions" in fields)
        assertFalse("score" in fields)
        assertFalse("threshold" in fields)
    }

    @Test
    fun `review report exposes source scope consequence conflicts and pending decisions`() {
        val reports = SafetyRuleReviewReport.records()
        assertEquals(14, reports.size)
        val clinical = reports.single { it.ruleId.value == "ctv204-r003-self-harm-specialized-boundary" }
        assertTrue(clinical.sourceLocator.contains("1.6.5-1.6.6"))
        assertTrue(clinical.population.contains("self-harmed"))
        assertTrue(clinical.conflicts.contains("scope-asq-vs-ng225"))
        assertTrue(clinical.pendingReviews.size >= 7)
    }

    @Test
    fun `explainability artifact exposes rule authority and next requirement`() {
        val decision = SafetyScopeGate().govern(
            QualificationSafetyGate.ordinaryInput("explain-safety").copy(
                selfHarmRelevance = com.conundrum.thomas.v2.safety.SafetyEvidence.unknown(),
            ),
        )
        val compact = SafetyDecisionExplainer.compact(decision)
        assertTrue(compact.contains("ctv204-r013-missing-evidence-clarification"))
        assertTrue(compact.contains("CLARIFICATION_REQUIRED"))
        assertTrue(compact.contains("SELF_HARM_RELEVANCE"))
        assertTrue(decision.rejectedRules.isNotEmpty())
    }

    @Test
    fun `safety types contain no numeric risk score band prediction or diagnostic classifier`() {
        val inputFields = com.conundrum.thomas.v2.safety.SafetyScopeInput::class.java.declaredFields
            .filterNot { it.isSynthetic }
        assertTrue(inputFields.none { it.type in setOf(Double::class.java, Float::class.java) })
        val source = File(repositoryRoot, "thomas/safety/src/main").walkTopDown()
            .filter { it.isFile }
            .joinToString("\n") { it.readText() }
        listOf(
            "SuicideProbability",
            "ViolenceProbability",
            "NumericRiskScore",
            "RiskBand",
            "DiagnosticClassifier",
            "CrisisKeywordRouter",
            "CrisisRegexRouter",
            "LanguageModelSafetyDecision",
            "LanguageModelTherapeuticDecision",
            "mutateProfile",
            "promoteToDurableFact",
        ).forEach { forbidden -> assertFalse("Forbidden authority surface: $forbidden", source.contains(forbidden)) }
        assertFalse(source.contains("kotlin.random"))
        assertFalse(source.contains("java.net"))
    }

    @Test
    fun `ordinary permit constructor is internal and only the CT-V2-15 runtime invokes the safety gate`() {
        val permitSource = File(repositoryRoot, "thomas/safety/src/main/kotlin/com/conundrum/thomas/v2/safety/SafetyScopeGate.kt").readText()
        assertTrue(permitSource.contains("class OrdinaryTherapyPermit internal constructor"))
        val productionPaths = listOf("app", "platform/persistence-android", "platform/renderer-llama-android", "platform/speech-android")
        productionPaths.forEach { path ->
            val material = File(repositoryRoot, path).walkTopDown()
                .onEnter { it.name != "build" }
                .filter { it.isFile && (it.extension == "kt" || it.name.endsWith(".gradle.kts")) }
                .joinToString("\n") { it.readText() }
            assertFalse("$path invokes the safety gate", material.contains("SafetyScopeGate"))
            assertFalse("$path invokes ordinary qualification policy", material.contains("BoundedProblemPolicyEvaluator"))
            assertFalse("$path depends on qualification", material.contains(":qualification"))
        }
        val runtime = File(repositoryRoot, "thomas/runtime/src/main").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .joinToString("\n") { it.readText() }
        assertEquals(2, Regex("SafetyScopeGate").findAll(runtime).count())
        assertFalse(runtime.contains("BoundedProblemPolicyEvaluator"))
    }

    @Test
    fun `mode ontology Journal and V1 authority denials remain intact`() {
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, ThomasModeAuthorityContracts.journal.defaultResponse)
        assertTrue(ThomasModeAuthorityContracts.all.all { it.languageModelDecisionAuthority == LanguageModelDecisionAuthority.NONE })
        assertTrue(TherapeuticOntologyCatalog.snapshot.concepts.all { it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED })
        val register = File(repositoryRoot, "migration/v1-component-register.json").readText()
        val componentCount = Regex("\"componentId\"\\s*:").findAll(register).count()
        val deniedCount = Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count()
        assertEquals(componentCount, deniedCount)
        assertFalse(register.contains("\"eventualMigrationCommit\": \""))
    }

    @Test
    fun `no model decision or runtime concept authority is introduced`() {
        assertNotNull(SafetyScopeRuleCatalog.rules)
        assertTrue(ThomasModeAuthorityContracts.all.all { it.languageModelDecisionAuthority == LanguageModelDecisionAuthority.NONE })
        assertTrue(TherapeuticOntologyCatalog.snapshot.concepts.all { it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED })
    }
}
