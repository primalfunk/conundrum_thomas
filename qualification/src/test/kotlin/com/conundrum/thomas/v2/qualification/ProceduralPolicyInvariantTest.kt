package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.LanguageModelDecisionAuthority
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemActions
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyEvaluator
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemRuleCatalog
import com.conundrum.thomas.v2.engine.verticalslice.PolicyDecisionExplainer
import com.conundrum.thomas.v2.engine.verticalslice.ProductionTherapeuticAuthority
import com.conundrum.thomas.v2.engine.verticalslice.RuleExecutionAuthority
import com.conundrum.thomas.v2.engine.verticalslice.RuleKind
import com.conundrum.thomas.v2.engine.verticalslice.RuleProvenance
import com.conundrum.thomas.v2.ontology.RuntimeAuthorizationStatus
import com.conundrum.thomas.v2.ontology.TherapeuticOntologyCatalog
import com.conundrum.thomas.v2.qualification.verticalslice.CanonicalBoundedProblemScenario
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ProceduralPolicyInvariantTest {
    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))

    @Test
    fun `every source-derived rule has exact governed provenance and qualification-only authority`() {
        val rules = BoundedProblemRuleCatalog.rules
        assertEquals(21, rules.size)
        assertTrue(rules.all { it.provenance.isNotEmpty() })
        assertTrue(rules.filter { it.kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD }.all { rule ->
            rule.provenance.any { it is RuleProvenance.ClinicalSource }
        })
        assertTrue(rules.flatMap { it.provenance }.filterIsInstance<RuleProvenance.ClinicalSource>().all {
            it.preciseLocator.contains("publication p") && it.preciseLocator.contains("PDF p")
        })
        assertTrue(rules.all { it.executionAuthority == RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION })
        assertTrue(rules.all { it.productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED })
        assertTrue(BoundedProblemActions.all.all {
            it.productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED
        })
    }

    @Test
    fun `missing provenance prevents a source-derived rule from being constructed`() {
        val rule = BoundedProblemRuleCatalog.rules.first { it.kind == RuleKind.SOURCE_DERIVED_ACTION }
        assertThrows(IllegalArgumentException::class.java) { rule.copy(provenance = emptyList()) }
    }

    @Test
    fun `selected actions satisfy prerequisites and no exclusions`() {
        CanonicalBoundedProblemScenario.states().forEach { state ->
            val decision = BoundedProblemPolicyEvaluator().evaluate(state)
            val winner = decision.ruleTrace.single { it.ruleId == decision.selectedAction?.selectedByRuleId }
            assertTrue(winner.preconditions.all { it.matched })
            assertTrue(winner.exclusions.none { it.matched })
        }
    }

    @Test
    fun `policy goals dialogue acts and intervention references resolve in ontology but gain no runtime authority`() {
        val concepts = TherapeuticOntologyCatalog.snapshot.concepts.associateBy { it.id }
        BoundedProblemActions.all.forEach { action ->
            assertNotNull(concepts[action.goalId])
            assertNotNull(concepts[action.dialogueActId])
            action.candidateInterventionFamilyId?.let { assertNotNull(concepts[it]) }
        }
        assertTrue(TherapeuticOntologyCatalog.snapshot.concepts.all {
            it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED
        })
    }

    @Test
    fun `explainability artifact exposes state goal rule action and expectation`() {
        val turn = CanonicalBoundedProblemScenario.run().first()
        val compact = PolicyDecisionExplainer.compact(turn.decision)
        assertTrue(compact.contains("ESTABLISH_SUPPORT_INTENT"))
        assertTrue(compact.contains("goal.clarify"))
        assertTrue(compact.contains("ctv203-r009-establish-support-intent"))
        assertTrue(compact.contains("ask-support-preference"))
        assertTrue(compact.contains("SUPPORT_INTENT"))
        assertTrue(turn.decision.rejectedCandidateActions.isNotEmpty())
    }

    @Test
    fun `mode authority and Journal silence remain unchanged`() {
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, ThomasModeAuthorityContracts.journal.defaultResponse)
        assertTrue(ThomasModeAuthorityContracts.all.all {
            it.languageModelDecisionAuthority == LanguageModelDecisionAuthority.NONE
        })
        assertEquals("dialogue.no-response", BoundedProblemActions.waitForOutcome.dialogueActId.value)
    }

    @Test
    fun `policy source has no model diagnosis prediction crisis router or profile mutation`() {
        val source = File(repositoryRoot, "thomas/engine/src/main").walkTopDown()
            .filter { it.isFile }
            .joinToString("\n") { it.readText() }
        listOf(
            "LanguageModel",
            "invokeModel",
            "DiagnosticClassifier",
            "RiskScore",
            "PredictiveRisk",
            "CrisisKeywordRouter",
            "SafetyAlgorithm",
            "mutateProfile",
            "promoteToDurableFact",
        ).forEach { forbidden -> assertFalse("Forbidden authority surface: $forbidden", source.contains(forbidden)) }
        assertFalse(source.contains("kotlin.random"))
        assertFalse(source.contains("java.time"))
        assertFalse(source.contains("java.net"))
    }

    @Test
    fun `V1 migration authority remains zero`() {
        val register = File(repositoryRoot, "migration/v1-component-register.json").readText()
        val componentCount = Regex("\"componentId\"\\s*:").findAll(register).count()
        val deniedCount = Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count()
        assertEquals(componentCount, deniedCount)
        assertFalse(register.contains("\"eventualMigrationCommit\": \""))
        assertFalse(register.contains("\"v1CodeMigrationAuthorized\": true"))
    }
}
