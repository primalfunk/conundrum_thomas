package com.conundrum.thomas.v2.longitudinal

enum class ContradictionAdjudication {
    UNRESOLVED,
    EXPLICIT_CORRECTION_EXISTS,
    DIFFERENT_SCOPE,
    RESOLVED_WITH_HISTORY_PRESERVED,
}

data class ContradictionRelation(
    val id: EvidenceRelationId,
    val leftAssertionId: AssertionId,
    val rightAssertionId: AssertionId,
    val adjudication: ContradictionAdjudication = ContradictionAdjudication.UNRESOLVED,
    val rationale: String,
) {
    init {
        require(leftAssertionId != rightAssertionId)
        require(rationale.isNotBlank())
    }
}

enum class CorrectionEffect { CORRECTS_DETAIL, CORRECTS_AND_SUPERSEDES }

data class CorrectionRelation(
    val id: EvidenceRelationId,
    val correctingAssertionId: AssertionId,
    val correctedAssertionId: AssertionId,
    val effect: CorrectionEffect,
    val rationale: String,
) {
    init {
        require(correctingAssertionId != correctedAssertionId)
        require(rationale.isNotBlank())
    }
}

enum class SupersessionKind { REFINES, REPLACES, CORRECTS, INVALIDATES_DERIVED_INTERPRETATION }

data class SupersessionRelation(
    val id: EvidenceRelationId,
    val successor: ClaimReference,
    val predecessor: ClaimReference,
    val kind: SupersessionKind,
    val rationale: String,
) {
    init {
        require(successor != predecessor)
        require(rationale.isNotBlank())
    }
}

enum class DependencyRole { SUPPORTS, CONTEXTUALIZES, WEAKENS }

data class HypothesisDependency(
    val id: EvidenceRelationId,
    val dependentHypothesisId: HypothesisId,
    val prerequisite: ClaimReference,
    val role: DependencyRole,
    val rationale: String,
) {
    init {
        require(prerequisite != ClaimReference.Hypothesis(dependentHypothesisId))
        require(rationale.isNotBlank())
    }
}
