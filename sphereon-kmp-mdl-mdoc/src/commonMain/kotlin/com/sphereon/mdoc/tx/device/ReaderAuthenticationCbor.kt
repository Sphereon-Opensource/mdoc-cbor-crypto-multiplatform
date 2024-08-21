package com.sphereon.mdoc.tx.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.crypto.cose.COSE_Key
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.mdoc.data.device.DeviceItemsRequestCbor
import com.sphereon.mdoc.data.device.DeviceItemsRequestJson
import kotlinx.serialization.encodeToString
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
    val deviceEngagement: DeviceEngagementJson,
    val eReaderKey: COSE_Key?,
    val handover: HandoverJson<*>?
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): SessionTranscriptCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class SessionTranscriptCbor(
    val deviceEngagement: DeviceEngagementCbor,
    val eReaderKey: COSE_Key?,
    val handover: HandoverCbor<*, *>?
) : CborView<SessionTranscriptCbor, SessionTranscriptJson, CborArray<AnyCborItem>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<SessionTranscriptCbor> {
        return CborArray.Static.builder(this)
            .add(CborByteString.Static.fromCborItem(deviceEngagement.toCbor()))
            .add(eReaderKey?.let { CborByteString.Static.fromCborItem(it.toCbor()) })
            .add(handover?.toCbor())
            .end()
    }

    override fun toJson(): SessionTranscriptJson {
        TODO("Not yet implemented")
    }

    object Static {
        const val DEVICE_ENGAGEMENT = 0
        const val ENGAGEMENT_READER_KEY = 1
        const val HANDOVER = 2

        @JsName("fromCborItem")
        fun fromCborItem(a: CborArray<AnyCborItem>) = SessionTranscriptCbor(
            DeviceEngagementCbor.Static.fromCborItem(
                cborSerializer.decode(a.required(DEVICE_ENGAGEMENT))
            ), CoseKeyCbor.Static.cborDecode(a.required(ENGAGEMENT_READER_KEY)), a.required(HANDOVER)
        )

        @JsName("cborDecode")
        fun cborDecode(data: ByteArray): SessionTranscriptCbor = fromCborItem(cborSerializer.decode(data))
    }

}


@JsExport
sealed class HandoverJson<CborType : Any> : JsonView()

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

    override fun toJson(): JsonView {
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
data class OID4VPHandoverJson(val clientIdHash: String, val responseUriHash: String) :
    HandoverJson<OID4VPHandoverCbor>() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): OID4VPHandoverCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class OID4VPHandoverCbor(val clientIdHash: CborByteString, val responseUriHash: CborByteString) :
    HandoverCbor<OID4VPHandoverCbor, OID4VPHandoverJson>() {
    override fun cborBuilder(): CborBuilder<OID4VPHandoverCbor> =
        CborArray.Static.builder(this)
            .addRequired(clientIdHash)
            .addRequired(responseUriHash)
            .end()

    override fun toJson(): NfcHandoverJson {
        TODO()
    }

    object Static {
        const val CLIENT_ID_HASH = 0
        const val RESPONSE_URI_HASH = 1
        fun fromCborItem(a: CborArray<AnyCborItem>) =
            NfcHandoverCbor(a.required(CLIENT_ID_HASH), a.required(RESPONSE_URI_HASH))

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }
}
