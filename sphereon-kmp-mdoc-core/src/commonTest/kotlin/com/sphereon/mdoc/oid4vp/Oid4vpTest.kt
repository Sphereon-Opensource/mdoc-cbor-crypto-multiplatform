package com.sphereon.mdoc.oid4vp


import com.sphereon.crypto.DefaultCallbacks
import com.sphereon.crypto.KeyEncoding
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.X509Service
import com.sphereon.crypto.cose.CoseCryptoProviderToCallbackAdapter
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.kms.CoseJoseProvidedKeyResolverService
import com.sphereon.crypto.kms.EcDSACryptoProvider
import com.sphereon.crypto.kms.KeyManagerService
import com.sphereon.json.oid4vpJsonSerializer
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.Uuid
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.decodeFromHex
import com.sphereon.kmp.encodeTo
import com.sphereon.mdoc.MdocSignService
import com.sphereon.mdoc.TestVectors.iso18013_7_pd
import com.sphereon.mdoc.TestVectors.iso18013_7_submission
import com.sphereon.mdoc.TestVectors.pid_docrequest_json_result
import com.sphereon.mdoc.TestVectors.sprindFunkeTestVector
import com.sphereon.mdoc.TestVectors.sprind_funke_pid_pd
import com.sphereon.mdoc.data.device.DeviceResponseCbor
import com.sphereon.mdoc.data.device.IssuerSignedCbor
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Oid4vpTest {

    @Test
    fun shouldTranslatePresentationDefinitionToDocRequest() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(sprind_funke_pid_pd)
        assertEquals("PID-sample-req", pd.id)
        assertEquals(1, pd.input_descriptors.size)
        assertEquals("eu.europa.ec.eudi.pid.1", pd.input_descriptors[0].id)

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

        assertEquals(pid_docrequest_json_result, docRequestJson.toJsonString())
    }


    @Test
    fun shouldApplyPresentationDefinitionToMdoc() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(sprind_funke_pid_pd)
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
        assertEquals(1, pd.input_descriptors.size)
        assertEquals("org.iso.18013.5.1.mDL", pd.input_descriptors[0].id)
        assertEquals("required", pd.input_descriptors[0].constraints.limit_disclosure)
        assertEquals(11, pd.input_descriptors[0].constraints.fields.size)


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
        assertEquals(pd.id, submission.definition_id)
        assertEquals(1, submission.descriptor_map.size)
        assertEquals("org.iso.18013.5.1.mDL", pd.input_descriptors[0].id)
    }

    @Test
    fun shouldSerializePresentationSubmissionFromDefinition() {

        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(iso18013_7_pd)
        val submission = Oid4VPPresentationSubmission.Static.fromPresentationDefinition(pd, "mDL-sample-res")
        val serializedSubmission = oid4vpJsonSerializer.encodeToString(Oid4VPPresentationSubmission.serializer(), submission)
        assertEquals(iso18013_7_submission, serializedSubmission.replace("\n", "").replace(" ", ""))
    }

    @Test
    fun shouldCreateDeviceResponseFromMdoc() = runTest {
        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(sprind_funke_pid_pd)
        // We are using its payload namespaces. We cannot use the test vector itself since we do not have the device key
        val funkeIssuerSigned = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFromHex())
        val nameSpaces = funkeIssuerSigned.nameSpaces
        assertNotNull(nameSpaces)


