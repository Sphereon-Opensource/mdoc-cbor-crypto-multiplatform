package com.sphereon.cbor.cose

import com.sphereon.cbor.cddl_bool

/**
 * There are two signature algorithm schemes.  The first is signature
 *    with appendix.  In this scheme, the message content is processed and
 *    a signature is produced; the signature is called the appendix.  This
 *    is the scheme used by algorithms such as ECDSA and the RSA
 *    Probabilistic Signature Scheme (RSASSA-PSS).  (In fact, the SSA in
 *    RSASSA-PSS stands for Signature Scheme with Appendix.)
 */
interface CborSignatureProvider1 {
    fun sign(message: ByteArray, keyId: String): ByteArray
    fun verification(message: ByteArray, keyId: String, signature: ByteArray): cddl_bool
}


