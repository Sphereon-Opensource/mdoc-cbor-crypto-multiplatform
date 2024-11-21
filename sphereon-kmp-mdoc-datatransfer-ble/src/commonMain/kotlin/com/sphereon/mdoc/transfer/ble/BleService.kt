package com.sphereon.mdoc.transfer.ble

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import kotlin.js.JsExport

/**
 * Marker interface for Retrieval Methods
 */
@JsExport
interface DeviceRetrievalMethod


@JsExport
data class BleRetrievalMethod(val peripheralServiceMode: Boolean, val centralClientMode: Boolean): DeviceRetrievalMethod

class BleService() {
    val scanner = Scanner {
        filters {
            match {
                services = listOf(uuidFrom(MdocReaderServiceChars.ByUuid.STATE))
            }
        }
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Events
            format = Logging.Format.Multiline
        }
    }

}
