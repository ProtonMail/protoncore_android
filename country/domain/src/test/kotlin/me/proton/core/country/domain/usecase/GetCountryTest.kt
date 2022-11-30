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
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.repository.CountriesRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetCountryTest {
    // region mocks
    private val repository = mockk<CountriesRepository>(relaxed = true)
    // endregion

    // region test data
    private val testCountryName = "test-country-name"
    private val testCountryCode = "test-country-code"
    // endregion

    private lateinit var useCase: GetCountry

    @Before
    fun beforeEveryTest() {
        useCase = GetCountry(repository)
    }

    @Test
    fun `get country code returns success`() = runTest {
        coEvery { repository.getCountry(testCountryName) } returns Country(testCountryCode, "name")
        val result = useCase.invoke(testCountryName)
        assertNotNull(result)
        assertEquals(testCountryCode, result.code)
    }
}
