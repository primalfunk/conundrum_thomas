package com.conundrum.thomas.v2.domain.mode

/** The three user-facing Thomas functions. This set is architectural and intentionally closed. */
enum class ThomasMode(val stableId: String) {
    THERAPIST("mode.therapist"),
    BIOGRAPHER("mode.biographer"),
    JOURNAL("mode.journal"),
}

enum class ModePrimaryFunction {
    INTERVENTION,
    INVESTIGATION,
    CAPTURE,
}

/** A future responsibility named by the contract, not authority granted in CT-V2-02. */
enum class FutureModeResponsibility {
    GOVERNED_THERAPEUTIC_ACTION_SELECTION,
    GOVERNED_INFORMATION_GAP_SELECTION,
    GOVERNED_ENTRY_CAPTURE,
}

enum class DefaultResponseDisposition {
    FUTURE_PROCEDURAL_SELECTION_REQUIRED,
    NO_RESPONSE,
}

enum class EvidenceDisposition {
    SUBMIT_OBSERVATIONS_TO_GOVERNED_STATE,
    SUBMIT_INVESTIGATIVE_EVIDENCE_FOR_GOVERNED_ADMISSION,
    SUBMIT_CAPTURE_FOR_GOVERNED_PRESERVATION,
}

enum class DirectProfileMutationAuthority {
    NONE,
}

enum class LanguageModelDecisionAuthority {
    NONE,
}

enum class ModeRuntimeAuthority {
    NOT_GRANTED_IN_CT_V2_02,
}

data class ModeAuthorityContract(
    val mode: ThomasMode,
    val primaryFunction: ModePrimaryFunction,
    val governingQuestion: String,
    val futureResponsibility: FutureModeResponsibility,
    val defaultResponse: DefaultResponseDisposition,
    val evidenceDisposition: EvidenceDisposition,
    val directProfileMutationAuthority: DirectProfileMutationAuthority = DirectProfileMutationAuthority.NONE,
    val languageModelDecisionAuthority: LanguageModelDecisionAuthority = LanguageModelDecisionAuthority.NONE,
    val runtimeAuthority: ModeRuntimeAuthority = ModeRuntimeAuthority.NOT_GRANTED_IN_CT_V2_02,
) {
    init {
        require(governingQuestion.isNotBlank()) { "A mode contract requires a governing question." }
    }
}

/** Declarative contracts only. No method here performs a mode operation. */
object ThomasModeAuthorityContracts {
    val therapist = ModeAuthorityContract(
        mode = ThomasMode.THERAPIST,
        primaryFunction = ModePrimaryFunction.INTERVENTION,
        governingQuestion = "What, if anything, would be helpful to do next?",
        futureResponsibility = FutureModeResponsibility.GOVERNED_THERAPEUTIC_ACTION_SELECTION,
        defaultResponse = DefaultResponseDisposition.FUTURE_PROCEDURAL_SELECTION_REQUIRED,
        evidenceDisposition = EvidenceDisposition.SUBMIT_OBSERVATIONS_TO_GOVERNED_STATE,
    )

    val biographer = ModeAuthorityContract(
        mode = ThomasMode.BIOGRAPHER,
        primaryFunction = ModePrimaryFunction.INVESTIGATION,
        governingQuestion = "What is worth learning next?",
        futureResponsibility = FutureModeResponsibility.GOVERNED_INFORMATION_GAP_SELECTION,
        defaultResponse = DefaultResponseDisposition.FUTURE_PROCEDURAL_SELECTION_REQUIRED,
        evidenceDisposition = EvidenceDisposition.SUBMIT_INVESTIGATIVE_EVIDENCE_FOR_GOVERNED_ADMISSION,
    )

    val journal = ModeAuthorityContract(
        mode = ThomasMode.JOURNAL,
        primaryFunction = ModePrimaryFunction.CAPTURE,
        governingQuestion = "What should be preserved from this entry?",
        futureResponsibility = FutureModeResponsibility.GOVERNED_ENTRY_CAPTURE,
        defaultResponse = DefaultResponseDisposition.NO_RESPONSE,
        evidenceDisposition = EvidenceDisposition.SUBMIT_CAPTURE_FOR_GOVERNED_PRESERVATION,
    )

    val all: List<ModeAuthorityContract> = listOf(therapist, biographer, journal)
}
