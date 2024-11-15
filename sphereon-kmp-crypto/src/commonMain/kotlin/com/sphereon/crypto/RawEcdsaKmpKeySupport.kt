import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.IJwk
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaCurve
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFromBase64Url
import com.sphereon.kmp.encodeTo
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.algorithms.SHA384
import dev.whyoleg.cryptography.algorithms.SHA512


/**
 * This file mainly contains conversion functions for the KMP crypto library we use, which in turn delegates to well-known platform specific libs
 *
 * We mainly use it for ECDSA conversions, like for instance bytes to JWK and vice versa, as well as bytes to pub/private keys
 */

/**
 * Prepares key information context by converting the provided `IKeyInfo` to a JWK format
 * and serializing its key into bytes, then resolving curve and algorithm implementations.
 *
 * @param keyInfo The key information to be prepared, implementing the `IKeyInfo` interface.
 * @return A `KeyInfoContext` containing the key, its byte representation, the curve, and the algorithm.
 */
@OptIn(ExperimentalStdlibApi::class)
fun keyInfoToEcdsaRawKmpContext(keyInfo: IKeyInfo<*>, resolver: ((info: IKeyInfo<*>) -> IKeyInfo<*>?)? = null): EcdsaRawKmpKeyInfoContext {
    val keyInfoJwk = toKeyInfoJwk(
        if (keyInfo.keyVisibility === KeyVisibility.PRIVATE && keyInfo.key !== null) keyInfo else (resolver?.let { resolveFunc -> resolveFunc(keyInfo) }
            ?: keyInfo)
    )

    val key = keyInfoJwk.key
        ?: throw IllegalArgumentException("Either we need to get the private key from a private key store or it needs to be passed in for this provider")
    if (key.x == null || key.y == null) {
        throw IllegalArgumentException("EC JWK needs an x and y coordinate as a public key")
    }
    val publicKeyBytes = "04${key.x.decodeFromBase64Url().encodeTo(Encoding.HEX)}${
        key.y.decodeFromBase64Url().encodeTo(Encoding.HEX)
    }".hexToByteArray()
    val privateKeyBytes = key.d?.decodeFromBase64Url()
    val curveImpl = resolveEcdsaKmpCurve(Curve.Static.fromJose(key.crv))
    val algImpl = resolveEcdsaKmpDigest(SignatureAlgorithm.Static.fromJose(key.alg))

    return EcdsaRawKmpKeyInfoContext(key, publicKeyBytes, privateKeyBytes, curveImpl, algImpl)
}


/**
 * Converts the provided `IKeyInfo` instance to a `KeyInfo` instance containing a `Jwk`.
 *
 * @param keyInfo The original `IKeyInfo` instance to be converted to `KeyInfo<Jwk>`.
 * @return A `KeyInfo` instance containing a `Jwk` generated from the provided `IKeyInfo`.
 * @throws IllegalArgumentException If the key is not present in the resulting `KeyInfo<Jwk>`.
 */
fun toKeyInfoJwk(keyInfo: IKeyInfo<*>): KeyInfo<Jwk> {
    val keyInfoJwk = CoseJoseKeyMappingService.toJwkKeyInfo(keyInfo)
    val key =
        keyInfoJwk.key ?: throw IllegalArgumentException("Looking up keys by kid is not supported yet. Please provide a public or private key")
    validateKey(key)
    return keyInfoJwk
}


/**
 * Validates the given JWK (JSON Web Key) to ensure it meets specific criteria for
 * Elliptic Curve Digital Signature Algorithm (ECDSA) keys.
 *
 * @param key the JWK to validate. It should have a key type of EC, a supported
 *            algorithm (ES256, ES384, or ES512), and a supported curve (P-256, P-384, or P-521).
 * @throws IllegalArgumentException if any of the validation checks fail.
 */
private fun validateKey(key: Jwk) {
    require(key.kty == JwaKeyType.EC) { "Key type (converted to JWA) ${key.kty} is not of type ${JwaKeyType.EC}" }
    require(
        arrayOf(
            JwaAlgorithm.ES256,
            JwaAlgorithm.ES384,
            JwaAlgorithm.ES512
        ).contains(key.alg)
    ) { "Key alg (converted to JWA) ${key.alg} is not supported for key type ${JwaKeyType.EC}" }
    require(
        arrayOf(
            JwaCurve.P_256,
            JwaCurve.P_384,
            JwaCurve.P_521
        ).contains(key.crv)
    ) { "Key crv (converted to JWA) ${key.crv} is not supported for key type ${JwaKeyType.EC}" }
}


