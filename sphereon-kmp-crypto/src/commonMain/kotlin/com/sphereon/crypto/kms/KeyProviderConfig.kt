package com.sphereon.crypto.kms

import com.sphereon.kmp.Base64UrlSerializer
import kotlin.js.JsExport

@JsExport
@kotlinx.serialization.Serializable
data class KeyProviderConfig(
    /** Enable caching of keys/certificates. Requires a JSR107 Cache implementation on the classpath! */
    val cacheEnabled: Boolean? = false,

    /** How long in seconds should certificates be kept in the cache since last access. Default: 5 min */
    val cacheTTLInSeconds: Int? = 5 * 60,

    val type: KeyProviderType,

    val password: PasswordInputCallback? = null,

    val pkcs11Parameters: Pkcs11Parameters? = null,

    val pkcs12Parameters: KeystoreParameters? = null,

    val jksParameters: KeystoreParameters? = null,

//    val restConfig: RestConfig? = null

)
/*
@kotlinx.serialization.Serializable
data class RestConfig(
    val baseUrl: String? = "http://localhost/",

    val connectTimeoutInMS: Int? = 5000,
    val readTimeoutInMS: Int? = 10000

)*/

@JsExport
@kotlinx.serialization.Serializable
data class Pkcs11Parameters(
    /** The path to the library  */
    val pkcs11LibraryPath: String? = null,

    /** The callback to enter a password/pincode  */
    val callback: PasswordInputCallback? = null,

    /** The slot Id to use  */
    val slotId: Int? = 0,

    /** The slot list index to use  */
    val slotListIndex: Int? = -1,

    /** Additional PKCS11 config  */
    val extraPkcs11Config: String? = null
)

@JsExport
@kotlinx.serialization.Serializable
data class KeystoreParameters(
    val providerPath: String? = null,
    @kotlinx.serialization.Serializable(with = Base64UrlSerializer::class)
    val providerBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KeystoreParameters

        if (providerPath != other.providerPath) return false
        if (providerBytes != null) {
            if (other.providerBytes == null) return false
            if (!providerBytes.contentEquals(other.providerBytes)) return false
        } else if (other.providerBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = providerPath?.hashCode() ?: 0
        result = 31 * result + (providerBytes?.contentHashCode() ?: 0)
        return result
    }
}
/*

@kotlinx.serialization.Serializable
class Pkcs12Paremeters(providerPath: String?, providerBytes: ByteArray?) : KeystoreParameters(providerPath, providerBytes)
*/
