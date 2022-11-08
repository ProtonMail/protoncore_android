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

import me.proton.core.key.domain.entity.key.KeyFlags
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserAddressKeyFlags

fun UserAddressKeyFlags.canVerify() = this.and(KeyFlags.NotCompromised) == KeyFlags.NotCompromised
fun UserAddressKeyFlags.canEncrypt() = this.and(KeyFlags.NotObsolete) == KeyFlags.NotObsolete

fun UserAddressKey.canEncrypt(): Boolean = flags.canEncrypt()
fun UserAddressKey.canVerify(): Boolean = flags.canVerify()
