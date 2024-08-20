package com.sphereon.cbor

@JsExport
actual fun <T> toJsonDTO(subject: JsonView): T = JSON.parse(subject.toJsonString())
