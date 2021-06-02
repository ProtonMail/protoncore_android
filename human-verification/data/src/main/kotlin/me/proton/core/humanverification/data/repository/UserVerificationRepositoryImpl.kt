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

package me.proton.core.humanverification.data.repository

import me.proton.core.humanverification.data.api.UserVerificationApi
import me.proton.core.humanverification.data.api.request.CreationTokenValidityRequest
import me.proton.core.humanverification.data.api.request.Destination
import me.proton.core.humanverification.data.api.request.VerificationRequest
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.SessionId

class UserVerificationRepositoryImpl(
    private val provider: ApiProvider,
    private val humanVerificationRepository: HumanVerificationRepository
) : UserVerificationRepository {

    override suspend fun sendVerificationCodePhoneNumber(
        sessionId: SessionId?,
        phoneNumber: String,
        type: TokenType
    ) {
        require(phoneNumber.isNotEmpty()) { "Invalid sms destination." }

        val destination = Destination(phoneNumber = phoneNumber)
        val verificationBody = VerificationRequest(type.value, destination)

        provider.get<UserVerificationApi>(sessionId).invoke {
            sendVerificationCode(verificationBody)
        }.valueOrThrow
    }

    override suspend fun sendVerificationCodeEmailAddress(
        sessionId: SessionId?,
        emailAddress: String,
        type: TokenType
    ) {
        require(emailAddress.isNotEmpty()) { "Invalid email destination." }

        val destination = Destination(emailAddress = emailAddress)
        val verificationBody = VerificationRequest(type.value, destination)

        provider.get<UserVerificationApi>(sessionId).invoke {
            sendVerificationCode(verificationBody)
        }.valueOrThrow
    }

    override suspend fun checkCreationTokenValidity(token: String, tokenType: String, type: Int) {
        val clientId = requireNotNull(provider.apiFactory.getClientId())

        provider.get<UserVerificationApi>().invoke {
            checkCreationTokenValidity(CreationTokenValidityRequest(token, tokenType, type))
        }.valueOrThrow

        humanVerificationRepository.insertHumanVerificationDetails(
            details = HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = listOf(VerificationMethod.EMAIL),
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = tokenType,
                tokenCode = token
            )
        )
    }
}
