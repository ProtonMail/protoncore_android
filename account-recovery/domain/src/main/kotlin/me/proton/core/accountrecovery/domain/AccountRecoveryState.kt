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

package me.proton.core.accountrecovery.domain

import me.proton.core.domain.type.IntEnum
import me.proton.core.user.domain.entity.UserRecovery

public enum class AccountRecoveryState {
    None,
    GracePeriod,
    ResetPassword,
    Cancelled,
    Expired
}

public fun IntEnum<UserRecovery.State>.toAccountRecoveryState(): AccountRecoveryState =
    when (this.enum) {
        UserRecovery.State.None -> AccountRecoveryState.None
        UserRecovery.State.Grace -> AccountRecoveryState.GracePeriod
        UserRecovery.State.Cancelled -> AccountRecoveryState.Cancelled
        UserRecovery.State.Insecure -> AccountRecoveryState.ResetPassword
        UserRecovery.State.Expired -> AccountRecoveryState.Expired
        null -> AccountRecoveryState.None
    }
