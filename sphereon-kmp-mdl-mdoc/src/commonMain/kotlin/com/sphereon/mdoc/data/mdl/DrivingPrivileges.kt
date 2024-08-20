package com.sphereon.mdoc.data.mdl


import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborFullDate
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborViewListToCborItem
import com.sphereon.cbor.cddl_full_date
import com.sphereon.cbor.cddl_tstr
import com.sphereon.cbor.instantToDateStringISO
import com.sphereon.cbor.localDateTimeToDateStringISO
import com.sphereon.cbor.localDateToCborFullDate
import com.sphereon.cbor.localDateToDateStringISO
import com.sphereon.cbor.toCborString
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.LocalDateTimeKMP
import com.sphereon.kmp.getDateTime
import com.sphereon.kmp.toKotlin
import com.sphereon.mdoc.mdocJsonSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName


typealias CborDrivingPrivilegesBuilder = DrivingPrivilegesCbor.Builder

@JsExport
data class DrivingPrivilegesCbor(
    private val backing: List<DrivingPrivilegeCbor> = mutableListOf()
) : List<DrivingPrivilegeCbor> by backing,
    CborView<DrivingPrivilegesCbor, DrivingPrivilegesJson, CborArray<CborMap<StringLabel, AnyCborItem>>>(CDDL.list) {
    @JsName("fromVarArgs")
    constructor(vararg drivingPrivileges: DrivingPrivilegeCbor) : this(drivingPrivileges.toMutableList())

    @JsName("fromCborArray")
    constructor(list: CborArray<CborMap<StringLabel, AnyCborItem>>) :
            this(list.value.map {
                DrivingPrivilegeCbor(
                    DrivingPrivilegeCbor.Static.VEHICLE_CATEGORY_CODE.required(it),
                    DrivingPrivilegeCbor.Static.ISSUE_DATE.optional(it),
                    DrivingPrivilegeCbor.Static.EXPIRY_DATE.optional(it),
                    DrivingPrivilegeCbor.Static.CODES.optional(it)
                )
            })


    data class Builder(val privilegeBuilders: MutableList<CborDrivingPrivilegeBuilder> = mutableListOf()) {
        fun newPrivilege(): CborDrivingPrivilegeBuilder {
            val privilegeBuilder = CborDrivingPrivilegeBuilder(parent = this)
            this.privilegeBuilders.add(privilegeBuilder)
            return privilegeBuilder
        }

        fun build() = DrivingPrivilegesCbor(privilegeBuilders.map { it.build() }.toMutableList())

    }

    override fun cborBuilder(): CborBuilder<DrivingPrivilegesCbor> {
        return CborArray.Static.builder(this).addCborArray(this.backing.cborViewListToCborItem()).end()
    }

    override fun toJson(): DrivingPrivilegesJson {
        return DrivingPrivilegesJson(backing.map { it.toJson() })
    }

    override fun toString(): String {
        return "DrivingPrivileges(privileges=$backing)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DrivingPrivilegesCbor) return false

        if (backing != other.backing) return false

        return true
    }

    override fun hashCode(): Int {
        return backing.hashCode()
    }

    object Static {
        @JsName("fromJson")
        fun fromJson(simple: DrivingPrivilegesJson): DrivingPrivilegesCbor {
            return DrivingPrivilegesCbor(simple.backing.map { it.toCbor() })
        }

        @JsName("cborDecode")
        fun cborDecode(encodedDrivingPrivilegesCbor: ByteArray): DrivingPrivilegesCbor {
            val cborArray: CborArray<CborMap<StringLabel, AnyCborItem>> = Cbor.decode(encodedDrivingPrivilegesCbor)
            return DrivingPrivilegesCbor(cborArray)
        }
    }


}

@JsExport
data class DrivingPrivilegesJson(val backing: List<DrivingPrivilegeJson> = mutableListOf()) :
    List<DrivingPrivilegeJson> by backing, JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DrivingPrivilegesCbor {
        return DrivingPrivilegesCbor(backing.map { it.toCbor() })
    }

}

typealias CborDrivingPrivilegeBuilder = DrivingPrivilegeCbor.Builder

