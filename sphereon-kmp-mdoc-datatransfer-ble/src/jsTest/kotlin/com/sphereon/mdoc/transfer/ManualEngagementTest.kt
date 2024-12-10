package com.sphereon.mdoc.transfer

import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.kms.EcDSACryptoProvider
import com.sphereon.crypto.kms.KeyManagerService
import io.kotest.common.runBlocking
import js.temporal.TimeUnit
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ManualEngagementTest {

    @Test
    fun testManualEngagementJS() = runTest {
        val kms = EcDSACryptoProvider()
        val keyManager = KeyManagerService<IX509ServiceMarkerType>(keyManagementSystems = arrayOf(kms))
        val transferService = TransferServiceJS(keyManager = keyManager, retrievalMethods = arrayOf(BleRetrievalMethod(centralClientMode = true)))
        val qr = transferService.startQrEngagement().await()

        println("QR:\r\n$qr")

        println(transferService.bleService?.advertisements?.value)

        println("join1")
        transferService.bleService?.scanJob?.join()
        println("join2")
    }
}
