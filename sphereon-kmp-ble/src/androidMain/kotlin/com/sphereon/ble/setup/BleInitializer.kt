

package setup

import android.content.Context
import androidx.startup.Initializer

object SphereonBle

class BleInitializer : Initializer<SphereonBle> {

    override fun create(context: Context): SphereonBle {
        applicationContext = context.applicationContext
        return SphereonBle
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
