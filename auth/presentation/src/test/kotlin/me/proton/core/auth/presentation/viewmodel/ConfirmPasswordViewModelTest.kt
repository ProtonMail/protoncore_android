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

package me.proton.core.auth.presentation.viewmodel

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.auth.presentation.ConfirmPasswordOrchestrator
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ConfirmPasswordViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val confirmPasswordOrchestratorSpy = spyk<ConfirmPasswordOrchestrator>()
    // endregion

    private lateinit var viewModel: ConfirmPasswordViewModel

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.auth.presentation.ConfirmPasswordOrchestratorKt")
        viewModel = ConfirmPasswordViewModel(confirmPasswordOrchestratorSpy)
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.auth.presentation.ConfirmPasswordOrchestratorKt")
    }

    @Test
    fun `no 2FA confirm pass result not null handled success`() = coroutinesTest {
        every { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(any()) } answers {
            confirmPasswordOrchestratorSpy.onConfirmPasswordResultListener?.invoke(
                ConfirmPasswordResult(confirmed = true)
            )
        }
        every { confirmPasswordOrchestratorSpy.register(any()) } answers { }
        val result = viewModel.confirmPassword(Scope.PASSWORD)
        verify { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(Scope.PASSWORD) }
        assertEquals(MissingScopeResult.Success, result)
    }

    @Test
    fun `2FA confirm pass result not null handled success`() = coroutinesTest {
        every { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(Scope.PASSWORD) } answers {
            confirmPasswordOrchestratorSpy.onConfirmPasswordResultListener?.invoke(
                ConfirmPasswordResult(confirmed = true)
            )
        }
        val result = viewModel.confirmPassword(Scope.PASSWORD)
        verify { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(Scope.PASSWORD) }
        assertEquals(MissingScopeResult.Success, result)
    }

    @Test
    fun `no 2FA confirm pass result null handled failure`() = coroutinesTest {
        every { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(Scope.PASSWORD) } answers {
            confirmPasswordOrchestratorSpy.onConfirmPasswordResultListener?.invoke(
                null
            )
        }
        val result = viewModel.confirmPassword(Scope.PASSWORD)
        verify { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(Scope.PASSWORD) }
        assertEquals(MissingScopeResult.Failure, result)
    }

    @Test
    fun `2FA confirm pass result null handled failure`() = coroutinesTest {
        every { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(Scope.PASSWORD) } answers {
            confirmPasswordOrchestratorSpy.onConfirmPasswordResultListener?.invoke(
                null
            )
        }
        val result = viewModel.confirmPassword(Scope.PASSWORD)
        verify { confirmPasswordOrchestratorSpy.startConfirmPasswordWorkflow(Scope.PASSWORD) }
        assertEquals(MissingScopeResult.Failure, result)
    }
}
