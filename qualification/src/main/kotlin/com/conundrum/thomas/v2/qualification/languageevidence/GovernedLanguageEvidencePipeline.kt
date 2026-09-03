package com.conundrum.thomas.v2.qualification.languageevidence

import com.conundrum.thomas.v2.languageevidence.perception.CommittedSourceText
import com.conundrum.thomas.v2.languageevidence.perception.ConservativeLanguagePerception
import com.conundrum.thomas.v2.languageevidence.perception.LanguageEvidenceProposalValidator
import com.conundrum.thomas.v2.languageevidence.perception.LanguagePerceptionResult
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionContext
import com.conundrum.thomas.v2.languageevidence.perception.ProposalValidation
import com.conundrum.thomas.v2.languageevidence.stateformation.DeterministicStateFormation
import com.conundrum.thomas.v2.languageevidence.stateformation.FormedLongitudinalState
import com.conundrum.thomas.v2.languageevidence.stateformation.LongitudinalStateEvidence
import com.conundrum.thomas.v2.languageevidence.stateformation.StructuralContradictionDetector
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.admission.assertionRef
import com.conundrum.thomas.v2.longitudinal.store.AdmissionResult
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalReader
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

enum class LanguagePipelineDisposition {
    ADMITTED,
    IDEMPOTENT_REPLAY,
    SOURCE_ONLY,
    REJECTED_PROPOSAL,
    ADMISSION_REJECTED,
}

data class GovernedLanguageProcessingResult(
    val disposition: LanguagePipelineDisposition,
    val perception: LanguagePerceptionResult,
    val validation: ProposalValidation,
    val evidenceAdmission: AdmissionResult?,
    val contradictionAdmissions: List<AdmissionResult>,
    val state: FormedLongitudinalState,
)

/**
 * The sole CT-V2-08 qualification composition root. It never admits a source: callers must first
 * commit exact source text through the existing CT-V2-07 controller.
 */
