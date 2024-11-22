package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.jose.JoseKeyOperations
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk

private fun String.toJwaKeyType(): JwaKeyType {
    return when (this) {
        "EC" -> JwaKeyType.EC
        else -> throw IllegalArgumentException("Unsupported key type: $this")
    }
}

private fun String.toJoseKeyOperationsArray(): JoseKeyOperations {
    return when (this) {
        "sign" -> JoseKeyOperations.SIGN
        "verify" -> JoseKeyOperations.VERIFY
        "encrypt" -> JoseKeyOperations.ENCRYPT
        "decrypt" -> JoseKeyOperations.DECRYPT
        "wrapKey" -> JoseKeyOperations.WRAP_KEY
        "unwrapKey" -> JoseKeyOperations.UNWRAP_KEY
        else -> throw IllegalArgumentException("Unsupported key operation: $this")
    }
}

fun String.toJwaAlgorithm(): JwaAlgorithm {
    return when (this) {
        "P-256" -> JwaAlgorithm.ES256
        "P-384" -> JwaAlgorithm.ES384
        "P-521" -> JwaAlgorithm.ES512
        else -> throw IllegalArgumentException("Unsupported algorithm: $this")
    }
}

fun AzureKeyvaultKey.toJwk(): Jwk {
    return Jwk(
        alg = key.crv.toJwaAlgorithm(),
        kid = key.kid,
        kty = key.kty.toJwaKeyType(),
        key_ops = key.keyOps.map { it.toJoseKeyOperationsArray() }.toTypedArray(),
        x = key.x.toString(),
        y = key.y.toString()
    )
}

fun String.toSignatureAlgorithm(): String {
    val algorithmMap = mapOf(
        "P-256" to "ES256",
        "P-384" to "ES384",
        "P-521" to "ES512"
    )
    return algorithmMap[this] ?: throw IllegalArgumentException("Unsupported algorithm or curve: $this")
}
