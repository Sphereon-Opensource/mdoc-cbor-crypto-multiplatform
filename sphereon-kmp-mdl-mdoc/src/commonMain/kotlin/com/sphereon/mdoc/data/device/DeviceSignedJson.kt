package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.cbor.StringLabel
import com.sphereon.json.JsonView
import com.sphereon.mdoc.data.DeviceSignedItemsCbor
import com.sphereon.mdoc.data.DeviceSignedItemsJson
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.jvm.Transient

@JsExport
@Serializable
data class DeviceSignedJson (
    val nameSpaces: DeviceSignedItemsJson, val deviceAuth: DeviceAuthJson
): JsonView() {
    override fun toJsonString(): String {
        TODO("Not yet implemented")
    }

    override fun toCbor(): Any {
        TODO("Not yet implemented")
    }
}

@JsExport
data class DeviceSignedCbor (
    val nameSpaces: DeviceSignedItemsCbor, val deviceAuth: DeviceAuthCbor
): CborView<DeviceSignedCbor, DeviceSignedJson, CborMap<StringLabel, AnyCborItem>>(cddl = CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceSignedCbor> {
        TODO("Not yet implemented")
    }

    override fun toJson(): DeviceSignedJson {
        TODO("Not yet implemented")
    }
}
