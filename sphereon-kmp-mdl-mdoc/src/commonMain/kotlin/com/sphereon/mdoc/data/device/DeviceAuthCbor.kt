package com.sphereon.mdoc.data.device

import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cddl_tstr
import com.sphereon.json.JsonView
import kotlin.js.JsExport

@JsExport
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
data class DeviceAuthCbor(
    val deviceSignature: cddl_tstr, //DeviceSignature FIXME
    val deviceMac: cddl_tstr //DeviceMac FIXME
) : CborView<DeviceAuthCbor, DeviceAuthJson, CborMap<StringLabel, CborItem<*>>>(cddl = CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceAuthCbor> {
        TODO("Not yet implemented")
    }

    override fun toJson(): DeviceAuthJson {
        TODO("Not yet implemented")
    }
}
