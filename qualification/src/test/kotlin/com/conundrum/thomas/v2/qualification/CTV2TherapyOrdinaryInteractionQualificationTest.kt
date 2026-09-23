package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.runtime.ProductionInputOrigin
import com.conundrum.thomas.v2.runtime.ProductionThomasMode
import com.conundrum.thomas.v2.runtime.ProductionTurnDisposition
import com.conundrum.thomas.v2.safety.ExplicitEmergencyCircumstance
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyEvidenceResolution
import com.conundrum.thomas.v2.safety.SafetyInformationRequirement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression coverage for CT_V2_THERAPY_ORDINARY_INTERACTION_RESTORED_SAFETY_GUARD_CONDITIONAL. */
class CTV2TherapyOrdinaryInteractionQualificationTest {
    private val ordinaryTurns = listOf(
        "I had a pretty normal day.",
        "Work has been frustrating lately.",
        "I'm nervous about a meeting tomorrow.",
        "I argued with my brother.",
        "I've been feeling lonely.",
        "I don't really know what I want to talk about today.",
        "Things are actually going pretty well.",
        "I want to think through a decision.",
    )

    @Test fun ordinaryTherapyExemplarsUseTheOrdinaryPathByDefault() = CTV215Harness().use { h ->
        ordinaryTurns.forEachIndexed { offset, text ->
            val result = h.runtime.submit(h.turn(offset + 1L, ProductionThomasMode.THERAPY, text))
            val currentEmergency = result.safetyObservation!!.observations
                .single { it.field.name == "CURRENT_EMERGENCY" }
            assertEquals(text, result.therapyObservation!!.concernStatement.value)
            assertEquals(SafetyEvidenceResolution.UNKNOWN, currentEmergency.resolution)
            assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, result.safetyObservation!!.authorityState)
            assertNull(result.safetyObservation!!.nextExpectedEvidence)
            assertNotNull(result.therapyPlan?.routeDecision?.selectedActionId)
            assertNotNull(result.assistantArtifact)
            assertFalse(result.assistantArtifact!!.text.contains("emergency", true))
            assertFalse(result.assistantArtifact!!.text.contains("ordinary problem-solving must stop", true))
        }
    }

    @Test fun severalOrdinaryTurnsDoNotLoopAConstantEmergencyOrResponse() = CTV215Harness().use { h ->
        val outputs = ordinaryTurns.take(4).mapIndexed { offset, text ->
            h.runtime.submit(h.turn(offset + 1L, ProductionThomasMode.THERAPY, text)).assistantArtifact!!.text
        }
        assertTrue(outputs.distinct().size > 1)
        assertTrue(outputs.none { it.contains("emergency", true) })
    }

    @Test fun typedAndSpokenEquivalentTextShareTheSameTherapyPipeline() = CTV215Harness().use { typed ->
        CTV215Harness().use { spoken ->
            val text = "Work has been frustrating lately."
            val typedResult = typed.runtime.submit(typed.turn(1, ProductionThomasMode.THERAPY, text))
            val spokenResult = spoken.runtime.submit(
                spoken.turn(1, ProductionThomasMode.THERAPY, text)
                    .copy(inputOrigin = ProductionInputOrigin.SPEECH_TRANSCRIPT),
            )
            assertEquals(text, typedResult.therapyObservation!!.concernStatement.value)
            assertEquals(typedResult.therapyObservation!!.concernStatement.value,
                spokenResult.therapyObservation!!.concernStatement.value)
            assertEquals(typedResult.safetyObservation!!.observations, spokenResult.safetyObservation!!.observations)
            assertEquals(typedResult.therapyPlan?.routeDecision?.selectedActionId,
                spokenResult.therapyPlan?.routeDecision?.selectedActionId)
            assertEquals(typedResult.assistantArtifact?.text, spokenResult.assistantArtifact?.text)
            assertEquals(typedResult.disposition, spokenResult.disposition)
        }
    }

    @Test fun noPendingCurrentEmergencyAndSafePhraseDoesNotLexicallyCreateSafetyState() = CTV215Harness().use { h ->
        val result = h.runtime.submit(h.turn(1, ProductionThomasMode.THERAPY, "I am safe."))
        assertEquals(SafetyEvidenceResolution.UNKNOWN,
            result.safetyObservation!!.observations.single { it.field.name == "CURRENT_EMERGENCY" }.resolution)
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, result.safetyObservation!!.authorityState)
        assertNull(result.safetyObservation!!.nextExpectedEvidence)
        assertNotNull(result.therapyPlan?.routeDecision?.selectedActionId)
    }

    @Test fun tentativeCurrentEmergencyEvidenceCreatesLegitimatePendingClarification() = CTV215Harness().use { h ->
        val result = h.runtime.submit(
            h.turn(1, ProductionThomasMode.THERAPY, "I am unsure: This is a current emergency."),
        )
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, result.safetyObservation!!.authorityState)
        assertEquals(SafetyInformationRequirement.CURRENT_EMERGENCY_STATUS,
            result.safetyObservation!!.nextExpectedEvidence)
        assertTrue(result.assistantArtifact!!.text.contains("emergency", true))
    }

    @Test fun pendingCurrentEmergencyAcceptsContextualNegativeAndReturnsToOrdinaryTherapy() = CTV215Harness().use { h ->
        h.runtime.submit(h.turn(1, ProductionThomasMode.THERAPY,
            "I am unsure: This is a current emergency."))
        val answered = h.runtime.submit(h.turn(2, ProductionThomasMode.THERAPY, "No, there is no emergency."))
        assertEquals(ExplicitEmergencyCircumstance.NONE_ESTABLISHED.name,
            answered.safetyObservation!!.observations.single { it.field.name == "CURRENT_EMERGENCY" }.value)
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, answered.safetyObservation!!.authorityState)
        assertNull(answered.safetyObservation!!.nextExpectedEvidence)
        val next = h.runtime.submit(h.turn(3, ProductionThomasMode.THERAPY, "I want to think through a decision."))
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, next.safetyObservation!!.authorityState)
        assertNotNull(next.therapyPlan?.routeDecision?.selectedActionId)
        assertFalse(next.assistantArtifact!!.text.contains("emergency", true))
    }

    @Test fun explicitCurrentEmergencyStillReachesTheExistingBoundary() = CTV215Harness().use { h ->
        val result = h.runtime.submit(h.turn(1, ProductionThomasMode.THERAPY, "This is a current emergency."))
        assertEquals(SafetyAuthorityState.EMERGENCY_BOUNDARY_REACHED, result.safetyObservation!!.authorityState)
        assertEquals(ProductionTurnDisposition.SAFETY_PREEMPTED, result.disposition)
        assertNull(result.therapyPlan?.routeDecision)
    }

    @Test fun restartDoesNotInventOrResurrectCurrentEmergencyClarification() = CTV215Harness().use { h ->
        h.runtime.submit(h.turn(1, ProductionThomasMode.THERAPY, "I am unsure: This is a current emergency."))
        h.reopen()
        val ordinary = h.runtime.submit(h.turn(2, ProductionThomasMode.THERAPY, "Things are actually going pretty well."))
        assertEquals(SafetyEvidenceResolution.UNKNOWN,
            ordinary.safetyObservation!!.observations.single { it.field.name == "CURRENT_EMERGENCY" }.resolution)
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, ordinary.safetyObservation!!.authorityState)
        assertNull(ordinary.safetyObservation!!.nextExpectedEvidence)

        h.runtime.submit(h.turn(3, ProductionThomasMode.THERAPY, "I am unsure: This is a current emergency."))
        h.runtime.submit(h.turn(4, ProductionThomasMode.THERAPY, "I am safe."))
        h.reopen()
        val afterSatisfied = h.runtime.submit(h.turn(5, ProductionThomasMode.THERAPY, "I had a pretty normal day."))
        assertEquals(SafetyEvidenceResolution.UNKNOWN,
            afterSatisfied.safetyObservation!!.observations.single { it.field.name == "CURRENT_EMERGENCY" }.resolution)
        assertEquals(SafetyAuthorityState.ORDINARY_POLICY_ALLOWED, afterSatisfied.safetyObservation!!.authorityState)
        assertNull(afterSatisfied.safetyObservation!!.nextExpectedEvidence)
    }
}
