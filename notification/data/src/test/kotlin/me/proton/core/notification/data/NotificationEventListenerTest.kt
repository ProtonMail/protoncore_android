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

package me.proton.core.notification.data

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.notification.data.remote.response.NotificationResponse
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.notification.domain.repository.NotificationRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class NotificationEventListenerTest {
    @MockK
    private lateinit var notificationDatabase: NotificationDatabase

    @MockK(relaxUnitFun = true)
    private lateinit var notificationRepository: NotificationRepository

    private lateinit var tested: NotificationEventListener

    private val testUserId = UserId("user-id")
    private val testEventManagerConfig = EventManagerConfig.Core(testUserId)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = NotificationEventListener(notificationDatabase, notificationRepository)
    }

    @Test
    fun `deserializing null notifications`() = runTest {
        val response = EventsResponse("{ \"Notifications\": null }")
        assertNull(tested.deserializeEvents(testEventManagerConfig, response))
    }

    @Test
    fun `deserializing notifications`() = runTest {
        val response = EventsResponse(
            """
                {
                    "Notifications": [
                        {
                            "ID": "notification_id",
                            "Time": 100,
                            "Type": "notification_type",
                            "Payload": {}
                        }
                    ]
                }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                Event(
                    Action.Update,
                    "notification_id",
                    NotificationResponse(
                        "notification_id",
                        100,
                        "notification_type",
                        JsonObject(emptyMap())
                    )
                )
            ),
            tested.deserializeEvents(testEventManagerConfig, response)
        )
    }

    @Test
    fun `updating notifications`() = runTest {
        val response = NotificationResponse(
            "notification_id",
            100,
            "notification_type",
            JsonObject(emptyMap())
        )
        tested.onUpdate(testEventManagerConfig, listOf(response))

        coVerify {
            notificationRepository.upsertNotifications(
                Notification(
                    NotificationId("notification_id"),
                    testUserId,
                    100,
                    "notification_type",
                    NotificationPayload.Unencrypted("{}")
                )
            )
        }
    }

    @Test
    fun `resetting notifications`() = runTest {
        tested.onResetAll(testEventManagerConfig)

        coVerify { notificationRepository.deleteAllNotificationsByUser(testUserId) }
    }
}
