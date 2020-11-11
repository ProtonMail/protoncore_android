/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.extension.onEachInstance
import me.proton.core.domain.arch.onFailure
import me.proton.core.domain.arch.onSuccess
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

/**
 * Fetches user details.
 *
 * @param authRepository mandatory authentication repository interface for contacting the api.
 * @author Dino Kadrikj.
 */
class GetUser @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * State sealed class with various (success, error) outcome state subclasses.
     */
    sealed class UserState {
        object Processing : UserState()
        data class Success(val user: User) : UserState()
        sealed class Error : UserState() {
            data class Message(val message: String?) : Error()
        }
    }

    /**
     * Generates the passphrase, derived from the login password for Single Password Accounts or from
     * the Mailbox Password for Two Password Accounts.
     *
     */
    operator fun invoke(sessionId: SessionId): Flow<UserState> = flow {
        emit(UserState.Processing)

        authRepository.getUser(sessionId)
            .onFailure { errorMessage, _, _ ->
                emit(UserState.Error.Message(errorMessage))
            }
            .onSuccess {
                emit(UserState.Success(it))
            }
    }
}

fun Flow<GetUser.UserState>.onUserSuccess(
    action: suspend (GetUser.UserState.Success) -> Unit
) = onEachInstance(action) as Flow<GetUser.UserState>

fun Flow<GetUser.UserState>.onUserError(
    action: suspend (GetUser.UserState.Error) -> Unit
) = onEachInstance(action) as Flow<GetUser.UserState>
