package com.conundrum.thomas.v2.engine.verticalslice

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.ontology.GovernanceReviewStatus
import com.conundrum.thomas.v2.ontology.OntologyConceptId
import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.SourceDocumentId
import com.conundrum.thomas.v2.provenance.SourceLocatorId
import com.conundrum.thomas.v2.provenance.SourceSectionId
import com.conundrum.thomas.v2.provenance.SourceVersionId

const val CT_V2_03_POLICY_VERSION = "ct-v2-03-bounded-problem-1.0.0"

enum class ProceduralStage {
    SCOPE_GATE,
    ESTABLISH_SUPPORT_INTENT,
    UNDERSTAND_PRESENT_PROBLEM,
    ESTABLISH_SHARED_UNDERSTANDING,
    SUPPORT_EXPRESSION,
    DETERMINE_INFLUENCEABLE_PROBLEM,
    ESTABLISH_READINESS,
    GENERATE_OPTIONS,
    SELECT_OPTION,
    DEVELOP_PLAN,
    AWAIT_OUTCOME,
    REVIEW_OUTCOME,
    TERMINATED,
}

enum class TieBreakPrinciple {
    HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER,
}

data class TieBreakTrace(
    val principle: TieBreakPrinciple,
    val highestPriority: Int?,
    val contenderRuleIds: List<PolicyRuleId>,
    val resolution: String,
)

data class RejectedCandidateAction(
    val ruleId: PolicyRuleId,
    val actionId: PolicyActionId?,
    val reasons: List<String>,
)

