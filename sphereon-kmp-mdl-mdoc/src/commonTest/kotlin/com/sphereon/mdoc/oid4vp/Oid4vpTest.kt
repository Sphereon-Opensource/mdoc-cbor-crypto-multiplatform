package com.sphereon.mdoc.oid4vp


import com.sphereon.json.oid4vpJsonSerializer
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import com.sphereon.mdoc.data.device.IssuerSignedCbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class Oid4vpTest {

    @Test
    fun shouldTranslatePresentationDefinitionToDocRequest() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(pid_pd)

        println(pd)
        assertEquals("PID-sample-req", pd.id)
        assertEquals(1, pd.inputDescriptors.size)
        assertEquals("eu.europa.ec.eudi.pid.1", pd.inputDescriptors[0].id)

        val docRequest = pd.toDocRequest()
        assertNotNull(docRequest)
        assertEquals("eu.europa.ec.eudi.pid.1", docRequest.getDocType())
        assertEquals("eu.europa.ec.eudi.pid.1", docRequest.getNameSpaces()[0])
        assertEquals(1, docRequest.itemsRequest.nameSpaces.value.values.size)
        assertEquals(7, docRequest.itemsRequest.nameSpaces.value.values.first().value.values.size)
        assertEquals("eu.europa.ec.eudi.pid.1", docRequest.itemsRequest.getNameSpaces()[0])
        assertEquals(7, docRequest.itemsRequest.getIdentifiers(docRequest.itemsRequest.getNameSpaces()[0]).size)

        val docRequestJson = docRequest.toJson()
        assertNotNull(docRequestJson)
        assertEquals("eu.europa.ec.eudi.pid.1", docRequestJson.getDocType())
        assertEquals(1, docRequestJson.getNameSpaces().size)
        assertEquals(7, docRequestJson.getIdentifiers(docRequestJson.getNameSpaces()[0]).size)

        assertEquals(pid_docrequest_json_result, com.sphereon.json.mdocJsonSerializer.encodeToString(docRequestJson.toJsonDTO<JsonObject>()))
    }


    @Test
    fun shouldApplyPresentationDefinitionToMdoc() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(pid_pd)
        val mdoc = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFrom(Encoding.HEX)).toDocument()

        val issuerSigned = mdoc.limitDisclosures(pd.toDocRequest())

        assertNotNull(issuerSigned)
        // We only should have 7 disclosed items from 22
        assertEquals(7, issuerSigned.nameSpaces?.value?.values?.first()?.value?.size)
        assertEquals(22, issuerSigned.MSO?.valueDigests?.value?.values?.first()?.value?.size)
    }


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

    val pid_docrequest_json_result = """
        {
            "itemsRequest": {
                "docType": "eu.europa.ec.eudi.pid.1",
                "nameSpaces": {
                    "eu.europa.ec.eudi.pid.1": {
                        "resident_country": false,
                        "age_over_12": false,
                        "given_name": true,
                        "nationality": true,
                        "issuing_country": false,
                        "issuance_date": false,
                        "birth_date": false
                    }
                },
                "requestInfo": null
            },
            "readerAuth": null
        }
    """.trimIndent()
    val pid_pd = """
        {
          "id": "PID-sample-req",
          "input_descriptors": [
            {
              "id": "eu.europa.ec.eudi.pid.1",
              "format": {
                "mso_mdoc": {
                  "alg": [
                    "ES256",
                    "ES384",
                    "ES512",
                    "EdDSA"
                  ]
                }
              },
              "constraints": {
                "fields": [
                  {
                    "path": [
                      "${'$'}['eu.europa.ec.eudi.pid.1']['resident_country']"
                    ],
                    "intent_to_retain": false
                  },
                  {
                    "path": [
                      "${'$'}['eu.europa.ec.eudi.pid.1']['age_over_12']"
                    ],
                    "intent_to_retain": false
                  },
                  {
                    "path": [
                      "${'$'}['eu.europa.ec.eudi.pid.1']['given_name']"
                    ],
                    "intent_to_retain": true
                  },
                  {
                    "path": [
                      "${'$'}['eu.europa.ec.eudi.pid.1']['nationality']"
                    ],
                    "intent_to_retain": true
                  },
                  {
                    "path": [
                      "${'$'}['eu.europa.ec.eudi.pid.1']['issuing_country']"
                    ],
                    "intent_to_retain": false
                  },
                  {
                    "path": [
                      "${'$'}['eu.europa.ec.eudi.pid.1']['issuance_date']"
                    ],
                    "intent_to_retain": false
                  },
                  {
                    "path": [
                      "${'$'}['eu.europa.ec.eudi.pid.1']['birth_date']"
                    ],
                    "intent_to_retain": false
                  }
                ],
                "limit_disclosure": "required"
              }
            }
          ]
        }
    """.trimIndent()

    private val sprindFunkeTestVector =
        "a26a697373756572417574688443a10126a1182182590278308202743082021ba003020102020102300a06082a8648ce3d040302308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e67204341301e170d3234303533313038313331375a170d3235303730353038313331375a306c310b3009060355040613024445311d301b060355040a0c1442756e646573647275636b6572656920476d6248310a3008060355040b0c01493132303006035504030c29535052494e442046756e6b6520455544492057616c6c65742050726f746f74797065204973737565723059301306072a8648ce3d020106082a8648ce3d0301070342000438506ae1830a838c397d389fb32b7006e25fffb13b56144f5e2366e764b7ab511322005d5f20cade45711b181e1cf8af2cfdeeb8cbd2ea20c473ba8cc66bddb8a3819030818d301d0603551d0e0416041488f84290b12b0d73cb5b6fc9d1655e821cb0fa62300c0603551d130101ff04023000300e0603551d0f0101ff040403020780302d0603551d1104263024822264656d6f2e7069642d6973737565722e62756e646573647275636b657265692e6465301f0603551d23041830168014d45618c08938e80e588418c97662bfabbbc590be300a06082a8648ce3d040302034700304402201b7f94f391c43385f5a8228ca2d5537b77c23d06c14a9b531696e4698766f219022029891dacd7f6c573e35526e35bf53fe52e6f0040b95f170e6a7bac381ae805b559027d3082027930820220a003020102021407913d41566d99461c0ed0a3281fc7dd542fef68300a06082a8648ce3d040302308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e67204341301e170d3234303533313036343830395a170d3334303532393036343830395a308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e672043413059301306072a8648ce3d020106082a8648ce3d03010703420004606cddc050e773bf8a9f989b02f08e33c91eefb550c6a7cc73064bf0868803e58244e7027e663f8221fddaa32bbb9a7f9323a2bc4d110bf21b74c38dbc3a14c9a3663064301d0603551d0e04160414d45618c08938e80e588418c97662bfabbbc590be301f0603551d23041830168014d45618c08938e80e588418c97662bfabbbc590be30120603551d130101ff040830060101ff020100300e0603551d0f0101ff040403020186300a06082a8648ce3d040302034700304402206126ef0919287b7f6ad6f831d1675d6eb2ae7c0c513daed77ea076d975d18ea102206e4c5aaf558b61d6b6f1cc23f4c566479902bd915cb19fc18f7d7dbb108cf3b3590440d81859043ba667646f63547970657765752e6575726f70612e65632e657564692e7069642e316776657273696f6e63312e306c76616c6964697479496e666fa3667369676e656474323032342d30362d32345430363a35303a34305a6976616c696446726f6d74323032342d30362d32345430363a35303a34305a6a76616c6964556e74696c74323032342d30372d30385430363a35303a34305a6c76616c756544696765737473a17765752e6575726f70612e65632e657564692e7069642e31b6005820c955f170b98a76428651380bc4376a72519d4a33ca445916577dd5ab1751e48a015820786997b911e4d02378b48525dd0bb23301f7f65e3818bea5888e4b01bbf2bac402582012287614c468ab4d6c0ab03c819fabfe952a8bb69d77df5a4a0fe5f62b95ef0f035820fdba6693f942c5a1949ec2b69535714559fde2366e6b823ef9390032ee7fb51d045820bf311fbfce2d79ac4ebb95308d402274e3b43c8f883924dd96a58ec5c531a798055820dbeed6230b697198152376692a214ea9ff1c57f47c1b6d1a740aa4df12e6691f0658208c16370d6f5629d2bc3cea1d4e39808fcc8844f83b79c96090ec14e935b641bb0758204ce61b28f2a60a26baec25c32a78e332e2eac5d3d7564da320c030a12c34fd2908582023610d85b0a73ab66c56fa8d1351897b5df2818ecc314fc7bfb97b8fad18e4180958204365beb3b621bed3d8e664d35cdd08b87b53a1caab4d9ab3b1ceecc2b4c60a720a58203198965270e0fc5097269e888f9ad2a69e0fd0b7aa1da1297b6f618a25f76f330b5820e1eb6891a87be4ae79faacc9ebf16d1362ad005f60cb78337137a2add6772c7c0c5820e70a7a9e5f53358897b72c7daa73490939740761412e6e9a958b6738c2db77c50d5820bedd56d824746f67da90efac1b60636d62ed7ed8ca25427bea7ad66b608708e70e5820424e05926292726ea80b01edb793a0e44ff54907ee5a914831d8f4c7c6424b4c0f5820463035d8aaa04f0ea7aa068167dc828949959c74c8fb2b253566d34e677384ea1058209cb38e5b8e7bf565612430d5a20172bb279c5d9ccf2e72a428727117e2d27ace11582028e77f9fdc4ab990dd9da93ebd0d73ac8cd258bc492253e024ca4b127d85b8b612582047c757a809bd727558ff10620a50e60f0b21230203f91f137e27fcd2654c2428135820dd210993dd863178a54f8b544a108bb15d39217796b43c122980ec2df535c561145820c6d93a8f4df6f1cca39f036858a09482f835524dfb064b69cdbe1ab65453e5521558200cba3ab8ddd44983b5e294924bd33fa1c50a0b5299333b6b6ae87e8b6b31b4b96d6465766963654b6579496e666fa1696465766963654b6579a401022001215820cac8ec658dbcac025eac1c2669013322110177a38844fd3d100508c84911fa3d22582012f5cbcbae6c4fc432ccb9d6b02eda20cd5e7a6db4dbd6b00dc588ed63b4112f6f646967657374416c676f726974686d675348412d3235365840b54a064e163165234c5592c14bb3eef08f34202ac39c7b1c804756bd47fe00b958e117c41685967c476018c182e1527cb7b97beeedf36c9275e7fbbafa3a77636a6e616d65537061636573a17765752e6575726f70612e65632e657564692e7069642e3196d8185856a46672616e646f6d50f62943bc0e10da5cca2ea7d4be7a51d8686469676573744944006c656c656d656e7456616c756562444571656c656d656e744964656e746966696572707265736964656e745f636f756e747279d818584fa46672616e646f6d50c460c64fef9c7945d06c034f5fd42f12686469676573744944016c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3132d818585ba46672616e646f6d502a3796b791b8af9faab59cad92f3c263686469676573744944026c656c656d656e7456616c7565664741424c455271656c656d656e744964656e7469666965727166616d696c795f6e616d655f6269727468d8185853a46672616e646f6d50436ea16f51ff6681bac340e6b7c31c1c686469676573744944036c656c656d656e7456616c7565654552494b4171656c656d656e744964656e7469666965726a676976656e5f6e616d65d8185854a46672616e646f6d50b4a6888f7b7431e7c2569ad3fb43f586686469676573744944046c656c656d656e7456616c75651907ac71656c656d656e744964656e7469666965726e6167655f62697274685f79656172d818584fa46672616e646f6d50bbb727e77ffa206d53880cfd6a757654686469676573744944056c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3138d818584fa46672616e646f6d50913d8c29321d7afbedc882b06abcf887686469676573744944066c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3231d8185856a46672616e646f6d506bb9375f0edf3b4a049448a97b97a6b1686469676573744944076c656c656d656e7456616c7565654bc3964c4e71656c656d656e744964656e7469666965726d7265736964656e745f63697479d818586ca46672616e646f6d5032976f92fd38644ca0ea98e22c4bae3e686469676573744944086c656c656d656e7456616c7565a26576616c75656244456b636f756e7472794e616d65674765726d616e7971656c656d656e744964656e7469666965726b6e6174696f6e616c697479d8185859a46672616e646f6d50f89c1dca7891017e2ee84d069480a99c686469676573744944096c656c656d656e7456616c75656a4d55535445524d414e4e71656c656d656e744964656e7469666965726b66616d696c795f6e616d65d8185855a46672616e646f6d50f325da430ba319bc86950c9fe9b12ec96864696765737449440a6c656c656d656e7456616c7565664245524c494e71656c656d656e744964656e7469666965726b62697274685f706c616365d8185855a46672616e646f6d50a10869d6b86dfcafe467806c56f7ade66864696765737449440b6c656c656d656e7456616c756562444571656c656d656e744964656e7469666965726f69737375696e675f636f756e747279d818584fa46672616e646f6d50a9ba374cf36fea2966eedbe547897f186864696765737449440c6c656c656d656e7456616c7565f471656c656d656e744964656e7469666965726b6167655f6f7665725f3635d818586ca46672616e646f6d50bf9ef3130a5c9375d65fc26fd6be25c06864696765737449440d6c656c656d656e7456616c7565a2646e616e6f1a350826cc6b65706f63685365636f6e641a6679174071656c656d656e744964656e7469666965726d69737375616e63655f64617465d818586aa46672616e646f6d503ea08aca65498463c00e537bb482e4da6864696765737449440e6c656c656d656e7456616c7565a2646e616e6f1a350826cc6b65706f63685365636f6e641a668b8c4071656c656d656e744964656e7469666965726b6578706972795f64617465d8185863a46672616e646f6d50b409df84e488dc2584c728dcee8ea5e56864696765737449440f6c656c656d656e7456616c756570484549444553545241e1ba9e4520313771656c656d656e744964656e7469666965726f7265736964656e745f737472656574d818584fa46672616e646f6d500527ee9713ffc129bc594277d630fd53686469676573744944106c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3136d818585da46672616e646f6d50c0caf17c36e5bb654e3258f16564443d686469676573744944116c656c656d656e7456616c756565353131343771656c656d656e744964656e746966696572747265736964656e745f706f7374616c5f636f6465d8185858a46672616e646f6d501ffd248b586ac166e500c15baf030ed8686469676573744944126c656c656d656e7456616c75656a313936342d30382d313271656c656d656e744964656e7469666965726a62697274685f64617465d8185857a46672616e646f6d505a5006cd2023aa4ebadb11a0caa9bb52686469676573744944136c656c656d656e7456616c756562444571656c656d656e744964656e7469666965727169737375696e675f617574686f72697479d818584fa46672616e646f6d50b720f2c8a884c6e645866b084b5335db686469676573744944146c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3134d8185851a46672616e646f6d50086d133424e77659fa6c3259ab31631a686469676573744944156c656c656d656e7456616c7565183b71656c656d656e744964656e7469666965726c6167655f696e5f7965617273".replace(
            " ",
            ""
        )

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
