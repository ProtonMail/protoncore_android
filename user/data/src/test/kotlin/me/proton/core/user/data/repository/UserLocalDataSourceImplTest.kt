package me.proton.core.user.data.repository

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.db.dao.UserDao
import me.proton.core.user.data.db.dao.UserKeyDao
import me.proton.core.user.data.db.dao.UserWithKeysDao
import me.proton.core.user.data.entity.UserWithKeys
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.domain.entity.User
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class UserLocalDataSourceImplTest {
    @MockK
    private lateinit var cryptoContext: CryptoContext

    @MockK
    private lateinit var userDao: UserDao

    @MockK
    private lateinit var userKeyDao: UserKeyDao

    @MockK
    private lateinit var userWithKeysDao: UserWithKeysDao

    @MockK
    private lateinit var userDatabase: UserDatabase

    private lateinit var tested: UserLocalDataSourceImpl
    private val testUserId = UserId("test-user-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { userDatabase.userDao() } returns userDao
        every { userDatabase.userKeyDao() } returns userKeyDao
        every { userDatabase.userWithKeysDao() } returns userWithKeysDao
        coEvery { userDatabase.inTransaction<Any?>(any()) } coAnswers {
            (firstArg() as (suspend () -> Any?)).invoke()
        }
        tested = UserLocalDataSourceImpl(cryptoContext, userDatabase)
    }

    @Test
    fun `get passphrase`() = runTest {
        // GIVEN
        val testPassphrase = mockk<EncryptedByteArray>()
        coEvery { userDao.getPassphrase(testUserId) } returns testPassphrase

        // WHEN
        val result = tested.getPassphrase(testUserId)

        // THEN
        assertSame(testPassphrase, result)
    }

    @Test
    fun `set passphrase`() = runTest {
        // GIVEN
        val testPassphrase = mockk<EncryptedByteArray>()

        coEvery { userDao.getPassphrase(any()) } returns null
        coEvery { userWithKeysDao.getByUserId(testUserId) } returns mockk(relaxed = true)

        coJustRun { userDao.setPassphrase(testUserId, testPassphrase) }
        coJustRun { userDao.insertOrUpdate(any()) }
        coJustRun { userKeyDao.deleteAllByUserId(any()) }
        coJustRun { userKeyDao.insertOrUpdate() }

        // WHEN
        var isSuccess = false
        tested.setPassphrase(testUserId, testPassphrase) {
            isSuccess = true
        }

        // THEN
        assertTrue(isSuccess)
        coVerify { userDao.setPassphrase(testUserId, testPassphrase) }
    }

    @Test
    fun `do not set passphrase if unchanged`() = runTest {
        // GIVEN
        val testPassphrase = mockk<EncryptedByteArray>()

        coEvery { userDao.getPassphrase(any()) } returns testPassphrase

        // WHEN
        var isSuccess = false
        tested.setPassphrase(testUserId, testPassphrase) {
            isSuccess = true
        }

        // THEN
        assertFalse(isSuccess)
        coVerify(exactly = 0) { userDao.setPassphrase(testUserId, testPassphrase) }
    }

    @Test
    fun `get user`() = runTest {
        // GIVEN
        val userWithKeys = mockk<UserWithKeys>(relaxed = true)
        coEvery { userWithKeysDao.getByUserId(testUserId) } returns userWithKeys

        // WHEN
        val user = tested.getUser(testUserId)

        // THEN
        assertEquals(userWithKeys.toUser(), user)
    }

    @Test
    fun `observe user`() = runTest {
        // GIVEN
        val flow = MutableStateFlow<UserWithKeys?>(null)
        every { userWithKeysDao.observeByUserId(testUserId) } returns flow

        // WHEN
        tested.observe(testUserId).test {
            // THEN
            assertNull(awaitItem())

            val userWithKeys = mockk<UserWithKeys>(relaxed = true)
            flow.value = userWithKeys

            assertEquals(userWithKeys.toUser(), awaitItem())
        }
    }

    @Test
    fun `upsert user`() = runTest {
        // GIVEN
        val testUser = mockk<User>(relaxed = true) {
            every { userId } returns testUserId
        }
        val testPassphrase = mockk<EncryptedByteArray>()

        coEvery { userDao.getPassphrase(any()) } returns testPassphrase
        coJustRun { userDao.insertOrUpdate(any()) }
        coJustRun { userKeyDao.deleteAllByUserId(any()) }
        coJustRun { userKeyDao.insertOrUpdate() }

        // WHEN
        tested.upsert(testUser)

        // THEN
        coVerify { userDao.insertOrUpdate(any()) }
        coVerify { userKeyDao.deleteAllByUserId(testUserId) }
        coVerify { userKeyDao.insertOrUpdate() }
    }
}