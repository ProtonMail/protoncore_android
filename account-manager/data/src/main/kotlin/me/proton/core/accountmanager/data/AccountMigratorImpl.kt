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

import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.migrator.AccountMigrator
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountMigratorImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val refreshUserWorkManager: RefreshUserWorkManager,
    private val refreshAddressesWorkManager: RefreshAddressesWorkManager,
    private val refreshUserAndAddressesWorkManager: RefreshUserAndAddressesWorkManager,
) : AccountMigrator {

    override suspend fun migrate(userId: UserId) {
        accountRepository.getAccountOrNull(userId)?.let { account ->
            runCatching {
                account.details.account?.migrations.orEmpty().forEach { current ->
                    when (AccountMigrator.Migration.valueOf(current)) {
                        AccountMigrator.Migration.DecryptPassphrase -> decryptPassphrase(account)
                        AccountMigrator.Migration.RefreshUser -> enqueueRefreshUser(userId)
                        AccountMigrator.Migration.RefreshAddresses -> enqueueRefreshAddresses(userId)
                        AccountMigrator.Migration.RefreshUserAndAddresses -> enqueueRefreshUserAndAddresses(userId)
                    }
                    accountRepository.removeMigration(account.userId, current)
                }
            }.onFailure {
                accountManager.disableAccount(userId)
            }
        }
    }

    // See AccountDatabase.MIGRATION_4.
    private suspend fun decryptPassphrase(account: Account) {
        // Force updating keys/passphrases by clearing + setting current passphrase.
        userRepository.getPassphrase(account.userId)?.let { passphrase ->
            userRepository.clearPassphrase(account.userId)
            userRepository.setPassphrase(account.userId, passphrase)
        }
    }

    // See AccountDatabase.MIGRATION_6
    private fun enqueueRefreshUser(userId: UserId) {
        refreshUserWorkManager.enqueue(userId)
    }

    // See AccountDatabase.MIGRATION_11
    private fun enqueueRefreshAddresses(userId: UserId) {
        refreshAddressesWorkManager.enqueue(userId)
    }

    // See AccountDatabase.MIGRATION_11
    private fun enqueueRefreshUserAndAddresses(userId: UserId) {
        refreshUserAndAddressesWorkManager.enqueue(userId)
    }
}
