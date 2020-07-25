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

package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.usecase.LoadCountries
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import me.proton.core.humanverification.presentation.viewmodel.utils.testCountriesExcludingMostUsed
import me.proton.core.humanverification.presentation.viewmodel.utils.testCountriesMostUsed
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.coroutinesTest
import org.junit.Rule
import org.junit.Test
import studio.forface.viewstatestore.ViewState
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * @author Dino Kadrikj.
 */
class CountryPickerViewModelTest : CoroutinesTest by coroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val loadCountries = mockk<LoadCountries>()

    private val viewModel by lazy {
        CountryPickerViewModel(
            loadCountries
        )
    }

    @Test
    fun `load countries has data`() = runBlockingTest {
        coEvery { loadCountries.invoke() } returns flowOf(
            testCountriesMostUsed.plus(
                testCountriesExcludingMostUsed
            )
        )
        val result = viewModel.countries.awaitNext()
        assertIs<ViewState.Success<List<CountryUIModel>>>(result)
        val resultData = result.data
        assertNotNull(resultData)
        assertEquals(14, resultData.size)
    }
}
