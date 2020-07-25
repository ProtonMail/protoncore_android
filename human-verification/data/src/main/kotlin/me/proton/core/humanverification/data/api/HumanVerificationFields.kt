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
package me.proton.core.humanverification.data.api

/**
 * @author Dino Kadrikj.
 */
object HumanVerificationFields {

    // region local data
    const val COUNTRY_CODE = "country_code"
    const val COUNTRY = "country_en"
    const val PHONE = "phone_code"
    // endregion

    // region api
    const val API_PHONE = "Phone"
    const val API_ADDRESS = "Address"
    const val API_USERNAME = "Username"
    const val API_TYPE = "Type"
    const val API_DESTINATION = "Destination"
    const val API_TOKEN = "Token"
    const val API_TOKEN_TYPE = "TokenType"
    const val API_HUMAN_VERIFICATION_METHODS = "HumanVerificationMethods"
    const val API_HUMAN_VERIFICATION_TOKEN = "HumanVerificationToken"
    // endregion

    const val API_CODE = "Code"

}
