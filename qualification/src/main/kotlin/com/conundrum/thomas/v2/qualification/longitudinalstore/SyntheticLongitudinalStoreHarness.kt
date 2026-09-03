package com.conundrum.thomas.v2.qualification.longitudinalstore

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.LongitudinalEvidenceSnapshot
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDraft
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.store.AdmissionResult
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStoreFactory
import com.conundrum.thomas.v2.longitudinal.store.QualificationStoreLocation
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

/** Synthetic-only helper. It never accepts natural language as evidence formation input. */
class SyntheticLongitudinalStoreHarness(
    val databasePath: Path,
    startTime: Instant = Instant.parse("2040-01-01T00:00:00Z"),
) : AutoCloseable {
    val clock = IncrementingFixtureClock(startTime)
    var store: QualificationLongitudinalStore
        private set
    private var requestSequence = 0L

    init {
        Files.createDirectories(databasePath.parent)
        Files.deleteIfExists(databasePath)
        store = QualificationLongitudinalStoreFactory.open(QualificationStoreLocation.file(databasePath), clock)
    }

    fun request(
        operation: LongitudinalWriteOperation,
        actor: AdmissionActor,
        origin: AdmissionOrigin,
        expectedRevision: Long = store.reader.currentStoreRevision(),
        idempotency: String? = null,
        policyVersion: AdmissionPolicyVersion = AdmissionPolicyVersion.CT_V2_07_V1,
    ): LongitudinalAdmissionRequest {
        requestSequence += 1
        val suffix = requestSequence.toString().padStart(4, '0')
        return LongitudinalAdmissionRequest(
            AdmissionRequestId.parse("request-$suffix"),
            IdempotencyKey.parse(idempotency ?: "idempotency-$suffix"),
            expectedRevision,
            actor,
            origin,
            policyVersion,
            StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
            operation,
        )
    }

    fun submitSource(source: SourceRecord): AdmissionResult = store.admission.submit(
        request(
            LongitudinalWriteOperation.AdmitSource(source.toDraft()),
            AdmissionActor.USER,
            originFor(source.provenance.acquisitionMode),
        ),
    )

    fun submitBundle(bundle: EvidenceBundle, actor: AdmissionActor = AdmissionActor.USER): AdmissionResult = store.admission.submit(
        request(
            LongitudinalWriteOperation.AdmitEvidenceBundle(bundle),
            actor,
            if (actor == AdmissionActor.THOMAS) AdmissionOrigin.THOMAS_DERIVATION else AdmissionOrigin.QUALIFICATION_HARNESS,
        ),
    )

    fun admitBasicSnapshot(snapshot: LongitudinalEvidenceSnapshot) {
        snapshot.sources.sortedBy { it.id }.forEach { requireAccepted(submitSource(it)) }
        if (snapshot.assertions.isNotEmpty() || snapshot.entities.isNotEmpty() || snapshot.contradictions.isNotEmpty() ||
            snapshot.corrections.isNotEmpty() ||
            snapshot.supersessions.isNotEmpty() || snapshot.identityLinks.isNotEmpty() || snapshot.coverageTopics.isNotEmpty()
        ) {
            requireAccepted(
                submitBundle(
                    EvidenceBundle(
                        assertions = snapshot.assertions,
                        entities = snapshot.entities,
                        contradictions = snapshot.contradictions,
                        corrections = snapshot.corrections,
                        supersessions = snapshot.supersessions,
                        identityLinks = snapshot.identityLinks,
                        coverageTopics = snapshot.coverageTopics,
                    ),
                ),
            )
        }
    }

    fun reopen() {
        store.close()
        store = QualificationLongitudinalStoreFactory.open(QualificationStoreLocation.file(databasePath), clock)
    }

    fun closePreservingFile() {
        store.close()
    }

    override fun close() {
        store.close()
        Files.deleteIfExists(databasePath)
    }

    companion object {
        fun originFor(mode: AcquisitionMode) = when (mode) {
            AcquisitionMode.JOURNAL -> AdmissionOrigin.JOURNAL
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE -> AdmissionOrigin.BIOGRAPHER_OPEN_NARRATIVE
            AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE -> AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE
            AcquisitionMode.THERAPIST_CONVERSATION -> AdmissionOrigin.THERAPIST_CONVERSATION
            AcquisitionMode.USER_CORRECTION -> AdmissionOrigin.USER_CORRECTION
        }

        fun requireAccepted(result: AdmissionResult) {
            check(result.disposition == com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition.ACCEPTED) {
                "Synthetic admission failed with ${result.disposition}:${result.reasonCodes}"
            }
        }
    }
}

fun SourceRecord.toDraft() = SourceDraft(
    stableSourceId = stableSourceId,
    revisionId = id,
    acquisitionMode = provenance.acquisitionMode,
    authorRole = SourceAuthorRole.USER,
    interactionId = provenance.interactionId,
    originalContent = originalContent,
    eventTime = eventTime,
    reportTime = reportTime,
    metadata = provenance.metadata,
)

class IncrementingFixtureClock(start: Instant) : StoreClock {
    private var next = start
    override fun instant(): Instant = next.also { next = next.plusSeconds(1) }
}
