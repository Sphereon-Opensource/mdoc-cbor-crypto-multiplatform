package com.sphereon.cbor

import com.sphereon.mdoc.data.mdl.DrivingPrivilegeCbor
import com.sphereon.mdoc.data.mdl.CborDrivingPrivilegesBuilder
import com.sphereon.mdoc.data.mdl.DrivingPrivilegesCbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.expect

class DrivingPrivilegeTest {

    val isoTestVector =
        "82a37576656869636c655f63617465676f72795f636f646561416a69737375655f6461746 5d903ec6a323031382d30382d30396b6578706972795f64617465d903ec6a323032342d31 302d3230a37576656869636c655f63617465676f72795f636f646561426a69737375655f6 4617465d903ec6a323031372d30322d32336b6578706972795f64617465d903ec6a323032 342d31302d3230".replace(
            " ",
            ""
        )

    @OptIn( ExperimentalStdlibApi::class)
    @Test
    fun shouldEncodeDrivingPrivileges() {
        val privileges: DrivingPrivilegesCbor = CborDrivingPrivilegesBuilder()
            .newPrivilege()
            .withVehicleCategoryCode("A")
            .withIssueDate("2018-08-09")
            .withExpiryDate("2024-10-20")
            .newPrivilege()
            .withVehicleCategoryCode("B")
            .withIssueDate("2017-02-23")
            .withExpiryDate("2024-10-20")
            .buildPrivileges()


        val bytes = privileges.cborEncode()
        println("Encoded object to hex:")
        println(bytes.toHexString())

        expect(isoTestVector) { bytes.toHexString() }

        val result: DrivingPrivilegesCbor = DrivingPrivilegesCbor.cborDecode(bytes)
//        val result: CborArray<*> = Cbor.decode(bytes)
        println("Decoded object:")
        println(result)
        assertEquals(result, privileges)

    }
}
