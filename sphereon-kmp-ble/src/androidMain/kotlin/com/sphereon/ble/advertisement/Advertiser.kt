

package com.sphereon.ble.advertisement

import android.annotation.SuppressLint
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn

actual class Advertiser(private val context: Context) {

    private var job: Job? = null

    private val scope = CoroutineScope(SupervisorJob())

    @SuppressLint("MissingPermission")
    actual suspend fun advertise(settings: AdvertisementSettings) {
        val advertiser = BleAdvertiser.create(context)

        val advertiserConfig = BleAdvertisingConfig(
            settings = BleAdvertisingSettings(
                deviceName = settings.name,
                legacyMode = true,
                scannable = true,
                connectable = true
            ),
            advertiseData = BleAdvertisingData(
                ParcelUuid(settings.uuid),
            )
        )

        job = advertiser.advertise(advertiserConfig).launchIn(scope)
    }

    actual suspend fun stop() {
        job?.cancel()
    }
}
