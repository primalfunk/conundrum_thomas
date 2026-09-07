package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.runtime.*
import com.conundrum.thomas.v2.engine.ordinary.*
import org.junit.Assert.*
import org.junit.Test

class CTV215R1PracticalAdjudicationTest {
    @Test fun exactUiPrefixDirectRuntimeDiagnostic() = CTV215Harness().use { h ->
        val inputs = listOf(
            CTV215R1ProductionConversationTest.DECLARATIONS + "\nMy specific concern is: the synthetic UI appointment",
            "My specific concern is: THE synthetic UI appointment!",
            "My specific concern is: the synthetic UI appointment.",
            "Another detail is: the organizer moved the date",
            "Please pause", "I am ready to resume", "I don't want to discuss this", "I want to continue",
            "That's all for now", "Thank you",
            "My specific concern is: a cancelled UI meeting\nWhat I haven't explained is: who changed it",
            "The missing detail is: the organizer changed the time", "No, that's not what I mean",
            "What I mean is: the UI meeting was delayed", "Yes, that's right",
            "My specific concern is: arranging a new UI meeting"
        )
        inputs.forEachIndexed { n, text ->
            val support = when { n < 10 -> RequestedOrdinarySupport.LISTEN; n < 15 -> RequestedOrdinarySupport.UNDERSTAND; else -> RequestedOrdinarySupport.PRACTICAL_HELP }
            val result = h.runtime.submit(h.turn(69L+n, ProductionThomasMode.THERAPY, text).copy(requestedTherapySupport=support))
            println("CHECKPOINT=${n+1} REQUEST_SUPPORT=$support RESULT=${result.disposition} PLAN=${result.therapyPlan?.routeDecision} REASONS=${result.reasonCodes} RENDER=${result.renderResult}")
            if (n >= 14) {
                println("OBSERVATION=${result.therapyObservation}")
                println("PLAN_FULL=${result.therapyPlan}")
            }
            assertNotNull(result.committedSourceId)
        }
    }
}
