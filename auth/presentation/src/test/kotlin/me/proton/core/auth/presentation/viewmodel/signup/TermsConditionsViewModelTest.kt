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
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TermsConditionsViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    private val networkManager = mockk<NetworkManager>(relaxed = true)
    private val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Metered)

    private lateinit var viewModel: TermsConditionsViewModel

    @Before
    fun beforeEveryTest() {
        every { networkManager.observe() } returns networkStatus
        viewModel = TermsConditionsViewModel(networkManager)
    }

    @Test
    fun `network manager returns has connection`() = coroutinesTest {
        networkStatus.emit(NetworkStatus.Metered)

        viewModel.networkState.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `network manager returns different flow of has and has not connection`() = coroutinesTest {
        networkStatus.emit(NetworkStatus.Disconnected)

        viewModel.networkState.test {
            assertFalse(awaitItem())

            networkStatus.emit(NetworkStatus.Unmetered)
            assertTrue(awaitItem())
        }
    }
}
