package com.sphereon.cbor.cose

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toHexStringArray
import com.sphereon.kmp.LongKMP
import kotlin.js.JsExport


expect interface IKey {
    val kty: Any
    val kid: Any?
    val alg: Any?

    val key_ops: Any?

    val baseIV: Any?

    val crv: Any?
    val x: Any?
    val y: Any?
    val x5chain: Any?
    val additional: Any?
}



expect interface ICoseKeyJson : IKey {
    override val kty: CoseKeyType
    override val kid: String?
    override val alg: CoseAlgorithm?

    override val key_ops: Array<CoseKeyOperations>?

    override val baseIV: String?

    override val crv: CoseCurve?
    override val x: String?
    override val y: String?
    override val x5chain: Array<String>?
    override val additional: MutableMap<*, *>?
}

@JsExport
class CoseKeyJson(
    override val kty: CoseKeyType,
    override val kid: String? = null,
    override val alg: CoseAlgorithm? = null,

    override val key_ops: Array<CoseKeyOperations>? = null,

    override val baseIV: String? = null,

    override val crv: CoseCurve? = null,
    // TODO: Probably some of these are bse64url instead of hex
    override val x: String? = null,
    override val y: String? = null,
    override val x5chain: Array<String>? = null,
    override val additional: MutableMap<*, *>? = mutableMapOf<Any, Any>()
) : JsonView<CoseKeyCbor>(), ICoseKeyJson {
    override fun toCbor(): CoseKeyCbor =
        CoseKeyCbor.Builder().withKty(kty).withKid(kid).withAlg(alg).withKeyOps(key_ops).withBaseIV(baseIV)
            .withCrv(crv).withX(x).withY(y).withX5Chain(x5chain).build() // todo: additional


    companion object {
        fun fromDTO(dto: ICoseKeyJson) = CoseKeyJson(
            kty = dto.kty,
            kid = dto.kid,
            alg = dto.alg,
            key_ops = dto.key_ops,
            baseIV = dto.baseIV,
            crv = dto.crv,
            x = dto.x,
            y = dto.y,
            x5chain = dto.x5chain,
            additional = dto.additional
        )
    }


    class Builder {
        private lateinit var kty: CoseKeyType
        var kid: String? = null
        var alg: CoseAlgorithm? = null
        var key_ops: Array<CoseKeyOperations>? = null
        var baseIV: String? = null
        var crv: CoseCurve? = null
        var x: String? = null
        var y: String? = null
        var x5chain: Array<String>? = null
        var additional: MutableMap<LongKMP, Any> = mutableMapOf()


        fun withKty(kty: CoseKeyType) = apply { this.kty = kty }

        fun withKid(kid: String?) = apply { this.kid = kid }
        fun withAlg(alg: CoseAlgorithm?) = apply { this.alg = alg }
        fun withKeyOps(key_ops: Array<CoseKeyOperations>?) = apply {
            this.key_ops = key_ops
        }

        fun withBaseIV(baseIVHex: String?) = apply { this.baseIV = baseIVHex }
        fun withCrv(crv: CoseCurve?) = apply { this.crv = crv }
        fun withX(x: String?) = apply { this.x = x }
        fun withY(y: String?) = apply { this.y = y }
        fun withX5Chain(x5c: Array<String>?) = apply { this.x5chain = x5c }

        // FIXME
//        fun withAdditional(additional: MutableMap<LongKMP, *>?) = apply { additional?.let { this.additional = CborMap(it) }}

        fun build(): CoseKeyJson {
            return CoseKeyJson(
                kty = kty,
                alg = alg,
                kid = kid,
                key_ops = key_ops,
                baseIV = baseIV,
                crv = crv,
                x = x,
                y = y,
                x5chain = x5chain,
                additional = additional
            )
        }
    }
}

/**
 * The CDDL grammar describing COSE_Key and COSE_KeySet is:
 *
 *    COSE_Key = {
 *        1 => tstr / int,          ; kty
 *        ? 2 => bstr,              ; kid
 *        ? 3 => tstr / int,        ; alg
 *        ? 4 => [+ (tstr / int) ], ; key_ops
 *        ? 5 => bstr,              ; Base IV
 *        * label => values
 *    }
 */


expect interface ICoseKeyCbor : IKey {
    override val kty: CborUInt
    override val kid: CborByteString?
    override val alg: CborUInt?
    override val key_ops: CborArray<CborUInt>?
    override val baseIV: CborByteString?
    override val crv: CborUInt?
    override val x: CborByteString?
    override val y: CborByteString?
    override val x5chain: CborArray<CborByteString>?
    override val additional: CborMap<NumberLabel, CborItem<*>>?
}

