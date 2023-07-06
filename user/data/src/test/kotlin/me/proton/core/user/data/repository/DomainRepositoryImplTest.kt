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

package me.proton.core.user.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.runTestWithResultContext
import me.proton.core.user.data.api.DomainApi
import me.proton.core.user.data.api.response.AvailableDomainsResponse
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DomainRepositoryImplTest {
    @MockK
    private lateinit var apiProvider: ApiProvider

    @MockK
    private lateinit var domainApi: DomainApi

    private lateinit var tested: DomainRepositoryImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { apiProvider.get<DomainApi>(any(), any()) } returns TestApiManager(domainApi)
        tested = DomainRepositoryImpl(apiProvider)
    }

    @Test
    fun getAvailableDomains_result_success() = runTestWithResultContext {
        // GIVEN
        val domains = listOf("domain")
        coEvery { domainApi.getAvailableDomains(any()) } returns AvailableDomainsResponse(domains)

        // WHEN
        tested.getAvailableDomains(null)


        // THEN
        val result = assertSingleResult("getAvailableDomains")
        assertTrue(result.isSuccess)

        val value = result.getOrNull()
        assertTrue(value is List<*>)
        assertContentEquals(domains, value)
    }

    @Test
    fun getAvailableDomains_result_failure() = runTestWithResultContext {
        // GIVEN
        coEvery { domainApi.getAvailableDomains(any()) } throws IOException("Test error")

        // WHEN
        assertFailsWith<ApiException> {
            tested.getAvailableDomains(null)
        }

        // THEN
        val result = assertSingleResult("getAvailableDomains")
        assertTrue(result.isFailure)
        val apiException = assertIs<ApiException>(result.exceptionOrNull())
        assertIs<IOException>(apiException.cause)
    }
}
