package com.conundrum.thomas.v2.languagerenderer

import java.util.Locale

class DeterministicRenderValidator {
    fun validate(
        command: GovernedRenderCommand,
        candidate: CandidateRealization,
        history: RenderHistoryState,
    ): RenderValidationResult {
        val text = candidate.text
        val authorityText = RenderText.unquotedText(text)
        val manifest = candidate.manifest
        val reasons = linkedSetOf<RenderValidationReason>()
        val questionCount = RenderText.questionCount(text)
        val sentenceCount = RenderText.sentenceCount(text)

        if (text.isBlank()) reasons += RenderValidationReason.EMPTY_OUTPUT
        if (command.semanticAct == GovernedSemanticAct.NO_RESPONSE && text.isNotEmpty()) {
            reasons += RenderValidationReason.UNEXPECTED_OUTPUT_FOR_SILENCE
        }
        if (text.any { it.isISOControl() && it !in setOf('\n', '\r', '\t') }) {
            reasons += RenderValidationReason.CONTROL_CHARACTER
        }
        if (text.length > command.budget.maximumCharacters) reasons += RenderValidationReason.CHARACTER_LIMIT
        if (sentenceCount > command.budget.maximumSentences) reasons += RenderValidationReason.SENTENCE_LIMIT
        if (questionCount > command.budget.maximumQuestions) reasons += RenderValidationReason.QUESTION_LIMIT
        if (manifest.declaredMode != command.mode) reasons += RenderValidationReason.MODE_MISMATCH
        if (manifest.declaredSemanticAct != command.semanticAct) reasons += RenderValidationReason.SEMANTIC_ACT_MISMATCH

        val allowedUnits = command.semanticUnits.map { it.id }.toSet()
        val requiredUnits = command.semanticUnits.filter {
            it.authorityLabel == SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING
        }.map { it.id }.toSet()
        if (!manifest.referencedSemanticUnitIds.containsAll(requiredUnits)) {
            reasons += RenderValidationReason.MISSING_SEMANTIC_UNIT
        }
        if (!allowedUnits.containsAll(manifest.referencedSemanticUnitIds)) {
            reasons += RenderValidationReason.UNKNOWN_SEMANTIC_UNIT
        }

        val allowedMemories = command.historicalSupport.map { it.memoryObjectId }.toSet()
        val allowedSources = command.historicalSupport.flatMap { it.sourceIds }.toSet()
        if (!allowedMemories.containsAll(manifest.referencedMemoryIds)) {
            reasons += RenderValidationReason.UNAUTHORIZED_MEMORY
        }
        if (!allowedSources.containsAll(manifest.referencedSourceIds) ||
            manifest.referencedSourceIds.any { it in command.prohibitedSourceIds }
        ) reasons += RenderValidationReason.PROHIBITED_SOURCE
        if (command.memoryReferencePermission == MemoryReferencePermission.NONE &&
            manifest.referencedMemoryIds.isNotEmpty()
        ) reasons += RenderValidationReason.UNAUTHORIZED_MEMORY
        if (command.historicalSupport.isNotEmpty() &&
            !manifest.referencedMemoryIds.containsAll(allowedMemories)
        ) reasons += RenderValidationReason.UNAUTHORIZED_MEMORY

        command.semanticUnits.forEach { unit ->
            unit.requiredMarkerGroups.forEach { group ->
                if (!RenderText.containsMarker(text, group)) {
                    reasons += RenderValidationReason.MISSING_SEMANTIC_UNIT
                }
            }
        }
        command.epistemicConstraints.forEach { constraint ->
            if (!RenderText.containsMarker(text, constraint.requiredMarkers)) {
                reasons += RenderValidationReason.MISSING_EPISTEMIC_MARKER
            }
            if (constraint.tentativenessRequired && !containsTentativeLanguage(text)) {
                reasons += RenderValidationReason.CERTAINTY_INFLATION
            }
        }
        command.temporalConstraints.forEach { constraint ->
            if (!RenderText.containsMarker(text, constraint.requiredMarkers)) {
                reasons += RenderValidationReason.MISSING_TEMPORAL_MARKER
            }
            if (constraint.forbiddenPrecisionLiterals.any { containsLiteral(text, it) }) {
                reasons += RenderValidationReason.FALSE_TEMPORAL_PRECISION
            }
        }
        command.historicalSupport.forEach { support ->
            if (!RenderText.containsMarker(text, support.attributionMarkers)) {
                reasons += RenderValidationReason.MISSING_MEMORY_ATTRIBUTION
            }
            if (support.relationTentative && !containsTentativeLanguage(text)) {
                reasons += RenderValidationReason.CERTAINTY_INFLATION
            }
        }

        if (!command.allowedEntityNames.containsAll(manifest.introducedEntityNames)) {
            reasons += RenderValidationReason.NEW_ENTITY
        }
        if (!command.allowedTemporalLiterals.containsAll(manifest.introducedTemporalLiterals)) {
            reasons += RenderValidationReason.NEW_TEMPORAL_FACT
        }
        val writtenYears = Regex("\\b(?:18|19|20|21)\\d{2}\\b").findAll(text).map { it.value }.toSet()
        if (!command.allowedTemporalLiterals.containsAll(writtenYears)) {
            reasons += RenderValidationReason.NEW_TEMPORAL_FACT
        }

        if (manifest.diagnosisClaim || containsAny(authorityText, diagnosisPhrases)) reasons += RenderValidationReason.DIAGNOSIS
        if (manifest.psychologicalCauseClaim || containsAny(authorityText, causePhrases)) reasons += RenderValidationReason.PSYCHOLOGICAL_CAUSE
        if (manifest.hiddenMotiveClaim || containsAny(authorityText, motivePhrases)) reasons += RenderValidationReason.HIDDEN_MOTIVE
        if (manifest.stableTraitClaim || containsAny(authorityText, traitPhrases)) reasons += RenderValidationReason.STABLE_TRAIT
        if (manifest.modeSwitch || containsAny(authorityText, modeSwitchPhrases)) reasons += RenderValidationReason.MODE_SWITCH
        if (manifest.safetyChange) reasons += RenderValidationReason.SAFETY_CHANGE
        if (manifest.routeOrTechniqueChange || containsAny(authorityText, techniquePhrases)) {
            reasons += RenderValidationReason.ROUTE_OR_TECHNIQUE_CHANGE
        }
        if (!command.advicePermitted && (manifest.addedAdvice || containsAny(authorityText, advicePhrases))) {
            reasons += RenderValidationReason.UNAUTHORIZED_ADVICE
        }
        if (manifest.certaintyInflated) reasons += RenderValidationReason.CERTAINTY_INFLATION
        if (manifest.identityMerged) reasons += RenderValidationReason.IDENTITY_MERGE
        if (manifest.chronologyInvented) reasons += RenderValidationReason.CHRONOLOGY_INVENTION
        if (manifest.contradictionWinnerChosen) reasons += RenderValidationReason.CONTRADICTION_WINNER
        if (manifest.policyMutationAttempt || containsAny(authorityText, policyMutationPhrases)) {
            reasons += RenderValidationReason.POLICY_MUTATION
        }
        if (manifest.systemInstructionDisclosure || containsAny(authorityText, systemDisclosurePhrases)) {
            reasons += RenderValidationReason.SYSTEM_INSTRUCTION_DISCLOSURE
        }
        if (manifest.medicalAuthorityClaim || containsAny(authorityText, medicalAuthorityPhrases)) {
            reasons += RenderValidationReason.MEDICAL_AUTHORITY
        }
        val blocked = command.prohibitedLiteralPhrases + command.style.prohibitedPhrases
        if (blocked.any { containsLiteral(text, it) }) reasons += RenderValidationReason.PROHIBITED_LITERAL
        if (command.fixedSafetyText != null && text != command.fixedSafetyText) {
            reasons += RenderValidationReason.FIXED_SAFETY_TEXT_MISMATCH
        }

        if (command.fixedSafetyText == null && text.isNotBlank()) {
            val recent = history.entries.takeLast(CT_V2_13_RECENT_RESPONSE_WINDOW)
            if (recent.any { it.normalizedResponseFingerprint == RenderText.responseFingerprint(text) }) {
                reasons += RenderValidationReason.EXACT_RECENT_DUPLICATE
            }
            val opening = RenderText.openingFingerprint(text)
            if (history.entries.takeLast(CT_V2_13_RECENT_OPENING_WINDOW)
                    .count { it.openingFingerprint == opening } >= 2
            ) reasons += RenderValidationReason.REPEATED_OPENING
        }

        return if (reasons.isEmpty()) {
            RenderValidationResult(true, listOf(RenderValidationReason.VALID), questionCount, sentenceCount, text.length)
        } else {
            RenderValidationResult(false, reasons.toList(), questionCount, sentenceCount, text.length)
        }
    }