@JsExport
data class DrivingPrivilegeJson(
    @SerialName("vehicle_category_code")
    val vehicle_category_code: String,
    @SerialName("issue_date")
    val issue_date: LocalDateTimeKMP? = null,
    @SerialName("expiry_date")
    val expiry_date: LocalDateTimeKMP? = null,
    @SerialName("codes")
    val codes: List<DrivingPrivilegesCodeJson>? = null
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DrivingPrivilegeCbor {
        return DrivingPrivilegeCbor.Static.fromJson(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DrivingPrivilegeJson) return false

        if (vehicle_category_code != other.vehicle_category_code) return false
        if (issue_date != other.issue_date) return false
        if (expiry_date != other.expiry_date) return false
        if (codes != other.codes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vehicle_category_code.hashCode()
        result = 31 * result + (issue_date?.hashCode() ?: 0)
        result = 31 * result + (expiry_date?.hashCode() ?: 0)
        result = 31 * result + (codes?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "DrivingPrivilegeSimple(vehicle_category_code='$vehicle_category_code', issue_date=$issue_date, expiry_date=$expiry_date, codes=$codes)"
    }


}

@JsExport
data class DrivingPrivilegeCbor(
    val vehicle_category_code: CborString,
    val issue_date: CborFullDate? = null,
    val expiry_date: CborFullDate? = null,
    val codes: List<DrivingPrivilegesCodeCbor>? = null
) : CborView<DrivingPrivilegeCbor, DrivingPrivilegeJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {

    override fun cborBuilder(): CborBuilder<DrivingPrivilegeCbor> {
        val builder = CborMap.Static.builder(this)
            .put(Static.VEHICLE_CATEGORY_CODE, vehicle_category_code)
            .put(Static.ISSUE_DATE, issue_date, true)
            .put(Static.EXPIRY_DATE, expiry_date, true)
            .put(Static.CODES, codes?.cborViewListToCborItem(), true)
        return builder.end()
    }

    override fun toJson(): DrivingPrivilegeJson {
        return DrivingPrivilegeJson(
            this.vehicle_category_code.value,
            issue_date?.value?.let { LocalDateTimeKMP.Static.fromString(it) },
            expiry_date?.value?.let { LocalDateTimeKMP.Static.fromString(it) },
            codes?.map { it.toJson() }
        )
    }

    object Static {
        val VEHICLE_CATEGORY_CODE = StringLabel("vehicle_category_code")
        val ISSUE_DATE = StringLabel("issue_date")
        val EXPIRY_DATE = StringLabel("expiry_date")
        val CODES = StringLabel("codes")

        @JsName("fromJson")
        fun fromJson(simple: DrivingPrivilegeJson): DrivingPrivilegeCbor {
            return DrivingPrivilegeCbor(
                simple.vehicle_category_code.toCborString(),
                simple.issue_date?.localDateToCborFullDate(),
                simple.expiry_date?.localDateToCborFullDate(),
                simple.codes?.map { it.toCbor() }
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encodedDrivingPrivilegeCbor: ByteArray): DrivingPrivilegeCbor {
            val m: CborMap<StringLabel, AnyCborItem> = Cbor.decode(encodedDrivingPrivilegeCbor)
            return DrivingPrivilegeCbor(
                VEHICLE_CATEGORY_CODE.required(m),
                ISSUE_DATE.optional(m),
                EXPIRY_DATE.optional(m),
                CODES.optional(m),
            )
        }
    }

    data class Builder(
        val parent: DrivingPrivilegesCbor.Builder? = null,
        var vehicleCategoryCode: CborString? = null,
        var issueDate: CborFullDate? = null,
        var expiryDate: CborFullDate? = null,
        var codes: MutableList<DrivingPrivilegesCodeCbor>? = mutableListOf()
    ) {
        fun newPrivilege(): Builder {
            if (parent == null) {
                throw IllegalArgumentException("No parent present. Cannot call add")
            }
            return parent.newPrivilege()
        }

        fun buildPrivileges(): DrivingPrivilegesCbor {
            if (parent == null) {
                throw IllegalArgumentException("No parent present. Cannot call add")
            }
            return parent.build()
        }

        fun withVehicleCategoryCode(vehicleCategoryCode: cddl_tstr) =
            apply { this.vehicleCategoryCode = vehicleCategoryCode.toCborString() }

        fun withIssueDateUsingLocalDateTime(
            issueDate: LocalDateTimeKMP,
            utils: DateTimeUtils = DateTimeUtils.Static.DEFAULT,
            timeZoneId: String? = null
        ) =
            apply {
                this.issueDate = CborFullDate(issueDate.toKotlin().localDateTimeToDateStringISO(utils, timeZoneId))
            }

        fun withIssueDateUsingLocalDate(
            issueDate: LocalDate,
            utils: DateTimeUtils = DateTimeUtils.Static.DEFAULT,
            timeZoneId: String? = null
        ) =
            apply { this.issueDate = CborFullDate(issueDate.localDateToDateStringISO(utils, timeZoneId)) }


        fun withIssueDateUsingEpochSeconds(
            epochSeconds: Int,
            utils: DateTimeUtils = DateTimeUtils.Static.DEFAULT,
            timeZoneId: String? = null
        ) =
            apply { this.issueDate = CborFullDate(getDateTime(utils).dateISO(timeZoneId, epochSeconds)) }

        fun withIssueDate(issueDate: cddl_full_date) = apply { this.issueDate = CborFullDate(issueDate) }

        fun withExpiryDateUsingLocalDateTime(
            issueDate: LocalDateTimeKMP,
            utils: DateTimeUtils = DateTimeUtils.Static.DEFAULT,
            timeZoneId: String? = null
        ) =
            apply {
                this.expiryDate = CborFullDate(issueDate.toKotlin().localDateTimeToDateStringISO(utils, timeZoneId))
            }

        fun withExpiryDateUsingLocalDate(
            issueDate: LocalDate,
            utils: DateTimeUtils = DateTimeUtils.Static.DEFAULT,
            timeZoneId: String? = null
        ) =
            apply { this.expiryDate = CborFullDate(issueDate.localDateToDateStringISO(utils, timeZoneId)) }

        fun withExpiryDateUsingInstant(
            issueDate: Instant,
            utils: DateTimeUtils = DateTimeUtils.Static.DEFAULT,
            timeZoneId: String? = null
        ) =
            apply { this.expiryDate = CborFullDate(issueDate.instantToDateStringISO(utils, timeZoneId)) }

        fun withExpiryDate(expiryDate: cddl_full_date) = apply { this.expiryDate = CborFullDate(expiryDate) }
        fun withDates(issueDate: cddl_full_date?, expiryDate: cddl_full_date?) = apply {
            issueDate?.let { withIssueDate(it) }
            expiryDate?.let { withExpiryDate(it) }
        }

        //        fun addCodes(vararg codes: DrivingPrivilegesCode) = apply { this.codes!!.addAll(codes) }
        fun addCodes(vararg codes: DrivingPrivilegesCodeJson) =
            apply { this.codes!!.addAll(codes.map { it.toCbor() }) }

        fun addCode(code: cddl_tstr, sign: cddl_tstr? = null, value: cddl_tstr? = null) = apply {
            this.addCodes(DrivingPrivilegesCodeJson(code, sign, value))
        }

        //        fun withCodes(vararg codes: DrivingPrivilegesCode) = apply { this.codes = codes.toMutableList() }
        fun withCodes(vararg codes: DrivingPrivilegesCodeJson) =
            apply { this.codes = codes.map { it.toCbor() }.toMutableList() }


        fun end(): DrivingPrivilegesCbor.Builder? {
            return parent
        }

        fun build(): DrivingPrivilegeCbor {
            val catCode = vehicleCategoryCode
            return if (catCode == null)
                throw IllegalArgumentException("Vehicle category code must not be null")
            else
                DrivingPrivilegeCbor(
                    vehicle_category_code = catCode,
                    issue_date = issueDate,
                    expiry_date = expiryDate,
                    codes = if (codes.isNullOrEmpty()) null else codes
                )
        }
    }

    override fun toString(): String {
        return "DrivingPrivilege(vehicle_category_code=$vehicle_category_code, issue_date=$issue_date, expiry_date=$expiry_date, codes=$codes)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DrivingPrivilegeCbor

        if (vehicle_category_code != other.vehicle_category_code) return false
        if (issue_date != other.issue_date) return false
        if (expiry_date != other.expiry_date) return false
        if (codes != other.codes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vehicle_category_code.hashCode()
        result = 31 * result + (issue_date?.hashCode() ?: 0)
        result = 31 * result + (expiry_date?.hashCode() ?: 0)
        result = 31 * result + (codes?.hashCode() ?: 0)
        return result
    }


}

@JsExport
data class DrivingPrivilegesCodeCbor(val code: CborString, val sign: CborString?, val value: CborString?) :
    CborView<DrivingPrivilegesCodeCbor, DrivingPrivilegesCodeJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DrivingPrivilegesCodeCbor> {
        return CborMap.Static.builder(this)
            .put(Static.CODE, code)
            .put(Static.SIGN, sign, true)
            .put(Static.VALUE, value, true).end()
    }

    override fun toJson(): DrivingPrivilegesCodeJson =
        DrivingPrivilegesCodeJson(code.value, sign?.value, value?.value)


    object Static {
        val CODE = StringLabel("code")
        val SIGN = StringLabel("sign")
        val VALUE = StringLabel("value")

        @JsName("fromJson")
        fun fromJson(simple: DrivingPrivilegesCodeJson): DrivingPrivilegesCodeCbor {
            return DrivingPrivilegesCodeCbor(
                simple.code.toCborString(),
                simple.sign?.toCborString(),
                simple.value?.toCborString()
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray): DrivingPrivilegeCbor {
            val m: CborMap<StringLabel, AnyCborItem> = Cbor.decode(encoded)
            return DrivingPrivilegeCbor(CODE.required(m), SIGN.optional(m), VALUE.optional(m))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DrivingPrivilegesCodeCbor) return false

        if (code != other.code) return false
        if (sign != other.sign) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + (sign?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CborDrivingPrivilegesCode(code=$code, sign=$sign, value=$value)"
    }


}

@JsExport
data class DrivingPrivilegesCodeJson(val code: cddl_tstr, val sign: cddl_tstr?, val value: cddl_tstr?) :
    JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)

    override fun toCbor(): DrivingPrivilegesCodeCbor =
        DrivingPrivilegesCodeCbor(code.toCborString(), sign?.toCborString(), value?.toCborString())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DrivingPrivilegesCodeJson) return false

        if (code != other.code) return false
        if (sign != other.sign) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + (sign?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }


    override fun toString(): String {
        return "SimpleDrivingPrivilegesCode(code='$code', sign=$sign, value=$value)"
    }


}
