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

package me.proton.android.core.data.api.entity

import me.proton.android.core.data.api.PGP_BEGIN
import me.proton.android.core.data.api.PGP_END

/**
 * Common interface for models that have [accessToken]
 * @author
 * Davide Farella
 * Dino Kadrikj
 */
internal interface HasAccessToken {
    val accessToken: String?

    /**
     * Checks if the access
     */
    val isAccessTokenArmored: Boolean get() {
        val trimmed = accessToken?.trim() ?: return false
        return trimmed.startsWith(PGP_BEGIN) && trimmed.endsWith(PGP_END)
    }
}
