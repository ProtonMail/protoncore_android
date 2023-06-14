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

package me.proton.core.accountrecovery.presentation.notification

import android.app.NotificationManager
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.accountrecovery.presentation.internal.GetNotificationId
import me.proton.core.accountrecovery.presentation.internal.GetNotificationTag
import me.proton.core.domain.entity.UserId
import kotlin.test.BeforeTest
import kotlin.test.Test

class CancelNotificationsImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var getNotificationId: GetNotificationId

    @MockK
    private lateinit var getNotificationTag: GetNotificationTag

    private lateinit var tested: CancelNotificationsImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = CancelNotificationsImpl(context, getNotificationId, getNotificationTag)
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

        every { getNotificationId() } returns 1
        every { getNotificationTag(any()) } returns "notification-tag"

        // WHEN
        tested(UserId("user-id"))

        // THEN
        verify { notificationManager.cancel("notification-tag", 1) }
    }
}
