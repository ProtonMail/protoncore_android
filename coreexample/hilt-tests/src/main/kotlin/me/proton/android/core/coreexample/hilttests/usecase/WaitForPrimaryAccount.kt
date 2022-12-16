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
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val ACCOUNT_WAIT_MS = 30L * 1000
private const val WAIT_DELAY_MS = 250L

class WaitForPrimaryAccount @Inject constructor(private val accountManager: AccountManager) {
    /** Waits for the primary account to be in the given [state].
     * @param state If `null`, the account can be in any state.
     */
    operator fun invoke(
        state: AccountState? = AccountState.Ready,
        timeout: Duration = ACCOUNT_WAIT_MS.milliseconds
    ): Account? = runBlocking { waitForAccount(state, timeout) }

    private suspend fun waitForAccount(
        state: AccountState?,
        timeout: Duration
    ): Account? = withTimeoutOrNull(timeout) {
        var account: Account?
        while (true) {
            account = accountManager.getPrimaryAccount().firstOrNull()
            if (account != null && (state == null || account.state == state)) break
            delay(WAIT_DELAY_MS)
        }
        account
    }
}
