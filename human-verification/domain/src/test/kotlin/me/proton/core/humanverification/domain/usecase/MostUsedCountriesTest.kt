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
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.repository.HumanVerificationLocalRepository
import me.proton.core.humanverification.domain.utils.testCountriesExcludingMostUsed
import me.proton.core.humanverification.domain.utils.testCountriesMostUsed
import org.junit.Test

/**
 * Tests the MostUsedCountries use case.
 *
 * @author Dino Kadrikj.
 */
class MostUsedCountriesTest {

    private val localRepository = mockk<HumanVerificationLocalRepository>()

    @InternalCoroutinesApi
    @Test
    fun `returns top five success`() = runBlockingTest {
        val useCase = MostUsedCountries(localRepository)
        every { localRepository.allCountries(false) } returns flowOf(testCountriesExcludingMostUsed)
        every { localRepository.allCountries(true) } returns flowOf(
            testCountriesMostUsed.plus(
                testCountriesExcludingMostUsed
            )
        )
        every { localRepository.mostUsedCountries() } returns flowOf(testCountriesMostUsed)
        val result = useCase().toList()[0]
        kotlin.test.assertNotNull(result)
        kotlin.test.assertEquals(5, result.size)
    }
}
