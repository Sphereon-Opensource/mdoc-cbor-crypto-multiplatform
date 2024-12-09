

package com.sphereon.ble.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import com.sphereon.ble.scanner.IoTDevice

expect class Server {

    val connections: Flow<Map<IoTDevice, ServerProfile>>

    suspend fun startServer(services: List<BleServerServiceConfig>, scope: CoroutineScope)

    suspend fun stopServer()
}
