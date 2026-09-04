package com.conundrum.thomas.v2.personaldata

import com.conundrum.thomas.v2.longitudinal.store.LongitudinalAdmissionController
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Instant
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

const val CT_V2_14_STORE_SCHEMA_VERSION = 2
const val CT_V2_14_MIN_SUPPORTED_SCHEMA_VERSION = 1
const val CT_V2_14_PROTECTION_FORMAT_VERSION = 1
const val CT_V2_14_PERSISTENCE_POLICY_VERSION = "ct-v2-14.personal-data.v1"
const val CT_V2_14_EXPORT_FORMAT_VERSION = 1
const val CT_V2_14_BACKUP_FORMAT_VERSION = 1

enum class ProtectedArtifactPurpose { PRIMARY_STORE, LOCAL_BACKUP }

data class PersonalDataKeyDescriptor(
    val alias: String,
    val provider: String,
    val hardwareBacked: Boolean?,
    val userAuthenticationRequired: Boolean,
) {
    init { require(alias.isNotBlank() && provider.isNotBlank()) }
}

class KeyMaterialUnavailableException(message: String) : IllegalStateException(message)

/** Key use is available, but raw application key bytes are never exposed by this port. */
interface PersonalDataKeyProvider {
    val descriptor: PersonalDataKeyDescriptor
    fun getOrCreate(): SecretKey
    fun existing(): SecretKey
    fun destroy()
}

/** Atomic encrypted-blob storage. Implementations never receive plaintext personal data. */
interface ProtectedArtifactStorage {
    val description: String
    fun exists(): Boolean
    fun read(): ByteArray?
    fun writeAtomically(protectedBytes: ByteArray)
    fun delete(): Boolean
}

enum class PersistenceFaultPoint {
    BEFORE_PROTECTION,
    AFTER_PROTECTION_BEFORE_ATOMIC_WRITE,
    AFTER_ATOMIC_WRITE,
    BEFORE_BACKUP_PROTECTION,
    AFTER_BACKUP_PROTECTION,
    BEFORE_RESTORE_COMMIT,
    BEFORE_PROJECTION_REBUILD_COMMIT,
    BEFORE_MIGRATION_COMMIT,
}

fun interface PersistenceFaultInjector { fun check(point: PersistenceFaultPoint) }

enum class PersonalDataFailureDisposition {
    KEY_UNAVAILABLE,
    AUTHENTICATION_OR_INTEGRITY_FAILURE,
    UNSUPPORTED_SCHEMA,
    MALFORMED_STORE,
    LEDGER_INTEGRITY_FAILURE,
    UNRECOVERABLE_SOURCE_CORRUPTION,
    STORAGE_UNAVAILABLE,
    NON_EMPTY_RESTORE_TARGET,
    BACKUP_INTEGRITY_FAILURE,
    INTERRUPTED_WITHOUT_COMMIT,
}

class PersonalDataPersistenceException(
    val disposition: PersonalDataFailureDisposition,
    val reasonCode: String,
    cause: Throwable? = null,
) : IllegalStateException(reasonCode, cause)

enum class PersonalDataOpenDisposition {
    OPENED_EMPTY,
    OPENED_CURRENT,
    OPENED_AFTER_SCHEMA_MIGRATION,
    OPENED_AFTER_PROJECTION_REBUILD,
}

sealed interface PersonalDataOpenResult {
    data class Opened(
        val store: ProtectedPersonalDataStore,
        val disposition: PersonalDataOpenDisposition,
    ) : PersonalDataOpenResult

    data class Unavailable(
        val disposition: PersonalDataFailureDisposition,
        val reasonCode: String,
    ) : PersonalDataOpenResult
}

interface ProtectedPersonalDataStore : AutoCloseable {
    val admission: LongitudinalAdmissionController
    val reader: LongitudinalReader
    val keyDescriptor: PersonalDataKeyDescriptor
    val schemaVersion: Int
    fun export(): PersonalDataExport
    fun createProtectedBackup(key: RecoveryKey): ProtectedBackupArtifact
    fun reset(): CompleteResetResult
}

data class PersonalDataExport(
    val formatVersion: Int,
    val createdAt: Instant,
    val storeRevision: Long,
    val logicalStateDigest: String,
    val machineReadableJson: String,
    val humanReadableMarkdown: String,
)

