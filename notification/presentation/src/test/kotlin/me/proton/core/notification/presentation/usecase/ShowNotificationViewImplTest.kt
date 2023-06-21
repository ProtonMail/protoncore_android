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

import android.app.Notification.EXTRA_SUB_TEXT
import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.IconCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.notification.presentation.internal.GetNotificationId
import me.proton.core.notification.presentation.internal.GetNotificationTag
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.notification.domain.usecase.GetNotificationChannelId
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ShowNotificationViewImplTest {
    private lateinit var context: Context

    @MockK
    private lateinit var getNotificationChannelId: GetNotificationChannelId

    @MockK
    private lateinit var getNotificationId: GetNotificationId

    @MockK
    private lateinit var getNotificationTag: GetNotificationTag

    @MockK
    private lateinit var hasNotificationPermission: HasNotificationPermission

    private lateinit var tested: ShowNotificationViewImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(IconCompat::class)
        context = ApplicationProvider.getApplicationContext()
    }

    @AfterTest
    fun afterEveryTest() {
        unmockkStatic(IconCompat::class)
    }

    @Test
    fun unknownNotification() {
        // GIVEN
        makeTested(context = context)
        every { hasNotificationPermission() } returns true

        // WHEN
        tested(
            Notification(
                notificationId = NotificationId("notification_id"),
                userId = UserId("user_id"),
                time = 11,
                type = "type",
                payload = NotificationPayload.Unknown(raw = "")
            )
        )

        // THEN
        val notificationManager =
            shadowOf(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        assertTrue(notificationManager.allNotifications.isEmpty())
    }

    @Test
    fun noNotificationPermission() {
        // GIVEN
        makeTested(context = context)
        every { hasNotificationPermission() } returns false

        // WHEN
        tested(mockk())

        // THEN
        val notificationManager =
            shadowOf(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        assertTrue(notificationManager.allNotifications.isEmpty())
    }

    @Test
    fun postNotification() {
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

        val packageManager = spyk<PackageManager>()
        val contextSpy = spyk(context)
        val notificationManager =
            shadowOf(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        every { contextSpy.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(any()) } returns mockk(relaxed = true)
        every { IconCompat.createWithResource(any(), any()) } returns mockk(relaxed = true)

        makeTested(context = contextSpy)

        every { hasNotificationPermission() } returns true
        every { getNotificationChannelId() } returns "channel-id"
        every { getNotificationId(any<Notification>()) } returns 1
        every { getNotificationTag(any<Notification>()) } returns "notification-tag"

        // WHEN
        tested(notification)

        // THEN
        verify { getNotificationId(any<Notification>()) }
        verify { getNotificationTag(notification) }

        val result = notificationManager.getNotification("notification-tag", 1)
        assertEquals("title", result.extras.getString(EXTRA_TITLE))
        assertEquals("subtitle", result.extras.getString(EXTRA_SUB_TEXT))
        assertEquals("body", result.extras.getString(EXTRA_TEXT))
    }

    private fun makeTested(product: Product = Product.Mail, context: Context) {
        tested = ShowNotificationViewImpl(
            context,
            getNotificationChannelId,
            getNotificationId,
            getNotificationTag,
            hasNotificationPermission,
            product
        )
    }
}
