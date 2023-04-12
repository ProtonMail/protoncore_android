/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.keytransparency.domain.entity

import me.proton.core.crypto.common.pgp.Signature

/**
 * The verified epoch acts as a checkpoint for self audit.
 * The client signs it and uploads it to the server, so that
 * it doesn't have to start from scratch at the next self audit.
 *
 * @param data: the serialized data of the verified epoch, see [VerifiedEpochData].
 * @param signature: the detached binary signature for the data, signed by the primary address key.
 */
public data class VerifiedEpoch(
    val data: String,
    val signature: Signature
) {
    internal fun getVerifiedEpoch() = VerifiedEpochData.fromJson(data)
}
