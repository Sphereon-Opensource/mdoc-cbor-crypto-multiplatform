import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.crypto.HashAlgorithm
import com.sphereon.crypto.hash
import com.sphereon.mdoc.tx.device.OID4VPHandoverCbor

/**
 * ISO 18013-7
 */
fun clientIdToHash(clientId: String, generatedNonce: String): ByteArray {
    val clientIdToHash = Cbor.encode(CborArray.Static.builder<Any>().addString(clientId).addString(generatedNonce).end().build())
    return hash(clientIdToHash, HashAlgorithm.SHA256)
}

fun responseUriToHash(responseUri: String, generatedNonce: String): ByteArray {
    val responseUriToHash = Cbor.encode(CborArray.Static.builder<Any>().addString(responseUri).addString(generatedNonce).end().build())
    return hash(responseUriToHash, HashAlgorithm.SHA256)
}


fun oid4vpHandoverFromClientIdAndResponseUri(clientId: String, responseUri: String, nonce: String): OID4VPHandoverCbor {
    return OID4VPHandoverCbor(
        clientIdHash = clientIdToHash(clientId, nonce).toCborByteString(),
        responseUriHash = responseUriToHash(responseUri, nonce).toCborByteString(),
        nonce = nonce.toCborString()
    )
}
