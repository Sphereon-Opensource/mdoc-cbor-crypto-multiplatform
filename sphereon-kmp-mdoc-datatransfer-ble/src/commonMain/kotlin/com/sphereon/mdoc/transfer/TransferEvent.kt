package com.sphereon.mdoc.transfer

import kotlin.js.JsExport

@JsExport
sealed interface TransferEvent {
}

@JsExport
data class QrEngagement(val qrCode: String) : TransferEvent

@JsExport
data object Connecting : TransferEvent

@JsExport
data object Connected : TransferEvent

@JsExport
data class Disconnected(val reason: String) : TransferEvent

@JsExport
data class Error(val reason: String, val error: Throwable) : TransferEvent

@JsExport
data class Response(val response: ByteArray) : TransferEvent

@JsExport
fun interface TransferEventListener {
    fun onTransferEvent(event: TransferEvent)
}

@JsExport
interface TransferEventHandler {

    fun addTransferEventListener(listener: TransferEventListener): TransferEventHandler

    fun removeTransferEventListener(listener: TransferEventListener): TransferEventHandler

    fun clearTransferEventListeners(): TransferEventHandler
}
