package com.conundrum.thomas.v2.personaldata

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

/** Qualification/desktop atomic blob adapter. Android uses android.util.AtomicFile instead. */
class NioAtomicProtectedArtifactStorage(private val path: Path) : ProtectedArtifactStorage {
    private val absolute = path.toAbsolutePath().normalize()
    override val description: String = "protected-internal-artifact"

    override fun exists(): Boolean = Files.isRegularFile(absolute)

    override fun read(): ByteArray? = if (exists()) Files.readAllBytes(absolute) else null

    override fun writeAtomically(protectedBytes: ByteArray) {
        val parent = requireNotNull(absolute.parent) { "Protected artifact requires a parent directory" }
        Files.createDirectories(parent)
        val temporary = parent.resolve(".${absolute.fileName}.pending")
        try {
            Files.newOutputStream(
                temporary,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
                StandardOpenOption.SYNC,
            ).use { output -> output.write(protectedBytes) }
            Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    override fun delete(): Boolean = Files.deleteIfExists(absolute)
}
