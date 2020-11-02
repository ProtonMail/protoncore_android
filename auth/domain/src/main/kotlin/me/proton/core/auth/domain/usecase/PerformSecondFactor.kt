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
import me.proton.core.auth.domain.repository.AuthRepository
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
    sealed class SecondFactorState {
        object Processing : SecondFactorState()
        data class Success(
            val sessionId: SessionId,
            val scopeInfo: ScopeInfo,
            val isTwoPassModeNeeded: Boolean
        ) : SecondFactorState()

        sealed class Error : SecondFactorState() {
            data class Message(val message: String?, val localError: Int = 0) : Error()
            object EmptyCredentials : Error()
            object Unrecoverable : Error()
        }
    }

    /**
     * Currently only supported Second Factor Code.
     * U2F still not supported.
     */
    operator fun invoke(
        sessionId: SessionId,
        secondFactorCode: String,
        isTwoPassModeNeeded: Boolean = false
    ): Flow<SecondFactorState> = flow {

        if (secondFactorCode.isEmpty()) {
            emit(SecondFactorState.Error.EmptyCredentials)
            return@flow
        }

        emit(SecondFactorState.Processing)

        authRepository.performSecondFactor(
            sessionId,
            SecondFactorProof.SecondFactorCode(secondFactorCode),
        ).onFailure { errorMessage, _, httpCode ->
            when (httpCode) {
                HTTP_ERROR_BAD_REQUEST,
                HTTP_ERROR_UNAUTHORIZED -> emit(SecondFactorState.Error.Unrecoverable)
                else -> emit(SecondFactorState.Error.Message(errorMessage))
            }
        }.onSuccess { scopeInfo ->
            emit(SecondFactorState.Success(sessionId, scopeInfo, isTwoPassModeNeeded))
        }
    }

    companion object {
        const val HTTP_ERROR_UNAUTHORIZED = 401
        const val HTTP_ERROR_BAD_REQUEST = 400
    }
}
