package com.conundrum.thomas.v2.longitudinal.admission

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.CoverageTopic
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.PersonalEvidenceProvenance
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import java.io.Serializable

private val admissionIdPattern = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")
private fun admissionId(label: String, value: String): String {
    require(admissionIdPattern.matches(value)) { "$label must be a stable lowercase identifier" }
    return value
}

@JvmInline value class AdmissionRequestId private constructor(val value: String) : Serializable {
    companion object { fun parse(value: String) = AdmissionRequestId(admissionId("Admission request ID", value)) }
}

@JvmInline value class IdempotencyKey private constructor(val value: String) : Serializable {
    companion object { fun parse(value: String) = IdempotencyKey(admissionId("Idempotency key", value)) }
}

@JvmInline value class AdmissionPolicyVersion private constructor(val value: String) : Serializable {
    companion object {
        val CT_V2_07_V1 = AdmissionPolicyVersion("ct-v2-07.admission.v1")
        fun unsupportedForTest(value: String) = AdmissionPolicyVersion(value)
    }
}

enum class StoreDataClassification { SYNTHETIC_QUALIFICATION_ONLY }
enum class AdmissionActor { USER, THOMAS, SYSTEM, QUALIFICATION_HARNESS }
enum class AdmissionOrigin {
    JOURNAL,
    BIOGRAPHER_OPEN_NARRATIVE,
    BIOGRAPHER_GUIDED_TIMELINE,
    THERAPIST_CONVERSATION,
    USER_CORRECTION,
    THOMAS_DERIVATION,
    QUALIFICATION_HARNESS,
}

enum class SourcePrivacy { ELIGIBLE, PRIVATE }

enum class AdmissionOperationType {
    ADMIT_SOURCE,
    APPEND_SOURCE_REVISION,
    ADMIT_EVIDENCE_BUNDLE,
    RECORD_USER_CORRECTION,
    RECORD_SUPERSESSION,
    RECORD_CONTRADICTION,
    REVISE_IDENTITY_LINK,
    CHANGE_COVERAGE,
    CHANGE_PRIVACY,
    RETIRE_CLAIM,
}

enum class AdmissionDisposition {
    ACCEPTED,
    IDEMPOTENT_REPLAY,
    REJECTED_VALIDATION,
    REJECTED_AUTHORITY,
    REJECTED_STALE_REVISION,
    REJECTED_IDEMPOTENCY_CONFLICT,
    REJECTED_MISSING_REFERENCE,
    REJECTED_DEPENDENCY,
    REJECTED_PRIVACY,
    REJECTED_TEMPORAL_DISHONESTY,
    REJECTED_RELATION_CYCLE,
    REJECTED_SCHEMA_OR_STORE_STATE,
    FAILED_WITHOUT_COMMIT,
}

data class SourceDraft(
    val stableSourceId: SourceIdentityId,
    val revisionId: SourceRecordId,
    val acquisitionMode: AcquisitionMode,
    val authorRole: SourceAuthorRole,
    val interactionId: InteractionId?,
    val originalContent: OriginalSourceContent,
    val eventTime: EventTime,
    val reportTime: ReportTime,
    val privacy: SourcePrivacy = SourcePrivacy.ELIGIBLE,
    val metadata: Map<String, String> = emptyMap(),
) : Serializable

data class HypothesisDraft(
    val id: HypothesisId,
    val subject: AssertionSubject,
    val predicate: AssertionPredicate,
    val proposedValue: AssertionValue,
    val status: HypothesisStatus,
    val rationale: String,
) : Serializable

data class EvidenceBundle(
    val assertions: List<EvidenceAssertion> = emptyList(),
    val entities: List<LifeEntity> = emptyList(),
    val contradictions: List<ContradictionRelation> = emptyList(),
    val supersessions: List<SupersessionRelation> = emptyList(),
    val hypothesisDrafts: List<HypothesisDraft> = emptyList(),
    val hypothesisDependencies: List<HypothesisDependency> = emptyList(),
    val identityLinks: List<EntityIdentityLink> = emptyList(),
    val coverageTopics: List<CoverageTopic> = emptyList(),
) : Serializable {
    init {
        require(
            assertions.isNotEmpty() || entities.isNotEmpty() || contradictions.isNotEmpty() ||
                supersessions.isNotEmpty() || hypothesisDrafts.isNotEmpty() ||
                hypothesisDependencies.isNotEmpty() || identityLinks.isNotEmpty() || coverageTopics.isNotEmpty(),
        ) { "Evidence bundle cannot be empty" }
    }
}

sealed interface LongitudinalWriteOperation : Serializable {
    val type: AdmissionOperationType

