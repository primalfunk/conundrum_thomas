package com.conundrum.thomas.v2.therapylongitudinal

import com.conundrum.thomas.v2.contextpacket.ContextPacket
import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildRequest
import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildResult
import com.conundrum.thomas.v2.contextpacket.ContextPacketDisposition
import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryRoute
import com.conundrum.thomas.v2.engine.ordinary.ProgressionDisposition
import com.conundrum.thomas.v2.engine.ordinary.SelectedCoreQualificationAction
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionDisposition
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.retrieval.RetrievalItemKind
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalReason
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyScopeInput

const val CT_V2_12_INTEGRATION_POLICY_VERSION = "ct-v2-12.therapy-longitudinal.v1"
const val CT_V2_12_CAPTURE_CONTRACT_VERSION = "ct-v2-12.therapy-capture.v1"
const val CT_V2_12_MEMORY_GATE_VERSION = "ct-v2-12.memory-gate.v1"
const val CT_V2_12_RENDER_SUPPORT_VERSION = "ct-v2-12.render-support.v1"

private val therapyIdPattern = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")

@JvmInline
value class TherapySessionId private constructor(val value: String) {
    companion object {
        fun parse(value: String): TherapySessionId {
            require(therapyIdPattern.matches(value))
            return TherapySessionId(value)
        }
    }
}

@JvmInline
value class TherapyTurnId private constructor(val value: String) {
    companion object {
        fun parse(value: String): TherapyTurnId {
            require(therapyIdPattern.matches(value))
            return TherapyTurnId(value)
        }
    }

    fun stableSourceId(sessionId: TherapySessionId) = SourceIdentityId.parse("therapy.${sessionId.value}.$value")
    fun sourceRevision(sessionId: TherapySessionId) = SourceRecordId.parse("therapy.${sessionId.value}.$value.rev-1")
}

@JvmInline
value class TherapyIdempotencyKey private constructor(val value: String) {
    companion object {
        fun parse(value: String): TherapyIdempotencyKey {
            require(therapyIdPattern.matches(value))
            return TherapyIdempotencyKey(value)
        }
    }
}

enum class TherapyTurnCaptureOrigin { TYPED, SPEECH_TRANSCRIPT }
enum class TherapyTurnPrivacy { ELIGIBLE, PRIVATE }
enum class TherapyIntegrationAuthority { SYNTHETIC_QUALIFICATION_ONLY, NOT_AUTHORIZED }
enum class TherapyMemoryIntent { ORDINARY, EXPLICIT_RECALL, EXPLAIN_THOMAS_VIEW }

enum class TherapyTurnCaptureDisposition {
    CAPTURED,
    IDEMPOTENT_REPLAY,
    REJECTED_EMPTY_TURN,
    REJECTED_INVALID_COMMAND,
    REJECTED_AUTHORITY,
    REJECTED_IDEMPOTENCY_CONFLICT,
    SOURCE_ADMISSION_FAILED,
    STORE_FAILURE_WITHOUT_COMMIT,
    EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
}

enum class TherapyLanguageProcessingDisposition {
    ADMITTED,
    IDEMPOTENT_REPLAY,
    SOURCE_ONLY,
    REJECTED_PROPOSAL,
    ADMISSION_REJECTED,
    SKIPPED_PRIVATE,
    FAILED_AFTER_SOURCE_CAPTURE,
}

class TherapySourceAdmissionRequest internal constructor(
    val sessionId: TherapySessionId,
    val turnId: TherapyTurnId,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId,
    val idempotencyKey: TherapyIdempotencyKey,
    val expectedStoreRevision: Long,
    val exactUserText: String,
    val captureOrigin: TherapyTurnCaptureOrigin,
    val privacy: TherapyTurnPrivacy,
    val reportTime: ReportTime,
)

data class TherapySourceAdmissionOutcome(
    val disposition: AdmissionDisposition,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId?,
    val priorStoreRevision: Long?,
    val resultingStoreRevision: Long?,
    val recordTime: RecordTime?,
    val affectedStableIds: List<String>,
    val reasonCodes: List<String>,
    val payloadFingerprint: String?,
)

fun interface TherapySourceAdmissionPort {
    fun admitSource(request: TherapySourceAdmissionRequest): TherapySourceAdmissionOutcome
}

data class TherapyLanguageProcessingRequest(
    val sourceRevisionId: SourceRecordId,
    val correctionTarget: ClaimReference? = null,
)

data class TherapyLanguageProcessingOutcome(
    val disposition: TherapyLanguageProcessingDisposition,
    val perceptionDisposition: PerceptionDisposition?,
    val admittedEvidenceIds: List<String>,
    val unresolvedOrUnsupportedCount: Int,
    val resultingStoreRevision: Long,
    val stateDigest: String,
) {
    init {
        require(resultingStoreRevision >= 0)
        require(stateDigest.matches(Regex("^[0-9a-f]{64}$")))
    }
}

