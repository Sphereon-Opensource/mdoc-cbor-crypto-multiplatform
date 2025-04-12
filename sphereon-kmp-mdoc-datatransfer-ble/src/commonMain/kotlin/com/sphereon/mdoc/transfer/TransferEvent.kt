@file:OptIn(ExperimentalUuidApi::class)

package com.sphereon.mdoc.transfer

import com.juul.kable.Identifier
import com.sphereon.mdoc.transfer.device.DeviceEngagementCbor
import kotlin.js.JsExport
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JsExport
sealed interface TransferEvent {
    val role: EngagementRole
}

@JsExport
data class QrEngagement(override val role: EngagementRole, val qrCodeData: String, val engagement: DeviceEngagementCbor, val ble: Boolean) :
    TransferEvent

@JsExport
data class Initializing(override val role: EngagementRole) : TransferEvent

@JsExport
data class DebugEvent(override val role: EngagementRole, val message: String) : TransferEvent

@JsExport
data class Connecting(override val role: EngagementRole, val identifier: String) : TransferEvent


@JsExport
data class Connected(override val role: EngagementRole) : TransferEvent

@JsExport
data class Canceled(override val role: EngagementRole, val reason: String? = null) : TransferEvent

@JsExport
data class Disconnected(override val role: EngagementRole, val reason: String) : TransferEvent

@JsExport
data class DeviceRequest(override val role: EngagementRole, val deviceRequest: UIntArray) : TransferEvent

@JsExport
data class DeviceResponse(override val role: EngagementRole, val deviceResponse: UIntArray) : TransferEvent

@JsExport
data class Error(override val role: EngagementRole, val reason: String, val error: Throwable? = null) : TransferEvent

@JsExport
data class Response(override val role: EngagementRole, val response: ByteArray) : TransferEvent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Response) return false

        if (role != other.role) return false
        if (!response.contentEquals(other.response)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = role.hashCode()
        result = 31 * result + response.contentHashCode()
        return result
    }
}

@JsExport
fun interface TransferEventListener {
    fun onTransferEvent(event: TransferEvent)
}

@JsExport
interface TransferEventHandler {

    fun addTransferEventListener(vararg listener: TransferEventListener): TransferEventHandler

    fun removeTransferEventListener(listener: TransferEventListener): TransferEventHandler

    fun clearTransferEventListeners(): TransferEventHandler
}
