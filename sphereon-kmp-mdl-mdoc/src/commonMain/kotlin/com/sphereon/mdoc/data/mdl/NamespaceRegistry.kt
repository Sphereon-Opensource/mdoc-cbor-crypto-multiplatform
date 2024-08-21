package com.sphereon.mdoc.data.mdl

import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborString
import com.sphereon.cbor.cddl_bool
import com.sphereon.mdoc.data.DataElementCbor
import kotlin.js.JsExport

class NamespaceRegistry {

    fun test() {
        MdlDefs.family_name.identifier
    }
}

@JsExport
object Mdl {
    const val MDL_NAMESPACE: String = "org.iso.18013.5.1.mDL"
    val MDL_NAMESPACE_CBOR: CborString = CborString(MDL_NAMESPACE)


}

@JsExport
enum class MdlDefs(val definition: DataElementDef) {
    family_name(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "family_name",
            Presence.MANDATORY,
            CDDL.tstr,
            arrayOf(CDDL.tstr)
        )
    ),
    given_name(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "given_name",
            Presence.MANDATORY,
            CDDL.tstr,
            arrayOf(CDDL.tstr)
        )
    ),
    birth_date(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "birth_date",
            Presence.MANDATORY,
            CDDL.full_date,
            arrayOf(CDDL.full_date)
        )
    ),
    issue_date(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "issue_date",
            Presence.MANDATORY,
            CDDL.full_date,
            arrayOf(CDDL.full_date, CDDL.tdate)
        )
    ),
    expiry_date(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "expiry_date",
            Presence.MANDATORY,
            CDDL.full_date,
            arrayOf(CDDL.full_date, CDDL.tdate)
        )
    ),
    issuing_country(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "issuing_country",
            Presence.MANDATORY,
            CDDL.tstr,
            arrayOf(CDDL.tstr)
        )
    ),
    issuing_authority(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "issuing_authority",
            Presence.MANDATORY,
            CDDL.tstr,
            arrayOf(CDDL.tstr)
        )
    ),
    document_number(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "document_number",
            Presence.MANDATORY,
            CDDL.tstr,
            arrayOf(CDDL.tstr)
        )
    ),
    portrait(
        DataElementDef(
            Mdl.MDL_NAMESPACE,
            "portrait",
            Presence.MANDATORY,
            CDDL.tstr,
            arrayOf(CDDL.bstr)
        )
    );

    val identifier: String = this.definition.identifier
    val nameSpace = this.definition.nameSpace
    fun intentToRetain(intentToRetain: cddl_bool = false): DataElementCbor = definition.toElement(intentToRetain)


}




