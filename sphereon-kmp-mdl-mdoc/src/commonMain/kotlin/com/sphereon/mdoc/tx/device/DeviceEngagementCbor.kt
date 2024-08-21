package com.sphereon.mdoc.tx.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBool
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborEncodedItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.NumberLabel
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cborViewArrayToCborItem
import com.sphereon.cbor.cddl_uint
import com.sphereon.cbor.toCborUInt
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.numberToKmpLong
import com.sphereon.kmp.toKmpLong
import com.sphereon.mdoc.mdocJsonSerializer
import com.sphereon.mdoc.tx.device.WifiOptionsCbor.Static.CHANNEL_INFO_CHANNEL_NUMBER
import com.sphereon.mdoc.tx.device.WifiOptionsCbor.Static.CHANNEL_INFO_OPERATING_CLASS
import com.sphereon.mdoc.tx.device.WifiOptionsCbor.Static.PASS_PHRASE
import com.sphereon.mdoc.tx.device.WifiOptionsCbor.Static.SUPPORTED_BANDS
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName


@JsExport
data class DeviceEngagementJson(
    val version: String = "1.0",
    val security: DeviceEngagementSecurityCbor,
    val deviceRetrievalMethods: Array<DeviceRetrievalMethodCbor>? = null,
    val serverRetrievalMethod: ServerRetrievalMethodsCbor? = null,
    val protocolInfo: ProtocolInfo? = null,
    val additionalItems: MutableMap<LongKMP, Any>? = mutableMapOf()
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DeviceEngagementCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class DeviceEngagementCbor(
    val version: CborString = CborString("1.0"),
    val security: DeviceEngagementSecurityCbor,
    val deviceRetrievalMethods: Array<DeviceRetrievalMethodCbor>? = null,
    val serverRetrievalMethod: ServerRetrievalMethodsCbor? = null,
    val protocolInfo: ProtocolInfo? = null,
    val additionalItems: CborMap<NumberLabel, AnyCborItem>? = CborMap(mutableMapOf())
) : CborView<DeviceEngagementCbor, DeviceEngagementJson, CborMap<NumberLabel, AnyCborItem>>(CDDL.map) {

    object Static {
        val VERSION = NumberLabel(0)
        val SECURITY = NumberLabel(1)
        val DEVICE_RETRIEVAL_METHODS = NumberLabel(2)
        val SERVER_RETRIEVAL_METHOD = NumberLabel(3)
        val PROTOCOL_INFO = NumberLabel(4)


        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>): DeviceEngagementCbor {
            return DeviceEngagementCbor(
                VERSION.required(m),
                DeviceEngagementSecurityCbor.Static.fromCborItem(SECURITY.required(m)),
                DeviceRetrievalMethodCbor.Static.fromDeviceEngagementCborItem(DEVICE_RETRIEVAL_METHODS.optional(m)),
                SERVER_RETRIEVAL_METHOD.optional(m),
                PROTOCOL_INFO.optional(m),
            )
        }

        fun cborDecode(encodedDeviceEngagement: ByteArray): DeviceEngagementCbor =
            fromCborItem(cborSerializer.decode(encodedDeviceEngagement))
    }

    override fun cborBuilder(): CborBuilder<DeviceEngagementCbor> {
        val mapBuilder = CborMap.Static.builder(this)
            .put(Static.VERSION, version)
            .put(Static.SECURITY, security.toCbor())
            .put(
                Static.DEVICE_RETRIEVAL_METHODS,
                deviceRetrievalMethods?.cborViewArrayToCborItem(),
                true
            )
            .put(Static.SERVER_RETRIEVAL_METHOD, serverRetrievalMethod?.toCbor(), true)
            .put(Static.PROTOCOL_INFO, protocolInfo, true)
        if (additionalItems?.value?.isNotEmpty() == true) {
            additionalItems.value.map { mapBuilder.put(it.key, it.value) }
        }
        return mapBuilder.end()
    }

    override fun toJson(): DeviceEngagementJson {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceEngagementCbor) return false

        if (version != other.version) return false
        if (security != other.security) return false
        if (deviceRetrievalMethods != null) {
            if (other.deviceRetrievalMethods == null) return false
            if (!deviceRetrievalMethods.contentEquals(other.deviceRetrievalMethods)) return false
        } else if (other.deviceRetrievalMethods != null) return false
        if (serverRetrievalMethod != other.serverRetrievalMethod) return false
        if (protocolInfo != other.protocolInfo) return false
        if (additionalItems != other.additionalItems) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + (deviceRetrievalMethods?.contentHashCode() ?: 0)
        result = 31 * result + (serverRetrievalMethod?.hashCode() ?: 0)
        result = 31 * result + (protocolInfo?.hashCode() ?: 0)
        result = 31 * result + (additionalItems?.hashCode() ?: 0)
        return result
    }

}

