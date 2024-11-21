package com.sphereon.crypto.cose

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborNull
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.NumberLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import com.sphereon.json.JsonView
import com.sphereon.json.cryptoJsonSerializer
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.encodeTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName


@JsExport
@Serializable
data class CoseMac0InputJson(
    val protectedHeader: CoseHeaderJson = CoseHeaderJson(),

    val unprotectedHeader: CoseHeaderJson? = null,

    val externalAad: String? = "", // base64url

    val payload: String? = null, // base64url

    val detachedPayload: String? = null // base64url

) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)

    override fun toCbor(): CoseMac0InputCbor = CoseMac0InputCbor(
        protectedHeader = protectedHeader.toCbor(),
        unprotectedHeader = unprotectedHeader?.toCbor(),
        externalAad = externalAad?.decodeFrom(Encoding.BASE64URL) ?: byteArrayOf(),
        payload = payload?.decodeFrom(Encoding.BASE64URL),
        detachedPayload = detachedPayload?.decodeFrom(Encoding.BASE64URL),
    )
}

@JsExport
@Serializable
data class CoseMac0Json(
    val protectedHeader: CoseHeaderJson,

    val unprotectedHeader: CoseHeaderJson?,

    val payload: String? = null, // base64url

    val tag: String // base64url
) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)
    override fun toCbor(): CoseMac0Cbor = CoseMac0Cbor(
        protectedHeader = protectedHeader.toCbor(),
        unprotectedHeader = unprotectedHeader?.toCbor(),
        payload = payload?.toCborByteString(Encoding.BASE64URL),
        tag = tag.toCborByteString(Encoding.BASE64URL)
    )


    object Static {
        @JsName("fromDTO")
        fun fromDTO(dto: CoseMac0Json) = CoseMac0Json(
            protectedHeader = dto.protectedHeader, unprotectedHeader = dto.unprotectedHeader, payload = dto.payload, tag = dto.tag
        )
    }
}

@JsExport
data class CoseMac0InputCbor(
    val protectedHeader: CoseHeaderCbor = CoseHeaderCbor(),

    val unprotectedHeader: CoseHeaderCbor? = null,

    val externalAad: ByteArray = byteArrayOf(),

    val payload: ByteArray? = null,

    val detachedPayload: ByteArray? = null
) : CborView<CoseMac0InputCbor, CoseMac0InputJson, CborArray<AnyCborItem>>(
    CDDL.list
) {

    fun toMac0Structure() = CoseMacStructureCbor(
        context = CborString(MacContext.Mac0.value),
        protected = CborByteString(protectedHeader.cborEncode()),
        externalAad = CborByteString(externalAad),
        payload = CborByteString(payload ?: detachedPayload ?: throw IllegalStateException("Payload or detached payload is required")),
    )

    /*@JsName("toBeMacedCbor")
    fun toBeMacedCbor(sharedSecret: ByteArray, alg: SignatureAlgorithm = SignatureAlgorithm.HMAC_SHA256) =
        ToBeMacedCbor(macStructureBytes = toMac0Structure().cborEncode(), secret = sharedSecret, alg = alg, input = this)


    @JsName("toBeMacedJson")
    fun toBeMacedJson(sharedSecretAsBase64Url: String, alg: SignatureAlgorithm = SignatureAlgorithm.ECDSA_SHA256) = ToBeMacedJson(
        macStructureAsBase64Url = toMac0Structure().cborEncode().encodeToBase64Url(),
        secretAsBase64Url = sharedSecretAsBase64Url,
        alg = alg,
        input = toJson()
    )
*/
    class Builder(
        private var protectedHeader: CoseHeaderCbor = CoseHeaderCbor(),
        private var unprotectedHeader: CoseHeaderCbor? = null,
        private var payload: ByteArray? = null,
        private var detachedPayload: ByteArray? = null,
        private var externalAad: ByteArray? = null,
    ) {

        fun withProtectedHeader(protectedHeader: CoseHeaderCbor) = apply { this.protectedHeader = protectedHeader }
        fun withUnprotectedHeader(unprotectedHeader: CoseHeaderCbor) = apply { this.unprotectedHeader = unprotectedHeader }
        fun withPayloadUsingView(payload: CborView<*, *, *>) = apply { this.payload = payload.cborEncode() }
        fun withPayload(payload: ByteArray) = apply { this.payload = payload }
        fun withDetachedPayload(detachedPayload: ByteArray) = apply { this.detachedPayload = detachedPayload }
        fun withExternalAad(externalAad: ByteArray) = apply { this.externalAad = externalAad }

        fun build(): CoseMac0InputCbor {
            return CoseMac0InputCbor(
                payload = payload,
                detachedPayload = detachedPayload,
                protectedHeader = protectedHeader,
                unprotectedHeader = unprotectedHeader,
                externalAad = externalAad ?: byteArrayOf()
            )
        }
    }


    override fun cborBuilder(): CborBuilder<CoseMac0InputCbor> {
        throw NotImplementedError("CoseMac0Input Cbor encoding is not needed/possible at present")
    }

    override fun toJson(): CoseMac0InputJson = CoseMac0InputJson(
        protectedHeader = protectedHeader.toJson(), unprotectedHeader = unprotectedHeader?.toJson(), payload = payload?.encodeTo(Encoding.BASE64URL)
    )

}


