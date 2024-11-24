

package com.sphereon.ble.client

expect class ClientDescriptor {

    suspend fun write(value: ByteArray)

    suspend fun read(): ByteArray
}
