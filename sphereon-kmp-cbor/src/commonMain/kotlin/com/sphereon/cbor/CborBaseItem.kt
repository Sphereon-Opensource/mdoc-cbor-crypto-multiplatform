package com.sphereon.cbor

import kotlin.js.JsExport

@JsExport
sealed class CborBaseItem(
    val cddl: CDDLType
)
