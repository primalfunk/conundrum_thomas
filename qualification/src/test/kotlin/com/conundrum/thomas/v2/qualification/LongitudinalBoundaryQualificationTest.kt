package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.LanguageModelDecisionAuthority
import com.conundrum.thomas.v2.domain.mode.ModePrimaryFunction
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalBoundaryQualificationTest {
    private val root = File(requireNotNull(System.getProperty("thomas.repositoryRoot")))

    @Test fun `forward plan moved byte for byte and root copy is absent`() {
        val canonical = File(root, "docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md")
        assertTrue(canonical.isFile)
        val content = canonical.readText()
        assertTrue(content.startsWith("# CONUNDRUM THOMAS V2"))
        assertTrue(content.contains("# 29. PROGRAM MILESTONES"))
        assertTrue(content.trimEnd().endsWith("That is now the forward path."))
        assertFalse(File(root, "fdp.txt").exists())
        assertTrue(File(root, "README.md").readText().contains("docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md"))
    }

    @Test fun `longitudinal module has no project or platform dependencies`() {
        val build = File(root, "thomas/longitudinal/build.gradle.kts").readText()
        assertFalse(build.contains("project("))
        assertFalse(build.contains("com.android"))
        assertFalse(build.contains("Room"))
        assertTrue(File(root, "settings.gradle.kts").readText().contains("include(\":thomas:longitudinal\")"))
    }

    @Test fun `longitudinal source has no persistence model network policy or profile mutation authority`() {
        val source = File(root, "thomas/longitudinal/src/main").walkTopDown().filter { it.isFile }.joinToString("\n") { it.readText() }
        listOf("android.", "androidx.", "Room", "SQLite", "java.net", "ktor", "invokeModel", "Embedding",
            "CoreOrdinaryTherapyEvaluator", "OrdinaryTherapyPermit", "mutateProfile", "UserProfile", "personalityTraits",
            "attachmentStyle", "DiagnosticClassifier", "com.conundrum.thomas.v2.provenance").forEach { forbidden ->
            assertFalse("Forbidden longitudinal authority surface: $forbidden", source.contains(forbidden))
        }
    }

    @Test fun `turn state and longitudinal state remain separate module types`() {
        val longitudinal = File(root, "thomas/longitudinal/src/main").walkTopDown().filter { it.isFile }.joinToString("\n") { it.readText() }
        val ordinary = File(root, "thomas/engine/src/main/kotlin/com/conundrum/thomas/v2/engine/ordinary").walkTopDown()
            .filter { it.isFile }.joinToString("\n") { it.readText() }
        assertFalse(longitudinal.contains("CoreOrdinaryTherapyState"))
        assertFalse(ordinary.contains("LongitudinalEvidenceSnapshot"))
    }

    @Test fun `all required acquisition paths are structurally distinct`() {
        assertTrue(setOf(
            AcquisitionMode.JOURNAL,
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE,
            AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE,
            AcquisitionMode.THERAPIST_CONVERSATION,
            AcquisitionMode.USER_CORRECTION,
        ).all { it in AcquisitionMode.entries })
    }

    @Test fun `mode authority remains unchanged`() {
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, ThomasModeAuthorityContracts.journal.defaultResponse)
        assertEquals(ModePrimaryFunction.INVESTIGATION, ThomasModeAuthorityContracts.biographer.primaryFunction)
        assertTrue(ThomasModeAuthorityContracts.all.all { it.languageModelDecisionAuthority == LanguageModelDecisionAuthority.NONE })
    }

    @Test fun `ordinary evaluator still requires the safety permit`() {
        val methods = CoreOrdinaryTherapyEvaluator::class.java.declaredMethods.filter { it.name == "evaluate" }
        assertTrue(methods.isNotEmpty())
        assertTrue(methods.all { it.parameterTypes.size == 2 && it.parameterTypes.last().simpleName == "OrdinaryTherapyPermit" })
    }

    @Test fun `no production module consumes longitudinal state in CT V2 06`() {
        listOf("app", "thomas/runtime", "thomas/engine", "thomas/safety", "platform/persistence-android").forEach { path ->
            val material = File(root, path).walkTopDown().onEnter { it.name != "build" }.filter { it.isFile }.joinToString("\n") { it.readText() }
            assertFalse("$path consumes longitudinal state", material.contains(":thomas:longitudinal") || material.contains("LongitudinalEvidenceSnapshot"))
        }
    }

    @Test fun `V1 migration authority remains zero`() {
        val register = File(root, "migration/v1-component-register.json").readText()
        assertEquals(Regex("\"componentId\"\\s*:").findAll(register).count(), Regex("\"approvalState\"\\s*:\\s*\"DENIED\"").findAll(register).count())
        assertFalse(register.contains("\"eventualMigrationCommit\": \""))
        assertFalse(register.contains("\"v1CodeMigrationAuthorized\": true"))
    }
}
