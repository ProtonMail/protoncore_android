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

package me.proton.core.auth.presentation.compose.sso

import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceSecretString

public sealed interface MemberApprovalOperation

public sealed interface MemberApprovalAction : MemberApprovalOperation {
    public data class Load(
        val background: Boolean = false,
        val unused: Long = System.currentTimeMillis()
    ) : MemberApprovalAction

    public data class SetInput(
        val deviceId: AuthDeviceId,
        val code: String
    ) : MemberApprovalAction

    public data class Confirm(
        val deviceId: AuthDeviceId,
        val deviceSecret: DeviceSecretString?,
        val unused: Long = System.currentTimeMillis()
    ) : MemberApprovalAction

    public data class Reject(
        val deviceId: AuthDeviceId,
        val unused: Long = System.currentTimeMillis()
    ) : MemberApprovalAction
}
