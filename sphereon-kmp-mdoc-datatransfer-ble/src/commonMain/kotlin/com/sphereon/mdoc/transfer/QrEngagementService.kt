package com.sphereon.mdoc.transfer

import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.generic.Curve
import com.sphereon.mdoc.transfer.ble.DeviceRetrievalMethod
import kotlin.js.JsExport

@JsExport
class QrEngagementService internal constructor(
    private val ephemeralDeviceKey: IResolvedKeyInfo<ICoseKeyCbor>,
    private val retrievalMethods: Set<DeviceRetrievalMethod>,
    private val curve: Curve
) {

    class Builder {
        private var ephemeralDeviceKey: IResolvedKeyInfo<*>? = null
        private var retrievalMethods: Set<DeviceRetrievalMethod> = emptySet()
        private var curve: Curve? = ephemeralDeviceKey?.signatureAlgorithm?.curve ?: ephemeralDeviceKey?.key?.getSignatureAlgorithm()?.curve

        fun withEphemeralDeviceKey(
            ephemeralDeviceKey: IResolvedKeyInfo<*>,
            curve: Curve? = ephemeralDeviceKey.signatureAlgorithm?.curve
                ?: ephemeralDeviceKey.key.getSignatureAlgorithm()?.curve
        ) = apply {
            this.ephemeralDeviceKey = ephemeralDeviceKey
            this.curve = curve
        }

        fun withRetrievalMethods(retrievalMethods: Set<DeviceRetrievalMethod>) = apply { this.retrievalMethods = retrievalMethods }
        fun addRetrievalMethod(retrievalMethod: DeviceRetrievalMethod) = apply { this.retrievalMethods += retrievalMethod }
        fun build(): QrEngagementService {
            require(ephemeralDeviceKey !== null) { "An ephemeral device key needs to be provided" }
            require(retrievalMethods.isNotEmpty()) { "At least one device retrieval method needs to be provided" }
            val ephemeralCborKey = CoseJoseKeyMappingService.toResolvedCoseKeyInfo(ephemeralDeviceKey!!)
            return QrEngagementService(ephemeralCborKey, retrievalMethods, curve ?: Curve.P_256)
        }
    }
}
