@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
package com.sphereon.jose.jwt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


class JwtPayloadJson(val underlying: MutableMap<String, Any>) : MutableMap<String, Any> by underlying {
    override fun get(key: String): Any? {
        return underlying[key]
    }

    var iss: String? = null
    var sub: String? = null
    var aud: Any? = null // could be a string or array of string
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
