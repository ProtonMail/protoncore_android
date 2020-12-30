/*
 * Copyright (c) 2020 Proton Technologies AG
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
package me.proton.core.account.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.account.data.db.AccountDao
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.db.AccountMetadataDao
import me.proton.core.account.data.db.HumanVerificationDetailsDao
import me.proton.core.account.data.db.SessionDao
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.data.crypto.EncryptedString
import me.proton.core.data.crypto.StringCrypto
import me.proton.core.domain.entity.Product
import org.junit.Before
import org.junit.Test

class AccountRepositoryImplTest {

    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var db: AccountDatabase

    @MockK
    private lateinit var stringCrypto: StringCrypto

    @RelaxedMockK
    private lateinit var accountDao: AccountDao

    @RelaxedMockK
    private lateinit var sessionDao: SessionDao

    @RelaxedMockK
    private lateinit var metadataDao: AccountMetadataDao

    @RelaxedMockK
    private lateinit var humanVerificationDao: HumanVerificationDetailsDao

    private val account1 = AccountEntity(
        userId = "user1",
        username = "username",
        email = "test@example.com",
        state = AccountState.Ready,
        sessionId = "session1",
        sessionState = SessionState.Authenticated
    )

    private val session1 = SessionEntity(
        userId = account1.userId,
        sessionId = "session1",
        accessToken = EncryptedString("accessToken"),
        refreshToken = EncryptedString("refreshToken"),
        scopes = "full,calendar,mail",
        humanHeaderTokenType = null,
        humanHeaderTokenCode = null,
        product = Product.Calendar
    )

    private val sessionInvalid = SessionEntity(
        userId = "",
        sessionId = "sessionInvalid",
        accessToken = EncryptedString(""),
        refreshToken = EncryptedString(""),
        scopes = "full,calendar,mail",
        humanHeaderTokenType = null,
        humanHeaderTokenCode = null,
        product = Product.Calendar
    )

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)

        setupStringCrypto()
        setupAccountDatabase()

        accountRepository = AccountRepositoryImpl(Product.Calendar, db, stringCrypto)
    }

    private fun setupStringCrypto() {
        val stringSlot = slot<String>()
        every { stringCrypto.encrypt(capture(stringSlot)) } answers {
            EncryptedString(stringSlot.captured)
        }
        val encryptedStringSlot = slot<EncryptedString>()
        every { stringCrypto.decrypt(capture(encryptedStringSlot)) } answers {
            encryptedStringSlot.captured.encrypted
        }
    }

    private fun setupAccountDatabase() {
        every { db.accountDao() } returns accountDao
        every { db.sessionDao() } returns sessionDao
        every { db.accountMetadataDao() } returns metadataDao
        every { db.humanVerificationDetailsDao() } returns humanVerificationDao

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { db.inTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        coEvery { accountDao.getByUserId(any()) } returns account1
        coEvery { accountDao.getBySessionId(any()) } returns account1
    }

    @Test
    fun `add user with session`() = runBlockingTest {
        accountRepository.createOrUpdateAccountSession(account1.toAccount(), session1.toSession(stringCrypto))

        coVerify(exactly = 1) { accountDao.insertOrUpdate(*anyVararg()) }
        coVerify(exactly = 1) { sessionDao.insertOrUpdate(*anyVararg()) }
        coVerify(exactly = 1) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `add user with invalid session`() = runBlockingTest {
        accountRepository.createOrUpdateAccountSession(account1.toAccount(), sessionInvalid.toSession(stringCrypto))
    }

    @Test
    fun `update account state Ready`() = runBlockingTest {
        accountRepository.updateAccountState(account1.toAccount().userId, AccountState.Ready)

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test
    fun `update account state Removed`() = runBlockingTest {
        accountRepository.updateAccountState(account1.toAccount().userId, AccountState.Removed)

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.delete(account1.userId, any()) }
    }

    @Test
    fun `update account state Disabled`() = runBlockingTest {
        accountRepository.updateAccountState(account1.toAccount().userId, AccountState.Disabled)

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.delete(account1.userId, any()) }
    }
}
