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
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.account.data.repository.AccountRepositoryImpl
import me.proton.core.accountmanager.data.AccountManagerImpl
import me.proton.core.accountmanager.data.db.AccountManagerDatabase
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.Product
import me.proton.core.key.data.api.response.AddressesResponse
import me.proton.core.key.data.api.response.UsersResponse
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyText
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.user.data.TestAccountManagerDatabase
import me.proton.core.user.data.TestAccounts
import me.proton.core.user.data.TestAddresses
import me.proton.core.user.data.TestSessionListener
import me.proton.core.user.data.TestUsers
import me.proton.core.user.data.UserAddressKeySecretProvider
import me.proton.core.user.data.api.AddressApi
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.db.dao.AddressWithKeysDao
import me.proton.core.user.data.extension.toUserAddress
import me.proton.core.user.data.repository.UserAddressRepositoryImpl.Companion.isFetchedTag
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.canEncrypt
import me.proton.core.user.domain.extension.canVerify
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserAddressRepositoryImplTests {

    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val sessionListener = mockk<SessionListener>(relaxed = true)
    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)

    private val userApi = mockk<UserApi>(relaxed = true)
    private val addressApi = mockk<AddressApi>(relaxed = true)

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = object : KeyStoreCrypto {
            override fun isUsingKeyStore(): Boolean = false
            override fun encrypt(value: String): EncryptedString = value
            override fun decrypt(value: EncryptedString): String = value
            override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array.copyOf())
            override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array.copyOf())
        },
        pgpCrypto = GOpenPGPCrypto()
    )

    private lateinit var apiProvider: ApiProvider

    private lateinit var accountManager: AccountManager

    private lateinit var db: AccountManagerDatabase
    private lateinit var addressWithKeysDao: AddressWithKeysDao
    private lateinit var userRepository: UserRepository
    private lateinit var userAddressRepository: UserAddressRepository
    private lateinit var userAddressKeySecretProvider: UserAddressKeySecretProvider

    private val product = Product.Mail
    private val validateServerProof = ValidateServerProof()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().context

        // Build a new fresh in memory Database, for each test.
        db = TestAccountManagerDatabase.buildMultiThreaded()
        addressWithKeysDao = spyk(db.addressWithKeysDao())

        coEvery { sessionProvider.getSessionId(TestAccounts.User1.account.userId) } returns TestAccounts.session1Id
        coEvery { sessionProvider.getSessionId(TestAccounts.User2.account.userId) } returns TestAccounts.session2Id
        every { apiManagerFactory.create(any(), interfaceClass = UserApi::class) } returns TestApiManager(userApi)
        every { apiManagerFactory.create(any(), interfaceClass = AddressApi::class) } returns TestApiManager(addressApi)

        val dispatcherProvider = TestDispatcherProvider(UnconfinedTestDispatcher())
        val scopeProvider = TestCoroutineScopeProvider(dispatcherProvider)

        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, dispatcherProvider)

        userRepository = UserRepositoryImpl(
            db,
            apiProvider,
            context,
            cryptoContext,
            product,
            validateServerProof,
            scopeProvider
        )

        userAddressKeySecretProvider = UserAddressKeySecretProvider(
            userRepository,
            cryptoContext
        )

        userAddressRepository = UserAddressRepositoryImpl(
            db,
            apiProvider,
            userRepository,
            userAddressKeySecretProvider,
            cryptoContext,
            scopeProvider
        )

        // Needed to addAccount (User.userId foreign key -> Account.userId).
        accountManager = AccountManagerImpl(
            Product.Mail,
            AccountRepositoryImpl(Product.Mail, db, cryptoContext.keyStoreCrypto),
            mockk(relaxed = true),
            mockk(relaxed = true),
            TestSessionListener()
        )

        // Before fetching any User, account need to be added to AccountManager (if not -> foreign key exception).
        runBlocking {
            accountManager.addAccount(TestAccounts.User1.account, TestAccounts.session1)
            accountManager.addAccount(TestAccounts.User2.account, TestAccounts.session2)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAddresses() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // First we need the User in DB.
        userRepository.getUser(TestUsers.User1.id, refresh = true)

        // WHEN
        userAddressRepository.getAddressesFlow(TestUsers.User1.id, refresh = true).test {
            // THEN
            val processing = awaitItem()
            assertIs<DataResult.Processing>(processing)

            val success = awaitItem()
            assertIs<DataResult.Success<List<UserAddress>>>(success)
            assertEquals(1, success.value.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeAddresses() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // First we need the User in DB.
        userRepository.getUser(TestUsers.User1.id, refresh = true)

        // WHEN
        userAddressRepository.observeAddresses(TestUsers.User1.id, refresh = true).test {
            // THEN
            val empty = awaitItem()
            assertEquals(0, empty.size)

            val data = awaitItem()
            assertEquals(1, data.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAddressesBlocking() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User1.id, refresh = true)

        // THEN
        assertNotNull(addresses)
        assertEquals(1, addresses.size)
    }

    @Test
    fun getAddressesBlocking_useKeys_userLocked() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User1.id, refresh = true)

        // THEN
        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            // Cannot encrypt/decrypt as UserAddressKey are inactive.
            assertFailsWith(CryptoException::class) { encryptText(message) }
        }
        Unit
    }

    @Test
    fun getAddressesBlocking_useKeys_userUnLocked() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(TestUsers.User1.id)
        userRepository.setPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User1.id, refresh = true)

        // THEN
        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            val encryptedText = encryptText(message)
            val decryptedText = decryptText(encryptedText)

            assertEquals(message, decryptedText)
        }
    }

    @Test
    fun getAddressesBlocking_useKeys_userUnLocked_token_signature() = runTest {
        // GIVEN
        val encryptedWithTestKeysKey1PrivateKey =
            """
            -----BEGIN PGP MESSAGE-----
            Version: ProtonMail

            wcBMA5kajsUECZmgAQgAgJuGP/0+pUPu24mWeviRQ79s6fKKsKh6y1aBXwJM
            eQ8mSaLvHNSaCa8s9yozs9gWo2/Uf8Lpmqb70SMh2npwI5hyOFqXsrMEoEHn
            KTf86kSHnGZEtwrScXnekJjO1rfYynnAYuppTfpUc2E/uGZg6RChlwPbBZMw
            tOk8n6iL6u0+Ren9fxAmmMTw66vc5PDejmfAgzbdxeD7qV8wzqmipgiErk/w
            dPEzI5QGtGXUwsDfJeSGEdCslN1kHtZRj2B3tg6Ms7Ea/VIb3Kq6uyn2hQhS
            MlWwjzauF5mryV4Kbi1RP6yTykbPnRz6ia22HwbWzOVJ2Nu534RqNYA/99Bd
            G9JcAXjM6al21XdX0ZQww2R0Of3VzFVwQX+RSG1SWGq11u2nu5iXBVUJDa5x
            MS2SksqmW3Bh7Tbz2zlrCNZxH8USiAxXt/3xjwNlXgCg4b8sKNHNN4+Wa6S8
            HNwbYAc=
            =9RxF
            -----END PGP MESSAGE-----
            """.trimIndent()

        val expected = "Test PGP/MIME Message\n\n\n"

        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User2.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User2.Address1.response))
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(TestUsers.User2.id)
        userRepository.setPassphrase(TestUsers.User2.id, TestUsers.User2.Key1.passphrase)

        // WHEN
        val user = userRepository.getUser(TestUsers.User2.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User2.id, refresh = true)

        // THEN
        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            val encryptedText = encryptText(message)
            val decryptedText = decryptText(encryptedText)

            assertEquals(message, decryptedText)

            val decryptedWithAddressPrivateKey = decryptText(encryptedWithTestKeysKey1PrivateKey)
            assertEquals(expected, decryptedWithAddressPrivateKey)
        }
    }

    @Test
    fun getAddressesBlocking_useKeys_userLocked_token_signature(): Unit = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User2.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User2.Address1.response))
        }

        // WHEN
        val user = userRepository.getUser(TestUsers.User2.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User2.id, refresh = true)

        // THEN
        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            // Cannot encrypt/decrypt as UserAddressKey are inactive.
            assertFailsWith(CryptoException::class) { encryptText(message) }
        }
    }

    @Test
    fun assert_flags_canVerify_canEncrypt_isActive_primary_address() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(
                listOf(
                    TestAddresses.User1.Address1.response,
                    TestAddresses.User1.Address2.response
                )
            )
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(TestUsers.User1.id)
        userRepository.setPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User1.id, refresh = true)
        val primary = assertNotNull(addresses.primary())

        // THEN
        val key1 = primary.keys.first { it.keyId.id == TestAddresses.User1.Address1.Key1.response.id }
        val key2 = primary.keys.first { it.keyId.id == TestAddresses.User1.Address1.Key2Inactive.response.id }

        assertTrue(key1.active)
        assertTrue(key1.privateKey.isActive)
        assertTrue(key1.privateKey.canVerify)
        assertTrue(key1.privateKey.canEncrypt)
        assertTrue(key1.canVerify())
        assertTrue(key1.canEncrypt())

        assertFalse(key2.active)
        assertFalse(key2.privateKey.isActive)
        assertTrue(key2.privateKey.canVerify)
        assertFalse(key2.privateKey.canEncrypt)
        assertTrue(key2.canVerify())
        assertFalse(key2.canEncrypt())

        primary.useKeys(cryptoContext) {
            val message = "message"

            val encryptedText = encryptText(message)
            val signature = signText(message)
            val decryptedText = decryptText(encryptedText)

            assertEquals(message, decryptedText)
            assertTrue(verifyText(decryptedText, signature))
        }
    }

    @Test
    fun assert_flags_canVerify_canEncrypt_isActive_secondary_address() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(
                listOf(
                    TestAddresses.User1.Address1.response,
                    TestAddresses.User1.Address2.response
                )
            )
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(TestUsers.User1.id)
        userRepository.setPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User1.id, refresh = true)
        val secondary = assertNotNull(addresses[1])

        // THEN
        val key3 = secondary.keys.first { it.keyId.id == TestAddresses.User1.Address2.Key3.response.id }
        val key4 = secondary.keys.first { it.keyId.id == TestAddresses.User1.Address2.Key4.response.id }

        assertTrue(key3.active)
        assertTrue(key3.privateKey.isActive)
        assertFalse(key3.privateKey.canVerify)
        assertTrue(key3.privateKey.canEncrypt)
        assertFalse(key3.canVerify())
        assertTrue(key3.canEncrypt())

        assertFalse(key4.active)
        assertFalse(key4.privateKey.isActive)
        assertTrue(key4.privateKey.canVerify)
        assertTrue(key4.privateKey.canEncrypt)
        assertTrue(key4.canVerify())
        assertTrue(key4.canEncrypt())

        secondary.useKeys(cryptoContext) {
            val message = "message"

            val encryptedText = encryptText(message)
            val signature = signText(message)
            val decryptedText = decryptText(encryptedText)

            assertEquals(message, decryptedText)
            assertFalse(verifyText(decryptedText, signature))
        }
    }

    @Test
    fun assert_not_unlockable_are_not_active_even_if_specified() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address3.response))
        }

        // Fetch User (add to cache/DB) and set passphrase -> unlock User.
        userRepository.getUser(TestUsers.User1.id)
        userRepository.setPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userAddressRepository.getAddresses(TestUsers.User1.id, refresh = true)
        val address = addresses.first()

        // THEN
        val key5 = address.keys.first { it.keyId.id == TestAddresses.User1.Address3.Key5Normal.response.id }
        val key6 = address.keys.first { it.keyId.id == TestAddresses.User1.Address3.Key6Suspicious.response.id }

        assertTrue(key5.active)
        assertTrue(key5.privateKey.isActive)

        assertTrue(key6.active)
        assertFalse(key6.privateKey.isActive)
    }

    @Test
    fun assert_remote_api_called_only_the_first_time_then_rely_on_db() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(emptyList())
        }

        // Fetch User (add to cache/DB).
        userRepository.getUser(TestUsers.User1.id)

        // THEN

        // Nothing in DB.
        assertTrue(addressWithKeysDao.getByUserId(TestUsers.User1.id).isEmpty())
        assertTrue(userAddressRepository.getAddresses(TestUsers.User1.id).isEmpty())
        coVerify(exactly = 1) { addressApi.getAddresses() }

        // FetchedTag in DB.
        assertTrue(addressWithKeysDao.getByUserId(TestUsers.User1.id).all { it.toUserAddress().isFetchedTag() })

        // Still no addresses, no api call.
        assertTrue(userAddressRepository.getAddresses(TestUsers.User1.id).isEmpty())
        coVerify(exactly = 1) { addressApi.getAddresses() } // Still only 1.

        // Still FetchedTag in DB.
        assertTrue(addressWithKeysDao.getByUserId(TestUsers.User1.id).all { it.toUserAddress().isFetchedTag() })
    }
}