/**
 * Converts the provided key bytes to a JSON Web Key (JWK).
 *
 * @param publicKeyBytes The bytes representing the key to be converted.
 * @return The equivalent JWK representation of the provided key bytes.
 */
fun convertRawKeyBytesToJwk(
    publicKeyBytes: ByteArray,
    privateKeyBytes: ByteArray?,
    use: JwkUse = JwkUse.sig,
    keyOperations: Array<out KeyOperations> = arrayOf(KeyOperations.SIGN),
    curve: Curve = Curve.P_256,
    alg: SignatureAlgorithm = SignatureAlgorithm.ECDSA_SHA256
): Jwk {

    val x = publicKeyBytes.copyOfRange(1, 33).encodeTo(Encoding.BASE64URL)
    val y = publicKeyBytes.copyOfRange(33, 65).encodeTo(Encoding.BASE64URL)
    val d = privateKeyBytes?.encodeTo(Encoding.BASE64URL)
    return Jwk(
        kty = JwaKeyType.EC,
        alg = alg.jose,
        crv = curve.jose,
        x = x,
        y = y,
        d = d,
        use = use.value,
        key_ops = keyOperations.map { it.jose }.toTypedArray()
    )
}

/**
 * Checks whether the provided curve is supported for EcDSA and throws an
 * IllegalArgumentException if it is not supported.
 *
 * @param curve The curve to be checked for support.
 */
fun checkSupportedEcdsaCurve(curve: Curve) {
    if (!arrayOf(Curve.P_256, Curve.P_384, Curve.P_521).contains(curve)) {
        throw IllegalArgumentException("Curve $curve not supported for EcDSA")
    }
}

/**
 * Resolves the given curve mapping to an elliptic curve.
 *
 * @param curve The curve mapping to be resolved.
 * @return The corresponding elliptic curve.
 * @throws IllegalArgumentException If the provided curve is not supported.
 */
fun resolveEcdsaKmpCurve(curve: Curve): EC.Curve {
    return when (curve) {
        is Curve.P_256 -> EC.Curve.P256
        is Curve.P_384 -> EC.Curve.P384
        is Curve.P_521 -> EC.Curve.P521
        else -> throw IllegalArgumentException("Curve $curve not supported")
    }
}

/**
 * Resolves the given algorithm mapping to its corresponding digest identifier.
 *
 * @param alg The algorithm mapping to resolve.
 * @return The corresponding CryptographyAlgorithmId for the given digest.
 */
fun resolveEcdsaKmpDigest(alg: SignatureAlgorithm): CryptographyAlgorithmId<Digest> {
    return when (alg) {
        is SignatureAlgorithm.ECDSA_SHA256 -> SHA256
        is SignatureAlgorithm.ECDSA_SHA384 -> SHA384
        is SignatureAlgorithm.ECDSA_SHA512 -> SHA512
        else -> throw IllegalArgumentException("Algorithm $alg not supported")
    }
}

/**
 * Represents the context information needed for key operations in ECDSA cryptography.
 *
 * @property key The JSON Web Key (JWK) representation of the cryptographic key.
 * @property publicKeyBytes The byte-array representation of the cryptographic key.
 * @property curveImpl The elliptic curve implementation used for cryptographic operations.
 * @property algImpl The cryptography algorithm identifier tied to a specific digest.
 */
@Suppress("NON_EXPORTABLE_TYPE")
data class EcdsaRawKmpKeyInfoContext(
    val key: Jwk,
    val publicKeyBytes: ByteArray,
    val privateKeyBytes: ByteArray?,
    val curveImpl: EC.Curve,
    val algImpl: CryptographyAlgorithmId<Digest>
)


/**
 * Converts an ECDSA public key represented as an `IJwk` to its raw byte array format.
 * The resulting byte array contains the uncompressed public key coordinates.
 *
 * @param key The JSON Web Key (JWK) containing the ECDSA public key information.
 * @return The raw byte array format of the ECDSA public key.
 */
@OptIn(ExperimentalStdlibApi::class)
fun toRawEcdsaPublicKeyBytes(key: IJwk): ByteArray {
    return "04${key.x!!.decodeFromBase64Url().encodeTo(Encoding.HEX)}${
        key.y!!.decodeFromBase64Url().encodeTo(Encoding.HEX)
    }".hexToByteArray()
}

