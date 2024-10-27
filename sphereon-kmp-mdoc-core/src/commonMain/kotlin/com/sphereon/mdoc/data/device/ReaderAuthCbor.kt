package com.sphereon.mdoc.data.device

import com.sphereon.cbor.cddl_bool
import com.sphereon.cbor.cddl_bstr
import com.sphereon.cbor.cddl_tstr


data class ReaderAuthCbor(
    val readerAuth: ByteArray,
    val readerSignIsValid: cddl_bool,
    // fixme: X509Certificate
    val readerCertificateChain: List<cddl_bstr>,
    val readerCertificatedIsTrusted: cddl_bool,
    val readerCommonName: cddl_tstr
)
