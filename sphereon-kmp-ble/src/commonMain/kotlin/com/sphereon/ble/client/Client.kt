

package com.sphereon.ble.client

import kotlinx.coroutines.CoroutineScope
import com.sphereon.ble.scanner.IoTDevice

expect class Client {

    suspend fun connect(device: IoTDevice, scope: CoroutineScope)

    suspend fun disconnect()

    suspend fun discoverServices(): ClientServices

}

sealed interface DeviceConnectionState

data object DeviceConnected : DeviceConnectionState

data object DeviceDisconnected: DeviceConnectionState


sealed interface OperationStatus

data object OperationSuccess : OperationStatus

data object OperationError : OperationStatus
