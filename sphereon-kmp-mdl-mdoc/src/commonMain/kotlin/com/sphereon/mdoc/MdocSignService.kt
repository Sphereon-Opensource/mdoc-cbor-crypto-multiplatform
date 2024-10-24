package com.sphereon.mdoc


import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.encodeToCborByteArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.CoseJoseKeyMappingService.toResolvedCoseKeyInfo
import com.sphereon.crypto.CoseJoseKeyMappingService.toResolvedKeyInfo
import com.sphereon.crypto.CryptoServices
import com.sphereon.crypto.DefaultCallbacks
import com.sphereon.crypto.ICoseCryptoCallbackService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.toJoseSignatureAlgorithm
import com.sphereon.kmp.Encoding
import com.sphereon.mdoc.MdocSignService.Static.getSuppliedOrMSODerivedCborKeyInfo
import com.sphereon.mdoc.data.device.DeviceAuthCbor
import com.sphereon.mdoc.data.device.DeviceAuthenticationCbor
import com.sphereon.mdoc.data.device.DeviceSignedCbor
import com.sphereon.mdoc.data.device.DocRequestCbor
import com.sphereon.mdoc.data.device.DocumentCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor

class MdocSignService(val cryptoCallbackService: ICoseCryptoCallbackService = DefaultCallbacks.coseCrypto()) {

    object Static {

        fun getSuppliedOrMSODerivedCborKeyInfo(keyInfo: IKeyInfo<*>? = null, mso: MobileSecurityObjectCbor? = null): IResolvedKeyInfo<ICoseKeyCbor> {

            val msoInfo: ResolvedKeyInfo<CoseKeyCbor>? = mso?.deviceKeyInfo?.deviceKey?.let {
                ResolvedKeyInfo(
                    key = it,
                    kid = it.kid?.encodeTo(Encoding.BASE64URL) ?: keyInfo?.kid,
                    signatureAlgorithm = it.alg?.let { alg -> SignatureAlgorithm.Static.fromCose(CoseAlgorithm.Static.fromValue(alg.value.toInt())) }
                        ?: keyInfo?.signatureAlgorithm,
                )
            }
            val key = keyInfo?.key ?: msoInfo?.key
            if (key == null) {
                throw IllegalArgumentException("No key information provided and it could not be derived from the Mobile Security Object")
            }
            val signatureAlgorithm = msoInfo?.signatureAlgorithm // The above object already takes passed in sig algo into account as fallback
            if (keyInfo != null) {
                return toResolvedCoseKeyInfo(toResolvedKeyInfo(keyInfo, key)).copy(
                    signatureAlgorithm = signatureAlgorithm,
                    kid = keyInfo.kid ?: msoInfo?.kid
                )
            } else if (msoInfo == null) {
                throw IllegalArgumentException("No key information provided and it could not be derived from the Mobile Security Object")
            }
            return msoInfo
        }
    }

    suspend fun signDocument(
        request: DocRequestCbor,
        document: DocumentCbor,
        deviceAuthentication: DeviceAuthenticationCbor,
        deviceKeyInfo: IKeyInfo<*>? = null,
        unprotectedHeader: CoseHeaderCbor? = null,
        protectedHeader: CoseHeaderCbor? = null,
        requireDeviceX5Chain: Boolean = false,
    ): DocumentCbor {
        if (request.itemsRequest.docType != document.docType) {
            throw IllegalArgumentException("Document request docType ${request.itemsRequest.docType} does not match document docType ${document.docType}")
        }
        val keyInfo = getSuppliedOrMSODerivedCborKeyInfo(keyInfo = deviceKeyInfo, mso = document.MSO)
        var signatureAlgorithm = keyInfo.signatureAlgorithm ?: keyInfo.key.alg?.let {
            SignatureAlgorithm.Static.fromCose(CoseAlgorithm.Static.fromValue(it.value.toInt()))
        }
        val alg = protectedHeader?.alg
        if (alg !== null) {
            signatureAlgorithm = SignatureAlgorithm.Static.fromCose(alg)
        }

        val protected = CoseHeaderCbor.Static.copyOrInit(protectedHeader, alg = signatureAlgorithm?.cose)
        val kidVal = keyInfo.kid ?: keyInfo.key.kid
        val kid = if (kidVal is String) kidVal.toCborByteString(Encoding.BASE64URL) else if (kidVal is CborByteString) kidVal else null
        val x5cStr = keyInfo.key.getX5cArray()
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
        val signResult = CryptoServices.cose(cryptoCallbackService).sign1<DeviceAuthenticationCbor>(
            input = input,
            keyInfo = keyInfo,
            requireX5Chain = requireDeviceX5Chain
        )
        val deviceSignature = signResult.coseSign1.detachedPayloadCopy()
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
