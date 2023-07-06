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

package me.proton.core.auth.presentation.observability

import me.proton.core.observability.domain.metrics.common.UnlockUserStatus
import me.proton.core.user.domain.UserManager

internal fun Result<*>.toUnlockUserStatus(): UnlockUserStatus = when {
    isSuccess -> (getOrNull() as? UserManager.UnlockResult)?.toUnlockUserStatus() ?: UnlockUserStatus.unknown
    else -> UnlockUserStatus.unknown
}

internal fun UserManager.UnlockResult.toUnlockUserStatus(): UnlockUserStatus = when (this) {
    UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey -> UnlockUserStatus.noKeySaltsForPrimaryKey
    UserManager.UnlockResult.Error.NoPrimaryKey -> UnlockUserStatus.noPrimaryKey
    UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase -> UnlockUserStatus.primaryKeyInvalidPassphrase
    UserManager.UnlockResult.Success -> UnlockUserStatus.success
}
