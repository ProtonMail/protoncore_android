/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.data.repository

import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.data.api.request.EmailValidationRequest
import me.proton.core.auth.data.api.request.PhoneValidationRequest
import me.proton.core.auth.domain.repository.AuthSignupRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess

/**
 * Implementation of the [AuthSignupRepository].
 * Provides implementation of the all auth signup related API routes.
 */
class AuthSignupRepositoryImpl(
    private val provider: ApiProvider
) : AuthSignupRepository {

    override suspend fun validateEmail(email: String): Boolean =
        provider.get<AuthenticationApi>().invoke {
            val request = EmailValidationRequest(email)
            validateEmail(request).isSuccess()
        }.valueOrThrow

    override suspend fun validatePhone(phone: String): Boolean =
        provider.get<AuthenticationApi>().invoke {
            val request = PhoneValidationRequest(phone)
            validatePhone(request).isSuccess()
        }.valueOrThrow

}
