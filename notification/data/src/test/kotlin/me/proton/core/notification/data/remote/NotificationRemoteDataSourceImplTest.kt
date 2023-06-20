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

package me.proton.core.notification.data.remote

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.notification.data.remote.response.GetNotificationsResponse
import me.proton.core.notification.data.remote.response.NotificationResponse
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.test.android.api.TestApiManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
internal class NotificationRemoteDataSourceImplTest {

    private val apiProvider = mockk<ApiProvider>()
    private lateinit var tested: NotificationRemoteDataSourceImpl

    private val testUserId = UserId("test-user-id")

    @Before
    fun beforeEveryTest() {
        tested = NotificationRemoteDataSourceImpl(apiProvider)
    }

    @Test
    fun `get notifications`() = runTest {
        // GIVEN
        val notificationApi = mockk<NotificationApi> {
            coEvery { getAllNotifications() } returns
                GetNotificationsResponse(
                    notifications = listOf(
                        NotificationResponse(
                            notificationId = "notification-id",
                            time = 1,
                            type = "type",
                            title = "title",
                            subtitle = null,
                            body = null
                        )
                    )
                )
        }
        coEvery { apiProvider.get(NotificationApi::class, any()) } returns TestApiManager(notificationApi)
        every { apiProvider.sessionProvider } returns mockk {
            coEvery { getSessionId(testUserId) } returns SessionId("session-id")
        }
        // WHEN
        val result = tested.getNotifications(testUserId)
        assertEquals(1, result.size)
        assertEquals(NotificationId("notification-id"), result[0].notificationId)
    }

    @Test
    fun `get notifications by type`() = runTest {
        // GIVEN
        val notificationApi = mockk<NotificationApi> {
            coEvery { getAllNotifications() } returns
                GetNotificationsResponse(
                    notifications = listOf(
                        NotificationResponse(
                            notificationId = "notification-id",
                            time = 1,
                            type = "type",
                            title = "title",
                            subtitle = null,
                            body = null
                        ),
                        NotificationResponse(
                            notificationId = "notification-id2",
                            time = 1,
                            type = "type 2",
                            title = "title 2",
                            subtitle = null,
                            body = null
                        )
                    )
                )
        }
        coEvery { apiProvider.get(NotificationApi::class, any()) } returns TestApiManager(notificationApi)
        every { apiProvider.sessionProvider } returns mockk {
            coEvery { getSessionId(testUserId) } returns SessionId("session-id")
        }
        // WHEN
        val result = tested.getNotificationsByType(testUserId, "type 2")
        assertEquals(1, result.size)
        assertEquals(NotificationId("notification-id2"), result[0].notificationId)
    }
}
