package com.conundrum.thomas.v2.safety

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.ontology.GovernanceReviewStatus
import com.conundrum.thomas.v2.provenance.CommercialUseStatus
import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.SourceConflictId
import com.conundrum.thomas.v2.provenance.SourceDocumentId
import com.conundrum.thomas.v2.provenance.SourceLocatorId
import com.conundrum.thomas.v2.provenance.SourceSectionId
import com.conundrum.thomas.v2.provenance.SourceVersionId

const val CT_V2_04_SAFETY_SCOPE_POLICY_VERSION = "ct-v2-04-safety-scope-gate-1.0.0"

enum class SafetyAuthorityState {
    ORDINARY_POLICY_ALLOWED,
    CLARIFICATION_REQUIRED,
    SPECIALIZED_POLICY_REQUIRED,
    EXTERNAL_SUPPORT_REQUIRED,
    EMERGENCY_BOUNDARY_REACHED,
    INSUFFICIENT_INFORMATION,
    OUT_OF_SUPPORTED_POPULATION,
    OUT_OF_SCOPE,
    REVIEW_BLOCKED,
    POLICY_CONFLICT,
    NO_AUTHORIZED_ACTION,
    INVALID_INPUT,
}

enum class SafetyTieBreakPrinciple { HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER }

data class SafetyTieBreakTrace(
    val principle: SafetyTieBreakPrinciple,
    val highestPriority: Int?,
    val contenderRuleIds: List<SafetyRuleId>,
    val resolution: String,
)

data class SafetyObservationTrace(
    val field: SafetyField,
    val resolution: SafetyEvidenceResolution,
    val value: String?,
    val origin: SafetyEvidenceOrigin?,
    val evidenceReferences: Set<String>,
)

data class RejectedSafetyRule(
    val ruleId: SafetyRuleId,
    val reasons: List<String>,
)

/**
 * Compile-visible authority capability. The internal constructor prevents normal callers and the
 * ordinary policy module from manufacturing a permit. It is bound to one evidence revision.
 */
class OrdinaryTherapyPermit internal constructor(
    val gatePolicyVersion: String,
    val gateDecisionReference: String,
    val stateId: String,
    val evidenceRevision: SafetyEvidenceRevision,
) {
    fun authorizes(candidateStateId: String, candidateRevision: SafetyEvidenceRevision): Boolean =
        gatePolicyVersion == CT_V2_04_SAFETY_SCOPE_POLICY_VERSION &&
            stateId == candidateStateId && evidenceRevision == candidateRevision

    override fun equals(other: Any?): Boolean = other is OrdinaryTherapyPermit &&
        gatePolicyVersion == other.gatePolicyVersion &&
        gateDecisionReference == other.gateDecisionReference &&
        stateId == other.stateId && evidenceRevision == other.evidenceRevision

    override fun hashCode(): Int = listOf(gatePolicyVersion, gateDecisionReference, stateId, evidenceRevision).hashCode()

    override fun toString(): String = "OrdinaryTherapyPermit($gateDecisionReference)"
}

data class SafetyScopeDecision(
    val policyVersion: String,
    val decisionReference: String,
    val stateId: String,
    val evidenceRevision: SafetyEvidenceRevision,
    val authorityState: SafetyAuthorityState,
    val observations: List<SafetyObservationTrace>,
    val eligibleRuleIds: List<SafetyRuleId>,
    val rejectedRules: List<RejectedSafetyRule>,
    val selectedRuleId: SafetyRuleId?,
    val selectedRuleProvenance: List<SafetyRuleProvenance>,
    val missingFacts: List<SafetyField>,
    val unresolvedReviewRestrictions: List<SafetyReviewRestriction>,
    val selectedAction: SelectedSafetyQualificationAction?,
    val nextExpectedEvidence: SafetyInformationRequirement?,
    val handoffRequirement: String?,
    val prohibitedContinuationPaths: Set<String>,
    val ordinaryTherapyPermit: OrdinaryTherapyPermit?,
    val ruleTrace: List<SafetyRuleEvaluationTrace>,
    val tieBreakTrace: SafetyTieBreakTrace,
) : SafetyGovernedResult {
    init {
        require(policyVersion == CT_V2_04_SAFETY_SCOPE_POLICY_VERSION)
        if (authorityState == SafetyAuthorityState.ORDINARY_POLICY_ALLOWED) {
            require(ordinaryTherapyPermit != null)
            require(selectedAction == null)
            require("ORDINARY_THERAPIST_POLICY" !in prohibitedContinuationPaths)
        } else {
            require(ordinaryTherapyPermit == null)
            require("ORDINARY_THERAPIST_POLICY" in prohibitedContinuationPaths)
        }
        if (selectedAction != null) {
            require(authorityState == SafetyAuthorityState.CLARIFICATION_REQUIRED)
            require(nextExpectedEvidence == selectedAction.informationRequirement)
        }
    }
}

