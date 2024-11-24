package com.sphereon.mdoc.transfer.ble

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Identifier
import com.juul.kable.PlatformAdvertisement
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.sphereon.mdoc.transfer.Error
import com.sphereon.mdoc.transfer.ble.IBleService.ScanState.Canceled
import com.sphereon.mdoc.transfer.ble.IBleService.ScanState.Error
import com.sphereon.mdoc.transfer.ble.IBleService.ScanState.Finished
import com.sphereon.mdoc.transfer.ble.IBleService.ScanState.Initial
import com.sphereon.mdoc.transfer.ble.IBleService.ScanState.Scanning
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid




@OptIn(ExperimentalUuidApi::class)
class BleService(
    private val scope: CoroutineScope,
    private val onStatus: ((state: IBleService.ScanState, message: String?) -> Unit)? = null,
) : IBleService {

    private fun convertUuidToImpl(uuid: Uuid) = uuidFrom(uuid.toString())

    protected fun scanner(filterServices: Array<Uuid>) = Scanner {
        filters {
            match {
                services = filterServices.map { convertUuidToImpl(it) }.toList()
            }
        }
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Events
            format = Logging.Format.Multiline
        }
    }

    private val found = mutableMapOf<Identifier, PlatformAdvertisement>()

    private val _isScanning = MutableStateFlow(Initial)
    override val state: StateFlow<IBleService.ScanState> = _isScanning.asStateFlow()

    private val _advertisements = MutableStateFlow<List<PlatformAdvertisement>>(emptyList())
    override val advertisements = _advertisements.asStateFlow()

    private var scanJob: Job? = null

    override fun initiateScan(services: Array<Uuid>, filter: (suspend (PlatformAdvertisement) -> Boolean)) {
        onStatus?.invoke(Initial, null)
        if (_isScanning.value == Scanning) {
            return
        }

        scanJob = scope.launch(CoroutineName("Scanner")) {
            _isScanning.value = Scanning
            try {
                withTimeout(30.seconds) {
                    scanner(services)
                        .advertisements
                        .onStart { onStatus?.invoke(Scanning, null) }
                        .filter(filter)
                        .collect { advertisement ->
                            found[advertisement.identifier] = advertisement
                            _advertisements.value = found.values.toList()
                        }
                }
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> onStatus?.invoke(Canceled, null)
                    else -> onStatus?.invoke(Error, e.message ?: "Unknown error")
                }
            } finally {
                _isScanning.value = Finished
            }
        }
    }



    override suspend fun cancelAndJoin() {
        scanJob?.cancelAndJoin()
    }

    override suspend fun clear() {
        cancelAndJoin()
        found.clear()
        _isScanning.value = Initial
    }
}


interface IBleService {

    enum class ScanState {
        Initial,
        Scanning,
        Error,
        Canceled,
        Finished,
    }

    /** On Javascript, value is always [ScanState.Initial]. */
    val state: StateFlow<ScanState>

    /** Value is always an empty [List] on JavaScript. */
    val advertisements: StateFlow<List<PlatformAdvertisement>>

    @OptIn(ExperimentalUuidApi::class)
    fun initiateScan(services: Array<Uuid>, filter: suspend (PlatformAdvertisement) -> Boolean = { advertisement -> true })

    /** No-op on Javascript. */
    suspend fun cancelAndJoin()

    /** No-op on Javascript. */
    suspend fun clear()
}
