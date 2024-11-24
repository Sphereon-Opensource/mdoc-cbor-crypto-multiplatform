

package com.sphereon.ble.client

import com.benasher44.uuid.Uuid
import com.sphereon.ble.client.main.service.ClientBleGattServices

actual class ClientServices(private val value: ClientBleGattServices) {

    actual fun findService(uuid: Uuid): ClientService? {
        return value.findService(uuid)?.let { ClientService(it) }
    }
}
