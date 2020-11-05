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
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class AvailableDomainsTest {

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private lateinit var useCase: AvailableDomains

    private val domains = listOf("protonmail.com", "protonmail.ch")

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = AvailableDomains(authRepository)
        coEvery { authRepository.getAvailableDomains() } returns DataResult.Success(ResponseSource.Remote, domains)
    }

    @Test
    fun `available domains success response`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke().toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        val event = listOfEvents[0]
        assertTrue(event is AvailableDomains.State.Success)
        assertEquals(domains, event.availableDomains)
    }

    @Test
    fun `available domains success response first domain is right`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke().toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        val event = listOfEvents[0]
        assertTrue(event is AvailableDomains.State.Success)
        assertEquals("@protonmail.com", event.firstDomainOrDefault)
    }

    @Test
    fun `available domains success no domains response`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getAvailableDomains() } returns DataResult.Success(ResponseSource.Remote, emptyList())
        // WHEN
        val listOfEvents = useCase.invoke().toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        val event = listOfEvents[0]
        assertTrue(event is AvailableDomains.State.Error.NoAvailableDomains)
    }

    @Test
    fun `available domains error response`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getAvailableDomains() } returns DataResult.Error.Remote(
            message = "api error",
            httpCode = 401
        )
        // WHEN
        val listOfEvents = useCase.invoke().toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        val event = listOfEvents[0]
        assertTrue(event is AvailableDomains.State.Error.Message)
        assertEquals("api error", event.message)
    }
}
