

package com.sphereon.ble.client

import com.benasher44.uuid.Uuid

expect class ClientService {

    val uuid: Uuid

    fun findCharacteristic(uuid: Uuid): ClientCharacteristic?
}
