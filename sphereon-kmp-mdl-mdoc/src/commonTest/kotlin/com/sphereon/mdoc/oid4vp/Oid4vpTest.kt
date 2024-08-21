package com.sphereon.mdoc.oid4vp


//import com.sphereon.cbor.cborSerializer

import com.sphereon.mdoc.mdocJsonSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceEngagementTest {


    @OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
    @Test
    fun shouldEncodeDecodePresentationDefinition() {

        val pd = mdocJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(iso1813015_07_pd)

        println(pd)
        assertEquals("mDL-sample-req", pd.id)
        assertEquals(1, pd.inputDescriptors.size)
        assertEquals("org.iso.18013.5.1.mDL", pd.inputDescriptors[0].id)
        assertEquals(Oid4VPLimitDisclosure.REQUIRED, pd.inputDescriptors[0].constraints.limitDisclosure)
        assertEquals(11, pd.inputDescriptors[0].constraints.fields.size)


        val serialized = mdocJsonSerializer.encodeToString(Oid4VPPresentationDefinition.serializer(), pd)
        println(serialized)

        // We cannot compare strings as the order of a JSON object is undefined (except for arrays)
        assertEquals(iso1813015_07_pd.length, serialized.length)
    }


    val iso1813015_07_pd = ("{\n" +
            "        \"id\": \"mDL-sample-req\",\n" +
            "        \"input_descriptors\": [\n" +
            "        {\n" +
            "            \"id\": \"org.iso.18013.5.1.mDL \",\n" +
            "            \"format\": {\n" +
            "            \"mso_mdoc\": {\n" +
            "            \"alg\": [\n" +
            "            \"ES256\",\n" +
            "            \"ES384\",\n" +
            "            \"ES512\",\n" +
            "            \"EdDSA\"\n" +
//            "            \"ESB256\",\n" +
//            "            \"ESB320\",\n" +
//            "            \"ESB384\",\n" +
//            "            \"ESB512\"\n" +
            "            ]\n" +
            "        }\n" +
            "        },\n" +
            "            \"constraints\": {\n" +
            "            \"fields\": [\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['birth_date']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['document_number']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['driving_privileges']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['expiry_date']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['family_name']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['given_name']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['issue_date']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['issuing_authority']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['issuing_country']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['portrait']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"path\": [\n" +
            "                \"\$['org.iso.18013.5.1']['un_distinguishing_sign']\"\n" +
            "                ],\n" +
            "                \"intent_to_retain\": false\n" +
            "            }\n" +
            "            ],\n" +
            "            \"limit_disclosure\": \"required\"\n" +
            "        }\n" +
            "        }\n" +
            "        ]\n" +
            "    }").replace("\n", "").replace("\r", "").replace(" ", "")

}
