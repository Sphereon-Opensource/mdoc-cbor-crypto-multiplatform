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
import com.sphereon.crypto.providers.CoseCryptoProviderToCallbackAdapterJS
import com.sphereon.json.oid4vpJsonSerializer
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.Uuid
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.decodeFromHex
import com.sphereon.kmp.encodeTo
import com.sphereon.mdoc.MdocSignService
import com.sphereon.mdoc.TestVectors.sprindFunkeTestVector
import com.sphereon.mdoc.TestVectors.sprind_funke_pid_pd
import com.sphereon.mdoc.data.device.DeviceResponseCbor
import com.sphereon.mdoc.data.device.IssuerSignedCbor
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.sujanpoudel.utils.platformIdentifier.Platform
import me.sujanpoudel.utils.platformIdentifier.platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Oid4vpTestJS {

    @Test
    fun shouldCreateDeviceResponseFromMdoc() = runTest {
        val pd = oid4vpJsonSerializer.decodeFromString<Oid4VPPresentationDefinition>(sprind_funke_pid_pd)
        // We are using its payload namespaces. We cannot use the test vector itself since we do not have the device key
        val funkeIssuerSigned = IssuerSignedCbor.Static.cborDecode(sprindFunkeTestVector.decodeFromHex())
        val nameSpaces = funkeIssuerSigned.nameSpaces
        assertNotNull(nameSpaces)


        val keyManagerService = KeyManagerService<X509Service>(
            keyManagementSystems = arrayOf(EcDSACryptoProvider()),
            keyResolvers = arrayOf(CoseJoseProvidedKeyResolverService<X509Service>())
        )
        val currentPlatform = platform()
        when (currentPlatform) {
            is Platform.JS.Node, is Platform.JS.Browser -> {
                DefaultCallbacks.setCoseCryptoDefault(CoseCryptoProviderToCallbackAdapterJS(keyManagerService = keyManagerService))
            }
            else -> {
                println("Test cannot run outside of JS")
                return@runTest
            }
        }

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
            .buildAndSignMdoc(mdocSignService = mdocSignService, requireDeviceX5Chain = false)

        val applicableDocs = mdocOid4vpService.filterApplicableDocumentsPerInputDescriptor(arrayOf(doc), pd.input_descriptors[0])
        assertEquals(1, applicableDocs.size)
        val mdocNonce = Uuid.v4String()
        val docsAndDescriptors =
            mdocOid4vpService.matchDocumentsAndDescriptors(applicableDocuments = applicableDocs, presentationDefinition = pd, mdocNonce = mdocNonce)
        assertEquals(1, docsAndDescriptors.size)
        assertNotNull(docsAndDescriptors[0].deviceKeyInfo)

        val deviceResponse = mdocOid4vpService.createDeviceResponse(
            matchingDocuments = docsAndDescriptors,
            presentationDefinition = pd,
            clientId = "https://test.com",
            responseUri = "https://test.com/response",
            authorizationRequestNonce = "auth-nonce"
        )

        assertNotNull(deviceResponse)
        val vpToken = deviceResponse.cborEncode().encodeTo(Encoding.BASE64URL)
        assertTrue(vpToken.startsWith("o2d2ZXJzaW9uYzEuMGlkb2N1bWVudHOBo2dkb2NUeXBld2V1LmV1cm9wYS5lYy5ldWRpLnBpZC4xbGlzc3VlclNpZ25lZKJqbmFtZVNwYWNlc6F3ZXUuZXVyb3BhLmVjLmV1ZGkucGlkLjGH"))
    }

}
