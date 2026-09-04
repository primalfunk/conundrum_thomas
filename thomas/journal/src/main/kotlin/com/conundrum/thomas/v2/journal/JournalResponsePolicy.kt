package com.conundrum.thomas.v2.journal

import com.conundrum.thomas.v2.languageevidence.perception.LanguagePerceptionResult
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass

private val journalProhibitions = JournalProhibitedResponseAct.entries.toSet()

/** Current-entry-only intent selection. It does not render, retrieve, investigate, or perform therapy. */
class DeterministicJournalResponseIntentPlanner : JournalResponseIntentPlanner {
    override fun plan(
        preference: JournalResponsePreference,
        perception: LanguagePerceptionResult,
    ): JournalResponsePlanningOutcome {
        require(preference != JournalResponsePreference.NO_RESPONSE) {
            "NO_RESPONSE must bypass response planning entirely"
        }
        val assertion = perception.proposals.firstOrNull()?.assertion
            ?: perception.correctionCandidate?.correctingAssertion
            ?: return JournalResponsePlanningOutcome(JournalResponseIntentDisposition.NO_SAFE_GROUNDED_RESPONSE)
        val grounding = JournalResponseGrounding(
            assertion.sourceRecordId,
            assertion.id,
            assertion.predicate.conceptId,
            assertion.epistemicClass,
            assertion.uncertainty,
            assertion.polarity,
        )
        val preserveAttribution = assertion.epistemicClass in setOf(
            EvidenceEpistemicClass.SELF_BELIEF,
            EvidenceEpistemicClass.USER_INTERPRETATION,
            EvidenceEpistemicClass.THIRD_PARTY_REPORT,
        )
        val preserveUncertainty = assertion.uncertainty != AssertionUncertainty.STATED_WITHOUT_QUALIFICATION
        return when (preference) {
            JournalResponsePreference.NO_RESPONSE -> error("NO_RESPONSE must bypass planner")
            JournalResponsePreference.REFLECT -> JournalResponsePlanningOutcome(
                JournalResponseIntentDisposition.REFLECTION_AUTHORIZED,
                JournalResponsePlan(
                    preference,
                    true,
                    JournalResponseSemanticAct.BRIEF_REFLECTION,
                    grounding,
                    0,
                    preserveAttribution,
                    preserveUncertainty,
                    journalProhibitions,
                    "CURRENT_ENTRY_GROUNDED_BRIEF_REFLECTION",
                ),
            )
            JournalResponsePreference.ASK_ONE_QUESTION -> JournalResponsePlanningOutcome(
                JournalResponseIntentDisposition.QUESTION_AUTHORIZED,
                JournalResponsePlan(
                    preference,
                    true,
                    JournalResponseSemanticAct.ONE_GROUNDED_QUESTION,
                    grounding,
                    1,
                    preserveAttribution,
                    preserveUncertainty,
                    journalProhibitions,
                    "CURRENT_ENTRY_LOCAL_OPTIONAL_QUESTION",
                ),
            )
        }
    }
}

/** Rendering is downstream of capture; failure cannot mutate or revoke a receipt. */
class JournalResponseExecutor {
    fun execute(result: JournalCaptureResult, renderer: JournalResponseRenderer): JournalResponseExecutionResult {
        val plan = result.responsePlan
            ?: return JournalResponseExecutionResult(
                JournalResponseExecutionDisposition.NOT_REQUESTED,
                reasonCode = "NO_AUTHORIZED_RESPONSE_PLAN",
            )
        return try {
            val rendered = renderer.render(plan)
            require(rendered.isNotBlank())
            require(plan.maximumQuestionCount == 1 || rendered.count { it == '?' } == 0)
            require(plan.maximumQuestionCount != 1 || rendered.count { it == '?' } <= 1)
            JournalResponseExecutionResult(
                JournalResponseExecutionDisposition.RENDERED,
                rendered,
                "AUTHORIZED_PLAN_RENDERED",
            )
        } catch (_: RuntimeException) {
            JournalResponseExecutionResult(
                JournalResponseExecutionDisposition.RENDERING_FAILED_AFTER_CAPTURE,
                reasonCode = "RENDERER_FAILURE_DID_NOT_AFFECT_CAPTURE",
            )
        }
    }
}
