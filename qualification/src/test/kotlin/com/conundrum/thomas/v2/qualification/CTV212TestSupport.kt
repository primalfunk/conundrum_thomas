package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.contextpacket.ContextPacket
import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildResult
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.DependencyRole
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.ThomasHypothesis
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import com.conundrum.thomas.v2.qualification.therapy.GovernedLongitudinalTherapyPipeline
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.retrieval.RetrievalArchiveSnapshot
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalObjectType
import com.conundrum.thomas.v2.retrieval.RetrievalReason
import com.conundrum.thomas.v2.safety.ExplicitEmergencyCircumstance
import com.conundrum.thomas.v2.safety.SafetyEvidence
import com.conundrum.thomas.v2.safety.SafetyEvidenceOrigin
import com.conundrum.thomas.v2.safety.SafetyScopeInput
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyIntegrationEngine
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnCommand
import com.conundrum.thomas.v2.therapylongitudinal.LongitudinalTherapyTurnResult
import com.conundrum.thomas.v2.therapylongitudinal.TherapyContextPacketPort
import com.conundrum.thomas.v2.therapylongitudinal.TherapyIdempotencyKey
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessingDisposition
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessingOutcome
import com.conundrum.thomas.v2.therapylongitudinal.TherapyLanguageProcessor
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import com.conundrum.thomas.v2.therapylongitudinal.TherapySessionId
import com.conundrum.thomas.v2.therapylongitudinal.TherapySessionMemoryState
import com.conundrum.thomas.v2.therapylongitudinal.TherapySourceAdmissionOutcome
import com.conundrum.thomas.v2.therapylongitudinal.TherapySourceAdmissionPort
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnCaptureOrigin
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnId
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnPrivacy
import java.nio.file.Files
import java.time.Instant

internal object CTV212TestSupport {
    private val R = CTV211TestSupport
    val sessionId = TherapySessionId.parse("synthetic-session")
    val reportTime = ReportTime(Instant.parse("2039-12-31T23:59:00Z"))

    data class FakeOptions(
        val memoryIntent: TherapyMemoryIntent = TherapyMemoryIntent.ORDINARY,
        val anchors: RetrievalAnchors = RetrievalAnchors(predicateIds = setOf(PersonalConceptId.parse("topic.work"))),
        val explicitTarget: String? = null,
        val sessionState: TherapySessionMemoryState = TherapySessionMemoryState(sessionId),
        val reinvoked: Set<String> = emptySet(),
        val continuation: Set<String> = emptySet(),
        val changed: Set<String> = emptySet(),
        val turn: String = "turn-1",
        val text: String = "I am frustrated at work.",
        val origin: TherapyTurnCaptureOrigin = TherapyTurnCaptureOrigin.TYPED,
        val privacy: TherapyTurnPrivacy = TherapyTurnPrivacy.ELIGIBLE,
        val support: RequestedOrdinarySupport = RequestedOrdinarySupport.UNDERSTAND,
        val safety: SafetyScopeInput? = null,
        val budget: ContextBudget = ContextBudget(),
        val failCapture: Boolean = false,
        val throwCapture: Boolean = false,
        val throwLanguage: Boolean = false,
        val throwRetrieval: Boolean = false,
        val invalidatePacket: Boolean = false,
        val correctionTarget: ClaimReference? = null,
        val packetTransform: (ContextPacket) -> ContextPacket = { it },
    )

    fun emptyArchive(revision: Long = 1) = R.archive(revision = revision)

    fun workArchive(count: Int = 1, revision: Long = 1): RetrievalArchiveSnapshot = R.archive(
        CTV211TestSupport.EvidenceParts((1..count).map { index ->
            CTV211TestSupport.Claim(
                id = "memory-$index",
                text = if (index == 1) "I was frustrated about the work project." else "Unrelated archive item $index",
                concept = if (index == 1) "topic.work" else "topic.unrelated-$index",
                mode = when (index % 3) {
                    0 -> AcquisitionMode.THERAPIST_CONVERSATION
                    1 -> AcquisitionMode.JOURNAL
                    else -> AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE
                },
            )
        }),
        revision = revision,
        digestSeed = "work-$count-$revision",
    )

