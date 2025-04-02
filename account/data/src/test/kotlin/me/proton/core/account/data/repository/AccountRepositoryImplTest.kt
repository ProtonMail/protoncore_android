/*
 * Copyright (c) 2023 Proton AG
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
package me.proton.core.account.data.repository

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.data.db.AccountDao
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.db.AccountMetadataDao
import me.proton.core.account.data.db.SessionDao
import me.proton.core.account.data.db.SessionDetailsDao
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountMetadataDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val ENCRYPTED_STRING_PREFIX = "encrypted-"

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
        override fun encrypt(value: String): EncryptedString = "$ENCRYPTED_STRING_PREFIX$value"
        override fun encrypt(value: PlainByteArray): EncryptedByteArray =
            EncryptedByteArray(value.array)

        override fun decrypt(value: EncryptedString): String =
            value.removePrefix(ENCRYPTED_STRING_PREFIX)

        override fun decrypt(value: EncryptedByteArray): PlainByteArray =
            PlainByteArray(value.array)
    }

    private val testUserId = UserId("user1")
    private val testUsername = "username"
    private val testEmail = "test@example.com"
    private val testSessionId = SessionId("session1")

    private val testAccountEntity = AccountEntity(
        userId = testUserId,
        username = testUsername,
        email = testEmail,
        state = AccountState.Ready,
        sessionId = testSessionId,
        sessionState = SessionState.Authenticated
    )

    private val testAccount = Account(
        userId = testUserId,
        username = testUsername,
        email = testEmail,
        state = AccountState.Ready,
        sessionId = testSessionId,
        sessionState = SessionState.Authenticated,
        details = AccountDetails(
            account = null,
            session = null
        )
    )

    private val product = Product.Calendar

    private val testSessionEntity = SessionEntity(
        userId = testAccountEntity.userId,
        sessionId = testSessionId,
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail"),
        product = product
    )
    private val testSession = Session.Authenticated(
        userId = testUserId,
        sessionId = testSessionId,
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail")
    )

    private val sessionInvalid = SessionEntity(
        userId = UserId(""),
        sessionId = SessionId("sessionInvalid"),
        accessToken = "",
        refreshToken = "",
        scopes = listOf("full", "calendar", "mail"),
        product = product
    )

    private val ad = AccountDetails(null, null)

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)

        setupAccountDatabase()

        accountRepository = AccountRepositoryImpl(product, db, simpleCrypto)
    }

    private fun setupAccountDatabase() {
        every { db.accountDao() } returns accountDao
        every { db.sessionDao() } returns sessionDao
        every { db.sessionDetailsDao() } returns sessionDetailsDao
        every { db.accountMetadataDao() } returns metadataDao

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Any?>()
        coEvery { db.inTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        coEvery { accountDao.getByUserId(any()) } returns testAccountEntity
        coEvery { accountDao.getBySessionId(any()) } returns testAccountEntity
        coEvery { sessionDetailsDao.getBySessionId(any()) } returns null
    }

    @Test
    fun `add user with session`() = runTest {
        accountRepository.createOrUpdateAccountSession(
            testAccountEntity.toAccount(ad),
            testSessionEntity.toSession(simpleCrypto)
        )

        coVerify(exactly = 1) { accountDao.insertOrUpdate(*anyVararg()) }
        coVerify(exactly = 1) { sessionDao.insertOrUpdate(*anyVararg()) }
        coVerify(exactly = 1) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `add user with invalid session`() = runTest {
        accountRepository.createOrUpdateAccountSession(
            testAccountEntity.toAccount(ad),
            sessionInvalid.toSession(simpleCrypto)
        )
    }

    @Test
    fun `update account state Ready`() = runTest {
        accountRepository.updateAccountState(
            testAccountEntity.toAccount(ad).userId,
            AccountState.Ready
        )

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test
    fun `update account state but account do not exist`() = runTest {
        // No user exist in DB.
        coEvery { accountDao.getByUserId(any()) } returns null

        accountRepository.updateAccountState(
            testAccountEntity.toAccount(ad).userId,
            AccountState.Ready
        )

        // AccountDao.updateAccountState call can be done.
        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        // But MetadataDao.insertOrUpdate cannot!
        coVerify(exactly = 0) { metadataDao.insertOrUpdate(*anyVararg()) }
    }

    @Test
    fun `update account state Removed`() = runTest {
        accountRepository.updateAccountState(
            testAccountEntity.toAccount(ad).userId,
            AccountState.Removed
        )

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.delete(product, testAccountEntity.userId) }
    }

    @Test
    fun `update account state Disabled`() = runTest {
        accountRepository.updateAccountState(
            testAccountEntity.toAccount(ad).userId,
            AccountState.Disabled
        )

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 1) { metadataDao.delete(product, testAccountEntity.userId) }
    }

    @Test
    fun `update account state MigrationNeeded`() = runTest {
        accountRepository.updateAccountState(
            testAccountEntity.toAccount(ad).userId,
            AccountState.MigrationNeeded
        )

        coVerify(exactly = 1) { accountDao.updateAccountState(any(), any()) }
        coVerify(exactly = 0) { metadataDao.delete(product, testAccountEntity.userId) }
    }

    @Test
    fun `clear account session details`() = runTest {
        accountRepository.clearSessionDetails(testAccountEntity.toAccount(ad).sessionId!!)

        coVerify(exactly = 1) { sessionDetailsDao.clearAuthSecrets(any()) }
    }

    @Test
    fun `get account by UserId`() = runTest {
        every { accountDao.findByUserId(any()) } returns flowOf(null, testAccountEntity)
        coEvery { metadataDao.getByUserId(any(), testUserId) } returns AccountMetadataEntity(
            userId = testUserId,
            product = product,
            primaryAtUtc = 100,
            migrations = null
        )
        coEvery { sessionDetailsDao.getBySessionId(testSessionId) } returns SessionDetailsEntity(
            sessionId = testSessionId,
            initialEventId = "event-id",
            requiredAccountType = AccountType.Internal,
            secondFactorEnabled = true,
            twoPassModeEnabled = true,
            passphrase = null,
            password = "encrypted-password",
            fido2AuthenticationOptionsJson = null
        )

        val expectedAccount = testAccount.copy(
            details = AccountDetails(
                account = AccountMetadataDetails(
                    primaryAtUtc = 100,
                    migrations = emptyList()
                ),
                session = SessionDetails(
                    initialEventId = "event-id",
                    requiredAccountType = AccountType.Internal,
                    secondFactorEnabled = true,
                    twoPassModeEnabled = true,
                    passphrase = null,
                    password = "encrypted-password",
                    fido2AuthenticationOptionsJson = null
                )
            )
        )

        accountRepository.getAccount(testUserId).test {
            assertNull(awaitItem())
            assertEquals(expectedAccount, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `get account by sessionId`() = runTest {
        every { accountDao.findBySessionId(testSessionId) } returns flowOf(null, testAccountEntity)
        coEvery { metadataDao.getByUserId(any(), testUserId) } returns null

        val expectedAccount = testAccount.copy(
            details = AccountDetails(
                account = null,
                session = null
            )
        )

        accountRepository.getAccount(testSessionId).test {
            assertNull(awaitItem())
            assertEquals(expectedAccount, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `get accounts`() = runTest {
        every { accountDao.findAll() } returns flowOf(emptyList(), listOf(testAccountEntity))
        coEvery { metadataDao.getByUserId(any(), testUserId) } returns null

        accountRepository.getAccounts().test {
            assertTrue(awaitItem().isEmpty())

            val accounts = awaitItem()
            assertEquals(1, accounts.size)
            assertEquals(testAccount, accounts.first())

            awaitComplete()
        }
    }

    @Test
    fun `get sessions`() = runTest {
        every { sessionDao.findAll(product) } returns flowOf(emptyList(), listOf(testSessionEntity))

        accountRepository.getSessions().test {
            assertTrue(awaitItem().isEmpty())

            val sessions = awaitItem()
            assertEquals(1, sessions.size)
            assertEquals(testSession, sessions.first())

            awaitComplete()
        }
    }

    @Test
    fun `get session by SessionId`() = runTest {
        every { sessionDao.findBySessionId(testSessionId) } returns flowOf(null, testSessionEntity)
        accountRepository.getSession(testSessionId).test {
            assertNull(awaitItem())
            assertEquals(testSession, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `get primary userId`() = runTest {
        every { metadataDao.observeLatestPrimary(product) } returns flowOf(
            null,
            AccountMetadataEntity(
                userId = testUserId,
                product = product,
                primaryAtUtc = 100,
                migrations = null
            )
        )

        accountRepository.getPrimaryUserId().test {
            assertNull(awaitItem())
            assertEquals(testUserId, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `get previous primary userId`() = runTest {
        // no users:
        coEvery { metadataDao.getAllDescending(product) } returns emptyList()
        assertNull(accountRepository.getPreviousPrimaryUserId())

        // single user:
        coEvery { metadataDao.getAllDescending(product) } returns listOf(
            AccountMetadataEntity(
                userId = testUserId,
                product = product,
                primaryAtUtc = 100,
                migrations = null
            )
        )
        assertNull(accountRepository.getPreviousPrimaryUserId())

        // two users:
        val previousUserId = UserId("previous-user-id")
        coEvery { metadataDao.getAllDescending(product) } returns listOf(
            AccountMetadataEntity(
                userId = testUserId,
                product = product,
                primaryAtUtc = 100,
                migrations = null
            ),
            AccountMetadataEntity(
                userId = previousUserId,
                product = product,
                primaryAtUtc = 90,
                migrations = null
            )
        )
        assertEquals(
            previousUserId,
            accountRepository.getPreviousPrimaryUserId()
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `set not-ready user as primary`() = runTest {
        val userId = UserId("not-ready-user")
        coEvery { accountDao.getByUserId(userId) } returns mockk {
            every { state } returns AccountState.NotReady
        }

        accountRepository.setAsPrimary(userId)
    }

    @Test
    fun `add migration`() = runTest {
        accountRepository.onAccountStateChanged(false).test {
            accountRepository.addMigration(testUserId, "TestMigration")
            assertEquals(testUserId, awaitItem().userId)
            coVerify { metadataDao.updateMigrations(product, testUserId, listOf("TestMigration")) }
            coVerify { accountDao.updateAccountState(testUserId, AccountState.MigrationNeeded) }
        }
    }

    @Test
    fun `remove migration`() = runTest {
        coEvery { metadataDao.getByUserId(any(), testUserId) } returns AccountMetadataEntity(
            userId = testUserId,
            product = product,
            primaryAtUtc = 100,
            migrations = listOf("TestMigration")
        )

        accountRepository.onAccountStateChanged(false).test {
            accountRepository.removeMigration(testUserId, "TestMigration")
            assertEquals(testUserId, awaitItem().userId)
            coVerify { metadataDao.updateMigrations(product, testUserId, null) }
            coVerify { accountDao.updateAccountState(testUserId, AccountState.Ready) }
        }
    }

    @Test
    fun `remove non-existing migration`() = runTest {
        coEvery { metadataDao.getByUserId(any(), testUserId) } returns AccountMetadataEntity(
            userId = testUserId,
            product = product,
            primaryAtUtc = 100,
            migrations = listOf("TestMigration")
        )

        accountRepository.removeMigration(testUserId, "UnknownMigration")
        coVerify { metadataDao.updateMigrations(product, testUserId, listOf("TestMigration")) }
    }

    @Test
    fun `update session token`() = runTest {
        accountRepository.updateSessionToken(
            testSessionId,
            accessToken = "accessToken",
            refreshToken = "refreshToken"
        )

        coVerify {
            sessionDao.updateToken(
                testSessionId,
                accessToken = "${ENCRYPTED_STRING_PREFIX}accessToken",
                refreshToken = "${ENCRYPTED_STRING_PREFIX}refreshToken",
            )
        }
    }

    @Test
    fun `set session details`() = runTest {
        accountRepository.setSessionDetails(
            testSessionId,
            SessionDetails(
                initialEventId = "event-id",
                requiredAccountType = AccountType.Internal,
                secondFactorEnabled = true,
                twoPassModeEnabled = true,
                passphrase = null,
                password = "encrypted-password",
                fido2AuthenticationOptionsJson = null
            )
        )

        coVerify {
            sessionDetailsDao.insertOrUpdate(
                SessionDetailsEntity(
                    sessionId = testSessionId,
                    initialEventId = "event-id",
                    requiredAccountType = AccountType.Internal,
                    secondFactorEnabled = true,
                    twoPassModeEnabled = true,
                    passphrase = null,
                    password = "encrypted-password",
                    fido2AuthenticationOptionsJson = null
                )
            )
        }
    }

    @Test
    fun `get unauth session id`() = runTest {
        val unauthSessionId = mockk<SessionId>()
        coEvery { sessionDao.getUnauthenticatedSessionId() } returns unauthSessionId
        assertEquals(unauthSessionId, accountRepository.getSessionIdOrNull(null))
    }

    @Test
    fun `account state changed with initial`() = runTest {
        every { accountDao.findAll() } returns flowOf(listOf(testAccountEntity))
        coEvery { metadataDao.getByUserId(product, testUserId) } returns null

        accountRepository.onAccountStateChanged(initialState = true).test {
            assertEquals(testAccount, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update session scopes`() = runTest {
        accountRepository.updateSessionScopes(testSessionId, listOf("full", "calendar", "mail"))
        coVerify { sessionDao.updateScopes(testSessionId, "full;calendar;mail") }
    }
}
