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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.entity.Address
import me.proton.core.auth.domain.entity.AddressKey
import me.proton.core.auth.domain.entity.AddressType
import me.proton.core.auth.domain.entity.Auth
import me.proton.core.auth.domain.entity.KeySecurity
import me.proton.core.auth.domain.entity.KeyType
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.entity.UserKey
import me.proton.core.auth.domain.exception.EmptyCredentialsException
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class UpdateUsernameOnlyAccountTest {
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val cryptoProvider = mockk<CryptoProvider>(relaxed = true)

    private lateinit var useCase: UpdateUsernameOnlyAccount

    // region test data
    private val testSessionId = SessionId("test-session-id")
    private val testUsername = "test-username"
    private val testDomain = "test-domain"
    private val testPassphrase = "test-passphrase"
    private val testDisplayName = "test-displayName"
    private val testAddressId = "test-addressId"

    private val testPrivateKey = "test-private-key"

    private val testSignedKeyListData = "test-signedKeyListData"
    private val testSignedKeyListSignature = "test-signedKeyListSignature"

    private val testModulusId = "test-modulusId"
    private val testModulus = "test-modulus"

    private val addressResult = Address(
        id = testAddressId,
        domainId = "test-domain-id",
        email = "test-email",
        canSend = true,
        canReceive = true,
        status = 1,
        type = AddressType.ORIGINAL,
        order = 1,
        displayName = testDisplayName,
        signature = "test-signature",
        hasKeys = false,
        keys = emptyList()
    )

    private val userResult = User(
        id = "test-id",
        name = "test-name",
        usedSpace = 1,
        currency = "test-currency",
        credit = 1,
        maxSpace = 1,
        maxUpload = 1,
        role = 1,
        private = true,
        subscribed = true,
        delinquent = false,
        email = "test-email",
        displayName = testDisplayName,
        keys = listOf(
            UserKey(
                "test-key-id",
                1,
                "test-private-key",
                "test-fingerprint",
                null,
                1
            )
        )
    )

    private val authResult = Auth(1, testModulusId, "test-salt", "test-verifier")
    private val modulus = Modulus(testModulusId, testModulus)
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = UpdateUsernameOnlyAccount(authRepository, cryptoProvider)
        coEvery { authRepository.createAddress(testSessionId, testDomain, testUsername) } returns DataResult.Success(
            ResponseSource.Remote,
            addressResult
        )
        coEvery { authRepository.randomModulus() } returns DataResult.Success(ResponseSource.Remote, modulus)
        every { cryptoProvider.generateNewPrivateKey(any(), any(), any(), any(), any()) } returns testPrivateKey
        every { cryptoProvider.generateSignedKeyList(any(), testPassphrase.toByteArray()) } returns Pair(
            testSignedKeyListData,
            testSignedKeyListSignature
        )
        coEvery {
            cryptoProvider.calculatePasswordVerifier(
                testUsername,
                testPassphrase.toByteArray(),
                testModulusId,
                testModulus
            )
        } returns
            authResult
        coEvery {
            authRepository.setupAddressKeys(
                testPrivateKey,
                "",
                any(),
                Auth(1, testModulusId, "test-salt", "test-verifier")
            )
        } returns
            DataResult.Success(
                ResponseSource.Remote, userResult
            )
    }

    @Test
    fun `update username-only account happy path`() = runBlockingTest {
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Success)
    }

    @Test
    fun `update username-only account API arguments are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        val displayNameArgument = slot<String>()
        val domainArgument = slot<String>()
        val sessionIdArgument = slot<SessionId>()
        coVerify {
            authRepository.createAddress(
                capture(sessionIdArgument),
                capture(domainArgument),
                capture(displayNameArgument)
            )
        }

        assertEquals(testUsername, displayNameArgument.captured)
        assertEquals(testDomain, domainArgument.captured)
        assertEquals(testSessionId, sessionIdArgument.captured)
    }

    @Test
    fun `update username-only account addressKeyList arguments are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        val primaryKeyArgument = slot<String>()
        val keySaltArgument = slot<String>()
        val addressKeyListArgument = slot<List<AddressKey>>()
        val authArgument = slot<Auth>()
        coVerify {
            authRepository.setupAddressKeys(
                capture(primaryKeyArgument), capture(keySaltArgument), capture(addressKeyListArgument),
                capture(authArgument)
            )
        }

        assertEquals(testPrivateKey, primaryKeyArgument.captured)
        assertEquals("", keySaltArgument.captured)
        val keyList = addressKeyListArgument.captured
        assertEquals(1, keyList.size)
        val addressKey = keyList[0]
        assertEquals(testAddressId, addressKey.addressId)
        assertEquals(testSignedKeyListData, addressKey.signedKeyList.data)
        assertEquals(testSignedKeyListSignature, addressKey.signedKeyList.signature)
        val auth = authArgument.captured
        assertEquals(authResult.version, auth.version)
        assertEquals(authResult.modulusId, auth.modulusId)
        assertEquals(authResult.salt, auth.salt)
        assertEquals(authResult.verifier, auth.verifier)
    }

    @Test
    fun `update username-only account Crypto arguments are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        val usernameArgument = slot<String>()
        val domainArgument = slot<String>()
        val passphraseArgument = slot<ByteArray>()
        val keyTypeArgument = slot<KeyType>()
        val keySecurityArgument = slot<KeySecurity>()
        verify {
            cryptoProvider.generateNewPrivateKey(
                capture(usernameArgument), capture(domainArgument),
                capture(passphraseArgument), capture(keyTypeArgument), capture(keySecurityArgument)
            )
        }

        assertEquals(testUsername, usernameArgument.captured)
        assertEquals(testDomain, domainArgument.captured)
        assertEquals(testPassphrase, String(passphraseArgument.captured))
        assertEquals(KeyType.RSA, keyTypeArgument.captured)
        assertEquals(KeySecurity.HIGH, keySecurityArgument.captured)

        val keyArgument = slot<String>()
        verify { cryptoProvider.generateSignedKeyList(capture(keyArgument), capture(passphraseArgument)) }
        assertEquals(testPrivateKey, keyArgument.captured)
        assertEquals(testPassphrase, String(passphraseArgument.captured))

        val modulusIdArgument = slot<String>()
        val modulusArgument = slot<String>()
        verify {
            cryptoProvider.calculatePasswordVerifier(
                capture(usernameArgument), capture(passphraseArgument),
                capture(modulusIdArgument), capture(modulusArgument)
            )
        }
        assertEquals(testUsername, usernameArgument.captured)
        assertEquals(testPassphrase, String(passphraseArgument.captured))
        assertEquals(testModulusId, modulusIdArgument.captured)
        assertEquals(testModulus, modulusArgument.captured)
    }

    @Test
    fun `empty username should return EmptyCredentials event`() = runBlockingTest {
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, "", testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.EmptyCredentials>(listOfEvents[0])
    }

    @Test
    fun `empty passphrase should return EmptyCredentials event`() = runBlockingTest {
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, testUsername, "".toByteArray()).toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.EmptyCredentials>(listOfEvents[0])
    }

    @Test
    fun `empty domain should return EmptyDomain event`() = runBlockingTest {
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, "", testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.EmptyDomain>(listOfEvents[0])
    }

    @Test
    fun `createAddress API returns Error event`() = runBlockingTest {
        // GIVEN
        coEvery {
            authRepository.createAddress(
                testSessionId,
                testDomain,
                testUsername
            )
        } returns DataResult.Error.Remote(
            "Invalid response"
        )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.Message)
        assertEquals("Invalid response", secondEvent.message)
    }

    @Test
    fun `randomModulus API returns Error event`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.randomModulus() } returns DataResult.Error.Remote(
            "Invalid response"
        )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.Message)
        assertEquals("Invalid response", secondEvent.message)
    }

    @Test
    fun `generating Private Key failure returns Error event`() = runBlockingTest {
        // GIVEN
        every {
            cryptoProvider.generateNewPrivateKey(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws EmptyCredentialsException("The passphrase for generating key can't be empty.")
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.GeneratingPrivateKeyFailed)
        assertEquals("The passphrase for generating key can't be empty.", secondEvent.message)
    }

    @Test
    fun `generating SignedKeyList failure returns Error event`() = runBlockingTest {
        // GIVEN
        every {
            cryptoProvider.generateSignedKeyList(any(), testPassphrase.toByteArray())
        } throws RuntimeException("Some gopenpgp exception.")
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.GeneratingSignedKeyListFailed)
        assertEquals("Some gopenpgp exception.", secondEvent.message)
    }

    @Test
    fun `setup address key API failure returns Error event`() = runBlockingTest {
        // GIVEN
        coEvery {
            authRepository.setupAddressKeys(
                testPrivateKey,
                "",
                any(),
                Auth(1, testModulusId, "test-salt", "test-verifier")
            )
        } returns
            DataResult.Error.Remote(
                "Invalid response"
            )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testDomain, testUsername, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateUsernameOnlyAccount.UpdateUsernameOnlyState.Error.Message)
        assertEquals("Invalid response", secondEvent.message)
    }
}
