package com.conundrum.thomas.v2

import android.content.ContentResolver
import android.net.Uri
import com.conundrum.thomas.v2.personaldata.ProtectedBackupArtifact
import com.conundrum.thomas.v2.personaldata.RecoveryKey
import java.nio.charset.StandardCharsets

/**
 * SAF custody adapter. Compose receives statuses only; raw recovery-key material never enters UI
 * state and is written only to the separately selected user-custody destination.
 */
class AndroidDataCustodyController(
    private val resolver: ContentResolver,
    private val root: ThomasAndroidCompositionRoot,
) : AutoCloseable {
    private var pendingRecoveryKey: RecoveryKey? = null

    fun writeMachineReadableExport(destination: Uri): Result<Unit> = runCatching {
        val export = requireNotNull(root.runtime).export()
        resolver.openOutputStream(destination, "wt").use { output ->
            requireNotNull(output).write(export.machineReadableJson.toByteArray(StandardCharsets.UTF_8))
        }
    }

    fun writeHumanReadableExport(destination: Uri): Result<Unit> = runCatching {
        val export = requireNotNull(root.runtime).export()
        resolver.openOutputStream(destination, "wt").use { output ->
            requireNotNull(output).write(export.humanReadableMarkdown.toByteArray(StandardCharsets.UTF_8))
        }
    }

    fun writeProtectedBackup(destination: Uri): Result<Unit> = runCatching {
        pendingRecoveryKey?.close()
        val key = RecoveryKey.generate()
        val artifact = requireNotNull(root.runtime).createProtectedBackup(key)
        resolver.openOutputStream(destination, "wt").use { output ->
            requireNotNull(output).write(artifact.protectedBytes())
        }
        pendingRecoveryKey = key
    }

    fun writePendingRecoveryKey(destination: Uri): Result<Unit> = runCatching {
        val key = requireNotNull(pendingRecoveryKey) { "No protected backup is awaiting key custody" }
        val custodyBytes = key.copyForUserCustody()
        try {
            resolver.openOutputStream(destination, "wt").use { output ->
                requireNotNull(output).write(custodyBytes)
            }
        } finally {
            custodyBytes.fill(0)
            key.close()
            pendingRecoveryKey = null
        }
    }

    /**
     * Explicit replacement restore. The composition root performs a complete local reset before
     * the CT-V2-14 empty-target restore; callers must display that destructive consequence.
     */
    fun replaceFromSelectedFiles(selected: List<Uri>): Result<Unit> = runCatching {
        require(selected.size == 2) { "Select one protected backup and its separate recovery key" }
        val byteSets = selected.map { uri ->
            resolver.openInputStream(uri).use { input -> requireNotNull(input).readBytes() }
        }
        val keyBytes = byteSets.singleOrNull { it.size == RECOVERY_KEY_BYTES }
            ?: error("Exactly one 256-bit recovery key is required")
        val backupBytes = byteSets.singleOrNull { it !== keyBytes && it.size != RECOVERY_KEY_BYTES }
            ?: error("Exactly one protected backup artifact is required")
        try {
            val artifact = ProtectedBackupArtifact.fromUserCustody(backupBytes)
            RecoveryKey.fromUserCustody(keyBytes).use { key ->
                root.replaceFromProtectedBackup(artifact, key).getOrThrow()
            }
        } finally {
            keyBytes.fill(0)
            backupBytes.fill(0)
        }
    }

    override fun close() {
        pendingRecoveryKey?.close()
        pendingRecoveryKey = null
    }

    private companion object {
        const val RECOVERY_KEY_BYTES = 32
    }
}
