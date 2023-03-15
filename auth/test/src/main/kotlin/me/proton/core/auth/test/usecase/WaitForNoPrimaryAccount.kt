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

package me.proton.core.auth.test.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import javax.inject.Inject
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val ACCOUNT_WAIT_MS = 30L * 1000
private const val WAIT_DELAY_MS = 250L

public class WaitForNoPrimaryAccount @Inject constructor(private val accountManager: AccountManager) {

    /**
     * Waits for no primary account.
     */
    public operator fun invoke(
        timeout: Duration = ACCOUNT_WAIT_MS.milliseconds
    ): Unit = runBlocking {
        val account = waitForNoAccount(timeout)
        assertNull(
            account,
            "Primary account is still accessible after $timeout (account: $account)."
        )
    }

    private suspend fun waitForNoAccount(
        timeout: Duration
    ): Account? {
        suspend fun getAccount(): Account? = accountManager.getPrimaryAccount().first()
        var account: Account? = getAccount()
        withTimeoutOrNull(timeout) {
            while (true) {
                if (account == null) break
                delay(WAIT_DELAY_MS)
                account = getAccount()
            }
        }
        return account
    }
}
