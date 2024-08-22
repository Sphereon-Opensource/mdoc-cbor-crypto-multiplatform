package com.sphereon.crypto.jose

import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ICoseKeyJson
import com.sphereon.crypto.IKey
import com.sphereon.crypto.toCoseCurve
import com.sphereon.crypto.toCoseKeyOperations
import com.sphereon.crypto.toCoseKeyType
import com.sphereon.crypto.toCoseSignatureAlgorithm
import com.sphereon.crypto.toJoseCurve
import com.sphereon.crypto.toJoseKeyOperations
import com.sphereon.crypto.toJoseKeyType
import com.sphereon.crypto.toJoseSignatureAlgorithm
import com.sphereon.json.cryptoJsonSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.JsExport


/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
expect interface IJwkJson : IKey {
    override val alg: String?
    override val crv: String?
    override val d: String?
    val e: String?
    val k: String?
    override val key_ops: Array<String>?
    override val kid: String?
    override val kty: String
    val n: String?
    val use: String?
    override val x: String?
    val x5c: Array<String>?
    val x5t: String?
    val x5u: String?

    @SerialName("x5t#S256")
    val x5t_S256: String?
    override val y: String?
}


/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
expect interface IJwk : IKey {
    override val alg: JwaAlgorithm?
    override val crv: JwaCurve?
    override val d: String?
    val e: String?
    val k: String?
    override val key_ops: Array<JoseKeyOperations>?
    override val kid: String?
    override val kty: JwaKeyType
    val n: String?
    val use: String?
    override val x: String?
    val x5c: Array<String>?
    val x5t: String?
    val x5u: String?

    @SerialName("x5t#S256")
    val x5t_S256: String?
    override val y: String?
}

