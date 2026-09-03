package com.conundrum.thomas.v2.qualification.safety

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDecision
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyEvaluator
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyState
import com.conundrum.thomas.v2.engine.verticalslice.PolicyDecision
import com.conundrum.thomas.v2.safety.ExplicitEmergencyCircumstance
import com.conundrum.thomas.v2.safety.OrdinaryTherapyPermit
import com.conundrum.thomas.v2.safety.PopulationApplicability
import com.conundrum.thomas.v2.safety.PresentingScope
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyEvidence
import com.conundrum.thomas.v2.safety.SafetyEvidenceOrigin
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision
import com.conundrum.thomas.v2.safety.SafetyPresence
import com.conundrum.thomas.v2.safety.SafetyScopeGate
import com.conundrum.thomas.v2.safety.SafetyScopeInput
import com.conundrum.thomas.v2.safety.SpecializedScopeCondition

/** Synthetic qualification evidence only. No production observation or inference is implemented. */
object QualificationSafetyGate {
    fun ordinaryInput(stateId: String, revision: SafetyEvidenceRevision = SafetyEvidenceRevision.of(1)) = SafetyScopeInput(
        stateId = stateId,
        evidenceRevision = revision,
        mode = ThomasMode.THERAPIST,
        currentEmergency = SafetyEvidence.established(
            ExplicitEmergencyCircumstance.NONE_ESTABLISHED,
            SafetyEvidenceOrigin.VERIFIED_GOVERNED_RESPONSE,
            "$stateId-current-emergency",
        ),
        acuteMedicalEmergency = SafetyEvidence.established(
            SafetyPresence.ABSENT,
            SafetyEvidenceOrigin.VERIFIED_GOVERNED_RESPONSE,
            "$stateId-medical-emergency",
        ),
        selfHarmRelevance = SafetyEvidence.established(
            SafetyPresence.ABSENT,
            SafetyEvidenceOrigin.VERIFIED_GOVERNED_RESPONSE,
            "$stateId-self-harm",
        ),
        harmToOthersRelevance = SafetyEvidence.established(
            SafetyPresence.ABSENT,
            SafetyEvidenceOrigin.VERIFIED_GOVERNED_RESPONSE,
            "$stateId-harm-others",
        ),
        specializedScopeCondition = SafetyEvidence.established(
            SpecializedScopeCondition.NONE_IDENTIFIED,
            SafetyEvidenceOrigin.UPSTREAM_SCOPE_AUTHORITY,
            "$stateId-specialized-scope",
        ),
        populationApplicability = SafetyEvidence.established(
            PopulationApplicability.SUPPORTED_ADULT_QUALIFICATION_CONTEXT,
            SafetyEvidenceOrigin.UPSTREAM_SCOPE_AUTHORITY,
            "$stateId-population",
        ),
        presentingScope = SafetyEvidence.established(
            PresentingScope.BOUNDED_ORDINARY_PERSONAL_PROBLEM,
            SafetyEvidenceOrigin.UPSTREAM_SCOPE_AUTHORITY,
            "$stateId-presenting-scope",
        ),
    )

    fun permitFor(state: BoundedProblemPolicyState): OrdinaryTherapyPermit {
        val decision = SafetyScopeGate().govern(ordinaryInput(state.stateId, state.safetyEvidenceRevision))
        require(decision.authorityState == SafetyAuthorityState.ORDINARY_POLICY_ALLOWED)
        return requireNotNull(decision.ordinaryTherapyPermit)
    }

    fun evaluateOrdinary(
        evaluator: BoundedProblemPolicyEvaluator,
        state: BoundedProblemPolicyState,
    ): PolicyDecision = evaluator.evaluate(state, permitFor(state))

    fun permitFor(state: CoreOrdinaryTherapyState): OrdinaryTherapyPermit {
        val decision = SafetyScopeGate().govern(ordinaryInput(state.stateId, state.safetyEvidenceRevision))
        require(decision.authorityState == SafetyAuthorityState.ORDINARY_POLICY_ALLOWED)
        return requireNotNull(decision.ordinaryTherapyPermit)
    }

    fun evaluateCoreOrdinary(
        evaluator: CoreOrdinaryTherapyEvaluator,
        state: CoreOrdinaryTherapyState,
    ): CorePolicyDecision = evaluator.evaluate(state, permitFor(state))
}
