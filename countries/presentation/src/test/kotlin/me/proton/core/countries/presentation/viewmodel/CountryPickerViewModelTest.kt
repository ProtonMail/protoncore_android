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

package me.proton.core.countries.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.countries.domain.usecase.LoadCountries
import me.proton.core.countries.presentation.entity.CountryUIModel
import me.proton.core.countries.presentation.utils.testCountriesExcludingMostUsed
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertTrue
import me.proton.core.test.kotlin.coroutinesTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CountryPickerViewModelTest : CoroutinesTest by coroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val loadCountries = mockk<LoadCountries>()

    private val viewModel by lazy {
        CountryPickerViewModel(loadCountries)
    }

    @Test
    fun `load countries has data`() = coroutinesTest {
        coEvery { loadCountries.invoke() } returns testCountriesExcludingMostUsed
        val observer = mockk<(CountryPickerViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<CountryPickerViewModel.State>()
        viewModel.countries.observeDataForever(observer)
        verify(exactly = 1) { observer(capture(arguments)) }
        val success = arguments[0]
        assertTrue(success is CountryPickerViewModel.State.Success)
        assertEquals(9, success.countries.size)
    }

    @Test
    fun `load countries returns empty list`() = coroutinesTest {
        coEvery { loadCountries.invoke() } returns emptyList()
        val observer = mockk<(CountryPickerViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<CountryPickerViewModel.State>()
        viewModel.countries.observeDataForever(observer)
        verify(exactly = 1) { observer(capture(arguments)) }
        val success = arguments[0]
        assertTrue(success is CountryPickerViewModel.State.Success)
        assertEquals(0, success.countries.size)
    }
}
