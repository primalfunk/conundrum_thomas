package com.conundrum.thomas.v2.safety

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.ontology.GovernanceReviewStatus
import com.conundrum.thomas.v2.provenance.CommercialUseStatus
import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.SourceConflictId
import com.conundrum.thomas.v2.provenance.SourceLocatorId

private val safetyPolicyIdFormat = Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")

@JvmInline
value class SafetyRuleId private constructor(val value: String) : Comparable<SafetyRuleId> {
    override fun compareTo(other: SafetyRuleId): Int = value.compareTo(other.value)

    companion object {
        fun parse(value: String): SafetyRuleId {
            require(safetyPolicyIdFormat.matches(value)) { "Safety rule IDs must be lowercase and hyphenated." }
            return SafetyRuleId(value)
        }
    }
}

@JvmInline
value class SafetyActionId private constructor(val value: String) : Comparable<SafetyActionId> {
    override fun compareTo(other: SafetyActionId): Int = value.compareTo(other.value)

    companion object {
        fun parse(value: String): SafetyActionId {
            require(safetyPolicyIdFormat.matches(value)) { "Safety action IDs must be lowercase and hyphenated." }
            return SafetyActionId(value)
        }
    }
}

enum class SafetyRuleKind {
    ENGINEERING_AUTHORITY_GUARD,
    SOURCE_DERIVED_SCOPE_BOUNDARY,
}

enum class SafetyRuleExecutionAuthority {
    CANDIDATE_RULE,
    EXECUTABLE_FOR_QUALIFICATION,
}

enum class ProductionSafetyAuthority { NOT_GRANTED }

enum class SafetyReviewRestriction {
    CLINICAL_REVIEW_PENDING,
    RIGHTS_REVIEW_PENDING,
    LEGAL_REVIEW_PENDING,
    IMPLEMENTATION_SCOPE_REVIEW_PENDING,
    SOFTWARE_AUTONOMY_REVIEW_PENDING,
    TRAINING_DELIVERER_RESTRICTION_UNRESOLVED,
    SUPERVISION_RESTRICTION_UNRESOLVED,
    PRODUCTION_AUTHORITY_NOT_GRANTED,
}

enum class SafetySourceRelationship {
    BOUNDS_ORDINARY_POLICY_SCOPE,
    IDENTIFIES_PROHIBITED_PREDICTIVE_USE,
    PRESERVES_SPECIALIST_ASSESSMENT_BOUNDARY,
}

enum class SafetySourceConflictState {
    NONE_RECORDED,
    DIFFERENT_SCOPE_RECORDED,
    OPEN,
}

sealed interface SafetyRuleProvenance {
    val preciseLocator: String

    data class ClinicalSource(
        val source: GovernedSourceReference,
        val immutableArtifactLocatorId: SourceLocatorId?,
        val relationship: SafetySourceRelationship,
        override val preciseLocator: String,
        val population: String,
        val setting: String,
        val intendedDeliverer: String,
        val purpose: String,
        val limitations: List<String>,
        val commercialUseStatus: CommercialUseStatus,
        val clinicalReviewStatus: GovernanceReviewStatus = GovernanceReviewStatus.PENDING,
        val rightsReviewStatus: GovernanceReviewStatus = GovernanceReviewStatus.PENDING,
        val legalReviewStatus: GovernanceReviewStatus = GovernanceReviewStatus.PENDING,
        val softwareAutonomyReviewStatus: GovernanceReviewStatus = GovernanceReviewStatus.PENDING,
        val implementationScopeReviewStatus: GovernanceReviewStatus = GovernanceReviewStatus.PENDING,
        val conflictState: SafetySourceConflictState,
        val conflictIds: Set<SourceConflictId> = emptySet(),
    ) : SafetyRuleProvenance {
        init {
            require(preciseLocator.isNotBlank())
            require(population.isNotBlank() && setting.isNotBlank() && intendedDeliverer.isNotBlank())
            require(purpose.isNotBlank())
            require(limitations.isNotEmpty() && limitations.none(String::isBlank))
            require(clinicalReviewStatus != GovernanceReviewStatus.COMPLETE)
            require(rightsReviewStatus != GovernanceReviewStatus.COMPLETE)
            require(legalReviewStatus != GovernanceReviewStatus.COMPLETE)
            require(softwareAutonomyReviewStatus != GovernanceReviewStatus.COMPLETE)
            require(implementationScopeReviewStatus != GovernanceReviewStatus.COMPLETE)
            require((conflictState == SafetySourceConflictState.NONE_RECORDED) == conflictIds.isEmpty())
        }
    }

    data class GoverningArchitecture(
        val artifactId: String,
        override val preciseLocator: String,
    ) : SafetyRuleProvenance {
        init {
            require(artifactId.isNotBlank())
            require(preciseLocator.isNotBlank())
        }
    }
}

data class SafetyConditionEvaluation(
    val conditionId: String,
    val matched: Boolean,
    val actual: String,
    val expected: String,
)

sealed interface SafetyCondition {
    val stableId: String
    fun evaluate(input: SafetyScopeInput): SafetyConditionEvaluation
}

private fun conditionEvaluation(id: String, actual: Any?, expected: Any?, matched: Boolean) =
    SafetyConditionEvaluation(id, matched, actual?.toString() ?: "UNKNOWN", expected.toString())

