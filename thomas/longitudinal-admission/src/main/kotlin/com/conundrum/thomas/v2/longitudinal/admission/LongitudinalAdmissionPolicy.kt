package com.conundrum.thomas.v2.longitudinal.admission

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.Behavior
import com.conundrum.thomas.v2.longitudinal.CopingResponse
import com.conundrum.thomas.v2.longitudinal.Decision
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.LifeEvent
import com.conundrum.thomas.v2.longitudinal.LifePeriod
import com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot
import com.conundrum.thomas.v2.longitudinal.LongitudinalValidationIssue
import com.conundrum.thomas.v2.longitudinal.PersonalEvidenceProvenance
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.Place
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.Relationship
import com.conundrum.thomas.v2.longitudinal.Role
import com.conundrum.thomas.v2.longitudinal.Outcome
import com.conundrum.thomas.v2.longitudinal.SupersessionKind
import com.conundrum.thomas.v2.longitudinal.ThomasHypothesis
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.sourceTextSha256

/** Deterministic qualification policy. It plans immutable state; it cannot persist or issue receipts. */
class LongitudinalAdmissionPolicy(
    private val allowedClassification: StoreDataClassification = StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
) {
    fun plan(
        request: LongitudinalAdmissionRequest,
        current: LongitudinalAggregateState,
        trustedRecordTime: RecordTime,
    ): AdmissionPlanResult {
        if (request.policyVersion != AdmissionPolicyVersion.CT_V2_07_V1) {
            return rejected(AdmissionDisposition.REJECTED_AUTHORITY, "UNSUPPORTED_POLICY_VERSION")
        }
        if (request.classification != allowedClassification) {
            return rejected(AdmissionDisposition.REJECTED_AUTHORITY, "STORE_CLASSIFICATION_MISMATCH")
        }
        if (request.expectedStoreRevision != current.storeRevision) {
            return rejected(AdmissionDisposition.REJECTED_STALE_REVISION, "STALE_STORE_REVISION")
        }

        return try {
            val nextRevision = current.storeRevision + 1
            val mutation = when (val operation = request.operation) {
                is LongitudinalWriteOperation.AdmitSource -> admitSource(request, current, operation, trustedRecordTime, nextRevision)
                is LongitudinalWriteOperation.AppendSourceRevision -> appendSourceRevision(request, current, operation, trustedRecordTime, nextRevision)
                is LongitudinalWriteOperation.AdmitEvidenceBundle -> admitBundle(request, current, operation.bundle, trustedRecordTime, nextRevision)
                is LongitudinalWriteOperation.RecordUserCorrection -> recordCorrection(request, current, operation, trustedRecordTime, nextRevision)
                is LongitudinalWriteOperation.RecordSupersession -> recordSupersession(current, operation, nextRevision)
                is LongitudinalWriteOperation.RecordContradiction -> recordContradiction(current, operation, nextRevision)
                is LongitudinalWriteOperation.ReviseIdentityLink -> reviseIdentity(current, operation, nextRevision)
                is LongitudinalWriteOperation.ChangeCoverage -> changeCoverage(current, operation, nextRevision)
                is LongitudinalWriteOperation.ChangePrivacy -> changePrivacy(current, operation, nextRevision)
                is LongitudinalWriteOperation.RetireClaim -> retireClaim(current, operation, nextRevision)
                is LongitudinalWriteOperation.DeleteSource -> deleteSource(request, current, operation, nextRevision)
            }
            val validation = mutation.state.snapshot.validationIssues()
            if (validation.isNotEmpty()) rejectValidation(validation) else AdmissionPlanResult.Accepted(mutation)
        } catch (failure: AdmissionPolicyFailure) {
            rejected(failure.disposition, failure.code)
        } catch (_: IllegalArgumentException) {
            rejected(AdmissionDisposition.REJECTED_VALIDATION, "INVALID_STRUCTURED_PAYLOAD")
        }
    }

    private fun admitSource(
        request: LongitudinalAdmissionRequest,
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.AdmitSource,
        recordTime: RecordTime,
        revision: Long,
    ): PlannedLongitudinalMutation {
        val draft = operation.source
        requireSourceAuthority(request, draft)
        if (recordTime.value.isBefore(draft.reportTime.value)) {
            fail(AdmissionDisposition.REJECTED_TEMPORAL_DISHONESTY, "STORE_TIME_PRECEDES_REPORT_TIME")
        }
        if (current.sourceHistory(draft.stableSourceId).isNotEmpty() || current.snapshot.sources.any { it.id == draft.revisionId } ||
            current.lifecycle[ref(StoredObjectType.SOURCE_ID, draft.stableSourceId.value)]?.status == LongitudinalLifecycleStatus.DELETED
        ) {
            fail(AdmissionDisposition.REJECTED_VALIDATION, "DUPLICATE_SOURCE_ID")
        }
        val source = SourceRecord(
            id = draft.revisionId,
            provenance = PersonalEvidenceProvenance(draft.acquisitionMode, draft.interactionId, 1, metadata = draft.metadata),
            reportTime = draft.reportTime,
            recordTime = recordTime,
            originalContent = draft.originalContent,
            stableSourceId = draft.stableSourceId,
            eventTime = draft.eventTime,
            authorRole = draft.authorRole,
        )
        val ref = ref(StoredObjectType.SOURCE_REVISION, source.id.value)
        return mutation(
            current,
            revision,
            current.snapshot.copy(sources = current.snapshot.sources + source),
            sourcePrivacy = current.sourcePrivacy + (draft.stableSourceId to draft.privacy),
            lifecycle = current.lifecycle + mapOf(
                ref to active(revision, draft.privacy != SourcePrivacy.PRIVATE, "SOURCE_ADMITTED"),
                ref(StoredObjectType.SOURCE_ID, draft.stableSourceId.value) to
                    active(revision, draft.privacy != SourcePrivacy.PRIVATE, "SOURCE_ADMITTED"),
            ),
            affected = listOf(source.id.value, draft.stableSourceId.value),
            trace = listOf("CT07-AUTH-SYNTHETIC", "CT07-SOURCE-IMMUTABLE", "CT07-STORE-TIME"),
        )
    }

    private fun appendSourceRevision(
        request: LongitudinalAdmissionRequest,
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.AppendSourceRevision,
        recordTime: RecordTime,
        revision: Long,
    ): PlannedLongitudinalMutation {
        if (request.actor != AdmissionActor.USER) fail(AdmissionDisposition.REJECTED_AUTHORITY, "SOURCE_REVISION_REQUIRES_USER")
        val history = current.sourceHistory(operation.stableSourceId)
        val prior = history.lastOrNull() ?: fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "SOURCE_NOT_FOUND")
        if (prior.id != operation.priorRevisionId) fail(AdmissionDisposition.REJECTED_STALE_REVISION, "SOURCE_REVISION_NOT_CURRENT")
        if (current.snapshot.sources.any { it.id == operation.newRevisionId }) fail(AdmissionDisposition.REJECTED_VALIDATION, "DUPLICATE_SOURCE_REVISION_ID")
        if (operation.reportTime.value.isBefore(prior.reportTime.value)) fail(AdmissionDisposition.REJECTED_TEMPORAL_DISHONESTY, "REPORT_TIME_MOVED_BACKWARD")
        if (recordTime.value.isBefore(operation.reportTime.value)) fail(AdmissionDisposition.REJECTED_TEMPORAL_DISHONESTY, "STORE_TIME_PRECEDES_REPORT_TIME")
        val next = prior.copy(
            id = operation.newRevisionId,
            provenance = prior.provenance.copy(
                sourceRevision = prior.provenance.sourceRevision + 1,
                previousRevisionId = prior.id,
            ),
            reportTime = operation.reportTime,
            recordTime = recordTime,
            originalContent = operation.originalContent,
        )
        val oldRef = ref(StoredObjectType.SOURCE_REVISION, prior.id.value)
        val newRef = ref(StoredObjectType.SOURCE_REVISION, next.id.value)
        val lifecycle = current.lifecycle.toMutableMap()
        lifecycle[oldRef] = inactive(LongitudinalLifecycleStatus.SUPERSEDED, revision, "SOURCE_REVISION_APPENDED")
        lifecycle[newRef] = active(revision, current.isSourceEligible(operation.stableSourceId), "SOURCE_REVISION_APPENDED")
        current.snapshot.assertions.filter { it.sourceRecordId == prior.id }.forEach { assertion ->
            lifecycle[assertionRef(assertion.id)] = inactive(
                LongitudinalLifecycleStatus.REVIEW_REQUIRED,
                revision,
                "SOURCE_REVISION_APPENDED",
            )
            markDependentsOnly(
                ClaimReference.Assertion(assertion.id),
                "SOURCE_REVISION_APPENDED",
                current.snapshot,
                lifecycle,
                revision,
            )
        }
        return mutation(
            current,
            revision,
            current.snapshot.copy(sources = current.snapshot.sources + next),
            lifecycle = lifecycle,
            affected = listOf(prior.id.value, next.id.value, operation.stableSourceId.value),
            trace = listOf("CT07-SOURCE-APPEND-ONLY", "CT07-TEMPORAL-PRECISION-PRESERVED", "CT07-STORE-TIME"),
        )
    }

    private fun admitBundle(
        request: LongitudinalAdmissionRequest,
        current: LongitudinalAggregateState,
        bundle: EvidenceBundle,
        recordTime: RecordTime,
        revision: Long,
    ): PlannedLongitudinalMutation {
        if (bundle.assertions.isNotEmpty() && request.actor != AdmissionActor.USER) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "USER_ASSERTION_REQUIRES_USER_ACTOR")
        }
        if (bundle.hypothesisDrafts.isNotEmpty() && request.actor != AdmissionActor.THOMAS) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "HYPOTHESIS_REQUIRES_THOMAS_ACTOR")
        }
        if (bundle.assertions.any { it.kind == UserEvidenceKind.EXPLICIT_USER_ASSERTION } && request.origin == AdmissionOrigin.THOMAS_DERIVATION) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "THOMAS_CONTENT_CANNOT_BE_USER_ASSERTION")
        }
        if (bundle.coverageTopics.any { it.status == InformationCoverageStatus.DECLINED && it.sourceRecordIds.isNotEmpty() }) {
            fail(AdmissionDisposition.REJECTED_PRIVACY, "DECLINED_IS_NOT_EVIDENCE")
        }
        if (bundle.corrections.isNotEmpty() &&
            (request.actor != AdmissionActor.USER || request.origin != AdmissionOrigin.USER_CORRECTION)
        ) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "USER_CORRECTION_REQUIRES_USER_ORIGIN")
        }

        val hypotheses = bundle.hypothesisDrafts.map { draft ->
            ThomasHypothesis(draft.id, draft.subject, draft.predicate, draft.proposedValue, draft.status, recordTime, draft.rationale)
        }
        val snapshot = current.snapshot.copy(
            assertions = current.snapshot.assertions + bundle.assertions,
            entities = current.snapshot.entities + bundle.entities,
            contradictions = current.snapshot.contradictions + bundle.contradictions,
            corrections = current.snapshot.corrections + bundle.corrections,
            supersessions = current.snapshot.supersessions + bundle.supersessions,
            hypotheses = current.snapshot.hypotheses + hypotheses,
            hypothesisDependencies = current.snapshot.hypothesisDependencies + bundle.hypothesisDependencies,
            identityLinks = current.snapshot.identityLinks + bundle.identityLinks,
            coverageTopics = current.snapshot.coverageTopics + bundle.coverageTopics,
        )
        if (snapshot.validationIssues().any { it.code.contains("CYCLE") }) {
            fail(AdmissionDisposition.REJECTED_RELATION_CYCLE, "RELATION_CYCLE")
        }
        bundle.assertions.forEach { validateSourceGrounding(it, snapshot) }
        bundle.corrections.forEach { correction ->
            val correcting = snapshot.assertions.firstOrNull { it.id == correction.correctingAssertionId }
                ?: fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "CORRECTION_ASSERTION_NOT_FOUND")
            val source = snapshot.sources.firstOrNull { it.id == correcting.sourceRecordId }
                ?: fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "CORRECTION_SOURCE_NOT_FOUND")
            if (source.provenance.acquisitionMode != AcquisitionMode.USER_CORRECTION || source.authorRole != SourceAuthorRole.USER) {
                fail(AdmissionDisposition.REJECTED_AUTHORITY, "CORRECTION_SOURCE_AUTHORITY_INVALID")
            }
            if (correction.effect == CorrectionEffect.CORRECTS_AND_SUPERSEDES && snapshot.supersessions.none {
                    it.successor == ClaimReference.Assertion(correction.correctingAssertionId) &&
                        it.predecessor == ClaimReference.Assertion(correction.correctedAssertionId) &&
                        it.kind == SupersessionKind.CORRECTS
                }
            ) {
                fail(AdmissionDisposition.REJECTED_VALIDATION, "CORRECTION_SUPERSESSION_REQUIRED")
            }
        }
        val newAssertionIds = bundle.assertions.map { it.id }.toSet()
        val newHypothesisIds = hypotheses.map { it.id }.toSet()
        bundle.hypothesisDependencies.filter { it.dependentHypothesisId in newHypothesisIds }.forEach { dependency ->
            if (!hasUltimateEligibleSource(
                    dependency.prerequisite,
                    snapshot,
                    current.sourcePrivacy,
                    current.lifecycle,
                    newAssertionIds,
                    newHypothesisIds,
                    mutableSetOf(),
                )
            ) {
                fail(AdmissionDisposition.REJECTED_DEPENDENCY, "HYPOTHESIS_WITHOUT_ELIGIBLE_SOURCE_SUPPORT")
            }
        }
        val lifecycle = current.lifecycle.toMutableMap()
        bundle.assertions.forEach {
            lifecycle[assertionRef(it.id)] = active(
                revision,
                sourceEligible(it.sourceRecordId, snapshot, current.sourcePrivacy, current.lifecycle),
                "ASSERTION_ADMITTED",
            )
        }
        bundle.entities.forEach { lifecycle[ref(StoredObjectType.ENTITY, it.id.value)] = active(revision, true, "ENTITY_ADMITTED") }
        hypotheses.forEach { lifecycle[hypothesisRef(it.id)] = active(revision, true, "HYPOTHESIS_ADMITTED") }
        bundle.contradictions.forEach { lifecycle[ref(StoredObjectType.CONTRADICTION, it.id.value)] = active(revision, true, "CONTRADICTION_RECORDED") }
        bundle.corrections.forEach { lifecycle[ref(StoredObjectType.CORRECTION, it.id.value)] = active(revision, true, "USER_CORRECTION_RECORDED") }
        bundle.supersessions.forEach { lifecycle[ref(StoredObjectType.SUPERSESSION, it.id.value)] = active(revision, true, "SUPERSESSION_RECORDED") }
        bundle.hypothesisDependencies.forEach { lifecycle[ref(StoredObjectType.HYPOTHESIS_DEPENDENCY, it.id.value)] = active(revision, true, "DEPENDENCY_RECORDED") }
        bundle.identityLinks.forEach { lifecycle[ref(StoredObjectType.IDENTITY_DECISION, it.id.value)] = active(revision, true, "IDENTITY_UNRESOLVED") }
        bundle.coverageTopics.forEach { lifecycle[ref(StoredObjectType.COVERAGE, it.id.value)] = active(revision, it.status !in setOf(InformationCoverageStatus.PRIVATE, InformationCoverageStatus.DECLINED), "COVERAGE_RECORDED") }
        bundle.supersessions.forEach { markClaimAndDependents(it.predecessor, LongitudinalLifecycleStatus.SUPERSEDED, "EXPLICIT_SUPERSESSION", snapshot, lifecycle, revision) }
        bundle.corrections.filter { it.effect == CorrectionEffect.CORRECTS_DETAIL }.forEach {
            markClaimAndDependents(
                ClaimReference.Assertion(it.correctedAssertionId),
                LongitudinalLifecycleStatus.CONTESTED,
                "USER_CORRECTION",
                snapshot,
                lifecycle,
                revision,
            )
        }
        val affected = objectIds(bundle) + hypotheses.map { it.id.value }
        val identity = current.currentIdentityDecisionByPair.toMutableMap()
        bundle.identityLinks.forEach { link ->
            val pair = identityPair(link)
            if (pair in identity) fail(AdmissionDisposition.REJECTED_VALIDATION, "IDENTITY_DECISION_REQUIRES_REVISION_OPERATION")
            identity[pair] = link.id
        }
        return mutation(current, revision, snapshot, lifecycle = lifecycle, identity = identity, affected = affected, trace = listOf("CT07-BUNDLE-ATOMIC", "CT07-REFERENTIAL-INTEGRITY", "CT07-DERIVATION-SUPPORT"))
    }

    private fun recordCorrection(
        request: LongitudinalAdmissionRequest,
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.RecordUserCorrection,
        recordTime: RecordTime,
        revision: Long,
    ): PlannedLongitudinalMutation {
        if (request.actor != AdmissionActor.USER || request.origin != AdmissionOrigin.USER_CORRECTION) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "USER_CORRECTION_REQUIRES_USER_ORIGIN")
        }
        if (operation.correctionSource.acquisitionMode != AcquisitionMode.USER_CORRECTION || operation.correctionSource.authorRole != SourceAuthorRole.USER) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "CORRECTION_SOURCE_AUTHORITY_INVALID")
        }
        if (operation.correctingAssertion.sourceRecordId != operation.correctionSource.revisionId ||
            operation.correction.correctingAssertionId != operation.correctingAssertion.id) {
            fail(AdmissionDisposition.REJECTED_VALIDATION, "CORRECTION_LINK_MISMATCH")
        }
        if (operation.correction.correctedAssertionId !in current.snapshot.assertions.map { it.id }.toSet()) {
            fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "CORRECTED_ASSERTION_NOT_FOUND")
        }
        if (operation.correction.correctedAssertionId == operation.correctingAssertion.id) {
            fail(AdmissionDisposition.REJECTED_VALIDATION, "SELF_CORRECTION")
        }
        val sourceMutation = admitSource(
            request.copy(operation = LongitudinalWriteOperation.AdmitSource(operation.correctionSource)),
            current,
            LongitudinalWriteOperation.AdmitSource(operation.correctionSource),
            recordTime,
            revision,
        )
        validateSourceGrounding(
            operation.correctingAssertion,
            sourceMutation.state.snapshot.copy(assertions = sourceMutation.state.snapshot.assertions + operation.correctingAssertion),
        )
        val snapshot = sourceMutation.state.snapshot.copy(
            assertions = sourceMutation.state.snapshot.assertions + operation.correctingAssertion,
            corrections = sourceMutation.state.snapshot.corrections + operation.correction,
            supersessions = sourceMutation.state.snapshot.supersessions + listOfNotNull(operation.supersession),
        )
        if (operation.correction.effect == CorrectionEffect.CORRECTS_AND_SUPERSEDES) {
            val relation = operation.supersession ?: fail(AdmissionDisposition.REJECTED_VALIDATION, "CORRECTION_SUPERSESSION_REQUIRED")
            if (relation.successor != ClaimReference.Assertion(operation.correctingAssertion.id) ||
                relation.predecessor != ClaimReference.Assertion(operation.correction.correctedAssertionId) ||
                relation.kind != SupersessionKind.CORRECTS) {
                fail(AdmissionDisposition.REJECTED_VALIDATION, "CORRECTION_SUPERSESSION_MISMATCH")
            }
        }
        val lifecycle = sourceMutation.state.lifecycle.toMutableMap()
        lifecycle[assertionRef(operation.correctingAssertion.id)] = active(
            revision,
            operation.correctionSource.privacy != SourcePrivacy.PRIVATE,
            "USER_CORRECTION_ADMITTED",
        )
        lifecycle[ref(StoredObjectType.CORRECTION, operation.correction.id.value)] = active(revision, true, "USER_CORRECTION_RECORDED")
        operation.supersession?.let { lifecycle[ref(StoredObjectType.SUPERSESSION, it.id.value)] = active(revision, true, "CORRECTION_SUPERSESSION_RECORDED") }
        val correctedStatus = if (operation.correction.effect == CorrectionEffect.CORRECTS_AND_SUPERSEDES) {
            LongitudinalLifecycleStatus.SUPERSEDED
        } else LongitudinalLifecycleStatus.CONTESTED
        markClaimAndDependents(ClaimReference.Assertion(operation.correction.correctedAssertionId), correctedStatus, "USER_CORRECTION", snapshot, lifecycle, revision)
        return mutation(
            current,
            revision,
            snapshot,
            sourcePrivacy = sourceMutation.state.sourcePrivacy,
            lifecycle = lifecycle,
            affected = listOf(operation.correctionSource.revisionId.value, operation.correctingAssertion.id.value, operation.correction.correctedAssertionId.value, operation.correction.id.value) + listOfNotNull(operation.supersession?.id?.value),
            trace = listOf("CT07-USER-CORRECTION-AUTHORITY", "CT07-HISTORY-PRESERVED", "CT07-DEPENDENTS-REVIEW"),
        )
    }

    private fun recordSupersession(
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.RecordSupersession,
        revision: Long,
    ): PlannedLongitudinalMutation {
        val snapshot = current.snapshot.copy(supersessions = current.snapshot.supersessions + operation.relation)
        val lifecycle = current.lifecycle.toMutableMap()
        lifecycle[ref(StoredObjectType.SUPERSESSION, operation.relation.id.value)] = active(revision, true, "SUPERSESSION_RECORDED")
        markClaimAndDependents(operation.relation.predecessor, LongitudinalLifecycleStatus.SUPERSEDED, "EXPLICIT_SUPERSESSION", snapshot, lifecycle, revision)
        return mutation(current, revision, snapshot, lifecycle = lifecycle, affected = listOf(operation.relation.id.value, claimId(operation.relation.predecessor)), trace = listOf("CT07-SUPERSESSION-EXPLICIT", "CT07-SUPERSESSION-ACYCLIC"))
    }

    private fun recordContradiction(
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.RecordContradiction,
        revision: Long,
    ): PlannedLongitudinalMutation {
        val snapshot = current.snapshot.copy(contradictions = current.snapshot.contradictions + operation.relation)
        val lifecycle = current.lifecycle + (ref(StoredObjectType.CONTRADICTION, operation.relation.id.value) to active(revision, true, "CONTRADICTION_RECORDED"))
        return mutation(current, revision, snapshot, lifecycle = lifecycle, affected = listOf(operation.relation.id.value, operation.relation.leftAssertionId.value, operation.relation.rightAssertionId.value), trace = listOf("CT07-CONTRADICTION-COEXISTS", "CT07-NO-AUTOMATIC-WINNER"))
    }

    private fun reviseIdentity(
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.ReviseIdentityLink,
        revision: Long,
    ): PlannedLongitudinalMutation {
        val pair = identityPair(operation.decision)
        val currentId = current.currentIdentityDecisionByPair[pair]
        if (operation.priorDecisionId != currentId) {
            if (operation.priorDecisionId == null && currentId != null) fail(AdmissionDisposition.REJECTED_STALE_REVISION, "IDENTITY_DECISION_ALREADY_EXISTS")
            fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "IDENTITY_PRIOR_DECISION_NOT_CURRENT")
        }
        if (current.snapshot.identityLinks.any { it.id == operation.decision.id }) fail(AdmissionDisposition.REJECTED_VALIDATION, "DUPLICATE_IDENTITY_DECISION_ID")
        val prior = currentId?.let { id -> current.snapshot.identityLinks.firstOrNull { it.id == id } }
        val links = current.snapshot.identityLinks.filterNot { it.id == currentId } + operation.decision
        val snapshot = current.snapshot.copy(identityLinks = links)
        val lifecycle = current.lifecycle.toMutableMap()
        prior?.let { lifecycle[ref(StoredObjectType.IDENTITY_DECISION, it.id.value)] = inactive(LongitudinalLifecycleStatus.SUPERSEDED, revision, "IDENTITY_DECISION_REVISED") }
        lifecycle[ref(StoredObjectType.IDENTITY_DECISION, operation.decision.id.value)] = active(revision, true, "IDENTITY_DECISION_RECORDED")
        if (prior != null && prior.status != operation.decision.status) {
            val affectedAssertions = current.snapshot.assertions.filter { assertionReferencesEither(it.subject, it.value, prior.leftEntityId.value, prior.rightEntityId.value) }.map { ClaimReference.Assertion(it.id) }
            affectedAssertions.forEach { markDependentsOnly(it, "IDENTITY_DECISION_REVISED", snapshot, lifecycle, revision) }
        }
        return mutation(
            current,
            revision,
            snapshot,
            lifecycle = lifecycle,
            identity = current.currentIdentityDecisionByPair + (pair to operation.decision.id),
            affected = listOfNotNull(prior?.id?.value, operation.decision.id.value),
            trace = listOf("CT07-IDENTITY-EXPLICIT", "CT07-NO-ENTITY-MERGE", "CT07-IDENTITY-HISTORY"),
        )
    }

    private fun changeCoverage(
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.ChangeCoverage,
        revision: Long,
    ): PlannedLongitudinalMutation {
        if (operation.topic.status == InformationCoverageStatus.DECLINED && operation.topic.sourceRecordIds.isNotEmpty()) {
            fail(AdmissionDisposition.REJECTED_PRIVACY, "DECLINED_IS_NOT_EVIDENCE")
        }
        val snapshot = current.snapshot.copy(coverageTopics = current.snapshot.coverageTopics.filterNot { it.id == operation.topic.id } + operation.topic)
        val eligible = operation.topic.status !in setOf(InformationCoverageStatus.PRIVATE, InformationCoverageStatus.DECLINED)
        val lifecycle = current.lifecycle + (ref(StoredObjectType.COVERAGE, operation.topic.id.value) to active(revision, eligible, "COVERAGE_CHANGED"))
        return mutation(current, revision, snapshot, lifecycle = lifecycle, affected = listOf(operation.topic.id.value), trace = listOf("CT07-COVERAGE-APPEND", "CT07-DECLINED-NOT-ABSENCE"))
    }

    private fun changePrivacy(
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.ChangePrivacy,
        revision: Long,
    ): PlannedLongitudinalMutation {
        val sourceHistory = current.sourceHistory(operation.stableSourceId)
        if (sourceHistory.isEmpty()) fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "SOURCE_NOT_FOUND")
        val lifecycle = current.lifecycle.toMutableMap()
        if (operation.privacy == SourcePrivacy.PRIVATE) {
            sourceHistory.forEach { lifecycle[ref(StoredObjectType.SOURCE_REVISION, it.id.value)] = inactive(LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE, revision, "SOURCE_MADE_PRIVATE") }
            current.snapshot.assertions.filter { assertion -> sourceHistory.any { it.id == assertion.sourceRecordId } }.forEach { assertion ->
                lifecycle[assertionRef(assertion.id)] = inactive(LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE, revision, "SOURCE_MADE_PRIVATE")
                markDependentsOnly(ClaimReference.Assertion(assertion.id), "PRIVATE_DEPENDENCY", current.snapshot, lifecycle, revision, LongitudinalLifecycleStatus.DEPENDENCY_BLOCKED)
            }
        } else {
            sourceHistory.forEach {
                lifecycle[ref(StoredObjectType.SOURCE_REVISION, it.id.value)] = inactive(
                    LongitudinalLifecycleStatus.REVIEW_REQUIRED,
                    revision,
                    "SOURCE_PRIVACY_RESTORED_REVIEW_REQUIRED",
                )
            }
            current.snapshot.assertions.filter { assertion -> sourceHistory.any { it.id == assertion.sourceRecordId } }.forEach {
                lifecycle[assertionRef(it.id)] = inactive(
                    LongitudinalLifecycleStatus.REVIEW_REQUIRED,
                    revision,
                    "SOURCE_PRIVACY_RESTORED_REVIEW_REQUIRED",
                )
                markDependentsOnly(
                    ClaimReference.Assertion(it.id),
                    "SOURCE_PRIVACY_RESTORED_REVIEW_REQUIRED",
                    current.snapshot,
                    lifecycle,
                    revision,
                )
            }
        }
        return mutation(current, revision, current.snapshot, sourcePrivacy = current.sourcePrivacy + (operation.stableSourceId to operation.privacy), lifecycle = lifecycle, affected = listOf(operation.stableSourceId.value), trace = listOf("CT07-PRIVACY-IMMEDIATE", "CT07-DERIVED-ELIGIBILITY-RECOMPUTED"))
    }

    private fun retireClaim(
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.RetireClaim,
        revision: Long,
    ): PlannedLongitudinalMutation {
        if (!claimExists(operation.claim, current.snapshot)) fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "CLAIM_NOT_FOUND")
        val lifecycle = current.lifecycle.toMutableMap()
        markClaimAndDependents(operation.claim, LongitudinalLifecycleStatus.RETIRED, "CLAIM_RETIRED", current.snapshot, lifecycle, revision)
        return mutation(current, revision, current.snapshot, lifecycle = lifecycle, affected = listOf(claimId(operation.claim)), trace = listOf("CT07-RETIRE-APPEND", "CT07-HISTORY-PRESERVED"))
    }

    private fun deleteSource(
        request: LongitudinalAdmissionRequest,
        current: LongitudinalAggregateState,
        operation: LongitudinalWriteOperation.DeleteSource,
        revision: Long,
    ): PlannedLongitudinalMutation {
        if (request.actor != AdmissionActor.USER || request.origin != AdmissionOrigin.PERSONAL_DATA_LIFECYCLE) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "SOURCE_DELETION_REQUIRES_USER_LIFECYCLE_AUTHORITY")
        }
        if (operation.scope != SourceDeletionScope.ENTIRE_SOURCE_HISTORY) {
            fail(AdmissionDisposition.REJECTED_VALIDATION, "REVISION_ONLY_DELETION_WOULD_BREAK_LINEAGE")
        }
        val sourceHistory = current.sourceHistory(operation.stableSourceId)
        if (sourceHistory.isEmpty()) fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "SOURCE_NOT_FOUND")
        val removedSourceIds = sourceHistory.map { it.id }.toSet()
        val removedDirectAssertionIds = current.snapshot.assertions
            .filter { it.sourceRecordId in removedSourceIds }.map { it.id }.toSet()

        var retainedAssertions = current.snapshot.assertions.filterNot { it.id in removedDirectAssertionIds }
        var retainedEntities = current.snapshot.entities
        var changed: Boolean
        do {
            val assertionIds = retainedAssertions.map { it.id }.toSet()
            val entityIds = retainedEntities.map { it.id }.toSet()
            val nextEntities = retainedEntities.mapNotNull { entity ->
                val supports = entity.supportingAssertionIds.intersect(assertionIds)
                val referencesValid = entityReferences(entity).all { it in entityIds }
                if (supports.isEmpty() || !referencesValid) null else entityWithSupports(entity, supports)
            }
            val nextEntityIds = nextEntities.map { it.id }.toSet()
            val nextAssertions = retainedAssertions.filter { assertionReferences(it).all { id -> id in nextEntityIds } }
            changed = nextEntities.size != retainedEntities.size || nextAssertions.size != retainedAssertions.size
            retainedEntities = nextEntities
            retainedAssertions = nextAssertions
        } while (changed)

        val retainedAssertionIds = retainedAssertions.map { it.id }.toSet()
        val retainedEntityIds = retainedEntities.map { it.id }.toSet()
        var retainedHypotheses = current.snapshot.hypotheses.filter { hypothesis ->
            assertionReferences(hypothesis.subject, hypothesis.proposedValue, null).all { it in retainedEntityIds }
        }
        do {
            val hypothesisIds = retainedHypotheses.map { it.id }.toSet()
            val next = retainedHypotheses.filter { hypothesis ->
                current.snapshot.hypothesisDependencies.any { dependency ->
                    dependency.dependentHypothesisId == hypothesis.id && when (val prerequisite = dependency.prerequisite) {
                        is ClaimReference.Assertion -> prerequisite.assertionId in retainedAssertionIds
                        is ClaimReference.Hypothesis -> prerequisite.hypothesisId in hypothesisIds
                    }
                }
            }
            changed = next.size != retainedHypotheses.size
            retainedHypotheses = next
        } while (changed)
        val retainedHypothesisIds = retainedHypotheses.map { it.id }.toSet()
        fun claimRetained(claim: ClaimReference) = when (claim) {
            is ClaimReference.Assertion -> claim.assertionId in retainedAssertionIds
            is ClaimReference.Hypothesis -> claim.hypothesisId in retainedHypothesisIds
        }
        val retainedDependencies = current.snapshot.hypothesisDependencies.filter {
            it.dependentHypothesisId in retainedHypothesisIds && claimRetained(it.prerequisite)
        }
        val retainedIdentity = current.snapshot.identityLinks.filter {
            it.leftEntityId in retainedEntityIds && it.rightEntityId in retainedEntityIds &&
                it.supportingAssertionIds.all { id -> id in retainedAssertionIds }
        }
        val snapshot = current.snapshot.copy(
            sources = current.snapshot.sources.filterNot { it.id in removedSourceIds },
            assertions = retainedAssertions,
            entities = retainedEntities,
            hypotheses = retainedHypotheses,
            hypothesisDependencies = retainedDependencies,
            contradictions = current.snapshot.contradictions.filter {
                it.leftAssertionId in retainedAssertionIds && it.rightAssertionId in retainedAssertionIds
            },
            corrections = current.snapshot.corrections.filter {
                it.correctingAssertionId in retainedAssertionIds && it.correctedAssertionId in retainedAssertionIds
            },
            supersessions = current.snapshot.supersessions.filter { claimRetained(it.successor) && claimRetained(it.predecessor) },
            identityLinks = retainedIdentity,
            coverageTopics = current.snapshot.coverageTopics.mapNotNull { topic ->
                val sources = topic.sourceRecordIds - removedSourceIds
                if (topic.sourceRecordIds.isNotEmpty() && sources.isEmpty()) null else topic.copy(sourceRecordIds = sources)
            },
        )
        val retainedObjectIds = buildSet {
            addAll(retainedAssertionIds.map { it.value })
            addAll(retainedEntityIds.map { it.value })
            addAll(retainedHypothesisIds.map { it.value })
            addAll(snapshot.contradictions.map { it.id.value })
            addAll(snapshot.corrections.map { it.id.value })
            addAll(snapshot.supersessions.map { it.id.value })
            addAll(snapshot.hypothesisDependencies.map { it.id.value })
            addAll(snapshot.identityLinks.map { it.id.value })
            addAll(snapshot.coverageTopics.map { it.id.value })
        }
        val lifecycle = current.lifecycle.filterKeys { reference ->
            reference.type == StoredObjectType.SOURCE_ID || reference.type == StoredObjectType.SOURCE_REVISION ||
                reference.stableId in retainedObjectIds
        }.toMutableMap()
        sourceHistory.forEach {
            lifecycle[ref(StoredObjectType.SOURCE_REVISION, it.id.value)] =
                inactive(LongitudinalLifecycleStatus.DELETED, revision, "USER_SOURCE_DELETION")
        }
        lifecycle[ref(StoredObjectType.SOURCE_ID, operation.stableSourceId.value)] =
            inactive(LongitudinalLifecycleStatus.DELETED, revision, "USER_SOURCE_DELETION")
        removedDirectAssertionIds.forEach {
            lifecycle[assertionRef(it)] = inactive(LongitudinalLifecycleStatus.DELETED, revision, "USER_SOURCE_DELETION")
        }
        val indirectlyRemoved = current.snapshot.assertions.map { it.id }.toSet() - retainedAssertionIds - removedDirectAssertionIds
        indirectlyRemoved.forEach {
            lifecycle[assertionRef(it)] = inactive(LongitudinalLifecycleStatus.DEPENDENCY_BLOCKED, revision, "DELETED_SOURCE_DEPENDENCY")
        }
        val unavailable = inactive(LongitudinalLifecycleStatus.DEPENDENCY_BLOCKED, revision, "DELETED_SOURCE_DEPENDENCY")
        (current.snapshot.entities.map { it.id }.toSet() - retainedEntityIds).forEach {
            lifecycle[ref(StoredObjectType.ENTITY, it.value)] = unavailable
        }
        (current.snapshot.hypotheses.map { it.id }.toSet() - retainedHypothesisIds).forEach {
            lifecycle[hypothesisRef(it)] = unavailable
        }
        fun markRemoved(type: StoredObjectType, before: Set<String>, retained: Set<String>) {
            before.filterNot { it in retained }.forEach { lifecycle[ref(type, it)] = unavailable }
        }
        markRemoved(StoredObjectType.CONTRADICTION, current.snapshot.contradictions.map { it.id.value }.toSet(), snapshot.contradictions.map { it.id.value }.toSet())
        markRemoved(StoredObjectType.CORRECTION, current.snapshot.corrections.map { it.id.value }.toSet(), snapshot.corrections.map { it.id.value }.toSet())
        markRemoved(StoredObjectType.SUPERSESSION, current.snapshot.supersessions.map { it.id.value }.toSet(), snapshot.supersessions.map { it.id.value }.toSet())
        markRemoved(StoredObjectType.HYPOTHESIS_DEPENDENCY, current.snapshot.hypothesisDependencies.map { it.id.value }.toSet(), snapshot.hypothesisDependencies.map { it.id.value }.toSet())
        markRemoved(StoredObjectType.IDENTITY_DECISION, current.snapshot.identityLinks.map { it.id.value }.toSet(), snapshot.identityLinks.map { it.id.value }.toSet())
        markRemoved(StoredObjectType.COVERAGE, current.snapshot.coverageTopics.map { it.id.value }.toSet(), snapshot.coverageTopics.map { it.id.value }.toSet())
        val identityIds = retainedIdentity.map { it.id }.toSet()
        return mutation(
            current,
            revision,
            snapshot,
            sourcePrivacy = current.sourcePrivacy - operation.stableSourceId,
            lifecycle = lifecycle,
            identity = current.currentIdentityDecisionByPair.filterValues { it in identityIds },
            affected = listOf(operation.stableSourceId.value) + removedSourceIds.map { it.value } +
                (current.snapshot.assertions.map { it.id }.toSet() - retainedAssertionIds).map { it.value } +
                (current.snapshot.hypotheses.map { it.id }.toSet() - retainedHypothesisIds).map { it.value },
            trace = listOf("CT14-USER-DELETION", "CT14-SOURCE-CONTENT-PURGED", "CT14-DERIVED-STATE-INVALIDATED"),
        )
    }

    private fun requireSourceAuthority(request: LongitudinalAdmissionRequest, draft: SourceDraft) {
        if (draft.authorRole != SourceAuthorRole.USER || request.actor != AdmissionActor.USER) {
            fail(AdmissionDisposition.REJECTED_AUTHORITY, "SOURCE_REQUIRES_USER_AUTHORSHIP")
        }
        val expected = when (draft.acquisitionMode) {
            AcquisitionMode.JOURNAL -> AdmissionOrigin.JOURNAL
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE -> AdmissionOrigin.BIOGRAPHER_OPEN_NARRATIVE
            AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE -> AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE
            AcquisitionMode.THERAPIST_CONVERSATION -> AdmissionOrigin.THERAPIST_CONVERSATION
            AcquisitionMode.USER_CORRECTION -> AdmissionOrigin.USER_CORRECTION
        }
        if (request.origin != expected) fail(AdmissionDisposition.REJECTED_AUTHORITY, "SOURCE_ORIGIN_MISMATCH")
    }

    private fun mutation(
        current: LongitudinalAggregateState,
        revision: Long,
        snapshot: LongitudinalEvidenceSnapshot,
        sourcePrivacy: Map<com.conundrum.thomas.v2.longitudinal.SourceIdentityId, SourcePrivacy> = current.sourcePrivacy,
        lifecycle: Map<LongitudinalObjectRef, LifecycleState> = current.lifecycle,
        identity: Map<String, IdentityLinkId> = current.currentIdentityDecisionByPair,
        affected: List<String>,
        trace: List<String>,
    ) = PlannedLongitudinalMutation(
        LongitudinalAggregateState(
            revision,
            snapshot,
            sourcePrivacy.entries.sortedBy { it.key.value }.associateTo(linkedMapOf()) { it.key to it.value },
            lifecycle.entries.sortedWith(compareBy({ it.key.type.name }, { it.key.stableId })).associateTo(linkedMapOf()) { it.key to it.value },
            identity.entries.sortedBy { it.key }.associateTo(linkedMapOf()) { it.key to it.value },
        ),
        affected.distinct().sorted(),
        trace,
    )

    private fun rejectValidation(issues: List<LongitudinalValidationIssue>): AdmissionPlanResult.Rejected {
        val codes = issues.map { it.code }.distinct().sorted()
        val disposition = when {
            codes.any { it.contains("CYCLE") } -> AdmissionDisposition.REJECTED_RELATION_CYCLE
            codes.any { it.contains("MISSING") } -> AdmissionDisposition.REJECTED_MISSING_REFERENCE
            else -> AdmissionDisposition.REJECTED_VALIDATION
        }
        return AdmissionPlanResult.Rejected(disposition, codes)
    }

    private fun hasUltimateEligibleSource(
        claim: ClaimReference,
        snapshot: LongitudinalEvidenceSnapshot,
        privacy: Map<com.conundrum.thomas.v2.longitudinal.SourceIdentityId, SourcePrivacy>,
        lifecycle: Map<LongitudinalObjectRef, LifecycleState>,
        newAssertionIds: Set<AssertionId>,
        newHypothesisIds: Set<com.conundrum.thomas.v2.longitudinal.HypothesisId>,
        visiting: MutableSet<ClaimReference>,
    ): Boolean {
        if (!visiting.add(claim)) return false
        return when (claim) {
            is ClaimReference.Assertion -> snapshot.assertions.firstOrNull { it.id == claim.assertionId }?.let { assertion ->
                val claimIsEligible = assertion.id in newAssertionIds ||
                    lifecycle[assertionRef(assertion.id)]?.eligibleForOrdinaryUse == true
                claimIsEligible && sourceEligible(assertion.sourceRecordId, snapshot, privacy, lifecycle)
            } == true
            is ClaimReference.Hypothesis -> {
                val claimIsEligible = claim.hypothesisId in newHypothesisIds ||
                    lifecycle[hypothesisRef(claim.hypothesisId)]?.eligibleForOrdinaryUse == true
                claimIsEligible && snapshot.hypothesisDependencies
                    .filter { it.dependentHypothesisId == claim.hypothesisId }
                    .any {
                        hasUltimateEligibleSource(
                            it.prerequisite,
                            snapshot,
                            privacy,
                            lifecycle,
                            newAssertionIds,
                            newHypothesisIds,
                            visiting.toMutableSet(),
                        )
                    }
            }
        }
    }

    private fun sourceEligible(
        sourceRecordId: com.conundrum.thomas.v2.longitudinal.SourceRecordId,
        snapshot: LongitudinalEvidenceSnapshot,
        privacy: Map<com.conundrum.thomas.v2.longitudinal.SourceIdentityId, SourcePrivacy>,
        lifecycle: Map<LongitudinalObjectRef, LifecycleState>,
    ): Boolean = snapshot.sources.firstOrNull { it.id == sourceRecordId }?.let {
        privacy[it.stableSourceId] != SourcePrivacy.PRIVATE &&
            lifecycle[ref(StoredObjectType.SOURCE_REVISION, it.id.value)]?.eligibleForOrdinaryUse == true
    } == true

    private fun markClaimAndDependents(
        claim: ClaimReference,
        status: LongitudinalLifecycleStatus,
        cause: String,
        snapshot: LongitudinalEvidenceSnapshot,
        lifecycle: MutableMap<LongitudinalObjectRef, LifecycleState>,
        revision: Long,
    ) {
        lifecycle[claimRef(claim)] = inactive(status, revision, cause)
        markDependentsOnly(claim, cause, snapshot, lifecycle, revision)
    }

    private fun markDependentsOnly(
        claim: ClaimReference,
        cause: String,
        snapshot: LongitudinalEvidenceSnapshot,
        lifecycle: MutableMap<LongitudinalObjectRef, LifecycleState>,
        revision: Long,
        status: LongitudinalLifecycleStatus = LongitudinalLifecycleStatus.REVIEW_REQUIRED,
        visited: MutableSet<ClaimReference> = mutableSetOf(),
    ) {
        if (!visited.add(claim)) return
        snapshot.hypothesisDependencies.filter { it.prerequisite == claim }.forEach { dependency ->
            val dependent = ClaimReference.Hypothesis(dependency.dependentHypothesisId)
            lifecycle[hypothesisRef(dependency.dependentHypothesisId)] = inactive(status, revision, cause)
            markDependentsOnly(dependent, cause, snapshot, lifecycle, revision, status, visited)
        }
        if (claim is ClaimReference.Assertion) {
            snapshot.entities.filter { claim.assertionId in it.supportingAssertionIds }.forEach {
                lifecycle[ref(StoredObjectType.ENTITY, it.id.value)] = inactive(status, revision, cause)
            }
        }
    }

    private fun objectIds(bundle: EvidenceBundle): List<String> =
        bundle.assertions.map { it.id.value } + bundle.entities.map { it.id.value } +
            bundle.contradictions.map { it.id.value } + bundle.corrections.map { it.id.value } + bundle.supersessions.map { it.id.value } +
            bundle.hypothesisDependencies.map { it.id.value } + bundle.identityLinks.map { it.id.value } +
            bundle.coverageTopics.map { it.id.value }

    private fun validateSourceGrounding(
        assertion: com.conundrum.thomas.v2.longitudinal.EvidenceAssertion,
        snapshot: LongitudinalEvidenceSnapshot,
    ) {
        val grounding = assertion.sourceGrounding ?: return
        if (grounding.sourceRevisionId != assertion.sourceRecordId) {
            fail(AdmissionDisposition.REJECTED_VALIDATION, "SOURCE_SPAN_REVISION_MISMATCH")
        }
        val source = snapshot.sources.firstOrNull { it.id == assertion.sourceRecordId }
            ?: fail(AdmissionDisposition.REJECTED_MISSING_REFERENCE, "SOURCE_SPAN_SOURCE_NOT_FOUND")
        val text = (source.originalContent as? com.conundrum.thomas.v2.longitudinal.OriginalSourceContent.Inline)?.exactContent
            ?: fail(AdmissionDisposition.REJECTED_VALIDATION, "SOURCE_SPAN_CONTENT_UNAVAILABLE")
        if (sourceTextSha256(text) != grounding.sourceRevisionSha256) {
            fail(AdmissionDisposition.REJECTED_VALIDATION, "SOURCE_SPAN_FINGERPRINT_MISMATCH")
        }
        if (grounding.startOffsetInclusive >= text.length || grounding.endOffsetExclusive > text.length ||
            text.substring(grounding.startOffsetInclusive, grounding.endOffsetExclusive) != grounding.exactFragment
        ) {
            fail(AdmissionDisposition.REJECTED_VALIDATION, "SOURCE_SPAN_CONTENT_MISMATCH")
        }
    }

    private fun claimExists(claim: ClaimReference, snapshot: LongitudinalEvidenceSnapshot) = when (claim) {
        is ClaimReference.Assertion -> snapshot.assertions.any { it.id == claim.assertionId }
        is ClaimReference.Hypothesis -> snapshot.hypotheses.any { it.id == claim.hypothesisId }
    }

    private fun claimRef(claim: ClaimReference) = when (claim) {
        is ClaimReference.Assertion -> assertionRef(claim.assertionId)
        is ClaimReference.Hypothesis -> hypothesisRef(claim.hypothesisId)
    }

    private fun claimId(claim: ClaimReference) = when (claim) {
        is ClaimReference.Assertion -> claim.assertionId.value
        is ClaimReference.Hypothesis -> claim.hypothesisId.value
    }

    private fun ref(type: StoredObjectType, id: String) = LongitudinalObjectRef(type, id)
    private fun active(revision: Long, eligible: Boolean, cause: String) = LifecycleState(
        if (eligible) LongitudinalLifecycleStatus.ACTIVE else LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE,
        eligible,
        cause,
        revision,
    )
    private fun inactive(status: LongitudinalLifecycleStatus, revision: Long, cause: String) = LifecycleState(status, false, cause, revision)
    private fun rejected(disposition: AdmissionDisposition, vararg reasons: String) = AdmissionPlanResult.Rejected(disposition, reasons.toList().sorted())
    private fun fail(disposition: AdmissionDisposition, code: String): Nothing = throw AdmissionPolicyFailure(disposition, code)

    private fun identityPair(link: EntityIdentityLink): String {
        val pair = link.canonicalPair()
        return "${pair.first.value}|${pair.second.value}"
    }

    private fun assertionReferencesEither(subject: AssertionSubject, value: AssertionValue, left: String, right: String): Boolean {
        val ids = buildSet {
            if (subject is AssertionSubject.Entity) add(subject.entityId.value)
            if (value is AssertionValue.EntityReference) add(value.entityId.value)
            if (value is AssertionValue.EntityReferences) addAll(value.entityIds.map { it.value })
        }
        return left in ids || right in ids
    }

    private fun assertionReferences(assertion: EvidenceAssertion) =
        assertionReferences(assertion.subject, assertion.value, assertion.eventTime)

    private fun assertionReferences(subject: AssertionSubject, value: AssertionValue, time: EventTime?): Set<com.conundrum.thomas.v2.longitudinal.LifeEntityId> = buildSet {
        if (subject is AssertionSubject.Entity) add(subject.entityId)
        when (value) {
            is AssertionValue.EntityReference -> add(value.entityId)
            is AssertionValue.EntityReferences -> addAll(value.entityIds)
            else -> Unit
        }
        when (time) {
            is EventTime.RelativePeriod -> time.anchorEntityId?.let(::add)
            is EventTime.BeforeOrAfter -> add(time.referenceEntityId)
            else -> Unit
        }
    }

    private fun entityReferences(entity: LifeEntity): Set<com.conundrum.thomas.v2.longitudinal.LifeEntityId> = when (entity) {
        is Person, is Place, is LifePeriod -> emptySet()
        is LifeEvent -> entity.participantIds + entity.placeIds
        is Relationship -> entity.participantIds
        is Role -> setOfNotNull(entity.holderId, entity.contextId)
        is Decision -> entity.decisionMakerIds
        is Behavior -> entity.actorIds
        is CopingResponse -> setOfNotNull(entity.actorId, entity.relatedEventId)
        is Outcome -> setOf(entity.outcomeOfId)
    }

    private fun entityWithSupports(entity: LifeEntity, supports: Set<AssertionId>): LifeEntity = when (entity) {
        is Person -> entity.copy(supportingAssertionIds = supports)
        is Place -> entity.copy(supportingAssertionIds = supports)
        is LifeEvent -> entity.copy(supportingAssertionIds = supports)
        is LifePeriod -> entity.copy(supportingAssertionIds = supports)
        is Relationship -> entity.copy(supportingAssertionIds = supports)
        is Role -> entity.copy(supportingAssertionIds = supports)
        is Decision -> entity.copy(supportingAssertionIds = supports)
        is Behavior -> entity.copy(supportingAssertionIds = supports)
        is CopingResponse -> entity.copy(supportingAssertionIds = supports)
        is Outcome -> entity.copy(supportingAssertionIds = supports)
    }
}

private class AdmissionPolicyFailure(val disposition: AdmissionDisposition, val code: String) : RuntimeException()
