package com.sphereon.cbor.cose

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborNull
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import kotlin.js.JsExport


@JsExport
data class CoseSign1InputJson<JsonType, CborType>(
    val protectedHeader: CoseHeaderJson,

    val unprotectedHeader: CoseHeaderJson?,

    val payload: String?, // hex

) : JsonView<CoseSign1InputCbor<CborType, JsonType>>() {
    override fun toCbor(): CoseSign1InputCbor<CborType, JsonType> = CoseSign1InputCbor(
        protectedHeader = protectedHeader.toCbor(),
        unprotectedHeader = unprotectedHeader?.toCbor(),
        payload = payload?.toCborByteString(),
    )
}

@JsExport
data class CoseSign1Json<JsonType, CborType>(
    val protectedHeader: CoseHeaderJson,

    val unprotectedHeader: CoseHeaderJson?,

    val payload: String?, // hex

    val signature: String // hex
) : JsonView<CoseSign1Cbor<CborType, JsonType>>() {
    override fun toCbor(): CoseSign1Cbor<CborType, JsonType> = CoseSign1Cbor(
        protectedHeader = protectedHeader.toCbor(),
        unprotectedHeader = unprotectedHeader?.toCbor(),
        payload = payload?.toCborByteString(),
        signature = signature.toCborByteString()
    )
}

@JsExport
data class CoseSign1InputCbor<CborType, JsonType>(
    val protectedHeader: CoseHeaderCbor,

    val unprotectedHeader: CoseHeaderCbor?,

    val payload: CborByteString?,
) : CborView<CoseSign1InputCbor<CborType, JsonType>, CoseSign1InputJson<JsonType, CborType>, CborArray<AnyCborItem>>(
    CDDL.list
) {
    companion object {
        fun <CborType, JsonType> fromCborItem(a: CborArray<AnyCborItem>): CoseSign1InputCbor<CborType, JsonType> {
            val protectedHeaderBytes: CborByteString = a.required(0)
            val unprotectedHeaders = a.optional<CborMap<NumberLabel, AnyCborItem>>(1)
            val payloadAvailable = a.value[2].value != null
            return CoseSign1InputCbor(
                CoseHeaderCbor.fromCborItem(protectedHeaderBytes.cborDecode()),
                unprotectedHeaders?.let { CoseHeaderCbor.fromCborItem(it) },
                if (payloadAvailable) a.required(2) else null,
            )
        }

        fun <CborType, JsonType> cborDecode(encoded: ByteArray) =
            fromCborItem<CborType, JsonType>(cborSerializer.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<CoseSign1InputCbor<CborType, JsonType>> {
        return CborArray.builder(this).add(CborByteString(protectedHeader.cborEncode()))
            .add(unprotectedHeader?.toCbor()).add(payload ?: CborNull())
            .end()
    }

    override fun toJson(): CoseSign1InputJson<JsonType, CborType> = CoseSign1InputJson(
        protectedHeader = protectedHeader.toJson(),
        unprotectedHeader = unprotectedHeader?.toJson(),
        payload = payload?.toHexString()
    )

}

@JsExport
data class CoseSign1Cbor<CborType, JsonType>(


    val protectedHeader: CoseHeaderCbor,

    val unprotectedHeader: CoseHeaderCbor?,

    val payload: CborByteString?,

    val signature: CborByteString

) : CborView<CoseSign1Cbor<CborType, JsonType>, CoseSign1Json<JsonType, CborType>, CborArray<AnyCborItem>>(CDDL.list) {
    fun cborDecodePayload(): CborType? {
        return payload?.value?.let { cborSerializer.decode(it) }
    }

    companion object {
        fun <CborType, JsonType> fromCborItem(a: CborArray<AnyCborItem>): CoseSign1Cbor<CborType, JsonType> {
            val protectedHeaderBytes: CborByteString = a.required(0)
            val unprotectedHeaders = a.optional<CborMap<NumberLabel, AnyCborItem>>(1)
            val payloadAvailable = a.value[2].value != null
            return CoseSign1Cbor(
                CoseHeaderCbor.fromCborItem(protectedHeaderBytes.cborDecode()),
                unprotectedHeaders?.let { CoseHeaderCbor.fromCborItem(it) },
                if (payloadAvailable) a.required(2) else null,
                a.required(3)
            )
        }

        fun <CborType, JsonType> cborDecode(encoded: ByteArray) =
            fromCborItem<CborType, JsonType>(cborSerializer.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<CoseSign1Cbor<CborType, JsonType>> {
        return CborArray.builder(this).add(CborByteString(protectedHeader.cborEncode()))
            .add(unprotectedHeader?.toCbor()).add(payload ?: CborNull())
            .add(signature)
            .end()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toJson(): CoseSign1Json<JsonType, CborType> = CoseSign1Json(
        protectedHeader = protectedHeader.toJson(),
        unprotectedHeader = unprotectedHeader?.toJson(),
        payload = payload?.toHexString(),
        signature = signature.toHexString()
    )


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoseSign1Cbor<*, *>) return false

        if (protectedHeader != other.protectedHeader) return false
        if (unprotectedHeader != other.unprotectedHeader) return false
        if (payload != other.payload) return false
        if (signature != other.signature) return false

        return true
    }

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

typealias COSE_Sign1<CborType, JsonType> = CoseSign1Cbor<CborType, JsonType>
