package com.conundrum.thomas.v2.biographer

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Structural coverage selection only. It has no therapeutic, diagnostic, renderer, persistence,
 * or language-classification dependency.
 */
class DeterministicBiographerCoverageEngine {
    fun decide(
        evidence: CoverageEvidence,
        history: BiographerInvestigationHistory = BiographerInvestigationHistory(),
        request: CoverageRequest,
    ): BiographerQuestionDecision {
        val targets = buildTargets(evidence, history, request)
        if (request.investigationAuthority == BiographerInvestigationAuthority.BLOCKED_BY_SAFETY_SCOPE) {
            val map = coverageMap(evidence, history, targets, null)
            return BiographerQuestionDecision(
                BiographerQuestionDisposition.NO_TARGET,
                null,
                map,
                history,
            )
        }
        if (request.posture == BiographerPosture.OPEN_STORY) {
            val map = coverageMap(evidence, history, targets, null)
            return BiographerQuestionDecision(
                BiographerQuestionDisposition.AUTHORIZED,
                BiographerQuestionPlan(
                    BiographerPosture.OPEN_STORY,
                    null,
                    InvestigationTargetKind.OPEN_STORY,
                    emptyList(),
                    emptyList(),
                    listOf("DO_NOT_PRESUPPOSE_PERIOD_TOPIC_OR_PSYCHOLOGICAL_MEANING"),
                    BiographerQuestionSemanticAct.OPEN_HISTORICAL_INVITATION,
                    reasonCode = "USER_SELECTED_OPEN_STORY",
                ),
                map,
                history,
            )
        }

        val selected = targets.filter { it.eligibility == TargetEligibility.ELIGIBLE }
            .minWithOrNull(compareBy<InvestigationTarget>({ priority(it.kind) }, { it.id.value }))
        val map = coverageMap(evidence, history, targets, selected)
        if (selected == null) {
            return BiographerQuestionDecision(
                BiographerQuestionDisposition.NO_TARGET,
                null,
                map,
                history,
            )
        }
        val plan = questionPlan(selected)
        return BiographerQuestionDecision(
            BiographerQuestionDisposition.AUTHORIZED,
            plan,
            map,
            history.recordOffer(selected, evidence.storeRevision),
        )
    }

    private fun buildTargets(
        evidence: CoverageEvidence,
        history: BiographerInvestigationHistory,
        request: CoverageRequest,
    ): List<InvestigationTarget> {
        val candidates = evidence.candidates.toMutableList()
        request.userNamedTarget?.let { named ->
            val existingIndex = candidates.indexOfFirst { it.id == named.id }
            val userDirected = if (existingIndex >= 0) {
                val existing = candidates[existingIndex]
                existing.copy(
                    kind = InvestigationTargetKind.USER_NAMED_TOPIC,
                    reasonCode = "EXPLICIT_CURRENT_USER_TARGET:" + named.topicConcept,
                    materialChangeToken = existing.materialChangeToken + "|user:" + named.topicConcept,
                )
            } else {
                CoverageCandidate(
                    named.id,
                    InvestigationTargetKind.USER_NAMED_TOPIC,
                    named.groundingIds,
                    reasonCode = "EXPLICIT_CURRENT_USER_TARGET:" + named.topicConcept,
                    status = CoverageStatus.UNRESOLVED,
                    safeFacts = emptyList(),
                    materialChangeToken = "user:" + named.topicConcept,
                )
            }
            if (existingIndex >= 0) candidates[existingIndex] = userDirected else candidates += userDirected
        }
        return candidates.distinctBy { it.id }.map { candidate ->
            val prior = history.entries[candidate.id]
            val reopened = request.explicitlyReopenedTargetId == candidate.id ||
                request.userNamedTarget?.explicitlyReopens == candidate.id ||
                request.userNamedTarget?.id == candidate.id
            InvestigationTarget(
                candidate.id,
                candidate.kind,
                candidate.groundingIds.distinct().sorted(),
                candidate.relevantEntityIds.distinct().sorted(),
                candidate.temporalBounds,
                candidate.uncertainty,
                candidate.reasonCode,
                eligibility(candidate, prior, reopened),
                prior,
                candidate.safeFacts,
                candidate.materialChangeToken,
            )
        }.sortedBy { it.id }
    }

