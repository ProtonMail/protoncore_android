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

package me.proton.core.accountrecovery.presentation.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.accountrecovery.domain.AccountRecoveryNotificationManager
import me.proton.core.accountrecovery.presentation.internal.GetAndroidSdkLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationPermissionViewModelTest {
    @MockK
    private lateinit var getAndroidSdkLevel: GetAndroidSdkLevel

    @MockK
    private lateinit var notificationManager: AccountRecoveryNotificationManager

    private lateinit var tested: NotificationPermissionViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { notificationManager.setupNotificationChannel() }
        tested = NotificationPermissionViewModel(getAndroidSdkLevel, notificationManager)
    }

    @Test
    fun setupWithSdkBeforeTiramisu() {
        // GIVEN
        mockSdkInt(Build.VERSION_CODES.S)

        // THEN (initial state)
        assertEquals(
            NotificationPermissionViewModel.State.Idle,
            tested.state.value
        )

        // WHEN
        tested.setup(mockk())

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    @Test
    fun setupWithTargetSdkBeforeTiramisu() {
        // GIVEN
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            mockTargetSdk(Build.VERSION_CODES.S)
        }

        // WHEN
        tested.setup(activity)

        // THEN
        verify { notificationManager.setupNotificationChannel() }
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    @Test
    fun setupOnTiramisuWithRationale() {
        // GIVEN
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            every { shouldShowRequestPermissionRationale(any()) } returns true
            mockTargetSdk(Build.VERSION_CODES.TIRAMISU)
        }

        // WHEN
        tested.setup(activity)

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.ShowRationale,
            tested.state.value
        )
    }

    @Test
    fun setupOnTiramisuWithoutRationale() {
        // GIVEN
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            every { shouldShowRequestPermissionRationale(any()) } returns false
            mockTargetSdk(Build.VERSION_CODES.TIRAMISU)
        }

        // WHEN
        tested.setup(activity)

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.LaunchPermissionRequest,
            tested.state.value
        )
    }

    @Test
    fun onPermissionGranted() {
        // WHEN
        tested.onNotificationPermissionRequestResult(true)

        // THEN
        verify { notificationManager.setupNotificationChannel() }
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    @Test
    fun onPermissionDenied() {
        // WHEN
        tested.onNotificationPermissionRequestResult(false)

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    private fun mockSdkInt(version: Int) = every { getAndroidSdkLevel() } returns version

    private fun Activity.mockTargetSdk(version: Int) {
        val appInfo = ApplicationInfo().apply {
            targetSdkVersion = version
        }
        val appContext = mockk<Context> {
            every { applicationInfo } returns appInfo
        }
        every { applicationContext } returns appContext
    }
}
