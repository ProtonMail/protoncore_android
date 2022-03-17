/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.domain.usecase.signup

import me.proton.core.challenge.domain.ChallengeConfig

class SignupChallengeConfig : ChallengeConfig {

    override val flowName: String
        get() = "signup"
    override val flowFramesCount: Int
        get() = 2
    override val flowFrames: List<String>
        get() = listOf(SIGN_UP_FRAME_USERNAME, SIGN_UP_FRAME_RECOVERY)

    companion object {
        const val SIGN_UP_FRAME_USERNAME = "username"
        const val SIGN_UP_FRAME_RECOVERY = "recovery"
    }
}
