package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.cbor.StringLabel
import com.sphereon.json.JsonView
import com.sphereon.mdoc.data.DeviceNameSpacesCbor
import com.sphereon.mdoc.data.DeviceSignedItemsJson
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class DeviceSignedJson(
    val nameSpaces: DeviceSignedItemsJson, val deviceAuth: DeviceAuthJson
) : JsonView() {
    override fun toJsonString(): String {
        TODO("Not yet implemented")
    }

    override fun toCbor(): Any {
        TODO("Not yet implemented")
    }
}

/**
 * Device Signed are essentially self-asserted claims/dataElements, contrary to issuer signed items, which are externally asserted
 */
@JsExport
data class DeviceSignedCbor(
    val nameSpaces: DeviceNameSpacesCbor = DeviceNameSpacesCbor(), val deviceAuth: DeviceAuthCbor
) : CborView<DeviceSignedCbor, DeviceSignedJson, CborMap<StringLabel, AnyCborItem>>(cddl = CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceSignedCbor> {
        return CborMap.Static.builder(this).put(Static.NAME_SPACES, this.nameSpaces.cborEncode()).put(Static.DEVICE_AUTH, this.deviceAuth.toCbor())
            .end()
    }

    override fun toJson(): DeviceSignedJson {
        TODO("Not yet implemented")
    }


    object Static {
        const val NAME_SPACES = "nameSpaces"
        const val DEVICE_AUTH = "deviceAuth"


    }
}
