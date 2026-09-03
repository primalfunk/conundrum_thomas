package com.conundrum.thomas.v2.qualification

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
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDraft
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.nio.file.Path
import java.time.Instant

internal object CTV207TestSupport {
    fun path(name: String): Path = Path.of(System.getProperty("thomas.repositoryRoot"), "qualification", "build", "ct-v2-07", "$name.sqlite")

    fun sourceDraft(
        suffix: String,
        content: String = "Synthetic qualification source $suffix.",
        eventTime: EventTime = EventTime.Unknown("Synthetic time unknown"),
        mode: AcquisitionMode = AcquisitionMode.JOURNAL,
    ) = SourceDraft(
        SourceIdentityId.parse("source-$suffix"), SourceRecordId.parse("source-$suffix-rev-1"), mode,
        SourceAuthorRole.USER, InteractionId.parse("interaction-$suffix"), OriginalSourceContent.Inline(content),
        eventTime, ReportTime(Instant.parse("2039-01-01T00:00:00Z")),
    )

    fun assertion(suffix: String, source: SourceRecordId, kind: UserEvidenceKind = UserEvidenceKind.EXPLICIT_USER_ASSERTION) = EvidenceAssertion(
        AssertionId.parse("assertion-$suffix"), source, AssertionSubject.User,
        AssertionPredicate(PersonalConceptId.parse("synthetic.$suffix"), PredicateSemantics.OTHER),
        AssertionValue.Text("Synthetic structured value $suffix"), kind,
        AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
    )

    fun admitSource(harness: SyntheticLongitudinalStoreHarness, draft: SourceDraft): com.conundrum.thomas.v2.longitudinal.store.AdmissionResult {
        val origin = SyntheticLongitudinalStoreHarness.originFor(draft.acquisitionMode)
        return harness.store.admission.submit(
            harness.request(LongitudinalWriteOperation.AdmitSource(draft), AdmissionActor.USER, origin),
        )
    }

    fun admitAssertion(harness: SyntheticLongitudinalStoreHarness, assertion: EvidenceAssertion) = harness.store.admission.submit(
        harness.request(
            LongitudinalWriteOperation.AdmitEvidenceBundle(EvidenceBundle(assertions = listOf(assertion))),
            AdmissionActor.USER,
            AdmissionOrigin.QUALIFICATION_HARNESS,
        ),
    )

    fun assertAccepted(result: com.conundrum.thomas.v2.longitudinal.store.AdmissionResult) {
        org.junit.Assert.assertEquals(result.reasonCodes.toString(), AdmissionDisposition.ACCEPTED, result.disposition)
    }
}
