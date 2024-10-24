package com.sphereon.kmp

import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport

@JsExport
enum class Encoding {
    BASE64,
    BASE64URL,
    HEX,
    UTF8
}

@OptIn(ExperimentalStdlibApi::class)
@JsExport
fun String.decodeFromHex(): ByteArray {
    return this.hexToByteArray(HexFormat.Default)
}

@OptIn(ExperimentalStdlibApi::class)
@JsExport
fun ByteArray.encodeToHex(): String {
    return this.toHexString(HexFormat.Default)
}


val base64UrlConf = Base64 {
    isLenient = true
    lineBreakInterval = 0
    encodeToUrlSafe = true
    padEncoded = false
}
val base64Conf = Base64 {
    isLenient = true
    lineBreakInterval = 0
    encodeToUrlSafe = false
    padEncoded = false
}

@JsExport
fun String.decodeFromBase64Url() = this.decodeToByteArray(base64UrlConf)

@JsExport
fun ByteArray.encodeToBase64Url(): String = this.encodeToString(base64UrlConf)

@JsExport
fun ByteArray.encodeToBase64(urlSafe: Boolean = false): String =
    this.encodeToString(if (urlSafe) base64UrlConf else base64Conf)

@JsExport
fun String.decodeFromBase64(urlSafe: Boolean = false) =
    this.decodeToByteArray(if (urlSafe) base64UrlConf else base64Conf)

@JsExport
fun String.decodeFrom(encoding: Encoding): ByteArray = when (encoding) {
    Encoding.BASE64URL -> this.decodeFromBase64Url()
    Encoding.BASE64 -> this.decodeFromBase64(urlSafe = false)
    Encoding.HEX -> this.decodeFromHex()
    Encoding.UTF8 -> this.encodeToByteArray()
}

@JsExport
fun ByteArray.encodeTo(encoding: Encoding): String = when (encoding) {
    Encoding.BASE64URL -> this.encodeToBase64Url()
    Encoding.BASE64 -> this.encodeToBase64(urlSafe = false)
    Encoding.HEX -> this.encodeToHex()
    Encoding.UTF8 -> this.decodeToString()
}


/**
 * Serializer that can be used in Kotlin's serialization support to convert byte arrays into base64 strings and vice versa.
 */
object Base64Serializer : KSerializer<ByteArray> {

    override val descriptor = PrimitiveSerialDescriptor("Base64", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    /**
     * Serializes the given byte array into a base64 string.
     */
    override fun serialize(encoder: Encoder, value: ByteArray) {
        return encoder.encodeString(value.encodeToBase64(urlSafe = false))
    }

    /**
     * Deserializes the given base64 string into a byte array.
     */
    override fun deserialize(decoder: Decoder): ByteArray {
        val str = decoder.decodeString()
        return str.decodeFromBase64(urlSafe = false)
    }
}


/**
 * Serializer that can be used in Kotlin's serialization support to convert byte arrays into base64 strings and vice versa.
 */
object Base64UrlSerializer : KSerializer<ByteArray> {

    override val descriptor = PrimitiveSerialDescriptor("Base64Url", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    /**
     * Serializes the given byte array into a base64 string.
     */
    override fun serialize(encoder: Encoder, value: ByteArray) {
        return encoder.encodeString(value.encodeToBase64(urlSafe = true))
    }

    /**
     * Deserializes the given base64 string into a byte array.
     */
    override fun deserialize(decoder: Decoder): ByteArray {
        val str = decoder.decodeString()
        return str.decodeFromBase64(urlSafe = true)
    }
}