    private fun eligibility(
        candidate: CoverageCandidate,
        prior: InvestigationHistoryEntry?,
        explicitlyReopened: Boolean,
    ): TargetEligibility {
        if (!candidate.answerable) return TargetEligibility.UNANSWERABLE
        if (candidate.status == CoverageStatus.PRIVATE) return TargetEligibility.PRIVATE
        if (candidate.status == CoverageStatus.DECLINED && !explicitlyReopened) return TargetEligibility.DECLINED
        if (candidate.status == CoverageStatus.COVERED_ENOUGH_FOR_CURRENT_PURPOSE) return TargetEligibility.COVERED
        if (prior == null || explicitlyReopened) return TargetEligibility.ELIGIBLE
        if (prior.materialChangeToken != candidate.materialChangeToken) return TargetEligibility.ELIGIBLE
        return when (prior.answerDisposition) {
            InvestigationAnswerDisposition.DECLINED -> TargetEligibility.DECLINED
            InvestigationAnswerDisposition.MARKED_PRIVATE -> TargetEligibility.PRIVATE
            InvestigationAnswerDisposition.DEFERRED -> TargetEligibility.DEFERRED
            InvestigationAnswerDisposition.ANSWERED_RELEVANT -> TargetEligibility.COVERED
            InvestigationAnswerDisposition.OFFERED,
            InvestigationAnswerDisposition.ANSWERED_OTHER_EVIDENCE,
            InvestigationAnswerDisposition.ANSWERED_AMBIGUOUS,
            InvestigationAnswerDisposition.NO_EXTRACTABLE_EVIDENCE,
            InvestigationAnswerDisposition.SKIPPED,
            InvestigationAnswerDisposition.CHANGED_TOPIC,
            InvestigationAnswerDisposition.STOPPED,
            -> TargetEligibility.RECENTLY_ASKED
        }
    }

    private fun priority(kind: InvestigationTargetKind): Int = when (kind) {
        InvestigationTargetKind.USER_NAMED_TOPIC -> 1
        InvestigationTargetKind.CORRECTION_TARGET_UNRESOLVED -> 2
        InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED -> 3
        InvestigationTargetKind.EXISTING_OPEN_EVIDENTIARY_QUESTION -> 4
        InvestigationTargetKind.CONTRADICTION_CLARIFICATION -> 5
        InvestigationTargetKind.EVENT_TIME_UNRESOLVED -> 6
        InvestigationTargetKind.TEMPORAL_GAP -> 7
        InvestigationTargetKind.PERIOD_DETAIL -> 8
        InvestigationTargetKind.ROLE_GAP -> 9
        InvestigationTargetKind.PLACE_GAP -> 10
        InvestigationTargetKind.RELATIONSHIP_CONTEXT -> 11
        InvestigationTargetKind.EVENT_DETAIL -> 12
        InvestigationTargetKind.OPEN_STORY -> 13
    }

    private fun questionPlan(target: InvestigationTarget): BiographerQuestionPlan {
        val act = when (target.kind) {
            InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED -> BiographerQuestionSemanticAct.CLARIFY_IDENTITY
            InvestigationTargetKind.CONTRADICTION_CLARIFICATION -> BiographerQuestionSemanticAct.CLARIFY_CONTRADICTION
            InvestigationTargetKind.CORRECTION_TARGET_UNRESOLVED -> BiographerQuestionSemanticAct.CLARIFY_CORRECTION_TARGET
            InvestigationTargetKind.USER_NAMED_TOPIC -> BiographerQuestionSemanticAct.EXPLORE_USER_NAMED_TOPIC
            else -> BiographerQuestionSemanticAct.EXPLORE_STRUCTURAL_GAP
        }
        val constraints = buildList {
            if (target.uncertainty) add("PRESERVE_SOURCE_UNCERTAINTY")
            if (target.temporalBounds.isNotEmpty()) add("DO_NOT_INCREASE_TEMPORAL_PRECISION")
            add("DO_NOT_ASSUME_AN_UNSTATED_EVENT_OR_CAUSE")
        }
        return BiographerQuestionPlan(
            BiographerPosture.TARGETED_COVERAGE,
            target.id,
            target.kind,
            target.groundingIds,
            target.safeFacts,
            constraints,
            act,
            reasonCode = target.reasonCode,
        )
    }

