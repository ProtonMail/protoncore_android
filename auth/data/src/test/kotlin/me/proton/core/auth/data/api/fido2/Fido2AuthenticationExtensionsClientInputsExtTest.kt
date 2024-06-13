package me.proton.core.auth.data.api.fido2

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationExtensionsClientInputs
import me.proton.core.auth.fido.domain.ext.toJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}
