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

package me.proton.core.payment.domain.usecase

import io.mockk.every
import io.mockk.mockk
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Country
import me.proton.core.payment.domain.repository.CountriesRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetCountriesTest {
    // region mocks
    private val repository = mockk<CountriesRepository>(relaxed = true)
    // endregion

    private lateinit var useCase: GetCountries

    @Before
    fun beforeEveryTest() {
        useCase = GetCountries(repository)
    }

    @Test
    fun `get countries returns success non empty list`() {
        every { repository.getCountries() } returns
            listOf(Country("test-country-1", "test-code-1"), Country("test-country-2", "test-code-2"))
        val result = useCase.invoke()
        assertEquals(2, result.size)
        assertEquals("test-country-1", result[0].name)
        assertEquals("test-country-2", result[1].name)
    }

    @Test
    fun `get countries returns success empty list`() {
        every { repository.getCountries() } returns emptyList()
        val result = useCase.invoke()
        assertEquals(0, result.size)
    }

    @Test
    fun `get countries returns error`() {
        every { repository.getCountries() } throws ApiException(
            ApiResult.Error.Connection(
                false,
                RuntimeException("Test error")
            )
        )
        assertFailsWith(ApiException::class) {
            useCase.invoke()
        }
    }
}
