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

package me.proton.core.humanverification.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.Country
import me.proton.core.humanverification.domain.repository.HumanVerificationLocalRepository
import me.proton.core.humanverification.domain.utils.testCountriesExcludingMostUsed
import me.proton.core.humanverification.domain.utils.testCountriesMostUsed
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * @author Dino Kadrikj.
 */
class LoadCountriesTest {

    private val localRepository = mockk<HumanVerificationLocalRepository>()

    @Test
    fun `returns all except top five success`() = runBlockingTest {
        val useCase = LoadCountries(localRepository)
        every { localRepository.allCountries(false) } returns flowOf(testCountriesExcludingMostUsed)
        every { localRepository.allCountries(true) } returns flowOf(
            testCountriesMostUsed.plus(
                testCountriesExcludingMostUsed
            )
        )
        var result: List<Country>? = null
        useCase(false).collect {
            result = it
        }
        assertNotNull(result)
        assertEquals(9, result!!.size)
    }

    @Test
    fun `returns all including top five success`() = runBlockingTest {
        val useCase = LoadCountries(localRepository)
        every { localRepository.allCountries(false) } returns flowOf(testCountriesExcludingMostUsed)
        every { localRepository.allCountries(true) } returns flowOf(
            testCountriesMostUsed.plus(
                testCountriesExcludingMostUsed
            )
        )
        var result: List<Country>? = null
        useCase.invoke(true).collect {
            result = it
        }
        assertNotNull(result)
        assertEquals(14, result!!.size)
    }
}
