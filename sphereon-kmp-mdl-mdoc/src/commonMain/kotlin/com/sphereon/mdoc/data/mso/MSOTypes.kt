package com.sphereon.mdoc.data.mso

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.mdoc.data.DataElementIdentifier
import com.sphereon.mdoc.data.NameSpace
import com.sphereon.cbor.NumberLabel

typealias DigestIDs = CborUInt
typealias ValueDigests = CborMap<NameSpace, DigestIDs>
typealias KeyInfoCbor = CborMap<NumberLabel, AnyCborItem>
typealias AuthorizedNameSpaces = CborArray<NameSpace>
typealias AuthorizedDataElements = CborMap<NameSpace, DataElementsArray>
typealias DataElementsArray = CborArray<DataElementIdentifier>
