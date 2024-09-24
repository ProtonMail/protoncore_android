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
import io.mockk.impl.annotations.MockK
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.sso.CreateAuthDevice
import me.proton.core.auth.domain.usecase.sso.GenerateDeviceSecret
import me.proton.core.auth.domain.usecase.sso.VerifyUnprivatization
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import me.proton.core.usersettings.domain.usecase.GetOrganization
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupPasswordSetupViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var context: CryptoContext

    @MockK
    private lateinit var getOrganization: GetOrganization

    @MockK
    private lateinit var generateDeviceSecret: GenerateDeviceSecret

    @MockK
    private lateinit var verifyUnprivatization: VerifyUnprivatization

    @MockK
    private lateinit var setupPrimaryKeys: SetupPrimaryKeys

    @MockK
    private lateinit var createAuthDevice: CreateAuthDevice

    @MockK
    private lateinit var organizationRepository: OrganizationRepository

    private lateinit var tested: BackupPasswordSetupViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = BackupPasswordSetupViewModel(
            savedStateHandle = SavedStateHandle(mapOf(DeviceSecretRoutes.Arg.KEY_USER_ID to "user-id")),
            context = context,
            generateDeviceSecret = generateDeviceSecret,
            verifyUnprivatization = verifyUnprivatization,
            setupPrimaryKeys = setupPrimaryKeys,
            createAuthDevice = createAuthDevice,
            organizationRepository = organizationRepository
        )
    }

    @Test
    fun `password too short`() = coroutinesTest {
        tested.state.test {
            assertEquals(BackupPasswordSetupState.Idle(BackupPasswordSetupData()), awaitItem())

            // WHEN
            tested.submit(BackupPasswordSetupAction.SetPassword("1234", "1234")).join()

            // THEN
            assertEquals(BackupPasswordSetupState.Loading(BackupPasswordSetupData()), awaitItem())
            assertEquals(
                expected = BackupPasswordSetupState.FormError(
                    data = BackupPasswordSetupData(),
                    cause = BackupPasswordSetupFormError.PasswordTooShort
                ),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `passwords not matching`() = coroutinesTest {
        tested.state.test {
            assertEquals(BackupPasswordSetupState.Idle(BackupPasswordSetupData()), awaitItem())

            // WHEN
            tested.submit(BackupPasswordSetupAction.SetPassword("12341234", "123412345")).join()

            // THEN
            assertEquals(BackupPasswordSetupState.Loading(BackupPasswordSetupData()), awaitItem())
            assertEquals(
                expected = BackupPasswordSetupState.FormError(
                    data = BackupPasswordSetupData(),
                    cause = BackupPasswordSetupFormError.PasswordsDoNotMatch
                ),
                actual = awaitItem()
            )
        }
    }
}
