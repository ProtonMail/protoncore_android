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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.Country
import me.proton.core.humanverification.domain.exception.NoCountriesException
import me.proton.core.humanverification.domain.repository.HumanVerificationLocalRepository
import me.proton.core.humanverification.domain.utils.testCountriesExcludingMostUsed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class MostUsedCountryCodeTest {

    private val localRepository = mockk<HumanVerificationLocalRepository>()

    @Test
    fun `returns the most used country code successfully`() = runBlockingTest {

        val returnFlow = flowOf(testCountriesExcludingMostUsed)
        val expectedResult = 1
        val useCase = MostUsedCountryCode(localRepository)
        every { localRepository.mostUsedCountries() } returns returnFlow

        val result = useCase.invoke().toList()
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(expectedResult, result[0])
    }

    @Test(expected = NoCountriesException::class)
    fun `empty flow throws exception`() = runBlockingTest {
        val returnFlow = flowOf(emptyList<Country>())
        val useCase = MostUsedCountryCode(localRepository)
        every { localRepository.mostUsedCountries() } returns returnFlow

        val result = useCase.invoke().toList()
    }
}