    fun privateWorkArchive(): RetrievalArchiveSnapshot {
        val claim = CTV211TestSupport.Claim("private-memory", "Private work memory", "topic.work")
        return R.archive(
            CTV211TestSupport.EvidenceParts(listOf(claim)),
            lifecycle = mapOf(
                R.lifecycle(
                    RetrievalObjectType.SOURCE_REVISION,
                    R.sourceRevisionId(claim).value,
                    RetrievalLifecycleStatus.PRIVATE_INELIGIBLE,
                    false,
                ),
            ),
            digestSeed = "private-work",
        )
    }

    fun hypothesisArchive(
        status: HypothesisStatus = HypothesisStatus.TENTATIVE,
        counterevidence: Boolean = false,
    ): RetrievalArchiveSnapshot {
        val claims = buildList {
            add(CTV211TestSupport.Claim("hypothesis-support", "I reported a work concern", "topic.work"))
            if (counterevidence) add(CTV211TestSupport.Claim("hypothesis-counter", "I also reported contrary work evidence", "topic.work"))
        }
        val hypothesis = ThomasHypothesis(
            HypothesisId.parse("hypothesis.work-view"),
            AssertionSubject.User,
            AssertionPredicate(PersonalConceptId.parse("topic.work"), PredicateSemantics.OTHER),
            AssertionValue.Text("A tentative synthetic work observation"),
            status,
            RecordTime(Instant.parse("2040-09-03T12:00:02Z")),
            "Synthetic evidence-bounded rationale",
        )
        val dependencies = buildList {
            add(HypothesisDependency(
                EvidenceRelationId.parse("dependency.work-support"),
                hypothesis.id,
                ClaimReference.Assertion(R.assertionId("hypothesis-support")),
                DependencyRole.SUPPORTS,
                "Direct synthetic support",
            ))
            if (counterevidence) add(HypothesisDependency(
                EvidenceRelationId.parse("dependency.work-counter"),
                hypothesis.id,
                ClaimReference.Assertion(R.assertionId("hypothesis-counter")),
                DependencyRole.WEAKENS,
                "Direct synthetic counterevidence",
            ))
        }
        return R.archive(
            CTV211TestSupport.EvidenceParts(claims, hypotheses = listOf(hypothesis), dependencies = dependencies),
            digestSeed = "hypothesis-$status-$counterevidence",
        )
    }

    fun runFake(archive: RetrievalArchiveSnapshot, options: FakeOptions = FakeOptions()): LongitudinalTherapyTurnResult {
        val admission = TherapySourceAdmissionPort { request ->
            if (options.throwCapture) error("synthetic store unavailable")
            if (options.failCapture) {
                TherapySourceAdmissionOutcome(
                    AdmissionDisposition.REJECTED_VALIDATION,
                    request.stableSourceId,
                    null,
                    null,
                    null,
                    null,
                    emptyList(),
                    listOf("SYNTHETIC_CAPTURE_REJECTION"),
                    null,
                )
            } else {
                TherapySourceAdmissionOutcome(
                    AdmissionDisposition.ACCEPTED,
                    request.stableSourceId,
                    request.sourceRevisionId,
                    request.expectedStoreRevision,
                    request.expectedStoreRevision + 1,
                    RecordTime(Instant.parse("2040-09-03T12:00:01Z")),
                    listOf(request.sourceRevisionId.value),
                    listOf("SYNTHETIC_CAPTURE_ACCEPTED"),
                    R.sha256(request.exactUserText),
                )
            }
        }
        val language = TherapyLanguageProcessor {
            if (options.throwLanguage) error("synthetic perception unavailable")
            TherapyLanguageProcessingOutcome(
                TherapyLanguageProcessingDisposition.SOURCE_ONLY,
                null,
                emptyList(),
                0,
                archive.storeRevision + 1,
                R.sha256("formed-state-${archive.storeRevision}"),
            )
        }
        val packets = TherapyContextPacketPort { build ->
            if (options.throwRetrieval) error("synthetic retrieval unavailable")
            val result = R.packet(
                archive,
                build.retrieval,
                build.immediateConversation,
                build.runtimeState,
            )
            val packet = result.packet
            if (packet == null) result else {
                val transformed = options.packetTransform(packet)
                ContextPacketBuildResult(
                    result.disposition,
                    if (options.invalidatePacket) transformed.copy(
                        metadata = transformed.metadata.copy(storeRevision = transformed.metadata.storeRevision + 1),
                    ) else transformed,
                    result.reasonCodes,
                )
            }
        }
        val engine = LongitudinalTherapyIntegrationEngine(admission, language, packets)
        return engine.integrate(command(archive.storeRevision, options))
    }

