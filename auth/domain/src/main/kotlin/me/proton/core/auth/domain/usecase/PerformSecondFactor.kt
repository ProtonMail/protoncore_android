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
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.extension.onEachInstance
import me.proton.core.domain.arch.onFailure
import me.proton.core.domain.arch.onSuccess
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

/**
 * Performs Second Factor operation, for accounts that have enabled it.
 *
 * @param authRepository mandatory dependency for contacting the API.
 * @author Dino Kadrikj.
 */
class PerformSecondFactor @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * State sealed class with various (success, error) outcome state subclasses.
     */
    sealed class State {
        object Processing : State()

        sealed class Success : State() {
            class SecondFactor(
                val sessionId: SessionId,
                val scopeInfo: ScopeInfo
            ) : Success()

            class UserSetup(
                val sessionId: SessionId,
                val scopeInfo: ScopeInfo,
                val user: User
            ) : Success()
        }

        sealed class Error : State() {
            data class Message(val message: String?, val localError: Int = 0) : Error()
            object EmptyCredentials : Error()
            object Unrecoverable : Error()
            data class UserSetup(val state: PerformUserSetup.State.Error) : State.Error()
        }
    }

    /**
     * Currently only supported Second Factor Code.
     * U2F still not supported.
     */
    operator fun invoke(
        sessionId: SessionId,
        secondFactorCode: String
    ): Flow<State> = flow {

        if (secondFactorCode.isEmpty()) {
            emit(State.Error.EmptyCredentials)
            return@flow
        }

        emit(State.Processing)

        authRepository.performSecondFactor(
            sessionId,
            SecondFactorProof.SecondFactorCode(secondFactorCode),
        ).onFailure { errorMessage, _, httpCode ->
            when (httpCode) {
                HTTP_ERROR_BAD_REQUEST,
                HTTP_ERROR_UNAUTHORIZED -> emit(State.Error.Unrecoverable)
                else -> emit(State.Error.Message(errorMessage))
            }
        }.onSuccess { scopeInfo ->
            emit(State.Success.SecondFactor(sessionId, scopeInfo))
        }
    }

    companion object {
        const val HTTP_ERROR_UNAUTHORIZED = 401
        const val HTTP_ERROR_BAD_REQUEST = 400
    }
}

fun Flow<PerformSecondFactor.State>.onProcessing(
    action: suspend (PerformSecondFactor.State.Processing) -> Unit
) = onEachInstance(action) as Flow<PerformSecondFactor.State>

fun Flow<PerformSecondFactor.State>.onSecondFactorSuccess(
    action: suspend (PerformSecondFactor.State.Success.SecondFactor) -> Unit
) = onEachInstance(action) as Flow<PerformSecondFactor.State>

fun Flow<PerformSecondFactor.State>.onError(
    action: suspend (PerformSecondFactor.State.Error) -> Unit
) = onEachInstance(action) as Flow<PerformSecondFactor.State>
