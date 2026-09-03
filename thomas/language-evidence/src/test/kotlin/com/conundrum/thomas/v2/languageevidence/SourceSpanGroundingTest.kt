package com.conundrum.thomas.v2.languageevidence

import com.conundrum.thomas.v2.languageevidence.perception.*
import com.conundrum.thomas.v2.longitudinal.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class SourceSpanGroundingTest {
    private val source = SourceRecord(
        SourceRecordId.parse("source.grounding.rev-1"), PersonalEvidenceProvenance(AcquisitionMode.JOURNAL),
        ReportTime(Instant.parse("2050-01-01T00:00:00Z")), RecordTime(Instant.parse("2050-01-01T00:00:01Z")),
        OriginalSourceContent.Inline("I moved to Denver in 2018."), SourceIdentityId.parse("source.grounding"),
    )
    private val committed = CommittedSourceText.from(source)
    private val valid = ConservativeLanguagePerception().perceive(committed)
    private val validator = LanguageEvidenceProposalValidator()

    @Test fun exactGroundingValidates() {
        assertTrue(validator.validate(committed, valid).accepted)
    }

    @Test fun mismatchedSpanTextIsRejected() {
        val claim = valid.proposals.single().assertion
        val changed = claim.copy(sourceGrounding = claim.sourceGrounding!!.copy(exactFragment = "not the original source fragment"))
        val result = valid.copy(proposals = listOf(EvidenceProposal(changed, valid.proposals.single().entities)))
        assertTrue(validator.validate(committed, result).issues.any { it.code == "SPAN_TEXT_MISMATCH" })
    }

    @Test fun wrongSourceRevisionIsRejected() {
        val claim = valid.proposals.single().assertion
        val changed = claim.copy(sourceGrounding = claim.sourceGrounding!!.copy(sourceRevisionId = SourceRecordId.parse("source.other.rev-1")))
        val result = valid.copy(proposals = listOf(EvidenceProposal(changed, valid.proposals.single().entities)))
        assertTrue(validator.validate(committed, result).issues.any { it.code == "SPAN_SOURCE_MISMATCH" })
    }

    @Test fun wrongFingerprintIsRejected() {
        val claim = valid.proposals.single().assertion
        val changed = claim.copy(sourceGrounding = claim.sourceGrounding!!.copy(sourceRevisionSha256 = "0".repeat(64)))
        val result = valid.copy(proposals = listOf(EvidenceProposal(changed, valid.proposals.single().entities)))
        assertTrue(validator.validate(committed, result).issues.any { it.code == "SPAN_FINGERPRINT_MISMATCH" })
    }

    @Test fun speculativeLanguageCannotBeRelabeledAsDirectFact() {
        val maybeSource = source.copy(
            id = SourceRecordId.parse("source.maybe.rev-1"), stableSourceId = SourceIdentityId.parse("source.maybe"),
            originalContent = OriginalSourceContent.Inline("Maybe it was 2012."),
        )
        val committedMaybe = CommittedSourceText.from(maybeSource)
        val result = ConservativeLanguagePerception().perceive(committedMaybe)
        val original = result.proposals.single().assertion
        val forged = original.copy(kind = UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            epistemicClass = EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION)
        val forgedResult = result.copy(proposals = listOf(EvidenceProposal(forged)))
        assertTrue(validator.validate(committedMaybe, forgedResult).issues.any {
            it.code == "SPECULATIVE_LANGUAGE_CANNOT_BE_DIRECT_FACT"
        })
    }

    @Test fun reportedSpeechCannotBeRelabeledAsOrdinaryFact() {
        val reportSource = source.copy(
            id = SourceRecordId.parse("source.report.rev-1"), stableSourceId = SourceIdentityId.parse("source.report"),
            originalContent = OriginalSourceContent.Inline("Sam told me he was furious."),
        )
        val committedReport = CommittedSourceText.from(reportSource)
        val result = ConservativeLanguagePerception().perceive(committedReport)
        val original = result.proposals.single().assertion
        val forged = original.copy(epistemicClass = EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION)
        assertTrue(validator.validate(committedReport, result.copy(proposals = listOf(EvidenceProposal(forged)))).issues.any {
            it.code == "REPORTED_SPEECH_CLASS_REQUIRED"
        })
    }
}
