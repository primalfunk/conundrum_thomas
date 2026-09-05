package com.conundrum.thomas.v2.journal

import com.conundrum.thomas.v2.languageevidence.perception.LanguagePerceptionResult
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPolarity
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition

const val CT_V2_09_JOURNAL_CONTRACT_VERSION = "ct-v2-09.journal.v1"
private val journalIdPattern = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")

@JvmInline
value class JournalEntryId private constructor(val value: String) {
    companion object {
        fun parse(value: String): JournalEntryId {
            require(journalIdPattern.matches(value)) { "Journal entry ID must be a stable lowercase identifier" }
            return JournalEntryId(value)
        }
    }

    fun sourceIdentity(): SourceIdentityId = SourceIdentityId.parse("journal.$value")
    fun sourceRevision(revision: Int): SourceRecordId {
        require(revision > 0)
        return SourceRecordId.parse("journal.$value.rev-$revision")
    }
}

@JvmInline
value class JournalIdempotencyKey private constructor(val value: String) {
    companion object {
        fun parse(value: String): JournalIdempotencyKey {
            require(journalIdPattern.matches(value)) { "Journal idempotency key must be a stable lowercase identifier" }
            return JournalIdempotencyKey(value)
        }
    }
}

enum class JournalCaptureOrigin { TYPED, SPEECH_TRANSCRIPT }
enum class JournalResponsePreference { NO_RESPONSE, REFLECT, ASK_ONE_QUESTION }
enum class JournalPrivacy { ELIGIBLE, PRIVATE }
enum class JournalQualificationAuthority { SYNTHETIC_QUALIFICATION_ONLY, ANDROID_PRODUCTION, NOT_AUTHORIZED }

/** A draft has no evidence authority and is not accepted by JournalCaptureEngine.commit. */
data class JournalDraft(val text: String)

data class JournalCommitCommand(
    val entryId: JournalEntryId,
    val idempotencyKey: JournalIdempotencyKey,
    val expectedStoreRevision: Long,
    val committedText: String,
    val captureOrigin: JournalCaptureOrigin,
    val responsePreference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
    val privacy: JournalPrivacy = JournalPrivacy.ELIGIBLE,
    val reportTime: ReportTime,
    val authority: JournalQualificationAuthority = JournalQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
)

data class JournalRevisionCommand(
    val entryId: JournalEntryId,
    val idempotencyKey: JournalIdempotencyKey,
    val expectedStoreRevision: Long,
    val priorRevisionId: SourceRecordId,
    val newRevisionNumber: Int,
    val committedText: String,
    val responsePreference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
    val reportTime: ReportTime,
    val authority: JournalQualificationAuthority = JournalQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
)

data class JournalPrivacyChangeCommand(
    val entryId: JournalEntryId,
    val idempotencyKey: JournalIdempotencyKey,
    val expectedStoreRevision: Long,
    val privacy: JournalPrivacy,
    val authority: JournalQualificationAuthority = JournalQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
)

enum class JournalCaptureDisposition {
    CAPTURED,
    IDEMPOTENT_REPLAY,
    REJECTED_EMPTY_ENTRY,
    REJECTED_INVALID_COMMAND,
    REJECTED_AUTHORITY,
    REJECTED_IDEMPOTENCY_CONFLICT,
    REJECTED_PRIVACY_STATE,
    SOURCE_ADMISSION_FAILED,
    EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
    RESPONSE_PLANNING_FAILED_AFTER_CAPTURE,
    STORE_FAILURE_WITHOUT_COMMIT,
}

enum class JournalLanguageProcessingDisposition {
    ADMITTED,
    IDEMPOTENT_REPLAY,
    SOURCE_ONLY,
    REJECTED_PROPOSAL,
    ADMISSION_REJECTED,
    SKIPPED_PRIVATE,
    FAILED_AFTER_SOURCE_CAPTURE,
}

enum class JournalResponseIntentDisposition {
    NONE_SELECTED,
    REFLECTION_AUTHORIZED,
    QUESTION_AUTHORIZED,
    NO_SAFE_GROUNDED_RESPONSE,
    PRIVATE_ENTRY_SUPPRESSED,
    NOT_REACHED,
    PLANNING_FAILED,
}

enum class JournalResponseSemanticAct { NONE, BRIEF_REFLECTION, ONE_GROUNDED_QUESTION }

enum class JournalProhibitedResponseAct {
    DIAGNOSIS,
    PSYCHOLOGICAL_FORMULATION,
    THERAPY_TECHNIQUE,
    COGNITIVE_CHALLENGE,
    HIDDEN_MOTIVE_INFERENCE,
    HISTORICAL_RETRIEVAL,
    BIOGRAPHER_GAP_PURSUIT,
    MULTIPLE_QUESTIONS,
    DIRECTIVE_ADVICE,
    EVIDENCE_MUTATION,
}

data class JournalResponseGrounding(
    val sourceRevisionId: SourceRecordId,
    val assertionId: AssertionId,
    val conceptId: PersonalConceptId,
    val epistemicClass: EvidenceEpistemicClass,
    val uncertainty: AssertionUncertainty,
    val polarity: AssertionPolarity,
)

