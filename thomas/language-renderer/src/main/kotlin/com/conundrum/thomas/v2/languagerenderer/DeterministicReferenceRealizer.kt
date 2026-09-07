package com.conundrum.thomas.v2.languagerenderer

class DeterministicReferenceRealizer : LanguageRealizer {
    override fun realize(input: RendererInput, attempt: Int): CandidateRealizationOutcome {
        require(attempt == 1)
        val forms = input.authorizedReferenceRealizations
        if (forms.isEmpty()) return CandidateRealizationOutcome.Unavailable("REFERENCE_FORM_UNAVAILABLE")
        // Some finite catalogs preserve a preferred realization and let the strict
        // validator govern when the existing exhaustive fallback search advances.
        if (input.referenceCatalogOrder) return CandidateRealizationOutcome.Candidate(
            candidate(input, forms.first(), CT_V2_13_REFERENCE_REALIZER_VERSION))
        val seed = RenderText.sha256(input.commandId.value).take(8).toLong(16).toInt() and Int.MAX_VALUE
        val ordered = forms.indices.map { (seed + it) % forms.size }.map(forms::get)
        val text = ordered.firstOrNull { candidate ->
            RenderText.openingFingerprint(candidate) !in input.recentOpeningFingerprints
        } ?: ordered.first()
        return CandidateRealizationOutcome.Candidate(candidate(input, text, CT_V2_13_REFERENCE_REALIZER_VERSION))
    }

    internal fun fallback(input: RendererInput, fallbackText: String): CandidateRealization =
        candidate(input, fallbackText, "ct-v2-13.deterministic-fallback.v1")

    private fun candidate(input: RendererInput, text: String, version: String): CandidateRealization =
        CandidateRealization(
            text = text,
            adapterId = "deterministic-reference-realizer",
            adapterVersion = version,
            manifest = CandidateManifest(
                declaredMode = input.mode,
                declaredSemanticAct = input.semanticAct,
                referencedSemanticUnitIds = input.semanticUnits
                    .filter { it.authorityLabel == SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING }
                    .map { it.id }.toSet(),
                referencedMemoryIds = input.historicalSupport.map { it.memoryObjectId }.toSet(),
                referencedSourceIds = input.historicalSupport.flatMap { it.sourceIds }.toSet(),
                introducedEntityNames = emptySet(),
                introducedTemporalLiterals = Regex("\\b(?:18|19|20|21)\\d{2}\\b")
                    .findAll(text).map { it.value }.toSet(),
            ),
        )
}
