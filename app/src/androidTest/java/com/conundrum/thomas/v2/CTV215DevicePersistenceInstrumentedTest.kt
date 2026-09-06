package com.conundrum.thomas.v2

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.personaldata.PersonalDataOpenResult
import com.conundrum.thomas.v2.personaldata.RecoveryKey
import com.conundrum.thomas.v2.platform.persistence.AndroidAtomicProtectedArtifactStorage
import com.conundrum.thomas.v2.platform.persistence.AndroidKeystorePersonalDataKeyProvider
import com.conundrum.thomas.v2.platform.persistence.AndroidPersonalDataPersistenceFactory
import com.conundrum.thomas.v2.runtime.ProductionThomasMode
import com.conundrum.thomas.v2.runtime.ProductionTurnDisposition
import com.conundrum.thomas.v2.runtime.ProductionTurnPrivacy
import com.conundrum.thomas.v2.runtime.ProductionTurnRequest
import com.conundrum.thomas.v2.runtime.TherapySafetyDeclaration
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import java.io.File
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Physical-device CT-V2-15 persistence gates. Fixtures are fixed and synthetic. */
@RunWith(AndroidJUnit4::class)
class CTV215DevicePersistenceInstrumentedTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val preferences get() = context.getSharedPreferences(GATE_PREFERENCES, 0)

    @Test
    fun a_createSyntheticCorpusForRestartAndRebootGate() {
        ThomasAndroidCompositionRoot.open(context).use { root ->
            check(root.runtime != null) { "Runtime unavailable: ${root.unavailableReason ?: "UNCLASSIFIED"}" }
            root.resetAndReopen()
            val runtime = requireNotNull(root.runtime)
            val beforeCalls = runtime.snapshot().rendererCallCount
            val result = runtime.submit(
                ProductionTurnRequest(
                    clientTurnIndex = 1,
                    mode = ProductionThomasMode.JOURNAL,
                    committedText = SYNTHETIC_SOURCE,
                    journalResponsePreference = JournalResponsePreference.NO_RESPONSE,
                    committedAt = Instant.parse("2025-03-04T05:06:07Z"),
                ),
            )
            val snapshot = runtime.snapshot()
            assertEquals(
                result.reasonCodes.joinToString(prefix = "reasonCodes=[", postfix = "]"),
                ProductionTurnDisposition.NO_RESPONSE,
                result.disposition,
            )
            assertNotNull(result.committedSourceId)
            assertEquals(beforeCalls, snapshot.rendererCallCount)
            assertEquals(0L, snapshot.acceptedAssistantArtifactCount)
            assertTrue(snapshot.storeRevision > 0)
            assertTrue(runtime.sourceSummaries().size == 1)
            assertTrue(preferences.edit()
                .putString(EXPECTED_DIGEST, snapshot.logicalStateDigest)
                .putLong(EXPECTED_REVISION, snapshot.storeRevision)
                .commit())
        }

        val protectedFile = File(context.noBackupFilesDir, "thomas-personal-data/store.ctpd")
        assertTrue(protectedFile.isFile)
        assertTrue(protectedFile.canonicalPath.startsWith(context.noBackupFilesDir.canonicalPath + File.separator))
        assertFalse(protectedFile.readBytes().containsSubsequence(SYNTHETIC_SOURCE.encodeToByteArray()))
        assertPlatformBackupDisabled()

        val observation = AndroidKeystorePersonalDataKeyProvider().observation()
        val evidence = Bundle().apply {
            putString("ct_v2_15_storage", "noBackupFilesDir")
            putString("ct_v2_15_keystore_provider", observation.provider)
            putString("ct_v2_15_keystore_algorithm", observation.algorithm)
            putString("ct_v2_15_hardware_backed", observation.hardwareBacked?.toString() ?: "UNKNOWN")
        }
        instrumentation.sendStatus(0, evidence)
    }

    @Test
    fun b_verifySyntheticCorpusAfterRuntimeRestartOrDeviceReboot() {
        val expectedDigest = requireNotNull(preferences.getString(EXPECTED_DIGEST, null))
        val expectedRevision = preferences.getLong(EXPECTED_REVISION, -1)
        ThomasAndroidCompositionRoot.open(context).use { root ->
            val runtime = requireNotNull(root.runtime) { root.unavailableReason ?: "Runtime unavailable" }
            val snapshot = runtime.snapshot()
            assertEquals(expectedRevision, snapshot.storeRevision)
            assertEquals(expectedDigest, snapshot.logicalStateDigest)
            assertEquals(1, runtime.sourceSummaries().size)
            assertEquals(ProductionThomasMode.JOURNAL.name, runtime.sourceSummaries().single().acquisitionMode.name)
        }
        assertPlatformBackupDisabled()
    }

    @Test
    fun c_backupRestoreResetAndKeyLossFailClosed() {
        val expectedDigest = requireNotNull(preferences.getString(EXPECTED_DIGEST, null))
        RecoveryKey.generate().use { recoveryKey ->
            val root = ThomasAndroidCompositionRoot.open(context)
            val runtime = requireNotNull(root.runtime) { root.unavailableReason ?: "Runtime unavailable" }
            val backup = runtime.createProtectedBackup(recoveryKey)
            assertFalse(backup.protectedBytes().containsSubsequence(SYNTHETIC_SOURCE.encodeToByteArray()))
            val validation = AndroidPersonalDataPersistenceFactory.validateBackup(backup, recoveryKey).getOrThrow()
            assertEquals(expectedDigest, validation.logicalStateDigest)

            val reset = root.resetAndReopen()
            assertTrue(reset.storeArtifactDeleted)
            assertTrue(reset.keyMaterialDestroyed)
            assertTrue(reset.inMemoryStateCleared)
            assertTrue(requireNotNull(root.runtime).sourceSummaries().isEmpty())

            val restored = root.replaceFromProtectedBackup(backup, recoveryKey).getOrThrow()
            assertEquals(expectedDigest, restored.logicalStateDigest)
            assertEquals(expectedDigest, requireNotNull(root.runtime).snapshot().logicalStateDigest)

            val secondBackup = requireNotNull(root.runtime).createProtectedBackup(recoveryKey)
            root.close()
            AndroidKeystorePersonalDataKeyProvider().destroy()
            assertTrue(AndroidPersonalDataPersistenceFactory.open(context) is PersonalDataOpenResult.Unavailable)

            assertTrue(AndroidAtomicProtectedArtifactStorage(context).delete())
            val recovered = AndroidPersonalDataPersistenceFactory.restoreIntoEmpty(
                context,
                secondBackup,
                recoveryKey,
            ).getOrThrow()
            recovered.store.use { restoredStore ->
                assertEquals(expectedDigest, restoredStore.reader.canonicalLogicalStateDigest())
            }
        }

        ThomasAndroidCompositionRoot.open(context).use { root ->
            val reset = root.resetAndReopen()
            assertTrue(reset.storeArtifactDeleted && reset.keyMaterialDestroyed && reset.inMemoryStateCleared)
            assertTrue(requireNotNull(root.runtime).sourceSummaries().isEmpty())
        }
        assertTrue(preferences.edit().clear().commit())
    }

    @Test
    fun d_productionGoldenPathsPreserveModeSafetyCorrectionPrivacyAndDeletion() {
        ThomasAndroidCompositionRoot.open(context).use { root ->
            root.resetAndReopen()
            val runtime = requireNotNull(root.runtime)
            val journal = runtime.submit(turn(1, ProductionThomasMode.JOURNAL, "I moved to Cedar Falls in 2018."))
            assertEquals(ProductionTurnDisposition.NO_RESPONSE, journal.disposition)
            val journalSource = requireNotNull(journal.committedSourceId)

            val biographerPrompt = runtime.nextBiographerPrompt(2)
            assertNotNull(biographerPrompt)
            val biographer = runtime.submit(
                turn(3, ProductionThomasMode.BIOGRAPHER, "Synthetic history with uncertain timing."),
            )
            assertNotNull(biographer.committedSourceId)

            val memoryless = runtime.submit(
                turn(4, ProductionThomasMode.THERAPY, "$CURRENT_DECLARATIONS\nMy specific concern is: I was furious yesterday.").copy(
                    requestedTherapySupport = RequestedOrdinarySupport.LISTEN,
                ),
            )
            assertEquals(ProductionTurnDisposition.COMPLETED, memoryless.disposition)
            assertNotNull(memoryless.therapyPlan?.routeDecision)

            val recall = runtime.submit(
                turn(5, ProductionThomasMode.THERAPY, "$CURRENT_DECLARATIONS\nMy specific concern is: I have been thinking about Cedar Falls.").copy(
                    requestedTherapySupport = RequestedOrdinarySupport.LISTEN,
                    therapyMemoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
                ),
            )
            assertTrue(recall.therapyPlan?.surfacedMemories.orEmpty().any {
                it.acquisitionMode == AcquisitionMode.JOURNAL
            })

            val safety = runtime.submit(
                turn(6, ProductionThomasMode.THERAPY, "I withdraw: There is no current emergency.").copy(
                    therapySafetyDeclaration = TherapySafetyDeclaration.UNSPECIFIED,
                ),
            )
            assertNull(safety.therapyPlan?.routeDecision)
            assertNull(safety.therapyPlan?.contextSummary)

            val revision = runtime.reviseSource(
                journalSource,
                "I moved to Cedar Falls in 2019.",
                7,
                ReportTime(Instant.now()),
            )
            assertTrue(revision.accepted)
            assertEquals(2, runtime.sourceSummaries().first { it.stableSourceId == journalSource }.revisionCount)

            assertTrue(runtime.changeSourcePrivacy(journalSource, makePrivate = true, commandIndex = 8).accepted)
            val privateRecall = runtime.submit(
                turn(9, ProductionThomasMode.THERAPY, "$CURRENT_DECLARATIONS\nMy specific concern is: I have been thinking about Cedar Falls.").copy(
                    requestedTherapySupport = RequestedOrdinarySupport.LISTEN,
                    therapyMemoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
                ),
            )
            assertTrue(privateRecall.therapyPlan?.surfacedMemories.orEmpty().none {
                journalSource.value in it.stableObjectId || it.sourceRevisionIds.any { id -> journalSource.value in id.value }
            })

            assertTrue(runtime.deleteSource(journalSource, commandIndex = 10).accepted)
            assertTrue(runtime.sourceSummaries().none { it.stableSourceId == journalSource })
            root.resetAndReopen()
        }
    }

    @Test
    fun e_productionPathPerformanceObservationIsBounded() {
        val openSamples = mutableListOf<Double>()
        ThomasAndroidCompositionRoot.open(context).use { initial -> initial.resetAndReopen() }
        repeat(3) {
            openSamples += elapsedMillis {
                ThomasAndroidCompositionRoot.open(context).use { root -> requireNotNull(root.runtime).snapshot() }
            }
        }
        val biographerSamples = (1L..3L).map { sample ->
            ThomasAndroidCompositionRoot.open(context).use { sampleRoot ->
                sampleRoot.resetAndReopen()
                val sampleRuntime = requireNotNull(sampleRoot.runtime)
                assertNotNull(
                    sampleRuntime.submit(
                        turn(1, ProductionThomasMode.JOURNAL, "I moved to Synthetic Place $sample in ${2010 + sample}."),
                    ).committedSourceId,
                )
                elapsedMillis {
                    assertNotNull(sampleRuntime.nextBiographerPrompt(2))
                    val result = sampleRuntime.submit(
                        turn(3, ProductionThomasMode.BIOGRAPHER, "Synthetic history answer for device timing."),
                    )
                    assertEquals(ProductionTurnDisposition.COMPLETED, result.disposition)
                }
            }
        }

        ThomasAndroidCompositionRoot.open(context).use { root ->
            root.resetAndReopen()
            val runtime = requireNotNull(root.runtime)
            val journalSamples = (1L..3L).map { index ->
                elapsedMillis {
                    val result = runtime.submit(turn(index, ProductionThomasMode.JOURNAL, "I was sad today."))
                    assertEquals(ProductionTurnDisposition.NO_RESPONSE, result.disposition)
                }
            }
            val renderedJournalSamples = (4L..6L).map { index ->
                elapsedMillis {
                    val result = runtime.submit(
                        turn(index, ProductionThomasMode.JOURNAL, "I was sad today.").copy(
                            journalResponsePreference = JournalResponsePreference.REFLECT,
                        ),
                    )
                    assertEquals(ProductionTurnDisposition.COMPLETED, result.disposition)
                }
            }
            val therapyWithoutMemory = (7L..9L).map { index ->
                elapsedMillis {
                    val result = runtime.submit(
                        turn(index, ProductionThomasMode.THERAPY, "$CURRENT_DECLARATIONS\nMy specific concern is: I was furious yesterday."),
                    )
                    assertNotNull(result.therapyPlan?.routeDecision)
                }
            }
            runtime.submit(turn(10, ProductionThomasMode.JOURNAL, "I moved to Cedar Falls in 2018."))
            val therapyWithMemory = (11L..13L).map { index ->
                elapsedMillis {
                    val result = runtime.submit(
                        turn(index, ProductionThomasMode.THERAPY, "$CURRENT_DECLARATIONS\nMy specific concern is: I have been thinking about Cedar Falls.").copy(
                            therapyMemoryIntent = TherapyMemoryIntent.EXPLICIT_RECALL,
                        ),
                    )
                    assertNotNull(result.therapyPlan?.contextSummary)
                }
            }
            RecoveryKey.generate().use { key ->
                lateinit var backup: com.conundrum.thomas.v2.personaldata.ProtectedBackupArtifact
                val exportSamples = (1..3).map {
                    elapsedMillis {
                        val export = runtime.export()
                        assertTrue(export.machineReadableJson.isNotBlank())
                        assertTrue(export.humanReadableMarkdown.isNotBlank())
                    }
                }
                val backupSamples = (1..3).map {
                    elapsedMillis { backup = runtime.createProtectedBackup(key) }
                }
                val restoreSamples = (1..3).map {
                    elapsedMillis { root.replaceFromProtectedBackup(backup, key).getOrThrow() }
                }
                instrumentation.sendStatus(0, performanceBundle(
                    openSamples,
                    journalSamples,
                    renderedJournalSamples,
                    biographerSamples,
                    therapyWithoutMemory,
                    therapyWithMemory,
                    exportSamples,
                    backupSamples,
                    restoreSamples,
                ))
            }
            root.resetAndReopen()
        }
    }

    private fun assertPlatformBackupDisabled() {
        val flags = context.applicationInfo.flags
        assertEquals(0, flags and ApplicationInfo.FLAG_ALLOW_BACKUP)
    }

    private fun ByteArray.containsSubsequence(needle: ByteArray): Boolean {
        if (needle.isEmpty() || needle.size > size) return false
        return indices.take(size - needle.size + 1).any { offset ->
            needle.indices.all { needleIndex -> this[offset + needleIndex] == needle[needleIndex] }
        }
    }

    private fun turn(index: Long, mode: ProductionThomasMode, text: String) = ProductionTurnRequest(
        clientTurnIndex = index,
        mode = mode,
        committedText = text,
        privacy = ProductionTurnPrivacy.ELIGIBLE,
        journalResponsePreference = JournalResponsePreference.NO_RESPONSE,
        committedAt = Instant.now(),
    )

    private inline fun elapsedMillis(operation: () -> Unit): Double {
        val started = System.nanoTime()
        operation()
        return (System.nanoTime() - started) / 1_000_000.0
    }

    private fun performanceBundle(vararg groups: List<Double>) = Bundle().apply {
        val names = listOf(
            "store_open",
            "journal",
            "deterministic_render",
            "biographer",
            "therapy_no_memory",
            "therapy_with_memory",
            "export",
            "backup",
            "restore",
        )
        names.zip(groups).forEach { (name, values) ->
            val sorted = values.sorted()
            putString("ct_v2_15_${name}_median_ms", "%.3f".format(sorted[sorted.size / 2]))
            putString("ct_v2_15_${name}_worst_ms", "%.3f".format(values.max()))
        }
    }

    private companion object {
        const val CURRENT_DECLARATIONS = "There is no current emergency.\nThere is no acute medical emergency.\nSelf-harm is not relevant now.\nHarm to others is not relevant now.\nI report no specialized condition for this conversation.\nI am an adult in the supported setting.\nMy present concern is one bounded ordinary personal problem."
        const val GATE_PREFERENCES = "ct-v2-15-device-gate"
        const val EXPECTED_DIGEST = "expected-digest"
        const val EXPECTED_REVISION = "expected-revision"
        const val SYNTHETIC_SOURCE = "Synthetic device qualification: I moved to Cedar Falls in 2018."
    }
}
