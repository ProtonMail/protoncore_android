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

import me.proton.core.user.domain.entity.User

private const val MASK_MAIL = 1 // 0001
private const val MASK_VPN = 4 // 0100

private fun User.hasServiceFor(mask: Int): Boolean = mask.and(services) == mask
private fun User.hasSubscriptionFor(mask: Int): Boolean = mask.and(subscribed) == mask

fun User.hasServiceForMail(): Boolean = hasServiceFor(MASK_MAIL)
fun User.hasServiceForVpn(): Boolean = hasServiceFor(MASK_VPN)

fun User.hasSubscriptionForMail(): Boolean = hasSubscriptionFor(MASK_MAIL)
fun User.hasSubscriptionForVpn(): Boolean = hasSubscriptionFor(MASK_VPN)
