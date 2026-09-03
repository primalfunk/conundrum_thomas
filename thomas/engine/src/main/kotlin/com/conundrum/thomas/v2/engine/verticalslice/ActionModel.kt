package com.conundrum.thomas.v2.engine.verticalslice

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.engine.TherapeuticActionProposal
import com.conundrum.thomas.v2.ontology.OntologyConceptId

enum class AuthorizedContextField {
    PROBLEM_STATEMENT,
    USER_GENERATED_OPTIONS,
    USER_SELECTED_OPTION,
    ACTION_PLAN,
    PLAN_OUTCOME,
}

data class OutcomeContract(
    val expectedInformation: InformationRequirement,
    val reassessConceptIds: Set<OntologyConceptId>,
) {
    init { require(reassessConceptIds.isNotEmpty()) }
}

data class RenderSpecification(
    val instruction: String,
    val requiredSemanticContent: List<String>,
    val allowedSemanticContent: List<String>,
    val prohibitedSemanticContent: List<String>,
    val toneConstraints: List<String>,
    val maximumWords: Int,
    val maximumQuestions: Int,
    val advicePermitted: Boolean,
    val form: RenderForm,
    val outputDisposition: RenderOutputDisposition = RenderOutputDisposition.GENERATE_TEXT,
    val authorizedContextFields: Set<AuthorizedContextField> = emptySet(),
)

