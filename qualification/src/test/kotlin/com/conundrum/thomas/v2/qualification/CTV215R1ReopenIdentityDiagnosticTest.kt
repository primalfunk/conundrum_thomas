package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.runtime.*
import org.junit.Assert.*
import org.junit.Test

/** Diagnostic for the newly observed UI admission failure, not a passing UI acceptance gate.
 * This proves the store correctly rejects a conflicting identity. The application allocator
 * (ThomasViewModel: storeRevision + 1 on reopen) still needs a separate bounded repair.
 */
class CTV215R1ReopenIdentityDiagnosticTest {
    @Test fun unrecordedPromptCanMakeReopenAllocatorReuseACommittedTurnIdentity(): Unit = CTV215Harness().use { h ->
        var uiNextIndex = h.runtime.snapshot().storeRevision + 1
        assertNotNull(h.runtime.nextBiographerPrompt(uiNextIndex++, openStory = true))
        assertEquals(0L, h.runtime.snapshot().storeRevision)
        val firstIndex = uiNextIndex++
        fun request(index: Long, concern: String) = h.turn(index, ProductionThomasMode.THERAPY,
            CTV215R1ProductionConversationTest.DECLARATIONS + "\nMy specific concern is: " + concern) {
            copy(requestedTherapySupport = RequestedOrdinarySupport.LISTEN)
        }
        val first = h.runtime.submit(request(firstIndex, "the first synthetic meeting"))
        assertNotNull(first.committedSourceId)
        assertEquals(ProductionTurnDisposition.COMPLETED, first.disposition)
        val revision = h.runtime.snapshot().storeRevision
        println("IDENTITY_BEFORE_REOPEN index=$firstIndex storeRevision=$revision source=${first.committedSourceId} nextUnused=$uiNextIndex")
        h.reopen()
        // The exact current ThomasViewModel initialization formula, not internal state injection.
        val reopenedIndex = h.runtime.snapshot().storeRevision + 1
        assertEquals(firstIndex, reopenedIndex)
        val second = h.runtime.submit(request(reopenedIndex, "a different synthetic meeting"))
        println("IDENTITY_AFTER_REOPEN index=$reopenedIndex storeRevision=${h.runtime.snapshot().storeRevision} result=$second")
        assertEquals(ProductionTurnDisposition.COMPLETED, second.disposition)
        assertNotNull(second.assistantArtifact)
        assertNull(second.committedSourceId)
        assertTrue(second.reasonCodes.toString(), "IDEMPOTENCY_KEY_PAYLOAD_CONFLICT" in second.reasonCodes)
        assertEquals(revision, h.runtime.snapshot().storeRevision)
        assertEquals(1, h.runtime.sourceSummaries().size)
        // These are correct store rejection assertions. They must never be reported as proof
        // that the application successfully admitted the second UI turn.
    }
}
