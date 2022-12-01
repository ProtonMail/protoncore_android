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

package me.proton.core.challenge.domain

import me.proton.core.domain.entity.Product
import me.proton.core.util.kotlin.exhaustive

public const val CHALLENGE_VERSION: String = "2.0.4"
private const val CHALLENGE_FRAME_SUFFIX = "challenge"

public fun Product.framePrefix(): String = when (this) {
    Product.Calendar -> "calendar-android-v4-$CHALLENGE_FRAME_SUFFIX"
    Product.Drive -> "drive-android-v4-$CHALLENGE_FRAME_SUFFIX"
    Product.Mail -> "mail-android-v4-$CHALLENGE_FRAME_SUFFIX"
    Product.Vpn -> "vpn-android-v4-$CHALLENGE_FRAME_SUFFIX"
}.exhaustive
