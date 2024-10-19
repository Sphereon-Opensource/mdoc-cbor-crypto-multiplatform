package com.sphereon.mdoc.oid4vp

import com.sphereon.cbor.CborMap
import com.sphereon.crypto.IKeyInfo
import com.sphereon.kmp.Uuid
import com.sphereon.mdoc.MdocSignService
import com.sphereon.mdoc.data.DeviceNameSpacesCbor
import com.sphereon.mdoc.data.DocType
import com.sphereon.mdoc.data.device.DeviceResponseCbor
import com.sphereon.mdoc.data.device.DocumentCbor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext
import kotlin.js.Promise

private const val COROUTINE_CONTEXT = "MdocOid4vpServiceJs"

@JsExport
class MdocOid4vpServiceJs(val signService: MdocSignService = MdocSignService()) {

    fun createDeviceResponse(
        matchingDocuments: Array<DocumentDescriptorMatchResult>,
        presentationDefinition: IOid4VPPresentationDefinition,
        clientId: String,
        responseUri: String,
        authorizationRequestNonce: String,
    ): Promise<DeviceResponseCbor> {
        return CoroutineScope(CoroutineName(COROUTINE_CONTEXT)).async {
            MdocOid4vpService().createDeviceResponse(
                matchingDocuments,
                presentationDefinition,
                clientId,
                responseUri,
                authorizationRequestNonce
            )
        }.asPromise()
    }

    fun signDocument(
        clientId: String,
        responseUri: String,
        mdocNonce: String = Uuid.v4String(),
        authorizationRequestNonce: String,
        deviceNamespaces: DeviceNameSpacesCbor = CborMap(),
        document: DocumentCbor? = null,
        docType: DocType, // required since we could also call this method without a document to generate an error
        deviceKeyInfo: IKeyInfo<*>? = null,
        presentationDefinition: IOid4VPPresentationDefinition,
    ): Promise<Oid4vpSignResult> {
        return CoroutineScope(CoroutineName(COROUTINE_CONTEXT)).async {
            MdocOid4vpService().signDocument(
                clientId,
                responseUri,
                mdocNonce,
                authorizationRequestNonce,
                deviceNamespaces,
                document,
                docType,
                deviceKeyInfo,
                presentationDefinition
            )
        }.asPromise()
    }

    fun filterApplicableDocumentsPerInputDescriptor(
        allDocument: Array<DocumentCbor> = arrayOf(),
        inputDescriptor: IOid4VPInputDescriptor
    ): Array<DocumentCbor> {
        return MdocOid4vpService().filterApplicableDocumentsPerInputDescriptor(allDocument, inputDescriptor)
    }

    fun matchDocumentsAndDescriptors(
        mdocNonce: String = Uuid.v4String(), // mdoc nonce will be set on all document results
        applicableDocuments: Array<DocumentCbor>,
        presentationDefinition: IOid4VPPresentationDefinition
    ): Array<DocumentDescriptorMatchResult> {
        return MdocOid4vpService().matchDocumentsAndDescriptors(mdocNonce, applicableDocuments, presentationDefinition)
    }

}
