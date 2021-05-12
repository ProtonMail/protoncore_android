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
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.presentation.exception.NotEnoughVerificationOptions
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.entity.UserVerificationTokenType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class HumanVerificationViewModelTest : CoroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()
    private val savedStateHandle = mockk<SavedStateHandle>()
    private val humanVerificationWorkflowHandler = mockk<HumanVerificationWorkflowHandler>(relaxed = true)

    @Test
    fun `correct initialization`() = coroutinesTest {
        val availableMethods = listOf(
            UserVerificationTokenType.EMAIL.tokenTypeValue,
            UserVerificationTokenType.CAPTCHA.tokenTypeValue
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HumanVerificationViewModel(humanVerificationWorkflowHandler, savedStateHandle)
        viewModel.enabledMethods.test {
            assertEquals(availableMethods, expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test(expected = NotEnoughVerificationOptions::class)
    fun `incorrect initialization throws exception`() {
        val availableMethods = emptyList<String>()
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        HumanVerificationViewModel(humanVerificationWorkflowHandler, savedStateHandle)
    }

    @Test
    fun `active method correctly set`() = coroutinesTest {
        val availableMethods = listOf(
            UserVerificationTokenType.EMAIL.tokenTypeValue,
            UserVerificationTokenType.CAPTCHA.tokenTypeValue,
            UserVerificationTokenType.SMS.tokenTypeValue
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HumanVerificationViewModel(humanVerificationWorkflowHandler, savedStateHandle)

        viewModel.activeMethod.test {
            assertEquals(UserVerificationTokenType.CAPTCHA.tokenTypeValue, expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `active method correctly set option 2`() = coroutinesTest {
        val availableMethods = listOf(
            UserVerificationTokenType.EMAIL.tokenTypeValue,
            UserVerificationTokenType.SMS.tokenTypeValue
        )
        every { savedStateHandle.get<List<String>>(any()) } returns availableMethods

        val viewModel = HumanVerificationViewModel(humanVerificationWorkflowHandler, savedStateHandle)
        viewModel.activeMethod.test {
            assertEquals(UserVerificationTokenType.EMAIL.tokenTypeValue, expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
