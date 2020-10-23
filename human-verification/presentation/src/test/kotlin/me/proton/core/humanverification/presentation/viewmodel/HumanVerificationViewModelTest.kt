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

package me.proton.core.humanverification.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.exception.NotEnoughVerificationOptions
import me.proton.core.test.kotlin.assertIs
import org.junit.Rule
import org.junit.Test
import studio.forface.viewstatestore.ViewState
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * @author Dino Kadrikj.
 */
class HumanVerificationViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()
    private val savedStateHandle = mockk<SavedStateHandle>()

    @Test
    fun `correct initialization`() {
        val availableMethods = listOf(
            TokenType.EMAIL.tokenTypeValue,
            TokenType.CAPTCHA.tokenTypeValue
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HumanVerificationViewModel(savedStateHandle)
        val result = viewModel.enabledMethods.state()
        assertNotNull(result)
        assertIs<ViewState.Success<List<String>>>(result)
        val resultData = result.data
        assertEquals(availableMethods, resultData)
    }

    @Test(expected = NotEnoughVerificationOptions::class)
    fun `incorrect initialization throws exception`() {
        val availableMethods = emptyList<String>()
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        HumanVerificationViewModel(savedStateHandle)
    }

    @Test
    fun `active method correctly set`() {
        val availableMethods = listOf(
            TokenType.EMAIL.tokenTypeValue,
            TokenType.CAPTCHA.tokenTypeValue,
            TokenType.SMS.tokenTypeValue
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HumanVerificationViewModel(savedStateHandle)
        val result = viewModel.activeMethod.state()
        assertNotNull(result)
        assertIs<ViewState.Success<String>>(result)
        val resultData = result.data
        assertEquals(TokenType.CAPTCHA.tokenTypeValue, resultData)
    }

    @Test
    fun `active method correctly set option 2`() {
        val availableMethods = listOf(
            TokenType.EMAIL.tokenTypeValue,
            TokenType.SMS.tokenTypeValue
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HumanVerificationViewModel(savedStateHandle)
        val result = viewModel.activeMethod.state()
        assertNotNull(result)
        assertIs<ViewState.Success<String>>(result)
        val resultData = result.data
        assertEquals(TokenType.EMAIL.tokenTypeValue, resultData)
    }
}