//        val privateKeyStore = MemoryKeyStoreService(keyVisibility = KeyVisibility.PRIVATE)
        val keyManagerService = KeyManagerService<X509Service>(
            keyManagementSystems = arrayOf(EcDSACryptoProvider()),
            keyResolvers = arrayOf(CoseJoseProvidedKeyResolverService<X509Service>())
        )
        DefaultCallbacks.setCoseCryptoDefault(CoseCryptoProviderToCallbackAdapter(keyManagerService = keyManagerService))

        // both below service have a default, but let's set it explicitly to show you could set another sign service as well
        val mdocSignService = MdocSignService(DefaultCallbacks.coseCrypto())
        val mdocOid4vpService = MdocOid4vpService(signService = mdocSignService)


        val issuerKey = keyManagerService.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val deviceKey = keyManagerService.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        assertNotNull(issuerKey.kmsKeyRef)
        assertNotNull(deviceKey.kmsKeyRef)
        assertNotEquals(issuerKey.kmsKeyRef, deviceKey.kmsKeyRef)

        assertNotNull(issuerKey.toManagedKeyInfo<CoseKeyCbor>(visibility = KeyVisibility.PUBLIC, KeyEncoding.COSE).key.kid)
        assertNotNull(deviceKey.toManagedKeyInfo<CoseKeyCbor>(visibility = KeyVisibility.PUBLIC, KeyEncoding.COSE).key.kid)

        val doc = IssuerSignedCbor.MsoBuilder(nameSpaces = nameSpaces).withDocType(funkeIssuerSigned.MSO!!.docType)
            .withDeviceKeyInfo(
                deviceKey.toManagedKeyInfo<ICoseKeyCbor>(visibility = KeyVisibility.PUBLIC, KeyEncoding.COSE).toResolvedPublicKeyInfo()
            ).withSigningKeyInfo(issuerKey.toManagedKeyInfo<ICoseKeyCbor>(visibility = KeyVisibility.PUBLIC, KeyEncoding.COSE))
            .withValidUntil(DateTimeUtils.Static.DEFAULT.dateTime(epochSeconds = (Clock.System.now().epochSeconds + 1000).toInt()))
            .buildAndSignMdoc(mdocSignService = mdocSignService)

        val applicableDocs = mdocOid4vpService.filterApplicableDocumentsPerInputDescriptor(arrayOf(doc), pd.input_descriptors[0])
        assertEquals(1, applicableDocs.size)
        val mdocNonce = Uuid.v4String()
        val docsAndDescriptors =
            mdocOid4vpService.matchDocumentsAndDescriptors(applicableDocuments = applicableDocs, presentationDefinition = pd, mdocNonce = mdocNonce)
        assertEquals(1, docsAndDescriptors.size)
        assertNotNull(docsAndDescriptors[0].deviceKeyInfo)

        println(doc.cborEncode().encodeTo(Encoding.HEX))

        // Will never work, as we are using a public example for which we do not have the private key. So we fail at the signature part
//        assertFailsWith(IllegalArgumentException::class, "Need to provide a kmsKeyRef", {
val deviceResponse =            mdocOid4vpService.createDeviceResponse(
                matchingDocuments = docsAndDescriptors,
                presentationDefinition = pd,
                clientId = "https://test.com",
                responseUri = "https://test.com/response",
                authorizationRequestNonce = "auth-nonce"
            )
