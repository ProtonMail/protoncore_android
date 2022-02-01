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

package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import me.proton.core.humanverification.presentation.viewmodel.hv2.verification.HumanVerificationCaptchaViewModel
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HumanVerificationCaptchaViewModelTest : CoroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val networkManager = mockk<NetworkManager>()

    private val viewModel by lazy {
        HumanVerificationCaptchaViewModel(
            networkManager = networkManager
        )
    }

    @Test
    fun `connectivity arrived metered`() = coroutinesTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Metered)
        viewModel.networkConnectionState.test {
            val result = awaitItem()
            assertTrue(result is ViewModelResult.Success<Boolean>)
            assertTrue(result.value)
        }
    }

    @Test
    fun `connectivity arrived unmetered`() = coroutinesTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Unmetered)
        viewModel.networkConnectionState.test {
            val result = awaitItem()
            assertTrue(result is ViewModelResult.Success<Boolean>)
            assertTrue(result.value)
        }
    }

    @Test
    fun `connectivity arrived disconnected`() = coroutinesTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Disconnected)
        viewModel.networkConnectionState.test {
            val result = awaitItem()
            assertTrue(result is ViewModelResult.Success<Boolean>)
            assertFalse(result.value)
        }
    }
}
