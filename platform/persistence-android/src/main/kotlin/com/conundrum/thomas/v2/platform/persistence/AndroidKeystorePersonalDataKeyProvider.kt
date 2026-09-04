package com.conundrum.thomas.v2.platform.persistence

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.conundrum.thomas.v2.personaldata.KeyMaterialUnavailableException
import com.conundrum.thomas.v2.personaldata.PersonalDataKeyDescriptor
import com.conundrum.thomas.v2.personaldata.PersonalDataKeyProvider
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeystorePersonalDataKeyProvider(
    private val alias: String = DEFAULT_ALIAS,
) : PersonalDataKeyProvider {
    override val descriptor = PersonalDataKeyDescriptor(
        alias = alias,
        provider = ANDROID_KEYSTORE,
        hardwareBacked = null,
        userAuthenticationRequired = false,
    )

    @Synchronized
    override fun getOrCreate(): SecretKey = runCatching { existing() }.getOrElse {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val specification = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUnlockedDeviceRequired(true)
            .setUserAuthenticationRequired(false)
            .build()
        generator.init(specification)
        generator.generateKey()
    }

    override fun existing(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return store.getKey(alias, null) as? SecretKey
            ?: throw KeyMaterialUnavailableException("Protected personal-data key is unavailable")
    }

    override fun destroy() {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (store.containsAlias(alias)) store.deleteEntry(alias)
    }

    companion object {
        const val DEFAULT_ALIAS = "ct-v2-14.personal-data.primary.v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }
}
