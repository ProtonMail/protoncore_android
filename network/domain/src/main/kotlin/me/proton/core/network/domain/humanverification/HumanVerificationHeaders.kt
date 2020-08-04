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

package me.proton.core.network.domain.humanverification

/**
 * Holds the values for Human Verification headers (verification type/method and the value).
 *
 * @param tokenType the verification method used to verify (ex. email, captcha..). Represents the
 * `x-pm-human-verification-token-type` header in the API requests.
 * @param tokenCode the verification code as a result from the verification process. Represents the
 * `x-pm-human-verification-token` header in the API requests.
 *
 * @author Dino Kadrikj.
 */
data class HumanVerificationHeaders(
    val tokenType: String,
    val tokenCode: String
)
