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
        val variants = if (memoryPrefix.isBlank()) therapyVariants(base, upstream.form) else listOf(complete)
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

    private fun therapyVariants(base: String, form: RenderForm): List<String> = when (form) {
        RenderForm.REFLECTIVE -> listOf(
            base,
            base.replaceFirst("I hear that", "What stands out is that"),
            base.replaceFirst("I hear that", "You have described that"),
        )
        else -> listOf(base)
    }.distinct()
}
