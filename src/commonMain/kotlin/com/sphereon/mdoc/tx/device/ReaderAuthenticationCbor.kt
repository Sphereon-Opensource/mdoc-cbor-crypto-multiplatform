package com.sphereon.mdoc.tx.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cose.COSE_Key
import com.sphereon.mdoc.data.device.DeviceItemsRequestCbor
import com.sphereon.mdoc.data.device.DeviceItemsRequestJson
import kotlin.js.JsExport

@JsExport
data class ReaderAuthenticationJson(
    val sessionTranscript: SessionTranscriptJson,
    val itemsRequest: DeviceItemsRequestJson
) : JsonView<ReaderAuthenticationCbor>() {
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
        return CborArray.builder(this).add(READER_AUTHENTICATION).add(sessionTranscript.toCborItem())
            .add(itemsRequest.toCborItem()).end()
    }

    override fun toJson(): ReaderAuthenticationJson {
        TODO("Not yet implemented")
    }

    companion object {
        val READER_AUTHENTICATION = CborString("ReaderAuthentication")

        fun fromCborItem(a: CborArray<AnyCborItem>): ReaderAuthenticationCbor {
            if (a.required<CborString>(0) != READER_AUTHENTICATION) {
                throw IllegalArgumentException("'ReaderAuthentication' element cannot be null")
            }
            return ReaderAuthenticationCbor(a.required(1), a.required(2))
        }

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }

}

data class SessionTranscriptJson(
    val deviceEngagement: DeviceEngagementJson,
    val eReaderKey: COSE_Key,
    val handover: HandoverJson<*>
) : JsonView<SessionTranscriptCbor>() {
    override fun toCbor(): SessionTranscriptCbor {
        TODO("Not yet implemented")
    }
}


data class SessionTranscriptCbor(
    val deviceEngagement: DeviceEngagementCbor,
    val eReaderKey: COSE_Key,
    val handover: HandoverCbor<*, *>
) : CborView<SessionTranscriptCbor, SessionTranscriptJson, CborArray<AnyCborItem>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<SessionTranscriptCbor> {
        return CborArray.builder(this)
            .add(CborByteString.fromCborItem(deviceEngagement.toCborItem()))
            .add(CborByteString.fromCborItem(eReaderKey.toCborItem()))
            .add(handover.toCborItem())
            .end()
    }

    override fun toJson(): SessionTranscriptJson {
        TODO("Not yet implemented")
    }

    companion object {
        const val DEVICE_ENGAGEMENT = 0
        const val ENGAGEMENT_READER_KEY = 1
        const val HANDOVER = 2

        fun fromCborItem(a: CborArray<AnyCborItem>) = SessionTranscriptCbor(
            DeviceEngagementCbor.fromCborItem(
                cborSerializer.decode(a.required(DEVICE_ENGAGEMENT))
            ), COSE_Key.cborDecode(a.required(ENGAGEMENT_READER_KEY)), a.required(HANDOVER)
        )

        fun cborDecode(data: ByteArray): SessionTranscriptCbor = fromCborItem(cborSerializer.decode(data))
    }

}


sealed class HandoverJson<CborType> : JsonView<CborType>()

sealed class HandoverCbor<CborType, JsonType> : CborView<CborType, JsonView<CborType>, CborArray<AnyCborItem>>(CDDL.list)
class QrHandoverJson : HandoverJson<QrHandoverCbor>() {
    override fun toCbor(): QrHandoverCbor {
        return QrHandoverCbor()
    }
}

class QrHandoverCbor : HandoverCbor<QrHandoverCbor, QrHandoverJson>() {
    override fun cborBuilder(): CborBuilder<QrHandoverCbor> {
        return CborArray.builder(this).end()
    }

    override fun toJson(): JsonView<QrHandoverCbor> {
        return QrHandoverJson()
    }
}

data class NfcHandoverSimple(val handoverSelectMessage: CborByteString?) :
    HandoverJson<NfcHandoverCbor>() {
    override fun toCbor(): NfcHandoverCbor {
        TODO("Not yet implemented")
    }
}

data class NfcHandoverCbor(val handoverSelectMessage: CborByteString, val handoverRequestMessage: CborByteString?) :
    HandoverCbor<NfcHandoverCbor, NfcHandoverSimple>() {
    override fun cborBuilder(): CborBuilder<NfcHandoverCbor> =
        CborArray.builder(this)
            .addRequired(handoverSelectMessage)
            .add(handoverRequestMessage)
            .end()

    override fun toJson(): NfcHandoverSimple {
        TODO("Not yet implemented")
    }

    companion object {
        const val HANDOVER_SELECT_MESSAGE = 0
        const val HANDOVER_REQUEST_MESSAGE = 1
        fun fromCborItem(a: CborArray<AnyCborItem>) =
            NfcHandoverCbor(a.required(HANDOVER_REQUEST_MESSAGE), a.optional(HANDOVER_SELECT_MESSAGE))

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }
}
