

package com.sphereon.ble.server

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual data class ServerDescriptor(
    val native: ServerBleGattDescriptor
) {
    actual val uuid: Uuid = native.uuid

    actual val permissions: List<GattPermission> = emptyList() //FIXME

    actual val value: Flow<ByteArray> = native.value.map { it.value }

    actual suspend fun setValue(value: ByteArray) {
        native.setValue(DataByteArray(value))
    }
}
