package com.sphereon.crypto

import kotlin.js.JsExport


@JsExport
data class VerifyResults<KeyType>(
    val error: Boolean,
    val verifications: Array<VerifyResult>,
    val keyInfo: KeyInfo<KeyType>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifyResults<*>) return false

        if (error != other.error) return false
        if (!verifications.contentEquals(other.verifications)) return false
        if (keyInfo != other.keyInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = error.hashCode()
        result = 31 * result + verifications.contentHashCode()
        result = 31 * result + (keyInfo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "VerifyResults(error=$error, verifications=${verifications.contentToString()}, keyInfo=$keyInfo)"
    }


}


@JsExport
open class VerifyResult(
    val name: String,
    val error: Boolean,
    val message: String? = null,
    val critical: Boolean = true
) {

    override fun toString(): String {
        return "VerifyResult(name='$name', error=$error, message=$message, critical=$critical)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifyResult) return false

        if (name != other.name) return false
        if (error != other.error) return false
        if (message != other.message) return false
        if (critical != other.critical) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + critical.hashCode()
        return result
    }
}

@JsExport
class VerifySignatureResult<KeyType>(
    error: Boolean,
    name: String,
    critical: Boolean,
    message: String?,
    val keyInfo: KeyInfo<KeyType>?
) : VerifyResult(error = error, name = name, critical = critical, message = message) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifySignatureResult<*>) return false
        if (!super.equals(other)) return false

        if (keyInfo != other.keyInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (keyInfo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "VerifySignatureResult(keyInfo=$keyInfo)"
    }


}



@JsExport
data class KeyInfo<KeyType>(val kid: String?, /*val jwk: JWK,*/ val key: KeyType?, val opts: Map<*, *>?) {

    override fun hashCode(): Int {
        var result = kid?.hashCode() ?: 0
        result = 31 * result + (key?.hashCode() ?: 0)
        result = 31 * result + (opts?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "KeyInfo(kid=$kid, coseKey=$key, opts=$opts)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyInfo<*>) return false

        if (kid != other.kid) return false
        if (key != other.key) return false
        if (opts != other.opts) return false

        return true
    }

}
