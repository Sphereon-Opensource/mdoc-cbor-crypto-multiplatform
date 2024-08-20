import com.sphereon.cbor.CDDL
import com.sphereon.cbor.cddl_bool
import com.sphereon.cbor.toCborBool
import com.sphereon.cbor.toCborString
import com.sphereon.mdoc.data.DataElementCbor
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
data class DataElementDef<Type : Any>(
    val nameSpace: String,
    val identifier: String,
    val presence: Presence,
    val cddl: CDDL,
    val cddls: Array<CDDL>
) {
    @JsName("toElement")
    fun toElement(intentToRetain: cddl_bool = false): DataElementCbor {
        return DataElementCbor(identifier.toCborString(), intentToRetain.toCborBool())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataElementDef<*>) return false

        if (nameSpace != other.nameSpace) return false
        if (identifier != other.identifier) return false
        if (presence != other.presence) return false
        if (cddl != other.cddl) return false
        if (!cddls.contentEquals(other.cddls)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameSpace.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + presence.hashCode()
        result = 31 * result + cddl.hashCode()
        result = 31 * result + cddls.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "DataElementDef(nameSpace='$nameSpace', identifier='$identifier', presence=$presence, cddl=$cddl, cddls=${cddls.contentToString()})"
    }


}

@JsExport
enum class Presence(val value: String, val mandatory: Boolean) {
    MANDATORY("MANDATORY", true),
    OPTIONAL("OPTIONAL", false);
}
