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

import me.proton.core.user.domain.entity.UserAddressKey

private const val MASK_CAN_VERIFY_SIGNATURE = 1 // 01
private const val MASK_CAN_ENCRYPT_VALUE = 2 // 10

fun UserAddressKey.canEncrypt(): Boolean = flags.and(MASK_CAN_ENCRYPT_VALUE) == MASK_CAN_ENCRYPT_VALUE
fun UserAddressKey.canVerifySignature(): Boolean = flags.and(MASK_CAN_VERIFY_SIGNATURE) == MASK_CAN_VERIFY_SIGNATURE
