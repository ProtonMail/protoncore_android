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

package me.proton.core.countries.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.countries.domain.entity.Country
import me.proton.core.countries.domain.repository.CountriesRepository
import me.proton.core.countries.domain.utils.testCountriesExcludingMostUsed
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * @author Dino Kadrikj.
 */
class LoadCountriesTest {

    // region mocks
    private val repository = mockk<CountriesRepository>(relaxed = true)
    // endregion

    private lateinit var useCase: LoadCountries

    @Before
    fun beforeEveryTest() {
        useCase = LoadCountries(repository)
    }

    @Test
    fun `get countries returns success non empty list`() = runBlockingTest {
        coEvery { repository.getAllCountriesSorted() } returns
            listOf(Country(name = "test-country-1", code = "test-code-1"), Country(name = "test-country-2", code =  "test-code-2"))
        val result = useCase.invoke()
        assertEquals(2, result.size)
        assertEquals("test-country-1", result[0].name)
        assertEquals("test-country-2", result[1].name)
    }

    @Test
    fun `get countries returns success empty list`() = runBlockingTest {
        coEvery { repository.getAllCountriesSorted() } returns emptyList()
        val result = useCase.invoke()
        assertEquals(0, result.size)
    }
}
