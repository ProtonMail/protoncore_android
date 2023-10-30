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

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.notification.domain.usecase.ObservePushNotifications
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

public class ObservePushNotificationsImpl @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val protonNotificationManager: ProtonNotificationManager,
    private val pushRepository: PushRepository,
    private val scopeProvider: CoroutineScopeProvider
) : ObservePushNotifications {
    override fun invoke(userId: UserId): Job {
        return scopeProvider.GlobalDefaultSupervisedScope.launch {
            pushRepository.observeAllPushes(userId, PushObjectType.Notifications).collect {
                handlePushes(it, userId)
            }
        }
    }

    private suspend fun handlePushes(pushes: List<Push>, userId: UserId) {
        val notifications = pushes.mapNotNull {
            val notificationId = NotificationId(it.objectId)
            notificationRepository.getNotificationById(it.userId, notificationId)
        }

        protonNotificationManager.replace(notifications, userId)
    }
}
