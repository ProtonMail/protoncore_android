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
import me.proton.core.user.domain.entity.hasNoKeys
import me.proton.core.user.domain.entity.isExternal
import me.proton.core.user.domain.entity.isInternal

/**
 * @return primary [UserAddress] (with lower [UserAddress.order]).
 */
fun List<UserAddress>.primary() = minByOrNull { it.order }

/**
 * @return [List] of [UserAddress] sorted by [UserAddress.order].
 */
fun List<UserAddress>.sorted() = sortedBy { it.order }

/**
 * @return first internal [UserAddress] from [List].
 */
fun List<UserAddress>.firstInternalOrNull() = filterInternal().firstOrNull()

/**
 * @return true if migrated/new key format must be generated, false otherwise.
 */
fun List<UserAddress>.generateNewKeyFormat() = all { it.keys.isEmpty() } || hasMigratedKey()

/**
 * @return true if at least one [UserAddress] is migrated into the new key format, false otherwise.
 */
fun List<UserAddress>.hasMigratedKey() = any { it.keys.any { key -> key.token != null && key.signature != null } }

/**
 * @return true if at least one internal [UserAddress] exist, and have at least 1 key.
 */
fun List<UserAddress>.hasInternalAddressKey() = firstInternalOrNull()?.keys?.isNotEmpty() ?: false

/**
 * @return true if at least one [AddressType.Original] address exist.
 */
fun List<UserAddress>.hasOriginalAddress() = any { it.type == AddressType.Original }

/**
 * @return A list of external addresses.
 */
fun List<UserAddress>.filterExternal(): List<UserAddress> = filter { it.isExternal() }

/**
 * @return A list of internal (non-external) addresses.
 */
fun List<UserAddress>.filterInternal(): List<UserAddress> = filter { it.isInternal() }

/**
 * @return True if any of the addresses has no keys.
 */
fun List<UserAddress>.hasMissingKeys(): Boolean = any { it.hasNoKeys() }

/**
 * @return Addresses that have no keys.
 */
fun List<UserAddress>.filterHasNoKeys(): List<UserAddress> = filter { it.hasNoKeys() }
