package com.sphereon.crypto.generic

import dev.whyoleg.cryptography.CryptographyAlgorithmId
import kotlinx.serialization.Serializable
import org.kotlincrypto.core.digest.Digest
import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.hash.sha2.SHA384
import org.kotlincrypto.hash.sha2.SHA512
import kotlin.js.JsExport
import kotlin.js.JsName

// Initialize the cryptography provider
fun getDigest(digestAlgorithm: DigestAlg = DigestAlg.SHA256): Digest {
    return when (digestAlgorithm) {
        DigestAlg.SHA256 -> SHA256()
        DigestAlg.SHA384 -> SHA384()
        DigestAlg.SHA512 -> SHA512()
        else -> throw IllegalArgumentException("digestAlgorithm $digestAlgorithm is not yet supported")
    }
}

/**
 * For now blocking, so we can easily use it in JS as well
 */
fun hash(dataInput: ByteArray, digestAlgorithm: DigestAlg = DigestAlg.SHA256): ByteArray {
    val digest = getDigest(digestAlgorithm)
    digest.update(dataInput)
    return digest.digest()
}

@JsExport
@Serializable
enum class DigestAlg(
    val internalName: String,
    val javaName: String,
    val oid: String,
    val xmlId: String? = null,
    val jadesId: String? = null,
    val httpHeaderId: String? = null,
    val saltLength: Int? = 0
) {
    NONE("", "", "", ""),
    SHA256("SHA256", "SHA-256", "2.16.840.1.101.3.4.2.1", "http://www.w3.org/2001/04/xmlenc#sha256", "S256", "SHA-256", 32),
    SHA384("SHA384", "SHA-384", "2.16.840.1.101.3.4.2.2", "http://www.w3.org/2001/04/xmlenc#sha384", "S384", "SHA-384", 48),
    SHA512("SHA512", "SHA-512", "2.16.840.1.101.3.4.2.3", "http://www.w3.org/2001/04/xmlenc#sha512", "S512", "SHA-512", 64),
    SHA3_256("SHA3-256", "SHA3-256", "2.16.840.1.101.3.4.2.8", "http://www.w3.org/2007/05/xmldsig-more#sha3-256", "S3-256", null, 32),
    SHA3_384("SHA3-384", "SHA3-384", "2.16.840.1.101.3.4.2.9", "http://www.w3.org/2007/05/xmldsig-more#sha3-384", "S3-384", null, 48),
    SHA3_512("SHA3-512", "SHA3-512", "2.16.840.1.101.3.4.2.10", "http://www.w3.org/2007/05/xmldsig-more#sha3-512", "S3-512", null, 64);

    @JsExport.Ignore
    fun toCryptoGraphicAlgorithm(): CryptographyAlgorithmId<dev.whyoleg.cryptography.algorithms.Digest> {
        return if (this == SHA256) dev.whyoleg.cryptography.algorithms.SHA256
        else if (this == SHA384) dev.whyoleg.cryptography.algorithms.SHA384
        else if (this == SHA512) dev.whyoleg.cryptography.algorithms.SHA512
        else throw IllegalArgumentException("$internalName is not yet supported")
    }

    object Static {
        fun isNone(digestAlg: DigestAlg?): Boolean {
            return digestAlg == null || digestAlg == NONE
        }

        @JsName("fromValue")
        fun fromValue(name: String): DigestAlg {
            return DigestAlg.entries.find { entry -> entry.internalName == name || entry.httpHeaderId == name || entry.javaName == name}
                ?: throw IllegalArgumentException("Unknown value $name")
        }
    }
}
