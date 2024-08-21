package com.sphereon.crypto.cose

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.NumberLabel
import com.sphereon.crypto.IKey
import kotlinx.serialization.json.JsonObject

@JsExport
actual external sealed interface ICoseKeyJson : IKey {
    actual override val kty: CoseKeyType
    actual override val kid: String?
    actual override val alg: CoseAlgorithm?
    actual override val key_ops: Array<CoseKeyOperations>?
    @JsName("baseIV")
    actual val baseIV: String?

    actual override val crv: CoseCurve?
    actual override val x: String?
    actual override val y: String?
    actual override val d: String?
    @JsName("x5chain") //x5c in JWK
    actual val x5chain: Array<String>?
    actual override val additional: JsonObject?

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
actual external interface ICoseKeyCbor : IKey {
    actual override val kty: CborUInt
    actual override val kid: CborByteString?
    actual override val alg: CborUInt?
    actual override val key_ops: CborArray<CborUInt>?
    @JsName("baseIV")
    actual val baseIV: CborByteString?
    actual override val crv: CborUInt?
    actual override val x: CborByteString?
    actual override val y: CborByteString?
    actual override val d: CborByteString?
    @JsName("x5chain")
    actual val x5chain: CborArray<CborByteString>?
    actual override val additional: CborMap<NumberLabel, CborItem<*>>?
}
