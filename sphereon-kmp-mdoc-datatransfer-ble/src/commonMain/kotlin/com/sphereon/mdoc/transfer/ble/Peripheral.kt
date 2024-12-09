package com.sphereon.mdoc.transfer.ble

import com.juul.kable.Peripheral
import kotlinx.coroutines.cancel
import kotlin.concurrent.Volatile

@Volatile
var peripheral: Peripheral? = null
    set(newPeripheral) {
        val current = field
        if (current !== newPeripheral) current?.cancel()
        field = newPeripheral
    }
