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

package me.proton.core.network.data.mapper

import me.proton.core.network.data.protonApi.Details
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods

/**
 * Convenient extensions for handling the optional and dynamic Details part of the [ApiResult.Error.ProtonData].
 *
 * @author Dino Kadrikj.
 */

fun Details.toHumanVerificationEntity(): HumanVerificationAvailableMethods =
    HumanVerificationAvailableMethods(
        verificationMethods = verificationMethods.orEmpty(),
        verificationToken = verificationToken
    )

fun ApiResult.Error.ProtonData.parseDetails(errorCode: Int, details: Details?): ApiResult.Error.ProtonData {
    when (errorCode) {
        ResponseCodes.HUMAN_VERIFICATION_REQUIRED -> {
            humanVerification = details?.toHumanVerificationEntity()
        }
    }
    return this
}
