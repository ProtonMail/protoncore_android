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
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.extension.onEachInstance
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
    sealed class State {
        object Processing : State()

        sealed class Success : State() {
            class Login(val sessionInfo: SessionInfo) : Success()
            class UserSetup(val sessionInfo: SessionInfo, val user: User) : Success()
        }

        sealed class Error : State() {
            data class Message(val message: String?, val validation: Boolean = false) : Error()
            object EmptyCredentials : Error()
            data class UserSetup(val state: PerformUserSetup.State.Error) : Error()
            data class AccountUpgrade(val state: UpdateUsernameOnlyAccount.State.Error) : Error()
            data class FetchUser(val state: GetUser.State.Error) : Error()
            object PasswordChange : Error()
        }
    }

    operator fun invoke(
        username: String,
        password: ByteArray
    ): Flow<State> = flow {

        if (username.isBlank() || password.isEmpty()) {
            emit(State.Error.EmptyCredentials)
            return@flow
        }

        emit(State.Processing)

        authRepository.getLoginInfo(
            username = username,
            clientSecret = clientSecret
        ).onFailure { errorMessage, _, _ ->
            emit(State.Error.Message(errorMessage))
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
            ).onFailure { errorMessage, protonCode, _ ->
                emit(State.Error.Message(errorMessage, protonCode == RESPONSE_CODE_INCORRECT_CREDENTIALS))
            }.onSuccess { sessionInfo ->
                emit(State.Success.Login(sessionInfo))
            }
        }
    }

    companion object {
        const val RESPONSE_CODE_INCORRECT_CREDENTIALS = 8002
    }
}

fun Flow<PerformLogin.State>.onProcessing(
    action: suspend (PerformLogin.State.Processing) -> Unit
) = onEachInstance(action) as Flow<PerformLogin.State>

fun Flow<PerformLogin.State>.onLoginSuccess(
    action: suspend (PerformLogin.State.Success.Login) -> Unit
) = onEachInstance(action) as Flow<PerformLogin.State>

fun Flow<PerformLogin.State>.onError(
    action: suspend (PerformLogin.State.Error) -> Unit
) = onEachInstance(action) as Flow<PerformLogin.State>
