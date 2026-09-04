package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.CT_V2_10_COVERAGE_RULE_VERSION
import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.engine.ordinary.CT_V2_05_CORE_POLICY_VERSION
import com.conundrum.thomas.v2.journal.CT_V2_09_JOURNAL_CONTRACT_VERSION
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.languagerenderer.*
import com.conundrum.thomas.v2.languageevidence.perception.CT_V2_08_PERCEPTION_VERSION
import com.conundrum.thomas.v2.languageevidence.stateformation.CT_V2_08_STATE_FORMATION_VERSION
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.retrieval.CT_V2_11_RETRIEVAL_POLICY_VERSION
import com.conundrum.thomas.v2.safety.CT_V2_04_SAFETY_SCOPE_POLICY_VERSION
import com.conundrum.thomas.v2.therapylongitudinal.CT_V2_12_INTEGRATION_POLICY_VERSION
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemorySemanticAct
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.*

internal object CTV213ScenariosD {
    private val S = CTV213TestSupport
    private val root: Path = Path.of(requireNotNull(System.getProperty("thomas.repositoryRoot")))

    fun run(id: Int) = when (id) {
        in 104..106 -> modeSpecific(id)
        107 -> cannotChangeJournal()
        108 -> cannotChangeBiographer()
        109 -> cannotChangeTherapy()
        110 -> cannotChangeSafety()
        111 -> therapyOneMemoryMaximum()
        112, 113 -> suppressedMemoryStaysHidden(id)
        114 -> explicitRecall()
        115 -> explainView()
        116 -> assertEquals("ct-v2-04-safety-scope-gate-1.0.0", CT_V2_04_SAFETY_SCOPE_POLICY_VERSION)
        117 -> assertEquals("ct-v2-05-core-ordinary-therapy-1.0.0", CT_V2_05_CORE_POLICY_VERSION)
        118 -> assertTrue(sourceText("thomas/engine").contains("explicitRepeatAuthorization"))
        119 -> assertTrue(sourceText("thomas/longitudinal").contains("SourceIdentityId"))
        120 -> assertEquals("ACCEPTED", AdmissionDisposition.ACCEPTED.name)
        121 -> assertEquals(setOf("ct-v2-08.perception.v1", "ct-v2-08.state-formation.v1"),
            setOf(CT_V2_08_PERCEPTION_VERSION, CT_V2_08_STATE_FORMATION_VERSION))
        122 -> assertEquals("ct-v2-09.journal.v1", CT_V2_09_JOURNAL_CONTRACT_VERSION)
        123 -> assertEquals(setOf(BiographerPosture.OPEN_STORY, BiographerPosture.TARGETED_COVERAGE), BiographerPosture.entries.toSet())
        124 -> assertEquals("ct-v2-11.retrieval.v1", CT_V2_11_RETRIEVAL_POLICY_VERSION)
        125 -> assertEquals("ct-v2-12.therapy-longitudinal.v1", CT_V2_12_INTEGRATION_POLICY_VERSION)
        126 -> assertEquals(JournalResponsePreference.NO_RESPONSE, JournalResponsePreference.entries.first())
        127 -> v1RegisterDenied()
        128 -> productionModelRootsZero()
        129 -> androidRendererRootsZero()
        130 -> canonicalGitRoot()
        131 -> temporaryGitMetadataZero()
        else -> error("Scenario D does not own $id")
    }

    private fun modeSpecific(id: Int) {
        val mode = when (id) { 104 -> GovernedRenderMode.JOURNAL; 105 -> GovernedRenderMode.BIOGRAPHER; else -> GovernedRenderMode.THERAPY }
        val posture = when (mode) {
            GovernedRenderMode.JOURNAL -> GovernedResponsePosture.JOURNAL_REFLECT
            GovernedRenderMode.BIOGRAPHER -> GovernedResponsePosture.BIOGRAPHER_TARGETED_COVERAGE
            else -> GovernedResponsePosture.THERAPY
        }
        val prefix = mode.name.lowercase().replaceFirstChar(Char::uppercase)
        val text = "$prefix rendering preserves interruptions."
        val result = S.renderer.render(S.basicCommand("mode.$id", mode = mode, posture = posture,
            forms = listOf(text), fallback = text))
        assertEquals(mode, result.mode)
        assertTrue(result.finalText!!.startsWith(prefix))
    }

    private fun cannotChangeJournal() {
        val command = S.basicCommand("authority.journal", mode = GovernedRenderMode.JOURNAL,
            posture = GovernedResponsePosture.JOURNAL_REFLECT)
        val result = S.renderExternal(command, "You described the interruptions at work.",
            S.manifest(command).copy(declaredMode = GovernedRenderMode.THERAPY))
        assertEquals(GovernedRenderMode.JOURNAL, result.mode)
        assertTrue(RenderValidationReason.MODE_MISMATCH in result.rejectedCandidateReasons.flatten())
    }

    private fun cannotChangeBiographer() {
        val command = S.basicCommand("authority.biographer", mode = GovernedRenderMode.BIOGRAPHER,
            posture = GovernedResponsePosture.BIOGRAPHER_TARGETED_COVERAGE,
            act = GovernedSemanticAct.CLARIFYING_QUESTION,
            forms = listOf("What about the interruptions?"), fallback = "What about the interruptions?",
            budget = RenderBudget.ONE_QUESTION)
        val result = S.renderExternal(command, "What about childhood instead?", S.manifest(command))
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertTrue(RenderValidationReason.MISSING_SEMANTIC_UNIT in result.rejectedCandidateReasons.flatten())
    }

