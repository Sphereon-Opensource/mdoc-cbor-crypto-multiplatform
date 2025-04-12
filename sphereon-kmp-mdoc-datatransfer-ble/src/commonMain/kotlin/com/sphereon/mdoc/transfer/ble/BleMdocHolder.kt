package com.sphereon.mdoc.transfer.ble

import com.benasher44.uuid.uuidFrom
import com.sphereon.mdoc.transfer.BleRetrievalMethod
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class BleMdocHolder {
    private val peripheralUuid = kotlin.uuid.Uuid.random()
    private val ble: BleService
    private val scope: CoroutineScope

    private constructor(
        scope: CoroutineScope? = null,
        onStatus: Array<OnBleStatusCallback> = emptyArray()
    ) {
        this.scope = scope ?: CoroutineScope(CoroutineName("BleHolder"))
        this.ble = BleService(scope = this.scope, onStatus = onStatus)
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun start(retrievalMethod: BleRetrievalMethod) {
        check(retrievalMethod.centralClientMode) { "Only central client mode is supported" }
        check(!retrievalMethod.peripheralServerMode) { "Peripheral service mode is not supported" }

        ble.initiateScan(arrayOf(peripheralUuid), filter = { advertisement ->
            advertisement.uuids.contains(uuidFrom(peripheralUuid.toString()))
        })

    }

    fun verifyIdent() {

    }


    object Static {
        fun init(onStatus: Array<OnBleStatusCallback> = emptyArray()): BleMdocHolder {
            return BleMdocHolder(onStatus = onStatus)
        }
    }
}

enum class BleHolderState {
    INIT,
    SCANNING,
    CONNECTING,

    CONNECTED,
    DISCONNECTED,
}
