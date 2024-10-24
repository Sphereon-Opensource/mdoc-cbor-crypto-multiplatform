package com.sphereon.crypto.sign

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.SigningException
import com.sphereon.crypto.generic.MaskGenFunction
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.Signature
import kotlin.js.JsExport

@JsExport
interface ISimpleSignatureService {
    /**
     * This method signs the `signInput` data with the digest `digestAlg`, the mask `mgf` and
     * the given `keyEntry`.
     *
     * @param signInput
     * The data that need to be signed
     * @param mgf
     * the mask generation function
     * @param keyInfo
     * The key to use
     * @return the signature value representation with the used algorithm and the binary value
     * @throws SigningException
     * If there is any problem during the signature process
     */
    @Throws(SigningException::class)
    fun createSignature(
        signInput: SignInput,
        keyInfo: IKeyInfo<*>? = null,
        mgf: MaskGenFunction? = null,
        signatureAlgorithm: SignatureAlgorithm? = keyInfo?.signatureAlgorithm,
    ): Signature

    fun isValidSignature(signInput: SignInput, signature: Signature, keyInfo: IKeyInfo<*>? = null): Boolean
}
