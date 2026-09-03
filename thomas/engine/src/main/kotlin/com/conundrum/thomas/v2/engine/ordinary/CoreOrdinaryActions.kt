package com.conundrum.thomas.v2.engine.ordinary

import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.engine.TherapeuticActionProposal
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.engine.verticalslice.ProductionTherapeuticAuthority
import com.conundrum.thomas.v2.engine.verticalslice.ReviewRestriction
import com.conundrum.thomas.v2.engine.verticalslice.RuleExecutionAuthority
import com.conundrum.thomas.v2.ontology.EpistemicResolution
import com.conundrum.thomas.v2.ontology.OntologyConceptId

enum class CoreAuthorizedContextField {
    CONCERN_STATEMENT,
    TENTATIVE_THOMAS_UNDERSTANDING,
    CONFIRMED_UNDERSTANDING,
    IMPORTANT_MISSING_INFORMATION,
    USER_CORRECTION,
    USER_GENERATED_OPTIONS,
    USER_SELECTED_OPTION,
    ACTION_PLAN,
    PLAN_OUTCOME,
}

data class CoreOutcomeContract(
    val expectedInformation: CoreInformationRequirement,
    val reassessConceptIds: Set<OntologyConceptId>,
) {
    init { require(reassessConceptIds.isNotEmpty()) }
}

data class CoreRenderSpecification(
    val instruction: String,
    val requiredSemanticContent: List<String>,
    val allowedSemanticContent: List<String>,
    val prohibitedSemanticContent: List<String>,
    val toneConstraints: List<String>,
    val maximumWords: Int,
    val maximumQuestions: Int,
    val advicePermitted: Boolean,
    val interpretationMustRemainTentative: Boolean,
    val userAgencyMustBeExplicitlyPreserved: Boolean,
    val form: RenderForm,
    val outputDisposition: RenderOutputDisposition,
    val authorizedContextFields: Set<CoreAuthorizedContextField>,
)

