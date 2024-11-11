package com.sphereon.crypto.kms

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IManagedKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.ManagedKeyInfo
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.kms.model.KeyProviderConfig
import com.sphereon.crypto.kms.model.KeyProviderSettings
import com.sphereon.crypto.kms.model.KeyProviderType
import com.sphereon.kmp.Uuid


class PublicFromPrivateKeyStore(
    private val privateKeyStore: IKeyStoreService,
    override val settings: KeyProviderSettings = KeyProviderSettings(
        id = Uuid.v4String(), config = KeyProviderConfig(
            type = KeyProviderType.MEMORY, cacheEnabled = false, /* In memory so caching not needed*/
            externalKeyVisibility = KeyVisibility.PUBLIC,
        )
    ),
) : IKeyStoreService {
    init {
        require(privateKeyStore.keyVisibility() === KeyVisibility.PRIVATE) { "A public from private key store needs to have a private key store exposing private keys" }
        // TODO: Is that true? If the "private" keystore is using hardware, we simply could delegate as that would only expose pub keys anyway
    }


    override fun listKeys(): Array<IManagedKeyInfo<*>> = privateKeyStore.listKeys().map { it.toManagedPublicKeyInfo() }.toTypedArray()


    override fun getKey(keyInfo: IKeyInfo<*>) = privateKeyStore.getKey(keyInfo).toManagedPublicKeyInfo()

    override fun storeKey(keyInfo: IResolvedKeyInfo<*>, kms: String, kmsKeyRef: String): IManagedKeyInfo<*> {
        require(keyInfo.keyVisibility === KeyVisibility.PRIVATE) { "Public key to private key store adapter is backed by a private key store. This means for storing you can only use private keys, as the key would otherwise not be backed" }
        return privateKeyStore.storeKey(keyInfo, kms, kmsKeyRef).toManagedPublicKeyInfo()
    }

    override fun deleteKey(keyInfo: IKeyInfo<*>): Boolean {
        return privateKeyStore.deleteKey(keyInfo)
    }

    override fun keyVisibility() = KeyVisibility.PUBLIC

}

class MemoryKeyStoreService(
    private val keyVisibility: KeyVisibility = KeyVisibility.PUBLIC, override val settings: KeyProviderSettings = KeyProviderSettings(
        id = Uuid.v4String(), config = KeyProviderConfig(
            type = KeyProviderType.MEMORY, cacheEnabled = false, /* In memory so caching not needed*/
            externalKeyVisibility = keyVisibility,
        )
    ), val keyTypesSupported: Array<KeyType> = KeyType.Static.asList.toTypedArray(), // all supported types as we can store anything in the map.
    val signatureAlgorithmsSupported: Array<SignatureAlgorithm> = SignatureAlgorithm.Static.asList.toTypedArray() // all algs as we can store anything in a map.

) : IKeyStoreService {

    init {
        require(settings.config.type === KeyProviderType.MEMORY) { "A memory keystore needs to be of config type MEMORY" }
    }

    private val keys = mutableMapOf<String, IManagedKeyInfo<*>>()

    override fun keyVisibility() = keyVisibility

    override fun listKeys(): Array<IManagedKeyInfo<*>> = keys.map { it.value }.toTypedArray()

    override fun getKey(keyInfo: IKeyInfo<*>): IManagedKeyInfo<*> {
        var managedKeyInfo = keyInfo
        if (managedKeyInfo.kmsKeyRef === null) {
            val matchingKey =
                listKeys().find { (keyInfo.kid !== null && it.kid == keyInfo.kid) || (keyInfo.key?.getXAsString() == it.key.getXAsString() && keyInfo.key?.getYAsString() == it.key.getYAsString()) }
            if (matchingKey != null) {
                managedKeyInfo = matchingKey
            }
        }
        require(managedKeyInfo.key !== null || managedKeyInfo.kid !== null) { "Either a kid needs to be provided or a key needs to be passed in" }
        val visibility = managedKeyInfo.keyVisibility ?: this.keyVisibility
        if (this.keyVisibility === KeyVisibility.PUBLIC && visibility === KeyVisibility.PRIVATE) {
            throw PKIException("Cannot get private key info for a public key store")
        }


        val kid = managedKeyInfo.kid ?: managedKeyInfo.key?.kid

        val kmsKeyRef = managedKeyInfo.kmsKeyRef
        require(kmsKeyRef !== null) { "Need to provide a kmsKeyRef" }
        val keyInfoResult = keys[kmsKeyRef]
        require(keyInfoResult !== null) { "Could not find key for kid $kid" }
        return if (visibility === KeyVisibility.PUBLIC) ManagedKeyInfo(
            kmsKeyRef = kmsKeyRef!!, kms = keyInfoResult!!.kms, resolvedKeyInfo = keyInfoResult.toResolvedPublicKeyInfo()
        ) else keyInfoResult!!
    }

    override fun deleteKey(keyInfo: IKeyInfo<*>): Boolean {
        try {
            val storedKeyInfo = getKey(keyInfo)
            return keys.remove(storedKeyInfo.kmsKeyRef) !== null
        } catch (_: Exception) {
        }
        return false

    }

    override fun storeKey(keyInfo: IResolvedKeyInfo<*>, kms: String, kmsKeyRef: String): IManagedKeyInfo<*> {
        val visibility = keyInfo.keyVisibility ?: this.keyVisibility
        if (this.keyVisibility === KeyVisibility.PUBLIC && visibility === KeyVisibility.PRIVATE) {
            throw PKIException("Cannot get private key info for a public key store")
        }
        val managedKeyInfo = ManagedKeyInfo(kms = kms, kmsKeyRef = kmsKeyRef, resolvedKeyInfo = keyInfo)
        this.keys[kmsKeyRef] = managedKeyInfo
        return managedKeyInfo
    }
}
