package com.sphereon.ble

import com.sphereon.ble.advertisement.Advertiser
import com.sphereon.ble.client.Client
import com.sphereon.ble.scanner.Scanner
import com.sphereon.ble.server.Server
import setup.applicationContext

actual object BleFactory {
    actual fun provideScanner(): Scanner {
        return Scanner(applicationContext)
    }

    actual fun provideAdvertiser(): Advertiser {
        return Advertiser(applicationContext)
    }

    actual fun provideClient(): Client {
        return Client(applicationContext)
    }

    actual fun provideServer(): Server {
        return Server(applicationContext)
    }
}
