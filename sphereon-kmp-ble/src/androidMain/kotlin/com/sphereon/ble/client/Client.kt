

package com.sphereon.ble.client

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import com.sphereon.ble.scanner.IoTDevice

@SuppressLint("MissingPermission")
actual class Client(
    private val context: Context
) {

    private var client: ClientBleGatt? = null

    actual suspend fun connect(device: IoTDevice, scope: CoroutineScope) {
        client = ClientBleGatt.connect(context, device.device as ServerDevice, scope)
    }

    actual suspend fun disconnect() {
        client?.disconnect()
    }

    actual suspend fun discoverServices(): ClientServices {
        return client!!.discoverServices().toCrossplatform()
    }
}
