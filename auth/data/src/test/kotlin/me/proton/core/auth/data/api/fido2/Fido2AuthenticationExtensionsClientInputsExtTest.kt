package me.proton.core.auth.data.api.fido2

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationExtensionsClientInputs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalUnsignedTypes::class)
class Fido2AuthenticationExtensionsClientInputsExtTest {
    @Test
    fun `null client inputs to json`() {
        val json = Fido2AuthenticationExtensionsClientInputs(
            appId = null,
            thirdPartyPayment = null,
            uvm = null
        ).toJson()

        assertNull(json)
    }

    @Test
    fun `client inputs to json`() {
        val json = Fido2AuthenticationExtensionsClientInputs(
            appId = "appId",
            thirdPartyPayment = true,
            uvm = true
        ).toJson()

        assertEquals(
            JsonObject(
                mapOf(
                    "appid" to JsonPrimitive("appId"),
                    "thirdPartyPayment" to JsonPrimitive(true),
                    "uvm" to JsonPrimitive(true)
                )
            ),
            json
        )
    }

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
