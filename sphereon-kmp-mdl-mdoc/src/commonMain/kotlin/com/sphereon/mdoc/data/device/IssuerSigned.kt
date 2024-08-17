package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CDDLType
import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBaseItem
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborEncodedItem
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.JsonView2
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.cbor.toCborUInt
import com.sphereon.crypto.cose.COSE_Sign1
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1Json
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.encodeToBase64Url
import com.sphereon.mdoc.data.DataElementIdentifier
import com.sphereon.mdoc.data.DataElementValue
import com.sphereon.mdoc.data.NameSpace
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport

@JsExport
data class IssuerSignedJson(
    val nameSpaces: IssuerSignedNamesSpacesJson?,
    val issuerAuth: CoseSign1Json<MobileSecurityObjectJson, MobileSecurityObjectCbor>
) : JsonView<IssuerSignedCbor>() {
    override fun toCbor(): IssuerSignedCbor {
        return IssuerSignedCbor(
            issuerAuth = issuerAuth.toCbor(),
            nameSpaces = if (nameSpaces == null) null else CborMap(
                mutableMapOf(* nameSpaces.map {
                    Pair(
                        it.key.toCborString(),
                        CborArray(it.value.map { signedItemJson -> CborEncodedItem(decodedValue = signedItemJson.toCbor()) }
                            .toMutableList())
                    )
                }.toTypedArray())
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuerSignedJson) return false

        if (nameSpaces != other.nameSpaces) return false
        if (issuerAuth != other.issuerAuth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameSpaces?.hashCode() ?: 0
        result = 31 * result + issuerAuth.hashCode()
        return result
    }

    override fun toString(): String {
        return "IssuerSignedJson(nameSpaces=$nameSpaces, issuerAuth=$issuerAuth)"
    }


}

@JsExport
data class IssuerSignedCbor(
    val nameSpaces: IssuerSignedNamesSpacesCbor? = null,
    val issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>
) : CborView<IssuerSignedCbor, IssuerSignedJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    class Builder(val nameSpaces: MutableMap<NameSpace, List<IssuerSignedItemCbor<Any>>> = mutableMapOf()) {

        fun addNameSpace(nameSpace: NameSpace, vararg issuerSignedItems: IssuerSignedItemCbor<Any>) = apply {
            val values = nameSpaces.getOrElse(nameSpace) { mutableListOf() }
            nameSpaces[nameSpace] = values.plus(issuerSignedItems)
        }

        /*

                fun build(): IssuerSigned {
                    val nameSpaces =  nameSpaces.map { it.key to IssuerSignedItems(it.value.map { item -> item.toDataItem() } )}.toMap()
                    return IssuerSigned(nameSpaces, issuerAuth = COSE_Sign1())
                }
        */


    }

    override fun cborBuilder(): CborBuilder<IssuerSignedCbor> {
        val builder = CborMap.builder(this)
            .put(NAME_SPACES, nameSpaces, true)
            .put(ISSUER_AUTH, issuerAuth.toCbor(), false)
        return builder.end()
    }

    override fun toJson(): IssuerSignedJson {
        return IssuerSignedJson(
            issuerAuth = issuerAuth.toJson(),
            nameSpaces = if (nameSpaces == null) null else mutableMapOf(* nameSpaces.value.map {
                Pair(
                    it.key.value,
                    it.value.value.map { elts -> elts.decodedValue.toJson() }
                        .toTypedArray()
                )
            }.toTypedArray())
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuerSignedCbor) return false

        if (nameSpaces != other.nameSpaces) return false
        if (issuerAuth != other.issuerAuth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameSpaces?.hashCode() ?: 0
        result = 31 * result + issuerAuth.hashCode()
        return result
    }

    override fun toString(): String {
        return "IssuerSignedCbor(nameSpaces=$nameSpaces, issuerAuth=$issuerAuth)"
    }

    companion object {
        val NAME_SPACES = StringLabel("nameSpaces")
        val ISSUER_AUTH = StringLabel("issuerAuth")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): IssuerSignedCbor {
            val nameSpacesMap =
                NAME_SPACES.optional<CborMap<NameSpace, CborArray<CborEncodedItem<CborMap<StringLabel, AnyCborItem>>>>>(
                    m
                )
            val nameSpaces = if (nameSpacesMap == null) null else CborMap(mutableMapOf(* nameSpacesMap.value.map {
                Pair(
                    it.key,
                    CborArray(it.value.value.map { encoded -> CborEncodedItem(IssuerSignedItemCbor.static.fromCborItem(encoded.decodedValue)) }
                        .toMutableList())
                )
            }.toTypedArray()))
            return IssuerSignedCbor(
                nameSpaces = nameSpaces,
                issuerAuth = ISSUER_AUTH.required<CborArray<AnyCborItem>>(m).let { CoseSign1Cbor.fromCborItem(it) }
            )
        }

        fun cborDecode(encoded: ByteArray): IssuerSignedCbor = fromCborItem(Cbor.decode(encoded))
    }
}


interface JsonElementWithCDDL {
    val cddl: CDDL
}

@JsExport
@Serializable
data class IssuerSignedItemJson(
    val digestID: LongKMP,
    val random: String, //todo: also add hex validation
    val elementIdentifier: String,
    val elementValue: JsonElement,
    val elementCDDL: CDDLType // We need this as otherwise we would lose type info. Strings can be (full)dates, tstr and text etc.
) : JsonView2() {
    override fun toCbor(): IssuerSignedItemCbor<Any> = IssuerSignedItemCbor(
        digestID = CborUInt(this.digestID),
        random = random.toCborByteString(),
        elementIdentifier = elementIdentifier.toCborString(),
        elementValue = (elementCDDL as CDDL).newCborItem(elementValue) as DataElementValue<Any>
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuerSignedItemJson) return false

        if (digestID != other.digestID) return false
        if (random != other.random) return false
        if (elementIdentifier != other.elementIdentifier) return false
        if (elementValue != other.elementValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = digestID.hashCode()
        result = 31 * result + random.hashCode()
        result = 31 * result + elementIdentifier.hashCode()
        result = 31 * result + elementValue.hashCode()
        return result
    }

    override fun toString(): String {
        return "IssuerSignedItemJson(digestID=$digestID, random='$random', elementIdentifier='$elementIdentifier', elementValue=$elementValue)"
    }


}


@JsExport
data class IssuerSignedItemCbor<Type : Any>(
    val digestID: CborUInt,
    val random: CborByteString,
    val elementIdentifier: DataElementIdentifier,
    val elementValue: DataElementValue<Type>
) : CborView<IssuerSignedItemCbor<Type>, IssuerSignedItemJson, Map<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<IssuerSignedItemCbor<Type>> {
        return CborMap.builder(this).put(static.DIGEST_ID, digestID).put(static.RANDOM, random)
            .put(static.ELEMENT_IDENTIFIER, elementIdentifier).put(
                static.ELEMENT_VALUE, elementValue
            ).end()
    }

    /*
        @ExperimentalSerializationApi
        internal fun mapCborValueToJson(cborItem: CborBaseItem): JsonElement {
            var elementVal: JsonElement
            var elementCDDL = cborItem.cddl
            if (elementCDDL === CDDL.list) {
                val listVal = cborItem as (CborArray<*>)
                elementVal = JsonArray(listVal.value.map { mapCborValueToJson(it) })
            } else if (elementCDDL === CDDL.map) {
                val mapVal = cborItem as (CborMap<*, *>)
                elementVal = JsonObject(mapVal.value.entries.map { Pair(it.key.value as String, mapCborValueToJson(it.value as CborBaseItem)) }.toMap())
    //            elementVal = JsonObject(mapVal.value.map { mapCborValueToJson(it) })
    //            TODO("Implement map to json")
            } else if (cborItem.cddl === CDDL.nil || cborItem.cddl === CDDL.Null || cborItem.cddl === CDDL.undefined) {
                elementVal = JsonPrimitive(null)
            } else if (cborItem is CborItem<*>) {
                val value = cborItem.toJson<Number>()
                try {

                    if (value === null) {
                        elementVal = JsonNull
                    } else {
                        elementVal = JsonPrimitive(value)
                    }

                } catch (e: Exception) {
                    println("Element value was ${elementCDDL}: ${cborItem.value}")
                    println("Element toJson ${cborItem.toJson<Any>()}")
                    throw e
                }
            } else {
                throw IllegalArgumentException("Unknown type encountered ${cborItem.cddl.format}")
            }
            return elementVal
        }*/
    @ExperimentalSerializationApi
    internal fun mapCborValueToJson(cborItem: CborBaseItem): JsonElement {
        if (cborItem is CborItem<*>) {
            return cborItem.toJson()
        } else {
            throw IllegalArgumentException("Unknown type encountered ${cborItem.cddl.format}")
        }
    }

    override fun toJson(): IssuerSignedItemJson {


        return IssuerSignedItemJson(
            digestID = digestID.value,
            random = random.value.encodeToBase64Url(),
            elementIdentifier = elementIdentifier.value,
            elementValue = mapCborValueToJson(this.elementValue),
            // We need this as otherwise we would lose type info. Strings can be (full)dates, tstr and text etc.
            elementCDDL = this.elementValue.cddl

        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuerSignedItemCbor<*>) return false

        if (digestID != other.digestID) return false
        if (random != other.random) return false
        if (elementIdentifier != other.elementIdentifier) return false
        if (elementValue != other.elementValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = digestID.hashCode()
        result = 31 * result + random.hashCode()
        result = 31 * result + elementIdentifier.hashCode()
        result = 31 * result + elementValue.hashCode()
        return result
    }

    override fun toString(): String {
        return "IssuerSignedItemCbor(digestID=$digestID, random=$random, elementIdentifier=$elementIdentifier, elementValue=$elementValue)"
    }

    object static {
        val DIGEST_ID = StringLabel("digestID")
        val RANDOM = StringLabel("random")
        val ELEMENT_IDENTIFIER = StringLabel("elementIdentifier")
        val ELEMENT_VALUE = StringLabel("elementValue")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) =
            IssuerSignedItemCbor(
                digestID = DIGEST_ID.required(m),
                random = RANDOM.required(m),
                elementIdentifier = ELEMENT_IDENTIFIER.required(m),
                elementValue = ELEMENT_VALUE.required(m) as DataElementValue<Any>
            )

        fun cborDecode(data: ByteArray): IssuerSignedItemCbor<*> = fromCborItem(cborSerializer.decode(data))
    }


}


typealias IssuerSignedNamesSpacesCbor = CborMap<NameSpace, CborArray<CborEncodedItem<IssuerSignedItemCbor<Any>>>>
typealias IssuerSignedNamesSpacesJson = MutableMap<String, Array<IssuerSignedItemJson>>

