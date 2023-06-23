/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.notification.data.local

import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.data.local.db.NotificationEntity
import me.proton.core.notification.data.local.db.toNotification
import me.proton.core.notification.data.local.db.toNotificationEntity
import me.proton.core.notification.data.remote.response.NotificationResponse
import me.proton.core.notification.data.remote.response.toNotification
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.util.kotlin.deserialize
import org.junit.Test
import kotlin.test.assertEquals

class NotificationPayloadTest {

    @Test
    fun deserializeToNotificationWithPayload() = runTest {
        val userId = UserId("userId")
        val input =
            """ {
                "ID": "notificationId",
                "Type": "type",
                "Time": "1",
                "Payload": {
                    "Title": "title",
                    "Subtitle": "subtitle",
                    "Body": "body"
                }
            }
            """.trimIndent()

        val notificationResponse: NotificationResponse = input.deserialize()
        val notification: Notification = notificationResponse.toNotification(userId)

        assertEquals(
            expected = Notification(
                notificationId = NotificationId("notificationId"),
                userId = userId,
                time = 1,
                type = "type",
                payload = NotificationPayload.Unencrypted(
                    raw = "{\"Title\":\"title\",\"Subtitle\":\"subtitle\",\"Body\":\"body\"}",
                    title = "title",
                    subtitle = "subtitle",
                    body = "body"
                )
            ),
            actual = notification
        )
    }

    @Test
    fun deserializeToNotificationWithEmptyPayload() = runTest {
        val userId = UserId("userId")
        val input =
            """ {
                "ID": "notificationId",
                "Type": "type",
                "Time": "1",
                "Payload": { }
            }
            """.trimIndent()

        val notificationResponse: NotificationResponse = input.deserialize()
        val notification: Notification = notificationResponse.toNotification(userId)

        assertEquals(
            expected = Notification(
                notificationId = NotificationId("notificationId"),
                userId = userId,
                time = 1,
                type = "type",
                payload = NotificationPayload.Unencrypted(
                    raw = "{}",
                    title = null,
                    subtitle = null,
                    body = null
                )
            ),
            actual = notification
        )
    }

    @Test
    fun deserializeToNotificationWithUnknownPayload() = runTest {
        val userId = UserId("userId")
        val input =
            """ {
                "ID": "notificationId",
                "Type": "type",
                "Time": "1",
                "Payload": "encrypted"
            }
            """.trimIndent()

        val notificationResponse: NotificationResponse = input.deserialize()
        val notification: Notification = notificationResponse.toNotification(userId)

        assertEquals(
            expected = Notification(
                notificationId = NotificationId("notificationId"),
                userId = userId,
                time = 1,
                type = "type",
                payload = NotificationPayload.Unknown(
                    raw = "\"encrypted\"",
                )
            ),
            actual = notification
        )
    }

    @Test
    fun notificationToEntityWithPayload() = runTest {
        val userId = UserId("userId")
        val input = Notification(
            notificationId = NotificationId("notificationId"),
            userId = userId,
            time = 1,
            type = "type",
            payload = NotificationPayload.Unencrypted(
                raw = "{\"Title\":\"title\",\"Subtitle\":\"subtitle\",\"Body\":\"body\"}",
                title = "title",
                subtitle = "subtitle",
                body = "body"
            )
        )

        val entity: NotificationEntity = input.toNotificationEntity()
        val output = entity.toNotification()
        assertEquals(
            expected = input,
            actual = output
        )
    }

    @Test
    fun notificationToEntityWithEmptyPayload() = runTest {
        val userId = UserId("userId")
        val input = Notification(
            notificationId = NotificationId("notificationId"),
            userId = userId,
            time = 1,
            type = "type",
            payload = NotificationPayload.Unencrypted(
                raw = "{}",
                title = null,
                subtitle = null,
                body = null
            )
        )

        val entity: NotificationEntity = input.toNotificationEntity()
        val output = entity.toNotification()
        assertEquals(
            expected = input,
            actual = output
        )
    }
}
