package com.conundrum.thomas.v2.languagerenderer

import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.domain.rendering.RenderOutputDisposition
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryReference
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemorySemanticAct
import com.conundrum.thomas.v2.therapylongitudinal.TherapyRenderSupportEnvelope

object TherapyRenderCommandAdapter {
    fun adapt(
        commandId: RenderCommandId,
        turnIndex: Int,
        envelope: TherapyRenderSupportEnvelope,
    ): GovernedRenderCommand {
        val upstream = envelope.command
        if (upstream.outputDisposition == RenderOutputDisposition.NO_RESPONSE) {
            return silenceCommand(commandId, turnIndex, GovernedRenderMode.THERAPY)
        }
        val support = envelope.policySupportingText.associate { it.reference to it.text }
        val base = therapyBase(upstream, support)
        val currentData = AuthorizedSemanticUnit(
            "current-user-content", SemanticUnitKind.CURRENT_EVENT, envelope.currentUserText,
            RenderEpistemicStatus.USER_ASSERTED, RenderAttribution.CURRENT_USER,
            SemanticAuthorityLabel.CURRENT_USER_CONTENT_DATA,
            allowedUses = setOf(SemanticUnitUse.REFLECTION),
        )
        val policyUnit = AuthorizedSemanticUnit(
            "therapy-policy-act",
            if (upstream.form == RenderForm.PLANNING) SemanticUnitKind.AUTHORIZED_THERAPEUTIC_ACTION
            else SemanticUnitKind.AUTHORIZED_REFLECTION_TARGET,
            base, RenderEpistemicStatus.STRUCTURAL, RenderAttribution.GOVERNED_POLICY,
            SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING,
            allowedUses = setOf(if (upstream.form == RenderForm.PLANNING)
                SemanticUnitUse.ACTION_GROUNDING else SemanticUnitUse.REFLECTION),
        )
        val memories = envelope.surfacedMemorySupport.mapIndexed(::memoryUnit)
        val units = listOf(currentData, policyUnit) + memories.map { it.first }
        val memorySupport = memories.map { it.second }
        val act = therapyAct(upstream, envelope.surfacedMemorySupport)
        val memoryPrefix = memorySupport.joinToString(" ") { reference ->
            val unit = units.single { it.id == reference.semanticUnitId }
            memorySentence(reference, unit.surfaceMeaning) +
                if (unit.temporalScope is com.conundrum.thomas.v2.longitudinal.EventTime.Unknown)
                    " I am not sure of the event time." else ""
        }
        val complete = listOf(memoryPrefix, base).filter(String::isNotBlank).joinToString(" ")
        val variants = if (memoryPrefix.isBlank()) therapyVariants(base, upstream, support) else listOf(complete)
        val epistemicConstraints = memorySupport.filter { it.relationTentative }.map {
            RenderEpistemicConstraint(it.semanticUnitId, setOf("may", "might", "not sure", "wonder"), true)
        }
        val maxQuestions = when (act) {
            GovernedSemanticAct.EVIDENCE_EXPLANATION -> minOf(2, upstream.maximumQuestions)
            else -> upstream.maximumQuestions
        }
        return GovernedRenderCommand(
            commandId, turnIndex, GovernedRenderMode.THERAPY, act,
            responsePosture = GovernedResponsePosture.THERAPY, semanticUnits = units,
            historicalSupport = memorySupport, epistemicConstraints = epistemicConstraints,
            temporalConstraints = memories.flatMap { temporalConstraints(it.first) },
            budget = RenderBudget(if (act == GovernedSemanticAct.EVIDENCE_EXPLANATION) 900 else 640,
                if (act == GovernedSemanticAct.EVIDENCE_EXPLANATION) 6 else 4, maxQuestions),
            style = RenderStyleContract(questionStyle = if (maxQuestions == 0)
                RenderQuestionStyle.NONE else RenderQuestionStyle.OPTIONAL),
            directivenessLimit = if (upstream.advicePermitted) RenderDirectness.BALANCED else RenderDirectness.GENTLE,
            advicePermitted = upstream.advicePermitted,
            memoryReferencePermission = when {
                memorySupport.isEmpty() -> MemoryReferencePermission.NONE
                memorySupport.size == 1 -> MemoryReferencePermission.ONE_AUTHORIZED
                else -> MemoryReferencePermission.BOUNDED_EXPLICIT
            },
            allowedTemporalLiterals = units.flatMap { temporalLiterals(it.temporalScope) }.toSet() +
                Regex("\\b(?:18|19|20|21)\\d{2}\\b").findAll(complete).map { it.value }.toSet(),
            authorizedReferenceRealizations = variants,
            referenceCatalogOrder = memoryPrefix.isBlank() && upstream.selectedPolicyActionId in setOf(
                "core-verify-tentative-understanding", "core-verify-problem-understanding"),
            deterministicFallbackText = complete,
            fallbackAuthority = RenderFallbackAuthority.DETERMINISTIC_FALLBACK,
        )
    }

