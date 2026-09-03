package com.conundrum.thomas.v2.engine.ordinary

import com.conundrum.thomas.v2.engine.verticalslice.PolicyRuleId
import com.conundrum.thomas.v2.engine.verticalslice.ProductionTherapeuticAuthority
import com.conundrum.thomas.v2.engine.verticalslice.ReviewRestriction
import com.conundrum.thomas.v2.engine.verticalslice.RuleExecutionAuthority
import com.conundrum.thomas.v2.engine.verticalslice.RuleKind
import com.conundrum.thomas.v2.engine.verticalslice.RuleProvenance
import com.conundrum.thomas.v2.engine.verticalslice.SourceRuleRelationship
import com.conundrum.thomas.v2.ontology.GovernanceReviewStatus
import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.SourceDocumentId
import com.conundrum.thomas.v2.provenance.SourceLocatorId
import com.conundrum.thomas.v2.provenance.SourceSectionId
import com.conundrum.thomas.v2.provenance.SourceVersionId

const val CT_V2_05_CORE_POLICY_VERSION = "ct-v2-05-core-ordinary-therapy-1.0.0"

data class CoreConditionEvaluation(
    val conditionId: String,
    val matched: Boolean,
    val actual: String,
    val expected: String,
)

class CoreCondition(
    val stableId: String,
    private val expected: String,
    private val actual: (CoreOrdinaryTherapyState) -> String,
    private val predicate: (CoreOrdinaryTherapyState) -> Boolean,
) {
    init {
        require(stableId.isNotBlank())
        require(expected.isNotBlank())
    }

    fun evaluate(state: CoreOrdinaryTherapyState) = CoreConditionEvaluation(
        conditionId = stableId,
        matched = predicate(state),
        actual = actual(state),
        expected = expected,
    )
}

sealed interface GovernedCoreRule {
    val id: PolicyRuleId
    val description: String
    val kind: RuleKind
    val priority: Int
    val provenance: List<RuleProvenance>
    val executionAuthority: RuleExecutionAuthority
    val productionAuthority: ProductionTherapeuticAuthority
    val unresolvedReviewRestrictions: Set<ReviewRestriction>
}

enum class CorePolicyDisposition {
    ACTION_SELECTED,
    INSUFFICIENT_INFORMATION,
    UNSUPPORTED_PATHWAY,
    OUT_OF_SCOPE,
    REVIEW_BLOCKED,
    POLICY_CONFLICT,
    NO_AUTHORIZED_ACTION,
    INVALID_INPUT,
}

sealed interface CoreRouteRuleResult {
    data class SelectRoute(val route: OrdinaryRoute) : CoreRouteRuleResult
    data class Terminate(val disposition: CorePolicyDisposition, val handoffRequirement: String) : CoreRouteRuleResult
}

data class CoreRouteRule(
    override val id: PolicyRuleId,
    override val description: String,
    override val kind: RuleKind,
    override val priority: Int,
    val preconditions: List<CoreCondition>,
    val result: CoreRouteRuleResult,
    override val provenance: List<RuleProvenance>,
    override val executionAuthority: RuleExecutionAuthority = RuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION,
    override val productionAuthority: ProductionTherapeuticAuthority = ProductionTherapeuticAuthority.NOT_GRANTED,
    override val unresolvedReviewRestrictions: Set<ReviewRestriction>,
) : GovernedCoreRule {
    init {
        require(description.isNotBlank())
        require(priority >= 0)
        require(preconditions.isNotEmpty())
        require(provenance.isNotEmpty())
        require(productionAuthority == ProductionTherapeuticAuthority.NOT_GRANTED)
        require(ReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED in unresolvedReviewRestrictions)
        if (kind != RuleKind.ARCHITECTURAL_SCOPE_GUARD) {
            require(provenance.any { it is RuleProvenance.ClinicalSource })
        }
    }

    fun evaluate(state: CoreOrdinaryTherapyState): CoreRuleEvaluationTrace {
        val checks = preconditions.map { it.evaluate(state) }
        return CoreRuleEvaluationTrace(
            ruleId = id,
            matched = checks.all { it.matched },
            preconditions = checks,
            rejectionReasons = checks.filterNot { it.matched }.map { "${it.conditionId}: expected ${it.expected}; actual ${it.actual}" },
        )
    }
}

data class CoreRuleEvaluationTrace(
    val ruleId: PolicyRuleId,
    val matched: Boolean,
    val preconditions: List<CoreConditionEvaluation>,
    val rejectionReasons: List<String>,
)

data class RejectedCoreRule(val ruleId: PolicyRuleId, val reasons: List<String>)

