package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborTDate
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_tdate
import com.sphereon.json.mdocJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport

@JsExport
@Serializable
data class ValidityInfoJson(
    val signed: cddl_tdate,
    val validFrom: cddl_tdate,
    val validUntil: cddl_tdate,
    val expectedUpdate: cddl_tdate? = null,
    ) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor() = ValidityInfoCbor(CborTDate(signed),
        CborTDate(validFrom),
        CborTDate(validUntil),
        expectedUpdate?.let { CborTDate(it) })
}

@JsExport
data class ValidityInfoCbor(
    val signed: CborTDate,
    val validFrom: CborTDate,
    val validUntil: CborTDate,
    val expectedUpdate: CborTDate? = null
) : CborView<ValidityInfoCbor, ValidityInfoJson, CborMap<StringLabel, CborTDate>>(CDDL.map) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ValidityInfoCbor

        if (signed != other.signed) return false
        if (validFrom != other.validFrom) return false
        if (validUntil != other.validUntil) return false
        if (expectedUpdate != other.expectedUpdate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = signed.hashCode()
        result = 31 * result + validFrom.hashCode()
        result = 31 * result + validUntil.hashCode()
        result = 31 * result + (expectedUpdate?.hashCode() ?: 0)
        return result
    }

    override fun cborBuilder(): CborBuilder<ValidityInfoCbor> =
        CborMap.Static.builder(this).put(Static.SIGNED, signed).put(Static.VALID_FROM, validFrom).put(Static.VALID_UNTIL, validUntil)
            .put(Static.EXPECTED_UPDATE, expectedUpdate, true).end()


    override fun toJson() = ValidityInfoJson(signed.value, validFrom.value, validUntil.value, expectedUpdate?.value)

    object Static {
        val SIGNED = StringLabel("signed")
        val VALID_FROM = StringLabel("validFrom")
        val VALID_UNTIL = StringLabel("validUntil")
        val EXPECTED_UPDATE = StringLabel("expectedUpdate")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) = ValidityInfoCbor(
            SIGNED.required(m),
            VALID_FROM.required(m),
            VALID_UNTIL.required(m),
            EXPECTED_UPDATE.optional(m)
        )

        fun cborDecode(encoded: ByteArray): ValidityInfoCbor = fromCborItem(cborSerializer.decode(encoded))
    }
}
