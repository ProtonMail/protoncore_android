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

package me.proton.core.country.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.country.domain.repository.CountriesRepository
import me.proton.core.country.domain.utils.testCountries
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNull

class DefaultCountryTest {

    private val localRepository = mockk<CountriesRepository>()

    @Test
    fun `returns the default country code successfully`() = runTest {
        val expectedResult = null
        val useCase = DefaultCountry(localRepository)
        coEvery { localRepository.getAllCountriesSorted() } returns testCountries

        val result = useCase.invoke()
        assertEquals(expectedResult, result)
    }

    @Test
    fun `empty flow throws exception`() = runTest {
        coEvery { localRepository.getAllCountriesSorted() } returns emptyList()
        val useCase = DefaultCountry(localRepository)
        val result = useCase.invoke()
        assertNull(result)
    }
}
