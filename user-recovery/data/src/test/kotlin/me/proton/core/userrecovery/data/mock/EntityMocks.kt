/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.data.mock

import io.mockk.every
import io.mockk.mockk
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.usersettings.domain.entity.UserSettings

internal const val TEST_RECOVERY_SECRET = "base64-secret"
internal const val TEST_RECOVERY_SECRET_HASH = "8c5f286a86467e860c9eda6129df8ef2a26d7eef69f768d06f7652dff94c468a"

fun mockAccount(testUserId: UserId, accountState: AccountState = AccountState.Ready): Account = mockk {
    every { state } returns accountState
    every { userId } returns testUserId
}

fun mockUser(testUserId: UserId, testKeys: List<UserKey> = emptyList()): User = mockk {
    every { userId } returns testUserId
    every { keys } returns testKeys
}

fun mockUserKey(
    testRecoverySecret: String? = TEST_RECOVERY_SECRET,
    testRecoverySecretHash: String? = TEST_RECOVERY_SECRET_HASH,
    isActive: Boolean = true,
    testPrivateKey: PrivateKey? = null
): UserKey = mockk {
    every { active } returns isActive
    testPrivateKey?.let { every { privateKey } returns testPrivateKey }
    every { recoverySecret } returns testRecoverySecret
    every { recoverySecretHash } returns testRecoverySecretHash
}

fun mockUserSettings(testUserId: UserId, testDeviceRecovery: Boolean?): UserSettings = mockk {
    every { userId } returns testUserId
    every { deviceRecovery } returns testDeviceRecovery
}
