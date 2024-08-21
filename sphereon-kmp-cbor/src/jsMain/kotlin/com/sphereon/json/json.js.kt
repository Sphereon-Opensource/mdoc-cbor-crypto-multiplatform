package com.sphereon.json

@JsExport
actual fun <T> toJsonDTO(subject: JsonView): T = JSON.parse(subject.toJsonString())
