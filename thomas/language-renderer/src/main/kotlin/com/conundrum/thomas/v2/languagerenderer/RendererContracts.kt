package com.conundrum.thomas.v2.languagerenderer

import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.EventTime

const val CT_V2_13_RENDER_POLICY_VERSION = "ct-v2-13.governed-renderer.v1"
const val CT_V2_13_REFERENCE_REALIZER_VERSION = "ct-v2-13.reference-realizer.v1"
const val CT_V2_13_VALIDATOR_VERSION = "ct-v2-13.render-validator.v1"
const val CT_V2_13_SEMANTIC_ACT_VERSION = "ct-v2-13.semantic-act.v1"
const val CT_V2_13_RECENT_RESPONSE_WINDOW = 6
const val CT_V2_13_RECENT_OPENING_WINDOW = 4
const val CT_V2_13_MAXIMUM_EXTERNAL_ATTEMPTS = 2

private val rendererIdPattern = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")

@JvmInline
value class RenderCommandId private constructor(val value: String) {
    companion object {
        fun parse(value: String): RenderCommandId {
            require(rendererIdPattern.matches(value))
            return RenderCommandId(value)
        }
    }
}

enum class GovernedRenderMode { JOURNAL, BIOGRAPHER, THERAPY, SAFETY }

enum class GovernedSemanticAct {
    NO_RESPONSE,
    BRIEF_REFLECTION,
    CLARIFYING_QUESTION,
    OPEN_QUESTION,
    AUTHORIZED_THERAPEUTIC_ACTION,
    DIRECT_MEMORY_RECALL,
    TENTATIVE_MEMORY_CONNECTION,
    EXPLICIT_RECALL,
    EVIDENCE_EXPLANATION,
    SAFETY_MESSAGE,
}

enum class GovernedResponsePosture {
    SILENT,
    JOURNAL_REFLECT,
    JOURNAL_ASK_ONE_QUESTION,
    BIOGRAPHER_OPEN_STORY,
    BIOGRAPHER_TARGETED_COVERAGE,
    THERAPY,
    SAFETY,
}

enum class SemanticUnitKind {
    USER_SELF_REPORT,
    USER_BELIEF,
    USER_INTERPRETATION,
    THIRD_PARTY_REPORT,
    CURRENT_EVENT,
    CURRENT_ENTITY,
    STRUCTURAL_FACT,
    AUTHORIZED_MEMORY_REFERENCE,
    AUTHORIZED_TENTATIVE_CONNECTION,
    AUTHORIZED_REFLECTION_TARGET,
    AUTHORIZED_QUESTION_TARGET,
    AUTHORIZED_THERAPEUTIC_ACTION,
    AUTHORIZED_EVIDENCE_EXPLANATION,
    SAFETY_REQUIREMENT,
}

enum class RenderEpistemicStatus {
    USER_ASSERTED,
    USER_SELF_REPORTED,
    USER_BELIEVES,
    USER_INTERPRETS,
    THIRD_PARTY_REPORTED,
    STRUCTURAL,
    THOMAS_TENTATIVE,
    CONTESTED,
    CORRECTED_CURRENT,
}

enum class RenderAttribution {
    CURRENT_USER,
    USER_SELF_REPORT,
    USER_BELIEF,
    USER_INTERPRETATION,
    THIRD_PARTY,
    JOURNAL_SOURCE,
    BIOGRAPHER_SOURCE,
    THERAPIST_CONVERSATION_SOURCE,
    THOMAS_TENTATIVE,
    GOVERNED_POLICY,
}

enum class SemanticAuthorityLabel {
    GOVERNED_SEMANTIC_MEANING,
    CURRENT_USER_CONTENT_DATA,
    HISTORICAL_USER_SOURCE_DATA,
}

enum class SemanticUnitUse {
    REFLECTION,
    QUESTION_GROUNDING,
    ACTION_GROUNDING,
    MEMORY_RECALL,
    EVIDENCE_EXPLANATION,
    SAFETY_REALIZATION,
}

