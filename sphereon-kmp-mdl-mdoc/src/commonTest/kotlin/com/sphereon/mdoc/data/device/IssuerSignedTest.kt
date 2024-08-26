package com.sphereon.mdoc.data.device

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.stringToCborByteString
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.cose.CoseCurve
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSignatureAlgorithm
import com.sphereon.crypto.cose.CoseSignatureStructureCbor
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.decodeFromHex
import com.sphereon.kmp.encodeTo
import com.sphereon.mdoc.TestVectors.iso18013_5_IssuerAuthTestVector
import com.sphereon.mdoc.TestVectors.iso18013_5_SignatureStructureTestVector
import com.sphereon.mdoc.TestVectors.sphereonValidEncoded
import com.sphereon.mdoc.TestVectors.sprindFunkeTestVector
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IssuerSignedTest {


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldDecodeAndEncodeISOIssuerAuthTestVector() {

        val issuerAuth = CoseSign1Cbor.Static.cborDecode<Any>(iso18013_5_IssuerAuthTestVector.decodeFromHex())
        assertNotNull(issuerAuth)
        assertNotNull(issuerAuth.payload)
        assertNotNull(issuerAuth.signature)
        assertEquals("ES256", issuerAuth.protectedHeader.alg?.name)
        assertEquals(1, issuerAuth.unprotectedHeader?.x5chain?.value?.size)
        val issuerAuthEncoded = issuerAuth.cborEncode()
        assertTrue { iso18013_5_IssuerAuthTestVector.contains(issuerAuthEncoded.encodeTo(Encoding.HEX)) }

        val sigStructure: CoseSignatureStructureCbor = issuerAuth.toSignature1Structure()
        assertNotNull(sigStructure)
        assertEquals(iso18013_5_SignatureStructureTestVector, sigStructure.cborEncode().encodeTo(Encoding.HEX))

        val isoSigStructure: CoseSignatureStructureCbor =
            CoseSignatureStructureCbor.Static.cborDecode(iso18013_5_SignatureStructureTestVector.decodeFromHex())
        assertNotNull(isoSigStructure)
        assertEquals(iso18013_5_SignatureStructureTestVector, isoSigStructure.cborEncode().encodeTo(Encoding.HEX))


        assertEquals(isoSigStructure, sigStructure) // make sure the equals code works correctly

    }

    @Test
    fun shouldConvertToMdoc() {

        val issuerSigned = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFromHex())

        assertNotNull(issuerSigned)
        val doc = issuerSigned.toDocument()

        assertNotNull(doc)
        assertEquals("eu.europa.ec.eudi.pid.1", doc.docType.value)
        assertNotNull(doc.issuerSigned.issuerAuth)
        assertNotNull(doc.issuerSigned.nameSpaces)
        assertEquals(2, doc.issuerSigned.issuerAuth.unprotectedHeader?.x5chain?.value?.size)
        assertEquals(22, doc.issuerSigned.nameSpaces?.getStringLabel<CborArray<*>>("eu.europa.ec.eudi.pid.1", true)?.value?.size)
    }


    @Test
    fun shouldConvertToMdocJson() {

        val issuerSigned = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFromHex())

        assertNotNull(issuerSigned)
        val doc = issuerSigned.toDocumentJson()

        assertNotNull(doc)
        assertEquals("eu.europa.ec.eudi.pid.1", doc.docType)
        assertNotNull(doc.issuerSigned.issuerAuth)
        assertNotNull(doc.issuerSigned.nameSpaces)
        assertEquals(2, doc.issuerSigned.issuerAuth.unprotectedHeader?.x5chain?.size)
        assertEquals(22, doc.issuerSigned.nameSpaces!!["eu.europa.ec.eudi.pid.1"]?.size)

        println(doc.toJsonDTO<DocumentJson>())
        println(doc.MSO?.toJsonDTO<JsonObject>())
    }



    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldDecodeAndEncodeSprindFunkeIssuerAuthTestVector() {

        val decoded = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFromHex())
        assertNotNull(decoded)
        assertNotNull(decoded.issuerAuth)
        assertNotNull(decoded.issuerAuth.payload)
        assertNotNull(decoded.issuerAuth.signature)
        assertEquals("ES256", decoded.issuerAuth.protectedHeader.alg?.name)
        assertEquals(2, decoded.issuerAuth.unprotectedHeader?.x5chain?.value?.size)
        assertEquals(22, decoded.nameSpaces?.getStringLabel<CborArray<*>>("eu.europa.ec.eudi.pid.1", true)?.value?.size)

        val issuerAuthEncoded = decoded.issuerAuth.cborEncode()
        //val nameSpacesEncoded = decoded.nameSpaces?.cborEncode()
        // Since the IssuerSigned map of the Sprind funke has a different order than ours and the spec, we cannot simply
        // compare the hex strings. This has no impact on the actual handling though as it is a map
        assertTrue { sprindFunkeTestVector.contains(issuerAuthEncoded.toHexString()) }
        // random and digestID are ordered differently, so we cannot test the hext string
        // assertTrue { sprindFunkeTestVector.contains(nameSpacesEncoded?.toHexString() ?: "ERROR if null") }
        assertEquals(sphereonValidEncoded, decoded.cborEncode().toHexString())


    }


    @Test
    fun shouldConvertToJsonForSprindFunkeIssuerAuthTestVector() {

        val issuerSignedCbor = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFromHex())
        assertNotNull(issuerSignedCbor)
        println(issuerSignedCbor)
        val issuerSignedJson = issuerSignedCbor.toJson()
        println(issuerSignedJson)
        assertNotNull(issuerSignedJson)
        assertNotNull(issuerSignedJson.issuerAuth.payload)
        assertNotNull(issuerSignedJson.issuerAuth.signature)
        assertEquals("ES256", issuerSignedJson.issuerAuth.protectedHeader.alg?.name)
        assertEquals(2, issuerSignedJson.issuerAuth.unprotectedHeader?.x5chain?.size)
        assertEquals(22, issuerSignedJson.nameSpaces?.get("eu.europa.ec.eudi.pid.1")?.size)


    }

    @Test
    fun shouldConvertToJsonAndBack() {
        val issuerSignedCbor = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFromHex())
        val issuerSignedJson: IssuerSignedJson = issuerSignedCbor.toJson()
        issuerSignedJson.nameSpaces?.values?.forEach { items -> items.map { item -> println(item.toString()) } }
        val issuerSignedCborFromJson = issuerSignedJson.toCbor()

        assertEquals(issuerSignedCbor, issuerSignedCborFromJson)
    }


    @Test
    fun shouldCreateSigned() {
        val cose = CoseSign1Cbor<Any>(
            payload = "This is the content.".toCborByteString(),
            protectedHeader = CoseHeaderCbor(alg = CoseSignatureAlgorithm.ES256),
            unprotectedHeader = CoseHeaderCbor(kid = "11".stringToCborByteString()),
            signature = CborByteString(
                "8eb33e4ca31d1c465ab05aac34cc6b23d58fef5c083106c4d25a91aef0b0117e2af9a291aa32e14ab834dc56ed2a223444547e01f11d3b0916e5a4c345cacb36".decodeFrom(
                    Encoding.HEX
                )
            )
        )
        val coseKeyCbor = CoseKeyJson(
            kty = CoseKeyType.EC2,
            kid = "11",
            crv = CoseCurve.P_256,
            x = "usWxHK2PmfnHKwXPS54m0kTcGJ90UiglWiGahtagnv8",
            y = "IBOL-C3BttVivg-lSreASjpkttcsz-1rb7btKLv8EX4",
            d = "V8kgd2ZBRuh2dgyVINBUqpPDr7BOMGcF22CQMIUHtNM"
        ).toCbor()
        val sigStructure = cose.toBeSignedCbor(coseKeyCbor, CoseSignatureAlgorithm.ES256)
        println(sigStructure.toCbor().encodeTo(Encoding.HEX))
        println(cose.cborEncode().encodeTo(Encoding.HEX))
    }
}