    private fun coverageMap(
        evidence: CoverageEvidence,
        history: BiographerInvestigationHistory,
        targets: List<InvestigationTarget>,
        selected: InvestigationTarget?,
    ): BiographerCoverageMap {
        fun idsFor(vararg kinds: InvestigationTargetKind) =
            targets.filter { it.kind in kinds }.map { it.id }.sorted()
        val canonical = buildString {
            append(evidence.storeRevision).append('|').append(evidence.derivationVersion).append('\n')
            evidence.representedPeriods.sortedBy { it.id }.forEach {
                append("period=").append(it.id).append('|').append(it.status).append('|').append(it.time).append('\n')
            }
            targets.forEach {
                append("target=").append(it.id.value).append('|').append(it.kind).append('|')
                    .append(it.eligibility).append('|').append(it.materialChangeToken).append('\n')
            }
            history.entries.toSortedMap().forEach { (id, entry) ->
                append("history=").append(id.value).append('|').append(entry.offerCount).append('|')
                    .append(entry.answerDisposition).append('|').append(entry.materialChangeToken).append('\n')
            }
            append("selected=").append(selected?.id?.value.orEmpty())
        }
        return BiographerCoverageMap(
            evidence.storeRevision,
            evidence.representedPeriods.sortedBy { it.id },
            evidence.representedPeriods.filter { it.status == CoverageStatus.SPARSE }.map { it.id }.sorted(),
            idsFor(InvestigationTargetKind.TEMPORAL_GAP, InvestigationTargetKind.EVENT_TIME_UNRESOLVED),
            evidence.representedRoleIds.distinct().sorted(),
            evidence.representedPlaceIds.distinct().sorted(),
            evidence.representedRelationshipIds.distinct().sorted(),
            idsFor(InvestigationTargetKind.ENTITY_IDENTITY_UNRESOLVED),
            idsFor(InvestigationTargetKind.CONTRADICTION_CLARIFICATION),
            idsFor(InvestigationTargetKind.CORRECTION_TARGET_UNRESOLVED),
            idsFor(InvestigationTargetKind.EXISTING_OPEN_EVIDENTIARY_QUESTION),
            targets.filter { it.eligibility == TargetEligibility.DEFERRED }.map { it.id }.sorted(),
            targets.filter { it.eligibility == TargetEligibility.DECLINED }.map { it.id }.sorted(),
            targets.filter { it.eligibility == TargetEligibility.PRIVATE }.map { it.id }.sorted(),
            targets.filter { it.priorInvestigation != null }.map { it.id }.sorted(),
            targets,
            targets.filter { it.eligibility == TargetEligibility.ELIGIBLE },
            selected,
            evidence.derivationVersion,
            "ct-v2-10.target-ranking.v1",
            sha256(canonical),
        )
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

/** Renderer can only render an existing plan; it cannot choose target or eligibility. */
class BiographerQuestionExecutor {
    fun execute(
        decision: BiographerQuestionDecision,
        renderer: BiographerQuestionRenderer,
    ): BiographerQuestionRenderResult {
        val plan = decision.plan ?: return BiographerQuestionRenderResult(
            BiographerQuestionRenderDisposition.NO_PLAN,
            reasonCode = "NO_AUTHORIZED_QUESTION_PLAN",
        )
        return try {
            val rendered = renderer.render(plan)
            require(rendered.isNotBlank())
            require(rendered.count { it == '?' } <= plan.maximumQuestionCount)
            BiographerQuestionRenderResult(
                BiographerQuestionRenderDisposition.RENDERED,
                rendered,
                "AUTHORIZED_ONE_QUESTION_PLAN_RENDERED",
            )
        } catch (_: RuntimeException) {
            BiographerQuestionRenderResult(
                BiographerQuestionRenderDisposition.FAILED_WITHOUT_SOURCE_MUTATION,
                reasonCode = "QUESTION_RENDER_FAILURE_HAS_NO_EVIDENCE_AUTHORITY",
            )
        }
    }
}
