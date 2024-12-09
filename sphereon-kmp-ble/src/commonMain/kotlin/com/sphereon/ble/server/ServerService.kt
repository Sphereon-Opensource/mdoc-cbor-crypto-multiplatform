

package com.sphereon.ble.server

import com.benasher44.uuid.Uuid

expect class ServerService {
    val uuid: Uuid
    val characteristics: List<ServerCharacteristic>
    fun findCharacteristic(uuid: Uuid): ServerCharacteristic?
}