    fun command(revision: Long, options: FakeOptions = FakeOptions()): LongitudinalTherapyTurnCommand {
        val state = CoreOrdinaryTestFixtures.state(
            id = "therapy-state-${options.turn}",
            support = options.support,
            revision = options.turn.filter(Char::isDigit).toLongOrNull() ?: 1,
        )
        return LongitudinalTherapyTurnCommand(
            sessionId,
            TherapyTurnId.parse(options.turn),
            TherapyIdempotencyKey.parse("key-${options.turn}"),
            revision,
            options.text,
            options.origin,
            options.privacy,
            reportTime,
            options.safety ?: QualificationSafetyGate.ordinaryInput(state.stateId, state.safetyEvidenceRevision),
            state,
            options.memoryIntent,
            options.anchors,
            options.explicitTarget,
            options.correctionTarget,
            options.budget,
            sessionMemoryState = options.sessionState,
            explicitReinvocationObjectIds = options.reinvoked,
            directContinuationObjectIds = options.continuation,
            materiallyChangedObjectIds = options.changed,
        )
    }

    fun strongMemoryOptions(turn: String = "turn-1") = FakeOptions(
        turn = turn,
        explicitTarget = "assertion.memory-1",
        packetTransform = withReason(RetrievalReason.EXPLICIT_TARGET),
    )

    fun withRealPipeline(block: (SyntheticLongitudinalStoreHarness, GovernedLongitudinalTherapyPipeline) -> Unit) {
        val directory = Files.createTempDirectory("ct-v2-12-")
        val harness = SyntheticLongitudinalStoreHarness(directory.resolve("therapy.sqlite"))
        try {
            block(harness, GovernedLongitudinalTherapyPipeline(harness.store))
        } finally {
            harness.close()
            Files.deleteIfExists(directory)
        }
    }

    fun realCommand(
        store: QualificationLongitudinalStore,
        turn: String = "turn-1",
        text: String = "I was furious yesterday.",
        origin: TherapyTurnCaptureOrigin = TherapyTurnCaptureOrigin.TYPED,
        privacy: TherapyTurnPrivacy = TherapyTurnPrivacy.ELIGIBLE,
    ) = command(
        store.reader.currentStoreRevision(),
        FakeOptions(turn = turn, text = text, origin = origin, privacy = privacy),
    )

    fun withReason(reason: RetrievalReason): (ContextPacket) -> ContextPacket = { packet ->
        packet.copy(
            longitudinal = packet.longitudinal.copy(
                items = packet.longitudinal.items.mapIndexed { index, item ->
                    if (index == 0) item.copy(retrievedBecause = listOf(reason)) else item
                },
            ),
        )
    }

    fun emergencyFor(turn: String): SafetyScopeInput {
        val state = CoreOrdinaryTestFixtures.state(id = "therapy-state-$turn", support = RequestedOrdinarySupport.UNDERSTAND)
        return QualificationSafetyGate.ordinaryInput(state.stateId, state.safetyEvidenceRevision).copy(
            currentEmergency = SafetyEvidence.established(
                ExplicitEmergencyCircumstance.OTHER_EMERGENCY_EXPLICITLY_ESTABLISHED,
                SafetyEvidenceOrigin.DIRECT_USER_REPORT,
                "current-turn-emergency",
            ),
        )
    }
}
