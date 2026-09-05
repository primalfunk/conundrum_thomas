package com.conundrum.thomas.v2.runtime

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.ordinary.CoreActionExecution
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState
import com.conundrum.thomas.v2.engine.ordinary.CorrectionStatus
import com.conundrum.thomas.v2.engine.ordinary.ExpressionProgress
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryEngagement
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.safety.ExplicitEmergencyCircumstance
import com.conundrum.thomas.v2.safety.PopulationApplicability
import com.conundrum.thomas.v2.safety.PresentingScope
import com.conundrum.thomas.v2.safety.SafetyEvidence
import com.conundrum.thomas.v2.safety.SafetyEvidenceOrigin
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision
import com.conundrum.thomas.v2.safety.SafetyPresence
import com.conundrum.thomas.v2.safety.SafetyScopeInput
import com.conundrum.thomas.v2.safety.SpecializedScopeCondition

/**
 * Converts explicit current interaction controls into CT-V2-04/05 state. It performs no text
 * diagnosis, history lookup, route selection, or technique selection.
 */
internal object ProductionTherapyInputBoundary {
    fun state(
        stateId: String,
        revision: Long,
        currentText: String,
        support: RequestedOrdinarySupport,
        history: List<CoreActionExecution>,
    ) = CoreOrdinaryTherapyState(
        stateId = stateId,
        safetyEvidenceRevision = SafetyEvidenceRevision.of(revision),
        conversationRevision = revision,
        mode = ThomasMode.THERAPIST,
        routePreference = PolicyEvidence.reported(support, "$stateId-support"),
        engagement = PolicyEvidence.reported(OrdinaryEngagement.ENGAGED, "$stateId-engagement"),
        concernStatement = PolicyEvidence.reported(currentText, "$stateId-current-turn"),
        problemClarity = PolicyEvidence.reported(ProblemClarity.BOUNDED, "$stateId-current-turn-bounded"),
        expressionProgress = PolicyEvidence.reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, "$stateId-new-content"),
        correctionStatus = PolicyEvidence.reported(CorrectionStatus.NONE, "$stateId-correction"),
        understandingSummaryDelivered = PolicyEvidence.reported(false, "$stateId-summary"),
        actionHistory = history,
    )

    fun safety(
        stateId: String,
        revision: SafetyEvidenceRevision,
        declaration: TherapySafetyDeclaration,
    ): SafetyScopeInput {
        val direct = SafetyEvidenceOrigin.DIRECT_USER_REPORT
        val scope = SafetyEvidenceOrigin.UPSTREAM_SCOPE_AUTHORITY
        val unknownEmergency = SafetyEvidence.unknown<ExplicitEmergencyCircumstance>()
        val unknownPresence = SafetyEvidence.unknown<SafetyPresence>()
        val ordinary = declaration == TherapySafetyDeclaration.ORDINARY_NON_EMERGENCY_ADULT_CONTEXT
        val emergency = declaration == TherapySafetyDeclaration.CURRENT_EMERGENCY
        return SafetyScopeInput(
            stateId = stateId,
            evidenceRevision = revision,
            mode = ThomasMode.THERAPIST,
            currentEmergency = when {
                ordinary -> SafetyEvidence.established(
                    ExplicitEmergencyCircumstance.NONE_ESTABLISHED,
                    direct,
                    "$stateId-user-ordinary-attestation",
                )
                emergency -> SafetyEvidence.established(
                    ExplicitEmergencyCircumstance.OTHER_EMERGENCY_EXPLICITLY_ESTABLISHED,
                    direct,
                    "$stateId-user-emergency-declaration",
                )
                else -> unknownEmergency
            },
            acuteMedicalEmergency = if (ordinary) {
                SafetyEvidence.established(SafetyPresence.ABSENT, direct, "$stateId-user-no-medical-emergency")
            } else {
                unknownPresence
            },
            selfHarmRelevance = if (ordinary) {
                SafetyEvidence.established(SafetyPresence.ABSENT, direct, "$stateId-user-no-self-harm")
            } else {
                unknownPresence
            },
            harmToOthersRelevance = if (ordinary) {
                SafetyEvidence.established(SafetyPresence.ABSENT, direct, "$stateId-user-no-harm-others")
            } else {
                unknownPresence
            },
            specializedScopeCondition = if (ordinary) {
                SafetyEvidence.established(
                    SpecializedScopeCondition.NONE_IDENTIFIED,
                    scope,
                    "$stateId-supported-scope",
                )
            } else {
                SafetyEvidence.unknown()
            },
            populationApplicability = if (ordinary) {
                SafetyEvidence.established(
                    PopulationApplicability.SUPPORTED_ADULT_QUALIFICATION_CONTEXT,
                    scope,
                    "$stateId-supported-adult-scope",
                )
            } else {
                SafetyEvidence.unknown()
            },
            presentingScope = if (ordinary) {
                SafetyEvidence.established(
                    PresentingScope.BOUNDED_ORDINARY_PERSONAL_PROBLEM,
                    scope,
                    "$stateId-bounded-ordinary-scope",
                )
            } else {
                SafetyEvidence.unknown()
            },
        )
    }
}
