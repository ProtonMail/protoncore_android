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

package me.proton.core.contact.domain.entity

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.domain.type.IntEnum

sealed class ContactCard {
    data class ClearText(val data: String): ContactCard()
    data class Signed(val data: String, val signature: Signature): ContactCard()
    data class Encrypted(val data: Armored, val signature: Signature): ContactCard()
}

enum class ContactCardType(val value: Int) {
    ClearText(0),
    Signed(2),
    Encrypted(3);

    companion object {
        val map = values().associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}
