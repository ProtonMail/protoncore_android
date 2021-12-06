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

package me.proton.core.user.domain.repository

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId

interface PassphraseRepository {
    /**
     * Set encrypted [passphrase] for a [userId].
     */
    suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray)

    /**
     * Get encrypted passphrase for a [userId], if exist, or `null` otherwise.
     */
    suspend fun getPassphrase(userId: UserId): EncryptedByteArray?

    /**
     * Clear passphrase for a [userId].
     */
    suspend fun clearPassphrase(userId: UserId)

    /**
     * Add a [listener] to [OnPassphraseChangedListener] list.
     */
    fun addOnPassphraseChangedListener(listener: OnPassphraseChangedListener)

    /**
     * Interface to implement to receive [onPassphraseChanged] event.
     */
    interface OnPassphraseChangedListener {

        /**
         * Called every time a passphrase change for a [userId].
         */
        suspend fun onPassphraseChanged(userId: UserId)
    }
}
