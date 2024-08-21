import kotlin.js.JsExport
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.Uuid.Companion.random

@OptIn(ExperimentalUuidApi::class)
@JsExport
object Uuid {
    fun v4(): Uuid = random()
    fun v4String(): String = v4().toString()
}
