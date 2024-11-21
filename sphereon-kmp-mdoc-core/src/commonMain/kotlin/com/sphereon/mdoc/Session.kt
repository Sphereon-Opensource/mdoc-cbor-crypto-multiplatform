package com.sphereon.mdoc

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborEncodedItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.cbor.NumberLabel
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborUInt
import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.hash
import com.sphereon.json.JsonView
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.encodeTo
import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlinx.io.bytestring.ByteStringBuilder
import toRawEcdhPrivateKey
import toRawEcdhPublicKey
import kotlin.js.JsExport

private val MDOC_READER_IDENTIFIER = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
private val MDOC_DEVICE_IDENTIFIER = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01)

@JsExport
class SessionEncryption internal constructor(
    val selfRole: MdocRole,
    val selfPrivateKey: IResolvedKeyInfo<ICoseKeyCbor>,
    val otherPublicKey: IResolvedKeyInfo<ICoseKeyCbor>,
    val cryptoProvider: CryptographyProvider = CryptographyProvider.Default,
    val selfSessionKey: ByteArray,
    val otherSessionKey: ByteArray
) {
    private var encryptCount: UInt = 1U // Starts at one according to 18013-5
    private var decryptCount: UInt = 1U // Starts at one according to 18013-5

    private fun sendSessionEstablishment(): Boolean {
        return encryptCount == 1U && selfRole === MdocRole.MDOC_READER
    }

    @JsExport.Ignore
    suspend fun decryptSessionEstablishment(messageData: ByteArray) =
        decrypt(messageData).encryptedSessionEstablishment
            ?: throw IllegalStateException("No session establishment found. Are you looking for session data? Then call the correct decrypt method")

    @JsExport.Ignore
    suspend fun decryptSessionData(messageData: ByteArray) =
        decrypt(messageData).sessionData
            ?: throw IllegalStateException("No session data found. Are you looking for session establishment? Then call the correct decrypt method")


    @OptIn(DelicateCryptographyApi::class)
    @JsExport.Ignore
    suspend fun decrypt(messageData: ByteArray): IDecryptResultCbor {
        val map: CborMap<StringLabel, AnyCborItem> = cborSerializer.decode(messageData)
        var eReaderKey: CoseKeyCbor? = null
        if (SessionEstablishmentCbor.Static.E_READER_KEY.optional<CoseKeyCbor>(map) != null) {
            eReaderKey = CoseKeyCbor.Static.fromEncodedCborItem(SessionEstablishmentCbor.Static.E_READER_KEY.required(map))
        }
        val encryptedSessionData = SessionDataCbor.Static.cborDecode(messageData)
        var decryptedSessionData: SessionDataCbor? = null
        if (encryptedSessionData.data != null) {
            val ivBuilder = ByteStringBuilder(12)
            ivBuilder.append(if (selfRole === MdocRole.MDOC) MDOC_READER_IDENTIFIER else MDOC_DEVICE_IDENTIFIER)
            ivBuilder.appendCounter(decryptCount)
            val iv = ivBuilder.toByteString().toByteArray()
            check(iv.size == 12) { "Invalid IV length, expected 12 bytes, got: ${iv.encodeTo(Encoding.HEX)}" }
            val otherSessionKeyRaw = cryptoProvider.get(AES.GCM).keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, otherSessionKey)
            val plainTextData =
                otherSessionKeyRaw.cipher(tagSize = 128.bits).decryptWithIv(
                    iv = ivBuilder.toByteString().toByteArray(),
                    ciphertext = encryptedSessionData.data!!.value,
                    associatedData = "".encodeToByteArray()
                )
            decryptCount++

            decryptedSessionData = encryptedSessionData.copy(data = plainTextData.toCborByteString())
        } else if (eReaderKey !== null) {
            throw IllegalStateException("Cannot have an eReaderKey (session establishment), without data")
        }
        return object : IDecryptResultCbor {
            // Both properties are mutual exclusive, hence the eReaderKey expression in both
            override val encryptedSessionEstablishment: SessionEstablishmentCbor?
                get() = if (eReaderKey != null) SessionEstablishmentCbor(
                    eReaderKey = eReaderKey,
                    data = encryptedSessionData.data!!
                ) else null
            override val sessionData: SessionDataCbor =
                if (decryptedSessionData !== null) decryptedSessionData else SessionDataCbor(status = encryptedSessionData.status)
        }

    }

    @OptIn(DelicateCryptographyApi::class)
    @JsExport.Ignore
    suspend fun encrypt(plainTextData: ByteArray? = null, status: LongKMP? = null): ByteArray {
        var cipherText: ByteArray? = null
        val sendSessionEstablishment =
            sendSessionEstablishment() // We do this before the below counter increase, as the logic looks for mdoc_reader and encrypt counter being 1
        if (plainTextData != null || sendSessionEstablishment) {
            requireNotNull(plainTextData) { "plain text data is required when using send session establishment" }
            val ivBuilder = ByteStringBuilder(12)
            ivBuilder.append(if (selfRole === MdocRole.MDOC) MDOC_DEVICE_IDENTIFIER else MDOC_READER_IDENTIFIER)
            ivBuilder.appendCounter(encryptCount)
            val iv = ivBuilder.toByteString().toByteArray()
            check(iv.size == 12) { "Invalid IV length, expected 12 bytes, got: ${iv.encodeTo(Encoding.HEX)}" }
            val selfSessionKeyRaw = cryptoProvider.get(AES.GCM).keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, selfSessionKey)
            cipherText = selfSessionKeyRaw.cipher(tagSize = 128.bits)
                .encryptWithIv(iv = iv, plaintext = plainTextData, associatedData = "".encodeToByteArray())
            encryptCount++
        }
        val message: CborView<*, *, *>
        if (sendSessionEstablishment) {
            requireNotNull(cipherText) { "An initial message always needs a data object" }
            message = SessionEstablishmentCbor(
                eReaderKey = CoseKeyCbor.Static.fromDTO(selfPrivateKey.toResolvedPublicKeyInfo().key),
                data = cipherText.toCborByteString()
            )
        } else {
            message = SessionDataCbor(
                data = cipherText?.toCborByteString(),
                status = status?.toCborUInt()
            )
        }
        return message.cborEncode()
    }

    @JsExport.Ignore
    suspend fun encryptAsSessionData(plainTextData: ByteArray? = null, status: LongKMP? = null) =
        encrypt(plainTextData, status).let { SessionDataCbor.Static.cborDecode(it) }

    @JsExport.Ignore
    suspend fun encryptAsSessionEstablishment(plainTextData: ByteArray? = null, status: LongKMP? = null): SessionEstablishmentCbor {
        check(sendSessionEstablishment()) { "Can only encrypt session establishment as a first action on the reader side" }
        return encrypt(plainTextData, status).let { SessionEstablishmentCbor.Static.cborDecode(it) }
    }


    class Builder(
        private var selfRole: MdocRole = MdocRole.MDOC,
        private var selfEphemeralPrivateKey: IResolvedKeyInfo<*>? = null,
        private var otherEphemeralPublicKey: IResolvedKeyInfo<*>? = null,
        private var sessionTranscriptBytes: ByteArray? = null,
        private var provider: CryptographyProvider = CryptographyProvider.Default,
    ) {

        fun withSelfRole(selfRole: MdocRole) = apply { this.selfRole = selfRole }
        fun withSelfPrivateEphemeralKey(selfEphemeralPrivateKey: IResolvedKeyInfo<*>) =
            apply { this.selfEphemeralPrivateKey = selfEphemeralPrivateKey }

        fun withOtherPublicEphemeralKey(otherEphemeralPublicKey: IResolvedKeyInfo<*>) =
            apply { this.otherEphemeralPublicKey = otherEphemeralPublicKey }

        fun withSessionTranscriptBytes(sessionTranscriptBytes: ByteArray) = apply { this.sessionTranscriptBytes = sessionTranscriptBytes }
        fun withProvider(provider: CryptographyProvider = CryptographyProvider.Default) = apply { this.provider = provider }

        @JsExport.Ignore
        suspend fun build(): SessionEncryption {
            requireNotNull(selfEphemeralPrivateKey) { "selfPrivateKey must be set" }
            requireNotNull(otherEphemeralPublicKey) { "otherPublicKey must be set" }
            requireNotNull(sessionTranscriptBytes) { "sessionTranscript must be set" }
            val selfPrivateCborKey = CoseJoseKeyMappingService.toResolvedCoseKeyInfo(selfEphemeralPrivateKey!!)
            val selfRawPrivateKey = toRawEcdhPrivateKey(provider = provider, selfEphemeralPrivateKey!!)
            val otherPublicCborKey = CoseJoseKeyMappingService.toResolvedCoseKeyInfo(otherEphemeralPublicKey!!)
            val otherRawPublicKey = toRawEcdhPublicKey(provider = provider, otherEphemeralPublicKey!!)
            val deviceInfo = "SKDevice".encodeToByteArray()
            val readerInfo = "SKReader".encodeToByteArray()
            val salt = hash(sessionTranscriptBytes!!, DigestAlg.SHA256)
            val sharedSecret = selfRawPrivateKey.sharedSecretGenerator().generateSharedSecretToByteArray(otherRawPublicKey)
            val hkdf = provider.get(HKDF)
            val sessionKeyDevice =
                hkdf.secretDerivation(digest = SHA256, outputSize = 32.bytes, salt = salt, info = deviceInfo).deriveSecretToByteArray(sharedSecret)
            val sessionKeyReader =
                hkdf.secretDerivation(digest = SHA256, outputSize = 32.bytes, salt = salt, info = readerInfo).deriveSecretToByteArray(sharedSecret)
            return SessionEncryption(
                selfRole,
                selfPrivateCborKey,
                otherPublicCborKey,
                cryptoProvider = provider,
                // Please keep the logic below invariant. They need to be opposites
                selfSessionKey = if (selfRole === MdocRole.MDOC) sessionKeyDevice else sessionKeyReader,
                otherSessionKey = if (selfRole === MdocRole.MDOC) sessionKeyReader else sessionKeyDevice,
            )
        }
    }
}


