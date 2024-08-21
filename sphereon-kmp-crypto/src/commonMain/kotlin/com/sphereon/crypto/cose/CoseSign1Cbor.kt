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
import com.sphereon.json.JsonView
import com.sphereon.cbor.NumberLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.cryptoJsonSerializer
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFromHex
import com.sphereon.kmp.encodeTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName


@JsExport
@Serializable
data class CoseSign1InputJson(
    val protectedHeader: CoseHeaderJson,

    val unprotectedHeader: CoseHeaderJson?,

    val payload: String?, // base64url

) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)


    override fun toCbor(): CoseSign1InputCbor = CoseSign1InputCbor(
        protectedHeader = protectedHeader.toCbor(),
        unprotectedHeader = unprotectedHeader?.toCbor(),
        payload = payload?.toCborByteString(),
    )
}

@JsExport
@Serializable
data class CoseSign1Json(
    val protectedHeader: CoseHeaderJson,

    val unprotectedHeader: CoseHeaderJson?,

    val payload: String?, // hex

    val signature: String // hex
) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)
    override fun toCbor(): CoseSign1Cbor<Any> = CoseSign1Cbor<Any>(
        protectedHeader = protectedHeader.toCbor(),
        unprotectedHeader = unprotectedHeader?.toCbor(),
        payload = payload?.toCborByteString(),
        signature = signature.toCborByteString()
    )

    object Static {
        @JsName("fromDTO")
        fun fromDTO(dto: CoseSign1Json) = CoseSign1Json(
            protectedHeader = dto.protectedHeader,
            unprotectedHeader = dto.unprotectedHeader,
            payload = dto.payload,
            signature = dto.signature
        )
    }
}

