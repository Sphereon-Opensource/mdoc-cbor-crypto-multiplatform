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
import com.sphereon.cbor.ICborItemValueJson
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.cbor.toCborUIntFromUint
import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IKey
import com.sphereon.crypto.IManagedKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.ManagedKeyInfo
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.cose.COSE_Sign1
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1Json
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.hash
import com.sphereon.json.JsonView
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.LocalDateTimeKMP
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.encodeToBase64Url
import com.sphereon.mdoc.MdocSignService
import com.sphereon.mdoc.data.DataElementIdentifier
import com.sphereon.mdoc.data.DataElementValue
import com.sphereon.mdoc.data.DocType
import com.sphereon.mdoc.data.NameSpace
import com.sphereon.mdoc.data.mso.DeviceKeyInfoCbor
import com.sphereon.mdoc.data.mso.DigestIDs
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectJson
import com.sphereon.mdoc.data.mso.ValidityInfoCbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.random.Random

@JsExport
@Serializable
data class IssuerSignedJson(
    val nameSpaces: IssuerSignedNamesSpacesJson?,
    val issuerAuth: CoseSign1Json
) : JsonView() {
    @Transient
    private val _mso = lazy {
        issuerAuth.payload?.let { MobileSecurityObjectCbor.Static.cborDecode(it.decodeFrom(Encoding.BASE64URL))?.toJson() }
    }


    // Double as we want it to be lazy, but that doesn't export to JS
    @Transient
    val MSO = _mso.value

    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): IssuerSignedCbor {
        return IssuerSignedCbor(
            issuerAuth = issuerAuth.toCbor() as COSE_Sign1<MobileSecurityObjectCbor>,
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

    fun toDocument(): DocumentJson {
        val mso = MobileSecurityObjectJson.Static.decodeCoseSign1(issuerAuth)
        return DocumentJson(docType = mso?.docType ?: throw IllegalStateException("doctype not set"), issuerSigned = this)
    }

    fun limitDisclosures(docRequest: DocRequestJson): IssuerSignedJson {
        return toCbor().limitDisclosures(docRequest.toCbor()).toJson()
    }

}

@JsExport
data class IssuerSignedCbor(
    val nameSpaces: IssuerSignedNamesSpacesCbor? = null,
    val issuerAuth: COSE_Sign1<MobileSecurityObjectCbor>
) : CborView<IssuerSignedCbor, IssuerSignedJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    private val _mso = lazy { MobileSecurityObjectCbor.Static.decodeCoseSign1(issuerAuth) }

    // Double as we want it to be lazy, but that doesn't export to JS
    val MSO = _mso.value


    fun limitDisclosures(docRequest: DocRequestCbor): IssuerSignedCbor = IssuerSignedCbor(
        nameSpaces = nameSpaces?.let {
            CborMap(
                mutableMapOf(
                    * nameSpaces.value.map { entry ->
                        val requestedIdentifiers = docRequest.getIdentifiers(entry.key.value)
                        val value = CborArray(
                            entry.value.value.filter { item -> requestedIdentifiers.containsKey(item.decodedValue.elementIdentifier.value) }
                                .toMutableList()
                        )
                        Pair(entry.key, value)
                    }.toTypedArray()
                )
            )
        },
        issuerAuth = issuerAuth
    )

    fun toDocument(): DocumentCbor {
        val decoded = MobileSecurityObjectCbor.Static.decodeCoseSign1(issuerAuth)
        return DocumentCbor(docType = decoded?.docType ?: throw IllegalStateException("doctype not set"), issuerSigned = this, deviceSigned = null)
    }

    fun toDocumentJson(): DocumentJson = toDocument().toJson()


    class MsoBuilder(
        var docType: DocType? = null,
        val nameSpaces: IssuerSignedNamesSpacesCbor = IssuerSignedNamesSpacesCbor(),
        var signed: LocalDateTimeKMP = DateTimeUtils.Static.DEFAULT.dateTimeLocal(),
        var validFrom: LocalDateTimeKMP = DateTimeUtils.Static.DEFAULT.dateTimeLocal(),
        var validUntil: LocalDateTimeKMP? = null,
        var expectedUpdate: LocalDateTimeKMP? = null,
        var deviceKeyInfo: IResolvedKeyInfo<CoseKeyCbor>? = null,
        var issuerKeyInfo: IManagedKeyInfo<*>? = null,
    ) {

        fun addNameSpace(nameSpace: NameSpace, vararg issuerSignedItems: IssuerSignedItemCbor<Any>) = apply {
            val values = nameSpaces.value.getOrElse(nameSpace) { CborArray() }
            values.value.addAll(issuerSignedItems.map { CborEncodedItem(it) }.toList())
        }

        fun withDocType(docType: DocType) = apply { this.docType = docType }
        fun withDocTypeString(docType: String) = apply { withDocType(docType.toCborString()) }

        fun withSigned(signed: LocalDateTimeKMP) = apply { this.signed = signed }

        fun withValidFrom(validFrom: LocalDateTimeKMP) = apply { this.validFrom = validFrom }
        fun withValidUntil(validUntil: LocalDateTimeKMP) = apply { this.validUntil = validUntil }
        fun withExpectedUpdate(expectedUpdate: LocalDateTimeKMP?) = apply { this.expectedUpdate = expectedUpdate }

        fun withValidityInfo(
            signed: LocalDateTimeKMP = DateTimeUtils.Static.DEFAULT.dateTimeLocal(),
            validFrom: LocalDateTimeKMP = DateTimeUtils.Static.DEFAULT.dateTimeLocal(),
            validUntil: LocalDateTimeKMP,
            expectedUpdate: LocalDateTimeKMP? = null
        ) = apply {
            this.withSigned(signed)
            this.withValidFrom(validFrom)
            this.withValidUntil(validUntil)
            this.withExpectedUpdate(expectedUpdate)
        }

        fun withDeviceKey(deviceKey: IKey) = apply {
            val pubKey = CoseJoseKeyMappingService.toCoseKey(deviceKey).toPublicKey()
            this.deviceKeyInfo = ResolvedKeyInfo.Static.fromKey(pubKey)
        }

        fun withDeviceKeyInfo(deviceKeyInfo: IResolvedKeyInfo<*>) = apply {
            this.deviceKeyInfo = CoseJoseKeyMappingService.toResolvedCoseKeyInfo(deviceKeyInfo)
        }

        fun withSigningKeyInfo(issuerKeyInfo: IManagedKeyInfo<*>) = apply {
            this.issuerKeyInfo = issuerKeyInfo
        }


        fun build(
            alg: DigestAlg? = issuerKeyInfo?.signatureAlgorithm?.digestAlgorithm ?: DigestAlg.SHA256
        ): Pair<MobileSecurityObjectCbor, IssuerSignedNamesSpacesCbor> {

            require(deviceKeyInfo !== null) { "Please provide a device key or key info object" }
            require(validUntil !== null) { "Please provide a valid until value" }
            require(docType !== null) { "Please provide a doc type" }
            require(nameSpaces.value.isNotEmpty() == true) { "Please provide at least one name space" }

            val validityInfo = ValidityInfoCbor.Static.fromDates(
                signed = signed,
                validFrom = validFrom,
                validUntil = validUntil!!,
                expectedUpdate = expectedUpdate
            )


            val alg = (issuerKeyInfo?.signatureAlgorithm?.digestAlgorithm ?: DigestAlg.SHA256)
            val mso = MobileSecurityObjectCbor(
                docType = docType!!,
                validityInfo = validityInfo,
                digestAlgorithm = alg.httpHeaderId!!.toCborString(),
                deviceKeyInfo = DeviceKeyInfoCbor.Static.fromKeyInfo(deviceKeyInfo!!),
                valueDigests = Static.toValueDigests(nameSpaces = nameSpaces, alg = alg)
            )

            return mso to nameSpaces

        }

        @JsExport.Ignore
        suspend fun buildAndSign(
            mdocSignService: MdocSignService,
            signatureAlgorithm: SignatureAlgorithm? = issuerKeyInfo?.signatureAlgorithm,
            alg: DigestAlg = signatureAlgorithm?.digestAlgorithm ?: DigestAlg.SHA256,
            unprotectedHeader: CoseHeaderCbor? = null,
            protectedHeader: CoseHeaderCbor? = null,
            requireDeviceX5Chain: Boolean = false,
        ): IssuerSignedCbor {
            require(issuerKeyInfo !== null) { "Please provide an issuer key info object to sign" }
            val (mso, issuerSignedNameSpaces) = build(alg = alg)
            return mdocSignService.issuerSignIssuerSigned(
                mso = mso,
                issuerSignedNameSpaces = issuerSignedNameSpaces,
                issuerKeyInfo = issuerKeyInfo!!,
                signatureAlgorithm = signatureAlgorithm,
                unprotectedHeader = unprotectedHeader,
                protectedHeader = protectedHeader,
                requireDeviceX5Chain = requireDeviceX5Chain
            )
        }


        @JsExport.Ignore
        suspend fun buildAndSignMdoc(
            mdocSignService: MdocSignService,
            signatureAlgorithm: SignatureAlgorithm? = issuerKeyInfo?.signatureAlgorithm,
            alg: DigestAlg = signatureAlgorithm?.digestAlgorithm ?: DigestAlg.SHA256,
            unprotectedHeader: CoseHeaderCbor? = null,
            protectedHeader: CoseHeaderCbor? = null,
            requireDeviceX5Chain: Boolean = false,
        ): DocumentCbor {
            require(issuerKeyInfo !== null) { "Please provide an issuer key info object to sign" }
            val (mso, issuerSignedNameSpaces) = build(alg = alg)
            return mdocSignService.issuerSignDocument(
                mso = mso,
                issuerSignedNameSpaces = issuerSignedNameSpaces,
                issuerKeyInfo = issuerKeyInfo!!,
                signatureAlgorithm = signatureAlgorithm,
                unprotectedHeader = unprotectedHeader,
                protectedHeader = protectedHeader,
                requireDeviceX5Chain = requireDeviceX5Chain
            )
        }
    }

    override fun cborBuilder(): CborBuilder<IssuerSignedCbor> {
        val builder = CborMap.Static.builder(this)
            .put(Static.NAME_SPACES, nameSpaces, true)
            .put(Static.ISSUER_AUTH, issuerAuth.toCbor(), false)
        return builder.end()
    }

    fun toJsonDTO() = toJson().toJsonDTO<IssuerSignedJson>()
    override fun toJson(): IssuerSignedJson {
        var count = 0
        return IssuerSignedJson(
            issuerAuth = issuerAuth.toJson(),
            nameSpaces = if (nameSpaces == null) null else mutableMapOf(* nameSpaces.value.map {
                Pair(
                    it.key.value,
                    it.value.value.map { elts ->
                        elts.decodedValue.toJson()

                    }
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

    object Static {
        val NAME_SPACES = StringLabel("nameSpaces")
        val ISSUER_AUTH = StringLabel("issuerAuth")

        fun toValueDigests(
            nameSpaces: IssuerSignedNamesSpacesCbor? = null,
            alg: DigestAlg = DigestAlg.SHA256
        ): CborMap<NameSpace, CborMap<DigestIDs, CborByteString>> {
            val digests = nameSpaces?.value?.map { (ns, encodedItems) ->
                ns to CborMap(mutableMapOf(* encodedItems.value.map { encodedItem ->
                    encodedItem.decodedValue.digestID to encodedItem.decodedValue.digest(
                        alg
                    )
                }.toTypedArray()))
            } ?: emptyList()
            return CborMap(mutableMapOf(*digests.toTypedArray()))
        }

        @JsName("fromCborItem")
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
                issuerAuth = ISSUER_AUTH.required<CborArray<AnyCborItem>>(m).let { CoseSign1Cbor.Static.fromCborItem(it) }
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray): IssuerSignedCbor = fromCborItem(Cbor.decode(encoded))
    }
}

@JsExport
@Serializable
data class IssuerSignedItemJson(
    val digestID: LongKMP,
    val random: String, //todo: also add hex validation
//    val elementIdentifier: String,
    val key: String, // This is the element identifier. Named key here
    val value: JsonElement, // This is the elementValue, named value here
    val cddl: CDDLType // We need this as otherwise we would lose type info. Strings can be (full)dates, tstr and text etc.
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)

    override fun toCbor(): IssuerSignedItemCbor<Any> = IssuerSignedItemCbor(
        digestID = CborUInt(this.digestID),
        random = random.toCborByteString(),
        elementIdentifier = key.toCborString(),
        elementValue = cddl.newCborItemFromJson(value, cddl) as DataElementValue<Any>
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuerSignedItemJson) return false

        if (digestID != other.digestID) return false
        if (random != other.random) return false
        if (key != other.key) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = digestID.hashCode()
        result = 31 * result + random.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "IssuerSignedItemJson(digestID=$digestID, random='$random', key='$key', value=$value, cddl=${cddl.format})"
    }


}


@JsExport
data class IssuerSignedItemCbor<Type : Any>(
    val digestID: CborUInt,
    val random: CborByteString = Random.nextBytes(24).toCborByteString(),
    val elementIdentifier: DataElementIdentifier,
    val elementValue: DataElementValue<Type>
) : CborView<IssuerSignedItemCbor<Type>, IssuerSignedItemJson, Map<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<IssuerSignedItemCbor<Type>> {
        return CborMap.Static.builder(this).put(static.DIGEST_ID, digestID).put(static.RANDOM, random)
            .put(static.ELEMENT_IDENTIFIER, elementIdentifier).put(
                static.ELEMENT_VALUE, elementValue
            ).end()
    }

    @ExperimentalSerializationApi
    internal fun mapCborValueToJson(cborItem: CborBaseItem): ICborItemValueJson {
        if (cborItem is CborItem<*>) {
            return cborItem.toJsonCborItem()
        } else {
            throw IllegalArgumentException("Unknown type encountered ${cborItem.cddl.format}")
        }
    }

    fun digest(alg: DigestAlg = DigestAlg.SHA256): CborByteString {
        return CborByteString(hash(cborEncode(), alg))
    }

    fun toJsonDTO() = toJson().toJsonDTO<IssuerSignedItemJson>()
    override fun toJson(): IssuerSignedItemJson {

        val json: ICborItemValueJson = mapCborValueToJson(this.elementValue)
        return IssuerSignedItemJson(
            digestID = digestID.value,
            random = random.value.encodeToBase64Url(),
//            elementIdentifier = elementIdentifier.value,
            key = elementIdentifier.value, //json.jsonObject.get("key")!!.jsonPrimitive.content,
            value = json.value,
            // We need this as otherwise we would lose type info. Strings can be (full)dates, tstr and text etc.
            cddl = json.cddl

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

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) =
            IssuerSignedItemCbor(
                digestID = DIGEST_ID.required(m),
                random = RANDOM.required(m),
                elementIdentifier = ELEMENT_IDENTIFIER.required(m),
                elementValue = ELEMENT_VALUE.required(m) as DataElementValue<Any>
            )

        @JsName("cborDecode")
        fun cborDecode(data: ByteArray): IssuerSignedItemCbor<*> = fromCborItem(cborSerializer.decode(data))

        fun <Type : Any> create(digestID: UInt, elementIdentifier: String, elementValue: DataElementValue<Type>): IssuerSignedItemCbor<Type> {
            return IssuerSignedItemCbor(
                digestID = digestID.toCborUIntFromUint(),
                random = Random.nextBytes(24).toCborByteString(),
                elementIdentifier = elementIdentifier.toCborString(),
                elementValue = elementValue
            )
        }
    }


}


typealias IssuerSignedNamesSpacesCbor = CborMap<NameSpace, CborArray<CborEncodedItem<IssuerSignedItemCbor<Any>>>>
typealias IssuerSignedNamesSpacesJson = MutableMap<String, Array<IssuerSignedItemJson>>

