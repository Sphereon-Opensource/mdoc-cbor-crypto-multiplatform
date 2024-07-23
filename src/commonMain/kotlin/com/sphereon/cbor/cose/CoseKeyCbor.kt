package com.sphereon.cbor.cose

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import kotlin.js.JsExport


@JsExport
class CoseKeyJson(
    val kty: CoseKeyType,
    val kid: ByteArray? = null, // todo: HEX
    val alg: CoseAlgorithm? = null,

    val key_ops: Array<CoseKeyOperations>? = null,

    val baseIV: BaseIV? = null
) : JsonView<CoseKeyCbor>() {
    override fun toCbor(): CoseKeyCbor {
        TODO("Not yet implemented")
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

@JsExport
class CoseKeyCbor(
    val kty: CborUInt,
    val kid: CborByteString? = null,
    val alg: CborUInt? = null,
    val key_ops: CborArray<CborUInt>? = null,
    val baseIV: CborByteString? = null,
    val crv: CborUInt? = null,
    val x: CborByteString? = null,
    val y: CborByteString? = null,
    val x5chain: CborArray<CborByteString>? = null,
    val additional: CborMap<NumberLabel, AnyCborItem> = CborMap<NumberLabel, AnyCborItem>()
) : CborView<CoseKeyCbor, CoseKeyJson, CborMap<NumberLabel, AnyCborItem>>(CDDL.map) {


    override fun cborBuilder(): CborBuilder<CoseKeyCbor> {
        val mapBuilder = CborMap.builder(this)
        additional.value.map { mapBuilder.put(it.key, it.value) }
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
        return mapBuilder.end()
    }

    override fun toJson(): CoseKeyJson {
        TODO("Not yet implemented")
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

typealias BaseIV = ByteArray

typealias COSE_Key = CoseKeyCbor




