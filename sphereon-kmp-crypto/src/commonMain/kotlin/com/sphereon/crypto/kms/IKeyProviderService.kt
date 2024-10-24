package com.sphereon.crypto.kms

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.sign.ISimpleSignatureService
import kotlin.js.JsExport

/**
 * The interface for the PKI service adds key functionality to the simple signature interface.
 */
@JsExport
interface IKeyProviderService : ISimpleSignatureService {

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
    fun getKeys(): Array<IResolvedKeyInfo<*>>

    /**
     * Retrieves a specific key by its kid.
     *
     * @param kid The key identifier
     * @return The key
     * @throws PKIException
     * If there is any problem during the retrieval process
     */
    @Throws(PKIException::class)
    fun getKey(keyInfo: IKeyInfo<*>): IResolvedKeyInfo<*>?

}