typealias ProtocolInfo = AnyCborItem


@JsExport
data class ServerRetrievalMethodsJson(val Oidc: ServerRetrievalInfo?, val WebApi: ServerRetrievalInfo?) :
    JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): ServerRetrievalMethodsCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class ServerRetrievalMethodsCbor(val Oidc: ServerRetrievalInfo?, val WebApi: ServerRetrievalInfo?) :
    CborView<ServerRetrievalMethodsCbor, ServerRetrievalMethodsJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<ServerRetrievalMethodsCbor> {
        return CborMap.Static.builder(this)
            .put(Static.OIDC, Oidc?.cborBuilder()?.build(), true)
            .put(Static.WEB_API, WebApi?.cborBuilder()?.build(), true)
            .end()
    }

    override fun toJson(): ServerRetrievalMethodsJson {
        TODO("Not yet implemented")
    }

    object Static {
        val OIDC = StringLabel("Oidc")
        val WEB_API = StringLabel("WebApi")
        fun fromCbor(encodedServerRetrievalMethods: ByteArray) {
            val m: CborMap<StringLabel, AnyCborItem> = Cbor.decode(encodedServerRetrievalMethods)
            ServerRetrievalMethodsCbor(
                OIDC.optional(m),
                WEB_API.optional(m)
            )
        }
    }
}

@JsExport
data class ServerRetrievalInfo(val version: CborUInt, val issuerUrl: CborString, val serverRetrievalToken: CborString) {
    fun cborBuilder(): CborBuilder<ServerRetrievalInfo> {
        return CborArray.Static.builder(this)
            .add(version)
            .add(issuerUrl)
            .add(serverRetrievalToken)
            .end()
    }

    fun toCbor(): ByteArray {
        return cborBuilder().encodedBuild()
    }

    object Static {
        fun fromCbor(serverRetrievalInfo: ByteArray): ServerRetrievalInfo {
            val items: CborArray<AnyCborItem> = Cbor.decode(serverRetrievalInfo)
            return ServerRetrievalInfo(items.required(0), items.required(1), items.required(2))
        }
    }
}

@JsExport
data class DeviceEngagementSecurityJson(val cypherSuite: LongKMP, val eDeviceKeyBytes: CoseKeyJson) :
    JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DeviceEngagementSecurityCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class DeviceEngagementSecurityCbor(val cypherSuite: CborUInt, val eDeviceKeyBytes: CoseKeyCbor) :
    CborView<DeviceEngagementSecurityCbor, DeviceEngagementSecurityJson, CborArray<AnyCborItem>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<DeviceEngagementSecurityCbor> {
        return CborArray.Static.builder(this)
            .add(cypherSuite)
            .add(CborEncodedItem(eDeviceKeyBytes.toCbor(), CborByteString(eDeviceKeyBytes.cborEncode())))
            .end()
    }

    override fun toJson(): DeviceEngagementSecurityJson {
        TODO("Not yet implemented")
    }


    object Static {
        fun fromCborItem(a: CborArray<AnyCborItem>): DeviceEngagementSecurityCbor {
            val ekeyBytes: CborEncodedItem<CborMap<NumberLabel, AnyCborItem>> = a.required(1)
            return DeviceEngagementSecurityCbor(
                a.required(0),
                CoseKeyCbor.Static.fromCborItem(ekeyBytes.cborDecode<CborMap<NumberLabel, AnyCborItem>>())
            )
        }

        fun cborDecode(deviceEngagementSecurity: ByteArray): DeviceEngagementSecurityCbor =
            fromCborItem(cborSerializer.decode(deviceEngagementSecurity))
    }
}

@JsExport
data class DeviceRetrievalMethodJson(
    val type: cddl_uint,
    val version: cddl_uint,
    val retrievalOptions: DeviceRetrievalOptionsCbor
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DeviceRetrievalMethodCbor {
        TODO("Not yet implemented")
    }
}

enum class DeviceRetrievalMethodType(val type: Int) {
    NFC(1), BLE(2), WIFI_WARE(3);

