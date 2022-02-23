/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.humanverification.presentation.viewmodel.hv2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HV2ViewModelTest : CoroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()
    private val savedStateHandle = mockk<SavedStateHandle>()
    private val humanVerificationWorkflowHandler = mockk<HumanVerificationWorkflowHandler>(relaxed = true)

    @Test
    fun `correct initialization`() = coroutinesTest {
        val availableMethods = listOf(
            TokenType.EMAIL.value,
            TokenType.CAPTCHA.value
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HV2ViewModel(humanVerificationWorkflowHandler, savedStateHandle)
        viewModel.enabledMethods.test {
            assertEquals(availableMethods, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `incorrect initialization does nothing`() {
        val availableMethods = emptyList<String>()
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HV2ViewModel(humanVerificationWorkflowHandler, savedStateHandle)

        assertNull(viewModel.activeMethod.value)
        assertTrue(viewModel.enabledMethods.value.isEmpty())
    }

    @Test
    fun `active method correctly set`() = coroutinesTest {
        val availableMethods = listOf(
            TokenType.EMAIL.value,
            TokenType.CAPTCHA.value,
            TokenType.SMS.value
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HV2ViewModel(humanVerificationWorkflowHandler, savedStateHandle)

        viewModel.activeMethod.test {
            assertEquals(TokenType.EMAIL.value, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `active method correctly set option 2`() = coroutinesTest {
        val availableMethods = listOf(
            TokenType.EMAIL.value,
            TokenType.SMS.value
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HV2ViewModel(humanVerificationWorkflowHandler, savedStateHandle)
        viewModel.activeMethod.test {
            assertEquals(TokenType.EMAIL.value, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
