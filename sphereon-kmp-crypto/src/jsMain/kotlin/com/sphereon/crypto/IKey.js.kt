package com.sphereon.crypto

import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm

/**
 * Represents a resolved cryptographic key information interface.
 *
 * This interface guarantees that the key is present and resolved, providing concrete access to the key.
 *
 * @param KT Generic type that extends IKey, representing the cryptographic key type.
 */
actual external interface IManagedKeyInfo<KT : IKey> : IResolvedKeyInfo<KT> {
    actual override val kmsKeyRef: String
    actual override val kms: String
    actual fun toManagedPublicKeyInfo(): IManagedKeyInfo<KT>
}


@JsExport
actual external interface IKey {
    @JsName("kty")
    actual val kty: Any

    @JsName("kid")
    actual val kid: Any?

    @JsName("alg")
    actual val alg: Any?

    @JsName("key_ops")
    actual val key_ops: Any?

    /*@JsName("baseIV")
    actual val baseIV: Any?
*/
    @JsName("crv")
    actual val crv: Any?

    @JsName("x")
    actual val x: Any?

    @JsName("y")
    actual val y: Any?

    /*  @JsName("x5chain") //x5c in JWK
      actual val x5chain: Any?
  */
    @JsName("additional")
    actual val additional: Any?

    @JsName("d")
    actual val d: Any?

    // Mappings to help implementers easily get values in their poison of choice (COSE/JWA) no matter the key type
    actual fun getSignatureAlgorithm(): SignatureAlgorithm?
    actual fun getKty(): KeyType
    actual fun getKeyOperations(): Array<KeyOperations>?
    actual fun getX509CertificateChain(): Array<String>?
    actual fun toPublicKey(): IKey
    actual fun getKidAsString(generate: Boolean): String?
    actual fun getXAsString(): String?
    actual fun getYAsString(): String?

}


@JsExport
actual external interface IKeyInfo<out KT : IKey> {
    @JsName("kid")
    actual val kid: String?

    @JsName("key")
    actual val key: KT?

    @JsName("opts")
    actual val opts: Map<*, *>?

    @JsName("signatureAlgorithm")
    actual val signatureAlgorithm: SignatureAlgorithm?

    @JsName("keyVisibility")
    actual val keyVisibility: KeyVisibility?
    actual val x5c: Array<String>?
    actual val kmsKeyRef: String?
    actual fun toPublicKeyInfo(): IKeyInfo<KT>
    actual val kms: String?
    actual val keyType: KeyType?
}

@JsExport
actual external interface IResolvedKeyInfo<out KT : IKey> : IKeyInfo<KT> {
    actual override val key: KT
//    actual val x509VerificationResult: IX509VerificationResult<KT>?
    actual fun toResolvedPublicKeyInfo(): IResolvedKeyInfo<KT>
}

