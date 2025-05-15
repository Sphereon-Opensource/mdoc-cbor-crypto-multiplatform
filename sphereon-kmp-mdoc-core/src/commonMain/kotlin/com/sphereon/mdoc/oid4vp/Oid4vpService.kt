package com.sphereon.mdoc.oid4vp

import com.sphereon.cbor.CborInt
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.toCborString
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.SigningException
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.kmp.DefaultLogger
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.Uuid
import com.sphereon.mdoc.MdocSignService
import com.sphereon.mdoc.MdocSignService.Static.getSuppliedOrMSODerivedCborKeyInfo
import com.sphereon.mdoc.data.DeviceNameSpacesCbor
import com.sphereon.mdoc.data.DeviceResponseDocumentErrorCbor
import com.sphereon.mdoc.data.DocType
import com.sphereon.mdoc.data.device.DeviceAuthenticationCbor
import com.sphereon.mdoc.data.device.DeviceResponseCbor
import com.sphereon.mdoc.data.device.DocumentCbor
import com.sphereon.mdoc.transfer.device.SessionTranscriptCbor
import kotlin.js.JsExport


class MdocOid4vpService(val signService: MdocSignService = MdocSignService()) {

    suspend fun createDeviceResponse(
        matchingDocuments: Array<DocumentDescriptorMatchResult>,
        presentationDefinition: IOid4VPPresentationDefinition,
        clientId: String,
        responseUri: String,
        authorizationRequestNonce: String,
    ): DeviceResponseCbor {
        var documentErrors = arrayOf<DeviceResponseDocumentErrorCbor>()
        var documents = arrayOf<DocumentCbor>()
        matchingDocuments.forEach { doc ->
            val docType = doc.inputDescriptor.id.toCborString() // 18013-7 matches doc types to the id of the input descriptor
            var error = doc.documentError
            if (doc.document !== null && error === null) {
                val signed = signDocument(
                    presentationDefinition = presentationDefinition,
                    clientId = clientId,
                    responseUri = responseUri,
                    authorizationRequestNonce = authorizationRequestNonce,
                    mdocNonce = doc.mdocNonce,
                    document = doc.document,
                    inputDescriptor = doc.inputDescriptor,

                    deviceKeyInfo = doc.deviceKeyInfo,
                    deviceNamespaces = doc.deviceNamespaces ?: CborMap()
                )
                error = signed.documentError
                val mdoc = signed.document
                if (mdoc === null && error === null) {
                    error = mapOf(Pair(docType, CborInt(LongKMP(0))))
                }
                if (error === null && mdoc !== null) {
                    documents = documents.plus(mdoc)
                }
            }
            if (error !== null) {
                documentErrors = documentErrors.plus(error)

            }
        }
        if (documents.isEmpty() && documentErrors.isEmpty()) {
            throw IllegalStateException("No documents and no errors present. We cannot create a device response with both not present")
        }

        return DeviceResponseCbor(documents = if (documents.isEmpty()) null else documents, documentErrors = documentErrors)
    }

    suspend fun signDocument(
        clientId: String,
        responseUri: String,
        mdocNonce: String = Uuid.v4String(),
        authorizationRequestNonce: String,
        deviceNamespaces: DeviceNameSpacesCbor = CborMap(),
        document: DocumentCbor? = null,
        inputDescriptor: IOid4VPInputDescriptor? = null,
        docType: DocType = inputDescriptor?.id?.toCborString()
            ?: throw IllegalArgumentException("Please provide a docType"), // required since we could also call this method without a document to generate an error
        deviceKeyInfo: IKeyInfo<*>? = null,
        presentationDefinition: IOid4VPPresentationDefinition,
    ): Oid4vpSignResult {
        val request = Oid4VPPresentationDefinition.Static.fromDTO(presentationDefinition).toDocRequest()
        val deviceAuthentication =
            DeviceAuthenticationCbor.Static.fromOid4vp(clientId, responseUri, mdocNonce, authorizationRequestNonce, docType.value, deviceNamespaces)
        val sessionTranscript = deviceAuthentication.sessionTranscript
        var resolvedKeyInfo = getSuppliedOrMSODerivedCborKeyInfo(keyInfo = deviceKeyInfo, mso = document?.MSO)
        val inputDescriptorAlg = inputDescriptor?.format?.mso_mdoc?.alg
        if (resolvedKeyInfo.signatureAlgorithm == null) {
            // if we do not have the signature algorithm on the key info, let's have a look
            if (inputDescriptorAlg.isNullOrEmpty()) {
                throw SigningException("Device key info, MSO and input descriptor mso_mdoc alg did not indicate their supported algorithms. Cannot sign mdoc")
            }
            val supportedSigAlgs = inputDescriptorAlg.flatMap { descAlg ->
                SignatureAlgorithm.Static.asList.map { enumValue ->
                    if (enumValue.cose?.id == descAlg) {
                        return@map enumValue
                    } else return@map null
                }.filterNotNull()
                    .filter { enumValue -> (enumValue.cose?.keyType == resolvedKeyInfo.keyType) || (enumValue.cose?.keyType?.toCbor() == resolvedKeyInfo.key.kty) }
            }
            if (supportedSigAlgs.isEmpty()) {
                throw SigningException("Device key info, MSO and input descriptor mso_mdoc could not provide their their supported algorithms properly. Cannot sign mdoc")
            }
            resolvedKeyInfo = ResolvedKeyInfo.Static.fromDTO(resolvedKeyInfo).copy(signatureAlgorithm = supportedSigAlgs[0])
        }


        val mdoc = document?.let {
            signService.deviceSignDocument(
                request = request, document = it, deviceAuthentication = deviceAuthentication, deviceKeyInfo = resolvedKeyInfo
            )
        }
        val documentError: DeviceResponseDocumentErrorCbor? = if (document != null) null else mapOf(Pair(docType, CborInt(LongKMP(0))))
        return Oid4vpSignResult(
            sessionTranscript = sessionTranscript,
            document = mdoc,
            presentationDefinition = presentationDefinition,
            documentError = documentError,
            deviceKeyInfo = resolvedKeyInfo
        )
    }

