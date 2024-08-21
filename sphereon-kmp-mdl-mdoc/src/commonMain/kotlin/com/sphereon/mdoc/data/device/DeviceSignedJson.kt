package com.sphereon.mdoc.data.device

import com.sphereon.mdoc.data.DeviceSignedItemsCbor
import com.sphereon.mdoc.data.DeviceSignedItemsJson
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.jvm.Transient

@JsExport
@Serializable
data class DeviceSignedJson (
    val nameSpaces: DeviceSignedItemsJson, val testREMOVE: ULong? = null
)
@JsExport
data class DeviceSignedCbor (
    val nameSpaces: DeviceSignedItemsCbor, val deviceAuth: DeviceAuthCbor
)
