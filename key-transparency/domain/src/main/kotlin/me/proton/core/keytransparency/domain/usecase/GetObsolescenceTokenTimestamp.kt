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

package me.proton.core.keytransparency.domain.usecase

import me.proton.core.keytransparency.domain.entity.ObsolescenceToken
import javax.inject.Inject

internal class GetObsolescenceTokenTimestamp @Inject constructor() {
    operator fun invoke(token: ObsolescenceToken): Long = with(token) {
        take(TIME_STAMP_HEX_LENGTH).toLong(HEX_RADIX)
    }
    companion object {
        private const val TIME_STAMP_HEX_LENGTH = 8 * 2
        private const val HEX_RADIX = 16
    }
}
