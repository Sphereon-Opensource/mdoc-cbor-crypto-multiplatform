package com.sphereon.crypto.kms

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IManagedKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.ManagedKeyInfo
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.kmp.Uuid


enum class KeyStoreType {
    PKCS11, PKCS12, JKS, AZURE_KEYVAULT, DIGIDENTITY, MEMORY
}


/**
 *
 */
class MemoryKeyStore(
    override val settings: KeyProviderSettings = KeyProviderSettings(
        id = Uuid.v4String(), config = KeyProviderConfig(
            type = KeyProviderType.MEMORY, cacheEnabled = false /* In memory so caching not needed*/
        )
    ),
    val keyVisibility: KeyVisibility = KeyVisibility.PUBLIC,
    val keyTypesSupported: Array<KeyType> = KeyType.Static.asList.toTypedArray(), // all supported types as we can store anything in the map.
    val signatureAlgorithmsSupported: Array<SignatureAlgorithm> = SignatureAlgorithm.Static.asList.toTypedArray() // all algs as we can store anything in a map.

) : IKeyStoreService {

    init {
        require(settings.config.type === KeyProviderType.MEMORY) { "A memory keystore needs to be of config type MEMORY" }
    }

    private val keys = mutableMapOf<String, IManagedKeyInfo<*>>()

    override fun listKeys(): Array<IManagedKeyInfo<*>> = keys.map { it.value }.toTypedArray()

    override fun getKey(keyInfo: IKeyInfo<*>): IManagedKeyInfo<*> {
        require(keyInfo.key !== null || keyInfo.kid !== null) { "Either a kid needs to be provided or a key needs to be passed in" }
        val visibility = keyInfo.keyVisibility ?: this.keyVisibility
        if (this.keyVisibility === KeyVisibility.PUBLIC && visibility === KeyVisibility.PRIVATE) {
            throw PKIException("Cannot get private key info for a public key store")
        }
        val kid = keyInfo.kid ?: keyInfo.key?.kid
        val kmsKeyRef = keyInfo.kmsKeyRef
        require(kmsKeyRef !== null) { "Need to provide a kmsKeyRef" }
        val keyInfoResult = keys[kmsKeyRef]
        require(keyInfoResult !== null) { "Could not find key for kid ${kid}" }
        return if (visibility === KeyVisibility.PUBLIC) ManagedKeyInfo(
            kmsKeyRef = keyInfoResult!!.kmsKeyRef,
            kms = keyInfoResult.kms,
            resolvedKeyInfo = keyInfoResult.toResolvedPublicKeyInfo()
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
