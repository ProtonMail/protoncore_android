/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.domain.entity

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.Signature

data class UnprivatizationInfo(
    val state: UnprivatizeState,
    val adminEmail: String,
    val orgKeyFingerprintSignature: Signature,
    val orgPublicKey: Armored
)

enum class UnprivatizeState(val value: Int) {
    Declined(0),
    Pending(1),
    Ready(2);

    companion object {
        val map = entries.associateBy { it.value }
    }
}