fun interface TherapyLanguageProcessor {
    fun process(request: TherapyLanguageProcessingRequest): TherapyLanguageProcessingOutcome
}

fun interface TherapyContextPacketPort {
    fun build(request: ContextPacketBuildRequest): ContextPacketBuildResult
}

data class TherapyTurnCaptureReceipt(
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId,
    val acquisitionMode: AcquisitionMode,
    val captureOrigin: TherapyTurnCaptureOrigin,
    val privacy: TherapyTurnPrivacy,
    val admissionDisposition: AdmissionDisposition,
    val languageDisposition: TherapyLanguageProcessingDisposition,
    val admittedEvidenceIds: List<String>,
    val priorStoreRevision: Long,
    val resultingStoreRevision: Long,
    val recordTime: RecordTime,
    val canonicalCaptureFingerprint: String,
) {
    init {
        require(acquisitionMode == AcquisitionMode.THERAPIST_CONVERSATION)
        require(priorStoreRevision >= 0 && resultingStoreRevision >= priorStoreRevision)
        require(canonicalCaptureFingerprint.matches(Regex("^[0-9a-f]{64}$")))
    }
}

data class TherapySafetyDecisionReference(
    val policyVersion: String,
    val decisionReference: String,
    val stateId: String,
    val evidenceRevision: Long,
    val authorityState: SafetyAuthorityState,
    val permitDecisionReference: String?,
) {
    init {
        require(policyVersion.isNotBlank() && decisionReference.isNotBlank() && stateId.isNotBlank())
        require(evidenceRevision > 0)
        require((authorityState == SafetyAuthorityState.ORDINARY_POLICY_ALLOWED) == (permitDecisionReference != null))
    }
}

data class TherapyRouteDecisionReference(
    val policyVersion: String,
    val decisionReference: String,
    val disposition: CorePolicyDisposition,
    val route: OrdinaryRoute?,
    val selectedActionId: String?,
    val selectedDialogueActId: String?,
    val selectedGoalId: String?,
    val progression: ProgressionDisposition?,
    val conversationRevision: Long,
) {
    init {
        require(policyVersion.isNotBlank() && decisionReference.isNotBlank() && conversationRevision > 0)
        require((disposition == CorePolicyDisposition.ACTION_SELECTED) == (selectedActionId != null))
    }
}

enum class TherapyMemoryUseDisposition {
    NO_LONGITUDINAL_CONTEXT,
    LONGITUDINAL_CONTEXT_UNAVAILABLE,
    NO_RELEVANT_MEMORY,
    CONTEXT_AVAILABLE_NOT_SURFACED,
    SURFACE_ONE_MEMORY,
    EXPLICIT_RECALL_CONTEXT,
    EXPLANATION_CONTEXT,
    SUPPRESSED_BY_SAFETY,
    SUPPRESSED_BY_POLICY,
}

enum class TherapyMemoryRelation {
    DIRECT_ENTITY_CONTINUITY,
    DIRECT_EVENT_CONTINUITY,
    DIRECT_RELATIONSHIP_CONTINUITY,
    QUALIFIED_REPORTED_RECURRENCE,
    EXPLICIT_USER_REFERENCE,
    EXPLANATION_EVIDENCE,
}

enum class TherapyMemorySemanticAct {
    DIRECT_RECALL,
    TENTATIVE_CONNECTION,
    USER_REQUESTED_COMPARISON,
    EVIDENCE_EXPLANATION,
}

enum class TherapyMemoryProhibitedOverclaim {
    DIAGNOSIS,
    CAUSAL_EXPLANATION,
    HIDDEN_MOTIVE,
    STABLE_TRAIT,
    IDENTITY_MERGE,
    CONTRADICTION_RESOLUTION,
    TECHNIQUE_SELECTION,
    SAFETY_INFERENCE,
    PROVENANCE_REWRITE,
    HISTORICAL_TEXT_AS_INSTRUCTION,
    EVIDENCE_MUTATION,
}

