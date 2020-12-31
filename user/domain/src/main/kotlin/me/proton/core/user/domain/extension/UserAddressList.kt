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

import me.proton.core.user.domain.entity.UserType
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
 * Checks if all addresses are of type [AddressType.External].
 */
fun List<UserAddress>.allExternal() = all { it.type == AddressType.External }

/**
 * Returns `true` if the account is of type [UserType.Username].
 */
fun List<UserAddress>.usernameOnly() = isEmpty()

/**
 * Client supplies the minimal [userType] it needs to operate. The result is if current account satisfies the
 * required account.
 */
fun List<UserAddress>.satisfiesUserType(userType: UserType): Boolean = when (userType) {
    // If client needs Username account, then it should be fine with any account type.
    UserType.Username -> true
    // If client needs External account, we return true only if current account is External or Internal.
    UserType.External -> !usernameOnly()
    // If client needs Internal only account to operate, we return true if current account is Internal only.
    UserType.Internal -> !usernameOnly() && !allExternal()
}

/**
 * Determines and returns current [UserType].
 */
fun List<UserAddress>.currentUserType(): UserType = when {
    usernameOnly() -> UserType.Username
    allExternal() -> UserType.External
    else -> UserType.Internal
}
