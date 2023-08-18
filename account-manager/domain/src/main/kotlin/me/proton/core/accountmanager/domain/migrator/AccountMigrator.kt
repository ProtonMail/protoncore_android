/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.accountmanager.domain.migrator

import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountMetadataDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId

/**
 * Migrate [Account] that are in [AccountState.MigrationNeeded].
 *
 * Note: Used only when pure DB migration is not possible.
 */
interface AccountMigrator {

    /**
     * Migrate an [Account] and change [AccountState] to
     * - [AccountState.Ready] on success or
     * - [AccountState.Disabled] on failure.
     *
     * Note: This will apply all pending migrations if possible.
     *
     * @see [AccountMetadataDetails.migrations]
     */
    suspend fun migrate(userId: UserId)

    enum class Migration {
        /**
         * Migrate UserKey and UserAddressKey to properly persist passphrase/isUnlockable.
         */
        DecryptPassphrase,

        /**
         * Fetch user data from backend.
         */
        RefreshUser,
    }
}
