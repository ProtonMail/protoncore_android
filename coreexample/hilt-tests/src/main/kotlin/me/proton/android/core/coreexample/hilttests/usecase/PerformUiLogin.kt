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

package me.proton.android.core.coreexample.hilttests.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.login.MailboxPasswordRobot
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private const val ACCOUNT_WAIT_MS = 30L * 1000
private const val WAIT_DELAY_MS = 250L

class PerformUiLogin @Inject constructor(private val accountManager: AccountManager) {
    operator fun invoke(usernameOrEmail: String, password: String, mailboxPass: String? = null): Account {
        AddAccountRobot()
            .signIn()
            .username(usernameOrEmail)
            .password(password)
            .signIn<CoreRobot>()

        if (mailboxPass != null) {
            MailboxPasswordRobot()
                .mailboxPassword(mailboxPass)
                .unlock<CoreRobot>()
        }

        val account = runBlocking { waitForAccount() }
        assertNotNull(account)
        assertEquals(AccountState.Ready, account.state)
        return account
    }

    private suspend fun waitForAccount(): Account? = withTimeoutOrNull(ACCOUNT_WAIT_MS) {
        var account: Account?
        while (true) {
            account = accountManager.getPrimaryAccount().firstOrNull()
            if (account != null) break
            delay(WAIT_DELAY_MS)
        }
        account
    }
}
