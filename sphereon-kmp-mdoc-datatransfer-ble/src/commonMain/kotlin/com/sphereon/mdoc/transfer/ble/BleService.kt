package com.sphereon.mdoc.transfer.ble

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Identifier
import com.juul.kable.PlatformAdvertisement
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.sphereon.mdoc.transfer.ble.BleScanState.Canceled
import com.sphereon.mdoc.transfer.ble.BleScanState.Error
import com.sphereon.mdoc.transfer.ble.BleScanState.Finished
import com.sphereon.mdoc.transfer.ble.BleScanState.Found
import com.sphereon.mdoc.transfer.ble.BleScanState.Initial
import com.sphereon.mdoc.transfer.ble.BleScanState.Scanning
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


@JsExport
@OptIn(ExperimentalUuidApi::class)
class BleService(
    private val scope: CoroutineScope,
    private val onStatus: ((state: BleScanState, message: String?) -> Unit)? = null,
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
    override val state: StateFlow<BleScanState> = _isScanning.asStateFlow()

    private val _advertisements = MutableStateFlow<List<PlatformAdvertisement>>(emptyList())
    override val advertisements = _advertisements.asStateFlow()

    public var scanJob: Job? = null

    override fun initiateScan(services: Array<Uuid>, filter: ((PlatformAdvertisement) -> Boolean)) {
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
                            onStatus?.invoke(Found, advertisement.identifier.toString())
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


    @JsExport.Ignore
    override suspend fun cancelAndJoin() {
        scanJob?.cancelAndJoin()
    }

    @JsExport.Ignore
    override suspend fun clear() {
        cancelAndJoin()
        found.clear()
        _isScanning.value = Initial
    }
}

enum class BleScanState {
    Initial,
    Scanning,
    Found,
    Error,
    Canceled,
    Finished,
}

@JsExport
interface IBleService {

    /** On Javascript, value is always [BleScanState.Initial]. */
    val state: StateFlow<BleScanState>

    /** Value is always an empty [List] on JavaScript. */
    val advertisements: StateFlow<List<PlatformAdvertisement>>

    @OptIn(ExperimentalUuidApi::class)
    fun initiateScan(services: Array<Uuid>, filter: (PlatformAdvertisement) -> Boolean = { advertisement -> true })

    /** No-op on Javascript. */
    @JsExport.Ignore
    suspend fun cancelAndJoin()

    /** No-op on Javascript. */
    @JsExport.Ignore
    suspend fun clear()
}
