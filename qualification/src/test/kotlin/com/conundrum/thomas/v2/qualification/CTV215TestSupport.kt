package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.store.StoreClock
import com.conundrum.thomas.v2.personaldata.KeyMaterialUnavailableException
import com.conundrum.thomas.v2.personaldata.NioAtomicProtectedArtifactStorage
import com.conundrum.thomas.v2.personaldata.PersonalDataKeyDescriptor
import com.conundrum.thomas.v2.personaldata.PersonalDataKeyProvider
import com.conundrum.thomas.v2.personaldata.PersonalDataOpenResult
import com.conundrum.thomas.v2.personaldata.ProtectedPersonalDataStoreFactory
import com.conundrum.thomas.v2.runtime.ProductionThomasMode
import com.conundrum.thomas.v2.runtime.ProductionTurnRequest
import com.conundrum.thomas.v2.runtime.ThomasProductionRuntime
import java.nio.file.Files
import java.time.Instant
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal class CTV215Harness : AutoCloseable {
    val directory = Files.createTempDirectory("ct-v2-15-")
    val storage = NioAtomicProtectedArtifactStorage(directory.resolve("production-shape.ctpd"))
    val keyProvider = CTV215MemoryKeyProvider()
    val clock = CTV215Clock()
    var runtime = openRuntime()

    fun turn(
        index: Long,
        mode: ProductionThomasMode,
        text: String = "Synthetic qualification statement about a named project.",
        customize: ProductionTurnRequest.() -> ProductionTurnRequest = { this },
    ): ProductionTurnRequest = ProductionTurnRequest(
        clientTurnIndex = index,
        mode = mode,
        committedText = text,
        committedAt = Instant.parse("2040-02-01T00:00:00Z").plusSeconds(index),
    ).customize()

    fun reopen(): ThomasProductionRuntime {
        runtime.close()
        runtime = openRuntime()
        return runtime
    }

    override fun close() {
        runCatching { runtime.close() }
        directory.toFile().walkBottomUp().forEach { runCatching { it.delete() } }
    }

    private fun openRuntime(): ThomasProductionRuntime {
        val result = ProtectedPersonalDataStoreFactory.open(storage, keyProvider, clock)
        return ThomasProductionRuntime((result as PersonalDataOpenResult.Opened).store)
    }
}

internal class CTV215MemoryKeyProvider : PersonalDataKeyProvider {
    private var key: SecretKey? = qualificationKey(17)
    override val descriptor = PersonalDataKeyDescriptor("ct-v2-15.qualification", "memory", false, false)
    override fun getOrCreate(): SecretKey = key ?: qualificationKey(29).also { key = it }
    override fun existing(): SecretKey = key ?: throw KeyMaterialUnavailableException("Synthetic key unavailable")
    override fun destroy() { key = null }

    private fun qualificationKey(seed: Int) = SecretKeySpec(ByteArray(32) { (seed + it).toByte() }, "AES")
}

internal class CTV215Clock : StoreClock {
    private var current = Instant.parse("2041-01-01T00:00:00Z")
    override fun instant(): Instant = current.also { current = current.plusSeconds(1) }
}
