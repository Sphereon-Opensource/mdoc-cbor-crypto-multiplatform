package com.sphereon.mdoc

import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.KeyType
import com.sphereon.kmp.decodeFromHex
import com.sphereon.mdoc.transfer.device.SessionTranscriptCbor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionTest {

    /**
     * Tests interactions between an mDoc application and a reader during a session using ISO vectors.
     *
     * Validates the creation of ephemeral device and reader keys, session establishment, and transcript decoding.
     * Ensures correct functioning of session encryption and decryption for device requests, device responses, and session termination messages.
     *
     * This test includes:
     * - Setting up the mDoc and reader session encryption.
     * - Encrypting a device request from the reader and verifying the session establishment.
     * - Decrypting session establishment and verifying the encrypted data.
     * - Encrypting and decrypting a device response.
     * - Terminating a session and verifying termination messages from both mDoc and reader.
     */
    @Test
    fun testMdocAppAndReaderSessionInteractionsWithIsoVectors() = runTest {
        val ephemeralDeviceKey = CoseKeyCbor(
            generateKid = false,
            kty = KeyType.EC.cose.toCbor(),
            crv = Curve.P_256.cose.toCbor(),
            d = TestVectors.ISO_18013_5_ANNEX_D_EPHEMERAL_DEVICE_KEY_D.decodeFromHex().toCborByteString(),
            x = TestVectors.ISO_18013_5_ANNEX_D_EPHEMERAL_DEVICE_KEY_X.decodeFromHex().toCborByteString(),
            y = TestVectors.ISO_18013_5_ANNEX_D_EPHEMERAL_DEVICE_KEY_Y.decodeFromHex().toCborByteString()
        )
        assertNotNull(ephemeralDeviceKey)

        val ephemeralReaderKey = CoseKeyCbor(
            generateKid = false,
            kty = KeyType.EC.cose.toCbor(),
            crv = Curve.P_256.cose.toCbor(),
            d = TestVectors.ISO_18013_5_ANNEX_D_EPHEMERAL_READER_KEY_D.decodeFromHex().toCborByteString(),
            x = TestVectors.ISO_18013_5_ANNEX_D_EPHEMERAL_READER_KEY_X.decodeFromHex().toCborByteString(),
            y = TestVectors.ISO_18013_5_ANNEX_D_EPHEMERAL_READER_KEY_Y.decodeFromHex().toCborByteString()
        )
        assertNotNull(ephemeralReaderKey)

        val encodedSessionTranscriptBytes =
            TestVectors.ISO_18013_5_ANNEX_D_SESSION_TRANSCRIPT_BYTES.decodeFromHex()
        val sessionTranscript = SessionTranscriptCbor.Static.cborDecode(encodedSessionTranscriptBytes)
        assertNotNull(sessionTranscript)
        val sessionEstablishmentBytes = TestVectors.ISO_18013_5_ANNEX_D_SESSION_ESTABLISHMENT.decodeFromHex()
        val sessionEstablishment = SessionEstablishmentCbor.Static.cborDecode(sessionEstablishmentBytes)
        assertNotNull(sessionEstablishment)
        assertContentEquals(sessionEstablishmentBytes, sessionEstablishment.cborEncode())
        assertContentEquals(encodedSessionTranscriptBytes, sessionTranscript.toCborEncodedItem().cborEncode())

        // Setup the mdoc session encryption first
        val mdocDeviceSessionEncryption = SessionEncryption.Builder()
            .withSessionTranscriptBytes(encodedSessionTranscriptBytes)
            .withSelfRole(MdocRole.MDOC)
            .withSelfPrivateEphemeralKey(ResolvedKeyInfo.Static.fromKey(ephemeralDeviceKey))
            .withOtherPublicEphemeralKey(ResolvedKeyInfo.Static.fromKey(ephemeralReaderKey.toPublicKey())).build()
        assertNotNull(mdocDeviceSessionEncryption)

        // Setup the mdoc reader session encryption next
        val mdocReaderSessionEncryption = SessionEncryption.Builder()
            .withSessionTranscriptBytes(encodedSessionTranscriptBytes)
            .withSelfRole(MdocRole.MDOC_READER)
            .withSelfPrivateEphemeralKey(ResolvedKeyInfo.Static.fromKey(ephemeralReaderKey))
            .withOtherPublicEphemeralKey(ResolvedKeyInfo.Static.fromKey(ephemeralDeviceKey.toPublicKey())).build()
        assertNotNull(mdocReaderSessionEncryption)

        // Let's encrypt a device request as session establishment from the mdoc reader first
        val sessionEstablishmentEncryptResult =
            mdocReaderSessionEncryption.encryptAsSessionEstablishment(TestVectors.ISO_18013_5_ANNEX_D_DEVICE_REQUEST.decodeFromHex(), status = null)
        assertNotNull(sessionEstablishmentEncryptResult)
        assertContentEquals(TestVectors.ISO_18013_5_ANNEX_D_SESSION_ESTABLISHMENT.decodeFromHex(), sessionEstablishmentEncryptResult.cborEncode())

        // Check that decryption works for the session establishment received from the mdoc reader
        val sessionEstablishmentDecryptResult = mdocDeviceSessionEncryption.decrypt(sessionEstablishmentBytes)
        assertNotNull(sessionEstablishmentDecryptResult.encryptedSessionEstablishment)
        assertNotNull(sessionEstablishmentDecryptResult.sessionData)
        // Since the session establishment is present and contain the encrypted data, it can never be the same as the session data which is unencrypted
        assertNotEquals(sessionEstablishmentDecryptResult.encryptedSessionEstablishment!!.data, sessionEstablishmentDecryptResult.sessionData.data)
        assertContentEquals(
            TestVectors.ISO_18013_5_ANNEX_D_SESSION_ESTABLISHMENT.decodeFromHex(),
            sessionEstablishmentDecryptResult.encryptedSessionEstablishment!!.cborEncode()
        )

        // Let's return an encrypted device response as session data from the mdoc device
        val mdocEncryptDeviceResponse =
            mdocDeviceSessionEncryption.encryptAsSessionData(TestVectors.ISO_18013_5_ANNEX_D_DEVICE_RESPONSE.decodeFromHex(), status = null)
        assertContentEquals(TestVectors.ISO_18013_5_ANNEX_D_SESSION_DATA.decodeFromHex(), mdocEncryptDeviceResponse.cborEncode())


        // Let's decrypt the Device response from the mdoc reader
        val mdocReaderDecryptDeviceResponse = mdocReaderSessionEncryption.decrypt(mdocEncryptDeviceResponse.cborEncode())
        assertContentEquals(TestVectors.ISO_18013_5_ANNEX_D_DEVICE_RESPONSE.decodeFromHex(), mdocReaderDecryptDeviceResponse.sessionData.data?.value)


        // Let's terminate a session from the mdoc device using a status
        val mdocEncryptTermination = mdocDeviceSessionEncryption.encrypt(status = SessionDataStatus.SESSION_TERMINATION.getCode())
        assertContentEquals(
            TestVectors.ISO_18013_5_ANNEX_D_SESSION_TERMINATION.decodeFromHex(),
            mdocEncryptTermination
        )

        // Let's inspect a termination from the mdoc reader
        val mdocReaderDecryptTermination = mdocReaderSessionEncryption.decryptSessionData(mdocEncryptTermination)
        assertEquals(SessionDataStatus.SESSION_TERMINATION.status, mdocReaderDecryptTermination.status)
        assertNull(mdocReaderDecryptTermination.data)


        // Let's terminate a session from the mdoc reader using a status
        val mdocReaderEncryptTermination = mdocReaderSessionEncryption.encrypt(status = SessionDataStatus.SESSION_TERMINATION.getCode())
        assertContentEquals(
            TestVectors.ISO_18013_5_ANNEX_D_SESSION_TERMINATION.decodeFromHex(),
            mdocReaderEncryptTermination
        )

        // Let's inspect a termination from the mdoc device
        val mdocDecryptTermination = mdocDeviceSessionEncryption.decryptSessionData(mdocReaderEncryptTermination)
        assertNotNull(mdocDecryptTermination.status)
        assertEquals(SessionDataStatus.SESSION_TERMINATION.status, mdocDecryptTermination.status)
        assertNull(mdocDecryptTermination.data)

    }
}
