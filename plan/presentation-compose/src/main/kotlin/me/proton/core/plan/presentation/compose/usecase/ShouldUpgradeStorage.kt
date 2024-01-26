/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.plan.presentation.compose.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.IsSplitStorageEnabled
import me.proton.core.plan.domain.usecase.CanUpgradeFromMobile
import me.proton.core.plan.presentation.view.STORAGE_ERROR_THRESHOLD
import javax.inject.Inject

public class ShouldUpgradeStorage @Inject constructor(
    private val accountManager: AccountManager,
    private val canUpgradeFromMobile: CanUpgradeFromMobile,
    private val isSplitStorageEnabled: IsSplitStorageEnabled,
    private val observeStorageUsage: ObserveStorageUsage
) {
    /**
     * If user should upgrade the plan, the flow will return an object with current storage usage.
     * Otherwise (if user is already on a paid plan, or if storage is still available),
     * the flow will return `NoUpgrade`.
     */
    public operator fun invoke(): Flow<Result> = accountManager.getPrimaryUserId()
        .flatMapLatest { userId ->
            if (userId != null && isSplitStorageEnabled(userId) && canUpgradeFromMobile(userId)) {
                observeStorageUsage(userId)
            } else {
                flowOf(null)
            }
        }.mapLatest { storageUsage ->
            when {
                storageUsage?.basePercentage != null && storageUsage.basePercentage >= STORAGE_ERROR_THRESHOLD ->
                    Result.MailStorageUpgrade(storageUsage.basePercentage, storageUsage.userId)

                storageUsage?.drivePercentage != null && storageUsage.drivePercentage >= STORAGE_ERROR_THRESHOLD ->
                    Result.DriveStorageUpgrade(storageUsage.drivePercentage, storageUsage.userId)

                else -> Result.NoUpgrade
            }
        }.distinctUntilChanged()

    public sealed class Result {
        public object NoUpgrade : Result()
        public data class DriveStorageUpgrade(
            val storagePercentage: Int,
            val userId: UserId
        ) : Result()

        public data class MailStorageUpgrade(
            val storagePercentage: Int,
            val userId: UserId
        ) : Result()
    }
}
