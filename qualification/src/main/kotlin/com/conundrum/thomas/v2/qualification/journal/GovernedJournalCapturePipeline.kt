package com.conundrum.thomas.v2.qualification.journal

import com.conundrum.thomas.v2.journal.CT_V2_09_JOURNAL_CONTRACT_VERSION
import com.conundrum.thomas.v2.journal.JournalAdmissionOutcome
import com.conundrum.thomas.v2.journal.JournalAdmissionPort
import com.conundrum.thomas.v2.journal.JournalCaptureEngine
import com.conundrum.thomas.v2.journal.JournalCaptureOrigin
import com.conundrum.thomas.v2.journal.JournalCaptureResult
import com.conundrum.thomas.v2.journal.JournalCommitCommand
import com.conundrum.thomas.v2.journal.JournalLanguageProcessingDisposition
import com.conundrum.thomas.v2.journal.JournalLanguageProcessingOutcome
import com.conundrum.thomas.v2.journal.JournalLanguageProcessor
import com.conundrum.thomas.v2.journal.JournalPrivacy
import com.conundrum.thomas.v2.journal.JournalPrivacyAdmissionRequest
import com.conundrum.thomas.v2.journal.JournalPrivacyChangeCommand
import com.conundrum.thomas.v2.journal.JournalPrivacyChangeResult
import com.conundrum.thomas.v2.journal.JournalResponseIntentPlanner
import com.conundrum.thomas.v2.journal.JournalRevisionCommand
import com.conundrum.thomas.v2.journal.JournalSourceAdmissionRequest
import com.conundrum.thomas.v2.journal.JournalSourceRevisionRequest
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionDisposition
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDraft
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.longitudinal.store.AdmissionResult
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import com.conundrum.thomas.v2.qualification.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.qualification.languageevidence.LanguagePipelineDisposition
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** The only CT-V2-09 composition root; it exists in qualification, never app/runtime. */
class GovernedJournalCapturePipeline(
    val store: QualificationLongitudinalStore,
    responsePlanner: JournalResponseIntentPlanner = com.conundrum.thomas.v2.journal.DeterministicJournalResponseIntentPlanner(),
) {
    private val languagePipeline = GovernedLanguageEvidencePipeline(store)
    private val engine = JournalCaptureEngine(
        QualificationJournalAdmissionPort(store),
        JournalLanguageProcessor { sourceRevisionId ->
            val result = languagePipeline.process(sourceRevisionId)
            val processing = when (result.disposition) {
                LanguagePipelineDisposition.ADMITTED -> JournalLanguageProcessingDisposition.ADMITTED
                LanguagePipelineDisposition.IDEMPOTENT_REPLAY -> JournalLanguageProcessingDisposition.IDEMPOTENT_REPLAY
                LanguagePipelineDisposition.SOURCE_ONLY -> JournalLanguageProcessingDisposition.SOURCE_ONLY
                LanguagePipelineDisposition.REJECTED_PROPOSAL -> JournalLanguageProcessingDisposition.REJECTED_PROPOSAL
                LanguagePipelineDisposition.ADMISSION_REJECTED -> JournalLanguageProcessingDisposition.ADMISSION_REJECTED
            }
            val evidenceAccepted = result.evidenceAdmission?.disposition in setOf(
                AdmissionDisposition.ACCEPTED,
                AdmissionDisposition.IDEMPOTENT_REPLAY,
            )
            val evidenceIds = if (evidenceAccepted) {
                (result.perception.proposals.map { it.assertion.id.value } +
                    listOfNotNull(result.perception.correctionCandidate?.correctingAssertion?.id?.value)).distinct().sorted()
            } else {
                emptyList()
            }
            JournalLanguageProcessingOutcome(
                processing,
                result.perception,
                evidenceIds,
                result.perception.unresolved.size + result.validation.issues.size,
                result.state.storeRevision,
                result.state.canonicalDigest,
            )
        },
        responsePlanner,
    )

    fun commit(command: JournalCommitCommand): JournalCaptureResult = engine.commit(command)
    fun revise(command: JournalRevisionCommand): JournalCaptureResult = engine.revise(command)
    fun changePrivacy(command: JournalPrivacyChangeCommand): JournalPrivacyChangeResult = engine.changePrivacy(command)
    fun formedState() = languagePipeline.formState()
}

