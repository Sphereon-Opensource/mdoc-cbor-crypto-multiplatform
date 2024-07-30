package com.sphereon.cbor

import kotlin.test.Test
import kotlin.test.assertEquals

class EncodedCborItemTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldEncodeAndDecodeWrapper() {
        val input = "input".toCborString()
        val cborInput = CborEncodedItem<CborString>(input)
        val bytes = cborSerializer.encode(cborInput)

        println("Encoded object to hex:")
        println(bytes.toHexString())

        // Not using type on purpose
        val test: AnyCborItem = cborSerializer.decode(bytes)
        assertEquals(CDDL.bstr, test.cddl)
        val cborEncoded: CborEncodedItem<CborString> = test as CborEncodedItem<CborString>
        assertEquals(input.value, cborEncoded.decodedValue.value)
        println(test.toString())

    }
}
