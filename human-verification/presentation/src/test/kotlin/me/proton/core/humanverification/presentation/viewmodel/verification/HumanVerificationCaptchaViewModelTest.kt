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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.usecase.VerifyCode
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.coroutinesTest
import org.junit.Rule
import org.junit.Test
import studio.forface.viewstatestore.ViewState
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class HumanVerificationCaptchaViewModelTest : CoroutinesTest by coroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val sessionId: SessionId = SessionId("id")
    private val verifyCode = mockk<VerifyCode>()
    private val networkManager = mockk<NetworkManager>()

    private val viewModel by lazy {
        HumanVerificationCaptchaViewModel(
            verifyCode = verifyCode,
            networkManager = networkManager
        )
    }

    @Test
    fun `connectivity arrived metered`() = runBlockingTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Metered)
        assertIs<ViewState.Success<Boolean>>(viewModel.networkConnectionState.awaitNext())
        val result = viewModel.networkConnectionState.awaitData()
        assertTrue(result)
    }

    @Test
    fun `connectivity arrived unmetered`() = runBlockingTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Unmetered)
        assertIs<ViewState.Success<Boolean>>(viewModel.networkConnectionState.awaitNext())
        val result = viewModel.networkConnectionState.awaitData()
        assertTrue(result)
    }

    @Test
    fun `connectivity arrived disconnected`() = runBlockingTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Disconnected)
        assertIs<ViewState.Success<Boolean>>(viewModel.networkConnectionState.awaitNext())
        val result = viewModel.networkConnectionState.awaitData()
        assertFalse(result)
    }

    @Test
    fun `verify code null fails`() = runBlockingTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Unmetered)
        viewModel.networkConnectionState.awaitNext()
        assertFailsWith<IllegalArgumentException> { viewModel.verifyTokenCode(sessionId, null) }
    }

    @Test
    fun `verify code empty fails`() = runBlockingTest {
        every {
            networkManager.observe()
        } returns flowOf(NetworkStatus.Unmetered)
        viewModel.networkConnectionState.awaitNext()
        assertFailsWith<IllegalArgumentException> { viewModel.verifyTokenCode(sessionId, "") }
    }
}
