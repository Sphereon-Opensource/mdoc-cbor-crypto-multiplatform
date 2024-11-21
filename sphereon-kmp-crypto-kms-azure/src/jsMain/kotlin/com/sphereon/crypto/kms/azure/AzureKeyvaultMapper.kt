package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JoseKeyOperations
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk

private fun String.toJwaKeyType(): JwaKeyType {
    return when (this) {
        "RSA" -> JwaKeyType.RSA
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

fun AzureKeyvaultKey.toJwk(): Jwk {
    return Jwk(
        kid = key.kid,
        kty = key.kty.toJwaKeyType(),
        key_ops = key.keyOps.map { it.toJoseKeyOperationsArray() }.toTypedArray(),
        n = key.n.toString(),
        e = key.e.toString()
    )
}

fun SignatureAlgorithm.toKeyTypeString(): String {
    return when (this) {
        SignatureAlgorithm.RSA_SHA256 -> "RSA"
        SignatureAlgorithm.RSA_SHA384 -> "RSA"
        SignatureAlgorithm.RSA_SHA512 -> "RSA"
        SignatureAlgorithm.ECDSA_SHA256 -> "EC"
        SignatureAlgorithm.ECDSA_SHA384 -> "EC"
        SignatureAlgorithm.ECDSA_SHA512 -> "EC"
        else -> throw IllegalArgumentException("Unsupported signature algorithm: $this")
    }
}
