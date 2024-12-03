package com.sphereon.mdoc


import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.encodeToCborByteArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.CoseJoseKeyMappingService.toResolvedCoseKeyInfo
import com.sphereon.crypto.CoseJoseKeyMappingService.toResolvedKeyInfo
import com.sphereon.crypto.CoseSign1Result
import com.sphereon.crypto.CryptoServices
import com.sphereon.crypto.DefaultCallbacks
import com.sphereon.crypto.ICoseCryptoCallbackService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IManagedKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.ManagedKeyInfo
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.kmp.Encoding
import com.sphereon.mdoc.MdocSignService.Static.getSuppliedOrMSODerivedCborKeyInfo
import com.sphereon.mdoc.data.device.DeviceAuthCbor
import com.sphereon.mdoc.data.device.DeviceAuthenticationCbor
import com.sphereon.mdoc.data.device.DeviceSignedCbor
import com.sphereon.mdoc.data.device.DocRequestCbor
import com.sphereon.mdoc.data.device.DocumentCbor
import com.sphereon.mdoc.data.device.IssuerSignedCbor
import com.sphereon.mdoc.data.device.IssuerSignedNamesSpacesCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor

class MdocSignService(val cryptoCallbackService: ICoseCryptoCallbackService = DefaultCallbacks.coseCrypto()) {

    object Static {

        fun getSuppliedOrMSODerivedCborKeyInfo(keyInfo: IKeyInfo<*>? = null, mso: MobileSecurityObjectCbor? = null, lookupManaged: Boolean = false): IResolvedKeyInfo<ICoseKeyCbor> {

            val msoInfo: ResolvedKeyInfo<CoseKeyCbor>? = mso?.deviceKeyInfo?.deviceKey?.let {
                ResolvedKeyInfo(
                    key = it,
                    kid = it.kid?.encodeTo(Encoding.BASE64URL) ?: keyInfo?.kid,
                    signatureAlgorithm = it.alg?.let { alg -> SignatureAlgorithm.Static.fromCose(CoseAlgorithm.Static.fromValue(alg.value.toInt())) }
                        ?: keyInfo?.signatureAlgorithm,
                )
            }
            val signatureAlgorithm = msoInfo?.signatureAlgorithm ?: keyInfo?.signatureAlgorithm ?: keyInfo?.key?.getSignatureAlgorithm() // The above object already takes passed in sig algo into account as fallback
            val key = (keyInfo?.key?.let{CoseJoseKeyMappingService.toCoseKey(it)} ?: msoInfo?.key)
            if (key == null) {
                throw IllegalArgumentException("No key information provided and it could not be derived from the Mobile Security Object")
            }
            val kty = signatureAlgorithm?.cose?.keyType?.toCbor() ?: key.kty ?: throw IllegalArgumentException("kty could not be resolved for Cbor Key!")
            val crv = key.crv ?: signatureAlgorithm?.cose?.curve?.toCbor()
            val resolvedKey = key.copy(kid = key.kid ?: msoInfo?.kid?.toCborByteString() ?: keyInfo?.kid?.toCborByteString(), kty = kty, crv = crv )

            if (keyInfo != null) {
                return toResolvedCoseKeyInfo(toResolvedKeyInfo(keyInfo, resolvedKey)).copy(
                    signatureAlgorithm = signatureAlgorithm,
                    kid = keyInfo.kid ?: msoInfo?.kid,
                    keyType = KeyType.Static.fromCose(CoseKeyType.Static.fromValue(kty.value)),
                    x5c = resolvedKey.getX509CertificateChain() ?: keyInfo.x5c,
                )
            } else if (msoInfo == null) {
                throw IllegalArgumentException("No key information provided and it could not be derived from the Mobile Security Object")
            }
            if (lookupManaged && msoInfo.kmsKeyRef === null) {

            }
            return msoInfo
        }
    }

