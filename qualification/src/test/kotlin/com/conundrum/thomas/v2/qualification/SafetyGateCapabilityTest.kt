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
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision
import com.conundrum.thomas.v2.safety.SafetyScopeGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyGateCapabilityTest {
    @Test
    fun `ordinary evaluator exposes no state-only bypass overload`() {
        val methods = BoundedProblemPolicyEvaluator::class.java.methods.filter { it.name == "evaluate" }
        assertTrue(methods.isNotEmpty())
        assertTrue(methods.all { it.parameterCount == 2 })
        assertTrue(methods.all { it.parameterTypes.last().simpleName == "OrdinaryTherapyPermit" })
    }

    @Test
    fun `permit is bound to exact state and evidence revision`() {
        val state = ordinaryState("permit-binding", SafetyEvidenceRevision.of(1))
        val permit = requireNotNull(
            SafetyScopeGate().govern(QualificationSafetyGate.ordinaryInput(state.stateId, state.safetyEvidenceRevision)).ordinaryTherapyPermit,
        )
        assertTrue(permit.authorizes("permit-binding", SafetyEvidenceRevision.of(1)))
        assertTrue(!permit.authorizes("different-state", SafetyEvidenceRevision.of(1)))
        assertTrue(!permit.authorizes("permit-binding", SafetyEvidenceRevision.of(2)))
    }

    @Test
    fun `stale permit makes ordinary evaluation fail closed`() {
        val state = ordinaryState("stale-permit", SafetyEvidenceRevision.of(1))
        val permit = QualificationSafetyGate.permitFor(state)
        val revised = state.copy(safetyEvidenceRevision = SafetyEvidenceRevision.of(2))
        val decision = BoundedProblemPolicyEvaluator().evaluate(revised, permit)
        assertEquals(PolicyDecisionDisposition.INVALID_INPUT, decision.disposition)
        assertNull(decision.selectedAction)
        assertEquals("FRESH_SAFETY_SCOPE_GATE_DECISION_REQUIRED", decision.handoffRequirement)
    }

    @Test
    fun `permit cannot override CT-V2-03 own explicit scope guard`() {
        val state = ordinaryState("defense-in-depth").copy(scope = BoundedProblemScope.SPECIALIZED_POLICY_REQUIRED)
        val permit = QualificationSafetyGate.permitFor(state)
        val decision = BoundedProblemPolicyEvaluator().evaluate(state, permit)
        assertEquals(PolicyDecisionDisposition.SPECIALIZED_POLICY_REQUIRED, decision.disposition)
        assertNull(decision.selectedAction)
    }

    private fun ordinaryState(
        id: String,
        revision: SafetyEvidenceRevision = SafetyEvidenceRevision.of(1),
    ) = BoundedProblemPolicyState(
        stateId = id,
        safetyEvidenceRevision = revision,
        mode = ThomasMode.THERAPIST,
        upstreamSafetyDisposition = UpstreamSafetyDisposition.ORDINARY_SLICE_ALLOWED,
        scope = BoundedProblemScope.BOUNDED_NON_EMERGENCY_PERSONAL_PROBLEM,
        willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_EXPLORE, "synthetic-willingness"),
    )
}
