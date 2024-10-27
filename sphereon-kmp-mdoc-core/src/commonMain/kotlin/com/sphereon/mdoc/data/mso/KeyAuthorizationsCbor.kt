package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.json.mdocJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@Serializable
data class KeyAuthorizationsJson(
    val nameSpaces: Array<String>? = null,
    val dataElements: Map<String, Map<String, Array<String>>>? = null
) : JsonView() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyAuthorizationsJson) return false

        if (nameSpaces != null) {
            if (other.nameSpaces == null) return false
            if (!nameSpaces.contentEquals(other.nameSpaces)) return false
        } else if (other.nameSpaces != null) return false
        if (dataElements != other.dataElements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameSpaces?.contentHashCode() ?: 0
        result = 31 * result + (dataElements?.hashCode() ?: 0)
        return result
    }
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)

    override fun toCbor(): KeyAuthorizationsCbor {
        TODO("Not yet implemented")
    }
}

@JsExport
data class KeyAuthorizationsCbor(
    val nameSpaces: AuthorizedNameSpaces? = null,
    val dataElements: AuthorizedDataElements? = null
) : CborView<KeyAuthorizationsCbor, KeyAuthorizationsJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<KeyAuthorizationsCbor> =
        CborMap.Static.builder(this).put(Static.NAME_SPACES, nameSpaces, true).put(Static.DATA_ELEMENTS, dataElements, true).end()

    override fun toJson(): KeyAuthorizationsJson {
        TODO("Not yet implemented")
    }

    object Static {
        val NAME_SPACES = StringLabel("nameSpaces")
        val DATA_ELEMENTS = StringLabel("dataElements")

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) =
            KeyAuthorizationsCbor(NAME_SPACES.optional(m), DATA_ELEMENTS.optional(m))

        @JsName("cborDecode")
        fun cborDecode(data: ByteArray): KeyAuthorizationsCbor = fromCborItem(cborSerializer.decode(data))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyAuthorizationsCbor) return false

        if (nameSpaces != other.nameSpaces) return false
        if (dataElements != other.dataElements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameSpaces?.hashCode() ?: 0
        result = 31 * result + (dataElements?.hashCode() ?: 0)
        return result
    }
}
