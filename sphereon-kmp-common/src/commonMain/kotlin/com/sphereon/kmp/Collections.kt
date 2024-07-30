package com.sphereon.kmp

import kotlin.js.ExperimentalJsCollectionsApi
import kotlin.js.JsExport

@JsExport
@ExperimentalJsCollectionsApi
fun <T>kmpListOf(elements: Array<T>): List<T> = elements.toList()

@JsExport
@ExperimentalJsCollectionsApi
fun <T>kmpSetOf(elements: Array<T>): Set<T> = elements.toSet()

@JsExport
@ExperimentalJsCollectionsApi
fun <K, V>kmpMapOf(): Map<K, V> = mapOf()