enum class ProhibitedSemanticTransformation {
    FACT_PROMOTION,
    CERTAINTY_INFLATION,
    TEMPORAL_PRECISION_INCREASE,
    IDENTITY_MERGE,
    NEW_PSYCHOLOGICAL_INTERPRETATION,
    SOURCE_AS_INSTRUCTION,
    PROVENANCE_REWRITE,
    CONTRADICTION_RESOLUTION,
    STABLE_TRAIT_PROMOTION,
}

enum class ProhibitedRenderedClaim {
    NEW_FACT,
    DIAGNOSIS,
    PSYCHOLOGICAL_CAUSE,
    HIDDEN_MOTIVE,
    STABLE_TRAIT,
    MODE_SWITCH,
    SAFETY_CHANGE,
    ROUTE_CHANGE,
    TECHNIQUE_ADDITION,
    UNAUTHORIZED_ADVICE,
    MEMORY_SELECTION,
    PRIVATE_MEMORY,
    IDENTITY_MERGE,
    CHRONOLOGY_INVENTION,
    CONTRADICTION_WINNER,
    POLICY_MUTATION,
    SYSTEM_INSTRUCTION_DISCLOSURE,
    MEDICAL_AUTHORITY,
    EVIDENCE_MUTATION,
}

data class AuthorizedSemanticUnit(
    val id: String,
    val kind: SemanticUnitKind,
    val surfaceMeaning: String,
    val epistemicStatus: RenderEpistemicStatus,
    val attribution: RenderAttribution,
    val authorityLabel: SemanticAuthorityLabel,
    val temporalScope: EventTime? = null,
    val uncertainty: String? = null,
    val allowedUses: Set<SemanticUnitUse>,
    val prohibitedTransformations: Set<ProhibitedSemanticTransformation> =
        ProhibitedSemanticTransformation.entries.toSet(),
    val requiredMarkerGroups: List<Set<String>> = emptyList(),
) {
    init {
        require(rendererIdPattern.matches(id))
        require(surfaceMeaning.isNotBlank())
        require(allowedUses.isNotEmpty())
        require(prohibitedTransformations.contains(ProhibitedSemanticTransformation.SOURCE_AS_INSTRUCTION))
        require(requiredMarkerGroups.all { it.isNotEmpty() && it.none(String::isBlank) })
        require(uncertainty == null || uncertainty.isNotBlank())
    }
}

data class AuthorizedHistoricalSupport(
    val memoryObjectId: String,
    val sourceIds: List<String>,
    val semanticUnitId: String,
    val acquisitionMode: AcquisitionMode,
    val attributionMarkers: Set<String>,
    val relationTentative: Boolean,
    val contradictionPresent: Boolean,
    val identityUnresolved: Boolean,
) {
    init {
        require(memoryObjectId.isNotBlank())
        require(sourceIds.isNotEmpty() && sourceIds.none(String::isBlank))
        require(rendererIdPattern.matches(semanticUnitId))
        require(attributionMarkers.isNotEmpty() && attributionMarkers.none(String::isBlank))
    }
}

data class RenderEpistemicConstraint(
    val semanticUnitId: String,
    val requiredMarkers: Set<String>,
    val tentativenessRequired: Boolean = false,
) {
    init {
        require(rendererIdPattern.matches(semanticUnitId))
        require(requiredMarkers.isNotEmpty() && requiredMarkers.none(String::isBlank))
    }
}

data class RenderTemporalConstraint(
    val semanticUnitId: String,
    val requiredMarkers: Set<String>,
    val forbiddenPrecisionLiterals: Set<String> = emptySet(),
) {
    init {
        require(rendererIdPattern.matches(semanticUnitId))
        require(requiredMarkers.isNotEmpty() && requiredMarkers.none(String::isBlank))
        require(forbiddenPrecisionLiterals.none(String::isBlank))
    }
}

