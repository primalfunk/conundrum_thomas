package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.runtime.*
import org.junit.Assert.*
import org.junit.Test

class CTV215R1DurableTurnIdentityTest {
    @Test fun multipleCommittedTurnsReopenAboveDurableIdentityFrontier(): Unit = CTV215Harness().use { h ->
        // Failure-first adapter: exact starting ThomasViewModel initialization/increment.
        var next = h.runtime.snapshot().storeRevision + 1
        fun allocate() = next++
        assertNotNull(h.runtime.nextBiographerPrompt(allocate(), openStory = true))
        fun commit(concern: String) = h.runtime.submit(h.turn(allocate(), ProductionThomasMode.THERAPY,
            CTV215R1ProductionConversationTest.DECLARATIONS + "\nMy specific concern is: " + concern))
        val first = commit("the first synthetic meeting")
        val second = commit("the second synthetic meeting")
        assertNotNull(first.committedSourceId)
        assertNotNull(second.committedSourceId)
        assertEquals("android-therapy-2", first.turnIdentity)
        assertEquals("android-therapy-3", second.turnIdentity)
        val original = h.runtime.sourceSummaries()
        val revision = h.runtime.snapshot().storeRevision
        assertEquals(2L, revision)
        println("BEFORE_REOPEN committed=${listOf(first.turnIdentity, second.turnIdentity)} revision=$revision nextUnused=$next")
        h.reopen()
        next = h.runtime.snapshot().storeRevision + 1
        val third = commit("a different synthetic meeting after reopen")
        println("AFTER_REOPEN selected=${third.turnIdentity} revision=${h.runtime.snapshot().storeRevision} source=${third.committedSourceId} reasons=${third.reasonCodes}")
        assertTrue(h.runtime.sourceSummaries().containsAll(original))
        assertNotNull("Reopened allocation must commit a new identity; ${third.reasonCodes}", third.committedSourceId)
        assertEquals("android-therapy-4", third.turnIdentity)
        assertNotEquals(second.turnIdentity, third.turnIdentity)
        h.reopen()
        assertEquals(3, h.runtime.sourceSummaries().size)
        assertTrue(h.runtime.sourceSummaries().containsAll(original))
    }
}
