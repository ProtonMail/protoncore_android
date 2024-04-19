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

package me.proton.core.user.data

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.repository.KeySaltRepositoryImpl
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.extension.updatePrivateKeyPassphraseOrNull
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.user.data.api.AddressApi
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.usecase.GenerateSignedKeyList
import me.proton.core.user.domain.SignedKeyListChangeListener
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertTrue

class UserManagerPasswordTests {

    // region mocks
    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val userApi = mockk<UserApi>(relaxed = true)
    private val addressApi = mockk<AddressApi>(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val passphraseRepository: PassphraseRepository = mockk(relaxed = true)
    private val keySaltRepository: KeySaltRepositoryImpl = mockk(relaxed = true)
    private val privateKeyRepository: PrivateKeyRepository = mockk(relaxed = true)
    private val accountRecoveryRepository: AccountRecoveryRepository = mockk(relaxed = true)
    private val generateSignedKeyList: GenerateSignedKeyList = mockk()
    private val signedKeyListChangeListener: SignedKeyListChangeListener = mockk()
    // endregion

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = object : KeyStoreCrypto {
            override fun isUsingKeyStore(): Boolean = false
            override fun encrypt(value: String): EncryptedString = value
            override fun decrypt(value: EncryptedString): String = value
            override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array.copyOf())
            override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array.copyOf())
        }
    )

    private lateinit var userAddressKeySecretProvider: UserAddressKeySecretProvider

    private lateinit var userManager: UserManager

    // region test data
    private val userAddressKey = UserAddressKey(
        addressId = AddressId("UserOrgAdminAddressId1"),
        version = 0,
        flags = 3,
        active = true,
        keyId = KeyId("key1"),
        privateKey = PrivateKey(
            key = TestKeys.Key1.privateKey,
            isPrimary = true,
            passphrase = EncryptedByteArray(TestKeys.Key1.passphrase),
        ),
        token = "test-token",
        signature = "test-signature"
    )

    private val userAddress = spyk(
        UserAddress(
            userId = UserId("userOrgAdminId"),
            addressId = AddressId("UserOrgAdminAddressId"),
            email = "email",
            canSend = true,
            canReceive = true,
            enabled = true,
            order = 0,
            keys = listOf(
                userAddressKey
            ),
            signedKeyList = null
        )
    )

    private val testUserId = UserId("test-user-id")
    private val encryptedPassphrase: EncryptedByteArray = mockk(relaxed = true)
    private val decryptedPassphrase: PlainByteArray = mockk(relaxed = true)
    // endregion

    @Before
    fun setup() {
        mockkStatic("me.proton.core.key.domain.extension.UpdatePrivateKeyKt")
        mockkStatic("me.proton.core.user.domain.extension.UserKt")
        mockkStatic("me.proton.core.crypto.common.keystore.EncryptedByteArrayKt")

        every { apiManagerFactory.create(any(), interfaceClass = UserApi::class) } returns TestApiManager(userApi)
        every { apiManagerFactory.create(any(), interfaceClass = AddressApi::class) } returns TestApiManager(addressApi)

        // UserRepositoryImpl implements PassphraseRepository.
        userAddressKeySecretProvider = UserAddressKeySecretProvider(
            userRepository,
            cryptoContext
        )

        val userAddressRepository = mockk<UserAddressRepository>()

        coEvery { userAddressRepository.getAddresses(any(), any()) } returns listOf(userAddress)

        userManager = UserManagerImpl(
            userRepository,
            userAddressRepository,
            passphraseRepository,
            keySaltRepository,
            privateKeyRepository,
            accountRecoveryRepository,
            userAddressKeySecretProvider,
            cryptoContext,
            generateSignedKeyList,
            Optional.of(signedKeyListChangeListener)
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("me.proton.core.key.domain.extension.UpdatePrivateKeyKt")
        unmockkStatic("me.proton.core.user.domain.extension.UserKt")
        unmockkStatic("me.proton.core.crypto.common.keystore.EncryptedByteArrayKt")
    }

    @Test
    fun changePasswordWithFaultyOrgPrivateKeyWorksProperly() = runTest {
        // GIVEN
        val pgpCrypto: PGPCrypto = mockk(relaxed = true)
        val srpCrypto: SrpCrypto = mockk(relaxed = true)
        val cryptoContext = spyk(cryptoContext)

        coEvery { srpCrypto.generateSrpProofs(any(), any(), any(), any(), any(), any()) } returns SrpProofs(
            "clientEphemeral", "clientProof", "expectedServerProof"
        )
        every { cryptoContext.srpCrypto } returns srpCrypto
        every { cryptoContext.pgpCrypto } returns pgpCrypto
        every { pgpCrypto.generateNewKeySalt() } returns "test-key-salt"
        every { pgpCrypto.getPassphrase(any(), any()) } returns "test-key-salt".toByteArray()


        every { userAddressKey.updatePrivateKeyPassphraseOrNull(any(), any()) } returns null

        val userKey1 = mockk<UserKey>(relaxed = true)
        every { userKey1.updatePrivateKeyPassphraseOrNull(any(), any()) } returns null

        val mockedUser = mockk<User>(relaxed = true).also {
            every { it.keys } returns listOf(userKey1)
            every { it.userId } returns testUserId
            every { it.role } returns Role.OrganizationAdmin
        }
        coEvery { userRepository.getUser(any(), any()) } returns mockedUser

        coEvery { passphraseRepository.getPassphrase(mockedUser.userId) } returns mockk()
        every { encryptedPassphrase.decrypt(any()) } returns decryptedPassphrase
        coEvery { passphraseRepository.clearPassphrase(mockedUser.userId) } answers { }

        coEvery {
            privateKeyRepository.updatePrivateKeys(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns true

        // WHEN
        val result = userManager.changePassword(
            userId = testUserId,
            newPassword = "new-password".encrypt(cryptoContext.keyStoreCrypto),
            proofs = SrpProofs(
                clientEphemeral = "test-client-ephemeral",
                clientProof = "test-client-proof",
                expectedServerProof = "expected-server-proof"
            ),
            srpSession = "test-srp-session",
            auth = mockk()
        )

        // THEN
        assertTrue(result)
    }
}
