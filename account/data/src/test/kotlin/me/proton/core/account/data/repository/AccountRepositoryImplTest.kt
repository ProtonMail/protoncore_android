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
import me.proton.core.account.data.db.SessionDao
import me.proton.core.account.data.db.SessionDetailsDao
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test

class AccountRepositoryImplTest {

    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var db: AccountDatabase

    @RelaxedMockK
    private lateinit var accountDao: AccountDao

    @RelaxedMockK
    private lateinit var sessionDao: SessionDao

    @RelaxedMockK
    private lateinit var sessionDetailsDao: SessionDetailsDao

    @RelaxedMockK
    private lateinit var metadataDao: AccountMetadataDao

    private val simpleCrypto = object : KeyStoreCrypto {
        override fun isUsingKeyStore(): Boolean = false
        override fun encrypt(value: String): EncryptedString = value
        override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array)
        override fun decrypt(value: EncryptedString): String = value
        override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array)
    }

    private val account1 = AccountEntity(
        userId = UserId("user1"),
        username = "username",
        email = "test@example.com",
        state = AccountState.Ready,
        sessionId = SessionId("session1"),
        sessionState = SessionState.Authenticated
    )

    private val session1 = SessionEntity(
        userId = account1.userId,
        sessionId = SessionId("session1"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = "full,calendar,mail",
        product = Product.Calendar
    )

    private val sessionInvalid = SessionEntity(
        userId = UserId(""),
        sessionId = SessionId("sessionInvalid"),
        accessToken = "",
        refreshToken = "",
        scopes = "full,calendar,mail",
        product = Product.Calendar
    )

    private val ad = AccountDetails(null)

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)

        setupAccountDatabase()

        accountRepository = AccountRepositoryImpl(Product.Calendar, db, simpleCrypto)
    }

    private fun setupAccountDatabase() {
        every { db.accountDao() } returns accountDao
        every { db.sessionDao() } returns sessionDao
        every { db.sessionDetailsDao() } returns sessionDetailsDao
        every { db.accountMetadataDao() } returns metadataDao

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { db.inTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        coEvery { accountDao.getByUserId(any()) } returns account1
        coEvery { accountDao.getBySessionId(any()) } returns account1
        coEvery { sessionDetailsDao.getBySessionId(any()) } returns null
    }

    @Test
    fun `add user with session`() = runBlockingTest {
        accountRepository.createOrUpdateAccountSession(account1.toAccount(ad), session1.toSession(simpleCrypto))

        coVerify(exactly = 1) { accountDao.insertOrUpdate(*anyVararg()) }
        coVerify(exactly = 1) { sessionDao.insertOrUpdate(*anyVararg()) }
        coVerify(exactly = 1) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `add user with invalid session`() = runBlockingTest {
        accountRepository.createOrUpdateAccountSession(account1.toAccount(ad), sessionInvalid.toSession(simpleCrypto))
    }

    @Test
    fun `update account state Ready`() = runBlockingTest {
        accountRepository.updateAccountState(account1.toAccount(ad).userId, AccountState.Ready)

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test
    fun `update account state but account do not exist`() = runBlockingTest {
        // No user exist in DB.
        coEvery { accountDao.getByUserId(any()) } returns null

        accountRepository.updateAccountState(account1.toAccount(ad).userId, AccountState.Ready)

        // AccountDao.updateAccountState call can be done.
        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        // But MetadataDao.insertOrUpdate cannot!
        coVerify(exactly = 0) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test
    fun `update account state Removed`() = runBlockingTest {
        accountRepository.updateAccountState(account1.toAccount(ad).userId, AccountState.Removed)

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.delete(account1.userId, any()) }
    }

    @Test
    fun `update account state Disabled`() = runBlockingTest {
        accountRepository.updateAccountState(account1.toAccount(ad).userId, AccountState.Disabled)

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.delete(account1.userId, any()) }
    }

    @Test
    fun `clear account session details`() = runBlockingTest {
        accountRepository.clearSessionDetails(account1.toAccount(ad).sessionId!!)

        coVerify(exactly = 1) { sessionDetailsDao.clearPassword(any()) }
    }
}
