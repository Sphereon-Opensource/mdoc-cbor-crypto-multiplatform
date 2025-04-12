package com.sphereon.mdoc.example.app.bluetooth.requirements

import kotlinx.coroutines.flow.Flow

public enum class Deficiency {
    LocationServicesDisabled, // Android (API 30 and lower) only.
    BluetoothOff,
}

public interface BluetoothRequirements {

    /**
     * On Apple, a Bluetooth permission dialog is shown on subscription (if not already authorized).
     */
    public val deficiencies: Flow<Set<Deficiency>>
}
