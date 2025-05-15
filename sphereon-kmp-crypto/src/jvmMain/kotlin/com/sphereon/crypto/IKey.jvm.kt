package com.sphereon.crypto

import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.IJwk

/**
 * Represents a resolved cryptographic key information interface.
 *
 * This interface guarantees that the key is present and resolved, providing concrete access to the key.
 *
 * @param KT Generic type that extends IKey, representing the cryptographic key type.
 */
actual interface IManagedKeyInfo<KT : IKey> : IResolvedKeyInfo<KT> {
    actual override val kmsKeyRef: String
    actual override val kms: String
    actual fun toManagedPublicKeyInfo(): IManagedKeyInfo<KT>
}

actual interface IKeyDTO {
    actual val kty: Any
    actual val kid: Any?
    actual val alg: Any?
    actual val key_ops: Any?
    actual val crv: Any?
    actual val x: Any?
    actual val y: Any?
    actual val d: Any?
    actual val additional: Any?

}
actual interface IKey: IKeyDTO {

    // Mappings to help implementers easily get values in their poison of choice (COSE/JWA) no matter the key type
    actual fun getSignatureAlgorithm(): SignatureAlgorithm?
    actual fun getKty(): KeyType
    actual fun getKeyOperations(): Array<KeyOperations>?
    actual fun getX509CertificateChain(): Array<String>?
    actual fun getKidAsString(generate: Boolean): String?
    actual fun toPublicKey(): IKey
    actual fun getXAsString(): String?
    actual fun getYAsString(): String?

}



actual interface IKeyInfo<out KT : IKey> {
    actual val kid: String?
    actual val key: KT?
    actual val opts: Map<*, *>?
    actual val signatureAlgorithm: SignatureAlgorithm?
    actual val keyVisibility: KeyVisibility?
    actual val x5c: Array<String>?
    actual val kms: String?
    actual val kmsKeyRef: String?
    actual val keyType: KeyType?
    actual fun toPublicKeyInfo(): IKeyInfo<KT>
}

actual interface IResolvedKeyInfo<out KT : IKey>: IKeyInfo<KT> {
    actual override val key: KT
//    actual val x509VerificationResult: IX509VerificationResult<KT>?
    actual fun toResolvedPublicKeyInfo(): IResolvedKeyInfo<KT>
}