    suspend fun issuerSignMso(
        mso: MobileSecurityObjectCbor,
        issuerKeyInfo: IManagedKeyInfo<*>,
        signatureAlgorithm: SignatureAlgorithm? = issuerKeyInfo.signatureAlgorithm,
        unprotectedHeader: CoseHeaderCbor? = null,
        protectedHeader: CoseHeaderCbor? = null,
        requireDeviceX5Chain: Boolean = false,
    ): CoseSign1Result<MobileSecurityObjectCbor> {

        // Just an assertion it is present
        getSuppliedOrMSODerivedCborKeyInfo(mso = mso)

        val cborIssuerSignKeyInfo: ManagedKeyInfo<CoseKeyCbor> = ManagedKeyInfo(
            kmsKeyRef = issuerKeyInfo.kmsKeyRef,
            kms = issuerKeyInfo.kms,
            resolvedKeyInfo = toResolvedCoseKeyInfo(issuerKeyInfo)
        )

        val alg = (signatureAlgorithm ?: issuerKeyInfo.signatureAlgorithm ?: cborIssuerSignKeyInfo.key.alg?.let {
            SignatureAlgorithm.Static.fromCose(CoseAlgorithm.Static.fromValue(it.value.toInt()))
        })


        val protected = CoseHeaderCbor.Static.copyOrInit(protectedHeader, alg = alg?.cose)
        val kidVal = cborIssuerSignKeyInfo.kid ?: cborIssuerSignKeyInfo.key.kid
        val kid = if (kidVal is String) kidVal.toCborByteString(Encoding.BASE64URL) else if (kidVal is CborByteString) kidVal else null
        val x5cStr = cborIssuerSignKeyInfo.key.getX509CertificateChain()
        if (protected.x5chain == null && x5cStr != null) {
            protected.x5chain = x5cStr.encodeToCborByteArray(Encoding.BASE64) // Base64 not base64url for x5c!
        }
        if (kid !== null) {
            if (protected.kid != null && kid !== protected.kid) {
                throw IllegalArgumentException("Mismatch between key info kid ${cborIssuerSignKeyInfo.kid} and key kid $kid")
            }
            protected.kid = kid
        }

        val input = CoseSign1InputCbor.Builder()
            .withPayload(mso)
//            .encodePayload(true)
            .withProtectedHeader(protected)
            .withUnprotectedHeader(unprotectedHeader)
            .build()
        val signResult = CryptoServices.cose(cryptoCallbackService).sign1<MobileSecurityObjectCbor>(
            input = input,
            keyInfo = cborIssuerSignKeyInfo,
            requireX5Chain = requireDeviceX5Chain
        )
        return signResult
    }

    suspend fun issuerSignIssuerSigned(
        mso: MobileSecurityObjectCbor,
        issuerSignedNameSpaces: IssuerSignedNamesSpacesCbor,
        issuerKeyInfo: IManagedKeyInfo<*>,
        signatureAlgorithm: SignatureAlgorithm? = issuerKeyInfo.signatureAlgorithm,
        unprotectedHeader: CoseHeaderCbor? = null,
        protectedHeader: CoseHeaderCbor? = null,
        requireDeviceX5Chain: Boolean = false,
    ): IssuerSignedCbor {
        val signResult = issuerSignMso(mso, issuerKeyInfo, signatureAlgorithm, unprotectedHeader, protectedHeader, requireDeviceX5Chain)
        return IssuerSignedCbor(nameSpaces = issuerSignedNameSpaces, issuerAuth = signResult.coseSign1)
    }

    suspend fun issuerSignDocument(
        mso: MobileSecurityObjectCbor,
        issuerSignedNameSpaces: IssuerSignedNamesSpacesCbor,
        issuerKeyInfo: IManagedKeyInfo<*>,
        signatureAlgorithm: SignatureAlgorithm? = issuerKeyInfo.signatureAlgorithm,
        unprotectedHeader: CoseHeaderCbor? = null,
        protectedHeader: CoseHeaderCbor? = null,
        requireDeviceX5Chain: Boolean = false,
    ): DocumentCbor {
        return DocumentCbor(
            docType = mso.docType,
            issuerSigned = issuerSignIssuerSigned(
                mso,
                issuerSignedNameSpaces,
                issuerKeyInfo,
                signatureAlgorithm,
                unprotectedHeader,
                protectedHeader,
                requireDeviceX5Chain
            ),
            deviceSigned = null
        )
    }

    suspend fun deviceSignDocument(
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
        val x5cStr = keyInfo.key.getX509CertificateChain()
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
