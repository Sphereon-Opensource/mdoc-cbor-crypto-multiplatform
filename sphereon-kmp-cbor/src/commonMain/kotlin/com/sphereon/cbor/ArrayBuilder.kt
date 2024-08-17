package com.sphereon.cbor

import kotlin.js.JsExport

/**
 * Array builder.
 */
@JsExport
data class ArrayBuilder<T>(private val parent: T, private val array: CborArray<CborItem<*>>) {
    fun addRequired(vararg item: AnyCborItem) = apply {
        if (item.isEmpty()) {
            throw IllegalArgumentException("item can not be empty")
        }
        item.map {
            if (it == null) {
                throw IllegalArgumentException("item can not be null")
            }
            array.value.add(it)
        }
    }


    /**
     * Adds a new data item.
     *
     * @param item the item to add.
     * @return the builder.
     */
    fun add(vararg item: AnyCborItem?) = apply {
        item.map {
            if (it != null) {
                array.value.add(it)
            }
        }
    }

    fun addCborArray(item: CborArray<AnyCborItem>?) = apply {
        item?.value?.map { add(it) }
    }

    fun addCborMap(item: CborMap<AnyCborItem, AnyCborItem>) = apply {
        add(item)
    }

    /**
     * Adds a tagged data item.
     *
     * @param tagNumber the number of the tag to use.
     * @param taggedItem the item to add.
     * @return the builder.
     */
    fun addTagged(tagNumber: Int, taggedItem: AnyCborItem) = apply {
        array.value.add(CborTagged(tagNumber, taggedItem))
    }

    /**
     * Adds a tagged bstr with encoded CBOR.
     *
     * @param encodedCbor the bytes of the encoded CBOR.
     */
    fun <T:AnyCborItem>addTaggedEncodedCbor(encodedCbor: ByteArray) = apply {
        array.value.add(CborTagged(CborTagged.ENCODED_CBOR, CborByteString(encodedCbor)))
    }

    /**
     * Adds a new map.
     *
     * This returns a new [MapBuilder], when done adding items to the map
     * [MapBuilder.end] should be called to get the current builder back.
     *
     * @return a builder for the map.
     */
    fun addMap(): MapBuilder<ArrayBuilder<T>> {
        val map = CborMap(mutableMapOf())
        add(map)
        return MapBuilder(this, map)
    }

    /**
     * Adds a new array.
     *
     * This returns a new [ArrayBuilder], when done adding items to the array,
     * [ArrayBuilder.end] should be called to get the current builder back.
     *
     * @return a builder for the array.
     */
    fun addArray(): ArrayBuilder<ArrayBuilder<T>> {
        val array = CborArray(mutableListOf())
        add(array)
        return ArrayBuilder(this, array)
    }

    /**
     * Ends building the array
     *
     * @return the containing builder.
     */
    fun end(): T = parent

// Convenience adders

    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addByteArray(value: ByteArray) = apply {
        add(CDDL.bstr.newCborItem(value))
    }

    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addString(value: String) = apply {
        add(CDDL.tstr.newCborItem(value))
    }

    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addByte(value: Byte) = apply {
        add(CDDL.int.newLong(value.toLong()))
    }

    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addShort(value: Short) = apply {
        add(CDDL.int.newLong(value.toLong()))
    }

    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addInt(value: Int) = apply {
        add(CDDL.int.newCborItem(value))
    }

    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addLong(value: Long) = apply {
        add(CDDL.int.newCborItem(value))
    }

    /**
     * Adds a boolean.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addBoolean(value: Boolean) = apply {
        add(CDDL.bool.newCborItem(value))
    }

    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addDouble(value: Double) = apply {
        add(CDDL.float64.newCborItem(value))
    }


    /**
     * Adds a new value.
     *
     * @param value the value to add.
     * @return the builder.
     */
    fun addFloat(value: Float) = apply {
        add(CDDL.float.newCborItem(value))
    }
}
