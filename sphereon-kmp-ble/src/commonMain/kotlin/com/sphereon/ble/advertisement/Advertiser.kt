

package com.sphereon.ble.advertisement

expect class Advertiser {

    suspend fun advertise(settings: AdvertisementSettings)

    suspend fun stop()
}
