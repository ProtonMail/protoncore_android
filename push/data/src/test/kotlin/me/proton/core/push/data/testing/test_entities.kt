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

package me.proton.core.push.data.testing

import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.user.data.entity.UserEntity

internal val testUserId = UserId("a")

internal fun testUserEntity(userId: UserId) = UserEntity(
    userId = userId,
    email = null,
    name = null,
    displayName = null,
    currency = "",
    credit = 0,
    createdAtUtc = 1000L,
    usedSpace = 0,
    maxSpace = 0,
    maxUpload = 0,
    role = null,
    isPrivate = false,
    subscribed = 0,
    services = 0,
    delinquent = null,
    recovery = null,
    passphrase = null,
    maxBaseSpace = null,
    maxDriveSpace = null,
    usedBaseSpace = null,
    usedDriveSpace = null,
)

internal fun testAccountEntity(userId: UserId) = AccountEntity(
    userId = userId,
    username = "",
    email = null,
    state = AccountState.Ready,
    sessionId = null,
    sessionState = null
)

internal val testPush1 = Push(testUserId, PushId("1"), "obj-1", "TestType1")
internal val testPush2 = Push(testUserId, PushId("2"), "obj-2", PushObjectType.Messages.value)
internal val testPush3 = Push(testUserId, PushId("3"), "obj-3", "TestType3")
internal val testPushesMessages = listOf(testPush2)
internal val allTestPushes = listOf(testPush1, testPush2, testPush3)
