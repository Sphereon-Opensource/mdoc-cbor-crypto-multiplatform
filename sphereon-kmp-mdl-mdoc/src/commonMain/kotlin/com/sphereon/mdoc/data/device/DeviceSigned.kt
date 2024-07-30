package com.sphereon.mdoc.data.device

import com.sphereon.mdoc.data.DeviceSignedItems
import kotlin.js.JsExport

@JsExport
data class DeviceSigned (
    val nameSpaces: DeviceSignedItems, val testREMOVE: ULong? = null
)
