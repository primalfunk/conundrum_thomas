package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.LanguageModelDecisionAuthority
import com.conundrum.thomas.v2.domain.mode.ModeRuntimeAuthority
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.engine.ordinary.CoreActionRuleCatalog
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryActions
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryRuleCatalog
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDecisionExplainer
import com.conundrum.thomas.v2.engine.ordinary.CoreProgressionGuardCatalog
import com.conundrum.thomas.v2.engine.ordinary.CoreRouteRuleCatalog
import com.conundrum.thomas.v2.engine.ordinary.CoreRouteTransitionCatalog
import com.conundrum.thomas.v2.engine.ordinary.GovernedCoreRule
import com.conundrum.thomas.v2.engine.verticalslice.ProductionTherapeuticAuthority
import com.conundrum.thomas.v2.engine.verticalslice.RuleExecutionAuthority
import com.conundrum.thomas.v2.engine.verticalslice.RuleKind
import com.conundrum.thomas.v2.engine.verticalslice.RuleProvenance
import com.conundrum.thomas.v2.ontology.RuntimeAuthorizationStatus
import com.conundrum.thomas.v2.ontology.TherapeuticOntologyCatalog
import com.conundrum.thomas.v2.qualification.ordinary.CanonicalCoreConversations
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreOrdinaryInvariantTest {
    private val repositoryRoot = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))

    @Test fun `core repertoire has exact bounded breadth and zero production authority`() {
        val rules = CoreOrdinaryRuleCatalog.allRules
        assertEquals(9, CoreRouteRuleCatalog.rules.size)
        assertEquals(26, CoreActionRuleCatalog.rules.size)
        assertEquals(4, CoreProgressionGuardCatalog.rules.size)
        assertEquals(39, rules.size)
        assertEquals(32, rules.count { it.kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD })
        assertEquals(7, rules.count { it.kind == RuleKind.ARCHITECTURAL_SCOPE_GUARD })
        assertEquals(25, CoreOrdinaryActions.all.size)
        assertEquals(12, CoreOrdinaryActions.all.map { it.goalId }.distinct().size)
        assertEquals(19, CoreRouteTransitionCatalog.transitions.size)
        assertTrue(rules.all { it.executionAuthority == RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION })
        assertTrue(rules.all { it.productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED })
        assertTrue(CoreOrdinaryActions.all.all { it.productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED })
    }

    @Test fun `every clinical core rule has exact source provenance and pending review`() {
        val clinicalRules = CoreOrdinaryRuleCatalog.allRules.filter { it.kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD }
        assertTrue(clinicalRules.all { rule -> rule.provenance.any { it is RuleProvenance.ClinicalSource } })
        val bindings = clinicalRules.flatMap { it.provenance }.filterIsInstance<RuleProvenance.ClinicalSource>()
        assertTrue(bindings.all { it.preciseLocator.contains("publication p") && it.preciseLocator.contains("PDF p") })
        assertTrue(bindings.all { it.clinicalReviewStatus.name == "PENDING" && it.rightsReviewStatus.name == "PENDING" })
        assertTrue(CoreOrdinaryRuleCatalog.allRules.all { it.provenance.isNotEmpty() })
    }

    @Test fun `source-derived rule construction rejects missing provenance`() {
        val rule = CoreActionRuleCatalog.rules.first { it.kind == RuleKind.SOURCE_DERIVED_ACTION }
        assertThrows(IllegalArgumentException::class.java) { rule.copy(provenance = emptyList()) }
    }

    @Test fun `canonical selected rules satisfy every prerequisite and no exclusion`() {
        val turns = listOf(
            CanonicalCoreConversations.listening(), CanonicalCoreConversations.understanding(),
            CanonicalCoreConversations.practicalProblemSolving(), CanonicalCoreConversations.preferenceChange(),
            CanonicalCoreConversations.correction(),
        ).flatten()
        turns.forEach { turn ->
            val selectedRuleId = turn.decision.selectedAction?.selectedByRuleId ?: return@forEach
            val trace = turn.decision.actionRuleTrace.singleOrNull { it.ruleId == selectedRuleId } ?: return@forEach
            assertTrue(trace.prerequisites.all { it.matched })
            assertTrue(trace.exclusions.none { it.matched })
        }
    }

    @Test fun `all action goals acts and intervention references resolve without ontology runtime authority`() {
        val concepts = TherapeuticOntologyCatalog.snapshot.concepts.associateBy { it.id }
        CoreOrdinaryActions.all.forEach { action ->
            assertNotNull(concepts[action.goalId])
            assertNotNull(concepts[action.dialogueActId])
            action.candidateInterventionFamilyId?.let { assertNotNull(concepts[it]) }
        }
        assertTrue(TherapeuticOntologyCatalog.snapshot.concepts.all { it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED })
    }

    @Test fun `explainability identifies route goal rule action and expectation`() {
        val turn = CanonicalCoreConversations.practicalProblemSolving().first()
        val explanation = CorePolicyDecisionExplainer.compact(turn.decision)
        assertTrue(explanation.contains("PRACTICAL_PROBLEM_SOLVING"))
        assertTrue(explanation.contains("goal.understand"))
        assertTrue(explanation.contains("ctv205-a016-problem-define"))
        assertTrue(explanation.contains("core-ask-problem-description"))
        assertTrue(explanation.contains("PRESENT_CONCERN"))
    }

    @Test fun `Journal Biographer and model authority remain unchanged`() {
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, ThomasModeAuthorityContracts.journal.defaultResponse)
        assertTrue(ThomasModeAuthorityContracts.all.all { it.languageModelDecisionAuthority == LanguageModelDecisionAuthority.NONE })
        assertTrue(ThomasModeAuthorityContracts.all.all { it.runtimeAuthority == ModeRuntimeAuthority.PRODUCTION_RUNTIME_NOT_GRANTED })
    }

    @Test fun `core evaluator is absent from app runtime safety and platform adapters`() {
        listOf("app", "thomas/runtime", "thomas/safety", "platform/persistence-android", "platform/renderer-llama-android", "platform/speech-android").forEach { path ->
            val material = File(repositoryRoot, path).walkTopDown().onEnter { it.name != "build" }
                .filter { it.isFile && (it.extension == "kt" || it.name.endsWith(".gradle.kts")) }
                .joinToString("\n") { it.readText() }
            assertFalse("$path wired the core evaluator", material.contains("CoreOrdinaryTherapyEvaluator"))
            assertFalse("$path wired the qualification renderer", material.contains("DeterministicCoreOrdinaryRenderer"))
            assertFalse("$path depends on qualification", material.contains(":qualification"))
        }
    }

    @Test fun `ordinary core evaluator exposes no state-only execution entry point`() {
        val evaluateMethods = CoreOrdinaryTherapyEvaluator::class.java.declaredMethods.filter { it.name == "evaluate" }
        assertTrue(evaluateMethods.isNotEmpty())
        assertTrue(evaluateMethods.all { it.parameterTypes.size == 2 })
        assertTrue(evaluateMethods.all { it.parameterTypes.last().simpleName == "OrdinaryTherapyPermit" })
    }

    @Test fun `core source has no model diagnosis prediction random network or profile mutation authority`() {
        val source = File(repositoryRoot, "thomas/engine/src/main/kotlin/com/conundrum/thomas/v2/engine/ordinary")
            .walkTopDown().filter { it.isFile }.joinToString("\n") { it.readText() }
        listOf("invokeModel", "DiagnosticClassifier", "RiskScore", "PredictiveRisk", "CrisisKeywordRouter",
            "mutateProfile", "promoteToDurableFact", "kotlin.random", "java.net", "java.time").forEach {
            assertFalse("Forbidden authority surface: $it", source.contains(it))
        }
    }

    @Test fun `V1 migration authority remains zero`() {
        val register = File(repositoryRoot, "migration/v1-component-register.json").readText()
        assertEquals(Regex("\"componentId\"\\s*:").findAll(register).count(), Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count())
        assertFalse(register.contains("\"eventualMigrationCommit\": \""))
        assertFalse(register.contains("\"v1CodeMigrationAuthorized\": true"))
    }
}
