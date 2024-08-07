package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CDDLType
import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborCollectionItem
import com.sphereon.cbor.CborEncodedItem
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cose.COSE_Sign1
import com.sphereon.cbor.cose.CoseSign1Cbor
import com.sphereon.cbor.cose.CoseSign1Json
import com.sphereon.cbor.cose.StringLabel
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.cbor.toCborUInt
import com.sphereon.kmp.LongKMP
import com.sphereon.mdoc.data.DataElementIdentifier
import com.sphereon.mdoc.data.DataElementValue
import com.sphereon.mdoc.data.NameSpace
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectJson
import kotlin.js.JsExport

@JsExport
data class IssuerSignedJson(
    val nameSpaces: IssuerSignedNamesSpacesJson?,
    val issuerAuth: CoseSign1Json<MobileSecurityObjectJson, MobileSecurityObjectCbor>
) : JsonView<IssuerSignedCbor>() {
    override fun toCbor(): IssuerSignedCbor {
        return IssuerSignedCbor(issuerAuth = issuerAuth.toCbor(),
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
                    CborArray(it.value.value.map { encoded -> CborEncodedItem(IssuerSignedItemCbor.fromCborItem(encoded.decodedValue)) }
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


@JsExport
data class IssuerSignedItemJson<Type : Any>(
    val digestID: LongKMP,
    val random: String, //todo: also add hex validation
    val elementIdentifier: String,
    val elementValue: Type,
    val elementCDDL: CDDLType // We need this as otherwise we would lose type info. Strings can be (full)dates, tstr and text etc.
) : JsonView<IssuerSignedItemCbor<Type>>() {
    override fun toCbor(): IssuerSignedItemCbor<Type> = IssuerSignedItemCbor(
        digestID = digestID.toCborUInt(),
        random = random.toCborByteString(),
        elementIdentifier = elementIdentifier.toCborString(),
        elementValue = (elementCDDL as CDDL).newCborItem(elementValue) as DataElementValue<Type>
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuerSignedItemJson<*>) return false

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


@OptIn(ExperimentalStdlibApi::class)
@JsExport
data class IssuerSignedItemCbor<Type : Any>(
    val digestID: CborUInt,
    val random: CborByteString,
    val elementIdentifier: DataElementIdentifier,
    val elementValue: DataElementValue<Type>
) : CborView<IssuerSignedItemCbor<Type>, IssuerSignedItemJson<Type>, Map<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<IssuerSignedItemCbor<Type>> {
        return CborMap.builder(this).put(DIGEST_ID, digestID).put(RANDOM, random)
            .put(ELEMENT_IDENTIFIER, elementIdentifier).put(
                ELEMENT_VALUE, elementValue
            ).end()
    }

    override fun toJson(): IssuerSignedItemJson<Type> {
        var elementVal = this.elementValue.value
        var elementCDDL = this.elementValue.cddl
        if (elementVal is CborItem<*>) {
            elementCDDL = elementVal.cddl
            elementVal = elementVal.toJson() as Type
        } else if (this.elementValue is CborCollectionItem<*>) {
            elementVal = this.elementValue.toJson() as Type
        }
        return IssuerSignedItemJson(
            digestID = digestID.value,
            random = random.value.toHexString(),
            elementIdentifier = elementIdentifier.value,
            elementValue = elementVal,
            // We need this as otherwise we would lose type info. Strings can be (full)dates, tstr and text etc.
            elementCDDL = elementCDDL

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


    companion object {
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
typealias IssuerSignedNamesSpacesJson = MutableMap<String, Array<IssuerSignedItemJson<Any>>>

