package com.sphereon.crypto

import dev.whyoleg.cryptography.CryptographyProvider

/**
 * For now blocking, so we can easily use it in JS as well
 */
fun hash(dataInput: ByteArray, hashAlgorithm: HashAlgorithm = HashAlgorithm.SHA256) =
    CryptographyProvider.Default
        .get(hashAlgorithm.toCryptoGraphicAlgorithm())
        .hasher().hashBlocking(dataInput)

