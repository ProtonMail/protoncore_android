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
import me.proton.core.countries.domain.exception.NoCountriesException
import me.proton.core.countries.domain.repository.CountriesRepository
import me.proton.core.countries.domain.utils.testCountriesExcludingMostUsed
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class MostUsedCountryCodeTest {

    private val localRepository = mockk<CountriesRepository>()

    @Test
    fun `returns the most used country code successfully`() = runBlockingTest {
        val expectedResult = 1
        val useCase = MostUsedCountryCode(localRepository)
        coEvery { localRepository.getAllCountriesSorted() } returns testCountriesExcludingMostUsed

        val result = useCase.invoke()
        assertEquals(expectedResult, result)
    }

    @Test
    fun `empty flow throws exception`() = runBlockingTest {
        coEvery { localRepository.getAllCountriesSorted() } returns emptyList()

        assertFailsWith(NoCountriesException::class) {
            MostUsedCountryCode(localRepository).invoke()
        }
    }
}
