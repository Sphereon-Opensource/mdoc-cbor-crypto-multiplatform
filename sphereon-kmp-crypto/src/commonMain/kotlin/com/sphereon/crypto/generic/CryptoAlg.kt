package com.sphereon.crypto.generic

@kotlinx.serialization.Serializable
enum class CryptoAlg(val internalName: String, val oid: String, val padding: String? = null) {

    RSA("RSA", "1.2.840.113549.1.1.1", "RSA/ECB/PKCS1Padding"),

    SHA256_WITH_RSA("SHA256withRSA", "1.2.840.113549.1.1.11"), // PKCS#1 v1.5 is implied for SHA256withRSA

//    DSA("DSA", "1.2.840.10040.4.1", "DSA"),

    ECDSA("ECDSA", "1.2.840.10045.2.1"),

//    PLAIN_ECDSA("PLAIN-ECDSA", "0.4.0.127.0.7.1.1.4.1", "PLAIN-ECDSA"),

    X25519("X25519", "1.3.101.110"),

    X448("X448", "1.3.101.111"),

    ED25519("Ed25519", "1.3.101.112"),

    ED448("Ed448", "1.3.101.113"),

    HMAC("HMAC", "");

    companion object {
        fun from(name: String): CryptoAlg = entries.find { it.internalName == name }
            ?: throw IllegalArgumentException("Algorithm $name not found")

    }
}
