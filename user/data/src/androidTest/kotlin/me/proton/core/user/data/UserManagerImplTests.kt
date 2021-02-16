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

package me.proton.core.user.data

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
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.Product
import me.proton.core.key.data.api.response.AddressesResponse
import me.proton.core.key.data.api.response.UsersResponse
import me.proton.core.key.data.repository.KeySaltRepositoryImpl
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.decryptTextOrNull
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.extension.areAllLocked
import me.proton.core.key.domain.useKeys
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.di.ApiFactory
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.runBlockingWithTimeout
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.data.api.AddressApi
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.repository.UserAddressRepositoryImpl
import me.proton.core.user.data.repository.UserRepositoryImpl
import me.proton.core.user.domain.UnlockResult
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.PassphraseRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserManagerImplTests {

    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiFactory = mockk<ApiFactory>(relaxed = true)

    private val userApi = mockk<UserApi>(relaxed = true)
    private val addressApi = mockk<AddressApi>(relaxed = true)

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = object : KeyStoreCrypto {
            override fun encrypt(value: String): EncryptedString = value
            override fun decrypt(value: EncryptedString): String = value
            override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array.copyOf())
            override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array.copyOf())
        }
    )

    private lateinit var apiProvider: ApiProvider

    private lateinit var accountManager: AccountManager

    private lateinit var db: AccountManagerDatabase
    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var userAddressRepository: UserAddressRepositoryImpl
    private lateinit var passphraseRepository: PassphraseRepository
    private lateinit var keySaltRepository: KeySaltRepositoryImpl

    private lateinit var userManager: UserManagerImpl

    @Before
    fun setup() {
        // Build a new fresh in memory Database, for each test.
        db = TestAccountManagerDatabase.buildMultiThreaded()

        coEvery { sessionProvider.getSessionId(any()) } returns TestAccounts.sessionId
        every { apiFactory.create(any(), interfaceClass = UserApi::class) } returns TestApiManager(userApi)
        every { apiFactory.create(any(), interfaceClass = AddressApi::class) } returns TestApiManager(addressApi)

        apiProvider = ApiProvider(apiFactory, sessionProvider)

        keySaltRepository = KeySaltRepositoryImpl(db, apiProvider)

        // UserRepositoryImpl implements PassphraseRepository.
        userRepository = UserRepositoryImpl(db, apiProvider)
        passphraseRepository = userRepository

        // UserManagerImpl need UserAddressRepository.
        userAddressRepository = UserAddressRepositoryImpl(
            db,
            apiProvider,
            userRepository,
            UserAddressKeySecretProvider(
                userRepository,
                userRepository,
                cryptoContext
            )
        )

        // Needed to addAccount (User.userId foreign key -> Account.userId).
        accountManager = AccountManagerImpl(
            Product.Mail,
            AccountRepositoryImpl(Product.Mail, db, cryptoContext.keyStoreCrypto),
            mockk(relaxed = true)
        )

        // Before fetching any User, account need to be added to AccountManager (if not -> foreign key exception).
        runBlocking {
            accountManager.addAccount(TestAccounts.User1.account, TestAccounts.session)
            accountManager.addAccount(TestAccounts.User2.account, TestAccounts.session)
        }

        // Implementation we want to test.
        userManager = UserManagerImpl(
            userRepository,
            userAddressRepository,
            passphraseRepository,
            keySaltRepository,
            cryptoContext
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun unlockWithPassphrase() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val result = userManager.unlockWithPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // THEN
        assertIs<UserManager.UnlockResult.Success>(result)

        val user = userManager.getUserFlow(TestUsers.User1.id)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNot { it?.keys?.areAllLocked() ?: true }
            .firstOrNull()

        assertNotNull(user)

        val passphrase = passphraseRepository.getPassphrase(TestUsers.User1.id)
        assertNotNull(passphrase)
        assertEquals(TestUsers.User1.Key1.passphrase, passphrase)
    }

    @Test
    fun getUser_useKeys_unlocked() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Unlock UserKey.
        userManager.unlockWithPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val user = userManager.getUserFlow(TestUsers.User1.id)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNot { it?.keys?.areAllLocked() ?: true }
            .firstOrNull()

        // THEN
        assertNotNull(user)

        user.useKeys(cryptoContext) {
            val message = "message"

            val encryptedText = encryptText(message)
            val decryptedText = decryptText(encryptedText)

            assertNotNull(decryptTextOrNull(encryptedText))
            assertEquals(message, decryptedText)
        }
    }

    @Test
    fun getUser_useKey_locked() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val user = userManager.getUserFlow(TestUsers.User1.id)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(user)

        user.useKeys(cryptoContext) {
            val message = "message"

            // Can encrypt (only need publicKey).
            val encryptedText = encryptText(message)

            // Cannot decrypt (need unlocked PrivateKey).
            assertFailsWith(CryptoException::class) { decryptText(encryptedText) }
            assertNull(decryptTextOrNull(encryptedText))
        }
    }

    @Test
    fun getAddresses_useKey_locked() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // WHEN
        val addresses = userManager.getAddressesFlow(TestUsers.User1.id)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(addresses)
        assertEquals(1, addresses.size)

        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            // Can encrypt (only need publicKey).
            val encryptedText = encryptText(message)

            // Cannot decrypt (need unlocked PrivateKey).
            assertFailsWith(CryptoException::class) { decryptText(encryptedText) }
            assertNull(decryptTextOrNull(encryptedText))
        }
    }

    @Test
    fun getAddresses_useKey_unlocked() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // Unlock UserKey.
        userManager.unlockWithPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val addresses = userManager.getAddressesFlow(TestUsers.User1.id)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(addresses)
        assertEquals(1, addresses.size)

        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            val encryptedText = encryptText(message)
            val decryptedText = decryptText(encryptedText)

            assertNotNull(decryptTextOrNull(encryptedText))
            assertEquals(message, decryptedText)
        }
    }

    @Test
    fun getAddresses_useKey_unlocked_token_signature() = runBlockingWithTimeout {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User2.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User2.Address1.response))
        }
        // Unlock UserKey.
        userManager.unlockWithPassphrase(TestUsers.User2.id, TestUsers.User2.Key1.passphrase)

        // WHEN
        val addresses = userManager.getAddressesFlow(TestUsers.User2.id)
            .mapLatest { it as? DataResult.Success }
            .mapLatest { it?.value }
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(addresses)
        assertEquals(1, addresses.size)

        // New format addressKey.
        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            val encryptedText = encryptText(message)
            val decryptedText = decryptText(encryptedText)

            assertNotNull(decryptTextOrNull(encryptedText))
            assertEquals(message, decryptedText)
        }
    }
}
