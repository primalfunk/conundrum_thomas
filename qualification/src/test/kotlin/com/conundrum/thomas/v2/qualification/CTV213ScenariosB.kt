package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.languagerenderer.*
import org.junit.Assert.*

internal object CTV213ScenariosB {
    private val S = CTV213TestSupport

    fun run(id: Int) = when (id) {
        in 32..40 -> epistemic(id)
        in 41..47 -> temporal(id)
        in 48..52 -> correctionOrContradiction(id)
        in 53..58 -> questions(id)
        in 59..63 -> length(id)
        64 -> duplicate()
        65, 66 -> variedSequence()
        67 -> distinctActsAndLargeCorpus()
        68 -> fixedSafetyPhrase()
        else -> error("Scenario B does not own $id")
    }

    private fun epistemic(id: Int) {
        val (meaning, marker, text, status, attribution) = when (id) {
            32 -> E("feeling exhausted", "exhausted", "You reported feeling exhausted.", RenderEpistemicStatus.USER_SELF_REPORTED, RenderAttribution.USER_SELF_REPORT)
            33 -> E("belief that Sam dislikes you", "think", "You think Sam may dislike you.", RenderEpistemicStatus.USER_BELIEVES, RenderAttribution.USER_BELIEF)
            34 -> E("interpretation of the interruption", "interpret", "You interpret the interruption as deliberate.", RenderEpistemicStatus.USER_INTERPRETS, RenderAttribution.USER_INTERPRETATION)
            35 -> E("Sam reportedly said he was furious", "reportedly", "Sam reportedly said he was furious.", RenderEpistemicStatus.THIRD_PARTY_REPORTED, RenderAttribution.THIRD_PARTY)
            36 -> E("tentative explanation", "tentative", "One tentative possibility may fit the evidence.", RenderEpistemicStatus.THOMAS_TENTATIVE, RenderAttribution.THOMAS_TENTATIVE)
            37 -> E("belief that Sam hates you", "think", "You think Sam may hate you.", RenderEpistemicStatus.USER_BELIEVES, RenderAttribution.USER_BELIEF)
            38 -> E("belief that nobody likes you", "feeling", "You are feeling as though nobody likes you.", RenderEpistemicStatus.USER_BELIEVES, RenderAttribution.USER_BELIEF)
            39 -> E("belief that you always fail", "belief", "You described the belief that you always fail.", RenderEpistemicStatus.USER_BELIEVES, RenderAttribution.USER_BELIEF)
            else -> E("qualified recurrence", "separate", "You described this in several separate situations, without making it a trait.", RenderEpistemicStatus.STRUCTURAL, RenderAttribution.GOVERNED_POLICY)
        }
        val constraint = if (id in setOf(33, 36, 37)) listOf(RenderEpistemicConstraint("unit.primary", setOf(marker), id == 36)) else emptyList()
        val command = S.basicCommand("epistemic.$id", meaning = meaning, markers = listOf(setOf(marker)),
            forms = listOf(text), fallback = text, epistemic = status, attribution = attribution,
            epistemicConstraints = constraint, allowedEntities = if (id in setOf(33, 35, 37)) setOf("Sam") else emptySet())
        val result = S.renderer.render(command)
        assertEquals(RenderDisposition.ACCEPTED_REFERENCE_REALIZATION, result.disposition)
        assertEquals(status, command.semanticUnits.single().epistemicStatus)
        when (id) {
            37 -> assertNotEquals("Sam hates you.", result.finalText)
            38 -> assertNotEquals("Nobody likes you.", result.finalText)
            39 -> assertFalse(result.finalText!!.contains("verified recurrence"))
            40 -> assertFalse(result.finalText!!.contains("core personality"))
        }
    }

    private data class E(val meaning: String, val marker: String, val text: String,
                         val status: RenderEpistemicStatus, val attribution: RenderAttribution)

