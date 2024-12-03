### Documentation for experimental SD-JWT and other credential format using 18013-5/138013-7

#### Abstract

This document provides a high-level overview of extending functionality for device interactions using 18013-5 and 18013-7 specifications, supporting
multiple credential formats. The implementation is not part of the regular ISO spec implementations, as these only support Mdocs.
This is experimental in nature, and likely to be removed/changed in the future. It only serves as a mechanism to transport other credential formats
using the in-person/attended support of 18013-5.
It includes CBOR serialization for Oid4vp input descriptors and credential formats .

#### Key Concepts

- **OID4VP Protocol**: Handles credential formats and device requests with the ability to serialize between JSON and CBOR.
- **Credential Formats**: Supports diverse credential formats like SD-JWT and mdoc.
- **Device Engagement**: Establishes a secure connection via device request and response structures, now supporting Oid4VP

#### Relevant Data Structures and Methods

**Device Engagement Request**
This section defines the structure and sequence of the initial device engagement request, which is used to establish a secured connection.

#### Device Engagement Structure:

```plaintext
DeviceEngagement = {
    0: tstr, ; Version 
    1: Security,
    ? 2: DeviceRetrievalMethods, 
    ? 3: ServerRetrievalMethods, 
    ? 4: ProtocolInfo,             <== We are using a new map in here for OID4VP
    ? 100: CredentialFormat
    * int => any 
}
```

##### Fields Explanation

All fields are as is. The change is in Protocol Info, which has always been reserved for future use (RFU) in the spec anyway

- **ProtocolInfo**: Used when the protocol type is "oid4vp".
- **CredentialFormat**: Specifies the supported credential formats such as `vc+sd-jwt`, `jwt_vc_json`, `ldp_vc`, or `mdoc`.

```plaintext
Oid4vpProtocolInfo = {
    "oid4vp" : Oid4vpInfo, ; Only set when we are using the custom extension for oid4vp. It quickly allows for checking supported credential formats
}

Oid4vpInfo = {
    "format" : CredentialFormat,
}

CredentialFormat = {
    "alg" : [+ Alg],
}

Alg = "vc+sd-jwt" // "mso_mdoc" // "jwt_vc_json"
```

### Device Retrieval Request

For requesting the retrieval of documents, the structure encapsulates various document request formats, as illustrated below.

#### Device Request Structure:

```plaintext
DeviceRequest = {
    "version" : tstr, ; Version of DeviceRequest structure
    "docRequests" : ?[+ DocRequest] ; Requested documents  <=== The change is that docRequests are made optional
    "oid4vpRequest": ?bstr ;                               <=== This is the optional OID4VP Presentation Definition as bytestring
}
```

#### sd-jwt vc Request Structure:

```plaintext
sd-jwt-vcoid4vpRequestStructure = {
    "oid4vpRequest", ; Encoded oid4vp presentation definition
}
```

### Device Retrieval Response

This structure is used to return the documents requested in the `DeviceRequest`.

#### Device Response Structure:

```plaintext
DeviceResponse = {
    "version" : tstr, ; Version of DeviceResponse structure
    ? "documents" : [+Document], ; Returned documents
    ? "documentErrors": [+DocumentError]; Errors for unreturned documents
    "status" : uint ; Status code
}
```

#### Document Structure:

```plaintext
Document = {
    ( mdocResponseStructure // sd-jwt-vcResponseStructure ) ; Document type returned
    ? "errors" : Errors
}
```

#### sd-jwt-vc Response Structure:

```plaintext
sd-jwt-vcResponseStructure = {
    "SDJWTwithKBJWT" : SDJWTwitKBJWT ; SD JWT with KB-JWT
}
```

### Implementation Details in `FunkeNonMdocCredentialSupport.kt`

