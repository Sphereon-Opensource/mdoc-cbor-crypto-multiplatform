package com.sphereon.mdoc.transfer

import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.kms.EcDSACryptoProvider
import com.sphereon.crypto.kms.KeyManagerService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ManualEngagementTest {

    @Test
    fun testManualEngagement() = runTest() {
        val kms = EcDSACryptoProvider()
        val keyManager = KeyManagerService<IX509ServiceMarkerType>(keyManagementSystems = arrayOf(kms))
        val transferService = TransferService(keyManager = keyManager, retrievalMethods = arrayOf(BleRetrievalMethod(centralClientMode = true)))
        val qr = transferService.startQrEngagement()

        println("QR:\r\n$qr")
    }
}
