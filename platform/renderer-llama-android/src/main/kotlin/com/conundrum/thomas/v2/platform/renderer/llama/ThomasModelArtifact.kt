package com.conundrum.thomas.v2.platform.renderer.llama

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.system.measureNanoTime

data class ThomasModelArtifact(
    val filename: String,
    val expectedSha256: String,
    val expectedBytes: Long,
    val path: File,
)

data class VerifiedThomasModel(
    val artifact: ThomasModelArtifact,
    val actualSha256: String,
    val verifiedBytes: Long,
    val verificationMillis: Long,
)

enum class ThomasModelVerificationErrorCode {
    PATH_OUTSIDE_NO_BACKUP,
    FILE_MISSING,
    FILENAME_MISMATCH,
    EXPECTATION_MISMATCH,
    SIZE_MISMATCH,
    HASH_MISMATCH,
    FILE_CHANGED_DURING_VERIFICATION,
}

class ThomasModelVerificationException(
    val code: ThomasModelVerificationErrorCode,
    message: String,
) : IllegalArgumentException(message)

/** Exact custody for the historically accepted R008 mobile Thomas artifact. */
object RecoveredThomasQ6K {
    const val FILENAME = "Thomas-CT-R007-Merged-Q6_K.gguf"
    const val SHA256 = "d8a98b45c1c0e72ad63fe4a55105dbbc8c59bfa8910d37c0d0ff83d01125cb95"
    const val BYTES = 3_155_622_624L
    const val MODEL_ID = "ct-r007-candidate-b-r16"
    const val BASE_MODEL = "microsoft/Phi-4-mini-instruct"
    const val BASE_REVISION = "cfbefacb99257ffa30c83adab238a50856ac3083"
    const val ADAPTER_SHA256 = "cd3967b9cb787c5e6bbbbe37b392bd87590241d594a3983159ff616e9a0113ad"
    const val LLAMA_CPP_RELEASE = "b10621"
    const val LLAMA_CPP_COMMIT = "c1d0e7a004015f23bc0233470b747b596f29b264"
}

object ThomasModelLocation {
    fun artifact(context: Context): ThomasModelArtifact = ThomasModelArtifact(
        filename = RecoveredThomasQ6K.FILENAME,
        expectedSha256 = RecoveredThomasQ6K.SHA256,
        expectedBytes = RecoveredThomasQ6K.BYTES,
        path = File(context.noBackupFilesDir, "models/${RecoveredThomasQ6K.FILENAME}"),
    )
}

class ThomasModelArtifactVerifier(private val noBackupRoot: File) {
    fun verify(artifact: ThomasModelArtifact): VerifiedThomasModel {
        val root = noBackupRoot.canonicalFile
        val candidate = artifact.path.canonicalFile
        if (candidate.parentFile?.canonicalFile != File(root, "models").canonicalFile) {
            fail(ThomasModelVerificationErrorCode.PATH_OUTSIDE_NO_BACKUP,
                "Thomas model must be in app-private no-backup models storage")
        }
        if (!candidate.isFile) fail(ThomasModelVerificationErrorCode.FILE_MISSING, "Thomas model is missing")
        if (candidate.name != RecoveredThomasQ6K.FILENAME) {
            fail(ThomasModelVerificationErrorCode.FILENAME_MISMATCH, "Thomas model filename is not admitted")
        }
        if (artifact.expectedSha256 != RecoveredThomasQ6K.SHA256 ||
            artifact.expectedBytes != RecoveredThomasQ6K.BYTES
        ) {
            fail(ThomasModelVerificationErrorCode.EXPECTATION_MISMATCH, "Caller supplied a non-governed model identity")
        }
        val bytesBefore = candidate.length()
        val modifiedBefore = candidate.lastModified()
        if (bytesBefore != RecoveredThomasQ6K.BYTES) {
            fail(ThomasModelVerificationErrorCode.SIZE_MISMATCH, "Thomas model size does not match the admitted artifact")
        }
        var digest = ""
        val elapsed = measureNanoTime {
            val md = MessageDigest.getInstance("SHA-256")
            FileInputStream(candidate).buffered(HASH_BUFFER_BYTES).use { input ->
                val buffer = ByteArray(HASH_BUFFER_BYTES)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    md.update(buffer, 0, read)
                }
            }
            digest = md.digest().joinToString("") { "%02x".format(it) }
        }
        if (candidate.length() != bytesBefore || candidate.lastModified() != modifiedBefore) {
            fail(ThomasModelVerificationErrorCode.FILE_CHANGED_DURING_VERIFICATION,
                "Thomas model changed during SHA-256 verification")
        }
        if (!digest.equals(RecoveredThomasQ6K.SHA256, ignoreCase = true)) {
            fail(ThomasModelVerificationErrorCode.HASH_MISMATCH, "Thomas model SHA-256 does not match")
        }
        return VerifiedThomasModel(artifact, digest, bytesBefore, elapsed / 1_000_000)
    }

    private fun fail(code: ThomasModelVerificationErrorCode, message: String): Nothing =
        throw ThomasModelVerificationException(code, message)

    private companion object {
        const val HASH_BUFFER_BYTES = 1024 * 1024
    }
}
