package com.sphereon.jose

import com.sphereon.crypto.cose.CoseCurve
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.jose.JwaCurve
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.cborToJwk
import com.sphereon.crypto.jose.jsonToJwk
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.encodeTo
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private const val HEX_ENCODED_CBOR_KEY =
    "a401022001215820bb11cddd6e9e869d1559729a30d89ed49f3631524215961271abbbe28d7b731f225820dbd639132e2ee561965b830530a6a024f1098888f313550515921184c86acac3"

class JWKTest {

    @Test
    fun shouldConvertECJWKToCoseKey(): TestResult = runTest {
        val jwk = Jwk(
            kty = JwaKeyType.EC,
            crv = JwaCurve.P_256,
            x = "uxHN3W6ehp0VWXKaMNie1J82MVJCFZYScau74o17cx8",
            y = "29Y5Ey4u5WGWW4MFMKagJPEJiIjzE1UFFZIRhMhqysM"
        )
        val coseKey = jwk.jwkToCoseKeyCbor()

        assertEquals(CoseKeyType.EC2.toCbor(), coseKey.kty)
        assertEquals(CoseCurve.P_256.toCbor(), coseKey.crv)
        assertContentEquals(
            byteArrayOf(
                -69,
                17,
                -51,
                -35,
                110,
                -98,
                -122,
                -99,
                21,
                89,
                114,
                -102,
                48,
                -40,
                -98,
                -44,
                -97,
                54,
                49,
                82,
                66,
                21,
                -106,
                18,
                113,
                -85,
                -69,
                -30,
                -115,
                123,
                115,
                31
            ), coseKey.x!!.value
        )
        assertEquals("bb11cddd6e9e869d1559729a30d89ed49f3631524215961271abbbe28d7b731f", coseKey.x!!.encodeTo(Encoding.HEX))
        assertEquals(jwk.x, coseKey.x!!.encodeTo(Encoding.BASE64URL))

        assertContentEquals(
            byteArrayOf(
                -37,
                -42,
                57,
                19,
                46,
                46,
                -27,
                97,
                -106,
                91,
                -125,
                5,
                48,
                -90,
                -96,
                36,
                -15,
                9,
                -120,
                -120,
                -13,
                19,
                85,
                5,
                21,
                -110,
                17,
                -124,
                -56,
                106,
                -54,
                -61
            ), coseKey.y!!.value
        )
        assertEquals("dbd639132e2ee561965b830530a6a024f1098888f313550515921184c86acac3", coseKey.y!!.encodeTo(Encoding.HEX))
        assertEquals(jwk.y, coseKey.y!!.encodeTo(Encoding.BASE64URL))
        assertEquals(
            HEX_ENCODED_CBOR_KEY,
            coseKey.toCbor().cborEncode().encodeTo(Encoding.HEX)
        )
    }


    @Test
    fun shouldConvertECJWKToCoseKeyAndBack(): TestResult = runTest {
        val jwk = Jwk(
            kty = JwaKeyType.EC,
            crv = JwaCurve.P_256,
            x = "uxHN3W6ehp0VWXKaMNie1J82MVJCFZYScau74o17cx8",
            y = "29Y5Ey4u5WGWW4MFMKagJPEJiIjzE1UFFZIRhMhqysM"
        )
        assertEquals(jwk, jwk.jwkToCoseKeyCbor().cborToJwk())
    }

    @Test
    fun shouldConvertECCoseKeyToJWKandBack(): TestResult = runTest {
        val cborKey = CoseKeyCbor.cborDecode(HEX_ENCODED_CBOR_KEY.decodeFrom(Encoding.HEX))
        assertEquals(cborKey, cborKey.cborToJwk().jwkToCoseKeyCbor())
    }


    @Test
    fun shouldHaveSomeFunWithConversion(): TestResult = runTest {
        val cborKey = CoseKeyCbor.cborDecode(HEX_ENCODED_CBOR_KEY.decodeFrom(Encoding.HEX))
        assertEquals(cborKey, cborKey.cborToJwk().jwkToCoseKeyJson().jsonToJwk().jwkToCoseKeyCbor().cborToJwk().jwkToCoseKeyCbor())
    }

}
