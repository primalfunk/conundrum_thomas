package com.conundrum.thomas.v2.safety

import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.SourceDocumentId
import com.conundrum.thomas.v2.provenance.SourceLocatorId
import com.conundrum.thomas.v2.provenance.SourceSectionId
import com.conundrum.thomas.v2.provenance.SourceVersionId

enum class ScreeningInstrumentImplementationStatus { NOT_IMPLEMENTED }
enum class ScreeningInstrumentRuntimeAuthority { NOT_GRANTED }

/** Bibliographic governance only: contains no item wording, score, threshold, or disposition logic. */
data class ScreeningInstrumentReference(
    val referenceId: String,
    val source: GovernedSourceReference,
    val intendedPopulation: String,
    val intendedSetting: String,
    val requiredAdministrator: String,
    val followUpRequirement: String,
    val softwareAutonomyLimitation: String,
    val rightsLimitation: String,
    val implementationStatus: ScreeningInstrumentImplementationStatus = ScreeningInstrumentImplementationStatus.NOT_IMPLEMENTED,
    val runtimeAuthority: ScreeningInstrumentRuntimeAuthority = ScreeningInstrumentRuntimeAuthority.NOT_GRANTED,
)

object ScreeningInstrumentRegistry {
    val references = listOf(
        ScreeningInstrumentReference(
            referenceId = "nimh-asq-reference-only",
            source = GovernedSourceReference(
                SourceDocumentId.parse("nimh-asq-screening-tool"),
                SourceVersionId.parse("nimh-asq-tool-2025"),
                SourceSectionId.parse("section-nimh-asq-tool"),
                SourceLocatorId.parse("artifact-nimh-asq"),
            ),
            intendedPopulation = "Medical patients ages 8 and above, with setting- and age-specific variants",
            intendedSetting = "Medical settings with an established positive-screen management pathway",
            requiredAdministrator = "Medical staff; follow-up safety assessment by a trained clinician",
            followUpRequirement = "A positive screen requires the toolkit's governed trained-clinician follow-up pathway",
            softwareAutonomyLimitation = "The governed corpus grants no autonomous consumer-software administration or disposition authority",
            rightsLimitation = "Public-domain status does not remove NIMH medical-advice, endorsement, image, and accuracy conditions",
        ),
    )

    val implementedCount = 0
    val runtimeAuthorizedCount = 0
}