object SafetyScopeRuleCatalog {
    private val architected = SafetyRuleProvenance.GoverningArchitecture(
        artifactId = "ct-v2-04-principal-authority-scope",
        preciseLocator = "docs/work-orders/CT-V2-04-AUTHORITY-SCOPE.md, safety gate and qualification authority",
    )

    private val niceSelfHarmBoundary = SafetyRuleProvenance.ClinicalSource(
        source = GovernedSourceReference(
            SourceDocumentId.parse("nice-ng225"),
            SourceVersionId.parse("nice-ng225-2025"),
            SourceSectionId.parse("section-ng225-assessment-focus"),
            SourceLocatorId.parse("loc-nice-ng225-recommendations"),
        ),
        immutableArtifactLocatorId = SourceLocatorId.parse("artifact-nice-ng225"),
        relationship = SafetySourceRelationship.PRESERVES_SPECIALIST_ASSESSMENT_BOUNDARY,
        preciseLocator = "NG225 recommendations 1.6.5-1.6.6; current PDF page 16 of 77",
        population = "Children, young people, and adults who have self-harmed",
        setting = "Healthcare, social care, education, third-sector, and criminal-justice services within NICE scope",
        intendedDeliverer = "Healthcare and social-care practitioners; risk formulation is assigned to mental-health professionals",
        purpose = "Needs- and safety-focused assessment while preserving specialist professional roles",
        limitations = listOf(
            "The recommendation does not authorize Thomas to perform psychosocial assessment or risk formulation.",
            "A direct disclosure is not converted into a prediction, score, band, diagnosis, or screening result.",
            "International and AI use requires prior NICE approval and agreement under the governed rights record.",
            "The NIMH medical-screening pathway remains a separate scope and is not merged into this rule.",
        ),
        commercialUseStatus = CommercialUseStatus.LEGAL_REVIEW_REQUIRED,
        clinicalReviewStatus = GovernanceReviewStatus.PENDING,
        rightsReviewStatus = GovernanceReviewStatus.PENDING,
        legalReviewStatus = GovernanceReviewStatus.PENDING,
        softwareAutonomyReviewStatus = GovernanceReviewStatus.PENDING,
        implementationScopeReviewStatus = GovernanceReviewStatus.PENDING,
        conflictState = SafetySourceConflictState.DIFFERENT_SCOPE_RECORDED,
        conflictIds = setOf(SourceConflictId.parse("scope-asq-vs-ng225")),
    )

    private val engineeringRestrictions = setOf(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED)
    private val niceRestrictions = setOf(
        SafetyReviewRestriction.CLINICAL_REVIEW_PENDING,
        SafetyReviewRestriction.RIGHTS_REVIEW_PENDING,
        SafetyReviewRestriction.LEGAL_REVIEW_PENDING,
        SafetyReviewRestriction.IMPLEMENTATION_SCOPE_REVIEW_PENDING,
        SafetyReviewRestriction.SOFTWARE_AUTONOMY_REVIEW_PENDING,
        SafetyReviewRestriction.TRAINING_DELIVERER_RESTRICTION_UNRESOLVED,
        SafetyReviewRestriction.SUPERVISION_RESTRICTION_UNRESOLVED,
        SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED,
    )

    private val allFields = SafetyField.entries.toSet()

    private fun field(field: SafetyField, vararg values: Enum<*>) =
        FieldEstablishedValueIn(field, values.map { it.name }.toSet())

