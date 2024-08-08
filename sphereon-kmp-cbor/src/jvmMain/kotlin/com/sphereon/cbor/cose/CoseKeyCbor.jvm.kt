package com.sphereon.cbor.cose

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt

actual interface IKey {
    actual val kty: Any
    actual val kid: Any?
    actual val alg: Any?
    actual val key_ops: Any?
    actual val baseIV: Any?
    actual val crv: Any?
    actual val x: Any?
    actual val y: Any?
    actual val d: Any?
    actual val x5chain: Any?
    actual val additional: Any?

}

actual interface ICoseKeyJson : IKey {
    actual abstract override val kty: CoseKeyType
    actual abstract override val kid: String?
    actual abstract override val alg: CoseAlgorithm?
    actual abstract override val key_ops: Array<CoseKeyOperations>?
    actual abstract override val baseIV: String?
    actual abstract override val crv: CoseCurve?
    actual abstract override val x: String?
    actual abstract override val y: String?
    actual abstract override val d: String?
    actual abstract override val x5chain: Array<String>?
    actual abstract override val additional: MutableMap<*, *>?

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


actual interface ICoseKeyCbor : IKey {
    actual abstract override val kty: CborUInt
    actual abstract override val kid: CborByteString?
    actual abstract override val alg: CborUInt?
    actual abstract override val key_ops: CborArray<CborUInt>?
    actual abstract override val baseIV: CborByteString?
    actual abstract override val crv: CborUInt?
    actual abstract override val x: CborByteString?
    actual abstract override val y: CborByteString?
    actual abstract override val d: CborByteString?
    actual abstract override val x5chain: CborArray<CborByteString>?
    actual abstract override val additional: CborMap<NumberLabel, CborItem<*>>?
}
