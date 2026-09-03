package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemActions
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyEvaluator
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyState
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemRuleCatalog
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemScope
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.engine.verticalslice.PolicyDecisionDisposition
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.PolicyRuleId
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.ProblemInfluence
import com.conundrum.thomas.v2.engine.verticalslice.RuleExecutionAuthority
import com.conundrum.thomas.v2.engine.verticalslice.RuleResult
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import com.conundrum.thomas.v2.engine.verticalslice.SupportIntent
import com.conundrum.thomas.v2.engine.verticalslice.UpstreamSafetyDisposition
import com.conundrum.thomas.v2.qualification.verticalslice.CanonicalBoundedProblemScenario
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProceduralDecisionTableTest {
    private val evaluator = BoundedProblemPolicyEvaluator()

    @Test
    fun `canonical practical-help progression selects exact actions`() {
        val expected = listOf(
            "ask-support-preference",
            "ask-problem-description",
            "verify-problem-understanding",
            "ask-influenceable-part",
            "invite-user-options",
            "ask-user-to-choose-option",
            "develop-bounded-plan",
            "wait-for-outcome",
            "review-reported-outcome",
        )
        val decisions = CanonicalBoundedProblemScenario.states().map(::evaluate)
        assertEquals(expected, decisions.map { it.selectedAction?.definition?.id?.value })
        assertTrue(decisions.all { it.disposition == PolicyDecisionDisposition.ACTION_SELECTED })
        assertTrue(decisions.all { it.activeGoalId != null && it.nextStateExpectations != null })
    }

    @Test
    fun `known intent is not asked again and missing problem is requested`() {
        val decision = evaluate(base().copy(
            supportIntent = PolicyEvidence.reported(SupportIntent.PRACTICAL_HELP, "user-intent"),
        ))
        assertSelected("ask-problem-description", decision)
        assertFalse(decision.eligibleCandidateActions.contains(BoundedProblemActions.askSupportPreference.id))
        val intentTrace = decision.ruleTrace.single { it.ruleId.value == "ctv203-r009-establish-support-intent" }
        assertFalse(intentTrace.matched)
    }

    @Test
    fun `tentative intent causes clarification rather than downstream action`() {
        val decision = evaluate(base().copy(
            supportIntent = PolicyEvidence.tentative(SupportIntent.PRACTICAL_HELP, "tentative-extraction"),
        ))
        assertSelected("ask-support-preference", decision)
    }

    @Test
    fun `unwillingness selects first-class no response`() {
        val decision = evaluate(base().copy(
            willingness = PolicyEvidence.reported(ParticipationWillingness.UNWILLING_TO_CONTINUE, "user-refusal"),
        ))
        assertSelected("pause-without-response", decision)
        assertEquals("dialogue.no-response", decision.selectedAction?.definition?.dialogueActId?.value)
    }

    @Test
    fun `listening preference does not enter problem solving`() {
        val state = establishedProblem().copy(
            supportIntent = PolicyEvidence.reported(SupportIntent.LISTENING, "user-intent"),
        )
        val decision = evaluate(state)
        assertSelected("reflect-for-listening", decision)
        assertEquals("intervention.supportive-listening", decision.selectedAction?.definition?.candidateInterventionFamilyId?.value)
    }

    @Test
    fun `understanding preference verifies tentative formulation then summarizes confirmed content`() {
        val tentative = establishedProblem().copy(
            supportIntent = PolicyEvidence.reported(SupportIntent.UNDERSTANDING, "user-intent"),
            sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "thomas-summary"),
        )
        assertSelected("verify-problem-understanding", evaluate(tentative))
        val confirmed = tentative.copy(
            sharedUnderstanding = PolicyEvidence.reported(SharedUnderstanding.CONFIRMED, "user-confirmation"),
        )
        assertSelected("summarize-for-understanding", evaluate(confirmed))
    }

    @Test
    fun `unknown safety cannot silently become ordinary-safe`() {
        val decision = evaluate(base().copy(upstreamSafetyDisposition = UpstreamSafetyDisposition.UNKNOWN))
        assertEquals(PolicyDecisionDisposition.INSUFFICIENT_INFORMATION, decision.disposition)
        assertNull(decision.selectedAction)
        assertEquals("UPSTREAM_SAFETY_AUTHORITY_REQUIRED", decision.handoffRequirement)
    }

    @Test
    fun `specialized safety and specialized scope terminate the ordinary slice`() {
        val safety = evaluate(base().copy(
            upstreamSafetyDisposition = UpstreamSafetyDisposition.SPECIALIZED_POLICY_REQUIRED,
        ))
        assertEquals(PolicyDecisionDisposition.SPECIALIZED_POLICY_REQUIRED, safety.disposition)
        assertEquals("SPECIALIZED_SAFETY_POLICY_REQUIRED", safety.handoffRequirement)

        val scope = evaluate(base().copy(scope = BoundedProblemScope.SPECIALIZED_POLICY_REQUIRED))
        assertEquals(PolicyDecisionDisposition.SPECIALIZED_POLICY_REQUIRED, scope.disposition)
        assertEquals("SPECIALIZED_POLICY_REQUIRED", scope.handoffRequirement)
    }

    @Test
    fun `wrong mode and explicit outside scope return out of scope`() {
        assertEquals(
            PolicyDecisionDisposition.OUT_OF_SCOPE,
            evaluate(base().copy(mode = ThomasMode.BIOGRAPHER)).disposition,
        )
        assertEquals(
            PolicyDecisionDisposition.OUT_OF_SCOPE,
            evaluate(base().copy(scope = BoundedProblemScope.OUT_OF_SCOPE)).disposition,
        )
    }

    @Test
    fun `non-influenceable problem exits rather than improvising another intervention`() {
        val decision = evaluate(practicalConfirmed().copy(
            problemInfluence = PolicyEvidence.reported(ProblemInfluence.NOT_INFLUENCEABLE, "user-influence"),
        ))
        assertEquals(PolicyDecisionDisposition.OUT_OF_SCOPE, decision.disposition)
        assertEquals("NON_INFLUENCEABLE_PROBLEM_POLICY_REQUIRED", decision.handoffRequirement)
    }

    @Test
    fun `conflicting evidence produces a visible policy conflict`() {
        val decision = evaluate(base().copy(
            supportIntent = PolicyEvidence.conflicting("user-said-listen", "user-said-practical"),
        ))
        assertEquals(PolicyDecisionDisposition.POLICY_CONFLICT, decision.disposition)
        assertEquals("EVIDENCE_ADJUDICATION_REQUIRED", decision.handoffRequirement)
    }

    @Test
    fun `review blocked matching rule cannot execute`() {
        val source = BoundedProblemRuleCatalog.rules.single { it.id.value == "ctv203-r009-establish-support-intent" }
        val blocked = source.copy(executionAuthority = RuleExecutionAuthority.CANDIDATE_RULE)
        val decision = evaluate(BoundedProblemPolicyEvaluator(listOf(blocked)), base())
        assertEquals(PolicyDecisionDisposition.REVIEW_BLOCKED, decision.disposition)
        assertNull(decision.selectedAction)
        assertTrue(decision.unresolvedRequirements.contains("RULE_NOT_QUALIFICATION_EXECUTABLE"))
    }

    @Test
    fun `equal-priority applicable rules fail as policy conflict`() {
        val original = BoundedProblemRuleCatalog.rules.single { it.id.value == "ctv203-r009-establish-support-intent" }
        val competing = original.copy(
            id = PolicyRuleId.parse("ctv203-synthetic-conflict"),
            result = RuleResult.SelectAction(PolicyActionId.parse("ask-problem-description")),
        )
        val decision = evaluate(BoundedProblemPolicyEvaluator(listOf(original, competing)), base())
        assertEquals(PolicyDecisionDisposition.POLICY_CONFLICT, decision.disposition)
        assertEquals(2, decision.tieBreakTrace.contenderRuleIds.size)
        assertEquals("CONFLICT_NOT_GUESSED", decision.tieBreakTrace.resolution)
    }

    @Test
    fun `multiple eligible actions select only the unique highest priority`() {
        val original = BoundedProblemRuleCatalog.rules.single { it.id.value == "ctv203-r009-establish-support-intent" }
        val lower = original.copy(
            id = PolicyRuleId.parse("ctv203-synthetic-lower"),
            priority = original.priority - 1,
            result = RuleResult.SelectAction(PolicyActionId.parse("ask-problem-description")),
        )
        val decision = evaluate(BoundedProblemPolicyEvaluator(listOf(original, lower)), base())
        assertSelected("ask-support-preference", decision)
        assertEquals(2, decision.eligibleCandidateActions.size)
        assertEquals("UNIQUE_HIGHEST_PRIORITY_RULE", decision.tieBreakTrace.resolution)
    }

    @Test
    fun `no rule match is a typed no-authorized-action result`() {
        val decision = evaluate(BoundedProblemPolicyEvaluator(emptyList()), base())
        assertEquals(PolicyDecisionDisposition.NO_AUTHORIZED_ACTION, decision.disposition)
        assertNull(decision.selectedAction)
        assertTrue(decision.unresolvedRequirements.contains("POLICY_GRAPH_HAS_NO_MATCH"))
    }

    @Test
    fun `identical state and policy version produce identical decisions`() {
        val state = practicalConfirmed()
        assertEquals(evaluate(state), evaluate(state))
    }

    @Test
    fun `reported outcome changes reassessment from waiting to review`() {
        val states = CanonicalBoundedProblemScenario.states()
        assertSelected("wait-for-outcome", evaluate(states[7]))
        assertSelected("review-reported-outcome", evaluate(states[8]))
    }

    @Test
    fun `malformed input is rejected before rules execute`() {
        val valid = base()
        val decision = evaluator.evaluate(
            valid.copy(stateId = "INVALID STATE"),
            QualificationSafetyGate.permitFor(valid),
        )
        assertEquals(PolicyDecisionDisposition.INVALID_INPUT, decision.disposition)
        assertTrue(decision.ruleTrace.isEmpty())
        assertTrue(decision.unresolvedRequirements.any { it.contains("stateId") })
    }

    private fun base() = BoundedProblemPolicyState(
        stateId = "decision-table-state",
        mode = ThomasMode.THERAPIST,
        upstreamSafetyDisposition = UpstreamSafetyDisposition.ORDINARY_SLICE_ALLOWED,
        scope = BoundedProblemScope.BOUNDED_NON_EMERGENCY_PERSONAL_PROBLEM,
        willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_EXPLORE, "user-willingness"),
    )

    private fun establishedProblem() = base().copy(
        problemStatement = PolicyEvidence.reported("a synthetic scheduling conflict", "user-problem"),
        problemClarity = PolicyEvidence.reported(ProblemClarity.BOUNDED, "derived-problem-clarity"),
    )

    private fun practicalConfirmed() = establishedProblem().copy(
        supportIntent = PolicyEvidence.reported(SupportIntent.PRACTICAL_HELP, "user-intent"),
        sharedUnderstanding = PolicyEvidence.reported(SharedUnderstanding.CONFIRMED, "user-confirmation"),
        willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_ACT, "user-readiness"),
    )

    private fun evaluate(state: BoundedProblemPolicyState) =
        QualificationSafetyGate.evaluateOrdinary(evaluator, state)

    private fun evaluate(
        customEvaluator: BoundedProblemPolicyEvaluator,
        state: BoundedProblemPolicyState,
    ) = customEvaluator.evaluate(state, QualificationSafetyGate.permitFor(state))

    private fun assertSelected(actionId: String, decision: com.conundrum.thomas.v2.engine.verticalslice.PolicyDecision) {
        assertEquals(PolicyDecisionDisposition.ACTION_SELECTED, decision.disposition)
        assertNotNull(decision.activeGoalId)
        assertEquals(actionId, decision.selectedAction?.definition?.id?.value)
    }
}
