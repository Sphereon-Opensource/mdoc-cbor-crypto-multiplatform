

package com.sphereon.mdoc.data

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBool
import com.sphereon.cbor.CborEncodedItem
import com.sphereon.cbor.CborInt
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.cddl_bool
import com.sphereon.cbor.cddl_bstr
import com.sphereon.cbor.cddl_int
import com.sphereon.cbor.cddl_tstr
import com.sphereon.cbor.toCborBool
import com.sphereon.mdoc.data.device.IssuerSignedItemCbor
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import kotlinx.serialization.json.JsonElement


import kotlin.js.JsExport


/**
 * 8.3.1 Data model
 * The CDDL definitions for DocType, NameSpace, DataElementIdentifier and DataElementValue are common
 * across different data retrieval methods and the applicable security mechanisms. The following CDDL
 * definitions shall be applied to the CDDL structures defined in Clause 8 and Clause 9:
 * DocType = tstr
 * NameSpace = tstr
 * DataElementIdentifier = tstr ; Data element identifier
 * DataElementValue = any ; Data element value
 */
typealias DocType = CborString
typealias NameSpace = CborString
typealias DataElementIdentifier = CborString
typealias DataElementValue<Type> = CborItem<Type>


typealias Identifier = CborString
typealias IntentToRetain = CborBool


/**
 * 8.3.2.1.2.1 Device retrieval mdoc request
 *
 * #6.24(bstr .cbor ItemsRequests
 */
typealias ItemsRequestBytes = cddl_bstr


//fixme: This needs to be an object not a map
/**
 * requestInfo may be used by the mdoc reader to provide additional information. This document does
 * not define any key-value pairs for use in requestInfo. An mdoc shall ignore any key-value pairs that it
 * is not able to interpret.
 */
typealias RequestInfo = CborMap<CborString, CborString>
//fixme: This needs to be an object not a map
/**
 * NameSpaces contains the requested data elements and the namespace they belong to.
 */
//typealias NameSpacesOrig = List<NameSpaceDataElements>


/*
@JsExport
data class NameSpaceDataElements(val nameSpace: CborString, val dataElements: DataElements) {

    */
/**
 * Although Kotlin typically does not need builders given the named params, we want to make it a bit easier
 * cross-platform and not to have developers take into account what the serialization structure is. We cannot use overloads because of JS
 *//*

    class Builder(
        val nameSpacesBuilder: DeviceItemsRequestCbor.NameSpacesBuilder,
        var nameSpace: NameSpace? = MDL_NAMESPACE_CBOR,
        val dataElements: Array<DataElementCbor> = arrayOf()
    ) {
        private val elementsBuilder = DataElements.Builder(this.dataElements)
        fun fromDataElements(nameSpace: NameSpace, vararg elements: DataElementCbor) = apply {
            this.nameSpace = nameSpace
            elementsBuilder.addUsingCborElements(*elements)
        }

        fun withNameSpace(nameSpace: cddl_tstr) = apply { this.nameSpace = nameSpace.toCborString() }

        fun add(identifier: cddl_tstr, intentToRetain: cddl_bool = false) = apply {
            addUsingElements(DataElementCbor(identifier.toCborString(), intentToRetain.toCborBool()))
        }

        fun addUsingElements(vararg elements: DataElementCbor) = apply {
            elementsBuilder.addUsingCborElements(*elements)
        }

        fun dockRequestBuild(): DocRequest {
            return nameSpacesBuilder.deviceItemsRequestBuilder?.docRequestBuilder?.build()
                ?: throw IllegalStateException("DocRequestBuilder or deviceItemsRequestBuilder cannot be null")
        }

        fun deviceItemsRequestBuild(): DeviceItemsRequestCbor {
            return nameSpacesBuilder.deviceItemsRequestBuilder?.build()
                ?: throw IllegalStateException("DocRequestBuilder cannot be null")
        }

        fun nameSpacesBuild(): NameSpaces {
            return nameSpacesBuilder.build()
        }

        fun build() = NameSpaceDataElements(nameSpace ?: MDL_NAMESPACE_CBOR, elementsBuilder.build())
    }
}
*/

