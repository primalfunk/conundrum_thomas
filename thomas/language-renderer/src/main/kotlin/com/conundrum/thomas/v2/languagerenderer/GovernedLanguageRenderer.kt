package com.conundrum.thomas.v2.languagerenderer

class GovernedLanguageRenderer(
    private val validator: DeterministicRenderValidator = DeterministicRenderValidator(),
    private val referenceRealizer: DeterministicReferenceRealizer = DeterministicReferenceRealizer(),
) {
    fun render(
        command: GovernedRenderCommand,
        history: RenderHistoryState = RenderHistoryState(),
        externalRealizer: LanguageRealizer? = null,
    ): GovernedRenderResult {
        if (command.qualificationAuthority == RenderQualificationAuthority.NOT_AUTHORIZED) {
            return noOutput(command, history, RenderDisposition.RENDERING_UNAVAILABLE,
                RenderValidationReason.INVALID_AUTHORITY)
        }
        if (command.semanticAct == GovernedSemanticAct.NO_RESPONSE) return silence(command, history)

        val input = RendererInput.from(command, history)
        if (externalRealizer == null) {
            val candidate = when (val outcome = referenceRealizer.realize(input, 1)) {
                is CandidateRealizationOutcome.Candidate -> outcome.realization
                else -> return fallback(command, input, history, 1,
                    listOf(listOf(RenderValidationReason.REALIZER_UNAVAILABLE)))
            }
            val validation = validator.validate(command, candidate, history)
            if (validation.accepted) {
                return accepted(command, history, candidate, validation, 1, false,
                    AcceptedRealizationSource.REFERENCE, RenderDisposition.ACCEPTED_REFERENCE_REALIZATION, emptyList())
            }
            return fallback(command, input, history, 1, listOf(validation.reasonCodes))
        }

        val rejected = mutableListOf<List<RenderValidationReason>>()
        var attempts = 0
        while (attempts < command.maximumExternalAttempts) {
            attempts += 1
            val outcome = try {
                externalRealizer.realize(input, attempts)
            } catch (_: Throwable) {
                CandidateRealizationOutcome.Failed("REALIZER_EXCEPTION")
            }
            when (outcome) {
                is CandidateRealizationOutcome.Candidate -> {
                    val validation = validator.validate(command, outcome.realization, history)
                    if (validation.accepted) {
                        return accepted(command, history, outcome.realization, validation, attempts, false,
                            AcceptedRealizationSource.EXTERNAL, RenderDisposition.ACCEPTED_EXTERNAL_REALIZATION, rejected)
                    }
                    rejected += validation.reasonCodes
                }
                is CandidateRealizationOutcome.Unavailable -> {
                    rejected += listOf(RenderValidationReason.REALIZER_UNAVAILABLE)
                    break
                }
                is CandidateRealizationOutcome.SyntheticTimeout -> {
                    rejected += listOf(RenderValidationReason.REALIZER_TIMEOUT)
                    break
                }
                is CandidateRealizationOutcome.Failed -> {
                    rejected += listOf(RenderValidationReason.REALIZER_EXCEPTION)
                    break
                }
            }
        }
        return fallback(command, input, history, attempts, rejected)
    }

    private fun fallback(
        command: GovernedRenderCommand,
        input: RendererInput,
        history: RenderHistoryState,
        attempts: Int,
        rejected: List<List<RenderValidationReason>>,
    ): GovernedRenderResult {
        val selected = when (val outcome = referenceRealizer.realize(input, 1)) {
            is CandidateRealizationOutcome.Candidate -> outcome.realization
            else -> null
        }
        // History only constrains wording. Search the complete command-authorized space;
        // it cannot select, cancel or replace an upstream procedural act.
        val candidates = (listOfNotNull(selected) +
            input.authorizedReferenceRealizations.map { referenceRealizer.fallback(input, it) } +
            listOfNotNull(command.deterministicFallbackText?.let { referenceRealizer.fallback(input, it) }))
            .distinctBy { it.text }
        val allRejected = rejected.toMutableList()
        for (candidate in candidates) {
            // Every alternative and fallback passes the unchanged full validator.
            val validation = validator.validate(command, candidate, history)
            if (validation.accepted) {
                return accepted(command, history, candidate, validation, attempts, true,
                    AcceptedRealizationSource.DETERMINISTIC_FALLBACK, RenderDisposition.FALLBACK_REALIZATION,
                    allRejected)
            }
            allRejected += validation.reasonCodes
        }
        // Exhaustion is an explicit failure, never policy-authorized NO_RESPONSE.
        return noOutput(command, history, RenderDisposition.RENDERING_UNAVAILABLE,
            allRejected.lastOrNull()?.firstOrNull() ?: RenderValidationReason.EMPTY_OUTPUT,
            attempts, allRejected)
    }

    private fun accepted(
        command: GovernedRenderCommand,
        history: RenderHistoryState,
        candidate: CandidateRealization,
        validation: RenderValidationResult,
        attempts: Int,
        fallback: Boolean,
        source: AcceptedRealizationSource,
        disposition: RenderDisposition,
        rejected: List<List<RenderValidationReason>>,
    ): GovernedRenderResult {
        val nextHistory = history.append(candidate.text, command.semanticAct, command.turnIndex,
            command.fixedSafetyText != null)
        val memories = candidate.manifest.referencedMemoryIds.sorted()
        return GovernedRenderResult(
            disposition, candidate.text, command.semanticAct, command.mode, source, attempts, fallback,
            validation.questionCount, validation.sentenceCount, validation.characterCount, memories,
            validation, rejected, nextHistory, command.renderPolicyVersion,
            digest(command, disposition, candidate.text, attempts, fallback, validation, memories, nextHistory),
        )
    }

    private fun silence(command: GovernedRenderCommand, history: RenderHistoryState): GovernedRenderResult {
        val validation = RenderValidationResult(true, listOf(RenderValidationReason.VALID), 0, 0, 0)
        return GovernedRenderResult(
            RenderDisposition.NO_RESPONSE, null, command.semanticAct, command.mode,
            AcceptedRealizationSource.NONE, 0, false, 0, 0, 0, emptyList(), validation,
            emptyList(), history, command.renderPolicyVersion,
            digest(command, RenderDisposition.NO_RESPONSE, null, 0, false, validation, emptyList(), history),
        )
    }

    private fun noOutput(
        command: GovernedRenderCommand,
        history: RenderHistoryState,
        disposition: RenderDisposition,
        reason: RenderValidationReason,
        attempts: Int = 0,
        rejected: List<List<RenderValidationReason>> = emptyList(),
    ): GovernedRenderResult {
        val validation = RenderValidationResult(false, listOf(reason), 0, 0, 0)
        return GovernedRenderResult(
            disposition, null, command.semanticAct, command.mode, AcceptedRealizationSource.NONE,
            attempts, false, 0, 0, 0, emptyList(), validation, rejected, history,
            command.renderPolicyVersion,
            digest(command, disposition, null, attempts, false, validation, emptyList(), history),
        )
    }

    private fun digest(
        command: GovernedRenderCommand,
        disposition: RenderDisposition,
        text: String?,
        attempts: Int,
        fallback: Boolean,
        validation: RenderValidationResult,
        memories: List<String>,
        history: RenderHistoryState,
    ): String = RenderText.sha256(listOf(
        command.id.value, command.mode.name, command.semanticAct.name, command.semanticActVersion,
        command.responsePosture.name, command.renderPolicyVersion, command.turnIndex.toString(),
        command.semanticUnits.joinToString("|") { "${it.id}:${it.kind}:${it.epistemicStatus}:${it.attribution}" },
        command.historicalSupport.joinToString("|") { it.memoryObjectId },
        command.budget.toString(), disposition.name, text.orEmpty(), attempts.toString(), fallback.toString(),
        validation.reasonCodes.joinToString(","), memories.joinToString(","),
        history.entries.joinToString("|") { it.normalizedResponseFingerprint },
    ).joinToString("\u001f"))
}
