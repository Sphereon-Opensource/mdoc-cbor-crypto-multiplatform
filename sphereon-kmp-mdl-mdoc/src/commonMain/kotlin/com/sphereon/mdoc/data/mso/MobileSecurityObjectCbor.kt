package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.StringLabel
import com.sphereon.kmp.LongKMP
import com.sphereon.mdoc.data.DocType
import com.sphereon.mdoc.data.NameSpace
import kotlin.js.JsExport

@JsExport
data class MobileSecurityObjectJson(
    val version: String = "1.0",
    val digestAlgorithm: String,
    val valueDigests: Map<String, Map<String, LongKMP>>,
    val deviceKeyInfo: DeviceKeyInfoJson,
    val docType: String,
    val validityInfo: ValidityInfoJson,
) : JsonView<MobileSecurityObjectCbor>() {
    override fun toCbor(): MobileSecurityObjectCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class MobileSecurityObjectCbor(
    val version: CborString = CborString("1.0"),
    val digestAlgorithm: CborString,
    val valueDigests: CborMap<NameSpace, ValueDigests>,
    val deviceKeyInfo: DeviceKeyInfoCbor,
    val docType: DocType,
    val validityInfo: ValidityInfoCbor,
) : CborView<MobileSecurityObjectCbor, MobileSecurityObjectJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<MobileSecurityObjectCbor> {
        return CborMap.builder(this).put(VERSION, version).put(DIGEST_ALGORITHM, digestAlgorithm)
            .put(VALUE_DIGESTS, valueDigests).put(DEVICE_KEY_INFO, deviceKeyInfo.toCbor()).put(DOC_TYPE, docType)
            .put(
                VALIDITY_INFO, validityInfo.toCbor()
            ).end()

        TODO("Not yet implemented")
    }

    override fun toJson(): MobileSecurityObjectJson {
        TODO("Not yet implemented")
    }

    companion object {
        val VERSION = StringLabel("version")
        val DIGEST_ALGORITHM = StringLabel("digestAlgorithm")
        val VALUE_DIGESTS = StringLabel("valueDigests")
        val DEVICE_KEY_INFO = StringLabel("deviceKeyInfo")
        val DOC_TYPE = StringLabel("docType")
        val VALIDITY_INFO = StringLabel("validityInfo")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): MobileSecurityObjectCbor {
            return MobileSecurityObjectCbor(
                VERSION.required(m),
                DIGEST_ALGORITHM.required(m),
                VALUE_DIGESTS.required(m),
                DeviceKeyInfoCbor.fromCborItem(DEVICE_KEY_INFO.required(m)),
                DOC_TYPE.required(m),
                ValidityInfoCbor.fromCborItem(VALIDITY_INFO.required(m))
            )
        }

        fun cborDecode(data: ByteArray) = fromCborItem(cborSerializer.decode(data))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MobileSecurityObjectCbor

        if (version != other.version) return false
        if (digestAlgorithm != other.digestAlgorithm) return false
        if (valueDigests != other.valueDigests) return false
        if (deviceKeyInfo != other.deviceKeyInfo) return false
        if (docType != other.docType) return false
        if (validityInfo != other.validityInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + digestAlgorithm.hashCode()
        result = 31 * result + valueDigests.hashCode()
        result = 31 * result + deviceKeyInfo.hashCode()
        result = 31 * result + docType.hashCode()
        result = 31 * result + validityInfo.hashCode()
        return result
    }
}