/*

*/
/**
 * DataElements contains the requested data elements identified by their data element identifier. For
 * each requested data element, the IntentToRetain variable indicates whether the mdoc verifier
 * intends to retain the received data element. The mdoc verifier shall not retain any data, including
 * digests and signatures, or derived data received from the mdoc, except for data elements for which the
 * IntentToRetain flag was set to true in the request. To retain is defined as “to store for a period longer
 * than necessary to conduct the transaction in realtime”.
 *//*

@JsExport
class DataElements(private val backing: MutableList<DataElementCbor> = mutableListOf()) :
    MutableList<DataElementCbor> by backing {
    @JsName("fromVarArgs")
    constructor(vararg elements: DataElementCbor) : this(elements.toMutableList())

    */
/**
     * Although Kotlin typically does not need builders given the named params, we want to make it a bit easier
     * cross-platform and not to have developers take into account what the serialization structure is
     *//*

    class Builder(val elements: Array<DataElementCbor> = arrayOf()) {
        fun from(vararg elements: DataElementCbor) = apply { this.elements.plus(elements) }
        fun add(identifier: cddl_tstr, intentToRetain: Boolean = false) =
            apply { addUsingCborElements(DataElementCbor(identifier.toCborString(), intentToRetain.toCborBool())) }

        fun addUsingCborElements(vararg elements: DataElementCbor) =
            apply { this.elements.plus(elements) }

        fun addUsingSimpleElements(vararg elements: DataElementSimple) =
            apply {
                addUsingCborElements(*elements.map {
                    DataElementCbor(
                        it.identifier.toCborString(),
                        it.intentToRetain.toCborBool()
                    )
                }.toTypedArray())
            }

        fun build(): CborMap<CborString, CborBool> {
            return CborMap(mutableMapOf(*elements.map { Pair(it.identifier, it.intentToRetain ?: CborTrue()) }
                .toTypedArray()))
        }
    }

}
*/


@JsExport
data class DataElementCbor(
    val identifier: DataElementIdentifier, val intentToRetain: IntentToRetain = true.toCborBool()
) {
    fun toPair(): Pair<DataElementIdentifier, IntentToRetain> {
        return Pair(identifier, intentToRetain)
    }
}

@JsExport
data class DataElementSimple(val identifier: cddl_tstr, val intentToRetain: cddl_bool = true)

typealias DeviceResponseDocumentErrorJson = Map<String, Int>
typealias DeviceResponseDocumentErrorCbor = Map<DocType, CborInt>

typealias DeviceSignedItemsCbor = CborMap<DataElementIdentifier, DataElementValue<Any>>
typealias DeviceSignedItemsJson = Map<String, JsonElement>

typealias DeviceNameSpacesCbor = CborMap<NameSpace, DeviceSignedItemsCbor>
typealias DeviceNameSpacesJson = MutableMap<String, DeviceSignedItemsJson>



typealias ErrorCode = cddl_int

typealias DocumentErrorsCbor = Map<NameSpace, DeviceResponseErrorItemsCbor>
typealias DocumentErrorsJson = Map<String, DeviceResponseErrorItemsJson>

typealias DeviceResponseErrorItemsCbor = Map<DataElementIdentifier, ErrorCode>
typealias DeviceResponseErrorItemsJson = Map<String, Int>

typealias JWTDocumentErrors = Map<NameSpace, ErrorCodes>


// fixme: Map to object
typealias ErrorCodes = Map<DataElementIdentifier, ErrorCode>


typealias NameSpacesResponse = Map<NameSpace, DataElementsValue>

typealias DataElementsValue = Map<DataElementIdentifier, DataElementValue<Any>>

typealias JWT = cddl_tstr
