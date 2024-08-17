package com.sphereon.cbor.cose

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.NumberLabel
import kotlinx.serialization.json.JsonObject

@JsExport
actual external interface IKey {
    @JsName("kty")
    actual val kty: Any

    @JsName("kid")
    actual val kid: Any?

    @JsName("alg")
    actual val alg: Any?

    @JsName("key_ops")
    actual val key_ops: Any?

    @JsName("baseIV")
    actual val baseIV: Any?

    @JsName("crv")
    actual val crv: Any?

    @JsName("x")
    actual val x: Any?

    @JsName("y")
    actual val y: Any?

    @JsName("x5chain") //x5c in JWK
    actual val x5chain: Any?

    @JsName("additional")
    actual val additional: Any?

    @JsName("d")
    actual val d: Any?

}

@JsExport
actual external interface ICoseKeyJson : IKey {
    actual override val kty: CoseKeyType
    actual override val kid: String?
    actual override val alg: CoseAlgorithm?
    actual override val key_ops: Array<CoseKeyOperations>?
    actual override val baseIV: String?
    actual override val crv: CoseCurve?
    actual override val x: String?
    actual override val y: String?
    actual override val d: String?
    actual override val x5chain: Array<String>?
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
    actual override val baseIV: CborByteString?
    actual override val crv: CborUInt?
    actual override val x: CborByteString?
    actual override val y: CborByteString?
    actual override val d: CborByteString?
    actual override val x5chain: CborArray<CborByteString>?
    actual override val additional: CborMap<NumberLabel, CborItem<*>>?
}
