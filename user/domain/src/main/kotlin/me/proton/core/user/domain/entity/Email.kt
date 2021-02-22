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

package me.proton.core.user.domain.entity

/**
 * Pair of [username] and [domain].
 */
data class Email(
    val username: String,
    val domain: String
)

/**
 * Split a String around `@` to extract username and domain.
 */
private fun String.split(): Email = split("@").let { pair ->
    require(pair.size == 2) { "Email is not correctly using `@` delimiter." }
    require(pair[0].isNotBlank()) { "Username is blank." }
    require(pair[1].isNotBlank()) { "Domain is blank." }
    Email(username = pair[0], domain = pair[1])
}

/**
 * Split [User.email] into [Email] extracting username and domain.
 */
val User.emailSplit: Email? get() = email?.split()

/**
 * Split [UserAddress.email] into [Email] extracting username and domain.
 */
val UserAddress.emailSplit: Email get() = email.split()