private class QualificationJournalAdmissionPort(
    private val store: QualificationLongitudinalStore,
) : JournalAdmissionPort {
    override fun admitSource(request: JournalSourceAdmissionRequest): JournalAdmissionOutcome {
        val operation = LongitudinalWriteOperation.AdmitSource(
            SourceDraft(
                request.stableSourceId,
                request.revisionId,
                AcquisitionMode.JOURNAL,
                SourceAuthorRole.USER,
                InteractionId.parse(request.stableSourceId.value),
                OriginalSourceContent.Inline(request.exactCommittedText),
                EventTime.Unknown("Journal report time is separate from described event time"),
                request.reportTime,
                request.privacy.toSourcePrivacy(),
                mapOf(
                    "journal.capture-origin" to request.captureOrigin.name,
                    "journal.contract-version" to CT_V2_09_JOURNAL_CONTRACT_VERSION,
                ),
            ),
        )
        val result = store.admission.submit(
            longitudinalRequest(
                "commit",
                request.idempotencyKey.value,
                request.expectedStoreRevision,
                operation,
            ),
        )
        return result.toJournalOutcome(
            request.stableSourceId,
            request.revisionId,
            request.captureOrigin,
            request.privacy,
        )
    }

    override fun appendSourceRevision(request: JournalSourceRevisionRequest): JournalAdmissionOutcome {
        val prior = store.reader.source(request.stableSourceId)
        val captureOrigin = prior?.provenance?.metadata?.get("journal.capture-origin")
            ?.let(JournalCaptureOrigin::valueOf)
        val priorLifecycle = prior?.let {
            store.reader.lifecycle(LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION, it.id.value))
        }
        val privacy = if (priorLifecycle?.status == LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE) {
            JournalPrivacy.PRIVATE
        } else {
            JournalPrivacy.ELIGIBLE
        }
        val operation = LongitudinalWriteOperation.AppendSourceRevision(
            request.stableSourceId,
            request.priorRevisionId,
            request.newRevisionId,
            OriginalSourceContent.Inline(request.exactCommittedText),
            request.reportTime,
        )
        val result = store.admission.submit(
            longitudinalRequest(
                "revision",
                request.idempotencyKey.value,
                request.expectedStoreRevision,
                operation,
            ),
        )
        return result.toJournalOutcome(
            request.stableSourceId,
            request.newRevisionId,
            captureOrigin,
            privacy,
        )
    }

    override fun changePrivacy(request: JournalPrivacyAdmissionRequest): JournalAdmissionOutcome {
        val result = store.admission.submit(
            longitudinalRequest(
                "privacy",
                request.idempotencyKey.value,
                request.expectedStoreRevision,
                LongitudinalWriteOperation.ChangePrivacy(request.stableSourceId, request.privacy.toSourcePrivacy()),
            ),
        )
        return result.toJournalOutcome(request.stableSourceId, null, null, request.privacy)
    }

    private fun longitudinalRequest(
        kind: String,
        journalKey: String,
        expectedRevision: Long,
        operation: LongitudinalWriteOperation,
    ): LongitudinalAdmissionRequest {
        val key = "journal.$kind.$journalKey"
        return LongitudinalAdmissionRequest(
            AdmissionRequestId.parse("journal.$kind.${sha256(key).take(20)}"),
            IdempotencyKey.parse(key),
            expectedRevision,
            AdmissionActor.USER,
            AdmissionOrigin.JOURNAL,
            AdmissionPolicyVersion.CT_V2_07_V1,
            StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
            operation,
        )
    }

    private fun AdmissionResult.toJournalOutcome(
        stableSourceId: com.conundrum.thomas.v2.longitudinal.SourceIdentityId,
        sourceRevisionId: com.conundrum.thomas.v2.longitudinal.SourceRecordId?,
        captureOrigin: JournalCaptureOrigin?,
        privacy: JournalPrivacy?,
    ) = JournalAdmissionOutcome(
        disposition,
        stableSourceId,
        sourceRevisionId,
        captureOrigin,
        privacy,
        receipt?.resultingStoreRevision,
        receipt?.affectedStableIds.orEmpty(),
        reasonCodes,
        receipt?.payloadFingerprint,
    )

    private fun JournalPrivacy.toSourcePrivacy() = when (this) {
        JournalPrivacy.ELIGIBLE -> SourcePrivacy.ELIGIBLE
        JournalPrivacy.PRIVATE -> SourcePrivacy.PRIVATE
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
