package com.conundrum.thomas.v2.ontology

import com.conundrum.thomas.v2.provenance.ReviewRequirementId
import com.conundrum.thomas.v2.provenance.SourceVersionId

sealed interface OntologyConcept {
    val id: OntologyConceptId
    val family: ConceptFamily
    val domain: OntologyDomain
    val label: String
    val definition: String
    val definitionStatus: DefinitionStatus
    val sourceSupportStatus: SourceSupportStatus
    val runtimeAuthorization: RuntimeAuthorizationStatus
}

data class ObservationConcept(
    override val id: OntologyConceptId,
    override val label: String,
    override val definition: String,
    override val definitionStatus: DefinitionStatus = DefinitionStatus.DEFINED,
    override val sourceSupportStatus: SourceSupportStatus = SourceSupportStatus.ARCHITECTURAL_DEFINITION,
    override val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
    override val family: ConceptFamily = ConceptFamily.OBSERVED_USER_STATE,
    override val domain: OntologyDomain = OntologyDomain.PSYCHOLOGICAL_SUPPORT,
) : OntologyConcept

data class GoalConcept(
    override val id: OntologyConceptId,
    override val label: String,
    override val definition: String,
    override val definitionStatus: DefinitionStatus = DefinitionStatus.DEFINED,
    override val sourceSupportStatus: SourceSupportStatus = SourceSupportStatus.ARCHITECTURAL_DEFINITION,
    override val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
    override val family: ConceptFamily = ConceptFamily.THERAPEUTIC_GOAL,
    override val domain: OntologyDomain = OntologyDomain.PSYCHOLOGICAL_SUPPORT,
) : OntologyConcept

data class DialogueActConcept(
    override val id: OntologyConceptId,
    override val label: String,
    override val definition: String,
    override val definitionStatus: DefinitionStatus = DefinitionStatus.DEFINED,
    override val sourceSupportStatus: SourceSupportStatus = SourceSupportStatus.ARCHITECTURAL_DEFINITION,
    override val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
    override val family: ConceptFamily = ConceptFamily.DIALOGUE_ACT,
    override val domain: OntologyDomain = OntologyDomain.PSYCHOLOGICAL_SUPPORT,
) : OntologyConcept

enum class GovernanceGate {
    CLINICAL_REVIEW,
    RIGHTS_REVIEW,
    IMPLEMENTATION_SCOPE_REVIEW,
    SOFTWARE_AUTONOMY_REVIEW,
    LEGAL_REVIEW,
    FUTURE_POLICY_AUTHORIZATION,
}

data class InterventionFamilyConcept(
    override val id: OntologyConceptId,
    override val label: String,
    override val definition: String,
    val governanceGates: Set<GovernanceGate>,
    override val definitionStatus: DefinitionStatus = DefinitionStatus.CANDIDATE,
    override val sourceSupportStatus: SourceSupportStatus,
    override val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
    override val family: ConceptFamily = ConceptFamily.INTERVENTION_FAMILY,
    override val domain: OntologyDomain = OntologyDomain.PSYCHOLOGICAL_SUPPORT,
) : OntologyConcept {
    init { require(governanceGates.contains(GovernanceGate.FUTURE_POLICY_AUTHORIZATION)) }
}

data class ConstraintConcept(
    override val id: OntologyConceptId,
    override val label: String,
    override val definition: String,
    override val definitionStatus: DefinitionStatus = DefinitionStatus.DEFINED,
    override val sourceSupportStatus: SourceSupportStatus = SourceSupportStatus.ARCHITECTURAL_DEFINITION,
    override val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
    override val family: ConceptFamily = ConceptFamily.CONSTRAINT,
    override val domain: OntologyDomain = OntologyDomain.PROCEDURAL_GRAMMAR,
) : OntologyConcept

data class SafetyContextConcept(
    override val id: OntologyConceptId,
    override val label: String,
    override val definition: String,
    val governanceGates: Set<GovernanceGate>,
    override val definitionStatus: DefinitionStatus = DefinitionStatus.CANDIDATE,
    override val sourceSupportStatus: SourceSupportStatus,
    override val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
    override val family: ConceptFamily = ConceptFamily.SAFETY_CONTEXT,
    override val domain: OntologyDomain = OntologyDomain.SAFETY_BOUNDARY,
) : OntologyConcept {
    init {
        require(governanceGates.contains(GovernanceGate.CLINICAL_REVIEW))
        require(governanceGates.contains(GovernanceGate.FUTURE_POLICY_AUTHORIZATION))
    }
}

data class ProceduralConcept(
    override val id: OntologyConceptId,
    override val label: String,
    override val definition: String,
    override val definitionStatus: DefinitionStatus = DefinitionStatus.DEFINED,
    override val sourceSupportStatus: SourceSupportStatus = SourceSupportStatus.ARCHITECTURAL_DEFINITION,
    override val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
    override val family: ConceptFamily = ConceptFamily.PROCEDURAL_VOCABULARY,
    override val domain: OntologyDomain = OntologyDomain.PROCEDURAL_GRAMMAR,
) : OntologyConcept

enum class ConstraintRelationshipKind {
    PREREQUISITE,
    POPULATION_CONSTRAINT,
    SETTING_CONSTRAINT,
    DELIVERER_ASSUMPTION,
    CONTRAINDICATION_CONCEPT,
    INFORMATION_REQUIREMENT,
    SAFETY_CONSTRAINT,
    AUTONOMY_RESTRICTION,
    CLINICAL_REVIEW_REQUIREMENT,
    RIGHTS_REQUIREMENT,
    IMPLEMENTATION_SCOPE_REQUIREMENT,
    LEGAL_REQUIREMENT,
    SOURCE_VERSION_DEPENDENCY,
}

sealed interface ConstraintTarget {
    data class Concept(val conceptId: OntologyConceptId) : ConstraintTarget
    data class Review(val reviewRequirementId: ReviewRequirementId) : ConstraintTarget
    data class SourceVersion(val sourceVersionId: SourceVersionId) : ConstraintTarget
    data class DeclaredScope(val scopeId: String, val description: String) : ConstraintTarget {
        init {
            require(scopeId.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")))
            require(description.isNotBlank())
        }
    }
}

/** Declarative relationship only; it has no eligibility evaluator. */
data class ConceptConstraintLink(
    val linkId: String,
    val subjectConceptId: OntologyConceptId,
    val relationship: ConstraintRelationshipKind,
    val target: ConstraintTarget,
    val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
) {
    init { require(linkId.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$"))) }
}
