package com.sphereon.crypto.sign

import com.sphereon.crypto.IKeyInfo
import kotlin.js.JsExport

/**
 * Service interface for creating and verifying raw digital signatures.
 */
interface IRawSignatureService {
    /**
     * Generates a signature for the given input data using the provided key information.
     *
     * @param keyInfo Information about the signing key.
     * @param input The data to be signed.
     * @return The generated signature as a byte array.
     * @throws IllegalArgumentException If the private key is not provided or not supported.
     */
    @JsExport.Ignore
    suspend fun createRawSignatureAsync(keyInfo: IKeyInfo<*>, input: ByteArray): ByteArray

    /**
     * Verifies the signature of the input data using the provided key information.
     *
     * @param keyInfo Key information that includes the public key and other details.
     * @param input The original data which the signature is supposed to represent.
     * @param signature The signature that needs to be verified.
     * @return true if the signature is valid, false otherwise.
     * @throws IllegalArgumentException if a private key is used to verify the signature.
     */
    @JsExport.Ignore
    suspend fun isValidRawSignatureAsync(keyInfo: IKeyInfo<*>, input: ByteArray, signature: ByteArray): Boolean
}