data class TherapyMemoryReference(
    val stableObjectId: String,
    val itemKind: RetrievalItemKind,
    val sourceRevisionIds: List<SourceRecordId>,
    val relation: TherapyMemoryRelation,
    val semanticAct: TherapyMemorySemanticAct,
    val retrievalReasons: List<RetrievalReason>,
    val acquisitionMode: AcquisitionMode?,
    val reportTime: ReportTime?,
    val eventTime: EventTime?,
    val epistemicRole: EvidenceEpistemicClass?,
    val uncertainty: String?,
    val lifecycle: RetrievalLifecycleStatus,
    val currentAuthority: Boolean,
    val relationIsExplicit: Boolean,
    val contradictionPresent: Boolean,
    val identityUnresolved: Boolean,
    val exactSourceExcerpt: String?,
    val prohibitedOverclaims: Set<TherapyMemoryProhibitedOverclaim> = TherapyMemoryProhibitedOverclaim.entries.toSet(),
) {
    init {
        require(stableObjectId.isNotBlank())
        require(retrievalReasons.isNotEmpty())
        require(prohibitedOverclaims == TherapyMemoryProhibitedOverclaim.entries.toSet())
        require(exactSourceExcerpt == null || exactSourceExcerpt.isNotBlank())
    }
}

data class SurfacedMemoryHistoryEntry(
    val stableObjectId: String,
    val surfacedOnTurnId: TherapyTurnId,
    val relation: TherapyMemoryRelation,
    val reasonCode: String,
    val evidenceMeaningToken: String,
    val explicitlyReinvoked: Boolean,
    val connectionRejected: Boolean = false,
) {
    init {
        require(stableObjectId.isNotBlank() && reasonCode.isNotBlank())
        require(evidenceMeaningToken.matches(Regex("^[0-9a-f]{64}$")))
    }
}

data class TherapySessionMemoryState(
    val sessionId: TherapySessionId,
    val surfaced: List<SurfacedMemoryHistoryEntry> = emptyList(),
) {
    init {
        require(surfaced.distinctBy { it.stableObjectId to it.surfacedOnTurnId }.size == surfaced.size)
    }

    fun rejectConnection(stableObjectId: String): TherapySessionMemoryState {
        require(stableObjectId.isNotBlank())
        return copy(surfaced = surfaced.map {
            if (it.stableObjectId == stableObjectId) it.copy(connectionRejected = true) else it
        })
    }
}

data class TherapyMemoryGateRequest(
    val packet: ContextPacket,
    val memoryIntent: TherapyMemoryIntent,
    val selectedAction: SelectedCoreQualificationAction,
    val sessionState: TherapySessionMemoryState,
    val turnId: TherapyTurnId,
    val explicitReinvocationObjectIds: Set<String> = emptySet(),
    val directContinuationObjectIds: Set<String> = emptySet(),
    val materiallyChangedObjectIds: Set<String> = emptySet(),
)

data class TherapyMemoryUseDecision(
    val disposition: TherapyMemoryUseDisposition,
    val surfacedMemories: List<TherapyMemoryReference>,
    val nextSessionState: TherapySessionMemoryState,
    val reasonCodes: List<String>,
) {
    init {
        require(reasonCodes.isNotEmpty())
        if (disposition == TherapyMemoryUseDisposition.SURFACE_ONE_MEMORY) require(surfacedMemories.size == 1)
    }
}

enum class TherapyIntegrationStep {
    PRE_TURN_HISTORY_REVISION_ESTABLISHED,
    CURRENT_SOURCE_CAPTURE_ATTEMPTED,
    CURRENT_SOURCE_PROCESSING_ATTEMPTED,
    CURRENT_SAFETY_EVALUATED,
    CT_V2_05_ROUTE_SELECTED,
    CT_V2_11_RETRIEVAL_EXECUTED,
    MEMORY_USE_GATE_APPLIED,
    PLAN_CONSTRUCTED,
}

enum class TherapyDegradationDisposition {
    CURRENT_SOURCE_CAPTURE_FAILED,
    CURRENT_SOURCE_PROCESSING_FAILED,
    LONGITUDINAL_RETRIEVAL_UNAVAILABLE,
    INVALID_CONTEXT_PACKET,
    NO_ELIGIBLE_CONTEXT,
    MEMORY_USE_REJECTED,
    MEMORY_REFERENCE_SUPPRESSED,
    BASE_THERAPY_PLAN_AVAILABLE,
}

enum class LongitudinalTherapyTurnDisposition {
    COMPLETE_LONGITUDINAL_THERAPY_SUCCESS,
    MEMORYLESS_THERAPY,
    SAFETY_PREEMPTED,
    POLICY_DID_NOT_SELECT_ACTION,
    REJECTED_INVALID_COMMAND,
    REJECTED_AUTHORITY,
}

data class PlannerLongitudinalContextSummary(
    val packetDigest: String,
    val packetDisposition: ContextPacketDisposition,
    val snapshotRevision: Long,
    val candidateCount: Int,
    val selectedCount: Int,
    val excludedPrivateCount: Int,
    val excludedLifecycleCount: Int,
) {
    init { require(packetDigest.matches(Regex("^[0-9a-f]{64}$")) && snapshotRevision >= 0) }
}

