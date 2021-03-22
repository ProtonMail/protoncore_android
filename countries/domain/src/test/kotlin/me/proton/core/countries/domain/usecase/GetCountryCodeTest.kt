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
import me.proton.core.countries.domain.repository.CountriesRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetCountryCodeTest {
    // region mocks
    private val repository = mockk<CountriesRepository>(relaxed = true)
    // endregion

    // region test data
    private val testCountryName = "test-country-name"
    private val testCountryCode = "test-country-code"
    // endregion

    private lateinit var useCase: GetCountryCode

    @Before
    fun beforeEveryTest() {
        useCase = GetCountryCode(repository)
    }

    @Test
    fun `get country code returns success`() = runBlockingTest {
        coEvery { repository.getCountryCodeByName(testCountryName) } returns testCountryCode
        val result = useCase.invoke(testCountryName)
        assertEquals(testCountryCode, result)
    }
}
