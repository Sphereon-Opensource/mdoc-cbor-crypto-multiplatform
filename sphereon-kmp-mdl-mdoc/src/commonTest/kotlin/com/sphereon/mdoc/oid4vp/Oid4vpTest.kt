package com.sphereon.mdoc.oid4vp



import com.sphereon.json.oid4vpJsonSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals

class Oid4vpTest {


    @OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
    @Test
    fun shouldEncodeDecodePresentationDefinition() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(iso18013_7_pd)

        println(pd)
        assertEquals("mDL-sample-req", pd.id)
        assertEquals(1, pd.inputDescriptors.size)
        assertEquals("org.iso.18013.5.1.mDL", pd.inputDescriptors[0].id)
        assertEquals(Oid4VPLimitDisclosure.REQUIRED, pd.inputDescriptors[0].constraints.limitDisclosure)
        assertEquals(11, pd.inputDescriptors[0].constraints.fields.size)


        val serialized = oid4vpJsonSerializer.encodeToString(Oid4VPPresentationDefinition.serializer(), pd)
        println(serialized)

        // We cannot compare strings as the order of a JSON object is undefined (except for arrays). Removing the newlines because of pretty printing
        assertEquals(iso18013_7_pd.length, serialized.replace("\n", "").replace(" ", "").length)
    }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
    @Test
    fun shouldCreatePresentationSubmissionFromDefinition() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(iso18013_7_pd)
        val submission = Oid4VPPresentationSubmission.Static.fromPresentationDefinition(pd, "mDL-sample-res")
        assertEquals("mDL-sample-res", submission.id)
        assertEquals(pd.id, submission.definitionId)
        assertEquals(1, submission.descriptorMap.size)
        assertEquals("org.iso.18013.5.1.mDL", pd.inputDescriptors[0].id)
    }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
    @Test
    fun shouldSerializePresentationSubmissionFromDefinition() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(iso18013_7_pd)
        val submission = Oid4VPPresentationSubmission.Static.fromPresentationDefinition(pd, "mDL-sample-res")
        val serializedSubmission = oid4vpJsonSerializer.encodeToString(Oid4VPPresentationSubmission.serializer(), submission)
        assertEquals(iso18013_7_submission, serializedSubmission.replace("\n", "").replace(" ", ""))
    }

    val iso18013_7_submission = ("{\n" +
            "\"definition_id\": \"mDL-sample-req\",\n" +
            "\"id\": \"mDL-sample-res\",\n" +
            "\"descriptor_map\": [\n" +
            "{\n" +
            "\"id\": \"org.iso.18013.5.1.mDL\",\n" +
            "\"format\": \"mso_mdoc\",\n" +
            "\"path\": \"\$\"\n" +
            "}\n" +
            "]\n" +
            "}").replace("\n", "").replace(" ", "")

    val iso18013_7_pd = ("{\n" +
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