    private fun cannotChangeTherapy() {
        val command = S.basicCommand("authority.therapy")
        val result = S.renderExternal(command, "Plan around the interruptions.",
            S.manifest(command).copy(declaredSemanticAct = GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION))
        assertEquals(GovernedSemanticAct.BRIEF_REFLECTION, result.semanticAct)
        assertTrue(RenderValidationReason.SEMANTIC_ACT_MISMATCH in result.rejectedCandidateReasons.flatten())
    }

    private fun safetyCommand() = SafetyRenderCommandAdapter.adapt(S.id("authority.safety"), 1,
        S.domainCommand("core-reflect-established-content", RenderForm.INTERROGATIVE, 1, ordinary = false),
        listOf(AuthorizedSupportingText("required-safety-information", "CURRENT_EMERGENCY_STATUS")))

    private fun cannotChangeSafety() {
        val command = safetyCommand()
        val result = S.renderExternal(command, "The situation is safe.",
            S.manifest(command).copy(safetyChange = true))
        assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
        assertEquals(command.fixedSafetyText, result.finalText)
        assertTrue(RenderValidationReason.SAFETY_CHANGE in result.rejectedCandidateReasons.flatten())
    }

    private fun therapyOneMemoryMaximum() {
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.max-one"), 1, S.therapyEnvelope(listOf(S.memory())))
        val result = S.renderer.render(command)
        assertEquals(1, command.historicalSupport.size)
        assertEquals(1, result.surfacedMemoryIds.size)
    }

    private fun suppressedMemoryStaysHidden(id: Int) {
        val command = TherapyRenderCommandAdapter.adapt(S.id("therapy.suppressed-$id"), id, S.therapyEnvelope())
        assertTrue(command.historicalSupport.isEmpty())
        val result = S.renderExternal(command, "An old memory explains work felt exhausting.",
            S.manifest(command).copy(referencedMemoryIds = setOf("assertion.memory")))
        assertTrue(RenderValidationReason.UNAUTHORIZED_MEMORY in result.rejectedCandidateReasons.flatten())
        assertTrue(result.surfacedMemoryIds.isEmpty())
    }

    private fun explicitRecall() {
        val memory = S.memory(semanticAct = TherapyMemorySemanticAct.USER_REQUESTED_COMPARISON)
        val result = S.renderer.render(TherapyRenderCommandAdapter.adapt(S.id("therapy.explicit-recall"), 1,
            S.therapyEnvelope(listOf(memory))))
        assertEquals(GovernedSemanticAct.EXPLICIT_RECALL, result.semanticAct)
        assertEquals(listOf(memory.stableObjectId), result.surfacedMemoryIds)
    }

    private fun explainView() {
        val memory = S.memory(semanticAct = TherapyMemorySemanticAct.EVIDENCE_EXPLANATION, contradiction = true)
        val result = S.renderer.render(TherapyRenderCommandAdapter.adapt(S.id("therapy.explain-view"), 1,
            S.therapyEnvelope(listOf(memory))))
        assertEquals(GovernedSemanticAct.EVIDENCE_EXPLANATION, result.semanticAct)
        assertTrue(result.finalText!!.contains("differing accounts"))
        assertTrue(result.finalText!!.contains("neither is settled"))
    }

    private fun sourceText(relative: String): String = root.resolve(relative).toFile().walkTopDown()
        .filter { it.isFile && it.extension in setOf("kt", "kts") && "build" !in it.path }
        .joinToString("\n") { it.readText() }

    private fun v1RegisterDenied() {
        val register = root.resolve("migration/v1-component-register.json").toFile().readText()
        val quote = 34.toChar()
        val token = quote + "approvalState" + quote + ": " + quote + "DENIED" + quote
        assertEquals(24, register.split(token).size - 1)
    }

    @Suppress("unused")
    private fun obsoleteV1RegisterAssertion() {
        val register = root.resolve("migration/v1-component-register.json").toFile().readText()
        assertEquals(24, Regex("\\\"disposition\\\"\\s*:\\s*\\\"DENIED\\\"").findAll(register).count())
    }

    private fun productionModelRootsZero() {
        val production = sourceText("app/src/main")
        assertFalse(production.contains("GovernedLanguageRenderer"))
        val module = sourceText("thomas/language-renderer/src/main")
        listOf("llama", "gguf", "model adapter", "network client").forEach { assertFalse(module.contains(it, true)) }
    }

    private fun androidRendererRootsZero() {
        val build = root.resolve("thomas/language-renderer/build.gradle.kts").toFile().readText()
        assertFalse(build.contains("android"))
        assertFalse(sourceText("app/src/main").contains("languagerenderer"))
    }

    private fun canonicalGitRoot() {
        assertTrue(Files.isDirectory(root.resolve(".git")))
        val process = ProcessBuilder("git", "rev-parse", "--show-toplevel").directory(root.toFile()).start()
        val top = process.inputStream.bufferedReader().readText().trim().replace('\\', '/')
        assertEquals(0, process.waitFor())
        assertEquals(root.toAbsolutePath().normalize().toString().replace('\\', '/'), top)
    }

    private fun temporaryGitMetadataZero() {
        Files.list(root).use { entries ->
            assertTrue(entries.noneMatch { path -> path.fileName.toString().startsWith(".git-ct-v2-") ||
                path.fileName.toString() in setOf(".git-work", ".git-temp", "git-metadata-backup") })
        }
    }
}
