package com.conundrum.thomas.v2.runtime

import com.conundrum.thomas.v2.biographer.BiographerAnswerId
import com.conundrum.thomas.v2.biographer.BiographerAnswerOrigin
import com.conundrum.thomas.v2.biographer.BiographerAnswerCommand
import com.conundrum.thomas.v2.biographer.BiographerIdempotencyKey
import com.conundrum.thomas.v2.biographer.BiographerInvestigationHistory
import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerPrivacy
import com.conundrum.thomas.v2.biographer.BiographerQualificationAuthority
import com.conundrum.thomas.v2.biographer.BiographerQuestionPlan
import com.conundrum.thomas.v2.biographer.CoverageRequest
import com.conundrum.thomas.v2.engine.ordinary.CoreActionExecution
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.journal.JournalCaptureOrigin
import com.conundrum.thomas.v2.journal.JournalCommitCommand
import com.conundrum.thomas.v2.journal.JournalEntryId
import com.conundrum.thomas.v2.journal.JournalIdempotencyKey
import com.conundrum.thomas.v2.journal.JournalPrivacy
import com.conundrum.thomas.v2.journal.JournalQualificationAuthority
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.languageevidence.stateformation.FormedLongitudinalState
import com.conundrum.thomas.v2.languageevidence.GovernedLanguageEvidencePipeline
import com.conundrum.thomas.v2.languagerenderer.BiographerRenderCommandAdapter
import com.conundrum.thomas.v2.languagerenderer.GovernedLanguageRenderer
import com.conundrum.thomas.v2.languagerenderer.GovernedRenderCommand
import com.conundrum.thomas.v2.languagerenderer.GovernedRenderResult
import com.conundrum.thomas.v2.languagerenderer.JournalRenderCommandAdapter
import com.conundrum.thomas.v2.languagerenderer.RenderHistoryState
import com.conundrum.thomas.v2.languagerenderer.RenderQualificationAuthority
import com.conundrum.thomas.v2.languagerenderer.RenderCommandId
import com.conundrum.thomas.v2.languagerenderer.RenderDisposition
import com.conundrum.thomas.v2.languagerenderer.RenderableGrounding
import com.conundrum.thomas.v2.languagerenderer.SafetyRenderCommandAdapter
import com.conundrum.thomas.v2.languagerenderer.TherapyRenderCommandAdapter
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStore
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyRenderRequestFactory
import com.conundrum.thomas.v2.safety.SafetyScopeGate
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnCommand
import com.conundrum.thomas.v2.therapylongitudinal.TherapyIdempotencyKey
import com.conundrum.thomas.v2.therapylongitudinal.TherapyIntegrationAuthority
import com.conundrum.thomas.v2.therapylongitudinal.TherapySessionId
import com.conundrum.thomas.v2.therapylongitudinal.TherapySessionMemoryState
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnCaptureOrigin
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnId
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnPrivacy
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.time.ZoneOffset

/**
 * The one platform-neutral production runtime. It sequences governed owners but contains no
 * therapeutic, safety, Journal-posture, Biographer-target, retrieval, or rendering policy.
 */
