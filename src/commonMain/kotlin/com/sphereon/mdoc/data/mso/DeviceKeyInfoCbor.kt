package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_tstr
import com.sphereon.cbor.cose.COSE_Key
import com.sphereon.cbor.cose.StringLabel
import kotlin.js.JsExport

@JsExport
data class DeviceKeyInfoJson(
    val deviceKey: cddl_tstr, // FIXME CoseKey,
    val keyAuthorizations: KeyAuthorizationsJson? = null,
    val keyInfo: KeyInfo? = null,
) : JsonView<DeviceKeyInfoCbor>() {
    override fun toCbor(): DeviceKeyInfoCbor {
        TODO("Not yet implemented")
    }

}

@JsExport
data class DeviceKeyInfoCbor(
    val deviceKey: COSE_Key,
    val keyAuthorizations: KeyAuthorizationsCbor? = null,
    val keyInfo: KeyInfo? = null,
) : CborView<DeviceKeyInfoCbor, DeviceKeyInfoJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceKeyInfoCbor> =
        CborMap.builder(this).put(DEVICE_KEY, deviceKey.toCborItem())
            .put(KEY_AUTHORIZATIONS, keyAuthorizations?.toCborItem(), true).put(
                KEY_INFO, keyInfo, true
            ).end()

    override fun toJson(): DeviceKeyInfoJson {
        TODO("Not yet implemented")
    }

    companion object {
        val DEVICE_KEY = StringLabel("deviceKey")
        val KEY_AUTHORIZATIONS = StringLabel("keyAuthorizations")
        val KEY_INFO = StringLabel("keyInfo")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) = DeviceKeyInfoCbor(
            COSE_Key.fromCborItem(DEVICE_KEY.required(m)),
            KEY_AUTHORIZATIONS.optional<CborMap<StringLabel, AnyCborItem>?>(m)?.let {
                KeyAuthorizationsCbor.fromCborItem(it)
            }, KEY_INFO.optional(m)
        )

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }

}
