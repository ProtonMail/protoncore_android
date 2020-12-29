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

package me.proton.core.accountmanager.domain

import kotlinx.coroutines.flow.Flow
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.Session

abstract class AccountManager(
    /**
     * Define which [Product] this instance is used by.
     */
    protected val product: Product
) {
    /**
     * Add a new [Account] into [AccountManager].
     *
     * A valid [Session] must be provided, if not, use [startLoginWorkflow] instead.
     *
     * The [Account.state] will start from [AccountState.Ready].
     *
     * Note: This function is usually used for importing accounts from a different storage or during migration.
     *
     * @throws IllegalArgumentException if provided [Session] is not valid.
     */
    abstract suspend fun addAccount(account: Account, session: Session)

    /**
     * Remove an [Account] from [AccountManager], revoking existing [Session] if needed.
     *
     * Note: The [Account.state] will shortly be set to [AccountState.Removed], before actual deletion.
     */
    abstract suspend fun removeAccount(userId: UserId)

    /**
     * Disable an [Account], revoking existing [Session] if needed.
     *
     * Note: The [Account.state] will be set to [AccountState.Disabled].
     */
    abstract suspend fun disableAccount(userId: UserId)

    /**
     * Flow of persisted [Account] on this device, by userId.
     */
    abstract fun getAccount(userId: UserId): Flow<Account?>

    /**
     * Flow of all persisted [Account] on this device.
     */
    abstract fun getAccounts(): Flow<List<Account>>

    /**
     * Flow of all persisted [Session] on this device.
     */
    abstract fun getSessions(): Flow<List<Session>>

    /**
     * Flow of [Account] where [Account.state] changed.
     */
    abstract fun onAccountStateChanged(): Flow<Account>

    /**
     * Flow of [Account] where [Account.sessionState] changed.
     */
    abstract fun onSessionStateChanged(): Flow<Account>

    /**
     * Flow of [Account] where [Account.sessionState] changed to [SessionState.HumanVerificationNeeded].
     */
    abstract fun onHumanVerificationNeeded(): Flow<Pair<Account, HumanVerificationDetails?>>

    /**
     * Get the primary [UserId], if exist.
     *
     * The latest [AccountState.Ready] [Account] will automatically be set as the primary.
     *
     * @return the latest primary UserId, as long as at least one [AccountState.Ready] [Account] exist.
     *
     * @see setAsPrimary
     * @see addAccount
     */
    abstract fun getPrimaryUserId(): Flow<UserId?>

    /**
     * Set the given [UserId] as primary, if exist.
     *
     * @throws IllegalArgumentException if userId doesn't exist or [AccountState] is not [AccountState.Ready].
     *
     * @see getPrimaryUserId
     * @see removeAccount
     */
    abstract suspend fun setAsPrimary(userId: UserId)

    abstract fun isHumanVerificationBlockedPrimary(): Flow<Pair<Account, HumanVerificationDetails?>?>
}
