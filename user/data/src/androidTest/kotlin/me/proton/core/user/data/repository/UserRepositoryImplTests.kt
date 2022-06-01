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

package me.proton.core.user.data.repository

import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.runBlocking
import me.proton.core.account.data.repository.AccountRepositoryImpl
import me.proton.core.accountmanager.data.AccountManagerImpl
import me.proton.core.accountmanager.data.db.AccountManagerDatabase
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.data.api.response.SRPAuthenticationResponse
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.Product
import me.proton.core.key.data.api.response.UsersResponse
import me.proton.core.key.domain.extension.areAllInactive
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.android.runBlockingWithTimeout
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.user.data.TestAccountManagerDatabase
import me.proton.core.user.data.TestAccounts
import me.proton.core.user.data.TestUsers
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.api.request.UnlockPasswordRequest
import me.proton.core.user.domain.repository.UserRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryImplTests {

    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)

    private val userApi = mockk<UserApi>(relaxed = true)

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = object : KeyStoreCrypto {
            override fun isUsingKeyStore(): Boolean = false
            override fun encrypt(value: String): EncryptedString = value
            override fun decrypt(value: EncryptedString): String = value
            override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array.copyOf())
            override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array.copyOf())
        }
    )

    private lateinit var apiProvider: ApiProvider

    private lateinit var accountManager: AccountManager

    private lateinit var db: AccountManagerDatabase
    private lateinit var userRepository: UserRepository

    private val product = Product.Mail

    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-client-ephemeral",
        clientProof = "test-client-proof",
        expectedServerProof = "test-server-proof"
    )

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().context

        // Build a new fresh in memory Database, for each test.
        db = TestAccountManagerDatabase.buildMultiThreaded()

        coEvery { sessionProvider.getSessionId(any()) } returns TestAccounts.sessionId
        every { apiManagerFactory.create(any(), interfaceClass = UserApi::class) } returns TestApiManager(userApi)

        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, TestDispatcherProvider)

        userRepository = UserRepositoryImpl(db, apiProvider, context, cryptoContext, product)

        // Needed to addAccount (User.userId foreign key -> Account.userId).
        accountManager = AccountManagerImpl(
            Product.Mail,
            AccountRepositoryImpl(Product.Mail, db, cryptoContext.keyStoreCrypto),
            mockk(relaxed = true),
            mockk(relaxed = true)
        )

        // Before fetching any User, account need to be added to AccountManager (if not -> foreign key exception).
        runBlocking {
            accountManager.addAccount(TestAccounts.User1.account, TestAccounts.session)
            accountManager.addAccount(TestAccounts.User2.account, TestAccounts.session)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getUser_locked() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val user = userRepository.getUserFlow(TestUsers.User1.id)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(user)
        assertEquals(TestUsers.User1.id, user.userId)
        assertEquals(TestUsers.User1.response.keys.size, user.keys.size)
        assertTrue(user.keys.areAllInactive())
    }

    @Test
    fun observeUser_locked() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val user = userRepository.observeUser(TestUsers.User1.id)
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(user)
        assertEquals(TestUsers.User1.id, user.userId)
        assertEquals(TestUsers.User1.response.keys.size, user.keys.size)
        assertTrue(user.keys.areAllInactive())
    }

    @Test
    fun getUser_locked_keys_assert_isActive_only_if_canUnlock() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val user = userRepository.getUser(userId, refresh = true)

        // THEN
        val key1 = user.keys.first { it.keyId.id == TestUsers.User1.Key1.response.id }
        val key2 = user.keys.first { it.keyId.id == TestUsers.User1.Key2Inactive.response.id }

        assertFalse(key1.privateKey.isActive)
        assertFalse(key2.privateKey.isActive)
    }

    @Test
    fun getUser_unlocked() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id
        val passphrase = TestUsers.User1.Key1.passphrase

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(userId)
        userRepository.setPassphrase(userId, passphrase)

        // WHEN
        val user = userRepository.getUserFlow(userId)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNot { it?.keys?.areAllInactive() ?: true }
            .firstOrNull()

        // THEN
        assertNotNull(user)
        assertEquals(TestUsers.User1.id, user.userId)
        assertEquals(TestUsers.User1.response.keys.size, user.keys.size)
        assertFalse(user.keys.areAllInactive())
        assertEquals(passphrase, userRepository.getPassphrase(userId))
    }

    @Test
    fun observeUser_unlocked() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id
        val passphrase = TestUsers.User1.Key1.passphrase

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(userId)
        userRepository.setPassphrase(userId, passphrase)

        // WHEN
        val user = userRepository.observeUser(userId)
            .filterNot { it?.keys?.areAllInactive() ?: true }
            .firstOrNull()

        // THEN
        assertNotNull(user)
        assertEquals(TestUsers.User1.id, user.userId)
        assertEquals(TestUsers.User1.response.keys.size, user.keys.size)
        assertFalse(user.keys.areAllInactive())
        assertEquals(passphrase, userRepository.getPassphrase(userId))
    }

    @Test
    fun getUser_unlocked_keys_assert_isActive_only_if_canUnlock() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id
        val passphrase = TestUsers.User1.Key1.passphrase

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(userId)
        userRepository.setPassphrase(userId, passphrase)

        // WHEN
        val user = userRepository.getUser(userId, refresh = true)

        // THEN
        val key1 = user.keys.first { it.keyId.id == TestUsers.User1.Key1.response.id }
        val key2 = user.keys.first { it.keyId.id == TestUsers.User1.Key2Inactive.response.id }

        assertTrue(key1.privateKey.isActive)
        assertFalse(key2.privateKey.isActive)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setPassphrase_userDoesNotExist() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Add User1 in DB.
        userRepository.getUser(TestUsers.User1.id)

        // WHEN
        // Try setPassphrase for User2.
        userRepository.setPassphrase(TestUsers.User2.id, TestUsers.User2.Key1.passphrase)
    }

    @Test
    fun clearPassphrase() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id
        val passphrase = TestUsers.User1.Key1.passphrase

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(userId)
        userRepository.setPassphrase(userId, passphrase)
        assertNotNull(userRepository.getPassphrase(userId))

        // WHEN
        userRepository.clearPassphrase(userId)

        val user = userRepository.getUser(userId)

        // THEN
        assertNotNull(user)
        assertEquals(TestUsers.User1.id, user.userId)
        assertEquals(TestUsers.User1.response.keys.size, user.keys.size)
        assertTrue(user.keys.areAllInactive())
        assertNull(userRepository.getPassphrase(userId))
    }

    @Test
    fun getUserBlocking_returnCached() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Fetch User (add to cache/DB).
        val oldUser = userRepository.getUser(userId)
        assertEquals(TestUsers.User1.response.credit, oldUser.credit)

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response.copy(credit = -10))
        }

        // WHEN
        val user = userRepository.getUser(userId)

        // THEN
        assertNotNull(user)
        assertEquals(oldUser.credit, user.credit)
    }

    @Test
    fun getUserBlocking_refresh() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id
        val updatedCredit = -10

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Fetch User (add to cache/DB).
        val oldUser = userRepository.getUser(userId)
        assertEquals(TestUsers.User1.response.credit, oldUser.credit)

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response.copy(credit = updatedCredit))
        }

        // WHEN
        val user = userRepository.getUser(userId, refresh = true)

        // THEN
        assertNotNull(user)
        assertEquals(updatedCredit, user.credit)
    }

    @Test
    fun getUserBlocking_refreshDoNotOverridePassphrase() = runBlockingWithTimeout {
        // GIVEN
        val userId = TestUsers.User1.id
        val updatedCredit = -10
        val passphrase = TestUsers.User1.Key1.passphrase

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Fetch User (add to cache/DB).
        val oldUser = userRepository.getUser(userId)
        userRepository.setPassphrase(userId, passphrase)
        assertNotNull(userRepository.getPassphrase(userId))

        assertEquals(TestUsers.User1.response.credit, oldUser.credit)

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response.copy(credit = updatedCredit))
        }

        // WHEN
        val user = userRepository.getUser(userId, refresh = true)

        // THEN
        assertNotNull(user)
        assertNotNull(userRepository.getPassphrase(userId))
        assertEquals(updatedCredit, user.credit)
    }

    @Test
    fun unlockUser_lockedScope() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.unlockLockedScope(any()) } answers {
            SRPAuthenticationResponse(
                code = 1000,
                serverProof = testSrpProofs.expectedServerProof,
            )
        }

        // WHEN
        val response = userRepository.unlockUserForLockedScope(
            TestUsers.User1.id,
            testSrpProofs,
            "test-srp-session"
        )
        assertNotNull(response)
        assertTrue(response)
    }

    @Test
    fun unlockUser_no2fa_passwordScope() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.unlockPasswordScope(any()) } answers {
            SRPAuthenticationResponse(
                code = 1000,
                serverProof = testSrpProofs.expectedServerProof,
            )
        }

        // WHEN
        val response = userRepository.unlockUserForPasswordScope(
            TestUsers.User1.id,
            testSrpProofs,
            "test-srp-session",
            null
        )
        assertNotNull(response)
        assertTrue(response)
    }

    @Test
    fun unlockUser_2fa_passwordScope() = runBlockingWithTimeout {
        // GIVEN
        coEvery {
            userApi.unlockPasswordScope(
                UnlockPasswordRequest(
                    testSrpProofs.clientEphemeral,
                    testSrpProofs.clientProof,
                    "test-srp-session",
                    "test-2fa"
                )
            )
        } answers {
            SRPAuthenticationResponse(
                code = 1000,
                serverProof = testSrpProofs.expectedServerProof,
            )
        }

        // WHEN
        val response = userRepository.unlockUserForPasswordScope(
            TestUsers.User1.id,
            testSrpProofs,
            "test-srp-session",
            "test-2fa"
        )
        assertNotNull(response)
        assertTrue(response)
    }

    @Test
    fun unlockUser_wrong_server_proof(): Unit = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.unlockLockedScope(any()) } answers {
            SRPAuthenticationResponse(
                code = 1000,
                serverProof = testSrpProofs.expectedServerProof + "corrupted",
            )
        }

        // WHEN
        assertFailsWith<InvalidServerAuthenticationException> {
            userRepository.unlockUserForLockedScope(
                TestUsers.User1.id,
                testSrpProofs,
                "test-srp-session"
            )
        }
    }
}
