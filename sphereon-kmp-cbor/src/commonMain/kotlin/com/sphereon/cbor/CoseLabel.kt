package com.sphereon.cbor

import com.sphereon.cbor.NumberLabeledMap.Companion.decodeNumberLabeledMap
import kotlin.js.JsExport


@JsExport
enum class LabelType {
    String,
    Int
}

@JsExport
sealed class CoseLabeled<Label : CoseLabel<*>, CborItem : AnyCborItem>(
    val label: Label,
    val value: CborItem
)


@JsExport
sealed class CoseLabel<ItemType>(
    value: ItemType,
    cddl: CDDLType,
    val type: LabelType,
) : CborItem<ItemType>(value, cddl) {

    @Suppress("UNCHECKED_CAST")
    fun <T> required(map: CborMap<out CoseLabel<*>, AnyCborItem>): T {
        if (!map.value.containsKey(this)) {
            throw IllegalArgumentException("Key (label: ${this.value}, type: ${this.cddl}) not found in cbor map")
        }
        val result = map.value[this] as T
        return result
    }

    fun requiredAsCborMap(map: CborMap<out CoseLabel<*>, AnyCborItem>): CborMap<AnyCborItem, AnyCborItem> {
        return required(map)
    }
    fun <T: AnyCborItem> requiredAsCborArray(map: CborMap<out CoseLabel<*>, AnyCborItem>): CborArray<T> {
        return required(map)
    }
    fun <T> requiredAsListFromCborArray(map: CborMap<out CoseLabel<*>, AnyCborItem>): cddl_list<T> {
        return requiredAsCborArray<AnyCborItem>(map).value as cddl_list<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> optional(map: CborMap<out CoseLabel<*>, AnyCborItem>): T? {
        CborConst.LOG.debug("Getting required label '${value}' from map...")
        if (!map.value.containsKey(this)) {
            CborConst.LOG.debug("... <${value.toString()} not available in map>")
            return null
        }
        val result = map.value[this] as T
        CborConst.LOG.debug("... ${result}")
        return result
    }
    fun optionalAsCborMap(map: CborMap<out CoseLabel<*>, AnyCborItem>): CborMap<AnyCborItem, AnyCborItem>? {
        return optional(map)
    }
    fun optionalAsCborArray(map: CborMap<out CoseLabel<*>, AnyCborItem>): CborArray<AnyCborItem>? {
        return optional(map)
    }

    companion object {
        fun <ItemType : Any> fromCborItem(cborItem: CborItem<ItemType>): CoseLabel<out Comparable<*>> =
            when (cborItem) {
                is CborUInt -> NumberLabel(cborItem.value.toInt())
                is CborNInt -> NumberLabel(-cborItem.value.toInt())
                is CborString -> StringLabel(cborItem.value)
                else -> throw IllegalStateException("Cannot create a label from cbor item with type ${cborItem.cddl}")
            }
    }


}

@Suppress("UNCHECKED_CAST")
@JsExport
open class NumberLabeledMap(
    protected val labeledItems: CborMap<NumberLabel, AnyCborItem> = CborMap(mutableMapOf())
) {
    protected fun <T>putLabel(label: Int, cborItem: CborItem<T>?) {
        if (cborItem == null) {
            return
        }
        labeledItems.value [NumberLabel(label)] = cborItem
    }

    fun <Type> requiredLabel(label: Int): Type {
        return labeledItems.value[NumberLabel(label)] as Type
    }

    fun <Type> optionalLabel(label: Int): Type? {
        val numberLabel = NumberLabel(label)
        val result = labeledItems.value[numberLabel] ?: return null
        return result as Type
    }

    fun hasLabel(label: Int): Boolean {
        return labeledItems.value.containsKey(NumberLabel(label))
    }

    fun getLabels(): Set<NumberLabel> {
        return labeledItems.value.keys
    }
/*
    fun <Type : Any> getLabeledValue(label: Int): NumberLabelValue<Type> {
        if (!hasLabel(label)) {
            throw IllegalArgumentException("Label with value $label is not valid for this object")
        }
        val value = optionalLabel<Type>(label)
        return NumberLabelValue(label, value)
    }

    fun toLabeledValues(): List<NumberLabelValue<Any>> {
        return value.map { NumberLabelValue(it.key.value, it.value) }
    }*/

    protected open fun connectLabels(): CborMap<NumberLabel, AnyCborItem> {
        if (labeledItems.value.isEmpty()) {
            // We do this instead of an abstract method to ensure a subclass not only overrides this method, but also calls the method from an init block
            throw IllegalArgumentException("Please implement connectLabels() in a sub class")
        }
        return labeledItems
    }


    /*open fun instanceFromLabels(): NumberLabeledMapObject {
        throw IllegalArgumentException("Please implement instanceFromLabels() in a sub class")
    }
*/




    /**
     * We call connectLabels, to ensure a subclass will implement this. The default implementation throws an error
     * The drawback is that we populate the labels map 2 times.
     *   Once when it is called as part of init of this super class. The values will be null at that time.
     *   Second from the subclass. Then the properties will be available and punt in the map
     *
     * Since the overhead is minimal, and we want to ensure developers follow the pattern we are "okay" with it.
     */

    /*init {
        this.connectLabels()
    }*/


    fun encode(): ByteArray {
        return Cbor.encode(connectLabels())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NumberLabeledMap) return false

        if (labeledItems != other.labeledItems) return false

        return true
    }

    override fun hashCode(): Int {
        return labeledItems.hashCode()
    }

    companion object {
        fun decodeNumberLabeledMap(numberMappedObject: ByteArray): NumberLabeledMap {
            val map : CborMap<NumberLabel, AnyCborItem> = Cbor.decode(numberMappedObject)
            return NumberLabeledMap(map)
        }
    }

}

fun ByteArray.toNumberLabeledMap() = decodeNumberLabeledMap(this)


@JsExport
fun Int.toNumberLabel() = NumberLabel(this)

@JsExport
fun Long.longToNumberLabel() = NumberLabel(this.toInt())

@JsExport
fun String.toStringLabel() = StringLabel(this)
/*

@JsExport
fun <Type : Any> CborItem<Type>.toCoseNumberLabel(label: Int) = NumberLabelValue(label, this)

@JsExport
fun <Type : Any> CborItem<Type>.toCoseStringLabel(label: String) = StringLabelValue(label, this)
*/
