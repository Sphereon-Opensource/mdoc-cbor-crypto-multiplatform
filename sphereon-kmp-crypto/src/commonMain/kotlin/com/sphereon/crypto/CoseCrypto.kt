package com.sphereon.crypto

import com.sphereon.cbor.cose.CoseKeyCbor
import com.sphereon.cbor.cose.CoseKeyType
import com.sphereon.cbor.cose.CoseSign1Cbor
import com.sphereon.cbor.cose.CoseSign1InputCbor
import dev.whyoleg.cryptography.serialization.asn1.BitArray
import dev.whyoleg.cryptography.serialization.asn1.ObjectIdentifier
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
interface CoseCryptoService {
    suspend fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>? = null
    ): CoseSign1Cbor<CborType, JsonType>

    suspend fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>? = null
    ): VerifyResults<CoseKeyCbor>
}

/**
 * The main entry point for COSE signature creation/validation, delegating to a platform specific callback implemented by external developers
 */
interface CoseCryptoCallbackService : CallbackService<CoseCryptoService>, CoseCryptoService

expect fun coseService(): CoseCryptoCallbackService

object CoseCryptoServiceObject : CoseCryptoCallbackService {
    @JvmStatic
    private lateinit var platformCallback: CoseCryptoService

    private var disabled = false

    override suspend fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>?
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
            ?: if (keyInfo?.key?.alg != null) CoseKeyType.entries.first { it.value == keyInfo.key.alg?.value?.toInt() } else null
        if (keyAlg == null) {
            throw IllegalStateException("No Key algorithm found or provided")
        }
        return this.platformCallback.sign1(input, keyInfo)
    }

    override suspend fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>?
    ): VerifyResults<CoseKeyCbor> {
        if (!this.isEnabled()) {
            return VerifyResults(
                keyInfo = keyInfo, error = false, verifications = arrayOf(
                    VerifyResult(
                        name = CryptoConst.COSE_LITERAL,
                        message = "COSE signing/verification has been disabled!",
                        error = false,
                        critical = false
                    )
                )
            )
        } else if (!CoseCryptoServiceObject::platformCallback.isInitialized) {
            throw IllegalStateException("COSE signing/verification has not been initialized. Please register your COSE implementation, or register a default implementation")
        }
        val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
        val keyAlg = sigAlg?.keyType
            ?: if (keyInfo?.key?.alg != null) CoseKeyType.entries.first { it.value == keyInfo.key.alg?.value?.toInt() } else null
        if (keyAlg == null) {
            return VerifyResults(
                error = true,
                verifications = arrayOf(
                    VerifyResult(
                        name = CryptoConst.COSE_LITERAL,
                        error = true,
                        message = "No Key algorithm found or provided",
                        critical = true
                    )
                ),
                keyInfo = keyInfo
            )
        }

        return platformCallback.verify1(input = input, keyInfo = keyInfo)
    }

    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun disable(): CoseCryptoService {
        this.disabled = true
        return this
    }

    override fun enable(): CoseCryptoService {
        this.disabled = false
        return this
    }

    override fun register(platformCallback: CoseCryptoService): CoseCryptoCallbackService {
        this.platformCallback = platformCallback
        return this
    }

}


@Serializable
class Certificate(
    val tbsCertificate: @Polymorphic Any,
    val signatureAlgorithm: SimpleAlgorithmIdentifier,
    val signature: BitArray
)

@Serializable
class SimpleAlgorithmIdentifier(
    val algorithm: ObjectIdentifier,
    val parameters: Nothing?,
)
