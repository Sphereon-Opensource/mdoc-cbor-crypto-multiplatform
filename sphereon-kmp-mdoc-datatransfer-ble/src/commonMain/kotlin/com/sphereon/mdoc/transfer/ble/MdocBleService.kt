@file:OptIn(ExperimentalUuidApi::class)

package com.sphereon.mdoc.transfer.ble

import com.juul.kable.Peripheral
import com.juul.kable.toIdentifier
import com.sphereon.mdoc.transfer.Connecting
import com.sphereon.mdoc.transfer.TransferEvent
import com.sphereon.mdoc.transfer.TransferEventListener
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.js.JsExport
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Since the ble class is generic in nature, not taking into account Mdoc specifics, we handle anything 18013-5 specific in here. Including restricting the scan to the central client or peripheralServer
 */
class MdocBleService(
    centralClientModeUUID: Uuid? = null,
    peripheralServerModeUUID: Uuid? = null,
    context: Any? = null,
    private val scope: CoroutineScope = CoroutineScope(CoroutineName("mdoc-ble-service")),
    private val onStatus: Array<OnBleStatusCallback> = emptyArray()
): TransferEventListener {
    val ble: BleService
    val services: Set<Uuid>
    lateinit var peripheral: Peripheral

    init {
        val services = mutableSetOf<Uuid>()
        centralClientModeUUID?.let { services.add(it) }
        peripheralServerModeUUID?.let { services.add(it) }
        this.services = services
        this.ble = BleService(scope, onStatus = onStatus)
    }

    fun initiateScan() = apply {
        ble.initiateScan(services.toTypedArray())
    }

    fun onConnect(connecting: Connecting) {
        println("Connecting to ${connecting.identifier}")
        val identifier = connecting.identifier.toIdentifier()
        val advertisement = ble.advertisements.value.firstOrNull { it.identifier == identifier }
            ?: throw IllegalStateException("Ble advertisement for $identifier not found")
        this.peripheral = Peripheral(advertisement)
        scope.async { peripheral.connect() }


       /* ble.scanJob?.cancel()
        ble.scanJob = null*/
    }

    /** No-op on Javascript. */
    @JsExport.Ignore
    suspend fun cancelAndJoin() = ble.cancelAndJoin()

    /** No-op on Javascript. */
    @JsExport.Ignore
    suspend fun clear() = ble.clear()

    override fun onTransferEvent(event: TransferEvent) {
        when (event) {
            is Connecting -> onConnect(event)
            else -> println("Not handling event: $event")
        }
    }


}