    private fun temporal(id: Int) {
        val (text, required, forbidden, allowed) = when (id) {
            41 -> T("You reported the event on 2012-05-03.", "2012-05-03", emptySet(), setOf("2012"))
            42 -> T("You reported the event around 2012.", "around", setOf("2012-01-01"), setOf("2012"))
            43 -> T("You reported the event between 2012 and 2014.", "between", emptySet(), setOf("2012", "2014"))
            44 -> T("You reported it sometime after college.", "after college", emptySet(), emptySet())
            45 -> T("You reported that it is still ongoing.", "ongoing", emptySet(), emptySet())
            46 -> T("You reported an uncertain date.", "uncertain", emptySet(), emptySet())
            else -> T("You reported that the date is unknown.", "unknown", emptySet(), emptySet())
        }
        val command = S.basicCommand("temporal.$id", meaning = "event time", markers = listOf(setOf(required)),
            forms = listOf(text), fallback = text, allowedTimes = allowed,
            temporalConstraints = listOf(RenderTemporalConstraint("unit.primary", setOf(required), forbidden)))
        val result = S.renderer.render(command)
        assertEquals(RenderDisposition.ACCEPTED_REFERENCE_REALIZATION, result.disposition)
        assertTrue(result.finalText!!.contains(required))
        assertTrue(forbidden.none(result.finalText!!::contains))
    }

    private data class T(val text: String, val required: String, val forbidden: Set<String>, val allowed: Set<String>)

    private fun correctionOrContradiction(id: Int) {
        val command = when (id) {
            48, 49 -> S.basicCommand("correction.$id", meaning = "corrected year 2013", markers = listOf(setOf("corrected")),
                forms = listOf("You corrected the account to 2013."), fallback = "You corrected the account to 2013.",
                epistemic = RenderEpistemicStatus.CORRECTED_CURRENT, allowedTimes = setOf("2013"),
                prohibitedPhrases = setOf("2012"))
            50 -> S.basicCommand("correction.$id", act = GovernedSemanticAct.EVIDENCE_EXPLANATION,
                meaning = "correction history", markers = listOf(setOf("earlier"), setOf("corrected")),
                forms = listOf("The earlier account said one thing, and you later corrected it."),
                fallback = "The earlier account said one thing, and you later corrected it.", budget = RenderBudget.EXPLANATION)
            else -> S.basicCommand("contradiction.$id", meaning = "unresolved differing accounts",
                markers = listOf(setOf("differing"), setOf("unresolved")),
                forms = listOf("The differing accounts remain unresolved."), fallback = "The differing accounts remain unresolved.",
                epistemic = RenderEpistemicStatus.CONTESTED)
        }
        if (id == 52) {
            val bad = S.manifest(command).copy(contradictionWinnerChosen = true)
            val result = S.renderExternal(command, "The differing accounts are resolved in favor of the newer one.", bad)
            assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
            assertTrue(RenderValidationReason.CONTRADICTION_WINNER in result.rejectedCandidateReasons.flatten())
        } else {
            val result = S.renderer.render(command)
            assertNotNull(result.finalText)
            if (id == 49) assertFalse(result.finalText!!.contains("2012"))
        }
    }

    private fun questions(id: Int) {
        val command = when (id) {
            53, 54, 57 -> S.basicCommand("questions.$id")
            58 -> S.basicCommand("questions.58")
            else -> S.questionCommand(1)
        }
        val text = when (id) {
            53 -> "You described the interruptions clearly."
            54 -> "Did the interruptions matter?"
            55 -> "What part of the interruptions mattered most?"
            56 -> "What happened with the interruptions? How did it feel?"
            57 -> "You wrote \"Did it happen?\" while describing the interruptions."
            else -> "Could the interruptions matter?"
        }
        val result = S.renderExternal(command, text)
        if (id in setOf(53, 55, 57)) assertEquals(RenderDisposition.ACCEPTED_EXTERNAL_REALIZATION, result.disposition)
        else {
            assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
            assertTrue(RenderValidationReason.QUESTION_LIMIT in result.rejectedCandidateReasons.flatten())
        }
        if (id == 57) assertEquals(0, result.questionCount)
    }

