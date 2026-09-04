package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionPolicyVersion
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionRequestId
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.IdempotencyKey
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalAdmissionRequest
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDraft
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal class MemoryKeyProvider(bytes: ByteArray = ByteArray(32) { (it + 7).toByte() }) : PersonalDataKeyProvider {
    private var key: SecretKey? = SecretKeySpec(bytes.copyOf(), "AES")
    override val descriptor = PersonalDataKeyDescriptor("qualification.primary", "in-memory-qualification", false, false)
    override fun getOrCreate(): SecretKey = key ?: SecretKeySpec(ByteArray(32) { (it + 11).toByte() }, "AES").also { key = it }
    override fun existing(): SecretKey = key ?: throw KeyMaterialUnavailableException("Synthetic key unavailable")
    override fun destroy() { key = null }
}

internal class PersonalDataHarness(
    val directory: Path = Files.createTempDirectory("ct-v2-14-"),
    val keyProvider: MemoryKeyProvider = MemoryKeyProvider(),
    val storage: NioAtomicProtectedArtifactStorage = NioAtomicProtectedArtifactStorage(directory.resolve("store.ctpd")),
    val clock: IncrementingClock = IncrementingClock(),
    faultInjector: PersistenceFaultInjector = PersistenceFaultInjector { },
) : AutoCloseable {
    var store = (ProtectedPersonalDataStoreFactory.open(storage, keyProvider, clock, faultInjector = faultInjector)
        as PersonalDataOpenResult.Opened).store
    private var sequence = 0

    fun request(
        operation: LongitudinalWriteOperation,
        actor: AdmissionActor,
        origin: AdmissionOrigin,
        key: String? = null,
        expectedRevision: Long = store.reader.currentStoreRevision(),
    ): LongitudinalAdmissionRequest {
        sequence += 1
        val suffix = sequence.toString().padStart(4, '0')
        return LongitudinalAdmissionRequest(
            AdmissionRequestId.parse("personal-request-$suffix"),
            IdempotencyKey.parse(key ?: "personal-key-$suffix"),
            expectedRevision,
            actor,
            origin,
            AdmissionPolicyVersion.CT_V2_07_V1,
            StoreDataClassification.PROTECTED_PERSONAL_DATA,
            operation,
        )
    }

    fun source(
        suffix: String,
        text: String = "Synthetic longitudinal statement $suffix.",
        mode: AcquisitionMode = AcquisitionMode.JOURNAL,
        privacy: SourcePrivacy = SourcePrivacy.ELIGIBLE,
        eventTime: EventTime = EventTime.Unknown("Synthetic time unknown"),
    ) = SourceDraft(
        SourceIdentityId.parse("personal-source-$suffix"),
        SourceRecordId.parse("personal-source-$suffix-rev-1"),
        mode,
        SourceAuthorRole.USER,
        InteractionId.parse("personal-interaction-$suffix"),
        OriginalSourceContent.Inline(text),
        eventTime,
        ReportTime(Instant.parse("2039-01-01T00:00:00Z")),
        privacy,
    )

    fun assertion(suffix: String, sourceId: SourceRecordId) = EvidenceAssertion(
        AssertionId.parse("personal-assertion-$suffix"),
        sourceId,
        AssertionSubject.User,
        AssertionPredicate(PersonalConceptId.parse("personal.$suffix"), PredicateSemantics.OTHER),
        AssertionValue.Text("Synthetic structured value $suffix"),
        UserEvidenceKind.EXPLICIT_USER_ASSERTION,
        AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
    )

    fun admit(source: SourceDraft, key: String? = null) = store.admission.submit(
        request(LongitudinalWriteOperation.AdmitSource(source), AdmissionActor.USER, origin(source.acquisitionMode), key),
    )

    fun admit(assertion: EvidenceAssertion) = store.admission.submit(
        request(
            LongitudinalWriteOperation.AdmitEvidenceBundle(EvidenceBundle(assertions = listOf(assertion))),
            AdmissionActor.USER,
            AdmissionOrigin.QUALIFICATION_HARNESS,
        ),
    )

    fun reopen(): PersonalDataOpenResult {
        store.close()
        val result = ProtectedPersonalDataStoreFactory.open(storage, keyProvider, clock)
        if (result is PersonalDataOpenResult.Opened) store = result.store
        return result
    }

    override fun close() {
        runCatching { store.close() }
        directory.toFile().walkBottomUp().forEach { runCatching { it.delete() } }
    }

    companion object {
        fun origin(mode: AcquisitionMode) = when (mode) {
            AcquisitionMode.JOURNAL -> AdmissionOrigin.JOURNAL
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE -> AdmissionOrigin.BIOGRAPHER_OPEN_NARRATIVE
            AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE -> AdmissionOrigin.BIOGRAPHER_GUIDED_TIMELINE
            AcquisitionMode.THERAPIST_CONVERSATION -> AdmissionOrigin.THERAPIST_CONVERSATION
            AcquisitionMode.USER_CORRECTION -> AdmissionOrigin.USER_CORRECTION
        }
    }
}

internal class IncrementingClock : StoreClock {
    private var next = Instant.parse("2040-01-01T00:00:00Z")
    override fun instant(): Instant = next.also { next = next.plusSeconds(1) }
}
