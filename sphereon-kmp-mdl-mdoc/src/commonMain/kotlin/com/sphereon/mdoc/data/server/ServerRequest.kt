package com.sphereon.mdoc.data.server

import com.sphereon.cbor.cddl_tstr

/**
 * 8.3.2.2.2.1 Server retrieval mdoc request
 * The server retrieval mdoc request shall be JSON encoded and formatted as follows:
 */

data class ServerRequest (
    /**
     * version is the version for the ServerRequest structure: in the current version of this document its value
     * shall be “1.0”.
     */
    val version: cddl_tstr = "1.0",

    /**
     * token shall contain the server retrieval token (see 8.2.1.2) which identifies the mdoc.
     */
    val token: cddl_tstr,

    /**
     * docRequests contains an array of all requested documents.
     */
    val docRequests: Array<ServerItemsRequest>
)
