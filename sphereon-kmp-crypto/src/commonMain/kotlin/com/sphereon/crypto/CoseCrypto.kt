package com.sphereon.crypto

import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import kotlin.jvm.JvmStatic

/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
interface ICoseCryptoService {
    suspend fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null
    ): CoseSign1Cbor<CborType, JsonType>

    suspend fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null
    ): IVerifySignatureResult<ICoseKeyCbor>
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

    override suspend fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): CoseSign1Cbor<CborType, JsonType> {
        if (!isEnabled()) {
            CryptoConst.LOG.info("COSE sign1 has been disabled")
            throw IllegalStateException("COSE service is disabled; cannot sign")
        } else if (!this::platformCallback.isInitialized) {
            // TODO: Probably good to provide an option to the logger whether it should do log-throws
            CryptoConst.LOG.error(
                "COSE callback (JS) is not registered"
            ) // Yes this is logs-exception anti pattern, but we are a lib with no knowledge about platform integration
            throw IllegalStateException("COSE have not been initialized. Please register your CoseCallbacksJS implementation, or register a default implementation")
        }
        val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
        val keyAlg = sigAlg?.keyType
            ?: if (keyInfo?.key?.alg != null) CoseKeyType.entries.first { it.value == keyInfo.key?.alg?.value?.toInt() } else null
        if (keyAlg == null) {
            throw IllegalStateException("No Key algorithm found or provided")
        }
        return this.platformCallback.sign1(input, keyInfo)
    }

    override suspend fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): IVerifySignatureResult<ICoseKeyCbor> {
        if (!this.isEnabled()) {
            return VerifySignatureResult(
                keyInfo = keyInfo,
                name = CryptoConst.COSE_LITERAL,
                message = "COSE signing/verification has been disabled!",
                error = false,
                critical = false
            )


        } else if (!CoseCryptoServiceObject::platformCallback.isInitialized) {
            throw IllegalStateException("COSE signing/verification has not been initialized. Please register your COSE implementation, or register a default implementation")
        }
        val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
        val keyAlg = sigAlg?.keyType
            ?: if (keyInfo?.key?.alg != null) CoseKeyType.entries.first { it.value == keyInfo.key?.alg?.value?.toInt() } else null
        if (keyAlg == null) {
            return VerifySignatureResult(
                keyInfo = keyInfo,
                name = CryptoConst.COSE_LITERAL,
                error = true,
                message = "No Key algorithm found or provided",
                critical = true
            )
        }

        return platformCallback.verify1(input = input, keyInfo = keyInfo)
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
