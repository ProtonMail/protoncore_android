/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.accountmanager.data

import kotlinx.coroutines.CoroutineScope
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.data.job.disableInitialNotReadyAccounts
import me.proton.core.accountmanager.data.job.onInvalidUserAddressKey
import me.proton.core.accountmanager.data.job.onInvalidUserKey
import me.proton.core.accountmanager.data.job.onMigrationNeeded
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.migrator.AccountMigrator
import me.proton.core.domain.entity.Product
import me.proton.core.user.domain.UserManager
import me.proton.core.util.kotlin.CoreLogger
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountStateHandler @Inject constructor(
    @AccountStateHandlerCoroutineScope
    internal val scope: CoroutineScope,
    internal val userManager: UserManager,
    internal val accountManager: AccountManager,
    private val accountRepository: AccountRepository,
    private val accountMigrator: AccountMigrator,
    private val product: Product,
) {
    fun start() {
        disableInitialNotReadyAccounts()
        onMigrationNeeded { userId ->
            accountMigrator.migrate(userId)
        }
        if (product != Product.Vpn) {
            onInvalidUserKey { userId ->
                CoreLogger.e(LogTag.INVALID_USER_KEY, IllegalStateException("Account with invalid user key: user id = $userId"))
                accountRepository.updateAccountState(userId, AccountState.UserKeyCheckFailed)
                accountManager.disableAccount(userId)
            }
            onInvalidUserAddressKey { userId ->
                CoreLogger.e(LogTag.INVALID_USER_ADDRESS_KEY, IllegalStateException("Account with invalid address key: user id = $userId"))
                accountRepository.updateAccountState(userId, AccountState.UserAddressKeyCheckFailed)
                accountManager.disableAccount(userId)
            }
        }
    }
}

object LogTag {
    /** Tag for Invalid User Key. */
    const val INVALID_USER_KEY = "core.accountmanager.invalid.user.key"

    /** Tag for Invalid UserAddress Key. */
    const val INVALID_USER_ADDRESS_KEY = "core.accountmanager.invalid.useraddress.key"

    /** Default tag for any other issue we need to log */
    const val DEFAULT = "core.accountmanager.default"
}
