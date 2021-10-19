/*
 * Copyright (c) 2021 Proton Technologies AG
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

import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.User
import me.proton.core.util.kotlin.exhaustive

/**
 * @return true if the user have at least 1 key.
 */
fun User.hasKeys() = keys.isNotEmpty()

/**
 * @return true if the user have a username (not blank).
 */
fun User.hasUsername() = !name.isNullOrBlank()

/**
 * @return true if the user is private, whether the user controls their own keys or not.
 */
fun User.isPrivate() = private

/**
 * @return true if the user is a sub-user, part of an organization (admin or member).
 */
fun User.isSubUser() = when (role) {
    null -> false
    Role.NoOrganization -> false
    Role.OrganizationMember -> true
    Role.OrganizationAdmin -> true
}.exhaustive
