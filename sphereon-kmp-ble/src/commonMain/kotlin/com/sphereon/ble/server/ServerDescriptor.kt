

package com.sphereon.ble.server

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow

expect class ServerDescriptor {
    val uuid: Uuid
    val permissions: List<GattPermission>
    val value: Flow<ByteArray>
    suspend fun setValue(value: ByteArray)
}
