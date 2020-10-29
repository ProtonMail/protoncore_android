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
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

/**
 * Performs the user logout procedure. It removes the current session on the API.
 *
 * @param authRepository mandatory authentication repository interface for contacting the api.
 * @author Dino Kadrikj.
 */
class PerformLogout @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * State sealed class with various (success, error) outcome state subclasses.
     */
    sealed class LogoutState {
        object Processing : LogoutState()
        data class Success(val sessionRevoked: Boolean) : LogoutState()
        sealed class Error : LogoutState() {
            data class Message(val message: String?) : Error()
        }
    }

    operator fun invoke(sessionId: SessionId): Flow<LogoutState> = flow {
        emit(LogoutState.Processing)

        authRepository.revokeSession(sessionId = sessionId)
            .onFailure { errorMessage, _, _ ->
                emit(LogoutState.Error.Message(errorMessage))
            }
            .onSuccess {
                emit(LogoutState.Success(it))
            }
    }
}
