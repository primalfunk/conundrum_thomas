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
    private var therapyMemory = TherapySessionMemoryState(SESSION_ID)
    private val therapyActionHistory = mutableListOf<CoreActionExecution>()
    private var rendererCalls = 0L
    private var assistantArtifacts = 0L
    private var closed = false

    val availability: ProductionRuntimeAvailability
        get() = if (closed) ProductionRuntimeAvailability.CLOSED else ProductionRuntimeAvailability.READY

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

    fun nextBiographerPrompt(clientTurnIndex: Long): ProductionBiographerPrompt? {
        check(!closed)
        val decision = biographer.decide(
            CoverageRequest(BiographerPosture.OPEN_STORY),
            biographerHistory,
        )
        val plan = decision.plan ?: return null
        biographerHistory = decision.resultingHistory
        pendingBiographerPlan = plan
        val grounding = biographerGrounding(plan)
        val command = BiographerRenderCommandAdapter.adapt(
            renderId("biographer-prompt", clientTurnIndex),
            clientTurnIndex.toInt(),
            plan,
            grounding,
        ).forProduction()
        val rendered = render(command)
        val text = rendered.finalText ?: return null
        assistantArtifacts += 1
        return ProductionBiographerPrompt(text, rendered, plan.targetId?.value)
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
        val plan = pendingBiographerPlan ?: biographer.decide(
            CoverageRequest(BiographerPosture.OPEN_STORY),
            biographerHistory,
        ).also {
            biographerHistory = it.resultingHistory
        }.plan
        if (plan == null) {
            return baseResult(request, ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT, "BIOGRAPHER_NO_TARGET")
        }
        val capture = biographer.captureAnswer(
            BiographerAnswerCommand(
                BiographerAnswerId.parse(identity),
                BiographerIdempotencyKey.parse("answer-$identity"),
                store.reader.currentStoreRevision(),
                plan,
                request.committedText,
                request.inputOrigin.toBiographerOrigin(),
                request.privacy.toBiographerPrivacy(),
                ReportTime(request.committedAt),
                BiographerQualificationAuthority.ANDROID_PRODUCTION,
            ),
        )
        pendingBiographerPlan = null
        val receipt = capture.receipt ?: return result(
            request,
            identity,
            null,
            null,
            null,
            null,
            ProductionTurnDisposition.SOURCE_CAPTURE_FAILED,
            capture.reasonCodes,
        )
        val prompt = nextBiographerPrompt(request.clientTurnIndex + 1)
        if (prompt == null) {
            return result(
                request,
                identity,
                receipt.stableSourceId,
                null,
                null,
                null,
                ProductionTurnDisposition.GOVERNED_POLICY_NO_OUTPUT,
                capture.reasonCodes + "BIOGRAPHER_NEXT_TARGET_UNAVAILABLE",
            )
        }
        return result(
            request,
            identity,
            receipt.stableSourceId,
            artifact(identity, request.mode, prompt.renderResult),
            prompt.renderResult,
            null,
            ProductionTurnDisposition.COMPLETED,
            capture.reasonCodes,
        )
    }

    private fun submitTherapy(request: ProductionTurnRequest): ProductionTurnResult {
        val identity = identity("therapy", request)
        val revision = request.clientTurnIndex.coerceAtMost(Long.MAX_VALUE)
        val stateId = "android-therapy-state-$revision"
        val therapyState = ProductionTherapyInputBoundary.state(
            stateId,
            revision,
            request.committedText,
            request.requestedTherapySupport,
            therapyActionHistory.toList(),
        )
        val safetyInput = ProductionTherapyInputBoundary.safety(
            stateId,
            therapyState.safetyEvidenceRevision,
            request.therapySafetyDeclaration,
        )
        val safety = safetyGate.govern(safetyInput)
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
                retrievalAnchors = anchors(request.committedText, preTurnRevision),
                sessionMemoryState = therapyMemory,
                authority = TherapyIntegrationAuthority.ANDROID_PRODUCTION,
            ),
        )
        val plan = integrated.plan
        plan?.let {
            therapyMemory = it.nextSessionMemoryState
            val route = it.routeDecision?.route
            val action = it.routeDecision?.selectedActionId
            if (route != null && action != null) {
                therapyActionHistory += CoreActionExecution(
                    PolicyActionId.parse(action),
                    therapyState.conversationRevision,
                    route,
                )
            }
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
            return result(request, identity, source, null, null, plan, disposition, integrated.reasonCodes)
        }
        val rendered = render(command)
        return renderedResult(request, identity, source, rendered, plan, integrated.reasonCodes)
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

    private fun anchors(text: String, revision: Long): RetrievalAnchors {
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
        return RetrievalAnchors(entityIds = explicitEntities, lexicalTerms = terms)
    }

    private fun biographerGrounding(plan: BiographerQuestionPlan): RenderableGrounding {
        val meaning = plan.safeFacts.firstOrNull()?.concept
            ?: plan.targetId?.value?.replace('-', ' ')
            ?: "your history"
        return RenderableGrounding(
            id = "biographer-grounding",
            surfaceMeaning = meaning,
            requiredMarkerGroups = listOf(setOf("history", meaning.lowercase(Locale.ROOT))),
            temporalScope = plan.safeFacts.firstOrNull()?.temporalExpression,
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
    }
}
