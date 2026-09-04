package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.BiographerAnswerCommand
import com.conundrum.thomas.v2.biographer.BiographerAnswerId
import com.conundrum.thomas.v2.biographer.BiographerAnswerOrigin
import com.conundrum.thomas.v2.biographer.BiographerIdempotencyKey
import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerPrivacy
import com.conundrum.thomas.v2.biographer.BiographerQuestionPlan
import com.conundrum.thomas.v2.biographer.BiographerQuestionSemanticAct
import com.conundrum.thomas.v2.biographer.CoverageCandidate
import com.conundrum.thomas.v2.biographer.CoverageStatus
import com.conundrum.thomas.v2.biographer.InvestigationTargetId
import com.conundrum.thomas.v2.biographer.InvestigationTargetKind
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.qualification.biographer.GovernedBiographerPipeline
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.nio.file.Path
import java.time.Instant

internal object CTV210TestSupport {
    fun path(name: String): Path = Path.of(
        System.getProperty("thomas.repositoryRoot"),
        "qualification",
        "build",
        "ct-v2-10",
        "$name.sqlite",
    )

    fun openStoryPlan() = BiographerQuestionPlan(
        BiographerPosture.OPEN_STORY,
        null,
        InvestigationTargetKind.OPEN_STORY,
        emptyList(),
        emptyList(),
        emptyList(),
        BiographerQuestionSemanticAct.OPEN_HISTORICAL_INVITATION,
        reasonCode = "USER_SELECTED_OPEN_STORY",
    )

    fun targetedPlan(
        id: String = "target.synthetic",
        kind: InvestigationTargetKind = InvestigationTargetKind.EVENT_DETAIL,
        grounding: List<String> = listOf("synthetic-grounding"),
    ) = BiographerQuestionPlan(
        BiographerPosture.TARGETED_COVERAGE,
        InvestigationTargetId.parse(id),
        kind,
        grounding,
        emptyList(),
        listOf("DO_NOT_ASSUME_AN_UNSTATED_EVENT_OR_CAUSE"),
        when (kind) {
            InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED -> BiographerQuestionSemanticAct.CLARIFY_IDENTITY
            InvestigationTargetKind.CONTRADICTION_CLARIFICATION -> BiographerQuestionSemanticAct.CLARIFY_CONTRADICTION
            InvestigationTargetKind.CORRECTION_TARGET_UNRESOLVED -> BiographerQuestionSemanticAct.CLARIFY_CORRECTION_TARGET
            InvestigationTargetKind.USER_NAMED_TOPIC -> BiographerQuestionSemanticAct.EXPLORE_USER_NAMED_TOPIC
            else -> BiographerQuestionSemanticAct.EXPLORE_STRUCTURAL_GAP
        },
        reasonCode = "SYNTHETIC_STRUCTURAL_TARGET",
    )

    fun command(
        harness: SyntheticLongitudinalStoreHarness,
        id: String,
        text: String,
        plan: BiographerQuestionPlan = openStoryPlan(),
        origin: BiographerAnswerOrigin = BiographerAnswerOrigin.TYPED,
        privacy: BiographerPrivacy = BiographerPrivacy.ELIGIBLE,
        key: String = "key-$id",
        expectedRevision: Long = harness.store.reader.currentStoreRevision(),
    ) = BiographerAnswerCommand(
        BiographerAnswerId.parse(id),
        BiographerIdempotencyKey.parse(key),
        expectedRevision,
        plan,
        text,
        origin,
        privacy,
        ReportTime(Instant.parse("2039-09-03T12:00:00Z")),
    )

    fun capture(
        harness: SyntheticLongitudinalStoreHarness,
        pipeline: GovernedBiographerPipeline,
        id: String,
        text: String,
        plan: BiographerQuestionPlan = openStoryPlan(),
        origin: BiographerAnswerOrigin = BiographerAnswerOrigin.TYPED,
        privacy: BiographerPrivacy = BiographerPrivacy.ELIGIBLE,
        key: String = "key-$id",
    ) = pipeline.captureAnswer(
        command(harness, id, text, plan, origin, privacy, key),
        com.conundrum.thomas.v2.biographer.BiographerInvestigationHistory(),
    )

    fun candidate(
        id: String,
        kind: InvestigationTargetKind = InvestigationTargetKind.EVENT_DETAIL,
        status: CoverageStatus = CoverageStatus.SPARSE,
        token: String = "basis-v1",
    ) = CoverageCandidate(
        InvestigationTargetId.parse(id),
        kind,
        listOf("synthetic-grounding"),
        reasonCode = "SYNTHETIC_STRUCTURAL_GAP",
        status = status,
        materialChangeToken = token,
    )
}
