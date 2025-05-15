package com.sphereon.cbor

import kotlin.js.JsExport

@JsExport
class CborTime(value: cddl_time) : CborTagged<cddl_time>(CDDL.time.info!!, CborUInt(value))
