package com.conundrum.thomas.v2.qualification.ordinary

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.domain.rendering.RenderRequest
import com.conundrum.thomas.v2.engine.ordinary.CoreActionExecution
import com.conundrum.thomas.v2.engine.ordinary.CoreInformationRequirement
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyEvaluator
import com.conundrum.thomas.v2.engine.ordinary.CoreOrdinaryTherapyState
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDecision
import com.conundrum.thomas.v2.engine.ordinary.CorePolicyDisposition
import com.conundrum.thomas.v2.engine.ordinary.CoreRenderRequestFactory
import com.conundrum.thomas.v2.engine.ordinary.CorrectionStatus
import com.conundrum.thomas.v2.engine.ordinary.ExpressionProgress
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryEngagement
import com.conundrum.thomas.v2.engine.ordinary.OrdinaryRoute
import com.conundrum.thomas.v2.engine.ordinary.PlanReviewStatus
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.engine.verticalslice.ParticipationWillingness
import com.conundrum.thomas.v2.engine.verticalslice.PlanOutcome
import com.conundrum.thomas.v2.engine.verticalslice.PolicyActionId
import com.conundrum.thomas.v2.engine.verticalslice.PolicyEvidence
import com.conundrum.thomas.v2.engine.verticalslice.ProblemClarity
import com.conundrum.thomas.v2.engine.verticalslice.ProblemInfluence
import com.conundrum.thomas.v2.engine.verticalslice.SharedUnderstanding
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyEvidence
import com.conundrum.thomas.v2.safety.SafetyEvidenceOrigin
import com.conundrum.thomas.v2.safety.SafetyEvidenceRevision
import com.conundrum.thomas.v2.safety.SafetyPresence
import com.conundrum.thomas.v2.safety.SafetyScopeGate

data class CoreConversationTurn(
    val state: CoreOrdinaryTherapyState,
    val decision: CorePolicyDecision,
    val renderRequest: RenderRequest?,
    val rendered: CoreQualificationRenderResult?,
)

data class SafetyRevocationFixture(
    val initialDecision: CorePolicyDecision,
    val revisedGateAuthority: SafetyAuthorityState,
    val stalePermitAttempt: CorePolicyDecision,
)

/** Entirely synthetic, deterministic qualification conversations. */
object CanonicalCoreConversations {
    private val evaluator = CoreOrdinaryTherapyEvaluator()
    private val renderer = DeterministicCoreOrdinaryRenderer()

