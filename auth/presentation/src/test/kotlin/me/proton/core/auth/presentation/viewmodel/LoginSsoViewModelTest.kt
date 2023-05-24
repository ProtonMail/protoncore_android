/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.presentation.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import me.proton.core.auth.domain.usecase.CreateLoginSsoSession
import me.proton.core.auth.domain.usecase.GetAuthInfo
import me.proton.core.auth.presentation.viewmodel.LoginSsoViewModel.State
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test

class LoginSsoViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    private val getAuthInfo = mockk<GetAuthInfo>()
    private val createLoginSession = mockk<CreateLoginSsoSession>()

    private val testEmail = "username@domain.com"
    private val testUserId = UserId("test-user-id")

    private lateinit var viewModel: LoginSsoViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = LoginSsoViewModel(getAuthInfo, createLoginSession)
    }

    @Test
    fun `getAuth for non SSO account switch to SRP`() = coroutinesTest {
        // GIVEN
        coEvery { getAuthInfo.invoke(any(), any()) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 404,
                message = "Email domain not found, please sign in with a password",
                proton = ApiResult.Error.ProtonData(8101, "error")
            )
        )

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testEmail)

            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.SignInWithSrp>(awaitItem())

            coVerify { getAuthInfo.invoke(null, testEmail) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAuth for SSO account StartToken`() = coroutinesTest {
        // GIVEN
        coEvery { getAuthInfo.invoke(any(), any()) } returns mockk()

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testEmail)

            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.StartToken>(awaitItem())

            coVerify { getAuthInfo.invoke(null, testEmail) }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
