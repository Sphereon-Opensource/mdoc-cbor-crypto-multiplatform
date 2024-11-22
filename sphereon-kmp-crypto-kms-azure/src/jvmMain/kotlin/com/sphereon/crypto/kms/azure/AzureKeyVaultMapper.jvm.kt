package com.sphereon.crypto.kms.azure

import com.azure.core.credential.TokenCredential
import com.azure.core.http.policy.ExponentialBackoffOptions
import com.azure.core.util.ClientOptions
import com.azure.identity.ClientCertificateCredential
import com.azure.identity.ClientCertificateCredentialBuilder
import com.azure.identity.ClientSecretCredential
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.identity.InteractiveBrowserCredential
import com.azure.identity.InteractiveBrowserCredentialBuilder
import com.azure.identity.UsernamePasswordCredential
import com.azure.identity.UsernamePasswordCredentialBuilder
import com.azure.security.keyvault.keys.models.JsonWebKey
import com.azure.security.keyvault.keys.models.KeyCurveName
import com.azure.security.keyvault.keys.models.KeyOperation
import com.azure.security.keyvault.keys.models.KeyType
import com.azure.security.keyvault.keys.models.KeyVaultKey
import com.sphereon.crypto.SignClientException
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.jose.JoseKeyOperations
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import java.time.Duration

fun AzureKeyvaultClientConfig.toClientOptions(): ClientOptions? {
    if (headers.isNullOrEmpty()) {
        return null
    }
    return ClientOptions().setApplicationId(applicationId)
        .setHeaders(headers.map { com.azure.core.util.Header(it.name, it.values) })
}

fun ExponentialBackoffRetryOpts.toExponentialBackoffOptions(): ExponentialBackoffOptions {
    return ExponentialBackoffOptions()
        .setMaxRetries(maxRetries)
        .setBaseDelay(if (baseDelayInMS == null) null else Duration.ofMillis(baseDelayInMS))
        .setMaxDelay(if (maxDelayInMS == null) null else Duration.ofMillis(maxDelayInMS))
}

fun CredentialOpts.toTokenCredential(tenantId: String): TokenCredential {
    return when (credentialMode) {
        CredentialMode.SERVICE_CLIENT_SECRET -> secretCredentialOpts?.toClientSecretCredential(tenantId)
            ?: throw SignClientException("No client secret options provided")

        CredentialMode.SERVICE_CLIENT_CERTIFICATE -> certificateCredentialOpts?.toClientCertificateCredential(
            tenantId
        )
            ?: throw SignClientException("No client certificate options provided")

        CredentialMode.USER_INTERACTIVE_BROWSER -> interactiveBrowserCredentialOpts?.toInteractiveBrowserCredential(
            tenantId
        )
            ?: throw SignClientException("No interactive browser options provided")

        CredentialMode.USER_USERNAME_PASSWORD -> usernamePasswordCredentialOpts?.toUsernamePasswordCredential(
            tenantId
        )
            ?: throw SignClientException("No username password options provided")
    }
}

fun SecretCredentialOpts.toClientSecretCredential(tenantId: String): ClientSecretCredential {
    return ClientSecretCredentialBuilder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .tenantId(tenantId)
        .build()
}

fun CertificateCredentialOpts.toClientCertificateCredential(tenantId: String): ClientCertificateCredential {
    return ClientCertificateCredentialBuilder()
        .clientId(clientId)
        .pemCertificate(pemCertificatePath)
        .tenantId(tenantId)
        .build()
}

fun UsernamePasswordCredentialOpts.toUsernamePasswordCredential(tenantId: String): UsernamePasswordCredential {
    return UsernamePasswordCredentialBuilder()
        .clientId(clientId)
        .username(userName)
        .password(password)
        .tenantId(tenantId)
        .build()
}

fun InteractiveBrowserCredentialOpts.toInteractiveBrowserCredential(tenantId: String): InteractiveBrowserCredential {
    return InteractiveBrowserCredentialBuilder()
        .clientId(clientId)
        .redirectUrl(redirectUrl)
        .tenantId(tenantId)
        .build()
}

fun ByteArray.toBase64UrlString(): String =
    java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(this)

