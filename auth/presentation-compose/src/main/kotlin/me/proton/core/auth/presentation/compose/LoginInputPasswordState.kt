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

package me.proton.core.auth.presentation.compose

import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.domain.entity.UserId

public sealed interface LoginInputPasswordState {
    public data object Idle : LoginInputPasswordState
    public data object Close : LoginInputPasswordState
    public data object Processing : LoginInputPasswordState
    public data object ValidationError : LoginInputPasswordState
    public data object ExternalEmailNotSupported : LoginInputPasswordState
    public data object ExternalSsoNotSupported : LoginInputPasswordState
    public data object ChangePassword : LoginInputPasswordState
    public data class NeedSrp(val authInfo: AuthInfo.Srp) : LoginInputPasswordState
    public data class NeedSso(val authInfo: AuthInfo.Sso) : LoginInputPasswordState
    public data class Error(val message: String?, val isPotentialBlocking: Boolean = false) : LoginInputPasswordState
    public data class Success(val userId: UserId) : LoginInputPasswordState
    public data class UserCheckError(
        val message: String,
        val action: UserCheckAction? = null
    ) : LoginInputPasswordState

    public val isLoading: Boolean
        get() = when (this) {
            is Processing,
            is Success,
            is Close,
            is NeedSrp,
            is NeedSso -> true

            is Idle,
            is Error,
            is ChangePassword,
            is ExternalEmailNotSupported,
            is ExternalSsoNotSupported,
            is UserCheckError,
            is ValidationError -> false
        }
}
