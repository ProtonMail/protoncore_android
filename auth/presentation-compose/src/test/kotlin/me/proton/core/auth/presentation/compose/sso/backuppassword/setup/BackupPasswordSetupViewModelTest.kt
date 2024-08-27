/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.presentation.compose.sso.backuppassword.setup

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.yield
import me.proton.core.domain.entity.Product
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.usersettings.domain.usecase.GetOrganization
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupPasswordSetupViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var getOrganization: GetOrganization

    private lateinit var tested: BackupPasswordSetupViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = BackupPasswordSetupViewModel(
            getOrganization, Product.Mail, SavedStateHandle(mapOf(BackupPasswordSetupScreen.KEY_USERID to "user-id"))
        )
    }

    @Test
    fun `loading organization data`() = coroutinesTest {
        // GIVEN
        coEvery { getOrganization(any(), any()) } coAnswers {
            yield()
            mockk {
                every { displayName } returns "Test Organization"
            }
        }

        tested.data.combine(tested.state) { data, state -> data to state }.test {
            assertEquals(
                Pair(BackupPasswordSetupUiData(product = Product.Mail), BackupPasswordSetupUiState.Idle),
                awaitItem()
            )

            assertEquals(
                Pair(BackupPasswordSetupUiData(product = Product.Mail), BackupPasswordSetupUiState.Loading),
                awaitItem()
            )

            assertEquals(
                Pair(
                    BackupPasswordSetupUiData(
                        organizationName = "Test Organization",
                        product = Product.Mail
                    ), BackupPasswordSetupUiState.Loading
                ),
                awaitItem()
            )

            assertEquals(
                Pair(
                    BackupPasswordSetupUiData(
                        organizationName = "Test Organization",
                        product = Product.Mail
                    ), BackupPasswordSetupUiState.Idle
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `loading organization data - failure`() = coroutinesTest {
        // GIVEN
        val loadingError = ApiException(ApiResult.Error.Timeout(isConnectedToNetwork = true))
        coEvery { getOrganization(any(), any()) } coAnswers {
            yield()
            throw loadingError
        }

        tested.data.combine(tested.state) { data, state -> data to state }.test {
            assertEquals(
                Pair(BackupPasswordSetupUiData(product = Product.Mail), BackupPasswordSetupUiState.Idle),
                awaitItem()
            )

            assertEquals(
                Pair(BackupPasswordSetupUiData(product = Product.Mail), BackupPasswordSetupUiState.Loading),
                awaitItem()
            )

            assertEquals(
                Pair(BackupPasswordSetupUiData(product = Product.Mail), BackupPasswordSetupUiState.Error(loadingError)),
                awaitItem()
            )
        }
    }

    @Test
    fun `password too short`() = coroutinesTest {
        tested.state.test {
            assertEquals(BackupPasswordSetupUiState.Idle, awaitItem())

            // WHEN
            tested.submit(BackupPasswordSetupAction.Submit("1234", "1234")).join()

            // THEN
            assertEquals(BackupPasswordSetupUiState.Loading, awaitItem())
            assertEquals(
                BackupPasswordSetupUiState.FormError(BackupPasswordSetupFormError.PasswordTooShort),
                awaitItem()
            )
        }
    }

    @Test
    fun `passwords not matching`() = coroutinesTest {
        tested.state.test {
            assertEquals(BackupPasswordSetupUiState.Idle, awaitItem())

            // WHEN
            tested.submit(BackupPasswordSetupAction.Submit("12341234", "123412345")).join()

            // THEN
            assertEquals(BackupPasswordSetupUiState.Loading, awaitItem())
            assertEquals(
                BackupPasswordSetupUiState.FormError(BackupPasswordSetupFormError.PasswordsDoNotMatch), awaitItem()
            )
        }
    }
}
