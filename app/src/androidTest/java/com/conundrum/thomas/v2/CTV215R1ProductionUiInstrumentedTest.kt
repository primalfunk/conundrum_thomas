package com.conundrum.thomas.v2

import android.os.Process
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.conundrum.thomas.v2.engine.ordinary.*
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.languagerenderer.*
import com.conundrum.thomas.v2.runtime.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Read-only inspection of actual delivered action/history; no state injection or policy selection. */
@RunWith(AndroidJUnit4::class)
class CTV215R1ProductionUiInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val driver by lazy { UiSubmissionDriver(compose) }
    private lateinit var model: ThomasViewModel
    private lateinit var runtime: ThomasProductionRuntime
    @Suppress("UNCHECKED_CAST")
    private fun <T> read(owner: Any, name: String): T =
        owner.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(owner) as T
    private fun session(): CoreOrdinaryTherapyState = read(read<Any>(runtime, "therapyInput"), "session")
    private fun assistantCount() = model.state.value.transcript.count { it.role == TranscriptRole.THOMAS }

    @Test fun aBiographerModeSelectionDoesNotRunLocalRealizationOnTheMainThread() {
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        val started = android.os.SystemClock.elapsedRealtime()
        compose.onNodeWithTag("mode-biographer").performClick()
        val elapsed = android.os.SystemClock.elapsedRealtime() - started
        compose.runOnIdle {
            assertEquals(ProductionThomasMode.BIOGRAPHER, model.state.value.mode)
        }
        assertTrue(
            "Biographer mode selection must return control before bounded local realization completes",
            elapsed < 2_000,
        )
    }

    private fun send(text: String, expectedAction: String?, silent: Boolean = false): CoreOrdinaryTherapyState {
        val before = assistantCount()
        val beforeUsers = model.state.value.transcript.count { it.role == TranscriptRole.USER }
        val frontierBefore = durableFrontier()
        val revisionBefore = runtime.snapshot().storeRevision
        val started = android.os.SystemClock.elapsedRealtime()
        driver.prepareForInput()
        compose.onNodeWithTag("turn-draft").performTextInput(text)
        println("R1 UI_BEFORE_CLICK input=" + text + " processing=" + model.state.value.processing +
            " draft=" + model.state.value.draft + " status=" + model.state.value.status)
        driver.clickCommit()
        compose.runOnIdle {
            assertTrue("Accepted UI action must synchronously enter presentation admission",
                model.state.value.processing ||
                    model.state.value.transcript.count { it.role == TranscriptRole.USER } == beforeUsers + 1)
        }
        try {
            // This is a bounded observation timeout, not a new product latency allowance.
            compose.waitUntil(60_000) { !model.state.value.processing && model.state.value.draft.isEmpty() }
        } catch (failure: Throwable) {
            println("R1 UI_TIMEOUT processing=" + model.state.value.processing + " draft=" + model.state.value.draft +
                " status=" + model.state.value.status + " sources=" + model.state.value.sourceSummaries.size)
            Thread.getAllStackTraces().filterKeys { it.name == "main" || it.name.startsWith("DefaultDispatcher") }
                .forEach { (thread, stack) -> println("R1 THREAD " + thread.name + "\n" + stack.joinToString("\n")) }
            throw failure
        }
        println("R1 UI_COMPLETED elapsedMs=" + (android.os.SystemClock.elapsedRealtime() - started))
        assertTrue("The actual UI submission must be admitted, not only rendered",
            model.state.value.transcript.last { it.role == TranscriptRole.USER }.committed)
        assertEquals("Exactly one user turn per UI action", beforeUsers + 1,
            model.state.value.transcript.count { it.role == TranscriptRole.USER })
        val user = model.state.value.transcript.last { it.role == TranscriptRole.USER }
        val allocated = user.id.substringAfterLast('-').toLong()
        assertTrue("New UI identity must exceed committed frontier", allocated > frontierBefore)
        compose.onNodeWithText("Not saved").assertDoesNotExist()
        val sources = store().reader.snapshot().sources
        val source = sources.single { it.provenance.metadata["therapy.turn-id"] == user.id }
        assertEquals(text, (source.originalContent as com.conundrum.thomas.v2.longitudinal.OriginalSourceContent.Inline).exactContent)
        assertEquals(allocated, durableFrontier())
        println("R1 UI_IDENTITY beforeFrontier=$frontierBefore beforeRevision=$revisionBefore allocated=$allocated " +
            "identity=" + user.id + " source=" + source.stableSourceId.value +
            " afterRevision=" + runtime.snapshot().storeRevision + " committed=" + user.committed + " NOT_SAVED=false")
        val state = session()
        if (expectedAction != null) assertEquals("core-" + expectedAction, state.actionHistory.last().actionId.value)
        assertEquals(ProductionThomasMode.THERAPY, model.state.value.mode)
        if (silent) assertEquals(before, assistantCount()) else {
            assertEquals(before + 1, assistantCount())
            assertEquals("Completed", model.state.value.status)
            val actual = model.state.value.transcript.last()
            assertEquals(TranscriptRole.THOMAS, actual.role)
            val history = read<RenderHistoryState>(runtime, "renderHistory").entries.last()
            val renderText = Class.forName("com.conundrum.thomas.v2.languagerenderer.RenderText")
            val fingerprint = renderText.getDeclaredMethod("responseFingerprint", String::class.java).invoke(
                renderText.getDeclaredField("INSTANCE").get(null), actual.text)
            assertEquals(history.normalizedResponseFingerprint, fingerprint)
            assertTrue(history.semanticAct in setOf(GovernedSemanticAct.BRIEF_REFLECTION,
                GovernedSemanticAct.CLARIFYING_QUESTION, GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION))
        }
        println("R1 UI input=" + text + " expectedAction=" + expectedAction + " actual=" + state.actionHistory.lastOrNull() +
            " revision=" + state.conversationRevision + " pending=" + state.pendingInformation +
            " status=" + model.state.value.status + " displayed=" + model.state.value.transcript.lastOrNull()?.text)
        return state
    }

    @Test fun cActualInputControlsAndSemanticProgression() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(CTV215R1ProductionDeviceInstrumentedTest.FIXTURE, context.packageName)
        assertNotEquals(requireNotNull(InstrumentationRegistry.getArguments().getString("canonicalUid")).toInt(), Process.myUid())
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        runtime = read<ThomasAndroidCompositionRoot>(model, "root").runtime!!
        assertTrue("Run complete device scenarios first", model.state.value.sourceSummaries.size > 20)
        compose.onNodeWithTag("mode-therapy").performClick()
        send(CTV215R1ProductionDeviceInstrumentedTest.DECLARATIONS + "\nMy specific concern is: the synthetic UI appointment", "reflect-established-content")
        val initial = session().conversationRevision
        send("My specific concern is: THE synthetic UI appointment!", "offer-direction-choice")
        assertEquals(initial, session().conversationRevision)
        send("My specific concern is: the synthetic UI appointment.", null, silent = true)
        assertEquals(initial, session().conversationRevision)
        send("Another detail is: the organizer moved the date", "reflect-established-content")
        assertTrue(session().conversationRevision > initial)
        assertEquals(OrdinaryEngagement.PAUSE_REQUESTED, send("Please pause", "pause-without-response", true).engagement.value)
        send("I am ready to resume", "invite-further-expression")
        assertEquals(OrdinaryEngagement.DOES_NOT_WANT_TOPIC, send("I don't want to discuss this", "pause-without-response", true).engagement.value)
        send("I want to continue", "invite-further-expression")
        send("That's all for now", "summarize-listening")
        send("Thank you", "check-further-or-close")
        compose.onNodeWithText("Understand").performClick()
        send("My specific concern is: a cancelled UI meeting\nWhat I haven't explained is: who changed it", "ask-important-missing-piece")
        send("The missing detail is: the organizer changed the time", "verify-tentative-understanding")
        assertFalse(session().thomasUnderstanding.isEstablished())
        send("No, that's not what I mean", "acknowledge-correction")
        assertNull(session().thomasUnderstanding.value)
        send("What I mean is: the UI meeting was delayed", "verify-tentative-understanding")
        send("Yes, that's right", "summarize-shared-understanding")
        assertTrue(session().sharedUnderstanding.isEstablished())
        compose.onNodeWithText("Practical").performClick()
        assertEquals(RequestedOrdinarySupport.PRACTICAL_HELP, model.state.value.therapySupport)
        val checkpointBefore = session()
        val checkpointHistory = read<RenderHistoryState>(runtime, "renderHistory")
        send("My specific concern is: arranging a new UI meeting.", "verify-problem-understanding")
        proveCheckpoint16(checkpointBefore, checkpointHistory)
        send("Yes, that's right", "ask-influenceable-part")
        send("I can influence: contacting the organizer", "ask-readiness-for-options")
        send("I am willing to act", "invite-user-options")
        send("My options are: email; call", "ask-user-to-choose-option")
        assertNull(session().selectedOption.value)
        send("I choose: email", "develop-bounded-plan")
        send("My first step is: email the organizer; when: tomorrow morning", "wait-for-outcome", true)
        assertNull(session().planOutcome.value)
        send("I have not attempted the plan", "review-reported-outcome")
        assertEquals(PlanOutcome.NOT_ATTEMPTED, session().planOutcome.value)
        send("What happened was: I did not have time", "consolidate-plan-learning")
        assertEquals(PlanReviewStatus.REVIEWED, session().planReviewStatus.value)
        preserveDurabilityExpectation("c")
        println("R1 UI_CONTROLS_COMPLETE=true")
    }

    private fun proveCheckpoint16(before: CoreOrdinaryTherapyState, history: RenderHistoryState) {
        val after = session()
        assertEquals(RequestedOrdinarySupport.PRACTICAL_HELP, after.routePreference.value)
        assertEquals(OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, after.activeRoute)
        assertEquals(CoreOrdinaryActions.verifyProblemUnderstanding.id, after.actionHistory.last().actionId)
        assertEquals(before.actionHistory, after.actionHistory.dropLast(1))
        // Read-only renderer replay from the actual delivered verification and its
        // retained tentative support. This test does not invoke procedural authority.
        assertEquals(com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding.TENTATIVE,after.sharedUnderstanding.value)
        assertEquals(com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity.BOUNDED,after.problemClarity.value)
        val action=CoreOrdinaryActions.byId.getValue(after.actionHistory.last().actionId)
        val spec=action.renderSpecification
        val upstream=com.conundrum.thomas.v2.domain.rendering.RenderCommand(
            policyDecisionReference="physical-delivered-verification-replay",selectedPolicyActionId=action.id.value,
            selectedDialogueActId=action.dialogueActId.value,therapeuticGoalId=action.goalId.value,
            instruction=spec.instruction,requiredSemanticContent=spec.requiredSemanticContent,
            allowedSemanticContent=spec.allowedSemanticContent,prohibitedSemanticContent=spec.prohibitedSemanticContent,
            toneConstraints=spec.toneConstraints,maximumWords=spec.maximumWords,maximumQuestions=spec.maximumQuestions,
            advicePermitted=spec.advicePermitted,form=spec.form,outputDisposition=spec.outputDisposition,
            interpretationMustRemainTentative=spec.interpretationMustRemainTentative,
            userAgencyMustBeExplicitlyPreserved=spec.userAgencyMustBeExplicitlyPreserved)
        val support=listOf(com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText(
            "tentative-thomas-understanding",requireNotNull(after.thomasUnderstanding.value)))
        val identity=model.state.value.transcript.last { it.role==TranscriptRole.USER }.id
        val index=identity.substringAfterLast('-').toInt()
        assertEquals(84,index)
        val envelope=com.conundrum.thomas.v2.therapylongitudinal.TherapyRenderSupportEnvelope(
            command=upstream,currentTurnId=com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnId.parse(identity),
            currentUserText="My specific concern is: arranging a new UI meeting.",policySupportingText=support,
            surfacedMemorySupport=emptyList(),completeContextPacketDisclosed=false)
        val command=TherapyRenderCommandAdapter.adapt(RenderCommandId.parse("android.therapy.$index"),index,envelope)
        val replay=GovernedLanguageRenderer().render(command,history)
        assertEquals(GovernedSemanticAct.CLARIFYING_QUESTION,command.semanticAct)
        assertTrue(RenderValidationReason.REPEATED_OPENING in replay.rejectedCandidateReasons.flatten())
        assertEquals(command.authorizedReferenceRealizations[1],replay.finalText)
        assertTrue(replay.validation.accepted)
        val actual=model.state.value.transcript.last { it.role==TranscriptRole.THOMAS }
        assertEquals(replay.finalText,actual.text)
        assertEquals(replay.nextHistory,read<RenderHistoryState>(runtime,"renderHistory"))
        val singleton=command.copy(authorizedReferenceRealizations=listOf(command.authorizedReferenceRealizations.first()))
        val rejected=GovernedLanguageRenderer().render(singleton,history)
        assertEquals(RenderDisposition.RENDERING_UNAVAILABLE,rejected.disposition)
        assertEquals(listOf(RenderValidationReason.REPEATED_OPENING),rejected.validation.reasonCodes)
        println("R1 CHECKPOINT16 MODE_UI_VM=Practical MODE_DECISION="+after.routePreference.value+
            " SELECTED="+action.id.value+" SEMANTIC="+replay.semanticAct+
            " OLD_REJECTED="+rejected.validation.reasonCodes+" REPLAY_EQUALS_ACTUAL=true ACCEPTED="+actual.text+
            " DELIVERED="+after.actionHistory.last().actionId.value+" IDENTITY="+identity)
        println("R1 CHECKPOINT16_PRE_HISTORY="+history)
        println("R1 CHECKPOINT16_ORDER="+command.authorizedReferenceRealizations)
        println("R1 CHECKPOINT16_REJECTIONS="+replay.rejectedCandidateReasons)
        assertTrue(prefs().edit().putString("checkpoint16-identity",identity)
            .putString("checkpoint16-text","My specific concern is: arranging a new UI meeting.").commit())
    }

    private fun store(): com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStore = read(runtime, "store")

    private fun durableFrontier(): Long = store().reader.redactedAdmissionHistory().mapNotNull {
        Regex("android-(?:journal|biographer|therapy|source-revision|lifecycle-(?:make-private|request-eligible-review|delete))-([0-9]+)$")
            .find(it.idempotencyKey)?.groupValues?.get(1)?.toLong()
    }.maxOrNull() ?: 0L

    private fun attach() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(CTV215R1ProductionDeviceInstrumentedTest.FIXTURE, context.packageName)
        assertNotEquals(requireNotNull(InstrumentationRegistry.getArguments().getString("canonicalUid")).toInt(), Process.myUid())
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        runtime = read<ThomasAndroidCompositionRoot>(model, "root").runtime!!
    }

    private fun prefs() = InstrumentationRegistry.getInstrumentation().targetContext
        .getSharedPreferences("ct-v2-15r1-identity-physical", 0)

    private fun preserveDurabilityExpectation(stage: String) {
        val user = model.state.value.transcript.last { it.role == TranscriptRole.USER }
        val snapshot = runtime.snapshot()
        assertTrue(prefs().edit().putString("stage", stage).putString("identity", user.id)
            .putString("text", user.text).putString("digest", snapshot.logicalStateDigest)
            .putLong("revision", snapshot.storeRevision).putLong("frontier", durableFrontier())
            .putStringSet("sources", model.state.value.sourceSummaries.map { it.stableSourceId.value }.toSet()).commit())
        println("R1 UI_DURABILITY_EXPECT stage=$stage identity=" + user.id + " frontier=" + durableFrontier() +
            " revision=" + snapshot.storeRevision + " digest=" + snapshot.logicalStateDigest)
    }

    private fun verifyDurabilityExpectation(stage: String) {
        assertEquals(stage, prefs().getString("stage", null))
        val snapshot = runtime.snapshot()
        assertEquals(prefs().getString("digest", null), snapshot.logicalStateDigest)
        assertEquals(prefs().getLong("revision", -1), snapshot.storeRevision)
        assertEquals(prefs().getLong("frontier", -1), durableFrontier())
        assertEquals(prefs().getStringSet("sources", emptySet()),
            model.state.value.sourceSummaries.map { it.stableSourceId.value }.toSet())
        val source = store().reader.snapshot().sources.single {
            it.provenance.metadata["therapy.turn-id"] == prefs().getString("identity", null)
        }
        assertEquals(prefs().getString("text", null),
            (source.originalContent as com.conundrum.thomas.v2.longitudinal.OriginalSourceContent.Inline).exactContent)
        val checkpoint = store().reader.snapshot().sources.single {
            it.provenance.metadata["therapy.turn-id"] == prefs().getString("checkpoint16-identity", null)
        }
        assertEquals(prefs().getString("checkpoint16-text", null),
            (checkpoint.originalContent as com.conundrum.thomas.v2.longitudinal.OriginalSourceContent.Inline).exactContent)
        println("R1 CHECKPOINT16_REOPEN_PASS stage=$stage identity=android-therapy-84")
        println("R1 UI_REOPEN_DURABILITY_PASS stage=$stage identity=" + prefs().getString("identity", null) +
            " frontier=" + durableFrontier() + " revision=" + snapshot.storeRevision +
            " digest=" + snapshot.logicalStateDigest)
    }

    @Test fun dActualUiIdentityAfterProcessReopen() {
        attach()
        verifyDurabilityExpectation("c")
        compose.onNodeWithTag("mode-therapy").performClick()
        send(CTV215R1ProductionDeviceInstrumentedTest.DECLARATIONS +
            "\nMy specific concern is: the synthetic identity reopen meeting", "reflect-established-content")
        preserveDurabilityExpectation("d")
        println("R1 UI_FIRST_IDENTITY_REOPEN_COMPLETE=true")
    }

    @Test fun eSecondReopenDurabilityAndSubsequentIdentity() {
        attach()
        verifyDurabilityExpectation("d")
        compose.onNodeWithTag("mode-therapy").performClick()
        send(CTV215R1ProductionDeviceInstrumentedTest.DECLARATIONS +
            "\nMy specific concern is: the next synthetic identity meeting", "reflect-established-content")
        val retained = model
        compose.activityRule.scenario.recreate()
        compose.activityRule.scenario.onActivity { model = ViewModelProvider(it)[ThomasViewModel::class.java] }
        assertSame("Activity recreation retains the allocator-owning ViewModel", retained, model)
        send("Yes, that's right", "invite-further-expression")
        preserveDurabilityExpectation("e")
        println("R1 UI_SECOND_REOPEN_AND_SUBSEQUENT_IDENTITY_COMPLETE=true")
    }
}
