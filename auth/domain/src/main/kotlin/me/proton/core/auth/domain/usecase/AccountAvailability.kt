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

import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.runWithObservability
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Availability of accounts (username, internal username@domain, external email).
 */
class AccountAvailability @Inject constructor(
    private val userRepository: UserRepository,
    private val domainRepository: DomainRepository,
    private val observabilityManager: ObservabilityManager
) {
    /** Fetch the domains.
     * @param metricData Optionally, a function that produces [ObservabilityData]
     *  that will be [enqueued][ObservabilityManager.enqueue].
     */
    suspend fun getDomains(metricData: ((Result<List<Domain>>) -> ObservabilityData)? = null): List<Domain> {
        return domainRepository.runWithObservability(observabilityManager, metricData) {
            getAvailableDomains()
        }
    }

    suspend fun getUser(userId: UserId) = userRepository.getUser(userId)

    suspend fun checkUsername(
        userId: UserId,
        username: String,
        metricData: ((Result<Unit>) -> ObservabilityData)? = null
    ) {
        val user = userRepository.getUser(userId)
        if (user.name == username) return
        return checkUsername(username, metricData)
    }

    suspend fun checkUsername(
        username: String,
        metricData: ((Result<Unit>) -> ObservabilityData)? = null
    ) {
        check(username.isNotBlank()) { "Username must not be blank." }

        return userRepository.runWithObservability(observabilityManager, metricData) {
            checkUsernameAvailable(username)
        }
    }

    suspend fun checkExternalEmail(
        email: String,
        metricData: ((Result<Unit>) -> ObservabilityData)? = null
    ) {
        check(email.isNotBlank()) { "Email must not be blank." }

        userRepository.runWithObservability(observabilityManager, metricData) {
            checkExternalEmailAvailable(email)
        }
    }
}
