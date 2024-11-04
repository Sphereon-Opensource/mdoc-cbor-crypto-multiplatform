package com.sphereon.crypto.kms

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.providers.ManagedKeyPair
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.kmp.Uuid
import kotlin.js.JsExport

@JsExport
open class KeyManagerService<X509PlatformCallback : IX509ServiceMarkerType>(
    keyManagementSystems: Array<IKeyManagementSystem>,
    keyResolvers: Array<IKeyResolverService> = arrayOf(
        X509CertificateChainKeyResolverService<X509PlatformCallback>(),
        CoseJoseProvidedKeyResolverService<X509PlatformCallback>()
    ),
    private val publicKeyStore: IKeyStoreService = MemoryKeyStore(),
    private var defaultKeyManagementSystem: String = keyManagementSystems[0].getId(), // Default to the first one
    private var defaultResolver: String = keyResolvers[0].getId() // Defaults to the first one
) : IKeyManagerService {
    private val keyManagementSystems: MutableMap<String, IKeyManagementSystem> = keyManagementSystems.associateBy { it.getId() }.toMutableMap()
    private val keyResolvers: MutableMap<String, IKeyResolverService> = keyResolvers.associateBy { it.getId() }.toMutableMap()

    init {
        require(keyManagementSystems.isNotEmpty()) { "At least one key manager system is required" }
        require(keyResolvers.isNotEmpty()) { "At least one resolver is required" }
    }

    override fun defaultKmsId() = defaultKeyManagementSystem
    override fun defaultResolverId() = defaultResolver
    override fun registerKms(kms: IKeyManagementSystem, makeDefaultKms: Boolean?) {
        keyManagementSystems[kms.getId()] = kms
        if (makeDefaultKms == true) {
            defaultKeyManagementSystem = kms.getId()
        }
    }

    override fun getKmsIds(): Array<String> = keyManagementSystems.keys.toTypedArray()
    override fun getKmsById(id: String): IKeyManagementSystem = keyManagementSystems[id] ?: throw PKIException("Invalid KMS id $id provider")
    override fun getKmsBySignatureAlgorithm(signatureAlgorithm: SignatureAlgorithm): IKeyManagementSystem {
        return keyManagementSystems.values.firstOrNull { it.supportedSignatureAlgorithms().contains(signatureAlgorithm) }
            ?: throw PKIException("No KMS found for signature algorithm $signatureAlgorithm")
    }

    override fun getResolverIds() = keyResolvers.keys.toTypedArray()
    override fun getResolverById(id: String): IKeyResolverService = keyResolvers[id] ?: throw PKIException("Invalid Resolver id $id provider")
    override fun getResolverByKeyTypeOrIdentifier(
        identifierMethod: IdentifierMethod?,
        keyType: KeyType?,
        identifierId: String?
    ): IKeyResolverService {
        val resolver = keyResolvers.values.firstOrNull {
            if (identifierId !== null && it.getId() == identifierId) {
                return@firstOrNull true
            }
            if (keyType !== null) {
                if (identifierMethod !== null) {
                    return@firstOrNull it.getSupportedKeyTypes(identifierMethod).contains(keyType)
                } else if (it.allSupportedKeyTypes().contains(keyType)) {
                    return@firstOrNull true
                }
            }
            if (identifierMethod !== null && it.allSupportedIdentifierMethods().contains(identifierMethod)) {
                return@firstOrNull true
            }
            return@firstOrNull it.getId() == defaultResolverId()
        }

        return resolver ?: throw IllegalArgumentException("Could not find resolver for identifier method $identifierMethod and key type $keyType")
    }

    override fun registerResolver(resolver: IKeyResolverService, makeDefaultResolver: Boolean?) {
        keyResolvers[resolver.getId()] = resolver
        if (makeDefaultResolver == true) {
            defaultResolver = resolver.getId()
        }
    }


    @JsExport.Ignore

    override suspend fun <KeyType : IKey> resolvePublicKeyAsync(
        keyInfo: IKeyInfo<KeyType>,
        identifierMethod: IdentifierMethod?,
        trustedCerts: Array<String>?,
        verifyX509CertificateChain: Boolean?
    ): IResolvedKeyInfo<KeyType> =
        getResolverByKeyTypeOrIdentifier(keyType = keyInfo.keyType, identifierId = keyInfo.kms).resolvePublicKeyAsync(
            keyInfo,
            identifierMethod,
            trustedCerts,
            verifyX509CertificateChain
        )


    @JsExport.Ignore
    override suspend fun generateKeyAsync(
        kms: String?,
        kmsKeyRef: String?,
        use: JwkUse?,
        keyOperations: Array<out KeyOperations>?,
        alg: SignatureAlgorithm?
    ): ManagedKeyPair {
        val kmsService = getKms(kms, alg)
        val keyPair = kmsService.generateKeyAsync(use, keyOperations, alg)
        val pubKey = keyPair.jose.publicJwk
        val resultKeyRef = kmsKeyRef ?: pubKey.kid ?: Uuid.v4String()
        val keyInfo = ResolvedKeyInfo(
            key = pubKey,
            keyType = pubKey.getKty(),
            kms = kmsService.getId(),
            kmsKeyRef = resultKeyRef,
            keyVisibility = KeyVisibility.PUBLIC,
            kid = pubKey.kid,
            signatureAlgorithm = alg ?: pubKey.getSignatureAlgorithm()
        )
        publicKeyStore.storeKey(keyInfo = keyInfo, kms = kmsService.getId(), kmsKeyRef = resultKeyRef)
        return keyPair
    }


    override fun getKms(kms: String?, alg: SignatureAlgorithm?): IKeyManagementSystem {
        if (kms === null && alg !== null) {
            return getKmsBySignatureAlgorithm(alg)
        }
        return getKmsById(kms ?: defaultKmsId())
    }

    @JsExport.Ignore
    override suspend fun createRawSignatureAsync(keyInfo: IKeyInfo<*>, input: ByteArray) =
        getKms(kms = keyInfo.kms, alg = keyInfo.signatureAlgorithm).createRawSignatureAsync(keyInfo, input)

    @JsExport.Ignore
    override suspend fun isValidRawSignatureAsync(keyInfo: IKeyInfo<*>, input: ByteArray, signature: ByteArray): Boolean {
        val kms = keyInfo.kms
        val alg = keyInfo.signatureAlgorithm
        if (kms !== null || alg !== null) {
            return getKms(kms, alg).isValidRawSignatureAsync(keyInfo, input, signature)
        }
        return getKmsById(defaultKmsId()).isValidRawSignatureAsync(keyInfo, input, signature)
    }
}