@JsExport
class CoseKeyCbor(
    override val kty: CborUInt,
    override val kid: CborByteString? = null,
    override val alg: CborUInt? = null,
    override val key_ops: CborArray<CborUInt>? = null,
    override val baseIV: CborByteString? = null,
    override val crv: CborUInt? = null,
    override val x: CborByteString? = null,
    override val y: CborByteString? = null,
    override val x5chain: CborArray<CborByteString>? = null,
    override val additional: CborMap<NumberLabel, AnyCborItem>? = CborMap()
) : ICoseKeyCbor, CborView<CoseKeyCbor, CoseKeyJson, CborMap<NumberLabel, AnyCborItem>>(CDDL.map) {

    override fun cborBuilder(): CborBuilder<CoseKeyCbor> {
        val mapBuilder = CborMap.builder(this)
        additional?.value?.map { mapBuilder.put(it.key, it.value) }
        // doing these last to make sure the additional map does not overwrite known props
        mapBuilder.put(KTY, kty)
            .put(KID, kid, true)
            .put(ALG, alg, true)
            .put(KEY_OPS, key_ops, true)
            .put(BASE_IV, baseIV, true)
            .put(CRV, crv, true)
            .put(X5_CHAIN, x5chain, true)
            .put(X, x, true)
            .put(Y, y, true)
        //todo additional
        return mapBuilder.end()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toJson(): CoseKeyJson {
        return CoseKeyJson.Builder().withKty(CoseKeyType.fromValue(kty.value.toInt()))
            .withKid(kid?.let { kid.value.toString() })
            .withAlg(alg?.let { CoseSignatureAlgorithm.fromValue(it.value.toInt()) })
            .withKeyOps(key_ops?.value?.map { ko -> CoseKeyOperations.Companion.fromValue(ko.value.toInt()) }
                ?.toTypedArray())
            .withBaseIV(baseIV?.value?.toHexString())
            .withCrv(crv?.let { CoseCurve.fromValue(it.value.toInt()) })
            .withX5Chain(x5chain?.toHexStringArray())
            .withX(x?.value?.toHexString())
            .withY(y?.value?.toHexString())
            //todo additional
            .build()

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoseKeyCbor) return false

        if (kty != other.kty) return false
        if (kid != other.kid) return false
        if (alg != other.alg) return false
        if (key_ops != other.key_ops) return false
        if (baseIV != other.baseIV) return false
        if (crv != other.crv) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (additional != other.additional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kty.hashCode()
        result = 31 * result + (kid?.hashCode() ?: 0)
        result = 31 * result + (alg?.hashCode() ?: 0)
        result = 31 * result + (key_ops?.hashCode() ?: 0)
        result = 31 * result + (baseIV?.hashCode() ?: 0)
        result = 31 * result + (crv?.hashCode() ?: 0)
        result = 31 * result + (x?.hashCode() ?: 0)
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + additional.hashCode()
        return result
    }

    class Builder {
        private lateinit var kty: CborUInt
        var kid: CborByteString? = null
        var alg: CborUInt? = null
        var key_ops: CborArray<CborUInt>? = null
        var baseIV: CborByteString? = null
        var crv: CborUInt? = null
        var x: CborByteString? = null
        var y: CborByteString? = null
        var x5chain: CborArray<CborByteString>? = null
        var additional: CborMap<NumberLabel, AnyCborItem> = CborMap()


        fun withKty(kty: CoseKeyType) = apply { this.kty = CborUInt(kty.value) }

        fun withKid(kid: String?) = apply { kid?.let { this.kid = it.toCborByteString() } }
        fun withAlg(alg: CoseAlgorithm?) = apply { alg?.let { this.alg = CborUInt(it.value) } }
        fun withKeyOps(key_ops: Array<CoseKeyOperations>?) = apply {
            key_ops?.let {
                this.key_ops = CborArray(it.map { op -> op.toCbor() }.toMutableList())
            }
        }

        fun withBaseIV(baseIVHex: String?) = apply { baseIVHex?.let { this.baseIV = it.toCborByteString() } }
        fun withCrv(crv: CoseCurve?) = apply {
            crv?.let { this.crv = it.toCbor() }
        }

        fun withX(x: String?) = apply { x?.let { this.x = it.toCborByteString() } }
        fun withY(y: String?) = apply { y?.let { this.y = it.toCborByteString() } }
        fun withX5Chain(x5c: Array<String>?) =
            apply {
                x5c?.let {
                    this.x5chain = CborArray(it.map { cert -> cert.toCborByteString() }.toMutableList())
                }
            }

        // FIXME
//        fun withAdditional(additional: MutableMap<LongKMP, *>?) = apply { additional?.let { this.additional = CborMap(it) }}

        fun build(): CoseKeyCbor {
            return CoseKeyCbor(
                kty = kty,
                alg = alg,
                kid = kid,
                key_ops = key_ops,
                baseIV = baseIV,
                crv = crv,
                x = x,
                y = y,
                x5chain = x5chain,
                additional = additional
            )
        }
    }

    companion object {
        val KTY = NumberLabel(1)
        val KID = NumberLabel(2)
        val ALG = NumberLabel(3)
        val KEY_OPS = NumberLabel(4)
        val BASE_IV = NumberLabel(5)
        val X5_CHAIN = NumberLabel(33)
        val CRV = NumberLabel(-1)
        val X = NumberLabel(-2)
        val Y = NumberLabel(-3)

        fun builder() = Builder()

        fun fromDTO(dto: ICoseKeyCbor) = CoseKeyCbor(
            kty = dto.kty,
            kid = dto.kid,
            alg = dto.alg,
            key_ops = dto.key_ops,
            baseIV = dto.baseIV,
            crv = dto.crv,
            x = dto.x,
            y = dto.y,
            x5chain = dto.x5chain,
            additional = dto.additional
        )

        fun cborDecode(encodedDeviceEngagement: ByteArray): CoseKeyCbor =
            fromCborItem(cborSerializer.decode(encodedDeviceEngagement))

        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>): CoseKeyCbor {
            return CoseKeyCbor(
                kty = KTY.required(m),
                kid = KID.optional(m),
                alg = ALG.optional(m),
                key_ops = KEY_OPS.optional(m),
                baseIV = BASE_IV.optional(m),
                crv = CRV.optional(m),
                x5chain = X5_CHAIN.optional(m),
                x = X.optional(m),
                y = Y.optional(m),
                additional = m // Yes this map also contains the above values
            )
        }
    }
}

typealias COSE_Key = CoseKeyCbor




