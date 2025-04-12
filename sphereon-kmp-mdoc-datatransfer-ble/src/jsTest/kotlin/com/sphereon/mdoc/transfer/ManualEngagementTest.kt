package com.sphereon.mdoc.transfer

import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.kms.EcDSACryptoProvider
import com.sphereon.crypto.kms.KeyManagerService
import io.kotest.common.runBlocking
import js.temporal.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class ManualEngagementTest {

    @Test
    @ExperimentalTime
    fun testManualEngagementJS() = runTest(timeout = 30.seconds) {
        val kms = EcDSACryptoProvider()
        val keyManager = KeyManagerService<IX509ServiceMarkerType>(keyManagementSystems = arrayOf(kms))
        val transferService = TransferServiceJS(keyManager = keyManager, retrievalMethods = arrayOf(BleRetrievalMethod(centralClientMode = true)))
        val qr = transferService.startQrEngagement().await()

        println("QR:\r\n$qr")
        println(transferService.bleService?.advertisements?.value)

        println("join1")
        println("join1")
        val result = withContext(Dispatchers.Default) {
            println("Wait for 20sec")
            delay(20.seconds)
            println("Done waiting for 20sec")
//            transferService.bleService?.advertisements?.value
//            println(transferService.bleService?.advertisements?.value)
        }
        println(result)
        transferService.bleService?.scanJob?.join()
        println("join2")
    }
}
