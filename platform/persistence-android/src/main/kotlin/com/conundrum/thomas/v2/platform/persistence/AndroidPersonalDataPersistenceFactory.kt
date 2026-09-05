package com.conundrum.thomas.v2.platform.persistence

import android.content.Context
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import com.conundrum.thomas.v2.personaldata.PersonalDataOpenResult
import com.conundrum.thomas.v2.personaldata.BackupValidationResult
import com.conundrum.thomas.v2.personaldata.ProtectedBackupArtifact
import com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStoreFactory
import com.conundrum.thomas.v2.personaldata.RecoveryKey
import com.conundrum.thomas.v2.personaldata.RestoreResult
import java.time.Instant

/** The single production-capable Android persistence composition root; it is not wired to :app in CT-V2-14. */
object AndroidPersonalDataPersistenceFactory {
    fun open(
        context: Context,
        clock: StoreClock = StoreClock { Instant.now() },
    ): PersonalDataOpenResult = ProtectedPersonalDataStoreFactory.open(
        AndroidAtomicProtectedArtifactStorage(context.applicationContext),
        AndroidKeystorePersonalDataKeyProvider(),
        clock,
    )

    fun restoreIntoEmpty(
        context: Context,
        artifact: ProtectedBackupArtifact,
        recoveryKey: RecoveryKey,
        clock: StoreClock = StoreClock { Instant.now() },
    ): Result<RestoreResult> = ProtectedPersonalDataStoreFactory.restoreIntoEmpty(
        artifact,
        recoveryKey,
        AndroidAtomicProtectedArtifactStorage(context.applicationContext),
        AndroidKeystorePersonalDataKeyProvider(),
        clock,
    )

    fun validateBackup(
        artifact: ProtectedBackupArtifact,
        recoveryKey: RecoveryKey,
    ): Result<BackupValidationResult> = ProtectedPersonalDataStoreFactory.validateBackup(
        artifact,
        recoveryKey,
    )
}