data class PolicyActionDefinition(
    val id: PolicyActionId,
    val label: String,
    val goalId: OntologyConceptId,
    val dialogueActId: OntologyConceptId,
    val candidateInterventionFamilyId: OntologyConceptId?,
    val renderSpecification: RenderSpecification,
    val outcomeContract: OutcomeContract,
    val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
) {
    init {
        require(label.isNotBlank())
        require(productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
    }
}

data class SelectedQualificationAction(
    val definition: PolicyActionDefinition,
    val selectedByRuleId: PolicyRuleId,
    val executionAuthority: RuleExecutionAuthority,
    val unresolvedReviewRestrictions: Set<ReviewRestriction>,
    val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
) : TherapeuticActionProposal {
    init {
        require(executionAuthority == RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION)
        require(productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
    }
}

object BoundedProblemActions {
    val askSupportPreference = action(
        id = "ask-support-preference",
        label = "Ask which kind of support the user wants",
        goal = "goal.clarify",
        dialogue = "dialogue.ask-focused-question",
        intervention = "intervention.supportive-listening",
        instruction = "Ask one principal question that lets the user choose listening, understanding, or practical problem-oriented help.",
        required = listOf("The user controls which support direction, if any, is taken."),
        form = RenderForm.INTERROGATIVE,
        expected = InformationRequirement.SUPPORT_INTENT,
    )

    val askProblemDescription = action(
        id = "ask-problem-description",
        label = "Invite a bounded description of the present problem",
        goal = "goal.understand",
        dialogue = "dialogue.ask-open-question",
        intervention = "intervention.supportive-listening",
        instruction = "Ask one open question inviting the user to describe the present problem in their own terms.",
        required = listOf("Ask about the one present problem the user wants to focus on."),
        form = RenderForm.INTERROGATIVE,
        expected = InformationRequirement.BOUNDED_PROBLEM_DESCRIPTION,
    )

    val verifyProblemUnderstanding = action(
        id = "verify-problem-understanding",
        label = "Verify the bounded problem understanding",
        goal = "goal.establish-shared-understanding",
        dialogue = "dialogue.verify-understanding",
        intervention = "intervention.supportive-listening",
        instruction = "Briefly summarize only the authorized problem statement and ask one question inviting correction.",
        required = listOf("Present the current understanding tentatively.", "Invite correction."),
        form = RenderForm.INTERROGATIVE,
        expected = InformationRequirement.SHARED_UNDERSTANDING_CONFIRMATION,
        context = setOf(AuthorizedContextField.PROBLEM_STATEMENT),
    )

    val reflectForListening = action(
        id = "reflect-for-listening",
        label = "Reflect without moving into problem solving",
        goal = "goal.support-expression",
        dialogue = "dialogue.reflect",
        intervention = "intervention.supportive-listening",
        instruction = "Reflect the authorized problem content without advice, a new interpretation, or a question.",
        required = listOf("Stay with what the user has already established."),
        form = RenderForm.REFLECTIVE,
        expected = InformationRequirement.BOUNDED_PROBLEM_DESCRIPTION,
        questions = 0,
        context = setOf(AuthorizedContextField.PROBLEM_STATEMENT),
    )

    val summarizeForUnderstanding = action(
        id = "summarize-for-understanding",
        label = "Summarize established understanding",
        goal = "goal.establish-shared-understanding",
        dialogue = "dialogue.summarize",
        intervention = "intervention.supportive-listening",
        instruction = "Summarize only the established problem content and leave room for the user to add or correct information.",
        required = listOf("Do not introduce a cause, diagnosis, or recommendation."),
        form = RenderForm.SUMMARY,
        expected = InformationRequirement.BOUNDED_PROBLEM_DESCRIPTION,
        questions = 0,
        context = setOf(AuthorizedContextField.PROBLEM_STATEMENT),
    )

    val askInfluenceablePart = action(
        id = "ask-influenceable-part",
        label = "Ask which part of the problem can be influenced",
        goal = "goal.clarify",
        dialogue = "dialogue.ask-focused-question",
        intervention = "intervention.problem-solving",
        instruction = "Ask one focused question about which part of the established practical problem the user can influence or change.",
        required = listOf("Ask about influence over the problem, not blame or cause."),
        form = RenderForm.INTERROGATIVE,
        expected = InformationRequirement.INFLUENCEABLE_PART,
        context = setOf(AuthorizedContextField.PROBLEM_STATEMENT),
    )

    val askReadinessForOptions = action(
        id = "ask-readiness-for-options",
        label = "Ask whether the user wants to consider options",
        goal = "goal.increase-agency",
        dialogue = "dialogue.ask-focused-question",
        intervention = "intervention.problem-solving",
        instruction = "Ask one question seeking permission to consider possible ways of influencing the bounded problem.",
        required = listOf("Preserve the user's choice not to proceed."),
        form = RenderForm.INTERROGATIVE,
        expected = InformationRequirement.READINESS_FOR_OPTIONS,
    )

    val inviteUserOptions = action(
        id = "invite-user-options",
        label = "Invite user-generated possible options",
        goal = "goal.support-problem-solving",
        dialogue = "dialogue.ask-open-question",
        intervention = "intervention.problem-solving",
        instruction = "Ask one open question inviting the user to name possible ways of influencing the bounded problem. Do not supply an option.",
        required = listOf("Options originate with the user in this qualification slice."),
        form = RenderForm.INTERROGATIVE,
        expected = InformationRequirement.USER_GENERATED_OPTIONS,
        context = setOf(AuthorizedContextField.PROBLEM_STATEMENT),
    )

    val askUserToChooseOption = action(
        id = "ask-user-to-choose-option",
        label = "Ask the user to choose among their options",
        goal = "goal.support-decision-making",
        dialogue = "dialogue.ask-focused-question",
        intervention = "intervention.problem-solving",
        instruction = "Ask one question inviting the user to choose which of their authorized options seems most helpful and feasible. Do not choose for them.",
        required = listOf("Refer only to user-generated options."),
        form = RenderForm.INTERROGATIVE,
        expected = InformationRequirement.USER_SELECTED_OPTION,
        context = setOf(AuthorizedContextField.USER_GENERATED_OPTIONS),
    )

    val developBoundedPlan = action(
        id = "develop-bounded-plan",
        label = "Ask for one small plan step",
        goal = "goal.support-behavioral-planning",
        dialogue = "dialogue.develop-plan",
        intervention = "intervention.problem-solving",
        instruction = "Ask one focused question about the first small step and when the user would take it. Do not add tasks.",
        required = listOf("Keep the plan tied to the user's selected option."),
        form = RenderForm.PLANNING,
        expected = InformationRequirement.BOUNDED_ACTION_PLAN,
        context = setOf(AuthorizedContextField.USER_SELECTED_OPTION),
    )

    val waitForOutcome = action(
        id = "wait-for-outcome",
        label = "Wait without generating language",
        goal = "goal.reduce-conversational-burden",
        dialogue = "dialogue.no-response",
        intervention = null,
        instruction = "Produce no generated conversational response while awaiting the governed outcome observation.",
        required = emptyList(),
        form = RenderForm.SILENCE,
        expected = InformationRequirement.REPORTED_PLAN_OUTCOME,
        words = 0,
        questions = 0,
        outputDisposition = RenderOutputDisposition.NO_RESPONSE,
    )

    val reviewReportedOutcome = action(
        id = "review-reported-outcome",
        label = "Review the user's reported result",
        goal = "goal.consolidate-learning",
        dialogue = "dialogue.review-plan",
        intervention = "intervention.problem-solving",
        instruction = "Ask one focused question about what happened, what helped, or what obstacle arose. Do not declare the plan a success or failure.",
        required = listOf("Use only the authorized plan and reported outcome."),
        form = RenderForm.REVIEW,
        expected = InformationRequirement.OUTCOME_MEANING_OR_OBSTACLE,
        context = setOf(AuthorizedContextField.ACTION_PLAN, AuthorizedContextField.PLAN_OUTCOME),
    )

    val pauseWithoutResponse = action(
        id = "pause-without-response",
        label = "Respect a decision not to continue",
        goal = "goal.close-or-pause",
        dialogue = "dialogue.no-response",
        intervention = null,
        instruction = "Produce no generated conversational response because the user is unwilling to continue this slice.",
        required = emptyList(),
        form = RenderForm.SILENCE,
        expected = InformationRequirement.SUPPORT_INTENT,
        words = 0,
        questions = 0,
        outputDisposition = RenderOutputDisposition.NO_RESPONSE,
    )

    val all: List<PolicyActionDefinition> = listOf(
        askSupportPreference,
        askProblemDescription,
        verifyProblemUnderstanding,
        reflectForListening,
        summarizeForUnderstanding,
        askInfluenceablePart,
        askReadinessForOptions,
        inviteUserOptions,
        askUserToChooseOption,
        developBoundedPlan,
        waitForOutcome,
        reviewReportedOutcome,
        pauseWithoutResponse,
    ).sortedBy { it.id }

    val byId: Map<PolicyActionId, PolicyActionDefinition> = all.associateBy { it.id }

    private fun action(
        id: String,
        label: String,
        goal: String,
        dialogue: String,
        intervention: String?,
        instruction: String,
        required: List<String>,
        form: RenderForm,
        expected: InformationRequirement,
        words: Int = 60,
        questions: Int = 1,
        outputDisposition: RenderOutputDisposition = RenderOutputDisposition.GENERATE_TEXT,
        context: Set<AuthorizedContextField> = emptySet(),
    ) = PolicyActionDefinition(
        id = PolicyActionId.parse(id),
        label = label,
        goalId = conceptId(goal),
        dialogueActId = conceptId(dialogue),
        candidateInterventionFamilyId = intervention?.let(::conceptId),
        renderSpecification = RenderSpecification(
            instruction = instruction,
            requiredSemanticContent = required,
            allowedSemanticContent = listOf("Only the selected dialogue act and explicitly authorized supporting text."),
            prohibitedSemanticContent = listOf(
                "Diagnosis or diagnostic implication.",
                "A new psychological interpretation.",
                "An option, fact, or personal detail not explicitly authorized.",
                "Any safety decision or escalation instruction.",
                "A different dialogue act.",
            ),
            toneConstraints = listOf("Concise", "Collaborative", "Tentative where meaning is not user-confirmed"),
            maximumWords = words,
            maximumQuestions = questions,
            advicePermitted = false,
            form = form,
            outputDisposition = outputDisposition,
            authorizedContextFields = context,
        ),
        outcomeContract = OutcomeContract(
            expectedInformation = expected,
            reassessConceptIds = setOf(
                conceptId("observation.conversational-intent"),
                conceptId("observation.problem-clarity"),
                conceptId("observation.readiness-to-act"),
                conceptId("observation.missing-information"),
            ),
        ),
    )
}

object PolicyRenderRequestFactory {
    fun create(decision: PolicyDecision, state: BoundedProblemPolicyState): RenderRequest {
        val selected = requireNotNull(decision.selectedAction) { "Only a selected qualification action can be rendered." }
        require(decision.disposition == PolicyDecisionDisposition.ACTION_SELECTED)
        require(selected.productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
        val action = selected.definition
        val specification = action.renderSpecification
        return RenderRequest(
            command = RenderCommand(
                policyDecisionReference = decision.decisionReference,
                selectedPolicyActionId = action.id.value,
                selectedDialogueActId = action.dialogueActId.value,
                therapeuticGoalId = action.goalId.value,
                instruction = specification.instruction,
                requiredSemanticContent = specification.requiredSemanticContent,
                allowedSemanticContent = specification.allowedSemanticContent,
                prohibitedSemanticContent = specification.prohibitedSemanticContent,
                toneConstraints = specification.toneConstraints,
                maximumWords = specification.maximumWords,
                maximumQuestions = specification.maximumQuestions,
                advicePermitted = specification.advicePermitted,
                form = specification.form,
                outputDisposition = specification.outputDisposition,
            ),
            authorizedSupportingText = authorizedText(specification.authorizedContextFields, state),
        )
    }

    private fun authorizedText(
        fields: Set<AuthorizedContextField>,
        state: BoundedProblemPolicyState,
    ): List<AuthorizedSupportingText> = buildList {
        if (AuthorizedContextField.PROBLEM_STATEMENT in fields && state.problemStatement.isEstablished()) {
            add(AuthorizedSupportingText("problem-statement", requireNotNull(state.problemStatement.value)))
        }
        if (AuthorizedContextField.USER_GENERATED_OPTIONS in fields && state.generatedOptions.isEstablished()) {
            state.generatedOptions.value.orEmpty().forEachIndexed { index, option ->
                add(AuthorizedSupportingText("user-option-${index + 1}", option))
            }
        }
        if (AuthorizedContextField.USER_SELECTED_OPTION in fields && state.selectedOption.isEstablished()) {
            add(AuthorizedSupportingText("user-selected-option", requireNotNull(state.selectedOption.value)))
        }
        if (AuthorizedContextField.ACTION_PLAN in fields && state.actionPlan.isEstablished()) {
            add(AuthorizedSupportingText("action-plan", requireNotNull(state.actionPlan.value)))
        }
        if (AuthorizedContextField.PLAN_OUTCOME in fields && state.planOutcome.isEstablished()) {
            add(AuthorizedSupportingText("plan-outcome", requireNotNull(state.planOutcome.value).name))
        }
    }
}