@JsExport
data class CoseMac0Cbor(
    val protectedHeader: CoseHeaderCbor,

    val unprotectedHeader: CoseHeaderCbor?,

    val payload: CborByteString? = null,

    val tag: CborByteString

) : CborView<CoseMac0Cbor, CoseMac0Json, CborArray<AnyCborItem>>(CDDL.list) {

    fun toMac0Structure(detachedContent: ByteArray? = null) = CoseMacStructureCbor(
        context = CborString(MacContext.Mac0.value),
        externalAad = CborByteString(byteArrayOf()),
        protected = CborByteString(protectedHeader.cborEncode()),
        payload = payload ?: detachedContent?.let { CborByteString(it) } ?: throw IllegalArgumentException("No payload present"))

    /*@JsName("toBeMacedCbor")
    fun toBeMacedCbor(sharedSecret: ByteArray, alg: SignatureAlgorithm = SignatureAlgorithm.HMAC_SHA256, input: CoseMac0InputCbor) = ToBeMacedCbor(
        macStructureBytes = toMac0Structure(detachedContent = input.detachedPayload).cborEncode(),
        secret = sharedSecret,
        alg = alg,
        input = toMac0Input()
    )

    @JsName("toBeMacedJson")
    fun toBeMacedJson(sharedSecret: ByteArray, alg: SignatureAlgorithm = SignatureAlgorithm.HMAC_SHA256, detachedContent: ByteArray? = null) =
        ToBeMacedJson(
            macStructureAsBase64Url = toMac0Structure(detachedContent = detachedContent).cborEncode().encodeToBase64Url(),
            secretAsBase64Url = sharedSecret.encodeToBase64Url(),
            alg = alg,
            input = toMac0Input().toJson()
        )
*/
    fun detachedPayloadCopy(): CoseMac0Cbor {
        return this.copy(payload = null)
    }

    object Static {
        @JsName("fromCborItem")
        fun fromCborItem(a: CborArray<AnyCborItem>): CoseMac0Cbor {
            val protectedHeaderBytes: CborByteString = a.required(0)
            val unprotectedHeaders = a.optional<CborMap<NumberLabel, AnyCborItem>>(1)
            val payloadAvailable = a.value[2].value != null
            return CoseMac0Cbor(
                CoseHeaderCbor.Static.fromCborItem(protectedHeaderBytes.cborDecode()),
                unprotectedHeaders?.let { CoseHeaderCbor.Static.fromCborItem(it) },
                if (payloadAvailable) a.required(2) else null,
                a.required(3)
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray) = fromCborItem(cborSerializer.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<CoseMac0Cbor> {
        return CborArray.Static.builder(this).add(CborByteString(protectedHeader.cborEncode())).add(unprotectedHeader?.toCbor())
            .add(payload ?: CborNull()).add(tag).end()
    }

    override fun toJson(): CoseMac0Json = CoseMac0Json(
        protectedHeader = protectedHeader.toJson(),
        unprotectedHeader = unprotectedHeader?.toJson(),
        payload = payload?.encodeTo(Encoding.BASE64URL),
        tag = tag.encodeTo(Encoding.BASE64URL)
    )


    override fun hashCode(): Int {
        var result = protectedHeader.hashCode()
        result = 31 * result + (unprotectedHeader?.hashCode() ?: 0)
        result = 31 * result + (payload?.hashCode() ?: 0)
        result = 31 * result + tag.hashCode()
        return result
    }

    override fun toString(): String {
        return "CoseMac0Cbor(protectedHeader=$protectedHeader, unprotectedHeader=$unprotectedHeader, payload=$payload, signature=$tag)"
    }


}

typealias COSE_Mac0 = CoseMac0Cbor


@JsExport
sealed class MacContext(val value: String) {
    data object Mac : MacContext("MAC")
    data object Mac0 : MacContext("MAC0")

    fun toCbor() = CborString(value)

    object Static {
        val asList = listOf(Mac, Mac0)

        @JsName("fromValue")
        fun fromValue(value: String) = asList.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Unknown signature $value")
    }
}

@JsExport
data class CoseMacStructureJson(
    val context: MacContext = MacContext.Mac0, val protected: String, val externalAad: String? = null, // todo: "" instead of null?
    val payload: String
) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)
    override fun toCbor(): CoseMacStructureCbor {
        return CoseMacStructureCbor(
            context = CborString(context.value),
            protected = CborByteString(protected.decodeFrom(Encoding.BASE64URL)),
            externalAad = externalAad?.decodeFrom(Encoding.BASE64URL)?.let { CborByteString(it) } ?: CborByteString(byteArrayOf()),
            payload = CborByteString(payload.decodeFrom(Encoding.BASE64URL)))
    }
}

@JsExport
data class CoseMacStructureCbor(
    val context: CborString = CborString(MacContext.Mac0.value),
    val protected: CborByteString = CborByteString(byteArrayOf()),
    val externalAad: CborByteString = CborByteString(byteArrayOf()),
    val payload: CborByteString
) : CborView<CoseMacStructureCbor, CoseMacStructureJson, CborArray<AnyCborItem>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<CoseMacStructureCbor> =
        CborArray.Static.builder(this).addRequired(CborString(context.value)).addRequired(protected).addRequired(externalAad).addRequired(payload)
            .end()

    @JsName("toBeMaced")
    fun toBeMaced(): CborByteString = CborByteString(this.cborEncode())

    override fun toJson(): CoseMacStructureJson = CoseMacStructureJson(
        context = MacContext.Static.fromValue(context.value),
        protected = protected.encodeTo(Encoding.BASE64URL),
        externalAad = externalAad.encodeTo(Encoding.BASE64URL),
        payload = payload.encodeTo(Encoding.BASE64URL),
    )

    object Static {
        @JsName("fromCborItem")
        fun fromCborItem(a: CborArray<AnyCborItem>): CoseMacStructureCbor {
            return CoseMacStructureCbor(
                context = a.required(0), protected = a.required(1), externalAad = a.required(2), payload = a.required(3)
            )
        }

        fun cborDecode(encoded: ByteArray) = fromCborItem(cborSerializer.decode(encoded))
    }
}

