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

package me.proton.core.auth.presentation.viewmodel.signup

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.usecase.DefaultCountry
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RecoverySMSViewModelTest : ArchTest, CoroutinesTest {
    // region mocks
    private val defaultCountryCode = mockk<DefaultCountry>(relaxed = true)
    // endregion

    private lateinit var viewModel: RecoverySMSViewModel

    private val country: Country = Country(
        code = "code",
        name = "name",
        callingCode = 0
    )

    @Before
    fun beforeEveryTest() {
        viewModel = RecoverySMSViewModel(defaultCountryCode)
    }

    @Test
    fun `calling code returns success`() = coroutinesTest {
        coEvery { defaultCountryCode.invoke() } returns country
        viewModel.countryCallingCode.test() {
            viewModel.getCountryCallingCode()
            assertIs<ViewModelResult.None>(expectItem())
            assertIs<ViewModelResult.Processing>(expectItem())
            assertIs<ViewModelResult.Success<Int>>(expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `calling code returns correct data`() = coroutinesTest {
        coEvery { defaultCountryCode.invoke() } returns country
        viewModel.countryCallingCode.test() {
            viewModel.getCountryCallingCode()
            assertIs<ViewModelResult.None>(expectItem())
            assertIs<ViewModelResult.Processing>(expectItem())
            Assert.assertEquals(0, (expectItem() as ViewModelResult.Success).value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `use case throws no countries exception`() = coroutinesTest {
        coEvery { defaultCountryCode.invoke() } returns null
        viewModel.countryCallingCode.test() {
            viewModel.getCountryCallingCode()
            assertIs<ViewModelResult.None>(expectItem())
            assertIs<ViewModelResult.Processing>(expectItem())
            assertIs<ViewModelResult.Error>(expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