@JsExport
data class SessionEstablishmentJson(val eReaderKey: CoseKeyJson, val data: String) : JsonView() {
    override fun toJsonString(): String {
        throw IllegalStateException("Only cbor version can be used for session establishment!")
    }

    override fun toCbor(): Any {
        throw IllegalStateException("Only cbor version can be used for session establishment!")
    }
}

@JsExport
data class SessionEstablishmentCbor(val eReaderKey: CoseKeyCbor, val data: CborByteString) :
    CborView<SessionEstablishmentCbor, SessionEstablishmentJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<SessionEstablishmentCbor> {
        return CborMap.Static.builder(this)
            .put(Static.E_READER_KEY, CborEncodedItem(eReaderKey.toCbor()))
            .put(Static.DATA, data).end()
    }

    override fun toJson(): SessionEstablishmentJson {
        TODO("Not yet implemented")
    }

    object Static {
        val E_READER_KEY = StringLabel("eReaderKey")
        val DATA = StringLabel("data")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): SessionEstablishmentCbor {
            val eReaderKeyBytes: CborEncodedItem<CborMap<NumberLabel, AnyCborItem>> = E_READER_KEY.required(m)
            return SessionEstablishmentCbor(
                eReaderKey = CoseKeyCbor.Static.fromCborItem(eReaderKeyBytes.cborDecode()),
                data = DATA.required(m)
            )
        }

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }
}


