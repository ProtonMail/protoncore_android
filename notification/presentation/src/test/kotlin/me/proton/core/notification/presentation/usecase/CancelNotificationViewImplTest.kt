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

package me.proton.core.notification.presentation.usecase

import android.app.NotificationManager
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.notification.presentation.internal.GetNotificationId
import me.proton.core.notification.presentation.internal.GetNotificationTag
import kotlin.test.BeforeTest
import kotlin.test.Test

class CancelNotificationViewImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var getNotificationId: GetNotificationId

    @MockK
    private lateinit var getNotificationTag: GetNotificationTag

    private lateinit var tested: CancelNotificationViewImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = CancelNotificationViewImpl(context, getNotificationId, getNotificationTag)
    }

    @Test
    fun cancelNotification() {
        // GIVEN
        val notificationManager = mockk<NotificationManager> {
            justRun { cancel(any(), any()) }
        }

        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.packageName } returns "package.name"
        every { context.applicationContext } returns mockk()

        every { getNotificationId(any<Notification>()) } returns 123
        every { getNotificationTag(any<Notification>()) } returns "notification-tag"

        // WHEN
        tested(
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

        // THEN
        verify { notificationManager.cancel("notification-tag", 123) }
    }
}
