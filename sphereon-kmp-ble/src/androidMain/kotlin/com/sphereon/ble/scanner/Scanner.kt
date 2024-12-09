

package com.sphereon.ble.scanner

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

actual class Scanner(private val context: Context) {

    @SuppressLint("MissingPermission")
    actual fun scan(): Flow<List<IoTDevice>> {
        val scanner = BleScanner(context)

        val result = mutableListOf<IoTDevice>()

        return scanner.scan().map { IoTDevice(it.device) }
            .onEach { result += it }
            .map { result }
            .map { it.distinctBy { it.address } }
    }
}