    private fun engineeringRule(
        id: String,
        description: String,
        priority: Int,
        prerequisites: List<SafetyCondition>,
        result: SafetyRuleResult,
    ) = SafetyScopeRule(
        id = SafetyRuleId.parse(id),
        description = description,
        kind = SafetyRuleKind.ENGINEERING_AUTHORITY_GUARD,
        priority = priority,
        prerequisites = prerequisites,
        exclusions = emptyList(),
        result = result,
        provenance = listOf(architected),
        executionAuthority = SafetyRuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION,
        unresolvedReviewRestrictions = engineeringRestrictions,
    )

    val rules: List<SafetyScopeRule> = listOf(
        engineeringRule(
            "ctv204-r001-explicit-emergency-boundary",
            "An explicitly established emergency condition blocks ordinary policy without predicting risk.",
            2200,
            listOf(field(SafetyField.CURRENT_EMERGENCY, ExplicitEmergencyCircumstance.SELF_HARM_EMERGENCY_EXPLICITLY_ESTABLISHED, ExplicitEmergencyCircumstance.OTHER_EMERGENCY_EXPLICITLY_ESTABLISHED)),
            SafetyRuleResult.Terminate(SafetyAuthorityState.EMERGENCY_BOUNDARY_REACHED, "EMERGENCY_EXTERNAL_ACTION_POLICY_REQUIRED"),
        ),
        engineeringRule(
            "ctv204-r002-explicit-medical-emergency-boundary",
            "An explicitly established acute medical emergency blocks ordinary policy.",
            2190,
            listOf(field(SafetyField.ACUTE_MEDICAL_EMERGENCY, SafetyPresence.PRESENT)),
            SafetyRuleResult.Terminate(SafetyAuthorityState.EMERGENCY_BOUNDARY_REACHED, "URGENT_MEDICAL_POLICY_REQUIRED"),
        ),
        SafetyScopeRule(
            id = SafetyRuleId.parse("ctv204-r003-self-harm-specialized-boundary"),
            description = "A directly established self-harm-relevant fact leaves the ordinary problem-solving policy.",
            kind = SafetyRuleKind.SOURCE_DERIVED_SCOPE_BOUNDARY,
            priority = 2100,
            prerequisites = listOf(field(SafetyField.SELF_HARM_RELEVANCE, SafetyPresence.PRESENT)),
            exclusions = emptyList(),
            result = SafetyRuleResult.Terminate(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, "SELF_HARM_SPECIALIZED_POLICY_REQUIRED"),
            provenance = listOf(niceSelfHarmBoundary),
            executionAuthority = SafetyRuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION,
            unresolvedReviewRestrictions = niceRestrictions,
        ),
        engineeringRule(
            "ctv204-r004-harm-to-others-specialized-boundary",
            "A harm-to-others concern requires a separately governed policy and cannot borrow self-harm logic.",
            2090,
            listOf(field(SafetyField.HARM_TO_OTHERS_RELEVANCE, SafetyPresence.PRESENT)),
            SafetyRuleResult.Terminate(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, "HARM_TO_OTHERS_SOURCE_AND_POLICY_REQUIRED"),
        ),
        engineeringRule(
            "ctv204-r005-specialized-condition-boundary",
            "An established specialized condition cannot be absorbed into ordinary policy.",
            2080,
            listOf(field(SafetyField.SPECIALIZED_SCOPE_CONDITION, *SpecializedScopeCondition.entries.filter { it != SpecializedScopeCondition.NONE_IDENTIFIED }.toTypedArray())),
            SafetyRuleResult.Terminate(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, "CONDITION_SPECIFIC_POLICY_REQUIRED"),
        ),
        engineeringRule(
            "ctv204-r006-specialized-presenting-scope",
            "An explicitly specialized presenting scope requires a different governed policy.",
            2070,
            listOf(field(SafetyField.PRESENTING_SCOPE, PresentingScope.SPECIALIZED_POLICY_REQUIRED)),
            SafetyRuleResult.Terminate(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, "SPECIALIZED_PRESENTING_POLICY_REQUIRED"),
        ),
        engineeringRule(
            "ctv204-r007-out-of-scope-boundary",
            "An explicitly out-of-scope presentation cannot continue through ordinary therapy.",
            2060,
            listOf(field(SafetyField.PRESENTING_SCOPE, PresentingScope.OUT_OF_SCOPE)),
            SafetyRuleResult.Terminate(SafetyAuthorityState.OUT_OF_SCOPE, "SUPPORTED_SCOPE_POLICY_REQUIRED"),
        ),
        engineeringRule(
            "ctv204-r008-unsupported-population-boundary",
            "Unsupported population or setting blocks the current ordinary policy.",
            2050,
            listOf(field(SafetyField.POPULATION_APPLICABILITY, PopulationApplicability.UNSUPPORTED_AGE_OR_POPULATION, PopulationApplicability.UNSUPPORTED_SETTING)),
            SafetyRuleResult.Terminate(SafetyAuthorityState.OUT_OF_SUPPORTED_POPULATION, "SUPPORTED_POPULATION_OR_SETTING_POLICY_REQUIRED"),
        ),
        engineeringRule(
            "ctv204-r009-therapist-mode-only",
            "This gate grants ordinary Therapist authority only; it grants no Journal or Biographer behavior.",
            2040,
            listOf(SafetyModeIsNot(ThomasMode.THERAPIST)),
            SafetyRuleResult.Terminate(SafetyAuthorityState.OUT_OF_SCOPE, "ACTIVE_MODE_POLICY_REQUIRED"),
        ),
        engineeringRule(
            "ctv204-r010-contradictory-evidence-clarification",
            "Contradictory required evidence remains unresolved and requires one selected clarification.",
            1800,
            listOf(FieldResolutionIn(allFields, setOf(SafetyEvidenceResolution.CONTRADICTORY))),
            SafetyRuleResult.RequireClarification(ClarificationSelector.FIRST_CONTRADICTORY_REQUIRED_FIELD),
        ),
        engineeringRule(
            "ctv204-r011-declined-required-information",
            "A declined required clarification remains unavailable and cannot be treated as absence.",
            1700,
            listOf(FieldResolutionIn(allFields, setOf(SafetyEvidenceResolution.USER_DECLINED))),
            SafetyRuleResult.Terminate(SafetyAuthorityState.INSUFFICIENT_INFORMATION, "REQUIRED_SAFETY_INFORMATION_DECLINED"),
        ),
        engineeringRule(
            "ctv204-r012-tentative-evidence-clarification",
            "Tentative safety evidence cannot grant ordinary authority and requires one selected clarification.",
            1600,
            listOf(FieldResolutionIn(allFields, setOf(SafetyEvidenceResolution.TENTATIVE))),
            SafetyRuleResult.RequireClarification(ClarificationSelector.FIRST_TENTATIVE_REQUIRED_FIELD),
        ),
        engineeringRule(
            "ctv204-r013-missing-evidence-clarification",
            "Unknown or not-asked required evidence cannot become reassuring evidence.",
            1500,
            listOf(FieldResolutionIn(allFields, setOf(SafetyEvidenceResolution.UNKNOWN, SafetyEvidenceResolution.NOT_ASKED))),
            SafetyRuleResult.RequireClarification(ClarificationSelector.FIRST_MISSING_REQUIRED_FIELD),
        ),
        engineeringRule(
            "ctv204-r014-ordinary-policy-permit",
            "Issue a revision-bound qualification permit only when every required fact is explicitly established for the ordinary slice.",
            1000,
            listOf(
                SafetyModeIs(ThomasMode.THERAPIST),
                field(SafetyField.CURRENT_EMERGENCY, ExplicitEmergencyCircumstance.NONE_ESTABLISHED),
                field(SafetyField.ACUTE_MEDICAL_EMERGENCY, SafetyPresence.ABSENT),
                field(SafetyField.SELF_HARM_RELEVANCE, SafetyPresence.ABSENT),
                field(SafetyField.HARM_TO_OTHERS_RELEVANCE, SafetyPresence.ABSENT),
                field(SafetyField.SPECIALIZED_SCOPE_CONDITION, SpecializedScopeCondition.NONE_IDENTIFIED),
                field(SafetyField.POPULATION_APPLICABILITY, PopulationApplicability.SUPPORTED_ADULT_QUALIFICATION_CONTEXT),
                field(SafetyField.PRESENTING_SCOPE, PresentingScope.BOUNDED_ORDINARY_PERSONAL_PROBLEM),
            ),
            SafetyRuleResult.AllowOrdinaryPolicy,
        ),
    )
}

