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

import com.google.crypto.tink.subtle.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.auth.domain.crypto.SrpProofProvider
import me.proton.core.auth.domain.crypto.SrpProofs
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.onFailure
import me.proton.core.domain.arch.onSuccess
import javax.inject.Inject

/**
 * Performs the login request along with the login info request which is always preceding it.
 *
 * @param authRepository mandatory authentication repository interface for contacting the API.
 * @param srpProofProvider [SrpProofProvider] implementation for generation of [SrpProofs].
 * @param clientSecret for [AuthRepository.getLoginInfo] and [AuthRepository.performLogin] API routes.
 * @author Dino Kadrikj.
 */
class PerformLogin @Inject constructor(
    private val authRepository: AuthRepository,
    private val srpProofProvider: SrpProofProvider,
    @ClientSecret private val clientSecret: String
) {

    /**
     * State sealed class with various (success, error) outcome state subclasses.
     */
    sealed class LoginState {
        object Processing : LoginState()
        data class Success(val sessionInfo: SessionInfo) : LoginState()
        sealed class Error : LoginState() {
            data class Message(val message: String?, val validation: Boolean = false, val localError: Int = 0) : Error()
            object EmptyCredentials : Error()
        }
    }

    operator fun invoke(
        username: String,
        password: ByteArray
    ): Flow<LoginState> = flow {

        if (username.isBlank() || password.isEmpty()) {
            emit(LoginState.Error.EmptyCredentials)
            return@flow
        }

        emit(LoginState.Processing)

        authRepository.getLoginInfo(
            username = username,
            clientSecret = clientSecret
        ).onFailure { errorMessage, _ ->
            emit(LoginState.Error.Message(errorMessage))
        }.onSuccess { loginInfo ->
            val clientProofs: SrpProofs = srpProofProvider.generateSrpProofs(
                username = username,
                passphrase = password,
                info = loginInfo
            )

            authRepository.performLogin(
                username = username,
                clientSecret = clientSecret,
                clientEphemeral = Base64.encode(clientProofs.clientEphemeral),
                clientProof = Base64.encode(clientProofs.clientProof),
                srpSession = loginInfo.srpSession
            ).onFailure { errorMessage, code ->
                emit(LoginState.Error.Message(errorMessage, code == RESPONSE_CODE_INCORRECT_CREDENTIALS))
            }.onSuccess { sessionInfo ->
                var result = sessionInfo
                if (sessionInfo.isSecondFactorNeeded && !sessionInfo.isTwoPassModeNeeded) {
                    result = sessionInfo.copy(loginPassword = password)
                }
                emit(LoginState.Success(result))
            }
        }
    }

    companion object {
        const val RESPONSE_CODE_INCORRECT_CREDENTIALS = 8002
    }
}