data class RouteTransition(
    val from: OrdinaryRoute,
    val to: OrdinaryRoute,
    val requiresChangedStructuredEvidence: Boolean = true,
)

object CoreRouteTransitionCatalog {
    private val conversational = listOf(
        OrdinaryRoute.LISTEN_SUPPORT,
        OrdinaryRoute.UNDERSTAND_CLARIFY,
        OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING,
    )

    val transitions: List<RouteTransition> = buildList {
        conversational.forEach { from ->
            conversational.filterNot { it == from }.forEach { to -> add(RouteTransition(from, to)) }
            add(RouteTransition(from, OrdinaryRoute.CONSOLIDATE_CLOSE))
            add(RouteTransition(from, OrdinaryRoute.CLARIFY_PREFERENCE))
        }
        conversational.forEach { add(RouteTransition(OrdinaryRoute.CLARIFY_PREFERENCE, it)) }
        add(RouteTransition(OrdinaryRoute.CLARIFY_PREFERENCE, OrdinaryRoute.CONSOLIDATE_CLOSE))
        conversational.forEach { add(RouteTransition(OrdinaryRoute.CONSOLIDATE_CLOSE, it)) }
    }.sortedWith(compareBy<RouteTransition> { it.from.name }.thenBy { it.to.name })

    fun supports(from: OrdinaryRoute, to: OrdinaryRoute): Boolean =
        from == to || transitions.any { it.from == from && it.to == to }
}

object CoreOrdinaryProvenance {
    val architecture = RuleProvenance.GoverningArchitecture(
        artifactId = "ct-v2-05-principal-authority-scope",
        preciseLocator = "docs/work-orders/CT-V2-05-AUTHORITY-SCOPE.md, qualification-only repertoire authority",
    )

    val architectureRestrictions = setOf(ReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED)
    val fhsRestrictions = setOf(
        ReviewRestriction.CLINICAL_REVIEW_PENDING,
        ReviewRestriction.RIGHTS_REVIEW_PENDING,
        ReviewRestriction.IMPLEMENTATION_SCOPE_REVIEW_PENDING,
        ReviewRestriction.SOFTWARE_AUTONOMY_REVIEW_PENDING,
        ReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED,
    )
    val pmRestrictions = fhsRestrictions + ReviewRestriction.HUMAN_TRAINING_AND_SUPERVISION_ASSUMPTIONS_UNRESOLVED

    fun fhsVerbal() = clinical(
        document = "who-unicef-foundational-helping",
        version = "who-fhs-2025",
        section = "section-fhs-verbal-communication",
        locator = "artifact-who-fhs",
        precise = "Module 1, Verbal communication, publication pp. 27-31; PDF pp. 38-42",
        relationship = SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR,
        limitations = listOf(
            "The source addresses trained human helpers; software-autonomy review remains pending.",
            "Only abstract dialogue-act semantics are represented; no training script or example is copied.",
        ),
    )

    fun fhsGoals() = clinical(
        document = "who-unicef-foundational-helping",
        version = "who-fhs-2025",
        section = "section-fhs-collaborative-goals",
        locator = "artifact-who-fhs",
        precise = "Module 8, Collaborative goal-setting, publication pp. 118-120; PDF pp. 129-131",
        relationship = SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP,
        limitations = listOf(
            "Route preference is structured upstream; this rule performs no natural-language inference.",
            "The source does not grant production or autonomous-software authority.",
        ),
    )

    fun fhsFeedback() = clinical(
        document = "who-unicef-foundational-helping",
        version = "who-fhs-2025",
        section = "section-fhs-eliciting-feedback",
        locator = "artifact-who-fhs",
        precise = "Module 8, Eliciting feedback, publication pp. 126-127; PDF pp. 137-138",
        relationship = SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP,
        limitations = listOf(
            "A user correction withdraws a tentative Thomas interpretation; no profile fact is written.",
            "No source wording is reproduced and software-autonomy review remains pending.",
        ),
    )

    fun pmReluctance() = pm(
        "section-pm-helper-reluctance",
        "Chapter 3, publication pp. 26-27; PDF pp. 28-29",
        SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR,
    )

    fun pmAdvice() = pm(
        "section-pm-advice-boundary",
        "Chapter 3, Giving advice, publication p. 24; PDF p. 26",
        SourceRuleRelationship.IDENTIFIES_HELPFUL_OR_UNHELPFUL_BEHAVIOR,
    )

    fun pmProblemDefinition() = pm(
        "section-pm-problem-definition",
        "Chapter 7, Managing Problems steps 1-3, publication pp. 46-49; PDF pp. 48-51",
        SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP,
    )

