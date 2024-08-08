package com.sphereon.kmp

import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import kotlin.js.JsExport

@JsExport
enum class Encoding {
    BASE64,
    BASE64URL,
    HEX
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
    padEncoded = true
}
val base64Conf = Base64 {
    isLenient = true
    lineBreakInterval = 0
    encodeToUrlSafe = false
    padEncoded = true
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
}

@JsExport
fun ByteArray.encodeTo(encoding: Encoding): String = when (encoding) {
    Encoding.BASE64URL -> this.encodeToBase64Url()
    Encoding.BASE64 -> this.encodeToBase64(urlSafe = false)
    Encoding.HEX -> this.encodeToHex()
}
