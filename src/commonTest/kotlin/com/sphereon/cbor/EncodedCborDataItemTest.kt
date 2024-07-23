@file:OptIn(ExperimentalStdlibApi::class)

package com.sphereon.cbor

import com.sphereon.cbor.cose.CoseSignatureAlgorithm
import com.sphereon.mdoc.data.DataElementCbor
import com.sphereon.mdoc.data.device.DeviceItemsRequestCbor
import com.sphereon.mdoc.data.device.DeviceRequestCbor
import com.sphereon.mdoc.data.device.deviceItemsRequestBuilder
import com.sphereon.mdoc.data.device.docRequestBuilder
import com.sphereon.mdoc.data.mdl.Mdl.MDL_NAMESPACE_CBOR
import com.sphereon.mdoc.data.mdl.MdlDefs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EncodedCborDataItemTest {


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldEncodeItemsRequest() {
        val deviceRequest: DeviceItemsRequestCbor =
            DeviceItemsRequestCbor.Builder(
                docType = MDL_NAMESPACE_CBOR,
            )

                .nameSpace("org.iso.18013.5.1")
                .add(MdlDefs.family_name.identifier, true)
                .add("document_number", true)
                .addElements(
                    DataElementCbor("driving_privileges".toCborString(), true.toCborBool()),
                    MdlDefs.issue_date.intentToRetain(true),
                    MdlDefs.expiry_date.intentToRetain(/*false is the default*/),
                    DataElementCbor("portrait".toCborString(), false.toCborBool())
                ).end().build()


        val encodeDeviceRequest = deviceRequest.cborEncode()
        println(encodeDeviceRequest.toHexString())

        val decodedDeviceRequest = DeviceItemsRequestCbor.decodeCbor(encodeDeviceRequest)

        assertEquals(deviceRequest.docType, decodedDeviceRequest.docType)
    }


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldEncodeDeviceRequest() {
        val deviceRequest: DeviceRequestCbor = DeviceRequestCbor(
            version = "1.0".toCborString(),
            docRequests = CborArray(
                mutableListOf(
                    docRequestBuilder()
                        .docType("org.iso.18013.5.1.mDL")

                        .nameSpace("org.iso.18013.5.1")
                        // Add identifier with intent to retain as argument
                        // TODO: Serialization should automatically take the identifier for default elements
//                    .add(MdlDefaultElements.family_name.definition.identifier, true)
                        .add("family_name", true)
                        .add("document_number", true)
                        // Add with a single data element
                        .addElements(DataElementCbor("driving_privileges".toCborString(), true.toCborBool()))
                        .addElements(
                            // Add multiple data elements
                            MdlDefs.issue_date.intentToRetain(true),
                            MdlDefs.expiry_date.intentToRetain(true),
                            DataElementCbor("portrait".toCborString(), false.toCborBool())

                        ).end().buildDocRequest()

                )
            )
        )


        val encodedDeviceRequest = deviceRequest.cborEncode()
        println(encodedDeviceRequest.toHexString())

        val decodedDeviceRequest = DeviceRequestCbor.cborDecode(encodedDeviceRequest)

        assertEquals(deviceRequest.version, decodedDeviceRequest.version)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun isoTestVector() {
        val decodedDeviceRequest = DeviceRequestCbor.cborDecode(ISO_TESTVECTOR.hexToByteArray())
        assertNotNull(decodedDeviceRequest.docRequests)
        assertNotNull(decodedDeviceRequest.docRequests.value[0])
        assertEquals(
            CoseSignatureAlgorithm.ES256,
            decodedDeviceRequest.docRequests.value[0].readerAuth?.protectedHeader?.alg
        )
        assertNotNull(decodedDeviceRequest.docRequests.value[0].readerAuth?.unprotectedHeader?.x5chain)
        assertNotNull(decodedDeviceRequest.docRequests.value[0].readerAuth?.signature)

        val encoded = decodedDeviceRequest.cborEncode()
//        cborSerializer.toDiagnosticsEncoded(encoded, setOf(DiagnosticOption.PRETTY_PRINT))
        assertEquals(ISO_TESTVECTOR, encoded.toHexString())
    }


    private val ISO_TESTVECTOR =
        "a26776657273696f6e63312e306b646f63526571756573747381a26c6974656d735265717 5657374d8185893a267646f6354797065756f72672e69736f2e31383031332e352e312e6d 444c6a6e616d65537061636573a1716f72672e69736f2e31383031332e352e31a66b66616 d696c795f6e616d65f56f646f63756d656e745f6e756d626572f57264726976696e675f70 726976696c65676573f56a69737375655f64617465f56b6578706972795f64617465f5687 06f727472616974f46a726561646572417574688443a10126a118215901b7308201b33082 0158a00302010202147552715f6add323d4934a1ba175dc945755d8b50300a06082a8648c e3d04030230163114301206035504030c0b72656164657220726f6f74301e170d32303130 30313030303030305a170d3233313233313030303030305a3011310f300d06035504030c0 67265616465723059301306072a8648ce3d020106082a8648ce3d03010703420004f8912e e0f912b6be683ba2fa0121b2630e601b2b628dff3b44f6394eaa9abdbcc2149d29d6ff1a3 e091135177e5c3d9c57f3bf839761eed02c64dd82ae1d3bbfa38188308185301c0603551d 1f041530133011a00fa00d820b6578616d706c652e636f6d301d0603551d0e04160414f2d fc4acafc5f30b464fada20bfcd533af5e07f5301f0603551d23041830168014cfb7a881ba ea5f32b6fb91cc29590c50dfac416e300e0603551d0f0101ff04040302078030150603551 d250101ff040b3009060728818c5d050106300a06082a8648ce3d04030203490030460221 00fb9ea3b686fd7ea2f0234858ff8328b4efef6a1ef71ec4aae4e307206f9214930221009 b94f0d739dfa84cca29efed529dd4838acfd8b6bee212dc6320c46feb839a35f658401f34 00069063c189138bdcd2f631427c589424113fc9ec26cebcacacfcdb9695d28e99953beca bc4e30ab4efacc839a81f9159933d192527ee91b449bb7f80bf".replace(
            " ",
            ""
        )
}
