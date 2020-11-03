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

import kotlinx.coroutines.flow.flow
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.onFailure
import me.proton.core.domain.arch.onSuccess
import javax.inject.Inject

/**
 * Use case that gets all available domains on the API for the address creation purposes.
 *
 * @author Dino Kadrikj.
 */
class AvailableDomains @Inject constructor(private val authRepository: AuthRepository) {

    sealed class AvailableDomainsState {
        data class Success(val availableDomains: List<String>) : AvailableDomainsState() {
            val firstDomainOrDefault =
                if (availableDomains.isNotEmpty()) "@${availableDomains[0]}" else "@protonmail.com"
        }

        sealed class Error : AvailableDomainsState() {
            data class Message(val message: String?) : AvailableDomainsState.Error()
            object NoAvailableDomains : AvailableDomainsState.Error()
        }
    }

    operator fun invoke() = flow {
        authRepository.getAvailableDomains()
            .onFailure { message, _, _ ->
                emit(AvailableDomainsState.Error.Message(message))
            }
            .onSuccess {
                if (it.isEmpty()) {
                    emit(AvailableDomainsState.Error.NoAvailableDomains)
                } else {
                    emit(AvailableDomainsState.Success(it))
                }
            }
    }
}
