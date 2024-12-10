@file:OptIn(ExperimentalUuidApi::class)

package com.sphereon.mdoc.transfer

import com.sphereon.crypto.kms.IKeyManagerService
import com.sphereon.mdoc.transfer.ble.BleService
import com.sphereon.mdoc.transfer.device.DeviceEngagementCbor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.uuid.ExperimentalUuidApi

@JsExport
interface ITransferServiceJS : TransferEventHandler {
    fun startQrEngagement(): Promise<String>
    fun isStarted(): Boolean
    fun getEngagementRole(): EngagementRole
}


@JsExport
class TransferServiceJS(
    keyManager: IKeyManagerService,
    retrievalMethods: Array<DeviceRetrievalMethod>,
    listeners: MutableSet<TransferEventListener> = mutableSetOf<TransferEventListener>(),
    context: Any? = null
) : AbstractTransferService(keyManager = keyManager, retrievalMethods = retrievalMethods, listeners = listeners, context = context),
    ITransferServiceJS {

    private constructor(
        keyManager: IKeyManagerService,
        retrievalMethods: Array<DeviceRetrievalMethod>,
        context: Any? = null,
        listeners: MutableSet<TransferEventListener> = mutableSetOf<TransferEventListener>(),
        engagementService: EngagementService,
        bleService: BleService
    ) : this(keyManager, retrievalMethods, listeners, context)


    override fun startQrEngagement() = CoroutineScope(CoroutineName("transfer-service")).promise {
        return@promise Promise.resolve(startQrEngagementImpl()).await()
    }


    object Static {
        fun fromQrEngagement(
            qrCodeData: String,
            context: Any? = null,
            bleService: BleService,
            retrievalMethods: Array<DeviceRetrievalMethod> = arrayOf(),
            keyManager: IKeyManagerService,
            vararg listeners: TransferEventListener
        ): TransferServiceJS {
            val deviceEngagement = DeviceEngagementCbor.Static.fromQRData(qrCodeData)
            val engagementService = EngagementService.Static.verifierFromDeviceEngagement(deviceEngagement)
            val ble = engagementService.isBleSupported()
            check(ble) { "BLE is not enabled for the engagement" }

            val transferService =
                TransferServiceJS(
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
            transferService.sendEvent(Connecting(EngagementRole.VERIFIER))
            return transferService
        }

    }

}
