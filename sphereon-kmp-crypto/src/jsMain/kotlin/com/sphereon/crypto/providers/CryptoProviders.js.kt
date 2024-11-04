package com.sphereon.crypto.providers

import com.sphereon.crypto.ICoseCryptoCallbackJS
import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ToBeSignedCbor
import com.sphereon.crypto.generic.IVerifySignatureResult
import com.sphereon.crypto.kms.IKeyManagerService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlin.js.Promise


private const val COSE_CRYPTO_ADAPTER_JS = "CoseCryptoAdapterJS"

@JsExport
class CoseCryptoProviderToCallbackAdapterJS(keyManager: IKeyManagerService) : ICoseCryptoCallbackJS {
    private val delegate = CoseCryptoProviderToCallbackAdapter(keyManager)

    override fun sign(input: ToBeSignedCbor): Promise<ByteArray> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_ADAPTER_JS)).async { delegate.sign(input) }.asPromise()
    }

    override fun verify1(input: CoseSign1Cbor<*>, keyInfo: IKeyInfo<ICoseKeyCbor>): Promise<IVerifySignatureResult<ICoseKeyCbor>> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_ADAPTER_JS)).async { delegate.verify1(input, keyInfo) }.asPromise()
    }

    override fun <KT : IKey> resolvePublicKeyAsync(keyInfo: IKeyInfo<KT>): Promise<IResolvedKeyInfo<KT>> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_ADAPTER_JS)).async { delegate.resolvePublicKeyAsync(keyInfo) }.asPromise()
    }

}

