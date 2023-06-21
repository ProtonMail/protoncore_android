/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.notification.presentation.internal

import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.entity.NotificationPayload
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

internal class GetNotificationTagTest {
    private lateinit var getNotificationTag: GetNotificationTag

    @Before
    fun beforeEveryTest() {
        getNotificationTag = GetNotificationTag()
    }

    @Test
    fun `get notification tag works correctly`() {
        val result = getNotificationTag(
            Notification(
                notificationId = NotificationId("notification_id"),
                userId = UserId("user_id"),
                time = 1,
                type = "notification_type",
                payload = NotificationPayload.Unencrypted(
                    raw = "",
                    title = "title",
                    subtitle = "subtitle",
                    body = "body"
                )
            )
        )
        assertEquals("user_id", result)
    }
}
