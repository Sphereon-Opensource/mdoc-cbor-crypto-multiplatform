package com.sphereon.cbor

import kotlin.test.Test
import kotlin.test.assertEquals

class CborEncoderTest {
    @Test
    fun shouldEncodeAndDecodeCorrectly() {
        // Create a test map with different CBOR types
        val originalMap = CborMap(
            linkedMapOf(
                CborString("text") to CborString("Hello World"),
                CborString("number") to CborUInt(42),
                CborString("float") to CborDouble(3.14),
                CborString("boolean") to CborSimple.Static.TRUE,
                CborString("array") to CborArray(mutableListOf(
                    CborString("item1"),
                    CborUInt(2),
                    CborSimple.Static.FALSE
                )),
                CborString("nested") to CborMap(linkedMapOf(
                    CborString("key") to CborString("value")
                ))
            )
        )

        // Encode the map
        val encoded = Cbor.encode(originalMap)

        // Decode it back
        val decoded = Cbor.decode<CborMap<CborString, CborItem<*>>>(encoded)

        // Verify the structure is preserved
        val textValue = (decoded.value[CborString("text")] as CborString).value
        assertEquals("Hello World", textValue)

        val numberValue = (decoded.value[CborString("number")] as CborUInt).value.toLong()
        assertEquals(42L, numberValue)

        val floatValue = (decoded.value[CborString("float")] as CborDouble).value
        assertEquals(3.14, floatValue)

        val booleanValue = (decoded.value[CborString("boolean")] as CborSimple<*>) === CborSimple.Static.TRUE
        assertEquals(true, booleanValue)

        val decodedArray = decoded.value[CborString("array")] as CborArray<CborItem<*>>
        val arrayFirstValue = (decodedArray.value[0] as CborString).value
        assertEquals("item1", arrayFirstValue)

        val arraySecondValue = (decodedArray.value[1] as CborUInt).value.toLong()
        assertEquals(2L, arraySecondValue)

        val arrayThirdValue = (decodedArray.value[2] as CborSimple<*>) === CborSimple.Static.FALSE
        assertEquals(true, arrayThirdValue)

        val nestedMap = decoded.value[CborString("nested")] as CborMap<CborString, CborItem<*>>
        val nestedValue = (nestedMap.value[CborString("key")] as CborString).value
        assertEquals("value", nestedValue)
    }
}