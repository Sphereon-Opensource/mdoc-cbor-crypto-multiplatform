package com.sphereon.json

import kotlinx.serialization.json.Json


actual fun <T> toJsonDTO(subject: HasToJsonString): T = Json.parseToJsonElement(subject.toJsonString()) as T
