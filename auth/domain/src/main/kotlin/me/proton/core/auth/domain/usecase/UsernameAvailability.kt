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
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.onFailure
import me.proton.core.domain.arch.onSuccess
import javax.inject.Inject

/**
 * Checks if the username picked from a user is available (no accounts exists with the associated username) in the
 * system.
 * @author Dino Kadrikj.
 */
class UsernameAvailability @Inject constructor(private val authRepository: AuthRepository) {

    sealed class State {
        object Processing : State()
        data class Success(val available: Boolean, val username: String, val domain: String? = null) :
            State()

        sealed class Error : State() {
            data class Message(val message: String?) : Error()
            object UsernameUnavailable : Error()
            object EmptyUsername : Error()
        }
    }

    operator fun invoke(username: String): Flow<State> = flow {
        if (username.isBlank()) {
            emit(State.Error.EmptyUsername)
            return@flow
        }
        emit(State.Processing)

        authRepository.isUsernameAvailable(username)
            .onFailure { message, code, _ ->
                if (code == RESPONSE_CODE_USERNAME_UNAVAILABLE) {
                    emit(State.Error.UsernameUnavailable)
                } else {
                    emit(State.Error.Message(message))
                }
            }.onSuccess {
                emit(State.Success(it, username))
            }
    }

    companion object {
        const val RESPONSE_CODE_USERNAME_UNAVAILABLE = 12106
    }
}