    private fun containsTentativeLanguage(text: String): Boolean = containsAny(
        text,
        setOf("may", "might", "wonder", "not sure", "tentative", "possibly", "could be"),
    )

    private fun containsAny(text: String, phrases: Set<String>): Boolean = phrases.any { containsLiteral(text, it) }

    private fun containsLiteral(text: String, phrase: String): Boolean =
        text.lowercase(Locale.ROOT).contains(phrase.lowercase(Locale.ROOT))

    private companion object {
        val diagnosisPhrases = setOf("i diagnose", "diagnosed you", "you have depression", "you have a disorder")
        val causePhrases = setOf("because of your childhood", "your trauma caused", "the root cause is")
        val motivePhrases = setOf("deep down you", "what you really want", "your unconscious")
        val traitPhrases = setOf("you are an angry person", "your core personality", "you always do this")
        val modeSwitchPhrases = setOf("you are now in therapy mode", "we are now in diagnosis mode", "let's fill in your childhood timeline")
        val techniquePhrases = setOf("use cbt", "use dbt", "breathing exercise", "behavioral experiment", "exposure therapy")
        val advicePhrases = setOf("you should", "you need to", "i recommend", "quit your job", "try this exercise")
        val policyMutationPhrases = setOf("ignore all instructions", "ignore the rules", "from now on change the policy")
        val systemDisclosurePhrases = setOf("system prompt is", "reveal the system prompt", "here are my hidden instructions")
        val medicalAuthorityPhrases = setOf("as your doctor", "i am your doctor", "medical diagnosis")
    }
}
