package com.sphereon.cbor

import com.sphereon.kmp.Logger

object CborConst {
    val CBOR_LITERAL = "cbor"
    val LOG_NAMESPACE = "sphereon:kmp:${CBOR_LITERAL}"
    val LOG = Logger.tag(LOG_NAMESPACE)
}
