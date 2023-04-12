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

package me.proton.core.key.domain.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress

/**
 * Optional listener to bind the public address
 * repository to the key transparency module.
 * If provided, the listener is called when fetching
 * public address keys to check them against the KT log.
 */
interface PublicAddressVerifier {

    /**
     * Verify that the public address keys are correctly included
     * in the KT check.
     *
     * @param userId the id of the user.
     * @param address the public address that needs to be checked.
     */
    suspend fun verifyPublicAddress(userId: UserId, address: PublicAddress)

}