data class SafetyModeIs(val expected: ThomasMode) : SafetyCondition {
    override val stableId = "condition-mode-is-${expected.name.lowercase()}"
    override fun evaluate(input: SafetyScopeInput) =
        conditionEvaluation(stableId, input.mode, expected, input.mode == expected)
}

data class SafetyModeIsNot(val prohibited: ThomasMode) : SafetyCondition {
    override val stableId = "condition-mode-is-not-${prohibited.name.lowercase()}"
    override fun evaluate(input: SafetyScopeInput) =
        conditionEvaluation(stableId, input.mode, "not $prohibited", input.mode != prohibited)
}

data class FieldResolutionIn(
    val fields: Set<SafetyField>,
    val expected: Set<SafetyEvidenceResolution>,
) : SafetyCondition {
    init {
        require(fields.isNotEmpty())
        require(expected.isNotEmpty())
    }

    override val stableId = "condition-${fields.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}-resolution-in-${expected.sortedBy { it.name }.joinToString("-") { it.name.lowercase() }}"

    override fun evaluate(input: SafetyScopeInput): SafetyConditionEvaluation {
        val matching = fields.filter { input.evidence(it).resolution in expected }.sortedBy { it.ordinal }
        val actual = fields.sortedBy { it.ordinal }.joinToString(",") { "${it.name}=${input.evidence(it).resolution}" }
        return conditionEvaluation(stableId, actual, expected.sortedBy { it.name }, matching.isNotEmpty())
    }
}

data class FieldEstablishedValueIn(
    val field: SafetyField,
    val expectedValues: Set<String>,
) : SafetyCondition {
    init { require(expectedValues.isNotEmpty()) }

    override val stableId = "condition-${field.name.lowercase()}-established-value-in-${expectedValues.sorted().joinToString("-") { it.lowercase() }}"

    override fun evaluate(input: SafetyScopeInput): SafetyConditionEvaluation {
        val evidence = input.evidence(field)
        val actualValue = (evidence.value as? Enum<*>)?.name ?: evidence.value?.toString()
        val matches = evidence.resolution == SafetyEvidenceResolution.ESTABLISHED && actualValue in expectedValues
        return conditionEvaluation(stableId, "${evidence.resolution}:$actualValue", expectedValues.sorted(), matches)
    }
}

enum class ClarificationSelector {
    FIRST_CONTRADICTORY_REQUIRED_FIELD,
    FIRST_TENTATIVE_REQUIRED_FIELD,
    FIRST_MISSING_REQUIRED_FIELD,
}

sealed interface SafetyRuleResult {
    data object AllowOrdinaryPolicy : SafetyRuleResult

    data class RequireClarification(val selector: ClarificationSelector) : SafetyRuleResult

    data class Terminate(
        val authorityState: SafetyAuthorityState,
        val handoffRequirement: String,
    ) : SafetyRuleResult {
        init {
            require(authorityState !in setOf(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, SafetyAuthorityState.CLARIFICATION_REQUIRED))
            require(handoffRequirement.isNotBlank())
        }
    }
}

data class SafetyScopeRule(
    val id: SafetyRuleId,
    val description: String,
    val kind: SafetyRuleKind,
    val priority: Int,
    val prerequisites: List<SafetyCondition>,
    val exclusions: List<SafetyCondition>,
    val result: SafetyRuleResult,
    val provenance: List<SafetyRuleProvenance>,
    val executionAuthority: SafetyRuleExecutionAuthority,
    val unresolvedReviewRestrictions: Set<SafetyReviewRestriction>,
    val productionAuthority: ProductionSafetyAuthority = ProductionSafetyAuthority.NOT_GRANTED,
) {
    init {
        require(description.isNotBlank())
        require(priority >= 0)
        require(prerequisites.isNotEmpty())
        require(provenance.isNotEmpty()) { "Safety rule ${id.value} requires exact provenance." }
        if (kind != SafetyRuleKind.ENGINEERING_AUTHORITY_GUARD) {
            require(provenance.any { it is SafetyRuleProvenance.ClinicalSource }) {
                "Source-derived safety rule ${id.value} requires governed clinical provenance."
            }
        }
        require(SafetyReviewRestriction.PRODUCTION_AUTHORITY_NOT_GRANTED in unresolvedReviewRestrictions)
        require(productionAuthority == ProductionSafetyAuthority.NOT_GRANTED)
    }
}

data class SafetyRuleEvaluationTrace(
    val ruleId: SafetyRuleId,
    val priority: Int,
    val prerequisites: List<SafetyConditionEvaluation>,
    val exclusions: List<SafetyConditionEvaluation>,
    val matched: Boolean,
    val rejectionReasons: List<String>,
    val executionAuthority: SafetyRuleExecutionAuthority,
)

internal fun SafetyScopeRule.evaluate(input: SafetyScopeInput): SafetyRuleEvaluationTrace {
    val prerequisiteResults = prerequisites.map { it.evaluate(input) }
    val exclusionResults = exclusions.map { it.evaluate(input) }
    val failed = prerequisiteResults.filterNot { it.matched }
    val excluded = exclusionResults.filter { it.matched }
    return SafetyRuleEvaluationTrace(
        ruleId = id,
        priority = priority,
        prerequisites = prerequisiteResults,
        exclusions = exclusionResults,
        matched = failed.isEmpty() && excluded.isEmpty(),
        rejectionReasons = failed.map { "PREREQUISITE_FAILED:${it.conditionId}" } +
            excluded.map { "EXCLUSION_MATCHED:${it.conditionId}" },
        executionAuthority = executionAuthority,
    )
}