    fun pmOptions() = pm(
        "section-pm-option-generation",
        "Chapter 7, Managing Problems steps 4-5, publication pp. 49-51; PDF pp. 51-53",
        SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP,
    )

    fun pmPlan() = pm(
        "section-pm-action-plan",
        "Chapter 7, Managing Problems step 6, publication pp. 50-51; PDF pp. 52-53",
        SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP,
    )

    fun pmOutcome() = pm(
        "section-pm-outcome-review",
        "Chapter 7, Managing Problems step 7, publication p. 51; PDF p. 53",
        SourceRuleRelationship.SUPPORTS_PROCEDURAL_STEP,
    )

    private fun pm(section: String, precise: String, relationship: SourceRuleRelationship) = clinical(
        document = "who-pm-plus-individual",
        version = "who-pm-plus-v1-1-2018",
        section = section,
        locator = "artifact-who-pm",
        precise = precise,
        relationship = relationship,
        limitations = listOf(
            "PM+ assumes trained and supervised human helpers in adversity-affected communities.",
            "Only a bounded abstract qualification step is represented; no script, worksheet, or exception is imported.",
        ),
    )

    private fun clinical(
        document: String,
        version: String,
        section: String,
        locator: String,
        precise: String,
        relationship: SourceRuleRelationship,
        limitations: List<String>,
    ) = RuleProvenance.ClinicalSource(
        source = GovernedSourceReference(
            SourceDocumentId.parse(document),
            SourceVersionId.parse(version),
            SourceSectionId.parse(section),
            SourceLocatorId.parse(locator),
        ),
        relationship = relationship,
        preciseLocator = precise,
        population = "Adults in the governed source scope",
        setting = "Qualification abstraction only; source setting and exclusions remain controlling metadata",
        intendedDeliverer = "Trained human helper under the source assumptions; Thomas is not admitted as equivalent",
        limitations = limitations,
        clinicalReviewStatus = GovernanceReviewStatus.PENDING,
        rightsReviewStatus = GovernanceReviewStatus.PENDING,
    )
}

private fun condition(
    id: String,
    expected: String,
    actual: (CoreOrdinaryTherapyState) -> String,
    predicate: (CoreOrdinaryTherapyState) -> Boolean,
) = CoreCondition(id, expected, actual, predicate)

object CoreRouteRuleCatalog {
    private fun architectureRule(
        id: String,
        description: String,
        priority: Int,
        condition: CoreCondition,
        result: CoreRouteRuleResult,
    ) = CoreRouteRule(
        PolicyRuleId.parse(id), description, RuleKind.ARCHITECTURAL_SCOPE_GUARD, priority,
        listOf(condition), result, listOf(CoreOrdinaryProvenance.architecture),
        unresolvedReviewRestrictions = CoreOrdinaryProvenance.architectureRestrictions,
    )

    private fun sourceRule(
        id: String,
        description: String,
        priority: Int,
        condition: CoreCondition,
        result: CoreRouteRuleResult,
        provenance: RuleProvenance.ClinicalSource,
        restrictions: Set<ReviewRestriction>,
    ) = CoreRouteRule(
        PolicyRuleId.parse(id), description, RuleKind.SOURCE_DERIVED_ACTION, priority,
        listOf(condition), result, listOf(provenance),
        unresolvedReviewRestrictions = restrictions,
    )

