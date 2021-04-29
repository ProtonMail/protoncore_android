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
 * Holds the currently available verification methods and if among them is captcha, then the
 * captcha token also.
 */
data class HumanVerificationApiDetails(
    val verificationMethods: List<VerificationMethod>,
    val captchaVerificationToken: String? = null
)

enum class HumanVerificationState {
    /**
     * A human verification is needed.
     *
     * Note: Usually followed by either [HumanVerificationSuccess] or [HumanVerificationFailed].
     *
     * @see [HumanVerificationSuccess]
     * @see [HumanVerificationFailed].
     */
    HumanVerificationNeeded,

    /**
     * The human verification has been successful.
     *
     * Note: Usually followed by [Authenticated].
     */
    HumanVerificationSuccess,

    /**
     * The human verification has failed.
     *
     * Note: Client should consider calling [startHumanVerificationWorkflow].
     */
    HumanVerificationFailed,
}