    fun toCborItem(): CborUInt {
        return CborUInt(type.numberToKmpLong())
    }

    object Static {
        fun fromCborItem(item: CborUInt): DeviceRetrievalMethodType {
            return entries.first { it.type == item.value.toInt() }
        }
    }
}

@JsExport
data class DeviceRetrievalMethodCbor(
    val type: CborUInt,
    val version: CborUInt,
    val retrievalOptions: DeviceRetrievalOptionsCbor
) : CborView<DeviceRetrievalMethodCbor, DeviceRetrievalMethodJson, CborArray<AnyCborItem>>(CDDL.list) {

    override fun cborBuilder(): CborBuilder<DeviceRetrievalMethodCbor> {
        return CborArray.Static.builder(this)
            .add(type)
            .add(version)
            .add(retrievalOptions.toCbor())
            .end()
    }


    override fun toJson(): DeviceRetrievalMethodJson {
        TODO("Not yet implemented")
    }

    object Static {
        fun fromDeviceEngagementCborItem(items: CborArray<AnyCborItem>?): Array<DeviceRetrievalMethodCbor>? {
            if (items == null || items.value.isEmpty()) {
                return null
            }
            return items.value.map { fromCborItem(it as CborArray<AnyCborItem>) }.toTypedArray()

        }

        fun fromCborItem(items: CborArray<AnyCborItem>): DeviceRetrievalMethodCbor {
            val type: CborUInt = items.required(0)
            val retrievalOptions = when (DeviceRetrievalMethodType.Static.fromCborItem(type)) {
                DeviceRetrievalMethodType.NFC -> NfcOptionsCbor.Static.fromCborItem(items.required(2))
                DeviceRetrievalMethodType.BLE -> BleOptionsCbor.Static.fromCborItem(items.required(2))
                DeviceRetrievalMethodType.WIFI_WARE -> WifiOptionsCbor.Static.fromCborItem(items.required(2))
                else -> throw IllegalArgumentException("Unknown device retrieval method type received ${type}")
            }
            return DeviceRetrievalMethodCbor(
                type = type,
                version = items.required(1),
                retrievalOptions = retrievalOptions
            )
        }

        fun cborDecode(deviceRetrievalMethod: ByteArray): DeviceRetrievalMethodCbor =
            fromCborItem(Cbor.decode(deviceRetrievalMethod))
    }
}

@JsExport
sealed class DeviceRetrievalOptionsJson : JsonView()

@JsExport
sealed class DeviceRetrievalOptionsCbor :
    CborView<DeviceRetrievalOptionsCbor, DeviceRetrievalOptionsJson, CborMap<NumberLabel, AnyCborItem>>(CDDL.map)

@JsExport
data class WifiOptionsCbor(
    val passPhrase: CborString?,
    val channelInfoOperatingClass: CborUInt? = null,
    val channelInfoChannelNumber: CborUInt? = null,
    val supportedBands: CborByteString? = null
) : DeviceRetrievalOptionsCbor() {

    override fun cborBuilder(): CborBuilder<DeviceRetrievalOptionsCbor> {
        return CborMap.Static.builder(this as DeviceRetrievalOptionsCbor)
            .put(PASS_PHRASE, passPhrase, true)
            .put(CHANNEL_INFO_OPERATING_CLASS, channelInfoOperatingClass, true)
            .put(CHANNEL_INFO_CHANNEL_NUMBER, channelInfoChannelNumber, true)
            .put(SUPPORTED_BANDS, supportedBands, true)
            .end()
    }


    override fun toJson(): DeviceRetrievalOptionsJson {
        TODO("Not yet implemented")
    }

    object Static {
        val PASS_PHRASE = NumberLabel(0)
        val CHANNEL_INFO_OPERATING_CLASS = NumberLabel(1)
        val CHANNEL_INFO_CHANNEL_NUMBER = NumberLabel(2)
        val SUPPORTED_BANDS = NumberLabel(3)

        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>): WifiOptionsCbor {
            return WifiOptionsCbor(
                PASS_PHRASE.optional(m),
                CHANNEL_INFO_OPERATING_CLASS.optional(m),
                CHANNEL_INFO_CHANNEL_NUMBER.optional(m),
                SUPPORTED_BANDS.optional(m)
            )
        }

        fun cborDecode(encodedWifiOptions: ByteArray): WifiOptionsCbor = fromCborItem(Cbor.decode(encodedWifiOptions))
    }
}


