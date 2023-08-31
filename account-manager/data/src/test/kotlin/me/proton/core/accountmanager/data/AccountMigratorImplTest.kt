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

package me.proton.core.accountmanager.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountMetadataDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.repository.UserRepository
import org.junit.Before
import org.junit.Test

class AccountMigratorImplTest {

    private val accountManager = mockk<AccountManager>()
    private val accountRepository = mockk<AccountRepository>()
    private val userRepository = mockk<UserRepository>()
    private val refreshUserWorkManager = mockk<RefreshUserWorkManager>()

    private lateinit var accountMigrator: AccountMigratorImpl

    @Before
    fun beforeEveryTest() {
        accountMigrator = AccountMigratorImpl(
            accountManager, accountRepository, userRepository, refreshUserWorkManager
        )
    }

    @Test
    fun `on migrate refresh user`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        val sessionId = SessionId("test-session-id")
        val account = Account(
            userId = userId,
            username = "username",
            email = "test@example.com",
            state = AccountState.MigrationNeeded,
            sessionId = sessionId,
            sessionState = SessionState.Authenticated,
            details = AccountDetails(
                AccountMetadataDetails(
                    primaryAtUtc = 1,
                    migrations = listOf("RefreshUser")
                ),
                null
            )
        )
        coEvery { accountRepository.getAccountOrNull(userId) } returns account
        coEvery { accountRepository.removeMigration(userId, "RefreshUser") } returns Unit
        coEvery { refreshUserWorkManager.enqueue(userId) } returns Unit
        // WHEN
        accountMigrator.migrate(userId)

        // THEN
        coVerify { refreshUserWorkManager.enqueue(userId) }
        coVerify(exactly = 1) { accountRepository.removeMigration(userId, "RefreshUser") }
        coVerify(exactly = 0) { accountManager.disableAccount(userId) }
    }

    @Test
    fun `on migrate failure`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        val sessionId = SessionId("test-session-id")
        val account = Account(
            userId = userId,
            username = "username",
            email = "test@example.com",
            state = AccountState.MigrationNeeded,
            sessionId = sessionId,
            sessionState = SessionState.Authenticated,
            details = AccountDetails(
                AccountMetadataDetails(
                    primaryAtUtc = 1,
                    migrations = listOf("DecryptPassphrase")
                ),
                null
            )
        )
        coEvery { accountRepository.getAccountOrNull(userId) } returns account
        coEvery { refreshUserWorkManager.enqueue(userId) } returns Unit

        coEvery { userRepository.getPassphrase(userId) } returns EncryptedByteArray("test-passphrase".toByteArray())
        coEvery { accountRepository.removeMigration(userId, "DecryptPassphrase") } returns Unit
        coEvery { accountManager.disableAccount(userId) } returns Unit
        coEvery { userRepository.clearPassphrase(userId) } throws RuntimeException("test-exception")
        // WHEN
        accountMigrator.migrate(userId)

        // THEN
        coVerify { accountManager.disableAccount(userId) }
        coVerify(exactly = 0) { refreshUserWorkManager.enqueue(userId) }
        coVerify(exactly = 0) {
            userRepository.setPassphrase(
                userId,
                EncryptedByteArray("test-passphrase".toByteArray())
            )
        }
    }

    @Test
    fun `on migrate decrypt passphrase`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        val sessionId = SessionId("test-session-id")
        val account = Account(
            userId = userId,
            username = "username",
            email = "test@example.com",
            state = AccountState.MigrationNeeded,
            sessionId = sessionId,
            sessionState = SessionState.Authenticated,
            details = AccountDetails(
                AccountMetadataDetails(
                    primaryAtUtc = 1,
                    migrations = listOf("DecryptPassphrase")
                ),
                null
            )
        )
        coEvery { accountRepository.getAccountOrNull(userId) } returns account
        coEvery { refreshUserWorkManager.enqueue(userId) } returns Unit

        coEvery { userRepository.getPassphrase(userId) } returns EncryptedByteArray("test-passphrase".toByteArray())
        coEvery { userRepository.clearPassphrase(userId) } returns Unit
        coEvery { userRepository.setPassphrase(userId, any()) } returns Unit
        coEvery { accountRepository.removeMigration(userId, "DecryptPassphrase") } returns Unit
        // WHEN
        accountMigrator.migrate(userId)

        // THEN
        coVerify(exactly = 0) { refreshUserWorkManager.enqueue(userId) }
        coVerify { userRepository.clearPassphrase(userId) }
        coVerify { userRepository.setPassphrase(userId, EncryptedByteArray("test-passphrase".toByteArray())) }
        coVerify(exactly = 1) { accountRepository.removeMigration(userId, "DecryptPassphrase") }
        coVerify(exactly = 0) { accountManager.disableAccount(userId) }
    }
}