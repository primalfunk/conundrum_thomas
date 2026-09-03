package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState
import com.conundrum.thomas.v2.engine.ordinary.CorrectionStatus
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryEngagement
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision

object CoreOrdinaryTestFixtures {
    fun state(
        id: String = "core-test-state",
        support: RequestedOrdinarySupport? = RequestedOrdinarySupport.LISTEN,
        revision: Long = 1,
        safetyRevision: Long = 1,
    ) = CoreOrdinaryTherapyState(
        stateId = id,
        safetyEvidenceRevision = SafetyEvidenceRevision.of(safetyRevision),
        conversationRevision = revision,
        mode = ThomasMode.THERAPIST,
        routePreference = support?.let { PolicyEvidence.reported(it, "$id-support") } ?: PolicyEvidence.unknown(),
        engagement = PolicyEvidence.reported(OrdinaryEngagement.ENGAGED, "$id-engagement"),
        correctionStatus = PolicyEvidence.reported(CorrectionStatus.NONE, "$id-correction"),
        understandingSummaryDelivered = PolicyEvidence.reported(false, "$id-summary"),
    )

    fun permit(state: CoreOrdinaryTherapyState) = QualificationSafetyGate.permitFor(state)
}
