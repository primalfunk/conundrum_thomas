package com.conundrum.thomas.v2.languagerenderer

import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.CalendarBoundary
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class RenderableGrounding(
    val id: String,
    val surfaceMeaning: String,
    val requiredMarkerGroups: List<Set<String>>,
    val epistemicClass: EvidenceEpistemicClass = EvidenceEpistemicClass.EXPLICIT_USER_ASSERTION,
    val uncertainty: AssertionUncertainty = AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
    val temporalScope: EventTime? = null,
    val allowedEntityNames: Set<String> = emptySet(),
    val allowedTemporalLiterals: Set<String> = emptySet(),
) {
    init {
        require(id.isNotBlank() && surfaceMeaning.isNotBlank())
        require(requiredMarkerGroups.isNotEmpty())
        require(requiredMarkerGroups.all { it.isNotEmpty() && it.none(String::isBlank) })
    }
}

internal data class EpistemicRendering(
    val status: RenderEpistemicStatus,
    val attribution: RenderAttribution,
    val markers: Set<String>,
)

internal fun epistemicRendering(value: EvidenceEpistemicClass): EpistemicRendering = when (value) {
    EvidenceEpistemicClass.EXPLICIT_SELF_REPORT -> EpistemicRendering(
        RenderEpistemicStatus.USER_SELF_REPORTED, RenderAttribution.USER_SELF_REPORT,
        setOf("you described", "you felt", "you feel"),
    )
    EvidenceEpistemicClass.SELF_BELIEF -> EpistemicRendering(
        RenderEpistemicStatus.USER_BELIEVES, RenderAttribution.USER_BELIEF,
        setOf("believing", "you think", "your belief"),
    )
    EvidenceEpistemicClass.USER_INTERPRETATION -> EpistemicRendering(
        RenderEpistemicStatus.USER_INTERPRETS, RenderAttribution.USER_INTERPRETATION,
        setOf("your sense", "you think", "you described"),
    )
    EvidenceEpistemicClass.THIRD_PARTY_REPORT -> EpistemicRendering(
        RenderEpistemicStatus.THIRD_PARTY_REPORTED, RenderAttribution.THIRD_PARTY,
        setOf("reported", "said", "told"),
    )
    else -> EpistemicRendering(
        RenderEpistemicStatus.USER_ASSERTED, RenderAttribution.CURRENT_USER,
        setOf("you described", "you said", "you wrote"),
    )
}

internal fun renderUnit(grounding: RenderableGrounding, kind: SemanticUnitKind, use: SemanticUnitUse): AuthorizedSemanticUnit {
    val epistemic = epistemicRendering(grounding.epistemicClass)
    return AuthorizedSemanticUnit(
        grounding.id, kind, grounding.surfaceMeaning, epistemic.status, epistemic.attribution,
        SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING, grounding.temporalScope,
        grounding.uncertainty.name, setOf(use), requiredMarkerGroups = grounding.requiredMarkerGroups,
    )
}

internal fun uncertaintyMarkers(value: AssertionUncertainty): Set<String> = when (value) {
    AssertionUncertainty.STATED_WITHOUT_QUALIFICATION -> setOf("you")
    AssertionUncertainty.STATED_AS_UNCERTAIN -> setOf("maybe", "might", "not sure", "uncertain")
    AssertionUncertainty.APPROXIMATE -> setOf("around", "approximately", "about")
    AssertionUncertainty.CONTESTED -> setOf("uncertain", "differing", "not settled")
}

internal fun temporalConstraints(unit: AuthorizedSemanticUnit): List<RenderTemporalConstraint> {
    val time = unit.temporalScope ?: return emptyList()
    val marker = temporalMarkers(time)
    return listOf(RenderTemporalConstraint(unit.id, marker.first, marker.second))
}

internal fun temporalMarkers(time: EventTime): Pair<Set<String>, Set<String>> = when (time) {
    is EventTime.ExactInstant -> setOf(time.value.atZone(ZoneOffset.UTC).toLocalDate().toString()) to emptySet()
    is EventTime.CalendarDate -> setOf(time.value.toString()) to emptySet()
    is EventTime.ApproximateDate -> setOf("around", "approximately", "about") to setOf(time.center.toString())
    is EventTime.ApproximateYear -> setOf("around", "approximately", "about") to emptySet()
    is EventTime.Range -> setOf("between", "from", "range") to emptySet()
    is EventTime.RelativePeriod -> setOf(time.description) to emptySet()
    is EventTime.OngoingInterval -> setOf("ongoing", "still", time.description) to emptySet()
    is EventTime.BeforeOrAfter -> setOf(time.description) to emptySet()
    is EventTime.UncertainChronology -> setOf("uncertain", "not sure", "possibly") to emptySet()
    is EventTime.Unknown -> setOf("unknown", "don't know", "not sure") to emptySet()
}

internal fun temporalLiterals(time: EventTime?): Set<String> = when (time) {
    null -> emptySet()
    is EventTime.ExactInstant -> setOf(
        time.value.atZone(ZoneOffset.UTC).year.toString(),
        time.value.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE),
    )
    is EventTime.CalendarDate -> setOf(time.value.year.toString(), time.value.toString())
    is EventTime.ApproximateDate -> setOf(time.center.year.toString())
    is EventTime.ApproximateYear -> setOf(time.year.toString())
    is EventTime.Range -> boundaryLiterals(time.start) + boundaryLiterals(time.end)
    is EventTime.UncertainChronology -> time.candidateLabels.toSet()
    else -> emptySet()
}

private fun boundaryLiterals(boundary: CalendarBoundary): Set<String> = when (boundary) {
    is CalendarBoundary.Date -> setOf(boundary.value.year.toString(), boundary.value.toString())
    is CalendarBoundary.Month -> setOf(boundary.value.year.toString(), boundary.value.toString())
    is CalendarBoundary.CalendarYear -> setOf(boundary.value.toString())
}

internal fun silenceCommand(id: RenderCommandId, turn: Int, mode: GovernedRenderMode) = GovernedRenderCommand(
    id, turn, mode, GovernedSemanticAct.NO_RESPONSE,
    responsePosture = GovernedResponsePosture.SILENT, semanticUnits = emptyList(),
    budget = RenderBudget.SILENCE, style = RenderStyleContract(),
    directivenessLimit = RenderDirectness.GENTLE, advicePermitted = false,
    memoryReferencePermission = MemoryReferencePermission.NONE,
    authorizedReferenceRealizations = emptyList(), deterministicFallbackText = null,
    maximumExternalAttempts = 0, fallbackAuthority = RenderFallbackAuthority.NO_RESPONSE_ONLY,
)
