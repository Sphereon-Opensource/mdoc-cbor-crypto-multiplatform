package com.sphereon.kmp

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.js.ExperimentalJsCollectionsApi
import kotlin.js.JsExport

@JsExport
@ExperimentalJsCollectionsApi
fun <T> kmpListOf(elements: Array<T>): List<T> = elements.toList()

@JsExport
@ExperimentalJsCollectionsApi
fun <T> kmpSetOf(elements: Array<T>): Set<T> = elements.toSet()

@JsExport
@ExperimentalJsCollectionsApi
fun <K, V> kmpMapOf(): MutableMap<K, V> = mutableMapOf()


fun JsonObject.mergeJsonElement(key: String, value: JsonElement): JsonObject {
    val result = this.toMutableMap()
    result.putAll(mapOf(Pair(key, value)))
    return JsonObject(result)
}

/**
 * Extension function for json object, which internally is a map, hence why it is found in this file
 * Adds properties to an existing json object, as the map is immutable. Returns a new object with the additional properties. Keys overwrite exiting keys!
 */
fun JsonObject.mergeJsonObject(newProperties: JsonObject): JsonObject {
    val result = this.toMutableMap()
    result.putAll(newProperties.toMap())
    return JsonObject(result)
}
