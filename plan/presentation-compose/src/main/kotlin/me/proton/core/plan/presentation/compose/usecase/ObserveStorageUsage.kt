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
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.getUsedBaseSpacePercentage
import me.proton.core.user.domain.extension.getUsedDriveSpacePercentage
import me.proton.core.user.domain.extension.getUsedTotalSpacePercentage
import javax.inject.Inject

/** Returns the current usage (percentage) for base (Mail), Drive and total.
 * Base and Drive storage are only returned for free accounts.
 */
public class ObserveStorageUsage @Inject constructor(
    private val userManager: UserManager
) {
    public operator fun invoke(userId: UserId): Flow<StorageUsage?> = userManager
        .observeUser(userId)
        .map { user ->
            if (user != null) {
                StorageUsage(
                    basePercentage = user.getUsedBaseSpacePercentage(),
                    drivePercentage = user.getUsedDriveSpacePercentage(),
                    totalPercentage = user.getUsedTotalSpacePercentage(),
                    userId = user.userId,
                )
            } else {
                null
            }
        }

    public data class StorageUsage(
        val basePercentage: Int?,
        val drivePercentage: Int?,
        val totalPercentage: Int,
        val userId: UserId
    )
}