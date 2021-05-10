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
import me.proton.core.network.domain.handlers.HumanVerificationHandler
import me.proton.core.network.domain.humanverification.HumanVerificationApiDetails
import me.proton.core.network.domain.humanverification.VerificationMethod

/**
 * Convenient extensions for handling the optional and dynamic Details part of the [ApiResult.Error.ProtonData].
 *
 * @author Dino Kadrikj.
 */

fun Details.toHumanVerificationEntity(): HumanVerificationApiDetails =
// it is safe to use !! here and the responsibility is to the call-site developer to make sure it is calling
// this function at appropriate time, because together with error code 9001 API guarantees it will return at least
    // 1 verification method.
    HumanVerificationApiDetails(
        verificationMethods = verificationMethods!!.map {
            VerificationMethod.valueOf(it.name)
        },
        captchaVerificationToken = verificationToken
    )

fun ApiResult.Error.ProtonData.parseDetails(errorCode: Int, details: Details?): ApiResult.Error.ProtonData {
    when (errorCode) {
        HumanVerificationHandler.ERROR_CODE_HUMAN_VERIFICATION -> {
            humanVerification = details?.toHumanVerificationEntity()
        }
    }
    return this
}
