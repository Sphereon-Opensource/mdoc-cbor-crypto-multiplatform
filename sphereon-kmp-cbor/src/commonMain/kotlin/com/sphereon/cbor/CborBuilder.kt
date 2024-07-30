package com.sphereon.cbor

import kotlin.js.JsExport

/**
 * CBOR data item builder.
 */
@JsExport
class CborBuilder<T>(private val item: AnyCborItem, private val subject:T) {
    /**
     * Builds the CBOR data items.
     *
     * @return a [CborItem<Any>]
     */
    fun build(): AnyCborItem = item

    fun subject(): T = subject

    fun encodedBuild() = Cbor.encode(build())
}
