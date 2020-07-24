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

package me.proton.core.humanverification.domain.entity

/**
 * @author Dino Kadrikj.
 */
const val VERIFICATION_OPTION_CAPTCHA = "captcha"
const val VERIFICATION_OPTION_EMAIL = "email"
const val VERIFICATION_OPTION_SMS = "sms"
const val VERIFICATION_OPTION_PAYMENT = "payment"
const val VERIFICATION_OPTION_HAS_CODE = "hasCode" // internal

/**
 * Enumeration for all supported verification types.
 */
enum class TokenType(val tokenTypeValue: String) {
    SMS(VERIFICATION_OPTION_SMS),
    EMAIL(VERIFICATION_OPTION_EMAIL),
    CAPTCHA(VERIFICATION_OPTION_CAPTCHA),
    PAYMENT(VERIFICATION_OPTION_PAYMENT), // currently unused
    HAS_CODE(VERIFICATION_OPTION_HAS_CODE); // internal

    companion object {
        fun fromString(tokenTypeValue: String?): TokenType {
            return values().find {
                tokenTypeValue == it.tokenTypeValue
            } ?: SMS
        }
    }
}
