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

package me.proton.core.devicemigration.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.devicemigration.domain.feature.IsEasyDeviceMigrationEnabled
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.usecase.IsUserSettingEnabled
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsEasyDeviceMigrationAvailableImplTest {
    @MockK
    private lateinit var isEasyDeviceMigrationEnabled: IsEasyDeviceMigrationEnabled

    @MockK
    private lateinit var isUserSettingEnabled: IsUserSettingEnabled

    private lateinit var tested: IsEasyDeviceMigrationAvailable

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = IsEasyDeviceMigrationAvailable(
            isEasyDeviceMigrationEnabled = isEasyDeviceMigrationEnabled,
            isUserSettingsEnabled = isUserSettingEnabled
        )
    }

    @Test
    fun `edm feature flag disabled`() = runTest {
        // Given
        every { isEasyDeviceMigrationEnabled(any()) } returns false

        // When
        val result = tested(userId = null)

        // Then
        assertFalse(result)
    }

    @Test
    fun `edm user setting disabled`() = runTest {
        // Given
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
        every { isEasyDeviceMigrationEnabled(any()) } returns true
        coEvery { isUserSettingEnabled(any(), any(), any()) } returns false // easyDeviceMigrationOptOut

        // When
        val result = tested(userId = UserId("id-1"))

        // Then
        assertTrue(result)
    }

    @Test
    fun `edm enabled for anonymous user`() = runTest {
        // Given
        every { isEasyDeviceMigrationEnabled(any()) } returns true

        // When
        val result = tested(userId = null)

        // Then
        assertTrue(result)
    }
}
