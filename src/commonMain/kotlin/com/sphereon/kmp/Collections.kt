package com.sphereon.kmp

import kotlin.js.JsExport

@JsExport
fun <T>kmpListOf(elements: Array<T>): List<T> = elements.toList()

@JsExport
fun <T>kmpSetOf(elements: Array<T>): Set<T> = elements.toSet()

@JsExport
fun <K, V>kmpMapOf(): Map<K, V> = mapOf()
