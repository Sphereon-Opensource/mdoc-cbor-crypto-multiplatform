package com.sphereon.mdoc.tx.device

//import com.sphereon.cbor.cborSerializer

import com.sphereon.cbor.CborHexEncodedItem
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.cose.COSE_Key
import com.sphereon.cbor.toCborBool
import com.sphereon.kmp.numberToKmpLong
import com.sphereon.kmp.decodeFromHex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.expect

class DeviceEngagementTest {

    val isoTestVector =
        "a30063312e30018201d818584ba4010220012158205a88d182bce5f42efa59943f33359d2 e8a968ff289d93e5fa444b624343167fe225820b16e8cf858ddc7690407ba61d4c338237a 8cfcf3de6aa672fc60a557aa32fc670281830201a300f401f50b5045efef742b2c4837a9a 3b0e1d05a6917".replace(
            " ",
            ""
        )

    val isoTestVector2 =
        "91022548721591020263720102110204616301013000110206616301036e6663005102046163010157001a201e016170706c69636174696f6e2f766e642e626c7565746f6f74682e6c652e6f6f6230081b28078080bf2801021c021107c832fff6d26fa0beb34dfcd555d4823a1c11010369736f2e6f72673a31383031333a6e66636e6663015a172b016170706c69636174696f6e2f766e642e7766612e6e616e57030101032302001324fec9a70b97ac9684a4e326176ef5b981c5e8533e5f00298cfccbc35e700a6b020414"

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

      /*  // TODO check this
        val decoded2 = DeviceEngagementCbor.cborDecode(isoTestVector2.decodeHex())
        assertNotNull(decoded2)
        assertNotNull(decoded2.security)
*/

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

        val bytes = engagement.cborEncode()
        println("Encoded object to hex:")
        println(bytes.toHexString())



        expect(isoTestVector) { bytes.toHexString() }

        val result = DeviceEngagementCbor.cborDecode(bytes)
        println("Decoded object:")
        println(result)
        assertEquals(result, engagement)

    }
}
