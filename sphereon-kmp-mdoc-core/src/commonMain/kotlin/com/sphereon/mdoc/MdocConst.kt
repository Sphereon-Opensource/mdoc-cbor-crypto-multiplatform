package com.sphereon.mdoc

import com.sphereon.kmp.Logger
import kotlin.js.JsExport

object MdocConst {
    val MDOC_LITERAL = "mdoc"
    val LOG_NAMESPACE = "sphereon:kmp:${MDOC_LITERAL}"
    val LOG = Logger.Static.tag(LOG_NAMESPACE)
}

@JsExport
enum class MdocRole {
    MDOC,

    MDOC_READER
}
