package com.sphereon.mdoc.transfer.ble

object MdocHolderServiceChars {
    object Command {
        val END = "0x02"
    }
     object ByUuid {
        val STATE = "00000001-A123-48CE-896B-4C76973373E6"
        val CLIENT_2_SERVER = "00000002-A123-48CE-896B-4C76973373E6"
        val SERVER_2_CLIENT = "00000003-A123-48CE-896B-4C76973373E6"
    }
}

object MdocReaderServiceChars {
    object Command {
        val START = "0x01"
        val END = "0x02"
    }
    object ByUuid {
        val STATE = "00000005-A123-48CE-896B-4C76973373E6"
        val CLIENT_2_SERVER = "00000006-A123-48CE-896B-4C76973373E6"
        val SERVER_2_CLIENT = "00000007-A123-48CE-896B-4C76973373E6"
        val IDENT = "00000008-A123-48CE-896B-4C76973373E6"
    }
}
