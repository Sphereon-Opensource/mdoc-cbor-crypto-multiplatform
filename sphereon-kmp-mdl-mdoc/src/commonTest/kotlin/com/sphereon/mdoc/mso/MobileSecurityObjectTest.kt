package com.sphereon.mdoc.mso

import com.sphereon.cbor.CborHexEncodedItem
import com.sphereon.cbor.CborUInt
import com.sphereon.crypto.cose.COSE_Key
import com.sphereon.cbor.toCborBool
import com.sphereon.kmp.numberToKmpLong
import com.sphereon.kmp.decodeFromHex
import com.sphereon.mdoc.tx.device.BleOptionsCbor
import com.sphereon.mdoc.tx.device.DeviceEngagementCbor
import com.sphereon.mdoc.tx.device.DeviceEngagementSecurityCbor
import com.sphereon.mdoc.tx.device.DeviceRetrievalMethodCbor
import com.sphereon.mdoc.tx.device.DeviceRetrievalMethodType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.expect

class MobileSecurityObjectTest {

    val isoTestVector =
        "a30063312e30018201d818584ba4010220012158205a88d182bce5f42efa59943f33359d2 e8a968ff289d93e5fa444b624343167fe225820b16e8cf858ddc7690407ba61d4c338237a 8cfcf3de6aa672fc60a557aa32fc670281830201a300f401f50b5045efef742b2c4837a9a 3b0e1d05a6917".replace(
            " ",
            ""
        )

    val coseKeyBytes =
        "a4010220012158205a88d182bce5f42e fa59943f33359d2e8a968ff289d93e5f a444b624343167fe225820b16e8cf858 ddc7690407ba61d4c338237a8cfcf3de 6aa672fc60a557aa32fc67".replace(
            " ",
            ""
        )

    @OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
    @Test
    fun shouldEncodeRawDeviceEngagement() {

        val decoded = DeviceEngagementCbor.cborDecode(isoTestVector.decodeFromHex())
        assertNotNull(decoded)
        assertNotNull(decoded.security)


        val coseKey = COSE_Key.cborDecode(coseKeyBytes.decodeFromHex())
        assertEquals(coseKey.kty, decoded.security.eDeviceKeyBytes.kty)
        assertEquals(coseKey.crv, decoded.security.eDeviceKeyBytes.crv)
        assertEquals(coseKey.x, decoded.security.eDeviceKeyBytes.x)
        assertEquals(coseKey.y, decoded.security.eDeviceKeyBytes.y)

        val engagement = DeviceEngagementCbor(
            security = DeviceEngagementSecurityCbor(
                cypherSuite = CborUInt(1),
                eDeviceKeyBytes = coseKey
            ),
            deviceRetrievalMethods = arrayOf(
                DeviceRetrievalMethodCbor(
                    type = DeviceRetrievalMethodType.BLE.toCborItem(),
                    version = CborUInt(1.numberToKmpLong()),
                    retrievalOptions = BleOptionsCbor(
                        peripheralServerMode = false.toCborBool(),
                        centralClientMode = true.toCborBool(),
                        centralClientModeUUID = CborHexEncodedItem("45efef742b2c4837a9a3b0e1d05a6917")
                    )
                )
            )
        )

        val bytes =  engagement.cborEncode()
        println("Encoded object to hex:")
        println(bytes.toHexString())



        expect(isoTestVector) { bytes.toHexString() }

        val result = DeviceEngagementCbor.cborDecode(bytes)
        println("Decoded object:")
        println(result)
        assertEquals(result, engagement)

    }

}