fun KeyOperation.toKeyOperations(): KeyOperations {
    return when (this) {
        KeyOperation.ENCRYPT -> KeyOperations.ENCRYPT
        KeyOperation.DECRYPT -> KeyOperations.DECRYPT
        KeyOperation.SIGN -> KeyOperations.SIGN
        KeyOperation.VERIFY -> KeyOperations.VERIFY
        KeyOperation.WRAP_KEY -> KeyOperations.WRAP_KEY
        KeyOperation.UNWRAP_KEY -> KeyOperations.UNWRAP_KEY
        else -> {
            throw SignClientException("Unsupported key operation for Azure Key Vault")
        }
    }
}

fun KeyVaultKey.toJwk(): Jwk {
    val jsonWebKey: JsonWebKey = this.key

    return Jwk.Builder()
        .withKid(jsonWebKey.id) // Key ID
        .withKty(jsonWebKey.keyType?.toString()?.let { JwaKeyType.Static.fromValue(it) }) // Key type
        .withAlg(mapJwkToAlgorithm(jsonWebKey)) // Algorithm
        .withX(jsonWebKey.x?.toBase64UrlString())
        .withY(jsonWebKey.y?.toBase64UrlString())
        .withKeyOps(jsonWebKey.keyOps?.map { JoseKeyOperations.Static.fromValue(it.toKeyOperations().jose.value) }
            ?.toTypedArray())
        .build()
}

fun KeyOperations.toAzureKeyOperation(): KeyOperation {
    return when (this) {
        KeyOperations.ENCRYPT -> KeyOperation.ENCRYPT
        KeyOperations.DECRYPT -> KeyOperation.DECRYPT
        KeyOperations.SIGN -> KeyOperation.SIGN
        KeyOperations.UNWRAP_KEY -> KeyOperation.UNWRAP_KEY
        KeyOperations.VERIFY -> KeyOperation.VERIFY
        KeyOperations.WRAP_KEY -> KeyOperation.WRAP_KEY
        KeyOperations.DERIVE_BITS -> throw SignClientException("Azure Key Vault does not support DERIVE_BITS operation")
        KeyOperations.DERIVE_KEY -> throw SignClientException("Azure Key Vault does not support DERIVE_KEY operation")
        KeyOperations.MAC_CREATE -> throw SignClientException("Azure Key Vault does not support MAC_CREATE operation")
        KeyOperations.MAC_VERIFY -> throw SignClientException("Azure Key Vault does not support MAC_VERIFY operation")
        else -> throw SignClientException("Unsupported key operation: $this")
    }
}

fun mapJwkToAlgorithm(jwk: JsonWebKey): JwaAlgorithm? {
    return when (jwk.keyType) {
        KeyType.EC -> when {
            jwk.curveName.toString() == "P-256" -> JwaAlgorithm.ES256
            jwk.curveName.toString() == "P-384" -> JwaAlgorithm.ES384
            jwk.curveName.toString() == "P-521" -> JwaAlgorithm.ES512
            else -> null
        }
        KeyType.RSA -> JwaAlgorithm.RS256
        else -> null
    }
}

fun Curve.toAzureKeyCurveName(): KeyCurveName {
    return when (this) {
        Curve.P_256 -> KeyCurveName.P_256
        Curve.P_384 -> KeyCurveName.P_384
        Curve.P_521 -> KeyCurveName.P_521
        Curve.Ed25519 -> throw SignClientException("Curve Ed25519 is not supported")
        Curve.X25519 -> throw SignClientException("Curve X25519 is not supported")
        else -> throw SignClientException("Unsupported curve: $this")
    }
}

fun KeyVaultKey.toSignatureAlgorithm(): com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm {
    return when (this.key.keyType) {
        KeyType.EC -> {
            when (this.key.curveName) {
                KeyCurveName.P_256 -> com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.ES256
                KeyCurveName.P_384 -> com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.ES384
                KeyCurveName.P_521 -> com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.ES512
                else -> throw SignClientException("Unsupported curve: ${this.key.curveName}")
            }
        }
        else -> throw SignClientException("Unsupported key type: ${this.key.keyType}")
    }
}
