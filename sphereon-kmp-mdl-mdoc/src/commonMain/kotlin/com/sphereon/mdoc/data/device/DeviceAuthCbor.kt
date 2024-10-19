package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_tstr
import com.sphereon.cbor.toCborString
import com.sphereon.crypto.CryptoService
import com.sphereon.crypto.ICoseCryptoService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.cose.COSE_Sign1
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.json.JsonView
import com.sphereon.kmp.Uuid
import com.sphereon.mdoc.data.DeviceNameSpacesCbor
import com.sphereon.mdoc.data.DeviceNameSpacesJson
import com.sphereon.mdoc.data.DeviceSignedItemsCbor
import com.sphereon.mdoc.data.DocType
import com.sphereon.mdoc.data.mso.KeyInfoCbor
import com.sphereon.mdoc.tx.device.SessionTranscriptCbor
import com.sphereon.mdoc.tx.device.SessionTranscriptJson
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@Serializable
data class DeviceAuthJson(
    val deviceSignature: cddl_tstr, //DeviceSignature FIXME
    val deviceMac: cddl_tstr //DeviceMac FIXME
) : JsonView() {
    override fun toJsonString(): String {
        TODO("Not yet implemented")
    }

    override fun toCbor(): Any {
        TODO("Not yet implemented")
    }
}

@JsExport
enum class DeviceAuthType {
    SIGNATURE, MAC
}

@JsExport
data class DeviceAuthCbor(
    val deviceSignature: COSE_Sign1<DeviceAuthenticationCbor>? = null,
    val deviceMac: cddl_tstr? = null, //DeviceMac FIXME
) : CborView<DeviceAuthCbor, DeviceAuthJson, CborMap<StringLabel, CborItem<*>>>(cddl = CDDL.map) {
    init {
        assertValidState()
    }

    override fun cborBuilder(): CborBuilder<DeviceAuthCbor> {
        assertValidState()
        val builder = CborMap.Static.builder(this)
        if (deviceSignature !== null) {
            builder.put(Static.DEVICE_SIGNATURE, deviceSignature.toCbor())
        } else {
            builder.put(Static.DEVICE_MAC, deviceMac?.toCborString() ?: throw IllegalStateException("Device MAC should not be null"))
        }
        return builder.end()
    }

    fun getAuthType(): DeviceAuthType {
        assertValidState()
        return if (deviceSignature != null) DeviceAuthType.SIGNATURE else DeviceAuthType.MAC
    }

    object Static {
        val DEVICE_SIGNATURE = StringLabel("deviceSignature")
        val DEVICE_MAC = StringLabel("deviceMac")

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DeviceAuthCbor {
            val deviceAuth = DeviceAuthCbor(
                deviceMac = DEVICE_MAC.optional(m),
                deviceSignature = DEVICE_SIGNATURE.optional(m)
            )
            return deviceAuth
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray): DeviceAuthCbor = fromCborItem(cborSerializer.decode(encoded))

       /* suspend fun fromItems(items: DeviceAuthenticationCbor, mode: DeviceAuthType = DeviceAuthType.MAC, deviceKeyInfo: IKeyInfo<ICoseKeyCbor>, coseCryptoService
                      : ICoseCryptoService = CryptoService.COSE): DeviceAuthCbor  {
            val deviceSignature = if (mode == DeviceAuthType.SIGNATURE) {
                coseCryptoService.sign1<DeviceAuthenticationCbor>(input = items, keyInfo = deviceKeyInfo)
            }

        }*/
    }

    private fun assertValidState() {
        if (this.deviceSignature === null && this.deviceMac === null) {
            throw IllegalStateException("Either a device signature or MAC should be present")
        } else if (this.deviceMac !== null && this.deviceSignature !== null) {
            throw IllegalStateException("Cannot have both a device signature and MAC at the same time")
        } else if (this.deviceMac !== null) {
            throw NotImplementedError("Device MAC is not implemented yet. Only signatures supported for now")
        }
    }

    override fun toJson(): DeviceAuthJson {
        TODO("Not yet implemented")
    }

}


@JsExport
data class DeviceAuthenticationCbor(
    val sessionTranscript: SessionTranscriptCbor,
    val docType: DocType,
    val deviceNamespaces: DeviceNameSpacesCbor

) : CborView<DeviceAuthenticationCbor, DeviceAuthenticationJson, CborArray<CborItem<*>>>(cddl = CDDL.list) {
    override fun cborBuilder(): CborBuilder<DeviceAuthenticationCbor> {
        return CborArray.Static.builder(this).addString("DeviceAuthentication").addCborArray(sessionTranscript.toCbor()).add(docType)
            .add(deviceNamespaces).end()
    }

    override fun toJson(): DeviceAuthenticationJson {
        TODO("Not yet implemented")
    }


    object Static {
        fun fromOid4vp(
            clientId: String,
            responseUri: String,
            mdocNonce: String = Uuid.v4String(),
            authorizationRequestNonce: String,
            docType: String,
            deviceNamespaces: DeviceNameSpacesCbor
        ): DeviceAuthenticationCbor {
            val sessionTranscript = SessionTranscriptCbor.Static.fromOid4vpClientIdAndResponseUri(
                clientId = clientId,
                responseUri = responseUri,
                mdocNonce = mdocNonce,
                authorizationRequestNonce = authorizationRequestNonce
            )
            return DeviceAuthenticationCbor(
                sessionTranscript = sessionTranscript,
                docType = docType.toCborString(),
                deviceNamespaces = deviceNamespaces
            )
        }
    }
}


@JsExport
data class DeviceAuthenticationJson(val sessionTranscript: SessionTranscriptJson,
                                    val docType: String,
                                    val deviceNamespaces: DeviceNameSpacesJson) : JsonView() {
    override fun toCbor(): Any {
        TODO("Not yet implemented")
    }

    override fun toJsonString(): String {
        TODO("Not yet implemented")
    }

}