//        })
         assertNotNull(deviceResponse)

        val vpToken = deviceResponse.cborEncode().encodeTo(Encoding.BASE64URL)
        assertTrue(vpToken.startsWith("o2d2ZXJzaW9uYzEuMGlkb2N1bWVudHOBo2dkb2NUeXBld2V1LmV1cm9wYS5lYy5ldWRpLnBpZC4xbGlzc3VlclNpZ25lZKJqbmFtZVNwYWNlc6F3ZXUuZXVyb3BhLmVjLmV1ZGkucGlkLjGH"))
    }

    @Test
    fun shouldDecodeResponseIntoIssuerAuth() {
        val deviceResponse = DeviceResponseCbor.Static.cborDecode(
            "o2d2ZXJzaW9uYzEuMGlkb2N1bWVudHOBomdkb2NUeXBld2V1LmV1cm9wYS5lYy5ldWRpLnBpZC4xbGlzc3VlclNpZ25lZKJqbmFtZVNwYWNlc6F3ZXUuZXVyb3BhLmVjLmV1ZGkucGlkLjGH2BhYVqRoZGlnZXN0SUQAZnJhbmRvbVD2KUO8DhDaXMoup9S-elHYcWVsZW1lbnRJZGVudGlmaWVycHJlc2lkZW50X2NvdW50cnlsZWxlbWVudFZhbHVlYkRF2BhYT6RoZGlnZXN0SUQBZnJhbmRvbVDEYMZP75x5RdBsA09f1C8ScWVsZW1lbnRJZGVudGlmaWVya2FnZV9vdmVyXzEybGVsZW1lbnRWYWx1ZfXYGFhTpGhkaWdlc3RJRANmcmFuZG9tUENuoW9R_2aBusNA5rfDHBxxZWxlbWVudElkZW50aWZpZXJqZ2l2ZW5fbmFtZWxlbGVtZW50VmFsdWVlRVJJS0HYGFhspGhkaWdlc3RJRAhmcmFuZG9tUDKXb5L9OGRMoOqY4ixLrj5xZWxlbWVudElkZW50aWZpZXJrbmF0aW9uYWxpdHlsZWxlbWVudFZhbHVlomV2YWx1ZWJERWtjb3VudHJ5TmFtZWdHZXJtYW552BhYVaRoZGlnZXN0SUQLZnJhbmRvbVChCGnWuG38r-RngGxW963mcWVsZW1lbnRJZGVudGlmaWVyb2lzc3VpbmdfY291bnRyeWxlbGVtZW50VmFsdWViREXYGFh0pGhkaWdlc3RJRA1mcmFuZG9tUL-e8xMKXJN11l_Cb9a-JcBxZWxlbWVudElkZW50aWZpZXJtaXNzdWFuY2VfZGF0ZWxlbGVtZW50VmFsdWWiZG5hbm8bAAAAADUIJsxrZXBvY2hTZWNvbmQbAAAAAGZ5F0DYGFhYpGhkaWdlc3RJRBJmcmFuZG9tUB_9JItYasFm5QDBW68DDthxZWxlbWVudElkZW50aWZpZXJqYmlydGhfZGF0ZWxlbGVtZW50VmFsdWVqMTk2NC0wOC0xMmppc3N1ZXJBdXRohEOhASahGCGCWQJ4MIICdDCCAhugAwIBAgIBAjAKBggqhkjOPQQDAjCBiDELMAkGA1UEBhMCREUxDzANBgNVBAcMBkJlcmxpbjEdMBsGA1UECgwUQnVuZGVzZHJ1Y2tlcmVpIEdtYkgxETAPBgNVBAsMCFQgQ1MgSURFMTYwNAYDVQQDDC1TUFJJTkQgRnVua2UgRVVESSBXYWxsZXQgUHJvdG90eXBlIElzc3VpbmcgQ0EwHhcNMjQwNTMxMDgxMzE3WhcNMjUwNzA1MDgxMzE3WjBsMQswCQYDVQQGEwJERTEdMBsGA1UECgwUQnVuZGVzZHJ1Y2tlcmVpIEdtYkgxCjAIBgNVBAsMAUkxMjAwBgNVBAMMKVNQUklORCBGdW5rZSBFVURJIFdhbGxldCBQcm90b3R5cGUgSXNzdWVyMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEOFBq4YMKg4w5fTifsytwBuJf_7E7VhRPXiNm52S3q1ETIgBdXyDK3kVxGxgeHPivLP3uuMvS6iDEc7qMxmvduKOBkDCBjTAdBgNVHQ4EFgQUiPhCkLErDXPLW2_J0WVeghyw-mIwDAYDVR0TAQH_BAIwADAOBgNVHQ8BAf8EBAMCB4AwLQYDVR0RBCYwJIIiZGVtby5waWQtaXNzdWVyLmJ1bmRlc2RydWNrZXJlaS5kZTAfBgNVHSMEGDAWgBTUVhjAiTjoDliEGMl2Yr-ru8WQvjAKBggqhkjOPQQDAgNHADBEAiAbf5TzkcQzhfWoIoyi1VN7d8I9BsFKm1MWluRph2byGQIgKYkdrNf2xXPjVSbjW_U_5S5vAEC5XxcOanusOBroBbVZAn0wggJ5MIICIKADAgECAhQHkT1BVm2ZRhwO0KMoH8fdVC_vaDAKBggqhkjOPQQDAjCBiDELMAkGA1UEBhMCREUxDzANBgNVBAcMBkJlcmxpbjEdMBsGA1UECgwUQnVuZGVzZHJ1Y2tlcmVpIEdtYkgxETAPBgNVBAsMCFQgQ1MgSURFMTYwNAYDVQQDDC1TUFJJTkQgRnVua2UgRVVESSBXYWxsZXQgUHJvdG90eXBlIElzc3VpbmcgQ0EwHhcNMjQwNTMxMDY0ODA5WhcNMzQwNTI5MDY0ODA5WjCBiDELMAkGA1UEBhMCREUxDzANBgNVBAcMBkJlcmxpbjEdMBsGA1UECgwUQnVuZGVzZHJ1Y2tlcmVpIEdtYkgxETAPBgNVBAsMCFQgQ1MgSURFMTYwNAYDVQQDDC1TUFJJTkQgRnVua2UgRVVESSBXYWxsZXQgUHJvdG90eXBlIElzc3VpbmcgQ0EwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARgbN3AUOdzv4qfmJsC8I4zyR7vtVDGp8xzBkvwhogD5YJE5wJ-Zj-CIf3aoyu7mn-TI6K8TREL8ht0w428OhTJo2YwZDAdBgNVHQ4EFgQU1FYYwIk46A5YhBjJdmK_q7vFkL4wHwYDVR0jBBgwFoAU1FYYwIk46A5YhBjJdmK_q7vFkL4wEgYDVR0TAQH_BAgwBgEB_wIBADAOBgNVHQ8BAf8EBAMCAYYwCgYIKoZIzj0EAwIDRwAwRAIgYSbvCRkoe39q1vgx0WddbrKufAxRPa7XfqB22XXRjqECIG5MWq9Vi2HWtvHMI_TFZkeZAr2RXLGfwY99fbsQjPOzWQRA2BhZBDumZ2RvY1R5cGV3ZXUuZXVyb3BhLmVjLmV1ZGkucGlkLjFndmVyc2lvbmMxLjBsdmFsaWRpdHlJbmZvo2ZzaWduZWR0MjAyNC0wNi0yNFQwNjo1MDo0MFppdmFsaWRGcm9tdDIwMjQtMDYtMjRUMDY6NTA6NDBaanZhbGlkVW50aWx0MjAyNC0wNy0wOFQwNjo1MDo0MFpsdmFsdWVEaWdlc3RzoXdldS5ldXJvcGEuZWMuZXVkaS5waWQuMbYAWCDJVfFwuYp2QoZROAvEN2pyUZ1KM8pEWRZXfdWrF1HkigFYIHhpl7kR5NAjeLSFJd0LsjMB9_ZeOBi-pYiOSwG78rrEAlggEih2FMRoq01sCrA8gZ-r_pUqi7add99aSg_l9iuV7w8DWCD9umaT-ULFoZSewraVNXFFWf3iNm5rgj75OQAy7n-1HQRYIL8xH7_OLXmsTruVMI1AInTjtDyPiDkk3ZaljsXFMaeYBVgg2-7WIwtpcZgVI3ZpKiFOqf8cV_R8G20adAqk3xLmaR8GWCCMFjcNb1Yp0rw86h1OOYCPzIhE-Dt5yWCQ7BTpNbZBuwdYIEzmGyjypgomuuwlwyp44zLi6sXT11ZNoyDAMKEsNP0pCFggI2ENhbCnOrZsVvqNE1GJe13ygY7MMU_Hv7l7j60Y5BgJWCBDZb6ztiG-09jmZNNc3Qi4e1OhyqtNmrOxzuzCtMYKcgpYIDGYllJw4PxQlyaeiI-a0qaeD9C3qh2hKXtvYYol928zC1gg4etokah75K55-qzJ6_FtE2KtAF9gy3gzcTeirdZ3LHwMWCDnCnqeX1M1iJe3LH2qc0kJOXQHYUEubpqVi2c4wtt3xQ1YIL7dVtgkdG9n2pDvrBtgY21i7X7YyiVCe-p61mtghwjnDlggQk4FkmKScm6oCwHtt5Og5E_1SQfuWpFIMdj0x8ZCS0wPWCBGMDXYqqBPDqeqBoFn3IKJSZWcdMj7KyU1ZtNOZ3OE6hBYIJyzjluOe_VlYSQw1aIBcrsnnF2czy5ypChycRfi0nrOEVggKOd_n9xKuZDdnak-vQ1zrIzSWLxJIlPgJMpLEn2FuLYSWCBHx1eoCb1ydVj_EGIKUOYPCyEjAgP5HxN-J_zSZUwkKBNYIN0hCZPdhjF4pU-LVEoQi7FdOSF3lrQ8EimA7C31NcVhFFggxtk6j0328cyjnwNoWKCUgvg1Uk37Bktpzb4atlRT5VIVWCAMujq43dRJg7XilJJL0z-hxQoLUpkzO2tq6H6LazG0uW1kZXZpY2VLZXlJbmZvoWlkZXZpY2VLZXmkAQIgASFYIMrI7GWNvKwCXqwcJmkBMyIRAXejiET9PRAFCMhJEfo9IlggEvXLy65sT8QyzLnWsC7aIM1eem2029awDcWI7WO0ES9vZGlnZXN0QWxnb3JpdGhtZ1NIQS0yNTZYQLVKBk4WMWUjTFWSwUuz7vCPNCAqw5x7HIBHVr1H_gC5WOEXxBaFlnxHYBjBguFSfLe5e-7t82ySdef7uvo6d2Nmc3RhdHVzAA".decodeFrom(
                Encoding.BASE64URL
            )
        )
        assertNotNull(deviceResponse)
        val doc = deviceResponse.documents?.get(0)
        assertNotNull(doc)
        assertNotNull(doc.MSO)


    }

}
