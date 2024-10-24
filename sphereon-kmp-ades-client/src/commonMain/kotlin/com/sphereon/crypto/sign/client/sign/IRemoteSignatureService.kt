package com.sphereon.crypto.sign.client.sign

import com.sphereon.crypto.sign.client.SignClientException
import com.sphereon.crypto.sign.client.SigningException
import com.sphereon.crypto.generic.MaskGenFunction
import com.sphereon.crypto.sign.model.SigningMode
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.sign.client.model.*
import com.sphereon.crypto.kms.IKeyProviderService
import com.sphereon.crypto.generic.Certificate
import com.sphereon.crypto.sign.model.OrigData
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature

interface IRemoteSignatureService {

    val keyProvider: IKeyProviderService

    /**
     * Determines the bytes that will serve as input for the digest or signature.
     * Since multiple signature types are supported the configuration and key are required te determine the appropriate mode
     *
     * @param origData The orignal data/file
     * @param kid
     * The key identifier
     * @param signMode The signmode to use
     * @param signatureConfiguration The configuration
     *
     * @return Sign input, which can be fed to the createSignature or digest methods
     */
    fun determineSignInput(
        origData: OrigData,
        kid: String,
        signMode: SigningMode,
        signatureConfiguration: SignatureConfiguration
    ): SignInput

    /**
     * Generate a digest from the input value, using the digest algorithm supplied. SignMode must be set to 'DIGEST'
     *
     * Please note that a Sign Input is returned, containing the digest value
     *
     * @param signInput
     * The data of which the digest will be generated, with sign mode set to 'DIGEST'
     *
     * @return A sign input object containing the digest value and digest algorithm, which can be fed to the sign functions
     */
    @Throws(SignClientException::class)
    fun digest(signInput: SignInput): SignInput

    /**
     *
     * This method signs the `signInput` data with the digest `digestAlg` and
     * the given `keyEntry`.
     *
     * @param signInput
     * The data that need to be signed
     * @param kid
     * The key identifier
     * @return the signature value representation with the used algorithm and the binary value
     * @throws SigningException
     * If there is any problem during the signature process
     */
    @Throws(SigningException::class)
    fun createSignature(signInput: SignInput, kid: String): Signature


    /**
     * This method signs the `signInput` data with the digest `digestAlg`, the mask `mgf` and
     * the given `keyEntry`.
     *
     * @param signInput
     * The data that need to be signed
     * @param mgf
     * the mask generation function
     * @param kid
     * The key identifier
     * @return the signature value representation with the used algorithm and the binary value
     * @throws SigningException
     * If there is any problem during the signature process
     */
    @Throws(SigningException::class)
    fun createSignature(
        signInput: SignInput,
        kid: String,
        mgf: MaskGenFunction
    ): Signature

    /**
     * This method signs the `signInput` data with the provided Signature Algorithm and
     * the given `keyEntry`.
     *
     * @param signInput
     * The data that need to be signed
     * @param signatureAlgorithm
     * the Signature Algorithm
     * @param kid
     * The key identifier
     * @return the signature value representation with the used algorithm and the binary value
     * @throws SigningException
     * If there is any problem during the signature process
     */
    @Throws(SigningException::class)
    fun createSignature(
        signInput: SignInput,
        kid: String,
        signatureAlgorithm: SignatureAlgorithm
    ): Signature

    fun isValidSignature(signInput: SignInput, signature: Signature, certificate: Certificate): Boolean


    fun isValidSignature(signInput: SignInput, signature: Signature, kid: String): Boolean

    /**
     *
     * This method signs the `signInput` data with the digest `digestAlg` and
     * the given `keyEntry`.
     *
     * @param origData
     * The data that need to be signed
     * @param kid
     * The key identifier
     * @param signMode
     * The signing mode
     * @param signatureConfiguration
     * The signature configuration
     * @return the sign output representation with the used algorithm and the binary value
     * @throws SigningException
     * If there is any problem during the signature process
     */
    @Throws(SigningException::class)
    fun sign(origData: OrigData, kid: String, signMode: SigningMode, signatureConfiguration: SignatureConfiguration): SignOutput

    /**
     * This method create the `signOutput` using the `signInput` a calculated `signature` and the provided configuration
     *
     * @param origData
     * The data that need to be signed
     * @param signature
     * The calculated signature
     * @param signatureConfiguration
     * The signature configuration
     * @return the sign output representation with the used algorithm and the binary value
     * @throws SigningException
     * If there is any problem during the signature process
     */
    @Throws(SigningException::class)
    fun sign(origData: OrigData, signature: Signature, signatureConfiguration: SignatureConfiguration): SignOutput
}
