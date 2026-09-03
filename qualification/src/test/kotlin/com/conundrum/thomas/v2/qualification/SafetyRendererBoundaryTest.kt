package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.qualification.safety.DeterministicSafetyGateRenderer
import com.conundrum.thomas.v2.qualification.safety.QualificationSafetyGate
import com.conundrum.thomas.v2.safety.SafetyAuthorityState
import com.conundrum.thomas.v2.safety.SafetyEvidence
import com.conundrum.thomas.v2.safety.SafetyEvidenceOrigin
import com.conundrum.thomas.v2.safety.SafetyPresence
import com.conundrum.thomas.v2.safety.SafetyRenderRequestFactory
import com.conundrum.thomas.v2.safety.SafetyScopeGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyRendererBoundaryTest {
    private val gate = SafetyScopeGate()

    @Test
    fun `clarification renderer preserves selected act and asks exactly one question`() {
        val decision = gate.govern(
            QualificationSafetyGate.ordinaryInput("renderer-clarification").copy(
                selfHarmRelevance = SafetyEvidence.unknown(),
            ),
        )
        val request = SafetyRenderRequestFactory.create(decision)
        val rendered = DeterministicSafetyGateRenderer().renderNow(request)
        assertEquals(SafetyAuthorityState.CLARIFICATION_REQUIRED, decision.authorityState)
        assertEquals("clarify-required-safety-fact", rendered.selectedActionId)
        assertEquals("dialogue.safety-oriented-inquiry", rendered.preservedDialogueActId)
        assertEquals(1, rendered.draft.text.count { it == '?' })
        assertTrue(rendered.draft.text.contains("self-harm concern"))
    }

    @Test
    fun `safety render command forbids ordinary therapeutic content and new authority`() {
        val decision = gate.govern(
            QualificationSafetyGate.ordinaryInput("renderer-contract").copy(
                currentEmergency = SafetyEvidence.unknown(),
            ),
        )
        val command = SafetyRenderRequestFactory.create(decision).command
        assertTrue(command.directWordingRequired)
        assertFalse(command.ordinaryTherapeuticContentPermitted)
        assertFalse(command.advicePermitted)
        assertFalse(command.externalHelpInformationRequired)
        assertEquals(1, command.maximumQuestions)
        assertTrue(command.prohibitedSemanticContent.any { it.contains("prediction") })
        assertTrue(command.prohibitedSemanticContent.any { it.contains("screening instrument") })
    }

    @Test
    fun `ordinary pass cannot be rendered as a safety clarification`() {
        val decision = gate.govern(QualificationSafetyGate.ordinaryInput("renderer-ordinary"))
        assertThrows(IllegalArgumentException::class.java) { SafetyRenderRequestFactory.create(decision) }
    }

    @Test
    fun `specialized handoff without an admitted policy cannot be rendered`() {
        val decision = gate.govern(
            QualificationSafetyGate.ordinaryInput("renderer-specialized").copy(
                selfHarmRelevance = SafetyEvidence.established(
                    SafetyPresence.PRESENT,
                    SafetyEvidenceOrigin.DIRECT_USER_REPORT,
                    "synthetic-disclosure",
                ),
            ),
        )
        assertEquals(SafetyAuthorityState.SPECIALIZED_POLICY_REQUIRED, decision.authorityState)
        assertThrows(IllegalArgumentException::class.java) { SafetyRenderRequestFactory.create(decision) }
    }

    @Test
    fun `renderer receives requirement metadata but no raw user evidence`() {
        val decision = gate.govern(
            QualificationSafetyGate.ordinaryInput("renderer-minimal-evidence").copy(
                harmToOthersRelevance = SafetyEvidence.notAsked(),
            ),
        )
        val request = SafetyRenderRequestFactory.create(decision)
        assertEquals(setOf("required-safety-information"), request.authorizedSupportingText.map { it.reference }.toSet())
        assertEquals("HARM_TO_OTHERS_RELEVANCE", request.authorizedSupportingText.single().text)
        assertEquals(setOf("command", "authorizedSupportingText"), request::class.java.declaredFields.filterNot { it.isSynthetic }.map { it.name }.toSet())
    }
}
