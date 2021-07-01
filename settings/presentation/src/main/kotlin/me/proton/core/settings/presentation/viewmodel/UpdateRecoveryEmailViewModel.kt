/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.settings.presentation.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
class UpdateRecoveryEmailViewModel @Inject constructor(
) : ProtonViewModel() {

    sealed class CurrentRecoveryEmailState {
        object Idle : CurrentRecoveryEmailState()
        object Processing : CurrentRecoveryEmailState()
        data class Success(val recoveryEmail: String) : CurrentRecoveryEmailState()
        sealed class Error {
            data class Message(val message: String?) : CurrentRecoveryEmailState.Error()
        }
    }

    sealed class UpdateRecoveryEmailState {
        object Idle : UpdateRecoveryEmailState()
        object Processing : UpdateRecoveryEmailState()
        data class Success(val recoveryEmail: String) : UpdateRecoveryEmailState()
        sealed class Error {
            data class Message(val message: String?) : UpdateRecoveryEmailState.Error()
        }
    }

    fun getCurrentRecoveryAddress() {

    }

    fun updateRecoveryEmail() {

    }
}
