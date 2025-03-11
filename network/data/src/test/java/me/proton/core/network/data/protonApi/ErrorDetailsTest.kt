/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.network.data.protonApi

import kotlinx.serialization.json.decodeFromJsonElement
import me.proton.core.util.kotlin.ProtonCoreConfig
import me.proton.core.util.kotlin.deserialize
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests the deserialization of the [ApiResult.Error.ErrorDetails].
 *
 * @author Dino Kadrikj.
 */
class ErrorDetailsTest {
    private val testHumanVerificationResponse =
        """
            {
                "Code": 9001,
                "Error": "Human verification required",
                "ErrorDescription": "",
                "Details": {
                    "HumanVerificationMethods" : [
                        "sms",
                        "email",
                        "payment",
                        "invite",
                        "coupon"
                    ],
                    "HumanVerificationToken": "test"
                }
            }
        """.trimIndent()

    private val testNoHumanVerificationResponse =
        """
            {
                "Code": 9002,
                "Error": "Some other error without details",
                "ErrorDescription": ""
            }
        """.trimIndent()

    @Test
    fun `test human verification required response`() {
        val result = testHumanVerificationResponse.deserialize(ProtonErrorData.serializer())
        assertNotNull(result)
        val resultDetails = result.details
        assertNotNull(resultDetails)
        val details = ProtonCoreConfig.defaultJson.decodeFromJsonElement<Details>(resultDetails)
        assertEquals(5, details.verificationMethods?.size)
    }

    @Test
    fun `test human verification required response proton data`() {
        val result =
            testHumanVerificationResponse.deserialize(ProtonErrorData.serializer()).apiResultData
        assertNotNull(result)
        assertEquals(5, result.humanVerification!!.verificationMethods.size)
    }

    @Test
    fun `test other error response`() {
        val result = testNoHumanVerificationResponse.deserialize(ProtonErrorData.serializer())
        assertNotNull(result)
        assertNull(result.details)
    }

    @Test
    fun `test other error response proton data`() {
        val result = testNoHumanVerificationResponse.deserialize(ProtonErrorData.serializer())
        assertNotNull(result)
        val protonData = result.apiResultData
        assertNotNull(protonData)
        assertNull(protonData.humanVerification)
    }
}