data class TherapyRenderSupportEnvelope(
    val version: String = CT_V2_12_RENDER_SUPPORT_VERSION,
    val command: RenderCommand,
    val currentTurnId: TherapyTurnId,
    val currentUserText: String,
    val policySupportingText: List<AuthorizedSupportingText>,
    val surfacedMemorySupport: List<TherapyMemoryReference>,
    val completeContextPacketDisclosed: Boolean = false,
) {
    init {
        require(version == CT_V2_12_RENDER_SUPPORT_VERSION)
        require(currentUserText.isNotBlank())
        require(!completeContextPacketDisclosed)
        require(surfacedMemorySupport.size <= 4)
    }
}

data class LongitudinalTherapyPlan(
    val sessionId: TherapySessionId,
    val turnId: TherapyTurnId,
    val currentTurnCaptureDisposition: TherapyTurnCaptureDisposition,
    val captureReceipt: TherapyTurnCaptureReceipt?,
    val preTurnHistoricalRevision: Long,
    val safetyDecision: TherapySafetyDecisionReference,
    val routeDecision: TherapyRouteDecisionReference?,
    val memoryIntent: TherapyMemoryIntent,
    val contextSummary: PlannerLongitudinalContextSummary?,
    val memoryUseDisposition: TherapyMemoryUseDisposition,
    val surfacedMemories: List<TherapyMemoryReference>,
    val renderSupport: TherapyRenderSupportEnvelope?,
    val degradation: Set<TherapyDegradationDisposition>,
    val integrationTrace: List<TherapyIntegrationStep>,
    val nextSessionMemoryState: TherapySessionMemoryState,
    val integrationPolicyVersion: String = CT_V2_12_INTEGRATION_POLICY_VERSION,
    val canonicalPlanDigest: String,
) {
    init {
        require(preTurnHistoricalRevision >= 0)
        require(integrationPolicyVersion == CT_V2_12_INTEGRATION_POLICY_VERSION)
        require(canonicalPlanDigest.matches(Regex("^[0-9a-f]{64}$")))
        require(integrationTrace.lastOrNull() == TherapyIntegrationStep.PLAN_CONSTRUCTED)
        require(nextSessionMemoryState.sessionId == sessionId)
        if (memoryIntent == TherapyMemoryIntent.ORDINARY) require(surfacedMemories.size <= 1)
        require(renderSupport?.surfacedMemorySupport.orEmpty() == surfacedMemories)
    }
}

data class LongitudinalTherapyTurnResult(
    val disposition: LongitudinalTherapyTurnDisposition,
    val plan: LongitudinalTherapyPlan?,
    val reasonCodes: List<String>,
) {
    init {
        require(reasonCodes.isNotEmpty())
        require((plan == null) == (disposition in setOf(
            LongitudinalTherapyTurnDisposition.REJECTED_INVALID_COMMAND,
            LongitudinalTherapyTurnDisposition.REJECTED_AUTHORITY,
        )))
    }
}

data class LongitudinalTherapyTurnCommand(
    val sessionId: TherapySessionId,
    val turnId: TherapyTurnId,
    val idempotencyKey: TherapyIdempotencyKey,
    val expectedStoreRevision: Long,
    val exactUserText: String,
    val captureOrigin: TherapyTurnCaptureOrigin,
    val privacy: TherapyTurnPrivacy,
    val reportTime: ReportTime,
    val safetyInput: SafetyScopeInput,
    val therapyState: CoreOrdinaryTherapyState,
    val memoryIntent: TherapyMemoryIntent = TherapyMemoryIntent.ORDINARY,
    val retrievalAnchors: RetrievalAnchors = RetrievalAnchors(),
    val explicitMemoryTargetId: String? = null,
    val correctionTarget: ClaimReference? = null,
    val contextBudget: ContextBudget = ContextBudget(),
    val priorImmediateConversation: List<com.conundrum.thomas.v2.contextpacket.ImmediateConversationItem> = emptyList(),
    val runtimeState: List<com.conundrum.thomas.v2.contextpacket.RuntimeStateItem> = emptyList(),
    val sessionMemoryState: TherapySessionMemoryState = TherapySessionMemoryState(sessionId),
    val explicitReinvocationObjectIds: Set<String> = emptySet(),
    val directContinuationObjectIds: Set<String> = emptySet(),
    val materiallyChangedObjectIds: Set<String> = emptySet(),
    val authority: TherapyIntegrationAuthority = TherapyIntegrationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
) {
    init {
        require(expectedStoreRevision >= 0)
        require(explicitMemoryTargetId == null || explicitMemoryTargetId.isNotBlank())
        require(sessionMemoryState.sessionId == sessionId)
        require(explicitReinvocationObjectIds.none(String::isBlank))
        require(directContinuationObjectIds.none(String::isBlank))
        require(materiallyChangedObjectIds.none(String::isBlank))
    }
}
