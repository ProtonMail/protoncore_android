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

package me.proton.core.humanverification.data.repository

import me.proton.core.humanverification.data.api.HumanVerificationApi
import me.proton.core.humanverification.data.entity.Destination
import me.proton.core.humanverification.data.entity.HumanVerificationBody
import me.proton.core.humanverification.data.entity.VerificationBody
import me.proton.core.humanverification.data.mapToVerificationResult
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.EmptyDestinationException
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId

/**
 * Implementation of the [HumanVerificationRemoteRepository].
 * Sends the verification code to the API. Supports only [TokenType.EMAIL] and [TokenType.SMS]
 *
 * @author Dino Kadrikj.
 */
class HumanVerificationRemoteRepositoryImpl(
    private val apiProvider: ApiProvider
) : HumanVerificationRemoteRepository {

    /**
     * Send the sms verification code to the API.
     *
     * @param phoneNumber or a phone number as a destination where the verification code should be send
     * if the verification type (method) selected is SMS.
     *
     * @throws EmptyDestinationException if the destination value is empty.
     */
    override suspend fun sendVerificationCodePhoneNumber(
        sessionId: SessionId,
        phoneNumber: String
    ): VerificationResult {
        if (phoneNumber.isEmpty()) {
            throw EmptyDestinationException("Provide valid sms destination.")
        }
        val destination = Destination(phoneNumber = phoneNumber)

        val verificationBody = VerificationBody(TokenType.SMS.tokenTypeValue, destination)

        val apiResult = apiProvider.get<HumanVerificationApi>(sessionId).invoke {
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
        sessionId: SessionId,
        emailAddress: String
    ): VerificationResult {
        if (emailAddress.isEmpty()) {
            throw EmptyDestinationException("Provide valid email destination.")
        }
        val destination = Destination(emailAddress = emailAddress)

        val verificationBody =
            VerificationBody(TokenType.EMAIL.tokenTypeValue, destination)

        val apiResult = apiProvider.get<HumanVerificationApi>(sessionId).invoke {
            sendVerificationCode(verificationBody)
        }

        return apiResult.mapToVerificationResult()
    }

    /**
     * Verifies the verification code (token) against the API.
     * Token is actually the code that has been sent to the user (or the captcha code) and that
     * needs verification.
     *
     * @param tokenType the token type [TokenType]
     * @param token the verification code (token) value
     */
    override suspend fun verifyCode(sessionId: SessionId, tokenType: String, token: String): VerificationResult {
        val apiResult = apiProvider.get<HumanVerificationApi>(sessionId).invoke {
            postHumanVerification(HumanVerificationBody(token, tokenType))
        }
        return apiResult.mapToVerificationResult()
    }
}
