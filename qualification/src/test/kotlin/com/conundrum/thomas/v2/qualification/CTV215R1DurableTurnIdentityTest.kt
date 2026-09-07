package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.runtime.*
import org.junit.Assert.*
import org.junit.Test

class CTV215R1DurableTurnIdentityTest {
    @Test fun multipleCommittedTurnsReopenAboveDurableIdentityFrontier(): Unit = CTV215Harness().use { h ->
        fun allocate() = h.runtime.allocateTurnIndex()
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
        println("BEFORE_REOPEN committed=${listOf(first.turnIdentity, second.turnIdentity)} revision=$revision")
        h.reopen()
        val third = commit("a different synthetic meeting after reopen")
        println("AFTER_REOPEN selected=${third.turnIdentity} revision=${h.runtime.snapshot().storeRevision} source=${third.committedSourceId} reasons=${third.reasonCodes}")
        assertTrue(h.runtime.sourceSummaries().containsAll(original))
        assertNotNull("Reopened allocation must commit a new identity; ${third.reasonCodes}", third.committedSourceId)
        assertEquals("android-therapy-4", third.turnIdentity)
        assertNotEquals(second.turnIdentity, third.turnIdentity)
        h.reopen()
        assertEquals(3, h.runtime.sourceSummaries().size)
        assertEquals(5L, h.runtime.allocateTurnIndex())
        assertTrue(h.runtime.sourceSummaries().containsAll(original))
    }

    @Test fun freshStoreStartsAtOne(): Unit = CTV215Harness().use { h ->
        assertEquals(0L, h.runtime.snapshot().storeRevision)
        assertEquals(1L, h.runtime.allocateTurnIndex())
    }

    @Test fun consecutiveAllocationsRemainDistinctAcrossModesAndUncommittedPrompts(): Unit = CTV215Harness().use { h ->
        val journal = h.runtime.submit(h.turn(h.runtime.allocateTurnIndex(), ProductionThomasMode.JOURNAL))
        assertNotNull(journal.committedSourceId)
        assertNotNull(h.runtime.nextBiographerPrompt(h.runtime.allocateTurnIndex(), openStory = true))
        val biography = h.runtime.submit(h.turn(h.runtime.allocateTurnIndex(), ProductionThomasMode.BIOGRAPHER,
            "A synthetic family gathering was memorable."))
        assertNotNull(biography.committedSourceId)
        val therapy = h.runtime.submit(h.turn(h.runtime.allocateTurnIndex(), ProductionThomasMode.THERAPY))
        assertNotNull(therapy.committedSourceId)
        assertEquals(listOf("android-journal-1", "android-biographer-3", "android-therapy-4"),
            listOf(journal.turnIdentity, biography.turnIdentity, therapy.turnIdentity))
        h.reopen()
        assertEquals(5L, h.runtime.allocateTurnIndex())
    }

    @Test fun sparsePreexistingHistoryUsesMaximumNotCountOrLastCommit(): Unit = CTV215Harness().use { h ->
        listOf(41L, 9L).forEach { index ->
            assertNotNull(h.runtime.submit(h.turn(index, ProductionThomasMode.JOURNAL)).committedSourceId)
        }
        val original = h.runtime.sourceSummaries()
        val digest = h.runtime.snapshot().logicalStateDigest
        h.reopen()
        assertEquals(original, h.runtime.sourceSummaries())
        assertEquals(digest, h.runtime.snapshot().logicalStateDigest)
        assertEquals(42L, h.runtime.allocateTurnIndex())
        assertEquals(digest, h.runtime.snapshot().logicalStateDigest)
    }

    @Test fun revisionAheadOfTurnIndexDoesNotGovernAllocation(): Unit = CTV215Harness().use { h ->
        assertNotNull(h.runtime.submit(h.turn(1, ProductionThomasMode.JOURNAL,
            "I moved to Denver in 2018.")).committedSourceId)
        assertTrue(h.runtime.snapshot().storeRevision > 1L)
        h.reopen()
        assertEquals(2L, h.runtime.allocateTurnIndex())
    }

