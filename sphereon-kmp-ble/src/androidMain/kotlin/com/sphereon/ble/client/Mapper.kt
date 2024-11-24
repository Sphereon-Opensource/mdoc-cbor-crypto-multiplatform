

package com.sphereon.ble.client

import com.sphereon.ble.client.main.service.ClientBleGattServices

internal fun ClientBleGattServices.toCrossplatform(): ClientServices {
    return ClientServices(this)
}
