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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TermsConditionsViewModelTest : ArchTest, CoroutinesTest {
    // region mocks
    private val networkManager = mockk<NetworkManager>()
    // endregion

    private lateinit var viewModel: TermsConditionsViewModel

    @Before
    fun beforeEveryTest() {
        every { networkManager.observe() } returns flowOf(NetworkStatus.Metered)
        viewModel = TermsConditionsViewModel(networkManager)
    }

    @Test
    fun `network manager returns has connection`() = coroutinesTest {
        viewModel.networkConnectionState.test {
            viewModel.watchNetwork()
            assertNull(expectItem())
            assertTrue(expectItem()!!)
        }
    }

    @Test
    fun `network manager returns different flow of has and has not connection`() = coroutinesTest {
        every { networkManager.observe() } returns flowOf(NetworkStatus.Disconnected, NetworkStatus.Unmetered)
        viewModel.networkConnectionState.test {
            viewModel.watchNetwork()
            assertNull(expectItem())
            assertFalse(expectItem()!!)
            assertTrue(expectItem()!!)
        }
    }
}
