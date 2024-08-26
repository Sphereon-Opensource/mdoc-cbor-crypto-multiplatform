package com.sphereon.json

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
interface HasToJsonString {
    fun toJsonString(): String
}

@Serializable
@JsExport
abstract class JsonView: HasToJsonString {
    fun <T> toJsonDTO() = toJsonDTO<T>(this)
    abstract fun toCbor(): Any
}

expect fun <T> toJsonDTO(subject: HasToJsonString): T


