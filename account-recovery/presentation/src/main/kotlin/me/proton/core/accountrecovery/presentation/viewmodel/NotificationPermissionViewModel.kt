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

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.os.Build
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.accountrecovery.domain.AccountRecoveryNotificationManager
import me.proton.core.accountrecovery.presentation.internal.GetAndroidSdkLevel
import javax.inject.Inject

@HiltViewModel
internal class NotificationPermissionViewModel @Inject constructor(
    private val getAndroidSdkLevel: GetAndroidSdkLevel,
    private val notificationManager: AccountRecoveryNotificationManager
) : ViewModel() {
    private val _state = MutableStateFlow(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    fun setup(activity: Activity) = when {
        getAndroidSdkLevel() < Build.VERSION_CODES.TIRAMISU -> {
            // On Android SDK level lower than 33,
            // notification permission is not needed.
            _state.value = State.Finish
        }

        activity.applicationContext.applicationInfo.targetSdkVersion < Build.VERSION_CODES.TIRAMISU -> {
            // For apps targeting Android SDK level lower than 33,
            // the only way to trigger a request for notification permission
            // is to create a notification channel.
            notificationManager.setupNotificationChannel()
            _state.value = State.Finish
        }

        activity.shouldShowRequestPermissionRationale(POST_NOTIFICATIONS) -> {
            _state.value = State.ShowRationale
        }

        else -> {
            _state.value = State.LaunchPermissionRequest
        }
    }

    fun onNotificationPermissionRequestResult(isGranted: Boolean) {
        _state.value = when {
            isGranted -> {
                notificationManager.setupNotificationChannel()
                State.Finish
            }

            else -> State.Finish
        }
    }

    enum class State {
        Idle,
        Finish,
        ShowRationale,
        LaunchPermissionRequest
    }
}
