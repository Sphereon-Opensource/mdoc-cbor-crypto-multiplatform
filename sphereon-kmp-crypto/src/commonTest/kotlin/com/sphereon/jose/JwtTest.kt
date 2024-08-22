package com.sphereon.jose

import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.Jwt
import com.sphereon.crypto.jose.JwtHeader
import com.sphereon.crypto.jose.JwtPayload
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class JWTTest {

    @Test
    fun shouldMakeWorkingWithJwtFunctionsPossible(): TestResult = runTest {
        val header = JwtHeader()
        header.putString("kid", "kid-example")
        header.epk = Jwk(alg = JwaAlgorithm.A256GCMKW, kty = JwaKeyType.EC)

        val payload = JwtPayload()
        payload.iss = "http://test"
        assertEquals("http://test", payload.iss)

        payload.put("test", JsonPrimitive(6))
        assertEquals(6, payload["test"]?.jsonPrimitive?.int)


        val jwt = Jwt(header, payload)
        assertEquals(jwt_payload, jwt.toJsonString())
        println(jwt.toJsonString())
    }


    val jwt_payload = """{"header":{"kid":"kid-example","epk":{"alg":"A256GCMKW","kty":"EC"}},"payload":{"iss":"http://test","test":6}}"""

}