    private fun therapyAct(command: RenderCommand, memories: List<TherapyMemoryReference>): GovernedSemanticAct {
        val memory = memories.firstOrNull()
        if (memory != null) return when (memory.semanticAct) {
            TherapyMemorySemanticAct.DIRECT_RECALL -> GovernedSemanticAct.DIRECT_MEMORY_RECALL
            TherapyMemorySemanticAct.TENTATIVE_CONNECTION -> GovernedSemanticAct.TENTATIVE_MEMORY_CONNECTION
            TherapyMemorySemanticAct.USER_REQUESTED_COMPARISON -> GovernedSemanticAct.EXPLICIT_RECALL
            TherapyMemorySemanticAct.EVIDENCE_EXPLANATION -> GovernedSemanticAct.EVIDENCE_EXPLANATION
        }
        return when (command.form) {
            RenderForm.INTERROGATIVE -> GovernedSemanticAct.CLARIFYING_QUESTION
            RenderForm.PLANNING -> GovernedSemanticAct.AUTHORIZED_THERAPEUTIC_ACTION
            else -> GovernedSemanticAct.BRIEF_REFLECTION
        }
    }

    private fun memoryUnit(index: Int, memory: TherapyMemoryReference): Pair<AuthorizedSemanticUnit, AuthorizedHistoricalSupport> {
        val excerpt = memory.exactSourceExcerpt ?: "an earlier eligible account"
        val id = "memory-unit-${index + 1}"
        val attribution = when (memory.acquisitionMode) {
            AcquisitionMode.JOURNAL -> RenderAttribution.JOURNAL_SOURCE
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE ->
                RenderAttribution.BIOGRAPHER_SOURCE
            AcquisitionMode.THERAPIST_CONVERSATION -> RenderAttribution.THERAPIST_CONVERSATION_SOURCE
            AcquisitionMode.USER_CORRECTION, null -> RenderAttribution.CURRENT_USER
        }
        val unit = AuthorizedSemanticUnit(
            id,
            if (memory.semanticAct == TherapyMemorySemanticAct.TENTATIVE_CONNECTION)
                SemanticUnitKind.AUTHORIZED_TENTATIVE_CONNECTION else SemanticUnitKind.AUTHORIZED_MEMORY_REFERENCE,
            excerpt,
            if (memory.semanticAct == TherapyMemorySemanticAct.TENTATIVE_CONNECTION)
                RenderEpistemicStatus.THOMAS_TENTATIVE else RenderEpistemicStatus.USER_ASSERTED,
            attribution, SemanticAuthorityLabel.HISTORICAL_USER_SOURCE_DATA,
            memory.eventTime, memory.uncertainty,
            setOf(SemanticUnitUse.MEMORY_RECALL, SemanticUnitUse.EVIDENCE_EXPLANATION),
        )
        val markers = attributionMarkers(memory.acquisitionMode)
        return unit to AuthorizedHistoricalSupport(
            memory.stableObjectId, memory.sourceRevisionIds.map { it.value }, id,
            memory.acquisitionMode ?: AcquisitionMode.THERAPIST_CONVERSATION, markers,
            memory.semanticAct == TherapyMemorySemanticAct.TENTATIVE_CONNECTION,
            memory.contradictionPresent, memory.identityUnresolved,
        )
    }

    private fun attributionMarkers(mode: AcquisitionMode?): Set<String> = when (mode) {
        AcquisitionMode.JOURNAL -> setOf("journal", "wrote")
        AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE ->
            setOf("history", "filling in")
        AcquisitionMode.THERAPIST_CONVERSATION -> setOf("mentioned before", "earlier")
        AcquisitionMode.USER_CORRECTION -> setOf("corrected")
        null -> setOf("earlier")
    }

