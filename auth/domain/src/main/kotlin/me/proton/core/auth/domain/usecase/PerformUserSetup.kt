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

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.extension.onEachInstance
import me.proton.core.domain.arch.onFailure
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

/**
 * Performs user setup (user, address and passphrase generation and validation).
 *
 * @param authRepository mandatory authentication repository interface for contacting the api.
 * @param cryptoProvider the crypto provider interface for generation and validation of the passphrase.
 * @author Dino Kadrikj.
 */
class PerformUserSetup @Inject constructor(
    private val authRepository: AuthRepository,
    private val cryptoProvider: CryptoProvider
) {

    /**
     * State sealed class with various (success, error) outcome state subclasses.
     */
    sealed class State {
        object Processing : State()
        data class Success(val user: User) : State()
        sealed class Error : State() {
            data class Message(val message: String?) : Error()
            object NoPrimaryKey : Error()
            object NoKeySaltsForPrimaryKey : Error()
            object PrimaryKeyInvalidPassphrase : Error()
            object EmptyCredentials : Error()
        }
    }

    /**
     * Generates the passphrase, derived from the login password for Single Password Accounts or from
     * the Mailbox Password for Two Password Accounts.
     *
     * @param sessionId for the API calls (fetching users and salts).
     * @param password the login password or mailbox password.
     */
    operator fun invoke(
        sessionId: SessionId,
        password: ByteArray
    ): Flow<State> = flow {
        if (password.isEmpty()) {
            emit(State.Error.EmptyCredentials)
            return@flow
        }
        emit(State.Processing)

        val (userResult, saltsResult, addressesResult) = coroutineScope {
            val user = async {
                authRepository.getUser(sessionId)
            }
            val salts = async {
                authRepository.getSalts(sessionId)
            }
            val addresses = async {
                authRepository.getAddresses(sessionId)
            }
            Triple(user.await(), salts.await(), addresses.await())
        }

        userResult.onFailure { errorMessage, _, _ ->
            emit(State.Error.Message(errorMessage))
            return@flow
        }
        saltsResult.onFailure { errorMessage, _, _ ->
            emit(State.Error.Message(errorMessage))
            return@flow
        }
        addressesResult.onFailure { errorMessage, _, _ ->
            emit(State.Error.Message(errorMessage))
            return@flow
        }

        val user = (userResult as DataResult.Success).value
        val salts = (saltsResult as DataResult.Success).value
        val addresses = (addressesResult as DataResult.Success).value

        if (user.primaryKey == null) {
            emit(State.Error.NoPrimaryKey)
            return@flow
        }

        val primaryKeySalt = salts.salts.find { it.keyId == user.primaryKey.id }?.takeIf { it.keySalt.isNotEmpty() }
        if (primaryKeySalt == null) {
            emit(State.Error.NoKeySaltsForPrimaryKey)
            return@flow
        }

        val passphrase = cryptoProvider.generatePassphrase(password, primaryKeySalt.keySalt)

        if (!cryptoProvider.passphraseCanUnlockKey(user.primaryKey.privateKey, passphrase)) {
            emit(State.Error.PrimaryKeyInvalidPassphrase)
            return@flow
        }

        emit(State.Success(user.copy(passphrase = passphrase, addresses = addresses)))
    }
}

fun Flow<PerformUserSetup.State>.onProcessing(
    action: suspend (PerformUserSetup.State.Processing) -> Unit
) = onEachInstance(action) as Flow<PerformUserSetup.State>

fun Flow<PerformUserSetup.State>.onSuccess(
    action: suspend (PerformUserSetup.State.Success) -> Unit
) = onEachInstance(action) as Flow<PerformUserSetup.State>

fun Flow<PerformUserSetup.State>.onError(
    action: suspend (PerformUserSetup.State.Error) -> Unit
) = onEachInstance(action) as Flow<PerformUserSetup.State>
