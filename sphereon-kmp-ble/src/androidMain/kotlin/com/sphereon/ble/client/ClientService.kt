

package com.sphereon.ble.client

import com.benasher44.uuid.Uuid

actual class ClientService(private val service: ClientBleGattService) {

    actual val uuid: Uuid = service.uuid

    actual fun findCharacteristic(uuid: Uuid): ClientCharacteristic? {
        return service.findCharacteristic(uuid)?.let { ClientCharacteristic(it) }
    }
}