    private fun memorySentence(support: AuthorizedHistoricalSupport, excerpt: String): String {
        val attribution = when (support.acquisitionMode) {
            AcquisitionMode.JOURNAL -> "You wrote in your Journal"
            AcquisitionMode.BIOGRAPHER_OPEN_NARRATIVE, AcquisitionMode.BIOGRAPHER_GUIDED_TIMELINE ->
                "When we were filling in your history, you mentioned"
            AcquisitionMode.THERAPIST_CONVERSATION -> "You mentioned before"
            AcquisitionMode.USER_CORRECTION -> "You later corrected this to"
        }
        val quoted = "“${excerpt.replace("”", "'")}”"
        return when {
            support.identityUnresolved -> "$attribution an earlier account, but I am not sure which person it concerns."
            support.contradictionPresent -> "$attribution $quoted, but there are differing accounts and neither is settled."
            support.relationTentative -> "I'm not sure these are related, but $attribution $quoted."
            else -> "$attribution $quoted."
        }
    }

    private fun therapyBase(command: RenderCommand, support: Map<String, String>): String =
        when (command.selectedPolicyActionId) {
            "core-ask-support-preference" ->
                "Would you like me to listen, help you understand this, or help structure a practical next step?"
            "core-offer-direction-choice" -> "Would you rather continue, change direction, pause, or stop?"
            "core-acknowledge-close" -> "Okay. We can stop here."
            "core-invite-expression" -> "What would you like me to hear?"
            "core-reflect-established-content" -> "I hear that ${required(support, "established-concern")}."
            "core-invite-further-expression" -> "What else would you like to say about it?"
            "core-summarize-listening" -> "What I have heard is: ${required(support, "established-concern")}."
            "core-check-further-or-close" -> "Would you like to add anything, or stop here?"
            "core-ask-present-concern" -> "What part of this would you most like to understand?"
            "core-ask-important-missing-piece" ->
                "Could you clarify ${required(support, "important-missing-information")}?"
            "core-verify-tentative-understanding", "core-verify-problem-understanding" ->
                "I might be understanding this as ${required(support, "tentative-thomas-understanding")}. Is that right?"
            "core-acknowledge-correction" ->
                "I had that wrong. What would be a better way to understand it?"
            "core-summarize-shared-understanding" ->
                "What we have established is: ${required(support, "confirmed-understanding")}."
            "core-check-understanding-next-direction" ->
                "Is that understanding enough for now, or would you like a different direction?"
            "core-ask-problem-description" -> "What is the one practical problem you would like to work on?"
            "core-ask-influenceable-part" ->
                "Which part of ${required(support, "established-concern")} can you influence?"
            "core-ask-readiness-for-options" ->
                "Would you like to consider possible options, or leave it here?"
            "core-invite-user-options" -> "What possible ways forward come to mind for you?"
            "core-ask-user-to-choose-option" -> "Which of your options seems most helpful and feasible?"
            "core-develop-bounded-plan" ->
                "For ${required(support, "user-selected-option")}, what small first step would you take, and when?"
            "core-review-reported-outcome" ->
                "What happened with your plan, ${required(support, "action-plan")}?"
            "core-consolidate-plan-learning" ->
                "Your plan was ${required(support, "action-plan")}; you reported ${required(support, "plan-outcome")}. Would you like to stop or choose another direction?"
            else -> when (command.form) {
                RenderForm.INTERROGATIVE -> "What would you like to focus on here?"
                RenderForm.PLANNING -> "We can stay with the practical step already selected."
                else -> "I am following the concern you described."
            }
        }

    private fun required(support: Map<String, String>, key: String): String = requireNotNull(support[key]) {
        "Missing already-authorized Therapy support: $key"
    }

