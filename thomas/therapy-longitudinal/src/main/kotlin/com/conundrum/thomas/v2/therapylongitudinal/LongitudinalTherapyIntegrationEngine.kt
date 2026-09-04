package com.conundrum.thomas.v2.therapylongitudinal

import com.conundrum.thomas.v2.contextpacket.ContextPacket
import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildRequest
import com.conundrum.thomas.v2.contextpacket.ContextPacketDisposition
import com.conundrum.thomas.v2.contextpacket.ImmediateConversationItem
import com.conundrum.thomas.v2.contextpacket.ImmediateTurnRole
import com.conundrum.thomas.v2.contextpacket.ModeAuthorityContract
import com.conundrum.thomas.v2.contextpacket.ModeAuthorityState
import com.conundrum.thomas.v2.contextpacket.SafetyConstraintRef
import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDecision
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.CoreRenderRequestFactory
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.retrieval.CT_V2_11_RETRIEVAL_POLICY_VERSION
import com.conundrum.thomas.v2.retrieval.RetrievalIntent
import com.conundrum.thomas.v2.retrieval.RetrievalMode
import com.conundrum.thomas.v2.retrieval.RetrievalRequest
import com.conundrum.thomas.v2.retrieval.RetrievalRequestId
import com.conundrum.thomas.v2.safety.CT_V2_04_SAFETY_SCOPE_POLICY_VERSION
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyScopeDecision
import com.conundrum.thomas.v2.safety.SafetyScopeGate
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * CT-V2-12's qualification-only composition authority. Source capture is attempted before current
 * safety evaluation; safety and CT-V2-05 route selection finish before this class can call the
 * CT-V2-11 packet port. Retrieval state is never an input to either decision.
 */
