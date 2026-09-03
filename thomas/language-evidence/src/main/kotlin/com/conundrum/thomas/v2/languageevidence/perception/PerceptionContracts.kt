package com.conundrum.thomas.v2.languageevidence.perception

import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.LifeEntity
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.SourceRecord
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.sourceTextSha256

const val CT_V2_08_PERCEPTION_VERSION = "ct-v2-08.perception.v1"

enum class PerceptionDisposition {
    UNDERSTOOD,
    PARTIALLY_UNDERSTOOD,
    AMBIGUOUS,
    UNSUPPORTED,
    NO_EVIDENCE_PROPOSAL,
    INVALID_INPUT,
}

enum class NonassertiveKind {
    QUESTION,
    HYPOTHETICAL,
    COUNTERFACTUAL,
    QUOTATION,
    RHETORICAL_QUESTION,
    AMBIGUOUS_HUMOR,
}

enum class UnresolvedPerceptionKind {
    AMBIGUOUS_REFERENT,
    AMBIGUOUS_CORRECTION_TARGET,
    UNKNOWN_EVENT_TIME,
    UNSUPPORTED_LANGUAGE,
    NONASSERTIVE_LANGUAGE,
    MULTIPLE_UNSUPPORTED_PROPOSITIONS,
}

/** Exact, already-admitted source text. Perception cannot create this authority. */
class CommittedSourceText private constructor(
    val source: SourceRecord,
    val exactText: String,
    val revisionSha256: String,
) {
    companion object {
        fun from(source: SourceRecord): CommittedSourceText {
            val text = (source.originalContent as? OriginalSourceContent.Inline)?.exactContent
                ?: error("CT-V2-08 only perceives exact inline committed source text")
            return CommittedSourceText(source, text, sourceTextSha256(text))
        }
    }
}

data class PerceptionContext(
    val correctionTarget: ClaimReference? = null,
)

data class EvidenceProposal(
    val assertion: EvidenceAssertion,
    val entities: List<LifeEntity> = emptyList(),
)

data class CorrectionCandidate(
    val correctingAssertion: EvidenceAssertion,
    val target: ClaimReference?,
    val correction: CorrectionRelation? = null,
    val supersession: SupersessionRelation? = null,
)

data class UnresolvedPerception(
    val kind: UnresolvedPerceptionKind,
    val reasonCode: String,
    val sourceStartOffset: Int? = null,
    val sourceEndOffset: Int? = null,
    val nonassertiveKind: NonassertiveKind? = null,
)

data class LanguagePerceptionResult(
    val sourceRevisionId: String,
    val sourceRevisionSha256: String,
    val perceptionVersion: String = CT_V2_08_PERCEPTION_VERSION,
    val disposition: PerceptionDisposition,
    val proposals: List<EvidenceProposal> = emptyList(),
    val correctionCandidate: CorrectionCandidate? = null,
    val unresolved: List<UnresolvedPerception> = emptyList(),
) {
    init {
        require(sourceRevisionId.isNotBlank())
        require(sourceRevisionSha256.matches(Regex("^[0-9a-f]{64}$")))
        if (disposition == PerceptionDisposition.UNDERSTOOD) {
            require(proposals.isNotEmpty() || correctionCandidate != null)
        }
        if (disposition == PerceptionDisposition.NO_EVIDENCE_PROPOSAL) {
            require(proposals.isEmpty() && correctionCandidate == null)
        }
    }
}

interface LanguagePerceptionEngine {
    fun perceive(source: CommittedSourceText, context: PerceptionContext = PerceptionContext()): LanguagePerceptionResult
}

data class ProposalValidationIssue(val code: String, val objectId: String)

data class ProposalValidation(val issues: List<ProposalValidationIssue>) {
    val accepted: Boolean get() = issues.isEmpty()
}

/** Revalidates extractive claims before any CT-V2-07 admission request is constructed. */
class LanguageEvidenceProposalValidator {
    fun validate(source: CommittedSourceText, result: LanguagePerceptionResult): ProposalValidation {
        val issues = mutableListOf<ProposalValidationIssue>()
        if (result.sourceRevisionId != source.source.id.value) {
            issues += ProposalValidationIssue("WRONG_SOURCE_REVISION", result.sourceRevisionId)
        }
        if (result.sourceRevisionSha256 != source.revisionSha256) {
            issues += ProposalValidationIssue("WRONG_SOURCE_FINGERPRINT", result.sourceRevisionId)
        }
        val assertions = result.proposals.map { it.assertion } + listOfNotNull(result.correctionCandidate?.correctingAssertion)
        val lowered = source.exactText.lowercase()
        val speculative = listOf("maybe", "probably", "i think", "i guess", "i feel like").any { marker -> marker in lowered }
        val nonassertive = source.exactText.trim().endsWith('?') || lowered.startsWith("if i'd ") ||
            lowered.startsWith("imagine ") || lowered.startsWith("suppose ") || lowered.startsWith("what if ")
        assertions.forEach { assertion ->
            val grounding = assertion.sourceGrounding
            if (assertion.sourceRecordId != source.source.id) {
                issues += ProposalValidationIssue("ASSERTION_SOURCE_MISMATCH", assertion.id.value)
            }
            if (grounding == null) {
                issues += ProposalValidationIssue("SOURCE_SPAN_REQUIRED", assertion.id.value)
                return@forEach
            }
            if (speculative && assertion.kind == UserEvidenceKind.EXPLICIT_USER_ASSERTION &&
                assertion.epistemicClass == com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION
            ) {
                issues += ProposalValidationIssue("SPECULATIVE_LANGUAGE_CANNOT_BE_DIRECT_FACT", assertion.id.value)
            }
            if (nonassertive) {
                issues += ProposalValidationIssue("NONASSERTIVE_LANGUAGE_CANNOT_FORM_EVIDENCE", assertion.id.value)
            }
            if (" told me " in lowered &&
                assertion.epistemicClass != com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass.THIRD_PARTY_REPORT
            ) {
                issues += ProposalValidationIssue("REPORTED_SPEECH_CLASS_REQUIRED", assertion.id.value)
            }
            if (grounding.sourceRevisionId != source.source.id) {
                issues += ProposalValidationIssue("SPAN_SOURCE_MISMATCH", assertion.id.value)
            }
            if (grounding.sourceRevisionSha256 != source.revisionSha256) {
                issues += ProposalValidationIssue("SPAN_FINGERPRINT_MISMATCH", assertion.id.value)
            }
            if (grounding.startOffsetInclusive >= source.exactText.length ||
                grounding.endOffsetExclusive > source.exactText.length
            ) {
                issues += ProposalValidationIssue("SPAN_OUT_OF_RANGE", assertion.id.value)
            } else if (source.exactText.substring(grounding.startOffsetInclusive, grounding.endOffsetExclusive) != grounding.exactFragment) {
                issues += ProposalValidationIssue("SPAN_TEXT_MISMATCH", assertion.id.value)
            }
        }
        return ProposalValidation(issues.distinct().sortedWith(compareBy({ it.code }, { it.objectId })))
    }
}
