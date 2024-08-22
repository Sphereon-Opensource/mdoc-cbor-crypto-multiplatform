package com.sphereon.mdoc.tx.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.crypto.cose.COSE_Key
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.json.JsonView
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import com.sphereon.mdoc.data.device.DeviceItemsRequestCbor
import com.sphereon.mdoc.data.device.DeviceItemsRequestJson
import kotlinx.serialization.encodeToString
import oid4vpHandoverFromClientIdAndResponseUri
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
data class ReaderAuthenticationJson(
    val sessionTranscript: SessionTranscriptJson,
    val itemsRequest: DeviceItemsRequestJson
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): ReaderAuthenticationCbor {
        TODO("Not yet implemented")
    }

}

@JsExport
data class ReaderAuthenticationCbor(
    val sessionTranscript: SessionTranscriptCbor,
    val itemsRequest: DeviceItemsRequestCbor
) : CborView<ReaderAuthenticationCbor, ReaderAuthenticationJson, CborArray<AnyCborItem>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<ReaderAuthenticationCbor> {
        return CborArray.Static.builder(this).add(Static.READER_AUTHENTICATION).add(sessionTranscript.toCbor())
            .add(itemsRequest.toCbor()).end()
    }

    override fun toJson(): ReaderAuthenticationJson {
        TODO("Not yet implemented")
    }

    object Static {
        val READER_AUTHENTICATION = CborString("ReaderAuthentication")

        @JsName("fromCborItem")
        fun fromCborItem(a: CborArray<AnyCborItem>): ReaderAuthenticationCbor {
            if (a.required<CborString>(0) != READER_AUTHENTICATION) {
                throw IllegalArgumentException("'ReaderAuthentication' element cannot be null")
            }
            return ReaderAuthenticationCbor(a.required(1), a.required(2))
        }

        @JsName("cborDecode")
        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }

}

@JsExport
data class SessionTranscriptJson(
    val deviceEngagement: DeviceEngagementJson? = null,
    val eReaderKey: CoseKeyJson? = null,
    val handover: HandoverJson<*>
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor() = SessionTranscriptCbor(
        deviceEngagement = deviceEngagement?.toCbor(), eReaderKey = eReaderKey?.toCbor(), handover = when (handover) {
            is OID4VPHandoverJson -> handover.toCbor()
            is NfcHandoverJson -> handover.toCbor()
            is QrHandoverJson -> handover.toCbor()
        }
    )
}

@JsExport
data class SessionTranscriptCbor(
    val deviceEngagement: DeviceEngagementCbor? = null,
    val eReaderKey: COSE_Key? = null,
    val handover: HandoverCbor<*, *>
) : CborView<SessionTranscriptCbor, SessionTranscriptJson, CborArray<AnyCborItem>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<SessionTranscriptCbor> {
        return CborArray.Static.builder(this)
            .add(deviceEngagement?.let { CborByteString.Static.fromCborItem(it.toCbor()) })
            .add(eReaderKey?.let { CborByteString.Static.fromCborItem(it.toCbor()) })
            .add(handover.toCbor())
            .end()
    }

    override fun toJson() =
        SessionTranscriptJson(
            deviceEngagement = deviceEngagement?.toJson(), eReaderKey = eReaderKey?.toJson(), handover = when (handover) {
                is OID4VPHandoverCbor -> handover.toJson()
                is NfcHandoverCbor -> handover.toJson()
                is QrHandoverCbor -> handover.toJson()
            }
        )

    object Static {
        const val DEVICE_ENGAGEMENT = 0
        const val ENGAGEMENT_READER_KEY = 1
        const val HANDOVER = 2

        @JsName("fromCborItem")
        fun fromCborItem(a: CborArray<AnyCborItem>): SessionTranscriptCbor {

            return SessionTranscriptCbor(
                a.optional<ByteArray>(DEVICE_ENGAGEMENT)?.let {
                    DeviceEngagementCbor.Static.fromCborItem(
                        cborSerializer.decode(it)
                    )
                },
                a.optional<ByteArray>(ENGAGEMENT_READER_KEY)?.let { CoseKeyCbor.Static.cborDecode(it) },
                a.required(HANDOVER)
            )

        }

        fun fromOid4vpHandover(handover: OID4VPHandoverCbor): SessionTranscriptCbor = SessionTranscriptCbor(handover = handover)
        fun fromOid4vpClientIdAndResponseUri(
            clientId: String,
            responseUri: String,
            mdocNonce: String = Uuid.v4String(),
            authorizationRequestNonce: String
        ) =
            fromOid4vpHandover(OID4VPHandoverCbor.Static.fromClientIdAndResponseUri(clientId, responseUri, mdocNonce, authorizationRequestNonce))


        @JsName("cborDecode")
        fun cborDecode(data: ByteArray): SessionTranscriptCbor = fromCborItem(cborSerializer.decode(data))
    }

}


