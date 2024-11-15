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

package me.proton.core.auth.domain

import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.domain.entity.UserId

sealed interface LoginState {
    data object Processing : LoginState
    data class NeedAuthSecret(val authInfo: AuthInfo) : LoginState
    data class LoggedIn(val userId: UserId) : LoginState

    sealed interface Error : LoginState {
        data class Message(val error: Throwable, val isPotentialBlocking: Boolean) : Error
        data class SwitchToSrp(val username: String, val error: Throwable) : Error
        data class SwitchToSso(val username: String, val error: Throwable) : Error
        data class InvalidPassword(val error: Throwable) : Error
        data class ExternalNotSupported(val error: Throwable) : Error
        data class UserCheck(val message: String, val action: UserCheckAction? = null) : Error
        data object UnlockPrimaryKey : Error
        data object ChangePassword : Error
    }
}
