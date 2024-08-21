package com.sphereon.json

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
abstract class JsonView {
    abstract fun toJsonString(): String
    fun <T> toJsonDTO() = toJsonDTO<T>(this)
    abstract fun toCbor(): Any
}

expect fun <T> toJsonDTO(subject: JsonView): T


