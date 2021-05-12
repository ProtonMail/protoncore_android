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

package me.proton.core.user.domain.repository

import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.UserVerificationTokenType
import me.proton.core.user.domain.entity.VerificationResult

interface UserValidationRepository {

    /**
     * Send the sms verification code to the API.
     *
     * @param phoneNumber or a phone number as a destination where the verification code should be send
     * if the verification type (method) selected is SMS.
     */
    suspend fun sendVerificationCodePhoneNumber(
        sessionId: SessionId?,
        phoneNumber: String,
        type: UserVerificationTokenType
    ): VerificationResult

    /**
     * Send the email address verification code to the API.
     *
     * @param emailAddress an email the destination where the verification code should be send
     * if the verification type (method) selected is Email.
     */
    suspend fun sendVerificationCodeEmailAddress(
        sessionId: SessionId?,
        emailAddress: String,
        type: UserVerificationTokenType
    ): VerificationResult

    /**
     * Check the token validity sent to an external email address.
     */
    suspend fun checkCreationTokenValidity(
        token: String,
        tokenType: String,
        type: Int
    ): VerificationResult
}