data class RenderBudget(
    val maximumCharacters: Int,
    val maximumSentences: Int,
    val maximumQuestions: Int,
) {
    init { require(maximumCharacters >= 0 && maximumSentences >= 0 && maximumQuestions >= 0) }

    companion object {
        val SILENCE = RenderBudget(0, 0, 0)
        val BRIEF_REFLECTION = RenderBudget(320, 2, 0)
        val ONE_QUESTION = RenderBudget(320, 2, 1)
        val THERAPY = RenderBudget(640, 4, 1)
        val EXPLANATION = RenderBudget(900, 6, 2)
        val SAFETY = RenderBudget(480, 3, 1)
    }
}

enum class RenderWarmth { RESTRAINED, WARM }
enum class RenderConcision { BRIEF, COMPACT }
enum class RenderConversationality { NATURAL, DIRECT }
enum class RenderDirectness { GENTLE, BALANCED, DIRECT }
enum class RenderQuestionStyle { NONE, OPTIONAL, NEUTRAL, DIRECT_REQUIRED }
enum class RenderAcknowledgementStrength { NONE, LIGHT, CLEAR }

data class RenderStyleContract(
    val warmth: RenderWarmth = RenderWarmth.RESTRAINED,
    val concision: RenderConcision = RenderConcision.BRIEF,
    val conversationality: RenderConversationality = RenderConversationality.NATURAL,
    val directness: RenderDirectness = RenderDirectness.BALANCED,
    val questionStyle: RenderQuestionStyle = RenderQuestionStyle.NONE,
    val acknowledgementStrength: RenderAcknowledgementStrength = RenderAcknowledgementStrength.LIGHT,
    val prohibitedPhrases: Set<String> = emptySet(),
) {
    init { require(prohibitedPhrases.none(String::isBlank)) }
}

enum class MemoryReferencePermission { NONE, ONE_AUTHORIZED, BOUNDED_EXPLICIT }
enum class RenderFallbackAuthority { DETERMINISTIC_FALLBACK, NO_RESPONSE_ONLY }
enum class RenderQualificationAuthority { SYNTHETIC_QUALIFICATION_ONLY, ANDROID_PRODUCTION, NOT_AUTHORIZED }

