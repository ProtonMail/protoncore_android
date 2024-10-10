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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.Key
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeySalt
import me.proton.core.key.domain.extension.updatePrivateKeyPassphraseOrNull
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.key.domain.repository.PrivateKeyRepository
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class UserManagerImplTest {

    private val userIdMigrated = UserId("userIdMigrated")
    private val userIdNotMigrated = UserId("userIdNotMigrated")
    private val password = PlainByteArray("password".toByteArray())
    private val salt = "keySalt"
    private val privateKeyMock = mockk<PrivateKey> {
        every { key } returns "privateKey"
        every { isPrimary } returns true
        every { passphrase } returns EncryptedByteArray("password".toByteArray())
    }
    private val userPrivateKey = mockk<PrivateKey> {
        every { key } returns "userPrivateKey"
        every { isPrimary } returns true
        every { passphrase } returns EncryptedByteArray("password".toByteArray())
        every { this@mockk.copy(passphrase = any()) } returns privateKeyMock
    }
    private val userPrimaryKey = mockk<UserKey> {
        every { keyId } returns KeyId("keyId")
        every { privateKey } returns userPrivateKey
    }
    private val userMigrated = mockk<User> {
        every { userId } returns userIdMigrated
        every { keys } returns listOf(userPrimaryKey)
        every { role } returns Role.OrganizationAdmin
    }
    private val userNotMigrated = mockk<User> {
        every { userId } returns userIdNotMigrated
        every { keys } returns listOf(userPrimaryKey)
        every { role } returns Role.NoOrganization
    }
    private val privateKeySalt = mockk<PrivateKeySalt> {
        every { keyId } returns KeyId("keyId")
        every { keySalt } returns salt
    }
    private val addressKeyMigrated = mockk<UserAddressKey> {
        every { keyId } returns KeyId("keyId")
        every { token } returns "token"
        every { signature } returns "signature"
        every { privateKey } returns privateKeyMock
    }
    private val addressKeyNotMigrated = mockk<UserAddressKey> {
        every { keyId } returns KeyId("keyId")
        every { token } returns null
        every { signature } returns null
        every { privateKey } returns privateKeyMock
    }
    private val addressMigrated = mockk<UserAddress> {
        every { userId } returns userIdMigrated
        every { keys } returns listOf(addressKeyMigrated)
    }
    private val addressNotMigrated = mockk<UserAddress> {
        every { userId } returns userIdNotMigrated
        every { keys } returns listOf(addressKeyNotMigrated)
    }

    private val userRepository: UserRepository = mockk(relaxed = true) {
        coEvery { this@mockk.getUser(userIdMigrated, any()) } returns userMigrated
        coEvery { this@mockk.getUser(userIdNotMigrated, any()) } returns userNotMigrated
    }
    private val userAddressRepository: UserAddressRepository = mockk(relaxed = true) {
        coEvery { this@mockk.getAddresses(userIdMigrated, any()) } returns listOf(addressMigrated)
        coEvery { this@mockk.getAddresses(userIdNotMigrated, any()) } returns listOf(addressNotMigrated)
    }
    private val passphraseRepository: PassphraseRepository = mockk(relaxed = true)
    private val keySaltRepository: KeySaltRepository = mockk(relaxed = true) {
        coEvery { this@mockk.getKeySalts(any(), any()) } returns listOf(privateKeySalt)
    }
    private val privateKeyRepository: PrivateKeyRepository = mockk(relaxed = true)
    private val accountRecoveryRepository: AccountRecoveryRepository = mockk(relaxed = true)
    private val userAddressKeySecretProvider: UserAddressKeySecretProvider = mockk(relaxed = true)
    private val pgpCryptoMock: PGPCrypto = mockk(relaxed = true) {
        every { this@mockk.unlock(any(), any()) } returns mockk()
        every { this@mockk.updatePrivateKeyPassphrase(any(), any(), any()) } answers { firstArg() }
    }
    private val cryptoContext: CryptoContext = mockk(relaxed = true) {
        every { pgpCrypto } returns pgpCryptoMock
    }

    private lateinit var manager: UserManagerImpl

    @BeforeTest
    fun setUp() {
        manager = UserManagerImpl(
            userRepository = userRepository,
            userAddressRepository = userAddressRepository,
            passphraseRepository = passphraseRepository,
            keySaltRepository = keySaltRepository,
            privateKeyRepository = privateKeyRepository,
            accountRecoveryRepository = accountRecoveryRepository,
            userAddressKeySecretProvider = userAddressKeySecretProvider,
            cryptoContext = cryptoContext,
            generateSignedKeyList = mockk(relaxed = true),
            signedKeyListChangeListener = mockk(relaxed = true),
            getEncryptedSecret = mockk(relaxed = true)
        )
    }

    @Test
    fun addUser() = runTest {
        // Given
        val user = mockk<User>()
        val userAddresses = listOf(mockk<UserAddress>())
        // When
        manager.addUser(user, userAddresses)
        // Then
        coVerify { userRepository.addUser(user) }
        coVerify { userAddressRepository.addAddresses(userAddresses) }
    }

    @Test
    fun observeUser() = runTest {
        // When
        manager.observeUser(userIdMigrated, refresh = false)
        // Then
        coVerify { userRepository.observeUser(userIdMigrated, refresh = false) }
    }

    @Test
    fun getUser() = runTest {
        // When
        manager.getUser(userIdMigrated, refresh = false)
        // Then
        coVerify { userRepository.getUser(userIdMigrated, refresh = false) }
    }

    @Test
    fun observeAddresses() = runTest {
        // When
        manager.observeAddresses(userIdMigrated, refresh = false)
        // Then
        coVerify { userAddressRepository.observeAddresses(userIdMigrated, refresh = false) }
    }

    @Test
    fun getAddresses() = runTest {
        // When
        manager.getAddresses(userIdMigrated, refresh = false)
        // Then
        coVerify { userAddressRepository.getAddresses(userIdMigrated, refresh = false) }
    }

    @Test
    fun unlockWithPassword() = runTest {
        // When
        manager.unlockWithPassword(userIdMigrated, password, refreshKeySalts = false)
        // Then
        coVerify { keySaltRepository.getKeySalts(userIdMigrated, refresh = false) }
    }

    @Test
    fun unlockWithPasswordRefreshKeySalt() = runTest {
        // When
        manager.unlockWithPassword(userIdMigrated, password, refreshKeySalts = true)
        // Then
        coVerify { keySaltRepository.getKeySalts(userIdMigrated, refresh = true) }
    }

    @Test
    fun unlockWithPasswordsReturnSuccess() = runTest {
        // When
        val result = manager.unlockWithPassword(userIdMigrated, password)
        // Then
        assertIs<UserManager.UnlockResult.Success>(result)
        coVerify { passphraseRepository.setPassphrase(userIdMigrated, any()) }
    }

    @Test
    fun unlockWithPasswordsReturnNoPrimaryKey() = runTest {
        // Given
        every { userMigrated.keys } returns emptyList()
        // When
        val result = manager.unlockWithPassword(userIdMigrated, password)
        // Then
        assertIs<UserManager.UnlockResult.Error.NoPrimaryKey>(result)
    }

    @Test
    fun unlockWithPasswordsReturnNoKeySaltsForPrimaryKey() = runTest {
        // Given
        coEvery { keySaltRepository.getKeySalts(any(), any()) } returns emptyList()
        // When
        val result = manager.unlockWithPassword(userIdMigrated, password)
        // Then
        assertIs<UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey>(result)
    }

    @Test
    fun unlockWithPasswordsReturnInvalidPassphrase() = runTest {
        // Given
        every { pgpCryptoMock.unlock(any(), any()) } throws CryptoException("InvalidPassphrase")
        // When
        val result = manager.unlockWithPassword(userIdMigrated, password)
        // Then
        assertIs<UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase>(result)
    }

    @Test
    fun lock() = runTest {
        // When
        manager.lock(userIdMigrated)
        // Then
        coVerify { passphraseRepository.clearPassphrase(userIdMigrated) }
    }

    @Test
    fun changePassword() = runTest {
        // Given
        val spyManager = spyk(manager)
        // When
        val result = spyManager.changePassword(
            userId = userIdMigrated,
            newPassword = "encrypted",
            secondFactorProof = SecondFactorProof.SecondFactorCode("code"),
            proofs = mockk(),
            srpSession = "srp",
            auth = mockk(),
            encryptedSecret = null
        )
        // Then
        assertFalse(result)
        coVerify { userAddressRepository.getAddresses(userIdMigrated, refresh = true) }
        coVerify { userRepository.getUser(userIdMigrated, refresh = true) }
        coVerify { spyManager.lock(userIdMigrated) }
        coVerify { spyManager.unlockWithPassphrase(userIdMigrated, any()) }
    }

    @Test
    fun changePasswordMigratedAccount() = runTest {
        // Given
        val expectedUserKeys = userMigrated.keys.map { Key(it.keyId, it.privateKey.key) }
        // When
        manager.changePassword(
            userId = userIdMigrated,
            newPassword = "encrypted",
            secondFactorProof = SecondFactorProof.SecondFactorCode("code"),
            proofs = mockk(),
            srpSession = "srp",
            auth = mockk(),
            encryptedSecret = null
        )
        // Then
        coVerify {
            privateKeyRepository.updatePrivateKeys(
                sessionUserId = userIdMigrated,
                keySalt = any(),
                srpProofs = any(),
                srpSession = any(),
                secondFactorProof = any(),
                auth = any(),
                keys = null,
                userKeys = expectedUserKeys,
            )
        }
    }

    @Test
    fun changePasswordNotMigratedAccount() = runTest {
        // Given
        val expectedUserKeys = userMigrated.keys.map { Key(it.keyId, it.privateKey.key) }
        val expectedAddressKeys = addressNotMigrated.keys.map { Key(it.keyId, it.privateKey.key) }
        // When
        manager.changePassword(
            userId = userIdNotMigrated,
            newPassword = "encrypted",
            secondFactorProof = SecondFactorProof.SecondFactorCode("code"),
            proofs = mockk(),
            srpSession = "srp",
            auth = mockk(),
            encryptedSecret = null
        )
        // Then
        coVerify {
            privateKeyRepository.updatePrivateKeys(
                sessionUserId = userIdNotMigrated,
                keySalt = any(),
                srpProofs = any(),
                srpSession = any(),
                secondFactorProof = any(),
                auth = any(),
                keys = expectedUserKeys + expectedAddressKeys,
                userKeys = null,
            )
        }
    }

    @Test
    fun changePasswordOrgPrivateKey() = runTest {
        // When
        manager.changePassword(
            userId = userIdMigrated,
            newPassword = "encrypted",
            secondFactorProof = SecondFactorProof.SecondFactorCode("code"),
            proofs = mockk(),
            srpSession = "srp",
            auth = mockk(),
            encryptedSecret = null
        )
        // Then
        coVerify {
            privateKeyRepository.updatePrivateKeys(
                sessionUserId = userIdMigrated,
                keySalt = any(),
                srpProofs = any(),
                srpSession = any(),
                secondFactorProof = any(),
                auth = any(),
                keys = any(),
                userKeys = any(),
            )
        }
    }

    @Test
    fun setupPrimaryKeys() = runTest {
        // When
        manager.setupPrimaryKeys(
            sessionUserId = userIdMigrated,
            username = "username",
            domain = "domain",
            auth = mockk(),
            password = "password".toByteArray()
        )
        // Then
        coVerify { userAddressRepository.getAddresses(userIdMigrated, refresh = true) }
        coVerify { userRepository.getUser(userIdMigrated, refresh = true) }
    }

    @Test
    fun resetPasswordMigrated() = runTest {
        // Given
        mockkStatic("me.proton.core.key.domain.extension.UpdatePrivateKeyKt")
        every { userPrimaryKey.updatePrivateKeyPassphraseOrNull(any(), any()) } returns mockk(relaxed = true)
        coEvery { pgpCryptoMock.generateNewKeySalt() } returns "test-key-salt"
        // When
        manager.resetPassword(
            sessionUserId = userIdMigrated,
            newPassword = "test-new-pass",
            auth = mockk()
        )
        // Then
        coVerify { userAddressRepository.getAddresses(userIdMigrated, refresh = true) }
        coVerify { userRepository.getUser(userIdMigrated, refresh = true) }
        coVerify {
            accountRecoveryRepository.resetPassword(
                userIdMigrated,
                "test-key-salt",
                any(),
                any()
            )
        }
        verify { userPrimaryKey.updatePrivateKeyPassphraseOrNull(any(), any()) }
        unmockkStatic("me.proton.core.key.domain.extension.UpdatePrivateKeyKt")
    }

    @Test
    fun resetPasswordNotMigrated() = runTest {
        // Given
        mockkStatic("me.proton.core.key.domain.extension.UpdatePrivateKeyKt")
        every { userPrimaryKey.updatePrivateKeyPassphraseOrNull(any(), any()) } returns mockk(relaxed = true)
        // When
        manager.resetPassword(
            sessionUserId = userIdNotMigrated,
            newPassword = "test-new-pass",
            auth = null
        )
        // Then
        coVerify { userAddressRepository.getAddresses(userIdNotMigrated, refresh = true) }
        coVerify { userRepository.getUser(userIdNotMigrated, refresh = true) }
        verify(exactly = 0) { userPrimaryKey.updatePrivateKeyPassphraseOrNull(any(), any()) }
        unmockkStatic("me.proton.core.key.domain.extension.UpdatePrivateKeyKt")
    }

    @Test
    fun reactivateKeys() = runTest {
        // GIVEN
        val testPrivateKeyMock = mockk<PrivateKey> {
            every { key } returns "privateKey"
            every { isPrimary } returns true
            every { passphrase } returns EncryptedByteArray("password".toByteArray())
            every { isActive } returns true
            every { canEncrypt } returns true
            every { canVerify } returns true
        }
        val testUserKeyMock = mockk<UserKey> {
            every { userId } returns userIdMigrated
            every { keyId } returns KeyId("keyId")
            every { privateKey } returns testPrivateKeyMock
        }

        val userAddressKey = mockk<UserAddressKey>(relaxed = true) {
            every { keyId } returns KeyId("keyId")
            every { token } returns "token"
            every { signature } returns "signature"
            every { privateKey } returns testPrivateKeyMock
            every { active } returns false
        }
        val addressMigrated = mockk<UserAddress> {
            every { userId } returns userIdMigrated
            every { keys } returns listOf(userAddressKey)
            every { addressId } returns AddressId("test-address-id")
        }
        val userAddressRepository: UserAddressRepository = mockk(relaxed = true) {
            coEvery { this@mockk.getAddresses(userIdMigrated, any()) } returns listOf(addressMigrated)
        }
        coEvery { userAddressKeySecretProvider.getPassphrase(userIdMigrated, any(), userAddressKey) } returns mockk(relaxed = true)

        manager = UserManagerImpl(
            userRepository = userRepository,
            userAddressRepository = userAddressRepository,
            passphraseRepository = passphraseRepository,
            keySaltRepository = keySaltRepository,
            privateKeyRepository = privateKeyRepository,
            accountRecoveryRepository = accountRecoveryRepository,
            userAddressKeySecretProvider = userAddressKeySecretProvider,
            cryptoContext = cryptoContext,
            generateSignedKeyList = mockk(relaxed = true),
            signedKeyListChangeListener = mockk(relaxed = true),
            getEncryptedSecret = mockk(relaxed = true)
        )
        // When
        val result = manager.reactivateKey(testUserKeyMock)
        // Then
        assertNotNull(result)
    }
}
