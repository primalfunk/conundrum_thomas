package com.conundrum.thomas.v2.platform.persistence

import android.content.Context
import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import com.conundrum.thomas.v2.personaldata.PersonalDataOpenResult
import com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStoreFactory
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
}
