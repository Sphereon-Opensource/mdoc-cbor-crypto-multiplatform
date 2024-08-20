package com.sphereon.crypto.cose

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.NumberLabel
import com.sphereon.crypto.IKey
import kotlinx.serialization.json.JsonObject

actual sealed interface ICoseKeyJson : IKey {
    actual abstract override val kty: CoseKeyType
    actual abstract override val kid: String?
    actual abstract override val alg: CoseAlgorithm?
    actual abstract override val key_ops: Array<CoseKeyOperations>?
    actual abstract val baseIV: String?
    actual abstract override val crv: CoseCurve?
    actual abstract override val x: String?
    actual abstract override val y: String?
    actual abstract override val d: String?
    actual abstract val x5chain: Array<String>?
    actual abstract override val additional: JsonObject?

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
    actual abstract val baseIV: CborByteString?
    actual abstract override val crv: CborUInt?
    actual abstract override val x: CborByteString?
    actual abstract override val y: CborByteString?
    actual abstract override val d: CborByteString?
    actual abstract val x5chain: CborArray<CborByteString>?
    actual abstract override val additional: CborMap<NumberLabel, CborItem<*>>?
}
