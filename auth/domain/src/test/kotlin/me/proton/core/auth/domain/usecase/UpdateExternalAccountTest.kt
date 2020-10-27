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
import me.proton.core.auth.domain.entity.AddressType
import me.proton.core.auth.domain.entity.KeySecurity
import me.proton.core.auth.domain.entity.KeyType
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
class UpdateExternalAccountTest {
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val cryptoProvider = mockk<CryptoProvider>(relaxed = true)

    private lateinit var useCase: UpdateExternalAccount

    // region test data
    private val testSessionId = SessionId("test-session-id")
    private val testUsername = "test-username"
    private val testDomain = "test-domain"
    private val testDisplayName = "test-displayName"
    private val testPassphrase = "test-passphrase"
    private val testPrivateKey = "test-private-key"
    private val testAddressId = "test-addressId"
    private val testSignedKeyListData = "test-signedKeyListData"
    private val testSignedKeyListSignature = "test-signedKeyListSignature"

    private val address = Address(
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

    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = UpdateExternalAccount(authRepository, cryptoProvider)
        coEvery { authRepository.setUsername(testSessionId, testUsername) } returns DataResult.Success(
            true,
            ResponseSource.Remote
        )
        coEvery { authRepository.createAddress(testSessionId, testDomain, testUsername) } returns DataResult.Success(
            address,
            ResponseSource.Remote
        )
    }

    @Test
    fun `update external account happy path`() = runBlockingTest {
        // GIVEN
        every { cryptoProvider.generateNewPrivateKey(any(), any(), any(), any(), any()) } returns testPrivateKey
        every { cryptoProvider.generateSignedKeyList(any(), testPassphrase.toByteArray()) } returns Pair(
            testSignedKeyListData,
            testSignedKeyListSignature
        )
        coEvery {
            authRepository.createAddressKey(
                testSessionId,
                testAddressId,
                testPrivateKey,
                true,
                testSignedKeyListData,
                testSignedKeyListSignature
            )
        } returns DataResult.Success(
            mockk(),
            ResponseSource.Remote
        )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateExternalAccount.UpdateExternalAccountState.Success)
    }

    @Test
    fun `update external account API arguments are correct`() = runBlockingTest {
        // GIVEN
        every { cryptoProvider.generateNewPrivateKey(any(), any(), any(), any(), any()) } returns testPrivateKey
        every { cryptoProvider.generateSignedKeyList(any(), testPassphrase.toByteArray()) } returns Pair(
            testSignedKeyListData,
            testSignedKeyListSignature
        )
        coEvery {
            authRepository.createAddressKey(
                testSessionId,
                testAddressId,
                testPrivateKey,
                true,
                testSignedKeyListData,
                testSignedKeyListSignature
            )
        } returns DataResult.Success(
            mockk(),
            ResponseSource.Remote
        )
        // WHEN
        useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
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

        val addressIdArgument = slot<String>()
        val privateKeyArgument = slot<String>()
        val primaryArgument = slot<Boolean>()
        val signedKeyListDataArgument = slot<String>()
        val signedKeyListSignatureArgument = slot<String>()
        coVerify {
            authRepository.createAddressKey(
                capture(sessionIdArgument), capture(addressIdArgument), capture(privateKeyArgument),
                capture(primaryArgument), capture(signedKeyListDataArgument), capture(signedKeyListSignatureArgument)
            )
        }
        assertEquals(testSessionId, sessionIdArgument.captured)
        assertEquals(testAddressId, addressIdArgument.captured)
        assertEquals(testPrivateKey, privateKeyArgument.captured)
        assertEquals(true, primaryArgument.captured)
        assertEquals(testSignedKeyListData, signedKeyListDataArgument.captured)
        assertEquals(testSignedKeyListSignature, signedKeyListSignatureArgument.captured)

    }

    @Test
    fun `update external account Crypto arguments are correct`() = runBlockingTest {
        every { cryptoProvider.generateNewPrivateKey(any(), any(), any(), any(), any()) } returns testPrivateKey
        every { cryptoProvider.generateSignedKeyList(any(), testPassphrase.toByteArray()) } returns Pair(
            testSignedKeyListData,
            testSignedKeyListSignature
        )
        coEvery {
            authRepository.createAddressKey(
                testSessionId,
                testAddressId,
                testPrivateKey,
                true,
                testSignedKeyListData,
                testSignedKeyListSignature
            )
        } returns DataResult.Success(
            mockk(),
            ResponseSource.Remote
        )
        // WHEN
        useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()

        // THEN
        val usernameArgument = slot<String>()
        val passphraseArgument = slot<ByteArray>()
        val domainArgument = slot<String>()
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
    }

    @Test
    fun `empty username should return EmptyCredentials event`() = runBlockingTest {
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, "", testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Error.EmptyCredentials>(listOfEvents[0])
    }

    @Test
    fun `empty passphrase should return EmptyCredentials event`() = runBlockingTest {
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testUsername, testDomain, "".toByteArray()).toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Error.EmptyCredentials>(listOfEvents[0])
    }

    @Test
    fun `empty domain should return EmptyDomain event`() = runBlockingTest {
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testUsername, "", testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Error.EmptyDomain>(listOfEvents[0])
    }

    @Test
    fun `unsuccessful setUsername returns Error event`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.setUsername(testSessionId, testUsername) } returns DataResult.Success(
            false,
            ResponseSource.Remote
        )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Processing>(listOfEvents[0])
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Error.SetUsernameFailed>(listOfEvents[1])
    }

    @Test
    fun `setUsername API returns Error event`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.setUsername(testSessionId, testUsername) } returns DataResult.Error.Message(
            "Invalid response", ResponseSource.Remote
        )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateExternalAccount.UpdateExternalAccountState.Error.Message)
        assertEquals("Invalid response", secondEvent.message)
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
        } returns DataResult.Error.Message(
            "Invalid response", ResponseSource.Remote
        )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateExternalAccount.UpdateExternalAccountState.Error.Message)
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
            useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateExternalAccount.UpdateExternalAccountState.Error.GeneratingPrivateKeyFailed)
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
            useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateExternalAccount.UpdateExternalAccountState.Error.GeneratingSignedKeyListFailed)
        assertEquals("Some gopenpgp exception.", secondEvent.message)
    }

    @Test
    fun `create Address API failure returns Error event`() = runBlockingTest {
        // GIVEN
        every { cryptoProvider.generateNewPrivateKey(any(), any(), any(), any(), any()) } returns testPrivateKey
        every { cryptoProvider.generateSignedKeyList(any(), testPassphrase.toByteArray()) } returns Pair(
            testSignedKeyListData,
            testSignedKeyListSignature
        )
        coEvery {
            authRepository.createAddressKey(
                testSessionId,
                testAddressId,
                testPrivateKey,
                true,
                testSignedKeyListData,
                testSignedKeyListSignature
            )
        } returns DataResult.Error.Message(
            "Invalid response", ResponseSource.Remote
        )
        // WHEN
        val listOfEvents =
            useCase.invoke(testSessionId, testUsername, testDomain, testPassphrase.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UpdateExternalAccount.UpdateExternalAccountState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UpdateExternalAccount.UpdateExternalAccountState.Error.Message)
        assertEquals("Invalid response", secondEvent.message)
    }
}
