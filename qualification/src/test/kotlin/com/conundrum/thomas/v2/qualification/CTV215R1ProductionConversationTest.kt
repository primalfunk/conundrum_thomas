package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.runtime.*
import com.conundrum.thomas.v2.engine.ordinary.*
import com.conundrum.thomas.v2.engine.verticalslice.*
import com.conundrum.thomas.v2.biographer.*
import com.conundrum.thomas.v2.safety.*
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import com.conundrum.thomas.v2.longitudinal.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

/** Every state in this suite comes from submit, the boundary called by ThomasViewModel. */
class CTV215R1ProductionConversationTest {
    private class Conversation(val h: CTV215Harness, val route: RequestedOrdinarySupport = RequestedOrdinarySupport.LISTEN) {
        var index = 100L
        fun send(text: String, scope: Boolean = index == 100L, memory: TherapyMemoryIntent = TherapyMemoryIntent.ORDINARY): ProductionTurnResult {
            val result = h.runtime.submit(ProductionTurnRequest(index++, ProductionThomasMode.THERAPY,
                (if (scope) DECLARATIONS + "\n" else "") + text, requestedTherapySupport = route,
                therapyMemoryIntent = memory, committedAt = Instant.parse("2040-02-02T00:00:00Z").plusSeconds(index)))
            println("TURN $text | revision=${result.therapyObservation?.conversationRevision} | action=${result.therapyPlan?.routeDecision?.selectedActionId} | progression=${result.therapyPlan?.routeDecision?.progression} | safety=${result.safetyObservation?.authorityState} | ${result.assistantArtifact?.text ?: result.disposition}")
            assertNotEquals("Unexpected runtime failure: ${result.reasonCodes}", ProductionTurnDisposition.PERSISTENCE_UNAVAILABLE, result.disposition)
            return result
        }
    }
    private fun action(result: ProductionTurnResult, expected: String) {
        assertEquals(result.toString(), "core-$expected", result.therapyPlan?.routeDecision?.selectedActionId)
        assertEquals(emptyList<String>(), result.therapyObservation?.validationErrors())
        if (expected in setOf("wait-for-outcome", "pause-without-response")) assertEquals(ProductionTurnDisposition.NO_RESPONSE, result.disposition)
        else assertEquals(result.renderResult.toString(), ProductionTurnDisposition.COMPLETED, result.disposition)
    }

    @Test fun listenProgressionUsesDeliveredActionsAndExplicitReplies() = CTV215Harness().use { h ->
        val c = Conversation(h)
        action(c.send("I want to begin"), "invite-expression")
        action(c.send("My specific concern is: the delayed project meeting"), "reflect-established-content")
        action(c.send("Yes, that's right."), "invite-further-expression")
        action(c.send("That's all for now."), "summarize-listening")
        action(c.send("Thank you."), "check-further-or-close")
        action(c.send("Stop"), "acknowledge-close")
    }

    @Test fun understandClarifiesConfirmsAndSummarizesActualUserEvidence() = CTV215Harness().use { h ->
        val c = Conversation(h, RequestedOrdinarySupport.UNDERSTAND)
        action(c.send("My specific concern is: the delayed meeting\nWhat I haven't explained is: who changed the time"), "ask-important-missing-piece")
        val tentative = c.send("The missing detail is: my manager changed the time")
        action(tentative, "verify-tentative-understanding")
        assertFalse(tentative.therapyObservation!!.thomasUnderstanding.isEstablished())
        action(c.send("Yes, that's right."), "summarize-shared-understanding")
        val next = c.send("Thank you")
        action(next, "check-understanding-next-direction")
        assertTrue(next.therapyObservation!!.understandingSummaryDelivered.value == true)
    }

    @Test fun practicalUsesUserOptionsChoicePlanAndActualOutcome() = CTV215Harness().use { h ->
        val c = practicalPlan(h)
        val wait = c.send("I have not tried it yet")
        assertNull(wait.therapyObservation!!.planOutcome.value)
        assertEquals(ProgressionDisposition.STOP_NO_PROGRESS, wait.therapyPlan?.routeDecision?.progression)
        action(c.send("I attempted the plan"), "review-reported-outcome")
        val complete = c.send("What happened was: the manager replied and it helped")
        action(complete, "consolidate-plan-learning")
        assertEquals(PlanOutcome.ATTEMPTED, complete.therapyObservation!!.planOutcome.value)
        assertEquals(PlanReviewStatus.REVIEWED, complete.therapyObservation!!.planReviewStatus.value)
    }

