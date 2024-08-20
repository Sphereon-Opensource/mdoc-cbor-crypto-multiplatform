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
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.js.JsExport


/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
expect interface IJwkJson: IKey {
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
    override val key_ops: Set<JoseKeyOperations>?
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
    override val key_ops: Set<JoseKeyOperations>? = null,
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
        var key_ops: Set<JoseKeyOperations>? = null
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
        fun withKeyOps(key_ops: Set<JoseKeyOperations>?) = apply { this.key_ops = key_ops }
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

    fun toJsonObject(): IJwkJson = object : IJwkJson {
        override val alg: String? = this@Jwk.alg?.value
        override val crv: String? = this@Jwk.crv?.value
        override val d: String? = this@Jwk.d
        override val e: String? = this@Jwk.e
        override val k: String? = this@Jwk.k
        override val key_ops: Array<String>? = this@Jwk.key_ops?.map { it.value }?.toTypedArray()
        override val kid: String? = this@Jwk.kid
        override val kty: String = this@Jwk.kty.value

        override val n: String? = this@Jwk.n
        override val use: String? = this@Jwk.use
        override val x: String? = this@Jwk.x
        override val x5c: Array<String>? = this@Jwk.x5c
        override val x5t: String? = this@Jwk.x5t
        override val x5u: String? = this@Jwk.x5u
        override val x5t_S256: String? = this@Jwk.x5t_S256
        override val y: String? = this@Jwk.y
        override val additional: JsonObject?
            get() = TODO("Not yet implemented")
    }


    companion object {
        fun fromJsonObject(jwk: IJwkJson): Jwk = with(jwk) {
            return Jwk(
                alg = JwaSignatureAlgorithm.fromValue(alg),
                crv = JwaCurve.fromValue(crv),
                d = d,
                e = e,
                k = k,
                key_ops = key_ops?.map { JoseKeyOperations.fromValue(it) }?.toSet(),
                kid = kid,
                kty = JwaKeyType.fromValue(kty),
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
                    .withKeyOps(key_ops?.map { it.toJoseKeyOperations() }?.toSet())
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
fun CoseKeyCbor.cborToJwk() = Jwk.fromCoseKey(this)

@JsExport
fun CoseKeyJson.jsonToJwk() = Jwk.fromCoseKeyJson(this)
