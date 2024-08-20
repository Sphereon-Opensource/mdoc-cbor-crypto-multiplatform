package com.sphereon.crypto

import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.crypto.cose.CoseCurve
import com.sphereon.crypto.cose.CoseKeyOperations
import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.cose.CoseSignatureAlgorithm
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaCurve
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.JwaSignatureAlgorithm
import com.sphereon.crypto.jose.JoseKeyOperations
import kotlin.js.JsExport

@JsExport
sealed class KeyTypeMapping(private val coseKeyType: CoseKeyType, private val joseKeyType: JwaKeyType) {

    object OKP : KeyTypeMapping(CoseKeyType.OKP, JwaKeyType.OKP)
    object EC2 : KeyTypeMapping(CoseKeyType.EC2, JwaKeyType.EC)
    object RSA : KeyTypeMapping(CoseKeyType.RSA, JwaKeyType.RSA)


    object Static {
        val asList = listOf(OKP, EC2, RSA)
        fun toJose(cose: CoseKeyType) = asList.find { it.coseKeyType === cose }?.joseKeyType
            ?: throw IllegalArgumentException("coseKeyType $cose not found")

        fun toCose(jose: JwaKeyType) = asList.find { it.joseKeyType === jose }?.coseKeyType
            ?: throw IllegalArgumentException("joseKeyType $jose not found")
    }
}

@JsExport
fun CoseKeyType.toJoseKeyType() = KeyTypeMapping.Static.toJose(this)

@JsExport
fun JwaKeyType.toCoseKeyType() = KeyTypeMapping.Static.toCose(this)


@JsExport
sealed class SignatureAlgorithmMapping(
    private val coseAlgorithm: CoseAlgorithm,
    private val joseAlgorithm: JwaAlgorithm
) {
    object EdDSA : SignatureAlgorithmMapping(CoseSignatureAlgorithm.EdDSA, JwaSignatureAlgorithm.EdDSA)
    object ES256 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.ES256, JwaSignatureAlgorithm.ES256)
    object ES384 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.ES384, JwaSignatureAlgorithm.ES384)
    object ES512 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.ES512, JwaSignatureAlgorithm.ES512)
    object ES256K : SignatureAlgorithmMapping(CoseSignatureAlgorithm.ES256K, JwaSignatureAlgorithm.ES256K)
    object HS256 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.HS256, JwaSignatureAlgorithm.HS256)
    object HS384 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.HS384, JwaSignatureAlgorithm.HS384)
    object HS512 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.HS512, JwaSignatureAlgorithm.HS512)
    object PS256 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.PS256, JwaSignatureAlgorithm.PS256)
    object PS384 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.PS384, JwaSignatureAlgorithm.PS384)
    object PS512 : SignatureAlgorithmMapping(CoseSignatureAlgorithm.PS512, JwaSignatureAlgorithm.PS512)

    object Static {
        val asList = listOf(EdDSA, ES256K, ES256, ES384, ES512, HS256, HS384, HS512, PS256, PS384, PS512)
        fun toJose(cose: CoseAlgorithm) = asList.find { it.coseAlgorithm === cose }?.joseAlgorithm
            ?: throw IllegalArgumentException("coseKeyType $cose not found")

        fun toCose(jose: JwaAlgorithm) = asList.find { it.joseAlgorithm === jose }?.coseAlgorithm
            ?: throw IllegalArgumentException("joseKeyType $jose not found")
    }
}

@JsExport
fun CoseAlgorithm.toJoseSignatureAlgorithm() = SignatureAlgorithmMapping.Static.toJose(this)

@JsExport
fun JwaAlgorithm.toCoseSignatureAlgorithm() = SignatureAlgorithmMapping.Static.toCose(this)

// TODO Enc algos

