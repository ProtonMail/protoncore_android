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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import kotlin.test.BeforeTest
import kotlin.test.Test

class ObservePushNotificationsImplTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var notificationRepository: NotificationRepository

    @MockK
    private lateinit var protonNotificationManager: ProtonNotificationManager

    @MockK
    private lateinit var pushRepository: PushRepository

    private lateinit var tested: ObservePushNotificationsImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ObservePushNotificationsImpl(
            notificationRepository,
            protonNotificationManager,
            pushRepository,
            TestCoroutineScopeProvider(dispatchers)
        )
    }

    @Test
    fun observesPushNotifications() = coroutinesTest {
        // GIVEN
        val userId = UserId("user_id")
        val notification = mockk<Notification>()

        coEvery { notificationRepository.getNotificationById(userId, any()) } returns notification
        every {
            pushRepository.observeAllPushes(
                userId,
                PushObjectType.Notifications,
                any()
            )
        } returns flowOf(
            listOf(
                Push(userId, PushId("push_1"), "notification_1", PushObjectType.Notifications.value)
            )
        )

        // WHEN
        tested(userId)
        runCurrent()

        // THEN
        verify { protonNotificationManager.replace(listOf(notification), userId) }
    }
}