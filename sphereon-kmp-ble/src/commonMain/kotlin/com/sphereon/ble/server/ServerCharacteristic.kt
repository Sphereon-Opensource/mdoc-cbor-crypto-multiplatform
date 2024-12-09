

package com.sphereon.ble.server

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow

expect class ServerCharacteristic {
    val uuid: Uuid
    val properties: List<GattProperty>
    val permissions: List<GattPermission>
    val descriptors: List<ServerDescriptor>
    val value: Flow<ByteArray>
    suspend fun setValue(value: ByteArray)
}
