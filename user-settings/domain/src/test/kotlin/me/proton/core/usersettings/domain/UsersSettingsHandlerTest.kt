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

package me.proton.core.usersettings.domain

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.coroutineScopeProvider
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class UsersSettingsHandlerTest {

    @Test
    fun test() = runTest {
        val accountManager = mockk<AccountManager>()
        val observeUserSettings = mockk<ObserveUserSettings>()

        every { accountManager.getAccounts(AccountState.Ready) } returns flowOf(
            listOf(
                account(UserId("user-1")),
                account(UserId("user-2")),
            )
        )

        every { observeUserSettings(any()) } answers {
            when (val userId = arg<UserId>(0)) {
                UserId("user-1") -> flowOf(
                    userSettings(userId, true)
                )

                UserId("user-2") -> flowOf(
                    userSettings(userId, true),
                    userSettings(userId, false)
                )

                else -> flowOf()
            }
        }

        var crashReports: List<Boolean> = emptyList()

        UsersSettingsHandler(
            coroutineScopeProvider,
            accountManager,
            observeUserSettings
        ).onUsersSettingsChanged(
            merge = { usersSettings ->
                usersSettings.none { userSettings -> userSettings?.crashReports == false }
            }
        ) {
            crashReports = crashReports + it
        }.join()

        assertEquals(
            listOf(
                true, // true + true
                false // true + false
            ), crashReports
        )
    }

    private fun account(userId1: UserId) = Account(
        userId = userId1,
        username = "",
        email = null,
        state = AccountState.Ready,
        sessionId = null,
        sessionState = null,
        details = AccountDetails(null, null)
    )

    private fun userSettings(userId: UserId, crashReports: Boolean?) = UserSettings.nil(userId).copy(
        crashReports = crashReports
    )
}