The Kotlin file [FunkeNonMdocCredentialSupport.kt](src/commonMain/kotlin/com/sphereon/mdoc/experimental/oid4vp/FunkeNonMdocCredentialSupport.kt)
enhances support for different credential formats by defining JSON and CBOR serialization methods
and various supporting classes. Obviously changes to the library are incorporated elsewhere in the code. It is easiest to search for references to
classes/interface in the above file to see where they mostly reside. Here's an overview of some of the classes in the above file:

#### Credential Format Serialization

**CredentialFormatCbor**

```kotlin
@JsExport
data class CredentialFormatCbor(val alg: CborArray<CborString>) :
    CborView<CredentialFormatCbor, CredentialFormatJson, CborArray<CborString>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<CredentialFormatCbor> {
        return CborMap.Static.builder(this).put(Static.ALG, alg, false).end()
    }

    override fun toJson(): CredentialFormatJson {
        return CredentialFormatJson(alg.value.map { it.value }.toTypedArray())
    }

    object Static {
        val ALG = StringLabel("alg")
    }
}
```

#### OID4VP Request Protocol Serialization

**Oid4vpRequestProtocolCbor**

```kotlin
@JsExport
data class Oid4vpRequestProtocolCbor(val format: MutableMap<CborString, CredentialFormatCbor>) :
    CborView<Oid4vpRequestProtocolCbor, Oid4vpRequestProtocolJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    fun getFormatIdentifiers() = format.map { Oid4VPFormatIdentifier.Static.fromValue(it.key.value)!! }.toSet().toTypedArray()

    fun hasFormatIdentifier(identifier: Oid4VPFormatIdentifier) = getFormatIdentifiers().contains(identifier)

    fun getSupportedAlgorithms(identifier: Oid4VPFormatIdentifier): Oid4VPSupportedAlgorithm? {
        val algs = format[CborString(identifier.value)]?.alg?.value?.map { it.value } ?: return null
        if (algs.isEmpty()) {
            return null
        }
        return Oid4VPSupportedAlgorithm(algs.toTypedArray())
    }

    fun getFormatsAndAlgorithms(): Map<Oid4VPFormatIdentifier, Oid4VPSupportedAlgorithm> =
        getFormatIdentifiers().associateWith { getSupportedAlgorithms(it)!! }

    fun toOid4vpCredentialFormat(): Oid4VPFormat {
        val algsPerFormat = getFormatIdentifiers()
        val json = Json.encodeToJsonElement(algsPerFormat).jsonObject
        return Json.decodeFromJsonElement(json)
    }

    fun hasCredentialFormat(format: Oid4VPFormat) {
        // Implement method body
    }

    override fun cborBuilder(): CborBuilder<Oid4vpRequestProtocolCbor> {
        val builder = CborMap.Static.builder(this)
        format.forEach { builder.put(it.key, it.value.toCbor(), false) }
        return builder.end()
    }

    override fun toJson() = Oid4vpRequestProtocolJson(format.map { it.key.value to it.value.toJson() }.toMap().toMutableMap())

    object Static {
        val CREDENTIAL_FORMAT = StringLabel("credentialFormat")

        fun fromProtocolInfo(protocolInfo: ProtocolInfo): Oid4vpRequestProtocolCbor {
            require(protocolInfo.value is CborMap<*, *>) { "Protocol info needs to be a map if it contains Oid4vpRequest" }
            val protocolInfoMap =
                (protocolInfo.value as CborMap<*, *>).asMap[OID4VP_PROTOCOL_INFO_LABEL]
            checkNotNull(protocolInfoMap) { "No ${OID4VP_PROTOCOL_INFO_LABEL.value} key present in the protocol info" }
            return fromCborItem(protocolInfoMap as CborMap<StringLabel, AnyCborItem>)
        }

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): Oid4vpRequestProtocolCbor {
            return Oid4vpRequestProtocolCbor(CREDENTIAL_FORMAT.required(m))
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray): Oid4vpRequestProtocolCbor = fromCborItem(cborSerializer.decode(encoded))
    }
}

class Oid4vpDeviceRequestCbor
```
