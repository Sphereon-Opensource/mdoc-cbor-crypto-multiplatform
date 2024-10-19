package com.sphereon.mdoc

import CoseJoseKeyMappingService
import CoseJoseKeyMappingService.toCoseKeyInfo
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.encodeToCborByteArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.CoseCryptoServiceObject
import com.sphereon.crypto.ICoseCryptoService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.kmp.Encoding
import com.sphereon.mdoc.MdocSignService.Static.getSuppliedOrMSODerivedCborKeyInfo
import com.sphereon.mdoc.data.device.DeviceAuthCbor
import com.sphereon.mdoc.data.device.DeviceAuthenticationCbor
import com.sphereon.mdoc.data.device.DeviceSignedCbor
import com.sphereon.mdoc.data.device.DocRequestCbor
import com.sphereon.mdoc.data.device.DocumentCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor

class MdocSignService(val cryptoService: ICoseCryptoService = CoseCryptoServiceObject) {

    object Static {

        fun getSuppliedOrMSODerivedCborKeyInfo(keyInfo: IKeyInfo<*>? = null, mso: MobileSecurityObjectCbor? = null): IKeyInfo<ICoseKeyCbor> {
            val key = keyInfo?.key
            if (key != null) {
                return toCoseKeyInfo(keyInfo)
            }

            val info = mso?.deviceKeyInfo?.deviceKey?.let { KeyInfo(key = CoseJoseKeyMappingService.toCoseKey(it), kid = it.kid?.encodeTo(Encoding.BASE64URL)) } ?: keyInfo
            if (info === null) {
                throw IllegalArgumentException("No key information provided and it could not be derived from the Mobile Security Object")
            }
            return toCoseKeyInfo(info)

        }
    }

    suspend fun signDocument(
        request: DocRequestCbor,
        document: DocumentCbor,
        deviceAuthentication: DeviceAuthenticationCbor,
        deviceKeyInfo: IKeyInfo<*>? = null,
        unprotectedHeader: CoseHeaderCbor? = null,
        protectedHeader: CoseHeaderCbor? = null
    ): DocumentCbor {
        if (request.itemsRequest.docType != document.docType) {
            throw IllegalArgumentException("Document request docType ${request.itemsRequest.docType} does not match document docType ${document.docType}")
        }


        val keyInfo = getSuppliedOrMSODerivedCborKeyInfo(keyInfo = deviceKeyInfo, mso = document.MSO)
        val protected = CoseHeaderCbor.Static.copyOrInit(protectedHeader)
        val kidVal = keyInfo.kid ?: keyInfo.key?.kid
        val kid = if (kidVal is String) kidVal.toCborByteString(Encoding.BASE64URL) else if (kidVal is CborByteString) kidVal else null
        val x5cStr = keyInfo.key?.getX5cArray()
        if (protected.x5chain == null && x5cStr != null) {
            protected.x5chain = x5cStr.encodeToCborByteArray(Encoding.BASE64) // Base64 not base64url for x5c!
        }
        if (kid !== null) {
            if (protected.kid != null && kid !== protected.kid) {
                throw IllegalArgumentException("Mismatch between key info kid ${keyInfo.kid} and key kid $kid")
            }
            protected.kid = kid
        }

        val input = CoseSign1InputCbor.Builder()
            .withPayload(deviceAuthentication)
            .withProtectedHeader(protected)
            .withUnprotectedHeader(unprotectedHeader)
            .build()
        val deviceSignature = this.cryptoService.sign1<DeviceAuthenticationCbor>(
            input = input,
            keyInfo = keyInfo
        ).detachedPayloadCopy()

        return DocumentCbor(
            docType = request.itemsRequest.docType,
            deviceSigned = DeviceSignedCbor(
                nameSpaces = deviceAuthentication.deviceNamespaces,
                deviceAuth = DeviceAuthCbor(deviceSignature = deviceSignature)
            ),
            issuerSigned = document.limitDisclosures(request)
        )
    }
}
