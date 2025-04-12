package com.sphereon.mdoc.example.app.bluetooth

internal object NopSystemControl : SystemControl {
    override fun showLocationSettings() {} // No-op
    override fun requestToTurnBluetoothOn() {} // No-op
}
