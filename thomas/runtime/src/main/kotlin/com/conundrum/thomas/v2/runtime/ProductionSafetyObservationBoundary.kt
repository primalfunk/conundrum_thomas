package com.conundrum.thomas.v2.runtime

import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.safety.*

/** Current-session declarations, never a diagnosis, screening tool, or scan of stored sources.
 * Exact, whole-line statements only; quotation/historical prefixes do not match.
 * Session declarations expire when this runtime closes. Withdrawal resets a field to UNKNOWN.
 */
internal class ProductionSafetyObservationBoundary {
    private var previous: SafetyScopeInput? = null

    private var pending: SafetyInformationRequirement? = null
    fun delivered(requirement: SafetyInformationRequirement?) { pending = requirement }
    fun handlesReply(text: String): Boolean = pending != null &&
        ProductionTherapyInputBoundary.normalize(text) in setOf("yes", "no", "i don't know", "i do not know", "i decline to answer")

    private fun replyDeclaration(text: String): String? {
        if (!handlesReply(text)) return null
        val positive = ProductionTherapyInputBoundary.normalize(text) == "yes"
        if (pending!!.field == SafetyField.POPULATION_APPLICABILITY && !positive &&
            ProductionTherapyInputBoundary.normalize(text) != "i decline to answer")
            return "I withdraw: I am an adult in the supported setting"
        val statement = when (pending!!.field) {
            SafetyField.CURRENT_EMERGENCY -> if (positive) "This is a current emergency" else "There is no current emergency"
            SafetyField.ACUTE_MEDICAL_EMERGENCY -> if (positive) "There is an acute medical emergency" else "There is no acute medical emergency"
            SafetyField.SELF_HARM_RELEVANCE -> if (positive) "Self-harm is relevant now" else "Self-harm is not relevant now"
            SafetyField.HARM_TO_OTHERS_RELEVANCE -> if (positive) "Harm to others is relevant now" else "Harm to others is not relevant now"
            SafetyField.SPECIALIZED_SCOPE_CONDITION -> if (positive) "I report a specialized condition for this conversation" else "I report no specialized condition for this conversation"
            SafetyField.POPULATION_APPLICABILITY -> if (positive) "I am an adult in the supported setting" else "I am not an adult"
            SafetyField.PRESENTING_SCOPE -> if (positive) "My present concern is one bounded ordinary personal problem" else "My present concern is outside ordinary support"
        }
        return when (ProductionTherapyInputBoundary.normalize(text)) {
            "i don't know", "i do not know" -> "I withdraw: $statement"
            "i decline to answer" -> "I decline to state: $statement"
            else -> "Correction: $statement"
        }
    }

    fun startsObservationBlock(text: String): Boolean {
        val bare = if (listOf("Correction: ", "I am unsure: ", "I withdraw: ", "I decline to state: ").any { text.startsWith(it, true) }) text.substringAfter(": ") else text
        return ProductionTherapyInputBoundary.normalize(bare) in setOf(
            "there is no current emergency",
            "this is a current emergency",
            "there is no acute medical emergency",
            "there is an acute medical emergency",
            "self-harm is not relevant now",
            "self-harm is relevant now",
            "harm to others is not relevant now",
            "harm to others is relevant now",
            "i report no specialized condition for this conversation",
            "i report a specialized condition for this conversation",
            "i am an adult in the supported setting",
            "i am not an adult",
            "my present concern is one bounded ordinary personal problem",
            "my present concern needs specialized support",
            "my present concern is outside ordinary support"
        )
    }

