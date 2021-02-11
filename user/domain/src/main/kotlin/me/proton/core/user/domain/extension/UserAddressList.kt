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

package me.proton.core.user.domain.extension

import me.proton.core.user.domain.entity.AddressType
import me.proton.core.user.domain.entity.UserAddress

/**
 * @return primary [UserAddress] (with lower [UserAddress.order]).
 */
fun List<UserAddress>.primary() = minByOrNull { it.order }

/**
 * @return [List] of [UserAddress] sorted by [UserAddress.order].
 */
fun List<UserAddress>.sorted() = sortedBy { it.order }

/**
 * @return original [UserAddress], or `null` otherwise.
 */
fun List<UserAddress>.originalOrNull() = firstOrNull { it.type == AddressType.Original }

/**
 * @return true if at least one [UserAddress] is migrated into the new key format, false otherwise.
 */
fun List<UserAddress>.hasMigratedKey() = any { it.keys.any { key -> key.token != null && key.signature != null } }
