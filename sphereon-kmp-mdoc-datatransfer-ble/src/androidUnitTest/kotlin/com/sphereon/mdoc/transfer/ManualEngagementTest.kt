package com.sphereon.mdoc.transfer

import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.kms.EcDSACryptoProvider
import com.sphereon.crypto.kms.KeyManagerService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext

import kotlin.time.ExperimentalTime


class ManualEngagementTest {

    @Test
    @ExperimentalTime
    fun testManualEngagement() = runTest(timeout = 120.seconds) {
        val kms = EcDSACryptoProvider()
        val keyManager = KeyManagerService<IX509ServiceMarkerType>(keyManagementSystems = arrayOf(kms))
        val transferService = TransferService(keyManager = keyManager, retrievalMethods = arrayOf(BleRetrievalMethod(centralClientMode = true)))
        val qr = transferService.startQrEngagement()

        println("QR ANDROID:\r\n$qr")

        println(transferService.bleService?.advertisements?.value)

        println("join1")
        val result = withContext(Dispatchers.Default) {
            println("Wait for 120sec")
            delay(120.seconds)
            println("Done waiting for 20sec")
            transferService.bleService?.scanJob?.join()
        }

        println(result)
        println("join2")
    }
}
