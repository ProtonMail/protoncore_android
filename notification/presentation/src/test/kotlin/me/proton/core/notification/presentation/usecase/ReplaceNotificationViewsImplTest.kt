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
import android.service.notification.StatusBarNotification
import androidx.core.os.bundleOf
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
import me.proton.core.notification.domain.usecase.CancelNotificationView
import me.proton.core.notification.domain.usecase.ShowNotificationView
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraNotificationId
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraUserId
import kotlin.test.BeforeTest
import kotlin.test.Test

class ReplaceNotificationViewsImplTest {
    @MockK
    private lateinit var cancelNotification: CancelNotificationView

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var showNotification: ShowNotificationView

    private lateinit var tested: ReplaceNotificationViewsImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ReplaceNotificationViewsImpl(cancelNotification, context, showNotification)
    }

    @Test
    fun replacesNotifications() {
        // GIVEN
        val userId = UserId("user_id")
        val notificationId1 = NotificationId("notification_1")
        val notificationId2 = NotificationId("notification_2")
        val notificationId3 = NotificationId("notification_3")

        justRun { cancelNotification(any(), any()) }
        justRun { showNotification(any()) }

        // currently displayed notifications:
        every { context.getSystemService(any()) } returns mockk<NotificationManager> {
            every { activeNotifications } returns arrayOf(
                mockStatusBarNotification(notificationId1, userId),
                mockStatusBarNotification(notificationId2, userId),

                // notification for different user:
                mockStatusBarNotification(
                    NotificationId("notification_1"),
                    UserId("different_user")
                ),

                // notification with missing notification ID:
                mockStatusBarNotification(notificationId = null, userId),
            )
        }

        // notifications that we want to display:
        val emptyPayload = NotificationPayload.Unencrypted(raw = "", null, null, null)
        val notification2 = Notification(notificationId2, userId, 1, "type_1", emptyPayload)
        val notification3 = Notification(notificationId3, userId, 1, "type_1", emptyPayload)
        val updatedNotifications = listOf(notification2, notification3)

        // WHEN
        tested(updatedNotifications, userId)

        // THEN
        verify { cancelNotification(notificationId1, userId) }
        verify { showNotification(notification2) }
        verify { showNotification(notification3) }
    }

    private fun mockStatusBarNotification(
        notificationId: NotificationId?,
        userId: UserId
    ): StatusBarNotification = mockk {
        every { notification } returns android.app.Notification().apply {
            extras = bundleOf(
                ExtraNotificationId to notificationId?.id,
                ExtraUserId to userId.id
            )
        }
    }
}