data class GovernedRenderCommand(
    val id: RenderCommandId,
    val turnIndex: Int,
    val mode: GovernedRenderMode,
    val semanticAct: GovernedSemanticAct,
    val semanticActVersion: String = CT_V2_13_SEMANTIC_ACT_VERSION,
    val responsePosture: GovernedResponsePosture,
    val semanticUnits: List<AuthorizedSemanticUnit>,
    val historicalSupport: List<AuthorizedHistoricalSupport> = emptyList(),
    val epistemicConstraints: List<RenderEpistemicConstraint> = emptyList(),
    val temporalConstraints: List<RenderTemporalConstraint> = emptyList(),
    val prohibitedClaims: Set<ProhibitedRenderedClaim> = ProhibitedRenderedClaim.entries.toSet(),
    val budget: RenderBudget,
    val style: RenderStyleContract,
    val directivenessLimit: RenderDirectness,
    val advicePermitted: Boolean,
    val memoryReferencePermission: MemoryReferencePermission,
    val allowedEntityNames: Set<String> = emptySet(),
    val allowedTemporalLiterals: Set<String> = emptySet(),
    val prohibitedSourceIds: Set<String> = emptySet(),
    val prohibitedLiteralPhrases: Set<String> = emptySet(),
    val authorizedReferenceRealizations: List<String>,
    val deterministicFallbackText: String?,
    val fixedSafetyText: String? = null,
    val maximumExternalAttempts: Int = CT_V2_13_MAXIMUM_EXTERNAL_ATTEMPTS,
    val fallbackAuthority: RenderFallbackAuthority,
    val renderPolicyVersion: String = CT_V2_13_RENDER_POLICY_VERSION,
    val referenceCatalogOrder: Boolean = false,
    val qualificationAuthority: RenderQualificationAuthority =
        RenderQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
) {
    init {
        require(turnIndex >= 0)
        require(semanticActVersion == CT_V2_13_SEMANTIC_ACT_VERSION)
        require(renderPolicyVersion == CT_V2_13_RENDER_POLICY_VERSION)
        require(maximumExternalAttempts in 0..CT_V2_13_MAXIMUM_EXTERNAL_ATTEMPTS)
        require(semanticUnits.map { it.id }.distinct().size == semanticUnits.size)
        require(historicalSupport.map { it.memoryObjectId }.distinct().size == historicalSupport.size)
        require(historicalSupport.all { memory -> semanticUnits.any { it.id == memory.semanticUnitId } })
        require(epistemicConstraints.all { constraint -> semanticUnits.any { it.id == constraint.semanticUnitId } })
        require(temporalConstraints.all { constraint -> semanticUnits.any { it.id == constraint.semanticUnitId } })
        require(prohibitedSourceIds.none(String::isBlank))
        require(prohibitedLiteralPhrases.none(String::isBlank))
        if (semanticAct == GovernedSemanticAct.NO_RESPONSE) {
            require(responsePosture == GovernedResponsePosture.SILENT)
            require(budget == RenderBudget.SILENCE)
            require(authorizedReferenceRealizations.isEmpty() && deterministicFallbackText == null)
            require(maximumExternalAttempts == 0)
            require(fallbackAuthority == RenderFallbackAuthority.NO_RESPONSE_ONLY)
            require(historicalSupport.isEmpty() && memoryReferencePermission == MemoryReferencePermission.NONE)
        } else {
            require(budget.maximumCharacters > 0 && budget.maximumSentences > 0)
            require(semanticUnits.isNotEmpty())
            require(authorizedReferenceRealizations.isNotEmpty())
            require(deterministicFallbackText?.isNotBlank() == true)
            require(fallbackAuthority == RenderFallbackAuthority.DETERMINISTIC_FALLBACK)
            require(authorizedReferenceRealizations.none(String::isBlank))
            require(authorizedReferenceRealizations.all { it.length <= budget.maximumCharacters })
            require(deterministicFallbackText.length <= budget.maximumCharacters)
        }
        when (memoryReferencePermission) {
            MemoryReferencePermission.NONE -> require(historicalSupport.isEmpty())
            MemoryReferencePermission.ONE_AUTHORIZED -> require(historicalSupport.size <= 1)
            MemoryReferencePermission.BOUNDED_EXPLICIT -> require(historicalSupport.size <= 4)
        }
        if (fixedSafetyText != null) {
            require(mode == GovernedRenderMode.SAFETY && semanticAct == GovernedSemanticAct.SAFETY_MESSAGE)
            require(fixedSafetyText == deterministicFallbackText)
        }
    }
}

/** The complete and exclusive future external-realizer input. Narrative fields remain typed data. */
data class RendererInput(
    val commandId: RenderCommandId,
    val turnIndex: Int,
    val mode: GovernedRenderMode,
    val semanticAct: GovernedSemanticAct,
    val responsePosture: GovernedResponsePosture,
    val semanticUnits: List<AuthorizedSemanticUnit>,
    val historicalSupport: List<AuthorizedHistoricalSupport>,
    val epistemicConstraints: List<RenderEpistemicConstraint>,
    val temporalConstraints: List<RenderTemporalConstraint>,
    val prohibitedClaims: Set<ProhibitedRenderedClaim>,
    val budget: RenderBudget,
    val style: RenderStyleContract,
    val advicePermitted: Boolean,
    val memoryReferencePermission: MemoryReferencePermission,
    val allowedEntityNames: Set<String>,
    val allowedTemporalLiterals: Set<String>,
    val authorizedReferenceRealizations: List<String>,
    val recentOpeningFingerprints: List<String>,
    val referenceCatalogOrder: Boolean = false,
) {
    companion object {
        internal fun from(command: GovernedRenderCommand, history: RenderHistoryState) = RendererInput(
            command.id, command.turnIndex, command.mode, command.semanticAct, command.responsePosture,
            command.semanticUnits, command.historicalSupport, command.epistemicConstraints,
            command.temporalConstraints, command.prohibitedClaims, command.budget, command.style,
            command.advicePermitted, command.memoryReferencePermission, command.allowedEntityNames,
            command.allowedTemporalLiterals, command.authorizedReferenceRealizations,
            history.entries.takeLast(CT_V2_13_RECENT_OPENING_WINDOW).map { it.openingFingerprint },
            command.referenceCatalogOrder,
        )
    }
}

