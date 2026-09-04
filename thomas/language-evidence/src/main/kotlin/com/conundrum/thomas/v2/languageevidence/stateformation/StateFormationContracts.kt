package com.conundrum.thomas.v2.languageevidence.stateformation

import com.conundrum.thomas.v2.languageevidence.perception.LanguagePerceptionResult
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId

const val CT_V2_08_STATE_FORMATION_VERSION = "ct-v2-08.state-formation.v1"

/** Persistence-neutral read projection supplied by a governed store adapter. */
data class LongitudinalStateEvidence(
    val storeRevision: Long,
    val snapshot: LongitudinalEvidenceSnapshot,
    val eligibleSourceRevisionIds: Set<SourceRecordId>,
    val eligibleAssertionIds: Set<AssertionId>,
    val excludedObjectIds: Set<String> = emptySet(),
    val excludedLifecycleStates: Map<String, String> = emptyMap(),
    val perceptionResults: List<LanguagePerceptionResult> = emptyList(),
)

enum class OpenEvidenceQuestionKind {
    UNRESOLVED_IDENTITY,
    UNRESOLVED_REFERENCE,
    AMBIGUOUS_CORRECTION_TARGET,
    UNRESOLVED_CONTRADICTION,
    UNKNOWN_EVENT_TIME,
    UNSUPPORTED_OR_AMBIGUOUS_SOURCE,
}

data class OpenEvidenceQuestion(
    val id: String,
    val kind: OpenEvidenceQuestionKind,
    val basisIds: List<String>,
    val reasonCode: String,
)

data class UnresolvedIdentityReference(
    val label: String,
    val entityIds: List<LifeEntityId>,
    val reasonCode: String,
)

data class StructuralRecurrenceCandidate(
    val conceptId: PersonalConceptId,
    val assertionIds: List<AssertionId>,
    val independentSourceIds: List<String>,
    val label: String = "REPEATED_REPORTED_OCCURRENCE",
)

data class ExcludedEvidence(
    val objectId: String,
    val lifecycleState: String,
)

/** Deterministic evidence organization, never a free-form profile or therapeutic formulation. */
data class FormedLongitudinalState(
    val storeRevision: Long,
    val stateFormationVersion: String,
    val contributingSourceRevisionIds: List<SourceRecordId>,
    val activeExplicitClaims: List<EvidenceAssertion>,
    val activeSelfReports: List<EvidenceAssertion>,
    val activeSelfBeliefs: List<EvidenceAssertion>,
    val activeUserInterpretations: List<EvidenceAssertion>,
    val activeThirdPartyReports: List<EvidenceAssertion>,
    val activeEntitiesAndEvents: List<LifeEntity>,
    val contradictions: List<ContradictionRelation>,
    val unresolvedIdentities: List<UnresolvedIdentityReference>,
    val unresolvedCorrectionSourceRevisionIds: List<SourceRecordId>,
    val recurrenceCandidates: List<StructuralRecurrenceCandidate>,
    val openEvidenceQuestions: List<OpenEvidenceQuestion>,
    val excludedObjectIds: List<String>,
    val excludedEvidence: List<ExcludedEvidence>,
    val canonicalDigest: String,
)

fun interface LongitudinalStateFormer {
    fun form(evidence: LongitudinalStateEvidence): FormedLongitudinalState
}
