package com.conundrum.thomas.v2.personaldata

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal object AuthenticatedProtection {
    private val magic = "CTV2PD14".toByteArray(StandardCharsets.US_ASCII)
    private const val NONCE_BYTES = 12
    private const val TAG_BITS = 128
    private const val MAX_CIPHERTEXT_BYTES = 64_000_000
    private const val GCM_TAG_BYTES = TAG_BITS / 8

    fun protect(
        plaintext: ByteArray,
        key: SecretKey,
        purpose: ProtectedArtifactPurpose,
        keyAlias: String,
        random: SecureRandom,
    ): ByteArray {
        require(key.algorithm.equals("AES", ignoreCase = true))
        if (plaintext.size > MAX_CIPHERTEXT_BYTES - GCM_TAG_BYTES) malformed("PROTECTION_PLAINTEXT_TOO_LARGE")
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val aad = aad(purpose, keyAlias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(aad)
        val ciphertext = cipher.doFinal(plaintext)
        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use {
                it.write(magic)
                it.writeInt(CT_V2_14_PROTECTION_FORMAT_VERSION)
                it.writeInt(purpose.ordinal)
                it.writeInt(nonce.size)
                it.write(nonce)
                it.writeInt(ciphertext.size)
                it.write(ciphertext)
            }
            bytes.toByteArray()
        }
    }

    fun unprotect(
        protectedBytes: ByteArray,
        key: SecretKey,
        purpose: ProtectedArtifactPurpose,
        keyAlias: String,
    ): ByteArray = try {
        DataInputStream(ByteArrayInputStream(protectedBytes)).use { input ->
            val foundMagic = ByteArray(magic.size).also(input::readFully)
            if (!foundMagic.contentEquals(magic)) malformed("PROTECTION_MAGIC_MISMATCH")
            if (input.readInt() != CT_V2_14_PROTECTION_FORMAT_VERSION) malformed("PROTECTION_VERSION_UNSUPPORTED")
            val foundPurpose = input.readInt()
            if (foundPurpose != purpose.ordinal) malformed("PROTECTION_PURPOSE_MISMATCH")
            val nonceLength = input.readInt()
            if (nonceLength != NONCE_BYTES) malformed("PROTECTION_NONCE_INVALID")
            val nonce = ByteArray(nonceLength).also(input::readFully)
            val ciphertextLength = input.readInt()
            if (ciphertextLength !in GCM_TAG_BYTES..MAX_CIPHERTEXT_BYTES || ciphertextLength != input.available()) malformed("PROTECTION_LENGTH_INVALID")
            val ciphertext = ByteArray(ciphertextLength).also(input::readFully)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
            cipher.updateAAD(aad(purpose, keyAlias))
            cipher.doFinal(ciphertext)
        }
    } catch (failure: PersonalDataPersistenceException) {
        throw failure
    } catch (failure: AEADBadTagException) {
        throw PersonalDataPersistenceException(
            PersonalDataFailureDisposition.AUTHENTICATION_OR_INTEGRITY_FAILURE,
            "PROTECTED_ARTIFACT_AUTHENTICATION_FAILED",
            failure,
        )
    } catch (failure: RuntimeException) {
        throw PersonalDataPersistenceException(PersonalDataFailureDisposition.MALFORMED_STORE, "PROTECTED_ARTIFACT_MALFORMED", failure)
    } catch (failure: java.io.IOException) {
        throw PersonalDataPersistenceException(PersonalDataFailureDisposition.MALFORMED_STORE, "PROTECTED_ARTIFACT_TRUNCATED", failure)
    }

    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }

    private fun aad(purpose: ProtectedArtifactPurpose, keyAlias: String) =
        "ct-v2-14|${purpose.name}|$keyAlias|$CT_V2_14_PROTECTION_FORMAT_VERSION".toByteArray(StandardCharsets.UTF_8)

    private fun malformed(code: String): Nothing = throw PersonalDataPersistenceException(
        PersonalDataFailureDisposition.MALFORMED_STORE,
        code,
    )
}
