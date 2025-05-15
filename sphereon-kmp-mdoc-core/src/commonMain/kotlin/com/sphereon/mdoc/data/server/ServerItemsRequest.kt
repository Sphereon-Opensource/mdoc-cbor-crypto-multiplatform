package com.sphereon.mdoc.data.server

import com.sphereon.mdoc.data.DocType
import com.sphereon.mdoc.data.RequestInfo

data class ServerItemsRequest(
    /**
     * docType is the requested document type
     */
    val docType: DocType,

    /**
     * NameSpaces contains the requested data elements and the namespace they belong to.
     */
    val nameSpaces: MutableMap<String, MutableMap<String, Boolean>>,

    /**
     * requestInfo may be used by the mdoc reader to provide additional information. This document does
     * not define any key-value pairs for use in requestInfo. An IA infrastructure shall ignore any key-value
     * pairs that it is not able to interpret.
     */
    val requestInfo: RequestInfo?
)
