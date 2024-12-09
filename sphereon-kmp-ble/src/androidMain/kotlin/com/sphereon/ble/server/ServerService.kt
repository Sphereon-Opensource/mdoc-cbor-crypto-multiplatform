

package com.sphereon.ble.server

import com.benasher44.uuid.Uuid


actual data class ServerService(private val native: ServerBleGattService) {

    actual val uuid: Uuid = native.uuid

    actual val characteristics: List<ServerCharacteristic> = native.characteristics
        .map { ServerCharacteristic(it) }

    actual fun findCharacteristic(uuid: Uuid): ServerCharacteristic? {
        return characteristics.first { it.uuid == uuid }
    }
}
