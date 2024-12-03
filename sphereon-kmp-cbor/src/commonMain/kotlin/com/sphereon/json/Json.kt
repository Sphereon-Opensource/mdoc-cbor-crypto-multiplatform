package com.sphereon.json

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
interface HasToJsonString {
    fun toJsonString(): String
}

@JsExport
interface HasToJsonDTO {
    fun <T> toJsonDTO(): T
}

@Serializable
@JsExport
abstract class JsonView: HasToJsonString, HasToJsonDTO {
    override fun <T> toJsonDTO() = toJsonDTO<T>(this)
    abstract fun toCbor(): Any
}

expect fun <T> toJsonDTO(subject: HasToJsonString): T


