/*
package com.sphereon.cbor.cose

import com.sphereon.cbor.CborBool
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.toCborBool
import com.sphereon.mdoc.tx.device.BleOptions
import kotlin.test.Test
import kotlin.test.assertEquals

class CoseLabelTest {

    val uuid = "uuid"
    val address = "address"
    val data = TestBleOptions(
        true.toCborBool(),
        true.toCborBool(),
        CborByteString(uuid.encodeToByteArray())
        */
/*lets skip 11*//*
,
        peripheralServerModeDeviceAddress = CborByteString(address.encodeToByteArray())
    )

  */
/*  @Test
    fun shouldHavePopulatedLabels() {
        assertEquals(true, data.getLabeledValue<CborBool>(0).value?.value)
        assertEquals(true, data.getLabeledValue<CborBool>(1).value?.value)
        assertContentEquals(uuid.encodeToByteArray(), data.getLabeledValue<CborByteString(10).value?.value)
        assertEquals(uuid, data.getLabeledValue<CborByteString(10).value?.deserializedValue)

        // Not pn existing label should return null
        assertNotNull(address, data.getLabeledValue<CborByteString(20).value?.deserializedValue)
    }

    @Test
    fun shouldThrowErrorForUnknownLabels() {
        assertFails { data.getLabeledValue<Any>(44) }
        assertFails { data.getLabeledValue<Any>(-1) }
    }

    @Test
    fun shouldReturnNullValueForNullLabels() {
        assertNull( data.getLabeledValue<CborByteString(11).value )
    }*//*


    @Test
    fun shouldReturnNewInstanceFromLabels() {
        assertEquals( data, data.instanceFromLabels() )
    }

    @Test
    fun shouldEncodeAndDeode() {
        val bytes = data.encode()

        assertEquals( data, TestBleOptions.decodeFromTestBleOptions(bytes) )
    }


    data class TestBleOptions(
        val peripheralServerMode: CborBool,
        val centralClientMode: CborBool,
        val peripheralServerModeUUID: CborByteString? = null,
        val centralClientModeUUID: CborByteString? = null,
        val peripheralServerModeDeviceAddress: CborByteString? = null,
    ) : NumberLabeledMap() {
        override fun connectLabels() {
            putLabel(0, peripheralServerMode)
            putLabel(1, centralClientMode)
            putLabel(10, peripheralServerModeUUID)
            putLabel(11, centralClientModeUUID)
            putLabel(20, peripheralServerModeDeviceAddress)
        }

        */
/*override fun instanceFromLabels(): TestBleOptions {
            if (labeledItems.value.size == 0) {
                connectLabels()
            }
            return TestBleOptions(
                requiredLabel(0),
                requiredLabel(1),
                optionalLabel(10),
                optionalLabel(11),
                optionalLabel(20)
            )
        }*//*

*/
/*
        init {
            this.connectLabels()
        }*//*


        object Static {
            fun decodeFromTestBleOptions(encodedBleOptions: ByteArray): TestBleOptions {
                val bleOptions = BleOptions.cborDecodeFromBleOptions(encodedBleOptions)
                return TestBleOptions(map.requiredLabel(0), map.requiredLabel(1), map.optionalLabel(10), map.optionalLabel(11), map.optionalLabel(20))
            }
        }
    }
}
*/
