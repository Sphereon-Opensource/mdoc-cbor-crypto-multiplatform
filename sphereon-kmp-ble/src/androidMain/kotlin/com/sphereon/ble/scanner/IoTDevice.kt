

package com.sphereon.ble.scanner


actual class IoTDevice(internal val device: BleDevice) {

    actual val name: String
        get() = device.name ?: ""

    actual val address: String
        get() = device.address

}
