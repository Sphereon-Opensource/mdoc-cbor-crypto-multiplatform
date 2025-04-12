package com.sphereon.mdoc.example.app.bluetooth.requirements

import androidx.compose.runtime.Composable

public fun interface BluetoothRequirementsFactory {
    public fun create(): BluetoothRequirements
}

@Composable
public expect fun rememberBluetoothRequirementsFactory(): BluetoothRequirementsFactory