class ThomasProductionRuntime(
    private val store: ProtectedPersonalDataStore,
    private val renderer: GovernedLanguageRenderer = GovernedLanguageRenderer(),
) : AutoCloseable {
    private val journal = ProductionJournalPipeline(store)
    private val biographer = ProductionBiographerPipeline(store)
    private val therapy = ProductionTherapyPipeline(store)
    private val sourceRevisionLanguage = GovernedLanguageEvidencePipeline(
        store.admission,
        store.reader,
        StoreDataClassification.PROTECTED_PERSONAL_DATA,
    )
    private val safetyGate = SafetyScopeGate()
    private val processing = AtomicBoolean(false)
    private var renderHistory = RenderHistoryState()
    private var biographerHistory = BiographerInvestigationHistory()
    private var pendingBiographerPlan: BiographerQuestionPlan? = null
    private var pendingBiographerTarget: com.conundrum.thomas.v2.biographer.InvestigationTarget? = null
    private var biographerStopped = false
    private var biographerSafetyBlocked = false
    private var therapyMemory = TherapySessionMemoryState(SESSION_ID)
    private var therapyInput = ProductionTherapyInputBoundary()
    private var safetyObservations = ProductionSafetyObservationBoundary()
    private var rendererCalls = 0L
    private var assistantArtifacts = 0L
    private var closed = false
    private var allocatedTurnFrontier = 0L

    val availability: ProductionRuntimeAvailability
        get() = if (closed) ProductionRuntimeAvailability.CLOSED else ProductionRuntimeAvailability.READY

    /**
     * The Android corpus shares an index sequence across turns, prompts and lifecycle commands.
     * Accepted keys survive reopen and source deletion in the redacted admission history.
     * Store revision counts mutations, not allocated indexes; it is never identity authority.
     */
    @Synchronized
    fun allocateTurnIndex(): Long {
        check(!closed)
        val committedFrontier = store.reader.redactedAdmissionHistory().asSequence()
            .mapNotNull { CANONICAL_ALLOCATION_KEY.matchEntire(it.idempotencyKey)?.groupValues?.get(1) }
            .map { it.toLong() }
            .maxOrNull() ?: 0L
        return Math.incrementExact(maxOf(allocatedTurnFrontier, committedFrontier))
            .also { allocatedTurnFrontier = it }
    }

    fun submit(request: ProductionTurnRequest): ProductionTurnResult {
        if (closed) return unavailable(request, "RUNTIME_CLOSED")
        if (request.committedText.isBlank()) {
            return baseResult(request, ProductionTurnDisposition.REJECTED_BLANK, "BLANK_INPUT_NOT_COMMITTED")
        }
        if (!processing.compareAndSet(false, true)) {
            return baseResult(request, ProductionTurnDisposition.REJECTED_BUSY, "TURN_ALREADY_PROCESSING")
        }
        return try {
            when (request.mode) {
                ProductionThomasMode.JOURNAL -> submitJournal(request)
                ProductionThomasMode.BIOGRAPHER -> submitBiographer(request)
                ProductionThomasMode.THERAPY -> submitTherapy(request)
            }
        } catch (_: RuntimeException) {
            baseResult(request, ProductionTurnDisposition.PERSISTENCE_UNAVAILABLE, "GOVERNED_RUNTIME_OPERATION_FAILED")
        } finally {
            processing.set(false)
        }
    }


    fun nextBiographerPrompt(clientTurnIndex: Long, openStory: Boolean = false): ProductionBiographerPrompt? {
        check(!closed)
        if (biographerStopped) return null
        if (biographerSafetyBlocked) {
            val blocked = biographer.decide(CoverageRequest(BiographerPosture.TARGETED_COVERAGE,
                investigationAuthority = com.conundrum.thomas.v2.biographer.BiographerInvestigationAuthority.BLOCKED_BY_SAFETY_SCOPE), biographerHistory)
            check(blocked.plan == null)
            return null
        }
        val targeted = biographer.decide(CoverageRequest(
            if (openStory) BiographerPosture.OPEN_STORY else BiographerPosture.TARGETED_COVERAGE), biographerHistory)
        val decision = if (targeted.plan != null) targeted else
            biographer.decide(CoverageRequest(BiographerPosture.OPEN_STORY), biographerHistory)
        val plan = decision.plan ?: return null
        val grounding = biographerGrounding(plan, decision.coverageMap.selectedTarget) ?: return null
        val command = BiographerRenderCommandAdapter.adapt(
            renderId("biographer-prompt", clientTurnIndex), clientTurnIndex.toInt(), plan, grounding).forProduction()
        val rendered = render(command)
        val text = rendered.finalText ?: return null
        // An undelivered question is not a pending investigation or an offer.
        biographerHistory = decision.resultingHistory
        pendingBiographerPlan = plan
        pendingBiographerTarget = decision.coverageMap.selectedTarget
        assistantArtifacts += 1
        return ProductionBiographerPrompt(text, rendered, plan.targetId?.value, decision)
    }

    fun snapshot(): ProductionRuntimeSnapshot {
        check(!closed)
        return ProductionRuntimeSnapshot(
            store.reader.currentStoreRevision(),
            store.reader.canonicalLogicalStateDigest(),
            formedState(),
            rendererCalls,
            assistantArtifacts,
        )
    }

    fun export() = store.export()
    fun createProtectedBackup(key: com.conundrum.thomas.v2.personaldata.RecoveryKey) =
        store.createProtectedBackup(key)
    fun reset() = store.reset()

    fun sourceSummaries(): List<ProductionSourceSummary> {
        check(!closed)
        val revision = store.reader.currentStoreRevision()
        return store.reader.snapshot(revision).sources
            .groupBy { it.stableSourceId }
            .map { (sourceId, history) ->
                val current = history.maxBy { it.provenance.sourceRevision }
                val lifecycle = store.reader.lifecycle(
                    LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION, current.id.value),
                    revision,
                )
                ProductionSourceSummary(
                    sourceId,
                    current.provenance.acquisitionMode,
                    current.reportTime.value,
                    history.size,
                    lifecycle?.eligibleForOrdinaryUse == true,
                    lifecycle?.status,
                )
            }
            .sortedWith(compareByDescending<ProductionSourceSummary> { it.reportTime }.thenBy { it.stableSourceId.value })
    }

    fun changeSourcePrivacy(
        sourceId: SourceIdentityId,
        makePrivate: Boolean,
        commandIndex: Long,
    ): ProductionSourceLifecycleResult = submitLifecycle(
        sourceId,
        if (makePrivate) ProductionSourceLifecycleAction.MAKE_PRIVATE
        else ProductionSourceLifecycleAction.REQUEST_ELIGIBLE_REVIEW,
        commandIndex,
        LongitudinalWriteOperation.ChangePrivacy(
            sourceId,
            if (makePrivate) SourcePrivacy.PRIVATE else SourcePrivacy.ELIGIBLE,
        ),
    )

    fun deleteSource(
        sourceId: SourceIdentityId,
        commandIndex: Long,
    ): ProductionSourceLifecycleResult = submitLifecycle(
        sourceId,
        ProductionSourceLifecycleAction.DELETE,
        commandIndex,
        LongitudinalWriteOperation.DeleteSource(sourceId),
    )

    /**
     * Appends corrected user wording as a new immutable source revision. The prior revision and
     * its derived assertions are made non-current by CT-V2-07; eligible corrected wording is then
     * conservatively processed through CT-V2-08. This is never an in-place database edit.
     */
    fun reviseSource(
        sourceId: SourceIdentityId,
        correctedText: String,
        commandIndex: Long,
        reportTime: ReportTime,
    ): ProductionSourceRevisionResult {
        check(!closed)
        require(commandIndex > 0)
        require(correctedText.isNotBlank() && correctedText.length <= 4_096)
        if (!processing.compareAndSet(false, true)) {
            return ProductionSourceRevisionResult(
                sourceId, null, AdmissionDisposition.FAILED_WITHOUT_COMMIT,
                store.reader.currentStoreRevision(), null, listOf("TURN_ALREADY_PROCESSING"),
            )
        }
        return try {
            val current = store.reader.source(sourceId)
                ?: return ProductionSourceRevisionResult(
                    sourceId, null, AdmissionDisposition.REJECTED_MISSING_REFERENCE,
                    store.reader.currentStoreRevision(), null, listOf("SOURCE_NOT_FOUND"),
                )
            val nextNumber = current.provenance.sourceRevision + 1
            val newRevisionId = SourceRecordId.parse("${sourceId.value}.revision-$nextNumber")
            val identity = "android-source-revision-$commandIndex"
            val admission = store.admission.submit(
                LongitudinalAdmissionRequest(
                    AdmissionRequestId.parse(identity),
                    IdempotencyKey.parse(identity),
                    store.reader.currentStoreRevision(),
                    AdmissionActor.USER,
                    AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
                    AdmissionPolicyVersion.CT_V2_07_V1,
                    StoreDataClassification.PROTECTED_PERSONAL_DATA,
                    LongitudinalWriteOperation.AppendSourceRevision(
                        sourceId,
                        current.id,
                        newRevisionId,
                        OriginalSourceContent.Inline(correctedText),
                        reportTime,
                    ),
                ),
            )
            val accepted = admission.disposition in setOf(
                AdmissionDisposition.ACCEPTED,
                AdmissionDisposition.IDEMPOTENT_REPLAY,
            )
            val evidenceDisposition = if (accepted && store.reader.isEligible(
                    LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION, newRevisionId.value),
                )
            ) {
                runCatching { sourceRevisionLanguage.process(newRevisionId).disposition.name }
                    .getOrElse { "FAILED_AFTER_SOURCE_REVISION" }
            } else if (accepted) {
                "SKIPPED_INELIGIBLE"
            } else {
                null
            }
            if (accepted) invalidateEphemeralEvidence()
            ProductionSourceRevisionResult(
                sourceId,
                newRevisionId.takeIf { accepted },
                admission.disposition,
                store.reader.currentStoreRevision(),
                evidenceDisposition,
                admission.reasonCodes.ifEmpty { listOf("GOVERNED_SOURCE_REVISION_ACCEPTED") },
            )
        } finally {
            processing.set(false)
        }
    }

    override fun close() {
        if (!closed) store.close()
        closed = true
    }

    private fun submitJournal(request: ProductionTurnRequest): ProductionTurnResult {
        val identity = identity("journal", request)
        val entry = JournalEntryId.parse(identity)
        val result = journal.commit(
            JournalCommitCommand(
                entry,
                JournalIdempotencyKey.parse("commit-$identity"),
                store.reader.currentStoreRevision(),
                request.committedText,
                request.inputOrigin.toJournalOrigin(),
                request.journalResponsePreference,
                request.privacy.toJournalPrivacy(),
                ReportTime(request.committedAt),
                JournalQualificationAuthority.ANDROID_PRODUCTION,
            ),
        )
        val receipt = result.receipt
        if (receipt == null) {
            return result(
                request,
                identity,
                null,
                null,
                null,
                null,
                ProductionTurnDisposition.SOURCE_CAPTURE_FAILED,
                result.reasonCodes,
            )
        }
        val responsePlan = result.responsePlan
        if (request.journalResponsePreference == JournalResponsePreference.NO_RESPONSE) {
            return result(
                request,
                identity,
                receipt.stableSourceId,
                null,
                null,
                null,
                ProductionTurnDisposition.NO_RESPONSE,
                result.reasonCodes + "JOURNAL_TRUE_SILENCE",
            )
        }
        if (responsePlan == null) {
            return result(
                request,
                identity,
                receipt.stableSourceId,
                null,
                null,
                null,
                ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT,
                result.reasonCodes + "JOURNAL_NO_SAFE_GROUNDED_RESPONSE",
            )
        }
        val assertion = store.reader.assertion(responsePlan.grounding.assertionId)
            ?: return result(
                request,
                identity,
                receipt.stableSourceId,
                null,
                null,
                null,
                ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT,
                result.reasonCodes + "JOURNAL_GROUNDING_UNAVAILABLE",
            )
        val grounding = RenderableGrounding(
            id = "journal-grounding",
            surfaceMeaning = journalSurfaceMeaning(assertion.value, assertion.eventTime),
            requiredMarkerGroups = listOf(setOf("you")),
            epistemicClass = assertion.epistemicClass,
            uncertainty = assertion.uncertainty,
            temporalScope = assertion.eventTime.takeUnless { it is EventTime.Unknown },
        )
        val command = JournalRenderCommandAdapter.adapt(
            renderId("journal", request.clientTurnIndex),
            request.clientTurnIndex.toInt(),
            request.journalResponsePreference,
            responsePlan,
            grounding,
        ).forProduction()
        val rendered = render(command)
        return renderedResult(request, identity, receipt.stableSourceId, rendered, null, result.reasonCodes)
    }


    private fun submitBiographer(request: ProductionTurnRequest): ProductionTurnResult {
        val identity = identity("biographer", request)
        updateBiographerSafetyInterruption(safetyGate.govern(safetyObservations.observe(request)))
        if (biographerSafetyBlocked) {
            pendingBiographerPlan = null
            pendingBiographerTarget = null
        }
        if (ProductionTherapyInputBoundary.normalize(request.committedText) == "open story") {
            biographerStopped = false
            pendingBiographerPlan = null
            pendingBiographerTarget = null
            val prompt = nextBiographerPrompt(request.clientTurnIndex, openStory = true)
            return if (prompt == null) baseResult(request, ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT, "BIOGRAPHER_NO_PROMPT")
            else result(request, identity, null, artifact(identity, request.mode, prompt.renderResult), prompt.renderResult,
                null, ProductionTurnDisposition.COMPLETED, listOf("USER_REQUESTED_OPEN_STORY"))
        }
        if (biographerStopped) return baseResult(request, ProductionTurnDisposition.NO_RESPONSE, "BIOGRAPHER_STOPPED")
        // A lifecycle change invalidates a pending target. Capture subsequent prose as open narrative,
        // not as an answer to a question whose grounding is no longer eligible/current.
        if (pendingBiographerTarget?.let { target ->
                biographer.coverageEvidence().candidates.none { it.id == target.id && it.materialChangeToken == target.materialChangeToken &&
                    it.status !in setOf(com.conundrum.thomas.v2.biographer.CoverageStatus.PRIVATE, com.conundrum.thomas.v2.biographer.CoverageStatus.DECLINED) }
            } == true) {
            pendingBiographerTarget = null
            pendingBiographerPlan = null
        }
        val plan = pendingBiographerPlan ?: biographer.decide(
            CoverageRequest(BiographerPosture.OPEN_STORY), biographerHistory).plan
            ?: return baseResult(request, ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT, "BIOGRAPHER_NO_PLAN")
        val answer = biographer.answer(BiographerAnswerCommand(
            BiographerAnswerId.parse(identity), BiographerIdempotencyKey.parse("answer-$identity"),
            store.reader.currentStoreRevision(), plan, request.committedText, request.inputOrigin.toBiographerOrigin(),
            request.privacy.toBiographerPrivacy(), ReportTime(request.committedAt), BiographerQualificationAuthority.ANDROID_PRODUCTION),
            pendingBiographerTarget, biographerHistory)
        biographerHistory = answer.resultingHistory
        pendingBiographerPlan = null
        pendingBiographerTarget = null
        val receipt = answer.capture?.receipt
        val reasons = answer.capture?.reasonCodes.orEmpty() + answer.disposition.name
        if (answer.capture != null && receipt == null) return result(request, identity, null, null, null, null,
            ProductionTurnDisposition.SOURCE_CAPTURE_FAILED, reasons).copy(biographerAnswer = answer)
        if (answer.disposition == com.conundrum.thomas.v2.biographer.InvestigationAnswerDisposition.STOPPED) {
            biographerStopped = true
            return result(request, identity, null, null, null, null, ProductionTurnDisposition.NO_RESPONSE, reasons).copy(biographerAnswer = answer)
        }
        val prompt = nextBiographerPrompt(request.clientTurnIndex + 1)
        return result(request, identity, receipt?.stableSourceId,
            prompt?.let { artifact(identity, request.mode, it.renderResult) }, prompt?.renderResult, null,
            if (prompt == null) ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT else ProductionTurnDisposition.COMPLETED,
            reasons).copy(biographerAnswer = answer, nextBiographerTargetId = prompt?.targetId)
    }

    private fun updateBiographerSafetyInterruption(decision: com.conundrum.thomas.v2.safety.SafetyScopeDecision) {
        if (decision.authorityState == SafetyAuthorityState.ORDINARY_POLICY_ALLOWED) biographerSafetyBlocked = false
        if (decision.observations.any { it.resolution == com.conundrum.thomas.v2.safety.SafetyEvidenceResolution.CONTRADICTORY } || decision.authorityState in setOf(SafetyAuthorityState.EMERGENCY_BOUNDARY_REACHED,
                SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, SafetyAuthorityState.EXTERNAL_SUPPORT_REQUIRED)) {
            biographerSafetyBlocked = true
        }
    }

    private fun submitTherapy(request: ProductionTurnRequest): ProductionTurnResult {
        val identity = identity("therapy", request)
        val proceduralText = if (safetyObservations.handlesReply(request.committedText)) "" else request.committedText
        val lines = proceduralText.lineSequence().toList()
        val currentBlock = lines.size == 1 || ProductionTherapyInputBoundary.startsObservationBlock(lines.first()) ||
            safetyObservations.startsObservationBlock(lines.first())
        val therapyState = (if (currentBlock) lines else listOf("" )).asSequence()
            .map { therapyInput.observe(request.copy(committedText = it)) }.last()
        val safetyInput = safetyObservations.observe(request)
        val safety = safetyGate.govern(safetyInput)
        updateBiographerSafetyInterruption(safety)
        val preTurnRevision = store.reader.currentStoreRevision()
        val turnId = TherapyTurnId.parse(identity)
        val integrated = therapy.integrate(
            LongitudinalTherapyTurnCommand(
                sessionId = SESSION_ID,
                turnId = turnId,
                idempotencyKey = TherapyIdempotencyKey.parse("capture-$identity"),
                expectedStoreRevision = preTurnRevision,
                exactUserText = request.committedText,
                captureOrigin = request.inputOrigin.toTherapyOrigin(),
                privacy = request.privacy.toTherapyPrivacy(),
                reportTime = ReportTime(request.committedAt),
                safetyInput = safetyInput,
                therapyState = therapyState,
                memoryIntent = request.therapyMemoryIntent,
                retrievalAnchors = anchors(therapyState.concernStatement.value ?: request.committedText, preTurnRevision, request),
                sessionMemoryState = therapyMemory,
                authority = TherapyIntegrationAuthority.ANDROID_PRODUCTION,
            ),
        )
        val plan = integrated.plan
        plan?.let {
            therapyMemory = it.nextSessionMemoryState
        }
        val source = plan?.captureReceipt?.stableSourceId
        val command = when {
            safety.authorityState != SafetyAuthorityState.ORDINARY_POLICY_ALLOWED &&
                safety.selectedAction != null -> {
                val safetyRequest = SafetyRenderRequestFactory.create(safety)
                SafetyRenderCommandAdapter.adapt(
                    renderId("safety", request.clientTurnIndex),
                    request.clientTurnIndex.toInt(),
                    safetyRequest.command,
                    safetyRequest.authorizedSupportingText,
                ).forProduction()
            }
            plan?.renderSupport != null -> TherapyRenderCommandAdapter.adapt(
                renderId("therapy", request.clientTurnIndex),
                request.clientTurnIndex.toInt(),
                requireNotNull(plan.renderSupport),
            ).forProduction()
            else -> null
        }
        if (command == null) {
            val disposition = if (safety.authorityState != SafetyAuthorityState.ORDINARY_POLICY_ALLOWED) {
                ProductionTurnDisposition.SAFETY_PREEMPTED
            } else {
                ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT
            }
            return result(request, identity, source, null, null, plan, disposition, integrated.reasonCodes).copy(therapyObservation = therapyState, safetyObservation = safety)
        }
        val rendered = render(command)
        safetyObservations.delivered(if (rendered.finalText != null) safety.nextExpectedEvidence else null)
        if (rendered.finalText != null || rendered.disposition == RenderDisposition.NO_RESPONSE) {
            val route = plan?.routeDecision?.route
            val action = plan?.routeDecision?.selectedActionId
            if (route != null && action != null) therapyInput.delivered(action, route)
        }
        return renderedResult(request, identity, source, rendered, plan, integrated.reasonCodes)
            .copy(therapyObservation = therapyState, safetyObservation = safety)
    }

    private fun renderedResult(
        request: ProductionTurnRequest,
        identity: String,
        source: SourceIdentityId?,
        rendered: GovernedRenderResult,
        plan: com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyPlan?,
        reasons: List<String>,
    ): ProductionTurnResult {
        val artifact = rendered.finalText?.let { artifact(identity, request.mode, rendered) }
        val disposition = when {
            rendered.disposition == RenderDisposition.NO_RESPONSE -> ProductionTurnDisposition.NO_RESPONSE
            artifact == null -> ProductionTurnDisposition.RENDERING_UNAVAILABLE
            else -> ProductionTurnDisposition.COMPLETED
        }
        if (artifact != null) assistantArtifacts += 1
        return result(request, identity, source, artifact, rendered, plan, disposition, reasons)
    }

    private fun render(command: GovernedRenderCommand): GovernedRenderResult {
        rendererCalls += 1
        return renderer.render(command, renderHistory).also { renderHistory = it.nextHistory }
    }

    private fun GovernedRenderCommand.forProduction() =
        copy(qualificationAuthority = RenderQualificationAuthority.ANDROID_PRODUCTION)

    private fun formedState(): FormedLongitudinalState = journal.formedState()

    /** Custody edits invalidate retained procedure; no deleted/corrected premise survives in it.
     * Fresh current declarations are required. No procedural facts are rebuilt from the corpus.
     */
    private fun invalidateEphemeralEvidence() {
        therapyInput = ProductionTherapyInputBoundary()
        safetyObservations = ProductionSafetyObservationBoundary()
        therapyMemory = TherapySessionMemoryState(SESSION_ID)
        pendingBiographerPlan = null
        pendingBiographerTarget = null
    }

    private fun submitLifecycle(
        sourceId: SourceIdentityId,
        action: ProductionSourceLifecycleAction,
        commandIndex: Long,
        operation: LongitudinalWriteOperation,
    ): ProductionSourceLifecycleResult {
        check(!closed)
        require(commandIndex > 0)
        if (!processing.compareAndSet(false, true)) {
            return ProductionSourceLifecycleResult(
                sourceId,
                action,
                com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition.FAILED_WITHOUT_COMMIT,
                store.reader.currentStoreRevision(),
                listOf("TURN_ALREADY_PROCESSING"),
            )
        }
        return try {
            val identity = "android-lifecycle-${action.name.lowercase(Locale.ROOT).replace('_', '-')}-$commandIndex"
            val response = store.admission.submit(
                LongitudinalAdmissionRequest(
                    AdmissionRequestId.parse(identity),
                    IdempotencyKey.parse(identity),
                    store.reader.currentStoreRevision(),
                    AdmissionActor.USER,
                    AdmissionOrigin.PERSONAL_DATA_LIFECYCLE,
                    AdmissionPolicyVersion.CT_V2_07_V1,
                    StoreDataClassification.PROTECTED_PERSONAL_DATA,
                    operation,
                ),
            )
            if (response.disposition in setOf(AdmissionDisposition.ACCEPTED, AdmissionDisposition.IDEMPOTENT_REPLAY)) invalidateEphemeralEvidence()
            ProductionSourceLifecycleResult(
                sourceId,
                action,
                response.disposition,
                store.reader.currentStoreRevision(),
                response.reasonCodes.ifEmpty { listOf("GOVERNED_LIFECYCLE_OPERATION_ACCEPTED") },
            )
        } finally {
            processing.set(false)
        }
    }

    private fun anchors(text: String, revision: Long, request: ProductionTurnRequest): RetrievalAnchors {
        val normalized = text.lowercase(Locale.ROOT)
        val terms = Regex("[a-z0-9]+").findAll(normalized)
            .map { it.value }.filter { it.length >= 3 }.take(24).toSet()
        val entitiesByLabel = store.reader.snapshot(revision).entities
            .filter {
                store.reader.isEligible(
                    LongitudinalObjectRef(StoredObjectType.ENTITY, it.id.value),
                    revision,
                )
            }
            .groupBy { it.label.lowercase(Locale.ROOT) }
        val explicitEntities = entitiesByLabel.filter { (label, matches) ->
            matches.size == 1 && Regex("(^|[^a-z0-9])${Regex.escape(label)}([^a-z0-9]|$)").containsMatchIn(normalized)
        }.values.map { it.single().id }.toSet()
        val namedSource = if (request.therapyMemoryIntent == com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent.EXPLICIT_RECALL) {
            val quotations = request.committedText.lineSequence().filter { it.startsWith("Please recall my earlier words: ") }
                .map { it.removePrefix("Please recall my earlier words: ").trim() }.toList()
            if (quotations.size != 1) null else store.reader.snapshot(revision).sources.filter { source ->
                (source.originalContent as? OriginalSourceContent.Inline)?.exactContent == quotations.single() &&
                    store.reader.isEligible(LongitudinalObjectRef(StoredObjectType.SOURCE_REVISION, source.id.value), revision)
            }.singleOrNull()?.stableSourceId
        } else null
        return RetrievalAnchors(entityIds = explicitEntities, lexicalTerms = terms, sourceIdentityIds = setOfNotNull(namedSource))
    }

    private fun biographerTime(value: EventTime): String = when (value) {
        is EventTime.Range -> if (value.start.earliest.year == value.end.latest.year) value.start.earliest.year.toString()
            else "the reported range ${value.start.earliest.year} to ${value.end.latest.year}"
        else -> temporalText(value)
    }


    private fun biographerGrounding(
        plan: BiographerQuestionPlan,
        target: com.conundrum.thomas.v2.biographer.InvestigationTarget?,
    ): RenderableGrounding? {
        val snapshot = store.reader.snapshot()
        fun eligible(type: StoredObjectType, id: String) =
            store.reader.isEligible(LongitudinalObjectRef(type, id), asOfRevision = store.reader.currentStoreRevision())
        val entityIds = target?.relevantEntityIds.orEmpty().map { it.value } + plan.groundingIds
        val labels = snapshot.entities.filter { it.id.value in entityIds && eligible(StoredObjectType.ENTITY, it.id.value) }.map { it.label }
        val assertionSources = snapshot.assertions.filter { it.id.value in plan.groundingIds && eligible(StoredObjectType.ASSERTION, it.id.value) }.map { it.sourceRecordId.value }
        val snippets = snapshot.sources.filter { it.id.value in assertionSources + plan.groundingIds && eligible(StoredObjectType.SOURCE_REVISION, it.id.value) }
            .mapNotNull { (it.originalContent as? OriginalSourceContent.Inline)?.exactContent }
            .distinct().filter { it.length <= 180 }.map { "“$it”" }
        val times = (plan.safeFacts.mapNotNull { it.temporalExpression } + target?.temporalBounds.orEmpty()).distinct()
        val subject = labels.takeIf { it.isNotEmpty() }?.joinToString(" and ")
            ?: snippets.takeIf { it.isNotEmpty() }?.joinToString(" and ")
        val meaning = when (plan.targetKind) {
            com.conundrum.thomas.v2.biographer.InvestigationTargetKind.OPEN_STORY -> "your history"
            com.conundrum.thomas.v2.biographer.InvestigationTargetKind.TEMPORAL_GAP ->
                if (times.size == 2) "your history between ${biographerTime(times[0])} and ${biographerTime(times[1])}" else return null
            com.conundrum.thomas.v2.biographer.InvestigationTargetKind.PERIOD_DETAIL ->
                times.firstOrNull()?.let { "your history during ${biographerTime(it)}" } ?: return null
            com.conundrum.thomas.v2.biographer.InvestigationTargetKind.ROLE_GAP -> subject?.let { "your reported role, $it" } ?: return null
            com.conundrum.thomas.v2.biographer.InvestigationTargetKind.PLACE_GAP -> subject?.let { "your history connected with $it" } ?: return null
            com.conundrum.thomas.v2.biographer.InvestigationTargetKind.RELATIONSHIP_CONTEXT -> subject?.let { "your reported relationship, $it" } ?: return null
            com.conundrum.thomas.v2.biographer.InvestigationTargetKind.EVENT_TIME_UNRESOLVED -> subject?.let { "the unresolved timing of $it" } ?: return null
            else -> subject ?: plan.safeFacts.firstOrNull()?.concept ?: return null
        }
        return RenderableGrounding(
            id = "biographer-grounding", surfaceMeaning = meaning,
            requiredMarkerGroups = listOf(setOf(meaning.lowercase(Locale.ROOT))),
            allowedEntityNames = labels.toSet(),
            allowedTemporalLiterals = Regex("[0-9]{4}").findAll(meaning).map { it.value }.toSet(),
        )
    }
    private fun assertionValue(value: AssertionValue): String = when (value) {
        is AssertionValue.Text -> value.value
        is AssertionValue.EntityReference -> store.reader.entity(value.entityId)?.label ?: "that person or event"
        is AssertionValue.EntityReferences -> value.entityIds.mapNotNull { store.reader.entity(it)?.label }.joinToString(" and ")
        is AssertionValue.TimeReference -> temporalText(value.value)
        is AssertionValue.BooleanValue -> value.value.toString()
        is AssertionValue.IntegerValue -> value.value.toString()
        is AssertionValue.ConceptValue -> value.conceptId.value.replace('.', ' ')
    }.ifBlank { "the committed entry" }

    private fun temporalText(value: EventTime): String = when (value) {
        is EventTime.ExactInstant -> value.value.toString()
        is EventTime.CalendarDate -> value.value.toString()
        is EventTime.ApproximateDate -> "around ${value.center}"
        is EventTime.ApproximateYear -> "around ${value.year}"
        is EventTime.Range -> "the reported range"
        is EventTime.RelativePeriod -> value.description
        is EventTime.OngoingInterval -> value.description
        is EventTime.BeforeOrAfter -> value.description
        is EventTime.UncertainChronology -> "an uncertain time"
        is EventTime.Unknown -> "an unknown time"
    }

    private fun journalSurfaceMeaning(value: AssertionValue, eventTime: EventTime): String {
        val base = assertionValue(value).trim().trimEnd('.')
        val temporal = when (eventTime) {
            is EventTime.ExactInstant -> eventTime.value.atZone(ZoneOffset.UTC).toLocalDate().toString()
            is EventTime.CalendarDate -> eventTime.value.toString()
            is EventTime.ApproximateDate -> "around ${eventTime.center}"
            is EventTime.ApproximateYear -> "around ${eventTime.year}"
            is EventTime.Range -> "in the described range"
            is EventTime.RelativePeriod -> eventTime.description
            is EventTime.OngoingInterval -> "ongoing ${eventTime.description}"
            is EventTime.BeforeOrAfter -> eventTime.description
            is EventTime.UncertainChronology -> "with uncertain timing: ${eventTime.description}"
            is EventTime.Unknown -> null
        }
        return listOfNotNull(base, temporal?.takeUnless { base.contains(it, ignoreCase = true) })
            .joinToString(" ")
    }

    private fun artifact(
        identity: String,
        mode: ProductionThomasMode,
        rendered: GovernedRenderResult,
    ) = ProductionAssistantArtifact(
        identity,
        mode,
        requireNotNull(rendered.finalText),
        rendered.canonicalRenderDigest,
        rendered.surfacedMemoryIds,
    )

    private fun result(
        request: ProductionTurnRequest,
        identity: String,
        source: SourceIdentityId?,
        artifact: ProductionAssistantArtifact?,
        rendered: GovernedRenderResult?,
        plan: com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyPlan?,
        disposition: ProductionTurnDisposition,
        reasons: List<String>,
    ) = ProductionTurnResult(
        disposition,
        identity,
        source,
        artifact,
        rendered,
        plan,
        store.reader.currentStoreRevision(),
        reasons.ifEmpty { listOf(disposition.name) },
    )

    private fun baseResult(
        request: ProductionTurnRequest,
        disposition: ProductionTurnDisposition,
        reason: String,
    ) = result(request, identity(request.mode.name.lowercase(Locale.ROOT), request), null, null, null, null, disposition, listOf(reason))

    private fun unavailable(request: ProductionTurnRequest, reason: String) = ProductionTurnResult(
        ProductionTurnDisposition.PERSISTENCE_UNAVAILABLE,
        identity(request.mode.name.lowercase(Locale.ROOT), request),
        null,
        null,
        null,
        null,
        0,
        listOf(reason),
    )

    private fun identity(prefix: String, request: ProductionTurnRequest) =
        "android-$prefix-${request.clientTurnIndex}"

    private fun renderId(prefix: String, index: Long) = RenderCommandId.parse("android.$prefix.$index")

    private fun ProductionInputOrigin.toJournalOrigin() = when (this) {
        ProductionInputOrigin.TYPED -> JournalCaptureOrigin.TYPED
        ProductionInputOrigin.SPEECH_TRANSCRIPT -> JournalCaptureOrigin.SPEECH_TRANSCRIPT
    }

    private fun ProductionInputOrigin.toBiographerOrigin() = when (this) {
        ProductionInputOrigin.TYPED -> BiographerAnswerOrigin.TYPED
        ProductionInputOrigin.SPEECH_TRANSCRIPT -> BiographerAnswerOrigin.SPEECH_TRANSCRIPT
    }

    private fun ProductionInputOrigin.toTherapyOrigin() = when (this) {
        ProductionInputOrigin.TYPED -> TherapyTurnCaptureOrigin.TYPED
        ProductionInputOrigin.SPEECH_TRANSCRIPT -> TherapyTurnCaptureOrigin.SPEECH_TRANSCRIPT
    }

    private fun ProductionTurnPrivacy.toJournalPrivacy() = when (this) {
        ProductionTurnPrivacy.ELIGIBLE -> JournalPrivacy.ELIGIBLE
        ProductionTurnPrivacy.PRIVATE -> JournalPrivacy.PRIVATE
    }

    private fun ProductionTurnPrivacy.toBiographerPrivacy() = when (this) {
        ProductionTurnPrivacy.ELIGIBLE -> BiographerPrivacy.ELIGIBLE
        ProductionTurnPrivacy.PRIVATE -> BiographerPrivacy.PRIVATE
    }

    private fun ProductionTurnPrivacy.toTherapyPrivacy() = when (this) {
        ProductionTurnPrivacy.ELIGIBLE -> TherapyTurnPrivacy.ELIGIBLE
        ProductionTurnPrivacy.PRIVATE -> TherapyTurnPrivacy.PRIVATE
    }

    private companion object {
        val SESSION_ID = TherapySessionId.parse("android-session")
        val CANONICAL_ALLOCATION_KEY = Regex(
            "(?:journal\\.commit\\.commit-android-journal-|" +
                "biographer\\.answer\\.answer-android-biographer-|" +
                "therapy\\.capture\\.capture-android-therapy-|" +
                "android-source-revision-|" +
                "android-lifecycle-(?:make-private|request-eligible-review|delete)-)([1-9][0-9]*)",
        )
    }
}
