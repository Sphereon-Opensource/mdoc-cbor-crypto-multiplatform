import com.sphereon.cbor.JsonView2
import com.sphereon.cbor.cose.CoseKeyJson
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.js.JsExport

val jsonSerializer = JsonSupport.serializer

@JsExport
object JsonSupport {
    private val serializersModule = SerializersModule {

        // Ensures we can do polymorphic serialization of the both Key and Private Key entries using the IKeyEntry interface
        polymorphic(JsonView2::class) {
            subclass(CoseKeyJson::class)
            subclass(IssuerSignedItemJson::class)
        }
    }
    val serializer = Json { serializersModule = this@JsonSupport.serializersModule }
}
