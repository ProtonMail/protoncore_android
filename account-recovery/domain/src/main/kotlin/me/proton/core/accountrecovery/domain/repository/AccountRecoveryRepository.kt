/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.domain.repository

import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.Key

public interface AccountRecoveryRepository {

    /**
     * Start an account recovery process.
     */
    public suspend fun startRecovery(
        userId: UserId
    )

    /**
     * Cancels an existing account recovery attempt for a given [userId].
     * To cancel, we need to perform "inline re-authentication" of the user,
     * (i.e. provide [srpProofs] and [srpSession]),
     * so that we verify the user knows the password.
     */
    public suspend fun cancelRecoveryAttempt(
        srpProofs: SrpProofs,
        srpSession: String,
        userId: UserId
    )

    /**
     * Reset the password without using old password, during account recovery process.
     *
     * Note: Old data cannot be decrypted without old password. 2FA will be disabled.
     */
    public suspend fun resetPassword(
        sessionUserId: UserId,
        keySalt: String,
        userKeys: List<Key>?,
        auth: Auth?
    ): Boolean
}
