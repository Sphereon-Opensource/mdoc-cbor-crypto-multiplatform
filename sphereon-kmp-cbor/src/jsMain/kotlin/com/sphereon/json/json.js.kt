package com.sphereon.json



@JsExport
actual fun <T> toJsonDTO(subject: HasToJsonString): T = JSON.parse(subject.toJsonString())
