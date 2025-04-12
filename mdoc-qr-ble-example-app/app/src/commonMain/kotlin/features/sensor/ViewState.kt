package com.sphereon.mdoc.example.app.features.sensor

import kotlin.time.Duration

sealed class ViewState {
    data object BluetoothOff : ViewState()
    data object Connecting : ViewState()
    data class Connected(
        val battery: Int,
        val rssi: Int?,
        val period: Duration,
    ) : ViewState()
    data object Disconnecting : ViewState()
    data object Disconnected : ViewState()
}
