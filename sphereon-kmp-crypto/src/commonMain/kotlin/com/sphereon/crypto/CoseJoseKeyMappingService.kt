package com.sphereon.crypto

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ICoseKeyJson
import com.sphereon.crypto.jose.IJwk
import com.sphereon.crypto.jose.IJwkJson
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.cborToJwk
import com.sphereon.crypto.jose.jsonToJwk
import com.sphereon.kmp.Encoding
import kotlin.js.JsExport

/**
 * CoseJoseKeyMappingService is an object designed to handle the conversion
 * between different key formats used in COSE (CBOR Object Signing and Encryption) and JOSE (JSON Object Signing and Encryption).
 */
@JsExport
object CoseJoseKeyMappingService {
    /**
     * Converts the given key to a JOSE JWK format.
     *
     * @param key The key to be converted. It can be of types CoseKeyCbor, ICoseKeyCbor, CoseKeyJson, ICoseKeyJson, Jwk, IJwk, or IJwkJson.
     * @return The equivalent key in JOSE JWK format.
     * @throws IllegalArgumentException if the key cannot be converted to JOSE JWK format.
     */
    fun toJoseJwk(key: IKey): Jwk = when (key) {
        is CoseKeyCbor -> key.cborToJwk()
        is ICoseKeyCbor -> CoseKeyCbor.Static.fromDTO(key).cborToJwk()
        is CoseKeyJson -> key.jsonToJwk()
        is ICoseKeyJson -> CoseKeyJson.Static.fromDTO(key).jsonToJwk()
        is Jwk -> key
        is IJwk -> Jwk.Static.fromDTO(key)
        is IJwkJson -> Jwk.Static.fromJson(key)
        else -> throw IllegalArgumentException("Cannot convert key to jose/jwk")
    }


    /**
     * Retrieves the X.509 certificate chain (x5c) from the provided key.
     *
     * @param key An instance of IKey from which to obtain the x5c array.
     * @return An array of strings representing the x5c certificate chain, or null if not available.
     */
    fun getJoseX5c(key: IKey): Array<String>? = key.getX5cArray()

    /**
     * Converts an array of mixed types to an array of base64-encoded strings.
     *
     * @param x5c an array of items which can be of type String or CborByteString.
     * @return an array of base64-encoded strings if the input array is non-null, otherwise returns null.
     * @throws IllegalArgumentException if an item in the input array is not a String or CborByteString.
     */
    fun toJoseX5c(x5c: Array<Any>?): Array<String>? {
        return x5c?.map {
            when (it) {
                is String -> it
                is CborByteString -> it.encodeTo(Encoding.BASE64) // x5c is always base64
                else -> throw IllegalArgumentException("Cannot convert value $it to Cbor Byte String")
            }
        }?.toTypedArray()
    }


    fun toJwkKeyInfo(keyInfo: IKeyInfo<*>): KeyInfo<Jwk> {
        val key = keyInfo.key?.let { toJoseJwk(it) }
        return KeyInfo(key = key, kid = keyInfo.kid ?: key?.kid, opts = keyInfo.opts)
    }



    fun toCoseKeyInfo(keyInfo: IKeyInfo<*>): KeyInfo<CoseKeyCbor> {
        val key = keyInfo.key?.let { toCoseKey(it) }
        return KeyInfo(key = key, kid = keyInfo.kid ?: key?.kid?.encodeTo(Encoding.BASE64URL), opts = keyInfo.opts)
    }


    /**
     * Retrieves the x5chain field from a COSE key derived from the given IKey instance.
     *
     * @param key The input key implementing the IKey interface.
     * @return A CborArray containing CborByteString elements that represent the x5chain, or null if not present.
     */
    fun getCoseX5chain(key: IKey): CborArray<CborByteString>? = toCoseKey(key).x5chain

    /**
     * Converts a given array of X.509 certificate values to a COSE X.509 Chain represented as a CborArray of CborByteString.
     *
     * @param x5c An array of values; each value should be either a CborByteString or a base64 encoded String representing the X.509 certificate.
     * @return A CborArray containing the CborByteString of each X.509 certificate if the input is not null, otherwise returns null.
     * @throws IllegalArgumentException if any elements in the input array are not a CborByteString or a base64 encoded String.
     */
    fun toCoseX5chain(x5c: Array<Any>?): CborArray<CborByteString>? {
        return x5c?.map {
            when (it) {
                is CborByteString -> it
                is String -> it.toCborByteString(Encoding.BASE64) // x5c is always base64
                else -> throw IllegalArgumentException("Cannot convert value $it to Cbor Byte String")

            }
        }?.toMutableList()?.let { arr -> CborArray(arr) }
    }


    /**
     * Converts an `IKey` instance to `CoseKeyCbor`.
     *
     * @param key The `IKey` instance to be converted. This can be one of several implementing types.
     * @return The converted `CoseKeyCbor` instance.
     * @throws IllegalArgumentException If the key cannot be converted to CBOR.
     */
    fun toCoseKey(key: IKey): CoseKeyCbor = when (key) {
        is CoseKeyCbor -> key
        is ICoseKeyCbor -> CoseKeyCbor.Static.fromDTO(key)
        is CoseKeyJson -> key.toCbor()
        is ICoseKeyJson -> CoseKeyJson.Static.fromDTO(key).toCbor()
        is Jwk -> key.jwkToCoseKeyCbor()
        is IJwk -> Jwk.Static.fromDTO(key).jwkToCoseKeyCbor()
        is IJwkJson -> Jwk.Static.fromJson(key).jwkToCoseKeyCbor()
        else -> throw IllegalArgumentException("Cannot convert key to cbor")
    }


}
