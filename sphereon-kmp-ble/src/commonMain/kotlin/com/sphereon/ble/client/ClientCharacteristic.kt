

package com.sphereon.ble.client

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow

expect class ClientCharacteristic {

    fun findDescriptor(uuid: Uuid): ClientDescriptor?

    suspend fun getNotifications(): Flow<ByteArray>

    suspend fun write(value: ByteArray, writeType: WriteType = WriteType.DEFAULT)

    suspend fun read(): ByteArray
}
