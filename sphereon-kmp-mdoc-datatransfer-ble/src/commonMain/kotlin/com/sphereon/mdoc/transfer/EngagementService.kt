@file:OptIn(ExperimentalUuidApi::class)

package com.sphereon.mdoc.transfer

import com.sphereon.cbor.toCborBool
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.KeyEncoding
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.cose.CoseCurve
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.kms.IKeyManagerService
import com.sphereon.mdoc.transfer.device.BleOptionsCbor
import com.sphereon.mdoc.transfer.device.DeviceEngagementCbor
import com.sphereon.mdoc.transfer.device.DeviceEngagementSecurityCbor
import com.sphereon.mdoc.transfer.device.DeviceRetrievalMethodCbor
import com.sphereon.mdoc.transfer.device.DeviceRetrievalMethodType
import kotlin.js.JsExport
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JsExport
enum class EngagementRole {
    HOLDER,
    VERIFIER
}

@JsExport
class EngagementService private constructor(
    private val ephemeralHolderDeviceKey: IResolvedKeyInfo<ICoseKeyCbor>,
    private val retrievalMethods: Set<DeviceRetrievalMethod>,
    private val curve: Curve = Curve.P_256,
    private val role: EngagementRole = EngagementRole.HOLDER,
    private var deviceEngagement: DeviceEngagementCbor? = null,
    private var blePeripheralServerModeUUID: Uuid? = null,
    private var bleCentralClientModeUUID: Uuid? = null

) {

    fun getEphemeralHolderDeviceKey() = ephemeralHolderDeviceKey
    fun getRetrievalMethods() = retrievalMethods.toTypedArray()
    fun getCurve() = curve
    fun getRole() = role

    fun isBleSupported() = retrievalMethods.any { it is BleRetrievalMethod }
    fun isNfcSupported() = false
    fun isWifiAwareSupported() = false

    fun getDeviceEngagement(): DeviceEngagementCbor {
        if (deviceEngagement === null) {
            this.deviceEngagement = createDeviceEngagement()
        }
        return this.deviceEngagement!!
    }

    fun getBlePeripheralServerModeUuid(): Uuid? {
        if (blePeripheralServerModeUUID == null && getBleRetrievalMethod()?.peripheralServerMode == true) {
            this.blePeripheralServerModeUUID = Uuid.random()
        }
        return blePeripheralServerModeUUID
    }

    fun getBleCentralClientModeUuid(): Uuid? {
        if (bleCentralClientModeUUID == null && getBleRetrievalMethod()?.centralClientMode == true) {
            this.bleCentralClientModeUUID = Uuid.random()
        }
        return bleCentralClientModeUUID
    }

    fun getBleRetrievalMethod(): BleRetrievalMethod? = retrievalMethods.firstOrNull { method -> method is BleRetrievalMethod } as BleRetrievalMethod?

    @OptIn(ExperimentalUuidApi::class)
    private fun createDeviceEngagement(): DeviceEngagementCbor {
        check(role == EngagementRole.HOLDER) { "Only holder role is supported to create a device engagement" }
        val bleMethod = getBleRetrievalMethod()
        checkNotNull(bleMethod) { "Only BLE retrieval method is supported" }
        check(!bleMethod.peripheralServerMode) { "Peripheral server mode is not supported. Cannot generate QR code" }
        check(bleMethod.centralClientMode) { "Only central client mode is supported. Cannot generate QR code" }

        val bleRetrievalOptions = BleOptionsCbor(
            peripheralServerMode = bleMethod.peripheralServerMode.toCborBool(),
            peripheralServerModeUUID = getBlePeripheralServerModeUuid()?.toByteArray()?.toCborByteString(),
            centralClientMode = bleMethod.centralClientMode.toCborBool(),
            centralClientModeUUID = getBleCentralClientModeUuid()?.toByteArray()?.toCborByteString()
        )
        return DeviceEngagementCbor(
            security = DeviceEngagementSecurityCbor(
                cypherSuite = curve.cose.toCbor(),
                eDeviceKeyBytes = CoseKeyCbor.Static.fromDTO(ephemeralHolderDeviceKey.key)
            ),
            deviceRetrievalMethods = arrayOf(
                DeviceRetrievalMethodCbor(
                    type = DeviceRetrievalMethodType.BLE.toCborItem(),
                    retrievalOptions = bleRetrievalOptions
                )
            )

        )
    }

    fun generateQrEngagementData() = with(getDeviceEngagement()) {
        checkNotNull(deviceRetrievalMethods) { "Device retrieval methods are not present. Cannot generate QR code" }
        val ble =
            (deviceRetrievalMethods as Array<out DeviceRetrievalMethodCbor>).firstOrNull { method -> method.type == DeviceRetrievalMethodType.BLE.toCborItem() }
        checkNotNull(ble) { "Only BLE is currently supported. Cannot generate QR code" }
        check(ble.retrievalOptions is BleOptionsCbor) { "BLE retrieval options are not present. Cannot generate QR code" }
        val deviceRetrievalOptions = ble.retrievalOptions as BleOptionsCbor
        check(!deviceRetrievalOptions.peripheralServerMode.value) { "Peripheral server mode is not supported. Cannot generate QR code" }
        check(deviceRetrievalOptions.centralClientMode.value) { "Only central client mode is supported. Cannot generate QR code" }
        checkNotNull(deviceRetrievalOptions.centralClientModeUUID?.value) { "Central client mode UUID is not present. Cannot generate QR code" }
        "mdoc:${this.toBase64Url()}"
    }


    object Static {
        fun holderBuilder() = HolderBuilder()

        fun verifierFromDeviceEngagement(engagement: DeviceEngagementCbor): EngagementService {
            var retrievalMethods = mutableSetOf<DeviceRetrievalMethod>()
            var blePeripheralServerModeUUID: Uuid? = null
            var bleCentralClientModeUUID: Uuid? = null
            engagement.deviceRetrievalMethods?.forEach { method ->
                if (method.type == DeviceRetrievalMethodType.BLE.toCborItem()) {
                    if (method.retrievalOptions is BleOptionsCbor) {
                        val bleOptions = method.retrievalOptions as BleOptionsCbor
                        retrievalMethods.add(BleRetrievalMethod(bleOptions.centralClientMode.value, bleOptions.peripheralServerMode.value))
                        bleCentralClientModeUUID = bleOptions.centralClientModeUUID?.value?.let(Uuid::fromByteArray)
                        blePeripheralServerModeUUID = bleOptions.peripheralServerModeUUID?.value?.let(Uuid::fromByteArray)
                    }
                }
            }
            return EngagementService(
                deviceEngagement = engagement,
                ephemeralHolderDeviceKey = ResolvedKeyInfo(key = engagement.security.eDeviceKeyBytes),
                curve = Curve.Static.fromCose(CoseCurve.Static.fromValue(engagement.security.cypherSuite.value.toInt())),
                role = EngagementRole.VERIFIER,
                retrievalMethods = retrievalMethods,
                bleCentralClientModeUUID = bleCentralClientModeUUID,
                blePeripheralServerModeUUID = blePeripheralServerModeUUID
            )
        }
    }

    class HolderBuilder {
        private var ephemeralDeviceKey: IResolvedKeyInfo<*>? = null
        private var retrievalMethods: Set<DeviceRetrievalMethod> = emptySet()
        private var curve: Curve? = ephemeralDeviceKey?.signatureAlgorithm?.curve ?: ephemeralDeviceKey?.key?.getSignatureAlgorithm()?.curve
        private val role: EngagementRole = EngagementRole.HOLDER

        @JsExport.Ignore
        suspend fun withEphemeralDeviceKeyFromKeyManager(keyManager: IKeyManagerService, signatureAlgorithm: SignatureAlgorithm, kms: String?) =
            apply {
                this.ephemeralDeviceKey =
                    keyManager.generateKeyAsync(kms = kms, alg = signatureAlgorithm)
                        .toManagedKeyInfo<ICoseKeyCbor>(keyEncoding = KeyEncoding.COSE)
            }

        fun withEphemeralDeviceKey(
            ephemeralDeviceKey: IResolvedKeyInfo<*>,
            curve: Curve? = ephemeralDeviceKey.signatureAlgorithm?.curve
                ?: ephemeralDeviceKey.key.getSignatureAlgorithm()?.curve
        ) = apply {
            this.ephemeralDeviceKey = ephemeralDeviceKey
            this.curve = curve
        }

        fun withRetrievalMethods(retrievalMethods: Array<DeviceRetrievalMethod>) = apply { this.retrievalMethods = retrievalMethods.toMutableSet() }
        fun addRetrievalMethod(retrievalMethod: DeviceRetrievalMethod) = apply { this.retrievalMethods += retrievalMethod }
        fun build(): EngagementService {
            require(ephemeralDeviceKey !== null) { "An ephemeral device key needs to be provided" }
            require(retrievalMethods.isNotEmpty()) { "At least one device retrieval method needs to be provided" }
            val ephemeralCborKey = CoseJoseKeyMappingService.toResolvedCoseKeyInfo(ephemeralDeviceKey!!)
            return EngagementService(ephemeralCborKey,this.retrievalMethods, curve ?: Curve.P_256, role = role)
        }
    }
}
