

package com.sphereon.ble.client

import com.benasher44.uuid.Uuid

expect class ClientServices {

    fun findService(uuid: Uuid): ClientService?
}
