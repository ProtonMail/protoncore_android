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
import me.proton.core.accountmanager.domain.entity.Account
import me.proton.core.accountmanager.domain.entity.AccountState
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

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
     * The [Account.state] will start from [AccountState.Added].
     *
     * Note: This function is usually used for importing accounts from a different storage or during migration.
     */
    abstract suspend fun addAccount(account: Account, session: Session): Account

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
    abstract fun getAccount(userId: UserId): Flow<Account>

    /**
     * Flow of all persisted [Account] on this device.
     */
    abstract fun getAccounts(): Flow<List<Account>>

    /**
     * Flow of [Account] where [Account.state] changed.
     *
     * Note: Initial/first state after subscribing is considered as changed.
     */
    abstract fun onAccountStateChanged(): Flow<Account>

    /**
     * Flow of [Account] where [Account.sessionState] changed.
     *
     * Note: Initial/first state after subscribing is considered as changed.
     */
    abstract fun onSessionStateChanged(): Flow<Account>

    /**
     * Return true if there is a workflow progressing.
     */
    abstract fun hasWorkflowProgressing(): Boolean

    /**
     * Stop the current [Account] or [Session] workflow if exist, and change the corresponding state to failed.
     */
    abstract fun stopCurrentWorkflow()

    /**
     * Get the current [UserId], if exist.
     *
     * The latest added [Account] will automatically be set as the current.
     *
     * @return the current UserId, as long as at least one [Account] exist.
     *
     * @see handleUserSwitch
     * @see addAccount
     */
    abstract suspend fun getCurrentUserId(): Flow<UserId?>

    /**
     * Switch to the given [UserId] as current, if exist.
     *
     * The previous [UserId] is saved and set back if the current [Account] is removed.
     *
     * @throws IllegalArgumentException if userId doesn't exist.
     *
     * @see getCurrentUserId
     * @see removeAccount
     */
    abstract suspend fun handleUserSwitch(userId: UserId)

    /**
     * Get current [HumanVerificationDetails] if exist, by sessionId.
     */
    abstract suspend fun getHumanVerificationDetails(sessionId: SessionId): HumanVerificationDetails?

    /**
     * Set current [HumanVerificationDetails], by sessionId.
     */
    abstract suspend fun setHumanVerificationDetails(sessionId: SessionId, details: HumanVerificationDetails?)
}
