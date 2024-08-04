package com.sphereon.mdoc

import com.sphereon.kmp.Logger

object MdocConst {
    val MDOC_LITERAL = "mdoc"
    val LOG_NAMESPACE = "sphereon:kmp:${MDOC_LITERAL}"
    val LOG = Logger.tag(LOG_NAMESPACE)
}