@JsExport
data class BleOptionsCbor(
    val peripheralServerMode: CborBool,
    val centralClientMode: CborBool,
    // TODO: Prob number
    val peripheralServerModeUUID: CborByteString? = null,
    // TODO: Prob number
    val centralClientModeUUID: CborByteString? = null,
    val peripheralServerModeDeviceAddress: CborByteString? = null,
) : DeviceRetrievalOptionsCbor() {
    override fun cborBuilder(): CborBuilder<DeviceRetrievalOptionsCbor> {
        return CborMap.Static.builder(this as DeviceRetrievalOptionsCbor)
            .put(Static.PERIPHERAL_SERVER_MODE, peripheralServerMode)
            .put(Static.CENTRAL_CLIENT_MODE, centralClientMode)
            .put(Static.PERIPHERAL_SERVER_MODE_UUID, peripheralServerModeUUID, true)
            .put(Static.CENTRAL_CLIENT_MODE_UUID, centralClientModeUUID, true)
            .put(Static.PERIPHERAL_SERVER_MODE_DEVICE_ADDRESS, peripheralServerModeDeviceAddress, true)
            .end()
    }

    override fun toJson(): DeviceRetrievalOptionsJson {
        TODO("Not yet implemented")
    }

    object Static {
        val PERIPHERAL_SERVER_MODE = NumberLabel(0)
        val CENTRAL_CLIENT_MODE = NumberLabel(1)
        val PERIPHERAL_SERVER_MODE_UUID = NumberLabel(10)
        val CENTRAL_CLIENT_MODE_UUID = NumberLabel(11)
        val PERIPHERAL_SERVER_MODE_DEVICE_ADDRESS = NumberLabel(20)

        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>) = BleOptionsCbor(
            PERIPHERAL_SERVER_MODE.required(m),
            CENTRAL_CLIENT_MODE.required(m),
            PERIPHERAL_SERVER_MODE_UUID.optional(m),
            CENTRAL_CLIENT_MODE_UUID.optional(m),
            PERIPHERAL_SERVER_MODE_DEVICE_ADDRESS.optional(m)
        )

        fun cborDecode(encodedBleOptions: ByteArray) = fromCborItem(Cbor.decode(encodedBleOptions))
    }

}

@JsExport
data class NfcOptionsJson(
    val maxCommandDataFieldLength: cddl_uint,
    val maxResponseDataFieldLength: cddl_uint,
) : JsonView() {
    @JsName("newInstance")
    constructor(
        maxCommandDataFieldLength: Int, // maps to number in JS
        maxResponseDataFieldLength: Int // maps to number in JS
    ) : this(maxCommandDataFieldLength.toKmpLong(), maxResponseDataFieldLength.toKmpLong())

    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): NfcOptionsCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class NfcOptionsCbor(
    val maxCommandDataFieldLength: CborUInt,
    val maxResponseDataFieldLength: CborUInt,
) : DeviceRetrievalOptionsCbor() {
    override fun cborBuilder(): CborBuilder<DeviceRetrievalOptionsCbor> {
        return CborMap.Static.builder(this as DeviceRetrievalOptionsCbor)
            .put(Static.MAX_COMMAND_DATA_FIELD_LENGTH, maxCommandDataFieldLength)
            .put(Static.MAX_RESPONSE_DATA_FIELD_LENGTH, maxResponseDataFieldLength)
            .end()
    }

    override fun toJson(): DeviceRetrievalOptionsJson {
        return NfcOptionsJson(
            maxCommandDataFieldLength.value,
            maxResponseDataFieldLength.value
        ) as DeviceRetrievalOptionsJson
    }

    object Static {
        val MAX_COMMAND_DATA_FIELD_LENGTH = NumberLabel(0)
        val MAX_RESPONSE_DATA_FIELD_LENGTH = NumberLabel(1)
        fun fromSimple(nfcOptions: NfcOptionsJson): NfcOptionsCbor {
            return NfcOptionsCbor(
                nfcOptions.maxCommandDataFieldLength.toCborUInt(),
                nfcOptions.maxResponseDataFieldLength.toCborUInt()
            )
        }

        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>) = NfcOptionsCbor(
            MAX_COMMAND_DATA_FIELD_LENGTH.required(m),
            MAX_RESPONSE_DATA_FIELD_LENGTH.required(m)
        )


        fun fromCbor(encodedNfcOptions: ByteArray) = fromCborItem(Cbor.decode(encodedNfcOptions))
    }
}
