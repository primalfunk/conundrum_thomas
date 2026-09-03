package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.InteractionId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourceDraft
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.nio.file.Path
import java.time.Instant
import org.junit.Assert.assertEquals

internal object CTV208TestSupport {
    fun path(name: String): Path = Path.of(System.getProperty("thomas.repositoryRoot"), "qualification", "build", "ct-v2-08", "$name.sqlite")

    fun admitText(
        harness: SyntheticLongitudinalStoreHarness,
        suffix: String,
        text: String,
        mode: AcquisitionMode = AcquisitionMode.JOURNAL,
    ): SourceRecordId {
        val stableId = SourceIdentityId.parse("language-$suffix")
        val revisionId = SourceRecordId.parse("language-$suffix-rev-1")
        val draft = SourceDraft(
            stableId, revisionId, mode, SourceAuthorRole.USER, InteractionId.parse("interaction-$suffix"),
            OriginalSourceContent.Inline(text), EventTime.Unknown("No source-level event time"),
            ReportTime(Instant.parse("2039-01-01T00:00:00Z")),
        )
        val result = harness.store.admission.submit(harness.request(LongitudinalWriteOperation.AdmitSource(draft),
            AdmissionActor.USER, SyntheticLongitudinalStoreHarness.originFor(mode)))
        assertEquals(result.reasonCodes.toString(), AdmissionDisposition.ACCEPTED, result.disposition)
        return revisionId
    }
}
