package com.conundrum.thomas.v2.ontology

import com.conundrum.thomas.v2.provenance.AuthorityDomain
import com.conundrum.thomas.v2.provenance.CommercialUseStatus
import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.ReviewRequirementId
import com.conundrum.thomas.v2.provenance.ReviewState
import com.conundrum.thomas.v2.provenance.SourceConflictId

enum class ConceptSourceRelationship {
    SUPPORTS_CANDIDATE_DEFINITION,
    BOUNDS_SCOPE,
    IDENTIFIES_RESTRICTION,
    QUALIFICATION_REFERENCE,
    CANDIDATE_SUBJECT_ONLY,
}

enum class BindingConflictState {
    NONE_RECORDED,
    OPEN,
    DIFFERENT_SCOPE_RECORDED,
    UNRESOLVED,
}

enum class GovernanceReviewStatus {
    PENDING,
    COMPLETE,
    NOT_REQUIRED,
}

enum class SourceBindingAuthority {
    PROVENANCE_ONLY,
}

data class OntologySourceBinding(
    val bindingId: String,
    val conceptId: OntologyConceptId,
    val source: GovernedSourceReference,
    val sourceAuthorityDomain: AuthorityDomain,
    val relationship: ConceptSourceRelationship,
    val sourceIdentityReviewState: ReviewState,
    val commercialUseStatus: CommercialUseStatus,
    val clinicalReviewRequirementId: ReviewRequirementId?,
    val clinicalReviewStatus: GovernanceReviewStatus,
    val rightsReviewRequirementId: ReviewRequirementId?,
    val rightsReviewStatus: GovernanceReviewStatus,
    val conflictState: BindingConflictState,
    val conflictIds: Set<SourceConflictId> = emptySet(),
    val scopeLimitations: List<String>,
    val bindingAuthority: SourceBindingAuthority = SourceBindingAuthority.PROVENANCE_ONLY,
    val runtimeAuthorization: RuntimeAuthorizationStatus = RuntimeAuthorizationStatus.NOT_AUTHORIZED,
) {
    init {
        require(bindingId.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")))
        require(scopeLimitations.isNotEmpty() && scopeLimitations.none(String::isBlank))
        require((clinicalReviewStatus == GovernanceReviewStatus.NOT_REQUIRED) == (clinicalReviewRequirementId == null))
        require((rightsReviewStatus == GovernanceReviewStatus.NOT_REQUIRED) == (rightsReviewRequirementId == null))
        require((conflictState == BindingConflictState.NONE_RECORDED) == conflictIds.isEmpty())
    }
}
