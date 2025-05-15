package com.sphereon.cbor

import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import kotlin.test.Test
import kotlin.test.assertNotNull

class CborJsonItemTest {

    private val testVector = "a46672616e646f6d50892b11bae288e57600b84994323e0198686469676573744944136c656c656d656e7456616c7565a26576616c75656244456b636f756e7472794e616d65674765726d616e7971656c656d656e744964656e7469666965726b6e6174696f6e616c697479 ".replace(
                " ",
                ""
            )




    @Test
    fun shouldConvertToJson() {
        val cborItem: CborMap<CborString, CborItem<*>> = cborSerializer.decode(testVector.decodeFrom(Encoding.HEX))
        val jsonItem = cborItem.toJsonWithCDDLObject()
        println(jsonItem)
        assertNotNull(jsonItem)
    }
}
