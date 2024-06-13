package me.proton.core.auth.data.api.fido2

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalUnsignedTypes::class)
class PublicKeyCredentialRequestOptionsResponseKtTest {
    @Test
    fun `request options response with null extensions to client inputs`() {
        val result = PublicKeyCredentialRequestOptionsResponse(
            challenge = ubyteArrayOf(1U, 2U, 3U),
            extensions = null
        ).toFido2AuthenticationExtensionsClientInputs()

        assertNull(result.appId)
        assertNull(result.thirdPartyPayment)
        assertNull(result.uvm)
    }

    @Test
    fun `request options response with extensions to client inputs`() {
        val result = PublicKeyCredentialRequestOptionsResponse(
            challenge = ubyteArrayOf(1U, 2U, 3U),
            extensions = JsonObject(
                mapOf(
                    "appid" to JsonPrimitive("appId"),
                    "thirdPartyPayment" to JsonPrimitive(true),
                    "uvm" to JsonPrimitive(true)
                )
            )
        ).toFido2AuthenticationExtensionsClientInputs()

        assertEquals("appId", result.appId)
        assertEquals(true, result.thirdPartyPayment)
        assertEquals(true, result.uvm)
    }
}
