

package com.sphereon.ble.server

import com.benasher44.uuid.Uuid

actual data class ServerProfile(private val native: ServerBleGattServices) {

    actual val services: List<ServerService> = native.services.map {
        ServerService(it)
    }

    actual fun findService(uuid: Uuid): ServerService? {
        return services.find { it.uuid == uuid }
    }

    actual fun copyWithNewService(service: ServerService): ServerProfile {
        return copy()
    }
}
