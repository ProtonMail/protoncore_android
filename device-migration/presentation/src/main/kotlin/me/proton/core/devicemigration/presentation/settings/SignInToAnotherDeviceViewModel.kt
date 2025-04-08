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

package me.proton.core.devicemigration.presentation.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import javax.inject.Inject

@HiltViewModel
public class SignInToAnotherDeviceViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val isEasyDeviceMigrationAvailable: IsEasyDeviceMigrationAvailable
) : BaseViewModel<SignInToAnotherDeviceAction, SignInToAnotherDeviceState>(
    initialAction = SignInToAnotherDeviceAction.Load,
    initialState = SignInToAnotherDeviceState.Hidden
) {
    override fun onAction(action: SignInToAnotherDeviceAction): Flow<SignInToAnotherDeviceState> = when (action) {
        is SignInToAnotherDeviceAction.Load -> onLoad()
    }

    override suspend fun FlowCollector<SignInToAnotherDeviceState>.onError(throwable: Throwable) {
        emit(SignInToAnotherDeviceState.Hidden)
    }

    private fun onLoad(): Flow<SignInToAnotherDeviceState> = accountManager.getPrimaryUserId().mapLatest { userId ->
        when (userId != null && isEasyDeviceMigrationAvailable(userId)) {
            true -> SignInToAnotherDeviceState.Visible(userId)
            false -> SignInToAnotherDeviceState.Hidden
        }
    }
}
