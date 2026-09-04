package com.conundrum.thomas.v2.contextpacket

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.RetrievalExclusions
import com.conundrum.thomas.v2.retrieval.RetrievalIntent
import com.conundrum.thomas.v2.retrieval.RetrievalItemKind
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalMode
import com.conundrum.thomas.v2.retrieval.RetrievalReason
import com.conundrum.thomas.v2.retrieval.RetrievalRequest
import java.time.Instant

const val CT_V2_11_CONTEXT_PACKET_VERSION = "ct-v2-11.context-packet.v1"

enum class ModeAuthorityState {
    JOURNAL_CAPTURE_ONLY,
    BIOGRAPHER_INVESTIGATION_ONLY,
    ORDINARY_THERAPY_QUALIFICATION_ONLY,
}

data class ModeAuthorityContract(
    val activeMode: RetrievalMode,
    val state: ModeAuthorityState,
    val contractVersion: String,
    val longitudinalMemoryCannotAlterMode: Boolean = true,
) {
    init {
        require(contractVersion.isNotBlank())
        require(longitudinalMemoryCannotAlterMode)
        require(
            when (activeMode) {
                RetrievalMode.JOURNAL -> state == ModeAuthorityState.JOURNAL_CAPTURE_ONLY
                RetrievalMode.BIOGRAPHER -> state == ModeAuthorityState.BIOGRAPHER_INVESTIGATION_ONLY
                RetrievalMode.THERAPY -> state == ModeAuthorityState.ORDINARY_THERAPY_QUALIFICATION_ONLY
            },
        )
    }
}

data class SafetyConstraintRef(
    val constraintId: String,
    val policyVersion: String,
    val authorityState: String,
    val suppliedByHigherAuthority: Boolean = true,
) {
    init {
        require(constraintId.isNotBlank() && policyVersion.isNotBlank() && authorityState.isNotBlank())
        require(suppliedByHigherAuthority) { "CT-V2-11 cannot infer safety state" }
    }
}

enum class ImmediateTurnRole { USER, ASSISTANT }
enum class ContextTextAuthority { CURRENT_USER_DATA, ASSISTANT_CONTINUITY_DATA, USER_SOURCE_EXCERPT, RUNTIME_DATA }

data class ImmediateConversationItem(
    val turnId: String,
    val role: ImmediateTurnRole,
    val content: String,
    val authority: ContextTextAuthority = if (role == ImmediateTurnRole.USER) {
        ContextTextAuthority.CURRENT_USER_DATA
    } else {
        ContextTextAuthority.ASSISTANT_CONTINUITY_DATA
    },
) {
    init {
        require(turnId.isNotBlank() && content.isNotBlank())
        require(
            (role == ImmediateTurnRole.USER && authority == ContextTextAuthority.CURRENT_USER_DATA) ||
                (role == ImmediateTurnRole.ASSISTANT && authority == ContextTextAuthority.ASSISTANT_CONTINUITY_DATA),
        )
    }
}

data class RuntimeStateItem(
    val stateId: String,
    val conceptId: String,
    val value: String,
    val observedAt: Instant,
    val epistemicRole: String,
    val authority: ContextTextAuthority = ContextTextAuthority.RUNTIME_DATA,
) {
    init {
        require(stateId.isNotBlank() && conceptId.isNotBlank() && value.isNotBlank() && epistemicRole.isNotBlank())
        require(authority == ContextTextAuthority.RUNTIME_DATA)
    }
}

sealed interface PacketDataValue {
    data class TextData(val value: String) : PacketDataValue { init { require(value.isNotBlank()) } }
    data class ConceptData(val conceptId: String) : PacketDataValue { init { require(conceptId.isNotBlank()) } }
    data class EntityData(val entityIds: List<LifeEntityId>) : PacketDataValue { init { require(entityIds.isNotEmpty()) } }
    data class TimeData(val value: EventTime) : PacketDataValue
    data class BooleanData(val value: Boolean) : PacketDataValue
    data class IntegerData(val value: Long) : PacketDataValue
    data class OmittedText(val reasonCode: String) : PacketDataValue { init { require(reasonCode.isNotBlank()) } }
}

data class PacketLongitudinalItem(
    val kind: RetrievalItemKind,
    val stableId: String,
    val lifecycle: RetrievalLifecycleStatus,
    val currentAuthority: Boolean,
    val epistemicRole: EvidenceEpistemicClass?,
    val uncertainty: String?,
    val eventTime: EventTime?,
    val reportTime: ReportTime?,
    val acquisitionMode: AcquisitionMode?,
    val sourceRevisionIds: List<SourceRecordId>,
    val entityIds: List<LifeEntityId>,
    val relatedStableIds: List<String>,
    val unresolvedIdentity: Boolean,
    val dataValue: PacketDataValue?,
    val retrievedBecause: List<RetrievalReason>,
) {
    init { require(stableId.isNotBlank() && retrievedBecause.isNotEmpty()) }
}

