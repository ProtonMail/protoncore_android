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

package me.proton.core.plan.presentation.usecase

import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.IsSplitStorageEnabled
import me.proton.core.plan.presentation.view.HUNDRED_PERCENT
import me.proton.core.plan.presentation.view.STORAGE_ERROR_THRESHOLD
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.hasSubscription
import me.proton.core.user.domain.usecase.GetUser
import javax.inject.Inject
import kotlin.math.round

class LoadStorageUsageState @Inject constructor(
    private val getUser: GetUser,
    private val isSplitStorageEnabled: IsSplitStorageEnabled
) {
    /** Returns appropriate state, if the storage of a user with a free account is full or nearly full.
     * For paid users, always returns `null` (even if their storage is full).
     */
    suspend operator fun invoke(userId: UserId, refresh: Boolean = false): StorageUsageState? =
        isSplitStorageEnabled(userId)
            .takeIf { it }
            ?.let { getUser(userId, refresh = refresh) }
            ?.takeIf { !it.hasSubscription() }
            ?.let { user ->
                listOfNotNull(
                    user.getUsedDriveSpacePercentage()?.let { Pair(Product.Drive, it) },
                    user.getUsedBaseSpacePercentage()?.let { Pair(Product.Mail, it) }
                )
            }?.firstNotNullOfOrNull { (product, percentage) ->
                when {
                    percentage == HUNDRED_PERCENT -> StorageUsageState.Full(product)
                    percentage >= STORAGE_ERROR_THRESHOLD -> StorageUsageState.NearlyFull(product)
                    else -> null
                }
            }

    private fun User.getUsedBaseSpacePercentage(): Double? =
        getUsedPercentage(usedBaseSpace, maxBaseSpace)

    private fun User.getUsedDriveSpacePercentage(): Double? =
        getUsedPercentage(usedDriveSpace, maxDriveSpace)

    private fun getUsedPercentage(used: Long?, max: Long?): Double? {
        if (used == null || max == null) return null
        return round(used.toDouble() / max * HUNDRED_PERCENT)
    }
}

sealed class StorageUsageState {
    data class NearlyFull(val product: Product) : StorageUsageState()
    data class Full(val product: Product) : StorageUsageState()
}
