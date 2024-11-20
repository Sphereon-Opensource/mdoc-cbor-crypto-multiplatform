package com.sphereon.crypto.sign

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.SigningException
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature
import kotlin.js.JsExport

/**
 * Provides functionalities for creating and validating digital signatures.
 */
@JsExport.Ignore
interface ISimpleSignatureService {

    /**
     * Creates a digital signature based on the provided input and key information.
     *
     * @param signInput The input data and metadata required for creating the signature.
     * @param keyInfo Optional key information required for the signing operation.
     * @param signatureAlgorithm Optional signature algorithm to be used; defaults to the algorithm in keyInfo.
     * @return The generated signature output.
     * @throws SigningException If any error occurs during the signing process.
     */
    @Throws(SigningException::class)
    suspend fun createSignature(
        signInput: SignInput,
        keyInfo: IKeyInfo<*>? = null, // Be aware that the SignInput mostly depends on the ConfigKeyBinding. You can use this value to already provide a key for instance
        signatureAlgorithm: SignatureAlgorithm? = keyInfo?.signatureAlgorithm,
    ): SignOutput

    /**
     * Validates a given cryptographic signature against the provided signing input.
     *
     * @param signInput The input required for signing operations, including the data to be signed, signing mode, and other relevant metadata.
     * @param signature The cryptographic signature to be validated, including the signature value, algorithm, and related key information.
     * @return `true` if the signature is valid for the given signing input; `false` otherwise.
     */
    suspend fun isValidSignature(signInput: SignInput, signature: Signature): Boolean
}
