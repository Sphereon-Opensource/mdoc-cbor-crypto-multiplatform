package com.sphereon.crypto.kms

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IManagedKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.kms.model.KeyProviderSettings
import kotlin.js.JsExport

/**
 * The interface for the PKI service adds key functionality to the simple signature interface.
 */
@JsExport
interface IKeyStoreService {

    /**
     * Holds the configuration settings for a Key Provider.
     *
     * This includes the provider's unique identifier, a configuration object specifying the
     * provider's settings, and an optional callback for securely inputting passwords.
     */
    val settings: KeyProviderSettings

    /**
     * Lists all the managed cryptographic keys available in the key store.
     *
     * @return An array of IManagedKeyInfo objects representing the managed keys.
     * @throws PKIException if there is an error accessing the key store.
     */
    @Throws(PKIException::class)
    fun listKeys(): Array<IManagedKeyInfo<*>>

    /**
     * Retrieves a managed cryptographic key based on the provided key information.
     *
     * @param keyInfo The key information used to locate and retrieve the managed key. This includes metadata and configuration details related to the key.
     * @return The managed key information, including the key and any additional details relevant to the managed key.
     * @throws PKIException If there is an error during key retrieval.
     */
    @Throws(PKIException::class)
    fun getKey(keyInfo: IKeyInfo<*>): IManagedKeyInfo<*>

    /**
     * Stores a resolved cryptographic key into the key management system (KMS) using the provided
     * KMS identifier and key reference.
     *
     * @param keyInfo The resolved key information that will be stored. This includes the cryptographic key
     *                and associated metadata that has been resolved and is ready for storage.
     * @param kms The identifier of the key management system where the key will be stored. This is a
     *            string that uniquely identifies the KMS within the system.
     * @param kmsKeyRef The key reference within the key management system. This is a string that uniquely
     *                  identifies the specific key within the KMS, allowing for precise retrieval and management.
     * @return IManagedKeyInfo representing the successfully stored managed key information, including
     *         the key and any additional details relevant to the managed key.
     */
    fun storeKey(keyInfo: IResolvedKeyInfo<*>, kms: String, kmsKeyRef: String): IManagedKeyInfo<*>


    /**
     * Deletes a key from the key store.
     *
     * @param keyInfo Information about the key to be deleted.
     * @return true if the key was successfully deleted, false otherwise.
     */
    fun deleteKey(keyInfo: IKeyInfo<*>): Boolean


    fun keyVisibility(): KeyVisibility
}
