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

package me.proton.core.notification.presentation.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.domain.usecase.IsNotificationsPermissionRequestEnabled
import me.proton.core.notification.domain.usecase.IsNotificationsPermissionShowRationale
import me.proton.core.notification.presentation.internal.GetAndroidSdkLevel
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationPermissionViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var permissionRequestEnabled: IsNotificationsPermissionRequestEnabled

    @MockK
    private lateinit var permissionShowRationale: IsNotificationsPermissionShowRationale

    @MockK
    private lateinit var getAndroidSdkLevel: GetAndroidSdkLevel

    @MockK
    private lateinit var notificationManager: ProtonNotificationManager

    private lateinit var tested: NotificationPermissionViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { notificationManager.setupNotificationChannel() }
        coJustRun { permissionShowRationale.onShowRationale() }
        tested = NotificationPermissionViewModel(
            permissionRequestEnabled = permissionRequestEnabled,
            permissionShowRationale = permissionShowRationale,
            getAndroidSdkLevel = getAndroidSdkLevel,
            notificationManager = notificationManager
        )
    }

    @Test
    fun setupWithDisabledPermissionRequest() = runTest {
        // GIVEN
        mockPermissionRequestEnabled(false)
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            every { shouldShowRequestPermissionRationale(any()) } returns true
            mockTargetSdk(Build.VERSION_CODES.TIRAMISU)
        }

        // WHEN
        tested.setup(activity).join()

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    @Test
    fun setupWithSdkBeforeTiramisu() = runTest {
        // GIVEN
        mockPermissionRequestEnabled(true)
        mockSdkInt(Build.VERSION_CODES.S)

        // THEN (initial state)
        assertEquals(
            NotificationPermissionViewModel.State.Idle,
            tested.state.value
        )

        // WHEN
        tested.setup(mockk()).join()

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    @Test
    fun setupWithTargetSdkBeforeTiramisu() = runTest {
        // GIVEN
        mockPermissionRequestEnabled(true)
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            mockTargetSdk(Build.VERSION_CODES.S)
        }

        // WHEN
        tested.setup(activity).join()

        // THEN
        verify { notificationManager.setupNotificationChannel() }
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    @Test
    fun setupOnTiramisuWithShouldShowRationale() = runTest {
        // GIVEN
        mockPermissionRequestEnabled(true)
        mockPermissionShowRationale(false)
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            every { shouldShowRequestPermissionRationale(any()) } returns true
            mockTargetSdk(Build.VERSION_CODES.TIRAMISU)
        }

        // WHEN
        tested.setup(activity).join()

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.ShowRationale,
            tested.state.value
        )
    }

    @Test
    fun setupOnTiramisuWithoutShouldShowRationale() = runTest {
        // GIVEN
        mockPermissionRequestEnabled(true)
        mockPermissionShowRationale(false)
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            every { shouldShowRequestPermissionRationale(any()) } returns false
            mockTargetSdk(Build.VERSION_CODES.TIRAMISU)
        }

        // WHEN
        tested.setup(activity).join()

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.LaunchPermissionRequest,
            tested.state.value
        )
    }

    @Test
    fun setupRationale() = runTest {
        // GIVEN
        mockPermissionRequestEnabled(true)
        mockPermissionShowRationale(true)
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            every { shouldShowRequestPermissionRationale(any()) } returns false
            mockTargetSdk(Build.VERSION_CODES.TIRAMISU)
        }

        // WHEN
        tested.setup(activity).join()

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.ShowRationale,
            tested.state.value
        )
    }

    @Test
    fun setupWithoutRationale() = runTest {
        // GIVEN
        mockPermissionRequestEnabled(true)
        mockPermissionShowRationale(false)
        mockSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity> {
            every { shouldShowRequestPermissionRationale(any()) } returns false
            mockTargetSdk(Build.VERSION_CODES.TIRAMISU)
        }

        // WHEN
        tested.setup(activity).join()

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.LaunchPermissionRequest,
            tested.state.value
        )
    }

    @Test
    fun onPermissionGranted() {
        // GIVEN
        mockPermissionRequestEnabled(true)

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
        // GIVEN
        mockPermissionRequestEnabled(true)

        // WHEN
        tested.onNotificationPermissionRequestResult(false)

        // THEN
        assertEquals(
            NotificationPermissionViewModel.State.Finish,
            tested.state.value
        )
    }

    private fun mockPermissionRequestEnabled(enabled: Boolean) =
        every { permissionRequestEnabled() } returns enabled

    private fun mockPermissionShowRationale(enabled: Boolean) =
        coEvery { permissionShowRationale.invoke() } returns enabled

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