@JsExport
data class SessionDataJson(val data: String? = null, val status: LongKMP? = null) : JsonView() {
    override fun toJsonString(): String {
        throw IllegalStateException("Only cbor version can be used for session data!")
    }

    override fun toCbor(): Any {
        throw IllegalStateException("Only cbor version can be used for session data!")
    }
}


@JsExport
data class SessionDataCbor(val data: CborByteString? = null, val status: CborUInt? = null) :
    CborView<SessionDataCbor, SessionDataJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<SessionDataCbor> {
        return CborMap.Static.builder(this)
            .put(Static.DATA, data, optional = true)
            .put(Static.STATUS, status, optional = true)
            .end()
    }

    fun getStatus(): SessionDataStatus? {
        return status?.let { SessionDataStatus.entries.firstOrNull { entry -> entry.status.value == it.value } }
    }

    override fun toJson(): SessionDataJson {
        TODO("Not yet implemented")
    }

    object Static {
        val DATA = StringLabel("data")
        val STATUS = StringLabel("status")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) = SessionDataCbor(
            data = DATA.optional(m),
            status = STATUS.optional(m)
        )

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }
}

@JsExport
enum class SessionDataStatus(val requiredAction: String, val description: String, val status: CborUInt) {
    ERROR_SESSION_ENCRYPTION(status = CborUInt(10), description = "Error: session encryption", requiredAction = "The session shall be terminated."),
    ERROR_CBOR_DECODING(status = CborUInt(11), description = "Error: CBOR decoding", requiredAction = "The session shall be terminated."),
    SESSION_TERMINATION(status = CborUInt(20), description = "Session termination", requiredAction = "The session shall be terminated.");

    fun getCode(): LongKMP = status.value

    fun isError(): Boolean = status.value != LongKMP(20)

}


/**
 * Interface representing the result of a session decryption operation in CBOR format.
 *
 * @property encryptedSessionEstablishment Contains the CBOR data related to session establishment (only present in the first message).
 *      The data in the establishment is still encrypted. Want to access the unencrypted version, then access the sessionData objects.
 * @property sessionData Contains the CBOR data related to the session itself.
 */
interface IDecryptResultCbor {
    val encryptedSessionEstablishment: SessionEstablishmentCbor?
    val sessionData: SessionDataCbor
}


private fun ByteStringBuilder.appendCounter(value: UInt) = apply {
    append((value shr 24).and(0xffU).toByte())
    append((value shr 16).and(0xffU).toByte())
    append((value shr 8).and(0xffU).toByte())
    append((value shr 0).and(0xffU).toByte())
}
