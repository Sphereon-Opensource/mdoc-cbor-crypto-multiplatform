package com.sphereon.cbor

/**
 * Map builder.
 */
class MapBuilder<T>(private val parent: T, private val map: CborMap<AnyCborItem, AnyCborItem?>) {

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: AnyCborItem, value: AnyCborItem?, optional: Boolean = false) = apply {
        if (!optional || value != null) {
            if (!optional && value == null) {
                throw IllegalArgumentException("Value for ${key} cannot be null")
            }
            // TODO: How do we want to handle an empty array or list. Probably needs another arg as there might be valid use cases to encode these
            map.value[key] = value
        }
    }

    /**
     * Ends building the array.
     *
     * @return the containing builder.
     */
    fun end(): T = parent

    /**
     * Puts a new array in the map.
     *
     * This returns a new [ArrayBuilder], when done adding items to the array,
     * [ArrayBuilder.end] should be called to get the current builder back.
     *
     * @param key the key.
     * @return a [ArrayBuilder].
     */
    fun putArray(key: AnyCborItem): ArrayBuilder<MapBuilder<T>> {
        val array = CborArray(mutableListOf())
        put(key, array)
        return ArrayBuilder(this, array)
    }

    fun putCborMap(key: AnyCborItem, value: CborMap<AnyCborItem, AnyCborItem>) = apply {
        put(key, value)
    }

    fun putCborArray(key: AnyCborItem, value: CborArray<AnyCborItem>) = apply {
        put(key, value)
    }

    /**
     * Puts a new map in the map.
     *
     * This returns a new [MapBuilder], when done adding items to the map
     * [MapBuilder.end] should be called to get the current builder back.
     *
     * @param key the key.
     * @return a [MapBuilder].
     */
    fun putMap(key: AnyCborItem): MapBuilder<MapBuilder<T>> {
        val map = CborMap(mutableMapOf())
        put(key, map)
        return MapBuilder(this, map)
    }

    /**
     * Puts a tagged data item in the map.
     *
     * @param key the key.
     * @param tagNumber the number of the tag to use.
     * @param taggedItem the item to add.
     * @return the builder.
     */
    fun putTagged(key: AnyCborItem, tagNumber: Int, taggedItem: AnyCborItem) = apply {
        map.value[key] = CborTagged(tagNumber, taggedItem)
    }

    /**
     * Puts a tagged bstr with encoded CBOR in the map.
     *
     * @param key the key.
     * @param encodedCbor the bytes of the encoded CBOR.
     */
    fun <T:AnyCborItem> putTaggedEncodedCbor(key: AnyCborItem, encodedCbor: ByteArray) = apply {
        putTagged(key, CborTagged.Static.ENCODED_CBOR, CborByteString(encodedCbor))
    }


    // Convenience putters for String keys

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: AnyCborItem?) = apply {
        map.value[key.toCborString()] = value
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: String) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.tstr.newCborItem(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: ByteArray) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.bstr.newCborItem(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: Byte) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.int.newInt(value.toInt()))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: Short) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.int.newInt(value.toInt()))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: Int) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.int.newCborItem(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: Long) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.int.newLong(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: Boolean) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.bool.newCborItem(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: Double) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.float64.newFloat64(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: String, value: Float) = apply {
        put(CDDL.tstr.newCborItem(key), CDDL.float.newCborItem(value))
    }

    /**
     * Puts a new array in the map.
     *
     * This returns a new [ArrayBuilder], when done adding items to the array,
     * [ArrayBuilder.end] should be called to get the current builder back.
     *
     * @param key the key.
     * @return a [ArrayBuilder].
     */
    fun putArray(key: String): ArrayBuilder<MapBuilder<T>> = putArray(key.toCborString())

    /**
     * Puts a new map in the map.
     *
     * This returns a new [MapBuilder], when done adding items to the map
     * [MapBuilder.end] should be called to get the current builder back.
     *
     * @param key the key.
     * @return a [MapBuilder].
     */
    fun putMap(key: String): MapBuilder<MapBuilder<T>> {
        return putMap(key.toCborString())
    }

    /**
     * Puts a tagged data item in the map.
     *
     * @param key the key.
     * @param tagNumber the number of the tag to use.
     * @param taggedItem the item to add.
     * @return the builder.
     */
    fun putTagged(key: String, tagNumber: Int, value: CborItem<Any>) = apply {
        putTagged(key.toCborString(), tagNumber, value)
    }

    /**
     * Puts a tagged bstr with encoded CBOR in the map.
     *
     * @param key the key.
     * @param encodedCbor the bytes of the encoded CBOR.
     */
    fun <T:AnyCborItem> putTaggedEncodedCbor(key: String, encodedCbor: ByteArray) = apply {
        putTaggedEncodedCbor<T>(CDDL.tstr.newCborItem(key), encodedCbor)
    }

    // Convenience putters for Long keys

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: AnyCborItem?) = apply {
        map.value[CDDL.int.newLong(key)] = value
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: String) = apply {
        put(CDDL.int.newLong(key), value.toCborString())
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: ByteArray) = apply {
        put(CDDL.int.newLong(key), CDDL.bstr.newCborItem(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: Byte) = apply {
        put(CDDL.int.newLong(key), CDDL.int.newInt(value.toInt()))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: Short) = apply {
        put(CDDL.int.newLong(key), CDDL.int.newInt(value.toInt()))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: Int) = apply {
        put(CDDL.int.newLong(key), CDDL.int.newInt(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: Long) = apply {
        put(CDDL.int.newLong(key), CDDL.int.newLong(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: Boolean) = apply {
        put(CDDL.int.newLong(key), CDDL.bool.newCborItem(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: Double) = apply {
        put(CDDL.int.newLong(key), CDDL.float64.newFloat64(value))
    }

    /**
     * Puts a new value in the map
     *
     * @param key the key.
     * @param value the value.
     * @return the builder.
     */
    fun put(key: Long, value: Float) = apply {
        put(CDDL.int.newLong(key), CDDL.float.newCborItem(value))
    }

    /**
     * Puts a new array in the map.
     *
     * This returns a new [ArrayBuilder], when done adding items to the array,
     * [ArrayBuilder.end] should be called to get the current builder back.
     *
     * @param key the key.
     * @return a [ArrayBuilder].
     */
    fun putArray(key: Long): ArrayBuilder<MapBuilder<T>> = putArray(CDDL.int.newLong(key))

    /**
     * Puts a new map in the map.
     *
     * This returns a new [MapBuilder], when done adding items to the map
     * [MapBuilder.end] should be called to get the current builder back.
     *
     * @param key the key.
     * @return a [MapBuilder].
     */
    fun putMap(key: Long): MapBuilder<MapBuilder<T>> = putMap(CDDL.int.newLong(key))

    /**
     * Puts a tagged data item in the map.
     *
     * @param key the key.
     * @param tagNumber the number of the tag to use.
     * @param taggedItem the item to add.
     * @return the builder.
     */
    fun putTagged(key: Long, tagNumber: Int, value: CborItem<Any>): MapBuilder<T> =
        putTagged(CDDL.int.newLong(key), tagNumber, value)


    /**
     * Puts a tagged bstr with encoded CBOR in the map.
     *
     * @param key the key.
     * @param encodedCbor the bytes of the encoded CBOR.
     */
    fun <T:AnyCborItem> putTaggedEncodedCbor(key: Long, encodedCbor: ByteArray) = apply {
        putTaggedEncodedCbor<T>(CDDL.int.newLong(key), encodedCbor)
    }
}