data class JournalResponsePlan(
    val posture: JournalResponsePreference,
    val responsePermitted: Boolean,
    val semanticAct: JournalResponseSemanticAct,
    val grounding: JournalResponseGrounding,
    val maximumQuestionCount: Int,
    val preserveUserAttribution: Boolean,
    val preserveUncertainty: Boolean,
    val prohibitedActs: Set<JournalProhibitedResponseAct>,
    val reasonCode: String,
) {
    init {
        require(posture != JournalResponsePreference.NO_RESPONSE)
        require(responsePermitted)
        require(maximumQuestionCount in 0..1)
        require((semanticAct == JournalResponseSemanticAct.ONE_GROUNDED_QUESTION) == (maximumQuestionCount == 1))
        require(JournalProhibitedResponseAct.EVIDENCE_MUTATION in prohibitedActs)
        require(JournalProhibitedResponseAct.HISTORICAL_RETRIEVAL in prohibitedActs)
    }
}

class JournalSourceAdmissionRequest internal constructor(
    val entryId: JournalEntryId,
    val stableSourceId: SourceIdentityId,
    val revisionId: SourceRecordId,
    val idempotencyKey: JournalIdempotencyKey,
    val expectedStoreRevision: Long,
    val exactCommittedText: String,
    val captureOrigin: JournalCaptureOrigin,
    val privacy: JournalPrivacy,
    val reportTime: ReportTime,
)

class JournalSourceRevisionRequest internal constructor(
    val entryId: JournalEntryId,
    val stableSourceId: SourceIdentityId,
    val priorRevisionId: SourceRecordId,
    val newRevisionId: SourceRecordId,
    val idempotencyKey: JournalIdempotencyKey,
    val expectedStoreRevision: Long,
    val exactCommittedText: String,
    val reportTime: ReportTime,
)

class JournalPrivacyAdmissionRequest internal constructor(
    val stableSourceId: SourceIdentityId,
    val idempotencyKey: JournalIdempotencyKey,
    val expectedStoreRevision: Long,
    val privacy: JournalPrivacy,
)

data class JournalAdmissionOutcome(
    val disposition: AdmissionDisposition,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId?,
    val captureOrigin: JournalCaptureOrigin?,
    val privacy: JournalPrivacy?,
    val resultingStoreRevision: Long?,
    val affectedStableIds: List<String>,
    val reasonCodes: List<String>,
    val payloadFingerprint: String?,
)

interface JournalAdmissionPort {
    fun admitSource(request: JournalSourceAdmissionRequest): JournalAdmissionOutcome
    fun appendSourceRevision(request: JournalSourceRevisionRequest): JournalAdmissionOutcome
    fun changePrivacy(request: JournalPrivacyAdmissionRequest): JournalAdmissionOutcome
}

data class JournalLanguageProcessingOutcome(
    val disposition: JournalLanguageProcessingDisposition,
    val perception: LanguagePerceptionResult,
    val admittedEvidenceIds: List<String>,
    val unresolvedOrUnsupportedCount: Int,
    val resultingStoreRevision: Long,
    val stateDigest: String,
)

fun interface JournalLanguageProcessor {
    fun process(sourceRevisionId: SourceRecordId): JournalLanguageProcessingOutcome
}

data class JournalResponsePlanningOutcome(
    val disposition: JournalResponseIntentDisposition,
    val plan: JournalResponsePlan? = null,
) {
    init {
        require((plan != null) == (disposition in setOf(
            JournalResponseIntentDisposition.REFLECTION_AUTHORIZED,
            JournalResponseIntentDisposition.QUESTION_AUTHORIZED,
        )))
    }
}

fun interface JournalResponseIntentPlanner {
    fun plan(preference: JournalResponsePreference, perception: LanguagePerceptionResult): JournalResponsePlanningOutcome
}

class JournalCaptureReceipt internal constructor(
    val entryId: JournalEntryId,
    val stableSourceId: SourceIdentityId,
    val sourceRevisionId: SourceRecordId,
    val acquisitionMode: AcquisitionMode,
    val captureOrigin: JournalCaptureOrigin,
    val privacy: JournalPrivacy,
    val admissionDisposition: AdmissionDisposition,
    val languageProcessingDisposition: JournalLanguageProcessingDisposition,
    val admittedEvidenceIds: List<String>,
    val unresolvedOrUnsupportedCount: Int,
    val resultingStoreRevision: Long,
    val responsePreference: JournalResponsePreference,
    val responseIntentDisposition: JournalResponseIntentDisposition,
    val canonicalCaptureFingerprint: String,
)

class JournalCaptureResult internal constructor(
    val disposition: JournalCaptureDisposition,
    val receipt: JournalCaptureReceipt?,
    val responsePlan: JournalResponsePlan?,
    val reasonCodes: List<String>,
) {
    val sourceCaptured: Boolean get() = receipt != null
}

enum class JournalPrivacyChangeDisposition { CHANGED, IDEMPOTENT_REPLAY, REJECTED, STORE_FAILURE }

data class JournalPrivacyChangeResult(
    val disposition: JournalPrivacyChangeDisposition,
    val admissionDisposition: AdmissionDisposition,
    val resultingStoreRevision: Long?,
    val reasonCodes: List<String>,
)

enum class JournalResponseExecutionDisposition { NOT_REQUESTED, RENDERED, RENDERING_FAILED_AFTER_CAPTURE }

data class JournalResponseExecutionResult(
    val disposition: JournalResponseExecutionDisposition,
    val renderedText: String? = null,
    val reasonCode: String,
)

fun interface JournalResponseRenderer {
    fun render(plan: JournalResponsePlan): String
}
