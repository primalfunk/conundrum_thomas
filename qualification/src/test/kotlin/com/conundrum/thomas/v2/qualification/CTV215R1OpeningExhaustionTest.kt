package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryActions
import com.conundrum.thomas.v2.languagerenderer.*
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest
import java.util.Locale

class CTV215R1OpeningExhaustionTest {
    private val s = CTV213TestSupport
    private val validator = DeterministicRenderValidator()
    private val renderer = GovernedLanguageRenderer()
    private val meaning = "arranging a new synthetic meeting"
    private fun command(action: String = "core-verify-problem-understanding", value: String = meaning): GovernedRenderCommand {
        val spec = CoreOrdinaryActions.all.single { it.id.value == action }.renderSpecification
        val domain = s.domainCommand(action, spec.form, spec.maximumQuestions).copy(
            interpretationMustRemainTentative = spec.interpretationMustRemainTentative,
            userAgencyMustBeExplicitlyPreserved = spec.userAgencyMustBeExplicitlyPreserved)
        val support = listOf("established-concern", "tentative-thomas-understanding", "confirmed-understanding",
            "important-missing-information", "user-selected-option", "action-plan", "plan-outcome").map {
            AuthorizedSupportingText(it, value)
        }
        return TherapyRenderCommandAdapter.adapt(s.id("opening.contract"), 100, s.therapyEnvelope(command=domain, support=support))
    }
    private fun normalize(text: String) = text.lowercase(Locale.ROOT).replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
    private fun sha(text: String) = MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
    private fun opening(text: String) = normalize(text).split(" ").take(3).joinToString(" ")
    private fun history(texts: List<String>) = RenderHistoryState(texts.mapIndexed { i, text ->
        RenderHistoryEntry(sha(normalize(text)), sha(opening(text)), GovernedSemanticAct.CLARIFYING_QUESTION, i+1, false)
    })
    private fun validate(c: GovernedRenderCommand, text: String, h: RenderHistoryState) = validator.validate(c, s.candidate(c,text),h)
    private fun assertSelection(c: GovernedRenderCommand, h: RenderHistoryState): GovernedRenderResult {
        val expected = c.authorizedReferenceRealizations.first { validate(c,it,h).accepted }
        val result = renderer.render(c,h)
        assertTrue(result.toString(),result.validation.accepted)
        assertEquals(c.semanticAct,result.semanticAct)
        assertEquals(expected,result.finalText)
        assertEquals(100,result.nextHistory.entries.last().turnIndex)
        return result
    }

    @Test fun oldSingletonStillFailsAndLeavesHistoryUnchanged() {
        val c = command(); val preferred = c.authorizedReferenceRealizations.first()
        val h = history(listOf("I might be understanding an earlier issue. Is that right?", "I might be understanding another issue. Is that right?"))
        val old = c.copy(authorizedReferenceRealizations=listOf(preferred), deterministicFallbackText=preferred)
        val result = renderer.render(old,h)
        assertEquals(RenderDisposition.RENDERING_UNAVAILABLE,result.disposition)
        assertEquals(listOf(RenderValidationReason.REPEATED_OPENING),result.validation.reasonCodes)
        assertEquals(h,result.nextHistory)
        assertNull(result.finalText)
        assertEquals(GovernedSemanticAct.CLARIFYING_QUESTION,result.semanticAct)
        assertEquals(c.authorizedReferenceRealizations[1],assertSelection(c,h).finalText)
    }

    @Test fun preferredUnusedOnceTwiceAndExactDuplicateStayStrict() {
        val c=command();val first=c.authorizedReferenceRealizations.first()
        assertEquals(first,assertSelection(c,history(emptyList())).finalText)
        val once=history(listOf("I might be considering an earlier account."))
        assertEquals(first,assertSelection(c,once).finalText)
        val twice=history(listOf("I might be considering an earlier account.","I might be considering another account."))
        assertEquals(listOf(RenderValidationReason.REPEATED_OPENING),validate(c,first,twice).reasonCodes)
        assertEquals(c.authorizedReferenceRealizations[1],assertSelection(c,twice).finalText)
        val exact=history(listOf(first))
        assertEquals(listOf(RenderValidationReason.EXACT_RECENT_DUPLICATE),validate(c,first,exact).reasonCodes)
        assertEquals(c.authorizedReferenceRealizations[1],assertSelection(c,exact).finalText)
    }

    @Test fun multipleRejectedCandidatesAndRepeatedFallbackAdvanceToFirstValidForm() {
        val c=command();val forms=c.authorizedReferenceRealizations
        val h=history(forms.take(3))
        val result=assertSelection(c,h)
        assertEquals(forms[3],result.finalText)
        assertTrue(result.rejectedCandidateReasons.size>=3)
        assertTrue(result.rejectedCandidateReasons.all { RenderValidationReason.EXACT_RECENT_DUPLICATE in it })
    }

