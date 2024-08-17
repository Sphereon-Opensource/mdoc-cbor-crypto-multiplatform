@file:JsExport

package com.sphereon.cbor


import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport



/**
 * We use closed polymorphism here (sealed)
 */
@JsExport
sealed class CborItem<Type>(
    val value: Type,
    cddl: CDDLType
) : CborBaseItem(cddl) {
    protected val HEX_DIGITS = "0123456789abcdef".toCharArray()

    internal abstract fun encode(builder: ByteStringBuilder)

    val majorType = cddl.majorType
    val info = cddl.info


    val asStr: cddl_tstr
        get() {
            require(this is CborString)
            return value
        }

    val asBool: cddl_bool
        get() {
            require(this is CborBool)
            return value
        }

    val asBstr: cddl_bstr
        get() {
            require(this is CborByteString)
            return value
        }

    val asLong: Long
        get() {
            return when (this) {
                is CborNInt -> -value.toLong()
                is CborUInt -> value.toLong()
                else -> throw IllegalArgumentException("Value $value ($cddl) is not long")
            }
        }

    val asInt: Int
        get() {
            return when (this) {
                is CborNInt -> if (value.toLong() > Int.MAX_VALUE) throw IllegalArgumentException("Unsigned value does not fit in Int field") else -value.toInt()
                is CborUInt -> if (value.toLong() > Int.MAX_VALUE) throw IllegalArgumentException("Unsigned value does not fit in Int field") else value.toInt()
                else -> throw IllegalArgumentException("Value $value ($cddl) is not int")
            }
        }

    fun toValue(): Type {
        return value
    }

    abstract fun toJson(): JsonElement

    @Suppress("UNCHECKED_CAST")
    val asMap: cddl_map<Any, Any>
        get() {
            require(this is CborMap<*, *>)
            return this.value as cddl_map<Any, Any>
        }

    @Suppress("UNCHECKED_CAST")
    val asList: cddl_list<Any>
        get() {
            require(this is CborArray<*>)
            return this.value as cddl_list<Any>
        }


    /**
     * The value of a [Tagged] data item.
     *
     * @throws IllegalArgumentException if not the data item isn't of type [Tagged].
     */
    val asTagged: CborItem<Type>
        get() {
            require(this is CborTagged<Type>)
            return this.taggedItem
        }

    /**
     * The decoded CBOR from a bstr with tag [Tagged.ENCODED_CBOR].
     *
     * @throws IllegalArgumentException if the data item isn't a tag with tag [Tagged.ENCODED_CBOR]
     * containing a bstr with valid CBOR.
     */
    val asTaggedEncodedCbor: CborItem<Type>
        get() {
            require(this is CborTagged)
            require(this.tagNumber == CborTagged.ENCODED_CBOR)
            val child = this.taggedItem
            require(child is CborByteString)
            return Cbor.decode(child.value)
        }

    /**
     * Allows subclasses to perform validations on the value
     */
    protected open fun validate() {
    }

    override fun toString(): String {
        return "CborItem(value=$value, cddl=$cddl)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyCborItem) return false

        if (value != other.value) return false
        if (majorType != other.majorType) return false
        if (info != other.info) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (majorType?.hashCode() ?: 0)
        result = 31 * result + (info ?: 0)
        return result
    }


    init {
        this.validate()
    }

}
@JsExport
abstract class CborCollectionItem<Type>(value: Type, cddl: CDDLType) : CborItem<Type>(value, cddl)


typealias AnyCborItem = CborItem<*>