    // Finite realizations of the upstream ordinary act, never alternate procedural acts.
    // Seven distinct openings exceed the six-response recurrence window; each candidate
    // still receives the complete validator. Historical-memory composition is separate.
    private fun therapyVariants(base: String, command: RenderCommand, support: Map<String, String>): List<String> =
        when (command.selectedPolicyActionId) {
        "core-ask-support-preference" -> listOf(
            base,
            "Which would you prefer: listening, help understanding this, or help structuring a practical next step?",
            "What kind of support would you like: listening, understanding, or a practical next step?",
            "Is listening, understanding this, or structuring a practical next step what you would prefer?",
            "Do you prefer being heard, help understanding this, or help with a practical next step?",
            "For this conversation, would you choose listening, understanding, or a practical next step?",
            "How would you like me to help: by listening, helping you understand, or structuring a practical next step?",
        )
        "core-offer-direction-choice" -> listOf(
            base,
            "Which would you prefer: continue, change direction, pause, or stop?",
            "What is your choice now: continuing, changing direction, pausing, or stopping?",
            "Is continuing, changing direction, pausing, or stopping your preference?",
            "Do you want to continue, take another direction, pause, or stop?",
            "For our next step, would you choose continuing, changing direction, pausing, or stopping?",
            "How would you like to proceed: continue, change direction, pause, or stop?",
        )
        "core-acknowledge-close" -> listOf(
            base,
            "Understood. We can end here.",
            "We can leave it here.",
            "I acknowledge your choice to stop.",
            "This is where we can stop.",
            "Stopping here is okay.",
            "Okay, let us end here.",
        )
        "core-invite-expression" -> listOf(
            base,
            "What do you want me to hear?",
            "Which thoughts would you like to share?",
            "Is there something you would like me to hear?",
            "Where would you like to begin sharing?",
            "What is it you would like to say?",
            "How would you like to begin telling me?",
        )
        "core-reflect-established-content" -> listOf(
            base,
            "What stands out is that ${required(support, "established-concern")}.",
            "You have described that ${required(support, "established-concern")}.",
            "You said that ${required(support, "established-concern")}.",
            "I heard that ${required(support, "established-concern")}.",
            "Your concern is: ${required(support, "established-concern")}.",
            "Your account is that ${required(support, "established-concern")}.",
        )
        "core-invite-further-expression" -> listOf(
            base,
            "What more would you like to share about it?",
            "Is there anything else you would like to say about it?",
            "What would you like to add about it?",
            "Which further thoughts would you like to share about it?",
            "Do you want to say anything more about it?",
            "Where would you like to continue with what you were saying?",
        )
        "core-summarize-listening" -> listOf(
            base,
            "To summarize: ${required(support, "established-concern")}.",
            "The account I heard: ${required(support, "established-concern")}.",
            "You have told me: ${required(support, "established-concern")}.",
            "In your account: ${required(support, "established-concern")}.",
            "The content shared: ${required(support, "established-concern")}.",
            "From your account: ${required(support, "established-concern")}.",
        )
        "core-check-further-or-close" -> listOf(
            base,
            "Is there anything you want to add, or would you prefer to stop?",
            "What would you prefer: adding anything else or stopping here?",
            "Do you want to add more, or stop here?",
            "Which suits you: saying anything more or stopping here?",
            "Are you inclined to add anything, or leave it here?",
            "Shall we stop here, or is there anything you want to add?",
        )
        "core-ask-present-concern" -> listOf(
            base,
            "Which concern would you like help understanding?",
            "What is the concern you want to understand?",
            "Where would you like to focus our understanding?",
            "Is there a concern you would like to understand?",
            "What concern should we try to understand together?",
            "Which part would you like us to understand?",
        )
        "core-ask-important-missing-piece" -> listOf(
            base,
            "Can you clarify ${required(support, "important-missing-information")}?",
            "Would you clarify ${required(support, "important-missing-information")}?",
            "Will you clarify ${required(support, "important-missing-information")}?",
            "Might you clarify ${required(support, "important-missing-information")}?",
            "Please clarify ${required(support, "important-missing-information")}?",
            "Could you explain ${required(support, "important-missing-information")}?",
        )
        "core-verify-tentative-understanding", "core-verify-problem-understanding" -> listOf(
            base,
            "My tentative understanding is: ${required(support, "tentative-thomas-understanding")}. Is that right?",
            "Have I tentatively understood: ${required(support, "tentative-thomas-understanding")}?",
            "Could this be right: ${required(support, "tentative-thomas-understanding")}?",
            "Perhaps I understand: ${required(support, "tentative-thomas-understanding")}. Is that right?",
            "As I tentatively understand it: ${required(support, "tentative-thomas-understanding")}. Is that right?",
            "Does this tentative understanding fit: ${required(support, "tentative-thomas-understanding")}?",
        )
        "core-acknowledge-correction" -> listOf(
            base,
            "My interpretation was wrong. How would you correct it?",
            "That interpretation was mistaken. What is the corrected meaning?",
            "I withdraw that interpretation. How would you put it correctly?",
            "You corrected my mistaken understanding. What is the right way to understand it?",
            "The understanding I offered was wrong. How would you describe it instead?",
            "I got that wrong. What would be the correct understanding?",
        )
        "core-summarize-shared-understanding" -> listOf(
            base,
            "Our shared understanding: ${required(support, "confirmed-understanding")}.",
            "You confirmed: ${required(support, "confirmed-understanding")}.",
            "The confirmed account: ${required(support, "confirmed-understanding")}.",
            "To summarize: ${required(support, "confirmed-understanding")}.",
            "As you confirmed: ${required(support, "confirmed-understanding")}.",
            "The shared understanding: ${required(support, "confirmed-understanding")}.",
        )
        "core-check-understanding-next-direction" -> listOf(
            base,
            "Would you like to leave that understanding here, or choose another direction?",
            "What would you prefer: this understanding for now, or a different direction?",
            "Does that understanding suffice for now, or do you want another direction?",
            "Are you satisfied to leave that understanding here, or would you prefer a different direction?",
            "Which suits you: staying with that understanding for now, or choosing another direction?",
            "Shall we leave that understanding here, or would you like a different direction?",
        )
        "core-ask-problem-description" -> listOf(
            base,
            "Which single practical problem would you like to work on?",
            "What practical problem do you want to address here?",
            "Is there one practical problem you want to work on?",
            "Where would you focus on one practical problem?",
            "What is one practical problem you want to address?",
            "Could you describe the one practical problem you want to work on?",
        )
        "core-ask-influenceable-part" -> listOf(
            base,
            "What can you influence in ${required(support, "established-concern")}?",
            "Where can you influence ${required(support, "established-concern")}?",
            "Is ${required(support, "established-concern")} partly influenceable by you?",
            "Which aspect of ${required(support, "established-concern")} can you affect?",
            "In ${required(support, "established-concern")}, what can you influence?",
            "As to ${required(support, "established-concern")}, what can you influence?",
        )
        "core-ask-readiness-for-options" -> listOf(
            base,
            "Are you willing to consider possible options, or would you prefer to leave it here?",
            "Do you want to consider possible options, or stop here?",
            "Is considering options something you want now, or would you rather leave it here?",
            "What would you prefer: considering options or leaving it here?",
            "Shall we consider possible options, or would you rather stop here?",
            "Which suits you now: considering possible options or leaving it here?",
        )
        "core-invite-user-options" -> listOf(
            base,
            "Which possible options come to mind for you?",
            "What options do you see?",
            "Are there possible ways forward you have in mind?",
            "How might you proceed, in your own view?",
            "What possibilities occur to you?",
            "Which ways forward can you think of?",
        )
        "core-ask-user-to-choose-option" -> listOf(
            base,
            "Of your options, which seems most helpful and feasible to you?",
            "What is your choice among your options for helpfulness and feasibility?",
            "Do any of your options stand out as most helpful and feasible?",
            "Considering your options, which would you choose as most helpful and feasible?",
            "Which option you proposed seems most helpful and workable?",
            "From your options, which do you find most useful and feasible?",
        )
        "core-develop-bounded-plan" -> listOf(
            base,
            "What small first step for ${required(support, "user-selected-option")} would you take, and when?",
            "To start ${required(support, "user-selected-option")}, what small step would you take, and when?",
            "Which small first step for ${required(support, "user-selected-option")} would you take, and when?",
            "On ${required(support, "user-selected-option")}, what small first step would you take, and when?",
            "How would you begin ${required(support, "user-selected-option")} with a small step, and when?",
            "Taking ${required(support, "user-selected-option")} forward, what small first step, and when?",
        )
        "core-review-reported-outcome" -> listOf(
            base,
            "How did your plan, ${required(support, "action-plan")}, turn out?",
            "What resulted from your plan, ${required(support, "action-plan")}?",
            "With plan ${required(support, "action-plan")}, what happened?",
            "Regarding plan ${required(support, "action-plan")}, what happened?",
            "Did plan ${required(support, "action-plan")} have an outcome?",
            "As to plan ${required(support, "action-plan")}, what happened?",
        )
        "core-consolidate-plan-learning" -> listOf(
            base,
            "Your plan: ${required(support, "action-plan")}; you reported: ${required(support, "plan-outcome")}. Stop or choose another direction?",
            "The plan was ${required(support, "action-plan")}; you reported ${required(support, "plan-outcome")}. Stop or choose another direction?",
            "To summarize: plan ${required(support, "action-plan")}; reported outcome ${required(support, "plan-outcome")}. Stop or choose another direction?",
            "Your reported outcome: ${required(support, "plan-outcome")}; your plan: ${required(support, "action-plan")}. Stop or choose another direction?",
            "We have your plan, ${required(support, "action-plan")}, and reported outcome, ${required(support, "plan-outcome")}. Stop or choose another direction?",
            "From your account: plan ${required(support, "action-plan")}; outcome ${required(support, "plan-outcome")}. Stop or choose another direction?",
        )
        else -> when (command.form) {
            RenderForm.REFLECTIVE -> listOf(base, base.replaceFirst("I hear that", "What stands out is that"),
                base.replaceFirst("I hear that", "You have described that"))
            else -> listOf(base)
        }
    }.distinct()
}