class GovernedLanguageEvidencePipeline(
    private val store: QualificationLongitudinalStore,
    private val perception: ConservativeLanguagePerception = ConservativeLanguagePerception(),
    private val validator: LanguageEvidenceProposalValidator = LanguageEvidenceProposalValidator(),
    private val stateFormer: DeterministicStateFormation = DeterministicStateFormation(),
    private val contradictionDetector: StructuralContradictionDetector = StructuralContradictionDetector(),
) {
    fun process(sourceRevisionId: SourceRecordId, context: PerceptionContext = PerceptionContext()): GovernedLanguageProcessingResult {
        val source = requireNotNull(store.reader.snapshot().sources.firstOrNull { it.id == sourceRevisionId }) {
            "Source revision must already be admitted before perception"
        }
        require(source.authorRole == SourceAuthorRole.USER) { "CT-V2-08 does not convert Thomas/system prose into user evidence" }
        val committed = CommittedSourceText.from(source)
        val perceived = perception.perceive(committed, context)
        val validation = validator.validate(committed, perceived)
        if (!validation.accepted) {
            return GovernedLanguageProcessingResult(LanguagePipelineDisposition.REJECTED_PROPOSAL, perceived, validation, null,
                emptyList(), formState())
        }

        val bundle = evidenceBundle(perceived)
        val evidenceAdmission = bundle?.let { submitEvidence(source, it, perceived) }
        val evidenceAccepted = evidenceAdmission == null || evidenceAdmission.disposition in setOf(
            AdmissionDisposition.ACCEPTED, AdmissionDisposition.IDEMPOTENT_REPLAY,
        )
        val contradictionAdmissions = if (evidenceAccepted) admitNewContradictions() else emptyList()
        val disposition = when {
            evidenceAdmission == null -> LanguagePipelineDisposition.SOURCE_ONLY
            evidenceAdmission.disposition == AdmissionDisposition.ACCEPTED -> LanguagePipelineDisposition.ADMITTED
            evidenceAdmission.disposition == AdmissionDisposition.IDEMPOTENT_REPLAY -> LanguagePipelineDisposition.IDEMPOTENT_REPLAY
            else -> LanguagePipelineDisposition.ADMISSION_REJECTED
        }
        return GovernedLanguageProcessingResult(disposition, perceived, validation, evidenceAdmission,
            contradictionAdmissions, formState())
    }

    fun formState(): FormedLongitudinalState = stateFormer.form(stateEvidence(store.reader))

    private fun evidenceBundle(result: LanguagePerceptionResult): EvidenceBundle? {
        val assertions = result.proposals.map { it.assertion } + listOfNotNull(result.correctionCandidate?.takeIf { it.target != null }?.correctingAssertion)
        val entities = result.proposals.flatMap { it.entities }
        val corrections = listOfNotNull(result.correctionCandidate?.correction)
        val supersessions = listOfNotNull(result.correctionCandidate?.supersession)
        if (assertions.isEmpty() && entities.isEmpty() && corrections.isEmpty() && supersessions.isEmpty()) return null
        return EvidenceBundle(assertions = assertions.distinctBy { it.id }, entities = entities.distinctBy { it.id },
            corrections = corrections, supersessions = supersessions)
    }

    private fun submitEvidence(source: SourceRecord, bundle: EvidenceBundle, result: LanguagePerceptionResult): AdmissionResult {
        val fingerprint = sha256("${source.id.value}|${result.perceptionVersion}|${sourceTextFingerprint(source)}")
        val correction = bundle.corrections.isNotEmpty() || bundle.supersessions.any { it.predecessor is ClaimReference.Hypothesis }
        val idempotency = "language.${fingerprint.take(32)}"
        val priorRevision = store.reader.redactedAdmissionHistory()
            .firstOrNull { it.idempotencyKey == idempotency && it.storeRevision != null }
            ?.storeRevision?.minus(1) ?: store.reader.currentStoreRevision()
        return store.admission.submit(
            LongitudinalAdmissionRequest(
                AdmissionRequestId.parse("language.${fingerprint.take(24)}"),
                IdempotencyKey.parse(idempotency),
                priorRevision,
                AdmissionActor.USER,
                if (correction) AdmissionOrigin.USER_CORRECTION else originFor(source),
                AdmissionPolicyVersion.CT_V2_07_V1,
                StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
                LongitudinalWriteOperation.AdmitEvidenceBundle(bundle),
            ),
        )
    }

    private fun admitNewContradictions(): List<AdmissionResult> {
        val evidence = stateEvidence(store.reader)
        return contradictionDetector.identify(evidence).map { relation ->
            val requestDigest = sha256(relation.id.value)
            store.admission.submit(
                LongitudinalAdmissionRequest(
                    AdmissionRequestId.parse("language-contradiction.${requestDigest.take(18)}"),
                    IdempotencyKey.parse("language-contradiction.${requestDigest.take(24)}"),
                    store.reader.currentStoreRevision(),
                    AdmissionActor.THOMAS,
                    AdmissionOrigin.THOMAS_DERIVATION,
                    AdmissionPolicyVersion.CT_V2_07_V1,
                    StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
                    LongitudinalWriteOperation.RecordContradiction(relation),
                ),
            )
        }
    }

    private fun stateEvidence(reader: LongitudinalReader): LongitudinalStateEvidence {
        val snapshot = reader.snapshot()
        val currentSources = snapshot.sources.groupBy { it.stableSourceId }.values.mapNotNull { revisions ->
            revisions.maxByOrNull { it.provenance.sourceRevision }
        }
        val eligibleCurrentSources = currentSources.filter { source ->
            reader.isEligible(LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION, source.id.value))
        }
        val eligibleSourceIds = eligibleCurrentSources.map { it.id }.toSet()
        val eligibleAssertions = snapshot.assertions.filter { assertion ->
            assertion.sourceRecordId in eligibleSourceIds && reader.isEligible(assertionRef(assertion.id))
        }.map { it.id }.toSet()
        val excluded = buildSet {
            snapshot.assertions.filter { it.id !in eligibleAssertions }.forEach { add(it.id.value) }
            currentSources.filter { it.id !in eligibleSourceIds }.forEach { add(it.id.value) }
            snapshot.hypotheses.filter { hypothesis ->
                !reader.isEligible(LongitudinalObjectRef(StoredObjectType.HYPOTHESIS, hypothesis.id.value))
            }.forEach { add(it.id.value) }
        }
        val excludedStates = excluded.associateWith { id ->
            val type = when {
                snapshot.assertions.any { it.id.value == id } -> StoredObjectType.ASSERTION
                snapshot.sources.any { it.id.value == id } -> StoredObjectType.SOURCE_REVISION
                else -> StoredObjectType.HYPOTHESIS
            }
            reader.lifecycle(LongitudinalObjectRef(type, id))?.let { "${it.status.name}:${it.causeCode}" }
                ?: "NON_CURRENT_OR_INELIGIBLE"
        }
        val perceptions = eligibleCurrentSources.sortedBy { it.id }.map { source ->
            perception.perceive(CommittedSourceText.from(source), persistedCorrectionContext(source, snapshot))
        }
        return LongitudinalStateEvidence(
            storeRevision = reader.currentStoreRevision(),
            snapshot = snapshot,
            eligibleSourceRevisionIds = eligibleSourceIds,
            eligibleAssertionIds = eligibleAssertions,
            excludedObjectIds = excluded,
            excludedLifecycleStates = excludedStates,
            perceptionResults = perceptions,
        )
    }

    private fun persistedCorrectionContext(
        source: SourceRecord,
        snapshot: com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot,
    ): PerceptionContext {
        val correctingIds = snapshot.assertions.filter { it.sourceRecordId == source.id }.map { ClaimReference.Assertion(it.id) }.toSet()
        val relation = snapshot.supersessions.firstOrNull { it.successor in correctingIds }
        return PerceptionContext(relation?.predecessor)
    }

    private fun sourceTextFingerprint(source: SourceRecord): String =
        com.conundrum.thomas.v2.longitudinal.sourceTextSha256(
            (source.originalContent as com.conundrum.thomas.v2.longitudinal.OriginalSourceContent.Inline).exactContent,
        )

    private fun originFor(source: SourceRecord): AdmissionOrigin = when (source.provenance.acquisitionMode) {
        com.conundrum.thomas.v2.longitudinal.AcquisitionMode.JOURNAL -> AdmissionOrigin.JOURNAL
        com.conundrum.thomas.v2.longitudinal.AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE -> AdmissionOrigin.BIOGRAPHER_OPEN_NARRATIVE
        com.conundrum.thomas.v2.longitudinal.AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE -> AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE
        com.conundrum.thomas.v2.longitudinal.AcquisitionMode.THERAPIST_CONVERSATION -> AdmissionOrigin.THERAPIST_CONVERSATION
        com.conundrum.thomas.v2.longitudinal.AcquisitionMode.USER_CORRECTION -> AdmissionOrigin.USER_CORRECTION
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
