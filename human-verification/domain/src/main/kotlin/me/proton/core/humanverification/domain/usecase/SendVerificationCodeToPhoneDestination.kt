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

package me.proton.core.humanverification.domain.usecase

import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

/**
 * Use case for sending the verification code to any destination chosen by the user, based on the
 * verification type (method), which can be captcha, email, sms etc..
 *
 * Depends on the [UserVerificationRepository] for contacting the API.
 *
 * @param userVerificationRepository the remote repository interface implementation.
 */
class SendVerificationCodeToPhoneDestination @Inject constructor(
    private val userVerificationRepository: UserVerificationRepository
) {

    /**
     * Sends the verification code (token) to the API.
     *
     * @param phoneNumber phone number (full, including the calling code), if the token type (verification
     * type/method) is SMS, this value should be populated.
     */
    suspend operator fun invoke(sessionId: SessionId?, phoneNumber: String) {
        require(phoneNumber.isNotEmpty()) { "Provide valid sms destination." }
        return userVerificationRepository.sendVerificationCodePhoneNumber(
            sessionId = sessionId,
            phoneNumber = phoneNumber,
            type = TokenType.SMS
        )
    }
}
