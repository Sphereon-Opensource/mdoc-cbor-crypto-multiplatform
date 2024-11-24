

package com.sphereon.ble.scanner

import kotlinx.coroutines.flow.Flow

expect class Scanner {

    fun scan(): Flow<List<IoTDevice>>
}