@JsExport
interface IKeyManagerService : IRawSignatureService, IPublicKeyResolver {
    fun defaultKmsId(): String
    fun defaultResolverId(): String
    fun registerKms(kms: IKeyManagementSystem, makeDefaultKms: Boolean? = false)
    fun getKmsIds(): Array<String>
    fun getKmsById(id: String = defaultKmsId()): IKeyManagementSystem
    fun getKmsBySignatureAlgorithm(signatureAlgorithm: SignatureAlgorithm): IKeyManagementSystem
    fun getResolverById(id: String = defaultResolverId()): IKeyResolverService
    fun getResolverByKeyTypeOrIdentifier(
        identifierMethod: IdentifierMethod? = null,
        keyType: KeyType? = null,
        resolverId: String? = null
    ): IKeyResolverService

    fun registerResolver(resolver: IKeyResolverService, makeDefaultResolver: Boolean? = false)



    /**
     * Asynchronously generates a cryptographic key pair based on the specified elliptic curve mapping.
     *
     * @param curve The elliptic curve mapping used to generate the key pair. This parameter defines the types of curves supported
     *              for cryptographic operations and includes mappings for both COSE and JOSE curves.
     * @return A `CryptoProviderKeyPair` containing both COSE and JOSE key pairs.
     */
    @JsExport.Ignore
    suspend fun generateKeyAsync(
        kms: String? = null, // Either we look for the signature algo supported or use the default KMS
        kmsKeyRef: String? = null,
        use: JwkUse? = null,
        keyOperations: Array<out KeyOperations>? = null,
        alg: SignatureAlgorithm? = null
    ): ManagedKeyPair

    fun getResolverIds(): Array<String>
    fun getKms(kms: String?, alg: SignatureAlgorithm?): IKeyManagementSystem
}

@JsExport
interface IKeyManagementSystem : IRawSignatureService {

    fun getId(): String

    /**
     * Retrieves an array of supported key type mappings.
     *
     * The supported key types represent the mappings between COSE (CBOR Object Signing and Encryption)
     * key types and their corresponding JWA (JSON Web Algorithms) key types. This method provides the
     * available key type mappings for cryptographic operations.
     *
     * @return An array of supported `KeyTypeMapping` objects.
     */
    fun supportedKeyTypes(): Array<KeyType>

    /**
     * Provides a list of supported algorithms by the crypto provider.
     *
     * @return An array of AlgorithmMapping objects representing the supported algorithms.
     */
    fun supportedSignatureAlgorithms(): Array<SignatureAlgorithm>

    fun supportedDigests(): Array<DigestAlg> // convenience, derived from signature algos above

    /**
     * Retrieves an array of supported elliptic curve mappings.
     *
     * @return An array of `CurveMapping` instances representing the supported elliptic curves
     *         for cryptographic operations.
     */
    fun supportedCurves(): Array<Curve>

    /**
     * Checks if the provided elliptic curve mapping is supported by the crypto provider.
     *
     * @param curve The curve mapping to be checked for support.
     * @return true if the curve is supported, false otherwise.
     */
    fun isSupportedCurve(curve: Curve): Boolean

    /**
     * Asynchronously generates a cryptographic key pair based on the specified elliptic curve mapping.
     *
     * @param curve The elliptic curve mapping used to generate the key pair. This parameter defines the types of curves supported
     *              for cryptographic operations and includes mappings for both COSE and JOSE curves.
     * @return A `CryptoProviderKeyPair` containing both COSE and JOSE key pairs.
     */
    @JsExport.Ignore
    suspend fun generateKeyAsync(
        use: JwkUse? = null,
        keyOperations: Array<out KeyOperations>? = null,
        alg: SignatureAlgorithm? = null
    ): ManagedKeyPair
}
