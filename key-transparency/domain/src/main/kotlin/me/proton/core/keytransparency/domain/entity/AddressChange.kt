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

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.domain.entity.UserId

/**
 * When the client modifies the address keys (or create new ones),
 * the change needs to be recorded, so
 * that the inclusion of the changes can be checked later.
 *
 * @param userId: the id of the user recording the change
 * @param changeId: a random id to identify the change locally
 * @param counter: the address can have several changes recorded
 * @param email: the email of the changed address
 * @param epochId: the expected min epoch ID where the address will be included.
 * @param creationTimestamp: the timestamp of the signature of the new SKL to be included.
 * @param publicKeys: the list of public keys for the address at the time of the change, to later verify the SKL.
 * @param isObsolete: whether the address had an obsolescence token.
 */
public data class AddressChange constructor(
    val userId: UserId,
    val changeId: String,
    val counter: Int,
    val email: String,
    val epochId: EpochId,
    val creationTimestamp: Long,
    val publicKeys: List<Armored>,
    val isObsolete: Boolean
)
