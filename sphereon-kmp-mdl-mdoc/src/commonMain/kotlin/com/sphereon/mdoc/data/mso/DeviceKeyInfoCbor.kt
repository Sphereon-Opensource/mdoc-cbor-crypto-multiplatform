package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.crypto.cose.COSE_Key
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.cbor.StringLabel
import com.sphereon.kmp.LongKMP
import kotlinx.serialization.Contextual
import kotlin.js.JsExport

@JsExport
data class DeviceKeyInfoJson(
    val deviceKey: CoseKeyJson,
    val keyAuthorizations: KeyAuthorizationsJson? = null,
    @Contextual
    val keyInfo: MutableMap<LongKMP, *>? = null,
) : JsonView<DeviceKeyInfoCbor>() {
    override fun toCbor(): DeviceKeyInfoCbor {
        TODO("Not yet implemented")
    }

}

@JsExport
data class DeviceKeyInfoCbor(
    val deviceKey: COSE_Key,
    val keyAuthorizations: KeyAuthorizationsCbor? = null,
    val keyInfo: KeyInfoCbor? = null,
) : CborView<DeviceKeyInfoCbor, DeviceKeyInfoJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceKeyInfoCbor> =
        CborMap.builder(this).put(DEVICE_KEY, deviceKey.toCbor())
            .put(KEY_AUTHORIZATIONS, keyAuthorizations?.toCbor(), true).put(
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
