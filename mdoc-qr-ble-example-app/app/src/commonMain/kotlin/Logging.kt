package com.sphereon.mdoc.example.app

import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.ConstantTagGenerator
import com.juul.khronicle.Log

fun configureLogging() {
    Log.tagGenerator = ConstantTagGenerator(tag = "SensorTag")
    Log.dispatcher.install(ConsoleLogger)
}
