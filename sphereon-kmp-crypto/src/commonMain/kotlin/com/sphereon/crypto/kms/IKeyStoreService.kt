package com.sphereon.crypto.kms

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IManagedKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.PKIException
import kotlin.js.JsExport

/**
 * The interface for the PKI service adds key functionality to the simple signature interface.
 */
@JsExport
interface IKeyStoreService {

    /**
     * The Key Provider Settings
     */
    val settings: KeyProviderSettings

    /**
     * Retrieves all the available keys from the token.
     *
     * @return List of encapsulated private keys
     * @throws PKIException
     * If there is any problem during the retrieval process
     */
    @Throws(PKIException::class)
    fun listKeys(): Array<IManagedKeyInfo<*>>

    /**
     * Retrieves a specific key by its kid.
     *
     * @param kid The key identifier
     * @return The key
     * @throws PKIException
     * If there is any problem during the retrieval process
     */
    @Throws(PKIException::class)
    fun getKey(keyInfo: IKeyInfo<*>): IManagedKeyInfo<*>

    /**
     * Stores a key and return the kid
     */
    fun storeKey(keyInfo: IResolvedKeyInfo<*>, kms: String, kmsKeyRef: String): IManagedKeyInfo<*>


    fun deleteKey(keyInfo: IKeyInfo<*>): Boolean


}