/*
@JsExport
data class ToBeMacedJson(
    val input: CoseMac0InputJson,
    val macStructureAsBase64Url: String,
    val secretAsBase64Url: String,
    val alg: SignatureAlgorithm = SignatureAlgorithm.HMAC_SHA256
) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)
    override fun toCbor() =
        ToBeMacedCbor(
            macStructureBytes = macStructureAsBase64Url.decodeFromBase64Url(),
            secret = secretAsBase64Url.decodeFromBase64Url(),
            alg = alg,
            input = input.toCbor()
        )

    fun coseMacStructure() = CoseMacStructureCbor.Static.fromCborItem(cborSerializer.decode(macStructureAsBase64Url.decodeFromBase64Url())).toJson()

}
*/

/*
@JsExport
data class ToBeMacedCbor(
    val input: CoseMac0InputCbor,
    val macStructureBytes: ByteArray,
    val secret: ByteArray,
    val alg: SignatureAlgorithm = SignatureAlgorithm.HMAC_SHA256
) :
    CborView<ToBeMacedCbor, ToBeMacedJson, CborByteString>(CDDL.bstr) {
    override fun cborBuilder(): CborBuilder<ToBeMacedCbor> = CborBuilder(CborByteString(macStructureBytes), this)

    fun coseMacStructure() = CoseMacStructureCbor.Static.fromCborItem(cborSerializer.decode(macStructureBytes))


    override fun toJson() =
        ToBeMacedJson(
            macStructureAsBase64Url = macStructureBytes.encodeToBase64Url(),
            secretAsBase64Url = secret.encodeToBase64Url(),
            alg = alg,
            input = input.toJson()
        )

}
*/