    fun observe(request: ProductionTurnRequest): SafetyScopeInput {
        val revision = SafetyEvidenceRevision.of(request.clientTurnIndex)
        val stateId = "android-therapy-state-${request.clientTurnIndex}"
        var input = previous?.copy(stateId = stateId, evidenceRevision = revision) ?: SafetyScopeInput(
            stateId, revision, ThomasMode.THERAPIST, SafetyEvidence.unknown(), SafetyEvidence.unknown(),
            SafetyEvidence.unknown(), SafetyEvidence.unknown(), SafetyEvidence.unknown(),
            SafetyEvidence.unknown(), SafetyEvidence.unknown())
        // The legacy broad declaration has no authority to establish unrelated absences or scope facts.
        if (request.therapySafetyDeclaration == TherapySafetyDeclaration.CURRENT_EMERGENCY) {
            input = input.copy(currentEmergency = SafetyEvidence.established(
                ExplicitEmergencyCircumstance.OTHER_EMERGENCY_EXPLICITLY_ESTABLISHED,
                SafetyEvidenceOrigin.DIRECT_USER_REPORT, "$stateId-emergency-control-declaration"))
        }
        val reply = if (request.mode == ProductionThomasMode.THERAPY) replyDeclaration(request.committedText) else null
        var declarationPrefix = true
        (reply ?: request.committedText).lineSequence().forEachIndexed { index, raw ->
            if (!declarationPrefix) return@forEachIndexed
            var line = raw.trim()
            val correcting = line.startsWith("Correction: ", true)
            val tentative = line.startsWith("I am unsure: ", true)
            val withdraw = line.startsWith("I withdraw: ", true)
            val refuse = line.startsWith("I decline to state: ", true)
            if (correcting || tentative || withdraw || refuse) line = line.substringAfter(": ").trim()
            val key = ProductionTherapyInputBoundary.normalize(line)
            val reference = "$stateId-line-$index-" + (if (reply != null) "reply-to-${pending!!.name}-" else "") + when {
                correcting -> "correction"; tentative -> "tentative-declaration"
                withdraw -> "withdrawal"; refuse -> "refusal"; else -> "explicit-declaration"
            }
            fun <T> update(old: SafetyEvidence<T>, value: T): SafetyEvidence<T> {
                if (withdraw) return SafetyEvidence.unknown()
                if (refuse) return SafetyEvidence.declined(reference)
                if (tentative) return SafetyEvidence.tentative(value, SafetyEvidenceOrigin.DIRECT_USER_REPORT, reference)
                if (!correcting && (old.resolution == SafetyEvidenceResolution.CONTRADICTORY ||
                        (old.resolution == SafetyEvidenceResolution.ESTABLISHED && old.value != value))) {
                    return SafetyEvidence.contradictory(*(old.evidenceReferences + reference).toTypedArray())
                }
                return SafetyEvidence.established(value, SafetyEvidenceOrigin.DIRECT_USER_REPORT, reference)
            }
            input = when (key) {
                "there is no current emergency" -> input.copy(currentEmergency = update(input.currentEmergency, ExplicitEmergencyCircumstance.NONE_ESTABLISHED))
                "this is a current emergency" -> input.copy(currentEmergency = update(input.currentEmergency, ExplicitEmergencyCircumstance.OTHER_EMERGENCY_EXPLICITLY_ESTABLISHED))
                "there is no acute medical emergency" -> input.copy(acuteMedicalEmergency = update(input.acuteMedicalEmergency, SafetyPresence.ABSENT))
                "there is an acute medical emergency" -> input.copy(acuteMedicalEmergency = update(input.acuteMedicalEmergency, SafetyPresence.PRESENT))
                "self-harm is not relevant now" -> input.copy(selfHarmRelevance = update(input.selfHarmRelevance, SafetyPresence.ABSENT))
                "self-harm is relevant now" -> input.copy(selfHarmRelevance = update(input.selfHarmRelevance, SafetyPresence.PRESENT))
                "harm to others is not relevant now" -> input.copy(harmToOthersRelevance = update(input.harmToOthersRelevance, SafetyPresence.ABSENT))
                "harm to others is relevant now" -> input.copy(harmToOthersRelevance = update(input.harmToOthersRelevance, SafetyPresence.PRESENT))
                "i report no specialized condition for this conversation" -> input.copy(specializedScopeCondition = update(input.specializedScopeCondition, SpecializedScopeCondition.NONE_IDENTIFIED))
                "i report a specialized condition for this conversation" -> input.copy(specializedScopeCondition = update(input.specializedScopeCondition, SpecializedScopeCondition.OTHER_SPECIALIZED_CONDITION))
                "i am an adult in the supported setting" -> input.copy(populationApplicability = update(input.populationApplicability, PopulationApplicability.SUPPORTED_ADULT_QUALIFICATION_CONTEXT))
                "i am not an adult" -> input.copy(populationApplicability = update(input.populationApplicability, PopulationApplicability.UNSUPPORTED_AGE_OR_POPULATION))
                "my present concern is one bounded ordinary personal problem" -> input.copy(presentingScope = update(input.presentingScope, PresentingScope.BOUNDED_ORDINARY_PERSONAL_PROBLEM))
                "my present concern needs specialized support" -> input.copy(presentingScope = update(input.presentingScope, PresentingScope.SPECIALIZED_POLICY_REQUIRED))
                "my present concern is outside ordinary support" -> input.copy(presentingScope = update(input.presentingScope, PresentingScope.OUT_OF_SCOPE))
                else -> input.also { declarationPrefix = false }
            }
        }
        previous = input
        return input
    }
}
