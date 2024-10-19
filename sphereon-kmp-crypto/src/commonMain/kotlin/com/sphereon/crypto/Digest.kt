package com.sphereon.crypto

import org.kotlincrypto.core.digest.Digest
import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.hash.sha2.SHA384
import org.kotlincrypto.hash.sha2.SHA512

// Initialize the cryptography provider
fun getDigest(hashAlgorithm: HashAlgorithm = HashAlgorithm.SHA256): Digest {
    return when (hashAlgorithm) {
        HashAlgorithm.SHA256 -> SHA256()
        HashAlgorithm.SHA384 -> SHA384()
        HashAlgorithm.SHA512 -> SHA512()
    }
}

/**
 * For now blocking, so we can easily use it in JS as well
 */
fun hash(dataInput: ByteArray, hashAlgorithm: HashAlgorithm = HashAlgorithm.SHA256): ByteArray {
    val digest = getDigest(hashAlgorithm)
    digest.update(dataInput)
    return digest.digest()
}

