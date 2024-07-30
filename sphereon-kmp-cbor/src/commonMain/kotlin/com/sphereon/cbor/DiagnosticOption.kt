package com.sphereon.cbor

import kotlin.js.JsExport

/**
 * Enumeration of options that can be passed to [Cbor.toDiagnosticsEncoded], shamelessly copied from Google
 */
@JsExport
enum class DiagnosticOption {
    /**
     * Prints out embedded CBOR, that is, byte strings tagged with [CborTagged.ENCODED_CBOR].
     */
    EMBEDDED_CBOR,

    /**
     * Inserts newlines and indentation to make the output more readable.
     */
    PRETTY_PRINT,

    /**
     * Prints "<length> bytes" or "indefinite-size byte-string" instead of the bytes in the byte
     * string.
     */
    BSTR_PRINT_LENGTH
}
