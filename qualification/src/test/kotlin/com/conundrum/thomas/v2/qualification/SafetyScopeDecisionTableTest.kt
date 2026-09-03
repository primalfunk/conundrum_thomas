package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyEvaluator
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyState
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemScope
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PolicyDecisionDisposition
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.UpstreamSafetyDisposition
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import com.conundrum.thomas.v2.safety.ExplicitEmergencyCircumstance
import com.conundrum.thomas.v2.safety.PopulationApplicability
import com.conundrum.thomas.v2.safety.PresentingScope
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyEvidence
import com.conundrum.thomas.v2.safety.SafetyEvidenceOrigin
import com.conundrum.thomas.v2.safety.SafetyEvidenceResolution
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision
import com.conundrum.thomas.v2.safety.SafetyPresence
import com.conundrum.thomas.v2.safety.SafetyRuleExecutionAuthority
import com.conundrum.thomas.v2.safety.SafetyRuleId
import com.conundrum.thomas.v2.safety.SafetyRuleResult
import com.conundrum.thomas.v2.safety.SafetyScopeGate
import com.conundrum.thomas.v2.safety.SafetyScopeRuleCatalog
import com.conundrum.thomas.v2.safety.SpecializedScopeCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyScopeDecisionTableTest {
    private val gate = SafetyScopeGate()

    @Test
    fun `ordinary bounded problem with established facts receives revision-bound permit`() {
        val input = ordinary()
        val decision = gate.govern(input)
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, decision.authorityState)
        assertNotNull(decision.ordinaryTherapyPermit)
        assertTrue(decision.ordinaryTherapyPermit!!.authorizes(input.stateId, input.evidenceRevision))
        assertFalse("ORDINARY_THERAPIST_POLICY" in decision.prohibitedContinuationPaths)
        assertEquals("ctv204-r014-ordinary-policy-permit", decision.selectedRuleId?.value)
    }

    @Test
    fun `unknown required safety fact selects one exact clarification`() {
        val decision = gate.govern(ordinary().copy(selfHarmRelevance = SafetyEvidence.unknown()))
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, decision.authorityState)
        assertEquals("SELF_HARM_RELEVANCE", decision.nextExpectedEvidence?.name)
        assertEquals("clarify-required-safety-fact", decision.selectedAction?.definition?.id?.value)
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `not asked remains distinct from established absence`() {
        val decision = gate.govern(ordinary().copy(selfHarmRelevance = SafetyEvidence.notAsked()))
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, decision.authorityState)
        assertEquals(SafetyEvidenceResolution.NOT_ASKED, decision.observations.single { it.field.name == "SELF_HARM_RELEVANCE" }.resolution)
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `declined required clarification blocks ordinary policy without repeated questioning`() {
        val decision = gate.govern(ordinary().copy(selfHarmRelevance = SafetyEvidence.declined("synthetic-user-declined")))
        assertEquals(SafetyAuthorityState.INSUFFICIENT_INFORMATION, decision.authorityState)
        assertEquals("REQUIRED_SAFETY_INFORMATION_DECLINED", decision.handoffRequirement)
        assertNull(decision.selectedAction)
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `explicit self-harm-relevant disclosure cannot enter ordinary problem solving`() {
        val decision = gate.govern(ordinary().copy(
            selfHarmRelevance = present("synthetic-direct-self-harm-disclosure"),
        ))
        assertEquals(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, decision.authorityState)
        assertEquals("SELF_HARM_SPECIALIZED_POLICY_REQUIRED", decision.handoffRequirement)
        assertEquals("ctv204-r003-self-harm-specialized-boundary", decision.selectedRuleId?.value)
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `contradictory safety evidence remains explicit and selects clarification`() {
        val decision = gate.govern(ordinary().copy(
            selfHarmRelevance = SafetyEvidence.contradictory("synthetic-present-report", "synthetic-absent-report"),
        ))
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, decision.authorityState)
        assertEquals("SELF_HARM_RELEVANCE", decision.nextExpectedEvidence?.name)
        assertEquals("ctv204-r010-contradictory-evidence-clarification", decision.selectedRuleId?.value)
    }

    @Test
    fun `tentative evidence cannot grant ordinary authority`() {
        val decision = gate.govern(ordinary().copy(
            selfHarmRelevance = SafetyEvidence.tentative(
                SafetyPresence.ABSENT,
                SafetyEvidenceOrigin.DERIVED_STRUCTURED_FACT,
                "synthetic-tentative-extraction",
            ),
        ))
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, decision.authorityState)
        assertEquals("ctv204-r012-tentative-evidence-clarification", decision.selectedRuleId?.value)
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `specialized mental-health condition terminates ordinary policy`() {
        val decision = gate.govern(ordinary().copy(
            specializedScopeCondition = established(SpecializedScopeCondition.PSYCHOSIS, "synthetic-upstream-specialized"),
        ))
        assertEquals(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, decision.authorityState)
        assertEquals("CONDITION_SPECIFIC_POLICY_REQUIRED", decision.handoffRequirement)
    }

    @Test
    fun `explicit medical emergency reaches emergency boundary`() {
        val decision = gate.govern(ordinary().copy(acuteMedicalEmergency = present("synthetic-medical-emergency")))
        assertEquals(SafetyAuthorityState.EMERGENCY_BOUNDARY_REACHED, decision.authorityState)
        assertEquals("URGENT_MEDICAL_POLICY_REQUIRED", decision.handoffRequirement)
    }

    @Test
    fun `explicit emergency overrides otherwise matching ordinary facts`() {
        val decision = gate.govern(ordinary().copy(
            currentEmergency = established(
                ExplicitEmergencyCircumstance.SELF_HARM_EMERGENCY_EXPLICITLY_ESTABLISHED,
                "synthetic-explicit-emergency",
            ),
            selfHarmRelevance = present("synthetic-self-harm-context"),
        ))
        assertEquals(SafetyAuthorityState.EMERGENCY_BOUNDARY_REACHED, decision.authorityState)
        assertEquals("ctv204-r001-explicit-emergency-boundary", decision.selectedRuleId?.value)
        assertTrue(decision.eligibleRuleIds.any { it.value == "ctv204-r003-self-harm-specialized-boundary" })
        assertTrue(decision.eligibleRuleIds.any { it.value == "ctv204-r014-ordinary-policy-permit" }.not())
    }

    @Test
    fun `harm-to-others concern requires separate policy and borrows no self-harm algorithm`() {
        val decision = gate.govern(ordinary().copy(harmToOthersRelevance = present("synthetic-harm-other-report")))
        assertEquals(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, decision.authorityState)
        assertEquals("HARM_TO_OTHERS_SOURCE_AND_POLICY_REQUIRED", decision.handoffRequirement)
    }

    @Test
    fun `unsupported population and setting are typed boundaries`() {
        val population = gate.govern(ordinary().copy(
            populationApplicability = established(PopulationApplicability.UNSUPPORTED_AGE_OR_POPULATION, "synthetic-population"),
        ))
        assertEquals(SafetyAuthorityState.OUT_OF_SUPPORTED_POPULATION, population.authorityState)
        val setting = gate.govern(ordinary().copy(
            populationApplicability = established(PopulationApplicability.UNSUPPORTED_SETTING, "synthetic-setting"),
        ))
        assertEquals(SafetyAuthorityState.OUT_OF_SUPPORTED_POPULATION, setting.authorityState)
    }

    @Test
    fun `specialized and out-of-scope presentations remain distinct`() {
        val specialized = gate.govern(ordinary().copy(
            presentingScope = established(PresentingScope.SPECIALIZED_POLICY_REQUIRED, "synthetic-specialized-scope"),
        ))
        assertEquals(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, specialized.authorityState)
        val outside = gate.govern(ordinary().copy(
            presentingScope = established(PresentingScope.OUT_OF_SCOPE, "synthetic-outside-scope"),
        ))
        assertEquals(SafetyAuthorityState.OUT_OF_SCOPE, outside.authorityState)
    }

    @Test
    fun `review-blocked matching rule cannot issue a permit`() {
        val ordinaryRule = SafetyScopeRuleCatalog.rules.single { it.id.value == "ctv204-r014-ordinary-policy-permit" }
        val blocked = ordinaryRule.copy(executionAuthority = SafetyRuleExecutionAuthority.CANDIDATE_RULE)
        val decision = SafetyScopeGate(listOf(blocked)).govern(ordinary())
        assertEquals(SafetyAuthorityState.REVIEW_BLOCKED, decision.authorityState)
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `equal-priority applicable rules produce visible policy conflict`() {
        val ordinaryRule = SafetyScopeRuleCatalog.rules.single { it.id.value == "ctv204-r014-ordinary-policy-permit" }
        val competing = ordinaryRule.copy(
            id = SafetyRuleId.parse("ctv204-synthetic-equal-priority"),
            result = SafetyRuleResult.Terminate(SafetyAuthorityState.OUT_OF_SCOPE, "SYNTHETIC_CONFLICT"),
        )
        val decision = SafetyScopeGate(listOf(ordinaryRule, competing)).govern(ordinary())
        assertEquals(SafetyAuthorityState.POLICY_CONFLICT, decision.authorityState)
        assertEquals(2, decision.tieBreakTrace.contenderRuleIds.size)
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `malformed input is rejected before rules execute`() {
        val decision = gate.govern(ordinary().copy(stateId = "INVALID STATE"))
        assertEquals(SafetyAuthorityState.INVALID_INPUT, decision.authorityState)
        assertTrue(decision.ruleTrace.isEmpty())
        assertNull(decision.ordinaryTherapyPermit)
    }

    @Test
    fun `identical input and policy version produce equal decisions`() {
        val input = ordinary()
        assertEquals(gate.govern(input), gate.govern(input))
    }

    @Test
    fun `gate pass permits exact CT-V2-03 ordinary action`() {
        val state = BoundedProblemPolicyState(
            stateId = "safety-to-ordinary",
            mode = ThomasMode.THERAPIST,
            upstreamSafetyDisposition = UpstreamSafetyDisposition.ORDINARY_SLICE_ALLOWED,
            scope = BoundedProblemScope.BOUNDED_NON_EMERGENCY_PERSONAL_PROBLEM,
            willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_EXPLORE, "synthetic-willingness"),
        )
        val gateDecision = gate.govern(QualificationSafetyGate.ordinaryInput(state.stateId, state.safetyEvidenceRevision))
        val policyDecision = BoundedProblemPolicyEvaluator().evaluate(state, requireNotNull(gateDecision.ordinaryTherapyPermit))
        assertEquals(PolicyDecisionDisposition.ACTION_SELECTED, policyDecision.disposition)
        assertEquals("ask-support-preference", policyDecision.selectedAction?.definition?.id?.value)
    }

    @Test
    fun `gate denial yields no capability for CT-V2-03`() {
        val denied = gate.govern(ordinary().copy(selfHarmRelevance = present("synthetic-self-harm")))
        assertNull(denied.ordinaryTherapyPermit)
        assertTrue("ORDINARY_THERAPIST_POLICY" in denied.prohibitedContinuationPaths)
        assertTrue(BoundedProblemPolicyEvaluator::class.java.methods.filter { it.name == "evaluate" }.all { it.parameterCount == 2 })
    }

    @Test
    fun `new established evidence revision causes gate reassessment`() {
        val unknown = ordinary().copy(selfHarmRelevance = SafetyEvidence.unknown())
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, gate.govern(unknown).authorityState)
        val established = ordinary(SafetyEvidenceRevision.of(2)).copy(
            selfHarmRelevance = absent("synthetic-clarified-absence"),
        )
        val reassessed = gate.govern(established)
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, reassessed.authorityState)
        assertEquals(2L, reassessed.ordinaryTherapyPermit?.evidenceRevision?.value)
    }

    @Test
    fun `undefined rule graph returns no authorized action`() {
        val decision = SafetyScopeGate(emptyList()).govern(ordinary())
        assertEquals(SafetyAuthorityState.NO_AUTHORIZED_ACTION, decision.authorityState)
        assertNull(decision.ordinaryTherapyPermit)
    }

    private fun ordinary(revision: SafetyEvidenceRevision = SafetyEvidenceRevision.of(1)) =
        QualificationSafetyGate.ordinaryInput("synthetic-safety-case", revision)

    private fun present(reference: String) = SafetyEvidence.established(
        SafetyPresence.PRESENT,
        SafetyEvidenceOrigin.DIRECT_USER_REPORT,
        reference,
    )

    private fun absent(reference: String) = SafetyEvidence.established(
        SafetyPresence.ABSENT,
        SafetyEvidenceOrigin.VERIFIED_GOVERNED_RESPONSE,
        reference,
    )

    private fun <T> established(value: T, reference: String) = SafetyEvidence.established(
        value,
        SafetyEvidenceOrigin.UPSTREAM_SCOPE_AUTHORITY,
        reference,
    )
}
