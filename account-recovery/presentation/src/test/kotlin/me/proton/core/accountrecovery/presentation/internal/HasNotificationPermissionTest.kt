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

package me.proton.core.accountrecovery.presentation.internal

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HasNotificationPermissionTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var getAndroidSdkLevel: GetAndroidSdkLevel

    private lateinit var tested: HasNotificationPermission

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = HasNotificationPermission(context, getAndroidSdkLevel)
    }

    @Test
    fun beforeTiramisu() {
        mockSdkInt(Build.VERSION_CODES.S)
        every { context.getSystemService(any()) } returns mockk<NotificationManager> {
            every { areNotificationsEnabled() } returns true
        }
        assertTrue(tested())
    }

    @Test
    fun permissionDenied() {
        every {
            context.checkSelfPermission(any())
        } returns PackageManager.PERMISSION_DENIED
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)

        assertFalse(tested())
    }

    @Test
    fun permissionGranted() {
        every {
            context.checkSelfPermission(any())
        } returns PackageManager.PERMISSION_GRANTED
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)

        assertTrue(tested())
    }

    private fun mockSdkInt(version: Int) = every { getAndroidSdkLevel() } returns version
}