    @Test fun allFiniteRecurrenceBlockingStatesHaveAnAuthorizedVerification() {
        val c=command();val forms=c.authorizedReferenceRealizations
        assertEquals(7,forms.size); assertEquals(7,forms.map(::opening).distinct().size)
        var cases=0
        // Equivalence classes: exact blocked subset plus zero, one or two repeated openings.
        // Each repeated opening needs two last-four witnesses; other exact witnesses fit
        // in the six-response window. Ordering within a class cannot affect either rule.
        for (exactMask in 0 until (1 shl 7)) for (openingMask in 0 until (1 shl 7)) {
            val exact=forms.indices.filter { exactMask and (1 shl it)!=0 }
            val repeated=forms.indices.filter { openingMask and (1 shl it)!=0 }
            if(repeated.size>2) continue
            val outside=exact.filter { it !in repeated }.map(forms::get)
            val pairs=repeated.flatMap { n -> listOf(
                if(n in exact) forms[n] else opening(forms[n])+" earlier account alpha.",
                opening(forms[n])+" earlier account beta.") }
            val texts=outside+pairs
            if(texts.size>6) continue
            val h=history(texts)
            forms.forEachIndexed { n,text ->
                val reasons=validate(c,text,h).reasonCodes
                assertEquals(n in exact, RenderValidationReason.EXACT_RECENT_DUPLICATE in reasons)
                assertEquals(n in repeated, RenderValidationReason.REPEATED_OPENING in reasons)
            }
            assertSelection(c,h);cases++
        }
        assertTrue(cases>1000)
        println("FINITE_RECURRENCE_EQUIVALENCE_CLASSES=$cases ALL_FIRST_VALID_VERIFICATION_PASS")
    }

    @Test fun recurrenceWindowRollsAndRenderingRemainsDeterministic() {
        val c=command();var h=RenderHistoryState();val outputs=mutableListOf<String>()
        repeat(30) { n ->
            val current=c.copy(turnIndex=100+n)
            val ten=List(10) { renderer.render(current,h) }
            assertEquals(1,ten.toSet().size)
            val result=ten.singleOrNull() ?: ten.first()
            assertTrue(result.validation.accepted)
            assertEquals(c.semanticAct,result.semanticAct)
            outputs+=requireNotNull(result.finalText);h=result.nextHistory
        }
        assertEquals(c.authorizedReferenceRealizations,outputs.take(7))
        assertEquals(outputs[0],outputs[7])
    }

    @Test fun verificationFormsRetainTentativenessReferentAndSingleCorrectionQuestion() {
        listOf("core-verify-problem-understanding","core-verify-tentative-understanding").forEach { action ->
            val c=command(action)
            assertEquals(GovernedSemanticAct.CLARIFYING_QUESTION,c.semanticAct)
            assertTrue(c.referenceCatalogOrder)
            c.authorizedReferenceRealizations.forEach { text ->
                assertTrue(text.contains(meaning))
                assertTrue(Regex("(?i)might|tentative|tentatively|could|perhaps").containsMatchIn(text))
                assertEquals(1,text.count { it=='?' })
                assertTrue(validate(c,text,RenderHistoryState()).accepted)
                val wrong=s.candidate(c,text,s.manifest(c).copy(declaredSemanticAct=GovernedSemanticAct.BRIEF_REFLECTION))
                assertTrue(RenderValidationReason.SEMANTIC_ACT_MISMATCH in validator.validate(c,wrong,RenderHistoryState()).reasonCodes)
            }
        }
    }

    @Test fun everyOrdinaryCatalogHasSevenValidatedDistinctOpeningsAndSixCannotExhaustIt() {
        val actions=CoreOrdinaryActions.all.filter { it.renderSpecification.outputDisposition!=RenderOutputDisposition.NO_RESPONSE }
        assertEquals(23,actions.size)
        actions.forEach { action ->
            val c=command(action.id.value);val forms=c.authorizedReferenceRealizations
            assertEquals(action.id.value,7,forms.size)
            assertEquals(action.id.value,7,forms.map(::opening).distinct().size)
            forms.forEach { text -> assertTrue("${action.id}: $text",validate(c,text,RenderHistoryState()).accepted) }
            forms.indices.forEach { available ->
                val h=history(forms.filterIndexed { n,_ -> n!=available })
                val result=renderer.render(c,h)
                assertTrue("${action.id}: $result",result.validation.accepted)
                assertEquals(forms[available],result.finalText)
                assertEquals(c.semanticAct,result.semanticAct)
            }
            println("CATALOG_AUDIT=${action.id.value} FORMS=7 OPENINGS=7 SIX_RECENT_EXHAUSTION=IMPOSSIBLE")
        }
    }

    @Test fun verificationAtExistingCharacterBoundaryRetainsAllSevenForms() {
        val short=command();val overhead=short.authorizedReferenceRealizations.first().length-meaning.length
        val c=command(value="x".repeat(short.budget.maximumCharacters-overhead))
        assertEquals(7,c.authorizedReferenceRealizations.size)
        assertTrue(c.authorizedReferenceRealizations.all { it.length<=c.budget.maximumCharacters })
    }
    @Test fun dynamicCatalogsPreserveTheExistingCharacterBoundary() {
        CoreOrdinaryActions.all.filter { it.renderSpecification.authorizedContextFields.isNotEmpty() &&
            it.renderSpecification.outputDisposition!=RenderOutputDisposition.NO_RESPONSE }.forEach { action ->
            val c=command(action.id.value,value="x")
            val base=c.semanticUnits.single { it.id=="therapy-policy-act" }.surfaceMeaning
            val count=base.count { it=='x' }
            if(count==0) return@forEach
            val oldMaximum = if(action.id.value=="core-reflect-established-content")
                base.replaceFirst("I hear that","What stands out is that").length else base.length
            val bound=(640-(oldMaximum-count))/count
            val bounded=command(action.id.value,"x".repeat(bound))
            assertEquals(action.id.value,7,bounded.authorizedReferenceRealizations.size)
            assertTrue(bounded.authorizedReferenceRealizations.all { it.length<=640 })
        }
    }

}