class LongitudinalTherapyIntegrationEngine(
    private val admission: TherapySourceAdmissionPort,
    private val language: TherapyLanguageProcessor,
    private val contextPackets: TherapyContextPacketPort,
    private val safetyGate: SafetyScopeGate = SafetyScopeGate(),
    private val therapyPolicy: CoreOrdinaryTherapyEvaluator = CoreOrdinaryTherapyEvaluator(),
    private val memoryGate: DeterministicTherapyMemoryUseGate = DeterministicTherapyMemoryUseGate(),
) {
    fun integrate(command: LongitudinalTherapyTurnCommand): LongitudinalTherapyTurnResult {
        validate(command)?.let { (disposition, code) ->
            return LongitudinalTherapyTurnResult(disposition, null, listOf(code))
        }

        val trace = mutableListOf(TherapyIntegrationStep.PRE_TURN_HISTORY_REVISION_ESTABLISHED)
        trace += TherapyIntegrationStep.CURRENT_SOURCE_CAPTURE_ATTEMPTED
        val capture = capture(command, trace)

        val safety = safetyGate.govern(command.safetyInput)
        trace += TherapyIntegrationStep.CURRENT_SAFETY_EVALUATED
        val safetyRef = safety.reference()
        if (safety.authorityState != SafetyAuthorityState.ORDINARY_POLICY_ALLOWED) {
            return resultWithPlan(
                LongitudinalTherapyTurnDisposition.SAFETY_PREEMPTED,
                command,
                capture,
                safetyRef,
                route = null,
                context = null,
                memoryDisposition = TherapyMemoryUseDisposition.SUPPRESSED_BY_SAFETY,
                memories = emptyList(),
                nextSession = command.sessionMemoryState,
                degradation = capture.degradation + TherapyDegradationDisposition.MEMORY_REFERENCE_SUPPRESSED,
                trace = trace,
                reasons = capture.reasonCodes + "CURRENT_SAFETY_AUTHORITY_PREEMPTED_ORDINARY_THERAPY_AND_RETRIEVAL",
            )
        }

        val routeDecision = therapyPolicy.evaluate(command.therapyState, requireNotNull(safety.ordinaryTherapyPermit))
        trace += TherapyIntegrationStep.CT_V2_05_ROUTE_SELECTED
        val routeRef = routeDecision.reference()
        if (routeDecision.disposition != CorePolicyDisposition.ACTION_SELECTED) {
            return resultWithPlan(
                LongitudinalTherapyTurnDisposition.POLICY_DID_NOT_SELECT_ACTION,
                command,
                capture,
                safetyRef,
                routeRef,
                null,
                TherapyMemoryUseDisposition.SUPPRESSED_BY_POLICY,
                emptyList(),
                command.sessionMemoryState,
                capture.degradation + TherapyDegradationDisposition.MEMORY_REFERENCE_SUPPRESSED,
                trace,
                capture.reasonCodes + "CT_V2_05_DID_NOT_SELECT_RENDERABLE_ACTION",
            )
        }

        if (!capture.memoryIntegrationPermitted) {
            return resultWithPlan(
                LongitudinalTherapyTurnDisposition.MEMORYLESS_THERAPY,
                command,
                capture,
                safetyRef,
                routeRef,
                null,
                TherapyMemoryUseDisposition.LONGITUDINAL_CONTEXT_UNAVAILABLE,
                emptyList(),
                command.sessionMemoryState,
                capture.degradation + TherapyDegradationDisposition.BASE_THERAPY_PLAN_AVAILABLE,
                trace,
                capture.reasonCodes + "BASE_THERAPY_PLAN_AVAILABLE_WITHOUT_LONGITUDINAL_CONTEXT",
                routeDecision,
            )
        }

        val packetResult = try {
            contextPackets.build(packetRequest(command, safety))
        } catch (_: RuntimeException) {
            return resultWithPlan(
                LongitudinalTherapyTurnDisposition.MEMORYLESS_THERAPY,
                command,
                capture,
                safetyRef,
                routeRef,
                null,
                TherapyMemoryUseDisposition.LONGITUDINAL_CONTEXT_UNAVAILABLE,
                emptyList(),
                command.sessionMemoryState,
                capture.degradation + setOf(
                    TherapyDegradationDisposition.LONGITUDINAL_RETRIEVAL_UNAVAILABLE,
                    TherapyDegradationDisposition.BASE_THERAPY_PLAN_AVAILABLE,
                ),
                trace,
                capture.reasonCodes + "CT_V2_11_PACKET_PORT_UNAVAILABLE",
                routeDecision,
            )
        }
        trace += TherapyIntegrationStep.CT_V2_11_RETRIEVAL_EXECUTED
        val packet = packetResult.packet
        if (packet == null || !validPacket(packet, command, capture)) {
            return resultWithPlan(
                LongitudinalTherapyTurnDisposition.MEMORYLESS_THERAPY,
                command,
                capture,
                safetyRef,
                routeRef,
                null,
                TherapyMemoryUseDisposition.LONGITUDINAL_CONTEXT_UNAVAILABLE,
                emptyList(),
                command.sessionMemoryState,
                capture.degradation + setOf(
                    TherapyDegradationDisposition.INVALID_CONTEXT_PACKET,
                    TherapyDegradationDisposition.BASE_THERAPY_PLAN_AVAILABLE,
                ),
                trace,
                capture.reasonCodes + packetResult.reasonCodes + "INVALID_OR_UNAVAILABLE_CONTEXT_PACKET",
                routeDecision,
            )
        }

        val memory = memoryGate.decide(
            TherapyMemoryGateRequest(
                packet = packet,
                memoryIntent = command.memoryIntent,
                selectedAction = requireNotNull(routeDecision.selectedAction),
                sessionState = command.sessionMemoryState,
                turnId = command.turnId,
                explicitReinvocationObjectIds = command.explicitReinvocationObjectIds,
                directContinuationObjectIds = command.directContinuationObjectIds,
                materiallyChangedObjectIds = command.materiallyChangedObjectIds,
            ),
        )
        trace += TherapyIntegrationStep.MEMORY_USE_GATE_APPLIED
        val degradation = buildSet {
            addAll(capture.degradation)
            if (memory.disposition == TherapyMemoryUseDisposition.NO_RELEVANT_MEMORY) {
                add(TherapyDegradationDisposition.NO_ELIGIBLE_CONTEXT)
            }
            if (memory.disposition == TherapyMemoryUseDisposition.CONTEXT_AVAILABLE_NOT_SURFACED) {
                add(TherapyDegradationDisposition.MEMORY_USE_REJECTED)
            }
            if (memory.disposition in setOf(
                    TherapyMemoryUseDisposition.SUPPRESSED_BY_POLICY,
                    TherapyMemoryUseDisposition.CONTEXT_AVAILABLE_NOT_SURFACED,
                )
            ) add(TherapyDegradationDisposition.MEMORY_REFERENCE_SUPPRESSED)
        }
        return resultWithPlan(
            LongitudinalTherapyTurnDisposition.COMPLETE_LONGITUDINAL_THERAPY_SUCCESS,
            command,
            capture,
            safetyRef,
            routeRef,
            packet,
            memory.disposition,
            memory.surfacedMemories,
            memory.nextSessionState,
            degradation,
            trace,
            capture.reasonCodes + packetResult.reasonCodes + memory.reasonCodes,
            routeDecision,
        )
    }

    private data class CaptureStage(
        val disposition: TherapyTurnCaptureDisposition,
        val receipt: TherapyTurnCaptureReceipt?,
        val memoryIntegrationPermitted: Boolean,
        val degradation: Set<TherapyDegradationDisposition>,
        val reasonCodes: List<String>,
    )

    private fun capture(
        command: LongitudinalTherapyTurnCommand,
        trace: MutableList<TherapyIntegrationStep>,
    ): CaptureStage {
        val stable = command.turnId.stableSourceId(command.sessionId)
        val revision = command.turnId.sourceRevision(command.sessionId)
        val outcome = try {
            admission.admitSource(
                TherapySourceAdmissionRequest(
                    command.sessionId,
                    command.turnId,
                    stable,
                    revision,
                    command.idempotencyKey,
                    command.expectedStoreRevision,
                    command.exactUserText,
                    command.captureOrigin,
                    command.privacy,
                    command.reportTime,
                ),
            )
        } catch (_: RuntimeException) {
            return CaptureStage(
                TherapyTurnCaptureDisposition.STORE_FAILURE_WITHOUT_COMMIT,
                null,
                false,
                setOf(TherapyDegradationDisposition.CURRENT_SOURCE_CAPTURE_FAILED),
                listOf("THERAPY_SOURCE_PORT_FAILURE"),
            )
        }
        if (outcome.disposition !in setOf(AdmissionDisposition.ACCEPTED, AdmissionDisposition.IDEMPOTENT_REPLAY)) {
            return CaptureStage(
                mapCaptureFailure(outcome.disposition),
                null,
                false,
                setOf(TherapyDegradationDisposition.CURRENT_SOURCE_CAPTURE_FAILED),
                outcome.reasonCodes.ifEmpty { listOf("THERAPY_SOURCE_ADMISSION_REJECTED") },
            )
        }
        if (outcome.priorStoreRevision != command.expectedStoreRevision || outcome.sourceRevisionId != revision) {
            return CaptureStage(
                TherapyTurnCaptureDisposition.SOURCE_ADMISSION_FAILED,
                null,
                false,
                setOf(TherapyDegradationDisposition.CURRENT_SOURCE_CAPTURE_FAILED),
                listOf("SOURCE_RECEIPT_HISTORY_BOUNDARY_MISMATCH"),
            )
        }

        val sourceStoreRevision = requireNotNull(outcome.resultingStoreRevision)
        val recordTime = RecordTime(requireNotNull(outcome.recordTime).value)
        if (command.privacy == TherapyTurnPrivacy.PRIVATE) {
            val receipt = captureReceipt(
                command,
                outcome,
                TherapyLanguageProcessingDisposition.SKIPPED_PRIVATE,
                emptyList(),
                sourceStoreRevision,
                recordTime,
            )
            return CaptureStage(captureDisposition(outcome), receipt, true, emptySet(), listOf("PRIVATE_SOURCE_EXCLUDED_FROM_DERIVATION"))
        }

        trace += TherapyIntegrationStep.CURRENT_SOURCE_PROCESSING_ATTEMPTED
        val processed = try {
            language.process(TherapyLanguageProcessingRequest(revision, command.correctionTarget))
        } catch (_: RuntimeException) {
            val receipt = captureReceipt(
                command,
                outcome,
                TherapyLanguageProcessingDisposition.FAILED_AFTER_SOURCE_CAPTURE,
                emptyList(),
                sourceStoreRevision,
                recordTime,
            )
            return CaptureStage(
                TherapyTurnCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
                receipt,
                false,
                setOf(TherapyDegradationDisposition.CURRENT_SOURCE_PROCESSING_FAILED),
                listOf("LANGUAGE_PROCESSING_FAILURE_AFTER_THERAPY_SOURCE_CAPTURE"),
            )
        }
        val processingFailed = processed.disposition in setOf(
            TherapyLanguageProcessingDisposition.ADMISSION_REJECTED,
            TherapyLanguageProcessingDisposition.FAILED_AFTER_SOURCE_CAPTURE,
        )
        val receipt = captureReceipt(
            command,
            outcome,
            processed.disposition,
            processed.admittedEvidenceIds,
            processed.resultingStoreRevision,
            recordTime,
        )
        return if (processingFailed) {
            CaptureStage(
                TherapyTurnCaptureDisposition.EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE,
                receipt,
                false,
                setOf(TherapyDegradationDisposition.CURRENT_SOURCE_PROCESSING_FAILED),
                listOf("LANGUAGE_EVIDENCE_NOT_ADMITTED_AFTER_THERAPY_SOURCE_CAPTURE"),
            )
        } else {
            CaptureStage(captureDisposition(outcome), receipt, true, emptySet(), listOf("THERAPY_SOURCE_CAPTURED_BEFORE_POLICY"))
        }
    }

    private fun captureReceipt(
        command: LongitudinalTherapyTurnCommand,
        outcome: TherapySourceAdmissionOutcome,
        languageDisposition: TherapyLanguageProcessingDisposition,
        evidenceIds: List<String>,
        resultingRevision: Long,
        recordTime: RecordTime,
    ): TherapyTurnCaptureReceipt {
        val fingerprint = sha256(
            listOf(
                command.sessionId.value,
                command.turnId.value,
                outcome.stableSourceId.value,
                requireNotNull(outcome.sourceRevisionId).value,
                command.captureOrigin.name,
                command.privacy.name,
                sha256(command.exactUserText),
                command.reportTime.value.toString(),
                recordTime.value.toString(),
                languageDisposition.name,
                evidenceIds.sorted().joinToString(","),
            ).joinToString("|"),
        )
        return TherapyTurnCaptureReceipt(
            outcome.stableSourceId,
            requireNotNull(outcome.sourceRevisionId),
            AcquisitionMode.THERAPIST_CONVERSATION,
            command.captureOrigin,
            command.privacy,
            outcome.disposition,
            languageDisposition,
            evidenceIds.distinct().sorted(),
            requireNotNull(outcome.priorStoreRevision),
            resultingRevision,
            recordTime,
            fingerprint,
        )
    }

    private fun packetRequest(
        command: LongitudinalTherapyTurnCommand,
        safety: SafetyScopeDecision,
    ): ContextPacketBuildRequest {
        val intent = when (command.memoryIntent) {
            TherapyMemoryIntent.ORDINARY -> RetrievalIntent.ORDINARY_MODE_CONTEXT
            TherapyMemoryIntent.EXPLAIN_THOMAS_VIEW -> RetrievalIntent.EXPLAIN_DERIVED_OBJECT
            TherapyMemoryIntent.EXPLICIT_RECALL -> if (
                command.retrievalAnchors.sourceIdentityIds.isNotEmpty() ||
                command.retrievalAnchors.sourceRevisionIds.isNotEmpty()
            ) RetrievalIntent.EXPLICIT_SOURCE_RECALL else RetrievalIntent.ORDINARY_MODE_CONTEXT
        }
        val retrieval = RetrievalRequest(
            requestId = RetrievalRequestId.parse("therapy.${command.sessionId.value}.${command.turnId.value}"),
            intent = intent,
            activeMode = RetrievalMode.THERAPY,
            snapshotRevision = command.expectedStoreRevision,
            policyVersion = CT_V2_11_RETRIEVAL_POLICY_VERSION,
            anchors = command.retrievalAnchors,
            budget = command.contextBudget,
            explicitTargetId = command.explicitMemoryTargetId,
            explicitlyUserDirected = command.memoryIntent != TherapyMemoryIntent.ORDINARY,
        )
        val immediate = command.priorImmediateConversation + ImmediateConversationItem(
            turnId = command.turnId.value,
            role = ImmediateTurnRole.USER,
            content = command.exactUserText,
        )
        return ContextPacketBuildRequest(
            retrieval = retrieval,
            modeAuthority = ModeAuthorityContract(
                RetrievalMode.THERAPY,
                ModeAuthorityState.ORDINARY_THERAPY_QUALIFICATION_ONLY,
                CT_V2_12_INTEGRATION_POLICY_VERSION,
            ),
            safetyConstraints = listOf(
                SafetyConstraintRef(
                    safety.decisionReference,
                    safety.policyVersion,
                    safety.authorityState.name,
                ),
            ),
            immediateConversation = immediate,
            runtimeState = command.runtimeState,
        )
    }

    private fun validPacket(
        packet: ContextPacket,
        command: LongitudinalTherapyTurnCommand,
        capture: CaptureStage,
    ): Boolean {
        if (packet.metadata.storeRevision != command.expectedStoreRevision) return false
        if (packet.metadata.activeMode != RetrievalMode.THERAPY) return false
        if (packet.authority.modeContract.state != ModeAuthorityState.ORDINARY_THERAPY_QUALIFICATION_ONLY) return false
        if (packet.authority.retrievedTextHasInstructionAuthority) return false
        val currentRevision = capture.receipt?.sourceRevisionId
        if (currentRevision != null) {
            if (packet.longitudinal.items.any { currentRevision in it.sourceRevisionIds }) return false
            if (packet.excerpts.excerpts.any { it.sourceRevisionId == currentRevision }) return false
        }
        return true
    }

    private fun resultWithPlan(
        disposition: LongitudinalTherapyTurnDisposition,
        command: LongitudinalTherapyTurnCommand,
        capture: CaptureStage,
        safety: TherapySafetyDecisionReference,
        route: TherapyRouteDecisionReference?,
        context: ContextPacket?,
        memoryDisposition: TherapyMemoryUseDisposition,
        memories: List<TherapyMemoryReference>,
        nextSession: TherapySessionMemoryState,
        degradation: Set<TherapyDegradationDisposition>,
        trace: MutableList<TherapyIntegrationStep>,
        reasons: List<String>,
        fullRoute: CorePolicyDecision? = null,
    ): LongitudinalTherapyTurnResult {
        val renderSupport = fullRoute?.takeIf { it.disposition == CorePolicyDisposition.ACTION_SELECTED }?.let { decision ->
            val base = CoreRenderRequestFactory.create(decision, command.therapyState)
            TherapyRenderSupportEnvelope(
                command = base.command,
                currentTurnId = command.turnId,
                currentUserText = command.exactUserText,
                policySupportingText = base.authorizedSupportingText,
                surfacedMemorySupport = memories,
            )
        }
        val summary = context?.let {
            PlannerLongitudinalContextSummary(
                it.metadata.packetDigest,
                if (it.longitudinal.items.isEmpty() && it.excerpts.excerpts.isEmpty()) ContextPacketDisposition.EMPTY else ContextPacketDisposition.BUILT,
                it.metadata.storeRevision,
                it.metadata.candidateCount,
                it.metadata.selectedCount,
                it.metadata.exclusions.privateCount,
                it.metadata.exclusions.lifecycleCount,
            )
        }
        val finalTrace = trace.toList() + TherapyIntegrationStep.PLAN_CONSTRUCTED
        val digest = planDigest(command, capture, safety, route, summary, memoryDisposition, memories, renderSupport, degradation, finalTrace, nextSession)
        val plan = LongitudinalTherapyPlan(
            command.sessionId,
            command.turnId,
            capture.disposition,
            capture.receipt,
            command.expectedStoreRevision,
            safety,
            route,
            command.memoryIntent,
            summary,
            memoryDisposition,
            memories,
            renderSupport,
            degradation,
            finalTrace,
            nextSession,
            canonicalPlanDigest = digest,
        )
        return LongitudinalTherapyTurnResult(disposition, plan, reasons.distinct().sorted())
    }

    private fun planDigest(
        command: LongitudinalTherapyTurnCommand,
        capture: CaptureStage,
        safety: TherapySafetyDecisionReference,
        route: TherapyRouteDecisionReference?,
        context: PlannerLongitudinalContextSummary?,
        memoryDisposition: TherapyMemoryUseDisposition,
        memories: List<TherapyMemoryReference>,
        renderSupport: TherapyRenderSupportEnvelope?,
        degradation: Set<TherapyDegradationDisposition>,
        trace: List<TherapyIntegrationStep>,
        nextSession: TherapySessionMemoryState,
    ): String = sha256(buildString {
        append(CT_V2_12_INTEGRATION_POLICY_VERSION).append('|')
        append(command.sessionId.value).append('|').append(command.turnId.value).append('|')
        append(command.expectedStoreRevision).append('|').append(sha256(command.exactUserText)).append('|')
        append(command.captureOrigin).append('|').append(command.privacy).append('|').append(command.reportTime.value).append('|')
        append(capture.disposition).append('|').append(capture.receipt?.canonicalCaptureFingerprint ?: "NO_CAPTURE").append('|')
        append(safety.policyVersion).append('|').append(safety.decisionReference).append('|').append(safety.authorityState).append('|')
        append(route?.policyVersion ?: "NO_ROUTE").append('|').append(route?.decisionReference ?: "NO_ROUTE").append('|')
        append(route?.route ?: "NO_ROUTE").append('|').append(route?.selectedActionId ?: "NO_ACTION").append('|')
        append(route?.progression ?: "NO_PROGRESSION").append('|').append(command.memoryIntent).append('|')
        append(context?.packetDigest ?: "NO_PACKET").append('|').append(memoryDisposition).append('|')
        memories.forEach { memory ->
            append(memory.stableObjectId).append(':').append(memory.relation).append(':').append(memory.semanticAct)
                .append(':').append(memory.lifecycle).append(':').append(memory.currentAuthority).append(':')
                .append(memory.sourceRevisionIds.joinToString(",") { it.value }).append('|')
        }
        append(renderSupport?.command?.selectedPolicyActionId ?: "NO_RENDER").append('|')
        append(degradation.map { it.name }.sorted().joinToString(",")).append('|')
        append(trace.joinToString(",") { it.name }).append('|')
        nextSession.surfaced.sortedWith(compareBy<SurfacedMemoryHistoryEntry> { it.stableObjectId }.thenBy { it.surfacedOnTurnId.value })
            .forEach { append(it.stableObjectId).append(':').append(it.surfacedOnTurnId.value).append(':').append(it.connectionRejected).append('|') }
    })

    private fun SafetyScopeDecision.reference() = TherapySafetyDecisionReference(
        policyVersion,
        decisionReference,
        stateId,
        evidenceRevision.value,
        authorityState,
        ordinaryTherapyPermit?.gateDecisionReference,
    )

    private fun CorePolicyDecision.reference() = TherapyRouteDecisionReference(
        policyVersion,
        decisionReference,
        disposition,
        routeSelection.selectedRoute,
        selectedAction?.definition?.id?.value,
        selectedAction?.definition?.dialogueActId?.value,
        selectedAction?.definition?.goalId?.value,
        progressionTrace?.disposition,
        conversationRevision,
    )

    private fun validate(command: LongitudinalTherapyTurnCommand): Pair<LongitudinalTherapyTurnDisposition, String>? = when {
        command.authority != TherapyIntegrationAuthority.SYNTHETIC_QUALIFICATION_ONLY ->
            LongitudinalTherapyTurnDisposition.REJECTED_AUTHORITY to "PRODUCTION_LONGITUDINAL_THERAPY_AUTHORITY_NOT_GRANTED"
        command.exactUserText.isBlank() ->
            LongitudinalTherapyTurnDisposition.REJECTED_INVALID_COMMAND to "EMPTY_THERAPY_USER_TURN"
        command.therapyState.mode != ThomasMode.THERAPIST ->
            LongitudinalTherapyTurnDisposition.REJECTED_INVALID_COMMAND to "THERAPIST_MODE_REQUIRED"
        command.safetyInput.stateId != command.therapyState.stateId ->
            LongitudinalTherapyTurnDisposition.REJECTED_INVALID_COMMAND to "SAFETY_AND_THERAPY_STATE_ID_MISMATCH"
        command.safetyInput.evidenceRevision != command.therapyState.safetyEvidenceRevision ->
            LongitudinalTherapyTurnDisposition.REJECTED_INVALID_COMMAND to "SAFETY_AND_THERAPY_REVISION_MISMATCH"
        command.memoryIntent == TherapyMemoryIntent.EXPLAIN_THOMAS_VIEW && command.retrievalAnchors.hypothesisIds.isEmpty() ->
            LongitudinalTherapyTurnDisposition.REJECTED_INVALID_COMMAND to "EXPLANATION_REQUIRES_EXPLICIT_HYPOTHESIS_TARGET"
        else -> null
    }

    private fun mapCaptureFailure(disposition: AdmissionDisposition): TherapyTurnCaptureDisposition = when (disposition) {
        AdmissionDisposition.REJECTED_AUTHORITY -> TherapyTurnCaptureDisposition.REJECTED_AUTHORITY
        AdmissionDisposition.REJECTED_IDEMPOTENCY_CONFLICT -> TherapyTurnCaptureDisposition.REJECTED_IDEMPOTENCY_CONFLICT
        AdmissionDisposition.FAILED_WITHOUT_COMMIT -> TherapyTurnCaptureDisposition.STORE_FAILURE_WITHOUT_COMMIT
        else -> TherapyTurnCaptureDisposition.SOURCE_ADMISSION_FAILED
    }

    private fun captureDisposition(outcome: TherapySourceAdmissionOutcome) = when (outcome.disposition) {
        AdmissionDisposition.IDEMPOTENT_REPLAY -> TherapyTurnCaptureDisposition.IDEMPOTENT_REPLAY
        else -> TherapyTurnCaptureDisposition.CAPTURED
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
