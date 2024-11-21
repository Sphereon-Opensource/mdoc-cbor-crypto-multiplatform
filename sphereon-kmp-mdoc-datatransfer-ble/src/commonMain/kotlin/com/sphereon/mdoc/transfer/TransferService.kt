package com.sphereon.mdoc.transfer

import kotlin.js.JsExport


@JsExport
interface ITransferService : TransferEventHandler {
    fun startQrEngagement()
}


@JsExport
class TransferService(val context: Any? = null) : ITransferService {

    private val listeners = mutableSetOf<TransferEventListener>()

    override fun startQrEngagement() {
        TODO("Not yet implemented")
    }

    override fun addTransferEventListener(listener: TransferEventListener) = apply {
        listeners.add(listener)
    }

    override fun removeTransferEventListener(listener: TransferEventListener) = apply {
        listeners.remove(listener)
    }

    override fun clearTransferEventListeners() = apply {
        listeners.clear()
    }

}
