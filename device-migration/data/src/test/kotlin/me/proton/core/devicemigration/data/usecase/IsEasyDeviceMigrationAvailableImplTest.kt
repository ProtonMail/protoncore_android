/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.data.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.biometric.data.StrongAuthenticatorsResolver
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability
import me.proton.core.devicemigration.domain.feature.IsEasyDeviceMigrationEnabled
import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.usersettings.domain.usecase.IsUserSettingEnabled
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsEasyDeviceMigrationAvailableImplTest {
    @MockK
    private lateinit var checkBiometricAuthAvailability: CheckBiometricAuthAvailability

    @MockK
    private lateinit var isEasyDeviceMigrationEnabled: IsEasyDeviceMigrationEnabled

    @MockK
    private lateinit var isUserSettingEnabled: IsUserSettingEnabled

    @MockK
    private lateinit var passphraseRepository: PassphraseRepository

    @MockK
    private lateinit var strongAuthenticatorsResolver: StrongAuthenticatorsResolver

    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: IsEasyDeviceMigrationAvailable

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `edm feature flag disabled`() = runTest {
        // Given
        makeTested()
        every { isEasyDeviceMigrationEnabled(any()) } returns false

        // When
        val result = tested(userId = null)

        // Then
        assertFalse(result)
    }

    @Test
    fun `edm user setting disabled`() = runTest {
        // Given
        makeTested()
        every { isEasyDeviceMigrationEnabled(any()) } returns true
        coEvery { isUserSettingEnabled(any(), any(), any()) } returns true // easyDeviceMigrationOptOut

        // When
        val result = tested(userId = UserId("id-1"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `edm user setting enabled`() = runTest {
        // Given
        makeTested()
        every { checkBiometricAuthAvailability(any(), any()) } returns CheckBiometricAuthAvailability.Result.Success
        every { isEasyDeviceMigrationEnabled(any()) } returns true
        coEvery { isUserSettingEnabled(any(), any(), any()) } returns false // easyDeviceMigrationOptOut
        coEvery { passphraseRepository.getPassphrase(any()) } returns mockk()

        // When
        val result = tested(userId = UserId("id-1"))

        // Then
        assertTrue(result)
    }

    @Test
    fun `edm user setting disabled because no passphrase`() = runTest {
        // Given
        makeTested()
        every { checkBiometricAuthAvailability(any(), any()) } returns CheckBiometricAuthAvailability.Result.Success
        every { isEasyDeviceMigrationEnabled(any()) } returns true
        coEvery { isUserSettingEnabled(any(), any(), any()) } returns false // easyDeviceMigrationOptOut
        coEvery { passphraseRepository.getPassphrase(any()) } returns null

        // When
        val result = tested(userId = UserId("id-1"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `edm user setting enabled on vpn with no passphrase and no user keys`() = runTest {
        // Given
        makeTested(Product.Vpn)
        every { checkBiometricAuthAvailability(any(), any()) } returns CheckBiometricAuthAvailability.Result.Success
        every { isEasyDeviceMigrationEnabled(any()) } returns true
        coEvery { isUserSettingEnabled(any(), any(), any()) } returns false // easyDeviceMigrationOptOut
        coEvery { passphraseRepository.getPassphrase(any()) } returns null
        coEvery { userManager.getUser(any()) } returns mockk(relaxed = true) {
            every { keys } returns emptyList()
        }

        // When
        val result = tested(userId = UserId("id-1"))

        // Then
        assertTrue(result)
    }

    @Test
    fun `edm user setting disabled on vpn with no passphrase and user keys`() = runTest {
        // Given
        makeTested(Product.Vpn)
        every { checkBiometricAuthAvailability(any(), any()) } returns CheckBiometricAuthAvailability.Result.Success
        every { isEasyDeviceMigrationEnabled(any()) } returns true
        coEvery { isUserSettingEnabled(any(), any(), any()) } returns false // easyDeviceMigrationOptOut
        coEvery { passphraseRepository.getPassphrase(any()) } returns null
        coEvery { userManager.getUser(any()) } returns mockk(relaxed = true) {
            every { keys } returns listOf(mockk())
        }

        // When
        val result = tested(userId = UserId("id-1"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `edm enabled for anonymous user`() = runTest {
        // Given
        makeTested()
        every { isEasyDeviceMigrationEnabled(any()) } returns true

        // When
        val result = tested(userId = null)

        // Then
        assertTrue(result)
    }

    @Test
    fun `edm user setting disabled because no biometrics`() = runTest {
        // Given
        makeTested()
        every {
            checkBiometricAuthAvailability(any(), any())
        } returns CheckBiometricAuthAvailability.Result.Failure.NotEnrolled
        every { isEasyDeviceMigrationEnabled(any()) } returns true
        coEvery { isUserSettingEnabled(any(), any(), any()) } returns false // easyDeviceMigrationOptOut

        // When
        val result = tested(userId = UserId("id-1"))

        // Then
        assertFalse(result)
    }

    private fun makeTested(product: Product = Product.Mail) {
        tested = IsEasyDeviceMigrationAvailableImpl(
            checkBiometricAuthAvailability = checkBiometricAuthAvailability,
            isEasyDeviceMigrationEnabled = isEasyDeviceMigrationEnabled,
            isUserSettingsEnabled = isUserSettingEnabled,
            passphraseRepository = passphraseRepository,
            product = product,
            strongAuthenticatorsResolver = strongAuthenticatorsResolver,
            userManager = userManager
        )
    }
}
