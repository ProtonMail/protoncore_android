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

package me.proton.core.user.data.repository

import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.HumanVerificationListener
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.api.request.CreationTokenValidityRequest
import me.proton.core.user.data.api.request.Destination
import me.proton.core.user.data.api.request.VerificationRequest
import me.proton.core.user.data.extension.mapToVerificationResult
import me.proton.core.user.domain.entity.UserVerificationTokenType
import me.proton.core.user.domain.entity.VerificationResult
import me.proton.core.user.domain.exception.EmptyDestinationException
import me.proton.core.user.domain.repository.UserValidationRepository

class UserValidationRepositoryImpl(
    private val provider: ApiProvider,
    private val humanVerificationListener: HumanVerificationListener
) : UserValidationRepository {

    /**
     * Send the sms verification code to the API.
     *
     * @param phoneNumber or a phone number as a destination where the verification code should be send
     * if the verification type (method) selected is SMS.
     *
     * @throws EmptyDestinationException if the destination value is empty.
     */
    override suspend fun sendVerificationCodePhoneNumber(
        sessionId: SessionId?,
        phoneNumber: String,
        type: UserVerificationTokenType
    ): VerificationResult {
        if (phoneNumber.isEmpty()) {
            throw EmptyDestinationException("Provide valid sms destination.")
        }
        val destination = Destination(phoneNumber = phoneNumber)

        val verificationBody = VerificationRequest(type.tokenTypeValue, destination)

        val apiResult = provider.get<UserApi>(sessionId).invoke {
            sendVerificationCode(verificationBody)
        }

        return apiResult.mapToVerificationResult()
    }

    /**
     * Send the email address verification code to the API.
     *
     * @param emailAddress an email the destination where the verification code should be send
     * if the verification type (method) selected is Email.
     *
     * @throws EmptyDestinationException if the destination value is empty.
     */
    override suspend fun sendVerificationCodeEmailAddress(
        sessionId: SessionId?,
        emailAddress: String,
        type: UserVerificationTokenType
    ): VerificationResult {
        if (emailAddress.isEmpty()) {
            throw EmptyDestinationException("Provide valid email destination.")
        }
        val destination = Destination(emailAddress = emailAddress)

        val verificationBody =
            VerificationRequest(type.tokenTypeValue, destination)

        val apiResult = provider.get<UserApi>(sessionId).invoke {
            sendVerificationCode(verificationBody)
        }

        return apiResult.mapToVerificationResult()
    }

    /**
     * Check the token validity sent to an external email address.
     */
    override suspend fun checkCreationTokenValidity(token: String, tokenType: String, type: Int): VerificationResult {
        val request = CreationTokenValidityRequest(token = token, tokenType = tokenType, type = type)
        val clientId = provider.apiFactory.getClientId()

        val result = provider.get<UserApi>().invoke {
            checkCreationTokenValidity(request)
        }.mapToVerificationResult()

        if (result is VerificationResult.Success) {
            humanVerificationListener.onExternalAccountHumanVerificationNeeded(
                clientId = clientId!!,
                details = HumanVerificationDetails(
                    clientId = clientId,
                    verificationMethods = listOf(VerificationMethod.EMAIL),
                    state = HumanVerificationState.HumanVerificationSuccess,
                    tokenType = tokenType,
                    tokenCode = token
                )
            )
        }

        return result
    }
}
