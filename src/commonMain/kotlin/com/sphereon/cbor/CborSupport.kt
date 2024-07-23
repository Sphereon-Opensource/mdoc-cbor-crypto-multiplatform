package com.sphereon.cbor


import kotlinx.serialization.cbor.Cbor
import kotlin.js.JsExport


val cborSerializer = CborSupport.serializer
val kotlinSerializer = CborSupport.kotlinSerializer

@JsExport
object CborSupport {

    val kotlinSerializer by lazy {
        return@lazy Cbor {
            ignoreUnknownKeys = true
//            alwaysUseByteString = true
            encodeDefaults = false
//            writeDefiniteLengths = true
        }
    }

    val serializer by lazy {
        return@lazy com.sphereon.cbor.Cbor
    }

    fun <Type> itemFromValue(value: Type, cddl: CDDL): CborItem<Type> {
        TODO()
//        return cddl.newCborItem(value)
    }

    fun <Type> itemToValue(value: CborItem<Type>): Type? {
        return value.value
    }

    fun <Type> itemToByteArray(value: CborItem<Type>): ByteArray = serializer.encode(value)
    fun <Type> itemFromByteArray(value: ByteArray): CborItem<Type> = serializer.decode(value)

    fun <Type> dataItemFromValue(bytes: ByteArray, value: Type): CborEncodedItem<Type> {
        return CborEncodedItem(value, CborByteString(bytes))
    }

    fun <Type: AnyCborItem> dataItemToValue(value: CborEncodedItem<Type>): Type {
        return CborEncodedItem.toDecodedValue(value)
    }

    fun <Type> dataItemToByteArray(
        value: CborEncodedItem<Type>
    ): ByteArray = serializer.encode(value)

    fun <Type> dataItemFromByteArray(value: ByteArray): CborEncodedItem<Type> =
        serializer.decode(value)

}
