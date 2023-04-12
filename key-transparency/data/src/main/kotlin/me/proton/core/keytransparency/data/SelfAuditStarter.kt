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

package me.proton.core.keytransparency.data

import androidx.lifecycle.Lifecycle
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountReady
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.keytransparency.domain.usecase.GetCurrentTime
import me.proton.core.presentation.app.AppLifecycleProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class SelfAuditStarter @Inject constructor(
    private val accountManager: AccountManager,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val getCurrentTime: GetCurrentTime,
    private val selfAuditWorkScheduler: SelfAuditWorker.Scheduler
) {

    private fun scheduleAuditForEachUser() {
        accountManager.observe(appLifecycleProvider.lifecycle, minActiveState = Lifecycle.State.CREATED)
            .onAccountReady {
                scheduleSelfAudit(it.userId)
            }
            .onAccountDisabled {
                cancelSelfAudit(it.userId)
            }
    }

    private suspend fun scheduleSelfAudit(userId: UserId) {
        val delay = getDelayForSelfAuditWorker(userId)
        selfAuditWorkScheduler.scheduleSelfAudit(userId, delay)
    }

    private fun cancelSelfAudit(userId: UserId) {
        selfAuditWorkScheduler.cancelSelfAudit(userId)
    }

    private suspend fun getDelayForSelfAuditWorker(userId: UserId): Long {
        val selfAuditTimestamp = keyTransparencyRepository.getTimestampOfSelfAudit(userId)
            ?: return 0
        val currentTime = getCurrentTime()
        val delay = Constants.KT_SELF_AUDIT_INTERVAL_SECONDS - (currentTime - selfAuditTimestamp)
        return delay.coerceAtLeast(0)
    }

    public fun start() {
        scheduleAuditForEachUser()
    }
}
