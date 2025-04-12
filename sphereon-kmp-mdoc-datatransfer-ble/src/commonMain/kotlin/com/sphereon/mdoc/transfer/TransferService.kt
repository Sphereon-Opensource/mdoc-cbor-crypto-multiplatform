@file:OptIn(ExperimentalUuidApi::class)

package com.sphereon.mdoc.transfer

import com.sphereon.crypto.KeyEncoding
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.kms.IKeyManagerService
import com.sphereon.mdoc.transfer.ble.BleScanState
import com.sphereon.mdoc.transfer.ble.MdocBleService
import com.sphereon.mdoc.transfer.ble.OnBleStatusCallback
import com.sphereon.mdoc.transfer.device.DeviceEngagementCbor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.js.JsExport
import kotlin.uuid.ExperimentalUuidApi


/**
 * Marker interface for Retrieval Methods
 */
@JsExport
interface DeviceRetrievalMethod


@JsExport
data class BleRetrievalMethod(val peripheralServerMode: Boolean = false, val centralClientMode: Boolean = true) : DeviceRetrievalMethod

interface ITransferService : TransferEventHandler {
    suspend fun startQrEngagement(): String
    fun isStarted(): Boolean
    fun getEngagementRole(): EngagementRole
}


class TransferService(
    keyManager: IKeyManagerService,
    retrievalMethods: Array<DeviceRetrievalMethod>,
    listeners: MutableSet<TransferEventListener> = mutableSetOf<TransferEventListener>(),
    context: Any? = null
) : AbstractTransferService(keyManager = keyManager, retrievalMethods = retrievalMethods, listeners = listeners, context = context),
    ITransferService {

    private constructor(
        keyManager: IKeyManagerService,
        retrievalMethods: Array<DeviceRetrievalMethod>,
        context: Any? = null,
        listeners: MutableSet<TransferEventListener> = mutableSetOf<TransferEventListener>(),
        engagementService: EngagementService,
        bleService: MdocBleService
    ) : this(keyManager, retrievalMethods, listeners, context)


    @OptIn(ExperimentalUuidApi::class)
    override suspend fun startQrEngagement() = startQrEngagementImpl()

    object Static {
        fun fromQrEngagement(
            qrCodeData: String,
            context: Any? = null,
            bleService: MdocBleService,
            retrievalMethods: Array<DeviceRetrievalMethod> = arrayOf(),
            keyManager: IKeyManagerService,
            vararg listeners: TransferEventListener
        ): TransferService {
            val deviceEngagement = DeviceEngagementCbor.Static.fromQRData(qrCodeData)
            val engagementService = EngagementService.Static.verifierFromDeviceEngagement(deviceEngagement)
            val ble = engagementService.isBleSupported()
            check(ble) { "BLE is not enabled for the engagement" }

            val transferService =
                TransferService(
                    retrievalMethods = retrievalMethods,
                    keyManager = keyManager,
                    context = context,
                    listeners = mutableSetOf<TransferEventListener>(*listeners),
                    engagementService = engagementService,
                    bleService = bleService
                )


            transferService.sendEvent(QrEngagement(EngagementRole.VERIFIER, qrCodeData, deviceEngagement, ble))
            if (ble) {
                checkNotNull(engagementService.getBleCentralClientModeUuid()) { "BLE central client mode UUID is missing from engagement" }
                // TODO: Enable GATT Server
//                bleService.

            }
            // TODO check
            transferService.sendEvent(
                Connecting(
                    EngagementRole.VERIFIER,
                    engagementService.getBleCentralClientModeUuid()?.let { it.toString() }
                        ?: throw IllegalStateException("Mdoc uuid expected in message: $qrCodeData")))
            return transferService
        }

    }

}


abstract class AbstractTransferService(
    val keyManager: IKeyManagerService,
    val retrievalMethods: Array<DeviceRetrievalMethod>,
    val listeners: MutableSet<TransferEventListener> = mutableSetOf<TransferEventListener>(),
    val context: Any? = null
) : TransferEventHandler {

    private constructor(
        keyManager: IKeyManagerService,
        retrievalMethods: Array<DeviceRetrievalMethod>,
        context: Any? = null,
        listeners: MutableSet<TransferEventListener> = mutableSetOf<TransferEventListener>(),
        engagementService: EngagementService,
        bleService: MdocBleService
    ) : this(keyManager, retrievalMethods, listeners, context) {
        this.bleService = bleService
        this.engagementService = engagementService
        this.started = true
    }

    var bleService: MdocBleService? = null
    private var engagementService: EngagementService? = null

    private var started = false


    @OptIn(ExperimentalUuidApi::class)
    suspend fun startQrEngagementImpl(): String {
        check(!started) { "Transfer has already been started" }
        val ephemeralDeviceKey =
            keyManager.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256).toManagedKeyInfo<CoseKeyCbor>(keyEncoding = KeyEncoding.COSE)
        val engagementService =
            EngagementService.HolderBuilder().withRetrievalMethods(retrievalMethods).withEphemeralDeviceKey(ephemeralDeviceKey).build()
        this.engagementService = engagementService
        check(engagementService.getRole() == EngagementRole.HOLDER) { "Only the holder can start a QR engagement" }
        val ble = engagementService.isBleSupported()
        check(ble) { "BLE is not enabled for the engagement" }

        val qrEngagement = engagementService.generateQrEngagementData().also { this.started = true }
        sendEvent(QrEngagement(EngagementRole.HOLDER, qrEngagement, engagementService.getDeviceEngagement(), ble))
        if (ble) {

            this.bleService = MdocBleService(
                centralClientModeUUID = engagementService.getBleCentralClientModeUuid(),
                peripheralServerModeUUID = engagementService.getBlePeripheralServerModeUuid(),
                context,
                onStatus = arrayOf(onBleStatusCallback())
            )
                .also {
                    addTransferEventListener(it);
                    it.initiateScan()
                }
        }

        return qrEngagement
    }

    private fun onBleStatusCallback(): OnBleStatusCallback {
        return object : OnBleStatusCallback {
            override fun invoke(state: BleScanState, message: String?) {
                when (state) {
                    BleScanState.Initial -> sendEvent(Initializing(EngagementRole.HOLDER))
                    BleScanState.Scanning -> sendEvent(DebugEvent(EngagementRole.HOLDER, message ?: "Scanning"))
                    BleScanState.Found -> sendEvent(
                        Connecting(
                            EngagementRole.HOLDER,
                            identifier = message /**/ ?: throw IllegalStateException("Mdoc ble identifier expected in message: $message")
                        )
                    )

                    BleScanState.Canceled -> sendEvent(Canceled(EngagementRole.HOLDER, "Scan canceled"))
                    BleScanState.Finished -> sendEvent(DebugEvent(EngagementRole.HOLDER, message ?: "Scan finished"))
                    BleScanState.Error -> sendEvent(Error(EngagementRole.HOLDER, message ?: "Scan error"))
                }
            }
        }
    }


    fun isStarted() = started

    fun getEngagementRole() = engagementService?.getRole() ?: EngagementRole.HOLDER

    protected fun sendEvent(event: TransferEvent) {
        println("Sending event1: $event")
        CoroutineScope(CoroutineName("transfer-service")).launch {
            listeners.forEach {
                println("Sending event2: $event")
                it.onTransferEvent(event)
            }
        }
    }


    override fun addTransferEventListener(vararg listener: TransferEventListener) = apply {
        listeners.addAll(listener)
    }

    override fun removeTransferEventListener(listener: TransferEventListener) = apply {
        listeners.remove(listener)
    }

    override fun clearTransferEventListeners() = apply {
        listeners.clear()
    }
}
