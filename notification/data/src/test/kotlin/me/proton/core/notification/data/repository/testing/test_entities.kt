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

package me.proton.core.notification.data.repository.testing

import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.user.data.entity.UserEntity

internal val testUserId = UserId("test-user-id")

internal fun testUserEntity(userId: UserId) = UserEntity(
    userId = userId,
    email = null,
    name = null,
    displayName = null,
    currency = "",
    credit = 0,
    usedSpace = 0,
    maxSpace = 0,
    maxUpload = 0,
    role = null,
    isPrivate = false,
    subscribed = 0,
    services = 0,
    delinquent = null,
    recovery = null,
    passphrase = null
)

internal fun testAccountEntity(userId: UserId) = AccountEntity(
    userId = userId,
    username = "",
    email = null,
    state = AccountState.Ready,
    sessionId = null,
    sessionState = null
)

internal val testNotification1 = Notification(
    NotificationId("1"),
    testUserId,
    1,
    type = "TestType1",
    title = "Title 1",
    subtitle = "Subtitle 1",
    body = "Body 1"
)
internal val testNotification2 =
    Notification(NotificationId("2"), testUserId, 2, type = "TestType2", title = null, subtitle = null, body = null)
internal val testNotification3 = Notification(
    NotificationId("3"),
    testUserId,
    3,
    type = "TestType1",
    title = "Title 3",
    subtitle = null,
    body = null
)
internal val allTestNotifications = listOf(testNotification1, testNotification2, testNotification3)
