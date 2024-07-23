package com.sphereon.mdoc.data

interface JWTClaimsSet {
    val docType: DocType
    val namespaces: NameSpacesResponse
    val errors: JWTDocumentErrors

}
