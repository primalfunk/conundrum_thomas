package com.conundrum.thomas.v2.platform.persistence

import android.content.Context
import android.util.AtomicFile
import com.conundrum.thomas.v2.personaldata.ProtectedArtifactStorage
import java.io.File

class AndroidAtomicProtectedArtifactStorage(context: Context) : ProtectedArtifactStorage {
    private val root = context.noBackupFilesDir.canonicalFile
    private val artifact = File(root, "thomas-personal-data/store.ctpd").canonicalFile.also {
        require(it.path.startsWith(root.path + File.separator))
    }
    private val atomicFile = AtomicFile(artifact)

    override val description: String = "android-no-backup-protected-personal-data"
    override fun exists(): Boolean = artifact.isFile
    override fun read(): ByteArray? = if (!exists()) null else atomicFile.openRead().use { it.readBytes() }

    override fun writeAtomically(protectedBytes: ByteArray) {
        artifact.parentFile?.mkdirs()
        val output = atomicFile.startWrite()
        try {
            output.write(protectedBytes)
            output.fd.sync()
            atomicFile.finishWrite(output)
        } catch (failure: Exception) {
            atomicFile.failWrite(output)
            throw failure
        }
    }

    override fun delete(): Boolean {
        val existed = exists()
        atomicFile.delete()
        return !existed || !exists()
    }
}
