package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.experimental.or

/**
 * Simple (major type 7)
 *
 * @param value the simple value, for example [Simple.TRUE].
 */
abstract class CborSimple<Type : Any?>(value: Type, cddl: CDDL) :
    CborItem<Type>(value, cddl) {

    init {
        val info = when (cddl) {
            is CDDL.bool -> when (value) {
                true -> CDDL.True.info
                else -> CDDL.False.info
            }

            else -> cddl.info
        }
        if (info == null) {
            throw IllegalArgumentException()
        }
        require(info is Int)
        check(info < 24 || (info in 32..255))
    }

    override fun encode(builder: ByteStringBuilder) {
        val majorTypeShifted = (majorType!!.type shl 5).toByte()
        builder.append(majorTypeShifted.or(info!!.toByte()))
    }

    companion object {
        /** The [Simple] value for FALSE */
        val FALSE = CborFalse()

        /** The [Simple] value for TRUE */
        val TRUE = CborTrue()

        /** The [Simple] value for NULL */
        val NULL = CborNull()

        /** The [Simple] value for UNDEFINED */
        val UNDEFINED = CborUndefined()

        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborSimple<out Any?>> {
            val (newOffset, value) = Cbor.decodeLength(encodedCbor, offset)
            if (newOffset - offset > 1 && value < 32UL) {
                throw IllegalArgumentException("two-byte simple value must be >= 32")
            }
            return when (val valueInfo = value.toInt()) {
                CDDL.False.info -> {
                    Pair(newOffset, FALSE)
                }

                CDDL.True.info -> {
                    Pair(newOffset, TRUE)
                }

                CDDL.Null.info -> {
                    Pair(newOffset, NULL)
                }

                CDDL.undefined.info -> {
                    Pair(newOffset, UNDEFINED)
                }

                // TODO: Probably should return a Simpe with UINT
                else -> throw IllegalArgumentException("Unknown simple value ${valueInfo}")
            }
        }
    }

    override fun equals(other: Any?): Boolean = other is CborSimple<*> && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString() = when (this) {
        FALSE -> "Simple(FALSE)"
        TRUE -> "Simple(TRUE)"
        NULL -> "Simple(NULL)"
        UNDEFINED -> "Simple(UNDEFINED)"
        else -> "Simple($value)"
    }

}
