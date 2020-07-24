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

package me.proton.core.humanverification.domain.repository

import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.util.kotlin.Invokable

/**
 * Remote repository interface that defines all operations that the dta layer (module) should
 * implement. All of these operations are network (remote) dependant and often it is required
 * to have internet connection to execute successfully.
 *
 * It is required to run them only on a IO thread.
 *
 * @author Dino Kadrikj.
 */
interface HumanVerificationRemoteRepository : Invokable {

    /**
     * Send the sms verification code to the API.
     *
     * @param phoneNumber or a phone number as a destination where the verification code should be send
     * if the verification type (method) selected is SMS.
     */
    suspend fun sendVerificationCodePhoneNumber(phoneNumber: String): VerificationResult

    /**
     * Send the email address verification code to the API.
     *
     * @param emailAddress an email the destination where the verification code should be send
     * if the verification type (method) selected is Email.
     */
    suspend fun sendVerificationCodeEmailAddress(emailAddress: String): VerificationResult

    /**
     * Token is actually the code that has been sent to the user (or the captcha code) and that
     * needs verification.
     *
     * @param tokenType the selected verification type (method)
     * @param token the verification token previously sent to the preferred destination depending
     * on the verification method or the captcha token generated from the captcha webview.
     */
    suspend fun verifyCode(tokenType: String, token: String): VerificationResult
}