@JsExport
sealed class CurveMapping(
    private val coseCurve: CoseCurve,
    private val joseCurve: JwaCurve
) {
    object P_256 : CurveMapping(CoseCurve.P_256, JwaCurve.P_256)
    object P_384 : CurveMapping(CoseCurve.P_384, JwaCurve.P_384)
    object P_521 : CurveMapping(CoseCurve.P_521, JwaCurve.P_521)
    object Secp256k1 : CurveMapping(CoseCurve.secp256k1, JwaCurve.Secp256k1)
    object Ed25519 : CurveMapping(CoseCurve.Ed25519, JwaCurve.Ed25519)
    object X25519 : CurveMapping(CoseCurve.X25519, JwaCurve.X25519)

    object Static {
        val asList = listOf(P_256, P_384, P_521, Secp256k1, Ed25519, X25519)
        fun toJose(cose: CoseCurve) = asList.find { it.coseCurve === cose }?.joseCurve
            ?: throw IllegalArgumentException("cose Curve $cose not found")

        fun toCose(jose: JwaCurve) = asList.find { it.joseCurve === jose }?.coseCurve
            ?: throw IllegalArgumentException("jose Curve $jose not found")
    }
}

@JsExport
fun CoseCurve.toJoseCurve() = CurveMapping.Static.toJose(this)

@JsExport
fun JwaCurve.toCoseCurve() = CurveMapping.Static.toCose(this)


@JsExport
sealed class KeyOperationsMapping(
    private val coseKeyOperations: CoseKeyOperations,
    private val joseKeyOperations: JoseKeyOperations
) {
    object WRAP_KEY : KeyOperationsMapping(CoseKeyOperations.WRAP_KEY, JoseKeyOperations.WRAP_KEY)
    object DERIVE_KEY : KeyOperationsMapping(CoseKeyOperations.DERIVE_KEY, JoseKeyOperations.DERIVE_KEY)
    object UNWRAP_KEY : KeyOperationsMapping(CoseKeyOperations.UNWRAP_KEY, JoseKeyOperations.UNWRAP_KEY)
    object SIGN : KeyOperationsMapping(CoseKeyOperations.SIGN, JoseKeyOperations.SIGN)
    object VERIFY : KeyOperationsMapping(CoseKeyOperations.VERIFY, JoseKeyOperations.VERIFY)
    object DECRYPT : KeyOperationsMapping(CoseKeyOperations.DECRYPT, JoseKeyOperations.DECRYPT)
    object DERIVE_BITS : KeyOperationsMapping(CoseKeyOperations.DERIVE_BITS, JoseKeyOperations.DERIVE_BITS)
    object ENCRYPT : KeyOperationsMapping(CoseKeyOperations.ENCRYPT, JoseKeyOperations.ENCRYPT)
    object MAC_CREATE : KeyOperationsMapping(CoseKeyOperations.MAC_CREATE, JoseKeyOperations.MAC_CREATE)
    object MAC_VERIFY : KeyOperationsMapping(CoseKeyOperations.MAC_VERIFY, JoseKeyOperations.MAC_VERIFY)

    object Static {
        val asList = listOf(
            WRAP_KEY,
            DERIVE_KEY,
            DERIVE_BITS,
            UNWRAP_KEY,
            SIGN,
            VERIFY,
            DECRYPT,
            ENCRYPT,
            MAC_CREATE,
            MAC_VERIFY
        )

        fun toJose(cose: CoseKeyOperations) = asList.find { it.coseKeyOperations === cose }?.joseKeyOperations
            ?: throw IllegalArgumentException("cose Curve $cose not found")

        fun toCose(jose: JoseKeyOperations) = asList.find { it.joseKeyOperations === jose }?.coseKeyOperations
            ?: throw IllegalArgumentException("jose Curve $jose not found")
    }
}

@JsExport
fun CoseKeyOperations.toJoseKeyOperations() = KeyOperationsMapping.Static.toJose(this)

@JsExport
fun JoseKeyOperations.toCoseKeyOperations() = KeyOperationsMapping.Static.toCose(this)
