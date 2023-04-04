/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.crypto.android.srp

import me.proton.core.crypto.common.srp.SrpChallenge

/**
 * Implementation of [SrpChallenge] using the gopenpgp srp library.
 */

class GOpenPGPSrpChallenge : SrpChallenge {

    override fun argon2PreimageChallenge(challenge: String): String {
        return com.proton.gopenpgp.srp.Srp.argon2PreimageChallenge(challenge, -1)
    }

    override fun ecdlpChallenge(challenge: String): String {
        return com.proton.gopenpgp.srp.Srp.ecdlpChallenge(challenge, -1)
    }
}