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

package me.proton.core.notification.domain

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
import me.proton.core.notification.domain.usecase.ConfigureNotificationChannel
import me.proton.core.notification.domain.usecase.GetNotificationChannelId
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.notification.domain.usecase.ShowNotificationView
import kotlin.test.BeforeTest
import kotlin.test.Test

class ProtonNotificationManagerTest {
    @MockK
    private lateinit var cancelNotificationView: CancelNotificationView

    @MockK
    private lateinit var configureNotificationChannel: ConfigureNotificationChannel

    @MockK
    private lateinit var getNotificationChannelId: GetNotificationChannelId

    @MockK
    private lateinit var isNotificationsEnabled: IsNotificationsEnabled

    @MockK
    private lateinit var showNotificationView: ShowNotificationView

    private lateinit var tested: ProtonNotificationManager

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ProtonNotificationManager(
            cancelNotificationView,
            configureNotificationChannel,
            getNotificationChannelId,
            isNotificationsEnabled,
            showNotificationView
        )
    }

    @Test
    fun noSetupIfDisabled() {
        // GIVEN
        every { isNotificationsEnabled() } returns false

        // WHEN
        tested.setupNotificationChannel()

        // THEN
        verify(exactly = 0) { getNotificationChannelId() }
        verify(exactly = 0) { configureNotificationChannel(any()) }
    }

    @Test
    fun setupIfEnabled() {
        // GIVEN
        every { isNotificationsEnabled() } returns true
        every { getNotificationChannelId() } returns "channel-id"
        justRun { configureNotificationChannel(any()) }

        // WHEN
        tested.setupNotificationChannel()

        // THEN
        verify(exactly = 1) { getNotificationChannelId() }
        verify(exactly = 1) { configureNotificationChannel("channel-id") }
    }

    @Test
    fun noNotificationIfDisabled() {
        // GIVEN
        every { isNotificationsEnabled() } returns false

        // WHEN
        tested.show(mockk())

        // THEN
        verify(exactly = 0) { getNotificationChannelId() }
        verify(exactly = 0) { cancelNotificationView(any()) }
        verify(exactly = 0) { showNotificationView(any()) }
    }

    @Test
    fun noDismissIfDisabled() {
        // GIVEN
        every { isNotificationsEnabled() } returns false

        // WHEN
        tested.dismiss(mockk())
        tested.dismiss(mockk(), mockk())

        // THEN
        verify(exactly = 0) { getNotificationChannelId() }
        verify(exactly = 0) { cancelNotificationView(any()) }
    }

    @Test
    fun dismissNotification() {
        // GIVEN
        val notification = Notification(
            notificationId = NotificationId("notification_id"),
            userId = UserId("user_id"),
            time = 11,
            type = "type",
            payload = NotificationPayload.Unencrypted(
                raw = "",
                title = "title",
                subtitle = "subtitle",
                body = "body"
            )
        )
        every { isNotificationsEnabled() } returns true
        justRun { cancelNotificationView(any()) }
        justRun { cancelNotificationView(any(), any()) }

        // WHEN
        tested.dismiss(notification.notificationId, notification.userId)
        tested.dismiss(notification)

        // THEN
        verify(exactly = 1) { cancelNotificationView(notification) }
        verify(exactly = 1) { cancelNotificationView(notification.notificationId, notification.userId) }
        verify(exactly = 0) { showNotificationView(any()) }
    }

    @Test
    fun showingNotification() {
        // GIVEN
        val notification = Notification(
            notificationId = NotificationId("notification_id"),
            userId = UserId("user_id"),
            time = 11,
            type = "type",
            payload = NotificationPayload.Unencrypted(
                raw = "",
                title = "title",
                subtitle = "subtitle",
                body = "body"
            )
        )
        val unknownNotification = Notification(
            notificationId = NotificationId("notification_id_2"),
            userId = UserId("user_id"),
            time = 12,
            type = "type",
            payload = NotificationPayload.Unknown(raw = "")
        )
        every { isNotificationsEnabled() } returns true
        justRun { showNotificationView(any()) }

        // WHEN
        tested.show(notification)
        tested.show(unknownNotification)

        // THEN
        verify(exactly = 0) { cancelNotificationView(any()) }
        verify(exactly = 1) { showNotificationView(notification) }
    }

    @Test
    fun showUnknownNotification() {
        // GIVEN
        val unknownNotification = Notification(
            notificationId = NotificationId("notification_id_2"),
            userId = UserId("user_id"),
            time = 12,
            type = "type",
            payload = NotificationPayload.Unknown(raw = "")
        )
        every { isNotificationsEnabled() } returns true
        justRun { showNotificationView(any()) }

        // WHEN
        tested.show(unknownNotification)

        // THEN
        verify(exactly = 0) { cancelNotificationView(any()) }
        verify(exactly = 1) { showNotificationView(unknownNotification) }
    }
}
