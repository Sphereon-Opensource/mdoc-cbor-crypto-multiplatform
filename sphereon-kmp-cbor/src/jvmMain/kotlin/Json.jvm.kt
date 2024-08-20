package com.sphereon.cbor

import kotlinx.serialization.json.Json


actual fun <T> toJsonDTO(subject: JsonView): T = Json.parseToJsonElement(subject.toJsonString()) as T
