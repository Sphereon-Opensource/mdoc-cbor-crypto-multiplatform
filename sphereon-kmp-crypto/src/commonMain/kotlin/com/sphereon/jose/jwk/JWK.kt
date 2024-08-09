package com.sphereon.jose.jwk

import com.sphereon.cbor.cose.CoseKeyCbor
import com.sphereon.cbor.cose.CoseKeyJson
import com.sphereon.cbor.cose.ICoseKeyCbor
import com.sphereon.cbor.cose.ICoseKeyJson
import com.sphereon.crypto.toCoseCurve
import com.sphereon.crypto.toCoseKeyOperations
import com.sphereon.crypto.toCoseKeyType
import com.sphereon.crypto.toCoseSignatureAlgorithm
import com.sphereon.crypto.toJoseCurve
import com.sphereon.crypto.toJoseKeyOperations
import com.sphereon.crypto.toJoseKeyType
import com.sphereon.crypto.toJoseSignatureAlgorithm
import com.sphereon.jose.jwa.JwaAlgorithm
import com.sphereon.jose.jwa.JwaCurve
import com.sphereon.jose.jwa.JwaKeyType
import kotlinx.serialization.SerialName
import kotlin.js.JsExport

/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
expect interface IJWK {
    val alg: JwaAlgorithm?
    val crv: JwaCurve?
    val d: String?
    val e: String?
    val k: String?
    val key_ops: Set<JoseKeyOperations>?
    val kid: String?
    val kty: JwaKeyType
    val n: String?
    val use: String?
    val x: String?
    val x5c: Array<String>?
    val x5t: String?
    val x5u: String?

    @SerialName("x5t#S256")
    val x5t_S256: String?
    val y: String?
}

data class JWK(
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
) : IJWK {

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


        fun build(): JWK = JWK(
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

    fun toCoseKeyJson(): CoseKeyJson =
        CoseKeyJson.Builder()
            .withKty(kty?.toCoseKeyType() ?: throw IllegalArgumentException("kty value missing"))
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


    fun toCoseKeyCbor(): CoseKeyCbor = this.toCoseKeyJson().toCbor()


    companion object {
        fun fromDTO(jwk: IJWK): JWK = with(jwk) {
            return@fromDTO JWK(
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

        fun fromCoseKeyJson(coseKey: ICoseKeyJson): JWK {
            with(coseKey) {
                val kty = kty.toJoseKeyType()
                return JWK.Builder()
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

        fun fromCoseKey(coseKey: ICoseKeyCbor) = fromCoseKeyJson(CoseKeyCbor.fromDTO(coseKey).toJson())

    }
}


@JsExport
fun CoseKeyCbor.cborToJwk() = JWK.fromCoseKey(this)

@JsExport
fun CoseKeyJson.jsonToJwk() = JWK.fromCoseKeyJson(this)
