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

    sealed class UsernameAvailabilityState {
        object Processing : UsernameAvailabilityState()
        data class Success(val available: Boolean, val username: String, val domain: String? = null) :
            UsernameAvailabilityState()

        sealed class Error : UsernameAvailabilityState() {
            data class Message(val message: String?) : Error()
            object UsernameUnavailable : Error()
            object EmptyUsername : Error()
        }
    }

    operator fun invoke(username: String): Flow<UsernameAvailabilityState> = flow {
        if (username.isBlank()) {
            emit(UsernameAvailabilityState.Error.EmptyUsername)
            return@flow
        }
        emit(UsernameAvailabilityState.Processing)

        authRepository.isUsernameAvailable(username)
            .onFailure { message, code, _ ->
                if (code == RESPONSE_CODE_USERNAME_UNAVAILABLE) {
                    emit(UsernameAvailabilityState.Error.UsernameUnavailable)
                } else {
                    emit(UsernameAvailabilityState.Error.Message(message))
                }
            }.onSuccess {
                emit(UsernameAvailabilityState.Success(it, username))
            }
    }

    companion object {
        const val RESPONSE_CODE_USERNAME_UNAVAILABLE = 12106
    }
}
