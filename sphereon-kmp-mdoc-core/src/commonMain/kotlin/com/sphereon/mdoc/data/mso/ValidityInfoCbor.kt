package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborTDate
import com.sphereon.cbor.CborTagged
import com.sphereon.cbor.CborView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_tdate
import com.sphereon.cbor.localDateToDateStringISO
import com.sphereon.json.JsonView
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.LocalDateTimeKMP
import com.sphereon.kmp.Logger
import com.sphereon.mdoc.MdocConst
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
        CborMap.Static.builder(this).put(Static.SIGNED, signed.asTagged).put(Static.VALID_FROM, validFrom.asTagged)
            .put(Static.VALID_UNTIL, validUntil.asTagged)
            .put(Static.EXPECTED_UPDATE, expectedUpdate?.asTagged, true).end()


    override fun toJson() = ValidityInfoJson(signed.value, validFrom.value, validUntil.value, expectedUpdate?.value)

    object Static {
        val SIGNED = StringLabel("signed")
        val VALID_FROM = StringLabel("validFrom")
        val VALID_UNTIL = StringLabel("validUntil")
        val EXPECTED_UPDATE = StringLabel("expectedUpdate")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) = ValidityInfoCbor(
            SIGNED.required<AnyCborItem>(m).let {
                when (it) {
                    is CborTDate -> it
                    is CborString -> CborTDate(it.value).also { MdocConst.LOG.warn("Validity info needs to have tagged dates, but a string was encountered. Issuer is not issuing valid mdocs!") }
                    is CborTagged<*> -> CborTDate(it.value as String)
                    else -> throw IllegalArgumentException(
                        "tdate object expected. Got ${it.cddl}"
                    )
                }
            },
            VALID_FROM.required<AnyCborItem>(m).let {
                when (it) {
                    is CborTDate -> it
                    is CborString -> CborTDate(it.value).also { MdocConst.LOG.warn("Validity info needs to have tagged dates, but a string was encountered. Issuer is not issuing valid mdocs!") }
                    is CborTagged<*> -> CborTDate(it.value as String)
                    else -> throw IllegalArgumentException(
                        "tdate object expected. Got ${it.cddl}"
                    )
                }
            },
            VALID_UNTIL.required<AnyCborItem>(m).let {
                when (it) {
                    is CborTDate -> it
                    is CborString -> CborTDate(it.value).also { MdocConst.LOG.warn("Validity info needs to have tagged dates, but a string was encountered. Issuer is not issuing valid mdocs!") }
                    is CborTagged<*> -> CborTDate(it.value as String)
                    else -> throw IllegalArgumentException(
                        "tdate object expected. Got ${it.cddl}"
                    )
                }
            },
            EXPECTED_UPDATE.optional<AnyCborItem?>(m)
                ?.let {
                    when (it) {
                        is CborTDate -> it
                        is CborString -> CborTDate(it.value).also { MdocConst.LOG.warn("Validity info needs to have tagged dates, but a string was encountered. Issuer is not issuing valid mdocs!") }
                        is CborTagged<*> -> CborTDate(it.value as String)
                        else -> throw IllegalArgumentException("tdate object expected. Got ${it.cddl}")
                    }
                }
        )

        fun fromDates(
            signed: LocalDateTimeKMP = DateTimeUtils.Static.DEFAULT.dateTimeLocal(),
            validFrom: LocalDateTimeKMP = DateTimeUtils.Static.DEFAULT.dateTimeLocal(),
            validUntil: LocalDateTimeKMP,
            expectedUpdate: LocalDateTimeKMP? = null,
            utils: DateTimeUtils = DateTimeUtils.Static.DEFAULT,
            timeZoneId: String? = null
        ) = ValidityInfoCbor(
            signed = CborTDate(signed.localDateToDateStringISO(utils, timeZoneId)),
            validFrom = CborTDate(validFrom.localDateToDateStringISO(utils, timeZoneId)),
            validUntil = CborTDate(validUntil.localDateToDateStringISO(utils, timeZoneId)),
            expectedUpdate = expectedUpdate?.let { CborTDate(it.localDateToDateStringISO(utils, timeZoneId)) })


        fun cborDecode(encoded: ByteArray): ValidityInfoCbor = fromCborItem(cborSerializer.decode(encoded))
    }
}
