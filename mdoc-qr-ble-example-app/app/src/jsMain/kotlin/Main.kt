import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.sphereon.mdoc.example.app.App
import com.sphereon.mdoc.example.app.configureLogging
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureLogging()
    onWasmReady {
        CanvasBasedWindow("SensorTag") {
            App()
        }
    }
}