    @Test fun nonAttemptIsNotRenderedAsAnAttempt() = CTV215Harness().use { h ->
        val c = practicalPlan(h)
        val review = c.send("I have not attempted the plan")
        action(review, "review-reported-outcome")
        assertFalse(review.assistantArtifact!!.text.contains("you tried", true))
        val summary = c.send("What happened was: I did not have time")
        action(summary, "consolidate-plan-learning")
        assertFalse(summary.assistantArtifact!!.text.contains("you tried", true))
        assertEquals(PlanOutcome.NOT_ATTEMPTED, summary.therapyObservation!!.planOutcome.value)
    }

    private fun practicalPlan(h: CTV215Harness): Conversation {
        val c = Conversation(h, RequestedOrdinarySupport.PRACTICAL_HELP)
        action(c.send("I want to begin"), "ask-problem-description")
        action(c.send("My specific concern is: arranging the project meeting"), "verify-problem-understanding")
        action(c.send("Yes, that's right."), "ask-influenceable-part")
        action(c.send("I can influence: when I contact the manager"), "ask-readiness-for-options")
        action(c.send("I am willing to act"), "invite-user-options")
        val options = c.send("My options are: email the manager; call the manager")
        action(options, "ask-user-to-choose-option")
        assertNull(options.therapyObservation!!.selectedOption.value)
        action(c.send("I choose: email the manager"), "develop-bounded-plan")
        val plan = c.send("My first step is: email the manager; when: tomorrow morning")
        action(plan, "wait-for-outcome")
        assertNull(plan.therapyObservation!!.planOutcome.value)
        assertEquals("email the manager", plan.therapyObservation!!.selectedOption.value)
        return c
    }

    @Test fun correctionWithdrawsTentativeMeaningBeforeAcceptingReplacement() = CTV215Harness().use { h ->
        val c = Conversation(h, RequestedOrdinarySupport.UNDERSTAND)
        val first = c.send("My specific concern is: the cancelled meeting")
        action(first, "verify-tentative-understanding")
        val correction = c.send("No, that's not what I mean")
        action(correction, "acknowledge-correction")
        assertNull(correction.therapyObservation!!.thomasUnderstanding.value)
        assertTrue(correction.therapyObservation!!.withdrawnInterpretationReferences.containsAll(first.therapyObservation!!.thomasUnderstanding.evidenceReferences))
        action(c.send("What I mean is: the meeting was delayed"), "verify-tentative-understanding")
        val confirmed = c.send("Yes, that's right")
        action(confirmed, "summarize-shared-understanding")
        assertFalse(confirmed.assistantArtifact!!.text.contains("cancelled"))
        assertTrue(confirmed.assistantArtifact!!.text.contains("delayed"))
    }

    @Test fun reluctancePauseAndResumeAreExplicit() = CTV215Harness().use { h ->
        val c = Conversation(h)
        action(c.send("My specific concern is: the project meeting"), "reflect-established-content")
        action(c.send("Please pause"), "pause-without-response")
        action(c.send("I am ready to resume"), "invite-further-expression")
        action(c.send("I don't want to discuss this"), "pause-without-response")
        action(c.send("Stop"), "acknowledge-close")
    }

    @Test fun unchangedAndStylisticEvidenceReachStagnationAndNewDetailReleasesIt() = CTV215Harness().use { h ->
        val c = Conversation(h)
        val first = c.send("My specific concern is: the project meeting")
        action(first, "reflect-established-content")
        val repeat = c.send("My specific concern is: THE project meeting!")
        action(repeat, "offer-direction-choice")
        assertEquals(first.therapyObservation!!.conversationRevision, repeat.therapyObservation!!.conversationRevision)
        val stop = c.send("My specific concern is: the project meeting.")
        assertEquals(ProgressionDisposition.STOP_NO_PROGRESS, stop.therapyPlan?.routeDecision?.progression)
        val changed = c.send("Another detail is: the manager moved the date")
        action(changed, "reflect-established-content")
        assertTrue(changed.therapyObservation!!.conversationRevision > repeat.therapyObservation!!.conversationRevision)
    }