data class CandidateManifest(
    val declaredMode: GovernedRenderMode,
    val declaredSemanticAct: GovernedSemanticAct,
    val referencedSemanticUnitIds: Set<String>,
    val referencedMemoryIds: Set<String> = emptySet(),
    val referencedSourceIds: Set<String> = emptySet(),
    val introducedEntityNames: Set<String> = emptySet(),
    val introducedTemporalLiterals: Set<String> = emptySet(),
    val addedAdvice: Boolean = false,
    val diagnosisClaim: Boolean = false,
    val psychologicalCauseClaim: Boolean = false,
    val hiddenMotiveClaim: Boolean = false,
    val stableTraitClaim: Boolean = false,
    val modeSwitch: Boolean = false,
    val safetyChange: Boolean = false,
    val routeOrTechniqueChange: Boolean = false,
    val certaintyInflated: Boolean = false,
    val identityMerged: Boolean = false,
    val chronologyInvented: Boolean = false,
    val contradictionWinnerChosen: Boolean = false,
    val policyMutationAttempt: Boolean = false,
    val systemInstructionDisclosure: Boolean = false,
    val medicalAuthorityClaim: Boolean = false,
)

data class CandidateRealization(
    val text: String,
    val adapterId: String,
    val adapterVersion: String,
    val manifest: CandidateManifest,
) {
    init {
        require(adapterId.matches(rendererIdPattern) && adapterVersion.matches(rendererIdPattern))
    }
}

sealed interface CandidateRealizationOutcome {
    data class Candidate(val realization: CandidateRealization) : CandidateRealizationOutcome
    data class Unavailable(val reasonCode: String) : CandidateRealizationOutcome
    data class SyntheticTimeout(val reasonCode: String) : CandidateRealizationOutcome
    data class Failed(val reasonCode: String) : CandidateRealizationOutcome
}

/** A subordinate candidate producer: no store, policy, safety, retrieval, or write port appears here. */
fun interface LanguageRealizer {
    fun realize(input: RendererInput, attempt: Int): CandidateRealizationOutcome
}

data class RenderHistoryEntry(
    val normalizedResponseFingerprint: String,
    val openingFingerprint: String,
    val semanticAct: GovernedSemanticAct,
    val turnIndex: Int,
    val fixedRequiredPhrase: Boolean,
) {
    init {
        require(normalizedResponseFingerprint.matches(Regex("^[0-9a-f]{64}$")))
        require(openingFingerprint.matches(Regex("^[0-9a-f]{64}$")))
        require(turnIndex >= 0)
    }
}

data class RenderHistoryState(val entries: List<RenderHistoryEntry> = emptyList()) {
    init { require(entries.zipWithNext().all { (a, b) -> a.turnIndex <= b.turnIndex }) }

    internal fun append(text: String, act: GovernedSemanticAct, turnIndex: Int, fixed: Boolean): RenderHistoryState {
        val next = RenderHistoryEntry(
            RenderText.responseFingerprint(text), RenderText.openingFingerprint(text), act, turnIndex, fixed,
        )
        return RenderHistoryState((entries + next).takeLast(CT_V2_13_RECENT_RESPONSE_WINDOW))
    }
}