@JsExport
@Serializable
data class Jwk(
    override val alg: JwaAlgorithm? = null,
    override val crv: JwaCurve? = null,
    override val d: String? = null,
    override val e: String? = null,
    override val k: String? = null,
    override val key_ops: Array<JoseKeyOperations>? = null,
    override val kid: String? = null,
    override val kty: JwaKeyType,
    override val n: String? = null,
    override val use: String? = null,
    override val x: String? = null,
    override val x5c: Array<String>? = null,
    override val x5t: String? = null,
    override val x5u: String? = null,

    @SerialName("x5t#S256")
    override val x5t_S256: String? = null,
    override val y: String? = null,
) : IJwk {

    override val additional: JsonObject?
        get() = TODO("Not yet implemented")

    class Builder {
        var alg: JwaAlgorithm? = null
        var crv: JwaCurve? = null
        var d: String? = null
        var e: String? = null
        var k: String? = null
        var key_ops: Array<JoseKeyOperations>? = null
        var kid: String? = null
        var kty: JwaKeyType? = null
        var n: String? = null
        var use: String? = null
        var x: String? = null
        var x5c: Array<String>? = null
        var x5t: String? = null
        var x5u: String? = null
        var x5t_S256: String? = null
        var y: String? = null

        fun withAlg(alg: JwaAlgorithm? = null) = apply { this.alg = alg }
        fun withCrv(crv: JwaCurve?) = apply { this.crv = crv }
        fun withD(d: String?) = apply { this.d = d }
        fun withE(e: String?) = apply { this.e = e }
        fun withK(k: String?) = apply { this.k = k }
        fun withKeyOps(key_ops: Array<JoseKeyOperations>?) = apply { this.key_ops = key_ops }
        fun withKid(kid: String?) = apply { this.kid = kid }
        fun withKty(kty: JwaKeyType?) = apply { this.kty = kty }
        fun withN(n: String?) = apply { this.n = n }
        fun withUse(use: String?) = apply { this.use = use }
        fun withX(x: String?) = apply { this.x = x }
        fun withX5c(x5c: Array<String>?) = apply { this.x5c = x5c }
        fun withX5t(x5t: String?) = apply { this.x5t = x5t }
        fun withX5u(x5u: String?) = apply { this.x5u = x5u }
        fun withX5t_S256(x5t_S256: String?) = apply { this.x5t_S256 = x5t_S256 }
        fun withY(y: String?) = apply { this.y = y }


        fun build(): Jwk = Jwk(
            alg = alg,
            crv = crv,
            d = d,
            e = e,
            k = k,
            key_ops = key_ops,
            kid = kid,
            kty = kty ?: throw IllegalArgumentException("kty value missing"),
            n = n,
            use = use,
            x = x,
            x5c = x5c,
            x5t = x5t,
            x5u = x5u,
            x5t_S256 = x5t_S256,
            y = y
        )

    }

    // Name is like other extensions functions to not class with JS
    fun jwkToCoseKeyJson(): CoseKeyJson =
        CoseKeyJson.Builder()
            .withKty(this@Jwk.kty.toCoseKeyType() ?: throw IllegalArgumentException("kty value missing"))
            .withAlg(alg?.toCoseSignatureAlgorithm())
            .withCrv(crv?.toCoseCurve())
            .withD(d)
//                    .withE(e)
//                    .withK(k)
            .withKeyOps(key_ops?.map { it.toCoseKeyOperations() }?.toTypedArray())
            .withKid(kid)
//                    .withN(n)
//                    .withUse(use)
            .withX(x)
//                    .withX5t(x5t) // todo
            .withY(y)
            .build()


    // Name is like other extensions functions to not class with JS
    fun jwkToCoseKeyCbor(): CoseKeyCbor = this.jwkToCoseKeyJson().toCbor()

    fun toJsonObject() = cryptoJsonSerializer.encodeToJsonElement(serializer(), this).jsonObject


    object Static {
        fun fromJson(jwk: IJwkJson): Jwk = with(jwk) {
            return Jwk(
                alg = JwaAlgorithm.Static.fromValue(alg),
                crv = JwaCurve.Static.fromValue(crv),
                d = d,
                e = e,
                k = k,
                key_ops = key_ops?.map { JoseKeyOperations.Static.fromValue(it) }?.toTypedArray(),
                kid = kid,
                kty = JwaKeyType.Static.fromValue(kty),
                n = n,
                use = use,
                x = x,
                x5c = x5c?.map { it }?.toTypedArray(),
                x5t = x5t,
                x5u = x5u,
                x5t_S256 = x5t_S256,
                y = y,
            )
        }

        fun fromJsonObject(jwk: JsonObject): Jwk = with(jwk) {
            return@fromJsonObject Jwk(
                alg = get("alg")?.jsonPrimitive?.content?.let {
                    JwaAlgorithm.Static.fromValue(it)
                },
                crv = get("crv")?.jsonPrimitive?.content?.let { JwaCurve.Static.fromValue(it) },
                d = get("d")?.jsonPrimitive?.content,
                e = get("e")?.jsonPrimitive?.content,
                k = get("k")?.jsonPrimitive?.content,
                key_ops = get("key_ops")?.jsonArray?.map { JoseKeyOperations.Static.fromValue(it.jsonPrimitive.content) }?.toTypedArray(),
                kid = get("kid")?.jsonPrimitive?.content,
                kty = get("kty")?.jsonPrimitive?.content?.let { JwaKeyType.Static.fromValue(it) } ?: throw IllegalArgumentException("kty is missing"),
                n = get("n")?.jsonPrimitive?.content,
                use = get("use")?.jsonPrimitive?.content,
                x = get("x")?.jsonPrimitive?.content,
                x5c = get("x5c")?.jsonArray?.map { it.jsonPrimitive.content }?.toTypedArray(),
                x5t = get("x5t")?.jsonPrimitive?.content,
                x5u = get("x5u")?.jsonPrimitive?.content,
                x5t_S256 = get("x5t#S256")?.jsonPrimitive?.content,
                y = get("y")?.jsonPrimitive?.content,
            )
        }

        fun fromDTO(jwk: IJwk): Jwk = with(jwk) {
            return@fromDTO Jwk(
                alg = alg,
                crv = crv,
                d = d,
                e = e,
                k = k,
                key_ops = key_ops,
                kid = kid,
                kty = kty,
                n = n,
                use = use,
                x = x,
                x5c = x5c,
                x5t = x5t,
                x5u = x5u,
                x5t_S256 = x5t_S256,
                y = y
            )
        }

        fun fromCoseKeyJson(coseKey: ICoseKeyJson): Jwk {
            with(coseKey) {
                val kty = kty.toJoseKeyType()
                return Builder()
                    .withKty(kty)
                    .withAlg(alg?.toJoseSignatureAlgorithm())
                    .withCrv(crv?.toJoseCurve())
                    .withD(d)
//                    .withE(e)
//                    .withK(k)
                    .withKeyOps(key_ops?.map { it.toJoseKeyOperations() }?.toTypedArray())
                    .withKid(kid)
//                    .withN(n)
//                    .withUse(use)
                    .withX(x)
                    .withX5c(x5chain)
//                    .withX5t(x5t) // todo
                    .withY(y)
                    .build()
            }


        }

        fun fromCoseKey(coseKey: ICoseKeyCbor) = fromCoseKeyJson(CoseKeyCbor.Static.fromDTO(coseKey).toJson())

    }


}


@JsExport
fun CoseKeyCbor.cborToJwk() = Jwk.Static.fromCoseKey(this)

@JsExport
fun CoseKeyJson.jsonToJwk() = Jwk.Static.fromCoseKeyJson(this)