    data class AdmitSource(val source: SourceDraft) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.ADMIT_SOURCE
    }

    data class AppendSourceRevision(
        val stableSourceId: SourceIdentityId,
        val priorRevisionId: SourceRecordId,
        val newRevisionId: SourceRecordId,
        val originalContent: OriginalSourceContent,
        val reportTime: ReportTime,
    ) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.APPEND_SOURCE_REVISION
    }

    data class AdmitEvidenceBundle(val bundle: EvidenceBundle) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.ADMIT_EVIDENCE_BUNDLE
    }

    data class RecordUserCorrection(
        val correctionSource: SourceDraft,
        val correctingAssertion: EvidenceAssertion,
        val correction: CorrectionRelation,
        val supersession: SupersessionRelation? = null,
    ) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.RECORD_USER_CORRECTION
    }

    data class RecordSupersession(val relation: SupersessionRelation) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.RECORD_SUPERSESSION
    }

    data class RecordContradiction(val relation: ContradictionRelation) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.RECORD_CONTRADICTION
    }

    data class ReviseIdentityLink(
        val decision: EntityIdentityLink,
        val priorDecisionId: IdentityLinkId? = null,
    ) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.REVISE_IDENTITY_LINK
    }

    data class ChangeCoverage(val topic: CoverageTopic) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.CHANGE_COVERAGE
    }

    data class ChangePrivacy(
        val stableSourceId: SourceIdentityId,
        val privacy: SourcePrivacy,
    ) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.CHANGE_PRIVACY
    }

    data class RetireClaim(val claim: ClaimReference) : LongitudinalWriteOperation {
        override val type = AdmissionOperationType.RETIRE_CLAIM
    }
}

data class LongitudinalAdmissionRequest(
    val requestId: AdmissionRequestId,
    val idempotencyKey: IdempotencyKey,
    val expectedStoreRevision: Long,
    val actor: AdmissionActor,
    val origin: AdmissionOrigin,
    val policyVersion: AdmissionPolicyVersion,
    val classification: StoreDataClassification,
    val operation: LongitudinalWriteOperation,
) : Serializable {
    init { require(expectedStoreRevision >= 0) }
}

enum class StoredObjectType {
    SOURCE_REVISION,
    ASSERTION,
    ENTITY,
    HYPOTHESIS,
    CONTRADICTION,
    CORRECTION,
    SUPERSESSION,
    HYPOTHESIS_DEPENDENCY,
    IDENTITY_DECISION,
    COVERAGE,
}

data class LongitudinalObjectRef(val type: StoredObjectType, val stableId: String) : Serializable {
    init { require(stableId.isNotBlank()) }
}

enum class LongitudinalLifecycleStatus {
    ACTIVE,
    CONTESTED,
    SUPERSEDED,
    RETIRED,
    REVIEW_REQUIRED,
    DEPENDENCY_BLOCKED,
    PRIVATE_INELIGIBLE,
}

data class LifecycleState(
    val status: LongitudinalLifecycleStatus,
    val eligibleForOrdinaryUse: Boolean,
    val causeCode: String,
    val changedAtRevision: Long,
) : Serializable

data class LongitudinalAggregateState(
    val storeRevision: Long = 0,
    val snapshot: com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot =
        com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot(),
    val sourcePrivacy: Map<SourceIdentityId, SourcePrivacy> = emptyMap(),
    val lifecycle: Map<LongitudinalObjectRef, LifecycleState> = emptyMap(),
    val currentIdentityDecisionByPair: Map<String, IdentityLinkId> = emptyMap(),
) : Serializable {
    fun sourceHistory(id: SourceIdentityId) = snapshot.sources.filter { it.stableSourceId == id }.sortedBy { it.provenance.sourceRevision }
    fun currentSource(id: SourceIdentityId) = sourceHistory(id).lastOrNull()
    fun isSourceEligible(id: SourceIdentityId) = sourcePrivacy[id] != SourcePrivacy.PRIVATE
    fun lifecycleOf(reference: LongitudinalObjectRef) = lifecycle[reference]
}

data class PlannedLongitudinalMutation(
    val state: LongitudinalAggregateState,
    val affectedStableIds: List<String>,
    val decisionTrace: List<String>,
) : Serializable

sealed interface AdmissionPlanResult {
    data class Accepted(val mutation: PlannedLongitudinalMutation) : AdmissionPlanResult
    data class Rejected(val disposition: AdmissionDisposition, val reasonCodes: List<String>) : AdmissionPlanResult
}

fun assertionRef(id: AssertionId) = LongitudinalObjectRef(StoredObjectType.ASSERTION, id.value)
fun hypothesisRef(id: HypothesisId) = LongitudinalObjectRef(StoredObjectType.HYPOTHESIS, id.value)
