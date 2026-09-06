package com.conundrum.thomas.v2

import android.os.Bundle
import android.os.Process
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.conundrum.thomas.v2.biographer.*
import com.conundrum.thomas.v2.engine.ordinary.*
import com.conundrum.thomas.v2.engine.verticalslice.*
import com.conundrum.thomas.v2.languagerenderer.*
import com.conundrum.thomas.v2.longitudinal.*
import com.conundrum.thomas.v2.platform.persistence.AndroidKeystorePersonalDataKeyProvider
import com.conundrum.thomas.v2.runtime.*
import com.conundrum.thomas.v2.safety.*
import com.conundrum.thomas.v2.therapylongitudinal.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** Actual Android root and submit boundary used by ThomasViewModel; no synthetic internal state.
 * Run a from an empty fixture, fixture-only force-stop, then b. UI controls are tested separately. */
@RunWith(AndroidJUnit4::class)
class CTV215R1ProductionDeviceInstrumentedTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val prefs get() = context.getSharedPreferences("ct-v2-15r1-physical-fixture", 0)
    private lateinit var runtime: ThomasProductionRuntime
    private var index = 1L
    private var route = RequestedOrdinarySupport.LISTEN

    private fun guard() {
        assertEquals(FIXTURE, context.packageName)
        assertEquals("/data/user/0/" + FIXTURE, context.applicationInfo.dataDir)
        val canonicalUid = requireNotNull(InstrumentationRegistry.getArguments().getString("canonicalUid")).toInt()
        assertNotEquals(canonicalUid, Process.myUid())
        assertNull(context.packageManager.getPackageInfo(FIXTURE, 0).sharedUserId)
        trace("ISOLATION uid=" + Process.myUid() + " data=" + context.applicationInfo.dataDir)
    }

    private fun trace(value: String) {
        println("R1 " + value)
        instrumentation.sendStatus(0, Bundle().apply { putString("stream", "\nR1 " + value + "\n") })
    }

    private fun send(text: String, mode: ProductionThomasMode = ProductionThomasMode.THERAPY,
                     scope: Boolean = false, recall: Boolean = false,
                     privacy: ProductionTurnPrivacy = ProductionTurnPrivacy.ELIGIBLE): ProductionTurnResult {
        val turn = index
        index += 2 // Biographer's answer can issue a question at turn+1.
        val result = runtime.submit(ProductionTurnRequest(turn, mode,
            (if (scope) DECLARATIONS + "\n" else "") + text, privacy = privacy,
            requestedTherapySupport = route,
            therapyMemoryIntent = if (recall) TherapyMemoryIntent.EXPLICIT_RECALL else TherapyMemoryIntent.ORDINARY,
            committedAt = Instant.now()))
        trace("TURN=" + turn + " input=" + text + " mode=" + mode + " result=" + result.disposition +
            " revision=" + result.therapyObservation?.conversationRevision +
            " route=" + result.therapyPlan?.routeDecision?.route + " action=" + result.therapyPlan?.routeDecision?.selectedActionId +
            " progression=" + result.therapyPlan?.routeDecision?.progression + " pending=" + result.therapyObservation?.pendingInformation +
            " safety=" + result.safetyObservation?.authorityState + " bio=" + result.biographerAnswer?.disposition +
            " nextTarget=" + result.nextBiographerTargetId + " render=" + result.renderResult?.semanticAct +
            "/" + result.renderResult?.disposition + " validation=" + result.renderResult?.validation?.reasonCodes + " reasons=" + result.reasonCodes +
            " text=" + result.assistantArtifact?.text)
        assertNotEquals(result.reasonCodes.toString(), ProductionTurnDisposition.PERSISTENCE_UNAVAILABLE, result.disposition)
        return result
    }

    private fun render(result: GovernedRenderResult, command: GovernedRenderCommand) {
        assertEquals(command.mode, result.mode)
        assertEquals(command.semanticAct, result.semanticAct)
        assertTrue(result.validation.toString(), result.validation.accepted)
        assertEquals(listOf(RenderValidationReason.VALID), result.validation.reasonCodes)
        assertTrue(result.questionCount <= command.budget.maximumQuestions)
        assertTrue(result.sentenceCount <= command.budget.maximumSentences)
        assertTrue(result.characterCount <= command.budget.maximumCharacters)
        assertEquals(command.historicalSupport.map { it.memoryObjectId }, result.surfacedMemoryIds)
        if (command.semanticAct == GovernedSemanticAct.NO_RESPONSE) {
            assertEquals(RenderDisposition.NO_RESPONSE, result.disposition)
            assertNull(result.finalText)
        } else {
            when (result.disposition) {
                RenderDisposition.ACCEPTED_REFERENCE_REALIZATION -> {
                    assertFalse(result.fallbackUsed)
                    assertEquals(AcceptedRealizationSource.REFERENCE, result.realizationSource)
                }
                RenderDisposition.FALLBACK_REALIZATION -> {
                    assertTrue(result.fallbackUsed)
                    assertEquals(AcceptedRealizationSource.DETERMINISTIC_FALLBACK, result.realizationSource)
                    assertTrue(result.rejectedCandidateReasons.isNotEmpty())
                    trace("VALIDATED_FALLBACK reasons=" + result.rejectedCandidateReasons)
                }
                else -> fail("Unaccepted rendering disposition: " + result.disposition)
            }
            val text = requireNotNull(result.finalText)
            assertTrue("Not an authorized realization: " + text, text in command.authorizedReferenceRealizations)
            command.historicalSupport.forEach { h -> assertTrue(h.attributionMarkers.any { text.contains(it, true) }) }
            command.epistemicConstraints.forEach { e -> assertTrue(e.requiredMarkers.any { text.contains(it, true) }) }
            command.temporalConstraints.forEach { t ->
                assertTrue(t.requiredMarkers.any { text.contains(it, true) })
                assertTrue(t.forbiddenPrecisionLiterals.none { text.contains(it, true) })
            }
        }
    }

    private fun action(result: ProductionTurnResult, id: String): ProductionTurnResult {
        val plan = requireNotNull(result.therapyPlan)
        assertEquals("core-" + id, plan.routeDecision?.selectedActionId)
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, result.safetyObservation!!.authorityState)
        assertTrue(result.therapyObservation!!.validationErrors().isEmpty())
        val support = requireNotNull(plan.renderSupport)
        val command = TherapyRenderCommandAdapter.adapt(RenderCommandId.parse("fixture.oracle"), 0, support)
        render(requireNotNull(result.renderResult), command)
        assertEquals(if (command.semanticAct == GovernedSemanticAct.NO_RESPONSE)
            ProductionTurnDisposition.NO_RESPONSE else ProductionTurnDisposition.COMPLETED, result.disposition)
        assertEquals(result.renderResult!!.finalText, result.assistantArtifact?.text)
        if (result.assistantArtifact != null) assertEquals(ProductionThomasMode.THERAPY, result.assistantArtifact!!.mode)
        assertEquals(plan.surfacedMemories, support.surfacedMemorySupport)
        return result
    }

    private fun safety(result: ProductionTurnResult, state: SafetyAuthorityState) {
        val decision = requireNotNull(result.safetyObservation)
        assertEquals(state, decision.authorityState)
        assertNull(result.therapyPlan?.routeDecision)
        assertTrue(result.therapyPlan!!.surfacedMemories.isEmpty())
        val fixed = SafetyRenderRequestFactory.create(decision)
        render(requireNotNull(result.renderResult), SafetyRenderCommandAdapter.adapt(
            RenderCommandId.parse("fixture.safety"), 0, fixed.command, fixed.authorizedSupportingText))
        trace("SAFETY observations=" + decision.observations)
    }

    private fun prompt(): ProductionBiographerPrompt {
        val p = requireNotNull(runtime.nextBiographerPrompt(index))
        index += 2
        trace("PROMPT_DECISION=" + p.decision + " render=" + p.renderResult)
        val target = requireNotNull(p.decision!!.coverageMap.selectedTarget)
        assertEquals(target.id.value, p.targetId)
        assertEquals(TargetEligibility.ELIGIBLE, target.eligibility)
        assertTrue(target.groundingIds.isNotEmpty())
        assertTrue(p.decision!!.coverageMap.eligibleTargets.any { it.id == target.id })
        assertEquals(target.id, p.decision!!.plan!!.targetId)
        assertEquals(BiographerPosture.TARGETED_COVERAGE, p.decision!!.plan!!.posture)
        assertEquals(GovernedSemanticAct.CLARIFYING_QUESTION, p.renderResult.semanticAct)
        assertEquals(GovernedRenderMode.BIOGRAPHER, p.renderResult.mode)
        val groundingMethod = runtime.javaClass.getDeclaredMethod("biographerGrounding",
            BiographerQuestionPlan::class.java, InvestigationTarget::class.java).apply { isAccessible = true }
        val grounding = requireNotNull(groundingMethod.invoke(runtime, p.decision!!.plan, target)) as RenderableGrounding
        render(p.renderResult, BiographerRenderCommandAdapter.adapt(RenderCommandId.parse("fixture.biographer"), 0,
            requireNotNull(p.decision!!.plan), grounding))
        assertTrue(p.renderResult.validation.accepted)
        assertEquals(1, p.renderResult.questionCount)
        assertTrue(p.renderResult.sentenceCount <= RenderBudget.ONE_QUESTION.maximumSentences)
        assertTrue(p.renderResult.characterCount <= RenderBudget.ONE_QUESTION.maximumCharacters)
        trace("TARGET=" + target.id.value + " kind=" + target.kind + " grounding=" + target.groundingIds +
            " bounds=" + target.temporalBounds + " digest=" + p.decision!!.coverageMap.canonicalDigest + " text=" + p.text)
        return p
    }

    private fun recall(text: String, mode: AcquisitionMode, source: SourceIdentityId): ProductionTurnResult {
        val result = action(send("My specific concern is: remembering " + (if (mode == AcquisitionMode.JOURNAL) "Denver" else "jobs") + "\nPlease recall my earlier words: " + text,
            scope = true, recall = true), "reflect-established-content")
        assertEquals(OrdinaryRoute.LISTEN_SUPPORT, result.therapyPlan!!.routeDecision!!.route)
        val memories = result.therapyPlan!!.surfacedMemories
        assertTrue(memories.isNotEmpty())
        val exact = memories.single { it.exactSourceExcerpt == text }
        assertEquals(mode, exact.acquisitionMode)
        assertTrue(exact.sourceRevisionIds.any { it.value.startsWith(source.value) })
        // Current eligible evidence is not authority to select a Therapy route (asserted above).
        assertTrue(exact.currentAuthority)
        assertTrue(result.assistantArtifact!!.text.contains(text))
        return result
    }

    @Test fun aCompleteTypedProductionScenarios() {
        guard()
        ThomasAndroidCompositionRoot.open(context).use { root ->
            runtime = requireNotNull(root.runtime) { root.unavailableReason.orEmpty() }
            assertTrue("Never clear an unknown corpus", runtime.sourceSummaries().isEmpty())
            assertEquals(0L, runtime.snapshot().storeRevision)
            trace("INITIAL_CORPUS_EMPTY=true")
            val journal = send(JOURNAL, ProductionThomasMode.JOURNAL)
            assertEquals(ProductionTurnDisposition.NO_RESPONSE, journal.disposition)
            val journalId = requireNotNull(journal.committedSourceId)
            send("I moved to Portland in 2018.", ProductionThomasMode.JOURNAL)
            val first = prompt()
            assertEquals(InvestigationTargetKind.TEMPORAL_GAP, first.decision!!.coverageMap.selectedTarget!!.kind)
            assertTrue(first.text.contains("2010") && first.text.contains("2018"))
            val answer = send(BIOGRAPHY, ProductionThomasMode.BIOGRAPHER)
            assertEquals(InvestigationAnswerDisposition.ANSWERED_RELEVANT, answer.biographerAnswer!!.disposition)
            assertTrue(answer.biographerAnswer!!.materialEvidenceChanged)
            assertNotNull(answer.nextBiographerTargetId)
            assertNotEquals(first.targetId, answer.nextBiographerTargetId)
            val biographyId = requireNotNull(answer.committedSourceId)
            assertEquals(AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE, answer.biographerAnswer!!.capture!!.receipt!!.acquisitionMode)
            assertTrue(runtime.snapshot().formedState.activeExplicitClaims.any { it.eventTime is EventTime.ApproximateYear })
            val after = prompt()
            assertNotEquals(first.decision!!.coverageMap.canonicalDigest, after.decision!!.coverageMap.canonicalDigest)
            assertNotEquals(first.targetId, after.targetId)
            val nonanswer = send("I don't know", ProductionThomasMode.BIOGRAPHER)
            assertEquals(InvestigationAnswerDisposition.NO_EXTRACTABLE_EVIDENCE, nonanswer.biographerAnswer!!.disposition)
            assertFalse(nonanswer.biographerAnswer!!.materialEvidenceChanged)
            assertNotEquals(after.targetId, nonanswer.nextBiographerTargetId)
            send("I moved to Austin in 1980.", ProductionThomasMode.JOURNAL)
            send("I moved to Dallas in 1990.", ProductionThomasMode.JOURNAL)
            val declined = prompt()
            val beforeDecline = runtime.sourceSummaries().size
            val decline = send("I decline", ProductionThomasMode.BIOGRAPHER)
            assertEquals(InvestigationAnswerDisposition.DECLINED, decline.biographerAnswer!!.disposition)
            assertEquals(beforeDecline, runtime.sourceSummaries().size)
            assertNotEquals(declined.targetId, decline.nextBiographerTargetId)
            // A decline may legitimately leave no renderable next question; do not manufacture one.

            val unknown = send("My specific concern is: the project meeting")
            safety(unknown, SafetyAuthorityState.CLARIFICATION_REQUIRED)
            assertTrue(unknown.safetyObservation!!.observations.all { it.resolution == SafetyEvidenceResolution.UNKNOWN })

            // A: LISTEN.
            action(send("My specific concern is: the delayed meeting", scope = true), "reflect-established-content")
            action(send("Yes, that's right"), "invite-further-expression")
            action(send("That's all for now"), "summarize-listening")
            action(send("Thank you"), "check-further-or-close")
            action(send("Stop"), "acknowledge-close")

            // E: explicit pause/refusal and resume.
            action(send("My specific concern is: a separate project meeting"), "reflect-established-content")
            val pause = action(send("Please pause"), "pause-without-response")
            assertEquals(OrdinaryEngagement.PAUSE_REQUESTED, pause.therapyObservation!!.engagement.value)
            action(send("I am ready to resume"), "invite-further-expression")
            val reluctant = action(send("I don't want to discuss this"), "pause-without-response")
            assertEquals(OrdinaryEngagement.DOES_NOT_WANT_TOPIC, reluctant.therapyObservation!!.engagement.value)
            action(send("I want to continue"), "invite-further-expression")

            // F/G: formatting does not advance; explicit detail does.
            val initial = action(send("My specific concern is: the weekly project meeting"), "reflect-established-content")
            val repeated = action(send("My specific concern is: THE weekly project meeting!"), "offer-direction-choice")
            assertEquals(initial.therapyObservation!!.conversationRevision, repeated.therapyObservation!!.conversationRevision)
            val stopped = send("My specific concern is: the weekly project meeting.")
            assertEquals(ProgressionDisposition.STOP_NO_PROGRESS, stopped.therapyPlan!!.routeDecision!!.progression)
            assertEquals(initial.therapyObservation!!.conversationRevision, stopped.therapyObservation!!.conversationRevision)
            assertNull(stopped.assistantArtifact)
            val newEvidence = action(send("Another detail is: the manager moved the date"), "reflect-established-content")
            assertTrue(newEvidence.therapyObservation!!.conversationRevision > repeated.therapyObservation!!.conversationRevision)

            // B/D: missing information, tentative understanding, correction, confirmation.
            route = RequestedOrdinarySupport.UNDERSTAND
            action(send("My specific concern is: the cancelled appointment\nWhat I haven't explained is: who changed the time"), "ask-important-missing-piece")
            val tentative = action(send("The missing detail is: the manager changed the time"), "verify-tentative-understanding")
            assertFalse(tentative.therapyObservation!!.thomasUnderstanding.isEstablished())
            val corrected = action(send("No, that's not what I mean"), "acknowledge-correction")
            assertNull(corrected.therapyObservation!!.thomasUnderstanding.value)
            assertTrue(corrected.therapyObservation!!.withdrawnInterpretationReferences.containsAll(
                tentative.therapyObservation!!.thomasUnderstanding.evidenceReferences))
            action(send("What I mean is: the appointment was delayed"), "verify-tentative-understanding")
            val confirmed = action(send("Yes, that's right"), "summarize-shared-understanding")
            assertEquals(SharedUnderstanding.CONFIRMED, confirmed.therapyObservation!!.sharedUnderstanding.value)
            assertFalse(confirmed.assistantArtifact!!.text.contains("cancelled"))
            action(send("Thank you"), "check-understanding-next-direction")

            // C: choice, plan, actual attempt and non-attempt.
            practical(false)
            practical(true)

            // Exhaustive renderer fallback delivered the post-decline target too. All four
            // unchanged gaps have now been offered; CT-V2-10 correctly forbids immediate re-asking.
            val exhausted = requireNotNull(runtime.nextBiographerPrompt(index))
            index += 2
            assertNull(exhausted.targetId)
            assertTrue(exhausted.decision!!.coverageMap.eligibleTargets.isEmpty())
            assertEquals(BiographerPosture.OPEN_STORY, exhausted.decision!!.plan!!.posture)
            trace("COVERAGE_EXHAUSTED=" + exhausted.decision!!.coverageMap)
            // Create a genuinely new eligible gap through the production Journal boundary.
            // Do not clear investigation history or substitute an internal target.
            val freshGapSource = send("I moved to Boston in 1970.", ProductionThomasMode.JOURNAL)
            assertNotNull(freshGapSource.committedSourceId)
            val privatePrompt = prompt()
            assertTrue(privatePrompt.decision!!.coverageMap.selectedTarget!!.groundingIds.any { it.startsWith(freshGapSource.committedSourceId!!.value + ".") })
            val privateTarget = requireNotNull(privatePrompt.targetId)
            val privateReply = send("This topic is private", ProductionThomasMode.BIOGRAPHER)
            assertEquals(InvestigationAnswerDisposition.MARKED_PRIVATE, privateReply.biographerAnswer!!.disposition)
            assertNotEquals(privateTarget, privateReply.nextBiographerTargetId)

            // L/M: exact source provenance and uncertainty.
            route = RequestedOrdinarySupport.LISTEN
            recall(JOURNAL, AcquisitionMode.JOURNAL, journalId)
            val biographyRecall = recall(BIOGRAPHY, AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE, biographyId)
            assertTrue(biographyRecall.therapyPlan!!.surfacedMemories.any {
                it.exactSourceExcerpt == BIOGRAPHY && (it.eventTime is EventTime.Unknown || it.eventTime is EventTime.ApproximateYear) })
            assertTrue(biographyRecall.assistantArtifact!!.text.contains("Around 2014"))

            val secret = send("I moved to Boise in 2005.", ProductionThomasMode.JOURNAL, privacy = ProductionTurnPrivacy.PRIVATE)
            assertNotNull(secret.committedSourceId)
            assertTrue(runtime.changeSourcePrivacy(biographyId, true, index++).accepted)
            val excluded = action(send("My specific concern is: remembering jobs\nPlease recall my earlier words: " + BIOGRAPHY,
                scope = true, recall = true), "reflect-established-content")
            assertTrue(excluded.therapyPlan!!.surfacedMemories.none { it.exactSourceExcerpt == BIOGRAPHY })
            assertFalse(excluded.assistantArtifact!!.text.contains(BIOGRAPHY))
            val privateJournal = action(send("My specific concern is: remembering Boise\nPlease recall my earlier words: I moved to Boise in 2005.",
                recall = true), "reflect-established-content")
            assertTrue(privateJournal.therapyPlan!!.surfacedMemories.none { it.exactSourceExcerpt?.contains("Boise") == true })

            // H/I: declaration provenance, unknown withdrawal, conflict and specialized boundary.
            val declared = action(send("My specific concern is: the current meeting", scope = true), "reflect-established-content")
            assertTrue(declared.safetyObservation!!.observations.all { it.origin == SafetyEvidenceOrigin.DIRECT_USER_REPORT })
            assertTrue(declared.safetyObservation!!.observations.all { it.evidenceReferences.single().contains("explicit-declaration") })
            val withdrawn = send("I withdraw: Self-harm is relevant now.")
            safety(withdrawn, SafetyAuthorityState.CLARIFICATION_REQUIRED)
            assertEquals(SafetyEvidenceResolution.UNKNOWN, withdrawn.safetyObservation!!.observations.single {
                it.field == SafetyField.SELF_HARM_RELEVANCE }.resolution)
            send("Correction: Self-harm is not relevant now.")
            val conflict = send("Self-harm is relevant now.")
            safety(conflict, SafetyAuthorityState.CLARIFICATION_REQUIRED)
            val field = conflict.safetyObservation!!.observations.single { it.field == SafetyField.SELF_HARM_RELEVANCE }
            assertEquals(SafetyEvidenceResolution.CONTRADICTORY, field.resolution)
            assertEquals(2, field.evidenceReferences.size)
            safety(send("Correction: Self-harm is relevant now."), SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED)

            val snapshot = runtime.snapshot()
            assertTrue(prefs.edit().putString("digest", snapshot.logicalStateDigest)
                .putLong("revision", snapshot.storeRevision).putInt("sourceCount", runtime.sourceSummaries().size)
                .putInt("pid", Process.myPid()).putString("journal", journalId.value)
                .putString("biography", biographyId.value).putString("declined", declined.targetId)
                .putString("privateTarget", privateTarget).commit())
            trace("FINAL count=" + runtime.sourceSummaries().size + " revision=" + snapshot.storeRevision +
                " digest=" + snapshot.logicalStateDigest + " key=" + AndroidKeystorePersonalDataKeyProvider().observation() +
                " uid=" + Process.myUid())
        }
        trace("A_THROUGH_M_COMPLETE=true")
    }

    private fun practical(attempted: Boolean) {
        route = RequestedOrdinarySupport.PRACTICAL_HELP
        action(send("My specific concern is: arranging the " + (if (attempted) "Friday" else "Thursday") + " meeting"), "verify-problem-understanding")
        action(send("Yes, that's right"), "ask-influenceable-part")
        action(send("I can influence: contacting the manager"), "ask-readiness-for-options")
        action(send("I am willing to act"), "invite-user-options")
        val options = action(send("My options are: email; call"), "ask-user-to-choose-option")
        assertNull(options.therapyObservation!!.selectedOption.value)
        action(send("I choose: email"), "develop-bounded-plan")
        val plan = action(send("My first step is: email the manager; when: tomorrow morning"), "wait-for-outcome")
        assertEquals("email", plan.therapyObservation!!.selectedOption.value)
        assertNull(plan.therapyObservation!!.planOutcome.value)
        val outcome = action(send(if (attempted) "I attempted the plan" else "I have not attempted the plan"), "review-reported-outcome")
        assertEquals(if (attempted) PlanOutcome.ATTEMPTED else PlanOutcome.NOT_ATTEMPTED, outcome.therapyObservation!!.planOutcome.value)
        val review = action(send(if (attempted) "What happened was: the manager replied and it helped" else "What happened was: I did not have time"), "consolidate-plan-learning")
        assertEquals(PlanReviewStatus.REVIEWED, review.therapyObservation!!.planReviewStatus.value)
        if (!attempted) assertFalse(review.assistantArtifact!!.text.contains("you tried", true))
    }

    @Test fun bColdReopenDurabilityAndCoverage() {
        guard()
        assertNotEquals("Run a then fixture-only force-stop", prefs.getInt("pid", -1), Process.myPid())
        assertTrue(prefs.contains("digest"))
        ThomasAndroidCompositionRoot.open(context).use { root ->
            runtime = requireNotNull(root.runtime)
            val snapshot = runtime.snapshot()
            assertEquals(prefs.getString("digest", null), snapshot.logicalStateDigest)
            assertEquals(prefs.getLong("revision", -1), snapshot.storeRevision)
            assertEquals(prefs.getInt("sourceCount", -1), runtime.sourceSummaries().size)
            index = snapshot.storeRevision + 10
            val p = prompt()
            // Private evidence can remove a former target's grounding; durable control must still exist.
            val store = runtime.javaClass.getDeclaredField("store").apply { isAccessible = true }.get(runtime)
                as com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStore
            val controls = store.reader.snapshot().coverageTopics
            assertTrue(controls.any { it.label == "biographer-target:" + prefs.getString("declined", null) &&
                it.status == InformationCoverageStatus.DECLINED })
            assertTrue(controls.any { it.label == "biographer-target:" + prefs.getString("privateTarget", null) && it.status == InformationCoverageStatus.PRIVATE })
            assertNotEquals(prefs.getString("declined", null), p.targetId)
            assertNotEquals(prefs.getString("privateTarget", null), p.targetId)
            val unknown = send("I attempted the plan")
            safety(unknown, SafetyAuthorityState.CLARIFICATION_REQUIRED)
            assertTrue(unknown.safetyObservation!!.observations.all { it.resolution == SafetyEvidenceResolution.UNKNOWN })
            assertNull(unknown.therapyObservation!!.actionPlan.value)
            assertNull(unknown.therapyObservation!!.planOutcome.value)
            recall(JOURNAL, AcquisitionMode.JOURNAL, SourceIdentityId.parse(requireNotNull(prefs.getString("journal", null))))
            val excluded = action(send("My specific concern is: remembering jobs\nPlease recall my earlier words: " + BIOGRAPHY,
                recall = true), "reflect-established-content")
            assertTrue(excluded.therapyPlan!!.surfacedMemories.none { it.exactSourceExcerpt == BIOGRAPHY })
            trace("N_REOPEN_COMPLETE=true digestAtOpen=" + snapshot.logicalStateDigest + " rebuiltTarget=" + p.targetId)
        }
    }

    companion object {
        const val FIXTURE = "com.conundrum.thomas.v2.ctv215r1fixture"
        const val JOURNAL = "I moved to Denver in 2010."
        const val BIOGRAPHY = "Around 2014 I changed jobs."
        const val DECLARATIONS = "There is no current emergency.\nThere is no acute medical emergency.\nSelf-harm is not relevant now.\nHarm to others is not relevant now.\nI report no specialized condition for this conversation.\nI am an adult in the supported setting.\nMy present concern is one bounded ordinary personal problem."
    }
}
