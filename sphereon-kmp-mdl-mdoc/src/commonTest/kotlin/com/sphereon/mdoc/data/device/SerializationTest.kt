package com.sphereon.mdoc.data.device

import com.sphereon.cbor.CDDL
import com.sphereon.kmp.toKmpLong
import com.sphereon.mdoc.mdocJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class TestClass (val cddl: CDDL)

class SerializationTest {
    @Test
    fun shouldSerializeCddl() {
        val json = "{\"cddl\":\"uint\"}"
        val example = CDDL.uint
        val test = TestClass(example)
        assertEquals(json, mdocJsonSerializer.encodeToString(test))
        val decode: TestClass = mdocJsonSerializer.decodeFromString(json)
        assertEquals(test, decode)
    }

    @Test
    fun shouldDecodeAndEncodeIssuerSignedItemJson() {
        val json = "{\"digestID\":200,\"random\":\"random\",\"key\":\"identifier\",\"value\":400,\"cddl\":\"uint\"}"
        val issuerSigned =
            IssuerSignedItemJson(digestID = 200L.toKmpLong(), random = "random", value = JsonPrimitive(400L), cddl = CDDL.uint, key = "identifier")
        assertEquals(json, mdocJsonSerializer.encodeToString(issuerSigned))
        assertEquals(issuerSigned, mdocJsonSerializer.decodeFromString(json))
        println(mdocJsonSerializer.encodeToString(issuerSigned))
    }
}