    @Test fun deletedHighestTurnStillReservesItsIdentity(): Unit = CTV215Harness().use { h ->
        val source = requireNotNull(h.runtime.submit(h.turn(73, ProductionThomasMode.JOURNAL)).committedSourceId)
        assertTrue(h.runtime.deleteSource(source, 2).accepted)
        assertTrue(h.runtime.sourceSummaries().isEmpty())
        h.reopen()
        assertEquals(74L, h.runtime.allocateTurnIndex())
        val next = h.runtime.submit(h.turn(h.runtime.allocateTurnIndex(), ProductionThomasMode.JOURNAL))
        assertNotNull(next.committedSourceId)
        assertEquals("android-journal-75", next.turnIdentity)
    }

    @Test fun everyLifecycleKeyAndRevisionKeyContributesItsDurableFrontier(): Unit = CTV215Harness().use { h ->
        val source = requireNotNull(h.runtime.submit(h.turn(1, ProductionThomasMode.JOURNAL)).committedSourceId)
        assertTrue(h.runtime.changeSourcePrivacy(source, true, 30).accepted)
        h.reopen()
        assertEquals(31L, h.runtime.allocateTurnIndex())
        assertTrue(h.runtime.changeSourcePrivacy(source, false, 40).accepted)
        h.reopen()
        assertEquals(41L, h.runtime.allocateTurnIndex())
        assertTrue(h.runtime.reviseSource(source, "Corrected synthetic wording.", 50,
            com.conundrum.thomas.v2.longitudinal.ReportTime(java.time.Instant.parse("2040-02-02T00:00:00Z"))).accepted)
        h.reopen()
        assertEquals(51L, h.runtime.allocateTurnIndex())
        assertTrue(h.runtime.deleteSource(source, 60).accepted)
        h.reopen()
        assertEquals(61L, h.runtime.allocateTurnIndex())
    }

    @Test fun explicitConflictingPayloadRemainsRejectedAfterAllocatorRepair(): Unit = CTV215Harness().use { h ->
        val request = h.turn(h.runtime.allocateTurnIndex(), ProductionThomasMode.JOURNAL)
        val first = h.runtime.submit(request)
        assertNotNull(first.committedSourceId)
        h.reopen()
        val digest = h.runtime.snapshot().logicalStateDigest
        val conflict = h.runtime.submit(request.copy(committedText = "A different payload."))
        assertNull(conflict.committedSourceId)
        assertTrue("IDEMPOTENCY_KEY_PAYLOAD_CONFLICT" in conflict.reasonCodes)
        assertEquals(digest, h.runtime.snapshot().logicalStateDigest)
        assertEquals(2L, h.runtime.allocateTurnIndex())
    }

    @Test fun committedLongMaximumFailsClosedWithoutWraparound(): Unit = CTV215Harness().use { h ->
        val request = h.turn(1, ProductionThomasMode.JOURNAL).copy(clientTurnIndex = Long.MAX_VALUE)
        assertNotNull(h.runtime.submit(request).committedSourceId)
        h.reopen()
        val digest = h.runtime.snapshot().logicalStateDigest
        assertThrows(ArithmeticException::class.java) { h.runtime.allocateTurnIndex() }
        assertThrows(ArithmeticException::class.java) { h.runtime.allocateTurnIndex() }
        assertEquals(digest, h.runtime.snapshot().logicalStateDigest)
    }

    @Test fun directCommittedIndexesAreObservedByAnAlreadyLiveAllocator(): Unit = CTV215Harness().use { h ->
        assertEquals(1L, h.runtime.allocateTurnIndex())
        assertNotNull(h.runtime.submit(h.turn(80, ProductionThomasMode.BIOGRAPHER,
            "A synthetic family gathering was memorable.")).committedSourceId)
        assertEquals(81L, h.runtime.allocateTurnIndex())
        h.reopen()
        assertEquals(81L, h.runtime.allocateTurnIndex())
    }
}