    @Test fun unknownSafetyAndBroadCheckboxNeverManufactureAbsence() = CTV215Harness().use { h ->
        val result = h.runtime.submit(h.turn(1, ProductionThomasMode.THERAPY, "My specific concern is: the meeting") {
            copy(therapySafetyDeclaration = TherapySafetyDeclaration.ORDINARY_NON_EMERGENCY_ADULT_CONTEXT)
        })
        assertNull(result.therapyPlan?.routeDecision)
        assertTrue(result.safetyObservation!!.observations.all { it.resolution == SafetyEvidenceResolution.UNKNOWN && it.evidenceReferences.isEmpty() })
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, result.safetyObservation!!.authorityState)
    }

    @Test fun declarationsRemainReportedAndCurrentConflictStopsPolicy() = CTV215Harness().use { h ->
        val c = Conversation(h)
        val first = c.send("My specific concern is: the meeting")
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, first.safetyObservation!!.authorityState)
        assertTrue(first.safetyObservation!!.observations.all { it.origin == SafetyEvidenceOrigin.DIRECT_USER_REPORT && it.evidenceReferences.single().contains("explicit-declaration") })
        val conflict = c.send("Self-harm is relevant now.")
        assertNull(conflict.therapyPlan?.routeDecision)
        val observation = conflict.safetyObservation!!.observations.single { it.field == SafetyField.SELF_HARM_RELEVANCE }
        assertEquals(SafetyEvidenceResolution.CONTRADICTORY, observation.resolution)
        assertEquals(2, observation.evidenceReferences.size)
        val corrected = c.send("Correction: Self-harm is relevant now.")
        assertEquals(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, corrected.safetyObservation!!.authorityState)
        assertNull(corrected.therapyPlan?.routeDecision)
    }

    @Test fun tentativeRefusedWithdrawnAndQuotedSafetyRemainDistinct() = CTV215Harness().use { h ->
        val c = Conversation(h)
        c.send("My specific concern is: the meeting")
        val tentative = c.send("I am unsure: Self-harm is relevant now.")
        assertEquals(SafetyEvidenceResolution.TENTATIVE, tentative.safetyObservation!!.observations.single { it.field == SafetyField.SELF_HARM_RELEVANCE }.resolution)
        val refused = c.send("I decline to state: Self-harm is relevant now.")
        assertEquals(SafetyEvidenceResolution.USER_DECLINED, refused.safetyObservation!!.observations.single { it.field == SafetyField.SELF_HARM_RELEVANCE }.resolution)
        val withdrawn = c.send("I withdraw: Self-harm is relevant now.")
        assertEquals(SafetyEvidenceResolution.UNKNOWN, withdrawn.safetyObservation!!.observations.single { it.field == SafetyField.SELF_HARM_RELEVANCE }.resolution)
        val quoted = c.send("\"Self-harm is not relevant now.\"")
        assertEquals(SafetyEvidenceResolution.UNKNOWN, quoted.safetyObservation!!.observations.single { it.field == SafetyField.SELF_HARM_RELEVANCE }.resolution)
    }

    @Test fun noButtonEstablishesConcernPlanOrEngagement() = CTV215Harness().use { h ->
        val result = Conversation(h, RequestedOrdinarySupport.PRACTICAL_HELP).send("Unclassified prose")
        assertNull(result.therapyPlan?.routeDecision?.selectedActionId)
        val s = result.therapyObservation!!
        assertNull(s.concernStatement.value); assertNull(s.problemClarity.value); assertNull(s.engagement.value)
        assertNull(s.generatedOptions.value); assertNull(s.actionPlan.value); assertNull(s.planOutcome.value)
    }

    @Test fun reopenKeepsEvidenceButLosesSafetyAndProcedure() = CTV215Harness().use { h ->
        val c = practicalPlan(h)
        val count = h.runtime.sourceSummaries().size
        h.reopen()
        assertEquals(count, h.runtime.sourceSummaries().size)
        val after = c.send("I attempted the plan", scope = false)
        assertNull(after.therapyObservation!!.actionPlan.value)
        assertNull(after.therapyObservation!!.planOutcome.value)
        assertNull(after.therapyPlan?.routeDecision)
        assertTrue(after.safetyObservation!!.observations.all { it.resolution == SafetyEvidenceResolution.UNKNOWN })
    }

    private fun history(h: CTV215Harness) {
        h.runtime.submit(h.turn(1, ProductionThomasMode.JOURNAL, "I moved to Denver in 2010."))
        h.runtime.submit(h.turn(2, ProductionThomasMode.JOURNAL, "I moved to Portland in 2018."))
    }

    @Test fun groundedTargetAndMaterialAnswerChangeCoverageAndNextQuestion() = CTV215Harness().use { h ->
        history(h)
        val prompt = requireNotNull(h.runtime.nextBiographerPrompt(3))
        assertNotNull(prompt.targetId)
        assertEquals(InvestigationTargetKind.TEMPORAL_GAP, prompt.decision!!.coverageMap.selectedTarget!!.kind)
        assertTrue(prompt.text, prompt.text.contains("2010") && prompt.text.contains("2018"))
        val answer = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, "I moved to Seattle in 2014."))
        assertEquals(InvestigationAnswerDisposition.ANSWERED_RELEVANT, answer.biographerAnswer!!.disposition)
        assertTrue(answer.biographerAnswer!!.materialEvidenceChanged)
        assertNotNull(answer.nextBiographerTargetId)
        assertNotEquals(prompt.targetId, answer.nextBiographerTargetId)
        assertEquals(AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE, h.runtime.sourceSummaries().first().acquisitionMode)
        h.reopen()
        val rebuilt = requireNotNull(h.runtime.nextBiographerPrompt(5))
        assertNotEquals(prompt.targetId, rebuilt.targetId)
    }

    @Test fun nonanswerDoesNotCoverAndOperationalSkipDoesNotBecomeEvidence() = CTV215Harness().use { h ->
        history(h)
        val target = requireNotNull(h.runtime.nextBiographerPrompt(3)).targetId
        val before = h.runtime.sourceSummaries().size
        val skipped = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, "Skip"))
        assertEquals(InvestigationAnswerDisposition.SKIPPED, skipped.biographerAnswer!!.disposition)
        assertFalse(skipped.biographerAnswer!!.materialEvidenceChanged)
        assertEquals(before, h.runtime.sourceSummaries().size)
        assertNotEquals(target, skipped.nextBiographerTargetId)
        h.reopen()
        assertEquals(target, h.runtime.nextBiographerPrompt(5)?.targetId)
    }

    @Test fun declineAndPrivacySurviveReopenWithoutPsychologicalEvidence() {
        for (reply in listOf("I decline", "This topic is private")) CTV215Harness().use { h ->
            history(h)
            val prompt = requireNotNull(h.runtime.nextBiographerPrompt(3))
            val before = h.runtime.sourceSummaries().size
            val answer = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, reply))
            assertTrue(answer.biographerAnswer!!.disposition in setOf(InvestigationAnswerDisposition.DECLINED, InvestigationAnswerDisposition.MARKED_PRIVATE))
            assertNotEquals(prompt.targetId, answer.nextBiographerTargetId)
            assertEquals(before, h.runtime.sourceSummaries().size)
            h.reopen()
            assertNotEquals(prompt.targetId, h.runtime.nextBiographerPrompt(5)?.targetId)
        }
    }

    @Test fun biographyRecallPreservesSourceUncertaintyAndCurrentRoute() = CTV215Harness().use { h ->
        history(h)
        assertNotNull(h.runtime.nextBiographerPrompt(3)?.targetId)
        val answer = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, "Around 2014 I changed jobs."))
        assertNotNull(answer.committedSourceId)
        val c = Conversation(h)
        val recall = c.send("My specific concern is: remembering jobs\nPlease recall my earlier words: Around 2014 I changed jobs.", memory = TherapyMemoryIntent.EXPLICIT_RECALL)
        assertEquals(OrdinaryRoute.LISTEN_SUPPORT, recall.therapyPlan?.routeDecision?.route)
        val memories = recall.therapyPlan!!.surfacedMemories
        assertTrue("plan=${recall.therapyPlan}", memories.any { it.acquisitionMode == AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE })
        assertTrue(h.runtime.snapshot().formedState.activeExplicitClaims.any { it.eventTime is EventTime.ApproximateYear })
        assertTrue(memories.any { it.exactSourceExcerpt?.contains("Around 2014") == true })
        assertEquals(recall.renderResult.toString(), ProductionTurnDisposition.COMPLETED, recall.disposition)
        assertTrue(recall.assistantArtifact!!.text.contains("jobs"))
        h.runtime.changeSourcePrivacy(answer.committedSourceId!!, true, 250)
        h.reopen()
        val privateRecall = Conversation(h).send("My specific concern is: remembering jobs\nPlease recall my earlier words: Around 2014 I changed jobs.", memory = TherapyMemoryIntent.EXPLICIT_RECALL)
        assertTrue(privateRecall.therapyPlan!!.surfacedMemories.none { it.acquisitionMode == AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE })
        h.runtime.deleteSource(answer.committedSourceId!!, 251)
        h.reopen()
        assertTrue(h.runtime.snapshot().formedState.activeExplicitClaims.none { it.eventTime is EventTime.ApproximateYear })
    }


    @Test fun safetyClarificationAnswersOnlyTheDeliveredField() = CTV215Harness().use { h ->
        val c = Conversation(h)
        val first = c.send("My specific concern is: the meeting", scope = false)
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, first.safetyObservation!!.nextExpectedEvidence)
        for (field in listOf(SafetyField.CURRENT_EMERGENCY, SafetyField.ACUTE_MEDICAL_EMERGENCY,
                SafetyField.SELF_HARM_RELEVANCE, SafetyField.HARM_TO_OTHERS_RELEVANCE, SafetyField.SPECIALIZED_SCOPE_CONDITION)) {
            val answered = c.send("No", scope = false)
            assertEquals(SafetyEvidenceResolution.ESTABLISHED, answered.safetyObservation!!.observations.single { it.field == field }.resolution)
            assertNull(answered.therapyPlan?.routeDecision)
            assertTrue(answered.safetyObservation!!.observations.single { it.field == field }.evidenceReferences.single().contains("reply-to-"))
        }
        val population = c.send("Yes", scope = false)
        assertEquals(SafetyInformationRequirement.PRESENTING_SCOPE, population.safetyObservation!!.nextExpectedEvidence)
        val allowed = c.send("Yes", scope = false)
        action(allowed, "reflect-established-content")
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, allowed.safetyObservation!!.authorityState)
    }

    @Test fun safetyClarificationAcceptsContextualNegativeAnswerAndExits() = CTV215Harness().use { h ->
        val c = Conversation(h)
        val first = c.send("I had a frustrating day at work.", scope = false)
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, first.safetyObservation!!.nextExpectedEvidence)
        val answered = c.send("No, there is no emergency.", scope = false)
        assertEquals(SafetyEvidenceResolution.ESTABLISHED,
            answered.safetyObservation!!.observations.single { it.field == SafetyField.CURRENT_EMERGENCY }.resolution)
        assertNotEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, answered.safetyObservation!!.nextExpectedEvidence)
    }
    @Test fun contextualNegativeSafetyRepliesAreBoundToTheDeliveredRequirement() {
        for (reply in listOf(
            "There is no current emergency.", "I am safe.", "I'm safe.", "No emergency.",
            "No, there is no emergency.", "No, nothing like that is happening.",
            "No, I am not in danger.", "Nothing is happening right now. I just want to talk about my day.",
        )) CTV215Harness().use { h ->
            val c = Conversation(h)
            val first = c.send("I had a frustrating day at work.", scope = false)
            assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, first.safetyObservation!!.nextExpectedEvidence)
            val answered = c.send(reply, scope = false)
            val observation = answered.safetyObservation!!.observations.single { it.field == SafetyField.CURRENT_EMERGENCY }
            assertEquals(SafetyEvidenceResolution.ESTABLISHED, observation.resolution)
            assertEquals(ExplicitEmergencyCircumstance.NONE_ESTABLISHED.toString(), observation.value.toString())
            assertNotEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, answered.safetyObservation!!.nextExpectedEvidence)
        }
    }
    @Test fun contradictoryAndAmbiguousSafetyRepliesRemainUnknown() = CTV215Harness().use { h ->
        val c = Conversation(h)
        c.send("I had a frustrating day at work.", scope = false)
        for (reply in listOf("No, but there is an emergency.", "I think everything is fine.")) {
            val result = c.send(reply, scope = false)
            assertEquals(SafetyEvidenceResolution.UNKNOWN,
                result.safetyObservation!!.observations.single { it.field == SafetyField.CURRENT_EMERGENCY }.resolution)
            assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, result.safetyObservation!!.nextExpectedEvidence)
        }
    }
    @Test fun contextualNegativeReplyDoesNotCrossBindToAnotherSafetyRequirement() = CTV215Harness().use { h ->
        val c = Conversation(h)
        c.send("I had a frustrating day at work.", scope = false)
        val current = c.send("No", scope = false)
        assertEquals(SafetyInformationRequirement.ACUTE_MEDICAL_EMERGENCY_STATUS, current.safetyObservation!!.nextExpectedEvidence)
        val unrelated = c.send("No emergency.", scope = false)
        assertEquals(SafetyEvidenceResolution.UNKNOWN,
            unrelated.safetyObservation!!.observations.single { it.field == SafetyField.ACUTE_MEDICAL_EMERGENCY }.resolution)
        assertEquals(SafetyInformationRequirement.ACUTE_MEDICAL_EMERGENCY_STATUS, unrelated.safetyObservation!!.nextExpectedEvidence)
    }
    @Test fun pendingSafetyClarificationIsFreshAfterRestart() = CTV215Harness().use { h ->
        Conversation(h).send("I had a frustrating day at work.", scope = false)
        h.reopen()
        val afterRestart = Conversation(h).send("I am safe.", scope = false)
        assertEquals(SafetyEvidenceResolution.UNKNOWN,
            afterRestart.safetyObservation!!.observations.single { it.field == SafetyField.CURRENT_EMERGENCY }.resolution)
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, afterRestart.safetyObservation!!.nextExpectedEvidence)
    }
    @Test fun satisfiedSafetyClarificationIsNotResurrectedAfterRestart() = CTV215Harness().use { h ->
        val c = Conversation(h)
        c.send("I had a frustrating day at work.", scope = false)
        val answered = c.send("I am safe.", scope = false)
        assertEquals(SafetyEvidenceResolution.ESTABLISHED,
            answered.safetyObservation!!.observations.single { it.field == SafetyField.CURRENT_EMERGENCY }.resolution)
        h.reopen()
        val afterRestart = Conversation(h).send("My manager changed priorities halfway through the afternoon.", scope = false)
        assertEquals(SafetyEvidenceResolution.UNKNOWN,
            afterRestart.safetyObservation!!.observations.single { it.field == SafetyField.CURRENT_EMERGENCY }.resolution)
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, afterRestart.safetyObservation!!.nextExpectedEvidence)
    }
    @Test fun contextualSafetyReplyPreservesTheExistingClarificationAndResumeProgression() = CTV215Harness().use { h ->
        val c = Conversation(h)
        c.send("My specific concern is: the meeting", scope = false)
        val current = c.send("No, there is no emergency.", scope = false)
        assertEquals(SafetyInformationRequirement.ACUTE_MEDICAL_EMERGENCY_STATUS, current.safetyObservation!!.nextExpectedEvidence)
        repeat(4) { c.send("No", scope = false) }
        assertEquals(SafetyInformationRequirement.PRESENTING_SCOPE, c.send("Yes", scope = false).safetyObservation!!.nextExpectedEvidence)
        val allowed = c.send("Yes", scope = false)
        action(allowed, "reflect-established-content")
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, allowed.safetyObservation!!.authorityState)
    }
    @Test fun speechTranscriptUsesTheSameSafetyAndSubmissionPathAsTypedInput() = CTV215Harness().use { spoken ->
        val typed = CTV215Harness()
        try {
            val typedFirst = typed.runtime.submit(typed.turn(1, ProductionThomasMode.THERAPY,
                "I had a frustrating day at work."))
            val spokenFirst = spoken.runtime.submit(spoken.turn(1, ProductionThomasMode.THERAPY,
                "I had a frustrating day at work").copy(inputOrigin = ProductionInputOrigin.SPEECH_TRANSCRIPT))
            assertEquals(typedFirst.safetyObservation!!.nextExpectedEvidence,
                spokenFirst.safetyObservation!!.nextExpectedEvidence)

            val typedAnswer = typed.runtime.submit(typed.turn(2, ProductionThomasMode.THERAPY,
                "I am safe."))
            val spokenAnswer = spoken.runtime.submit(spoken.turn(2, ProductionThomasMode.THERAPY,
                "I am safe.").copy(inputOrigin = ProductionInputOrigin.SPEECH_TRANSCRIPT))
            assertEquals(SafetyEvidenceResolution.ESTABLISHED,
                spokenAnswer.safetyObservation!!.observations.single { it.field == SafetyField.CURRENT_EMERGENCY }.resolution)
            assertEquals(typedAnswer.safetyObservation!!.nextExpectedEvidence,
                spokenAnswer.safetyObservation!!.nextExpectedEvidence)
            assertEquals(typedAnswer.therapyPlan?.routeDecision?.selectedActionId,
                spokenAnswer.therapyPlan?.routeDecision?.selectedActionId)
            assertEquals(typedAnswer.disposition, spokenAnswer.disposition)
        } finally {
            typed.close()
        }
    }
    @Test fun restartDoesNotHydrateAnObsoleteSafetyClarification() = CTV215Harness().use { h ->
        val c = Conversation(h)
        val first = c.send("I had a frustrating day at work.", scope = false)
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, first.safetyObservation!!.nextExpectedEvidence)
        h.reopen()
        val afterRestart = Conversation(h).send("My manager changed priorities halfway through the afternoon.", scope = false)
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, afterRestart.safetyObservation!!.nextExpectedEvidence)
    }
    @Test fun historicalQuotedDeclarationBlockCannotEstablishCurrentSafety() = CTV215Harness().use { h ->
        val result = Conversation(h).send("Historical quotation:\n" + DECLARATIONS, scope = false)
        assertTrue(result.safetyObservation!!.observations.all { it.resolution == SafetyEvidenceResolution.UNKNOWN })
        assertNull(result.therapyPlan?.routeDecision)
    }

    @Test fun ambiguousSafetyPopulationNoDoesNotInventAnAge() = CTV215Harness().use { h ->
        val c = Conversation(h)
        c.send("My specific concern is: the meeting")
        c.send("I withdraw: I am an adult in the supported setting.")
        val no = c.send("No")
        assertEquals(SafetyEvidenceResolution.UNKNOWN, no.safetyObservation!!.observations.single { it.field == SafetyField.POPULATION_APPLICABILITY }.resolution)
    }

    @Test fun optionsReadinessAndUnchosenPlansCannotBeManufactured() = CTV215Harness().use { h ->
        val c = Conversation(h, RequestedOrdinarySupport.PRACTICAL_HELP)
        action(c.send("My specific concern is: the meeting"), "verify-problem-understanding")
        action(c.send("Yes"), "ask-influenceable-part")
        action(c.send("I can influence: contacting the manager"), "ask-readiness-for-options")
        val willingToExplore = c.send("I am willing to consider options")
        assertNull(willingToExplore.therapyObservation!!.willingness.value)
        action(c.send("I am willing to act"), "invite-user-options")
        action(c.send("My options are: email; call"), "ask-user-to-choose-option")
        val unchosen = c.send("I choose: quit")
        assertNull(unchosen.therapyObservation!!.selectedOption.value)
        assertNull(unchosen.therapyObservation!!.actionPlan.value)
        assertNull(unchosen.therapyObservation!!.planOutcome.value)
    }

    @Test fun biographerDeferralNonanswerAndStopDoNotBecomeProgress() {
        for ((text, outcome) in listOf("Later" to InvestigationAnswerDisposition.DEFERRED,
                "I don't know" to InvestigationAnswerDisposition.NO_EXTRACTABLE_EVIDENCE,
                "Stop" to InvestigationAnswerDisposition.STOPPED)) CTV215Harness().use { h ->
            history(h)
            val target = requireNotNull(h.runtime.nextBiographerPrompt(3)).targetId
            val answer = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, text))
            assertEquals(outcome, answer.biographerAnswer!!.disposition)
            assertFalse(answer.biographerAnswer!!.materialEvidenceChanged)
            assertNotEquals(target, answer.nextBiographerTargetId)
            if (outcome == InvestigationAnswerDisposition.STOPPED) {
                assertNull(answer.assistantArtifact)
                assertNull(h.runtime.nextBiographerPrompt(5))
            }
        }
    }

    @Test fun staleBiographerTargetDoesNotBindAnswerAfterDeletion() = CTV215Harness().use { h ->
        history(h)
        assertNotNull(h.runtime.nextBiographerPrompt(3)?.targetId)
        val removed = h.runtime.sourceSummaries().first().stableSourceId
        assertTrue(h.runtime.deleteSource(removed, 4).accepted)
        val answer = h.runtime.submit(h.turn(5, ProductionThomasMode.BIOGRAPHER, "I moved to Seattle in 2014."))
        assertEquals(AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, answer.biographerAnswer!!.capture!!.receipt!!.acquisitionMode)
        assertNull(answer.biographerAnswer!!.capture!!.receipt!!.targetId)
    }


    @Test fun repeatedDatedBiographyDoesNotManufactureCoverageProgress() = CTV215Harness().use { h ->
        history(h)
        val target = requireNotNull(h.runtime.nextBiographerPrompt(3)).targetId
        val repeat = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, "I moved to Denver in 2010."))
        assertFalse(repeat.biographerAnswer!!.materialEvidenceChanged)
        assertEquals(InvestigationAnswerDisposition.NO_EXTRACTABLE_EVIDENCE, repeat.biographerAnswer!!.disposition)
        h.reopen()
        assertEquals(target, h.runtime.nextBiographerPrompt(5)?.targetId)
    }

    @Test fun historicalProceduralBlockCannotCreateConversationState() = CTV215Harness().use { h ->
        val result = Conversation(h).send("Historical quotation:\nMy specific concern is: old instructions\nI am willing to act", scope = false)
        assertNull(result.therapyObservation!!.concernStatement.value)
        assertNull(result.therapyObservation!!.willingness.value)
        assertNull(result.therapyPlan?.routeDecision)
    }

    @Test fun establishedCurrentSafetyInterruptionBlocksBiographerInvestigation() = CTV215Harness().use { h ->
        history(h)
        assertNotNull(h.runtime.nextBiographerPrompt(3)?.targetId)
        val result = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, "This is a current emergency."))
        assertNotNull(result.committedSourceId)
        assertNull(result.assistantArtifact)
        assertNull(h.runtime.nextBiographerPrompt(5))
    }


    @Test fun custodyDeletionInvalidatesEphemeralConcernAndSafety() = CTV215Harness().use { h ->
        val c = Conversation(h, RequestedOrdinarySupport.UNDERSTAND)
        val first = c.send("My specific concern is: the cancelled meeting")
        action(first, "verify-tentative-understanding")
        assertTrue(h.runtime.deleteSource(first.committedSourceId!!, 200).accepted)
        val after = c.send("Yes")
        assertNull(after.therapyObservation!!.concernStatement.value)
        assertNull(after.therapyObservation!!.thomasUnderstanding.value)
        assertNull(after.therapyPlan?.routeDecision)
        assertTrue(after.safetyObservation!!.observations.all { it.resolution == SafetyEvidenceResolution.UNKNOWN })
    }

    @Test fun undeliveredUnderstandingCannotBeConfirmedByAShortReply() = CTV215Harness().use { h ->
        val c = Conversation(h, RequestedOrdinarySupport.UNDERSTAND)
        val first = c.send("My specific concern is: you should quit your job")
        assertEquals(ProductionTurnDisposition.RENDERING_UNAVAILABLE, first.disposition)
        val reply = c.send("Yes")
        assertFalse(reply.therapyObservation!!.sharedUnderstanding.isEstablished())
        assertNull(reply.therapyObservation!!.pendingInformation)
        assertTrue(reply.therapyObservation!!.actionHistory.isEmpty())
    }

    @Test fun biographerCorrectionRebuildsTargetsFromCurrentRevision() = CTV215Harness().use { h ->
        history(h)
        h.runtime.nextBiographerPrompt(3)
        val answer = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, "I moved to Seattle in 2014."))
        assertTrue(h.runtime.reviseSource(answer.committedSourceId!!, "I moved to Seattle in 2015.", 6,
            ReportTime(Instant.parse("2040-02-03T00:00:00Z"))).accepted)
        h.reopen()
        val prompt = requireNotNull(h.runtime.nextBiographerPrompt(7))
        assertTrue(prompt.text.contains("2015"))
        assertFalse(prompt.text.contains("2014"))
        assertTrue(h.runtime.snapshot().formedState.activeExplicitClaims.none { it.eventTime.toString().contains("2014") })
    }


    @Test fun journalRecallIsVisibleAndRetainsProvenance() = CTV215Harness().use { h ->
        h.runtime.submit(h.turn(1, ProductionThomasMode.JOURNAL, "I moved to Denver in 2010."))
        h.reopen()
        val result = Conversation(h).send("My specific concern is: remembering Denver\nPlease recall my earlier words: I moved to Denver in 2010.", memory = TherapyMemoryIntent.EXPLICIT_RECALL)
        action(result, "reflect-established-content")
        assertTrue(result.therapyPlan!!.surfacedMemories.all { it.acquisitionMode == AcquisitionMode.JOURNAL })
        assertTrue(result.assistantArtifact!!.text.contains("Journal"))
        assertTrue(result.assistantArtifact!!.text.contains("Denver"))
    }

    @Test fun unresolvedIdentityPromptNamesItsActualEligibleTarget() = CTV215Harness().use { h ->
        h.runtime.submit(h.turn(1, ProductionThomasMode.JOURNAL, "I think Alex was angry."))
        h.runtime.submit(h.turn(2, ProductionThomasMode.JOURNAL, "Alex told me he was worried."))
        val prompt = requireNotNull(h.runtime.nextBiographerPrompt(3))
        assertNotNull(prompt.targetId)
        assertEquals(InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED, prompt.decision!!.coverageMap.selectedTarget!!.kind)
        assertTrue(prompt.text, prompt.text.contains("Alex"))
        assertTrue(prompt.text, prompt.text.contains("same person"))
        assertEquals(1, prompt.renderResult.questionCount)
        val answer = h.runtime.submit(h.turn(4, ProductionThomasMode.BIOGRAPHER, "I don't know"))
        assertFalse(answer.biographerAnswer!!.materialEvidenceChanged)
        assertTrue(h.runtime.snapshot().formedState.unresolvedIdentities.isNotEmpty())
    }

    @Test fun biographerShortAnswerCannotAnswerAPendingTherapySafetyQuestion() = CTV215Harness().use { h ->
        val first = h.runtime.submit(h.turn(1, ProductionThomasMode.THERAPY, "My specific concern is: the meeting"))
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS, first.safetyObservation!!.nextExpectedEvidence)
        h.runtime.submit(h.turn(2, ProductionThomasMode.BIOGRAPHER, "No"))
        val later = h.runtime.submit(h.turn(3, ProductionThomasMode.THERAPY, "I want to begin"))
        assertTrue(later.safetyObservation!!.observations.all { it.resolution == SafetyEvidenceResolution.UNKNOWN })
        assertNull(later.therapyPlan?.routeDecision)
    }
    @Test fun internalNumericPunctuationCannotCollapseDifferentEvidence() = CTV215Harness().use { h ->
        val c = Conversation(h)
        val first = c.send("My specific concern is: a delay of 1.5 hours")
        action(first, "reflect-established-content")
        val changed = c.send("My specific concern is: a delay of 15 hours")
        action(changed, "reflect-established-content")
        assertTrue(changed.therapyObservation!!.conversationRevision > first.therapyObservation!!.conversationRevision)
        assertEquals("a delay of 15 hours", changed.therapyObservation!!.concernStatement.value)
    }
    companion object {
        const val DECLARATIONS = "There is no current emergency.\nThere is no acute medical emergency.\nSelf-harm is not relevant now.\nHarm to others is not relevant now.\nI report no specialized condition for this conversation.\nI am an adult in the supported setting.\nMy present concern is one bounded ordinary personal problem."
    }
}
