package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildRequest
import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildResult
import com.conundrum.thomas.v2.contextpacket.DeterministicContextPacketBuilder
import com.conundrum.thomas.v2.contextpacket.ModeAuthorityContract
import com.conundrum.thomas.v2.contextpacket.ModeAuthorityState
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PersonalEvidenceProvenance
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.SourceSpanGrounding
import com.conundrum.thomas.v2.longitudinal.ThomasHypothesis
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.sourceTextSha256
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.DeterministicLongitudinalRetriever
import com.conundrum.thomas.v2.retrieval.LongitudinalRetrievalReadPort
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.retrieval.RetrievalArchiveSnapshot
import com.conundrum.thomas.v2.retrieval.RetrievalAuthority
import com.conundrum.thomas.v2.retrieval.RetrievalIntent
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycle
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalMode
import com.conundrum.thomas.v2.retrieval.RetrievalObjectKey
import com.conundrum.thomas.v2.retrieval.RetrievalObjectType
import com.conundrum.thomas.v2.retrieval.RetrievalOpenQuestion
import com.conundrum.thomas.v2.retrieval.RetrievalRecurrenceCandidate
import com.conundrum.thomas.v2.retrieval.RetrievalRequest
import com.conundrum.thomas.v2.retrieval.RetrievalRequestId
import java.security.MessageDigest
import java.time.Instant

internal object CTV211TestSupport {
    data class Claim(
        val id: String,
        val text: String,
        val concept: String = "topic.work",
        val time: EventTime = EventTime.Unknown("Synthetic event time unknown"),
        val mode: AcquisitionMode = AcquisitionMode.JOURNAL,
        val epistemic: EvidenceEpistemicClass = EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION,
        val kind: UserEvidenceKind = UserEvidenceKind.EXPLICIT_USER_ASSERTION,
        val author: SourceAuthorRole = SourceAuthorRole.USER,
        val stableSource: String = "",
        val revision: Int = 1,
        val previousRevision: String? = null,
        val uncertainty: AssertionUncertainty = AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
        val value: AssertionValue? = null,
        val subject: AssertionSubject = AssertionSubject.User,
        val validGrounding: Boolean = true,
    )

    data class EvidenceParts(
        val claims: List<Claim> = emptyList(),
        val entities: List<LifeEntity> = emptyList(),
        val hypotheses: List<ThomasHypothesis> = emptyList(),
        val dependencies: List<HypothesisDependency> = emptyList(),
        val contradictions: List<ContradictionRelation> = emptyList(),
        val corrections: List<CorrectionRelation> = emptyList(),
        val identities: List<EntityIdentityLink> = emptyList(),
    )

    fun assertionId(id: String) = AssertionId.parse("assertion.$id")
    fun sourceRevisionId(claim: Claim) = SourceRecordId.parse("source.${claim.id}.rev${claim.revision}")
    fun sourceIdentityId(claim: Claim) = SourceIdentityId.parse(
        if (claim.stableSource.isBlank()) "source.${claim.id}" else "source.${claim.stableSource}",
    )

    fun evidence(parts: EvidenceParts): LongitudinalEvidenceSnapshot {
        val sources = parts.claims.map { claim ->
            val id = sourceRevisionId(claim)
            SourceRecord(
                id = id,
                provenance = PersonalEvidenceProvenance(
                    acquisitionMode = claim.mode,
                    sourceRevision = claim.revision,
                    previousRevisionId = claim.previousRevision?.let(SourceRecordId::parse),
                ),
                reportTime = ReportTime(Instant.parse("2040-09-03T12:00:00Z")),
                recordTime = RecordTime(Instant.parse("2040-09-03T12:00:01Z")),
                originalContent = OriginalSourceContent.Inline(claim.text),
                stableSourceId = sourceIdentityId(claim),
                eventTime = claim.time,
                authorRole = claim.author,
            )
        }
        val assertions = parts.claims.map { claim ->
            val sourceId = sourceRevisionId(claim)
            EvidenceAssertion(
                id = assertionId(claim.id),
                sourceRecordId = sourceId,
                subject = claim.subject,
                predicate = AssertionPredicate(PersonalConceptId.parse(claim.concept), PredicateSemantics.OTHER),
                value = claim.value ?: AssertionValue.Text(claim.text),
                kind = claim.kind,
                uncertainty = claim.uncertainty,
                eventTime = claim.time,
                epistemicClass = claim.epistemic,
                sourceGrounding = SourceSpanGrounding(
                    sourceRevisionId = sourceId,
                    startOffsetInclusive = 0,
                    endOffsetExclusive = claim.text.length,
                    exactFragment = claim.text,
                    sourceRevisionSha256 = if (claim.validGrounding) sourceTextSha256(claim.text) else "0".repeat(64),
                    extractionRuleVersion = "ct-v2-11.fixture.v1",
                ),
            )
        }
        return LongitudinalEvidenceSnapshot(
            sources = sources,
            assertions = assertions,
            entities = parts.entities,
            contradictions = parts.contradictions,
            corrections = parts.corrections,
            hypotheses = parts.hypotheses,
            hypothesisDependencies = parts.dependencies,
            identityLinks = parts.identities,
        ).requireValid()
    }