    private fun length(id: Int) {
        val budget = if (id == 61) RenderBudget(120, 1, 0) else RenderBudget(80, 2, 0)
        val fallback = "The interruptions mattered."
        val command = S.basicCommand("length.$id", forms = listOf(fallback), fallback = fallback, budget = budget)
        val text = when (id) {
            59 -> "The interruptions mattered."
            60 -> "The interruptions " + "were difficult ".repeat(8) + "."
            61 -> "The interruptions mattered. They also took time."
            62 -> "The interruptions " + "made an already difficult and tiring workday far more exhausting than it needed to be ".repeat(3) + "."
            else -> "The interruptions " + "went on ".repeat(20) + "."
        }
        val result = S.renderExternal(command, text)
        if (id == 59) assertEquals(RenderDisposition.ACCEPTED_EXTERNAL_REALIZATION, result.disposition)
        else {
            assertEquals(RenderDisposition.FALLBACK_REALIZATION, result.disposition)
            assertTrue(result.characterCount <= budget.maximumCharacters)
            if (id == 61) assertTrue(RenderValidationReason.SENTENCE_LIMIT in result.rejectedCandidateReasons.flatten())
        }
    }

    private fun duplicate() {
        val first = S.renderer.render(S.basicCommand("duplicate.first", turn = 1))
        val result = S.renderExternal(S.basicCommand("duplicate.second", turn = 2), first.finalText!!, history = first.nextHistory)
        assertTrue(RenderValidationReason.EXACT_RECENT_DUPLICATE in result.rejectedCandidateReasons.flatten())
        assertNotEquals(first.finalText, result.finalText)
    }

    private fun variedSequence() {
        var history = RenderHistoryState()
        val outputs = mutableListOf<String>()
        repeat(3) { index ->
            val result = S.renderer.render(S.basicCommand("variation.${index + 1}", turn = index + 1), history)
            outputs += result.finalText!!
            history = result.nextHistory
        }
        assertEquals(3, outputs.toSet().size)
        assertEquals(setOf(GovernedSemanticAct.BRIEF_REFLECTION), history.entries.map { it.semanticAct }.toSet())
    }

    private fun distinctActsAndLargeCorpus() {
        val digestCommand = S.basicCommand("digest.reproducible")
        val digestOne = S.renderer.render(digestCommand)
        val digestTwo = S.renderer.render(digestCommand)
        assertEquals(digestOne.finalText, digestTwo.finalText)
        assertEquals(digestOne.canonicalRenderDigest, digestTwo.canonicalRenderDigest)
        val acts = listOf(GovernedSemanticAct.BRIEF_REFLECTION, GovernedSemanticAct.CLARIFYING_QUESTION,
            GovernedSemanticAct.OPEN_QUESTION, GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION,
            GovernedSemanticAct.TENTATIVE_MEMORY_CONNECTION, GovernedSemanticAct.EXPLICIT_RECALL,
            GovernedSemanticAct.EVIDENCE_EXPLANATION)
        val outputs = acts.mapIndexed { i, act ->
            val question = act in setOf(GovernedSemanticAct.CLARIFYING_QUESTION, GovernedSemanticAct.OPEN_QUESTION)
            val text = if (question) "Act $i asks about interruptions?" else "Act $i expresses interruptions."
            S.renderer.render(S.basicCommand("acts.$i", act = act, forms = listOf(text), fallback = text,
                budget = RenderBudget(120, 2, if (question) 1 else 0), advice = act == GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION)).finalText!!
        }
        assertEquals(acts.size, outputs.toSet().size)
        val large = (1..180).map { i ->
            val text = "Rendering $i expresses interruptions."
            S.renderer.render(S.basicCommand("corpus.$i", turn = i, forms = listOf(text), fallback = text)).also {
                assertEquals(RenderDisposition.ACCEPTED_REFERENCE_REALIZATION, it.disposition)
                assertTrue(it.questionCount == 0 && it.characterCount <= 320)
            }.finalText!!
        }
        assertEquals(180, large.toSet().size)
    }

    private fun fixedSafetyPhrase() {
        val command = SafetyRenderCommandAdapter.adapt(S.id("safety.fixed"), 1,
            S.domainCommand("core-reflect-established-content", RenderForm.INTERROGATIVE, 1, ordinary = false),
            listOf(AuthorizedSupportingText("required-safety-information", "CURRENT_EMERGENCY_STATUS")))
        val first = S.renderer.render(command)
        val second = S.renderer.render(command.copy(id = S.id("safety.fixed-again"), turnIndex = 2), first.nextHistory)
        assertEquals(first.finalText, second.finalText)
        assertEquals(RenderDisposition.ACCEPTED_REFERENCE_REALIZATION, second.disposition)
    }
}
