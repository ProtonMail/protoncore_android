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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.entity.Fido2Info
import me.proton.core.auth.domain.entity.SecondFactor
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.auth.domain.usecase.GetAuthInfoSrp
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.usersettings.domain.usecase.GetUserSettings
import javax.inject.Inject

@HiltViewModel
class TwoFAInputDialogViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getAuthInfoSrp: GetAuthInfoSrp,
    private val getUserSettings: GetUserSettings,
    private val isFido2Enabled: IsFido2Enabled,
) : ProtonViewModel() {

    var fido2Info: Fido2Info? = null

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)

    val state = _state.asSharedFlow()

    sealed class State {
        data class Idle(val showSecurityKey: Boolean) : State()

        sealed class Error : State() {
            object InvalidAccount : Error()
        }
    }

    fun setup(userId: UserId) = flow {
        when {
            !isFido2Enabled(userId) -> emit(State.Idle(false))
            else -> {
                val userSettings = getUserSettings(userId, refresh = false)
                val account = accountManager.getAccount(userId).firstOrNull()
                if (account == null) {
                    emit(State.Error.InvalidAccount)
                    return@flow
                }
                val authInfo = getAuthInfoSrp(requireNotNull(account.sessionId), requireNotNull(account.username))
                val secondFactor = authInfo.secondFactor as? SecondFactor.Enabled
                fido2Info = secondFactor?.fido2
                emit(
                    State.Idle(userSettings.twoFA?.registeredKeys?.isNotEmpty() == true)
                )
            }
        }
    }.catch { _ ->
        emit(State.Idle(false))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)
}
