package com.conundrum.thomas.v2.safety

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.ontology.OntologyConceptId

enum class SafetyInformationRequirement(val field: SafetyField) {
    CURRENT_EMERGENCY_STATUS(SafetyField.CURRENT_EMERGENCY),
    ACUTE_MEDICAL_EMERGENCY_STATUS(SafetyField.ACUTE_MEDICAL_EMERGENCY),
    SELF_HARM_RELEVANCE(SafetyField.SELF_HARM_RELEVANCE),
    HARM_TO_OTHERS_RELEVANCE(SafetyField.HARM_TO_OTHERS_RELEVANCE),
    SPECIALIZED_SCOPE_STATUS(SafetyField.SPECIALIZED_SCOPE_CONDITION),
    POPULATION_APPLICABILITY(SafetyField.POPULATION_APPLICABILITY),
    PRESENTING_SCOPE(SafetyField.PRESENTING_SCOPE),
    ;

    companion object {
        fun forField(field: SafetyField) = entries.single { it.field == field }
    }
}

data class SafetyRenderSpecification(
    val instruction: String,
    val requiredSemanticContent: List<String>,
    val allowedSemanticContent: List<String>,
    val prohibitedSemanticContent: List<String>,
    val toneConstraints: List<String>,
    val maximumWords: Int,
    val maximumQuestions: Int,
    val directWordingRequired: Boolean,
    val externalHelpInformationRequired: Boolean,
    val conversationContinuationPermitted: Boolean,
)

data class SafetyActionDefinition(
    val id: SafetyActionId,
    val goalId: OntologyConceptId,
    val dialogueActId: OntologyConceptId,
    val renderSpecification: SafetyRenderSpecification,
    val unresolvedReviewRestrictions: Set<SafetyReviewRestriction>,
    val productionAuthority: ProductionSafetyAuthority = ProductionSafetyAuthority.NOT_GRANTED,
) {
    init {
        require(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED in unresolvedReviewRestrictions)
        require(productionAuthority == ProductionSafetyAuthority.NOT_GRANTED)
    }
}

data class SelectedSafetyQualificationAction(
    val definition: SafetyActionDefinition,
    val informationRequirement: SafetyInformationRequirement,
    val selectedByRuleId: SafetyRuleId,
    val executionAuthority: SafetyRuleExecutionAuthority,
    val productionAuthority: ProductionSafetyAuthority = ProductionSafetyAuthority.NOT_GRANTED,
) {
    init {
        require(executionAuthority == SafetyRuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION)
        require(productionAuthority == ProductionSafetyAuthority.NOT_GRANTED)
    }
}

object SafetyGateActions {
    val clarifyRequiredFact = SafetyActionDefinition(
        id = SafetyActionId.parse("clarify-required-safety-fact"),
        goalId = OntologyConceptId.parse("goal.assess-safety"),
        dialogueActId = OntologyConceptId.parse("dialogue.safety-oriented-inquiry"),
        renderSpecification = SafetyRenderSpecification(
            instruction = "Ask one direct question only to establish the safety or scope fact selected by the gate.",
            requiredSemanticContent = listOf(
                "State or ask only for the specifically identified required fact.",
                "Make clear that ordinary problem-solving is paused pending clarification.",
            ),
            allowedSemanticContent = listOf("The selected information requirement and the fact that ordinary policy is paused."),
            prohibitedSemanticContent = listOf(
                "Ordinary therapeutic exploration, advice, problem solving, or reassurance.",
                "Diagnosis, prediction, probability, score, threshold, or low/medium/high risk label.",
                "A screening instrument, screening-item wording, or a multi-question assessment.",
                "An emergency instruction or external-help destination not selected by a specialized policy.",
                "Any inference that an unknown, tentative, declined, or contradictory fact is absent.",
            ),
            toneConstraints = listOf("Direct", "Plain", "Nonjudgmental", "One question maximum"),
            maximumWords = 35,
            maximumQuestions = 1,
            directWordingRequired = true,
            externalHelpInformationRequired = false,
            conversationContinuationPermitted = true,
        ),
        unresolvedReviewRestrictions = setOf(
            SafetyReviewRestriction.CLINICAL_REVIEW_PENDING,
            SafetyReviewRestriction.IMPLEMENTATION_SCOPE_REVIEW_PENDING,
            SafetyReviewRestriction.SOFTWARE_AUTONOMY_REVIEW_PENDING,
            SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED,
        ),
    )

    val all = listOf(clarifyRequiredFact)
}

object SafetyRenderRequestFactory {
    fun create(decision: SafetyScopeDecision): RenderRequest {
        val selected = requireNotNull(decision.selectedAction) {
            "Only a safety-gate decision with an explicitly selected user-facing action can be rendered."
        }
        require(decision.authorityState == SafetyAuthorityState.CLARIFICATION_REQUIRED)
        require(decision.ordinaryTherapyPermit == null)
        val specification = selected.definition.renderSpecification
        return RenderRequest(
            command = RenderCommand(
                policyDecisionReference = decision.decisionReference,
                selectedPolicyActionId = selected.definition.id.value,
                selectedDialogueActId = selected.definition.dialogueActId.value,
                therapeuticGoalId = selected.definition.goalId.value,
                instruction = specification.instruction,
                requiredSemanticContent = specification.requiredSemanticContent,
                allowedSemanticContent = specification.allowedSemanticContent,
                prohibitedSemanticContent = specification.prohibitedSemanticContent,
                toneConstraints = specification.toneConstraints,
                maximumWords = specification.maximumWords,
                maximumQuestions = specification.maximumQuestions,
                advicePermitted = false,
                form = RenderForm.INTERROGATIVE,
                outputDisposition = RenderOutputDisposition.GENERATE_TEXT,
                directWordingRequired = specification.directWordingRequired,
                ordinaryTherapeuticContentPermitted = false,
                externalHelpInformationRequired = specification.externalHelpInformationRequired,
                conversationContinuationPermitted = specification.conversationContinuationPermitted,
            ),
            authorizedSupportingText = listOf(
                AuthorizedSupportingText(
                    reference = "required-safety-information",
                    text = selected.informationRequirement.name,
                ),
            ),
        )
    }
}
