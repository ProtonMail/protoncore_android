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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.entity.Address
import me.proton.core.auth.domain.entity.AddressType
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.entity.KeySalt
import me.proton.core.auth.domain.entity.KeySalts
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.entity.UserKey
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class PerformUserSetupTest {
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val cryptoProvider = mockk<CryptoProvider>(relaxed = true)

    private val testSessionId = "test-session-id"
    private val testMailboxPassword = "test-mailbox-password"
    private val testGeneratedPassphrase = "test-generated-passphrase"

    private lateinit var useCase: PerformUserSetup
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
        subscribed = 1,
        delinquent = 0,
        email = "test-email",
        displayName = "test-display-name",
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
    private val keySaltsResult = KeySalts(
        salts = listOf(KeySalt(keyId = "test-key-id", keySalt = "test-key-salt"))
    )

    private val addressesResult = Addresses(
        addresses = listOf(
            Address(
                id = "test-address-id",
                domainId = "test-domain-id",
                email = "test-email",
                canSend = true,
                canReceive = true,
                status = 1,
                type = AddressType.ORIGINAL,
                order = 1,
                displayName = "test-display-name",
                signature = "test-signature",
                hasKeys = false,
                keys = emptyList()
            )
        )
    )

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = PerformUserSetup(authRepository, cryptoProvider)
        coEvery { authRepository.getUser(SessionId(testSessionId)) } returns DataResult.Success(
            ResponseSource.Remote,
            userResult
        )
        coEvery { authRepository.getSalts(SessionId(testSessionId)) } returns DataResult.Success(
            ResponseSource.Remote,
            keySaltsResult
        )
        coEvery { authRepository.getAddresses(SessionId(testSessionId)) } returns DataResult.Success(
            ResponseSource.Remote,
            addressesResult
        )

        every {
            cryptoProvider.generatePassphrase(
                testMailboxPassword.toByteArray(),
                any()
            )
        } returns testGeneratedPassphrase.toByteArray()

        every {
            cryptoProvider.passphraseCanUnlockKey(any(), testGeneratedPassphrase.toByteArray())
        } returns true
    }

    @Test
    fun `mailbox login happy path events list is correct`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testMailboxPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformUserSetup.State.Processing)
        val successEvent = listOfEvents[1]
        assertTrue(successEvent is PerformUserSetup.State.Success)
        val user = successEvent.user
        assertNotNull(user.passphrase)
    }

    @Test
    fun `mailbox login happy path invocations are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(SessionId(testSessionId), testMailboxPassword.toByteArray()).toList()
        // THEN
        coVerify(exactly = 1) { authRepository.getUser(SessionId(testSessionId)) }
        coVerify(exactly = 1) { authRepository.getSalts(SessionId(testSessionId)) }
    }

    @Test
    fun `mailbox login no primary key events list is correct`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getUser(SessionId(testSessionId)) } returns DataResult.Success(
            ResponseSource.Remote,
            userResult.copy(keys = emptyList())
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testMailboxPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformUserSetup.State.Processing)
        val errorEvent = listOfEvents[1]
        assertTrue(errorEvent is PerformUserSetup.State.Error.NoPrimaryKey)
    }

    @Test
    fun `mailbox login no key salts for primary key events list is correct`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getSalts(SessionId(testSessionId)) } returns DataResult.Success(
            ResponseSource.Remote,
            KeySalts(
                salts = listOf(KeySalt(keyId = "test-key-id2", keySalt = "test-key-salt2"))
            )
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testMailboxPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformUserSetup.State.Processing)
        val errorEvent = listOfEvents[1]
        assertTrue(errorEvent is PerformUserSetup.State.Error.NoKeySaltsForPrimaryKey)
    }

    @Test
    fun `mailbox login empty generated mailbox passphrase`() = runBlockingTest {
        // GIVEN
        every {
            cryptoProvider.generatePassphrase(
                testMailboxPassword.toByteArray(),
                any()
            )
        } returns "".toByteArray()
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testMailboxPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformUserSetup.State.Processing)
        val errorEvent = listOfEvents[1]
        assertTrue(errorEvent is PerformUserSetup.State.Error.PrimaryKeyInvalidPassphrase)
    }

    @Test
    fun `mailbox login error user response`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getUser(SessionId(testSessionId)) } returns DataResult.Error.Remote(
            "Invalid user response"
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testMailboxPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformUserSetup.State.Processing)
        val errorEvent = listOfEvents[1]
        assertTrue(errorEvent is PerformUserSetup.State.Error.Message)
        assertEquals("Invalid user response", errorEvent.message)
    }

    @Test
    fun `mailbox login error salts response`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getSalts(SessionId(testSessionId)) } returns DataResult.Error.Remote(
            "Invalid salts response"
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testMailboxPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformUserSetup.State.Processing)
        val errorEvent = listOfEvents[1]
        assertTrue(errorEvent is PerformUserSetup.State.Error.Message)
        assertEquals("Invalid salts response", errorEvent.message)
    }
}
