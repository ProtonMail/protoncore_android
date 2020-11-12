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
import me.proton.core.domain.arch.extension.onEachInstance
import me.proton.core.domain.arch.onFailure
import me.proton.core.domain.arch.onSuccess
import javax.inject.Inject

/**
 * Use case that gets all available domains on the API for the address creation purposes.
 *
 * @author Dino Kadrikj.
 */
class AvailableDomains @Inject constructor(private val authRepository: AuthRepository) {

    sealed class State {
        data class Success(val availableDomains: List<String>) : State() {
            val firstOrDefault = availableDomains.firstOrNull() ?: "protonmail.com"
        }

        sealed class Error : State() {
            data class Message(val message: String?) : State.Error()
            object NoAvailableDomains : State.Error()
        }
    }

    operator fun invoke() = flow {
        authRepository.getAvailableDomains()
            .onFailure { message, _, _ ->
                emit(State.Error.Message(message))
            }
            .onSuccess {
                if (it.isEmpty()) {
                    emit(State.Error.NoAvailableDomains)
                } else {
                    emit(State.Success(it))
                }
            }
    }
}

fun Flow<AvailableDomains.State>.onSuccess(
    action: suspend (AvailableDomains.State.Success) -> Unit
) = onEachInstance(action) as Flow<AvailableDomains.State>

fun Flow<AvailableDomains.State>.onError(
    action: suspend (AvailableDomains.State.Error) -> Unit
) = onEachInstance(action) as Flow<AvailableDomains.State>
