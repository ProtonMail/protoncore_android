/*
 * Copyright (c) 2024 Proton AG
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
import io.mockk.every
import io.mockk.mockk
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.CreateLoginLessSession
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.PostLoginLessAccountSetup
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test

class CredentialLessViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    private val userId = UserId("userId")
    private val sessionInfo = mockk<SessionInfo> {
        every { this@mockk.userId } returns this@CredentialLessViewModelTest.userId
        every { this@mockk.username } returns null
    }

    private val createLoginSession = mockk<CreateLoginLessSession> {
        coEvery { this@mockk.invoke() } returns sessionInfo
    }
    private val postLoginAccountSetup = mockk<PostLoginLessAccountSetup> {
        coEvery { this@mockk.invoke(any()) } returns PostLoginAccountSetup.UserCheckResult.Success
    }

    private lateinit var viewModel: CredentialLessViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = CredentialLessViewModel(createLoginSession, postLoginAccountSetup)
    }

    @Test
    fun happyPath() = coroutinesTest {
        viewModel.state.test {
            // WHEN
            viewModel.startLoginLessWorkflow()

            // THEN
            assertIs<CredentialLessViewModel.State.Idle>(awaitItem())
            assertIs<CredentialLessViewModel.State.AccountSetupResult>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun anyError() = coroutinesTest {
        // GIVEN
        coEvery { createLoginSession.invoke() } throws Throwable("Error")

        viewModel.state.test {
            // WHEN
            viewModel.startLoginLessWorkflow()

            // THEN
            assertIs<CredentialLessViewModel.State.Idle>(awaitItem())
            assertIs<CredentialLessViewModel.State.Error>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onCredentialLessInvalidError() = coroutinesTest {
        // GIVEN
        coEvery { createLoginSession.invoke() } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 422,
                message = "Invalid",
                proton = ApiResult.Error.ProtonData(
                    code = ResponseCodes.ACCOUNT_CREDENTIALLESS_INVALID,
                    error = "Abuse detected"
                )
            )
        )

        viewModel.state.test {
            // WHEN
            viewModel.startLoginLessWorkflow()

            // THEN
            assertIs<CredentialLessViewModel.State.Idle>(awaitItem())
            assertIs<CredentialLessViewModel.State.CredentialLessDisabled>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
