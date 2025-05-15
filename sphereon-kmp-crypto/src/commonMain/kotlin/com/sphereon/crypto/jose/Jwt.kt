@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.crypto.jose

import com.sphereon.json.cryptoJsonSerializer
import com.sphereon.kmp.mergeJsonElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@Serializable
data class Jwt(val header: JsonObject, val payload: JsonObject) {
    @JsName("from")
    constructor(header: JwtHeader, payload: JwtPayload) : this(header.underlying, payload.underlying)


    object Static {
        fun from(header: JwtHeader, payload: JwtPayload): Jwt = Jwt(header.underlying, JsonObject(mapOf()))
    }

    fun getHeaderObject() = JwtHeader(header)
    fun getPayloadObject() = JwtPayload(payload)

    fun toJson(): JsonObject = JsonObject(mapOf(Pair("header", header), Pair("payload", payload)))
    fun toJsonString() = cryptoJsonSerializer.encodeToString(toJson())
}

@JsExport
class JwtHeader(underlying: JsonObject = JsonObject(mutableMapOf())) : JwtBase(underlying) {
    @Transient
    var kid: String?
        set(value) {
            putString("kid", value)
        }
        get() = get("kid")?.jsonPrimitive?.contentOrNull

    @Transient
    var x5c: Array<String>?
        set(value) {
            put("x5c", value?.let { JsonArray(it.map { der -> JsonPrimitive(der) }) })
        }
        get() = get("x5c")?.jsonArray?.map { it.jsonPrimitive.content }?.toTypedArray()
    var jwk: Jwk?
        set(value) {
            put("jwk", value?.toJsonObject())
        }
        get() = get("jwk")?.let { Jwk.Static.fromJsonObject(it.jsonObject) }
    var epk: Jwk?
        set(value) {
            put("epk", value?.toJsonObject())
        }
        get() = get("epk")?.let { Jwk.Static.fromJsonObject(it.jsonObject) }

    @Transient
    var apu: String?
        set(value) {
            putString("apu", value)
        }
        get() = get("apu")?.jsonPrimitive?.contentOrNull

    @Transient
    var apv: String?
        set(value) {
            putString("apv", value)
        }
        get() = get("apv")?.jsonPrimitive?.contentOrNull
}

@JsExport
class JwtPayload(
    underlying: JsonObject = JsonObject(mutableMapOf())
) : JwtBase(underlying) {
    @Transient
    var iss: String?
        set(value) {
            putString("iss", value)
        }
        get() = get("iss")?.jsonPrimitive?.contentOrNull

    @Transient
    var sub: String?
        set(value) {
            putString("sub", value)
        }
        get() = get("sub")?.jsonPrimitive?.contentOrNull

    //    var aud: Array<String>? = null // TODO:could be a string or array of string
    var exp: Int?
        set(value) {
            putNumber("exp", value)
        }
        get() = get("exp")?.jsonPrimitive?.int

    var nbf: Int?
        set(value) {
            putNumber("nbf", value)
        }
        get() = get("nbf")?.jsonPrimitive?.int

    var iat: Int?
        set(value) {
            putNumber("iat", value)
        }
        get() = get("iat")?.jsonPrimitive?.int

    var jti: String?
        set(value) {
            putString("jti", value)
        }
        get() = get("jti")?.jsonPrimitive?.contentOrNull

}

@JsExport
sealed class JwtBase(
    var underlying: JsonObject = JsonObject(mutableMapOf())
) : Map<String, JsonElement> by underlying {
    fun toJson(): JsonObject = underlying
    fun toJsonString() = cryptoJsonSerializer.encodeToString(underlying)

    override fun get(key: String): JsonElement? {
        return underlying[key]
    }

    fun getPrimitive(key: String): JsonPrimitive? = underlying[key]?.jsonPrimitive
    fun getString(key: String): String? = underlying[key]?.jsonPrimitive?.content
    fun getBoolean(key: String): Boolean? = underlying[key]?.jsonPrimitive?.boolean

    fun put(key: String, value: JsonElement?) = apply {
        if (value !== null) {
            underlying = underlying.mergeJsonElement(key, value)
        }
    }

    fun putString(key: String, value: String?) = apply {
        put(key, JsonPrimitive(value))
    }

    fun putNumber(key: String, value: Number?) = apply {
        put(key, JsonPrimitive(value))
    }


}
