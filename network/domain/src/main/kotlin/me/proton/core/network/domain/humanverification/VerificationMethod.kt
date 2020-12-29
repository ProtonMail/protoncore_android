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

import me.proton.core.util.kotlin.equalsNoCase

/**
 * All possible verification methods enum.
 */
enum class VerificationMethod(val value: String) {
    PHONE("sms"), // the default one, should be always present
    EMAIL("email"),
    CAPTCHA("captcha"),
    PAYMENT("payment"),
    INVITE("invite"),
    COUPON("coupon");

    companion object {
        fun getByValue(value: String): VerificationMethod =
            values().find {
                value.equalsNoCase(it.value)
            } ?: PHONE
    }
}
