package com.conundrum.thomas.v2.runtime

import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.languageevidence.stateformation.FormedLongitudinalState
import com.conundrum.thomas.v2.languagerenderer.GovernedRenderResult
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyPlan
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import java.time.Instant

const val CT_V2_15_RUNTIME_POLICY_VERSION = "ct-v2-15.android-runtime.v1"

enum class ProductionThomasMode { JOURNAL, BIOGRAPHER, THERAPY }
enum class ProductionInputOrigin { TYPED, SPEECH_TRANSCRIPT }
enum class ProductionTurnPrivacy { ELIGIBLE, PRIVATE }

/**
 * Explicit current-turn safety declaration. No historical text, renderer, or lexical matcher can
 * manufacture one of these values.
 */
enum class TherapySafetyDeclaration {
    ORDINARY_NON_EMERGENCY_ADULT_CONTEXT,
    CURRENT_EMERGENCY,
    UNSPECIFIED,
}

data class ProductionTurnRequest(
    val clientTurnIndex: Long,
    val mode: ProductionThomasMode,
    val committedText: String,
    val inputOrigin: ProductionInputOrigin = ProductionInputOrigin.TYPED,
    val privacy: ProductionTurnPrivacy = ProductionTurnPrivacy.ELIGIBLE,
    val journalResponsePreference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
    val requestedTherapySupport: RequestedOrdinarySupport = RequestedOrdinarySupport.LISTEN,
    val therapyMemoryIntent: TherapyMemoryIntent = TherapyMemoryIntent.ORDINARY,
    val therapySafetyDeclaration: TherapySafetyDeclaration =
        TherapySafetyDeclaration.ORDINARY_NON_EMERGENCY_ADULT_CONTEXT,
    val committedAt: Instant,
) {
    init {
        require(clientTurnIndex > 0)
        require(committedText.length <= 4_096)
    }
}

enum class ProductionTurnDisposition {
    COMPLETED,
    NO_RESPONSE,
    REJECTED_BLANK,
    REJECTED_BUSY,
    SOURCE_CAPTURE_FAILED,
    SAFETY_PREEMPTED,
    GOVERNED_POLICY_NO_OUTPUT,
    RENDERING_UNAVAILABLE,
    PERSISTENCE_UNAVAILABLE,
}

data class ProductionAssistantArtifact(
    val turnIdentity: String,
    val mode: ProductionThomasMode,
    val text: String,
    val renderDigest: String,
    val surfacedMemoryIds: List<String>,
) {
    init {
        require(turnIdentity.isNotBlank() && text.isNotBlank())
        require(renderDigest.matches(Regex("^[0-9a-f]{64}$")))
    }
}

data class ProductionTurnResult(
    val disposition: ProductionTurnDisposition,
    val turnIdentity: String,
    val committedSourceId: SourceIdentityId?,
    val assistantArtifact: ProductionAssistantArtifact?,
    val renderResult: GovernedRenderResult?,
    val therapyPlan: LongitudinalTherapyPlan?,
    val resultingStoreRevision: Long,
    val reasonCodes: List<String>,
) {
    init {
        require(turnIdentity.isNotBlank())
        require(resultingStoreRevision >= 0)
        require(reasonCodes.isNotEmpty())
        require((assistantArtifact != null) == (assistantArtifact?.text?.isNotBlank() == true))
    }
}

data class ProductionBiographerPrompt(
    val text: String,
    val renderResult: GovernedRenderResult,
    val targetId: String?,
)

data class ProductionRuntimeSnapshot(
    val storeRevision: Long,
    val logicalStateDigest: String,
    val formedState: FormedLongitudinalState,
    val rendererCallCount: Long,
    val acceptedAssistantArtifactCount: Long,
    val runtimePolicyVersion: String = CT_V2_15_RUNTIME_POLICY_VERSION,
)

enum class ProductionRuntimeAvailability {
    READY,
    PERSISTENCE_UNAVAILABLE,
    CLOSED,
}

data class ProductionSourceSummary(
    val stableSourceId: SourceIdentityId,
    val acquisitionMode: AcquisitionMode,
    val reportTime: Instant,
    val revisionCount: Int,
    val eligibleForOrdinaryUse: Boolean,
    val lifecycleStatus: LongitudinalLifecycleStatus?,
)

enum class ProductionSourceLifecycleAction { MAKE_PRIVATE, REQUEST_ELIGIBLE_REVIEW, DELETE }

data class ProductionSourceLifecycleResult(
    val sourceId: SourceIdentityId,
    val action: ProductionSourceLifecycleAction,
    val admissionDisposition: AdmissionDisposition,
    val resultingStoreRevision: Long,
    val reasonCodes: List<String>,
) {
    val accepted: Boolean
        get() = admissionDisposition == AdmissionDisposition.ACCEPTED ||
            admissionDisposition == AdmissionDisposition.IDEMPOTENT_REPLAY
}

data class ProductionSourceRevisionResult(
    val sourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId?,
    val admissionDisposition: AdmissionDisposition,
    val resultingStoreRevision: Long,
    val evidenceProcessingDisposition: String?,
    val reasonCodes: List<String>,
) {
    val accepted: Boolean
        get() = admissionDisposition == AdmissionDisposition.ACCEPTED ||
            admissionDisposition == AdmissionDisposition.IDEMPOTENT_REPLAY
}