    fun archive(
        parts: EvidenceParts = EvidenceParts(),
        revision: Long = 1,
        lifecycle: Map<RetrievalObjectKey, RetrievalLifecycle> = emptyMap(),
        currentRevisionIds: Set<SourceRecordId>? = null,
        questions: List<RetrievalOpenQuestion> = emptyList(),
        recurrences: List<RetrievalRecurrenceCandidate> = emptyList(),
        digestSeed: String = "archive-$revision",
    ): RetrievalArchiveSnapshot {
        val evidence = evidence(parts)
        val current = currentRevisionIds ?: evidence.sources.groupBy { it.stableSourceId }.values
            .mapNotNull { it.maxByOrNull { source -> source.provenance.sourceRevision }?.id }.toSet()
        return RetrievalArchiveSnapshot(
            revision, evidence, lifecycle, current, questions, recurrences, sha256(digestSeed),
        )
    }

    fun lifecycle(
        type: RetrievalObjectType,
        id: String,
        status: RetrievalLifecycleStatus,
        eligible: Boolean = status in setOf(RetrievalLifecycleStatus.ACTIVE, RetrievalLifecycleStatus.CONTESTED),
        revision: Long = 1,
    ) = RetrievalObjectKey(type, id) to RetrievalLifecycle(status, eligible, "SYNTHETIC_FIXTURE_STATE", revision)

    class Port(private val snapshots: Map<Long, RetrievalArchiveSnapshot>) : LongitudinalRetrievalReadPort {
        override fun read(asOfRevision: Long): RetrievalArchiveSnapshot =
            snapshots[asOfRevision] ?: error("Revision $asOfRevision unavailable")
    }

    fun request(
        revision: Long = 1,
        intent: RetrievalIntent = RetrievalIntent.ORDINARY_MODE_CONTEXT,
        mode: RetrievalMode = RetrievalMode.THERAPY,
        anchors: RetrievalAnchors = RetrievalAnchors(predicateIds = setOf(PersonalConceptId.parse("topic.work"))),
        budget: ContextBudget = ContextBudget(),
        target: String? = null,
        userDirected: Boolean = false,
        authority: RetrievalAuthority = RetrievalAuthority.SYNTHETIC_QUALIFICATION_ONLY,
        id: String = "request.fixture",
    ) = RetrievalRequest(
        RetrievalRequestId.parse(id), intent, mode, revision, anchors = anchors, budget = budget,
        explicitTargetId = target, explicitlyUserDirected = userDirected, authority = authority,
    )

    fun retrieve(archive: RetrievalArchiveSnapshot, request: RetrievalRequest = request()) =
        DeterministicLongitudinalRetriever(Port(mapOf(archive.storeRevision to archive))).retrieve(request)

    fun packet(
        archive: RetrievalArchiveSnapshot,
        request: RetrievalRequest = request(revision = archive.storeRevision),
        immediate: List<com.conundrum.thomas.v2.contextpacket.ImmediateConversationItem> = emptyList(),
        runtime: List<com.conundrum.thomas.v2.contextpacket.RuntimeStateItem> = emptyList(),
    ): ContextPacketBuildResult {
        val authorityState = when (request.activeMode) {
            RetrievalMode.JOURNAL -> ModeAuthorityState.JOURNAL_CAPTURE_ONLY
            RetrievalMode.BIOGRAPHER -> ModeAuthorityState.BIOGRAPHER_INVESTIGATION_ONLY
            RetrievalMode.THERAPY -> ModeAuthorityState.ORDINARY_THERAPY_QUALIFICATION_ONLY
        }
        return DeterministicContextPacketBuilder(
            DeterministicLongitudinalRetriever(Port(mapOf(archive.storeRevision to archive))),
        ).build(
            ContextPacketBuildRequest(
                request,
                ModeAuthorityContract(request.activeMode, authorityState, "ct-v2-11.fixture-mode.v1"),
                immediateConversation = immediate,
                runtimeState = runtime,
            ),
        )
    }

    fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