    fun listening(): List<CoreConversationTurn> {
        val s1 = base("listen-one", RequestedOrdinarySupport.LISTEN).copy(
            expressionProgress = reported(ExpressionProgress.NO_CONTENT_YET, "listen-user-1"),
        )
        val s2 = advance(s1, "listen-two", OrdinaryRoute.LISTEN_SUPPORT, "core-invite-expression") {
            copy(
                concernStatement = reported("a fictional colleague interrupted the user during a presentation", "listen-user-2"),
                problemClarity = reported(ProblemClarity.BOUNDED, "listen-structure-2"),
                expressionProgress = reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, "listen-user-2"),
            )
        }
        val s3 = advance(s2, "listen-three", OrdinaryRoute.LISTEN_SUPPORT, "core-reflect-established-content") {
            copy(expressionProgress = reported(ExpressionProgress.USER_WANTS_TO_CONTINUE, "listen-user-3"))
        }
        val s4 = advance(s3, "listen-four", OrdinaryRoute.LISTEN_SUPPORT, "core-invite-further-expression") {
            copy(expressionProgress = reported(ExpressionProgress.EXPRESSION_COMPLETE, "listen-user-4"))
        }
        val s5 = advance(s4, "listen-five", OrdinaryRoute.LISTEN_SUPPORT, "core-summarize-listening") {
            copy(expressionProgress = reported(ExpressionProgress.SUMMARY_DELIVERED, "listen-process-5"))
        }
        val s6 = advance(s5, "listen-six", OrdinaryRoute.LISTEN_SUPPORT, "core-check-further-or-close") {
            copy(engagement = reported(OrdinaryEngagement.CLOSE_REQUESTED, "listen-user-6"))
        }
        return run(listOf(s1, s2, s3, s4, s5, s6))
    }

    fun understanding(): List<CoreConversationTurn> {
        val concern = "a fictional invitation was omitted from a group message"
        val s1 = base("understand-one", RequestedOrdinarySupport.UNDERSTAND).copy(
            concernStatement = reported(concern, "understand-user-1"),
            problemClarity = reported(ProblemClarity.BOUNDED, "understand-structure-1"),
            importantMissingInformation = reported("what the invitation meant to the user", "understand-gap-1"),
        )
        val s2 = advance(s1, "understand-two", OrdinaryRoute.UNDERSTAND_CLARIFY, "core-ask-important-missing-piece") {
            copy(
                importantMissingInformation = PolicyEvidence.unknown(),
                thomasUnderstanding = PolicyEvidence.tentative("the omission may have felt excluding", "understand-thomas-2"),
                sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "understand-thomas-2"),
            )
        }
        val s3 = advance(s2, "understand-three", OrdinaryRoute.UNDERSTAND_CLARIFY, "core-verify-tentative-understanding") {
            copy(
                thomasUnderstanding = reported("the omission felt excluding to the fictional user", "understand-user-3"),
                sharedUnderstanding = reported(SharedUnderstanding.CONFIRMED, "understand-user-3"),
                understandingSummaryDelivered = reported(false, "understand-process-3"),
            )
        }
        val s4 = advance(s3, "understand-four", OrdinaryRoute.UNDERSTAND_CLARIFY, "core-summarize-shared-understanding") {
            copy(understandingSummaryDelivered = reported(true, "understand-process-4"))
        }
        return run(listOf(s1, s2, s3, s4))
    }

    fun practicalProblemSolving(): List<CoreConversationTurn> {
        val problem = "two fictional recurring calendar commitments overlap"
        val options = listOf("move the first commitment", "alternate the second commitment each week")
        val s1 = base("problem-one", RequestedOrdinarySupport.PRACTICAL_HELP)
        val s2 = advance(s1, "problem-two", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-ask-problem-description") {
            copy(
                concernStatement = reported(problem, "problem-user-2"),
                problemClarity = reported(ProblemClarity.BOUNDED, "problem-structure-2"),
                thomasUnderstanding = PolicyEvidence.tentative("the overlap is the single practical problem", "problem-thomas-2"),
                sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "problem-thomas-2"),
            )
        }
        val s3 = advance(s2, "problem-three", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-verify-problem-understanding") {
            copy(
                thomasUnderstanding = reported("the overlap is the single practical problem", "problem-user-3"),
                sharedUnderstanding = reported(SharedUnderstanding.CONFIRMED, "problem-user-3"),
            )
        }
        val s4 = advance(s3, "problem-four", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-ask-influenceable-part") {
            copy(problemInfluence = reported(ProblemInfluence.AT_LEAST_PARTLY_INFLUENCEABLE, "problem-user-4"))
        }
        val s5 = advance(s4, "problem-five", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-ask-readiness-for-options") {
            copy(willingness = reported(ParticipationWillingness.WILLING_TO_ACT, "problem-user-5"))
        }
        val s6 = advance(s5, "problem-six", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-invite-user-options") {
            copy(generatedOptions = reported(options, "problem-user-6"))
        }
        val s7 = advance(s6, "problem-seven", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-ask-user-to-choose-option") {
            copy(selectedOption = reported(options.first(), "problem-user-7"))
        }
        val s8 = advance(s7, "problem-eight", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-develop-bounded-plan") {
            copy(actionPlan = reported("ask the fictional organizer on Thursday to move the first commitment", "problem-user-8"))
        }
        val s9 = advance(s8, "problem-nine", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-wait-for-outcome") {
            copy(
                planOutcome = reported(PlanOutcome.ATTEMPTED, "problem-user-9"),
                planReviewStatus = reported(PlanReviewStatus.NOT_REVIEWED, "problem-process-9"),
            )
        }
        val s10 = advance(s9, "problem-ten", OrdinaryRoute.PRACTICAL_PROBLEM_SOLVING, "core-review-reported-outcome") {
            copy(planReviewStatus = reported(PlanReviewStatus.REVIEWED, "problem-user-10"))
        }
        return run(listOf(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10))
    }

    fun preferenceChange(): List<CoreConversationTurn> {
        val s1 = base("switch-one", RequestedOrdinarySupport.LISTEN).copy(
            expressionProgress = reported(ExpressionProgress.NO_CONTENT_YET, "switch-user-1"),
        )
        val s2 = advance(s1, "switch-two", OrdinaryRoute.LISTEN_SUPPORT, "core-invite-expression") {
            copy(
                routePreference = reported(RequestedOrdinarySupport.UNDERSTAND, "switch-user-2"),
                concernStatement = reported("a fictional brief exchange still feels important", "switch-user-2"),
                problemClarity = reported(ProblemClarity.BOUNDED, "switch-structure-2"),
                importantMissingInformation = reported("which part of the exchange remains unclear", "switch-gap-2"),
            )
        }
        return run(listOf(s1, s2))
    }

    fun correction(): List<CoreConversationTurn> {
        val s1 = base("correction-one", RequestedOrdinarySupport.UNDERSTAND).copy(
            concernStatement = reported("a fictional teammate changed a plan", "correction-user-1"),
            problemClarity = reported(ProblemClarity.BOUNDED, "correction-structure-1"),
            thomasUnderstanding = PolicyEvidence.tentative("the change may have felt rejecting", "correction-thomas-1"),
            sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "correction-thomas-1"),
        )
        val s2 = advance(s1, "correction-two", OrdinaryRoute.UNDERSTAND_CLARIFY, "core-verify-tentative-understanding") {
            copy(
                thomasUnderstanding = PolicyEvidence.unknown(),
                sharedUnderstanding = PolicyEvidence.unknown(),
                correctionStatus = reported(CorrectionStatus.UNHANDLED, "correction-user-2"),
                userCorrection = reported("the issue was unpredictability, not rejection", "correction-user-2"),
                withdrawnInterpretationReferences = setOf("correction-thomas-1"),
            )
        }
        val s3 = advance(s2, "correction-three", OrdinaryRoute.UNDERSTAND_CLARIFY, "core-acknowledge-correction") {
            copy(
                correctionStatus = reported(CorrectionStatus.ACKNOWLEDGED, "correction-process-3"),
                thomasUnderstanding = PolicyEvidence.tentative("the unpredictability may be what bothered the fictional user", "correction-thomas-3"),
                sharedUnderstanding = PolicyEvidence.tentative(SharedUnderstanding.TENTATIVE, "correction-thomas-3"),
            )
        }
        return run(listOf(s1, s2, s3))
    }

    fun stagnation(): List<CoreConversationTurn> {
        val s1 = base("stagnation-one", RequestedOrdinarySupport.LISTEN).copy(
            concernStatement = reported("a fictional meeting felt frustrating", "stagnation-user-1"),
            problemClarity = reported(ProblemClarity.BOUNDED, "stagnation-structure-1"),
            expressionProgress = reported(ExpressionProgress.NEW_CONTENT_AVAILABLE, "stagnation-user-1"),
        )
        val reflected = s1.copy(
            activeRoute = OrdinaryRoute.LISTEN_SUPPORT,
            actionHistory = listOf(CoreActionExecution(PolicyActionId.parse("core-reflect-established-content"), 1, OrdinaryRoute.LISTEN_SUPPORT)),
        )
        val directionOffered = reflected.copy(
            actionHistory = reflected.actionHistory + CoreActionExecution(PolicyActionId.parse("core-offer-direction-choice"), 1, OrdinaryRoute.LISTEN_SUPPORT),
        )
        return run(listOf(s1, reflected, directionOffered))
    }

    fun safetyRevocation(): SafetyRevocationFixture {
        val state = base("revocation-case", RequestedOrdinarySupport.LISTEN).copy(
            expressionProgress = reported(ExpressionProgress.NO_CONTENT_YET, "revocation-user-1"),
        )
        val gate = SafetyScopeGate()
        val firstGate = gate.govern(QualificationSafetyGate.ordinaryInput(state.stateId, SafetyEvidenceRevision.of(1)))
        val permit = requireNotNull(firstGate.ordinaryTherapyPermit)
        val initial = evaluator.evaluate(state, permit)
        val revisedInput = QualificationSafetyGate.ordinaryInput(state.stateId, SafetyEvidenceRevision.of(2)).copy(
            selfHarmRelevance = SafetyEvidence.established(
                SafetyPresence.PRESENT,
                SafetyEvidenceOrigin.DIRECT_USER_REPORT,
                "revocation-synthetic-self-harm-disclosure",
            ),
        )
        val revisedGate = gate.govern(revisedInput)
        val staleAttempt = evaluator.evaluate(state.copy(safetyEvidenceRevision = SafetyEvidenceRevision.of(2)), permit)
        return SafetyRevocationFixture(initial, revisedGate.authorityState, staleAttempt)
    }

    private fun run(states: List<CoreOrdinaryTherapyState>): List<CoreConversationTurn> = states.map { state ->
        val decision = QualificationSafetyGate.evaluateCoreOrdinary(evaluator, state)
        if (decision.disposition == CorePolicyDisposition.ACTION_SELECTED) {
            val request = CoreRenderRequestFactory.create(decision, state)
            CoreConversationTurn(state, decision, request, renderer.renderNow(request))
        } else CoreConversationTurn(state, decision, null, null)
    }

    private fun base(id: String, support: RequestedOrdinarySupport) = CoreOrdinaryTherapyState(
        stateId = id,
        safetyEvidenceRevision = SafetyEvidenceRevision.of(1),
        conversationRevision = 1,
        mode = ThomasMode.THERAPIST,
        routePreference = reported(support, "$id-support"),
        engagement = reported(OrdinaryEngagement.ENGAGED, "$id-engagement"),
        correctionStatus = reported(CorrectionStatus.NONE, "$id-correction"),
        understandingSummaryDelivered = reported(false, "$id-summary"),
    )

    private fun advance(
        state: CoreOrdinaryTherapyState,
        id: String,
        route: OrdinaryRoute,
        executedAction: String,
        update: CoreOrdinaryTherapyState.() -> CoreOrdinaryTherapyState,
    ): CoreOrdinaryTherapyState = state.copy(
        stateId = id,
        conversationRevision = state.conversationRevision + 1,
        activeRoute = route,
        pendingInformation = CoreInformationRequirement.NONE,
        actionHistory = state.actionHistory + CoreActionExecution(PolicyActionId.parse(executedAction), state.conversationRevision, route),
    ).update()

    private fun <T> reported(value: T, reference: String) = PolicyEvidence.reported(value, reference)
}
