package com.sphereon.mdoc.data.server

import com.sphereon.cbor.cddl_tstr
import com.sphereon.mdoc.data.DeviceResponseDocumentError
import com.sphereon.mdoc.data.JWT

/**
 * 8.3.2.2.2.2 Server retrieval mdoc response
 */
data class ServerResponse(
    /**
     * version is the version for the ServerResponse structure: in the current version of this document its value
     * shall be “1.0”.
     */
    val version: cddl_tstr,

    /**
     * documents contains an array of all returned documents. Each document shall be returned as a JSON
     * Web Token (JWT), as specified in RFC 7519. The claims conveyed by each JWT are in com.sphereon.mdoc.dataelements.data.JWTClaimsSet.
     * Each JWT is protected using a JSON Web Signature (JWS) as specified in 9.2.2.
     */
    val documents: Array<JWT>?,

    /**
     * documentErrors can contain error codes for documents that are not returned.
     */
    // fixme: Does this need an array around the map?
    val documentErrors: Array<DeviceResponseDocumentError>


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerResponse) return false

        if (version != other.version) return false
        if (documents != null) {
            if (other.documents == null) return false
            if (!documents.contentEquals(other.documents)) return false
        } else if (other.documents != null) return false
        if (!documentErrors.contentEquals(other.documentErrors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + (documents?.contentHashCode() ?: 0)
        result = 31 * result + documentErrors.contentHashCode()
        return result
    }
}
