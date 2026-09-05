package com.conundrum.thomas.v2.longitudinal.store

import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.CoverageTopic
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOperationType
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LifecycleState
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import java.time.Instant

fun interface StoreClock { fun instant(): Instant }

interface AcceptedAdmissionReceipt {
    val requestId: AdmissionRequestId
    val idempotencyKey: IdempotencyKey
    val admissionPolicyVersion: AdmissionPolicyVersion
    val operationType: AdmissionOperationType
    val priorStoreRevision: Long
    val resultingStoreRevision: Long
    val affectedStableIds: List<String>
    val ledgerEventIds: List<String>
    val recordTime: Instant
    val decisionTrace: List<String>
    val payloadFingerprint: String
}

interface AdmissionResult {
    val disposition: AdmissionDisposition
    val receipt: AcceptedAdmissionReceipt?
    val reasonCodes: List<String>
}

/** The only ordinary mutation entry point for qualification and protected stores. */
interface LongitudinalAdmissionController {
    fun submit(request: LongitudinalAdmissionRequest): AdmissionResult
}

data class RedactedAdmissionHistoryEntry(
    val sequence: Long,
    val storeRevision: Long?,
    val requestId: String,
    val idempotencyKey: String,
    val operationType: AdmissionOperationType,
    val disposition: AdmissionDisposition,
    val policyVersion: String,
    val reasonCodes: List<String>,
    val payloadFingerprint: String,
    val recordTime: Instant,
)

/** Governed read-only projection; implementation details never escape this port. */
interface LongitudinalReader {
    val classification: StoreDataClassification
    fun currentStoreRevision(): Long
    fun snapshot(asOfRevision: Long = currentStoreRevision()): LongitudinalEvidenceSnapshot
    fun source(stableId: SourceIdentityId, asOfRevision: Long = currentStoreRevision()): SourceRecord?
    fun sourceRevisionHistory(stableId: SourceIdentityId, asOfRevision: Long = currentStoreRevision()): List<SourceRecord>
    fun assertion(id: AssertionId, asOfRevision: Long = currentStoreRevision()): EvidenceAssertion?
    fun entity(id: LifeEntityId, asOfRevision: Long = currentStoreRevision()): LifeEntity?
    fun lifecycle(reference: LongitudinalObjectRef, asOfRevision: Long = currentStoreRevision()): LifecycleState?
    fun lifecycleHistory(reference: LongitudinalObjectRef): List<LifecycleState>
    fun directDependencies(claim: ClaimReference, asOfRevision: Long = currentStoreRevision()): List<HypothesisDependency>
    fun directDependents(claim: ClaimReference, asOfRevision: Long = currentStoreRevision()): List<HypothesisDependency>
    fun corrections(asOfRevision: Long = currentStoreRevision()): List<CorrectionRelation>
    fun contradictions(asOfRevision: Long = currentStoreRevision()): List<ContradictionRelation>
    fun supersessions(asOfRevision: Long = currentStoreRevision()): List<SupersessionRelation>
    fun identityLinks(asOfRevision: Long = currentStoreRevision()): List<EntityIdentityLink>
    fun identityDecisionHistory(left: LifeEntityId, right: LifeEntityId): List<EntityIdentityLink>
    fun coverage(asOfRevision: Long = currentStoreRevision()): List<CoverageTopic>
    fun isEligible(reference: LongitudinalObjectRef, asOfRevision: Long = currentStoreRevision()): Boolean
    fun redactedAdmissionHistory(): List<RedactedAdmissionHistoryEntry>
    fun canonicalLogicalStateDigest(asOfRevision: Long = currentStoreRevision()): String
}

open class UnsupportedStoreSchemaException(message: String) : IllegalStateException(message)
open class InconsistentLongitudinalStoreException(message: String) : IllegalStateException(message)
class HistoricalStateErasedException(message: String) : IllegalStateException(message)
