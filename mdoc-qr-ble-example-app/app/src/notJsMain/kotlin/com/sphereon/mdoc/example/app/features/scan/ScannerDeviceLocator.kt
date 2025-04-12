package com.sphereon.mdoc.example.app.features.scan

import android.util.Log
import com.juul.kable.Identifier
import com.juul.kable.PlatformAdvertisement
import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.kms.EcDSACryptoProvider
import com.sphereon.crypto.kms.KeyManagerService
import com.sphereon.mdoc.example.app.SensorTag
import com.sphereon.mdoc.example.app.features.scan.DeviceLocator.State.NotYetScanned
import com.sphereon.mdoc.example.app.features.scan.DeviceLocator.State.Scanned
import com.sphereon.mdoc.example.app.features.scan.DeviceLocator.State.Scanning
import com.sphereon.mdoc.transfer.BleRetrievalMethod
import com.sphereon.mdoc.transfer.TransferService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

class ScannerDeviceLocator(
    private val scope: CoroutineScope,
    private val onStatus: suspend (String?) -> Unit,
) : DeviceLocator {

    private val found = mutableMapOf<Identifier, PlatformAdvertisement>()

    private val _isScanning = MutableStateFlow(NotYetScanned)
    override val state: StateFlow<DeviceLocator.State> = _isScanning.asStateFlow()

    private val _advertisements = MutableStateFlow<List<PlatformAdvertisement>>(emptyList())
    override val advertisements = _advertisements.asStateFlow()

    private var scanJob: Job? = null

    override fun run() {
        if (_isScanning.value == Scanning) return

        scanJob = scope.launch(CoroutineName("Scanner")) {
            _isScanning.value = Scanning
            try {
                Log.i("MDOC","QR ANDROID:\r\nStarting scan")
                val kms = EcDSACryptoProvider()
                val keyManager = KeyManagerService<IX509ServiceMarkerType>(keyManagementSystems = arrayOf(kms))
                Log.i("MDOC","QR ANDROID:\r\nKeyManagerService created")
                val transferService = TransferService(keyManager = keyManager, retrievalMethods = arrayOf(BleRetrievalMethod(centralClientMode = true)))
                Log.i("MDOC","QR ANDROID:\r\nTransferService created")
                val qr = transferService.startQrEngagement()
                Log.i("MDOC","QR ANDROID:\r\nQR code retrieved")

                Log.i("MDOC","QR ANDROID:\r\n$qr")
            }catch (e: Exception) {

                Log.i("MDOC", "WHOOPSIE")
                Log.i("MDOC", e.message ?: "Unknown error")
                e.printStackTrace()
                onStatus(e.message ?: "Unknown error")
                return@launch

            }
           /* try {
                withTimeout(10.seconds) {
                    SensorTag.scanner
                        .advertisements
                        .onStart { onStatus("Scanning") }
                        .collect { advertisement ->
                            found[advertisement.identifier] = advertisement
                            _advertisements.value = found.values.toList()
                        }
                }
            } catch (e: Exception) {
                onStatus(
                    when (e) {
                        is CancellationException -> null
                        else -> e.message ?: "Unknown error"
                    }
                )
            } finally {
                _isScanning.value = Scanned
            }*/
        }
    }

    override suspend fun cancelAndJoin() {
        scanJob?.cancelAndJoin()
    }

    override suspend fun clear() {
        cancelAndJoin()
        found.clear()
        _isScanning.value = NotYetScanned
    }
}
