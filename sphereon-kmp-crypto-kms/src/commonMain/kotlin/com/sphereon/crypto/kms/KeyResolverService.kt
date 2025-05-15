package com.sphereon.crypto.kms

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.kms.model.IdentifierMethod
import com.sphereon.crypto.x509Service
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import kotlin.js.JsExport

/**
 * Service for resolving public keys from X.509 certificate chains.
 *
 * This service is used to resolve and verify the leaf public key contained within
 * X.509 certificate chains.
 *
 * @param X509PlatformCallback A platform-specific callback type for X.509 services.
 * @param id The identifier for the service, defaults to "x5c".
 * @param supported A map of supported identifier methods and their respective key types, defaults to predefined values.
 */
@JsExport
class X509CertificateChainKeyResolverService<X509PlatformCallback : IX509ServiceMarkerType>(
    id: String = "x5c",
    supported: Map<IdentifierMethod, Array<KeyType>> = mapOf(
        Pair(IdentifierMethod.x5c, arrayOf(KeyType.EC, KeyType.RSA)),
        Pair(IdentifierMethod.jwk, arrayOf(KeyType.EC, KeyType.RSA)),
        Pair(IdentifierMethod.cose_key, arrayOf(KeyType.EC, KeyType.RSA)),
    )
) : AbstractKeyResolverService(id = id, supported = supported), IKeyResolverService {
    @JsExport.Ignore
    override suspend fun <KT : IKey> resolvePublicKeyAsync(
        keyInfo: IKeyInfo<KT>,
        identifierMethod: IdentifierMethod?,
        trustedCerts: Array<String>?,
        verifyX509CertificateChain: Boolean?
    ): IResolvedKeyInfo<KT> {
        val x509 = keyInfo.x5c ?: keyInfo.key?.getX509CertificateChain() ?: throw IllegalArgumentException("X509 chain not present")
        val x509Result = x509Service<X509PlatformCallback>().verifyCertificateChainAsync<KT>(
            chainDER = x509.map { it.decodeFrom(Encoding.BASE64) }.toTypedArray()
        )
        val leafKey = x509Result.publicKey ?: throw PKIException("No public key could be extracted from the provided certification chain")
        val updateKeyInfo = KeyInfo.Static.fromDTO(keyInfo).copy(kid = keyInfo.kid ?: leafKey.getKidAsString(true), key = leafKey)
        return ResolvedKeyInfo.Static.fromKeyInfo(updateKeyInfo, leafKey)
    }
}

/**
 * A service for resolving provided keys specifically for JOSE (JSON Object Signing and Encryption)
 * and COSE (CBOR Object Signing and Encryption).
 *
 * @param x509PlatformCallback A marker type for the platform-specific X509 service.
 * @param id The identifier for the resolver service, default is "jose_cose_resolver".
 * @param supported A map of identifier methods to the associated key types supported by this resolver.
 */
@JsExport
class CoseJoseProvidedKeyResolverService<x509PlatformCallback : IX509ServiceMarkerType>(
    id: String = "jose_cose_resolver",
    supported: Map<IdentifierMethod, Array<KeyType>> = mapOf(
        Pair(IdentifierMethod.jwk, KeyType.Static.asList.toTypedArray()),
        Pair(IdentifierMethod.cose_key, KeyType.Static.asList.toTypedArray())
    )
) : AbstractKeyResolverService(id = id, supported = supported), IKeyResolverService {
    @JsExport.Ignore
    override suspend fun <KT : IKey> resolvePublicKeyAsync(
        keyInfo: IKeyInfo<KT>,
        identifierMethod: IdentifierMethod?,
        trustedCerts: Array<String>?,
        verifyX509CertificateChain: Boolean?
    ): IResolvedKeyInfo<KT> {
        require(keyInfo.key is KT) { "Jose-cose key resolver only accepts Jwk or cose keys in the key info object" }
        require(identifierMethod === null || identifierMethod == IdentifierMethod.jwk || identifierMethod == IdentifierMethod.cose_key) { "Cannot use an identifier method other than jwk for the jwk key resolver" }
        val resolvedKeyInfo = ResolvedKeyInfo.Static.fromKeyInfo<KT>(keyInfo).toResolvedPublicKeyInfo()
        if (verifyX509CertificateChain == true && !trustedCerts.isNullOrEmpty() && !resolvedKeyInfo.key.getX509CertificateChain().isNullOrEmpty()) {
            val x509Result = x509Service<x509PlatformCallback>().verifyCertificateChainAsync<KT>(
                trustedCerts = trustedCerts,
                chainDER = keyInfo.key?.getX509CertificateChain()?.map { it.decodeFrom(Encoding.BASE64) }?.toTypedArray()
                    ?: throw IllegalArgumentException("X509 chain not present")
            )
            // TODO: Reenable the below data. For whatever reason it messes up the JS side of things where it cannot find the IX509VerificationResult
//            return resolvedKeyInfo.copy(x509VerificationResult = x509Result)
        }
        return resolvedKeyInfo
    }
}

