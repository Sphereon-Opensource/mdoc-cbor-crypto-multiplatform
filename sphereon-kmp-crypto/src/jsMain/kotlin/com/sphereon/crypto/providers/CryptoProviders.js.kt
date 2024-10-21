package com.sphereon.crypto.providers

import com.sphereon.crypto.ICoseCryptoCallbackJS
import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IVerifySignatureResult
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ToBeSignedCbor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlin.js.Promise


private const val COSE_CRYPTO_ADAPTER_JS = "CoseCryptoAdapterJS"

@JsExport
class CoseCryptoProviderToCallbackAdapterJS(providers: Array<ICryptoProvider>) : ICoseCryptoCallbackJS {
    private val delegate = CoseCryptoProviderToCallbackAdapter(providers)

    override fun sign(input: ToBeSignedCbor): Promise<ByteArray> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_ADAPTER_JS)).async { delegate.sign(input) }.asPromise()
    }

    override fun verify1(input: CoseSign1Cbor<*>, keyInfo: IKeyInfo<ICoseKeyCbor>): Promise<IVerifySignatureResult<ICoseKeyCbor>> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_ADAPTER_JS)).async { delegate.verify1(input, keyInfo) }.asPromise()
    }

    override fun resolvePublicKey(keyInfo: IKeyInfo<*>): Promise<IKey> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_ADAPTER_JS)).async { delegate.resolvePublicKey(keyInfo) }.asPromise()
    }

}

