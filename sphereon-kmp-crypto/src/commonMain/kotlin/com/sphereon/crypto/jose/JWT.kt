@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
package com.sphereon.crypto.jose

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
class JwtPayloadJson(val underlying: JsonObject) : Map<String, Any> by underlying {
    override fun get(key: String): Any? {
        return underlying[key]
    }

    var iss: String? = null
    var sub: String? = null
    var aud: Array<String>? = null // TODO:could be a string or array of string
    var exp: Int? = null
    var nbf: Int? = null
    var iat: Int? = null
    var jti: String? = null
}

data class JwtPayloadOptions(
    var iss: String? = null,
    var sub: String? = null,
    var aud: Any? = null, // could be a string or array of string
    var exp: Int? = null,
    var nbf: Int? = null,
    var iat: Int? = null,
    var jti: String? = null,
    var additionalClaims: Map<String, Any>? = null
)

class JwtPayload(options: JwtPayloadOptions? = null) {
    var iss: String? = options?.iss
    var sub: String? = options?.sub
    var aud: Any? = options?.aud // Could be string or array of string
    var exp: Int? = options?.exp
    var nbf: Int? = options?.nbf
    var iat: Int? = options?.iat
    var jti: String? = options?.jti
    var additionalClaims: Map<String, Any>? = options?.additionalClaims

    // Add your validation method and other class methods here
}
