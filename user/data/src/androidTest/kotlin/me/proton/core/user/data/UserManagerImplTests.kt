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

import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.account.data.repository.AccountRepositoryImpl
import me.proton.core.accountmanager.data.AccountManagerImpl
import me.proton.core.accountmanager.data.db.AccountManagerDatabase
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.auth.domain.usecase.sso.GetEncryptedSecret
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.domain.entity.Product
import me.proton.core.key.data.api.response.AddressesResponse
import me.proton.core.key.data.api.response.UsersResponse
import me.proton.core.key.data.repository.KeySaltRepositoryImpl
import me.proton.core.key.data.repository.PrivateKeyRepositoryImpl
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.decryptTextOrNull
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.extension.areAllInactive
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.key.domain.useKeys
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.test.kotlin.UnconfinedTestCoroutineScopeProvider
import me.proton.core.user.data.api.AddressApi
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.repository.UserAddressRemoteDataSourceImpl
import me.proton.core.user.data.repository.UserAddressRepositoryImpl
import me.proton.core.user.data.repository.UserLocalDataSourceImpl
import me.proton.core.user.data.repository.UserRemoteDataSourceImpl
import me.proton.core.user.data.repository.UserRepositoryImpl
import me.proton.core.user.data.usecase.GenerateSignedKeyList
import me.proton.core.user.domain.SignedKeyListChangeListener
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRemoteDataSource
import me.proton.core.user.domain.repository.UserLocalDataSource
import me.proton.core.user.domain.repository.UserRemoteDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@Suppress("OptionalUnit")
class
UserManagerImplTests {

    @MockK(relaxed = true)
    private lateinit var sessionProvider: SessionProvider

    @MockK(relaxed = true)
    private lateinit var apiManagerFactory: ApiManagerFactory

    @MockK(relaxed = true)
    private lateinit var userApi: UserApi

    @MockK(relaxed = true)
    private lateinit var addressApi: AddressApi

    @MockK(relaxed = true)
    private lateinit var accountRecoveryRepository: AccountRecoveryRepository

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
    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userAddressRepository: UserAddressRepositoryImpl
    private lateinit var passphraseRepository: PassphraseRepository
    private lateinit var keySaltRepository: KeySaltRepositoryImpl
    private lateinit var privateKeyRepository: PrivateKeyRepository
    private lateinit var userAddressRemoteDataSource: UserAddressRemoteDataSource
    private lateinit var userAddressKeySecretProvider: UserAddressKeySecretProvider
    private val getEncryptedSecret = mockk<GetEncryptedSecret>()
    private val signedKeyListChangeListener = mockk<SignedKeyListChangeListener>()
    private val generateSignedKeyList = mockk<GenerateSignedKeyList>()

    private lateinit var userManager: UserManager

    private val product = Product.Mail
    private val validateServerProof = ValidateServerProof()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val context = InstrumentationRegistry.getInstrumentation().context

        // Build a new fresh in memory Database, for each test.
        db = TestAccountManagerDatabase.buildMultiThreaded()

        coEvery { sessionProvider.getSessionId(TestUsers.User1.id) } returns TestAccounts.session1Id
        coEvery { sessionProvider.getSessionId(TestUsers.User2.id) } returns TestAccounts.session2Id
        every { apiManagerFactory.create(any(), interfaceClass = UserApi::class) } returns TestApiManager(userApi)
        every { apiManagerFactory.create(any(), interfaceClass = AddressApi::class) } returns TestApiManager(addressApi)

        val dispatcherProvider = TestDispatcherProvider(UnconfinedTestDispatcher())
        val scopeProvider = TestCoroutineScopeProvider(dispatcherProvider)

        apiProvider = ApiProvider(
            apiManagerFactory = apiManagerFactory,
            sessionProvider = sessionProvider,
            dispatcherProvider = dispatcherProvider
        )

        keySaltRepository = KeySaltRepositoryImpl(
            db = db,
            provider = apiProvider,
            scopeProvider = scopeProvider
        )
        privateKeyRepository = PrivateKeyRepositoryImpl(
            provider = apiProvider,
            validateServerProof = validateServerProof
        )

        userLocalDataSource = UserLocalDataSourceImpl(
            cryptoContext = cryptoContext,
            db = db
        )
        userRemoteDataSource = UserRemoteDataSourceImpl(
            apiProvider = apiProvider,
            userLocalDataSource = userLocalDataSource
        )

        // UserRepositoryImpl implements PassphraseRepository.
        userRepository = UserRepositoryImpl(
            provider = apiProvider,
            context = context,
            product = product,
            validateServerProof = validateServerProof,
            scopeProvider = scopeProvider,
            userLocalDataSource = userLocalDataSource,
            userRemoteDataSource = userRemoteDataSource
        )
        passphraseRepository = userRepository

        userAddressRemoteDataSource = UserAddressRemoteDataSourceImpl(
            apiProvider = apiProvider,
            userLocalDataSource = userLocalDataSource
        )

        userAddressKeySecretProvider = UserAddressKeySecretProvider(
            passphraseRepository = userRepository,
            cryptoContext = cryptoContext
        )

        // UserManagerImpl need UserAddressRepository.
        userAddressRepository = UserAddressRepositoryImpl(
            db = db,
            userRepository = userRepository,
            userAddressRemoteDataSource = userAddressRemoteDataSource,
            userAddressKeySecretProvider = userAddressKeySecretProvider,
            context = cryptoContext,
            scopeProvider = scopeProvider
        )

        // Implementation we want to test.
        userManager = UserManagerImpl(
            userRepository = userRepository,
            userAddressRepository = userAddressRepository,
            passphraseRepository = passphraseRepository,
            keySaltRepository = keySaltRepository,
            privateKeyRepository = privateKeyRepository,
            accountRecoveryRepository = accountRecoveryRepository,
            userAddressKeySecretProvider = userAddressKeySecretProvider,
            cryptoContext = cryptoContext,
            generateSignedKeyList = generateSignedKeyList,
            signedKeyListChangeListener = Optional.of(signedKeyListChangeListener),
            getEncryptedSecret = getEncryptedSecret
        )

        // Needed to addAccount (User.userId foreign key -> Account.userId).
        accountManager = AccountManagerImpl(
            product = Product.Mail,
            scopeProvider = UnconfinedTestCoroutineScopeProvider(),
            accountRepository = AccountRepositoryImpl(Product.Mail, db, cryptoContext.keyStoreCrypto),
            authRepository = mockk(relaxed = true),
            userManager = userManager,
            sessionListener = TestSessionListener()
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
    fun unlockWithPassphrase() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val result = userManager.unlockWithPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // THEN
        assertIs<UserManager.UnlockResult.Success>(result)

        val user = userManager.observeUser(TestUsers.User1.id)
            .filterNot { it?.keys?.areAllInactive() ?: true }
            .firstOrNull()

        assertNotNull(user)

        val passphrase = passphraseRepository.getPassphrase(TestUsers.User1.id)
        assertNotNull(passphrase)
        assertEquals(TestUsers.User1.Key1.passphrase, passphrase)
    }

    @Test
    fun getUser_useKeys_unlocked() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Unlock UserKey.
        userManager.unlockWithPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val user = userManager.observeUser(TestUsers.User1.id)
            .filterNot { it?.keys?.areAllInactive() ?: true }
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
    fun observeUser_useKeys_unlocked() = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // Unlock UserKey.
        userManager.unlockWithPassphrase(TestUsers.User1.id, TestUsers.User1.Key1.passphrase)

        // WHEN
        val user = userManager.observeUser(TestUsers.User1.id)
            .filterNot { it?.keys?.areAllInactive() ?: true }
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
    fun getUser_useKeys_locked(): Unit = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val user = userManager.observeUser(TestUsers.User1.id)
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(user)

        user.useKeys(cryptoContext) {
            val message = "message"

            // Cannot encrypt/decrypt as UserKey are inactive (cannot be unlocked).
            assertFailsWith(CryptoException::class) { encryptText(message) }
        }
    }

    @Test
    fun observeUser_useKeys_locked(): Unit = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }

        // WHEN
        val user = userManager.observeUser(TestUsers.User1.id)
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(user)

        user.useKeys(cryptoContext) {
            val message = "message"

            // Cannot encrypt/decrypt as UserKey are inactive (cannot be unlocked).
            assertFailsWith(CryptoException::class) { encryptText(message) }
        }
    }

    @Test
    fun getAddresses_useKeys_locked(): Unit = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userManager.observeAddresses(TestUsers.User1.id, refresh = true)
            .filter { it.isNotEmpty() }
            .filterNotNull()
            .firstOrNull()

        // THEN
        assertNotNull(addresses)
        assertEquals(1, addresses.size)

        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            // Cannot encrypt/decrypt as UserAddressKey are inactive (cannot be unlocked).
            assertFailsWith(CryptoException::class) { encryptText(message) }
        }
    }

    @Test
    fun observeAddresses_useKeys_locked() : Unit = runTest {
        // GIVEN
        coEvery { userApi.getUsers() } answers {
            UsersResponse(TestUsers.User1.response)
        }
        coEvery { addressApi.getAddresses() } answers {
            AddressesResponse(listOf(TestAddresses.User1.Address1.response))
        }

        // WHEN
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userManager.observeAddresses(TestUsers.User1.id, refresh = true)
            .filter { it.isNotEmpty() }
            .firstOrNull()

        // THEN
        assertNotNull(addresses)
        assertEquals(1, addresses.size)

        addresses.primary()!!.useKeys(cryptoContext) {
            val message = "message"

            // Cannot encrypt/decrypt as UserAddressKey are inactive (cannot be unlocked).
            assertFailsWith(CryptoException::class) { encryptText(message) }
        }
    }

    @Test
    fun getAddresses_useKeys_unlocked() = runTest {
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
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userManager.observeAddresses(TestUsers.User1.id, refresh = true)
            .filter { it.isNotEmpty() }
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
    fun observeAddresses_useKeys_unlocked(): Unit = runTest {
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
        val user = userRepository.getUser(TestUsers.User1.id, refresh = true)
        val addresses = userManager.observeAddresses(TestUsers.User1.id, refresh = true)
            .filter { it.isNotEmpty() }
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
    fun getAddresses_useKeys_unlocked_token_signature() = runTest {
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
        val user = userRepository.getUser(TestUsers.User2.id, refresh = true)
        val addresses = userManager.observeAddresses(TestUsers.User2.id, refresh = true)
            .filter { it.isNotEmpty() }
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

    @Test
    fun observeAddresses_useKeys_unlocked_token_signature() = runTest {
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
        val user = userRepository.getUser(TestUsers.User2.id, refresh = true)
        val addresses = userManager.observeAddresses(TestUsers.User2.id, refresh = true)
            .filter { it.isNotEmpty() }
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
