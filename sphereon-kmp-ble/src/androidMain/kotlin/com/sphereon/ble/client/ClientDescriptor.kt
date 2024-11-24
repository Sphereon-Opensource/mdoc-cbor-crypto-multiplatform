

package com.sphereon.ble.client

import android.annotation.SuppressLint
import com.sphereon.android.common.core.DataByteArray
import com.sphereon.ble.client.main.service.ClientBleGattDescriptor

@SuppressLint("MissingPermission")
actual class ClientDescriptor(private val descriptor: ClientBleGattDescriptor) {

    actual suspend fun write(value: ByteArray) {
        descriptor.write(DataByteArray(value))
    }

    actual suspend fun read(): ByteArray {
        return descriptor.read().value
    }
}
