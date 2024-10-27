package com.sphereon.mdoc.transfer.ble

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine

class BleService() {
    val scanner = Scanner {
        filters {
            match {
                services = listOf(uuidFrom(MdocReaderServiceCharacteristics.Uuid.State))
            }
        }
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Events
            format = Logging.Format.Multiline
        }
    }
}
