/*
 * Copyright (c) 2021 Proton Technologies AG
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

package ui

import me.proton.core.humanverification.presentation.ui.hv3.HV3ResponseMessage
import me.proton.core.humanverification.presentation.ui.hv3.HV3ResponseMessage.VerificationMessageTypeSerializer
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.deserializeOrNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class VerificationResponseGeneralTest {

    @Test
    fun `Deserializing a VerificationResponseMessage from JSON works`() {
        val json = """
            {
                "type": "NOTIFICATION",
                "payload": {
                    "type": "message",
                    "text": "Some message to show"
                }
            }
            """
        val result = runCatching { json.deserialize<HV3ResponseMessage>() }
        assertNull(result.exceptionOrNull())
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `Can deserialize all Types`() {
        val rawSuccessType = "HUMAN_VERIFICATION_SUCCESS"
        val rawNotificationType = "NOTIFICATION"
        val rawResizeType = "RESIZE"
        val rawRandomString = "ASDA"

        val successType = rawSuccessType.deserialize(VerificationMessageTypeSerializer)
        val notificationType = rawNotificationType.deserialize(VerificationMessageTypeSerializer)
        val resizeType = rawResizeType.deserialize(VerificationMessageTypeSerializer)
        val incorrectType = rawRandomString.deserializeOrNull(VerificationMessageTypeSerializer)

        assertEquals(HV3ResponseMessage.Type.Success, successType)
        assertEquals(HV3ResponseMessage.Type.Notification, notificationType)
        assertEquals(HV3ResponseMessage.Type.Resize, resizeType)
        assertNull(incorrectType)
    }

    @Test
    fun `Can match all MessageTypes`() {
        val successType = HV3ResponseMessage.MessageType.map["success"]
        val infoType = HV3ResponseMessage.MessageType.map["info"]
        val warningType = HV3ResponseMessage.MessageType.map["warning"]
        val errorType = HV3ResponseMessage.MessageType.map["error"]
        val invalidType = HV3ResponseMessage.MessageType.map["ASDA"]

        assertEquals(HV3ResponseMessage.MessageType.Success, successType)
        assertEquals(HV3ResponseMessage.MessageType.Info, infoType)
        assertEquals(HV3ResponseMessage.MessageType.Warning, warningType)
        assertEquals(HV3ResponseMessage.MessageType.Error, errorType)
        assertNull(invalidType)
    }

    @Test
    fun `Can deserialize notification payload`() {
        val json = """
            {
                "type": "message",
                "text": "Some message to show"
            }
            """
        assertNotNull(json.deserializeOrNull<HV3ResponseMessage.Payload>())
    }

    @Test
    fun `Can deserialize success payload`() {
        val json = """
            {
                "type": "captcha",
                "token": "SOME_RANDOM_TOKEN"
            }
            """
        assertNotNull(json.deserializeOrNull<HV3ResponseMessage.Payload>())
    }

    @Test
    fun `Can deserialize resize payload`() {
        val json = """
            {
                "height": 1024
            }
            """
        assertNotNull(json.deserializeOrNull<HV3ResponseMessage.Payload>())
    }

}
