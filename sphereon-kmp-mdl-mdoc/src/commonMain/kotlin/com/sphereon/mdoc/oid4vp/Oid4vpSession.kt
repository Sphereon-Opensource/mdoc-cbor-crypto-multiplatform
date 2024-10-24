import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.hash
import com.sphereon.kmp.Uuid
import com.sphereon.mdoc.tx.device.OID4VPHandoverCbor

/**
 * ISO 18013-7
 */
fun clientIdToHash(clientId: String, generatedNonce: String): ByteArray {
    val clientIdToHash = Cbor.encode(CborArray.Static.builder<Any>().addString(clientId).addString(generatedNonce).end().build())
    return hash(clientIdToHash, DigestAlg.SHA256)
}

fun responseUriToHash(responseUri: String, generatedNonce: String): ByteArray {
    val responseUriToHash = Cbor.encode(CborArray.Static.builder<Any>().addString(responseUri).addString(generatedNonce).end().build())
    return hash(responseUriToHash, DigestAlg.SHA256)
}


fun oid4vpHandoverFromClientIdAndResponseUri(clientId: String, responseUri: String, mdocGeneratedNonce: String = Uuid.v4String(), authorizationRequestNonce: String): OID4VPHandoverCbor {
    return OID4VPHandoverCbor(
        clientIdHash = clientIdToHash(clientId, mdocGeneratedNonce).toCborByteString(),
        responseUriHash = responseUriToHash(responseUri, mdocGeneratedNonce).toCborByteString(),
        nonce = authorizationRequestNonce.toCborString()
    )
}


fun parseConstraintFieldPath(path: String) {
    val (nameSpace,elementIdentifier) = assertedPathEntry(path)

}

fun assertedPathEntry(pathEntry: String): Pair<String, String> {
    // WARNING: Do not remove the backslashes that seem redundant near the ] bracket. If you do JS will fail!
    val match = Regex("^\\\$\\['([\\w.]+)'\\]\\['(\\w+)'\\]$").matchEntire(pathEntry)
    val results = match?.groupValues
    if (results.isNullOrEmpty() || results.size != 3) {
        throw IllegalArgumentException("Path entry in the OID4VP constraint field is not valid: $pathEntry")
    }
    return Pair(results[1], results[2])
}
