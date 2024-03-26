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

package me.proton.core.featureflag.data

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.featureflag.domain.FeatureFlagWorkerManager
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class FeatureFlagRefreshStarter @Inject constructor(
    private val workerManager: FeatureFlagWorkerManager,
    private val accountManager: AccountManager,
    private val scopeProvider: CoroutineScopeProvider,
) {

    public fun start(immediately: Boolean = false) {
        // For not logged in flows.
        workerManager.enqueueOneTime(userId = null)
        // For logged in flows.
        accountManager.onAccountStateChanged(initialState = true).onEach { account ->
            when (account.state) {
                AccountState.Ready -> workerManager.enqueuePeriodic(account.userId, immediately)
                else -> workerManager.cancel(account.userId)
            }
        }.launchIn(scopeProvider.GlobalDefaultSupervisedScope)
    }
}