@JsExport
data class CoseSign1InputCbor(
    val protectedHeader: CoseHeaderCbor,

    val unprotectedHeader: CoseHeaderCbor?,

    val payload: CborByteString?,
) : CborView<CoseSign1InputCbor, CoseSign1InputJson, CborArray<AnyCborItem>>(
    CDDL.list
) {
    object Static {
        @JsName("fromCborItem")
        fun fromCborItem(a: CborArray<AnyCborItem>): CoseSign1InputCbor {
            val protectedHeaderBytes: CborByteString = a.required(0)
            val unprotectedHeaders = a.optional<CborMap<NumberLabel, AnyCborItem>>(1)
            val payloadAvailable = a.value[2].value != null
            return CoseSign1InputCbor(
                CoseHeaderCbor.Static.fromCborItem(protectedHeaderBytes.cborDecode()),
                unprotectedHeaders?.let { CoseHeaderCbor.Static.fromCborItem(it) },
                if (payloadAvailable) a.required(2) else null,
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray) =
            fromCborItem(cborSerializer.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<CoseSign1InputCbor> {
        return CborArray.Static.builder(this).add(CborByteString(protectedHeader.cborEncode()))
            .add(unprotectedHeader?.toCbor()).add(payload ?: CborNull())
            .end()
    }

    override fun toJson(): CoseSign1InputJson = CoseSign1InputJson(
        protectedHeader = protectedHeader.toJson(),
        unprotectedHeader = unprotectedHeader?.toJson(),
        payload = payload?.encodeTo(Encoding.BASE64URL)
    )

}


@JsExport
data class CoseSign1Cbor<CborType>(


    val protectedHeader: CoseHeaderCbor,

    val unprotectedHeader: CoseHeaderCbor?,

    val payload: CborByteString?,

    val signature: CborByteString

) : CborView<CoseSign1Cbor<CborType>, CoseSign1Json, CborArray<AnyCborItem>>(CDDL.list) {
    fun cborDecodePayload(): CborType? {
        return payload?.value?.let { cborSerializer.decode(it) }
    }

    fun toSignature1Structure() = CoseSignatureStructureCbor(
        structure = SigStructure.Signature1.toCbor(),
        externalAad = CborByteString(byteArrayOf()),
        bodyProtected = CborByteString(protectedHeader.cborEncode()),
        payload = payload ?: throw IllegalArgumentException("No payload present")
    )

    @JsName("toBeSignedCbor")
    fun toBeSignedCbor(key: ICoseKeyCbor?, alg: CoseAlgorithm?) =
        ToBeSignedCbor(value = toSignature1Structure().cborEncode().toCborByteString(), key = key, alg = alg)

    @JsName("toBeSignedJson")
    fun toBeSignedJson(key: ICoseKeyJson?, alg: CoseAlgorithm?) =
        ToBeSignedJson(hexValue = toSignature1Structure().cborEncode().encodeTo(Encoding.HEX), key = key, alg = alg)

    object Static {
        @JsName("fromCborItem")
        fun <CborType> fromCborItem(a: CborArray<AnyCborItem>): CoseSign1Cbor<CborType> {
            val protectedHeaderBytes: CborByteString = a.required(0)
            val unprotectedHeaders = a.optional<CborMap<NumberLabel, AnyCborItem>>(1)
            val payloadAvailable = a.value[2].value != null
            return CoseSign1Cbor<CborType>(
                CoseHeaderCbor.Static.fromCborItem(protectedHeaderBytes.cborDecode()),
                unprotectedHeaders?.let { CoseHeaderCbor.Static.fromCborItem(it) },
                if (payloadAvailable) a.required(2) else null,
                a.required(3)
            )
        }

        @JsName("cborDecode")
        fun <CborType> cborDecode(encoded: ByteArray) =
            fromCborItem<CborType>(cborSerializer.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<CoseSign1Cbor<CborType>> {
        return CborArray.Static.builder(this).add(CborByteString(protectedHeader.cborEncode()))
            .add(unprotectedHeader?.toCbor()).add(payload ?: CborNull())
            .add(signature)
            .end()
    }

    override fun toJson(): CoseSign1Json = CoseSign1Json(
        protectedHeader = protectedHeader.toJson(),
        unprotectedHeader = unprotectedHeader?.toJson(),
        payload = payload?.encodeTo(Encoding.BASE64URL),
        signature = signature.encodeTo(Encoding.BASE64URL)
    )


    override fun hashCode(): Int {
        var result = protectedHeader.hashCode()
        result = 31 * result + (unprotectedHeader?.hashCode() ?: 0)
        result = 31 * result + (payload?.hashCode() ?: 0)
        result = 31 * result + signature.hashCode()
        return result
    }

    override fun toString(): String {
        return "CoseSign1Cbor(protectedHeader=$protectedHeader, unprotectedHeader=$unprotectedHeader, payload=$payload, signature=$signature)"
    }


}

typealias COSE_Sign1<CborType> = CoseSign1Cbor<CborType>


@JsExport
sealed class SigStructure(val value: String) {
    object Signature : SigStructure("Signature")
    object Signature1 : SigStructure("Signature1")
    object CounterSignature : SigStructure("CounterSignature")

    fun toCbor() = CborString(value)

    object Static {
        val asList = listOf(Signature, Signature1, CounterSignature)
        @JsName("fromValue")
        fun fromValue(value: String) = asList.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Unknown signature $value")
    }
}

@JsExport
data class CoseSignatureStructureJson(
    val structure: SigStructure = SigStructure.Signature1,
    val bodyProtected: String,
    val signProtected: String? = null,
    val externalAad: String? = null, // todo: "" instead of null?
    val payload: String
) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)
    override fun toCbor(): CoseSignatureStructureCbor = throw NotImplementedError("CoseSignatureStructure Json to Cbor is not yet implemented")
}

@JsExport
data class CoseSignatureStructureCbor(
    val structure: CborString = CborString(SigStructure.Signature1.value),
    val bodyProtected: CborByteString,
    val signProtected: CborByteString? = null,
    val externalAad: CborByteString = CborByteString(byteArrayOf()),
    val payload: CborByteString
) : CborView<CoseSignatureStructureCbor, CoseSignatureStructureJson, CborArray<AnyCborItem>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<CoseSignatureStructureCbor> =
        CborArray.Static.builder(this).addRequired(structure).addRequired(bodyProtected).add(signProtected).addRequired(externalAad).addRequired(payload)
            .end()

    @JsName("toBeSigned")
    fun toBeSigned(key: ICoseKeyCbor?, alg: CoseAlgorithm?) = ToBeSignedCbor(value = CborByteString(toCbor().cborEncode()), key = key, alg = alg)
    @JsName("toBeSignedJson")
    fun toBeSignedJson(key: ICoseKeyJson?, alg: CoseAlgorithm?) =
        ToBeSignedJson(hexValue = toCbor().cborEncode().encodeTo(Encoding.HEX), key = key, alg = alg)

    override fun toJson(): CoseSignatureStructureJson = CoseSignatureStructureJson(
        structure = SigStructure.Static.fromValue(structure.value),
        bodyProtected = bodyProtected.encodeTo(Encoding.BASE64URL),
        signProtected = signProtected?.encodeTo(Encoding.BASE64URL),
        externalAad = externalAad.encodeTo(Encoding.BASE64URL),
        payload = payload.encodeTo(Encoding.BASE64URL)
    )

    object Static {
        @JsName("fromCborItem")
        fun fromCborItem(a: CborArray<AnyCborItem>): CoseSignatureStructureCbor {

            if (a.value.size == 4) {
                return CoseSignatureStructureCbor(
                    structure = a.required(0),
                    bodyProtected = a.required(1),
                    externalAad = a.required(2),
                    payload = a.required(3)
                )
            }
            return CoseSignatureStructureCbor(
                structure = a.required(0),
                bodyProtected = a.required(1),
                signProtected = a.optional(2),
                externalAad = a.required(3),
                payload = a.required(4)
            )
        }

        fun cborDecode(encoded: ByteArray) =
            fromCborItem(cborSerializer.decode(encoded))
    }
}

@JsExport
data class ToBeSignedJson(val hexValue: String, val key: ICoseKeyJson?, val alg: CoseAlgorithm?) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)
    override fun toCbor() =
        ToBeSignedCbor(hexValue.decodeFromHex().toCborByteString(), key = key?.let { CoseKeyJson.Static.fromDTO(it).toCbor() }, alg = alg)

}

@JsExport
data class ToBeSignedCbor(val value: CborByteString, val key: ICoseKeyCbor?, val alg: CoseAlgorithm?) :
    CborView<ToBeSignedCbor, ToBeSignedJson, CborByteString>(CDDL.bstr) {
    override fun cborBuilder(): CborBuilder<ToBeSignedCbor> = CborBuilder(value, this)


    override fun toJson() = ToBeSignedJson(hexValue = value.encodeTo(Encoding.HEX), key = key?.let { CoseKeyCbor.Static.fromDTO(it).toJson() }, alg = alg)

}
