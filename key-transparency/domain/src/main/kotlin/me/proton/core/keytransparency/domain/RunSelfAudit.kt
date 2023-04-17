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

package me.proton.core.keytransparency.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.keytransparency.domain.usecase.GetCurrentTime
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.LogKeyTransparency
import me.proton.core.keytransparency.domain.usecase.SelfAudit
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

public class RunSelfAudit @Inject internal constructor(
    private val userManager: UserManager,
    private val isKeyTransparencyEnabled: IsKeyTransparencyEnabled,
    private val selfAudit: SelfAudit,
    private val logKeyTransparency: LogKeyTransparency,
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val getCurrentTime: GetCurrentTime
) {

    public suspend operator fun invoke(userId: UserId, forceRefresh: Boolean = false) {
        if (isKeyTransparencyEnabled(userId)) {
            if (forceRefresh || selfAuditIsExpired(userId)) {
                val userAddresses = userManager.getAddresses(userId, refresh = true)
                KeyTransparencyLogger.d("Running self audit")
                val newAudit = selfAudit.invoke(userId, userAddresses)
                logKeyTransparency.logSelfAuditResult(newAudit)
                keyTransparencyRepository.storeSelfAuditResult(userId, newAudit)
            } else {
                KeyTransparencyLogger.d("Self audit skipped because it was run recently")
            }
        }
    }

    private suspend fun selfAuditIsExpired(userId: UserId): Boolean {
        val selfAuditTimestamp = keyTransparencyRepository.getTimestampOfSelfAudit(userId)
            ?: return true
        val currentTime = getCurrentTime()
        val boundary = currentTime - Constants.KT_SELF_AUDIT_INTERVAL_SECONDS
        return selfAuditTimestamp <= boundary
    }
}