data class ContextSourceExcerpt(
    val itemStableId: String,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId,
    val acquisitionMode: AcquisitionMode,
    val reportTime: ReportTime,
    val eventTime: EventTime,
    val startOffsetInclusive: Int,
    val endOffsetExclusive: Int,
    val sourceRevisionFingerprint: String,
    val epistemicRole: EvidenceEpistemicClass,
    val lifecycle: RetrievalLifecycleStatus,
    val exactText: String,
    val truncatedAtEnd: Boolean,
    val textAuthority: ContextTextAuthority = ContextTextAuthority.USER_SOURCE_EXCERPT,
) {
    init {
        require(itemStableId.isNotBlank())
        require(startOffsetInclusive >= 0 && endOffsetExclusive > startOffsetInclusive)
        require(endOffsetExclusive - startOffsetInclusive == exactText.length)
        require(sourceRevisionFingerprint.matches(Regex("^[0-9a-f]{64}$")))
        require(textAuthority == ContextTextAuthority.USER_SOURCE_EXCERPT)
    }
}

data class PacketAuthorityLayer(
    val modeContract: ModeAuthorityContract,
    val retrievedTextHasInstructionAuthority: Boolean = false,
) {
    init { require(!retrievedTextHasInstructionAuthority) }
}

data class PacketSafetyLayer(
    val constraints: List<SafetyConstraintRef>,
    val inferredByRetrieval: Boolean = false,
) {
    init { require(!inferredByRetrieval) }
}

data class PacketImmediateLayer(val items: List<ImmediateConversationItem>)
data class PacketRuntimeLayer(val items: List<RuntimeStateItem>)
data class PacketLongitudinalLayer(val items: List<PacketLongitudinalItem>)
data class PacketExcerptLayer(val excerpts: List<ContextSourceExcerpt>)

data class ContextPacketMetadata(
    val packetVersion: String,
    val retrievalPolicyVersion: String,
    val storeRevision: Long,
    val intent: RetrievalIntent,
    val activeMode: RetrievalMode,
    val candidateCount: Int,
    val selectedCount: Int,
    val exclusions: RetrievalExclusions,
    val budget: ContextBudget,
    val totalTextCharacters: Int,
    val omittedLongitudinalTextCount: Int,
    val omittedExcerptCount: Int,
    val truncatedExcerptCount: Int,
    val maximumTraversalDepthUsed: Int,
    val packetDigest: String,
) {
    init {
        require(packetVersion == CT_V2_11_CONTEXT_PACKET_VERSION)
        require(storeRevision >= 0 && candidateCount >= 0 && selectedCount >= 0)
        require(totalTextCharacters in 0..budget.maximumTotalTextCharacters)
        require(maximumTraversalDepthUsed <= budget.maximumDependencyDepth)
        require(packetDigest.matches(Regex("^[0-9a-f]{64}$")))
    }
}

data class ContextPacket(
    val authority: PacketAuthorityLayer,
    val safety: PacketSafetyLayer,
    val immediate: PacketImmediateLayer,
    val runtime: PacketRuntimeLayer,
    val longitudinal: PacketLongitudinalLayer,
    val excerpts: PacketExcerptLayer,
    val metadata: ContextPacketMetadata,
) {
    init {
        require(authority.modeContract.activeMode == metadata.activeMode)
        require(longitudinal.items.size <= metadata.budget.maximumLongitudinalObjects)
        require(excerpts.excerpts.size <= metadata.budget.maximumSourceExcerpts)
        require(immediate.items.size <= metadata.budget.maximumImmediateItems)
        require(runtime.items.size <= metadata.budget.maximumRuntimeItems)
    }
}

data class ContextPacketBuildRequest(
    val retrieval: RetrievalRequest,
    val modeAuthority: ModeAuthorityContract,
    val safetyConstraints: List<SafetyConstraintRef> = emptyList(),
    val immediateConversation: List<ImmediateConversationItem> = emptyList(),
    val runtimeState: List<RuntimeStateItem> = emptyList(),
)

enum class ContextPacketDisposition { BUILT, EMPTY, REJECTED_AUTHORITY, REJECTED_INVALID_REQUEST, REVISION_UNAVAILABLE }

data class ContextPacketBuildResult(
    val disposition: ContextPacketDisposition,
    val packet: ContextPacket?,
    val reasonCodes: List<String>,
) {
    init { require((packet != null) == (disposition in setOf(ContextPacketDisposition.BUILT, ContextPacketDisposition.EMPTY))) }
}