data class CoreActionDefinition(
    val id: PolicyActionId,
    val label: String,
    val goalId: OntologyConceptId,
    val dialogueActId: OntologyConceptId,
    val candidateInterventionFamilyId: OntologyConceptId?,
    val renderSpecification: CoreRenderSpecification,
    val outcomeContract: CoreOutcomeContract,
    val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
) {
    init {
        require(label.isNotBlank())
        require(productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
    }
}

data class SelectedCoreQualificationAction(
    val definition: CoreActionDefinition,
    val selectedByRuleId: com.conundrum.thomas.v2.engine.verticalslice.PolicyRuleId,
    val executionAuthority: RuleExecutionAuthority,
    val unresolvedReviewRestrictions: Set<ReviewRestriction>,
    val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
) : TherapeuticActionProposal {
    init {
        require(executionAuthority == RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION)
        require(productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
    }
}

object CoreOrdinaryActions {
    val askSupportPreference = action(
        "core-ask-support-preference", "Ask which supported direction the user wants", "goal.clarify",
        "dialogue.ask-focused-question", "intervention.supportive-listening",
        "Ask one question offering listening, understanding, or practical problem-oriented help.",
        listOf("The user controls the supported route."), CoreInformationRequirement.SUPPORT_PREFERENCE,
        RenderForm.INTERROGATIVE, preserveAgency = true,
    )
    val offerDirectionChoice = action(
        "core-offer-direction-choice", "Offer a bounded direction choice after stagnation", "goal.clarify",
        "dialogue.offer-choices", "intervention.supportive-listening",
        "Offer one bounded choice between continuing the current direction, changing direction, pausing, or closing.",
        listOf("State that repeating the prior act would add no new value.", "Preserve the user's choice."),
        CoreInformationRequirement.CLOSURE_OR_NEW_DIRECTION, RenderForm.INTERROGATIVE, preserveAgency = true,
    )
    val acknowledgeClose = action(
        "core-acknowledge-close", "Acknowledge an explicit close request", "goal.close-or-pause",
        "dialogue.close", null, "Briefly acknowledge the user's decision to end without reopening the conversation.",
        listOf("End the interaction without a question."), CoreInformationRequirement.NONE, RenderForm.SUMMARY,
        questions = 0,
    )
    val pauseWithoutResponse = action(
        "core-pause-without-response", "Respect a pause or topic refusal with silence", "goal.close-or-pause",
        "dialogue.no-response", null, "Produce no generated response.", emptyList(), CoreInformationRequirement.NONE,
        RenderForm.SILENCE, words = 0, questions = 0, output = RenderOutputDisposition.NO_RESPONSE,
    )
    val inviteExpression = action(
        "core-invite-expression", "Invite expression without problem solving", "goal.support-expression",
        "dialogue.ask-open-question", "intervention.supportive-listening",
        "Ask one open question inviting the user to say what they want heard.",
        listOf("Do not redirect toward solutions."), CoreInformationRequirement.USER_EXPRESSION, RenderForm.INTERROGATIVE,
    )
    val reflectEstablishedContent = action(
        "core-reflect-established-content", "Reflect newly established content", "goal.support-expression",
        "dialogue.reflect", "intervention.supportive-listening",
        "Reflect only the authorized established concern without advice, explanation, or a question.",
        listOf("Stay with the user's established content."), CoreInformationRequirement.RESPONSE_TO_REFLECTION,
        RenderForm.REFLECTIVE, questions = 0, context = setOf(CoreAuthorizedContextField.CONCERN_STATEMENT),
    )
    val inviteFurtherExpression = action(
        "core-invite-further-expression", "Invite the user to continue talking", "goal.support-expression",
        "dialogue.ask-open-question", "intervention.supportive-listening",
        "Ask one open question that leaves the direction of further expression with the user.",
        listOf("Do not introduce a new topic or a solution."), CoreInformationRequirement.MORE_USER_EXPRESSION,
        RenderForm.INTERROGATIVE,
    )
    val summarizeListening = action(
        "core-summarize-listening", "Summarize what was heard", "goal.establish-shared-understanding",
        "dialogue.summarize", "intervention.supportive-listening",
        "Summarize only established content without adding a cause or recommendation.",
        listOf("Use only established user content."), CoreInformationRequirement.CLOSURE_OR_NEW_DIRECTION,
        RenderForm.SUMMARY, questions = 0, context = setOf(CoreAuthorizedContextField.CONCERN_STATEMENT),
    )
    val checkFurtherOrClose = action(
        "core-check-further-or-close", "Check whether listening should continue or close", "goal.close-or-pause",
        "dialogue.ask-focused-question", "intervention.supportive-listening",
        "Ask one brief question about whether the user wants to add anything or stop here.",
        listOf("Do not propose problem solving."), CoreInformationRequirement.CLOSURE_OR_NEW_DIRECTION,
        RenderForm.INTERROGATIVE, preserveAgency = true,
    )
    val askPresentConcern = action(
        "core-ask-present-concern", "Ask for the concern to understand", "goal.understand",
        "dialogue.ask-open-question", "intervention.supportive-listening",
        "Ask one open question about the concern the user wants help understanding.",
        listOf("Let the user define the concern."), CoreInformationRequirement.PRESENT_CONCERN,
        RenderForm.INTERROGATIVE,
    )
    val askImportantMissingPiece = action(
        "core-ask-important-missing-piece", "Ask for one identified missing piece", "goal.clarify",
        "dialogue.ask-focused-question", "intervention.supportive-listening",
        "Ask one focused question for the single missing item identified by policy.",
        listOf("Ask only for the authorized missing information."), CoreInformationRequirement.IMPORTANT_MISSING_INFORMATION,
        RenderForm.INTERROGATIVE, context = setOf(CoreAuthorizedContextField.IMPORTANT_MISSING_INFORMATION),
    )
    val verifyTentativeUnderstanding = action(
        "core-verify-tentative-understanding", "Verify a tentative understanding", "goal.establish-shared-understanding",
        "dialogue.verify-understanding", "intervention.supportive-listening",
        "Express the authorized Thomas understanding explicitly as tentative and invite correction with one question.",
        listOf("Mark the interpretation as tentative.", "Invite correction."),
        CoreInformationRequirement.SHARED_UNDERSTANDING_CONFIRMATION, RenderForm.INTERROGATIVE,
        tentative = true, context = setOf(CoreAuthorizedContextField.TENTATIVE_THOMAS_UNDERSTANDING),
    )
    val acknowledgeCorrection = action(
        "core-acknowledge-correction", "Withdraw and acknowledge a corrected interpretation", "goal.understand",
        "dialogue.verify-understanding", "intervention.supportive-listening",
        "Acknowledge that the prior interpretation was wrong and ask one question for the corrected meaning. Do not defend it.",
        listOf("Treat the prior tentative interpretation as withdrawn.", "Invite the user's corrected meaning."),
        CoreInformationRequirement.CORRECTED_MEANING, RenderForm.INTERROGATIVE,
        context = setOf(CoreAuthorizedContextField.USER_CORRECTION),
    )
    val summarizeSharedUnderstanding = action(
        "core-summarize-shared-understanding", "Summarize confirmed understanding", "goal.establish-shared-understanding",
        "dialogue.summarize", "intervention.supportive-listening",
        "Summarize only the user-confirmed understanding without adding explanation or diagnosis.",
        listOf("Use confirmed understanding only."), CoreInformationRequirement.CLOSURE_OR_NEW_DIRECTION,
        RenderForm.SUMMARY, questions = 0, context = setOf(CoreAuthorizedContextField.CONFIRMED_UNDERSTANDING),
    )
    val checkUnderstandingNextDirection = action(
        "core-check-understanding-next-direction", "Check the direction after understanding", "goal.check-understanding",
        "dialogue.ask-focused-question", "intervention.supportive-listening",
        "Ask whether the established understanding is enough or whether the user wants another supported direction.",
        listOf("Preserve the user's choice of next direction."), CoreInformationRequirement.CLOSURE_OR_NEW_DIRECTION,
        RenderForm.INTERROGATIVE, preserveAgency = true,
    )
    val askProblemDescription = action(
        "core-ask-problem-description", "Ask for a bounded practical problem", "goal.understand",
        "dialogue.ask-open-question", "intervention.problem-solving",
        "Ask one open question for the present practical problem the user wants to work on.",
        listOf("Ask for one bounded problem."), CoreInformationRequirement.PRESENT_CONCERN, RenderForm.INTERROGATIVE,
    )
    val verifyProblemUnderstanding = action(
        "core-verify-problem-understanding", "Verify the practical problem", "goal.establish-shared-understanding",
        "dialogue.verify-understanding", "intervention.problem-solving",
        "Present the authorized understanding tentatively and invite correction with one question.",
        listOf("Do not proceed to options before confirmation."), CoreInformationRequirement.SHARED_UNDERSTANDING_CONFIRMATION,
        RenderForm.INTERROGATIVE, tentative = true, context = setOf(CoreAuthorizedContextField.TENTATIVE_THOMAS_UNDERSTANDING),
    )
    val askInfluenceablePart = action(
        "core-ask-influenceable-part", "Ask which part can be influenced", "goal.clarify",
        "dialogue.ask-focused-question", "intervention.problem-solving",
        "Ask one question about the part of the established practical problem the user can influence.",
        listOf("Ask about influence, not blame."), CoreInformationRequirement.INFLUENCEABLE_PART,
        RenderForm.INTERROGATIVE, context = setOf(CoreAuthorizedContextField.CONCERN_STATEMENT),
    )
    val askReadinessForOptions = action(
        "core-ask-readiness-for-options", "Ask permission to consider options", "goal.increase-agency",
        "dialogue.ask-focused-question", "intervention.problem-solving",
        "Ask one question seeking permission to consider possible options.",
        listOf("Preserve the choice not to proceed."), CoreInformationRequirement.READINESS_FOR_OPTIONS,
        RenderForm.INTERROGATIVE, preserveAgency = true,
    )
    val inviteUserOptions = action(
        "core-invite-user-options", "Invite user-generated options", "goal.support-problem-solving",
        "dialogue.ask-open-question", "intervention.problem-solving",
        "Ask for the user's possible options. Do not supply or recommend an option.",
        listOf("Options originate with the user."), CoreInformationRequirement.USER_GENERATED_OPTIONS,
        RenderForm.INTERROGATIVE, preserveAgency = true,
    )
    val askUserToChooseOption = action(
        "core-ask-user-to-choose-option", "Ask the user to choose among their options", "goal.support-decision-making",
        "dialogue.ask-focused-question", "intervention.problem-solving",
        "Ask which user-generated option seems most helpful and feasible. Do not choose for the user.",
        listOf("Refer only to user-generated options."), CoreInformationRequirement.USER_SELECTED_OPTION,
        RenderForm.INTERROGATIVE, preserveAgency = true, context = setOf(CoreAuthorizedContextField.USER_GENERATED_OPTIONS),
    )
    val developBoundedPlan = action(
        "core-develop-bounded-plan", "Develop one bounded plan step", "goal.support-behavioral-planning",
        "dialogue.develop-plan", "intervention.problem-solving",
        "Ask for one small first step and when the user would take it. Do not add tasks.",
        listOf("Tie the plan to the user's selected option."), CoreInformationRequirement.BOUNDED_ACTION_PLAN,
        RenderForm.PLANNING, preserveAgency = true, context = setOf(CoreAuthorizedContextField.USER_SELECTED_OPTION),
    )
    val waitForOutcome = action(
        "core-wait-for-outcome", "Wait for the plan outcome", "goal.reduce-conversational-burden",
        "dialogue.no-response", null, "Produce no response while awaiting the governed outcome.", emptyList(),
        CoreInformationRequirement.REPORTED_PLAN_OUTCOME, RenderForm.SILENCE,
        words = 0, questions = 0, output = RenderOutputDisposition.NO_RESPONSE,
    )
    val reviewReportedOutcome = action(
        "core-review-reported-outcome", "Review the reported plan outcome", "goal.consolidate-learning",
        "dialogue.review-plan", "intervention.problem-solving",
        "Ask one question about what happened, what helped, or what obstacle arose without declaring success or failure.",
        listOf("Use only the authorized plan and reported outcome."), CoreInformationRequirement.OUTCOME_MEANING_OR_OBSTACLE,
        RenderForm.REVIEW, context = setOf(CoreAuthorizedContextField.ACTION_PLAN, CoreAuthorizedContextField.PLAN_OUTCOME),
    )
    val consolidatePlanLearning = action(
        "core-consolidate-plan-learning", "Consolidate the user's reviewed learning", "goal.consolidate-learning",
        "dialogue.summarize", "intervention.problem-solving",
        "Briefly summarize the established plan and outcome, then ask whether to close or choose another supported direction.",
        listOf("Do not infer success, failure, or a new plan."), CoreInformationRequirement.CLOSURE_OR_NEW_DIRECTION,
        RenderForm.REVIEW, context = setOf(CoreAuthorizedContextField.ACTION_PLAN, CoreAuthorizedContextField.PLAN_OUTCOME),
        preserveAgency = true,
    )

    val all: List<CoreActionDefinition> = listOf(
        askSupportPreference, offerDirectionChoice, acknowledgeClose, pauseWithoutResponse,
        inviteExpression, reflectEstablishedContent, inviteFurtherExpression, summarizeListening, checkFurtherOrClose,
        askPresentConcern, askImportantMissingPiece, verifyTentativeUnderstanding, acknowledgeCorrection,
        summarizeSharedUnderstanding, checkUnderstandingNextDirection, askProblemDescription,
        verifyProblemUnderstanding, askInfluenceablePart, askReadinessForOptions, inviteUserOptions,
        askUserToChooseOption, developBoundedPlan, waitForOutcome, reviewReportedOutcome, consolidatePlanLearning,
    ).sortedBy { it.id }

    val byId = all.associateBy { it.id }

    private fun action(
        id: String,
        label: String,
        goal: String,
        dialogue: String,
        intervention: String?,
        instruction: String,
        required: List<String>,
        expected: CoreInformationRequirement,
        form: RenderForm,
        words: Int = 65,
        questions: Int = 1,
        output: RenderOutputDisposition = RenderOutputDisposition.GENERATE_TEXT,
        tentative: Boolean = false,
        preserveAgency: Boolean = false,
        context: Set<CoreAuthorizedContextField> = emptySet(),
    ) = CoreActionDefinition(
        id = PolicyActionId.parse(id),
        label = label,
        goalId = OntologyConceptId.parse(goal),
        dialogueActId = OntologyConceptId.parse(dialogue),
        candidateInterventionFamilyId = intervention?.let(OntologyConceptId::parse),
        renderSpecification = CoreRenderSpecification(
            instruction = instruction,
            requiredSemanticContent = required,
            allowedSemanticContent = listOf("Only the selected act and explicitly authorized supporting text."),
            prohibitedSemanticContent = listOf(
                "Diagnosis, risk prediction, or safety disposition.",
                "A new psychological interpretation or personal fact.",
                "Advice, direction, or an option not explicitly authorized.",
                "Treating tentative evidence as established fact.",
                "A different dialogue act or therapeutic route.",
            ),
            toneConstraints = listOf("Concise", "Collaborative", "Nonjudgmental"),
            maximumWords = words,
            maximumQuestions = questions,
            advicePermitted = false,
            interpretationMustRemainTentative = tentative,
            userAgencyMustBeExplicitlyPreserved = preserveAgency,
            form = form,
            outputDisposition = output,
            authorizedContextFields = context,
        ),
        outcomeContract = CoreOutcomeContract(
            expectedInformation = expected,
            reassessConceptIds = setOf(
                OntologyConceptId.parse("observation.conversational-intent"),
                OntologyConceptId.parse("observation.missing-information"),
                OntologyConceptId.parse("observation.problem-clarity"),
            ),
        ),
    )
}

object CoreRenderRequestFactory {
    fun create(decision: CorePolicyDecision, state: CoreOrdinaryTherapyState): RenderRequest {
        val selected = requireNotNull(decision.selectedAction) { "Only a selected core qualification action can be rendered." }
        require(decision.disposition == CorePolicyDisposition.ACTION_SELECTED)
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
                interpretationMustRemainTentative = specification.interpretationMustRemainTentative,
                userAgencyMustBeExplicitlyPreserved = specification.userAgencyMustBeExplicitlyPreserved,
            ),
            authorizedSupportingText = authorizedText(specification.authorizedContextFields, state),
        )
    }

    private fun authorizedText(
        fields: Set<CoreAuthorizedContextField>,
        state: CoreOrdinaryTherapyState,
    ): List<AuthorizedSupportingText> = buildList {
        if (CoreAuthorizedContextField.CONCERN_STATEMENT in fields && state.concernStatement.isEstablished()) {
            add(AuthorizedSupportingText("established-concern", requireNotNull(state.concernStatement.value)))
        }
        if (CoreAuthorizedContextField.TENTATIVE_THOMAS_UNDERSTANDING in fields &&
            state.thomasUnderstanding.resolution == EpistemicResolution.TENTATIVE
        ) add(AuthorizedSupportingText("tentative-thomas-understanding", requireNotNull(state.thomasUnderstanding.value)))
        if (CoreAuthorizedContextField.CONFIRMED_UNDERSTANDING in fields && state.thomasUnderstanding.isEstablished()) {
            add(AuthorizedSupportingText("confirmed-understanding", requireNotNull(state.thomasUnderstanding.value)))
        }
        if (CoreAuthorizedContextField.IMPORTANT_MISSING_INFORMATION in fields && state.importantMissingInformation.isEstablished()) {
            add(AuthorizedSupportingText("important-missing-information", requireNotNull(state.importantMissingInformation.value)))
        }
        if (CoreAuthorizedContextField.USER_CORRECTION in fields && state.userCorrection.isEstablished()) {
            add(AuthorizedSupportingText("user-correction", requireNotNull(state.userCorrection.value)))
        }
        if (CoreAuthorizedContextField.USER_GENERATED_OPTIONS in fields && state.generatedOptions.isEstablished()) {
            state.generatedOptions.value.orEmpty().forEachIndexed { index, option ->
                add(AuthorizedSupportingText("user-option-${index + 1}", option))
            }
        }
        if (CoreAuthorizedContextField.USER_SELECTED_OPTION in fields && state.selectedOption.isEstablished()) {
            add(AuthorizedSupportingText("user-selected-option", requireNotNull(state.selectedOption.value)))
        }
        if (CoreAuthorizedContextField.ACTION_PLAN in fields && state.actionPlan.isEstablished()) {
            add(AuthorizedSupportingText("action-plan", requireNotNull(state.actionPlan.value)))
        }
        if (CoreAuthorizedContextField.PLAN_OUTCOME in fields && state.planOutcome.isEstablished()) {
            add(AuthorizedSupportingText("plan-outcome", requireNotNull(state.planOutcome.value).name))
        }
    }
}
