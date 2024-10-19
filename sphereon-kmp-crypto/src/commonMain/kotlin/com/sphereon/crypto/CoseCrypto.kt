package com.sphereon.crypto

import CoseJoseKeyMappingService
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.jose.IJwkJson
import com.sphereon.kmp.Encoding
import kotlin.js.ExperimentalJsCollectionsApi
import kotlin.jvm.JvmStatic

/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
interface ICoseCryptoService {
    suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>? = null
    ): CoseSign1Cbor<CborType>

    suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>? = null
    ): IVerifySignatureResult<ICoseKeyCbor>

    suspend fun resolvePublicKey(keyInfo: IKeyInfo<*>): ICoseKeyCbor
}

/**
 * The main entry point for COSE signature creation/validation, delegating to a platform specific callback implemented by external developers
 */
interface CoseCryptoCallbackService : ICallbackService<ICoseCryptoService>, ICoseCryptoService

expect fun coseService(): CoseCryptoCallbackService

object CoseCryptoServiceObject : CoseCryptoCallbackService {
    @JvmStatic
    private lateinit var platformCallback: ICoseCryptoService

    private var disabled = false

    override suspend fun resolvePublicKey(keyInfo: IKeyInfo<*>): ICoseKeyCbor {
        val key = keyInfo.key
        if (key === null) {
            return platformCallback.resolvePublicKey(keyInfo)
        }
        return CoseJoseKeyMappingService.toCoseKey(key)
    }


    override suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?
    ): CoseSign1Cbor<CborType> {
        if (!isEnabled()) {
            CryptoConst.LOG.info("COSE sign1 has been disabled")
            throw IllegalStateException("COSE service is disabled; cannot sign")
        } else if (!this::platformCallback.isInitialized) {
            // TODO: Probably good to provide an option to the logger whether it should do log-throws
            CryptoConst.LOG.error(
                "COSE callback is not registered"
            ) // Yes this is logs-exception anti pattern, but we are a lib with no knowledge about platform integration
            throw IllegalStateException("COSE have not been initialized. Please register your CoseCallback implementation, or register a default implementation")
        }
        val pair = verifyAndAmendKeyInfo(input, keyInfo)
        return this.platformCallback.sign1(pair.first, pair.second)
    }


    @OptIn(ExperimentalJsCollectionsApi::class)
    override suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>?
    ): IVerifySignatureResult<ICoseKeyCbor> {
        var info: IKeyInfo<ICoseKeyCbor>? = keyInfo?.let { CoseJoseKeyMappingService.toCoseKeyInfo(it) }
        if (!this.isEnabled()) {
            return VerifySignatureResult(
                keyInfo = info,
                name = CryptoConst.COSE_LITERAL,
                message = "COSE signing/verification has been disabled!",
                error = false,
                critical = false
            )
        } else if (!CoseCryptoServiceObject::platformCallback.isInitialized) {
            throw IllegalStateException("COSE signing/verification has not been initialized. Please register your COSE implementation, or register a default implementation")
        }
        val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
        val keyType = sigAlg?.keyType ?: info?.key?.getKtyMapping()?.cose
        if (keyType == null) {
            return VerifySignatureResult(
                keyInfo = info,
                name = CryptoConst.COSE_LITERAL,
                error = true,
                message = "No signature algorithm or key type found or provided",
                critical = true
            )
        }
        if (info === null) {
            // Let's create a key info for platform specific code from the x5chain // TODO: We could also get the leaf cert and fill the rest, see also above
            info = KeyInfo(key = CoseKeyCbor(x5chain = input.protectedHeader.x5chain, kty = keyType.toCbor()))
        }

        return platformCallback.verify1(input = input, keyInfo = info)
    }


    @OptIn(ExperimentalJsCollectionsApi::class)
    suspend fun verifyAndAmendKeyInfo(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?,
    ): Pair<CoseSign1InputCbor, IKeyInfo<ICoseKeyCbor>?> {
        var x5chain = input.protectedHeader?.x5chain
        val sigAlg = input.protectedHeader?.alg ?: input.unprotectedHeader?.alg

        var keyInfoWithKey = keyInfo
        if (x5chain !== null) {
            if (keyInfo === null && sigAlg?.keyType !== null) {
                // Let's create a key info for platform specific code from the x5chain // TODO: We could also get the leaf cert and fill the rest
                keyInfoWithKey = KeyInfo(key = CoseKeyCbor(x5chain = x5chain, kty = sigAlg.keyType.toCbor()))
            }
        } else {
            if (keyInfo === null) {
                throw IllegalStateException("No protected header or key info passed in")
            }
            val key = this.resolvePublicKey(keyInfo)
            if (key is ICoseKeyCbor) {
                x5chain = key.x5chain
            } else if (key is IJwkJson) {
                x5chain = key.x5c?.let { CborArray(it.map { cert -> cert.toCborByteString(Encoding.BASE64) }.toMutableList()) }
            }
            if (keyInfo is KeyInfo<*>) {
                keyInfoWithKey = keyInfo.copy(key = key)
            } else {
                throw IllegalArgumentException("Key info is not an instance of KeyInfo")
            }
        }
        if (x5chain === null) {
            throw IllegalArgumentException("No x5c or x5chain could be found in header or resolved key")
        }


        val protectedHeaderWithX5chain = input.protectedHeader?.copy(x5chain = x5chain) ?: CoseHeaderCbor(x5chain = x5chain)
        val inputWithHeader = input.copy(protectedHeader = protectedHeaderWithX5chain)


        val keyType = sigAlg?.keyType ?: keyInfoWithKey?.key?.getKtyMapping()?.cose
        if (keyType == null) {
            throw IllegalStateException("No Key type found or provided")
        }
        return Pair(inputWithHeader, keyInfoWithKey?.let { CoseJoseKeyMappingService.toCoseKeyInfo(it) })
    }


    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun disable(): ICoseCryptoService {
        this.disabled = true
        return this
    }

    override fun enable(): ICoseCryptoService {
        this.disabled = false
        return this
    }

    override fun register(platformCallback: ICoseCryptoService): CoseCryptoCallbackService {
        this.platformCallback = platformCallback
        return this
    }

}
