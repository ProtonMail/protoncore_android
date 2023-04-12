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

package me.proton.core.keytransparency.domain

public sealed class KeyTransparencyParameters(
    public val certificateDomain: String,
    public val vrfPublicKey: String
) {
    /**
     * The configuration for running KT in production environment
     */
    public object Production : KeyTransparencyParameters(
        certificateDomain = "keytransparency.ch",
        vrfPublicKey = "kKdLPTrZy5LmLE6cMqzDzD7/GZoyoKtHoFhywvFamcY="
    )

    /**
     * The configuration for running KT in dev environment
     */
    public object Dev : KeyTransparencyParameters(
        certificateDomain = "dev.proton.wtf",
        vrfPublicKey = "LXaI/rQp9xTxAvdYQSzUuBM3swcSJ3D2IK2eSsiYous="
    )
}


