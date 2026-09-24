package com.conundrum.thomas.v2

import android.content.Context
import com.conundrum.thomas.v2.personaldata.CompleteResetResult
import com.conundrum.thomas.v2.personaldata.PersonalDataOpenResult
import com.conundrum.thomas.v2.personaldata.ProtectedBackupArtifact
import com.conundrum.thomas.v2.personaldata.RecoveryKey
import com.conundrum.thomas.v2.personaldata.RestoreResult
import com.conundrum.thomas.v2.platform.persistence.AndroidPersonalDataPersistenceFactory
import com.conundrum.thomas.v2.platform.renderer.llama.ThomasLlamaLanguageRealizer
import com.conundrum.thomas.v2.runtime.ThomasProductionRuntime

/** The single canonical Android production composition root for CT-V2-15. */
class ThomasAndroidCompositionRoot private constructor(
    private val applicationContext: Context,
    private var runtimeHolder: ThomasProductionRuntime?,
    private var failureCode: String?,
) : AutoCloseable {
    val runtime: ThomasProductionRuntime?
        get() = runtimeHolder

    val unavailableReason: String?
        get() = failureCode

    fun resetAndReopen(): CompleteResetResult {
        val active = requireNotNull(runtimeHolder) { "Runtime is unavailable" }
        val result = active.reset()
        active.close()
        runtimeHolder = null
        openRuntime()
        return result
    }

    fun replaceFromProtectedBackup(
        artifact: ProtectedBackupArtifact,
        recoveryKey: RecoveryKey,
    ): Result<RestoreResult> = runCatching {
        // Authentication, schema, ledger, projection, and source-revision checks happen before
        // the destructive empty-target transition. A bad backup leaves the current corpus intact.
        val validated = AndroidPersonalDataPersistenceFactory.validateBackup(
            artifact,
            recoveryKey,
        ).getOrThrow()
        val active = requireNotNull(runtimeHolder) { "Runtime is unavailable" }
        val reset = active.reset()
        check(reset.storeArtifactDeleted && reset.keyMaterialDestroyed && reset.inMemoryStateCleared) {
            "Existing personal-data corpus could not be reset for restore"
        }
        active.close()
        runtimeHolder = null
        val restored = AndroidPersonalDataPersistenceFactory.restoreIntoEmpty(
            applicationContext,
            artifact,
            recoveryKey,
        ).getOrThrow()
        check(restored.restoredRevision == validated.sourceStoreRevision)
        check(restored.logicalStateDigest == validated.logicalStateDigest)
        runtimeHolder = ThomasProductionRuntime(
            restored.store,
            externalRealizer = ThomasLlamaLanguageRealizer(applicationContext),
        )
        failureCode = null
        restored
    }.onFailure {
        openRuntime()
    }

    override fun close() {
        runtimeHolder?.close()
        runtimeHolder = null
    }

    private fun openRuntime() {
        when (val opened = AndroidPersonalDataPersistenceFactory.open(applicationContext)) {
            is PersonalDataOpenResult.Opened -> {
                runtimeHolder = ThomasProductionRuntime(
                    opened.store,
                    externalRealizer = ThomasLlamaLanguageRealizer(applicationContext),
                )
                failureCode = null
            }
            is PersonalDataOpenResult.Unavailable -> {
                runtimeHolder = null
                failureCode = opened.reasonCode
            }
        }
    }

    companion object {
        fun open(context: Context): ThomasAndroidCompositionRoot {
            val root = ThomasAndroidCompositionRoot(context.applicationContext, null, null)
            root.openRuntime()
            return root
        }
    }
}