    fun filterApplicableDocumentsPerInputDescriptor(
        allDocument: Array<DocumentCbor> = arrayOf(), inputDescriptor: IOid4VPInputDescriptor
    ): Array<DocumentCbor> {
        /**
         * From 18013-7:
         * The value for id shall be set to the requested document type. This indicates that all requested data elements shall be selected from that document type.
         *
         * The Input Descriptor id shall be unique per Presentation Definition object. This implies that a document type can only be used once within the Presentation Definition.
         */
        return allDocument.filter { inputDescriptor.format.mso_mdoc !== null }.filter { it.docType.value == inputDescriptor.id }.toTypedArray()
    }

    fun matchDocumentsAndDescriptors(
        mdocNonce: String = Uuid.v4String(), // mdoc nonce will be set on all document results
        applicableDocuments: Array<DocumentCbor>, presentationDefinition: IOid4VPPresentationDefinition
    ): Array<DocumentDescriptorMatchResult> {
        return presentationDefinition.input_descriptors.map { inputDescriptor ->
            val matchingDocuments = filterApplicableDocumentsPerInputDescriptor(applicableDocuments, inputDescriptor)
            if (matchingDocuments.isEmpty()) {
                DefaultLogger.warn("No documents found satisfying descriptor id/document type ${inputDescriptor.id}")
                DocumentDescriptorMatchResult(
                    inputDescriptor = inputDescriptor,
                    document = null,
                    mdocNonce = mdocNonce,
                    documentError = mapOf(Pair(DocType(inputDescriptor.id), CborInt(LongKMP(0)))),
                    deviceKeyInfo = null
                )
            } else if (matchingDocuments.size > 1) {
                DefaultLogger.warn("Multiple documents found satisfying descriptor id/document type ${inputDescriptor.id}, which is not allowed")
                DocumentDescriptorMatchResult(
                    inputDescriptor = inputDescriptor,
                    document = null,
                    mdocNonce = mdocNonce,
                    documentError = mapOf(Pair(DocType(inputDescriptor.id), CborInt(LongKMP(0)))),
                    deviceKeyInfo = null
                )
            } else {
                val document = matchingDocuments[0]
                val deviceKeyInfo = getSuppliedOrMSODerivedCborKeyInfo(mso = document.MSO)
                DocumentDescriptorMatchResult(
                    inputDescriptor = inputDescriptor, document = document, mdocNonce = mdocNonce, documentError = null, deviceKeyInfo = deviceKeyInfo
                )

            }
        }.toTypedArray()
    }


}


@JsExport
data class Oid4vpSignResult(
    val sessionTranscript: SessionTranscriptCbor,
    val document: DocumentCbor? = null,
    val documentError: DeviceResponseDocumentErrorCbor? = null,
    val presentationDefinition: IOid4VPPresentationDefinition,
    val deviceKeyInfo: IKeyInfo<ICoseKeyCbor>?
)


@JsExport
data class DocumentDescriptorMatchResult(
    val inputDescriptor: IOid4VPInputDescriptor,
    val document: DocumentCbor?, // Either a document or documentError should be returned, not both
    val documentError: DeviceResponseDocumentErrorCbor?, // Either a document or documentError should be returned, not both
    val deviceKeyInfo: IKeyInfo<ICoseKeyCbor>?, // Derived from the document. Cannot be derived if no document is passed in
    var deviceNamespaces: DeviceNameSpacesCbor? = null,
    val mdocNonce: String // No default/uuid as the nonce's needs to match in case multiple mdocs are returned
)
