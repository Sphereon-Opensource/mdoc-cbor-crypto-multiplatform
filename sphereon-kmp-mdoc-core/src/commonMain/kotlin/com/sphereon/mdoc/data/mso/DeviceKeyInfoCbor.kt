package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.crypto.cose.COSE_Key
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.kmp.LongKMP
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport

@JsExport
@Serializable
data class DeviceKeyInfoJson(
    val deviceKey: CoseKeyJson,
    val keyAuthorizations: KeyAuthorizationsJson? = null,
    // FIXME
    val keyInfo: MutableMap<LongKMP, String>? = null,
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DeviceKeyInfoCbor {
        TODO("Not yet implemented")
    }

}

@JsExport
data class DeviceKeyInfoCbor(
    val deviceKey: CoseKeyCbor,
    val keyAuthorizations: KeyAuthorizationsCbor? = null,
    val keyInfo: KeyInfoCbor? = null,
) : CborView<DeviceKeyInfoCbor, DeviceKeyInfoJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceKeyInfoCbor> =
        CborMap.Static.builder(this).put(Static.DEVICE_KEY, deviceKey.toCbor())
            .put(Static.KEY_AUTHORIZATIONS, keyAuthorizations?.toCbor(), true).put(
                Static.KEY_INFO, keyInfo, true
            ).end()

    override fun toJson(): DeviceKeyInfoJson {
        return DeviceKeyInfoJson(deviceKey = deviceKey.toJson(), keyAuthorizations = keyAuthorizations?.toJson(), keyInfo = null /*FIXME*/)
    }

    object Static {
        val DEVICE_KEY = StringLabel("deviceKey")
        val KEY_AUTHORIZATIONS = StringLabel("keyAuthorizations")
        val KEY_INFO = StringLabel("keyInfo")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) = DeviceKeyInfoCbor(
            CoseKeyCbor.Static.fromCborItem(DEVICE_KEY.required(m)),
            KEY_AUTHORIZATIONS.optional<CborMap<StringLabel, AnyCborItem>?>(m)?.let {
                KeyAuthorizationsCbor.Static.fromCborItem(it)
            }, KEY_INFO.optional(m)
        )

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }

}
