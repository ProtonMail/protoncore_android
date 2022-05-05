/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes.APP_VERSION_BAD
import me.proton.core.network.domain.ResponseCodes.NOT_ALLOWED
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SetupPrimaryKeysTest {
    private val decryptedPassword = "decrypted-password"
    private val encryptedPassword: EncryptedString = "encrypted-password"
    private val testUsername = "test-username"
    private val testDomain = "example.com"
    private val testEmail = "$testUsername@$testDomain"
    private val testModulus = Modulus(modulusId = "test-id", modulus = "test-modulus")
    private val testUserId = UserId("test-user-id")
    private val testAuth = Auth(
        version = 0,
        modulusId = testModulus.modulusId,
        salt = "test-salt",
        verifier = "test-verifier"
    )

    private lateinit var userManager: UserManager
    private lateinit var userAddressRepository: UserAddressRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var domainRepository: DomainRepository
    private lateinit var srpCrypto: SrpCrypto
    private lateinit var keyStoreCrypto: KeyStoreCrypto
    private lateinit var tested: SetupPrimaryKeys

    @Before
    fun setUp() {
        userManager = mockk()
        userAddressRepository = mockk()
        authRepository = mockk()
        domainRepository = mockk()
        srpCrypto = mockk()
        keyStoreCrypto = mockk()
        tested = SetupPrimaryKeys(
            userManager,
            userAddressRepository,
            authRepository,
            domainRepository,
            srpCrypto,
            keyStoreCrypto
        )
    }

    @Test
    fun `primary key already exists`() = runBlockingTest {
        userManager.mockGetUser(mockUser(withPrimaryPrivateKey = true))

        tested.invoke(testUserId, encryptedPassword, mockk())

        coVerify { userManager.setupPrimaryKeys(any(), any(), any(), any(), any()) wasNot Called }
        coVerify { userAddressRepository wasNot Called }
        coVerify { authRepository wasNot Called }
        coVerify { domainRepository wasNot Called }
        coVerify { srpCrypto wasNot Called }
        coVerify { keyStoreCrypto wasNot Called }
    }

    @Test
    fun `setup primary keys for internal account`() = runBlockingTest {
        authRepository.mockRandomModulus()
        domainRepository.mockGetAvailableDomains()
        keyStoreCrypto.mockDecrypt()
        srpCrypto.mockCalculatePasswordVerifier(testEmail)
        userAddressRepository.mockCreateAddress(displayName = testUsername)
        userAddressRepository.mockGetAddress()
        userManager.mockGetUser(mockUser(username = testUsername))
        userManager.mockSetupPrimaryKeys(testUsername)

        tested.invoke(testUserId, encryptedPassword, AccountType.Internal)

        coVerify(exactly = 1) { userAddressRepository.createAddress(testUserId, testUsername, testDomain) }
        coVerify(exactly = 1) {
            userManager.setupPrimaryKeys(
                testUserId,
                testUsername,
                testDomain,
                testAuth,
                withArg { it contentEquals decryptedPassword.toByteArray() }
            )
        }
    }

    @Test
    fun `setup primary keys for external account`() = runBlockingTest {
        authRepository.mockRandomModulus()
        domainRepository.mockGetAvailableDomains()
        keyStoreCrypto.mockDecrypt()
        srpCrypto.mockCalculatePasswordVerifier(testEmail)
        userAddressRepository.mockCreateAddress(displayName = testUsername)
        userAddressRepository.mockGetAddress()
        userManager.mockGetUser(mockUser(userEmail = testEmail))
        userManager.mockSetupPrimaryKeys(testUsername)

        tested.invoke(testUserId, encryptedPassword, AccountType.External)

        coVerify { userAddressRepository wasNot Called }
        coVerify(exactly = 1) {
            userManager.setupPrimaryKeys(
                testUserId,
                testUsername,
                testDomain,
                testAuth,
                withArg { it contentEquals decryptedPassword.toByteArray() }
            )
        }
    }

    @Test
    fun `fails to recover from error when creating address`() = runBlockingTest {
        authRepository.mockRandomModulus()
        domainRepository.mockGetAvailableDomains()
        keyStoreCrypto.mockDecrypt()
        srpCrypto.mockCalculatePasswordVerifier(testEmail)
        val data = ApiResult.Error.ProtonData(NOT_ALLOWED, "User has already set up an address")
        val apiException = ApiException(ApiResult.Error.Http(400, "Bad request", data))
        userAddressRepository.mockCreateAddress(displayName = testUsername, withException = apiException)
        userAddressRepository.mockGetAddress()
        userManager.mockGetUser(mockUser(username = testUsername))
        userManager.mockSetupPrimaryKeys(testUsername)

        val result = assertFailsWith(ApiException::class) {
            tested.invoke(testUserId, encryptedPassword, AccountType.Internal)
        }
        assertEquals("User has already set up an address", result.message)
    }

    @Test
    fun `fails to recover from error when setting up keys`() = runBlockingTest {
        authRepository.mockRandomModulus()
        domainRepository.mockGetAvailableDomains()
        keyStoreCrypto.mockDecrypt()
        srpCrypto.mockCalculatePasswordVerifier(testEmail)
        userAddressRepository.mockCreateAddress(displayName = testUsername)
        userAddressRepository.mockGetAddress()
        userManager.mockGetUser(mockUser(username = testUsername))
        val data = ApiResult.Error.ProtonData(NOT_ALLOWED, "Primary key exists")
        val apiException = ApiException(ApiResult.Error.Http(400, "Bad request", data))
        userManager.mockSetupPrimaryKeys(testUsername, withException = apiException)

        val result = assertFailsWith(ApiException::class) {
            tested.invoke(testUserId, encryptedPassword, AccountType.Internal)
        }
        assertEquals("Primary key exists", result.message)
    }

    @Test
    fun `rethrows other exceptions`() = runBlockingTest {
        val data = ApiResult.Error.ProtonData(APP_VERSION_BAD, "Unsupported API version")
        val apiException = ApiException(ApiResult.Error.Http(400, "Bad request", data))
        coEvery { userManager.getUser(testUserId, any()) } throws apiException

        val result = assertFailsWith<ApiException> {
            tested.invoke(testUserId, encryptedPassword, mockk())
        }
        assertEquals("Unsupported API version", result.message)
    }

    //region Mock helpers

    private fun mockUser(
        userEmail: String? = null,
        username: String? = null,
        withPrimaryPrivateKey: Boolean = false
    ): User {
        return mockk {
            every { email } returns userEmail
            every { name } returns username
            every { keys } returns if (withPrimaryPrivateKey) {
                val userPrivateKey = mockk<PrivateKey> { every { isPrimary } returns true }
                val userKey = mockk<UserKey> { every { privateKey } returns userPrivateKey }
                listOf(userKey)
            } else emptyList()
        }
    }

    private fun AuthRepository.mockRandomModulus() {
        coEvery { randomModulus() } returns testModulus
    }

    private fun DomainRepository.mockGetAvailableDomains() {
        coEvery { getAvailableDomains() } returns listOf(testDomain)
    }

    private fun KeyStoreCrypto.mockDecrypt() {
        every { decrypt(encryptedPassword) } returns decryptedPassword
    }

    private fun SrpCrypto.mockCalculatePasswordVerifier(email: String) {
        every {
            calculatePasswordVerifier(
                email,
                any(),
                testModulus.modulusId,
                testModulus.modulus
            )
        } returns testAuth
    }

    private fun UserAddressRepository.mockCreateAddress(displayName: String, withException: Throwable? = null) {
        coEvery { createAddress(testUserId, displayName, testDomain) } answers {
            if (withException != null) {
                throw withException
            } else {
                mockk {
                    every { email } returns "$displayName@$testDomain"
                }
            }
        }
    }

    private fun UserAddressRepository.mockGetAddress(vararg responses: List<UserAddress>) {
        coEvery { getAddresses(testUserId, any()) }.apply {
            if (responses.isNotEmpty()) {
                returnsMany(responses.toList())
            } else {
                returns(emptyList())
            }
        }
    }

    private fun UserManager.mockGetUser(vararg users: User) {
        coEvery { getUser(testUserId, any()) } returnsMany users.toList()
    }

    private fun UserManager.mockSetupPrimaryKeys(username: String, withException: Throwable? = null) {
        coEvery { setupPrimaryKeys(testUserId, username, testDomain, testAuth, any()) }.apply {
            if (withException != null) {
                throws(withException)
            } else {
                returns(mockk { every { userId } returns testUserId })
            }
        }
    }

    //endregion
}