class ProtectedBackupArtifact private constructor(
    encryptedPayload: ByteArray,
    val formatVersion: Int,
    val sourceStoreRevision: Long,
    val protectedSha256: String,
) {
    private val payloadCopy = encryptedPayload.copyOf()
    internal fun encryptedPayload(): ByteArray = payloadCopy.copyOf()
    fun protectedBytes(): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use {
            it.write(BACKUP_MAGIC)
            it.writeInt(formatVersion)
            it.writeLong(sourceStoreRevision)
            it.writeUTF(protectedSha256)
            it.writeInt(payloadCopy.size)
            it.write(payloadCopy)
        }
        bytes.toByteArray()
    }

    companion object {
        private val BACKUP_MAGIC = "CTV2BAK".toByteArray(StandardCharsets.US_ASCII)

        internal fun create(bytes: ByteArray, sourceStoreRevision: Long, digest: String): ProtectedBackupArtifact {
            require(digest == AuthenticatedProtection.sha256(bytes))
            return ProtectedBackupArtifact(bytes, CT_V2_14_BACKUP_FORMAT_VERSION, sourceStoreRevision, digest)
        }

        fun fromUserCustody(bytes: ByteArray): ProtectedBackupArtifact = try {
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                val magic = ByteArray(BACKUP_MAGIC.size).also(input::readFully)
                if (!magic.contentEquals(BACKUP_MAGIC)) backupMalformed("BACKUP_MAGIC_MISMATCH")
                val version = input.readInt()
                if (version != CT_V2_14_BACKUP_FORMAT_VERSION) backupMalformed("BACKUP_VERSION_UNSUPPORTED")
                val revision = input.readLong()
                if (revision < 0) backupMalformed("BACKUP_REVISION_INVALID")
                val digest = input.readUTF()
                if (!digest.matches(Regex("^[0-9a-f]{64}$"))) backupMalformed("BACKUP_DIGEST_INVALID")
                val length = input.readInt()
                if (length !in 32..64_000_000 || length != input.available()) backupMalformed("BACKUP_LENGTH_INVALID")
                val payload = ByteArray(length).also(input::readFully)
                if (AuthenticatedProtection.sha256(payload) != digest) backupMalformed("BACKUP_DIGEST_MISMATCH")
                ProtectedBackupArtifact(payload, version, revision, digest)
            }
        } catch (failure: PersonalDataPersistenceException) {
            throw failure
        } catch (failure: Exception) {
            throw PersonalDataPersistenceException(
                PersonalDataFailureDisposition.BACKUP_INTEGRITY_FAILURE,
                "BACKUP_ARTIFACT_MALFORMED",
                failure,
            )
        }

        private fun backupMalformed(reason: String): Nothing = throw PersonalDataPersistenceException(
            PersonalDataFailureDisposition.BACKUP_INTEGRITY_FAILURE,
            reason,
        )
    }
}

class RecoveryKey private constructor(private var bytes: ByteArray) : AutoCloseable {
    internal fun secretKey(): SecretKey {
        check(bytes.isNotEmpty()) { "Recovery key has been destroyed" }
        return SecretKeySpec(bytes.copyOf(), "AES")
    }

    fun copyForUserCustody(): ByteArray {
        check(bytes.isNotEmpty()) { "Recovery key has been destroyed" }
        return bytes.copyOf()
    }

    override fun close() {
        bytes.fill(0)
        bytes = ByteArray(0)
    }

    companion object {
        fun generate(random: SecureRandom = SecureRandom()): RecoveryKey =
            RecoveryKey(ByteArray(32).also(random::nextBytes))

        fun fromUserCustody(bytes: ByteArray): RecoveryKey {
            require(bytes.size == 32) { "Recovery key must contain 256 bits" }
            return RecoveryKey(bytes.copyOf())
        }
    }
}

data class CompleteResetResult(
    val storeArtifactDeleted: Boolean,
    val keyMaterialDestroyed: Boolean,
    val inMemoryStateCleared: Boolean,
    val externallyHeldBackupsUnaffected: Boolean = true,
)

data class RestoreResult(
    val store: ProtectedPersonalDataStore,
    val restoredRevision: Long,
    val logicalStateDigest: String,
    val projectionRebuilt: Boolean,
)

enum class RetentionCategory { RETAIN_UNTIL_USER_DELETION, REVISION_RETAINED, EPHEMERAL, RECOMPUTABLE, NEVER_PERSISTED }

data class RetentionRule(val category: RetentionCategory, val reason: String) : Serializable

object PersonalDataRetentionPolicy {
    val version = "ct-v2-14.retention.v1"
    val rules: Map<String, RetentionRule> = linkedMapOf(
        "committed-user-source" to RetentionRule(RetentionCategory.RETAIN_UNTIL_USER_DELETION, "Authoritative user evidence"),
        "source-revision-history" to RetentionRule(RetentionCategory.REVISION_RETAINED, "Correction and provenance lineage"),
        "derived-state" to RetentionRule(RetentionCategory.RECOMPUTABLE, "Subordinate to eligible evidence"),
        "speech-transcript" to RetentionRule(RetentionCategory.RETAIN_UNTIL_USER_DELETION, "Committed text source; no raw audio"),
        "draft" to RetentionRule(RetentionCategory.NEVER_PERSISTED, "Drafts are not evidence"),
        "raw-speech-audio" to RetentionRule(RetentionCategory.NEVER_PERSISTED, "No raw microphone authority"),
        "context-packet" to RetentionRule(RetentionCategory.EPHEMERAL, "Read-only per-turn artifact"),
        "retrieval-working-set" to RetentionRule(RetentionCategory.EPHEMERAL, "Recomputed per request"),
        "renderer-candidate" to RetentionRule(RetentionCategory.NEVER_PERSISTED, "Untrusted candidate"),
        "rejected-renderer-candidate" to RetentionRule(RetentionCategory.NEVER_PERSISTED, "Untrusted candidate"),
        "render-fingerprint" to RetentionRule(RetentionCategory.EPHEMERAL, "Session-only anti-repetition"),
        "model-prompt" to RetentionRule(RetentionCategory.NEVER_PERSISTED, "No production model authority"),
        "model-output" to RetentionRule(RetentionCategory.NEVER_PERSISTED, "Thomas output is not evidence"),
        "narrative-log" to RetentionRule(RetentionCategory.NEVER_PERSISTED, "Operational logs contain identifiers only"),
    )
}
