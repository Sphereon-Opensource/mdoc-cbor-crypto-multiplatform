package com.sphereon.ble

import com.sphereon.ble.advertisement.Advertiser

import com.sphereon.ble.client.Client
import com.sphereon.ble.scanner.Scanner
import com.sphereon.ble.server.Server



expect object BleFactory {

    fun provideScanner(): Scanner

    fun provideAdvertiser(): Advertiser

    fun provideClient(): Client

    fun provideServer(): Server
}
