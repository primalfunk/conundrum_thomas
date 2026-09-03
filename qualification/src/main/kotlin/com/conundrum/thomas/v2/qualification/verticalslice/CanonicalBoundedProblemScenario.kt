package com.conundrum.thomas.v2.qualification.verticalslice

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyEvaluator
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemPolicyState
import com.conundrum.thomas.v2.engine.verticalslice.BoundedProblemScope
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.engine.verticalslice.PolicyDecision
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.PolicyRenderRequestFactory
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.ProblemInfluence
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import com.conundrum.thomas.v2.engine.verticalslice.SupportIntent
import com.conundrum.thomas.v2.engine.verticalslice.UpstreamSafetyDisposition
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate

data class CanonicalScenarioTurn(
    val state: BoundedProblemPolicyState,
    val decision: PolicyDecision,
    val renderRequest: RenderRequest,
    val rendered: QualificationRenderResult,
)

/** Synthetic content only. No real person's psychological information is represented here. */
object CanonicalBoundedProblemScenario {
    private const val problem = "two recurring calendar conflicts are disrupting a fictional weekly study group"
    private val options = listOf("move the group by thirty minutes", "alternate the meeting day each week")
    private const val selected = "move the group by thirty minutes"
    private const val plan = "ask the fictional group on Thursday about moving the next meeting by thirty minutes"

    fun run(): List<CanonicalScenarioTurn> {
        val evaluator = BoundedProblemPolicyEvaluator()
        val renderer = DeterministicQualificationRenderer()
        return states().map { state ->
            val decision = QualificationSafetyGate.evaluateOrdinary(evaluator, state)
            val request = PolicyRenderRequestFactory.create(decision, state)
            CanonicalScenarioTurn(state, decision, request, renderer.renderNow(request))
        }
    }

    fun states(): List<BoundedProblemPolicyState> {
        val base = BoundedProblemPolicyState(
            stateId = "synthetic-turn-one",
            mode = ThomasMode.THERAPIST,
            upstreamSafetyDisposition = UpstreamSafetyDisposition.ORDINARY_SLICE_ALLOWED,
            scope = BoundedProblemScope.BOUNDED_NON_EMERGENCY_PERSONAL_PROBLEM,
            willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_EXPLORE, "synthetic-user-turn-1"),
        )
        val withIntent = base.copy(
            stateId = "synthetic-turn-two",
            supportIntent = PolicyEvidence.reported(SupportIntent.PRACTICAL_HELP, "synthetic-user-turn-2"),
        )
        val withProblem = withIntent.copy(
            stateId = "synthetic-turn-three",
            problemStatement = PolicyEvidence.reported(problem, "synthetic-user-turn-3"),
            problemClarity = PolicyEvidence.reported(ProblemClarity.BOUNDED, "synthetic-derived-turn-3"),
            sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "synthetic-thomas-summary-3"),
        )
        val confirmed = withProblem.copy(
            stateId = "synthetic-turn-four",
            sharedUnderstanding = PolicyEvidence.reported(SharedUnderstanding.CONFIRMED, "synthetic-user-turn-4"),
            willingness = PolicyEvidence.reported(ParticipationWillingness.WILLING_TO_ACT, "synthetic-user-turn-4"),
        )
        val influenceable = confirmed.copy(
            stateId = "synthetic-turn-five",
            problemInfluence = PolicyEvidence.reported(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE, "synthetic-user-turn-5"),
        )
        val withOptions = influenceable.copy(
            stateId = "synthetic-turn-six",
            generatedOptions = PolicyEvidence.reported(options, "synthetic-user-turn-6"),
        )
        val withSelection = withOptions.copy(
            stateId = "synthetic-turn-seven",
            selectedOption = PolicyEvidence.reported(selected, "synthetic-user-turn-7"),
        )
        val withPlan = withSelection.copy(
            stateId = "synthetic-turn-eight",
            actionPlan = PolicyEvidence.reported(plan, "synthetic-user-turn-8"),
        )
        val withOutcome = withPlan.copy(
            stateId = "synthetic-turn-nine",
            planOutcome = PolicyEvidence.reported(PlanOutcome.ATTEMPTED, "synthetic-user-turn-9"),
        )
        return listOf(base, withIntent, withProblem, confirmed, influenceable, withOptions, withSelection, withPlan, withOutcome)
    }
}
