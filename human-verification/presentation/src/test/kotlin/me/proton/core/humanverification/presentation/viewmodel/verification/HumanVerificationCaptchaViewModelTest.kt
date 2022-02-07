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
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HumanVerificationCaptchaViewModelTest : CoroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val networkManager = mockk<NetworkManager>()
    private val networkPrefs = mockk<NetworkPrefs>()

    private val viewModel by lazy {
        HumanVerificationCaptchaViewModel(
            networkManager = networkManager,
            networkPrefs = networkPrefs,
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

    @Test
    fun `if networkPrefs has activeAltBaseUrl it is used for activeAltUrlForDoH`() {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Disconnected)
        val altBaseUrl = "https://alternative-url.com"
        every { networkPrefs.activeAltBaseUrl } returns altBaseUrl

        val url = viewModel.activeAltUrlForDoH

        assertNotNull(url)
        assertEquals("https://alternative-url.com/core/v4/captcha", url)
    }

    @Test
    fun `if networkPrefs has no activeAltBaseUrl activeAltUrlForDoH returns null`() {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Disconnected)
        every { networkPrefs.activeAltBaseUrl } returns null

        val url = viewModel.activeAltUrlForDoH

        assertNull(url)
    }
}