/**
 * Converts the private key component of a given IJwk to a raw ECDSA private key byte array.
 *
 * @param key The input JWK from which the private key bytes will be extracted. The key must have
 *            a non-null 'd' parameter, which represents the private or secret part of the cryptographic key.
 * @return A byte array representing the raw ECDSA private key.
 * @throws IllegalArgumentException if the 'd' parameter of the input key is null, indicating that
 *         the key is not a private key JWK.
 */
fun toRawEcdsaPrivateKeyBytes(key: IJwk): ByteArray {
    require(key.d != null) { "Cannot convert to private key bytes if the input key is not a private key jwk (missing d param)" }
    return key.d!!.decodeFromBase64Url()
}

/**
 * Converts a resolved ECDSA public key to its raw byte representation.
 *
 * @param provider The cryptographic provider to use for the conversion. Defaults to `CryptographyProvider.Default`.
 * @param keyInfo An instance of `IResolvedKeyInfo` containing the resolved ECDSA key information.
 * @return An instance of `ECDSA.PublicKey` decoded from the raw byte representation.
 */
suspend fun toRawEcdsaPublicKey(provider: CryptographyProvider = CryptographyProvider.Default, keyInfo: IResolvedKeyInfo<*>): ECDSA.PublicKey {
    val jwkInfo = CoseJoseKeyMappingService.toResolvedJwkKeyInfo(keyInfo)
    val pubKeyBytes = toRawEcdsaPublicKeyBytes(jwkInfo.key)
    val ecdsa = provider.get(ECDSA)
    val curveImpl = resolveEcdsaKmpCurve(Curve.Static.fromJose(jwkInfo.key.crv))
    return ecdsa.publicKeyDecoder(curveImpl).decodeFromByteArray(EC.PublicKey.Format.RAW, pubKeyBytes)
}


suspend fun toRawEcdhPublicKey(provider: CryptographyProvider = CryptographyProvider.Default, keyInfo: IResolvedKeyInfo<*>): ECDH.PublicKey {
    val jwkInfo = CoseJoseKeyMappingService.toResolvedJwkKeyInfo(keyInfo)
    val pubKeyBytes = toRawEcdsaPublicKeyBytes(jwkInfo.key)
    val ecdh = provider.get(ECDH)
    val curveImpl = resolveEcdsaKmpCurve(Curve.Static.fromJose(jwkInfo.key.crv))
    return ecdh.publicKeyDecoder(curveImpl).decodeFromByteArray(EC.PublicKey.Format.RAW, pubKeyBytes)
}


/**
 * Converts the given resolved ECDSA key information to a raw ECDSA private key.
 *
 * @param provider The cryptography provider to use for the conversion. Defaults to CryptographyProvider.Default.
 * @param keyInfo The resolved key information of the ECDSA key to be converted.
 * @return The decoded raw ECDSA private key.
 */
suspend fun toRawEcdsaPrivateKey(provider: CryptographyProvider = CryptographyProvider.Default, keyInfo: IResolvedKeyInfo<*>): ECDSA.PrivateKey {
    val jwkInfo = CoseJoseKeyMappingService.toResolvedJwkKeyInfo(keyInfo)
    val privKeyBytes = toRawEcdsaPrivateKeyBytes(jwkInfo.key)
    val ecdsa = provider.get(ECDSA)
    val curveImpl = resolveEcdsaKmpCurve(Curve.Static.fromJose(jwkInfo.key.crv))
    return ecdsa.privateKeyDecoder(curveImpl).decodeFromByteArray(EC.PrivateKey.Format.RAW, privKeyBytes)
}


suspend fun toRawEcdhPrivateKey(provider: CryptographyProvider = CryptographyProvider.Default, keyInfo: IResolvedKeyInfo<*>): ECDH.PrivateKey {
    val jwkInfo = CoseJoseKeyMappingService.toResolvedJwkKeyInfo(keyInfo)
    val privKeyBytes = toRawEcdsaPrivateKeyBytes(jwkInfo.key)
    val ecdh = provider.get(ECDH)
    val curveImpl = resolveEcdsaKmpCurve(Curve.Static.fromJose(jwkInfo.key.crv))
    return ecdh.privateKeyDecoder(curveImpl).decodeFromByteArray(EC.PrivateKey.Format.RAW, privKeyBytes)
}
