

package com.sphereon.ble.server

import com.benasher44.uuid.Uuid

expect class ServerProfile {
    val services: List<ServerService>
    fun findService(uuid: Uuid): ServerService?
    fun copyWithNewService(service: ServerService): ServerProfile
}
