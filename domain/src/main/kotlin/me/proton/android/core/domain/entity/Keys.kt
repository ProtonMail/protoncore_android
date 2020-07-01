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

package me.proton.android.core.domain.entity

/**
 * Created by dinokadrikj on 4/22/20.
 * TODO: temporary for now, also needs probably parcelable
 */
data class Keys(
    var id: String? = null,
    var privateKey: String? = null,
    var flags: Int = 0,
    var primary: Int = 0,
    var token: String? = null,
    var signature: String? = null,
    var activation: String? = null
) {
    fun isPrimary() = primary == 1
}