package com.sphereon.mdoc.transfer.ble

class MdocServiceCharacteristics {
    class Command {
        val End = "0x02"
    }
    class Uuid {
        public val State = "00000001-A123-48CE-896B-4C76973373E6"
        val Client2Server = "00000002-A123-48CE-896B-4C76973373E6"
        val Server2Client = "00000003-A123-48CE-896B-4C76973373E6"
    }
}

object MdocReaderServiceCharacteristics {
    object Command {
        val Start = "0x01"
        val End = "0x02"
    }
    object Uuid {
        val State = "00000005-A123-48CE-896B-4C76973373E6"
        val Client2Server = "00000006-A123-48CE-896B-4C76973373E6"
        val Server2Client = "00000007-A123-48CE-896B-4C76973373E6"
        val Ident = "00000008-A123-48CE-896B-4C76973373E6"
    }
}