enum class RenderValidationReason {
    VALID, INVALID_AUTHORITY, REALIZER_UNAVAILABLE, REALIZER_EXCEPTION, REALIZER_TIMEOUT,
    MALFORMED_CANDIDATE, EMPTY_OUTPUT, UNEXPECTED_OUTPUT_FOR_SILENCE, CONTROL_CHARACTER, CHARACTER_LIMIT,
    SENTENCE_LIMIT, QUESTION_LIMIT, MODE_MISMATCH, SEMANTIC_ACT_MISMATCH, MISSING_SEMANTIC_UNIT,
    UNKNOWN_SEMANTIC_UNIT, UNAUTHORIZED_MEMORY, PROHIBITED_SOURCE, MISSING_EPISTEMIC_MARKER,
    MISSING_TEMPORAL_MARKER, FALSE_TEMPORAL_PRECISION, MISSING_MEMORY_ATTRIBUTION,
    CERTAINTY_INFLATION, NEW_ENTITY, NEW_TEMPORAL_FACT, DIAGNOSIS, PSYCHOLOGICAL_CAUSE,
    HIDDEN_MOTIVE, STABLE_TRAIT, MODE_SWITCH, SAFETY_CHANGE, ROUTE_OR_TECHNIQUE_CHANGE,
    UNAUTHORIZED_ADVICE, IDENTITY_MERGE, CHRONOLOGY_INVENTION, CONTRADICTION_WINNER,
    POLICY_MUTATION, SYSTEM_INSTRUCTION_DISCLOSURE, MEDICAL_AUTHORITY, PROHIBITED_LITERAL,
    FIXED_SAFETY_TEXT_MISMATCH, EXACT_RECENT_DUPLICATE, REPEATED_OPENING,
}

data class RenderValidationResult(
    val accepted: Boolean,
    val reasonCodes: List<RenderValidationReason>,
    val questionCount: Int,
    val sentenceCount: Int,
    val characterCount: Int,
) {
    init {
        require(reasonCodes.isNotEmpty())
        require(accepted == (reasonCodes == listOf(RenderValidationReason.VALID)))
        require(questionCount >= 0 && sentenceCount >= 0 && characterCount >= 0)
    }
}

enum class RenderDisposition {
    NO_RESPONSE, ACCEPTED_REFERENCE_REALIZATION, ACCEPTED_EXTERNAL_REALIZATION,
    FALLBACK_REALIZATION, REJECTED_NO_OUTPUT, RENDERING_UNAVAILABLE,
}

enum class AcceptedRealizationSource { NONE, REFERENCE, EXTERNAL, DETERMINISTIC_FALLBACK }

data class GovernedRenderResult(
    val disposition: RenderDisposition,
    val finalText: String?,
    val semanticAct: GovernedSemanticAct,
    val mode: GovernedRenderMode,
    val realizationSource: AcceptedRealizationSource,
    val candidateAttemptCount: Int,
    val fallbackUsed: Boolean,
    val questionCount: Int,
    val sentenceCount: Int,
    val characterCount: Int,
    val surfacedMemoryIds: List<String>,
    val validation: RenderValidationResult,
    val rejectedCandidateReasons: List<List<RenderValidationReason>>,
    val nextHistory: RenderHistoryState,
    val renderPolicyVersion: String,
    val canonicalRenderDigest: String,
) {
    init {
        require(candidateAttemptCount in 0..CT_V2_13_MAXIMUM_EXTERNAL_ATTEMPTS)
        require(canonicalRenderDigest.matches(Regex("^[0-9a-f]{64}$")))
        require(renderPolicyVersion == CT_V2_13_RENDER_POLICY_VERSION)
        val noOutput = disposition in setOf(
            RenderDisposition.NO_RESPONSE, RenderDisposition.REJECTED_NO_OUTPUT,
            RenderDisposition.RENDERING_UNAVAILABLE,
        )
        require(noOutput == (finalText == null))
        if (noOutput) require(realizationSource == AcceptedRealizationSource.NONE)
    }
}
