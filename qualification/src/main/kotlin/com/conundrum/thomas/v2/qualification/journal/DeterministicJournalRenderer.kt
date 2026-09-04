package com.conundrum.thomas.v2.qualification.journal

import com.conundrum.thomas.v2.journal.JournalResponsePlan
import com.conundrum.thomas.v2.journal.JournalResponseRenderer
import com.conundrum.thomas.v2.journal.JournalResponseSemanticAct
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass

/** Mechanical qualification renderer. It receives no source body and has no evidence authority. */
class DeterministicJournalRenderer : JournalResponseRenderer {
    var invocationCount: Int = 0
        private set

    override fun render(plan: JournalResponsePlan): String {
        invocationCount += 1
        return when (plan.semanticAct) {
            JournalResponseSemanticAct.NONE -> error("NONE is not a renderable plan")
            JournalResponseSemanticAct.BRIEF_REFLECTION -> when (plan.grounding.epistemicClass) {
                EvidenceEpistemicClass.USER_INTERPRETATION -> "You recorded this as your interpretation."
                EvidenceEpistemicClass.SELF_BELIEF -> "You recorded a judgment you are making about yourself."
                EvidenceEpistemicClass.THIRD_PARTY_REPORT -> "You recorded what the other person reported."
                EvidenceEpistemicClass.EXPLICIT_SELF_REPORT -> "You recorded how this experience felt to you."
                else -> "You recorded that this happened."
            }
            JournalResponseSemanticAct.ONE_GROUNDED_QUESTION ->
                "Would you like to write more about the part you just recorded?"
        }
    }
}
