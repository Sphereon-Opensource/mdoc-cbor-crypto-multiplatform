package com.sphereon.mdoc.data

import com.sphereon.cbor.cddl_tstr
import kotlin.js.JsExport

@JsExport
data class DeviceAuth (
    val deviceSignature: cddl_tstr, //DeviceSignature FIXME
    val deviceMac: cddl_tstr //DeviceMac FIXME
)