@JsExport
sealed class HandoverJson<CborType : HandoverCbor<*, *>> : JsonView()

@JsExport
sealed class HandoverCbor<CborType : Any, JsonType> : CborView<CborType, JsonView, CborArray<AnyCborItem>>(CDDL.list)

@JsExport
class QrHandoverJson : HandoverJson<QrHandoverCbor>() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): QrHandoverCbor {
        return QrHandoverCbor()
    }
}

@JsExport
class QrHandoverCbor : HandoverCbor<QrHandoverCbor, QrHandoverJson>() {
    override fun cborBuilder(): CborBuilder<QrHandoverCbor> {
        return CborArray.Static.builder(this).end()
    }

    override fun toJson(): QrHandoverJson {
        return QrHandoverJson()
    }
}

@JsExport
data class NfcHandoverJson(val handoverSelectMessage: CborByteString?) :
    HandoverJson<NfcHandoverCbor>() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): NfcHandoverCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class NfcHandoverCbor(val handoverSelectMessage: CborByteString, val handoverRequestMessage: CborByteString?) :
    HandoverCbor<NfcHandoverCbor, NfcHandoverJson>() {
    override fun cborBuilder(): CborBuilder<NfcHandoverCbor> =
        CborArray.Static.builder(this)
            .addRequired(handoverSelectMessage)
            .add(handoverRequestMessage)
            .end()

    override fun toJson(): NfcHandoverJson {
        TODO("Not yet implemented")
    }

    object Static {
        const val HANDOVER_SELECT_MESSAGE = 0
        const val HANDOVER_REQUEST_MESSAGE = 1
        fun fromCborItem(a: CborArray<AnyCborItem>) =
            NfcHandoverCbor(a.required(HANDOVER_REQUEST_MESSAGE), a.optional(HANDOVER_SELECT_MESSAGE))

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }
}


@JsExport
data class OID4VPHandoverJson(val clientIdHash: String, val responseUriHash: String, val nonce: String) :
    HandoverJson<OID4VPHandoverCbor>() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor() = OID4VPHandoverCbor(
        clientIdHash = clientIdHash.decodeFrom(Encoding.BASE64URL).toCborByteString(),
        responseUriHash = responseUriHash.decodeFrom(Encoding.BASE64URL).toCborByteString(),
        nonce = nonce.toCborString()
    )

    object Static {
        fun fromClientIdAndResponseUri(
            clientId: String,
            responseUri: String,
            mdocNonce: String = Uuid.v4String(),
            authorizationRequestNonce: String
        ) =
            oid4vpHandoverFromClientIdAndResponseUri(
                clientId = clientId,
                responseUri = responseUri,
                mdocNonce = mdocNonce,
                authorizationRequestNonce = authorizationRequestNonce
            ).toJson()

    }
}

@JsExport
data class OID4VPHandoverCbor(val clientIdHash: CborByteString, val responseUriHash: CborByteString, val nonce: CborString) :
    HandoverCbor<OID4VPHandoverCbor, OID4VPHandoverJson>() {
    override fun cborBuilder(): CborBuilder<OID4VPHandoverCbor> =
        CborArray.Static.builder(this)
            .addRequired(clientIdHash)
            .addRequired(responseUriHash)
            .addRequired(nonce)
            .end()

    override fun toJson(): OID4VPHandoverJson = OID4VPHandoverJson(
        clientIdHash = clientIdHash.encodeTo(Encoding.BASE64URL),
        responseUriHash = responseUriHash.encodeTo(Encoding.BASE64URL),
        nonce = nonce.value
    )


    object Static {
        const val CLIENT_ID_HASH = 0
        const val RESPONSE_URI_HASH = 1
        const val NONCE = 1
        fun fromCborItem(a: CborArray<AnyCborItem>) =
            OID4VPHandoverCbor(a.required(CLIENT_ID_HASH), a.required(RESPONSE_URI_HASH), a.required(NONCE))

        fun fromClientIdAndResponseUri(
            clientId: String,
            responseUri: String,
            mdocNonce: String = Uuid.v4String(),
            authorizationRequestNonce: String
        ) =
            oid4vpHandoverFromClientIdAndResponseUri(
                clientId = clientId,
                responseUri = responseUri,
                mdocNonce = mdocNonce,
                authorizationRequestNonce = authorizationRequestNonce
            )

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }
}