class SafetyScopeGate(
    rules: List<SafetyScopeRule> = SafetyScopeRuleCatalog.rules,
    private val clarificationAction: SafetyActionDefinition = SafetyGateActions.clarifyRequiredFact,
) : SafetyGovernor<SafetyScopeInput, SafetyScopeDecision> {
    private val orderedRules = rules.sortedWith(compareByDescending<SafetyScopeRule> { it.priority }.thenBy { it.id })

    init { require(orderedRules.map { it.id }.distinct().size == orderedRules.size) }

    override fun govern(input: SafetyScopeInput): SafetyScopeDecision {
        val invalid = input.validationErrors()
        if (invalid.isNotEmpty()) return terminalWithoutRules(input, SafetyAuthorityState.INVALID_INPUT, invalid, "STRUCTURED_SAFETY_INPUT_CORRECTION_REQUIRED")

        val traces = orderedRules.map { it.evaluate(input) }
        val traceById = traces.associateBy { it.ruleId }
        val matched = orderedRules.filter { requireNotNull(traceById[it.id]).matched }
        val rejected = orderedRules.filterNot { it in matched }.map { rule ->
            RejectedSafetyRule(rule.id, requireNotNull(traceById[rule.id]).rejectionReasons)
        }
        if (matched.isEmpty()) return decision(
            input,
            SafetyAuthorityState.NO_AUTHORIZED_ACTION,
            traces,
            emptyList(),
            rejected,
            unresolved = setOf(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED),
            handoff = "SAFETY_POLICY_DEFINITION_REQUIRED",
            tieBreak = SafetyTieBreakTrace(SafetyTieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER, null, emptyList(), "NO_MATCH"),
        )

        // Production Therapy is allowed to converse while safety evidence is simply absent.
        // UNKNOWN is deliberately retained in the observations and is never converted into a
        // reassuring value. Any explicit boundary, tentative/contradictory evidence, declined
        // evidence, or a caller that requires clarification still follows the ordered rules.
        val missingEvidenceRule = SafetyRuleId.parse("ctv204-r013-missing-evidence-clarification")
        if (input.unknownEvidencePolicy == SafetyUnknownEvidencePolicy.ALLOW_ORDINARY_WITHOUT_REASSURANCE &&
            matched.size == 1 && matched.single().id == missingEvidenceRule) {
            val reference = decisionReference(input)
            return decision(
                input,
                SafetyAuthorityState.ORDINARY_POLICY_ALLOWED,
                traces,
                matched.map { it.id }.sorted(),
                rejected,
                unresolved = setOf(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED),
                permit = OrdinaryTherapyPermit(CT_V2_04_SAFETY_SCOPE_POLICY_VERSION, reference, input.stateId, input.evidenceRevision),
                tieBreak = SafetyTieBreakTrace(
                    SafetyTieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER,
                    null,
                    emptyList(),
                    "CONDITIONAL_ORDINARY_DEFAULT_WITHOUT_REASSURANCE",
                ),
            )
        }

        val highest = matched.maxOf { it.priority }
        val contenders = matched.filter { it.priority == highest }.sortedBy { it.id }
        if (contenders.size != 1) return decision(
            input,
            SafetyAuthorityState.POLICY_CONFLICT,
            traces,
            matched.map { it.id }.sorted(),
            rejected,
            unresolved = setOf(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED),
            handoff = "SAFETY_POLICY_ADJUDICATION_REQUIRED",
            tieBreak = SafetyTieBreakTrace(SafetyTieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER, highest, contenders.map { it.id }, "CONFLICT_NOT_GUESSED"),
        )

        val winner = contenders.single()
        val tieBreak = SafetyTieBreakTrace(SafetyTieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER, highest, listOf(winner.id), "UNIQUE_HIGHEST_PRIORITY_RULE")
        if (winner.executionAuthority != SafetyRuleExecutionAuthority.EXECUTABLE_FOR_QUALIFICATION) return decision(
            input,
            SafetyAuthorityState.REVIEW_BLOCKED,
            traces,
            matched.map { it.id }.sorted(),
            rejected,
            winner = winner,
            unresolved = winner.unresolvedReviewRestrictions,
            handoff = "SAFETY_RULE_REVIEW_REQUIRED",
            tieBreak = tieBreak,
        )

        return when (val result = winner.result) {
            SafetyRuleResult.AllowOrdinaryPolicy -> {
                val reference = decisionReference(input)
                decision(
                    input,
                    SafetyAuthorityState.ORDINARY_POLICY_ALLOWED,
                    traces,
                    matched.map { it.id }.sorted(),
                    rejected,
                    winner = winner,
                    unresolved = winner.unresolvedReviewRestrictions,
                    permit = OrdinaryTherapyPermit(CT_V2_04_SAFETY_SCOPE_POLICY_VERSION, reference, input.stateId, input.evidenceRevision),
                    tieBreak = tieBreak,
                )
            }
            is SafetyRuleResult.RequireClarification -> {
                val requirement = selectClarificationRequirement(input, result.selector)
                decision(
                    input,
                    SafetyAuthorityState.CLARIFICATION_REQUIRED,
                    traces,
                    matched.map { it.id }.sorted(),
                    rejected,
                    winner = winner,
                    unresolved = winner.unresolvedReviewRestrictions + clarificationAction.unresolvedReviewRestrictions,
                    missing = unresolvedFields(input),
                    action = SelectedSafetyQualificationAction(clarificationAction, requirement, winner.id, winner.executionAuthority),
                    nextExpected = requirement,
                    handoff = "REASSESS_SAFETY_SCOPE_GATE_AFTER_CLARIFICATION",
                    tieBreak = tieBreak,
                )
            }
            is SafetyRuleResult.Terminate -> decision(
                input,
                result.authorityState,
                traces,
                matched.map { it.id }.sorted(),
                rejected,
                winner = winner,
                unresolved = winner.unresolvedReviewRestrictions,
                handoff = result.handoffRequirement,
                tieBreak = tieBreak,
            )
        }
    }

    private fun selectClarificationRequirement(input: SafetyScopeInput, selector: ClarificationSelector): SafetyInformationRequirement {
        val expectedResolution = when (selector) {
            ClarificationSelector.FIRST_CONTRADICTORY_REQUIRED_FIELD -> setOf(SafetyEvidenceResolution.CONTRADICTORY)
            ClarificationSelector.FIRST_TENTATIVE_REQUIRED_FIELD -> setOf(SafetyEvidenceResolution.TENTATIVE)
            ClarificationSelector.FIRST_MISSING_REQUIRED_FIELD -> setOf(SafetyEvidenceResolution.UNKNOWN, SafetyEvidenceResolution.NOT_ASKED)
        }
        val field = SafetyField.entries.firstOrNull { input.evidence(it).resolution in expectedResolution }
            ?: error("Matched clarification rule did not identify its required field.")
        return SafetyInformationRequirement.forField(field)
    }

    private fun terminalWithoutRules(
        input: SafetyScopeInput,
        state: SafetyAuthorityState,
        validationErrors: List<String>,
        handoff: String,
    ) = decision(
        input,
        state,
        emptyList(),
        emptyList(),
        emptyList(),
        missing = emptyList(),
        handoff = handoff,
        validationErrors = validationErrors,
        tieBreak = SafetyTieBreakTrace(SafetyTieBreakPrinciple.HIGHEST_PRIORITY_REQUIRES_UNIQUE_WINNER, null, emptyList(), "INPUT_REJECTED"),
    )

    private fun decision(
        input: SafetyScopeInput,
        state: SafetyAuthorityState,
        traces: List<SafetyRuleEvaluationTrace>,
        eligible: List<SafetyRuleId>,
        rejected: List<RejectedSafetyRule>,
        winner: SafetyScopeRule? = null,
        unresolved: Set<SafetyReviewRestriction> = setOf(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED),
        missing: List<SafetyField> = unresolvedFields(input),
        action: SelectedSafetyQualificationAction? = null,
        nextExpected: SafetyInformationRequirement? = null,
        handoff: String? = null,
        permit: OrdinaryTherapyPermit? = null,
        validationErrors: List<String> = emptyList(),
        tieBreak: SafetyTieBreakTrace,
    ) = SafetyScopeDecision(
        policyVersion = CT_V2_04_SAFETY_SCOPE_POLICY_VERSION,
        decisionReference = decisionReference(input),
        stateId = input.stateId,
        evidenceRevision = input.evidenceRevision,
        authorityState = state,
        observations = SafetyField.entries.map { field ->
            val evidence = input.evidence(field)
            SafetyObservationTrace(field, evidence.resolution, (evidence.value as? Enum<*>)?.name ?: evidence.value?.toString(), evidence.origin, evidence.evidenceReferences)
        },
        eligibleRuleIds = eligible,
        rejectedRules = rejected,
        selectedRuleId = winner?.id,
        selectedRuleProvenance = winner?.provenance.orEmpty(),
        missingFacts = missing,
        unresolvedReviewRestrictions = (unresolved.map { it } + if (validationErrors.isEmpty()) emptyList() else listOf(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED)).distinct().sortedBy { it.name },
        selectedAction = action,
        nextExpectedEvidence = nextExpected,
        handoffRequirement = listOfNotNull(handoff, validationErrors.takeIf { it.isNotEmpty() }?.joinToString(" | ")).joinToString(" | ").ifBlank { null },
        prohibitedContinuationPaths = if (state == SafetyAuthorityState.ORDINARY_POLICY_ALLOWED) emptySet() else setOf("ORDINARY_THERAPIST_POLICY"),
        ordinaryTherapyPermit = permit,
        ruleTrace = traces,
        tieBreakTrace = tieBreak,
    )

    private fun decisionReference(input: SafetyScopeInput) =
        "$CT_V2_04_SAFETY_SCOPE_POLICY_VERSION:${input.stateId}:revision-${input.evidenceRevision.value}"

    private fun unresolvedFields(input: SafetyScopeInput): List<SafetyField> = SafetyField.entries.filter {
        input.evidence(it).resolution != SafetyEvidenceResolution.ESTABLISHED
    }
}