    val rules: List<CoreRouteRule> = listOf(
        architectureRule(
            "ctv205-r001-close-route",
            "An explicit close request selects the bounded close route.",
            2200,
            condition("engagement-close", "CLOSE_REQUESTED", { it.engagement.value?.name ?: it.engagement.resolution.name }) {
                it.engagement.isEstablished() && it.engagement.value == OrdinaryEngagement.CLOSE_REQUESTED
            },
            CoreRouteRuleResult.SelectRoute(OrdinaryRoute.CONSOLIDATE_CLOSE),
        ),
        sourceRule(
            "ctv205-r002-pause-or-refusal-route",
            "A pause, topic refusal, or unwillingness selects the non-pressuring close route.",
            2190,
            condition("engagement-pause-or-refusal", "PAUSE_REQUESTED or DOES_NOT_WANT_TOPIC", { it.engagement.value?.name ?: it.engagement.resolution.name }) {
                (it.engagement.isEstablished() && it.engagement.value in setOf(OrdinaryEngagement.PAUSE_REQUESTED, OrdinaryEngagement.DOES_NOT_WANT_TOPIC)) ||
                    (it.willingness.isEstablished() && it.willingness.value == com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness.UNWILLING_TO_CONTINUE)
            },
            CoreRouteRuleResult.SelectRoute(OrdinaryRoute.CONSOLIDATE_CLOSE),
            CoreOrdinaryProvenance.pmReluctance(),
            CoreOrdinaryProvenance.pmRestrictions,
        ),
        architectureRule(
            "ctv205-r003-engagement-required",
            "Ordinary conversation requires established voluntary engagement.",
            2100,
            condition("engagement-not-established", "established engagement", { it.engagement.resolution.name }) { !it.engagement.isEstablished() },
            CoreRouteRuleResult.Terminate(CorePolicyDisposition.INSUFFICIENT_INFORMATION, "VOLUNTARY_ENGAGEMENT_EVIDENCE_REQUIRED"),
        ),
        architectureRule(
            "ctv205-r004-decision-path-unopened",
            "Standalone decision support remains unopened because the governed corpus supports only bounded choice inside PM+ problem solving.",
            2050,
            condition("preference-decision-support", "DECISION_SUPPORT", { it.routePreference.value?.name ?: it.routePreference.resolution.name }) {
                it.routePreference.isEstablished() && it.routePreference.value == RequestedOrdinarySupport.DECISION_SUPPORT
            },
            CoreRouteRuleResult.Terminate(CorePolicyDisposition.UNSUPPORTED_PATHWAY, "DECISION_SUPPORT_SOURCE_REQUIRED"),
        ),
        sourceRule(
            "ctv205-r005-listen-route",
            "An established request to be heard selects listening rather than problem solving.",
            1900,
            condition("preference-listen", "LISTEN", { it.routePreference.value?.name ?: it.routePreference.resolution.name }) {
                it.routePreference.isEstablished() && it.routePreference.value == RequestedOrdinarySupport.LISTEN
            },
            CoreRouteRuleResult.SelectRoute(OrdinaryRoute.LISTEN_SUPPORT),
            CoreOrdinaryProvenance.fhsGoals(),
            CoreOrdinaryProvenance.fhsRestrictions,
        ),
        sourceRule(
            "ctv205-r006-understand-route",
            "An established request for understanding selects bounded clarification.",
            1890,
            condition("preference-understand", "UNDERSTAND", { it.routePreference.value?.name ?: it.routePreference.resolution.name }) {
                it.routePreference.isEstablished() && it.routePreference.value == RequestedOrdinarySupport.UNDERSTAND
            },
            CoreRouteRuleResult.SelectRoute(OrdinaryRoute.UNDERSTAND_CLARIFY),
            CoreOrdinaryProvenance.fhsGoals(),
            CoreOrdinaryProvenance.fhsRestrictions,
        ),
        sourceRule(
            "ctv205-r007-practical-route",
            "An established request for bounded practical help selects problem solving.",
            1880,
            condition("preference-practical", "PRACTICAL_HELP", { it.routePreference.value?.name ?: it.routePreference.resolution.name }) {
                it.routePreference.isEstablished() && it.routePreference.value == RequestedOrdinarySupport.PRACTICAL_HELP
            },
            CoreRouteRuleResult.SelectRoute(OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING),
            CoreOrdinaryProvenance.fhsGoals(),
            CoreOrdinaryProvenance.fhsRestrictions,
        ),
        sourceRule(
            "ctv205-r008-correction-understand-route",
            "An unhandled correction reopens understanding when no explicit route preference is established.",
            1800,
            condition("unhandled-correction-without-preference", "UNHANDLED correction and no established preference", {
                "${it.correctionStatus.value}:${it.routePreference.resolution}"
            }) {
                it.correctionStatus.isEstablished() && it.correctionStatus.value == CorrectionStatus.UNHANDLED && !it.routePreference.isEstablished()
            },
            CoreRouteRuleResult.SelectRoute(OrdinaryRoute.UNDERSTAND_CLARIFY),
            CoreOrdinaryProvenance.fhsFeedback(),
            CoreOrdinaryProvenance.fhsRestrictions,
        ),
        sourceRule(
            "ctv205-r009-clarify-preference-route",
            "When no support direction is established, select one bounded preference clarification.",
            1700,
            condition("preference-not-established", "support preference not established", { it.routePreference.resolution.name }) { !it.routePreference.isEstablished() },
            CoreRouteRuleResult.SelectRoute(OrdinaryRoute.CLARIFY_PREFERENCE),
            CoreOrdinaryProvenance.fhsGoals(),
            CoreOrdinaryProvenance.fhsRestrictions,
        ),
    ).sortedBy { it.id }

    init { require(rules.map { it.id }.distinct().size == rules.size) }
}