data class PolicyDecision(
    val policyVersion: String,
    val decisionReference: String,
    val stateId: String,
    val currentProceduralStage: ProceduralStage,
    val disposition: PolicyDecisionDisposition,
    val activeGoalId: OntologyConceptId?,
    val eligibleCandidateActions: List<PolicyActionId>,
    val rejectedCandidateActions: List<RejectedCandidateAction>,
    val selectedAction: SelectedQualificationAction?,
    val selectedRuleProvenance: List<RuleProvenance>,
    val unresolvedRequirements: List<String>,
    val nextStateExpectations: OutcomeContract?,
    val handoffRequirement: String?,
    val ruleTrace: List<RuleEvaluationTrace>,
    val tieBreakTrace: TieBreakTrace,
) {
    init {
        require(policyVersion == CT_V2_03_POLICY_VERSION)
        if (selectedAction != null) {
            require(disposition == PolicyDecisionDisposition.ACTION_SELECTED)
            require(activeGoalId == selectedAction.definition.goalId)
            require(nextStateExpectations == selectedAction.definition.outcomeContract)
            require(selectedAction.productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
        } else {
            require(disposition != PolicyDecisionDisposition.ACTION_SELECTED)
        }
    }
}

object BoundedProblemRuleCatalog {
    private val architected = RuleProvenance.GoverningArchitecture(
        artifactId = "ct-v2-03-principal-authority-scope",
        preciseLocator = "docs/work-orders/CT-V2-03-AUTHORITY-SCOPE.md, Scope gates and authority",
    )

    private fun fhs(sectionId: String, preciseLocator: String, relationship: SourceRuleRelationship) =
        RuleProvenance.ClinicalSource(
            source = source(
                document = "who-unicef-foundational-helping",
                version = "who-fhs-2025",
                section = sectionId,
                locator = "artifact-who-fhs",
            ),
            relationship = relationship,
            preciseLocator = preciseLocator,
            population = "Adults receiving psychologically supportive help",
            setting = "Human-helper foundational helping-skills training adaptable to varied helping contexts",
            intendedDeliverer = "Human helpers trained and supervised under the source's competency model",
            limitations = listOf(
                "The source trains human helpers and does not authorize autonomous software delivery.",
                "The governed artifact is CC BY-NC-SA 3.0 IGO; commercial implementation requires rights review or permission.",
                "Clinical and software-autonomy review remain pending.",
            ),
        )

    private fun pm(sectionId: String, preciseLocator: String, relationship: SourceRuleRelationship) =
        RuleProvenance.ClinicalSource(
            source = source(
                document = "who-pm-plus-individual",
                version = "who-pm-plus-v1-1-2018",
                section = sectionId,
                locator = "artifact-who-pm",
            ),
            relationship = relationship,
            preciseLocator = preciseLocator,
            population = "Adults impaired by distress in communities exposed to adversity",
            setting = "Individual PM+ under a manualized program",
            intendedDeliverer = "Trained, supervised human helpers operating within the PM+ delivery model",
            limitations = listOf(
                "This qualification slice is not PM+ delivery and imports no script, worksheet, or assessment.",
                "Population fit, helper competence, training, supervision, and implementation scope remain unadjudicated for Thomas.",
                "The governed artifact is CC BY-NC-SA 3.0 IGO; commercial implementation requires rights review or permission.",
                "Clinical, implementation-scope, and software-autonomy review remain pending.",
            ),
        )

    private val fhsRestrictions = setOf(
        ReviewRestriction.CLINICAL_REVIEW_PENDING,
        ReviewRestriction.RIGHTS_REVIEW_PENDING,
        ReviewRestriction.SOFTWARE_AUTONOMY_REVIEW_PENDING,
        ReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED,
    )

    private val pmRestrictions = fhsRestrictions + setOf(
        ReviewRestriction.IMPLEMENTATION_SCOPE_REVIEW_PENDING,
        ReviewRestriction.HUMAN_TRAINING_AND_SUPERVISION_ASSUMPTIONS_UNRESOLVED,
    )

    private val architectureRestrictions = setOf(ReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED)

    private val ordinarySlice = listOf<PolicyCondition>(
        ModeIs(ThomasMode.THERAPIST),
        SafetyDispositionIs(setOf(UpstreamSafetyDisposition.ORDINARY_SLICE_ALLOWED)),
        ScopeIs(setOf(BoundedProblemScope.BOUNDED_NON_EMERGENCY_PERSONAL_PROBLEM)),
    )

    private val ordinaryExclusions = listOf<PolicyCondition>(
        ConflictingEvidencePresent,
        WillingnessIs(setOf(ParticipationWillingness.UNWILLING_TO_CONTINUE)),
    )

    val rules: List<ProceduralRule> = listOf(
        architectureRule(
            "ctv203-r001-specialized-safety-handoff",
            "Stop this ordinary slice when upstream safety authority requires specialized policy.",
            1200,
            listOf(SafetyDispositionIs(setOf(UpstreamSafetyDisposition.SPECIALIZED_POLICY_REQUIRED))),
            RuleResult.Terminate(
                PolicyDecisionDisposition.SPECIALIZED_POLICY_REQUIRED,
                handoffRequirement = "SPECIALIZED_SAFETY_POLICY_REQUIRED",
            ),
        ),
        architectureRule(
            "ctv203-r002-unknown-safety-stop",
            "Unknown upstream safety disposition cannot be treated as ordinary-safe.",
            1190,
            listOf(SafetyDispositionIs(setOf(UpstreamSafetyDisposition.UNKNOWN))),
            RuleResult.Terminate(
                PolicyDecisionDisposition.INSUFFICIENT_INFORMATION,
                handoffRequirement = "UPSTREAM_SAFETY_AUTHORITY_REQUIRED",
            ),
        ),
        architectureRule(
            "ctv203-r003-therapist-mode-only",
            "This vertical slice has no authority in Journal or Biographer mode.",
            1180,
            listOf(ModeIsNot(ThomasMode.THERAPIST)),
            RuleResult.Terminate(PolicyDecisionDisposition.OUT_OF_SCOPE, "ACTIVE_MODE_POLICY_REQUIRED"),
        ),
        architectureRule(
            "ctv203-r004-specialized-scope-handoff",
            "A specialized case leaves the bounded ordinary-problem slice.",
            1170,
            listOf(ScopeIs(setOf(BoundedProblemScope.SPECIALIZED_POLICY_REQUIRED))),
            RuleResult.Terminate(PolicyDecisionDisposition.SPECIALIZED_POLICY_REQUIRED, "SPECIALIZED_POLICY_REQUIRED"),
        ),
        architectureRule(
            "ctv203-r005-unknown-scope-stop",
            "The slice requires an explicit bounded ordinary-problem scope supplied upstream.",
            1160,
            listOf(ScopeIs(setOf(BoundedProblemScope.UNKNOWN))),
            RuleResult.Terminate(PolicyDecisionDisposition.INSUFFICIENT_INFORMATION, "UPSTREAM_SCOPE_AUTHORITY_REQUIRED"),
        ),
        architectureRule(
            "ctv203-r006-out-of-scope-stop",
            "An explicitly out-of-scope case cannot continue through this slice.",
            1150,
            listOf(ScopeIs(setOf(BoundedProblemScope.OUT_OF_SCOPE))),
            RuleResult.Terminate(PolicyDecisionDisposition.OUT_OF_SCOPE, "OUTSIDE_SLICE_POLICY_REQUIRED"),
        ),
        architectureRule(
            "ctv203-r007-conflicting-evidence-stop",
            "Materially conflicting structured evidence requires visible adjudication.",
            1140,
            listOf(ConflictingEvidencePresent),
            RuleResult.Terminate(PolicyDecisionDisposition.POLICY_CONFLICT, "EVIDENCE_ADJUDICATION_REQUIRED"),
        ),
        sourceRule(
            "ctv203-r008-respect-unwillingness",
            "Respect a user's decision not to continue and select silence.",
            RuleKind.SOURCE_DERIVED_ACTION,
            1100,
            ordinarySlice + WillingnessIs(setOf(ParticipationWillingness.UNWILLING_TO_CONTINUE)),
            emptyList(),
            RuleResult.SelectAction(BoundedProblemActions.pauseWithoutResponse.id),
            listOf(pm("section-pm-helper-reluctance", "Chapter 3, publication pp. 26-27; PDF pp. 28-29", SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR)),
            pmRestrictions,
        ),
        sourceRule(
            "ctv203-r009-establish-support-intent",
            "Ask which kind of support the user wants before choosing a path.",
            RuleKind.SOURCE_DERIVED_ACTION,
            1000,
            ordinarySlice + SupportIntentMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.askSupportPreference.id),
            listOf(fhs("section-fhs-collaborative-goals", "Module 8, Collaborative goal-setting, publication pp. 118-120; PDF pp. 129-131", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            fhsRestrictions,
        ),
        sourceRule(
            "ctv203-r010-understand-present-problem",
            "Invite one present problem description when the focus is missing or vague.",
            RuleKind.SOURCE_DERIVED_ACTION,
            900,
            ordinarySlice + SupportIntentIs(SupportIntent.entries.toSet()) + ProblemDescriptionMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.askProblemDescription.id),
            listOf(fhs("section-fhs-verbal-communication", "Module 1, Verbal communication, publication pp. 27-31; PDF pp. 38-42", SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR)),
            fhsRestrictions,
        ),
        sourceRule(
            "ctv203-r011-verify-problem-understanding",
            "Verify tentative understanding before understanding-oriented or practical work.",
            RuleKind.SOURCE_DERIVED_ACTION,
            800,
            ordinarySlice + SupportIntentIs(setOf(SupportIntent.UNDERSTANDING, SupportIntent.PRACTICAL_HELP)) +
                BoundedProblemAvailable + SharedUnderstandingMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.verifyProblemUnderstanding.id),
            listOf(fhs("section-fhs-eliciting-feedback", "Module 8, Eliciting feedback, publication pp. 126-127; PDF pp. 137-138", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            fhsRestrictions,
        ),
        sourceRule(
            "ctv203-r012-listening-reflection",
            "When listening was requested and the problem is bounded, reflect without problem solving.",
            RuleKind.SOURCE_DERIVED_ACTION,
            790,
            ordinarySlice + SupportIntentIs(setOf(SupportIntent.LISTENING)) + BoundedProblemAvailable,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.reflectForListening.id),
            listOf(fhs("section-fhs-verbal-communication", "Module 1, Verbal communication, publication pp. 27-31; PDF pp. 38-42", SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR)),
            fhsRestrictions,
        ),
        sourceRule(
            "ctv203-r013-understanding-summary",
            "When shared understanding is confirmed, summarize without adding an interpretation.",
            RuleKind.SOURCE_DERIVED_ACTION,
            780,
            ordinarySlice + SupportIntentIs(setOf(SupportIntent.UNDERSTANDING)) + BoundedProblemAvailable +
                SharedUnderstandingIs(setOf(SharedUnderstanding.CONFIRMED)),
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.summarizeForUnderstanding.id),
            listOf(fhs("section-fhs-verbal-communication", "Module 1, Verbal communication, publication pp. 27-31; PDF pp. 38-42", SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR)),
            fhsRestrictions,
        ),
        sourceRule(
            "ctv203-r014-clarify-influenceable-part",
            "Before problem-oriented work, clarify which part of the bounded problem can be influenced.",
            RuleKind.SOURCE_DERIVED_ACTION,
            700,
            ordinarySlice + practicalConfirmed() + ProblemInfluenceMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.askInfluenceablePart.id),
            listOf(pm("section-pm-problem-definition", "Chapter 7, Managing Problems steps 1-3, publication pp. 46-49; PDF pp. 48-51", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            pmRestrictions,
        ),
        sourceRule(
            "ctv203-r015-non-influenceable-boundary",
            "A problem represented as not influenceable is not eligible for this manageable-problem slice.",
            RuleKind.SOURCE_DERIVED_SCOPE_BOUNDARY,
            690,
            ordinarySlice + practicalConfirmed() + ProblemInfluenceIs(setOf(ProblemInfluence.NOT_INFLUENCEABLE)),
            ordinaryExclusions,
            RuleResult.Terminate(PolicyDecisionDisposition.OUT_OF_SCOPE, "NON_INFLUENCEABLE_PROBLEM_POLICY_REQUIRED"),
            listOf(pm("section-pm-problem-definition", "Chapter 7, Managing Problems steps 1-3, publication pp. 46-49; PDF pp. 48-51", SourceRuleRelationship.BOUNDS_PROCEDURAL_SCOPE)),
            pmRestrictions,
        ),
        sourceRule(
            "ctv203-r016-establish-readiness-for-options",
            "Seek permission before moving from exploration into generating options.",
            RuleKind.SOURCE_DERIVED_ACTION,
            680,
            ordinarySlice + practicalConfirmed() + influenceable() + listOf(WillingnessMissing),
            listOf(ConflictingEvidencePresent),
            RuleResult.SelectAction(BoundedProblemActions.askReadinessForOptions.id),
            listOf(fhs("section-fhs-collaborative-goals", "Module 8, Collaborative goal-setting, publication pp. 118-120; PDF pp. 129-131", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            fhsRestrictions,
        ),
        sourceRule(
            "ctv203-r017-invite-user-options",
            "Invite user-generated options for the influenceable problem without supplying advice.",
            RuleKind.SOURCE_DERIVED_ACTION,
            600,
            ordinarySlice + practicalConfirmed() + influenceable() +
                WillingnessIs(setOf(ParticipationWillingness.WILLING_TO_ACT)) + UserOptionsMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.inviteUserOptions.id),
            listOf(
                pm("section-pm-option-generation", "Chapter 7, Managing Problems steps 4-5, publication pp. 49-51; PDF pp. 51-53", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP),
                pm("section-pm-advice-boundary", "Chapter 3, Giving advice, publication p. 24; PDF p. 26", SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR),
            ),
            pmRestrictions,
        ),
        sourceRule(
            "ctv203-r018-select-user-option",
            "Ask the user to choose among their own known options.",
            RuleKind.SOURCE_DERIVED_ACTION,
            500,
            ordinarySlice + practicalConfirmed() + influenceable() + UserOptionsAvailable + SelectedOptionMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.askUserToChooseOption.id),
            listOf(pm("section-pm-option-generation", "Chapter 7, Managing Problems steps 4-5, publication pp. 49-51; PDF pp. 51-53", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            pmRestrictions,
        ),
        sourceRule(
            "ctv203-r019-develop-bounded-plan",
            "After the user selects an option, ask for one small first plan step.",
            RuleKind.SOURCE_DERIVED_ACTION,
            400,
            ordinarySlice + practicalConfirmed() + influenceable() + UserOptionsAvailable +
                SelectedOptionAvailable + ActionPlanMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.developBoundedPlan.id),
            listOf(pm("section-pm-action-plan", "Chapter 7, Managing Problems step 6, publication pp. 50-51; PDF pp. 52-53", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            pmRestrictions,
        ),
        sourceRule(
            "ctv203-r020-wait-for-reported-outcome",
            "Once a plan is represented, wait for an outcome rather than adding another intervention.",
            RuleKind.SOURCE_DERIVED_ACTION,
            300,
            ordinarySlice + practicalConfirmed() + influenceable() + SelectedOptionAvailable +
                ActionPlanAvailable + PlanOutcomeMissing,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.waitForOutcome.id),
            listOf(pm("section-pm-outcome-review", "Chapter 7, Managing Problems step 7, publication p. 51; PDF p. 53", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            pmRestrictions,
        ),
        sourceRule(
            "ctv203-r021-review-reported-outcome",
            "When the user reports an outcome, select bounded review and reassessment.",
            RuleKind.SOURCE_DERIVED_ACTION,
            200,
            ordinarySlice + practicalConfirmed() + influenceable() + SelectedOptionAvailable +
                ActionPlanAvailable + PlanOutcomeAvailable,
            ordinaryExclusions,
            RuleResult.SelectAction(BoundedProblemActions.reviewReportedOutcome.id),
            listOf(pm("section-pm-outcome-review", "Chapter 7, Managing Problems step 7, publication p. 51; PDF p. 53", SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP)),
            pmRestrictions,
        ),
    ).sortedBy { it.id }

    init {
        require(rules.map { it.id }.distinct().size == rules.size)
        require(rules.all { it.productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED })
        require(rules.all { it.executionAuthority == RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION })
    }

    private fun practicalConfirmed(): List<PolicyCondition> = listOf(
        SupportIntentIs(setOf(SupportIntent.PRACTICAL_HELP)),
        BoundedProblemAvailable,
        SharedUnderstandingIs(setOf(SharedUnderstanding.CONFIRMED)),
    )

    private fun influenceable(): List<PolicyCondition> =
        listOf(ProblemInfluenceIs(setOf(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE)))

    private fun architectureRule(
        id: String,
        description: String,
        priority: Int,
        conditions: List<PolicyCondition>,
        result: RuleResult,
    ) = ProceduralRule(
        id = PolicyRuleId.parse(id),
        description = description,
        kind = RuleKind.ARCHITECTURAL_SCOPE_GUARD,
        priority = priority,
        preconditions = conditions,
        exclusions = emptyList(),
        result = result,
        provenance = listOf(architected),
        executionAuthority = RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION,
        unresolvedReviewRestrictions = architectureRestrictions,
    )

    private fun sourceRule(
        id: String,
        description: String,
        kind: RuleKind,
        priority: Int,
        preconditions: List<PolicyCondition>,
        exclusions: List<PolicyCondition>,
        result: RuleResult,
        provenance: List<RuleProvenance>,
        restrictions: Set<ReviewRestriction>,
    ) = ProceduralRule(
        id = PolicyRuleId.parse(id),
        description = description,
        kind = kind,
        priority = priority,
        preconditions = preconditions,
        exclusions = exclusions,
        result = result,
        provenance = provenance,
        executionAuthority = RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION,
        unresolvedReviewRestrictions = restrictions,
    )

    private fun source(document: String, version: String, section: String, locator: String) = GovernedSourceReference(
        documentId = SourceDocumentId.parse(document),
        versionId = SourceVersionId.parse(version),
        sectionId = SourceSectionId.parse(section),
        locatorId = SourceLocatorId.parse(locator),
    )
}

class BoundedProblemPolicyEvaluator(
    rules: List<ProceduralRule> = BoundedProblemRuleCatalog.rules,
    actions: List<PolicyActionDefinition> = BoundedProblemActions.all,
) {
    private val orderedRules = rules.sortedWith(compareByDescending<ProceduralRule> { it.priority }.thenBy { it.id })
    private val actionsById = actions.associateBy { it.id }

    init {
        require(orderedRules.map { it.id }.distinct().size == orderedRules.size)
        require(actionsById.size == actions.size)
    }

    fun evaluate(state: BoundedProblemPolicyState): PolicyDecision {
        val invalid = state.validationErrors()
        if (invalid.isNotEmpty()) return terminalWithoutRules(
            state,
            PolicyDecisionDisposition.INVALID_INPUT,
            invalid,
            "STRUCTURED_INPUT_CORRECTION_REQUIRED",
        )

        val traces = orderedRules.map { it.evaluate(state) }
        val traceById = traces.associateBy { it.ruleId }
        val matched = orderedRules.filter { requireNotNull(traceById[it.id]).matched }
        val eligibleActions = matched.mapNotNull { (it.result as? RuleResult.SelectAction)?.actionId }.distinct().sorted()
        val rejected = orderedRules.filterNot { it in matched }.map { rule ->
            RejectedCandidateAction(
                ruleId = rule.id,
                actionId = (rule.result as? RuleResult.SelectAction)?.actionId,
                reasons = requireNotNull(traceById[rule.id]).rejectionReasons,
            )
        }

        if (matched.isEmpty()) return decision(
            state = state,
            disposition = PolicyDecisionDisposition.NO_AUTHORIZED_ACTION,
            traces = traces,
            eligibleActions = emptyList(),
            rejected = rejected,
            unresolved = listOf("POLICY_GRAPH_HAS_NO_MATCH"),
            handoff = "POLICY_DEFINITION_REQUIRED",
            tieBreak = TieBreakTrace(TieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER, null, emptyList(), "NO_MATCH"),
        )

        val highestPriority = matched.maxOf { it.priority }
        val contenders = matched.filter { it.priority == highestPriority }.sortedBy { it.id }
        if (contenders.size != 1) return decision(
            state = state,
            disposition = PolicyDecisionDisposition.POLICY_CONFLICT,
            traces = traces,
            eligibleActions = eligibleActions,
            rejected = rejected,
            unresolved = listOf("EQUAL_PRIORITY_RULE_CONFLICT"),
            handoff = "POLICY_ADJUDICATION_REQUIRED",
            tieBreak = TieBreakTrace(
                TieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER,
                highestPriority,
                contenders.map { it.id },
                "CONFLICT_NOT_GUESSED",
            ),
        )

        val winner = contenders.single()
        val tieBreak = TieBreakTrace(
            TieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER,
            highestPriority,
            listOf(winner.id),
            "UNIQUE_HIGHEST_PRIORITY_RULE",
        )
        if (winner.executionAuthority != RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION) return decision(
            state = state,
            disposition = PolicyDecisionDisposition.REVIEW_BLOCKED,
            traces = traces,
            eligibleActions = eligibleActions,
            rejected = rejected,
            provenance = winner.provenance,
            unresolved = winner.unresolvedReviewRestrictions.map { it.name }.sorted() + "RULE_NOT_QUALIFICATION_EXECUTABLE",
            handoff = "RULE_REVIEW_REQUIRED",
            tieBreak = tieBreak,
        )

        return when (val result = winner.result) {
            is RuleResult.Terminate -> decision(
                state = state,
                disposition = result.disposition,
                traces = traces,
                eligibleActions = eligibleActions,
                rejected = rejected,
                provenance = winner.provenance,
                unresolved = winner.unresolvedReviewRestrictions.map { it.name }.sorted() +
                    listOfNotNull(result.unresolvedRequirement?.name),
                handoff = result.handoffRequirement,
                tieBreak = tieBreak,
            )
            is RuleResult.SelectAction -> {
                val action = actionsById[result.actionId] ?: return decision(
                    state = state,
                    disposition = PolicyDecisionDisposition.NO_AUTHORIZED_ACTION,
                    traces = traces,
                    eligibleActions = eligibleActions,
                    rejected = rejected,
                    provenance = winner.provenance,
                    unresolved = listOf("SELECTED_ACTION_DEFINITION_MISSING:${result.actionId.value}"),
                    handoff = "POLICY_DEFINITION_REQUIRED",
                    tieBreak = tieBreak,
                )
                val selected = SelectedQualificationAction(
                    definition = action,
                    selectedByRuleId = winner.id,
                    executionAuthority = winner.executionAuthority,
                    unresolvedReviewRestrictions = winner.unresolvedReviewRestrictions,
                )
                decision(
                    state = state,
                    disposition = PolicyDecisionDisposition.ACTION_SELECTED,
                    traces = traces,
                    eligibleActions = eligibleActions,
                    rejected = rejected,
                    selected = selected,
                    activeGoal = action.goalId,
                    provenance = winner.provenance,
                    unresolved = winner.unresolvedReviewRestrictions.map { it.name }.sorted(),
                    expectation = action.outcomeContract,
                    tieBreak = tieBreak,
                )
            }
        }
    }

    private fun terminalWithoutRules(
        state: BoundedProblemPolicyState,
        disposition: PolicyDecisionDisposition,
        unresolved: List<String>,
        handoff: String,
    ) = decision(
        state = state,
        disposition = disposition,
        traces = emptyList(),
        eligibleActions = emptyList(),
        rejected = emptyList(),
        unresolved = unresolved,
        handoff = handoff,
        tieBreak = TieBreakTrace(TieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER, null, emptyList(), "INPUT_REJECTED"),
    )

    private fun decision(
        state: BoundedProblemPolicyState,
        disposition: PolicyDecisionDisposition,
        traces: List<RuleEvaluationTrace>,
        eligibleActions: List<PolicyActionId>,
        rejected: List<RejectedCandidateAction>,
        selected: SelectedQualificationAction? = null,
        activeGoal: OntologyConceptId? = null,
        provenance: List<RuleProvenance> = emptyList(),
        unresolved: List<String> = emptyList(),
        expectation: OutcomeContract? = null,
        handoff: String? = null,
        tieBreak: TieBreakTrace,
    ) = PolicyDecision(
        policyVersion = CT_V2_03_POLICY_VERSION,
        decisionReference = "$CT_V2_03_POLICY_VERSION:${state.stateId}",
        stateId = state.stateId,
        currentProceduralStage = inferStage(state, disposition, selected),
        disposition = disposition,
        activeGoalId = activeGoal,
        eligibleCandidateActions = eligibleActions,
        rejectedCandidateActions = rejected,
        selectedAction = selected,
        selectedRuleProvenance = provenance,
        unresolvedRequirements = unresolved.distinct().sorted(),
        nextStateExpectations = expectation,
        handoffRequirement = handoff,
        ruleTrace = traces,
        tieBreakTrace = tieBreak,
    )

    private fun inferStage(
        state: BoundedProblemPolicyState,
        disposition: PolicyDecisionDisposition,
        selected: SelectedQualificationAction?,
    ): ProceduralStage {
        if (disposition != PolicyDecisionDisposition.ACTION_SELECTED) {
            return if (disposition == PolicyDecisionDisposition.INVALID_INPUT) ProceduralStage.SCOPE_GATE else ProceduralStage.TERMINATED
        }
        return when (selected?.definition?.id) {
            BoundedProblemActions.askSupportPreference.id -> ProceduralStage.ESTABLISH_SUPPORT_INTENT
            BoundedProblemActions.askProblemDescription.id -> ProceduralStage.UNDERSTAND_PRESENT_PROBLEM
            BoundedProblemActions.verifyProblemUnderstanding.id -> ProceduralStage.ESTABLISH_SHARED_UNDERSTANDING
            BoundedProblemActions.reflectForListening.id,
            BoundedProblemActions.summarizeForUnderstanding.id -> ProceduralStage.SUPPORT_EXPRESSION
            BoundedProblemActions.askInfluenceablePart.id -> ProceduralStage.DETERMINE_INFLUENCEABLE_PROBLEM
            BoundedProblemActions.askReadinessForOptions.id -> ProceduralStage.ESTABLISH_READINESS
            BoundedProblemActions.inviteUserOptions.id -> ProceduralStage.GENERATE_OPTIONS
            BoundedProblemActions.askUserToChooseOption.id -> ProceduralStage.SELECT_OPTION
            BoundedProblemActions.developBoundedPlan.id -> ProceduralStage.DEVELOP_PLAN
            BoundedProblemActions.waitForOutcome.id -> ProceduralStage.AWAIT_OUTCOME
            BoundedProblemActions.reviewReportedOutcome.id -> ProceduralStage.REVIEW_OUTCOME
            BoundedProblemActions.pauseWithoutResponse.id -> ProceduralStage.TERMINATED
            else -> if (state.awaitingInformation != null) ProceduralStage.AWAIT_OUTCOME else ProceduralStage.SCOPE_GATE
        }
    }
}

object PolicyDecisionExplainer {
    fun compact(decision: PolicyDecision): String {
        val goal = decision.activeGoalId?.value ?: "none"
        val rule = decision.selectedAction?.selectedByRuleId?.value
            ?: decision.tieBreakTrace.contenderRuleIds.joinToString(",") { it.value }.ifBlank { "none" }
        val action = decision.selectedAction?.definition?.id?.value ?: decision.disposition.name
        val expected = decision.nextStateExpectations?.expectedInformation?.name ?: decision.handoffRequirement ?: "none"
        return "${decision.currentProceduralStage} -> $goal -> $rule -> $action -> $expected"
    }

    fun withRejectedAlternatives(decision: PolicyDecision): String = buildString {
        appendLine(compact(decision))
        decision.rejectedCandidateActions.forEach { rejected ->
            append(rejected.ruleId.value)
            append(" rejected: ")
            appendLine(rejected.reasons.joinToString(","))
        }
    }.trimEnd()
}