object SafetyDecisionExplainer {
    fun compact(decision: SafetyScopeDecision): String {
        val rule = decision.selectedRuleId?.value ?: decision.tieBreakTrace.contenderRuleIds.joinToString(",") { it.value }.ifBlank { "none" }
        val next = decision.nextExpectedEvidence?.name ?: decision.handoffRequirement ?: "ORDINARY_THERAPIST_POLICY"
        return "SAFETY_SCOPE_GATE -> $rule -> ${decision.authorityState} -> $next"
    }
}

data class SafetyRuleReviewRecord(
    val ruleId: SafetyRuleId,
    val description: String,
    val sourceLocator: String,
    val population: String,
    val setting: String,
    val intendedDeliverer: String,
    val runtimeConsequence: String,
    val limitations: List<String>,
    val conflicts: Set<String>,
    val pendingReviews: Set<SafetyReviewRestriction>,
)

object SafetyRuleReviewReport {
    fun records(): List<SafetyRuleReviewRecord> = SafetyScopeRuleCatalog.rules.map { rule ->
        val clinical = rule.provenance.filterIsInstance<SafetyRuleProvenance.ClinicalSource>()
        SafetyRuleReviewRecord(
            ruleId = rule.id,
            description = rule.description,
            sourceLocator = rule.provenance.joinToString(" | ") { it.preciseLocator },
            population = clinical.joinToString(" | ") { it.population }.ifBlank { "Engineering authority boundary; no clinical population claim" },
            setting = clinical.joinToString(" | ") { it.setting }.ifBlank { "Qualification harness only" },
            intendedDeliverer = clinical.joinToString(" | ") { it.intendedDeliverer }.ifBlank { "Thomas qualification policy; production authority not granted" },
            runtimeConsequence = rule.result.toString(),
            limitations = clinical.flatMap { it.limitations },
            conflicts = clinical.flatMap { it.conflictIds }.map { it.value }.toSet(),
            pendingReviews = rule.unresolvedReviewRestrictions,
        )
    }.sortedBy { it.ruleId }
}
